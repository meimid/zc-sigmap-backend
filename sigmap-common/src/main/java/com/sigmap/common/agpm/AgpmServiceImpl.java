/**
 * Service implementation for managing Agpm entities.
 * Provides functionality for CRUD operations, document management, versioning, and status updates
 * for Agpm entities, with integration to Alfresco for document storage and handling of associated
 * Ac and Docs entities. Includes soft deletion and filtering of non-deleted documents in responses.
 */
package com.sigmap.common.agpm;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.agpm.response.AgpmMinimalResponseDto;
import com.sigmap.common.agpm.response.AgpmResponseDto;
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
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
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
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;

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
    private final CurrentUserService currentUserService;

    /**
     * Constructor for AgpmServiceImpl with dependency injection.
     *
     * @param repository              Repository for Agpm entities
     * @param docsRepository          Repository for Docs entities
     * @param agpmMapper              Mapper for converting between Agpm and DTOs
     * @param refParamRepository      Repository for reference parameters
     * @param messageSource           Source for internationalized messages
     * @param alfrescoService         Service for Alfresco document storage
     * @param documentCommuneService  Service for common document operations
     * @param acService               Service for Ac entities
     */
    public AgpmServiceImpl(AgpmRepository repository,
                           DocsRepository docsRepository,
                           AgpmMapper agpmMapper,
                           RefParamRepository refParamRepository,
                           MessageSource messageSource,
                           AlfrescoService alfrescoService,
                           DocumentCommuneService documentCommuneService,
                           AcService acService,
                           CurrentUserService currentUserService) {
        this.repository = repository;
        this.docsRepository = docsRepository;
        this.agpmMapper = agpmMapper;
        this.refParamRepository = refParamRepository;
        this.messageSource = messageSource;
        this.alfrescoService = alfrescoService;
        this.documentCommuneService = documentCommuneService;
        this.acService = acService;
        this.currentUserService = currentUserService;
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
     * based on the given specification, filtered by the ACs of the current user. Only non-deleted Docs are included.
     *
     * @param spec     Specification to filter Agpm entities
     * @param pageable Pagination and sorting configuration
     * @return Page of Agpm entities with the latest version per ID and only non-deleted Docs
     * @throws AgpmNotFound if no Agpm entities are found for the user's ACs
     * @throws AgpmNotAssociatedWithUserAcException if an Agpm is not associated with the user's ACs
     */
    public Page<Agpm> findAll(Specification<Agpm> spec, Pageable pageable) throws AgpmNotAssociatedWithUserAcException, AcNotFound, AgpmNotFound {
        log.info("Recherche de tous les AGPM avec spécification: {}", spec);

        List<Long> userAcIdsLong = currentUserService.getCurrentUserAcIds();
        if (userAcIdsLong.isEmpty()) {
            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté");
            throw new AcNotFound("Aucun ID AC trouvé pour l'utilisateur connecté", Collections.emptyList());
        }
        Set<Integer> userAcIds = userAcIdsLong.stream()
                .map(Long::intValue)
                .collect(Collectors.toSet());

        for (Integer acId : userAcIds) {
            try {
                acService.findById(acId);
            } catch (AcNotFound e) {
                log.warn("AC non trouvé avec ID: {}", acId);
                throw e;
            }
        }

        Specification<Agpm> acFilterSpec = (root, query, criteriaBuilder) ->
                root.get("ac").get("id").in(userAcIds);
        Specification<Agpm> combinedSpec = spec != null ? spec.and(acFilterSpec) : acFilterSpec;

        Page<Agpm> allAgpms = repository.findAll(combinedSpec, pageable);

        if (allAgpms.isEmpty()) {
            log.warn("Aucun AGPM trouvé pour les AC de l'utilisateur: {}", userAcIds);
            throw new AgpmNotFound("Aucun AGPM trouvé pour les AC de l'utilisateur: " + userAcIds,
                    Collections.emptyList(), Collections.emptyList());
        }

        Map<Integer, Agpm> latestAgpms = allAgpms.getContent().stream()
                .collect(Collectors.toMap(
                        Agpm::getId,
                        agpm -> agpm,
                        (agpm1, agpm2) -> agpm1.getVersion() > agpm2.getVersion() ? agpm1 : agpm2
                ));

        for (Agpm agpm : latestAgpms.values()) {
            if (agpm.getAc() == null || !userAcIds.contains(agpm.getAc().getId())) {
                log.warn("AGPM avec ID {} n'est pas associé à un AC de l'utilisateur", agpm.getId());
                throw new AgpmNotAssociatedWithUserAcException(
                        "L'AGPM avec l'ID " + agpm.getId() + " n'est pas associé à un AC de l'utilisateur connecté",
                        List.of(agpm.getId()),
                        List.of(agpm.getVersion())
                );
            }
        }

        List<Agpm> latestAgpmList = latestAgpms.values().stream()
                .map(this::createFilteredResponseAgpm)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        int totalElements = (int) allAgpms.getTotalElements();

        log.info("Récupération de {} AGPM pour les AC de l'utilisateur {}", latestAgpmList.size(), userAcIds);

        return new PageImpl<>(latestAgpmList, pageable, totalElements);
    }

    /**
     * Retrieves the latest version of an Agpm entity by its ID. Only non-deleted Docs are included in the response,
     * and the Agpm must be associated with one of the ACs of the current user.
     *
     * @param agpmId  ID of the Agpm entity
     * @param agpmVer Ignored parameter (kept for compatibility); latest version is fetched
     * @return Agpm entity with the latest version and only non-deleted Docs
     * @throws AgpmNotFound if no Agpm entity is found for the ID
     * @throws AcNotFound if no AC is found for the current user
     * @throws AgpmNotAssociatedWithUserAcException if the Agpm is not associated with the user's ACs
     */
    @Override
    public Agpm findById(Integer agpmId, Integer agpmVer) throws AgpmNotFound, AcNotFound, AgpmNotAssociatedWithUserAcException {
        log.info("Recherche de l'AGPM avec ID: {}", agpmId);

        List<Long> userAcIdsLong = currentUserService.getCurrentUserAcIds();
        if (userAcIdsLong.isEmpty()) {
            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté");
            throw new AcNotFound("Aucun ID AC trouvé pour l'utilisateur connecté", Collections.emptyList());
        }
        Set<Integer> userAcIds = userAcIdsLong.stream()
                .map(Long::intValue)
                .collect(Collectors.toSet());

        for (Integer acId : userAcIds) {
            try {
                acService.findById(acId);
            } catch (AcNotFound e) {
                log.warn("AC non trouvé avec ID: {}", acId);
                throw e;
            }
        }

        Integer latestVersion = repository.findMaxVersionById(agpmId);
        if (latestVersion == null) {
            log.warn("Aucun AGPM trouvé pour l'ID: {}", agpmId);
            throw new AgpmNotFound("Aucun AGPM trouvé pour l'ID", List.of(agpmId), agpmVer != null ? List.of(agpmVer) : Collections.emptyList());
        }

        Agpm agpm = repository.findById(new CompositeId(agpmId, latestVersion))
                .orElseThrow(() -> new AgpmNotFound("Aucun AGPM trouvé pour l'ID et la version", List.of(agpmId), List.of(latestVersion)));

        if (agpm.getAc() == null || !userAcIds.contains(agpm.getAc().getId())) {
            log.warn("AGPM avec ID {} n'est pas associé à un AC de l'utilisateur", agpmId);
            throw new AgpmNotAssociatedWithUserAcException(
                    "L'AGPM avec l'ID " + agpmId + " n'est pas associé à un AC de l'utilisateur connecté",
                    List.of(agpmId),
                    List.of(latestVersion)
            );
        }

        Agpm responseAgpm = createFilteredResponseAgpm(agpm);
        log.info("Retour de l'AGPM {}:{} avec uniquement les documents non supprimés: {}",
                agpmId, latestVersion, responseAgpm.getDocs() != null
                        ? responseAgpm.getDocs().stream().map(Docs::getId).collect(Collectors.toList())
                        : Collections.emptyList());

        return responseAgpm;
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

        Optional<Long> acIdOptional = Authentication.getCurrentUserAC();// will be replaced by the logic of acIds
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
     * Soft deletes an Agpm entity by its ID and version, only if the status is DRAFT.
     *
     * @param agpmId  ID of the Agpm entity to delete
     * @param agpmVer Version of the Agpm entity to delete
     * @throws AgpmNotFound if the Agpm entity is not found
     * @throws AgpmSoftDelete if the soft delete operation fails
     * @throws AgpmInvalidStatusException if the Agpm status is not DRAFT
     */
    @Override
    public void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound, AgpmSoftDelete, AgpmInvalidStatusException {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Optional<Agpm> existingAgpm = repository.findById(compositeId);
        if (existingAgpm.isEmpty()) {
            throw new AgpmNotFound("Agpm not found", List.of(agpmId), List.of(agpmVer));
        }

        Agpm agpm = existingAgpm.get();
        if (!Constants.DRAFT_STATUS.equals(agpm.getStatus().getId())) {
            throw new AgpmInvalidStatusException("Agpm can only be deleted when status is DRAFT",
                    List.of(agpmId),
                    List.of(agpmVer),
                    agpm.getStatus().getId());
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
     * @throws AgpmInvalidStateException if the Agpm is not in DRAFT status or has a non-null processInstanceId
     */
    @Override
    @Transactional
    public Agpm updateWithoutVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, DocNotFound, IOException, InvalidIdAndVersionAgpm, AgpmInvalidStateException {
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

        // Check if Agpm is in DRAFT status and has null processInstanceId
        restrictUpdateAgpm(agpm);

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
     * @param agpmId   ID of the Agpm entity to update
     * @param agpmVer  Version of the Agpm entity to update
     * @param statusId The new status ID to set
     * @return Updated Agpm entity
     * @throws AgpmNotFound if the Agpm entity is not found
     * @throws ReferenceNotFound if the status ID is not found
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
     * Processes and saves uploaded documents for an Agpm entity, storing them in Alfresco.
     *
     * @param files     List of MultipartFile objects to process
     * @param savedAgpm Agpm entity to associate the documents with
     * @throws IOException if an error occurs during file processing or upload to Alfresco
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

    /**
     * Creates a filtered response Agpm entity, excluding soft-deleted documents.
     *
     * @param source Source Agpm entity
     * @return Filtered Agpm entity with only non-deleted documents
     */
    private Agpm createFilteredResponseAgpm(Agpm source)  {
        if (source == null) {
            log.error("AGPM passé à createFilteredResponseAgpm est null");
            try {
                throw new AgpmNotFound("AGPM cannot be null", List.of(source.getId()), List.of(source.getVersion()));
            } catch (AgpmNotFound e) {
                throw new RuntimeException(e);
            }
        }
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

    /**
     * Restricts updates to Agpm entities that are in DRAFT status and have a null processInstanceId.
     *
     * @param agpm The Agpm entity to check
     * @throws AgpmInvalidStateException if the Agpm is not in DRAFT status or has a non-null processInstanceId
     */
    private void restrictUpdateAgpm(Agpm agpm) throws AgpmInvalidStateException {
        RefParam draftStatus = refParamRepository.findById(Constants.DRAFT_STATUS)
                .orElseThrow(() -> new NotFoundException("Default status not found"));

        boolean isDraft = Objects.equals(agpm.getStatus().getId(), draftStatus.getId());
        boolean hasNoProcessInstance = agpm.getProcessInstanceId() == null;

        if (!isDraft || !hasNoProcessInstance) {
            StringBuilder message = new StringBuilder("Agpm with ID " + agpm.getId() + " and version " + agpm.getVersion() +
                    " cannot be updated because it does not meet the required state: ");
            if (!isDraft) {
                message.append("it is not in DRAFT status");
            }
            if (!isDraft && !hasNoProcessInstance) {
                message.append(" and ");
            }
            if (!hasNoProcessInstance) {
                message.append("it has an active process instance (processInstanceId: " + agpm.getProcessInstanceId() + ")");
            }
            message.append(".");

            throw new AgpmInvalidStateException(
                    message.toString(),
                    List.of(agpm.getId()),
                    List.of(agpm.getVersion())
            );
        }
    }

    @Override
    public Page<AgpmResponseDto> findAllVersionsByIdFull(Integer agpmId, Pageable pageable) throws AgpmNotFound {
        if (agpmId == null || agpmId < 0) {
            throw new IllegalArgumentException("Invalid AGPM ID: " + agpmId);
        }

        Optional<Integer> maxVersion = findLatestVersionById(agpmId);
        if (maxVersion.isEmpty()) {
            throw new AgpmNotFound("No Agpm found for ID", List.of(agpmId), Collections.emptyList());
        }

        Page<Agpm> agpmPage = repository.findAllById(agpmId, pageable);
        if (agpmPage.isEmpty()) {
            throw new AgpmNotFound("No Agpm versions found for ID", List.of(agpmId), Collections.emptyList());
        }

        List<AgpmResponseDto> responseDtos = agpmPage.getContent().stream()
                .map(this::createFilteredResponseAgpm)
                .map(agpmMapper::mapToDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} versions for Agpm ID {} (page {} of {})",
                responseDtos.size(), agpmId, agpmPage.getNumber(), agpmPage.getTotalPages());

        return new PageImpl<>(responseDtos, pageable, agpmPage.getTotalElements());
    }

    @Override
    public Page<AgpmMinimalResponseDto> findAllVersionsByIdMinimal(Integer agpmId, Pageable pageable) throws AgpmNotFound {
        if (agpmId == null || agpmId < 0) {
            throw new IllegalArgumentException("Invalid AGPM ID: " + agpmId);
        }

        // Check if any versions exist for the given ID
        Optional<Integer> maxVersion = findLatestVersionById(agpmId);
        if (maxVersion.isEmpty()) {
            throw new AgpmNotFound("No Agpm found for ID", List.of(agpmId), Collections.emptyList());
        }

        // Fetch paginated versions for the given ID
        Page<Agpm> agpmPage = repository.findAllById(agpmId, pageable);
        if (agpmPage.isEmpty()) {
            throw new AgpmNotFound("No Agpm versions found for ID", List.of(agpmId), Collections.emptyList());
        }

        // Map to AgpmMinimalResponseDto
        List<AgpmMinimalResponseDto> responseDtos = agpmPage.getContent().stream()
                .map(agpmMapper::toMinimalResponseDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} minimal versions for Agpm ID {} (page {} of {})",
                responseDtos.size(), agpmId, agpmPage.getNumber(), agpmPage.getTotalPages());

        return new PageImpl<>(responseDtos, pageable, agpmPage.getTotalElements());
    }
}