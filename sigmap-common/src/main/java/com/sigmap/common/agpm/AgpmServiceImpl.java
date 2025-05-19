package com.sigmap.common.agpm;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.config.Constants;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import com.sigmap.common.security.Authentication;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for managing Agpm entities.
 * Handles CRUD operations and associations with Ac and Docs.
 */
@Service
@Slf4j
public class AgpmServiceImpl implements AgpmService {
    private final AgpmRepository repository;
    private final DocsRepository docsRepository;
    private final AgpmMapper agpmMapper;
    private final RefParamRepository refParamRepository;
    private final MessageSource messageSource;
    private final AlfrescoService alfrescoService;
    private final DocumentCommuneService documentCommuneService;
    private final AcService acService;

    public AgpmServiceImpl(AgpmRepository repository,
                           DocsRepository docsRepository,
                           AgpmMapper agpmMapper,
                           RefParamRepository refParamRepository,
                           MessageSource messageSource,
                           AlfrescoService alfrescoService,
                           DocumentCommuneService documentCommuneService,
                           AcService acService) {
        this.repository = repository;
        this.docsRepository = docsRepository;
        this.agpmMapper = agpmMapper;
        this.refParamRepository = refParamRepository;
        this.messageSource = messageSource;
        this.alfrescoService = alfrescoService;
        this.documentCommuneService = documentCommuneService;
        this.acService = acService;
    }

    /**
     * Retrieves the current locale for internationalization.
     *
     * @return The current Locale
     */
    private Locale getLocal() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Retrieves a paginated list of non-deleted Agpm entities, returning only the latest version for each ID
     * based on the given specification. Only non-deleted Docs are included in the response.
     *
     * @param spec     Specification to filter Agpm entities
     * @param pageable Pagination and sorting configuration
     * @return Page of Agpm entities with the latest version per ID and only non-deleted Docs
     */
    public Page<Agpm> findAll(Specification<Agpm> spec, Pageable pageable) {
        Page<Agpm> allAgpms = repository.findAll(spec, pageable);

        Map<Integer, Agpm> latestAgpms = allAgpms.getContent().stream()
                .collect(Collectors.toMap(
                        Agpm::getId,
                        this::createFilteredResponseAgpm,
                        (agpm1, agpm2) -> agpm1.getVersion() > agpm2.getVersion() ? agpm1 : agpm2
                ));

        List<Agpm> latestAgpmList = new ArrayList<>(latestAgpms.values());
        int totalElements = (int) allAgpms.getTotalElements();
        return new PageImpl<>(latestAgpmList, pageable, totalElements);
    }

    /**
     * Retrieves a paginated list of deleted Agpm entities.
     *
     * @param pageable Pagination and sorting configuration
     * @return Page of deleted Agpm entities
     */
    @Override
    public Page<Agpm> findAllDeleted(Pageable pageable) {
        Specification<Agpm> spec = AgpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    /**
     * Retrieves a single Agpm entity based on the given specification.
     *
     * @param specification Specification to filter the Agpm entity
     * @return Optional containing the Agpm entity if found
     */
    @Override
    public Optional<Agpm> findOne(Specification<Agpm> specification) {
        return repository.findOne(specification);
    }

    /**
     * Retrieves the latest version of an Agpm entity by its ID. Only non-deleted Docs are included in the response.
     *
     * @param agpmId ID of the Agpm entity
     * @param agpmVer Ignored parameter (kept for compatibility); latest version is fetched
     * @return Agpm entity with the latest version and only non-deleted Docs
     * @throws AgpmNotFound if no Agpm entity is found for the ID
     */
    @Override
    public Agpm findById(Integer agpmId, Integer agpmVer) throws AgpmNotFound {
        Integer latestVersion = findLatestVersionById(agpmId)
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID", List.of(agpmId), List.of(agpmVer)));
        Agpm agpm = repository.findById(new CompositeId(agpmId, latestVersion))
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID and version", List.of(agpmId), List.of(latestVersion)));

        Agpm responseAgpm = createFilteredResponseAgpm(agpm);
        log.info("Returning Agpm {}:{} with only non-soft-deleted document IDs: {}",
                agpmId, latestVersion, responseAgpm.getDocs() != null
                        ? responseAgpm.getDocs().stream().map(Docs::getId).collect(Collectors.toList())
                        : Collections.emptyList());

        return responseAgpm;
    }

    /**
     * Retrieves the latest version number for a given Agpm ID.
     *
     * @param agpmId ID of the Agpm entity
     * @return Optional containing the latest version, or empty if not found
     */
    private Optional<Integer> findLatestVersionById(Integer agpmId) {
        return Optional.ofNullable(repository.findMaxVersionById(agpmId));
    }

    /**
     * Saves an Agpm entity to the database.
     *
     * @param agpm Agpm entity to save
     * @return Saved Agpm entity
     */
    @Override
    public Agpm save(Agpm agpm) {
        return repository.save(agpm);
    }

    /**
     * Creates a new Agpm entity based on the provided request.
     *
     * @param request AgpmRequest containing data for the new entity
     * @return Created Agpm entity
     * @throws AgpmAlreadyExistsException if an Agpm with the same ID and version exists
     * @throws AgpmNotNull if required fields are null
     * @throws AcNotFound if the associated Ac entity is not found
     * @throws DocNotFound if a requested document is not found
     * @throws AgpmDefaultStatus if the default status cannot be set
     * @throws IOException if an error occurs during file processing
     * @note Relies on Authentication for Ac ID; consider abstracting security logic
     */
    @Override
    public Agpm create(AgpmRequest request) throws AgpmAlreadyExistsException, AgpmNotNull, AcNotFound, DocNotFound, AgpmDefaultStatus, IOException {
        Integer version = request.getVersion();
        if (version == null) {
            throw new AgpmNotNull("Version cannot be null");
        }

        Integer id = request.getId();
        if (id == null) {
            id = repository.findMaxId();
            if (id == null) {
                id = 1;
            } else {
                id++;
            }
        }

        Optional<Agpm> existingAgpm = repository.findById(new CompositeId(id, version));
        if (existingAgpm.isPresent()) {
            throw new AgpmAlreadyExistsException("Agpm already exists", id, version);
        }

        Agpm agpm = agpmMapper.toEntity(request);
        agpm.setId(id);
        agpm.setVersion(version);

        Optional<Long> acIdOptional = Authentication.getCurrentUserAC();
        if (acIdOptional.isPresent()) {
            Long acIdLong = acIdOptional.get();
            Integer acIdInt = Math.toIntExact(acIdLong);
            Optional<Ac> acOptional = Optional.ofNullable(acService.findById(acIdInt));
            if (acOptional.isPresent()) {
                agpm.setAc(acOptional.get());
            } else {
                throw new AcNotFound("AC not found with ID: ", Collections.singletonList(acIdInt));
            }
        } else {
            throw new NotFoundException("No AC ID found for current user");
        }

        if (request.getFiles() != null) {
            processAndSaveDocuments(request.getFiles(), agpm);
        }

        agpm.setIsDeleted(false);
        RefParam defaultStatus = refParamRepository.findById(Constants.DRAFT_STATUS)
                .orElseThrow(() -> new AgpmDefaultStatus("Default status not found"));
        agpm.setStatus(defaultStatus);

        return repository.save(agpm);
    }

    /**
     * Soft deletes an Agpm entity by its ID and version.
     *
     * @param agpmId  ID of the Agpm entity to delete
     * @param agpmVer Version of the Agpm entity to delete
     * @throws AgpmNotFound if the Agpm entity is not found
     * @throws AgpmSoftDelete if the soft delete operation fails
     */
    @Override
    public void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound, AgpmSoftDelete {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Optional<Agpm> existingAgpm = repository.findById(compositeId);
        if (existingAgpm.isEmpty()) {
            throw new AgpmNotFound("Agpm not found", List.of(agpmId), List.of(agpmVer));
        }

        Integer rowsAffected = repository.softDelete(agpmId, agpmVer, Calendar.getInstance());
        if (rowsAffected != 1) {
            throw new AgpmSoftDelete("Soft delete failed", rowsAffected, agpmId, agpmVer);
        }
    }

    /**
     * Updates an existing Agpm entity by its ID with versioning.
     * The latest version is fetched using the ID, ignoring the provided version.
     * Soft-deleted documents are marked as deleted in the database and retained in the Agpm's document association,
     * but excluded from the response.
     *
     * @param agpmId  ID of the Agpm entity to update
     * @param agpmVer Ignored parameter (kept for compatibility); latest version is fetched
     * @param request AgpmRequest containing updated data
     * @return Updated Agpm entity with only non-soft-deleted documents in the response
     * @throws AgpmNotFound if the Agpm entity is not found
     * @throws DocNotFound if a requested document is not found
     * @throws ReferenceNotFound if a reference (e.g., status) is not found
     * @throws IOException if an error occurs during file processing
     */
    @Override
    @Transactional
    public Agpm updateWithVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, DocNotFound, ReferenceNotFound, IOException {
        if (agpmId < 0) {
            throw new IllegalArgumentException("Invalid ID: " + agpmId);
        }

        Integer latestVersion = findLatestVersionById(agpmId)
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID", List.of(agpmId), List.of(agpmVer)));
        CompositeId compositeId = new CompositeId(agpmId, latestVersion);
        Agpm existingAgpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID and version", List.of(agpmId), List.of(latestVersion)));

        if (Boolean.TRUE.equals(existingAgpm.getIsDeleted())) {
            throw new IllegalArgumentException(messageSource.getMessage("agpm.deleted.update", new Object[]{agpmId, latestVersion}, getLocal()));
        }

        List<Integer> existingDocIds = existingAgpm.getDocs() != null
                ? existingAgpm.getDocs().stream().map(Docs::getId).toList()
                : new ArrayList<>();
        log.debug("Existing document IDs for Agpm {}:{}: {}", agpmId, latestVersion, existingDocIds);

        List<Integer> requestedDocIds = request.getDocIds() != null ? new ArrayList<>(request.getDocIds()) : new ArrayList<>();
        List<Docs> requestedDocs = docsRepository.findByIdIn(requestedDocIds);
        if (requestedDocIds.size() != requestedDocs.size()) {
            List<Integer> foundDocIds = requestedDocs.stream().map(Docs::getId).toList();
            List<Integer> missingDocIds = requestedDocIds.stream()
                    .filter(docId -> !foundDocIds.contains(docId))
                    .collect(Collectors.toList());
            throw new DocNotFound("Documents not found", missingDocIds);
        }
        log.debug("Requested document IDs: {}", requestedDocIds);

        Agpm tempAgpm = new Agpm();
        tempAgpm.setId(agpmId);
        tempAgpm.setVersion(latestVersion);
        tempAgpm.setAc(existingAgpm.getAc());
        tempAgpm.setStatus(existingAgpm.getStatus());
        tempAgpm.setIsDeleted(existingAgpm.getIsDeleted());
        agpmMapper.updateAgpmFromRequest(request, tempAgpm);

        boolean fieldsChanged = false;
        if (!Objects.equals(existingAgpm.getRef(), tempAgpm.getRef())) {
            fieldsChanged = true;
        }
        if (!Objects.equals(existingAgpm.getNom(), tempAgpm.getNom())) {
            fieldsChanged = true;
        }
        if (!Objects.equals(existingAgpm.getContenu(), tempAgpm.getContenu())) {
            fieldsChanged = true;
        }
        if (!Objects.equals(existingAgpm.getRemarque(), tempAgpm.getRemarque())) {
            fieldsChanged = true;
        }

        boolean docsChanged = !existingDocIds.equals(requestedDocIds);
        boolean filesChanged = request.getFiles() != null && !request.getFiles().isEmpty();

        if (!fieldsChanged && !docsChanged && !filesChanged) {
            log.info("No changes detected for Agpm {}:{}. Returning existing version.", agpmId, latestVersion);
            if (existingAgpm.getDocs() != null) {
                existingAgpm.setDocs(existingAgpm.getDocs().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                        .collect(Collectors.toList()));
            }
            return existingAgpm;
        }

        log.info("Changes detected for Agpm {}:{}. Incrementing version.", agpmId, latestVersion);
        Integer nextVersion = repository.findMaxVersionById(agpmId);
        nextVersion = (nextVersion != null) ? nextVersion + 1 : 1;

        Agpm newAgpm = new Agpm();
        newAgpm.setId(agpmId);
        newAgpm.setVersion(nextVersion);
        newAgpm.setAc(existingAgpm.getAc());
        newAgpm.setProcessInstanceId(existingAgpm.getProcessInstanceId());
        newAgpm.setStatus(existingAgpm.getStatus());
        newAgpm.setIsDeleted(existingAgpm.getIsDeleted());
        agpmMapper.updateAgpmFromRequest(request, newAgpm);

        List<Docs> existingDocs = newAgpm.getDocs() != null ? new ArrayList<>(newAgpm.getDocs()) : new ArrayList<>();

        // Process requested documents
        if (!requestedDocs.isEmpty()) {
            existingDocs.removeIf(doc -> requestedDocIds.contains(doc.getId()));
            existingDocs.addAll(requestedDocs);
            newAgpm.setDocs(existingDocs);
        }

        // Process uploaded files
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            try {
                processAndSaveDocuments(request.getFiles(), newAgpm);
                log.debug("New documents added from files to Agpm {}:{}", agpmId, nextVersion);
            } catch (IOException e) {
                log.error("Failed to process files: {}", e.getMessage());
                throw e;
            }
        }

        // Soft-delete documents not in requestedDocIds
        List<Integer> docIdsToDelete = existingDocIds.stream()
                .filter(docId -> !requestedDocIds.contains(docId))
                .collect(Collectors.toList());
        log.debug("Documents to soft delete: {}", docIdsToDelete);

        for (Integer docId : docIdsToDelete) {
            Optional<Docs> docToDelete = docsRepository.findById(docId);
            if (docToDelete.isPresent()) {
                docsRepository.softDelete(docToDelete.get().getId());
                log.info("Soft deleted document with ID: {} (isDeleted set to true)", docId);
            } else {
                log.warn("Document with ID {} not found for soft deletion", docId);
                throw new DocNotFound("Document not found for deletion", List.of(docId));
            }
        }

        newAgpm.setUpdatedAt(Calendar.getInstance());
        Agpm savedAgpm = repository.save(newAgpm);

        Agpm responseAgpm = createFilteredResponseAgpm(savedAgpm);
        log.info("Returning Agpm {}:{} with only non-soft-deleted document IDs: {}",
                agpmId, latestVersion, responseAgpm.getDocs().stream().map(Docs::getId).collect(Collectors.toList()));

        return responseAgpm;
    }

    /**
     * Updates an existing Agpm entity by its ID, using the latest version, without versioning.
     * The latest version is fetched using the ID, ignoring the provided version.
     * Soft-deleted documents are marked as deleted in the database and retained in the Agpm's document association,
     * but excluded from the response.
     *
     * @param agpmId  ID of the Agpm entity to update
     * @param agpmVer Ignored parameter (kept for compatibility); latest version is fetched
     * @param request AgpmRequest containing updated data
     * @return Updated Agpm entity with only non-soft-deleted documents in the response
     * @throws AgpmNotFound if the Agpm entity is not found
     * @throws DocNotFound if a requested document is not found
     * @throws IOException if an error occurs during file processing
     * @throws InvalidIdAndVersionAgpm if the ID is invalid
     */
    @Override
    @Transactional
    public Agpm updateWithoutVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, DocNotFound, IOException, InvalidIdAndVersionAgpm {
        if (agpmId < 0) {
            throw new InvalidIdAndVersionAgpm("Invalid ID", List.of(agpmId), List.of(agpmVer));
        }

        Integer latestVersion = findLatestVersionById(agpmId)
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID", List.of(agpmId), List.of(agpmVer)));
        CompositeId compositeId = new CompositeId(agpmId, latestVersion);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID and version", List.of(agpmId), List.of(latestVersion)));

        if (Boolean.TRUE.equals(agpm.getIsDeleted())) {
            throw new IllegalArgumentException(messageSource.getMessage("agpm.deleted.update", new Object[]{agpmId, latestVersion}, getLocal()));
        }

        agpmMapper.updateAgpmFromRequest(request, agpm);

        List<Integer> existingDocIds = agpm.getDocs() != null
                ? agpm.getDocs().stream().map(Docs::getId).toList()
                : new ArrayList<>();
        log.info("Existing Docs for updating Agpm {}:{}: {}", agpmId, latestVersion, existingDocIds);

        List<Integer> requestedDocIds = request.getDocIds() != null ? new ArrayList<>(request.getDocIds()) : new ArrayList<>();
        List<Docs> requestedDocs = docsRepository.findByIdIn(requestedDocIds);
        if (requestedDocIds.size() != requestedDocs.size()) {
            List<Integer> foundDocIds = requestedDocs.stream().map(Docs::getId).toList();
            List<Integer> missingDocIds = requestedDocIds.stream()
                    .filter(docId -> !foundDocIds.contains(docId))
                    .collect(Collectors.toList());
            throw new DocNotFound("Documents not found", missingDocIds);
        }
        log.debug("Requested document IDs: {}", requestedDocIds);

        List<Docs> existingDocs = agpm.getDocs() != null ? new ArrayList<>(agpm.getDocs()) : new ArrayList<>();

        // Process requested documents
        if (!requestedDocs.isEmpty()) {
            existingDocs.removeIf(doc -> requestedDocIds.contains(doc.getId()));
            existingDocs.addAll(requestedDocs);
            agpm.setDocs(existingDocs);
        }

        // Process uploaded files
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            try {
                processAndSaveDocuments(request.getFiles(), agpm);
                log.debug("New documents added from files to Agpm");
            } catch (IOException e) {
                log.error("Failed to process files: {}", e.getMessage());
                throw e;
            }
        }

        // Soft-delete documents not in requestedDocIds
        List<Integer> docIdsToDelete = existingDocIds.stream()
                .filter(docId -> !requestedDocIds.contains(docId))
                .collect(Collectors.toList());
        log.debug("Documents to soft delete: {}", docIdsToDelete);

        for (Integer docId : docIdsToDelete) {
            Optional<Docs> docToDelete = docsRepository.findById(docId);
            if (docToDelete.isPresent()) {
                docsRepository.softDelete(docToDelete.get().getId());
                log.info("Soft deleted document with ID: {} (isDeleted set to true)", docId);
            } else {
                log.warn("Document with ID {} not found for soft deletion", docId);
                throw new DocNotFound("Document not found for deletion", List.of(docId));
            }
        }

        agpm.setUpdatedAt(Calendar.getInstance());
        Agpm updatedAgpm = repository.save(agpm);

        Agpm responseAgpm = createFilteredResponseAgpm(updatedAgpm);
        log.info("Returning Agpm {}:{} with only non-soft-deleted document IDs: {}",
                agpmId, latestVersion, responseAgpm.getDocs().stream().map(Docs::getId).collect(Collectors.toList()));

        return responseAgpm;
    }

    /**
     * Updates the status of an Agpm entity by its ID and version.
     *
     * @param agpmId  ID of the Agpm entity to update
     * @param agpmVer Version of the Agpm entity to update
     * @return Updated Agpm entity
     * @throws AgpmNotFound if the Agpm entity is not found
     */
    @Override
    @Transactional
    public Agpm updateStatus(Integer agpmId, Integer agpmVer, Integer statusId) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("Agpm not found", List.of(agpmId), List.of(agpmVer)));

        RefParam newStatus = refParamRepository.findById(statusId).orElseThrow(() ->
                new ReferenceNotFound("Status not found with ID: ", List.of(String.valueOf(statusId))));
        agpm.setStatus(newStatus);

        agpm.setUpdatedAt(Calendar.getInstance());
        return repository.save(agpm);
    }

    /**
     * Processes and saves uploaded documents for an Agpm entity.
     *
     * @param files     List of MultipartFile objects to process
     * @param savedAgpm Agpm entity to associate the documents with
     * @throws IOException if an error occurs during file processing
     */
    private void processAndSaveDocuments(List<MultipartFile> files, Agpm savedAgpm) throws IOException {
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        UUID uuid = UUID.randomUUID();
                        Tika tika = new Tika();
                        String detectedMimeType = tika.detect(file.getInputStream());
                        String originalFilename = file.getOriginalFilename();

                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
                        if (fileExtension.isEmpty()) {
                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
                        }

                        long fileSize = file.getSize();
                        String formattedSize = documentCommuneService.formatSize(fileSize);

                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED + "/%d/%d",
                                savedAgpm.getId(), savedAgpm.getVersion());

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        String uniqueFileName = "agpm_" + uuid + "_" + extension;
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
                        log.info("Uploaded document to Alfresco with ID: {}", alfrescoId);

                        Docs document = new Docs();
                        document.setName(baseName);
                        document.setAlfresco_id(alfrescoId);
                        document.setPathFolder(folderPath);
                        document.setFileSize(formattedSize);
                        document.setContentType(detectedMimeType);
                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
                        document.setAlfrescoName(uniqueFileName);

                        Docs savedDoc = docsRepository.save(document);
                        if (savedAgpm.getDocs() == null) {
                            savedAgpm.setDocs(new ArrayList<>());
                        }
                        savedAgpm.getDocs().add(savedDoc);
                    } catch (Exception e) {
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    private Agpm createFilteredResponseAgpm(Agpm source) {
        Agpm responseAgpm = new Agpm();
        responseAgpm.setId(source.getId());
        responseAgpm.setVersion(source.getVersion());
        responseAgpm.setRef(source.getRef());
        responseAgpm.setNom(source.getNom());
        responseAgpm.setContenu(source.getContenu());
        responseAgpm.setRemarque(source.getRemarque());
        responseAgpm.setProcessInstanceId(source.getProcessInstanceId());
        responseAgpm.setAc(source.getAc());
        responseAgpm.setStatus(source.getStatus());
        responseAgpm.setIsDeleted(source.getIsDeleted());
        responseAgpm.setCreatedAt(source.getCreatedAt());
        responseAgpm.setCreatedBy(source.getCreatedBy());
        responseAgpm.setUpdatedBy(source.getUpdatedBy());
        responseAgpm.setUpdatedAt(source.getUpdatedAt());
        if (source.getDocs() != null) {
            responseAgpm.setDocs(source.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList()));
        }
        return responseAgpm;
    }
}