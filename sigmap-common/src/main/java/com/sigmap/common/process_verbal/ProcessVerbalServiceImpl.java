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
    public ProcessVerbal findByIds(Integer ppmId, Integer ppmVer)  {
        log.info("------- in serviceImpl");
        return repository.findTopByPpmIdAndPpmVersionAndIsDeletedFalseOrderByCreatedAtDesc(
                        ppmId, ppmVer)
                .orElseThrow(() -> new ProcessVerbalNotFound("Process Verbal not found for update", ppmId, ppmVer, ""));
    }
   /* public ProcessVerbal findByIds(Integer ppmId, Integer ppmVer, String processVerbalId) throws PpmNotFound{
        ProcessVerbal pv = repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(
                        ppmId, ppmVer, processVerbalId)
                .orElseThrow(() -> new ProcessVerbalNotFound("Process Verbal not found for update", ppmId, ppmVer, processVerbalId));
        Hibernate.initialize(pv.getMembres()); // Initialize if needed
        Hibernate.initialize(pv.getDocuments()); // Initialize if needed
        return pv;
    }*/
   @Transactional
   public ProcessVerbal update(ProcessVerbalRequest request) throws ProcessVerbalNotFound, DocNotFound, RefParamNotFound, IOException {

       // --- 1. Fetch the Existing Entity ---
       ProcessVerbal pvToUpdate = repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(
                       request.getPpmId(), request.getPpmVersion(), request.getProcessVerbalId())
               .orElseThrow(() -> new ProcessVerbalNotFound("Process Verbal not found for update", request.getPpmId(), request.getPpmVersion(), request.getProcessVerbalId()));

       // --- 2. Update Simple Fields ---
       pvToUpdate.setObservation(request.getObservation());
       pvToUpdate.setDecision(request.isDecision());
       pvToUpdate.setStatus(request.getStatus());
      // pvToUpdate.setDateDebutReunion(request.getDateDebutReunion());
       pvToUpdate.setTypeId(refParamService.findById(request.getTypeId()));
       repository.save(pvToUpdate); // Save changes to the main entity fields

       // --- 3. Handle Member Deletion (Soft Delete) ---
       List<Integer> memberIdsToKeep = request.getExistingMemberIds() != null ? request.getExistingMemberIds() : new ArrayList<>();
       // Fetch current members directly from the database to work with a separate, clean list
       List<PvMembrePresent> currentMembers = pvMembrePresentRepository.findByPpmIdAndPpmVersionAndProcessVerbalId(
               pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId());

       List<PvMembrePresent> membersToSoftDelete = currentMembers.stream()
               .filter(member -> !memberIdsToKeep.contains(member.getPvMembrePresentId()))
               .collect(Collectors.toList());

       if (!membersToSoftDelete.isEmpty()){
           log.info("Soft-deleting {} old members.", membersToSoftDelete.size());
           // Perform soft delete directly on the repository
           pvMembrePresentRepository.softDeleteAll(membersToSoftDelete);
       }

       // --- 4. Handle Document Deletion (Soft Delete Doc, Hard Delete Link) ---
       List<Integer> docIdsToKeep = request.getExistingDocIds() != null ? request.getExistingDocIds() : new ArrayList<>();
       List<PvDocs> currentDocLinks = pvDocsRepository.findByPpmIdAndPpmVersionAndProcessVerbalId(
               pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId());

       List<Integer> docIdsToDelete = currentDocLinks.stream()
               .map(PvDocs::getDocId)
               .filter(id -> !docIdsToKeep.contains(id))
               .collect(Collectors.toList());

       if (!docIdsToDelete.isEmpty()) {
           log.info("Soft deleting Docs with IDs: {} and removing links.", docIdsToDelete);
           docsRepository.softDeleteAllById(docIdsToDelete);

           List<PvDocs> linksToDelete = currentDocLinks.stream()
                   .filter(link -> docIdsToDelete.contains(link.getDocId()))
                   .collect(Collectors.toList());
           // Perform hard delete of the link record directly on the repository
           pvDocsRepository.deleteAll(linksToDelete);
       }

       // --- 5. Handle Member Updates and Additions ---
       if (request.getMembers() != null && !request.getMembers().isEmpty()) {
         //  RefParam fonction = refParamService.findById(request.getFuncationId());

           List<PvMembrePresent> membersAfterDeletion = pvMembrePresentRepository.findByPpmIdAndPpmVersionAndProcessVerbalId(pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId());
           Set<String> existingUserIds = membersAfterDeletion.stream().map(PvMembrePresent::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());

           for (PvMembrePresentRequest memberRequest : request.getMembers()) {
               if (memberRequest.getPvMembrePresentId() != null) {
                   // UPDATE EXISTING MEMBER
                   Optional<PvMembrePresent> memberOpt = pvMembrePresentRepository.findById(new PvMembrePresentId(pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId(), memberRequest.getPvMembrePresentId()));
                   if (memberOpt.isPresent()) {
                       PvMembrePresent memberToUpdate = memberOpt.get();
                       memberToUpdate.setPresent(memberRequest.isPresent());
                       pvMembrePresentRepository.save(memberToUpdate);
                   }
               } else {
                   // ADD NEW MEMBER, but check for duplicates first
                   boolean alreadyExists = memberRequest.getUserId() != null && existingUserIds.contains(memberRequest.getUserId());
                   if (!alreadyExists) {
                       PvMembrePresent newMember = new PvMembrePresent();
                       // ... set all fields from memberRequest ...
                       newMember.setPpmId(pvToUpdate.getPpmId());
                       newMember.setPpmVersion(pvToUpdate.getPpmVersion());
                       newMember.setProcessVerbalId(pvToUpdate.getProcessVerbalId());
                       newMember.setPvMembrePresentId(sequenceGeneratorService.getNextId(Constants.PV_MEMEBER_PRESENT));
                       newMember.setNom(memberRequest.getNom());
                       newMember.setPrenom(memberRequest.getPrenom());
                       newMember.setPresent(memberRequest.isPresent());
                       newMember.setUserId(memberRequest.getUserId());
                       newMember.setFoncation(Foncation.INVITE);
                       //newMember.setFonctionId(fonction);
                       pvMembrePresentRepository.save(newMember);
                   }
               }
           }
       }

       // --- 6. Handle Adding New Documents ---
       if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
           List<PvDocRequest> newDocumentRequests = request.getDocuments().stream()
                   .filter(docReq -> docReq.getDocId() == null)
                   .collect(Collectors.toList());
           if(!newDocumentRequests.isEmpty()){
               documentCommuneService.processAndSaveDocumentsForPv(newDocumentRequests, pvToUpdate.getPpm(),
                       Constants.PATH_ENTREPOT_GED_PPM, pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId());
           }
       }

       // --- 7. Return Final State ---
       // Re-fetch from the database to ensure the returned object is fully up-to-date
       return repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(
               pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId()
       ).orElseThrow(() -> new ProcessVerbalNotFound("Could not re-fetch updated Process Verbal.", pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId()));
   }

    }