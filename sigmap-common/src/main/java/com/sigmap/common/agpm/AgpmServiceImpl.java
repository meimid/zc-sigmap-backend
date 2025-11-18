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
import com.sigmap.common.agpm.response.AgpmMinimalResponseDto;
import com.sigmap.common.agpm.response.AgpmResponseDto;
import com.sigmap.common.config.Constants;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.model_agpm.ModelAgpmRepository;
import com.sigmap.common.model_agpm.ModelAgpmService;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SequenceGeneratorService sequenceGeneratorService;
    private final ModelAgpmService modelAgpmService;
    private final ModelAgpmRepository modelAgpmRepository;


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
                           CurrentUserService currentUserService, SequenceGeneratorService sequenceGeneratorService, ModelAgpmService modelAgpmService, ModelAgpmRepository modelAgpmRepository) {
        this.repository = repository;
        this.docsRepository = docsRepository;
        this.agpmMapper = agpmMapper;
        this.refParamRepository = refParamRepository;
        this.messageSource = messageSource;
        this.alfrescoService = alfrescoService;
        this.documentCommuneService = documentCommuneService;
        this.acService = acService;
        this.currentUserService = currentUserService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.modelAgpmService = modelAgpmService;
        this.modelAgpmRepository = modelAgpmRepository;
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
    /**
     * Retrieves a paginated list of non-deleted Agpm entities, returning only the latest version for each ID
     * based on the given specification, filtered by the ACs of the current user. Only non-deleted Docs are included.
     *
     * @param spec     Specification to filter Agpm entities
     * @param pageable Pagination and sorting configuration
     * @return Page of Agpm entities with the latest version per ID and only non-deleted Docs
     */
    public Page<Agpm> findAll(Specification<Agpm> spec, Pageable pageable) {
        log.info("Recherche de tous les AGPM avec spécification: {}", spec);

        // Get user's AC IDs
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            log.info("Aucun ID AC trouvé pour l'utilisateur connecté");
            return Page.empty(pageable);
        }

        // Validate ACs
        for (Integer acId : userAcIds) {
            try {
                acService.findById(acId);
            } catch (Exception e) {
                log.info("AC non trouvé avec ID: {}", acId);
                return Page.empty(pageable); // Return empty page instead of throwing AcNotFound
            }
        }

        // Specification for latest version
        Specification<Agpm> latestVersionSpec = (root, query, criteriaBuilder) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Agpm> subRoot = subquery.from(Agpm.class);
            subquery.select(criteriaBuilder.max(subRoot.get("version")))
                    .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            return criteriaBuilder.equal(root.get("version"), subquery);
        };

        // Apply AC filter
        Specification<Agpm> acFilterSpec = (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("ac").get("id")).value(userAcIds);

        // Combine specifications
        Specification<Agpm> combinedSpec = spec != null
                ? spec.and(acFilterSpec).and(latestVersionSpec)
                : acFilterSpec.and(latestVersionSpec);

        // Query AGPMs
        Page<Agpm> allAgpms = repository.findAll(combinedSpec, pageable);

        if (allAgpms.isEmpty()) {
            log.info("Aucun AGPM trouvé pour les AC de l'utilisateur: {}", userAcIds);
            return Page.empty(pageable);
        }

        // Transform AGPMs to include only non-deleted Docs
        Page<Agpm> agpmsResponse = allAgpms.map(this::createFilteredResponseAgpm);
        log.info("Récupération de {} AGPM pour les AC de l'utilisateur {}", agpmsResponse.getContent().size(), userAcIds);

        return agpmsResponse;
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

        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté");
            throw new UserHaveNoAc("Aucun ID AC trouvé pour l'utilisateur connecté");
        }


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
        String modelAlfrescoId = modelAgpmRepository
                .findModelAlfrescoId(agpmId, latestVersion)
                .orElse(null);
        if (modelAlfrescoId != null && !modelAlfrescoId.isBlank()) {
            agpm.setModelAlfrescoId(modelAlfrescoId);
        }

        Agpm responseAgpm = createFilteredResponseAgpm(agpm);
        return responseAgpm;
    }
    @Override
    public Agpm findByIdAndVer(Integer agpmId, Integer agpmVer) throws AgpmNotFound, AcNotFound, AgpmNotAssociatedWithUserAcException {
        log.info("Recherche de l'AGPM avec ID: {}", agpmId);

        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté");
            throw new AcNotFound("Aucun ID AC trouvé pour l'utilisateur connecté", Collections.emptyList());
        }


        for (Integer acId : userAcIds) {
            try {
                acService.findById(acId);
            } catch (AcNotFound e) {
                log.warn("AC non trouvé avec ID: {}", acId);
                throw e;
            }
        }



        Agpm agpm = repository.findById(new CompositeId(agpmId, agpmVer))
                .orElseThrow(() -> new AgpmNotFound("Aucun AGPM trouvé pour l'ID et la version", List.of(agpmId), List.of(agpmVer)));

        if (agpm.getAc() == null || !userAcIds.contains(agpm.getAc().getId())) {
            log.warn("AGPM avec ID {} n'est pas associé à un AC de l'utilisateur", agpmId);
            throw new AgpmNotAssociatedWithUserAcException(
                    "L'AGPM avec l'ID " + agpmId + " n'est pas associé à un AC de l'utilisateur connecté",
                    List.of(agpmId),
                    List.of(agpmVer)
            );
        }

        Agpm responseAgpm = createFilteredResponseAgpm(agpm);
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
    public Agpm create(AgpmRequest request) throws AgpmAlreadyExistsException, AgpmNotNull, AcNotFound, DocNotFound, AgpmDefaultStatus, IOException, NotFoundException, SecurityException {
        Integer version = request.getVersion();
        if (version == null) {
            throw new AgpmNotNull("Version cannot be null");
        }

        Integer id = request.getId();
        if (id == null) {
            id = sequenceGeneratorService.getNextId(Constants.AGPM_ID_SEQ);
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
        setRef(agpm);
        List<UserAc> userAcs = currentUserService.getCurrentUserAc();
        if (userAcs == null || userAcs.isEmpty()) {
            throw new UserHaveNoAc("Aucun ID AC trouvé pour l'utilisateur connecté");
        }

        Integer requestAcId = request.getAcId();
        if (requestAcId != null) {
            boolean hasAcId = userAcs.stream()
                    .anyMatch(userAc -> userAc.getAcId().equals(requestAcId));
            if (!hasAcId) {
                throw new AcNotFound("User does not have permission for AC ID: ", List.of(requestAcId));
            }
            agpm.setAc(acService.findById(requestAcId));
        } else {
            Optional<UserAc> piloteAc = userAcs.stream()
                    .filter(UserAc::getAcPilote)
                    .findFirst();
            if (piloteAc.isPresent()) {
                agpm.setAc(acService.findById(piloteAc.get().getAcId()));
            } else {
                if (!userAcs.isEmpty()) {
                    agpm.setAc(acService.findById(userAcs.get(0).getAcId()));
                } else {
                    throw new NotFoundException("No AC IDs available to assign");
                }
            }
        }

        if (request.getFiles() != null) {
            documentCommuneService.processAndSaveDocuments(request.getFiles(), agpm, Constants.PATH_ENTREPOT_GED);
        }

        agpm.setIsDeleted(false);
        agpm.setStatus(ProcessStatus.BROUILLON);
        // Set annee from request
        if (request.getAnnee() != null) {
            agpm.setAnnee(request.getAnnee());
        }

        agpm.setActualise(Boolean.FALSE);
        agpm.setNbrActualisation(0);

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
        if (!ProcessStatus.BROUILLON.equals(agpm.getStatus())) {
            throw new AgpmInvalidStatusException("Agpm can only be deleted when status is DRAFT",
                    List.of(agpmId),
                    List.of(agpmVer),
                    agpm.getStatus());
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
        public Agpm updateWithVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request)
                throws AgpmNotFound, DocNotFound, ReferenceNotFound, IOException {
            if (agpmId < 0) {
                throw new IllegalArgumentException("Invalid ID: " + agpmId);
            }

            Integer latestVersion = findLatestVersionById(agpmId)
                    .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID", List.of(agpmId), List.of(agpmVer)));
            CompositeId compositeId = new CompositeId(agpmId, latestVersion);
            Agpm existingAgpm = repository.findById(compositeId)
                    .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID and version", List.of(agpmId), List.of(latestVersion)));

            if (Boolean.TRUE.equals(existingAgpm.getIsDeleted())) {
                throw new IllegalArgumentException(
                        messageSource.getMessage("agpm.deleted.update", new Object[]{agpmId, latestVersion}, getLocal()));
            }

            List<Integer> existingDocIdsRaw = existingAgpm.getDocs() != null
                    ? existingAgpm.getDocs().stream().map(Docs::getId).toList()
                    : new ArrayList<>();
            List<Integer> existingActiveDocIds = existingAgpm.getDocs() != null
                    ? existingAgpm.getDocs().stream()
                    .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                    .map(Docs::getId)
                    .toList()
                    : new ArrayList<>();

            log.debug("Existing ALL doc IDs for Agpm {}:{}: {}", agpmId, latestVersion, existingDocIdsRaw);
            log.debug("Existing ACTIVE (non-deleted) doc IDs for Agpm {}:{}: {}", agpmId, latestVersion, existingActiveDocIds);

            final boolean docIdsProvided = request.getDocIds() != null;
            List<Integer> requestedDocIds = docIdsProvided ? new ArrayList<>(request.getDocIds()) : new ArrayList<>();

            List<Docs> requestedDocs = Collections.emptyList();
            if (docIdsProvided) {
                requestedDocs = docsRepository.findByIdIn(requestedDocIds);
                if (requestedDocIds.size() != requestedDocs.size()) {
                    List<Integer> foundDocIds = requestedDocs.stream().map(Docs::getId).toList();
                    List<Integer> missingDocIds = requestedDocIds.stream()
                            .filter(docId -> !foundDocIds.contains(docId))
                            .collect(Collectors.toList());
                    throw new DocNotFound("Documents not found", missingDocIds);
                }
            }
            log.debug("Requested document IDs (provided? {}): {}", docIdsProvided, requestedDocIds);

            boolean fieldsChanged = false;

            java.util.function.BiConsumer<String, Object[]> logChange =
                    (name, vals) -> log.info("Change detected on field '{}': old='{}' -> new='{}'", name, vals[0], vals[1]);

            if (request.getNom() != null && !Objects.equals(existingAgpm.getNom(), request.getNom())) {
                fieldsChanged = true;
                logChange.accept("nom", new Object[]{existingAgpm.getNom(), request.getNom()});
            }
            if (request.getContenu() != null && !Objects.equals(existingAgpm.getContenu(), request.getContenu())) {
                fieldsChanged = true;
                logChange.accept("contenu", new Object[]{existingAgpm.getContenu(), request.getContenu()});
            }
            if (request.getRemarque() != null && !Objects.equals(existingAgpm.getRemarque(), request.getRemarque())) {
                fieldsChanged = true;
                logChange.accept("remarque", new Object[]{existingAgpm.getRemarque(), request.getRemarque()});
            }
            if (request.getAnnee() != null && !Objects.equals(existingAgpm.getAnnee(), request.getAnnee())) {
                fieldsChanged = true;
                logChange.accept("annee", new Object[]{existingAgpm.getAnnee(), request.getAnnee()});
            }


            boolean acChanged = false;
            Integer newAcId = request.getAcId();
            if (newAcId != null) {
                Integer oldAcId = existingAgpm.getAc() != null ? existingAgpm.getAc().getId() : null;
                if (!Objects.equals(oldAcId, newAcId)) {
                    acChanged = true;
                    fieldsChanged = true;
                    logChange.accept("acId", new Object[]{oldAcId, newAcId});
                }
            }


            boolean docsChanged = false;
            List<Integer> addedDocs = Collections.emptyList();
            List<Integer> removedDocs = Collections.emptyList();
            if (docIdsProvided) {
                Set<Integer> existingSet = new HashSet<>(existingActiveDocIds);
                Set<Integer> requestedSet = new HashSet<>(requestedDocIds);
                if (!existingSet.equals(requestedSet)) {
                    docsChanged = true;
                    addedDocs = requestedSet.stream().filter(id -> !existingSet.contains(id)).sorted().toList();
                    removedDocs = existingSet.stream().filter(id -> !requestedSet.contains(id)).sorted().toList();
                    log.info("Docs change detected. Added={}, Removed={}", addedDocs, removedDocs);
                }
            }

            boolean filesChanged = request.getFiles() != null && !request.getFiles().isEmpty();
            if (filesChanged) {
                log.info("Files change detected: {} new file(s) uploaded.", request.getFiles().size());
            }

            if (!fieldsChanged && !docsChanged && !filesChanged) {
                log.info("No changes detected for Agpm {}:{}. No new version will be created.", agpmId, latestVersion);
                if (existingAgpm.getDocs() != null) {
                    existingAgpm.setDocs(existingAgpm.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .collect(Collectors.toList()));
                }
                return existingAgpm;
            }

            log.info("Changes detected for Agpm {}:{} -> creating new version.", agpmId, latestVersion);
            Integer nextVersion = repository.findMaxVersionById(agpmId);
            nextVersion = (nextVersion != null) ? nextVersion + 1 : 1;

            Agpm newAgpm = new Agpm();
            newAgpm.setId(agpmId);
            newAgpm.setVersion(nextVersion);
            newAgpm.setProcessInstanceId(existingAgpm.getProcessInstanceId());
            newAgpm.setStatus(existingAgpm.getStatus());
            newAgpm.setIsDeleted(existingAgpm.getIsDeleted());

            newAgpm.setRef(existingAgpm.getRef());
            newAgpm.setNom(existingAgpm.getNom());
            newAgpm.setContenu(existingAgpm.getContenu());
            newAgpm.setRemarque(existingAgpm.getRemarque());
            newAgpm.setAnnee(existingAgpm.getAnnee());
            newAgpm.setAc(existingAgpm.getAc());

            agpmMapper.updateAgpmFromRequest(request, newAgpm);

            if (acChanged) {
                List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
                if (userAcIds.isEmpty() || !userAcIds.contains(newAcId)) {
                    throw new AgpmNotAssociatedWithUserAcException(
                            "User is not associated with AC ID: " + newAcId,
                            List.of(agpmId),
                            List.of(nextVersion)
                    );
                }
                try {
                    newAgpm.setAc(acService.findById(newAcId));
                } catch (AcNotFound e) {
                    throw new AgpmNotAssociatedWithUserAcException(
                            "Invalid AC ID: " + newAcId,
                            List.of(agpmId),
                            List.of(nextVersion)
                    );
                }
            }

            List<Docs> nextDocs = new ArrayList<>();
            if (docIdsProvided) {
                // Start from requested docs
                nextDocs.addAll(requestedDocs);
            } else {
                // Keep current active docs as-is
                if (existingAgpm.getDocs() != null) {
                    nextDocs.addAll(existingAgpm.getDocs().stream()
                            .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                            .collect(Collectors.toList()));
                }
            }
            newAgpm.setDocs(nextDocs);

            if (filesChanged) {
                documentCommuneService.processAndSaveDocuments(request.getFiles(), newAgpm, Constants.PATH_ENTREPOT_GED);
                log.debug("New documents added from files to Agpm {}:{}", agpmId, nextVersion);
            }

            if (docIdsProvided) {
                List<Integer> docIdsToDelete = existingActiveDocIds.stream()
                        .filter(docId -> !requestedDocIds.contains(docId))
                        .collect(Collectors.toList());
                log.debug("Documents to soft delete (because docIds provided): {}", docIdsToDelete);

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
            }

            boolean somethingChanged = fieldsChanged || docsChanged || filesChanged;
            if (Boolean.TRUE.equals(existingAgpm.getActualise()) && somethingChanged) {
                newAgpm.setActualise(false);
                log.info("Flag 'actualise' toggled from true -> false due to detected changes.");
            } else {
                newAgpm.setActualise(existingAgpm.getActualise());
                log.info("Flag 'actualise' kept as {}", existingAgpm.getActualise());
            }
            newAgpm.setNbrActualisation(existingAgpm.getNbrActualisation());

            newAgpm.setRef(existingAgpm.getRef());
            newAgpm.setUpdatedAt(Calendar.getInstance());
            Agpm savedAgpm = repository.save(newAgpm);

            Agpm responseAgpm = createFilteredResponseAgpm(savedAgpm);
            log.info("Returning Agpm {}:{} -> new version {} created. Active doc IDs now: {}",
                    agpmId, latestVersion, nextVersion,
                    responseAgpm.getDocs() != null
                            ? responseAgpm.getDocs().stream().map(Docs::getId).collect(Collectors.toList())
                            : List.of());

            if (request.getAnnee() != null) {
                responseAgpm.setAnnee(request.getAnnee());
            }

            return responseAgpm;
        }


    @Override
    @Transactional
    public Agpm updateWithoutVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, DocNotFound, IOException, InvalidIdAndVersionAgpm, AgpmInvalidStateException {
        if (agpmId < 0) {
            throw new InvalidIdAndVersionAgpm("Invalid ID", List.of(agpmId), List.of(agpmVer));
        }

        Integer latestVersion = findLatestVersionById(agpmId)
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID", List.of(agpmId), List.of(agpmVer)));
        CompositeId compositeId = new CompositeId(agpmId, latestVersion);
        Agpm existingAgpm = repository.findById(compositeId) // Fetched from database
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID and version", List.of(agpmId), List.of(latestVersion)));

        if (Boolean.TRUE.equals(existingAgpm.getIsDeleted())) {
            throw new IllegalArgumentException(messageSource.getMessage("agpm.deleted.update", new Object[]{agpmId, latestVersion}, getLocal()));
        }

        restrictUpdateAgpm(existingAgpm);

        Agpm agpm = new Agpm();
        agpm.setId(existingAgpm.getId());
        agpm.setVersion(existingAgpm.getVersion());
        agpm.setAc(existingAgpm.getAc());
        agpm.setStatus(existingAgpm.getStatus());
        agpm.setIsDeleted(existingAgpm.getIsDeleted());
        agpmMapper.updateAgpmFromRequest(request, agpm);

        List<Integer> existingDocIds = existingAgpm.getDocs() != null
                ? existingAgpm.getDocs().stream().map(Docs::getId).toList()
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

        if (!requestedDocs.isEmpty()) {
            existingDocs.removeIf(doc -> requestedDocIds.contains(doc.getId()));
            existingDocs.addAll(requestedDocs);
            agpm.setDocs(existingDocs);
        }

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            documentCommuneService.processAndSaveDocuments(request.getFiles(), agpm, Constants.PATH_ENTREPOT_GED);
            log.debug("New documents added from files to Agpm");
        }

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

        // Check and update acId if provided in request
        Integer newAcId = request.getAcId();
        if (newAcId != null) {
            List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
            if (userAcIds.isEmpty() || !userAcIds.contains(newAcId)) {
                throw new AgpmNotAssociatedWithUserAcException(
                        "User is not associated with AC ID: " + newAcId,
                        List.of(agpmId),
                        List.of(latestVersion)
                );
            }
            try {
                agpm.setAc(acService.findById(newAcId));
            } catch (AcNotFound e) {
                throw new AgpmNotAssociatedWithUserAcException(
                        "Invalid AC ID: " + newAcId,
                        List.of(agpmId),
                        List.of(latestVersion)
                );
            }
        }

        // Retain the old ref from the existingAgpm
        agpm.setRef(existingAgpm.getRef());

        agpm.setActualise(existingAgpm.getActualise());
        agpm.setNbrActualisation(existingAgpm.getNbrActualisation());

        agpm.setUpdatedAt(Calendar.getInstance());
        Agpm updatedAgpm = repository.save(agpm);

        Agpm responseAgpm = createFilteredResponseAgpm(updatedAgpm);
        log.info("Returning Agpm {}:{} with only non-soft-deleted document IDs: {}",
                agpmId, latestVersion, responseAgpm.getDocs().stream().map(Docs::getId).collect(Collectors.toList()));
        // Set annee from request
        if (request.getAnnee() != null) {
            responseAgpm.setAnnee(request.getAnnee());
        }

        return responseAgpm;
    }

    /**
     * Updates the status of an Agpm entity by its ID and version.
     *
     * @param agpmId   ID of the Agpm entity to update
     * @param agpmVer  Version of the Agpm entity to update
     * @param status The new status ID to set
     * @return Updated Agpm entity
     * @throws AgpmNotFound if the Agpm entity is not found
     * @throws ReferenceNotFound if the status ID is not found
     */
    @Override
    @Transactional
    public Agpm updateStatus(Integer agpmId, Integer agpmVer, ProcessStatus status) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound("Agpm not found", List.of(agpmId), List.of(agpmVer)));


        agpm.setStatus(status);

        agpm.setUpdatedAt(Calendar.getInstance());
        return repository.save(agpm);
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
        responseAgpm.setModelAlfrescoId(source.getModelAlfrescoId());
        responseAgpm.setAnnee(source.getAnnee());
        responseAgpm.setProcessInstanceId(source.getProcessInstanceId());
        responseAgpm.setAc(source.getAc());
        responseAgpm.setStatus(source.getStatus());
        responseAgpm.setActualise(source.getActualise());
        responseAgpm.setNbrActualisation(source.getNbrActualisation());
        responseAgpm.setEtapeProcess(source.getEtapeProcess());
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


        boolean isDraft = Objects.equals(agpm.getStatus(), ProcessStatus.BROUILLON);
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

    /**
     * find page of Agpms with Ac Id*/
    @Override
    public Page<Agpm> findAgpmsByAcId(Integer acId,Pageable pageable) throws AcNotFound {
        acService.findById(acId);
        Specification<Agpm> agpmSpec = Specification.where(
                AgpmSpecifications.deleted(false)
                        .and(AgpmSpecifications.isLatestVersion().and(AgpmSpecifications.agpmAc(acId)))
        );
        return repository.findAll(agpmSpec, pageable);

    }



    /**
     * Generates and sets a reference string for the Agpm entity.
     * Format: AGPM_<YEAR>_<ID>
     *
     * @param agpm the Agpm entity to set the reference for
     */
    private void setRef(Agpm agpm) {
        if (agpm == null || agpm.getId() == null) {
            throw new IllegalArgumentException("Cannot set reference: agpm or agpm ID is null.");
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String ref = "AGPM_" + year + "_" + agpm.getId();
        agpm.setRef(ref);
    }

    @Override
    public Page<Agpm> findAllForWebsite(Specification<Agpm> spec, Pageable pageable) {
        Specification<Agpm> finalSpec = Specification.where(spec)
                .and(AgpmSpecifications.isLatestVersion())
                .and(AgpmSpecifications.isPublished())
                .and(AgpmSpecifications.isNotDeleted());
        return repository.findAll(finalSpec, pageable);
    }

    /**
     * Creates a new version from the latest AGPM matching the given ID when it is PUBLIE.
     * The new version has version = latest + 1, inherits core data (ref, nom, contenu, remarque, annee, AC),
     * resets processInstanceId to null, sets status to BROUILLON, marks actualise = true,
     * and increments nbrActualisation by 1. Non-deleted Docs are copied to the new version.
     *
     * @param agpmId ID of the AGPM to actualise
     * @return The persisted AGPM new version containing only non-deleted Docs
     * @throws AgpmNotFound if the latest AGPM cannot be resolved for the given ID
     * @throws AgpmInvalidStatusException if the latest AGPM is not PUBLIE
     * @throws AgpmNotAssociatedWithUserAcException if the AGPM’s AC is not among the current user’s ACs
     */
    @Transactional
    @Override
    public Agpm actualise(Integer agpmId)
            throws AgpmNotFound, AgpmInvalidStatusException, AgpmNotAssociatedWithUserAcException {


        Integer latestVersion = Optional.ofNullable(repository.findMaxVersionById(agpmId))
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID", List.of(agpmId), List.of()));

        Agpm latest = repository.findById(new CompositeId(agpmId, latestVersion))
                .orElseThrow(() -> new AgpmNotFound("No Agpm found for ID and version",
                        List.of(agpmId), List.of(latestVersion)));


        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty() || latest.getAc() == null || !userAcIds.contains(latest.getAc().getId())) {
            throw new AgpmNotAssociatedWithUserAcException(
                    "AGPM not associated with one of the user's ACs",
                    List.of(agpmId), List.of(latestVersion));
        }


        if (!ProcessStatus.PUBLIE.equals(latest.getStatus())) {
            throw new AgpmInvalidStatusException(
                    "Actualisation is only allowed when status is PUBLIE",
                    List.of(latest.getId()),
                    List.of(latest.getVersion()),
                    latest.getStatus());
        }


        int nextVersion = latestVersion + 1;

        Agpm next = new Agpm();
        next.setId(latest.getId());
        next.setVersion(nextVersion);


        next.setRef(latest.getRef());
        next.setNom(latest.getNom());
        next.setContenu(latest.getContenu());
        next.setRemarque(latest.getRemarque());
        next.setAnnee(latest.getAnnee());
        next.setAc(latest.getAc());


        next.setProcessInstanceId(null);
        next.setStatus(ProcessStatus.BROUILLON);
        next.setIsDeleted(false);


        next.setActualise(true);
        Integer prevNbr = latest.getNbrActualisation();
            next.setNbrActualisation((prevNbr == null ? 0 : prevNbr) + 1);


        if (latest.getDocs() != null && !latest.getDocs().isEmpty()) {
            List<Docs> activeDocs = latest.getDocs().stream()
                    .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                    .collect(Collectors.toList());
            next.setDocs(new ArrayList<>(activeDocs));
        } else {
            next.setDocs(new ArrayList<>());
        }


        Agpm saved = repository.save(next);

        return createFilteredResponseAgpm(saved);
    }
}