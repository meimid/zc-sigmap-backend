package com.sigmap.common.paas;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;

import com.sigmap.common.config.Constants;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.paas.exceptions.*;
import com.sigmap.common.paas.mappers.PaaMapper;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.request.PaaRequestGlobal;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.RealisationService;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.request.RealisationRequestForPaa;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service implementation of {@link PaaService}
 */
@Service
@Slf4j
public class PaaServiceImpl implements PaaService {
    private final PaaRepository repository;
    private final RealisationRepository realisationRepository;
    private final RefParamRepository refParamRepository;
    private final DocsRepository docsRepository;
    private final AcRepository acRepository;
    private final LotRepository lotRepository;
    private final RealisationService realisationService;
    private final AlfrescoService alfrescoService;
    private final DocumentCommuneService documentCommuneService;
    private final AcService acService;
    private final CurrentUserService currentUserService;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Value("${alfresco.url}")
    private String alfrescoUrl;

    public PaaServiceImpl(PaaRepository repository, CurrentUserService currentUserService, RealisationRepository realisationRepository, RefParamRepository refParamRepository, DocsRepository docsRepository, AcRepository acRepository, LotRepository lotRepository, RealisationService realisationService, DocumentCommuneService documentCommuneService, AcService acService, AlfrescoService alfrescoService, SequenceGeneratorService sequenceGeneratorService


    ) {
        this.repository = repository;
        this.realisationRepository = realisationRepository;
        this.refParamRepository = refParamRepository;
        this.docsRepository = docsRepository;
        this.acRepository = acRepository;
        this.lotRepository = lotRepository;
        this.realisationService = realisationService;
        this.alfrescoService = alfrescoService;
        this.documentCommuneService = documentCommuneService;
        this.acService = acService;
        this.currentUserService = currentUserService;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    public Paa getPaaOfCurrentYear() {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        return repository.findByCurrentYear(currentYear).orElseThrow(() -> new PaaCurrentYearNotFound("", currentYear));
    }

        @Override
        public List<Paa> findAll() {
            Specification<Paa> spec = PaaSpecifications.deleted(false);
            return repository.findAll(spec);
        }

//    /**
//     * Retrieves a paginated list of non-deleted Paa entities, returning only the latest version for each ID
//     * based on the given specification, filtered by the ACs of the current user. Only non-deleted Docs are included.
//     *
//     * @param spec     Specification to filter Paa entities
//     * @param pageable Pagination and sorting configuration
//     * @return Page of Paa entities with the latest version per ID and only non-deleted Docs
//     * @throws PaaNotFound if no Paa entities are found for the user's ACs
//     * @throws PaaNotAssociatedWithUserAcException if a Paa is not associated with the user's ACs
//     */
//    @Override
//    public Page<Paa> findAll(Specification<Paa> spec, Pageable pageable) throws PaaNotAssociatedWithUserAcException, AcNotFound, PaaNotFound {
//        log.info("Recherche de tous les PAAs avec spécification: {}", spec);
//
//        // Récupérer les IDs des AC de l'utilisateur connecté
//        List<Long> userAcIdsLong = currentUserService.getCurrentUserAcIds();
//        if (userAcIdsLong.isEmpty()) {
//            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté");
//            throw new AcNotFound("Aucun ID AC trouvé pour l'utilisateur connecté", Collections.emptyList());
//        }
//        Set<Integer> userAcIds = userAcIdsLong.stream()
//                .map(Long::intValue)
//                .collect(Collectors.toSet());
//
//        // Vérifier l'existence des AC
//        for (Integer acId : userAcIds) {
//            try {
//                acService.findById(acId);
//            } catch (AcNotFound e) {
//                log.warn("AC non trouvé avec ID: {}", acId);
//                throw e;
//            }
//        }
//
//        Specification<Paa> acFilterSpec = (root, query, criteriaBuilder) ->
//                criteriaBuilder.in(root.get("ac").get("id")).value(userAcIds);
//
//        Specification<Paa> latestVersionSpec = (root, query, criteriaBuilder) -> {
//            Subquery<Integer> subquery = query.subquery(Integer.class);
//            Root<Paa> subRoot = subquery.from(Paa.class);
//            subquery.select(criteriaBuilder.max(subRoot.get("version")))
//                    .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
//            return criteriaBuilder.equal(root.get("version"), subquery);
//        };
//
//        Specification<Paa> combinedSpec = spec != null
//                ? spec.and(acFilterSpec).and(latestVersionSpec)
//                : acFilterSpec.and(latestVersionSpec);
//
//        Page<Paa> allPaas = repository.findAll(combinedSpec, pageable);
//
//        if (allPaas.isEmpty()) {
//            log.warn("Aucun PAA trouvé pour les AC de l'utilisateur: {}", userAcIds);
//            throw new PaaNotFound("Aucun PAA trouvé pour les AC de l'utilisateur: " + userAcIds,
//                    Collections.emptyList(), Collections.emptyList());
//        }
//
//        // Transformer les PAAs pour inclure uniquement les documents non supprimés
//        Page<Paa> paasResponse = allPaas.map(this::createFilteredResponsePaa);
//
//        log.info("Récupération de {} PAAs pour les AC de l'utilisateur {}", paasResponse.getContent().size(), userAcIds);
//
//        return paasResponse;
//    }

    public Page<Paa> findAllDeleted(Specification<Paa> spec, Pageable pageable) {
        Specification<Paa> deletedSpec = PaaSpecifications.deleted(true);

        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Paa> findAllDeleted(Pageable pageable) {
        Specification<Paa> spec = PaaSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Paa> findDeletedById(Integer paaId, Integer paaVer) {
        Specification<Paa> spec = PaaSpecifications.id(new CompositeId(paaId, paaVer)).and(PaaSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Paa> findOne(Specification<Paa> specification) {
        return repository.findOne(specification);
    }

    /**
     * Retrieves the latest version of a Paa entity by its ID. Only non-deleted Docs are included in the response,
     * and the Paa must be associated with one of the ACs of the current user.
     *
     * @param paaId  ID of the Paa entity
     * @param paaVer Ignored parameter (kept for compatibility); latest version is fetched
     * @return Paa entity with the latest version and only non-deleted Docs
     * @throws PaaNotFound                         if no Paa entity is found for the ID
     * @throws AcNotFound                          if no AC is found for the current user
     * @throws PaaNotAssociatedWithUserAcException if the Paa is not associated with the user's ACs
     */
    @Override
    public Optional<Paa> findById(Integer paaId, Integer paaVer) throws PaaNotFound, AcNotFound, PaaNotAssociatedWithUserAcException {
        log.info("Recherche du PAA avec ID: {}", paaId);

        // Step 1: Get user's AC IDs
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds == null || userAcIds.isEmpty()) {
            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté. userAcIdsLong: {}", userAcIds);
            throw new AcNotFound("Aucun ID AC trouvé pour l'utilisateur connecté", Collections.emptyList());
        }

        if (userAcIds.isEmpty()) {
            log.warn("Aucun ID AC valide après conversion pour l'utilisateur connecté");
            throw new AcNotFound("Aucun ID AC valide après conversion", Collections.emptyList());
        }
        log.debug("ACs de l'utilisateur connecté: {}", userAcIds);

        // Step 2: Validate ACs
        for (Integer acId : userAcIds) {
            try {
                log.debug("Validation de l'AC ID: {}", acId);
                acService.findById(acId);
            } catch (AcNotFound e) {
                log.warn("AC non trouvé avec ID: {}", acId);
                throw e;
            }
        }

        // Step 3: Find the latest version
        Integer latestVersion = repository.findMaxVersionById(paaId);
        if (latestVersion == null) {
            log.warn("Aucun PAA trouvé pour l'ID: {}", paaId);
            throw new PaaNotFound("Aucun PAA trouvé pour l'ID", List.of(paaId), paaVer != null ? List.of(paaVer) : Collections.emptyList());
        }
        log.debug("Dernière version du PAA ID {}: {}", paaId, latestVersion);

        // Step 4: Retrieve the PAA
        Optional<Paa> paaOpt = repository.findById(new CompositeId(paaId, latestVersion));
        if (paaOpt.isEmpty()) {
            log.warn("Aucun PAA trouvé pour l'ID: {} et la version: {}", paaId, latestVersion);
            throw new PaaNotFound("Aucun PAA trouvé pour l'ID et la version", List.of(paaId), List.of(latestVersion));
        }
        Paa paa = paaOpt.get();
        log.debug("PAA récupéré: ID={}, Version={}, AC={}", paa.getId(), paa.getVersion(), paa.getAc() != null ? paa.getAc().getId() : "null");

        // Step 5: Check if the PAA is deleted (if applicable)
        if (Boolean.TRUE.equals(paa.getIsDeleted())) {
            log.warn("PAA avec ID {} est marqué comme supprimé", paaId);
            throw new PaaNotFound("PAA est supprimé", List.of(paaId), List.of(latestVersion));
        }

        // Step 6: Validate AC association
        Integer paaAcId = paa.getAc() != null ? paa.getAc().getId() : null;
        log.debug("Validation de l'association AC: PAA AC ID={}, User ACs={}", paaAcId, userAcIds);
        if (paaAcId == null || !userAcIds.contains(paaAcId)) {
            log.warn("PAA avec ID {} n'est pas associé à un AC de l'utilisateur. AC du PAA: {}, ACs de l'utilisateur: {}", paaId, paaAcId, userAcIds);
            throw new PaaNotAssociatedWithUserAcException(
                    "Le PAA avec l'ID " + paaId + " n'est pas associé à un AC de l'utilisateur connecté",
                    List.of(paaId.longValue()),
                    List.of(latestVersion)
            );
        }

        // Step 7: Final validation before return
        if (paa.getAc() == null || !userAcIds.contains(paa.getAc().getId())) {
            log.warn("Validation finale échouée: PAA avec ID {} n'est pas associé à un AC de l'utilisateur. AC du PAA: {}, ACs de l'utilisateur: {}", paaId, paa.getAc() != null ? paa.getAc().getId() : "null", userAcIds);
            throw new PaaNotAssociatedWithUserAcException(
                    "Le PAA avec l'ID " + paaId + " n'est pas associé à un AC de l'utilisateur connecté (validation finale)",
                    List.of(paaId.longValue()),
                    List.of(latestVersion)
            );
        }

        // Step 8: Prepare and return the response
        Paa responsePaa = createFilteredResponsePaa(paa);
        log.info("PAA avec ID {} récupéré avec succès pour l'utilisateur", paaId);
        return Optional.ofNullable(responsePaa);
    }

    private Paa createFilteredResponsePaa(Paa paa) {
        if (paa == null) {
            log.warn("PAA fourni est null");
            return null; // Should not occur given prior validation
          }

        Paa filteredPaa = new Paa();
        filteredPaa.setId(paa.getId());
        filteredPaa.setVersion(paa.getVersion());
        filteredPaa.setAc(paa.getAc());
        filteredPaa.setCreatedAt(paa.getCreatedAt());
        filteredPaa.setCreatedBy(paa.getCreatedBy());
        filteredPaa.setUpdatedAt(paa.getUpdatedAt());
        filteredPaa.setUpdatedBy(paa.getUpdatedBy());
        filteredPaa.setIsDeleted(paa.getIsDeleted());
        filteredPaa.setDeletedBy(paa.getDeletedBy());
        filteredPaa.setDeletedAt(paa.getDeletedAt());
        filteredPaa.setProcessInstanceId(paa.getProcessInstanceId());
        filteredPaa.setRealisations(paa.getRealisations());

        if (paa.getDocs() != null) {
            List<Docs> nonDeletedDocs = paa.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList());
            filteredPaa.setDocs(nonDeletedDocs);
        }

        return filteredPaa;
    }

    /**
     * Retrieves a paginated list of non-deleted Paa entities, returning only the latest version for each ID
     * based on the given specification, filtered by the ACs of the current user. Only non-deleted Docs are included.
     *
     * @param spec     Specification to filter Paa entities
     * @param pageable Pagination and sorting configuration
     * @return Page of Paa entities with the latest version per ID and only non-deleted Docs
     * @throws PaaNotFound                         if no Paa entities are found for the user's ACs
     * @throws PaaNotAssociatedWithUserAcException if a Paa is not associated with the user's ACs
     * @throws AcNotFound                          if no AC is found for the current user
     */
    @Override
    public Page<Paa> findAll(Specification<Paa> spec, Pageable pageable) throws PaaNotAssociatedWithUserAcException, AcNotFound, PaaNotFound {
        log.info("Recherche de tous les PAAs avec spécification: {}", spec);

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

        // First, check if any non-deleted Paa entities exist at all (without AC filter)
        Specification<Paa> deletedSpec = (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("isDeleted"));
        Specification<Paa> latestVersionSpec = (root, query, criteriaBuilder) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Paa> subRoot = subquery.from(Paa.class);
            subquery.select(criteriaBuilder.max(subRoot.get("version")))
                    .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            return criteriaBuilder.equal(root.get("version"), subquery);
        };
        Specification<Paa> baseSpec = deletedSpec.and(latestVersionSpec);
        if (spec != null) {
            baseSpec = baseSpec.and(spec);
        }

        Page<Paa> allPaasWithoutAcFilter = repository.findAll(baseSpec, pageable);
        if (allPaasWithoutAcFilter.isEmpty()) {
            log.warn("Aucun PAA non supprimé trouvé dans la base de données");
            throw new PaaNotFound("Aucun PAA non supprimé trouvé dans la base de données", Collections.emptyList(), Collections.emptyList());
        }

        // Now apply AC filter
        Specification<Paa> acFilterSpec = (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("ac").get("id")).value(userAcIds);

        Specification<Paa> combinedSpec = baseSpec.and(acFilterSpec);
        Page<Paa> allPaas = repository.findAll(combinedSpec, pageable);

        if (allPaas.isEmpty()) {
            // Since we know Paa entities exist (from the earlier check), the emptiness must be due to AC association
            log.warn("Aucun PAA associé aux ACs de l'utilisateur: {}", userAcIds);
            throw new PaaNotAssociatedWithUserAcException(
                    "Aucun PAA associé aux ACs de l'utilisateur: " + userAcIds,
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        // Double-check AC association for each Paa (for consistency with findById)
        List<Integer> invalidPaaIds = new ArrayList<>();
        List<Integer> invalidVersions = new ArrayList<>();
        for (Paa paa : allPaas.getContent()) {
            if (paa.getAc() == null || !userAcIds.contains(paa.getAc().getId())) {
                invalidPaaIds.add(paa.getId());
                invalidVersions.add(paa.getVersion());
            }
        }
        if (!invalidPaaIds.isEmpty()) {
            log.warn("Certains PAAs ne sont pas associés aux ACs de l'utilisateur: IDs={}, Versions={}", invalidPaaIds, invalidVersions);
            throw new PaaNotAssociatedWithUserAcException(
                    "Certains PAAs ne sont pas associés aux ACs de l'utilisateur",
                    invalidPaaIds.stream().map(Integer::longValue).collect(Collectors.toList()),
                    invalidVersions
            );
        }

        Page<Paa> paasResponse = allPaas.map(this::createFilteredResponsePaa);
        log.info("Récupération de {} PAAs pour les AC de l'utilisateur {}", paasResponse.getContent().size(), userAcIds);

        return paasResponse;
    }
    @Override
    public Optional<Paa> findLatestById(Integer paaId) {
        // Find the maximum version for this PAA ID
        Integer maxVersion = repository.findMaxVersionById(paaId);

        if (maxVersion == null) {
            // No versions found
            return Optional.empty();
        }

        // Return the PAA with the latest version
        return repository.findById(new CompositeId(paaId, maxVersion));
    }

    @Override
    public Paa save(Paa paa) {
        return repository.save(paa);
    }

    /**
     * Restricts creation of a new PAA if one already exists for the current year.
     *
     * @throws PaaAlreadyExistsException if a PAA already exists for the current year
     */
    private void restrictPaaCreationForCurrentYear() throws PaaCreationLimitExceededException {
        int currentYear = LocalDate.now().getYear();
        Specification<Paa> spec = PaaSpecifications.annee(String.valueOf(currentYear)).and(PaaSpecifications.deleted(false));

        List<Paa> existingPaas = repository.findAll(spec);
        if (existingPaas.size() >= 1) { // Limit is 1 Paa per year
            throw new PaaCreationLimitExceededException("The creation limit of 1 PAA per year has been exceeded for year " + currentYear + ".", existingPaas.get(0).getId(), existingPaas.get(0).getVersion(), 1);
        }
    }

    @Override
    public Paa create(PaaRequest request) throws PaaAlreadyExistsException {

        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        // Always generate the id using the sequence
        Integer id = sequenceGeneratorService.getNextId(Constants.PAA_ID_SEQ);

        // Remove the old findMaxId logic and the id++ logic:
        // If the sequence is working, it always returns the next available number!

        Optional<Paa> existingPaa = repository.findById(new CompositeId(id, version));
        if (existingPaa.isPresent()) {
            throw new PaaAlreadyExistsException("Paa with ID " + id + " and version " + version + " already exists.", id, version);
        }

        Paa paa = PaaMapper.INSTANCE.toEntity(request);
        paa.setRealisations(new ArrayList<>());

        paa.setStatus(ProcessStatus.BROUILLON);
        paa.setModePassation(request.getModePassation());
        paa.setRemarque(request.getRemarque());
        paa.setAnnee(String.valueOf(LocalDate.now().getYear()));

        // Only set the id and version here
        paa.setId(id);
        paa.setVersion(version);

        log.info("Paa before saving: id={}, version={}, annee={}", paa.getId(), paa.getVersion(), paa.getAnnee());
        Paa savedPaa = repository.save(paa);
        log.info("Paa after saving: id={}, version={}, annee={}", savedPaa.getId(), savedPaa.getVersion(), savedPaa.getAnnee());

        if (request.getRealisationIds() != null && !request.getRealisationIds().isEmpty()) {
            List<Realisation> realisations = realisationRepository.findAllById(request.getRealisationIds());
            if (realisations.size() != request.getRealisationIds().size()) {
                throw new RuntimeException("Some realisations were not found");
            }
            for (Realisation realisation : realisations) {
                if (realisation.getPaas() == null) {
                    realisation.setPaas(new ArrayList<>());
                }
                if (!realisation.getPaas().contains(savedPaa)) {
                    realisation.getPaas().add(savedPaa);
                }
            }
            realisationRepository.saveAll(realisations);
            savedPaa.setRealisations(realisations);
            savedPaa = repository.save(savedPaa);
        }
        return savedPaa;
    }

    /**
     * Soft deletes a Paa entity by its ID and version, only if the status is DRAFT.
     *
     * @param paaId  ID of the Paa entity to delete
     * @param paaVer Version of the Paa entity to delete
     * @throws PaaNotFound               if the Paa entity is not found
     * @throws PaaSoftDelete             if the soft delete operation fails
     * @throws PaaInvalidStatusException if the Paa status is not DRAFT
     */
    @Override
    public void delete(Integer paaId, Integer paaVer) throws PaaNotFound, PaaSoftDelete, PaaInvalidStatusException {
        CompositeId compositeId = new CompositeId(paaId, paaVer);
        Optional<Paa> existingPaa = repository.findById(compositeId);
        if (existingPaa.isEmpty()) {
            throw new PaaNotFound("Paa not found", List.of(paaId), List.of(paaVer));
        }

        Paa paa = existingPaa.get();
        if (!ProcessStatus.BROUILLON.equals(paa.getStatus())) {
            throw new PaaInvalidStatusException("Paa can only be deleted when status is DRAFT", List.of(paaId), List.of(paaVer), paa.getStatus());
        }

        Integer rowsAffected = repository.softDelete(paaId, paaVer, Calendar.getInstance());
        if (rowsAffected != 1) {
            throw new PaaSoftDelete("Soft delete failed", rowsAffected, paaId, paaVer);
        }
    }

    /**
     * Checks if the PAA is in DRAFT status and has a null processInstanceId.
     *
     * @param paa The PAA entity to check
     * @throws PaaInvalidStateException if the PAA is not in DRAFT status or has a non-null processInstanceId
     */
    private void restrictUpdatePaa(Paa paa) throws PaaInvalidStateException {

        boolean isDraft = Objects.equals(paa.getStatus(), ProcessStatus.BROUILLON);
        boolean hasNoProcessInstance = paa.getProcessInstanceId() == null;

        if (!isDraft || !hasNoProcessInstance) {
            StringBuilder message = new StringBuilder("PAA with ID " + paa.getId() + " and version " + paa.getVersion() + " cannot be updated because it does not meet the required state: ");
            if (!isDraft) {
                message.append("it is not in DRAFT status");
            }
            if (!isDraft && !hasNoProcessInstance) {
                message.append(" and ");
            }
            if (!hasNoProcessInstance) {
                message.append("it has an active process instance (processInstanceId: " + paa.getProcessInstanceId() + ")");
            }
            message.append(".");

            throw new PaaInvalidStateException(message.toString(), List.of(paa.getId()), List.of(paa.getVersion()));
        }
    }

    @Override
    public Paa update(Integer paaId, Integer paaVer, PaaRequestGlobal request) throws PaaNotFound, PaaInvalidStateException {
        System.out.println("calling the method");
        CompositeId compositeId = new CompositeId(paaId, paaVer);
        Optional<Paa> existingPaaOpt = repository.findById(compositeId);

        if (existingPaaOpt.isEmpty()) {
            throw new PaaNotFound(
                    "Paa with ID " + paaId + " and version " + paaVer + " was not found in the database.",
                    List.of(paaId), List.of(paaVer));
        }

        Paa existingPaa = existingPaaOpt.get();
        if (Boolean.TRUE.equals(existingPaa.getIsDeleted())) {
            throw new PaaNotFound(
                    "Cannot update a deleted PAA with ID " + paaId + " and version " + paaVer,
                    List.of(paaId), List.of(paaVer));
        }

        // Check if PAA is in DRAFT status and has null processInstanceId
        restrictUpdatePaa(existingPaa);

//        existingPaa.setRef(request.getRef());
        existingPaa.setNom(request.getNom());
        existingPaa.setAnnee(existingPaa.getAnnee());
        existingPaa.setBudgetEstime(request.getBudgetEstime());
        existingPaa.setModePassation(request.getModePassation());
        existingPaa.setRemarque(request.getRemarque());
        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new EntityNotFoundException("Ac with ID " + request.getAcId() + " not found"));
            existingPaa.setAc(ac);
        }

        if (request.getDocIds() != null) {
            Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());
            Set<Integer> activeExistingDocIds = existingPaa.getDocs() != null ?
                    existingPaa.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .map(Docs::getId)
                            .collect(Collectors.toSet()) : new HashSet<>();

            if (!requestedDocIds.equals(activeExistingDocIds)) {
                System.out.println("Document changes detected for PAA update ID: " + paaId);
                Set<Integer> newDocIds = new HashSet<>(requestedDocIds);
                newDocIds.removeAll(activeExistingDocIds);
                if (!newDocIds.isEmpty()) {
                    System.out.println("  - New documents added: " + newDocIds);
                }
                Set<Integer> removedDocIds = new HashSet<>(activeExistingDocIds);
                removedDocIds.removeAll(requestedDocIds);
                if (!removedDocIds.isEmpty()) {
                    System.out.println("  - Documents marked as deleted: " + removedDocIds);
                }
            }

            List<Docs> updatedDocs = new ArrayList<>();
            if (existingPaa.getDocs() != null) {
                for (Docs existingDoc : existingPaa.getDocs()) {
                    if (requestedDocIds.contains(existingDoc.getId())) {
                        existingDoc.setIsDeleted(false);
                        docsRepository.save(existingDoc);
                        updatedDocs.add(existingDoc);
                        requestedDocIds.remove(existingDoc.getId());
                    } else {
                        existingDoc.setIsDeleted(true);
                        docsRepository.save(existingDoc);
                        updatedDocs.add(existingDoc);
                        System.out.println("Marked document with ID " + existingDoc.getId() + " as deleted (not in request)");
                    }
                }
            }

            for (Integer docId : requestedDocIds) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                if (doc.getPaas() == null) {
                    doc.setPaas(new ArrayList<>());
                }
                if (!doc.getPaas().contains(existingPaa)) {
                    doc.getPaas().add(existingPaa);
                }
                doc.setIsDeleted(false);
                docsRepository.save(doc);
                updatedDocs.add(doc);
                System.out.println("Added new document with ID " + doc.getId() + " to PAA");
            }
            existingPaa.setDocs(updatedDocs);
        } else if (existingPaa.getDocs() != null && !existingPaa.getDocs().isEmpty()) {
            List<Docs> existingDocs = existingPaa.getDocs();
            for (Docs doc : existingDocs) {
                if (!Boolean.TRUE.equals(doc.getIsDeleted())) {
                    doc.setIsDeleted(true);
                    docsRepository.save(doc);
                    System.out.println("Marked document with ID " + doc.getId() + " as deleted (no docIds provided)");
                }
            }
        }

        if (request.getPaaFiles() != null) {
            try {
               // processAndSaveDocuments(request.getPaaFiles(), existingPaa);
                documentCommuneService.processAndSaveDocuments(request.getPaaFiles(),existingPaa,Constants.PATH_ENTREPOT_GED_PAA);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            List<Realisation> realisations = new ArrayList<>();
            for (RealisationRequestForPaa realisationRequest : request.getRealisations()) {
                Realisation realisation;
                if (realisationRequest.getId() != null) {
                    Integer versionToUse = realisationRequest.getVersion();
                    if (versionToUse == null) {
                        Integer maxVersion = realisationRepository.findMaxVersionById(realisationRequest.getId());
                        if (maxVersion != null) {
                            versionToUse = maxVersion;
                        } else {
                            throw new RuntimeException("No versions found for Realisation with ID: " + realisationRequest.getId());
                        }
                    }
                    CompositeId realisationId = new CompositeId(realisationRequest.getId(), versionToUse);
                    realisation = realisationRepository.findById(realisationId)
                            .orElseThrow(() -> new RuntimeException("Realisation not found with ID: " + realisationId.getId()));
                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                    realisation.setService(realisationRequest.getService());
                    realisation.setDirection(realisationRequest.getDirection());
                    realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
                    realisation.setMonais(realisationRequest.getMonais());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                    realisation.setModeSelection(realisationRequest.getModeSelection());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort());

                    if (realisationRequest.getDocIds() != null) {
                        Set<Integer> requestedRealisationDocIds = new HashSet<>(realisationRequest.getDocIds());
                        Set<Integer> activeExistingRealisationDocIds = realisation.getDocs() != null ?
                                realisation.getDocs().stream()
                                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                        .map(Docs::getId)
                                        .collect(Collectors.toSet()) : new HashSet<>();
                        if (!requestedRealisationDocIds.equals(activeExistingRealisationDocIds)) {
                            System.out.println("Document changes detected for Realisation update ID: " + realisationRequest.getId());
                        }
                        List<Docs> updatedRealisationDocs = new ArrayList<>();
                        if (realisation.getDocs() != null) {
                            for (Docs existingDoc : realisation.getDocs()) {
                                if (requestedRealisationDocIds.contains(existingDoc.getId())) {
                                    existingDoc.setIsDeleted(false);
                                    docsRepository.save(existingDoc);
                                    updatedRealisationDocs.add(existingDoc);
                                    requestedRealisationDocIds.remove(existingDoc.getId());
                                } else {
                                    existingDoc.setIsDeleted(true);
                                    docsRepository.save(existingDoc);
                                    updatedRealisationDocs.add(existingDoc);
                                    System.out.println("Marked realisation document with ID " + existingDoc.getId() + " as deleted (not in request)");
                                }
                            }
                        }
                        for (Integer docId : requestedRealisationDocIds) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                            if (doc.getRealisations() == null) {
                                doc.setRealisations(new ArrayList<>());
                            }
                            if (!doc.getRealisations().contains(realisation)) {
                                doc.getRealisations().add(realisation);
                            }
                            doc.setIsDeleted(false);
                            docsRepository.save(doc);
                            updatedRealisationDocs.add(doc);
                            System.out.println("Added new document with ID " + doc.getId() + " to Realisation");
                        }
                        realisation.setDocs(updatedRealisationDocs);
                    } else if (realisation.getDocs() != null && !realisation.getDocs().isEmpty()) {
                        List<Docs> realisationDocs = realisation.getDocs();
                        for (Docs doc : realisationDocs) {
                            if (!Boolean.TRUE.equals(doc.getIsDeleted())) {
                                doc.setIsDeleted(true);
                                docsRepository.save(doc);
                                System.out.println("Marked realisation document with ID " + doc.getId() + " as deleted (no docIds provided)");
                            }
                        }
                    }

                    // Process uploaded files AFTER setting the docIds documents
                    if (realisationRequest.getRealisationFiles() != null) {
                        try {
                            // Get current docs list to preserve it
                            List<Docs> currentDocs = realisation.getDocs();
                            if (currentDocs == null) {
                                currentDocs = new ArrayList<>();
                            }

                            // Process the uploaded files
                            //processAndSaveRelisationsDocumentsForUpdate(realisationRequest.getRealisationFiles(), realisation);
                            documentCommuneService.processAndSaveDocuments(realisationRequest.getRealisationFiles(),realisation,Constants.PATH_ENTREPOT_GED_PAA+"/"+existingPaa.getId()+"/"+existingPaa.getVersion()+Constants.PATH_ENTREPOT_GED_REALISATION);


                            // After processing, get the new docs and merge with existing ones
                            List<Docs> newDocs = realisation.getDocs();
                            if (newDocs != null) {
                                // Create a set to track existing doc IDs to avoid duplicates
                                Set<Integer> existingDocIds = currentDocs.stream()
                                        .map(Docs::getId)
                                        .collect(Collectors.toSet());

                                // Add only new documents that weren't already in the list
                                for (Docs newDoc : newDocs) {
                                    if (!existingDocIds.contains(newDoc.getId())) {
                                        currentDocs.add(newDoc);
                                    }
                                }

                                // Set the combined list back
                                realisation.setDocs(currentDocs);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (realisation.getPaas() == null) {
                        realisation.setPaas(new ArrayList<>());
                    }
                    if (!realisation.getPaas().contains(existingPaa)) {
                        realisation.getPaas().add(existingPaa);
                    }

                    if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                        Set<Lot> lots = new HashSet<>();
                        for (LotResponse lotRequest : realisationRequest.getLots()) {
                            Lot lot = new Lot();
                            Integer lotId = lotRequest.getId();
                            if (lotId == null) {
//                                Integer maxLotId = lotRepository.findMaxId();
                                Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                                lotId = (maxLotId != null) ? maxLotId + 1 : 1;
                            }
                            lot.setId(lotId);
                            lot.setVersion(1);
                            lot.setDescription(lotRequest.getDescription());
                            lot = lotRepository.save(lot);
                            lots.add(lot);
                        }
                        realisation.setLots(lots);
                    }

                    realisation = realisationRepository.save(realisation);
                } else {
                    realisation = new Realisation();
//                    Integer maxRealisationId = realisationRepository.findMaxId();
                    Integer maxRealisationId =  sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);;
                    Integer realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;
                    realisation.setId(realisationId);
                    realisation.setVersion(1);
                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                    realisation.setService(realisationRequest.getService());
                    realisation.setDirection(realisationRequest.getDirection());
                    realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
                    realisation.setMonais(realisationRequest.getMonais());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                    realisation.setModeSelection(realisationRequest.getModeSelection());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort());

                    if (realisationRequest.getRealisationFiles() != null) {
                        try {
                            //processAndSaveRelisationsDocuments(realisationRequest.getRealisationFiles(), realisation);
                            documentCommuneService.processAndSaveDocuments(realisationRequest.getRealisationFiles(),realisation,Constants.PATH_ENTREPOT_GED_PAA+"/"+existingPaa.getId()+"/"+existingPaa.getVersion()+Constants.PATH_ENTREPOT_GED_REALISATION);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    List<Paa> paaList = new ArrayList<>();
                    paaList.add(existingPaa);
                    realisation.setPaas(paaList);

                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        List<Docs> docs = new ArrayList<>();
                        for (Integer docId : realisationRequest.getDocIds()) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                            if (doc.getRealisations() == null) {
                                doc.setRealisations(new ArrayList<>());
                            }
                            if (!doc.getRealisations().contains(realisation)) {
                                doc.getRealisations().add(realisation);
                            }
                            doc.setIsDeleted(false);
                            docsRepository.save(doc);
                            docs.add(doc);
                        }
                        realisation.setDocs(docs);
                    }

                    if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                        Set<Lot> lots = new HashSet<>();
                        for (LotResponse lotRequest : realisationRequest.getLots()) {
                            Lot lot = new Lot();
                            Integer lotId = lotRequest.getId();
                            if (lotId == null) {
//                                Integer maxLotId = lotRepository.findMaxId();
                                Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                                lotId = (maxLotId != null) ? maxLotId + 1 : 1;
                            }
                            lot.setId(lotId);
                            lot.setVersion(1);
                            lot.setDescription(lotRequest.getDescription());
                            lot = lotRepository.save(lot);
                            lots.add(lot);
                        }
                        realisation.setLots(lots);
                    }

                    realisation = realisationRepository.save(realisation);
                }
                realisations.add(realisation);
            }
            existingPaa.setRealisations(realisations);
        } else {
            existingPaa.getRealisations().clear();
        }

        Paa savedPaa = repository.save(existingPaa);
        Paa copyForResponse = new Paa();
        BeanUtils.copyProperties(savedPaa, copyForResponse);
        if (savedPaa.getDocs() != null) {
            copyForResponse.setDocs(
                    savedPaa.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .collect(Collectors.toList())
            );
        }
        if (copyForResponse.getRealisations() != null) {
            copyForResponse.getRealisations().forEach(realisation -> {
                if (realisation.getDocs() != null) {
                    realisation.setDocs(
                            realisation.getDocs().stream()
                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                    .collect(Collectors.toList())
                    );
                }
            });
        }
        return copyForResponse;
    }


    private boolean hasRealisationFiles(List<RealisationRequestForPaa> realisations) {
        if (realisations == null) {
            return false;
        }

        for (RealisationRequestForPaa real : realisations) {
            if (real.getRealisationFiles() != null && !real.getRealisationFiles().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Transactional
    @Override
    public Paa updateWithVersioning(Integer paaId, Integer paaVer, PaaRequestGlobal request) throws PaaNotFound {
        // Find the existing PAA
        CompositeId compositeId = new CompositeId(paaId, paaVer);
        Paa existingPaa = repository.findById(compositeId).orElseThrow(() -> new PaaNotFound("PAA with ID " + paaId + " and version " + paaVer + " not found", List.of(paaId), List.of(paaVer)));

        if (Boolean.TRUE.equals(existingPaa.getIsDeleted())) {
            throw new PaaNotFound("Cannot update a deleted PAA with ID " + paaId + " and version " + paaVer, List.of(paaId), List.of(paaVer));
        }

        // Log the current state and request
        System.out.println("Updating PAA ID: " + paaId + ", Current Version: " + paaVer);
        System.out.println("Request data: " + request);

        // Track whether any documents have changed
        boolean documentsChanged = false;

        // Process any document changes
        if (request.getDocIds() != null) {
            // Get only the active (non-deleted) existing document IDs
            Set<Integer> activeExistingDocIds = existingPaa.getDocs() != null ? existingPaa.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).map(Docs::getId).collect(Collectors.toSet()) : new HashSet<>();

            // Compare with requested document IDs
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());
            documentsChanged = !requestDocIds.equals(activeExistingDocIds);

            if (documentsChanged) {
                System.out.println("Document changes detected:");
                System.out.println("  - Active existing docs: " + activeExistingDocIds);
                System.out.println("  - Requested docs: " + requestDocIds);
            }
        }

        // Get hash for the current request
        String requestHash = generateRequestHash(request);
        String previousRequestHash = getPreviousRequestHash(paaId, paaVer);


        // Check for new lots without IDs to force versioning
        boolean hasNewLotsWithoutIds = false;
        if (request.getRealisations() != null) {
            for (RealisationRequestForPaa realisationDto : request.getRealisations()) {
                if (realisationDto.getLots() != null) {
                    hasNewLotsWithoutIds |= realisationDto.getLots().stream().anyMatch(lot -> lot.getId() == null);
                }
            }
        }
// Check if files are present - if so, we'll always create a new version
        boolean hasFiles = request.getPaaFiles() != null && !request.getPaaFiles().isEmpty();
        if (hasFiles) {
            System.out.println("Files detected in request - will create new version regardless of other changes");
        } else {
            // Only check hash if no files are present
            System.out.println("Current request hash: " + requestHash);
            System.out.println("Previous request hash: " + previousRequestHash);

            // If this is the exact same request as the previous one, return the existing PAA without changes
            boolean hasRealisationFiles = hasRealisationFiles(request.getRealisations());
            if (requestHash != null && requestHash.equals(previousRequestHash) && !documentsChanged && !hasNewLotsWithoutIds && !hasRealisationFiles) {
                System.out.println("Duplicate request detected and no document changes, returning existing PAA without changes");

                // Create a filtered copy for response
                Paa copyForResponse = new Paa();
                BeanUtils.copyProperties(existingPaa, copyForResponse);

                // Filter out deleted documents for the response
                if (existingPaa.getDocs() != null) {
                    copyForResponse.setDocs(existingPaa.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                }

                // Also filter realisation documents
                if (copyForResponse.getRealisations() != null) {
                    copyForResponse.getRealisations().forEach(realisation -> {
                        if (realisation.getDocs() != null) {
                            realisation.setDocs(realisation.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                        }

                        // Also filter lots in realisations
                        if (realisation.getLots() != null) {
                            realisation.setLots(realisation.getLots().stream().filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted())).collect(Collectors.toSet()));
                        }
                    });
                }

                return copyForResponse;
            }
        }

        // Create a temporary PAA to check for changes in direct fields
        Paa tempPaa = new Paa();

        // Only use request values when they're not null, otherwise keep existing values

//        tempPaa.setRef(request.getRef() != null ? request.getRef() : existingPaa.getRef());
        tempPaa.setNom(request.getNom() != null ? request.getNom() : existingPaa.getNom());
        tempPaa.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPaa.getAnnee());
        tempPaa.setProcessInstanceId(existingPaa.getProcessInstanceId());
        tempPaa.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPaa.getBudgetEstime());
        tempPaa.setStatus(existingPaa.getStatus());
        tempPaa.setModePassation(request.getModePassation() != null ? request.getModePassation() : existingPaa.getModePassation());
        tempPaa.setRemarque(request.getRemarque() != null ? request.getRemarque() : existingPaa.getRemarque());

        // Handle AC
        Ac requestAc = null;
        if (request.getAcId() != null) {
            requestAc = acRepository.findById(request.getAcId()).orElse(null);
        }
        tempPaa.setAc(requestAc != null ? requestAc : existingPaa.getAc());

        // Check for changes in direct PAA fields
        boolean hasDirectChanges = !arePaaFieldsIdentical(existingPaa, tempPaa);
        System.out.println("Direct field changes detected: " + hasDirectChanges);

        // Track whether any realisations have changed
        boolean realisationsChanged = false;

        // Prepare lists for relationships
        List<Realisation> updatedRealisations = new ArrayList<>();
        List<Docs> updatedDocs = new ArrayList<>();

        // Keep track of newly created lots to detect duplicates
        Map<String, Lot> newLotsMap = new HashMap<>();

        // Process Realisation DTOs
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            System.out.println("Processing " + request.getRealisations().size() + " realisations");
            for (RealisationRequestForPaa realisationDto : request.getRealisations()) {
                try {
                    System.out.println("Processing realisation DTO: " + realisationDto);
                    System.out.println("Realisation ID: " + realisationDto.getId() + ", Lots: " + (realisationDto.getLots() != null ? realisationDto.getLots().size() : "null"));

                    if (realisationDto.getId() != null) {
                        Integer versionToUse = realisationDto.getVersion();
                        if (versionToUse == null) {
                            Integer maxVersion = realisationRepository.findMaxVersionById(realisationDto.getId());
                            if (maxVersion != null) {
                                versionToUse = maxVersion;
                                System.out.println("Found latest version for Realisation " + realisationDto.getId() + ": " + maxVersion);
                            } else {
                                throw new RuntimeException("No versions found for Realisation with ID: " + realisationDto.getId());
                            }
                        }

                        CompositeId realisationId = new CompositeId(realisationDto.getId(), versionToUse);
                        Realisation existingRealisation = realisationRepository.findById(realisationId).orElseThrow(() -> new RuntimeException("Realisation not found with ID: " + realisationId.getId()));

                        System.out.println("Found existing Realisation: " + existingRealisation.getId() + " version " + existingRealisation.getVersion());
                        System.out.println("Current lots: " + (existingRealisation.getLots() != null ? existingRealisation.getLots().size() : "null"));

                        boolean updateNeeded = realisationNeedsUpdate(existingRealisation, realisationDto);
                        System.out.println("Realisation update needed: " + updateNeeded);

                        // Always check for files even if no other changes
                        boolean hasRealisationFiles = realisationDto.getRealisationFiles() != null && !realisationDto.getRealisationFiles().isEmpty();
                        if (hasRealisationFiles) {
                            System.out.println("Files detected in realisation, forcing update");
                            updateNeeded = true;
                        }

                        if (updateNeeded) {
                            System.out.println("Updating Realisation with versioning...");
                            Realisation updatedRealisation = realisationService.updateWithVersioning(realisationDto.getId(), versionToUse, realisationDto);

                            // Handle realisation files if provided
                            if (realisationDto.getRealisationFiles() != null && !realisationDto.getRealisationFiles().isEmpty()) {
                                try {
                                    System.out.println("Processing " + realisationDto.getRealisationFiles().size() +
                                            " files for Realisation ID: " + updatedRealisation.getId() +
                                            " version: " + updatedRealisation.getVersion());
                                    //processAndSaveRelisationsDocumentsForUpdate(realisationDto.getRealisationFiles(), updatedRealisation);
                                    documentCommuneService.processAndSaveDocuments(realisationDto.getRealisationFiles(),updatedRealisation,Constants.PATH_ENTREPOT_GED_PAA+"/"+existingPaa.getId()+"/"+(existingPaa.getVersion()+1)+Constants.PATH_ENTREPOT_GED_REALISATION);

                                    // Save the realisation again after adding documents
                                    updatedRealisation = realisationRepository.save(updatedRealisation);
                                    System.out.println("Saved Realisation after processing files");
                                } catch (IOException e) {
                                    System.err.println("Error processing Realisation files: " + e.getMessage());
                                    throw new RuntimeException("Error processing Realisation files: " + e.getMessage(), e);
                                }                            }
                            // Handle realisation document IDs if provided
                            // --- Merge docIds and uploaded files ---
                            if (realisationDto.getDocIds() != null) {
                                // Start with docs from uploaded files (if any)
                                List<Docs> allDocs = new ArrayList<>();
                                if (updatedRealisation.getDocs() != null) {
                                    allDocs.addAll(updatedRealisation.getDocs());
                                }
                                System.out.println("[DEBUG] Docs from uploaded files for realisation " + updatedRealisation.getId() + ": " + allDocs.stream().map(Docs::getId).toList());
                                Set<Integer> alreadyAddedDocIds = allDocs.stream().map(Docs::getId).collect(Collectors.toSet());
                                System.out.println("[DEBUG] DocIds from request for realisation " + updatedRealisation.getId() + ": " + realisationDto.getDocIds());
                                for (Integer docId : realisationDto.getDocIds()) {

                                    if (alreadyAddedDocIds.contains(docId)) {
                                        System.out.println("[DEBUG] Skipping already added docId: " + docId);
                                        continue;
                                    }

                                    Docs doc = docsRepository.findById(docId)
                                            .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));

                                    doc.setIsDeleted(false);

                                    // Don't attempt to set realisation on doc here to avoid circular references

                                    docsRepository.save(doc);
                                    allDocs.add(doc);

                                    System.out.println("[DEBUG] Added docId from docIds: " + docId);
                                }

                                System.out.println("[DEBUG] Final merged docs for realisation " + updatedRealisation.getId() + ": " + updatedRealisation.getVersion() + ": " +  allDocs.stream().map(Docs::getId).toList());
                                updatedRealisation.setDocs(allDocs);
                                updatedRealisation = realisationRepository.save(updatedRealisation);
                                // --- Ensure bidirectional relationship and save docs ---
                                final Realisation realToCheck = updatedRealisation;
                            }
                            // Update the realisation's docs
                            updatedRealisations.add(updatedRealisation);

                            realisationsChanged = true;
                        } else {
                            System.out.println("Using existing Realisation without changes");

                            // Even if no field changes detected, still process realisation files if any
                            if (realisationDto.getRealisationFiles() != null && !realisationDto.getRealisationFiles().isEmpty()) {
                                try {
                                    System.out.println("Processing " + realisationDto.getRealisationFiles().size() + " files for existing Realisation ID: " + existingRealisation.getId() + " version: " + existingRealisation.getVersion());
                                   // processAndSaveRelisationsDocumentsForUpdate(realisationDto.getRealisationFiles(), existingRealisation);
                                    documentCommuneService.processAndSaveDocuments(realisationDto.getRealisationFiles(),existingRealisation,Constants.PATH_ENTREPOT_GED_PAA+"/"+existingPaa.getId()+"/"+(existingPaa.getVersion()+1)+Constants.PATH_ENTREPOT_GED_REALISATION);

                                    existingRealisation = realisationRepository.save(existingRealisation);
                                    System.out.println("Saved existing Realisation after processing files");
                                    updateNeeded = true; // Force update needed
                                    realisationsChanged = true;

                                    // Create a new version of the Realisation through the service
                                    System.out.println("Creating new Realisation version due to file uploads");
                                    Realisation updatedRealisation = realisationService.updateWithVersioning(realisationDto.getId(), versionToUse, realisationDto);
                                    updatedRealisations.add(updatedRealisation);
                                    continue; // Skip to next iteration since we've handled this Realisation
                                } catch (IOException e) {
                                    System.err.println("Error processing existing Realisation files: " + e.getMessage());
                                    throw new RuntimeException("Error processing existing Realisation files: " + e.getMessage(), e);
                                }
                            }

                            if (realisationDto.getLots() != null && !realisationDto.getLots().isEmpty()) {
                                System.out.println("Processing " + realisationDto.getLots().size() + " lots in DTO");

                                Set<Lot> updatedLots = new HashSet<>();
                                Set<Integer> processedLotIds = new HashSet<>();
                                boolean lotChanges = false;

                                Set<Integer> existingLotIds = existingRealisation.getLots() != null ? existingRealisation.getLots().stream().map(Lot::getId).collect(Collectors.toSet()) : new HashSet<>();
                                Map<Integer, String> existingLotDescriptions = existingRealisation.getLots() != null ? existingRealisation.getLots().stream().collect(Collectors.toMap(Lot::getId, lot -> lot.getDescription() != null ? lot.getDescription() : "", (a, b) -> a)) : new HashMap<>();

                                for (LotResponse lotDto : realisationDto.getLots()) {
                                    if (lotDto.getId() == null) {
                                        // Handle new lot without ID
                                        String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                        System.out.println("Processing new lot with description: " + lotHash);

                                        if (newLotsMap.containsKey(lotHash)) {
                                            System.out.println("Using previously created lot for description: " + lotHash);
                                            updatedLots.add(newLotsMap.get(lotHash));
                                            processedLotIds.add(newLotsMap.get(lotHash).getId());
                                            continue;
                                        }

                                        boolean lotExists = false;
                                        if (existingRealisation.getLots() != null) {
                                            for (Lot existingLot : existingRealisation.getLots()) {
                                                String existingDesc = existingLot.getDescription() != null ? existingLot.getDescription() : "";
                                                if (existingDesc.equals(lotHash)) {
                                                    System.out.println("Found matching existing lot with description: " + lotHash);
                                                    updatedLots.add(existingLot);
                                                    processedLotIds.add(existingLot.getId());
                                                    lotExists = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (lotExists) continue;

                                        System.out.println("Creating new lot with description: " + lotHash);
                                        Lot newLot = new Lot();
                                        newLot.setDescription(lotDto.getDescription());
                                        newLot.setVersion(1); // First version

                                        // Generate new ID if not provided
//                                        Integer maxLotId = lotRepository.findMaxId();
                                        Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                                        Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                                        newLot.setId(lotId);

                                        Lot savedLot = lotRepository.save(newLot);
                                        updatedLots.add(savedLot);
                                        processedLotIds.add(savedLot.getId());
                                        newLotsMap.put(lotHash, savedLot);
                                        System.out.println("Created new Lot with ID: " + savedLot.getId());
                                        lotChanges = true;


                                    } else {
                                        if (processedLotIds.contains(lotDto.getId())) {
                                            System.out.println("Skipping duplicate lot ID: " + lotDto.getId());
                                            continue;
                                        }

                                        Integer lotId = lotDto.getId();
                                        Integer lotVersion = lotDto.getVersion();
                                        if (lotVersion == null) {
                                            lotVersion = lotRepository.findMaxVersionById(lotId);
                                            if (lotVersion == null) {
                                                System.out.println("Lot ID " + lotId + " not found, skipping");
                                                continue;
                                            }
                                        }

                                        Optional<Lot> existingLotOpt = lotRepository.findById(new CompositeId(lotId, lotVersion));
                                        if (existingLotOpt.isPresent()) {
                                            Lot existingLot = existingLotOpt.get();
                                            String requestDesc = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                            String existingDesc = existingLot.getDescription() != null ? existingLot.getDescription() : "";
                                            if (!requestDesc.equals(existingDesc)) {
                                                Lot newLot = new Lot();
                                                newLot.setId(existingLot.getId());
                                                newLot.setVersion(existingLot.getVersion() + 1);
                                                newLot.setDescription(lotDto.getDescription());

                                                Lot savedLot = lotRepository.save(newLot);
                                                updatedLots.add(savedLot);
                                                processedLotIds.add(savedLot.getId());
                                                System.out.println("Created new version of Lot: " + savedLot.getId() + ", Version: " + savedLot.getVersion());
                                                lotChanges = true;
                                            } else {
                                                updatedLots.add(existingLot);
                                                processedLotIds.add(existingLot.getId());
                                            }
                                        }
                                    }
                                }

                                Set<Integer> requestLotIds = realisationDto.getLots().stream().filter(lotDto -> lotDto.getId() != null).map(LotResponse::getId).collect(Collectors.toSet());
                                requestLotIds.addAll(processedLotIds.stream().filter(id -> !existingLotIds.contains(id)).collect(Collectors.toSet()));

                                if (!requestLotIds.equals(existingLotIds)) {
                                    lotChanges = true;
                                }

                                if (lotChanges) {
                                    System.out.println("Lot associations changed, updating Realisation");
                                    existingRealisation.setLots(updatedLots);
                                    Realisation savedRealisation = realisationRepository.save(existingRealisation);
                                    updatedRealisations.add(savedRealisation);
                                    realisationsChanged = true;
                                } else {
                                    System.out.println("No changes to lot associations, using existing Realisation");
                                    updatedRealisations.add(existingRealisation);
                                }
                            } else {
                                updatedRealisations.add(existingRealisation);
                            }
                        }
                    } else {
                        System.out.println("Creating new Realisation");
                        RealisationRequest newRealisationRequest = new RealisationRequest();
                        newRealisationRequest.setRealisationEnvisagees(realisationDto.getRealisationEnvisagees());
                        newRealisationRequest.setService(realisationDto.getService());
                        newRealisationRequest.setDirection(realisationDto.getDirection());
                        newRealisationRequest.setSourceFinancement(realisationDto.getSourceFinancement());
                        newRealisationRequest.setMonais(realisationDto.getMonais());
                        newRealisationRequest.setTypeMarcher(realisationDto.getTypeMarcher());
                        newRealisationRequest.setModeSelection(realisationDto.getModeSelection());
                        newRealisationRequest.setDatePrevLps(realisationDto.getDatePrevLps());
                        newRealisationRequest.setDatePrevAc(realisationDto.getDatePrevAc());
                        newRealisationRequest.setDatePrevDp(realisationDto.getDatePrevDp());
                        newRealisationRequest.setDatePrevAp(realisationDto.getDatePrevAp());
                        newRealisationRequest.setBudgetEstime(realisationDto.getBudgetEstime());
                        newRealisationRequest.setIsPublished(realisationDto.getIsPublished());
                        newRealisationRequest.setPublishedAt(realisationDto.getPublishedAt());
                        newRealisationRequest.setSort(realisationDto.getSort());
                        newRealisationRequest.setDocIds(realisationDto.getDocIds());

                        if (realisationDto.getLots() != null && !realisationDto.getLots().isEmpty()) {
                            newRealisationRequest.setLots((Set<LotResponse>) realisationDto.getLots());
                        }

                        Realisation newRealisation = realisationService.create(newRealisationRequest);

                        // Handle new realisation files if provided
                        if (realisationDto.getRealisationFiles() != null && !realisationDto.getRealisationFiles().isEmpty()) {
                            try {
                                System.out.println("Processing " + realisationDto.getRealisationFiles().size() + " files for new Realisation ID: " + newRealisation.getId() + " version: " + newRealisation.getVersion());
                                //processAndSaveRelisationsDocuments(realisationDto.getRealisationFiles(), newRealisation);
                                documentCommuneService.processAndSaveDocuments(realisationDto.getRealisationFiles(),newRealisation,Constants.PATH_ENTREPOT_GED_PAA+"/"+existingPaa.getId()+"/"+(existingPaa.getVersion()+1)+Constants.PATH_ENTREPOT_GED_REALISATION);

                                // Save the new realisation again after adding documents
                                newRealisation = realisationRepository.save(newRealisation);
                                System.out.println("Saved new Realisation after processing files");
                            } catch (IOException e) {
                                System.err.println("Error processing new Realisation files: " + e.getMessage());
                                throw new RuntimeException("Error processing new Realisation files: " + e.getMessage(), e);
                            }
                        }

                        updatedRealisations.add(newRealisation);
                        realisationsChanged = true;
                    }
                } catch (Exception e) {
                    System.err.println("Failed to process Realisation: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Failed to process Realisation: " + e.getMessage(), e);
                }
            }

            // Check if realisations have changed compared to existing ones
            if (!realisationsChanged && existingPaa.getRealisations() != null) {
                if (existingPaa.getRealisations().size() != updatedRealisations.size()) {
                    System.out.println("Realisation count changed: " + existingPaa.getRealisations().size() + " -> " + updatedRealisations.size());
                    realisationsChanged = true;
                } else {
                    // Create a map of existing realisation IDs to their versions
                    Map<Integer, Integer> existingRealisationVersions = existingPaa.getRealisations().stream().collect(Collectors.toMap(Realisation::getId, Realisation::getVersion, (a, b) -> a));

                    // Check if any realisation versions have changed
                    for (Realisation updatedRealisation : updatedRealisations) {
                        Integer existingVersion = existingRealisationVersions.get(updatedRealisation.getId());
                        if (existingVersion == null || !existingVersion.equals(updatedRealisation.getVersion())) {
                            System.out.println("Realisation changed: " + updatedRealisation.getId() + " version " + updatedRealisation.getVersion());
                            realisationsChanged = true;
                            break;
                        }
                    }
                }
            } else if (existingPaa.getRealisations() == null || existingPaa.getRealisations().isEmpty()) {
                // If existing PAA had no realisations but now we have some
                if (!updatedRealisations.isEmpty()) {
                    System.out.println("Adding realisations to previously empty PAA");
                    realisationsChanged = true;
                }
            }
        } else if (existingPaa.getRealisations() != null && !existingPaa.getRealisations().isEmpty()) {
            // No realisations in request but there were some before - this is a change
            System.out.println("Realisations removed from PAA");
            realisationsChanged = true;
        }

        // Process document IDs - improved document handling
        if (request.getDocIds() != null) {
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());

            // Get only the active (non-deleted) existing document IDs
            Set<Integer> activeExistingDocIds = existingPaa.getDocs() != null ? existingPaa.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).map(Docs::getId).collect(Collectors.toSet()) : new HashSet<>();

            // Compare requested doc IDs with active doc IDs
            System.out.println("Comparing document IDs:");
            System.out.println("  - Existing active docs: " + activeExistingDocIds);
            System.out.println("  - Requested docs: " + requestDocIds);

            // If the sets don't match exactly, we need to mark it as changed
            if (!requestDocIds.equals(activeExistingDocIds)) {
                System.out.println("Document ID sets are different - marking as changed");
                documentsChanged = true;
            } else {
                System.out.println("Document ID sets match - no document changes detected");
            }

            // Prepare documents for the new version
            updatedDocs = new ArrayList<>();

            // Process existing documents first
            if (existingPaa.getDocs() != null) {
                for (Docs existingDoc : existingPaa.getDocs()) {
                    // Get a fresh copy of the document to avoid stale data
                    Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                    if (docToProcess == null) {
                        System.out.println("Warning: Document " + existingDoc.getId() + " no longer exists in database, skipping");
                        continue;
                    }

                    if (requestDocIds.contains(docToProcess.getId())) {
                        // This document is in the request, make sure it's not deleted
                        docToProcess.setIsDeleted(false);
                        docToProcess = docsRepository.save(docToProcess);
                        updatedDocs.add(docToProcess);

                        // Remove from requestDocIds to track what we've processed
                        requestDocIds.remove(docToProcess.getId());
                    } else {
                        // This document is not in the request, mark as deleted
                        docToProcess.setIsDeleted(true);
                        docToProcess = docsRepository.save(docToProcess);
                        updatedDocs.add(docToProcess);
                        System.out.println("Marked document with ID " + docToProcess.getId() + " as deleted for new version");
                    }
                }
            }

            // Now process any new documents in the request
            for (Integer docId : requestDocIds) {
                Docs doc = docsRepository.findById(docId).orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));

                // Ensure it's active
                doc.setIsDeleted(false);
                doc = docsRepository.save(doc);
                updatedDocs.add(doc);
                System.out.println("Added new document with ID " + doc.getId() + " for new version");
            }
        } else if (existingPaa.getDocs() != null && !existingPaa.getDocs().isEmpty()) {
            // No document IDs provided, but existing docs exist - mark all as deleted
            updatedDocs = new ArrayList<>();
            for (Docs existingDoc : existingPaa.getDocs()) {
                // Get a fresh copy of the document
                Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                if (docToProcess == null) continue;

                // Mark as deleted
                docToProcess.setIsDeleted(true);
                docToProcess = docsRepository.save(docToProcess);
                updatedDocs.add(docToProcess);
                System.out.println("Marked document with ID " + docToProcess.getId() + " as deleted (no docIds provided)");
            }
            documentsChanged = true;
        } else {
            // No docs in request or existing PAA
            updatedDocs = new ArrayList<>();
        }

        // Determine if we need to create a new PAA version
        boolean shouldCreateNewVersion = hasDirectChanges || realisationsChanged || documentsChanged;

        // Log detailed versioning decision information
        System.out.println("Should create new PAA version: " + shouldCreateNewVersion);
        System.out.println("  - Direct field changes: " + hasDirectChanges);
        System.out.println("  - Realisations changed: " + realisationsChanged);
        System.out.println("  - Documents changed: " + documentsChanged);
        System.out.println("  - Files uploaded: " + (request.getPaaFiles() != null && !request.getPaaFiles().isEmpty()));

        // Force version increment if files are uploaded - this ensures files alone trigger a new version
        if (request.getPaaFiles() != null && !request.getPaaFiles().isEmpty()) {
            System.out.println("Files are present - forcing creation of new version");
            shouldCreateNewVersion = true;
        }

        // Forcing version increment if any of the update criteria are met
        if (shouldCreateNewVersion) {
            System.out.println("Changes detected, creating new PAA version: " + (paaVer + 1));
            storePreviousRequestHash(paaId, paaVer, requestHash);
        } else {
            System.out.println("No changes detected, returning existing PAA version: " + paaVer);

            // Even if no changes detected, still process uploaded files if any - this is the fix
            if (request.getPaaFiles() != null) {
                try {
                    System.out.println("Processing " + request.getPaaFiles().size() + " files for existing PAA version");
                   // processAndSaveDocuments(request.getPaaFiles(), existingPaa);
                    documentCommuneService.processAndSaveDocuments(request.getPaaFiles(),existingPaa,Constants.PATH_ENTREPOT_GED_PAA);

                    existingPaa = repository.save(existingPaa);
                    System.out.println("Saved PAA after processing files: " + existingPaa.getId() + " version " + existingPaa.getVersion());
                    // Force creation of new version if files are uploaded
                    if (!request.getPaaFiles().isEmpty()) {
                        System.out.println("Files were uploaded - forcing creation of new version");
                        shouldCreateNewVersion = true;
                        storePreviousRequestHash(paaId, paaVer, requestHash);
                        // No need to continue with this block since we'll be creating a new version
                    }
                } catch (IOException e) {
                    System.err.println("Error processing PAA files for existing version: " + e.getMessage());
                    throw new RuntimeException("Error processing PAA files for existing version: " + e.getMessage(), e);
                }
            }

            // Only return existing PAA if we're still not creating a new version
            if (!shouldCreateNewVersion) {
                // Create a filtered copy for response when no changes
                Paa copyForResponse = new Paa();
                BeanUtils.copyProperties(existingPaa, copyForResponse);

                // Filter out deleted documents for the response
                if (existingPaa.getDocs() != null) {
                    copyForResponse.setDocs(existingPaa.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                }

                // Also filter realisation documents
                if (copyForResponse.getRealisations() != null) {
                    copyForResponse.getRealisations().forEach(realisation -> {
                        if (realisation.getDocs() != null) {
                            realisation.setDocs(realisation.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                        }

                        // Also filter lots in realisations
                        if (realisation.getLots() != null) {
                            realisation.setLots(realisation.getLots().stream().filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted())).collect(Collectors.toSet()));
                        }
                    });
                }

                return copyForResponse;
            }
        }

        // Create a new PAA version
        System.out.println("Changes detected, creating new PAA version: " + (paaVer + 1));
        Paa newPaa = new Paa();
        newPaa.setId(paaId);
        newPaa.setVersion(paaVer + 1);

        // Copy properties from temp PAA
//        newPaa.setRef(tempPaa.getRef());
        newPaa.setNom(tempPaa.getNom());
        newPaa.setAnnee(tempPaa.getAnnee());
        newPaa.setBudgetEstime(tempPaa.getBudgetEstime());
        newPaa.setProcessInstanceId(tempPaa.getProcessInstanceId());
        newPaa.setStatus(tempPaa.getStatus());
        newPaa.setAc(tempPaa.getAc());

        // Initialize empty collections
        newPaa.setRealisations(new ArrayList<>());
        newPaa.setDocs(new ArrayList<>());

        // Save the new PAA entity
        newPaa = repository.save(newPaa);
        System.out.println("Saved new PAA: " + newPaa.getId() + " version " + newPaa.getVersion());

        // Set up relationships
        if (!updatedRealisations.isEmpty()) {
            System.out.println("Setting up relationships with " + updatedRealisations.size() + " realisations");
            List<Realisation> confirmedRealisations = new ArrayList<>();

            // Create a final reference to newPaa for use in lambdas
            final Paa finalNewPaa = newPaa;

            for (Realisation realisation : updatedRealisations) {
                try {
                    // Verify the realisation still exists in the database
                    Realisation confirmedRealisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion())).orElse(null);

                    if (confirmedRealisation == null) {
                        System.err.println("Warning: Realisation " + realisation.getId() + " version " + realisation.getVersion() + " not found in database. Skipping relationship setup.");
                        continue;
                    }

                    // Check if this realisation is already associated with this PAA version
                    boolean alreadyAssociated = false;
                    if (confirmedRealisation.getPaas() != null) {
                        alreadyAssociated = confirmedRealisation.getPaas().stream().anyMatch(p -> p.getId().equals(finalNewPaa.getId()) && p.getVersion().equals(finalNewPaa.getVersion()));
                    }

                    if (alreadyAssociated) {
                        System.out.println("Realisation " + confirmedRealisation.getId() + " version " + confirmedRealisation.getVersion() + " is already associated with PAA " + finalNewPaa.getId() + " version " + finalNewPaa.getVersion() + ". Skipping association.");
                        confirmedRealisations.add(confirmedRealisation);
                        continue;
                    }

                    // Similar to PpmServiceImpl approach: Update the inverse side of the relationship first
                    List<Paa> paaList = confirmedRealisation.getPaas();
                    if (paaList == null) {
                        paaList = new ArrayList<>();
                        confirmedRealisation.setPaas(paaList);
                    }

                    // Add the PAA to the realisation if not already present
                    if (!paaList.contains(finalNewPaa)) {
                        paaList.add(finalNewPaa);
                    }

                    // Save the realisation with updated relationship
                    try {
                        confirmedRealisation = realisationRepository.save(confirmedRealisation);
                        System.out.println("Successfully associated Realisation " + confirmedRealisation.getId() + " version " + confirmedRealisation.getVersion() + " with new PAA");
                        confirmedRealisations.add(confirmedRealisation);
                    } catch (Exception e) {
                        System.err.println("Error saving realisation relationship: " + e.getMessage());
                        // If we encounter an error, try to continue with other realisations
                    }
                } catch (Exception e) {
                    System.err.println("Error associating Realisation with PAA: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Now set all realisations on the PAA at once
            newPaa.setRealisations(confirmedRealisations);
            System.out.println("Set " + confirmedRealisations.size() + " realisations on the new PAA");
        } else if (existingPaa.getRealisations() != null) {
            List<Realisation> confirmedRealisations = new ArrayList<>();
            System.out.println("Copying " + existingPaa.getRealisations().size() + " realisations from existing PAA");

            // Create a final reference to newPaa for use in lambdas
            final Paa finalNewPaa = newPaa;

            for (Realisation existingRealization : existingPaa.getRealisations()) {
                try {
                    // Find the latest version of this realisation
                    Integer latestVersion = realisationRepository.findMaxVersionById(existingRealization.getId());
                    if (latestVersion == null) {
                        System.err.println("Warning: No versions found for Realisation ID " + existingRealization.getId());
                        continue;
                    }

                    Realisation latestRealisation = realisationRepository.findById(new CompositeId(existingRealization.getId(), latestVersion)).orElse(null);

                    if (latestRealisation == null) {
                        System.err.println("Warning: Latest Realisation " + existingRealization.getId() + " version " + latestVersion + " not found in database. Skipping.");
                        continue;
                    }

                    // Check if this realisation is already associated with this PAA version
                    boolean alreadyAssociated = false;
                    if (latestRealisation.getPaas() != null) {
                        alreadyAssociated = latestRealisation.getPaas().stream().anyMatch(p -> p.getId().equals(finalNewPaa.getId()) && p.getVersion().equals(finalNewPaa.getVersion()));
                    }

                    if (alreadyAssociated) {
                        System.out.println("Realisation " + latestRealisation.getId() + " version " + latestRealisation.getVersion() + " is already associated with PAA " + finalNewPaa.getId() + " version " + finalNewPaa.getVersion() + ". Skipping association.");
                        confirmedRealisations.add(latestRealisation);
                        continue;
                    }

                    // Update the inverse side of the relationship first
                    List<Paa> paaList = latestRealisation.getPaas();
                    if (paaList == null) {
                        paaList = new ArrayList<>();
                        latestRealisation.setPaas(paaList);
                    }

                    // Add the PAA to the realisation if not already present
                    if (!paaList.contains(finalNewPaa)) {
                        paaList.add(finalNewPaa);
                    }

                    // Save the realisation with updated relationship
                    try {
                        latestRealisation = realisationRepository.save(latestRealisation);
                        System.out.println("Successfully associated latest Realisation " + latestRealisation.getId() + " version " + latestRealisation.getVersion() + " with new PAA");
                        confirmedRealisations.add(latestRealisation);
                    } catch (Exception e) {
                        System.err.println("Error saving relationship with latest realisation: " + e.getMessage());
                        // If we encounter an error, try to continue with other realisations
                    }
                } catch (Exception e) {
                    System.err.println("Error handling Realisation relationship: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Now set all realisations on the PAA at once
            newPaa.setRealisations(confirmedRealisations);
            System.out.println("Updated new PAA with " + confirmedRealisations.size() + " confirmed realisations");
        }

        // Handle documents - copy all documents to the new PAA and update their associations
        if (!updatedDocs.isEmpty()) {
            List<Docs> confirmedDocs = new ArrayList<>();
            // Create a final reference to newPaa for use in lambdas
            final Paa finalNewPaaForDocs = newPaa;

            for (Docs doc : updatedDocs) {
                try {
                    // Verify doc still exists
                    Docs confirmedDoc = docsRepository.findById(doc.getId()).orElse(null);
                    if (confirmedDoc == null) {
                        System.err.println("Warning: Document " + doc.getId() + " not found in database. Skipping.");
                        continue;
                    }

                    // Update the inverse side of the relationship first
                    List<Paa> paaList = confirmedDoc.getPaas();
                    if (paaList == null) {
                        paaList = new ArrayList<>();
                        confirmedDoc.setPaas(paaList);
                    }

                    // Check if PAA already exists in the doc's relationships
                    boolean alreadyAssociated = paaList.stream().anyMatch(p -> p.getId().equals(finalNewPaaForDocs.getId()) && p.getVersion().equals(finalNewPaaForDocs.getVersion()));

                    if (!alreadyAssociated) {
                        paaList.add(finalNewPaaForDocs);
                    }

                    // Save document with relationship
                    confirmedDoc = docsRepository.save(confirmedDoc);
                    confirmedDocs.add(confirmedDoc);
                } catch (Exception e) {
                    System.err.println("Error associating Document with PAA: " + e.getMessage());
                }
            }

            // Now set the documents on the PAA
            newPaa.setDocs(confirmedDocs);
            System.out.println("Copied " + confirmedDocs.size() + " documents to new PAA version with correct delete status");
        } else if (existingPaa.getDocs() != null && !existingPaa.getDocs().isEmpty()) {
            // Copy all existing documents to new PAA
            List<Docs> confirmedDocs = new ArrayList<>();
            // Create a final reference to newPaa for use in lambdas
            final Paa finalNewPaaForExistingDocs = newPaa;

            for (Docs existingDoc : existingPaa.getDocs()) {
                try {
                    // Verify doc still exists
                    Docs confirmedDoc = docsRepository.findById(existingDoc.getId()).orElse(null);
                    if (confirmedDoc == null) {
                        System.err.println("Warning: Document " + existingDoc.getId() + " not found in database. Skipping.");
                        continue;
                    }

                    // Update the inverse side of the relationship first
                    List<Paa> paaList = confirmedDoc.getPaas();
                    if (paaList == null) {
                        paaList = new ArrayList<>();
                        confirmedDoc.setPaas(paaList);
                    }

                    // Check if PAA already exists in the doc's relationships
                    boolean alreadyAssociated = paaList.stream().anyMatch(p -> p.getId().equals(finalNewPaaForExistingDocs.getId()) && p.getVersion().equals(finalNewPaaForExistingDocs.getVersion()));

                    if (!alreadyAssociated) {
                        paaList.add(finalNewPaaForExistingDocs);
                    }

                    // Save document with relationship
                    confirmedDoc = docsRepository.save(confirmedDoc);
                    confirmedDocs.add(confirmedDoc);
                } catch (Exception e) {
                    System.err.println("Error associating existing Document with PAA: " + e.getMessage());
                }
            }

            // Now set the documents on the PAA
            newPaa.setDocs(confirmedDocs);
            System.out.println("Copied " + confirmedDocs.size() + " existing documents to new PAA version");
        }

        // Final save after setting up all relationships
        newPaa = repository.save(newPaa);
        System.out.println("Final save of PAA: " + newPaa.getId() + " version " + newPaa.getVersion());

        // Clear the existing relationships between this PAA ID and the selected realisations to prevent duplicates
        // This approach prevents duplicate key violations in the join table
        try {
            // Create a final reference to newPaa for use in lambda expressions
            final Paa finalNewPaa = newPaa;

            // Get the list of realisation IDs we're associating
            List<Integer> realisationIds = finalNewPaa.getRealisations().stream().map(Realisation::getId).collect(Collectors.toList());

            // Only attempt to clean up if we have realisations to associate
            if (!realisationIds.isEmpty()) {

                System.out.println("Refreshing PAA-Realisation relationships to prevent primary key conflicts");

                // Detach existing realisations from any other version of this PAA to avoid conflicts
                for (Realisation realisation : finalNewPaa.getRealisations()) {
                    // Filter out other versions of the current PAA from each realisation's PAAs
                    if (realisation.getPaas() != null) {
                        List<Paa> filteredPaas = realisation.getPaas().stream().filter(p -> !(p.getId().equals(finalNewPaa.getId()) && !p.getVersion().equals(finalNewPaa.getVersion()))).collect(Collectors.toList());
                        realisation.setPaas(filteredPaas);
                        realisationRepository.save(realisation);

                        // Add the new PAA version back to ensure the relationship exists
                        if (!realisation.getPaas().contains(finalNewPaa)) {
                            realisation.getPaas().add(finalNewPaa);
                            realisationRepository.save(realisation);
                        }
                    }
                }

                // Clear the in-memory collection and rebuild it
                List<Realisation> tempRealisations = new ArrayList<>(finalNewPaa.getRealisations());
                finalNewPaa.getRealisations().clear();
                // Save with empty collection to remove all relationships
                newPaa = repository.save(finalNewPaa);

                // Create a new final reference after reassignment
                final Paa updatedNewPaa = newPaa;

                // Add back the relationships one by one
                for (Realisation realisation : tempRealisations) {
                    updatedNewPaa.getRealisations().add(realisation);
                }
                // Save again with the rebuilt relationships
                newPaa = repository.save(updatedNewPaa);
                System.out.println("Successfully refreshed PAA-Realisation relationships");
            }
        } catch (Exception e) {
            System.err.println("Warning: Error handling relationship refresh: " + e.getMessage());
            e.printStackTrace();
            // Continue execution - we'll let Hibernate try to handle it
        }
        // Handle document IDs for realizations



        if (request.getRealisations() != null) {
            for (RealisationRequestForPaa realisationRequest : request.getRealisations()) {
                if (realisationRequest.getDocIds() != null) {
                    Realisation realisation = newPaa.getRealisations().stream()
                            .filter(r -> r.getId().equals(realisationRequest.getId()))
                            .findFirst().orElse(null);

                    if (realisation != null) {
                        // Get existing (possibly uploaded) doc IDs
                        Set<Integer> existingDocIds = realisation.getDocs() != null
                                ? realisation.getDocs().stream().map(Docs::getId).collect(Collectors.toSet())
                                : new HashSet<>();

                        // Get docIds from request
                        Set<Integer> requestedDocIds = new HashSet<>(realisationRequest.getDocIds());

                        // Merge both sets
                        requestedDocIds.addAll(existingDocIds);

                        List<Docs> finalDocList = new ArrayList<>();

                        for (Integer docId : requestedDocIds) {
                            Docs doc = docsRepository.findById(docId).orElse(null);
                            if (doc != null) {
                                // Prevent circular linking if already handled on the Realisation side

                                boolean bidirectionalHandledElsewhere = true; // or make this a config flag

                                if (!bidirectionalHandledElsewhere) {
                                    if (doc.getRealisations() == null) doc.setRealisations(new ArrayList<>());
                                    boolean found = doc.getRealisations().stream()
                                            .anyMatch(r -> r.getId().equals(realisation.getId()) && r.getVersion().equals(realisation.getVersion()));
                                    if (!found) {
                                        doc.getRealisations().add(realisation);
                                        System.out.println("[DEBUG] Added realisation " + realisation.getId() + " v" + realisation.getVersion() + " to doc " + doc.getId());
                                    }
                                }

                                // Still update isDeleted and save
                                doc.setIsDeleted(false);
                                doc = docsRepository.save(doc);

                                finalDocList.add(doc);
                            }
                        }

                        realisation.setDocs(finalDocList);
                        realisationRepository.save(realisation);

                        System.out.println("[DEBUG-FINAL] Realisation " + realisation.getId() + " final docs: " + finalDocList.stream().map(Docs::getId).toList());
                    }
                }
            }
        }

        // Handle PAA files if provided - this was missing
        if (request.getPaaFiles() != null) {
            try {
                System.out.println("Processing " + request.getPaaFiles().size() + " files for new PAA version");
                //processAndSaveDocuments(request.getPaaFiles(), newPaa);
                documentCommuneService.processAndSaveDocuments(request.getPaaFiles(),newPaa,Constants.PATH_ENTREPOT_GED_PAA);

                // Save again after adding documents
                newPaa = repository.save(newPaa);
                System.out.println("Saved PAA after processing files: " + newPaa.getId() + " version " + newPaa.getVersion());
            } catch (IOException e) {
                System.err.println("Error processing PAA files for new version: " + e.getMessage());
                throw new RuntimeException("Error processing PAA files for new version: " + e.getMessage(), e);
            }
        }

        // Update the hash for the new version
        storePreviousRequestHash(paaId, newPaa.getVersion(), requestHash);
        Paa copyForReturn = new Paa();
        BeanUtils.copyProperties(newPaa, copyForReturn);
        copyForReturn.setDocs(newPaa.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));

        // Also filter realisation documents
        if (copyForReturn.getRealisations() != null) {
            copyForReturn.getRealisations().forEach(realisation -> {
                if (realisation.getDocs() != null) {
                    realisation.setDocs(realisation.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                }

                // Also filter lots in realisations
                if (realisation.getLots() != null) {
                    realisation.setLots(realisation.getLots().stream().filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted())).collect(Collectors.toSet()));
                }
            });
        }

        return copyForReturn;
    }



    /**
     * Updates a PAA with versioning by automatically finding and using the latest version.
     * <p>
     * This method finds the latest version of the PAA with the given ID and then
     * calls the version-specific updateWithVersioning method.
     *
     * @param paaId   the ID of the PAA to update
     * @param request the request containing the updated data
     * @return the newly created version of the PAA
     * @throws PaaNotFound if no versions of the PAA exist or if all versions are deleted
     */
    @Transactional
    @Override
    public Paa updateWithVersioning(Integer paaId, PaaRequestGlobal request) throws PaaNotFound {
        // Find the latest version of the PAA
        Integer latestVersion = repository.findMaxVersionById(paaId);

        if (latestVersion == null) {
            throw new PaaNotFound("No versions found for PAA with ID: " + paaId, List.of(paaId), List.of(null));
        }

        System.out.println("Found latest version for PAA ID " + paaId + ": " + latestVersion);

        // Call the version-specific method with the latest version
        return updateWithVersioning(paaId, latestVersion, request);
    }

    /**
     * Compares two PAA objects to check if their direct fields are identical.
     *
     * @param paa1 The first PAA object
     * @param paa2 The second PAA object
     * @return true if all direct fields are identical, false otherwise
     */

    private boolean arePaaFieldsIdentical(Paa paa1, Paa paa2) {
        if (paa1 == paa2) return true;
        if (paa1 == null || paa2 == null) return false;

        return Objects.equals(paa1.getRef(), paa2.getRef())
                && Objects.equals(paa1.getNom(), paa2.getNom())
                && Objects.equals(paa1.getRemarque(), paa2.getRemarque())
                && Objects.equals(paa1.getAnnee(), paa2.getAnnee())
                && Objects.equals(paa1.getBudgetEstime(), paa2.getBudgetEstime())
                && Objects.equals((paa1.getStatus()), (paa2.getStatus()))
                && Objects.equals(getRefParamId(paa1.getModePassation()), getRefParamId(paa2.getModePassation()))
                && Objects.equals(getAcId(paa1.getAc()), getAcId(paa2.getAc()));
    }

    /**
     * Safely extracts the ID from a RefParam object.
     *
     * @param refParam The RefParam object
     * @return The ID of the RefParam or null if the RefParam is null
     */
    private Integer getRefParamId(RefParam refParam) {
        return refParam != null ? refParam.getId() : null;
    }

    /**
     * Safely extracts the ID from an Ac object.
     *
     * @param ac The Ac object
     * @return The ID of the Ac or null if the Ac is null
     */
    private Integer getAcId(Ac ac) {
        return ac != null ? ac.getId() : null;
    }

    /**
     * Generates a hash string representing the request for duplicate detection.
     *
     * @param request The request to hash
     * @return A string hash of the request content
     */

    private String generateRequestHash(PaaRequestGlobal request) {
        if (request == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ref:").append(request.getRef() != null ? request.getRef() : "").append(";");
        sb.append("nom:").append(request.getNom() != null ? request.getNom() : "").append(";");
        sb.append("annee:").append(request.getAnnee() != null ? request.getAnnee() : "").append(";");
        sb.append("budgetEstime:").append(request.getBudgetEstime() != null ? request.getBudgetEstime() : "").append(";");
        sb.append("remarque:").append(request.getRemarque() != null ? request.getRemarque() : "").append(";");

        if (request.getStatus() != null && request.getStatus().getId() != null) {
            sb.append("statusId:").append(request.getStatus().getId()).append(";");
        } else {
            sb.append("statusId:;");
        }

        if (request.getAcId() != null) {
            sb.append("acId:").append(request.getAcId()).append(";");
        } else {
            sb.append("acId:;");
        }

        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Integer> sortedDocIds = new ArrayList<>(request.getDocIds());
            Collections.sort(sortedDocIds);
            sb.append("docIds:");
            for (Integer docId : sortedDocIds) {
                sb.append(docId).append(",");
            }
            sb.append(";");
        } else {
            sb.append("docIds:;");
        }

        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            // Sort realisations by ID to ensure consistent ordering
            List<RealisationRequestForPaa> sortedRealisations = new ArrayList<>(request.getRealisations());
            sortedRealisations.sort(Comparator.comparing(r -> r.getId() != null ? r.getId() : Integer.MAX_VALUE));

            for (RealisationRequestForPaa realisation : sortedRealisations) {
                sb.append("real[");
                sb.append("id:").append(realisation.getId() != null ? realisation.getId() : "").append(",");
                sb.append("ver:").append(realisation.getVersion() != null ? realisation.getVersion() : "").append(",");
                sb.append("text:").append(realisation.getRealisationEnvisagees() != null ? realisation.getRealisationEnvisagees() : "").append(",");

                // Include other Realisation fields
                sb.append("service:").append(realisation.getService() != null && realisation.getService().getId() != null ? realisation.getService().getId() : "").append(",");
                sb.append("direction:").append(realisation.getDirection() != null && realisation.getDirection().getId() != null ? realisation.getDirection().getId() : "").append(",");
                sb.append("sourceFin:").append(realisation.getSourceFinancement() != null ? realisation.getSourceFinancement() : "").append(",");
                sb.append("monais:").append(realisation.getMonais() != null ? realisation.getMonais() : "").append(",");
                sb.append("typeMarcher:").append(realisation.getTypeMarcher() != null ? realisation.getTypeMarcher() : "").append(",");
                sb.append("modeSelection:").append(realisation.getModeSelection() != null ? realisation.getModeSelection() : "").append(",");

                if (realisation.getLots() != null && !realisation.getLots().isEmpty()) {
                    // Sort lots by ID
                    List<LotResponse> sortedLots = new ArrayList<>(realisation.getLots());
                    sortedLots.sort(Comparator.comparing(l -> l.getId() != null ? l.getId() : Integer.MAX_VALUE));

                    sb.append("lots:[");
                    for (LotResponse lot : sortedLots) {
                        sb.append("{");
                        sb.append("id:").append(lot.getId() != null ? lot.getId() : "").append(",");
                        sb.append("ver:").append(lot.getVersion() != null ? lot.getVersion() : "").append(",");
                        sb.append("desc:").append(lot.getDescription() != null ? lot.getDescription() : "");
                        sb.append("},");
                    }
                    sb.append("]");
                } else {
                    sb.append("lots:[]");
                }
                sb.append("];");
            }
        } else {
            sb.append("realisations:[];");
        }

        return sb.toString();
    }

    // Simple in-memory cache for request hashes
    private static final Map<String, String> requestHashCache = new HashMap<>();

    /**
     * Gets the hash of the previous request for a PAA ID and version.
     *
     * @param paaId  The PAA ID
     * @param paaVer The PAA version
     * @return The hash string or null if no previous request
     */
    private String getPreviousRequestHash(Integer paaId, Integer paaVer) {
        String key = paaId + "-" + paaVer;
        return requestHashCache.get(key);
    }

    /**
     * Stores the hash of a request for a PAA ID and version.
     *
     * @param paaId  The PAA ID
     * @param paaVer The PAA version
     * @param hash   The hash string to store
     */
    private void storePreviousRequestHash(Integer paaId, Integer paaVer, String hash) {
        String key = paaId + "-" + paaVer;
        requestHashCache.put(key, hash);
    }



    /**
     * Creates a PAA with its Realisations and Lots in a single operation.
     * This method handles creating the complete object hierarchy:
     * - Creates the PAA entity
     * - Creates associated Realisations
     * - Creates Lots within each Realisation
     * - Establishes the bidirectional relationships
     *
     * @param request the global request containing PAA, Realisations, and Lots data
     * @return the created PAA entity with all relationships established
     * @throws PaaAlreadyExistsException if a PAA with the generated ID and version already exists
     */

    @Transactional
    @Override
    public Paa createWithRelationships(PaaRequestGlobal request) throws PaaAlreadyExistsException, AcNotFound, PaaCreationLimitExceededException {
//        Integer paaId = repository.findMaxId();
        Integer paaId = sequenceGeneratorService.getNextId(Constants.PAA_ID_SEQ);
        paaId = (paaId != null) ? paaId + 1 : 1;
        Integer paaVersion = 1;

        restrictPaaCreationForCurrentYear();

        Paa paa = new Paa();
        paa.setId(paaId);
        paa.setVersion(paaVersion);
//        paa.setRef(request.getRef());
        paa.setNom(request.getNom());
        paa.setModePassation(request.getModePassation());
        paa.setRemarque(request.getRemarque());

        String currentYearStr = String.valueOf(LocalDate.now().getYear());
        log.info("Setting annee to: {}", currentYearStr);
        paa.setAnnee(currentYearStr);
        setRef(paa);
        paa.setBudgetEstime(request.getBudgetEstime());

        paa.setStatus(ProcessStatus.BROUILLON);

        List<UserAc> userAcs = currentUserService.getCurrentUserAc();
        if (userAcs == null || userAcs.isEmpty()) {
            throw new AcNotFound("No AC IDs found for current user",List.of());
        }

        Integer requestAcId = request.getAcId();
        if (requestAcId != null) {
            boolean hasAcId = userAcs.stream()
                    .anyMatch(userAc -> userAc.getAcId().equals(requestAcId));
            if (!hasAcId) {
                throw new AcNotFound("User does not have permission for AC ID: " ,List.of(requestAcId));
            }
            paa.setAc(acService.findById(requestAcId));
        } else {
            Optional<UserAc> piloteAc = userAcs.stream()
                    .filter(UserAc::getAcPilote)
                    .findFirst();
            if (piloteAc.isPresent()) {
                paa.setAc(acService.findById(piloteAc.get().getAcId()));
            } else {
                if (!userAcs.isEmpty()) {
                    paa.setAc(acService.findById(userAcs.get(0).getAcId()));
                } else {
                    throw new NotFoundException("No AC IDs available to assign");
                }
            }
        }

        // Initialize collections
        paa.setRealisations(new ArrayList<>());
        paa.setDocs(new ArrayList<>());

        if (paa.getAnnee() == null || !paa.getAnnee().equals(currentYearStr)) {
            throw new IllegalStateException("Annee field is not set to the current year (" + currentYearStr + "). Current value: " + paa.getAnnee());
        }
        log.info("Annee test check passed: {}", paa.getAnnee());

        // Save PAA initially
        Paa savedPaa = repository.save(paa);

        // Handle documents for PAA
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = new ArrayList<>();
            for (Integer docId : request.getDocIds()) {
                Docs doc = docsRepository.findById(docId).orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));

                if (doc.getPaas() == null) {
                    doc.setPaas(new ArrayList<>());
                }
                doc.getPaas().add(savedPaa);
                docs.add(doc);
                docsRepository.save(doc);
            }
            savedPaa.setDocs(docs);
        }

        if (request.getPaaFiles() != null) {
            try {
                //processAndSaveDocuments(request.getPaaFiles(), savedPaa);
                documentCommuneService.processAndSaveDocuments(request.getPaaFiles(),savedPaa,Constants.PATH_ENTREPOT_GED_PAA);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Process realisations if provided
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            // Get the current max ID for Realisation once before the loop
            // This will be our starting point for generating new IDs
//            Integer currentMaxRealisationId = realisationRepository.findMaxId();
            Integer currentMaxRealisationId =  sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);;
            // Initialize next ID to use. If no realisations exist, start from 1.
            // Otherwise, start from maxId + 1.
            Integer nextRealisationId = (currentMaxRealisationId != null) ? currentMaxRealisationId + 1 : 1;


            List<Realisation> realisationsToSave = new ArrayList<>();

            for (RealisationRequestForPaa realisationRequest : request.getRealisations()) {
                Realisation realisation = new Realisation();

                // Assign the next available ID and then increment it for the next iteration
                realisation.setId(nextRealisationId++); // Assign and then increment
                realisation.setVersion(1);
                realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                realisation.setService(realisationRequest.getService());
                realisation.setDirection(realisationRequest.getDirection());
                realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
                realisation.setMonais(realisationRequest.getMonais());
                realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                realisation.setModeSelection(realisationRequest.getModeSelection());
                realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                realisation.setIsPublished(realisationRequest.getIsPublished());
                realisation.setPublishedAt(realisationRequest.getPublishedAt());
                realisation.setSort(realisationRequest.getSort());

                System.out.println("the realisations have files");
                System.out.println(realisationRequest.getRealisationFiles());
                if (realisationRequest.getRealisationFiles() != null) {
                    try {
                       // processAndSaveRelisationsDocuments(realisationRequest.getRealisationFiles(), realisation);
                        documentCommuneService.processAndSaveDocuments(realisationRequest.getRealisationFiles(),realisation,Constants.PATH_ENTREPOT_GED_PAA+"/"+paa.getId()+"/"+paa.getVersion()+Constants.PATH_ENTREPOT_GED_REALISATION);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (realisation.getPaas() == null) {
                    realisation.setPaas(new ArrayList<>());
                }
                realisation.getPaas().add(savedPaa);
                savedPaa.getRealisations().add(realisation);

                // Handle Lots
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    Set<Lot> lots = new HashSet<>();
//                    Integer currentMaxLotId = lotRepository.findMaxId();
                    Integer currentMaxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                    Integer nextLotId = (currentMaxLotId != null) ? currentMaxLotId + 1 : 1;

                    for (LotResponse lotRequest : realisationRequest.getLots()) {
                        Lot lot = new Lot();
                        // If lot ID is provided in request, use it. Otherwise, generate a new one.
                        Integer lotId = lotRequest.getId();
                        if (lotId == null) {
                            lotId = nextLotId++; // Assign and then increment for lots
                        }
                        lot.setId(lotId);
                        lot.setVersion(1);
                        lot.setDescription(lotRequest.getDescription());
                        lot = lotRepository.save(lot); // Save individual lot
                        lots.add(lot);
                    }
                    realisation.setLots(lots);
                }

                realisationsToSave.add(realisation);
            }

            realisationRepository.saveAll(realisationsToSave);
        }

        return repository.save(savedPaa);
    }


    private boolean realisationNeedsUpdate(Realisation existingRealisation, RealisationRequestForPaa dto) {
        // Check if any field is different
        if (dto.getRealisationEnvisagees() != null && !dto.getRealisationEnvisagees().equals(existingRealisation.getRealisationEnvisagees())) {
            return true;
        }

        // Check service
        if (dto.getService() != null && (existingRealisation.getService() == null || !dto.getService().getId().equals(existingRealisation.getService().getId()))) {
            return true;
        }

        // Check direction
        if (dto.getDirection() != null && (existingRealisation.getDirection() == null || !dto.getDirection().getId().equals(existingRealisation.getDirection().getId()))) {
            return true;
        }

        // Check if doc IDs have changed
        if (dto.getDocIds() != null) {
            if (existingRealisation.getDocs() == null && !dto.getDocIds().isEmpty()) {
                return true;
            }

            if (existingRealisation.getDocs() != null) {
                if (existingRealisation.getDocs().size() != dto.getDocIds().size()) {
                    return true;
                }

                // Check if all doc IDs match
                List<Integer> existingDocIds = existingRealisation.getDocs().stream().map(Docs::getId).toList();

                for (Integer docId : dto.getDocIds()) {
                    if (!existingDocIds.contains(docId)) {
                        return true;
                    }
                }
            }
        }

        // Description changes to existing lots are handled separately and don't increment Realisation version
        if (dto.getLots() != null) {
            // If there are no lots in the request but the realisation has lots, this is a major change
            if (dto.getLots().isEmpty() && existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty()) {
                System.out.println("Major lot change: Removing all lots");
                return true;
            }

            // If there are lots in the request but the realisation has none, this is a major change
            if (!dto.getLots().isEmpty() && (existingRealisation.getLots() == null || existingRealisation.getLots().isEmpty())) {
                System.out.println("Major lot change: Adding first lots");
                return true;
            }

            // If both have lots, check for additions/removals (but not description changes)
            if (!dto.getLots().isEmpty() && existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty()) {
                // Different number of lots = major change
                if (dto.getLots().size() != existingRealisation.getLots().size()) {
                    System.out.println("Major lot change: Different number of lots");
                    return true;
                }

                // Check for new lots (without IDs) = major change
                boolean hasNewLots = dto.getLots().stream().anyMatch(lotResponse -> lotResponse.getId() == null);
                if (hasNewLots) {
                    System.out.println("Major lot change: New lots without IDs");
                    return true;
                }

                // Extract lot IDs
                Set<Integer> requestLotIds = dto.getLots().stream().filter(lotResponse -> lotResponse.getId() != null).map(LotResponse::getId).collect(Collectors.toSet());

                Set<Integer> existingLotIds = existingRealisation.getLots().stream().map(Lot::getId).collect(Collectors.toSet());

                // Different lot IDs (additions or removals) = major change
                if (!requestLotIds.equals(existingLotIds)) {
                    System.out.println("Major lot change: Different lot IDs");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isLotBeingReplaced(Collection<LotResponse> lotDtos, Lot existingLot) {
        if (lotDtos == null || existingLot == null) {
            return false;
        }

        for (LotResponse lotDto : lotDtos) {
            // If the DTO has the same ID but a different description, it's replacing the existing lot
            if (lotDto.getId() != null && lotDto.getId().equals(existingLot.getId()) && lotDto.getDescription() != null && !lotDto.getDescription().equals(existingLot.getDescription())) {
                return true;
            }
        }

        return false;
    }

    // Add a synchronization object
    private final Object folderCreationLock = new Object();

    /*private void processAndSaveDocuments(List<MultipartFile> files, Paa savedPaa) throws IOException {
        UUID uuid = UUID.randomUUID();
        // Add a file counter to ensure unique filenames even with the same UUID
        AtomicInteger fileCounter = new AtomicInteger(0);

        if (files != null && !files.isEmpty()) {
            // Create a unique folder path with timestamp to prevent any possible conflict
            String timestamp = String.valueOf(System.currentTimeMillis());
            // Use parent folder path from Constants but add our unique structure
            String baseFolder = Constants.PATH_ENTREPOT_GED_PAA.replace("/SIGMAP-DEV/ARCHIVE/PAA/", "/SIGMAP-DEV/ARCHIVE/PAA_DOCS/");
            String folderPath = String.format("%s/PAA_%d/%d_%s", baseFolder, savedPaa.getId(), savedPaa.getVersion(), timestamp);

            // Create the folder structure first, synchronizing to prevent conflicts
            String folderId;
            synchronized (folderCreationLock) {
                try {
                    // Get or create the folder first, outside the file processing loop
                    folderId = alfrescoService.getOrCreateFolderByPath(folderPath);
                    log.info("Created/Retrieved PAA folder: Path={}, ID={}", folderPath, folderId);
                } catch (Exception e) {
                    log.error("Failed to create folder: Path={}, Error={}", folderPath, e.getMessage());
                    throw new IOException("Failed to create folder: " + e.getMessage(), e);
                }
            }

            // Now process each file using the already-created folder
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        Tika tika = new Tika();
                        String detectedMimeType = tika.detect(file.getInputStream());
                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";

                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
                        if (fileExtension.isEmpty()) {
                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
                        }

                        long fileSize = file.getSize();
                        String formattedSize = documentCommuneService.formatSize(fileSize);

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        // Include timestamp and counter to ensure uniqueness
                        String uniqueFileName = String.format("paa_%s_%s_%d%s", uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        log.info("Uploading file: OriginalName={}, UniqueName={}, FolderPath={}, FolderID={}", originalFilename, uniqueFileName, folderPath, folderId);

                        // Upload directly to the already-created folder
                        String alfrescoId = alfrescoService.uploadToExistingFolder(renamedFile, folderId);
                        log.info("Uploaded document to Alfresco: AlfrescoID={}, FileName={}", alfrescoId, uniqueFileName);

                        Docs document = new Docs();
                        document.setName(baseName);
                        document.setAlfresco_id(alfrescoId);
                        document.setPathFolder(folderPath);
                        document.setFileSize(formattedSize);
                        document.setContentType(detectedMimeType);
                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
                        document.setAlfrescoName(uniqueFileName);
                        if (document.getPaas() == null) {
                            document.setPaas(new ArrayList<>());
                        }
                        if (savedPaa.getDocs() == null) {
                            savedPaa.setDocs(new ArrayList<>());
                        }
                        document.getPaas().add(savedPaa);
                        savedPaa.getDocs().add(document);

                        docsRepository.save(document);
                    } catch (Exception e) {
                        log.error("Failed to process file: OriginalName={}, Error={}", file.getOriginalFilename(), e.getMessage());
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                }
            }
        }
    }*/

    public Paa findByIdPaa(Integer paaId, Integer paaVer) throws PaaNotFound {
        Integer latestVersion = findLatestVersionById(paaId).orElseThrow(() -> new PaaNotFound("No PAA found for ID", List.of(paaId), List.of(paaVer)));
        Paa paa = repository.findById(new CompositeId(paaId, latestVersion)).orElseThrow(() -> new PaaNotFound("No PAA found for ID and version", List.of(paaId), List.of(latestVersion)));


        return paa;
    }

    private Optional<Integer> findLatestVersionById(Integer paaId) {
        return Optional.ofNullable(repository.findMaxVersionById(paaId));
    }


   /* void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
        List<Docs> docs = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        // Add a file counter to ensure unique filenames even with the same UUID
        AtomicInteger fileCounter = new AtomicInteger(0);

        if (files != null && !files.isEmpty()) {
            // Create a completely unique folder name with timestamp to avoid conflicts
            String timestamp = String.valueOf(System.currentTimeMillis());
            // Use parent folder path from Constants but avoid using "REALISATION" directly
            String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION.replace("/SIGMAP-DEV/ARCHIVE/REALISATION/", "/SIGMAP-DEV/ARCHIVE/REAL_DOCS/");
//            String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION;
            String folderPath = String.format("%s/REL_%d/%d_%s", baseFolder, savedRealisation.getId(), savedRealisation.getVersion(), timestamp);

            // Create the folder structure first, synchronizing to prevent conflicts
            String folderId;
            synchronized (folderCreationLock) {
                try {
                    // Get or create the folder first, outside the file processing loop
                    folderId = alfrescoService.getOrCreateFolderByPath(folderPath);
                    log.info("Created/Retrieved Realisation folder: Path={}, ID={}", folderPath, folderId);
                } catch (Exception e) {
                    log.error("Failed to create folder: Path={}, Error={}", folderPath, e.getMessage());
                    throw new IOException("Failed to create folder: " + e.getMessage(), e);
                }
            }

            // Now process each file using the already-created folder
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        Tika tika = new Tika();
                        String detectedMimeType = tika.detect(file.getInputStream());
                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";

                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
                        if (fileExtension.isEmpty()) {
                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
                        }

                        long fileSize = file.getSize();
                        String formattedSize = documentCommuneService.formatSize(fileSize);

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        // Include timestamp and counter to ensure uniqueness
                        String uniqueFileName = String.format("realisation_%s_%s_%d%s", uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        log.info("Uploading file: OriginalName={}, UniqueName={}, FolderPath={}, FolderID={}", originalFilename, uniqueFileName, folderPath, folderId);

                        // Upload directly to the already-created folder
                        String alfrescoId = alfrescoService.uploadToExistingFolder(renamedFile, folderId);
                        log.info("Uploaded document to Alfresco: AlfrescoID={}, FileName={}", alfrescoId, uniqueFileName);

                        Docs document = new Docs();
                        document.setName(baseName);
                        document.setAlfresco_id(alfrescoId);
                        document.setPathFolder(folderPath);
                        document.setFileSize(formattedSize);
                        document.setContentType(detectedMimeType);
                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
                        document.setAlfrescoName(uniqueFileName);

                        // Ensure collections are initialized
                        if (document.getRealisations() == null) {
                            document.setRealisations(new ArrayList<>());
                        }
                        if (savedRealisation.getDocs() == null) {
                            savedRealisation.setDocs(new ArrayList<>());
                        }

                        // Set up bidirectional relationship
                        document.getRealisations().add(savedRealisation);
                        savedRealisation.getDocs().add(document);

                        // Save the document
                        docsRepository.save(document);
                        docs.add(document);
                    } catch (Exception e) {
                        log.error("Failed to process file: OriginalName={}, Error={}", file.getOriginalFilename(), e.getMessage());
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                }
            }
        }

        // Update the realisation's docs
        savedRealisation.setDocs(docs);
    }*/

    @Transactional
    public Paa updateStatus(Integer paaId, Integer paaVer, ProcessStatus status) throws PaaNotFound {
        CompositeId compositeId = new CompositeId(paaId, paaVer);
        Paa paa = repository.findById(compositeId).orElseThrow(() -> new PaaNotFound("Paa not found", List.of(paaId), List.of(paaVer)));

        paa.setStatus(status);

        paa.setUpdatedAt(Calendar.getInstance());
        return repository.save(paa);
    }


   /* void processAndSaveRelisationsDocumentsForUpdate(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
        List<Docs> docs = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        AtomicInteger fileCounter = new AtomicInteger(0);

        if (files != null && !files.isEmpty()) {
            // Create a unique folder path with timestamp to avoid conflicts
            String timestamp = String.valueOf(System.currentTimeMillis());
            String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION.replace("/SIGMAP-DEV/ARCHIVE/REALISATION/", "/SIGMAP-DEV/ARCHIVE/REAL_DOCS/");
            String folderPath = String.format("%s/REL_%d/%d_%s", baseFolder, savedRealisation.getId(), savedRealisation.getVersion(), timestamp);

            // Create a new folder for this upload operation
            String folderId;
            synchronized (folderCreationLock) {
                try {
                    folderId = alfrescoService.getOrCreateFolderByPath(folderPath);
                    log.info("Created/Retrieved Realisation folder: Path={}, ID={}", folderPath, folderId);
                } catch (Exception e) {
                    log.error("Failed to create folder: Path={}, Error={}", folderPath, e.getMessage());
                    throw new IOException("Failed to create folder: " + e.getMessage(), e);
                }
            }

            // Process each file
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        Tika tika = new Tika();
                        String detectedMimeType = tika.detect(file.getInputStream());
                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";

                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);

                        long fileSize = file.getSize();
                        String formattedSize = documentCommuneService.formatSize(fileSize);

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        String uniqueFileName = String.format("realisation_%s_%s_%d%s", uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        log.info("Uploading file: OriginalName={}, UniqueName={}, FolderPath={}, FolderID={}", originalFilename, uniqueFileName, folderPath, folderId);

                        // Upload with retry logic
                        String alfrescoId = alfrescoService.uploadToExistingFolder(renamedFile, folderId);
                        log.info("Uploaded document to Alfresco: AlfrescoID={}, FileName={}", alfrescoId, uniqueFileName);

                        Docs document = new Docs();
                        document.setName(baseName);
                        document.setAlfresco_id(alfrescoId);
                        document.setPathFolder(folderPath);
                        document.setFileSize(formattedSize);
                        document.setContentType(detectedMimeType);
                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
                        document.setAlfrescoName(uniqueFileName);

                        // Ensure collections are initialized
                        if (document.getRealisations() == null) {
                            document.setRealisations(new ArrayList<>());
                        }
                        if (savedRealisation.getDocs() == null) {
                            savedRealisation.setDocs(new ArrayList<>());
                        }

                        // Set up bidirectional relationship
                        document.getRealisations().add(savedRealisation);
                        savedRealisation.getDocs().add(document);

                        // Save the document
                        docsRepository.save(document);
                        docs.add(document);
                    } catch (Exception e) {
                        log.error("Failed to process file: OriginalName={}, Error={}", file.getOriginalFilename(), e.getMessage());
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                }
            }
        }

        // Update the realisation's docs
        savedRealisation.setDocs(docs);
    }*/

    @Transactional
    @Override
    public void deleteTest(Integer paaId, Integer paaVer) throws PaaNotFound, PaaSoftDelete {
        CompositeId compositeId = new CompositeId(paaId, paaVer);
        if (!repository.existsById(compositeId)) {
            throw new PaaNotFound("Paa not found", List.of(paaId), List.of(paaVer));
        }

        try {
            repository.softDelete(paaId, paaVer);
        } catch (Exception e) {
            throw new PaaSoftDelete("Soft delete failed", 0, paaId, paaVer);
        }
    }
    @Override
    public Page<Paa> findPaasByAcId(Integer acId, Pageable pageable) throws AcNotFound {
        acService.findById(acId);
        Specification<Paa> paaSpec = Specification.where(
                        PaaSpecifications.deleted(false)
                        .and(PaaSpecifications.isLatestVersion().and(PaaSpecifications.paaAc(acId)))
        );
        return repository.findAll(paaSpec, pageable);
    }

    @Override
    public Page<Paa> findAllVersionsById(Integer paaId, Pageable pageable) throws PaaIdNotFound {
        if (paaId == null || paaId < 0) {
            throw new IllegalArgumentException("Invalid Paa ID: " + paaId);
        }
        Optional<Integer> maxVersion = findLatestVersionById(paaId);
        if (maxVersion.isEmpty()) {
            throw new PaaIdNotFound("No Paa found for ID", paaId);
        }
        Page<Paa> paaPage = repository.findAllById(paaId, pageable);
        if (paaPage.isEmpty()) {
            throw new PaaIdNotFound("No Paa versions found for ID", paaId);
        }
        return paaPage;
    }

    /**
     * Generates and sets the reference for a Paa entity.
     * Format: PAA_<YEAR>_<ID>
     *
     * @param paa the Paa entity
     */
    private void setRef(Paa paa) {
        if (paa == null || paa.getId() == null || paa.getAnnee() == null) {
            throw new IllegalArgumentException("Cannot set reference: paa, id, or year is null.");
        }
        String ref = "PAA_" + paa.getAnnee() + "_" + paa.getId();
        paa.setRef(ref);
    }

}
