package com.sigmap.common.process_verbal;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.Docs;
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
import com.sigmap.common.process_verbal.request.PvRequest;
import com.sigmap.common.pv_docs.PvDocs;
import com.sigmap.common.pv_docs.PvDocsRepository;
import com.sigmap.common.pv_docs.request.PvDocRequest;
import com.sigmap.common.pv_membre_present.PvMembrePresent;
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
import org.apache.tika.Tika;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hibernate.cache.spi.support.SimpleTimestamper.next;

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
    private final AlfrescoService alfrescoService;
    private final UserComRepository comRepository;

    public ProcessVerbalServiceImpl(ProcessVerbalRepository repository, PpmService ppmService, SequenceGeneratorService sequenceGeneratorService, RefParamService refParamService, UserComRepository userComRepository, DocumentCommuneService documentCommuneService, KeycloakAdminService keycloakAdminService, DocsRepository docsRepository, PvMembrePresentRepository pvMembrePresentRepository, PvDocsRepository pvDocsRepository, AlfrescoService alfrescoService, UserComRepository comRepository) {
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
        this.alfrescoService = alfrescoService;
        this.comRepository = comRepository;
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
        if (request.getTaskId() != null) {
            processVerbal.setTaskId(request.getTaskId());
        }
        if (request.getProcessInstanceId() != null) {
            processVerbal.setProcessInstanceId(request.getProcessInstanceId());
        }
        if (request.getDocuments() != null) {
            documentCommuneService.processAndSaveDocumentsForPv(request.getDocuments(), Constants.PATH_ENTREPOT_GED_PPM, processVerbal);
        }

       /* processVerbal.setObservation(request.getObservation());
        processVerbal.setDecision(request.isDecision());*/
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
                if(membrePresent.getUserId()!=null){
                   next();
                }else {
                    PvMembrePresent newMember = new PvMembrePresent();
                    newMember.setPpmId(ppm.getId());
                    newMember.setPpmVersion(ppm.getVersion());
                    newMember.setProcessVerbalId(processVerbalId);
                    newMember.setPvMembrePresentId(sequenceGeneratorService.getNextId(Constants.PV_MEMEBER_PRESENT));
                    newMember.setNom(membrePresent.getNom());
                    newMember.setPrenom(membrePresent.getPrenom());
                    newMember.setPresent(membrePresent.isPresent());
                    newMember.setFoncation(Foncation.INVITE);
                    processVerbal.getMembres().add(newMember);
                }
            }
        }
        return repository.save(processVerbal);
    }

public void createAndSetMembersFromUsers(ProcessVerbalRequest request, String processVerbalId, List<UserCom> usersToSave) throws RefParamNotFound {
    ProcessVerbal processVerbal = repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(
                    request.getPpmId(), request.getPpmVersion(), processVerbalId)
            .orElseThrow(() -> new ProcessVerbalNotFound("", request.getPpmId(), request.getPpmVersion(), processVerbalId));
    Map<String, PvMembrePresentRequest> memberRequestMap = request.getMembers().stream()
            .filter(member -> member.getUserId() != null)
            .collect(Collectors.toMap(PvMembrePresentRequest::getUserId, Function.identity()));
    for (UserCom user : usersToSave) {
        UserDTO userDTO = keycloakAdminService.getUserById(user.getUserId());
        PvMembrePresent newMember = new PvMembrePresent();
        newMember.setPpmId(request.getPpmId());
        newMember.setPpmVersion(request.getPpmVersion());
        newMember.setProcessVerbalId(processVerbalId);
        newMember.setPvMembrePresentId(sequenceGeneratorService.getNextId(Constants.PV_MEMEBER_PRESENT));
        newMember.setNom(userDTO.getFirstName());
        newMember.setPrenom(userDTO.getLastName());
        newMember.setUserId(user.getUserId());
        PvMembrePresentRequest memberDataFromRequest = memberRequestMap.get(user.getUserId());
        boolean wasPresent = (memberDataFromRequest != null && memberDataFromRequest.isPresent());
        newMember.setPresent(wasPresent);
        if (user.isResponsable()) {
            newMember.setFoncation(Foncation.RESPONSABLE);
        } else {
            newMember.setFoncation(Foncation.MEMEBRE);
        }
        processVerbal.getMembres().add(newMember);
    }
    repository.save(processVerbal);
}

    @Override
    @Transactional
    public ProcessVerbal findByIds(Integer ppmId, Integer ppmVer) {
        ProcessVerbal response = repository.findTopByPpmIdAndPpmVersionAndIsDeletedFalseOrderByCreatedAtDesc(
                        ppmId, ppmVer)
                .orElseThrow(() -> new ProcessVerbalNotFound("Process Verbal not found for update", ppmId, ppmVer, ""));
        Set<PvMembrePresent> membres = response.getMembres().stream()
                .filter(el -> !el.getIsDeleted())
                .collect(Collectors.toSet());

        Set<PvDocs> docs = response.getDocuments().stream()
                .filter(el -> !el.getDoc().getIsDeleted())
                .collect(Collectors.toSet());

        response.setMembres(membres);
        response.setDocuments(docs);
        return response;
    }


    @Transactional
    public ProcessVerbal update(ProcessVerbalRequest request) throws ProcessVerbalNotFound, DocNotFound, RefParamNotFound, IOException {

        // --- 1. Fetch the Existing ProcessVerbal to Update ---
        log.info("Starting update for ProcessVerbal with ID: {}", request.getProcessVerbalId());
        ProcessVerbal pvToUpdate = repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(
                        request.getPpmId(), request.getPpmVersion(), request.getProcessVerbalId())
                .orElseThrow(() -> new ProcessVerbalNotFound("Process Verbal not found for update", request.getPpmId(), request.getPpmVersion(), request.getProcessVerbalId()));

        // --- 2. Update Simple Fields on the Main Entity ---
      /*  pvToUpdate.setObservation(request.getObservation());
        pvToUpdate.setDecision(request.isDecision());*/
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
        ProcessVerbal response = repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId())
                .orElseThrow(() -> new ProcessVerbalNotFound("Could not re-fetch updated Process Verbal.", pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId()));

        Set<PvMembrePresent> membres = pvMembrePresentRepository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedIsFalse(request.getPpmId(), request.getPpmVersion(),request.getProcessVerbalId()).stream()
                .filter(el -> !el.getIsDeleted())
                .collect(Collectors.toSet());

        Set<PvDocs> docs = pvToUpdate.getDocuments().stream()
                .filter(el -> !el.getDoc().getIsDeleted())
                .collect(Collectors.toSet());

        pvToUpdate.setMembres(membres);
        pvToUpdate.setDocuments(docs);
        return pvToUpdate;
    }

    @Override
    public ProcessVerbal findByIdsAndTaskIdAndProcessInstanceId(Integer ppmId, Integer ppmVer, String taskId, String processInstanceId) {
        ProcessVerbal response = repository.findTopByPpmIdAndPpmVersionAndTaskIdAndProcessInstanceIdAndIsDeletedFalse(
                        ppmId, ppmVer, taskId, processInstanceId)
                .orElseThrow(() -> new ProcessVerbalNotFound("Process Verbal not found for update", ppmId, ppmVer, ""));
        Set<PvMembrePresent> membres = response.getMembres().stream()
                .filter(el -> !el.getIsDeleted())
                .collect(Collectors.toSet());

        Set<PvDocs> docs = response.getDocuments().stream()
                .filter(el -> !el.getDoc().getIsDeleted())
                .collect(Collectors.toSet());

        response.setMembres(membres);
        response.setDocuments(docs);
        return response;
    }

    @Override
    @Transactional // Ensures all database operations in this method are atomic
    public ProcessVerbal addNewPv(PvRequest request) throws ProcessVerbalNotFound, IOException {

        // Fetch the existing ProcessVerbal. This will throw ProcessVerbalNotFound if not found.
        ProcessVerbal processVerbal = findByIdsAndTaskIdAndProcessInstanceId(
                request.getPpmId(),
                request.getPpmVersion(),
                request.getTaskId(),
                request.getProcessInstanceId()
        );

        // No need for a null check on processVerbal if the find... method throws an exception.

        if (request.getDocument() != null) {
            String baseFolderPath = String.format(Constants.PATH_ENTREPOT_GED_PPM + "/" + processVerbal.getPpmId() + "/" + processVerbal.getPpmVersion() + Constants.PATH_ENTREPOT_GED_PV);

            try {
                // Initialize the set if it's null
                if (processVerbal.getDocuments() == null) {
                    processVerbal.setDocuments(new HashSet<>());
                }
                UUID uuid = UUID.randomUUID();
                Tika tika = new Tika();
                String detectedMimeType = tika.detect(request.getDocument().getInputStream());
                String originalFilename = request.getDocument().getOriginalFilename() != null ? request.getDocument().getOriginalFilename() : "document";

                String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
                if (fileExtension.isEmpty()) {
                    // Fallback to original filename extension if MIME type detection fails
                    fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
                }
                String[] parts = originalFilename.split("\\.");
                String baseName = parts[0];
                String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                String uniqueFileName = "ppm_pv" + "_" + uuid + "_" + extension;
                MultipartFile renamedFile = new RenamedMultipartFile(request.getDocument(), uniqueFileName);
                long fileSize = request.getDocument().getSize();
                String formattedSize = documentCommuneService.formatSize(fileSize);

                String alfrescoId = alfrescoService.uploadDocument(renamedFile, baseFolderPath);
                log.info("Uploaded document to Alfresco: AlfrescoID={}, FileName={}, Path={}", alfrescoId, uniqueFileName, baseFolderPath);

                //2-remplissage mte3 document with data
                Docs document = new Docs();
                document.setName(baseName);
                document.setAlfresco_id(alfrescoId);
                document.setPathFolder(baseFolderPath);
                document.setFileSize(formattedSize);
                document.setContentType(detectedMimeType);
                document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
                document.setAlfrescoName(uniqueFileName);
                // Call the injected service instance, not a static method
                Docs pv = documentCommuneService.mapperDocument(request.getDocument(), "ppm_pv", baseFolderPath);
                // Save the main document entity first
                Docs savedDoc = docsRepository.save(document);

                // Create the linking entity
                PvDocs pvDocs = new PvDocs();
                pvDocs.setDocId(savedDoc.getId());
                pvDocs.setPv(true);
                pvDocs.setProcessVerbalId(processVerbal.getProcessVerbalId());
                pvDocs.setPpmId(processVerbal.getPpmId());
                pvDocs.setPpmVersion(processVerbal.getPpmVersion());
                pvDocs.setDoc(savedDoc); // Associate the saved document

                // Add the new document link to the collection
                processVerbal.getDocuments().add(pvDocs);

                // Save the parent entity to persist the new relationship
                ProcessVerbal updatedProcessVerbal = repository.save(processVerbal);

                // Return the updated object
                return updatedProcessVerbal;

            } catch (Exception e) {
                // Log the error for debugging
                // logger.error("Failed to process or upload document", e);
                // Re-throw as IOException or a more specific custom exception for upstream handling
                throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
            }
        }

        // If there was no document in the request, return the original object
        return processVerbal;
    }


    /**
     * Handles the logic for deleting, updating, and adding members associated with a ProcessVerbal.
     */

    private void synchronizeMembers(ProcessVerbalRequest request, ProcessVerbal pvToUpdate) {
        log.info("Starting member synchronization for ProcessVerbal ID: {}", pvToUpdate.getProcessVerbalId());

        // Get all valid users for the commission for validation.
        Set<String> validCommissionUserIds = userComRepository.findAllByComIdAndIsDeletedFalse(request.getComId())
                .stream()
                .map(UserCom::getUserId)
                .collect(Collectors.toSet());

        // Get the current members from the database for comparison.
        Map<Integer, PvMembrePresent> currentMembersMap = pvMembrePresentRepository
                .findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedIsFalse(
                        pvToUpdate.getPpmId(), pvToUpdate.getPpmVersion(), pvToUpdate.getProcessVerbalId())
                .stream()
                .collect(Collectors.toMap(PvMembrePresent::getPvMembrePresentId, Function.identity()));

        // Get userIds of members already in the meeting to prevent adding duplicates.
        Set<String> existingMemberUserIds = currentMembersMap.values().stream()
                .map(PvMembrePresent::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<PvMembrePresent> membersToSave = new ArrayList<>();
        List<PvMembrePresentRequest> membersInRequest = request.getMembers() != null ? request.getMembers() : Collections.emptyList();
        Set<Integer> memberIdsToKeep = new HashSet<>();



        for (PvMembrePresentRequest memberRequest : membersInRequest) {
            if (memberRequest.getPvMembrePresentId() != null) {
                //  UPDATE an existing member ---
                PvMembrePresent memberToUpdate = currentMembersMap.get(memberRequest.getPvMembrePresentId());
                if (memberToUpdate != null) {
                    memberToUpdate.setPresent(memberRequest.isPresent());
                    membersToSave.add(memberToUpdate);
                    memberIdsToKeep.add(memberToUpdate.getPvMembrePresentId());
                }
            } else {
                // Check if user is already in the meeting to avoid duplicates
                if (memberRequest.getUserId() != null && existingMemberUserIds.contains(memberRequest.getUserId())) {
                    log.warn("Skipping add for existing user ID: {}", memberRequest.getUserId());
                    continue;
                }

                PvMembrePresent newMember = new PvMembrePresent();
                newMember.setPpmId(pvToUpdate.getPpmId());
                newMember.setPpmVersion(pvToUpdate.getPpmVersion());
                newMember.setProcessVerbalId(pvToUpdate.getProcessVerbalId());
                newMember.setPvMembrePresentId(sequenceGeneratorService.getNextId(Constants.PV_MEMEBER_PRESENT));
                newMember.setNom(memberRequest.getNom());
                newMember.setPrenom(memberRequest.getPrenom());
                newMember.setPresent(memberRequest.isPresent());
                newMember.setUserId(memberRequest.getUserId());

                // Check if the new member is a Commission Member or an Invited Guest
                if (memberRequest.getUserId() != null && validCommissionUserIds.contains(memberRequest.getUserId())) {
                    log.info("Adding new Commission Member with UserID: {}", memberRequest.getUserId());
                    newMember.setFoncation(Foncation.MEMEBRE); // Or get from request
                }
                else if(memberRequest.getUserId() == null){
                    log.info("Adding new Invited Guest: {} {}", memberRequest.getPrenom(), memberRequest.getNom());
                    newMember.setFoncation(Foncation.INVITE);
                }else {
                    continue;
                }
                membersToSave.add(newMember);
            }
        }

        // === 3. PROCESS DELETIONS ===
        List<PvMembrePresent> membersToDelete = new ArrayList<>();
        for (PvMembrePresent currentMember : currentMembersMap.values()) {
            if (!memberIdsToKeep.contains(currentMember.getPvMembrePresentId())) {
                membersToDelete.add(currentMember);
            }
        }

        // === 4. EXECUTE DATABASE CHANGES ===
        if (!membersToDelete.isEmpty()) {
            log.info("Soft-deleting {} members.", membersToDelete.size());
            // Make sure your softDeleteAll method works correctly (see previous answers).
            pvMembrePresentRepository.softDeleteAll(membersToDelete);
        }

        if (!membersToSave.isEmpty()) {
            log.info("Saving {} members (adds/updates).", membersToSave.size());
            pvMembrePresentRepository.saveAll(membersToSave);
        }
    }
    /**
     * Handles the logic for deleting and adding documents associated with a ProcessVerbal.
     */

    private void synchronizeDocuments(ProcessVerbalRequest request, ProcessVerbal pvToUpdate) throws IOException, DocNotFound {

        List<PvDocRequest> docRequests = request.getDocuments() != null ? request.getDocuments() : Collections.emptyList();

        // Create a map of requests for existing documents for easy lookup of the new 'pv' value.
        // Key: docId, Value: PvDocRequest
        Map<Integer, PvDocRequest> updateRequestsMap = docRequests.stream()
                .filter(req -> req.getDocId() != null)
                .collect(Collectors.toMap(PvDocRequest::getDocId, Function.identity()));

        // Get the IDs of documents that should remain linked.
        List<Integer> docIdsToKeep = new ArrayList<>(updateRequestsMap.keySet());

        // Get the IDs of documents currently linked in the database.
        List<Integer> currentDocIds = pvDocsRepository.findDocIdsByProcessVerbal(pvToUpdate);


        // === 1. Handle Document Deletion ===
        List<Integer> docIdsToDelete = currentDocIds.stream()
                .filter(id -> !docIdsToKeep.contains(id))
                .collect(Collectors.toList());

        if (!docIdsToDelete.isEmpty()) {
            log.info("Found {} documents to remove.", docIdsToDelete.size());
            // Step 1: Soft-delete the actual documents in the 'docs' table
            documentCommuneService.deleteDocuments(docIdsToDelete);
            log.info("Soft-deleted Docs with IDs: {}", docIdsToDelete);
            // Step 2: Hard-delete the link records from the 'pv_docs' join table
            pvDocsRepository.deleteByDocIdIn(docIdsToDelete);
            log.info("Hard-deleted PvDocs links for Doc IDs: {}", docIdsToDelete);
        }


        // === 2. NEW: Handle Document Updates ===
        if (!docIdsToKeep.isEmpty()) {
            // Fetch the actual PvDocs link entities that we need to update.
            List<PvDocs> linksToUpdate = pvDocsRepository.findAllByProcessVerbalAndDocIdIn(pvToUpdate, docIdsToKeep);

            boolean hasChanges = false;
            for (PvDocs pvDocLink : linksToUpdate) {
                // Find the matching request for this link.
                PvDocRequest docRequest = updateRequestsMap.get(pvDocLink.getDoc().getId());
                if (docRequest != null) {
                    // Check if the 'pv' flag has changed and update it.
                    if (pvDocLink.isPv() != docRequest.isPv()) {
                        log.info("Updating 'pv' flag for Doc ID: {} to: {}", pvDocLink.getDoc().getId(), docRequest.isPv());
                        pvDocLink.setPv(docRequest.isPv());
                        hasChanges = true;
                    }
                }
            }

            if (hasChanges) {
                pvDocsRepository.saveAll(linksToUpdate);
            }
        }

        if (!docRequests.isEmpty()) {
            documentCommuneService.processAndSaveDocumentsForPv(docRequests,
                    Constants.PATH_ENTREPOT_GED_PPM, pvToUpdate);
        }
    }

}

