package com.sigmap.common.paas;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac_seuil.AcSeuil;
import com.sigmap.common.ac_seuil.AcSeuilService;
import com.sigmap.common.ac_seuil.AcSeuilSpecifications;
import com.sigmap.common.config.Constants;
import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.financement.Financement;
import com.sigmap.common.financement.FinancementId;
import com.sigmap.common.financement.FinancementRepository;
import com.sigmap.common.financement.request.FinancementBaseRequestDto;
import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.lots.exception.LotBudgetConsistencyException;
import com.sigmap.common.lots.exception.LotBudgetExceedsRealisationException;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.MonnaieRepository;
import com.sigmap.common.paas.exceptions.*;
import com.sigmap.common.paas.mappers.PaaMapper;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.request.PaaRequestGlobal;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.RealisationService;
import com.sigmap.common.realisations.exceptions.RealisationBudgetExceedsAcSeuilException;
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
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation of {@link PaaService}
 */
@Service
@Slf4j
public class PaaServiceImpl implements PaaService {
    private final PaaRepository repository;
    private final RealisationRepository realisationRepository;
    private final DocsRepository docsRepository;
    private final AcRepository acRepository;
    private final LotRepository lotRepository;
    private final RealisationService realisationService;
    private final DocumentCommuneService documentCommuneService;
    private final AcService acService;
    private final CurrentUserService currentUserService;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final AcSeuilService acSeuilService;
    private final MonnaieRepository monnaieRepository;
    private final FinancementRepository financementRepository;
    private final RefParamRepository refParamRepository;
    private final EntityManager entityManager;

    @Value("${alfresco.url}")
    private String alfrescoUrl;

    public PaaServiceImpl(PaaRepository repository, CurrentUserService currentUserService, EntityManager entityManager,RealisationRepository realisationRepository, RefParamRepository refParamRepository, DocsRepository docsRepository, AcRepository acRepository, LotRepository lotRepository, RealisationService realisationService, DocumentCommuneService documentCommuneService, AcService acService, AlfrescoService alfrescoService, SequenceGeneratorService sequenceGeneratorService,
                          AcSeuilService acSeuilService, MonnaieRepository monnaieRepository, FinancementRepository financementRepository, RefParamRepository refParamRepository1) {
        this.repository = repository;
        this.realisationRepository = realisationRepository;
        this.docsRepository = docsRepository;
        this.acRepository = acRepository;
        this.lotRepository = lotRepository;
        this.realisationService = realisationService;
        this.documentCommuneService = documentCommuneService;
        this.acService = acService;
        this.currentUserService = currentUserService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.acSeuilService = acSeuilService;
        this.monnaieRepository = monnaieRepository;
        this.financementRepository = financementRepository;
        this.refParamRepository = refParamRepository1;
        this.entityManager = entityManager;
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
    @Override
    public Page<Paa> findAll(Specification<Paa> spec, Pageable pageable) {
        log.info("Recherche de tous les PAAs avec spécification: {}", spec);

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
                return Page.empty(pageable);
            }
        }

        // Base specification: non-deleted and latest version
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

        // Check if any non-deleted PAAs exist
        Page<Paa> allPaasWithoutAcFilter = repository.findAll(baseSpec, pageable);
        if (allPaasWithoutAcFilter.isEmpty()) {
            log.info("Aucun PAA non supprimé trouvé dans la base de données");
            return Page.empty(pageable);
        }

        // Apply AC filter
        Specification<Paa> acFilterSpec = (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("ac").get("id")).value(userAcIds);
        Specification<Paa> combinedSpec = baseSpec.and(acFilterSpec);
        Page<Paa> allPaas = repository.findAll(combinedSpec, pageable);

        if (allPaas.isEmpty()) {
            log.info("Aucun PAA associé aux ACs de l'utilisateur: {}", userAcIds);
            return Page.empty(pageable);
        }

        // Map the results to filter Docs
        Page<Paa> paasResponse = allPaas.map(this::createFilteredResponsePaa);
        log.info("Récupération de {} PAAs pour les AC de l'utilisateur {}", paasResponse.getContent().size(), userAcIds);

        return paasResponse;
    }

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
    /**
     * Retrieves a Paa entity by its ID and specific version. Only non-deleted Docs are included in the response,
     * and the Paa must be associated with one of the ACs of the current user.
     *
     * @param paaId  ID of the Paa entity
     * @param paaVer Version of the Paa entity to retrieve
     * @return Optional containing the Paa entity with the specified ID and version, or empty if not found
     * @throws PaaNotFound                         if no Paa entity is found for the ID and version
     * @throws AcNotFound                          if no AC is found for the current user
     * @throws PaaNotAssociatedWithUserAcException if the Paa is not associated with the user's ACs
     */
    @Override
    public Optional<Paa> findById(Integer paaId, Integer paaVer) throws PaaNotFound, AcNotFound, PaaNotAssociatedWithUserAcException {
        log.info("Recherche du PAA avec ID: {} et version: {}", paaId, paaVer);

        // Step 1: Get user's AC IDs
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds == null || userAcIds.isEmpty()) {
            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté. userAcIds: {}", userAcIds);
            throw new AcNotFound("Aucun ID AC trouvé pour l'utilisateur connecté", Collections.emptyList());
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

        // Step 3: Validate input parameters
        if (paaId == null || paaVer == null) {
            log.warn("Paramètres invalides: paaId={}, paaVer={}", paaId, paaVer);
            throw new PaaNotFound("ID ou version du PAA non fourni",
                    paaId != null ? List.of(paaId) : Collections.emptyList(),
                    paaVer != null ? List.of(paaVer) : Collections.emptyList());
        }

        // Step 4: Retrieve the PAA for the specified ID and version
        Optional<Paa> paaOpt = repository.findById(new CompositeId(paaId, paaVer));
        if (paaOpt.isEmpty()) {
            log.warn("Aucun PAA trouvé pour l'ID: {} et la version: {}", paaId, paaVer);
            throw new PaaNotFound("Aucun PAA trouvé pour l'ID et la version",
                    List.of(paaId), List.of(paaVer));
        }
        Paa paa = paaOpt.get();
        log.debug("PAA récupéré: ID={}, Version={}, AC={}",
                paa.getId(), paa.getVersion(), paa.getAc() != null ? paa.getAc().getId() : "null");

        // Step 5: Check if the PAA is deleted
        if (Boolean.TRUE.equals(paa.getIsDeleted())) {
            log.warn("PAA avec ID {} et version {} est marqué comme supprimé", paaId, paaVer);
            throw new PaaNotFound("PAA est supprimé", List.of(paaId), List.of(paaVer));
        }

        // Step 6: Validate AC association
        Integer paaAcId = paa.getAc() != null ? paa.getAc().getId() : null;
        log.debug("Validation de l'association AC: PAA AC ID={}, User ACs={}", paaAcId, userAcIds);
        if (paaAcId == null || !userAcIds.contains(paaAcId)) {
            log.warn("PAA avec ID {} et version {} n'est pas associé à un AC de l'utilisateur. AC du PAA: {}, ACs de l'utilisateur: {}",
                    paaId, paaVer, paaAcId, userAcIds);
            throw new PaaNotAssociatedWithUserAcException(
                    "Le PAA avec l'ID " + paaId + " et version " + paaVer + " n'est pas associé à un AC de l'utilisateur connecté",
                    List.of(paaId.longValue()),
                    List.of(paaVer)
            );
        }

        // Step 7: Final validation before return
        if (paa.getAc() == null || !userAcIds.contains(paa.getAc().getId())) {
            log.warn("Validation finale échouée: PAA avec ID {} et version {} n'est pas associé à un AC de l'utilisateur. AC du PAA: {}, ACs de l'utilisateur: {}",
                    paaId, paaVer, paa.getAc() != null ? paa.getAc().getId() : "null", userAcIds);
            throw new PaaNotAssociatedWithUserAcException(
                    "Le PAA avec l'ID " + paaId + " et version " + paaVer + " n'est pas associé à un AC de l'utilisateur connecté (validation finale)",
                    List.of(paaId.longValue()),
                    List.of(paaVer)
            );
        }

        // Step 8: Prepare and return the response
        Paa responsePaa = createFilteredResponsePaa(paa);
        log.info("PAA avec ID {} et version {} récupéré avec succès pour l'utilisateur", paaId, paaVer);
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
        filteredPaa.setAnnee(paa.getAnnee());
        filteredPaa.setRef(paa.getRef());
        filteredPaa.setNom(paa.getNom());
        filteredPaa.setBudgetEstime(paa.getBudgetEstime());
        filteredPaa.setDocs(paa.getDocs());
        filteredPaa.setModePassation(paa.getModePassation());
        filteredPaa.setRemarque(paa.getRemarque());
        filteredPaa.setProcessInstanceId(paa.getProcessInstanceId());
        filteredPaa.setStatus(paa.getStatus());
        filteredPaa.setCreatedAt(paa.getCreatedAt());
        filteredPaa.setCreatedBy(paa.getCreatedBy());
        filteredPaa.setUpdatedAt(paa.getUpdatedAt());
        filteredPaa.setUpdatedBy(paa.getUpdatedBy());
        filteredPaa.setIsDeleted(paa.getIsDeleted());
        filteredPaa.setDeletedBy(paa.getDeletedBy());
        filteredPaa.setDeletedAt(paa.getDeletedAt());
        filteredPaa.setProcessInstanceId(paa.getProcessInstanceId());
//        filteredPaa.setRealisations(paa.getRealisations());

        if (paa.getDocs() != null) {
            List<Docs> nonDeletedDocs = paa.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList());
            filteredPaa.setDocs(nonDeletedDocs);
        }

        if (paa.getRealisations() != null) {
            List<Realisation> nondeletedRea = paa.getRealisations().stream()
                    .filter(rea -> !Boolean.TRUE.equals(rea.getIsDeleted()))
                    .collect(Collectors.toList());
            filteredPaa.setRealisations(nondeletedRea);
        }

        return filteredPaa;
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

@Transactional
@Override
public Paa update(Integer paaId, Integer paaVer, PaaRequestGlobal request) throws PaaNotFound, PaaInvalidStateException {
    log.info("Updating PAA with ID: {}, Version: {}", paaId, paaVer);
    CompositeId compositeId = new CompositeId(paaId, paaVer);
    Paa existingPaa = repository.findById(compositeId).orElseThrow(
            () -> new PaaNotFound("Paa with ID " + paaId + " and version " + paaVer + " was not found in the database.",
                    List.of(paaId), List.of(paaVer))
    );

    if (Boolean.TRUE.equals(existingPaa.getIsDeleted())) {
        throw new PaaNotFound("Cannot update a deleted PAA with ID " + paaId + " and version " + paaVer,
                List.of(paaId), List.of(paaVer));
    }

    restrictUpdatePaa(existingPaa);

    // === Simple fields
    existingPaa.setNom(request.getNom());
    existingPaa.setAnnee(existingPaa.getAnnee()); // (kept as-is, per your original code)
    existingPaa.setModePassation(request.getModePassation());
    existingPaa.setRemarque(request.getRemarque());

    Integer requestAcId = (request.getAcId() != null)
            ? request.getAcId()
            : (existingPaa.getAc() != null ? existingPaa.getAc().getId() : null);

    if (request.getAcId() != null) {
        Ac ac = acRepository.findById(request.getAcId())
                .orElseThrow(() -> new EntityNotFoundException("Ac with ID " + request.getAcId() + " not found"));
        existingPaa.setAc(ac);
    }

    // === PAA Docs
    if (request.getDocIds() != null) {
        Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());
        List<Docs> updatedDocs = new ArrayList<>();

        if (existingPaa.getDocs() != null) {
            for (Docs d : existingPaa.getDocs()) {
                if (requestedDocIds.remove(d.getId())) {
                    if (Boolean.TRUE.equals(d.getIsDeleted())) {
                        d.setIsDeleted(false);
                        docsRepository.save(d);
                    }
                    updatedDocs.add(d);
                } else {
                    if (!Boolean.TRUE.equals(d.getIsDeleted())) {
                        d.setIsDeleted(true);
                        docsRepository.save(d);
                        log.info("Marked PAA doc {} as deleted (not in request)", d.getId());
                    }
                    updatedDocs.add(d);
                }
            }
        }

        for (Integer docId : requestedDocIds) {
            Docs doc = docsRepository.findById(docId)
                    .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
            if (doc.getPaas() == null) doc.setPaas(new ArrayList<>());
            if (!doc.getPaas().contains(existingPaa)) doc.getPaas().add(existingPaa);
            doc.setIsDeleted(false);
            docsRepository.save(doc);
            updatedDocs.add(doc);
            log.info("Added new document {} to PAA {}", docId, paaId);
        }

        existingPaa.setDocs(updatedDocs);
    } else if (existingPaa.getDocs() != null && !existingPaa.getDocs().isEmpty()) {
        for (Docs doc : existingPaa.getDocs()) {
            if (!Boolean.TRUE.equals(doc.getIsDeleted())) {
                doc.setIsDeleted(true);
                docsRepository.save(doc);
                log.info("Marked PAA doc {} as deleted (no docIds provided)", doc.getId());
            }
        }
    }

    // === PAA file uploads
    if (request.getPaaFiles() != null) {
        try {
            documentCommuneService.processAndSaveDocuments(
                    request.getPaaFiles(),
                    existingPaa,
                    Constants.PATH_ENTREPOT_GED_PAA
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to process PAA files: " + e.getMessage(), e);
        }
    }

    // === Realisations
    Set<Integer> processedRealisationIds = new HashSet<>();

    if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
        List<Realisation> realisations = new ArrayList<>();

        for (RealisationRequestForPaa rReq : request.getRealisations()) {
            Realisation realisation;

            if (rReq.getId() != null) {
                // ============== UPDATE existing Realisation
                processedRealisationIds.add(rReq.getId());
                Integer versionToUse = rReq.getVersion();
                if (versionToUse == null) {
                    Integer maxVersion = realisationRepository.findMaxVersionById(rReq.getId());
                    if (maxVersion == null) {
                        throw new RuntimeException("No versions found for Realisation with ID: " + rReq.getId());
                    }
                    versionToUse = maxVersion;
                }

                CompositeId rid = new CompositeId(rReq.getId(), versionToUse);
                realisation = realisationRepository.findById(rid).orElseThrow(
                        () -> new RuntimeException("Realisation not found with ID: " + rid.getId())
                );

                // scalars
                realisation.setRealisationEnvisagees(rReq.getRealisationEnvisagees());
                realisation.setBeneficiaire(rReq.getBeneficiaire());
                realisation.setImputationBudgetaire(rReq.getImputationBudgetaire());
                realisation.setTypeMarcher(rReq.getTypeMarcher());
                realisation.setModePassation(rReq.getModePassation());
                realisation.setDatePrevLps(rReq.getDatePrevLps());
                realisation.setDatePrevAc(rReq.getDatePrevAc());
                realisation.setDatePrevDp(rReq.getDatePrevDp());
                realisation.setDatePrevAp(rReq.getDatePrevAp());
                realisation.setIsPublished(rReq.getIsPublished());
                realisation.setPublishedAt(rReq.getPublishedAt());
                realisation.setSort(rReq.getSort());

                // ---- Realisation Docs
                if (rReq.getDocIds() != null) {
                    Set<Integer> requested = new HashSet<>(rReq.getDocIds());
                    List<Docs> newDocsState = new ArrayList<>();

                    if (realisation.getDocs() != null) {
                        for (Docs d : realisation.getDocs()) {
                            if (requested.remove(d.getId())) {
                                if (Boolean.TRUE.equals(d.getIsDeleted())) {
                                    d.setIsDeleted(false);
                                    docsRepository.save(d);
                                }
                                newDocsState.add(d);
                            } else {
                                if (!Boolean.TRUE.equals(d.getIsDeleted())) {
                                    d.setIsDeleted(true);
                                    docsRepository.save(d);
                                    log.info("Marked realisation doc {} as deleted", d.getId());
                                }
                                newDocsState.add(d);
                            }
                        }
                    }

                    for (Integer docId : requested) {
                        Docs d = docsRepository.findById(docId)
                                .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                        if (d.getRealisations() == null) d.setRealisations(new ArrayList<>());
                        if (!d.getRealisations().contains(realisation)) d.getRealisations().add(realisation);
                        d.setIsDeleted(false);
                        docsRepository.save(d);
                        newDocsState.add(d);
                    }
                    realisation.setDocs(newDocsState);
                } else if (realisation.getDocs() != null && !realisation.getDocs().isEmpty()) {
                    for (Docs d : realisation.getDocs()) {
                        if (!Boolean.TRUE.equals(d.getIsDeleted())) {
                            d.setIsDeleted(true);
                            docsRepository.save(d);
                            log.info("Marked realisation doc {} as deleted (no docIds provided)", d.getId());
                        }
                    }
                }

                // ---- Realisation files
                if (rReq.getRealisationFiles() != null) {
                    try {
                        List<Docs> before = (realisation.getDocs() == null) ? new ArrayList<>() : new ArrayList<>(realisation.getDocs());
                        documentCommuneService.processAndSaveDocuments(
                                rReq.getRealisationFiles(),
                                realisation,
                                Constants.PATH_ENTREPOT_GED_PAA + "/" + existingPaa.getId() + Constants.PATH_ENTREPOT_GED_REALISATION
                        );
                        List<Docs> after = realisation.getDocs();
                        if (after != null) {
                            Set<Integer> ids = before.stream().map(Docs::getId).collect(Collectors.toSet());
                            for (Docs nd : after) {
                                if (!ids.contains(nd.getId())) before.add(nd);
                            }
                            realisation.setDocs(before);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to process realisation files: " + e.getMessage(), e);
                    }
                }

                // ---- Lots
                if (rReq.getLots() != null && !rReq.getLots().isEmpty()) {
                    List<Lot> lots = new ArrayList<>();
                    Set<Integer> requestedLotIds = rReq.getLots().stream()
                            .filter(lr -> lr.getId() != null)
                            .map(LotResponse::getId)
                            .collect(Collectors.toSet());

                    for (LotResponse lr : rReq.getLots()) {
                        Lot lot;
                        Integer lotId = lr.getId();
                        if (lotId != null) {
                            lot = lotRepository.findById(new CompositeId(lotId, 1)).orElseGet(Lot::new);
                            if (lot.getId() == null) {
                                lot.setId(lotId);
                                lot.setVersion(1);
                            }
                        } else {
                            Integer nextLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                            lot = new Lot();
                            lot.setId(nextLotId);
                            lot.setVersion(1);
                        }
                        lot.setDescription(lr.getDescription());
                        lot.setBudget(lr.getBudget());
                        lot.setIsDeleted(false);
                        lots.add(lotRepository.save(lot));
                    }

                    if (realisation.getLots() != null) {
                        for (Lot ex : realisation.getLots()) {
                            if (!requestedLotIds.contains(ex.getId()) && !Boolean.TRUE.equals(ex.getIsDeleted())) {
                                ex.setIsDeleted(true);
                                lotRepository.save(ex);
                                lots.add(ex);
                                log.info("Marked lot {} as deleted (not in request)", ex.getId());
                            }
                        }
                    }
                    realisation.setLots(lots);
                } else {
                    if (realisation.getLots() != null) {
                        for (Lot ex : realisation.getLots()) {
                            if (!Boolean.TRUE.equals(ex.getIsDeleted())) {
                                ex.setIsDeleted(true);
                                lotRepository.save(ex);
                                log.info("Marked lot {} as deleted (no lots provided)", ex.getId());
                            }
                        }
                        realisation.setLots(new ArrayList<>());
                    }
                }

                // ---- Financements (UPDATE path) — safe pattern
                if (rReq.getFinancements() != null) {
                    Map<Integer, Financement> existingById = (realisation.getFinancements() == null ? (Map<Integer, Financement>) List.<Financement>of()
                            : realisation.getFinancements().stream()
                            .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                            .collect(Collectors.toMap(f -> f.getId().getFinancementId(), f -> f, (a, b) -> a)));

                    if (realisation.getFinancements() == null) {
                        realisation.setFinancements(new ArrayList<>());
                    }

                    List<Financement> newState = new ArrayList<>();

                    for (FinancementRequest fr : rReq.getFinancements()) {
                        if (fr.getMonnaieId() == null) {
                            log.warn("Skipping financement with null monnaieId for Realisation {}", realisation.getId());
                            continue;
                        }

                        if (fr.getFinancementId() != null) {
                            Financement fin = existingById.remove(fr.getFinancementId());
                            if (fin == null) {
                                throw new EntityNotFoundException("Financement with ID " + fr.getFinancementId() + " not found on this Realisation");
                            }
                            fin.setBudgetEstimer(fr.getBudgetEstimer());

                            Monnaie monnaie = monnaieRepository.findById(fr.getMonnaieId())
                                    .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " + fr.getMonnaieId() + " not found"));
                            fin.setMonnaie(monnaie);
                            fin.setTauxApplicable(monnaie.getTaux() != null ? monnaie.getTaux() : 0.0);

                            if (fr.getSourceFinancement() != null && fr.getSourceFinancement().getId() != null) {
                                RefParam src = refParamRepository.findById(fr.getSourceFinancement().getId())
                                        .orElseThrow(() -> new EntityNotFoundException("Source Financement with ID " + fr.getSourceFinancement().getId() + " not found"));
                                fin.setSourceFinancement(src);
                            } else {
                                fin.setSourceFinancement(null);
                            }

                            fin.setIsDeleted(false);
                            newState.add(fin);
                        } else {
                            Integer nextId = generateFinancementIdWithRetry(); // IMPORTANT: no +1
                            Financement fin = new Financement();

                            FinancementId fid = new FinancementId();
                            fid.setFinancementId(nextId);
                            fid.setRealisation(realisation); // SAME managed instance
                            fin.setId(fid);

                            Monnaie monnaie = monnaieRepository.findById(fr.getMonnaieId())
                                    .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " + fr.getMonnaieId() + " not found"));
                            fin.setMonnaie(monnaie);
                            fin.setTauxApplicable(monnaie.getTaux() != null ? monnaie.getTaux() : 0.0);

                            if (fr.getSourceFinancement() != null && fr.getSourceFinancement().getId() != null) {
                                RefParam src = refParamRepository.findById(fr.getSourceFinancement().getId())
                                        .orElseThrow(() -> new EntityNotFoundException("Source Financement with ID " + fr.getSourceFinancement().getId() + " not found"));
                                fin.setSourceFinancement(src);
                            } else {
                                fin.setSourceFinancement(null);
                            }

                            fin.setBudgetEstimer(fr.getBudgetEstimer());
                            fin.setIsDeleted(false);

                            // DO NOT save here
                            newState.add(fin);
                        }
                    }

                    // Soft-delete leftovers
                    for (Financement leftover : existingById.values()) {
                        if (!Boolean.TRUE.equals(leftover.getIsDeleted())) {
                            leftover.setIsDeleted(true);
                            log.info("Soft-deleting Financement ID: {}", leftover.getId().getFinancementId());
                        }
                        newState.add(leftover);
                    }

                    realisation.setFinancements(newState);

                    // === Recompute budget as Σ(budgetEstimer × Monnaie.taux)  << ONLY CHANGE HERE
                    BigDecimal rb = newState.stream()
                            .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                            .map(f -> {
                                BigDecimal budget = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0;
                                return budget.multiply(BigDecimal.valueOf(taux));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    realisation.setBudgetEstime(rb);
                } else {
                    // No financements array -> soft delete all
                    if (realisation.getFinancements() != null) {
                        realisation.getFinancements().forEach(f -> {
                            if (!Boolean.TRUE.equals(f.getIsDeleted())) f.setIsDeleted(true);
                        });
                    } else {
                        realisation.setFinancements(new ArrayList<>());
                    }
                    realisation.setBudgetEstime(BigDecimal.ZERO);
                }

                // ---- Seuil AFTER collections/budget are consistent (avoids auto-flush issues)
                setSeuilApplicableForRealisation(realisation, requestAcId);

                // link PAA if missing
                if (realisation.getPaas() == null) realisation.setPaas(new ArrayList<>());
                if (!realisation.getPaas().contains(existingPaa)) realisation.getPaas().add(existingPaa);

                // Save the parent once
                realisation = realisationRepository.save(realisation);
                realisations.add(realisation);

            } else {
                // ============== CREATE new Realisation
                Realisation real = new Realisation();
                real.setIsDeleted(false);

                Integer nextRealId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
                real.setId(nextRealId);            // IMPORTANT: no +1
                real.setVersion(1);

                real.setRealisationEnvisagees(rReq.getRealisationEnvisagees());
                real.setBeneficiaire(rReq.getBeneficiaire());
                real.setImputationBudgetaire(rReq.getImputationBudgetaire());
                real.setTypeMarcher(rReq.getTypeMarcher());
                real.setModePassation(rReq.getModePassation());
                real.setDatePrevLps(rReq.getDatePrevLps());
                real.setDatePrevAc(rReq.getDatePrevAc());
                real.setDatePrevDp(rReq.getDatePrevDp());
                real.setDatePrevAp(rReq.getDatePrevAp());
                real.setIsPublished(rReq.getIsPublished());
                real.setPublishedAt(rReq.getPublishedAt());
                real.setSort(rReq.getSort());

                real.setPaas(new ArrayList<>(Collections.singletonList(existingPaa)));
                real.setLots(new ArrayList<>());
                real.setFinancements(new ArrayList<>());
                real.setDocs(new ArrayList<>());

                Realisation savedReal = realisationRepository.save(real);

                // Docs
                if (rReq.getDocIds() != null && !rReq.getDocIds().isEmpty()) {
                    List<Docs> docs = new ArrayList<>();
                    for (Integer docId : rReq.getDocIds()) {
                        Docs d = docsRepository.findById(docId)
                                .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                        if (d.getRealisations() == null) d.setRealisations(new ArrayList<>());
                        if (!d.getRealisations().contains(savedReal)) d.getRealisations().add(savedReal);
                        d.setIsDeleted(false);
                        docsRepository.save(d);
                        docs.add(d);
                    }
                    savedReal.setDocs(docs);
                }

                // Lots
                if (rReq.getLots() != null && !rReq.getLots().isEmpty()) {
                    List<Lot> lots = new ArrayList<>();
                    for (LotResponse lr : rReq.getLots()) {
                        Lot lot = new Lot();
                        Integer lotId = (lr.getId() != null) ? lr.getId() : sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                        lot.setId(lotId); // no +1
                        lot.setVersion(1);
                        lot.setDescription(lr.getDescription());
                        lot.setBudget(lr.getBudget());
                        lot.setIsDeleted(false);
                        lots.add(lotRepository.save(lot));
                    }
                    savedReal.setLots(lots);
                }

                // Financements (CREATE path)
                if (rReq.getFinancements() != null && !rReq.getFinancements().isEmpty()) {
                    List<Financement> fins = new ArrayList<>();
                    for (FinancementRequest fr : rReq.getFinancements()) {
                        if (fr.getMonnaieId() == null) {
                            log.warn("Skipping financement with null monnaieId for new Realisation {}", savedReal.getId());
                            continue;
                        }
                        Integer nextFinId = generateFinancementIdWithRetry(); // no +1

                        Financement fin = new Financement();
                        FinancementId fid = new FinancementId();
                        fid.setFinancementId(nextFinId);
                        fid.setRealisation(savedReal);
                        fin.setId(fid);

                        Monnaie monnaie = monnaieRepository.findById(fr.getMonnaieId())
                                .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " + fr.getMonnaieId() + " not found"));
                        fin.setMonnaie(monnaie);
                        fin.setTauxApplicable(monnaie.getTaux() != null ? monnaie.getTaux() : 0.0);

                        if (fr.getSourceFinancement() != null && fr.getSourceFinancement().getId() != null) {
                            RefParam src = refParamRepository.findById(fr.getSourceFinancement().getId())
                                    .orElseThrow(() -> new EntityNotFoundException("Source Financement with ID " + fr.getSourceFinancement().getId() + " not found"));
                            fin.setSourceFinancement(src);
                        } else {
                            fin.setSourceFinancement(null);
                        }

                        fin.setBudgetEstimer(fr.getBudgetEstimer());
                        fin.setIsDeleted(false);
                        fins.add(fin); // no explicit save
                    }

                    savedReal.setFinancements(fins);

                    // === Compute budget as Σ(budgetEstimer × Monnaie.taux)  << ONLY CHANGE HERE
                    BigDecimal rb = fins.stream()
                            .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                            .map(f -> {
                                BigDecimal budget = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0;
                                return budget.multiply(BigDecimal.valueOf(taux));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    savedReal.setBudgetEstime(rb);
                } else {
                    savedReal.setFinancements(new ArrayList<>());
                    savedReal.setBudgetEstime(BigDecimal.ZERO);
                }

                // Files
                if (rReq.getRealisationFiles() != null) {
                    try {
                        documentCommuneService.processAndSaveDocuments(
                                rReq.getRealisationFiles(),
                                savedReal,
                                Constants.PATH_ENTREPOT_GED_PAA + "/" + existingPaa.getId() + Constants.PATH_ENTREPOT_GED_REALISATION
                        );
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to process realisation files: " + e.getMessage(), e);
                    }
                }

                // Seuil
                setSeuilApplicableForRealisation(savedReal, requestAcId);

                Realisation finalSaved = realisationRepository.save(savedReal);
                realisations.add(finalSaved);
                processedRealisationIds.add(finalSaved.getId());
            }
        }

        // Soft delete realisations not present
        if (existingPaa.getRealisations() != null) {
            for (Realisation ex : existingPaa.getRealisations()) {
                if (!processedRealisationIds.contains(ex.getId()) && !Boolean.TRUE.equals(ex.getIsDeleted())) {
                    ex.setIsDeleted(true);
                    ex.setBudgetEstime(BigDecimal.ZERO);
                    realisationRepository.save(ex);
                    log.info("Marked Realisation {} as deleted (not in request)", ex.getId());
                    realisations.add(ex);
                }
            }
        }

        // Deduplicate (id:version)
        List<Realisation> uniqueRealisations = realisations.stream()
                .filter(r -> r != null && r.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(r -> r.getId() + ":" + r.getVersion(), r -> r, (a, b) -> a),
                        m -> new ArrayList<>(m.values())
                ));
        existingPaa.setRealisations(uniqueRealisations);

    } else {
        // No realisations in request -> soft delete all
        if (existingPaa.getRealisations() != null && !existingPaa.getRealisations().isEmpty()) {
            for (Realisation ex : existingPaa.getRealisations()) {
                if (!Boolean.TRUE.equals(ex.getIsDeleted())) {
                    ex.setIsDeleted(true);
                    ex.setBudgetEstime(BigDecimal.ZERO);
                    realisationRepository.save(ex);
                    log.info("Marked Realisation {} as deleted (no realisations provided)", ex.getId());
                }
            }
            List<Realisation> unique = existingPaa.getRealisations().stream()
                    .filter(r -> r != null && r.getId() != null)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(r -> r.getId() + ":" + r.getVersion(), r -> r, (a, b) -> a),
                            m -> new ArrayList<>(m.values())
                    ));
            existingPaa.setRealisations(unique);
        } else {
            existingPaa.setRealisations(new ArrayList<>());
        }
        existingPaa.setBudgetEstime(BigDecimal.ZERO);
        log.info("Cleared realisations for PAA ID: {}, set budgetEstime: 0", paaId);
    }

    // === Validate and save PAA
    validateRealisations(existingPaa);

    Paa savedPaa = repository.save(existingPaa);

    // Build filtered response copy
    Paa copyForResponse = new Paa();
    BeanUtils.copyProperties(savedPaa, copyForResponse);

    if (savedPaa.getDocs() != null) {
        copyForResponse.setDocs(
                savedPaa.getDocs().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                        .collect(Collectors.toList())
        );
    }
    if (savedPaa.getRealisations() != null) {
        copyForResponse.setRealisations(
                savedPaa.getRealisations().stream()
                        .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                        .collect(Collectors.toList())
        );
        copyForResponse.getRealisations().forEach(r -> {
            if (r.getDocs() != null) {
                r.setDocs(r.getDocs().stream().filter(d -> !Boolean.TRUE.equals(d.getIsDeleted())).collect(Collectors.toList()));
            }
            if (r.getLots() != null) {
                r.setLots(r.getLots().stream().filter(l -> !Boolean.TRUE.equals(l.getIsDeleted())).collect(Collectors.toList()));
            }
            if (r.getFinancements() != null) {
                r.setFinancements(r.getFinancements().stream().filter(f -> !Boolean.TRUE.equals(f.getIsDeleted())).collect(Collectors.toList()));
            }
            if (r.getSeuilApplicable() != null && r.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
                log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", r.getId());
                r.setSeuilApplicable(BigDecimal.ZERO);
            }
        });
    }

    BigDecimal budgetEstime = savedPaa.getRealisations() == null ? BigDecimal.ZERO :
            savedPaa.getRealisations().stream()
                    .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                    .map(Realisation::getBudgetEstime)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

    savedPaa.setBudgetEstime(budgetEstime);
    repository.save(savedPaa);
    copyForResponse.setBudgetEstime(budgetEstime);

    log.info("Updated PAA ID: {}, budgetEstime: {}", paaId, budgetEstime);
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
            Set<Integer> activeExistingDocIds = existingPaa.getDocs() != null ? existingPaa.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).map(Docs::getId).collect(Collectors.toSet()) : new HashSet<>();
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

        // Check if files are present
        boolean hasFiles = request.getPaaFiles() != null && !request.getPaaFiles().isEmpty();
        if (hasFiles) {
            System.out.println("Files detected in request - will create new version regardless of other changes");
        } else {
            System.out.println("Current request hash: " + requestHash);
            System.out.println("Previous request hash: " + previousRequestHash);
            boolean hasRealisationsWithoutIds = false;
            if (request.getRealisations() != null) {
                hasRealisationsWithoutIds = request.getRealisations().stream()
                        .anyMatch(r -> r.getId() == null);
            }

            boolean hasRealisationFiles = hasRealisationFiles(request.getRealisations());
            if (requestHash != null && requestHash.equals(previousRequestHash) &&
                    !documentsChanged && !hasNewLotsWithoutIds && !hasRealisationFiles &&
                    !hasRealisationsWithoutIds) {
                System.out.println("Duplicate request detected and no document changes, returning existing PAA without changes");

                Paa copyForResponse = new Paa();
                BeanUtils.copyProperties(existingPaa, copyForResponse);

                if (existingPaa.getDocs() != null) {
                    copyForResponse.setDocs(existingPaa.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                }

                if (copyForResponse.getRealisations() != null) {
                    copyForResponse.getRealisations().forEach(realisation -> {
                        if (realisation.getDocs() != null) {
                            realisation.setDocs(realisation.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                        }
                        if (realisation.getLots() != null) {
                            realisation.setLots(realisation.getLots().stream().filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted())).collect(Collectors.toList()));
                        }
                        if (realisation.getSeuilApplicable() != null && realisation.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
                            log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", realisation.getId());
                            realisation.setSeuilApplicable(BigDecimal.ZERO);
                        }
                    });
                }

                return copyForResponse;
            }
        }

        // Create a temporary PAA to check for changes in direct fields
        Paa tempPaa = new Paa();
        tempPaa.setRef(existingPaa.getRef());
        tempPaa.setNom(request.getNom() != null ? request.getNom() : existingPaa.getNom());
        tempPaa.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPaa.getAnnee());
        tempPaa.setProcessInstanceId(existingPaa.getProcessInstanceId());
        tempPaa.setStatus(existingPaa.getStatus());
        tempPaa.setModePassation(request.getModePassation() != null ? request.getModePassation() : existingPaa.getModePassation());
        tempPaa.setRemarque(request.getRemarque() != null ? request.getRemarque() : existingPaa.getRemarque());

        Ac requestAc = null;
        if (request.getAcId() != null) {
            requestAc = acRepository.findById(request.getAcId()).orElse(null);
        }
        tempPaa.setAc(requestAc != null ? requestAc : existingPaa.getAc());

        Integer requestAcId = request.getAcId() != null ? request.getAcId() : existingPaa.getAc().getId();

        boolean hasDirectChanges = !arePaaFieldsIdentical(existingPaa, tempPaa);
        System.out.println("Direct field changes detected: " + hasDirectChanges);

        boolean realisationsChanged = false;
        List<Realisation> updatedRealisations = new ArrayList<>();
        List<Docs> updatedDocs = new ArrayList<>();
        Map<String, Lot> newLotsMap = new HashMap<>(); // Used for new realisations only

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

                        boolean hasRealisationFiles = realisationDto.getRealisationFiles() != null && !realisationDto.getRealisationFiles().isEmpty();
                        boolean hasLots = realisationDto.getLots() != null && !realisationDto.getLots().isEmpty();
                        if (hasRealisationFiles || hasLots) {
                            System.out.println("Files or lots detected in realisation, forcing update");
                            updateNeeded = true;
                        }

                        if (updateNeeded) {
                            System.out.println("Updating Realisation with versioning...");
                            List<Lot> updatedLots = new ArrayList<>();
                            Set<Integer> processedLotIds = new HashSet<>();
                            boolean lotChanges = false;

                            Set<Integer> existingLotIds = existingRealisation.getLots() != null
                                    ? existingRealisation.getLots().stream()
                                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                    .map(Lot::getId)
                                    .collect(Collectors.toSet())
                                    : new HashSet<>();

                            if (hasLots) {
                                System.out.println("Processing " + realisationDto.getLots().size() + " lots in DTO");
                                if (existingRealisation.getLots() != null) {
                                    System.out.println("Existing lots for Realisation " + existingRealisation.getId() +
                                            ", version " + existingRealisation.getVersion() + ":");
                                    for (Lot existingLot : existingRealisation.getLots()) {
                                        System.out.println("  - Lot ID: " + existingLot.getId() +
                                                ", Description: " + existingLot.getDescription() +
                                                ", Budget: " + existingLot.getBudget() +
                                                ", isDeleted: " + existingLot.getIsDeleted());
                                    }
                                } else {
                                    System.out.println("No existing lots for Realisation " + existingRealisation.getId());
                                }

                                for (LotResponse lotDto : realisationDto.getLots()) {
                                    String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                    System.out.println("Processing lot with description: " + lotHash + ", ID: " + lotDto.getId());

                                    if (lotDto.getId() == null) {
                                        System.out.println("Creating new lot with description: " + lotHash);
                                        Lot newLot = new Lot();
                                        newLot.setDescription(lotDto.getDescription());
                                        newLot.setBudget(lotDto.getBudget());
                                        newLot.setVersion(1);
                                        Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                                        if (maxLotId == null) {
                                            System.err.println("Warning: sequenceGeneratorService returned null for LOT_ID_SEQ");
                                            maxLotId = 0;
                                        }
                                        Integer lotId = maxLotId + 1;
                                        newLot.setId(lotId);

                                        Lot savedLot = lotRepository.save(newLot);
                                        updatedLots.add(savedLot);
                                        processedLotIds.add(savedLot.getId());
                                        System.out.println("Created new Lot with ID: " + savedLot.getId() +
                                                ", Version: " + savedLot.getVersion() +
                                                ", Budget: " + savedLot.getBudget());
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
                                            BigDecimal requestBudget = lotDto.getBudget();
                                            BigDecimal existingBudget = existingLot.getBudget();

                                            boolean descChanged = !Objects.equals(requestDesc, existingDesc);
                                            boolean budgetChanged = (requestBudget != null && existingBudget != null && requestBudget.compareTo(existingBudget) != 0) ||
                                                    (requestBudget != null && existingBudget == null) ||
                                                    (requestBudget == null && existingBudget != null);

                                            if (descChanged || budgetChanged) {
                                                System.out.println("Changes detected for Lot ID: " + lotId +
                                                        ", Description changed: " + (descChanged ? requestDesc + " != " + existingDesc : "no") +
                                                        ", Budget changed: " + (budgetChanged ? requestBudget + " != " + existingBudget : "no"));
                                                Lot newLot = new Lot();
                                                newLot.setId(existingLot.getId());
                                                newLot.setVersion(existingLot.getVersion() + 1);
                                                newLot.setDescription(lotDto.getDescription());
                                                newLot.setBudget(lotDto.getBudget());
                                                Lot savedLot = lotRepository.save(newLot);
                                                updatedLots.add(savedLot);
                                                processedLotIds.add(savedLot.getId());
                                                System.out.println("Created new version of Lot: " + savedLot.getId() +
                                                        ", Version: " + savedLot.getVersion() +
                                                        ", Budget: " + savedLot.getBudget());
                                                lotChanges = true;
                                            } else {
                                                updatedLots.add(existingLot);
                                                processedLotIds.add(existingLot.getId());
                                                System.out.println("No changes for Lot ID: " + existingLot.getId() +
                                                        ", Version: " + existingLot.getVersion() +
                                                        ", Budget: " + existingLot.getBudget());
                                            }
                                        }
                                    }
                                }
                                // Mark existing lots as deleted if not in request
                                if (existingRealisation.getLots() != null) {
                                    for (Lot existingLot : existingRealisation.getLots()) {
                                        if (!processedLotIds.contains(existingLot.getId()) && !Boolean.TRUE.equals(existingLot.getIsDeleted())) {
                                            Lot newLotVersion = new Lot();
                                            newLotVersion.setId(existingLot.getId());
                                            newLotVersion.setVersion(existingLot.getVersion() + 1);
                                            newLotVersion.setDescription(existingLot.getDescription());
                                            newLotVersion.setBudget(existingLot.getBudget());
                                            newLotVersion.setIsDeleted(true);
                                            lotRepository.save(newLotVersion);
                                            System.out.println("Marked lot " + existingLot.getId() +
                                                    " as deleted in new version, Budget: " + existingLot.getBudget());
                                            lotChanges = true;
                                        }
                                    }
                                }
                            } else {
                                if (existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty()) {
                                    for (Lot existingLot : existingRealisation.getLots()) {
                                        if (!Boolean.TRUE.equals(existingLot.getIsDeleted())) {
                                            Lot newLotVersion = new Lot();
                                            newLotVersion.setId(existingLot.getId());
                                            newLotVersion.setVersion(existingLot.getVersion() + 1);
                                            newLotVersion.setDescription(existingLot.getDescription());
                                            newLotVersion.setBudget(existingLot.getBudget());
                                            newLotVersion.setIsDeleted(true);
                                            lotRepository.save(newLotVersion);
                                            System.out.println("Marked lot " + existingLot.getId() +
                                                    " as deleted (no lots in request), Budget: " + existingLot.getBudget());
                                            lotChanges = true;
                                        }
                                    }
                                    updatedLots.clear();
                                }
                            }

                            // Update Realisation via service
                            Realisation updatedRealisation = realisationService.updateWithVersioning(realisationDto.getId(), versionToUse, realisationDto);

                            // === ADDED: FX conversion ONLY from Monnaie.taux for seuil
                            try {
                                BigDecimal rbFx = (updatedRealisation.getFinancements() == null ? BigDecimal.ZERO :
                                        updatedRealisation.getFinancements().stream()
                                                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                                                .map(f -> {
                                                    BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                                    Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0;
                                                    return amt.multiply(BigDecimal.valueOf(taux));
                                                })
                                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                                updatedRealisation.setBudgetEstime(rbFx);
                                System.out.println("[SEUIL FX] Realisation " + updatedRealisation.getId() + " converted budget (Σ amt×taux) for seuil: " + rbFx);
                            } catch (Exception e) {
                                System.err.println("[SEUIL FX] Failed to compute converted budget for Realisation " + updatedRealisation.getId() + ": " + e.getMessage());
                            }
                            // Seuil using converted amount
                            setSeuilApplicableForRealisation(updatedRealisation, requestAcId);

                            // Set lots and keep your original budget logic
                            updatedRealisation.setLots(updatedLots);
                            BigDecimal userBudget = realisationDto.getBudgetEstime();
                            if (userBudget != null) {
                                updatedRealisation.setBudgetEstime(userBudget);
                                System.out.println("Set Realisation ID: " + updatedRealisation.getId() +
                                        " budgetEstime from user input: " + userBudget);
                            } else {
                                BigDecimal calculatedBudget = updatedLots.stream()
                                        .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                        .map(Lot::getBudget)
                                        .filter(Objects::nonNull)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                updatedRealisation.setBudgetEstime(calculatedBudget);
                                System.out.println("Set Realisation ID: " + updatedRealisation.getId() +
                                        " budgetEstime from lots: " + calculatedBudget);
                            }
                            updatedRealisation = realisationRepository.save(updatedRealisation);
                            System.out.println("Updated Realisation ID: " + updatedRealisation.getId() +
                                    " version: " + updatedRealisation.getVersion() +
                                    ", budgetEstime: " + updatedRealisation.getBudgetEstime());

                            if (hasRealisationFiles) {
                                try {
                                    System.out.println("Processing " + realisationDto.getRealisationFiles().size() +
                                            " files for Realisation ID: " + updatedRealisation.getId() +
                                            " version: " + updatedRealisation.getVersion());
                                    documentCommuneService.processAndSaveDocuments(realisationDto.getRealisationFiles(), updatedRealisation, Constants.PATH_ENTREPOT_GED_PAA + "/" + existingPaa.getId() + Constants.PATH_ENTREPOT_GED_REALISATION);
                                    updatedRealisation = realisationRepository.save(updatedRealisation);
                                    System.out.println("Saved Realisation after processing files");
                                } catch (IOException e) {
                                    System.err.println("Error processing Realisation files: " + e.getMessage());
                                    throw new RuntimeException("Error processing Realisation files: " + e.getMessage(), e);
                                }
                            }

                            if (realisationDto.getDocIds() != null) {
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
                                    docsRepository.save(doc);
                                    allDocs.add(doc);
                                    System.out.println("[DEBUG] Added docId from docIds: " + docId);
                                }

                                updatedRealisation.setDocs(allDocs);
                                updatedRealisation = realisationRepository.save(updatedRealisation);
                            }

                            updatedRealisations.add(updatedRealisation);
                            realisationsChanged = true;
                        } else {
                            System.out.println("Using existing Realisation without changes");

                            // === ADDED: FX conversion ONLY from Monnaie.taux for seuil
                            try {
                                BigDecimal rbFx = (existingRealisation.getFinancements() == null ? BigDecimal.ZERO :
                                        existingRealisation.getFinancements().stream()
                                                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                                                .map(f -> {
                                                    BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                                    Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0;
                                                    return amt.multiply(BigDecimal.valueOf(taux));
                                                })
                                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                                existingRealisation.setBudgetEstime(rbFx);
                                System.out.println("[SEUIL FX] Realisation " + existingRealisation.getId() + " converted budget (Σ amt×taux) for seuil: " + rbFx);
                            } catch (Exception e) {
                                System.err.println("[SEUIL FX] Failed to compute converted budget for Realisation " + existingRealisation.getId() + ": " + e.getMessage());
                            }
                            // Seuil using converted amount
                            setSeuilApplicableForRealisation(existingRealisation, requestAcId);

                            if (hasRealisationFiles || hasLots) {
                                if (hasRealisationFiles) {
                                    try {
                                        System.out.println("Processing " + realisationDto.getRealisationFiles().size() +
                                                " files for existing Realisation ID: " + existingRealisation.getId() +
                                                " version: " + existingRealisation.getVersion());
                                        documentCommuneService.processAndSaveDocuments(realisationDto.getRealisationFiles(), existingRealisation, Constants.PATH_ENTREPOT_GED_PAA + "/" + existingPaa.getId() + Constants.PATH_ENTREPOT_GED_REALISATION);
                                        existingRealisation = realisationRepository.save(existingRealisation);
                                        System.out.println("Saved existing Realisation after processing files");
                                        realisationsChanged = true;
                                    } catch (IOException e) {
                                        System.err.println("Error processing existing Realisation files: " + e.getMessage());
                                        throw new RuntimeException("Error processing existing Realisation files: " + e.getMessage(), e);
                                    }
                                }

                                if (hasLots) {
                                    List<Lot> updatedLots = new ArrayList<>();
                                    Set<Integer> processedLotIds = new HashSet<>();
                                    boolean lotChanges = false;

                                    Set<Integer> existingLotIds = existingRealisation.getLots() != null
                                            ? existingRealisation.getLots().stream()
                                            .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                            .map(Lot::getId)
                                            .collect(Collectors.toSet())
                                            : new HashSet<>();

                                    for (LotResponse lotDto : realisationDto.getLots()) {
                                        String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                        System.out.println("Processing lot with description: " + lotHash + ", ID: " + lotDto.getId());

                                        if (lotDto.getId() == null) {
                                            boolean lotExists = false;
                                            if (existingRealisation.getLots() != null) {
                                                for (Lot existingLot : existingRealisation.getLots()) {
                                                    String existingDesc = existingLot.getDescription() != null ? existingLot.getDescription() : "";
                                                    if (existingDesc.equals(lotHash) && !Boolean.TRUE.equals(existingLot.getIsDeleted())) {
                                                        System.out.println("Found matching active lot in Realisation " + existingRealisation.getId() +
                                                                ", Lot ID: " + existingLot.getId() +
                                                                ", Description: " + lotHash +
                                                                ", Budget: " + existingLot.getBudget() +
                                                                ", skipping new lot creation");
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
                                            newLot.setBudget(lotDto.getBudget());
                                            newLot.setVersion(1);
                                            Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                                            Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                                            newLot.setId(lotId);

                                            Lot savedLot = lotRepository.save(newLot);
                                            updatedLots.add(savedLot);
                                            processedLotIds.add(savedLot.getId());
                                            System.out.println("Created new Lot with ID: " + savedLot.getId() +
                                                    ", Version: " + savedLot.getVersion() +
                                                    ", Budget: " + savedLot.getBudget());
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
                                                BigDecimal requestBudget = lotDto.getBudget();
                                                BigDecimal existingBudget = existingLot.getBudget();

                                                boolean descChanged = !Objects.equals(requestDesc, existingDesc);
                                                boolean budgetChanged = (requestBudget != null && existingBudget != null && requestBudget.compareTo(existingBudget) != 0) ||
                                                        (requestBudget != null && existingBudget == null) ||
                                                        (requestBudget == null && existingBudget != null);

                                                if (descChanged || budgetChanged) {
                                                    System.out.println("Changes detected for Lot ID: " + lotId +
                                                            ", Description changed: " + (descChanged ? requestDesc + " != " + existingDesc : "no") +
                                                            ", Budget changed: " + (budgetChanged ? requestBudget + " != " + existingBudget : "no"));
                                                    Lot newLot = new Lot();
                                                    newLot.setId(existingLot.getId());
                                                    newLot.setVersion(existingLot.getVersion() + 1);
                                                    newLot.setDescription(lotDto.getDescription());
                                                    newLot.setBudget(lotDto.getBudget());
                                                    Lot savedLot = lotRepository.save(newLot);
                                                    updatedLots.add(savedLot);
                                                    processedLotIds.add(savedLot.getId());
                                                    System.out.println("Created new version of Lot: " + savedLot.getId() +
                                                            ", Version: " + savedLot.getVersion() +
                                                            ", Budget: " + savedLot.getBudget());
                                                    lotChanges = true;
                                                } else {
                                                    updatedLots.add(existingLot);
                                                    processedLotIds.add(existingLot.getId());
                                                    System.out.println("No changes for Lot ID: " + existingLot.getId() +
                                                            ", Version: " + existingLot.getVersion() +
                                                            ", Budget: " + existingLot.getBudget());
                                                }
                                            }
                                        }
                                    }

                                    if (existingRealisation.getLots() != null) {
                                        for (Lot existingLot : existingRealisation.getLots()) {
                                            if (!processedLotIds.contains(existingLot.getId()) && !Boolean.TRUE.equals(existingLot.getIsDeleted())) {
                                                Lot newLotVersion = new Lot();
                                                newLotVersion.setId(existingLot.getId());
                                                newLotVersion.setVersion(existingLot.getVersion() + 1);
                                                newLotVersion.setDescription(existingLot.getDescription());
                                                newLotVersion.setBudget(existingLot.getBudget());
                                                newLotVersion.setIsDeleted(true);
                                                lotRepository.save(newLotVersion);
                                                System.out.println("Marked lot " + existingLot.getId() +
                                                        " as deleted in new version, Budget: " + existingLot.getBudget());
                                                lotChanges = true;
                                            }
                                        }
                                    }

                                    existingRealisation.setLots(updatedLots);
                                    BigDecimal userBudget = realisationDto.getBudgetEstime();
                                    if (userBudget != null) {
                                        existingRealisation.setBudgetEstime(userBudget);
                                        System.out.println("Set existing Realisation ID: " + existingRealisation.getId() +
                                                " budgetEstime from user input: " + userBudget);
                                    } else {
                                        BigDecimal calculatedBudget = updatedLots.stream()
                                                .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                                .map(Lot::getBudget)
                                                .filter(Objects::nonNull)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        existingRealisation.setBudgetEstime(calculatedBudget);
                                        System.out.println("Set existing Realisation ID: " + existingRealisation.getId() +
                                                " budgetEstime from lots: " + calculatedBudget);
                                    }
                                    existingRealisation = realisationRepository.save(existingRealisation);
                                    System.out.println("Updated existing Realisation ID: " + existingRealisation.getId() +
                                            " version: " + existingRealisation.getVersion() +
                                            ", budgetEstime: " + existingRealisation.getBudgetEstime());
                                    realisationsChanged = true;
                                }

                                updatedRealisations.add(existingRealisation);
                            } else {
                                //  FIX: Always add existing realization even when no files/lots
                                updatedRealisations.add(existingRealisation);
                                System.out.println(" Added existing realization to list: " + existingRealisation.getId() +
                                        " v" + existingRealisation.getVersion());
                            }
                        }
                    } else {
                        System.out.println("Creating new Realisation");
                        RealisationRequest newRealisationRequest = new RealisationRequest();
                        newRealisationRequest.setRealisationEnvisagees(realisationDto.getRealisationEnvisagees());
//                        newRealisationRequest.setService(realisationDto.getService());
//                        newRealisationRequest.setDirection(realisationDto.getDirection());
                        newRealisationRequest.setTypeMarcher(realisationDto.getTypeMarcher());
                        newRealisationRequest.setModePassation(realisationDto.getModePassation());
                        newRealisationRequest.setDatePrevLps(realisationDto.getDatePrevLps());
                        newRealisationRequest.setDatePrevAc(realisationDto.getDatePrevAc());
                        newRealisationRequest.setDatePrevDp(realisationDto.getDatePrevDp());
                        newRealisationRequest.setDatePrevAp(realisationDto.getDatePrevAp());
                        newRealisationRequest.setIsPublished(realisationDto.getIsPublished());
                        newRealisationRequest.setPublishedAt(realisationDto.getPublishedAt());
                        newRealisationRequest.setSort(realisationDto.getSort());
                        newRealisationRequest.setDocIds(realisationDto.getDocIds());

                        if (realisationDto.getLots() != null && !realisationDto.getLots().isEmpty()) {
                            newRealisationRequest.setLots((List<LotResponse>) realisationDto.getLots());
                        }
                        //  FIXED: Convert FinancementRequest to FinancementBaseRequestDto
                        if (realisationDto.getFinancements() != null && !realisationDto.getFinancements().isEmpty()) {
                            List<FinancementBaseRequestDto> financementBaseRequests = realisationDto.getFinancements().stream()
                                    .map(financementRequest -> {
                                        FinancementBaseRequestDto baseRequest = new FinancementBaseRequestDto();
                                        BeanUtils.copyProperties(financementRequest, baseRequest);
                                        return baseRequest;
                                    })
                                    .collect(Collectors.toList());

                            newRealisationRequest.setFinancementRequests(financementBaseRequests);
                            System.out.println(" Assigned " + financementBaseRequests.size() +
                                    " financements to new realization");
                        }
                        Realisation newRealisation = realisationService.create(newRealisationRequest);

                        // === ADDED: FX conversion ONLY from Monnaie.taux for seuil
                        try {
                            BigDecimal rbFx = (newRealisation.getFinancements() == null ? BigDecimal.ZERO :
                                    newRealisation.getFinancements().stream()
                                            .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                                            .map(f -> {
                                                BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                                Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0;
                                                return amt.multiply(BigDecimal.valueOf(taux));
                                            })
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                            newRealisation.setBudgetEstime(rbFx);
                            System.out.println("[SEUIL FX] New Realisation " + newRealisation.getId() + " converted budget (Σ amt×taux) for seuil: " + rbFx);
                        } catch (Exception e) {
                            System.err.println("[SEUIL FX] Failed to compute converted budget for new Realisation: " + e.getMessage());
                        }
                        // Seuil using converted amount
                        setSeuilApplicableForRealisation(newRealisation, requestAcId);

                        List<Lot> updatedLots = new ArrayList<>();
                        if (realisationDto.getLots() != null && !realisationDto.getLots().isEmpty()) {
                            for (LotResponse lotDto : realisationDto.getLots()) {
                                String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                if (newLotsMap.containsKey(lotHash + "_" + newRealisation.getId())) {
                                    System.out.println("Using previously created lot for description: " + lotHash +
                                            " in realisation " + newRealisation.getId());
                                    updatedLots.add(newLotsMap.get(lotHash + "_" + newRealisation.getId()));
                                    continue;
                                }

                                Lot newLot = new Lot();
                                newLot.setDescription(lotDto.getDescription());
                                newLot.setBudget(lotDto.getBudget());
                                newLot.setVersion(1);
                                Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                                Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                                newLot.setId(lotId);

                                Lot savedLot = lotRepository.save(newLot);
                                updatedLots.add(savedLot);
                                newLotsMap.put(lotHash + "_" + newRealisation.getId(), savedLot);
                                System.out.println("Created new Lot with ID: " + savedLot.getId() +
                                        ", Version: " + savedLot.getVersion() +
                                        ", Budget: " + savedLot.getBudget());
                            }
                        }

                        newRealisation.setLots(updatedLots);
                        BigDecimal userBudget = realisationDto.getBudgetEstime();
                        if (userBudget != null) {
                            newRealisation.setBudgetEstime(userBudget);
                            System.out.println("Set new Realisation ID: " + newRealisation.getId() +
                                    " budgetEstime from user input: " + userBudget);
                        } else {
                            BigDecimal calculatedBudget = updatedLots.stream()
                                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                    .map(Lot::getBudget)
                                    .filter(Objects::nonNull)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            newRealisation.setBudgetEstime(calculatedBudget);
                            System.out.println("Set new Realisation ID: " + newRealisation.getId() +
                                    " budgetEstime from lots: " + calculatedBudget);
                        }
                        newRealisation = realisationRepository.save(newRealisation);
                        System.out.println("Set new Realisation ID: " + newRealisation.getId() +
                                " version: " + newRealisation.getVersion() +
                                ", budgetEstime: " + newRealisation.getBudgetEstime());

                        if (realisationDto.getRealisationFiles() != null && !realisationDto.getRealisationFiles().isEmpty()) {
                            try {
                                System.out.println("Processing " + realisationDto.getRealisationFiles().size() +
                                        " files for new Realisation ID: " + newRealisation.getId() +
                                        " version: " + newRealisation.getVersion());
                                documentCommuneService.processAndSaveDocuments(realisationDto.getRealisationFiles(), newRealisation, Constants.PATH_ENTREPOT_GED_PAA + "/" + existingPaa.getId() + Constants.PATH_ENTREPOT_GED_REALISATION);
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

            if (existingPaa.getRealisations() != null && !existingPaa.getRealisations().isEmpty()) {
                // Get existing active realization IDs from PAA
                Set<Integer> existingRealizationIds = existingPaa.getRealisations().stream()
                        .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                        .map(Realisation::getId)
                        .collect(Collectors.toSet());

                // Get requested realization IDs
                Set<Integer> requestedRealizationIds = new HashSet<>();
                if (request.getRealisations() != null) {
                    requestedRealizationIds = request.getRealisations().stream()
                            .filter(r -> r.getId() != null)
                            .map(RealisationRequestForPaa::getId)
                            .collect(Collectors.toSet());
                }

                System.out.println("Existing realization IDs: " + existingRealizationIds);
                System.out.println("Requested realization IDs: " + requestedRealizationIds);

                // Find realizations to soft delete (existing but not in request)
                Set<Integer> realizationsToDelete = new HashSet<>(existingRealizationIds);
                realizationsToDelete.removeAll(requestedRealizationIds);

                if (!realizationsToDelete.isEmpty()) {
                    System.out.println("Soft deleting realizations: " + realizationsToDelete);
                    for (Integer realizationId : realizationsToDelete) {
                        // Find the realization in the existing PAA
                        Realisation realizationToDelete = existingPaa.getRealisations().stream()
                                .filter(r -> r.getId().equals(realizationId) && !Boolean.TRUE.equals(r.getIsDeleted()))
                                .findFirst()
                                .orElse(null);

                        if (realizationToDelete != null) {
                            System.out.println("Soft deleting realization ID: " + realizationId);

                            // Create new version with isDeleted = true
                            Realisation deletedVersion = new Realisation();
                            BeanUtils.copyProperties(realizationToDelete, deletedVersion);
                            deletedVersion.setVersion(realizationToDelete.getVersion() + 1);
                            deletedVersion.setIsDeleted(true);

                            // Also soft delete associated lots
                            if (realizationToDelete.getLots() != null) {
                                for (Lot lot : realizationToDelete.getLots()) {
                                    if (!Boolean.TRUE.equals(lot.getIsDeleted())) {
                                        Lot deletedLot = new Lot();
                                        BeanUtils.copyProperties(lot, deletedLot);
                                        deletedLot.setVersion(lot.getVersion() + 1);
                                        deletedLot.setIsDeleted(true);
                                        lotRepository.save(deletedLot);
                                        System.out.println("Soft deleted lot ID: " + lot.getId());
                                    }
                                }
                            }

                            realisationRepository.save(deletedVersion);
                            System.out.println("Successfully soft deleted realization ID: " + realizationId);
                            realisationsChanged = true;
                        }
                    }
                }
            }

            if (!realisationsChanged && existingPaa.getRealisations() != null) {
                if (existingPaa.getRealisations().size() != updatedRealisations.size()) {
                    System.out.println("Realisation count changed: " + existingPaa.getRealisations().size() + " -> " + updatedRealisations.size());
                    realisationsChanged = true;
                } else {
                    Map<Integer, Integer> existingRealisationVersions = existingPaa.getRealisations().stream().collect(Collectors.toMap(Realisation::getId, Realisation::getVersion, (a, b) -> a));

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
                if (!updatedRealisations.isEmpty()) {
                    System.out.println("Adding realisations to previously empty PAA");
                    realisationsChanged = true;
                }
            }
        } else if (existingPaa.getRealisations() != null && !existingPaa.getRealisations().isEmpty()) {
            System.out.println("Realisations removed from PAA");
            realisationsChanged = true;
            for (Realisation existingRealisation : existingPaa.getRealisations()) {
                if (existingRealisation.getLots() != null) {
                    for (Lot existingLot : existingRealisation.getLots()) {
                        if (!Boolean.TRUE.equals(existingLot.getIsDeleted())) {
                            Lot newLotVersion = new Lot();
                            newLotVersion.setId(existingLot.getId());
                            newLotVersion.setVersion(existingLot.getVersion() + 1);
                            newLotVersion.setDescription(existingLot.getDescription());
                            newLotVersion.setBudget(existingLot.getBudget());
                            newLotVersion.setIsDeleted(true);
                            lotRepository.save(newLotVersion);
                            System.out.println("Marked lot " + existingLot.getId() +
                                    " as deleted in new version, Budget: " + existingLot.getBudget());
                        }
                    }
                }
                existingRealisation.setBudgetEstime(BigDecimal.ZERO);
                realisationRepository.save(existingRealisation);
                System.out.println("Set Realisation ID: " + existingRealisation.getId() +
                        " budgetEstime: 0 (realisations removed)");
            }
        }

        // Process document IDs
        if (request.getDocIds() != null) {
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());
            Set<Integer> activeExistingDocIds = existingPaa.getDocs() != null ? existingPaa.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).map(Docs::getId).collect(Collectors.toSet()) : new HashSet<>();

            System.out.println("Comparing document IDs:");
            System.out.println("  - Existing active docs: " + activeExistingDocIds);
            System.out.println("  - Requested docs: " + requestDocIds);

            if (!requestDocIds.equals(activeExistingDocIds)) {
                System.out.println("Document ID sets are different - marking as changed");
                documentsChanged = true;
            }

            updatedDocs = new ArrayList<>();
            if (existingPaa.getDocs() != null) {
                for (Docs existingDoc : existingPaa.getDocs()) {
                    Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                    if (docToProcess == null) {
                        System.out.println("Warning: Document " + existingDoc.getId() + " no longer exists in database, skipping");
                        continue;
                    }

                    if (requestDocIds.contains(docToProcess.getId())) {
                        docToProcess.setIsDeleted(false);
                        docToProcess = docsRepository.save(docToProcess);
                        updatedDocs.add(docToProcess);
                        requestDocIds.remove(docToProcess.getId());
                    } else {
                        docToProcess.setIsDeleted(true);
                        docToProcess = docsRepository.save(docToProcess);
                        updatedDocs.add(docToProcess);
                        System.out.println("Marked document with ID " + docToProcess.getId() + " as deleted for new version");
                    }
                }
            }

            for (Integer docId : requestDocIds) {
                Docs doc = docsRepository.findById(docId).orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
                doc.setIsDeleted(false);
                doc = docsRepository.save(doc);
                updatedDocs.add(doc);
                System.out.println("Added new document with ID " + doc.getId() + " for new version");
            }
        } else if (existingPaa.getDocs() != null && !existingPaa.getDocs().isEmpty()) {
            updatedDocs = new ArrayList<>();
            for (Docs existingDoc : existingPaa.getDocs()) {
                Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                if (docToProcess == null) continue;

                docToProcess.setIsDeleted(true);
                docToProcess = docsRepository.save(docToProcess);
                updatedDocs.add(docToProcess);
                System.out.println("Marked document with ID " + docToProcess.getId() + " as deleted (no docIds provided)");
            }
            documentsChanged = true;
        } else {
            updatedDocs = new ArrayList<>();
        }

        // Determine if we need to create a new PAA version
        boolean shouldCreateNewVersion = hasDirectChanges || realisationsChanged || documentsChanged;

        System.out.println("Should create new PAA version: " + shouldCreateNewVersion);
        System.out.println("  - Direct field changes: " + hasDirectChanges);
        System.out.println("  - Realisations changed: " + realisationsChanged);
        System.out.println("  - Documents changed: " + documentsChanged);
        System.out.println("  - Files uploaded: " + (request.getPaaFiles() != null && !request.getPaaFiles().isEmpty()));

        if (request.getPaaFiles() != null && !request.getPaaFiles().isEmpty()) {
            System.out.println("Files are present - forcing creation of new version");
            shouldCreateNewVersion = true;
        }

        if (shouldCreateNewVersion) {
            System.out.println("Changes detected, creating new PAA version: " + (paaVer + 1));
            storePreviousRequestHash(paaId, paaVer, requestHash);
        } else {
            System.out.println("No changes detected, returning existing PAA version: " + paaVer);

            if (request.getPaaFiles() != null) {
                try {
                    System.out.println("Processing " + request.getPaaFiles().size() + " files for existing PAA version");
                    documentCommuneService.processAndSaveDocuments(request.getPaaFiles(), existingPaa, Constants.PATH_ENTREPOT_GED_PAA);
                    existingPaa = repository.save(existingPaa);
                    System.out.println("Saved PAA after processing files: " + existingPaa.getId() + " version " + existingPaa.getVersion());
                    if (!request.getPaaFiles().isEmpty()) {
                        System.out.println("Files were uploaded - forcing creation of new version");
                        shouldCreateNewVersion = true;
                        storePreviousRequestHash(paaId, paaVer, requestHash);
                    }
                } catch (IOException e) {
                    System.err.println("Error processing PAA files for existing version: " + e.getMessage());
                    throw new RuntimeException("Error processing PAA files for existing version: " + e.getMessage(), e);
                }
            }

            if (!shouldCreateNewVersion) {
                Paa copyForResponse = new Paa();
                BeanUtils.copyProperties(existingPaa, copyForResponse);

                if (existingPaa.getDocs() != null) {
                    copyForResponse.setDocs(existingPaa.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                }

                if (copyForResponse.getRealisations() != null) {
                    copyForResponse.getRealisations().forEach(realisation -> {
                        if (realisation.getDocs() != null) {
                            realisation.setDocs(realisation.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                        }
                        if (realisation.getLots() != null) {
                            realisation.setLots(realisation.getLots().stream().filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted())).collect(Collectors.toList()));
                        }
                        if (realisation.getSeuilApplicable() != null && realisation.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
                            log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", realisation.getId());
                            realisation.setSeuilApplicable(BigDecimal.ZERO);
                        }
                    });
                }

                BigDecimal totalBudget = copyForResponse.getRealisations().stream()
                        .map(Realisation::getBudgetEstime)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                copyForResponse.setBudgetEstime(totalBudget);
                System.out.println("Updated existing PAA ID: " + copyForResponse.getId() +
                        " version: " + copyForResponse.getVersion() +
                        ", budgetEstime: " + totalBudget);

                return copyForResponse;
            }
        }

        // Create a new PAA version
        System.out.println("Changes detected, creating new PAA version: " + (paaVer + 1));
        Paa newPaa = new Paa();
        newPaa.setId(paaId);
        newPaa.setVersion(paaVer + 1);

        newPaa.setRef(tempPaa.getRef());
        newPaa.setNom(tempPaa.getNom());
        newPaa.setAnnee(tempPaa.getAnnee());
        newPaa.setProcessInstanceId(tempPaa.getProcessInstanceId());
        newPaa.setStatus(tempPaa.getStatus());
        newPaa.setAc(tempPaa.getAc());
        newPaa.setModePassation(tempPaa.getModePassation());
        newPaa.setRemarque(tempPaa.getRemarque());

        newPaa.setRealisations(new ArrayList<>());
        newPaa.setDocs(new ArrayList<>());

        newPaa = repository.save(newPaa);
        System.out.println("Saved new PAA: " + newPaa.getId() + " version " + newPaa.getVersion());

        // Set up relationships
        if (!updatedRealisations.isEmpty()) {
            System.out.println("Setting up relationships with " + updatedRealisations.size() + " realisations");
            List<Realisation> confirmedRealisations = new ArrayList<>();
            final Paa finalNewPaa = newPaa;

            for (Realisation realisation : updatedRealisations) {
                try {
                    Realisation confirmedRealisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion())).orElse(null);
                    if (confirmedRealisation == null) {
                        System.err.println("Warning: Realisation " + realisation.getId() +
                                " version " + realisation.getVersion() +
                                " not found in database. Skipping relationship setup.");
                        continue;
                    }

                    boolean alreadyAssociated = false;
                    if (confirmedRealisation.getPaas() != null) {
                        alreadyAssociated = confirmedRealisation.getPaas().stream().anyMatch(p -> p.getId().equals(finalNewPaa.getId()) && p.getVersion().equals(finalNewPaa.getVersion()));
                    }

                    if (alreadyAssociated) {
                        System.out.println("Realisation " + confirmedRealisation.getId() +
                                " version " + confirmedRealisation.getVersion() +
                                " is already associated with PAA " + finalNewPaa.getId() +
                                " version " + finalNewPaa.getVersion() + ". Skipping association.");
                        confirmedRealisations.add(confirmedRealisation);
                        continue;
                    }

                    List<Paa> paaList = confirmedRealisation.getPaas();
                    if (paaList == null) {
                        paaList = new ArrayList<>();
                        confirmedRealisation.setPaas(paaList);
                    }

                    if (!paaList.contains(finalNewPaa)) {
                        paaList.add(finalNewPaa);
                    }

                    confirmedRealisation = realisationRepository.save(confirmedRealisation);
                    System.out.println("Successfully associated Realisation " + confirmedRealisation.getId() +
                            " version " + confirmedRealisation.getVersion() +
                            " with new PAA");
                    confirmedRealisations.add(confirmedRealisation);
                } catch (Exception e) {
                    System.err.println("Error associating Realisation with PAA: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            newPaa.setRealisations(confirmedRealisations);
            System.out.println("Set " + confirmedRealisations.size() + " realisations on the new PAA");
            BigDecimal totalBudget = newPaa.getRealisations().stream()
                    .map(Realisation::getBudgetEstime)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            newPaa.setBudgetEstime(totalBudget);
            System.out.println("Updated PAA ID: " + newPaa.getId() +
                    " version: " + newPaa.getVersion() +
                    ", budgetEstime: " + totalBudget);

            // Validate realisations before saving
            validateRealisations(newPaa);

            newPaa = repository.save(newPaa);
        } else if (existingPaa.getRealisations() != null) {
            System.out.println("Copying " + existingPaa.getRealisations().size() + " realisations from existing PAA");
            List<Realisation> confirmedRealisations = new ArrayList<>();
            final Paa finalNewPaa = newPaa;

            for (Realisation existingRealisation : existingPaa.getRealisations()) {
                try {
                    Integer latestVersion = realisationRepository.findMaxVersionById(existingRealisation.getId());
                    if (latestVersion == null) {
                        System.err.println("Warning: No versions found for Realisation ID " + existingRealisation.getId());
                        continue;
                    }

                    Realisation latestRealisation = realisationRepository.findById(new CompositeId(existingRealisation.getId(), latestVersion)).orElse(null);
                    if (latestRealisation == null) {
                        System.err.println("Warning: Latest Realisation " + existingRealisation.getId() +
                                " version " + latestVersion + " not found in database. Skipping.");
                        continue;
                    }

                    // === ADDED: FX conversion ONLY from Monnaie.taux for seuil on copied realisation
                    try {
                        BigDecimal rbFx = (latestRealisation.getFinancements() == null ? BigDecimal.ZERO :
                                latestRealisation.getFinancements().stream()
                                        .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                                        .map(f -> {
                                            BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                            Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0;
                                            return amt.multiply(BigDecimal.valueOf(taux));
                                        })
                                        .reduce(BigDecimal.ZERO, BigDecimal::add));
                        latestRealisation.setBudgetEstime(rbFx);
                        System.out.println("[SEUIL FX] Copied Realisation " + latestRealisation.getId() + " converted budget (Σ amt×taux) for seuil: " + rbFx);
                    } catch (Exception e) {
                        System.err.println("[SEUIL FX] Failed to compute converted budget for copied Realisation " + latestRealisation.getId() + ": " + e.getMessage());
                    }
                    // Seuil using converted amount
                    setSeuilApplicableForRealisation(latestRealisation, requestAcId);

                    boolean alreadyAssociated = false;
                    if (latestRealisation.getPaas() != null) {
                        alreadyAssociated = latestRealisation.getPaas().stream().anyMatch(p -> p.getId().equals(finalNewPaa.getId()) && p.getVersion().equals(finalNewPaa.getVersion()));
                    }

                    if (alreadyAssociated) {
                        System.out.println("Realisation " + latestRealisation.getId() +
                                " version " + latestRealisation.getVersion() +
                                " is already associated with PAA " + finalNewPaa.getId() +
                                " version " + finalNewPaa.getVersion() + ". Skipping association.");
                        confirmedRealisations.add(latestRealisation);
                        continue;
                    }

                    List<Paa> paaList = latestRealisation.getPaas();
                    if (paaList == null) {
                        paaList = new ArrayList<>();
                        latestRealisation.setPaas(paaList);
                    }

                    if (!paaList.contains(finalNewPaa)) {
                        paaList.add(finalNewPaa);
                    }

                    System.out.println("Set existing Realisation ID: " + latestRealisation.getId() +
                            " version: " + latestRealisation.getVersion() +
                            ", budgetEstime: " + latestRealisation.getBudgetEstime());
                    latestRealisation = realisationRepository.save(latestRealisation);
                    System.out.println("Successfully associated latest Realisation " + latestRealisation.getId() +
                            " version " + latestRealisation.getVersion() +
                            " with new PAA");
                    confirmedRealisations.add(latestRealisation);
                } catch (Exception e) {
                    System.err.println("Error handling Realisation relationship: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            newPaa.setRealisations(confirmedRealisations);
            System.out.println("Updated new PAA with " + confirmedRealisations.size() + " confirmed realisations");
            BigDecimal totalBudget = newPaa.getRealisations().stream()
                    .map(Realisation::getBudgetEstime)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            newPaa.setBudgetEstime(totalBudget);

            // Validate realisations before saving
            validateRealisations(newPaa);

            newPaa = repository.save(newPaa);
            System.out.println("Updated PAA ID: " + newPaa.getId() +
                    " version: " + newPaa.getVersion() +
                    ", budgetEstime: " + totalBudget);
        }

        // Handle documents
        if (!updatedDocs.isEmpty()) {
            List<Docs> confirmedDocs = new ArrayList<>();
            final Paa finalNewPaaForDocs = newPaa;

            for (Docs doc : updatedDocs) {
                try {
                    Docs confirmedDoc = docsRepository.findById(doc.getId()).orElse(null);
                    if (confirmedDoc == null) {
                        System.err.println("Warning: Document " + doc.getId() + " not found in database. Skipping.");
                        continue;
                    }

                    List<Paa> paaList = confirmedDoc.getPaas();
                    if (paaList == null) {
                        paaList = new ArrayList<>();
                        confirmedDoc.setPaas(paaList);
                    }

                    boolean alreadyAssociated = paaList.stream().anyMatch(p -> p.getId().equals(finalNewPaaForDocs.getId()) && p.getVersion().equals(finalNewPaaForDocs.getVersion()));
                    if (!alreadyAssociated) {
                        paaList.add(finalNewPaaForDocs);
                    }

                    confirmedDoc = docsRepository.save(confirmedDoc);
                    confirmedDocs.add(confirmedDoc);
                } catch (Exception e) {
                    System.err.println("Error associating Document with PAA: " + e.getMessage());
                }
            }

            newPaa.setDocs(confirmedDocs);
            System.out.println("Copied " + confirmedDocs.size() + " documents to new PAA version with correct delete status");
        } else if (existingPaa.getDocs() != null && !existingPaa.getDocs().isEmpty()) {
            List<Docs> confirmedDocs = new ArrayList<>();
            final Paa finalNewPaaForExistingDocs = newPaa;

            for (Docs existingDoc : existingPaa.getDocs()) {
                try {
                    Docs confirmedDoc = docsRepository.findById(existingDoc.getId()).orElse(null);
                    if (confirmedDoc == null) {
                        System.err.println("Warning: Document " + existingDoc.getId() + " not found in database. Skipping.");
                        continue;
                    }

                    List<Paa> paaList = confirmedDoc.getPaas();
                    if (paaList == null) {
                        paaList = new ArrayList<>();
                        confirmedDoc.setPaas(paaList);
                    }

                    boolean alreadyAssociated = paaList.stream().anyMatch(p -> p.getId().equals(finalNewPaaForExistingDocs.getId()) && p.getVersion().equals(finalNewPaaForExistingDocs.getVersion()));
                    if (!alreadyAssociated) {
                        paaList.add(finalNewPaaForExistingDocs);
                    }

                    confirmedDoc = docsRepository.save(confirmedDoc);
                    confirmedDocs.add(confirmedDoc);
                } catch (Exception e) {
                    System.err.println("Error associating existing Document with PAA: " + e.getMessage());
                }
            }

            newPaa.setDocs(confirmedDocs);
            System.out.println("Copied " + confirmedDocs.size() + " existing documents to new PAA version");
        }

        // Final save after setting up all relationships
        newPaa = repository.save(newPaa);
        System.out.println("Final save of PAA: " + newPaa.getId() + " version " + newPaa.getVersion());

        // Clear existing relationships to prevent duplicates
        try {
            final Paa finalNewPaa = newPaa;
            List<Integer> realisationIds = finalNewPaa.getRealisations().stream().map(Realisation::getId).collect(Collectors.toList());

            if (!realisationIds.isEmpty()) {
                System.out.println("Refreshing PAA-Realisation relationships to prevent primary key conflicts");
                for (Realisation realisation : finalNewPaa.getRealisations()) {
                    if (realisation.getPaas() != null) {
                        List<Paa> filteredPaas = realisation.getPaas().stream()
                                .filter(p -> !(p.getId().equals(finalNewPaa.getId()) && !p.getVersion().equals(finalNewPaa.getVersion())))
                                .collect(Collectors.toList());
                        realisation.setPaas(filteredPaas);
                        realisationRepository.save(realisation);
                        if (!realisation.getPaas().contains(finalNewPaa)) {
                            realisation.getPaas().add(finalNewPaa);
                            realisationRepository.save(realisation);
                        }
                    }
                }

                List<Realisation> tempRealisations = new ArrayList<>(finalNewPaa.getRealisations());
                finalNewPaa.getRealisations().clear();
                newPaa = repository.save(finalNewPaa);
                final Paa updatedNewPaa = newPaa;

                for (Realisation realisation : tempRealisations) {
                    updatedNewPaa.getRealisations().add(realisation);
                }
                newPaa = repository.save(updatedNewPaa);
                System.out.println("Successfully refreshed PAA-Realisation relationships");
            }
        } catch (Exception e) {
            System.err.println("Warning: Error handling relationship refresh: " + e.getMessage());
            e.printStackTrace();
        }

        // Handle document IDs for realizations
        if (request.getRealisations() != null) {
            for (RealisationRequestForPaa realisationRequest : request.getRealisations()) {
                if (realisationRequest.getDocIds() != null) {
                    Realisation realisation = newPaa.getRealisations().stream()
                            .filter(r -> r.getId().equals(realisationRequest.getId()))
                            .findFirst().orElse(null);

                    if (realisation != null) {
                        Set<Integer> existingDocIds = realisation.getDocs() != null
                                ? realisation.getDocs().stream().map(Docs::getId).collect(Collectors.toSet())
                                : new HashSet<>();
                        Set<Integer> requestedDocIds = new HashSet<>(realisationRequest.getDocIds());
                        requestedDocIds.addAll(existingDocIds);

                        List<Docs> finalDocList = new ArrayList<>();
                        for (Integer docId : requestedDocIds) {
                            Docs doc = docsRepository.findById(docId).orElse(null);
                            if (doc != null) {
                                boolean bidirectionalHandledElsewhere = true;
                                if (!bidirectionalHandledElsewhere) {
                                    if (doc.getRealisations() == null) doc.setRealisations(new ArrayList<>());
                                    boolean found = doc.getRealisations().stream()
                                            .anyMatch(r -> r.getId().equals(realisation.getId()) && r.getVersion().equals(realisation.getVersion()));
                                    if (!found) {
                                        doc.getRealisations().add(realisation);
                                        System.out.println("[DEBUG] Added realisation " + realisation.getId() +
                                                " v" + realisation.getVersion() +
                                                " to doc " + doc.getId());
                                    }
                                }

                                doc.setIsDeleted(false);
                                doc = docsRepository.save(doc);
                                finalDocList.add(doc);
                            }
                        }

                        realisation.setDocs(finalDocList);
                        realisationRepository.save(realisation);
                        System.out.println("[DEBUG-FINAL] Realisation " + realisation.getId() +
                                " final docs: " + finalDocList.stream().map(Docs::getId).toList());
                    }
                }
            }
        }

        // Handle PAA files if provided
        if (request.getPaaFiles() != null) {
            try {
                System.out.println("Processing " + request.getPaaFiles().size() + " files for new PAA version");
                documentCommuneService.processAndSaveDocuments(request.getPaaFiles(), newPaa, Constants.PATH_ENTREPOT_GED_PAA);
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

        if (copyForReturn.getRealisations() != null) {
            copyForReturn.getRealisations().forEach(realisation -> {
                if (realisation.getDocs() != null) {
                    realisation.setDocs(realisation.getDocs().stream().filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted())).collect(Collectors.toList()));
                }
                if (realisation.getLots() != null) {
                    realisation.setLots(realisation.getLots().stream().filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted())).collect(Collectors.toList()));
                }
                if (realisation.getSeuilApplicable() != null && realisation.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
                    log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", realisation.getId());
                    realisation.setSeuilApplicable(BigDecimal.ZERO);
                }
            });
        }

        BigDecimal finalBudget = copyForReturn.getRealisations().stream()
                .map(Realisation::getBudgetEstime)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        copyForReturn.setBudgetEstime(finalBudget);
        System.out.println("Final response PAA ID: " + copyForReturn.getId() +
                " version: " + copyForReturn.getVersion() +
                ", budgetEstime: " + finalBudget);

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
                sb.append("text:").append(realisation.getBeneficiaire() != null ? realisation.getBeneficiaire() : "").append(",");


                // Include other Realisation fields
//                sb.append("service:").append(realisation.getService() != null && realisation.getService().getId() != null ? realisation.getService().getId() : "").append(",");
//                sb.append("direction:").append(realisation.getDirection() != null && realisation.getDirection().getId() != null ? realisation.getDirection().getId() : "").append(",");
//                sb.append("sourceFin:").append(realisation.getSourceFinancement() != null ? realisation.getSourceFinancement() : "").append(",");
                sb.append("imputationBudgetaire:").append(realisation.getImputationBudgetaire() != null && realisation.getImputationBudgetaire().getId() != null ? realisation.getImputationBudgetaire().getId() : "").append(",");
//                sb.append("monais:").append(realisation.getMonais() != null ? realisation.getMonais() : "").append(",");
//                sb.append("monnaie:").append(realisation.getMonnaie() != null ? realisation.getMonnaie() : "").append(",");
                //             sb.append("monnaie:").append(realisation.getMonnaie() != null && realisation.getMonnaie().getId() != null ? realisation.getMonnaie().getId() : "").append(",");
                sb.append("typeMarcher:").append(realisation.getTypeMarcher() != null ? realisation.getTypeMarcher() : "").append(",");
//                sb.append("modeSelection:").append(realisation.getModeSelection() != null ? realisation.getModeSelection() : "").append(",");
                sb.append("modePassation:").append(realisation.getModePassation() != null ? realisation.getModePassation() : "").append(",");

                sb.append("budgetEstime:").append(realisation.getBudgetEstime() != null ? realisation.getBudgetEstime() : "").append(",");

                if (realisation.getFinancements() != null && !realisation.getFinancements().isEmpty()) {
                    sb.append("financements:[");
                    for (FinancementRequest financement : realisation.getFinancements()) {
                        sb.append("monnaieId:").append(financement.getMonnaieId() != null ? financement.getMonnaieId() : "").append(",");
                        sb.append("sourceFinId:").append(financement.getSourceFinancement() != null && financement.getSourceFinancement().getId() != null ? financement.getSourceFinancement().getId() : "").append(",");
                        sb.append("budgetEstimer:").append(financement.getBudgetEstimer() != null ? financement.getBudgetEstimer() : "").append("},");
                    }
                    sb.append("]");
                }
                sb.append("],");


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
        log.info("a ce niveau ca va 1 ");
        paa.setStatus(ProcessStatus.BROUILLON);
        log.info("a ce niveau ca va 2 ");
        List<UserAc> userAcs = currentUserService.getCurrentUserAc();
        if (userAcs == null || userAcs.isEmpty()) {
            throw new AcNotFound("No AC IDs found for current user", List.of());
        }

        Integer requestAcId = request.getAcId();
        if (requestAcId != null) {
            boolean hasAcId = userAcs.stream()
                    .anyMatch(userAc -> userAc.getAcId().equals(requestAcId));
            if (!hasAcId) {
                throw new AcNotFound("User does not have permission for AC ID: ", List.of(requestAcId));
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
        log.info("a ce niveau ca va 3  ");
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
                documentCommuneService.processAndSaveDocuments(request.getPaaFiles(), savedPaa, Constants.PATH_ENTREPOT_GED_PAA);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Process realisations if provided
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            // Get the current max ID for Realisation once before the loop
            // This will be our starting point for generating new IDs
//            Integer currentMaxRealisationId = realisationRepository.findMaxId();
            Integer currentMaxRealisationId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
            ;
            // Initialize next ID to use. If no realisations exist, start from 1.
            // Otherwise, start from maxId + 1.
            Integer nextRealisationId = (currentMaxRealisationId != null) ? currentMaxRealisationId + 1 : 1;


            List<Realisation> realisationsToSave = new ArrayList<>();
            log.info("a ce niveau ca va 4  ");
            for (RealisationRequestForPaa realisationRequest : request.getRealisations()) {
                Realisation realisation = new Realisation();

                // Assign the next available ID and then increment it for the next iteration
                realisation.setId(nextRealisationId++); // Assign and then increment
                realisation.setVersion(1);
                realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                realisation.setBeneficiaire(realisationRequest.getBeneficiaire());
//                realisation.setService(realisationRequest.getService());
//                realisation.setDirection(realisationRequest.getDirection());
//                realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
//                realisation.setMonais(realisationRequest.getMonais());
//                realisation.setMonnaie(realisationRequest.getMonnaie());
                realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
//                realisation.setModeSelection(realisationRequest.getModeSelection());
                realisation.setModePassation(realisationRequest.getModePassation());
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

                log.info("a ce niveau ca va  5");
                if (realisation.getPaas() == null) {
                    realisation.setPaas(new ArrayList<>());
                }
                realisation.getPaas().add(savedPaa);
                savedPaa.getRealisations().add(realisation);
                if (realisationRequest.getRealisationFiles() != null) {
                    try {
                        // processAndSaveRelisationsDocuments(realisationRequest.getRealisationFiles(), realisation);
                        documentCommuneService.processAndSaveDocuments(realisationRequest.getRealisationFiles(), realisation, Constants.PATH_ENTREPOT_GED_PAA + "/" + paa.getId() + Constants.PATH_ENTREPOT_GED_REALISATION);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                log.info("a ce niveau ca va 6 ");
                // Handle Lots
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    List<Lot> lots = new ArrayList<>();
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
                log.info("a ce niveau ca va 7 ");
                realisationsToSave.add(realisation);
            }
            log.info("a ce niveau ca va 8  ");
            realisationRepository.saveAll(realisationsToSave);
            log.info("a ce niveau ca va 9  ");
        }
        log.info("a ce niveau ca va 10 ");
        BigDecimal budgetEstime=savedPaa.getRealisations().stream().map(Realisation::getBudgetEstime).reduce(BigDecimal.ZERO, BigDecimal::add);
        savedPaa.setBudgetEstime(budgetEstime);
        return repository.save(savedPaa);
    }


    private boolean realisationNeedsUpdate(Realisation existingRealisation, RealisationRequestForPaa dto) {
        // Check if any field is different
        if (dto.getRealisationEnvisagees() != null && !dto.getRealisationEnvisagees().equals(existingRealisation.getRealisationEnvisagees())) {
            return true;
        }

        // Check if any field is different
        if (dto.getBeneficiaire() != null && !dto.getBeneficiaire().equals(existingRealisation.getBeneficiaire())) {
            return true;
        }

        if (dto.getBudgetEstime() != null && !dto.getBudgetEstime().equals(existingRealisation.getBudgetEstime())) {
            return true;
        }

        // Check imputationBudgetaire
        if (dto.getImputationBudgetaire() != null && (existingRealisation.getImputationBudgetaire() == null || !dto.getImputationBudgetaire().getId().equals(existingRealisation.getImputationBudgetaire().getId()))) {
            return true;
        }

//        // Check service
//        if (dto.getService() != null && (existingRealisation.getService() == null || !dto.getService().getId().equals(existingRealisation.getService().getId()))) {
//            return true;
//        }



        // Check modePassation
        if (dto.getModePassation() != null && (existingRealisation.getModePassation() == null || !dto.getModePassation().getId().equals(existingRealisation.getModePassation().getId()))) {
            return true;
        }

//        // Check direction
//        if (dto.getDirection() != null && (existingRealisation.getDirection() == null || !dto.getDirection().getId().equals(existingRealisation.getDirection().getId()))) {
//            return true;
//        }

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


    public Paa findByIdPaa(Integer paaId, Integer paaVer) throws PaaNotFound {
        Integer latestVersion = findLatestVersionById(paaId).orElseThrow(() -> new PaaNotFound("No PAA found for ID", List.of(paaId), List.of(paaVer)));
        Paa paa = repository.findById(new CompositeId(paaId, latestVersion)).orElseThrow(() -> new PaaNotFound("No PAA found for ID and version", List.of(paaId), List.of(latestVersion)));


        return paa;
    }

    private Optional<Integer> findLatestVersionById(Integer paaId) {
        return Optional.ofNullable(repository.findMaxVersionById(paaId));
    }


    @Transactional
    public Paa updateStatus(Integer paaId, Integer paaVer, ProcessStatus status) throws PaaNotFound {
        CompositeId compositeId = new CompositeId(paaId, paaVer);
        Paa paa = repository.findById(compositeId).orElseThrow(() -> new PaaNotFound("Paa not found", List.of(paaId), List.of(paaVer)));
        log.info("status name " + status);
        paa.setStatus(status);
        log.info("status name 2" + paa.getStatus());
        paa.setUpdatedAt(Calendar.getInstance());
        return repository.save(paa);
    }

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
    @Override
    public Page<Paa> findCurrentYearPaasByUser(Pageable pageable) {
        log.info("Retrieving current year's PAAs for connected user with pageable: {}", pageable);

        // Get user's AC IDs
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            log.info("No AC IDs found for the connected user");
            return Page.empty(pageable);
        }

        // Validate ACs
        for (Integer acId : userAcIds) {
            try {
                acService.findById(acId);
            } catch (Exception e) {
                log.info("AC not found with ID: {}", acId);
                return Page.empty(pageable);
            }
        }

        // Get current year
        String currentYear = String.valueOf(LocalDate.now().getYear());

        // Build specification
        Specification<Paa> spec = Specification.where(PaaSpecifications.annee(currentYear))
                .and(PaaSpecifications.paaAc(userAcIds))
                .and(PaaSpecifications.deleted(false))
                .and(PaaSpecifications.isLatestVersion());

        // Query PAAs with pagination
        Page<Paa> paasPage = repository.findAll(spec, pageable);

        if (paasPage.isEmpty()) {
            log.info("No PAAs found for the current year {} and user ACs {}", currentYear, userAcIds);
            return Page.empty(pageable);
        }

        log.info("Retrieved {} PAAs for the current year {} and user ACs {}", paasPage.getContent().size(), currentYear, userAcIds);
        return paasPage;
    }

    public List<Ac> getAcsWithoutPaaForCurrentYear() throws AcNotFound {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        log.info("Checking ACs without PAA for current year: {}", currentYear);

        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            log.warn("No AC IDs found for the connected user");
            throw new AcNotFound("No AC IDs found for the connected user", Collections.emptyList());
        }
        log.info("User AC IDs: {}", userAcIds);

        for (Integer acId : userAcIds) {
            try {
                acService.findById(acId);
            } catch (AcNotFound e) {
                log.warn("AC not found with ID: {}", acId);
                throw e;
            }
        }

        Specification<Paa> spec = (root, query, criteriaBuilder) -> {
            Predicate yearPredicate = criteriaBuilder.equal(root.get("annee"), currentYear);
            Predicate notDeletedPredicate = criteriaBuilder.isFalse(root.get("isDeleted"));
            return criteriaBuilder.and(yearPredicate, notDeletedPredicate);
        };
        List<Paa> paas = repository.findAll(spec);
        log.info("Found {} PAAs for year {} (non-deleted)", paas.size(), currentYear);

        Set<Integer> acIdsWithPaa = paas.stream()
                .filter(paa -> paa.getAc() != null)
                .map(paa -> paa.getAc().getId())
                .collect(Collectors.toSet());
        log.info("ACs with non-deleted PAA IDs: {}", acIdsWithPaa);

        List<Integer> acIdsWithoutPaa = userAcIds.stream()
                .filter(acId -> !acIdsWithPaa.contains(acId))
                .collect(Collectors.toList());
        log.info("ACs without non-deleted PAA IDs: {}", acIdsWithoutPaa);

        if (acIdsWithoutPaa.isEmpty()) {
            log.info("All ACs for user have non-deleted PAAs for year {}", currentYear);
            throw new AllAcsHavePaaException("Vous ne pouvez pas actuellement créer un nouveau PAA ! Toutes les AC dont vous êtes membre ont des PAA non supprimés en cours pour l'année " + currentYear + ".");
        }

        List<Ac> acsWithoutPaa = acRepository.findAllById(acIdsWithoutPaa);
        log.info("Found {} ACs without non-deleted PAA for user", acsWithoutPaa.size());
        return acsWithoutPaa;
    }
    @Transactional
    @Override
    public Paa createPaaPerAc(PaaRequestGlobal request) throws AcNotFound, PaaCreationLimitExceededException, IOException {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        log.info("Creating PAA for year: {}", currentYear);

        Integer requestAcId = request.getAcId();
        if (requestAcId == null) {
            throw new IllegalArgumentException("AC ID is required to create a PAA.");
        }

        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            log.warn("No AC IDs found for the connected user");
            throw new AcNotFound("No AC IDs found for the connected user", Collections.emptyList());
        }
        if (!userAcIds.contains(requestAcId)) {
            log.warn("User does not have permission for AC ID: {}", requestAcId);
            throw new AcNotFound("User does not have permission for AC ID: " + requestAcId, List.of(requestAcId));
        }

        Ac ac = acService.findById(requestAcId);

        Specification<Paa> spec = PaaSpecifications.annee(currentYear)
                .and(PaaSpecifications.paaAc(requestAcId))
                .and(PaaSpecifications.deleted(false));
        List<Paa> existingPaas = repository.findAll(spec);
        if (!existingPaas.isEmpty()) {
            Paa existingPaa = existingPaas.get(0);
            throw new PaaCreationLimitExceededException(
                    "A PAA already exists for AC ID " + requestAcId + " for year " + currentYear + ".",
                    existingPaa.getId(),
                    existingPaa.getVersion(),
                    1
            );
        }

        Integer paaId = sequenceGeneratorService.getNextId(Constants.PAA_ID_SEQ);
        Integer paaVersion = 1;

        Paa paa = new Paa();
        paa.setId(paaId);
        paa.setVersion(paaVersion);
        paa.setNom(request.getNom());
        paa.setModePassation(request.getModePassation());
        paa.setRemarque(request.getRemarque());
        paa.setAnnee(currentYear);
        setRef(paa);
        paa.setBudgetEstime(request.getBudgetEstime());
        paa.setStatus(ProcessStatus.BROUILLON);
        paa.setAc(ac);

        paa.setRealisations(new ArrayList<>());
        paa.setDocs(new ArrayList<>());
        log.info("Annee test check passed: {}", paa.getAnnee());

        Paa savedPaa = repository.save(paa);

        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = new ArrayList<>();
            for (Integer docId : request.getDocIds()) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                if (doc.getPaas() == null) {
                    doc.setPaas(new ArrayList<>());
                }
                doc.getPaas().add(savedPaa);
                docs.add(doc);
                docsRepository.save(doc);
            }
            savedPaa.setDocs(docs);
        }

        if (request.getPaaFiles() != null && !request.getPaaFiles().isEmpty()) {
            documentCommuneService.processAndSaveDocuments(
                    request.getPaaFiles(),
                    savedPaa,
                    Constants.PATH_ENTREPOT_GED_PAA
            );
        }

        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            Integer currentMaxRealisationId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
            Integer nextRealisationId = (currentMaxRealisationId != null) ? currentMaxRealisationId : 1;
            Integer currentMaxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
            Integer nextLotId = (currentMaxLotId != null) ? currentMaxLotId + 1 : 1;
            log.info("Starting lot ID sequence at: {}", nextLotId);

            Integer currentMaxFinancementId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);
            Integer nextFinancementId = (currentMaxFinancementId != null) ? currentMaxFinancementId + 1 : 1;
            log.info("Starting financement ID sequence at: {}", nextFinancementId);

            List<Realisation> realisationsToSave = new ArrayList<>();
            for (RealisationRequestForPaa realisationRequest : request.getRealisations()) {
                Realisation realisation = new Realisation();
                realisation.setId(nextRealisationId++);
                realisation.setVersion(1);
                realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                realisation.setBeneficiaire(realisationRequest.getBeneficiaire());
                realisation.setImputationBudgetaire(realisationRequest.getImputationBudgetaire());
                realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                realisation.setModePassation(realisationRequest.getModePassation());
                realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                realisation.setIsPublished(realisationRequest.getIsPublished());
                realisation.setPublishedAt(realisationRequest.getPublishedAt());
                realisation.setSort(realisationRequest.getSort());

                if (realisationRequest.getRealisationFiles() != null && !realisationRequest.getRealisationFiles().isEmpty()) {
                    documentCommuneService.processAndSaveDocuments(
                            realisationRequest.getRealisationFiles(),
                            realisation,
                            Constants.PATH_ENTREPOT_GED_PAA + "/" + paa.getId() + Constants.PATH_ENTREPOT_GED_REALISATION
                    );
                }

                if (realisation.getPaas() == null) {
                    realisation.setPaas(new ArrayList<>());
                }
                realisation.getPaas().add(savedPaa);
                savedPaa.getRealisations().add(realisation);

                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    log.info("Processing {} lots for realisation ID: {}", realisationRequest.getLots().size(), realisation.getId());
                    List<Lot> lots = new ArrayList<>();
                    for (LotResponse lotRequest : realisationRequest.getLots()) {
                        log.info("Processing lot with description: {}", lotRequest.getDescription());
                        if (lotRequest.getDescription() == null || lotRequest.getDescription().isEmpty()) {
                            log.warn("Skipping lot with null or empty description for realisation ID: {}", realisation.getId());
                            continue;
                        }
                        Lot lot = new Lot();
                        Integer lotId = lotRequest.getId();
                        if (lotId == null) {
                            lotId = nextLotId++;
                            log.info("Assigned new lot ID: {}", lotId);
                        }
                        CompositeId compositeId = new CompositeId(lotId, 1);
                        if (lotRepository.existsById(compositeId)) {
                            log.error("Lot ID {} already exists in the database", lotId);
                            throw new IllegalStateException("Generated lot ID " + lotId + " already exists");
                        }
                        lot.setId(lotId);
                        lot.setVersion(1);
                        lot.setDescription(lotRequest.getDescription());
                        lot.setBudget(lotRequest.getBudget());
                        try {
                            lot = lotRepository.save(lot);
                            log.info("Saved lot with ID: {}, Description: {}", lot.getId(), lot.getDescription());
                            lots.add(lot);
                        } catch (Exception e) {
                            log.error("Failed to save lot with ID: {}, Description: {}. Error: {}", lotId, lotRequest.getDescription(), e.getMessage());
                            throw new RuntimeException("Failed to save lot: " + e.getMessage(), e);
                        }
                    }
                    realisation.setLots(lots);
                    log.info("Assigned {} lots to realisation ID: {}", lots.size(), realisation.getId());
                } else {
                    log.info("No lots provided for realisation ID: {}", realisation.getId());
                    realisation.setLots(new ArrayList<>());
                }

                // Process Financements
                if (realisationRequest.getFinancements() != null && !realisationRequest.getFinancements().isEmpty()) {
                    log.info("Processing {} financements for realisation ID: {}", realisationRequest.getFinancements().size(), realisation.getId());
                    List<Financement> financements = new ArrayList<>();

                    for (FinancementRequest financementRequest : realisationRequest.getFinancements()) {
                        log.info("Processing financement with monnaieId: {}", financementRequest.getMonnaieId());

                        if (financementRequest.getMonnaieId() == null) {
                            log.warn("Skipping financement with null monnaieId for realisation ID: {}", realisation.getId());
                            continue;
                        }
                        Financement financement = new Financement();
                        FinancementId financementId = new FinancementId();
                        financementId.setRealisation(realisation);

                        Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
                                .orElseThrow(() -> new EntityNotFoundException("Monnaie not found with ID: " + financementRequest.getMonnaieId()));
                        financement.setMonnaie(monnaie);
                        if (monnaie.getTaux() != null) {
                            financement.setTauxApplicable(monnaie.getTaux());
                            System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux() + " from Monnaie ID: " + monnaie.getId());
                        } else {
                            financement.setTauxApplicable(0.0);
                            System.out.println("Monnaie taux is null, set tauxApplicable to 0.0 for Monnaie ID: " + monnaie.getId());
                        }

                        financementId.setFinancementId(nextFinancementId++);
                        financement.setId(financementId);
                        financement.setBudgetEstimer(financementRequest.getBudgetEstimer());

                        if (financementRequest.getSourceFinancement() != null && financementRequest.getSourceFinancement().getId() != null) {
                            RefParam sourceFinancement = refParamRepository.findById(financementRequest.getSourceFinancement().getId())
                                    .orElseThrow(() -> new EntityNotFoundException("Source Financement not found with ID: " + financementRequest.getSourceFinancement().getId()));
                            financement.setSourceFinancement(sourceFinancement);
                        }

                        financements.add(financement);
                        log.info("Created financement with ID: {}, MonnaieId: {}, Budget: {}",
                                financementId.getFinancementId(),
                                financementRequest.getMonnaieId(),
                                financementRequest.getBudgetEstimer());
                    }

                    realisation.setFinancements(financements);
                    log.info("Assigned {} financements to realisation ID: {}", financements.size(), realisation.getId());

                    // === Calculate realisation budget as Σ(budgetEstimer × Monnaie.taux) ===
                    BigDecimal realisationBudget = financements.stream()
                            .map(f -> {
                                BigDecimal budget = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0;
                                return budget.multiply(BigDecimal.valueOf(taux));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    realisation.setBudgetEstime(realisationBudget);
                    log.info("Set realisation ID {} budgetEstime to {} (Σ budgetEstimer × Monnaie.taux)", realisation.getId(), realisationBudget);
                } else {
                    log.info("No financements provided for realisation ID: {}", realisation.getId());
                    realisation.setFinancements(new ArrayList<>());
                    realisation.setBudgetEstime(BigDecimal.ZERO);
                    log.info("Set realisation ID {} budgetEstime to 0 due to no financements", realisation.getId());
                }

                // Set seuilApplicable and validate budget (uses already converted budget)
                setSeuilApplicableForRealisation(realisation, requestAcId);

                realisationsToSave.add(realisation);
            }
            realisationRepository.saveAll(realisationsToSave);
        }

        validateRealisations(savedPaa);

        BigDecimal budgetEstime = savedPaa.getRealisations().stream()
                .map(realisation -> realisation.getBudgetEstime() != null ? realisation.getBudgetEstime() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        savedPaa.setBudgetEstime(budgetEstime);
        log.info("Set PAA ID {} budgetEstime to {} based on sum of realisation budgets", savedPaa.getId(), budgetEstime);

        for (Realisation realisation : savedPaa.getRealisations()) {
            if (realisation.getSeuilApplicable() != null && realisation.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
                log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", realisation.getId());
                realisation.setSeuilApplicable(BigDecimal.ZERO);
            }
        }

        return repository.save(savedPaa);
    }



    @Override
    public Page<Paa> findAllForWebsite(Specification<Paa> spec, Pageable pageable) {
        Specification<Paa> finalSpec = Specification.where(spec)
                .and(PaaSpecifications.isLatestVersion())
                .and(PaaSpecifications.isPublished())
                .and(PaaSpecifications.isNotDeleted());
        return repository.findAll(finalSpec, pageable);
    }

    /**
     * Validates that all lots within a realisation have consistent budgets (all null or all non-null).
     */
    private void validateLotBudgetConsistency(Realisation realisation) {
        List<Lot> nonDeletedLots = realisation.getLots().stream()
                .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                .collect(Collectors.toList());

        if (!nonDeletedLots.isEmpty()) {
            boolean hasBudget = nonDeletedLots.get(0).getBudget() != null;
            boolean allConsistent = nonDeletedLots.stream()
                    .allMatch(lot -> (lot.getBudget() != null) == hasBudget);

            if (!allConsistent) {
                throw new LotBudgetConsistencyException(
                        "Inconsistent budgets in lots for Realisation ID " + realisation.getId() +
                                ": all lots must have either non-null or null budgets.");
            }
        }
    }

    /**
     * Validates that the sum of non-deleted lot budgets does not exceed the realisation's budgetEstime.
     */
    private void validateLotBudgetSum(Realisation realisation) {
        BigDecimal budgetEstime = realisation.getBudgetEstime();
        if (budgetEstime == null) {
            return; // No validation needed if budgetEstime is null
        }

        BigDecimal totalLotBudget = realisation.getLots().stream()
                .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()) && lot.getBudget() != null)
                .map(Lot::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalLotBudget.compareTo(budgetEstime) > 0) {
            throw new LotBudgetExceedsRealisationException(
                    "Sum of lot budgets (" + totalLotBudget + ") exceeds Realisation ID " + realisation.getId() +
                            " budgetEstime (" + budgetEstime + ").");
        }
    }

    /**
     * Validates all realisations associated with a PAA.
     */
    private void validateRealisations(Paa paa) {
        if (paa.getRealisations() != null) {
            for (Realisation realisation : paa.getRealisations()) {
                if (realisation.getLots() != null && !realisation.getLots().isEmpty()) {
                    validateLotBudgetConsistency(realisation);
                    validateLotBudgetSum(realisation);
                }
            }
        }
    }

    private void setSeuilApplicableForRealisation(Realisation realisation, Integer acId) {
        if (realisation.getTypeMarcher() != null && realisation.getModePassation() != null) {
            try {
                log.info("************** ac id : {} type marche id : {} mode de passation : {}", acId,  realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());
                Specification<AcSeuil> acSeuilSpec = AcSeuilSpecifications.acId(acId)
                        .and(AcSeuilSpecifications.typeMarcherId(realisation.getTypeMarcher().getId()))
                        .and(AcSeuilSpecifications.modePassationId(realisation.getModePassation().getId()));
                Optional<AcSeuil> acSeuilOpt = acSeuilService.findOne(acSeuilSpec);
                if (acSeuilOpt.isPresent()) {
                    BigDecimal acSeuil = acSeuilOpt.get().getSeuil();
                    realisation.setSeuilApplicable(acSeuil);
                    log.info("Set seuilApplicable to {} for Realisation ID: {} with AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}",
                            realisation.getSeuilApplicable(), realisation.getId(), acId,
                            realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());

                    // Validate Realisation budget against AcSeuil
                    BigDecimal budgetEstime = realisation.getBudgetEstime() != null ? realisation.getBudgetEstime() : BigDecimal.ZERO;
                    if (budgetEstime.compareTo(acSeuil) > 0) {
                        log.error("Realisation ID {} budgetEstime {} exceeds Ac seuil {} for PAA",
                                realisation.getId(), budgetEstime, acSeuil);
                        throw new RealisationBudgetExceedsAcSeuilException(
                                String.format(
                                        "La réalisation %s a un budget estimé %s supérieur au seuil AC %s. Cette activité est compatible avec le PPM, mais pas avec le PAA.",
                                        realisation.getRealisationEnvisagees(), budgetEstime, acSeuil
                                ));
                    }
                } else {
                    realisation.setSeuilApplicable(BigDecimal.ZERO);
                    log.warn("No AcSeuil found for AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}. Setting seuilApplicable to 0.",
                            acId, realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());
                }
            } catch (RealisationBudgetExceedsAcSeuilException e) {
                log.error("Budget validation failed for Realisation ID: {}. Error: {}", realisation.getId(), e.getMessage());
                throw e; // Rethrow to stop processing
            }
        } else {
            realisation.setSeuilApplicable(BigDecimal.ZERO);
            log.warn("TypeMarcher or ModePassation is null for Realisation ID: {}. Setting seuilApplicable to 0.", realisation.getId());
        }
    }

//    private void setSeuilApplicableForRealisation(Realisation realisation, Integer acId) {
//        if (realisation.getTypeMarcher() != null && realisation.getModePassation() != null) {
//            try {
//                Specification<AcSeuil> acSeuilSpec = AcSeuilSpecifications.acId(acId)
//                        .and(AcSeuilSpecifications.typeMarcherId(realisation.getTypeMarcher().getId()))
//                        .and(AcSeuilSpecifications.modePassationId(realisation.getModePassation().getId()));
//                Optional<AcSeuil> acSeuilOpt = acSeuilService.findOne(acSeuilSpec);
//                if (acSeuilOpt.isPresent()) {
//                    realisation.setSeuilApplicable(acSeuilOpt.get().getSeuil());
//                    log.info("Set seuilApplicable to {} for Realisation ID: {} with AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}",
//                            realisation.getSeuilApplicable(), realisation.getId(), acId,
//                            realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());
//                } else {
//                    realisation.setSeuilApplicable(BigDecimal.ZERO);
//                    log.warn("No AcSeuil found for AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}. Setting seuilApplicable to 0.",
//                            acId, realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());
//                }
//            } catch (Exception e) {
//                realisation.setSeuilApplicable(BigDecimal.ZERO);
//                log.error("Failed to fetch AcSeuil for AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}. Setting seuilApplicable to 0. Error: {}",
//                        acId, realisation.getTypeMarcher().getId(), realisation.getModePassation().getId(), e.getMessage());
//            }
//        } else {
//            realisation.setSeuilApplicable(BigDecimal.ZERO);
//            log.warn("TypeMarcher or ModePassation is null for Realisation ID: {}. Setting seuilApplicable to 0.", realisation.getId());
//        }
//    }


    private Integer generateFinancementIdWithRetry() {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Integer nextVal = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);
                return (nextVal != null) ? nextVal + 1 : 1;
            } catch (Exception e) {
                if (attempt == 2) throw e; // Re-throw on final attempt
                try { Thread.sleep(100); } catch (InterruptedException ie) { /* ignore */ }
            }
        }
        return 1; // Fallback
    }


}
