package com.sigmap.common.process_verbal;

import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.Foncation;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal.exception.ProcessVerbalNotFound;
import com.sigmap.common.process_verbal.request.ProcessVerbalRequest;
import com.sigmap.common.pv_docs.PvDocs;
import com.sigmap.common.pv_docs.PvDocsRepository;
import com.sigmap.common.pv_docs.request.PvDocRequest;
import com.sigmap.common.pv_membre_present.PvMembrePresent;
import com.sigmap.common.pv_membre_present.PvMembrePresentId;
import com.sigmap.common.pv_membre_present.PvMembrePresentRepository;
import com.sigmap.common.pv_membre_present.request.PvMembrePresentRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.UserComRepository;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ProcessVerbalServiceImpl implements ProcessVerbalService {
    @PersistenceContext
    private EntityManager entityManager;
    private final ProcessVerbalRepository repository;
    private final PpmService ppmService;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final RefParamService refParamService;
    private final UserComRepository userComRepository;
    private final DocumentCommuneService documentCommuneService;
    private final KeycloakAdminService keycloakAdminService;
    private final DocsRepository docsRepository;
    private final PvMembrePresentRepository pvMembrePresentRepository;
    private final PvDocsRepository pvDocsRepository;

    public ProcessVerbalServiceImpl(ProcessVerbalRepository repository, PpmService ppmService, SequenceGeneratorService sequenceGeneratorService, RefParamService refParamService, UserComRepository userComRepository, DocumentCommuneService documentCommuneService, KeycloakAdminService keycloakAdminService, DocsRepository docsRepository, PvMembrePresentRepository pvMembrePresentRepository, PvDocsRepository pvDocsRepository) {
        this.repository = repository;
        this.ppmService = ppmService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.refParamService = refParamService;
        this.userComRepository = userComRepository;
        this.documentCommuneService = documentCommuneService;
        this.keycloakAdminService = keycloakAdminService;
        this.docsRepository = docsRepository;
        this.pvMembrePresentRepository = pvMembrePresentRepository;

        this.pvDocsRepository = pvDocsRepository;
    }

    @Override
    public ProcessVerbal save(ProcessVerbal processVerbal) {
        return repository.save(processVerbal);
    }

    @Override
    public ProcessVerbal create(ProcessVerbalRequest request) throws PpmNotFound, AcNotFound, ComNotFound, RefParamNotFound, IOException {
        RefParam typeId = refParamService.findById(request.getTypeId());
        Ppm ppm = ppmService.findByIdPpm(request.getPpmId(), request.getPpmVersion()).orElseThrow(() -> new PpmIdNotFound("", request.getPpmId()));
        UUID uuid = UUID.randomUUID();
        if (request.getMembers() == null || request.getMembers().isEmpty()) {
            request.setMembers(new ArrayList<>());
        }
        ProcessVerbal processVerbal = new ProcessVerbal();
        processVerbal.setMembres(new HashSet<>());
        log.info("memberr" + processVerbal.getMembres().size());
        log.info("memberr" + processVerbal.getMembres());
        String processVerbalId = "ppm" + "/" + ppm.getId() + "/" + ppm.getVersion() + "/" + uuid;
        List<UserCom> usersCom = userComRepository.findAllByComIdAndIsDeletedFalse(request.getComId());
        processVerbal.setProcessVerbalId(processVerbalId);
        processVerbal.setPpmId(ppm.getId());
        processVerbal.setPpmVersion(ppm.getVersion());
        if (request.getDocuments() != null) {
            documentCommuneService.processAndSaveDocumentsForPv(request.getDocuments(), ppm, Constants.PATH_ENTREPOT_GED_PPM, ppm.getId(), ppm.getVersion(), processVerbalId);
        }

        processVerbal.setObservation(request.getObservation());
        processVerbal.setDecision(request.isDecision());
        processVerbal.setTypeId(typeId);
        if (request.getStatus() != null) {
            processVerbal.setStatus(request.getStatus());

        }
        if (request.getDateDebutReunion() != null) {
            processVerbal.setDateDebutReunion(request.getDateDebutReunion());
        } else {
            processVerbal.setDateDebutReunion(LocalDateTime.now());
        }
        processVerbal.setPpm(ppm);
        processVerbal = repository.save(processVerbal);
        createAndSetMembersFromUsers(request, processVerbalId, usersCom);
        Set<PvMembrePresent> pvMembrePresent = processVerbal.getMembres();

        // add the invited user to member set
        if (pvMembrePresent.size() != request.getMembers().size()) {
            Set<PvMembrePresentRequest> invitedMemebers = request.getMembers().stream().filter(el -> !usersCom.contains(el)).collect(Collectors.toSet());
            for (PvMembrePresentRequest membrePresent : invitedMemebers) {
                PvMembrePresent newMember = new PvMembrePresent();
                newMember.setPpmId(ppm.getId());
                newMember.setPpmVersion(ppm.getVersion());
                newMember.setProcessVerbalId(processVerbalId);
                newMember.setPvMembrePresentId(sequenceGeneratorService.getNextId(Constants.PV_MEMEBER_PRESENT));
                newMember.setNom(membrePresent.getNom());
                newMember.setPrenom(membrePresent.getPrenom());
                newMember.setPresent(membrePresent.isPresent());
                //RefParam fonction = refParamService.findById(membrePresent.getFonctionId());
                //newMember.setFonctionId(fonction);
                if (membrePresent.getUserId() != null) {
                    newMember.setUserId(membrePresent.getUserId());
                }
                newMember.setFoncation(Foncation.INVITE);
                processVerbal.getMembres().add(newMember);


            }
        }
        repository.save(processVerbal);
        return processVerbal;
    }


    public void createAndSetMembersFromUsers(ProcessVerbalRequest request, String processVerbalId, List<UserCom> usersToSave) throws RefParamNotFound {
        ProcessVerbal processVerbal = repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(request.getPpmId(), request.getPpmVersion(), processVerbalId).orElseThrow(() -> new ProcessVerbalNotFound("", request.getPpmId(), request.getPpmVersion(), processVerbalId));
        Map<String, PvMembrePresentRequest> requestMemberStatusMap = request.getMembers().stream()
                .filter(member -> member.getUserId() != null)
                .collect(Collectors.toMap(PvMembrePresentRequest::getUserId, Function.identity()));

        for (UserCom user : usersToSave) {
            //Integer fonctionId;
            UserDTO userDTO = keycloakAdminService.getUserById(user.getUserId());
            PvMembrePresent newMember = new PvMembrePresent();
            newMember.setPpmId(request.getPpmId());
            newMember.setPpmVersion(request.getPpmVersion());
            newMember.setProcessVerbalId(processVerbalId);
            newMember.setPvMembrePresentId(sequenceGeneratorService.getNextId(Constants.PV_MEMEBER_PRESENT));
            newMember.setNom(userDTO.getFirstName());
            newMember.setPrenom(userDTO.getLastName());
            if (user.getUserId() != null) {
                newMember.setUserId(user.getUserId());
            }
            newMember.setPresent(processVerbal.getMembres().contains(newMember));
            if (user.isResponsable()) {
                //fonctionId = 1;
                newMember.setFoncation(Foncation.RESPONSABLE);
            } else {
                //fonctionId = 2;
                newMember.setFoncation(Foncation.MEMEBRE);
            }
            //RefParam fonction = refParamService.findById(fonctionId);
            // newMember.setFonctionId(fonction);
            PvMembrePresentRequest attendanceData = requestMemberStatusMap.get(user.getUserId());
            boolean wasPresent = (attendanceData != null && attendanceData.isPresent());
            newMember.setPresent(wasPresent);
            processVerbal.getMembres().add(newMember);
        }
        repository.save(processVerbal);
    }

    @Override
    @Transactional
    public ProcessVerbal findByIds(Integer ppmId, Integer ppmVer) {
        log.info("------- in serviceImpl");
        return repository.findTopByPpmIdAndPpmVersionAndIsDeletedFalseOrderByCreatedAtDesc(
                        ppmId, ppmVer)
                .orElseThrow(() -> new ProcessVerbalNotFound("Process Verbal not found for update", ppmId, ppmVer, ""));
    }


    @Transactional
    public ProcessVerbal update(ProcessVerbalRequest request) throws ProcessVerbalNotFound, DocNotFound, RefParamNotFound, IOException {

        // --- 1. Fetch the Existing ProcessVerbal to Update ---
        log.info("Starting update for ProcessVerbal with ID: {}", request.getProcessVerbalId());
        ProcessVerbal pvToUpdate = repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(
                        request.getPpmId(), request.getPpmVersion(), request.getProcessVerbalId())
                .orElseThrow(() -> new ProcessVerbalNotFound("Process Verbal not found for update", request.getPpmId(), request.getPpmVersion(), request.getProcessVerbalId()));

        // --- 2. Update Simple Fields on the Main Entity ---
        pvToUpdate.setObservation(request.getObservation());
        pvToUpdate.setDecision(request.isDecision());
        pvToUpdate.setStatus(request.getStatus());
        if (request.getTypeId() != null) {
            pvToUpdate.setTypeId(refParamService.findById(request.getTypeId()));
        }
        // --- 3. Synchronize Members (Delete, Update, Add) ---
        synchronizeMembers(request, pvToUpdate);

        // --- 4. Synchronize Documents (Delete, Add) ---
        synchronizeDocuments(request, pvToUpdate);

        // --- 5. Save and Return the Final, Consistent State ---
        repository.save(pvToUpdate);
        log.info("Successfully updated ProcessVerbal with ID: {}", pvToUpdate.getProcessVerbalId());

        // Re-fetch to ensure the returned object is the exact representation from the DB
        return repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId())
                .orElseThrow(() -> new ProcessVerbalNotFound("Could not re-fetch updated Process Verbal.", pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId()));
    }


    /**
     * Handles the logic for deleting, updating, and adding members associated with a ProcessVerbal.
     */
    private void synchronizeMembers(ProcessVerbalRequest request, ProcessVerbal pvToUpdate) throws RefParamNotFound {
        // Fetch the current state of members from the database ONCE.
        List<PvMembrePresent> currentMembers = pvMembrePresentRepository.findByPpmIdAndPpmVersionAndProcessVerbalId(
                pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId());

        // For efficient lookup, put current members into a Map by their ID.
        Map<Integer, PvMembrePresent> currentMembersMap = currentMembers.stream()
                .collect(Collectors.toMap(PvMembrePresent::getPvMembrePresentId, member -> member));

        List<Integer> memberIdsToKeep = request.getExistingMemberIds() != null ? request.getExistingMemberIds() : Collections.emptyList();

        // === 3a. Handle Member Deletion ===
        List<PvMembrePresent> membersToDelete = new ArrayList<>();
        for (PvMembrePresent member : currentMembers) {
            if (!memberIdsToKeep.contains(member.getPvMembrePresentId())) {
                membersToDelete.add(member);
            }
        }

        if (!membersToDelete.isEmpty()) {
            log.info("Soft-deleting {} members.", membersToDelete.size());
            pvMembrePresentRepository.softDeleteAll(membersToDelete); // Assumes custom soft-delete method
        }

        // === 3b. Handle Member Updates and Additions ===
        if (request.getMembers() != null && !request.getMembers().isEmpty()) {
            Set<String> existingUserIds = currentMembers.stream()
                    .map(PvMembrePresent::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (PvMembrePresentRequest memberRequest : request.getMembers()) {
                if (memberRequest.getPvMembrePresentId() != null) {
                    // UPDATE existing member
                    PvMembrePresent memberToUpdate = currentMembersMap.get(memberRequest.getPvMembrePresentId());
                    if (memberToUpdate != null) {
                        log.debug("Updating member with ID: {}", memberToUpdate.getPvMembrePresentId());
                        memberToUpdate.setPresent(memberRequest.isPresent());
                        // Update any other fields from the request here
                        pvMembrePresentRepository.save(memberToUpdate);
                    }
                } else {
                    // ADD new member, checking for duplicates by userId
                    boolean alreadyExists = memberRequest.getUserId() != null && existingUserIds.contains(memberRequest.getUserId());
                    if (!alreadyExists) {
                        log.debug("Adding new member with name: {} {}", memberRequest.getPrenom(), memberRequest.getNom());
                        PvMembrePresent newMember = new PvMembrePresent();
                        newMember.setPpmId(pvToUpdate.getPpmId());
                        newMember.setPpmVersion(pvToUpdate.getPpmVersion());
                        newMember.setProcessVerbalId(pvToUpdate.getProcessVerbalId());
                        newMember.setPvMembrePresentId(sequenceGeneratorService.getNextId(Constants.PV_MEMEBER_PRESENT));
                        newMember.setNom(memberRequest.getNom());
                        newMember.setPrenom(memberRequest.getPrenom());
                        newMember.setPresent(memberRequest.isPresent());
                        newMember.setUserId(memberRequest.getUserId());
                        newMember.setFoncation(Foncation.INVITE); // Or get from request if needed

                        pvMembrePresentRepository.save(newMember);
                    } else {
                        log.warn("Skipping add for member with userId: {}. Already exists.", memberRequest.getUserId());
                    }
                }
            }
        }
    }

    /**
     * Handles the logic for deleting and adding documents associated with a ProcessVerbal.
     */
    private void synchronizeDocuments(ProcessVerbalRequest request, ProcessVerbal pvToUpdate) throws IOException, DocNotFound {
        // Fetch the IDs of documents currently linked to the ProcessVerbal.
        List<Integer> currentDocIds = pvDocsRepository.findDocIdsByProcessVerbal(pvToUpdate);
        List<Integer> docIdsToKeep = request.getExistingDocIds() != null ? request.getExistingDocIds() : Collections.emptyList();

        // === 4a. Handle Document Deletion ===
        List<Integer> docIdsToDelete = currentDocIds.stream()
                .filter(id -> !docIdsToKeep.contains(id))
                .collect(Collectors.toList());

        if (!docIdsToDelete.isEmpty()) {
            log.info("Found {} documents to remove.", docIdsToDelete.size());

            // Step 1: Soft-delete the actual documents in the 'docs' table
            docsRepository.softDeleteAllById(docIdsToDelete); // Assumes custom method `softDeleteAllById(List<Integer> ids)`
            log.info("Soft-deleted Docs with IDs: {}", docIdsToDelete);

            // Step 2: Hard-delete the link records from the 'pv_docs' join table
            pvDocsRepository.deleteByDocIdIn(docIdsToDelete); // Assumes custom method `deleteByDocIdIn(List<Integer> docIds)`
            log.info("Hard-deleted PvDocs links for Doc IDs: {}", docIdsToDelete);
        }

        // === 4b. Handle Adding New Documents ===
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {

            if (!request.getDocuments().isEmpty()) {
                documentCommuneService.processAndSaveDocumentsForPv(request.getDocuments(), pvToUpdate.getPpm(),
                        Constants.PATH_ENTREPOT_GED_PPM, pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId());
            }
        }
    }
}