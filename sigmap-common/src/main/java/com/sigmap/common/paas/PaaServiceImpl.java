package com.sigmap.common.paas;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.response.AcAvailablePaaResponse;
import com.sigmap.common.ac_seuil.AcSeuil;
import com.sigmap.common.ac_seuil.AcSeuilService;
import com.sigmap.common.ac_seuil.AcSeuilSpecifications;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.exceptions.AgpmInvalidStatusException;
import com.sigmap.common.agpm.exceptions.AgpmNotAssociatedWithUserAcException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
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
import com.sigmap.common.model_paa.ModelPaaRepository;
import com.sigmap.common.model_ppm.ModelPpmRepository;
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
import com.sigmap.common.seuil_general.SeuilGeneral;
import com.sigmap.common.seuil_general.SeuilGeneralService;
import com.sigmap.common.seuil_general.SeuilGeneralSpecifications;
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
    private final ModelPaaRepository modelPaaRepository;

    private final SeuilGeneralService seuilGeneralService;

    @Value("${alfresco.url}")
    private String alfrescoUrl;

    public PaaServiceImpl(PaaRepository repository, CurrentUserService currentUserService, EntityManager entityManager, RealisationRepository realisationRepository, RefParamRepository refParamRepository, DocsRepository docsRepository, AcRepository acRepository, LotRepository lotRepository, RealisationService realisationService, DocumentCommuneService documentCommuneService, AcService acService, AlfrescoService alfrescoService, SequenceGeneratorService sequenceGeneratorService,
                          AcSeuilService acSeuilService, MonnaieRepository monnaieRepository, FinancementRepository financementRepository, RefParamRepository refParamRepository1, ModelPaaRepository modelPaaRepository, SeuilGeneralService seuilGeneralService) {
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
        this.modelPaaRepository = modelPaaRepository;
        this.seuilGeneralService = seuilGeneralService;

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

        String modelAlfrescoId = modelPaaRepository
                .findModelAlfrescoId(paaId, paaVer)
                .orElse(null);
        if (modelAlfrescoId != null && !modelAlfrescoId.isBlank()) {
            paa.setModelAlfrescoId(modelAlfrescoId);
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
        filteredPaa.setModelAlfrescoId(paa.getModelAlfrescoId());
        filteredPaa.setModePassation(paa.getModePassation());
        filteredPaa.setRemarque(paa.getRemarque());
        filteredPaa.setProcessInstanceId(paa.getProcessInstanceId());
        filteredPaa.setStatus(paa.getStatus());
        filteredPaa.setActualise(paa.getActualise());
        filteredPaa.setNbrActualisation(paa.getNbrActualisation());
        filteredPaa.setEtapeProcess(paa.getEtapeProcess());
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

    if (existingPaa.getActualise() == null) {
        existingPaa.setActualise(Boolean.FALSE);
    }
    if (existingPaa.getNbrActualisation() == null) {
        existingPaa.setNbrActualisation(0);
    }

    // === Simple fields
    existingPaa.setNom(request.getNom());
    existingPaa.setAnnee(existingPaa.getAnnee());
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
                LocalDate lps = rReq.getDatePrevLps();
                realisation.setDatePrevLps(lps);
                realisation.setDatePrevAc(lps != null ? lps.plusDays(30) : null);
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

                // =========================== FINANCEMENTS (UPDATE EXISTING REALISATION) ===========================
                if (rReq.getFinancements() != null && !rReq.getFinancements().isEmpty()) {

                    // 1) Soft-delete all existing financements
                    List<Financement> allFins =
                            (realisation.getFinancements() != null) ? realisation.getFinancements() : new ArrayList<>();
                    for (Financement f : allFins) {
                        if (!Boolean.TRUE.equals(f.getIsDeleted())) {
                            f.setIsDeleted(true);
                        }
                    }

                    // 2) Create new financements from request
                    for (FinancementRequest fr : rReq.getFinancements()) {
                        if (fr.getMonnaieId() == null) {
                            log.warn("Skipping financement with null monnaieId for Realisation {}", realisation.getId());
                            continue;
                        }

                        Integer nextFinId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);

                        Financement fin = new Financement();
                        FinancementId fid = new FinancementId(
                                realisation.getId(),
                                realisation.getVersion(),
                                nextFinId
                        );
                        fin.setId(fid);
                        fin.setRealisation(realisation);

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

                        allFins.add(fin);
                    }

                    realisation.setFinancements(allFins);
                    financementRepository.saveAll(allFins);

                    // 3) Recompute budget from non-deleted financements
                    BigDecimal rb = allFins.stream()
                            .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                            .map(f -> {
                                BigDecimal budget = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null)
                                        ? f.getMonnaie().getTaux()
                                        : 0.0;
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
                        financementRepository.saveAll(realisation.getFinancements());
                    } else {
                        realisation.setFinancements(new ArrayList<>());
                    }
                    realisation.setBudgetEstime(BigDecimal.ZERO);
                }
                // =========================== END FINANCEMENTS (UPDATE) ===========================

                // ---- Seuil AFTER collections/budget are consistent
                setSeuilApplicableForRealisation(realisation, requestAcId);

                // link PAA if missing
                if (realisation.getPaas() == null) realisation.setPaas(new ArrayList<>());
                if (!realisation.getPaas().contains(existingPaa)) realisation.getPaas().add(existingPaa);

                realisation = realisationRepository.save(realisation);
                realisations.add(realisation);

            } else {
                // ============== CREATE new Realisation
                Realisation real = new Realisation();
                real.setIsDeleted(false);

                Integer nextRealId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
                real.setId(nextRealId);
                real.setVersion(1);

                real.setRealisationEnvisagees(rReq.getRealisationEnvisagees());
                real.setBeneficiaire(rReq.getBeneficiaire());
                real.setImputationBudgetaire(rReq.getImputationBudgetaire());
                real.setTypeMarcher(rReq.getTypeMarcher());
                real.setModePassation(rReq.getModePassation());
                LocalDate lpsNew = rReq.getDatePrevLps();
                real.setDatePrevLps(lpsNew);
                real.setDatePrevAc(lpsNew != null ? lpsNew.plusDays(30) : null);
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
                        Integer lotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                        lot.setId(lotId);
                        lot.setVersion(1);
                        lot.setDescription(lr.getDescription());
                        lot.setBudget(lr.getBudget());
                        lot.setIsDeleted(false);
                        lots.add(lotRepository.save(lot));
                    }
                    savedReal.setLots(lots);
                }

                // =========================== FINANCEMENTS (CREATE NEW REALISATION) ===========================
                if (rReq.getFinancements() != null && !rReq.getFinancements().isEmpty()) {
                    List<Financement> fins = new ArrayList<>();

                    for (FinancementRequest fr : rReq.getFinancements()) {
                        if (fr.getMonnaieId() == null) {
                            log.warn("Skipping financement with null monnaieId for new Realisation {}", savedReal.getId());
                            continue;
                        }

                        Integer nextFinId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);

                        Financement fin = new Financement();
                        FinancementId fid = new FinancementId(
                                savedReal.getId(),
                                savedReal.getVersion(),
                                nextFinId
                        );
                        fin.setId(fid);
                        fin.setRealisation(savedReal);

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

                        fins.add(fin);
                    }

                    savedReal.setFinancements(fins);
                    financementRepository.saveAll(fins);

                    BigDecimal rb = fins.stream()
                            .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                            .map(f -> {
                                BigDecimal budget = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null)
                                        ? f.getMonnaie().getTaux()
                                        : 0.0;
                                return budget.multiply(BigDecimal.valueOf(taux));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    savedReal.setBudgetEstime(rb);
                } else {
                    savedReal.setFinancements(new ArrayList<>());
                    savedReal.setBudgetEstime(BigDecimal.ZERO);
                }
                // =========================== END FINANCEMENTS (CREATE) ===========================

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

    copyForResponse.setActualise(savedPaa.getActualise());
    copyForResponse.setNbrActualisation(savedPaa.getNbrActualisation());

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
        Paa existingPaa = repository.findById(compositeId).orElseThrow(() -> new PaaNotFound(
                "PAA with ID " + paaId + " and version " + paaVer + " not found",
                List.of(paaId), List.of(paaVer)));

        if (Boolean.TRUE.equals(existingPaa.getIsDeleted())) {
            throw new PaaNotFound(
                    "Cannot update a deleted PAA with ID " + paaId + " and version " + paaVer,
                    List.of(paaId), List.of(paaVer));
        }

        if (existingPaa.getActualise() == null) existingPaa.setActualise(false);
        if (existingPaa.getNbrActualisation() == null) existingPaa.setNbrActualisation(0);

        // Log the current state and request
        log.info("Updating PAA ID: {} , Current Version: {}" , paaId , paaVer);
       log.info("Request data: {}" , request);

        List<BigDecimal> reqBudgetSums = new ArrayList<>();
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            for (int i = 0; i < request.getRealisations().size(); i++) {
                RealisationRequestForPaa rDto = request.getRealisations().get(i);
                BigDecimal sum = null;
                if (rDto.getFinancements() != null && !rDto.getFinancements().isEmpty()) {
                    sum = rDto.getFinancements().stream()
                            .map(FinancementRequest::getBudgetEstimer)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                }
                reqBudgetSums.add(sum);
                log.info("[REQ] Realisation index {}  financement sum = {}" , i , sum);
            }
        }

        // Track whether any documents have changed
        boolean documentsChanged = false;

        // Process any document changes
        if (request.getDocIds() != null) {
            Set<Integer> activeExistingDocIds =
                    existingPaa.getDocs() != null
                            ? existingPaa.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .map(Docs::getId)
                            .collect(Collectors.toSet())
                            : new HashSet<>();
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());
            documentsChanged = !requestDocIds.equals(activeExistingDocIds);

            if (documentsChanged) {
                log.info("Document changes detected:");
                log.info("  - Active existing docs: {}" , activeExistingDocIds);
                log.info("  - Requested docs: {}" , requestDocIds);
            }
        }

        String requestHash = generateRequestHash(request);
        String previousRequestHash = getPreviousRequestHash(paaId, paaVer);

        boolean hasNewLotsWithoutIds = false;
        if (request.getRealisations() != null) {
            for (RealisationRequestForPaa realisationDto : request.getRealisations()) {
                if (realisationDto.getLots() != null) {
                    hasNewLotsWithoutIds |= realisationDto.getLots().stream().anyMatch(lot -> lot.getId() == null);
                }
            }
        }

        boolean hasFiles = request.getPaaFiles() != null && !request.getPaaFiles().isEmpty();
        if (hasFiles) {
            log.info("Files detected in request - will create new version regardless of other changes");
        } else {
            log.info("Current request hash: {}" , requestHash);
            log.info("Previous request hash: {}" , previousRequestHash);
            boolean hasRealisationsWithoutIds = false;
            if (request.getRealisations() != null) {
                hasRealisationsWithoutIds = request.getRealisations().stream().anyMatch(r -> r.getId() == null);
            }

            boolean hasRealisationFiles = hasRealisationFiles(request.getRealisations());
            if (requestHash != null
                    && requestHash.equals(previousRequestHash)
                    && !documentsChanged
                    && !hasNewLotsWithoutIds
                    && !hasRealisationFiles
                    && !hasRealisationsWithoutIds) {
                log.info("Duplicate request detected and no document changes, returning existing PAA without changes");

                Paa copyForResponse = new Paa();
                BeanUtils.copyProperties(existingPaa, copyForResponse);

                if (existingPaa.getDocs() != null) {
                    copyForResponse.setDocs(
                            existingPaa.getDocs().stream()
                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                    .collect(Collectors.toList()));
                }

                if (copyForResponse.getRealisations() != null) {
                    copyForResponse.getRealisations().forEach(realisation -> {
                        if (realisation.getDocs() != null) {
                            realisation.setDocs(
                                    realisation.getDocs().stream()
                                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                            .collect(Collectors.toList()));
                        }
                        if (realisation.getLots() != null) {
                            realisation.setLots(
                                    realisation.getLots().stream()
                                            .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                            .collect(Collectors.toList()));
                        }
                        if (realisation.getSeuilApplicable() != null
                                && realisation.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
                            log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", realisation.getId());
                            realisation.setSeuilApplicable(BigDecimal.ZERO);
                        }
                    });
                }

                copyForResponse.setActualise(existingPaa.getActualise());
                copyForResponse.setNbrActualisation(existingPaa.getNbrActualisation());

                return copyForResponse;
            }
        }

        Paa tempPaa = new Paa();
        tempPaa.setRef(existingPaa.getRef());
        tempPaa.setNom(request.getNom() != null ? request.getNom() : existingPaa.getNom());
        tempPaa.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPaa.getAnnee());
        tempPaa.setProcessInstanceId(existingPaa.getProcessInstanceId());
        tempPaa.setStatus(existingPaa.getStatus());
        tempPaa.setModePassation(request.getModePassation() != null ? request.getModePassation() : existingPaa.getModePassation());
        tempPaa.setRemarque(request.getRemarque() != null ? request.getRemarque() : existingPaa.getRemarque());
        tempPaa.setActualise(existingPaa.getActualise());
        tempPaa.setNbrActualisation(existingPaa.getNbrActualisation());

        Ac requestAc = null;
        if (request.getAcId() != null) {
            requestAc = acRepository.findById(request.getAcId()).orElse(null);
        }
        tempPaa.setAc(requestAc != null ? requestAc : existingPaa.getAc());

        Integer requestAcId = request.getAcId() != null ? request.getAcId() : existingPaa.getAc().getId();

        boolean hasDirectChanges = !arePaaFieldsIdentical(existingPaa, tempPaa);
        log.info("Direct field changes detected: {}" , hasDirectChanges);

        boolean realisationsChanged = false;
        List<Realisation> updatedRealisations = new ArrayList<>();
        List<Docs> updatedDocs = new ArrayList<>();

        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            log.info("Processing {} realisations" , request.getRealisations().size() );
            for (RealisationRequestForPaa realisationDto : request.getRealisations()) {
                try {
                    log.info("Processing realisation DTO: {}", realisationDto);
                    log.info("Realisation ID: {} , Lots: {}" , realisationDto.getId() ,
                            (realisationDto.getLots() != null ? realisationDto.getLots().size() : "null"));

                    if (realisationDto.getId() != null) {
                        Integer versionToUse = realisationDto.getVersion();
                        if (versionToUse == null) {
                            Integer maxVersion = realisationRepository.findMaxVersionById(realisationDto.getId());
                            if (maxVersion != null) {
                                versionToUse = maxVersion;
                                log.info("Found latest version for Realisation {} : {}" , realisationDto.getId()  , maxVersion);
                            } else {
                                throw new RuntimeException("No versions found for Realisation with ID: " + realisationDto.getId());
                            }
                        }

                        Realisation updatedRealisation =
                                realisationService.updateWithVersioning(realisationDto.getId(), versionToUse, realisationDto);

                        realisationRepository.flush();

                        Integer latestVerForThisReal = realisationRepository.findMaxVersionById(updatedRealisation.getId());
                        Realisation refreshed = realisationRepository.findById(
                                new CompositeId(updatedRealisation.getId(), latestVerForThisReal)
                        ).orElseThrow(() -> new RuntimeException(
                                "Updated Realisation not found after flush: id=" + updatedRealisation.getId()
                                        + ", ver=" + latestVerForThisReal));

                        log.info("[PAA:update] Refreshed Realisation id= {}" , refreshed.getId()
                                + " ver=" + refreshed.getVersion());

                        updatedRealisations.add(refreshed);
                    } else {
                        log.info("Creating new Realisation");
                        RealisationRequest newRealisationRequest = new RealisationRequest();
                        newRealisationRequest.setRealisationEnvisagees(realisationDto.getRealisationEnvisagees());
                        newRealisationRequest.setTypeMarcher(realisationDto.getTypeMarcher());
                        newRealisationRequest.setModePassation(realisationDto.getModePassation());
                        LocalDate lps = realisationDto.getDatePrevLps();
                        newRealisationRequest.setDatePrevLps(lps);
                        newRealisationRequest.setDatePrevAc(lps != null ? lps.plusDays(30) : null);
                        newRealisationRequest.setIsPublished(realisationDto.getIsPublished());
                        newRealisationRequest.setPublishedAt(realisationDto.getPublishedAt());
                        newRealisationRequest.setSort(realisationDto.getSort());
                        newRealisationRequest.setDocIds(realisationDto.getDocIds());
                        if (realisationDto.getLots() != null && !realisationDto.getLots().isEmpty()) {
                            newRealisationRequest.setLots((List<LotResponse>) realisationDto.getLots());
                        }
                        if (realisationDto.getFinancements() != null && !realisationDto.getFinancements().isEmpty()) {
                            List<FinancementBaseRequestDto> financementBaseRequests = realisationDto.getFinancements().stream()
                                    .map(financementRequest -> {
                                        FinancementBaseRequestDto baseRequest = new FinancementBaseRequestDto();
                                        BeanUtils.copyProperties(financementRequest, baseRequest);
                                        return baseRequest;
                                    })
                                    .collect(Collectors.toList());
                            newRealisationRequest.setFinancementRequests(financementBaseRequests);
                            log.info(" Assigned {}  financements to new realization" ,financementBaseRequests.size());
                        }

                        Realisation newRealisation = realisationService.create(newRealisationRequest);
                        updatedRealisations.add(newRealisation);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to process Realisation: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Failed to process Realisation: " + e.getMessage(), e);
                }
            }

            if (existingPaa.getRealisations() != null && !existingPaa.getRealisations().isEmpty()) {
                Set<Integer> existingActiveIds = existingPaa.getRealisations().stream()
                        .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                        .map(Realisation::getId)
                        .collect(Collectors.toSet());

                Set<Integer> requestedIds = request.getRealisations().stream()
                        .filter(r -> r.getId() != null)
                        .map(RealisationRequestForPaa::getId)
                        .collect(Collectors.toSet());

                log.info("Existing realization IDs: {}" , existingActiveIds);
                log.info("Requested realization IDs: {}" , requestedIds);

                Set<String> beforeSet = existingPaa.getRealisations() == null ? Set.of() :
                        existingPaa.getRealisations().stream()
                                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                                .map(r -> r.getId() + ":" + r.getVersion())
                                .collect(Collectors.toSet());

                Set<String> afterSet = updatedRealisations.stream()
                        .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                        .map(r -> r.getId() + ":" + r.getVersion())
                        .collect(Collectors.toSet());

                if (!beforeSet.equals(afterSet)) {
                    log.info("Realisation set (id:version) changed");
                    realisationsChanged = true;
                }
            }
        } else if (existingPaa.getRealisations() != null && !existingPaa.getRealisations().isEmpty()) {
            log.info("Realisations removed from PAA");
            realisationsChanged = true;
        }

        if (request.getDocIds() != null) {
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());
            Set<Integer> activeExistingDocIds =
                    existingPaa.getDocs() != null
                            ? existingPaa.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .map(Docs::getId)
                            .collect(Collectors.toSet())
                            : new HashSet<>();

            log.info("Comparing document IDs:");
            log.info("  - Existing active docs: {}" , activeExistingDocIds);
            log.info("  - Requested docs: {}" , requestDocIds);

            if (!requestDocIds.equals(activeExistingDocIds)) {
                log.info("Document ID sets are different - marking as changed");
                documentsChanged = true;
            }

            updatedDocs = new ArrayList<>();
            if (existingPaa.getDocs() != null) {
                for (Docs existingDoc : existingPaa.getDocs()) {
                    Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                    if (docToProcess == null) {
                        log.info("Warning: Document {} no longer exists in database, skipping" , existingDoc.getId() );
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
                        log.info("Marked document with ID {}  as deleted for new version" , docToProcess.getId() );
                    }
                }
            }

            for (Integer docId : requestDocIds) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
                doc.setIsDeleted(false);
                doc = docsRepository.save(doc);
                updatedDocs.add(doc);
                log.info("Added new document with ID {} for new version" , doc.getId());
            }
        } else if (existingPaa.getDocs() != null && !existingPaa.getDocs().isEmpty()) {
            updatedDocs = new ArrayList<>();
            for (Docs existingDoc : existingPaa.getDocs()) {
                Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                if (docToProcess == null) continue;

                docToProcess.setIsDeleted(true);
                docToProcess = docsRepository.save(docToProcess);
                updatedDocs.add(docToProcess);
                log.info("Marked document with ID {}  as deleted (no docIds provided)" , docToProcess.getId() );
            }
            documentsChanged = true;
        } else {
            updatedDocs = new ArrayList<>();
        }

        boolean shouldCreateNewVersion = hasDirectChanges || realisationsChanged || documentsChanged;

        log.info("Should create new PAA version: {} " , shouldCreateNewVersion);
        log.info("  - Direct field changes: {}" , hasDirectChanges);
        log.info("  - Realisations changed: {}" , realisationsChanged);
        log.info("  - Documents changed: {}" , documentsChanged);
        log.info("  - Files uploaded: {}" , (request.getPaaFiles() != null && !request.getPaaFiles().isEmpty()));

        if (request.getPaaFiles() != null && !request.getPaaFiles().isEmpty()) {
            log.info("Files are present - forcing creation of new version");
            shouldCreateNewVersion = true;
        }

        if (!shouldCreateNewVersion) {
            log.info("No changes detected, returning existing PAA version: {}" , paaVer);

            if (request.getPaaFiles() != null) {
                try {
                    log.info("Processing {} files for existing PAA version" , request.getPaaFiles().size());
                    documentCommuneService.processAndSaveDocuments(request.getPaaFiles(), existingPaa, Constants.PATH_ENTREPOT_GED_PAA);
                    existingPaa = repository.save(existingPaa);
                    log.info("Saved PAA after processing files: {} version: {}" , existingPaa.getId(), existingPaa.getVersion());
                    if (!request.getPaaFiles().isEmpty()) {
                        log.info("Files were uploaded - forcing creation of new version");
                        shouldCreateNewVersion = true;
                        storePreviousRequestHash(paaId, paaVer, requestHash);
                    }
                } catch (IOException e) {
                    log.error("Error processing PAA files for existing version: {}" , e.getMessage());
                    throw new RuntimeException("Error processing PAA files for existing version: " + e.getMessage(), e);
                }
            }

            if (!shouldCreateNewVersion) {
                Paa copyForResponse = new Paa();
                BeanUtils.copyProperties(existingPaa, copyForResponse);

                if (existingPaa.getDocs() != null) {
                    copyForResponse.setDocs(
                            existingPaa.getDocs().stream()
                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                    .collect(Collectors.toList()));
                }

                if (copyForResponse.getRealisations() != null) {
                    copyForResponse.getRealisations().forEach(realisation -> {
                        if (realisation.getDocs() != null) {
                            realisation.setDocs(
                                    realisation.getDocs().stream()
                                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                            .collect(Collectors.toList()));
                        }
                        if (realisation.getLots() != null) {
                            realisation.setLots(
                                    realisation.getLots().stream()
                                            .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                            .collect(Collectors.toList()));
                        }
                        if (realisation.getSeuilApplicable() != null
                                && realisation.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
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

                copyForResponse.setActualise(existingPaa.getActualise());
                copyForResponse.setNbrActualisation(existingPaa.getNbrActualisation());

                log.info("Updated existing PAA ID: {} version: {} ,budgetEstime {}" , copyForResponse.getId()
                        , copyForResponse.getVersion()
                        , totalBudget);
                return copyForResponse;
            }
        }

        log.info("Changes detected, creating new PAA version: {}" , (paaVer + 1));
        storePreviousRequestHash(paaId, paaVer, requestHash);

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

        boolean somethingChanged = hasDirectChanges || realisationsChanged || documentsChanged || hasFiles;
        if (Boolean.TRUE.equals(existingPaa.getActualise()) && somethingChanged) {
            newPaa.setActualise(false);
            log.info("Flag 'actualise' toggled from true -> false due to detected changes (new version).");
        } else {
            newPaa.setActualise(existingPaa.getActualise());
            log.info("Flag 'actualise' kept as {}", existingPaa.getActualise());
        }
        newPaa.setNbrActualisation(existingPaa.getNbrActualisation());

        newPaa.setRealisations(new ArrayList<>());
        newPaa.setDocs(new ArrayList<>());

        newPaa = repository.save(newPaa);
        log.info("Saved new PAA: {} version {}" , newPaa.getId() , newPaa.getVersion());

        if (!updatedRealisations.isEmpty()) {
            log.info("Setting up relationships with {} realisations" , updatedRealisations.size());
            List<Realisation> confirmedRealisations = new ArrayList<>();
            final Paa finalNewPaa = newPaa;

            int __reqIdx = 0;
            for (Realisation realisation : updatedRealisations) {
                try {
                    Realisation confirmedRealisation =
                            realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion())).orElse(null);
                    if (confirmedRealisation == null) {
                        System.err.println("Warning: Realisation " + realisation.getId() +
                                " version " + realisation.getVersion() +
                                " not found in database. Skipping relationship setup.");
                        __reqIdx++;
                        continue;
                    }

                    boolean alreadyAssociated = false;
                    if (confirmedRealisation.getPaas() != null) {
                        alreadyAssociated = confirmedRealisation.getPaas().stream()
                                .anyMatch(p -> p.getId().equals(finalNewPaa.getId())
                                        && p.getVersion().equals(finalNewPaa.getVersion()));
                    }

                    if (!alreadyAssociated) {
                        List<Paa> paaList = confirmedRealisation.getPaas();
                        if (paaList == null) {
                            paaList = new ArrayList<>();
                            confirmedRealisation.setPaas(paaList);
                        }
                        paaList.add(finalNewPaa);
                    }

                    try {
                        BigDecimal rbFx = Optional.ofNullable(confirmedRealisation.getFinancements())
                                .orElseGet(List::of).stream()
                                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                                .map(f -> {
                                    BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                    double taux = (f.getTauxApplicable() != null) ? f.getTauxApplicable()
                                            : ((f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0);
                                    return amt.multiply(BigDecimal.valueOf(taux));
                                })
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        confirmedRealisation.setBudgetEstime(rbFx);
                    } catch (Exception e) {
                        System.err.println("[FX] Failed to compute converted budget for Realisation " +
                                confirmedRealisation.getId() + ": " + e.getMessage());
                    }

                    try {
                        setSeuilApplicableForRealisation(confirmedRealisation, requestAcId);
                    } catch (Exception e) {
                        System.err.println("[SEUIL] Failed to set seuilApplicable for Realisation " +
                                confirmedRealisation.getId() + ": " + e.getMessage());
                    }

                    BigDecimal reqSum = (reqBudgetSums != null && __reqIdx < reqBudgetSums.size()) ? reqBudgetSums.get(__reqIdx) : null;
                    if (reqSum != null) {
                        log.info("[REQ] Using request financement sum {} as budgetEstime for Realisation {}" , reqSum ,confirmedRealisation.getId());
                        confirmedRealisation.setBudgetEstime(reqSum);
                    }
                    __reqIdx++;

                    confirmedRealisation = realisationRepository.save(confirmedRealisation);
                    log.info("Successfully associated Realisation {} version {}  with new PAA (budget= {} , seuil= {} )" , confirmedRealisation.getId()
                           , confirmedRealisation.getVersion() ,
                             confirmedRealisation.getBudgetEstime() , confirmedRealisation.getSeuilApplicable() );
                    confirmedRealisations.add(confirmedRealisation);
                } catch (Exception e) {
                    log.error("Error associating Realisation with PAA: {}" , e.getMessage());
                    e.printStackTrace();
                }
            }

            newPaa.setRealisations(confirmedRealisations);
            log.info("Set {} realisations on the new PAA" , confirmedRealisations.size());

            BigDecimal totalBudget = newPaa.getRealisations().stream()
                    .map(Realisation::getBudgetEstime)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            newPaa.setBudgetEstime(totalBudget);
            log.info("Updated PAA ID: {}  version: {}  budgetEstime: {}", newPaa.getId() ,
                     newPaa.getVersion() ,
                     totalBudget);

            realisationRepository.flush();
            validateRealisations(newPaa);

            newPaa = repository.save(newPaa);
        } else if (existingPaa.getRealisations() != null) {
            log.info("Copying {} realisations from existing PAA" , existingPaa.getRealisations().size() );
            List<Realisation> confirmedRealisations = new ArrayList<>();
            final Paa finalNewPaa = newPaa;

            for (Realisation existingRealisation : existingPaa.getRealisations()) {
                try {
                    Integer latestVersion = realisationRepository.findMaxVersionById(existingRealisation.getId());
                    if (latestVersion == null) {
                        System.err.println("Warning: No versions found for Realisation ID " + existingRealisation.getId());
                        continue;
                    }

                    Realisation latestRealisation =
                            realisationRepository.findById(new CompositeId(existingRealisation.getId(), latestVersion)).orElse(null);
                    if (latestRealisation == null) {
                        System.err.println("Warning: Latest Realisation " + existingRealisation.getId() +
                                " version " + latestVersion + " not found in database. Skipping.");
                        continue;
                    }

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
                        log.info("[SEUIL FX] Copied Realisation {} converted budget (Σ amt×taux) for seuil: {}" ,
                                latestRealisation.getId(), rbFx);
                    } catch (Exception e) {
                        log.error("[SEUIL FX] Failed to compute converted budget for copied Realisation {} : {}" ,
                                latestRealisation.getId() , e.getMessage());
                    }
                    setSeuilApplicableForRealisation(latestRealisation, requestAcId);

                    boolean alreadyAssociated = false;
                    if (latestRealisation.getPaas() != null) {
                        alreadyAssociated = latestRealisation.getPaas().stream()
                                .anyMatch(p -> p.getId().equals(finalNewPaa.getId())
                                        && p.getVersion().equals(finalNewPaa.getVersion()));
                    }

                    if (alreadyAssociated) {
                        log.info("Realisation {},version {} is already associated with PAA  {}  version {}. Skipping association." ,
                                latestRealisation.getId() ,
                                 latestRealisation.getVersion() ,
                                finalNewPaa.getId()
                               , finalNewPaa.getVersion() );
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

                    log.info("Set existing Realisation ID: {} version {} budgetEstime {}" ,
                            latestRealisation.getId(),latestRealisation.getVersion() ,
                            latestRealisation.getBudgetEstime());
                    latestRealisation = realisationRepository.save(latestRealisation);
                    log.info("Successfully associated latest Realisation {} version {} with new PAA" ,
                            latestRealisation.getId()
                            , latestRealisation.getVersion() );
                    confirmedRealisations.add(latestRealisation);
                } catch (Exception e) {
                    log.error("Error handling Realisation relationship: {}" , e.getMessage());
                    e.printStackTrace();
                }
            }

            newPaa.setRealisations(confirmedRealisations);
            log.info("Updated new PAA with {}  confirmed realisations ", confirmedRealisations.size() );
            BigDecimal totalBudget = newPaa.getRealisations().stream()
                    .map(Realisation::getBudgetEstime)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            newPaa.setBudgetEstime(totalBudget);

            realisationRepository.flush();
            validateRealisations(newPaa);

            newPaa = repository.save(newPaa);
            log.info("Updated PAA ID: {} version {} budgetEstime {}" , newPaa.getId() ,
                     newPaa.getVersion()
                    , totalBudget);
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

                    boolean alreadyAssociated = paaList.stream().anyMatch(
                            p -> p.getId().equals(finalNewPaaForDocs.getId())
                                    && p.getVersion().equals(finalNewPaaForDocs.getVersion()));
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
            log.info("Copied {}  documents to new PAA version with correct delete status" , confirmedDocs.size() );
        } else if (existingPaa.getDocs() != null && !existingPaa.getDocs().isEmpty()) {
            List<Docs> confirmedDocs = new ArrayList<>();
            final Paa finalNewPaaForExistingDocs = newPaa;

            for (Docs existingDoc : existingPaa.getDocs()) {
                try {
                    Docs confirmedDoc = docsRepository.findById(existingDoc.getId()).orElse(null);
                    if (confirmedDoc == null) {
                        log.error("Warning: Document {} not found in database. Skipping." , existingDoc.getId() );
                        continue;
                    }

                    List<Paa> paaList = confirmedDoc.getPaas();
                    if (paaList == null) {
                        paaList = new ArrayList<>();
                        confirmedDoc.setPaas(paaList);
                    }

                    boolean alreadyAssociated = paaList.stream().anyMatch(
                            p -> p.getId().equals(finalNewPaaForExistingDocs.getId())
                                    && p.getVersion().equals(finalNewPaaForExistingDocs.getVersion()));
                    if (!alreadyAssociated) {
                        paaList.add(finalNewPaaForExistingDocs);
                    }

                    confirmedDoc = docsRepository.save(confirmedDoc);
                    confirmedDocs.add(confirmedDoc);
                } catch (Exception e) {
                    log.error("Error associating existing Document with PAA: {}" , e.getMessage());
                }
            }

            newPaa.setDocs(confirmedDocs);
            log.info("Copied {}  existing documents to new PAA version" , confirmedDocs.size());
        }

        newPaa = repository.save(newPaa);
        log.info("Final save of PAA: {} version {}" , newPaa.getId() , newPaa.getVersion());

        if (request.getPaaFiles() != null) {
            try {
                log.info("Processing {} files for new PAA version" , request.getPaaFiles().size());
                documentCommuneService.processAndSaveDocuments(request.getPaaFiles(), newPaa, Constants.PATH_ENTREPOT_GED_PAA);
                newPaa = repository.save(newPaa);
                log.info("Saved PAA after processing files: {} version {}" , newPaa.getId() , newPaa.getVersion());
            } catch (IOException e) {
                log.error("Error processing PAA files for new version: {} " , e.getMessage());
                throw new RuntimeException("Error processing PAA files for new version: " + e.getMessage(), e);
            }
        }

        storePreviousRequestHash(paaId, newPaa.getVersion(), requestHash);
        Paa copyForReturn = new Paa();
        BeanUtils.copyProperties(newPaa, copyForReturn);
        copyForReturn.setDocs(
                newPaa.getDocs().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                        .collect(Collectors.toList()));

        if (copyForReturn.getRealisations() != null) {
            copyForReturn.getRealisations().forEach(realisation -> {
                if (realisation.getDocs() != null) {
                    realisation.setDocs(
                            realisation.getDocs().stream()
                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                    .collect(Collectors.toList()));
                }
                if (realisation.getLots() != null) {
                    realisation.setLots(
                            realisation.getLots().stream()
                                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                    .collect(Collectors.toList()));
                }
                if (realisation.getSeuilApplicable() != null
                        && realisation.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
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

        copyForReturn.setActualise(newPaa.getActualise());
        copyForReturn.setNbrActualisation(newPaa.getNbrActualisation());

        log.info("Final response PAA ID: {} version {}: budgetEstime: {} actualise: {} nbrActualisation: {}" ,
                copyForReturn.getId()
                , copyForReturn.getVersion(),
                finalBudget, copyForReturn.getActualise() ,
                 copyForReturn.getNbrActualisation());

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

        log.info("Found latest version for PAA ID {}  verion latest {}" , paaId , latestVersion);

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

        return
                 Objects.equals(paa1.getNom(), paa2.getNom())
                && Objects.equals(paa1.getRemarque(), paa2.getRemarque())
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
//                realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
//                realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
//                realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                realisation.setIsPublished(realisationRequest.getIsPublished());
                realisation.setPublishedAt(realisationRequest.getPublishedAt());
                realisation.setSort(realisationRequest.getSort());

                log.info("the realisations have files {} ",realisationRequest.getRealisationFiles());

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
                log.info("Major lot change: Removing all lots");
                return true;
            }

            // If there are lots in the request but the realisation has none, this is a major change
            if (!dto.getLots().isEmpty() && (existingRealisation.getLots() == null || existingRealisation.getLots().isEmpty())) {
                log.info("Major lot change: Adding first lots");
                return true;
            }

            // If both have lots, check for additions/removals (but not description changes)
            if (!dto.getLots().isEmpty() && existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty()) {
                // Different number of lots = major change
                if (dto.getLots().size() != existingRealisation.getLots().size()) {
                    log.info("Major lot change: Different number of lots");
                    return true;
                }

                // Check for new lots (without IDs) = major change
                boolean hasNewLots = dto.getLots().stream().anyMatch(lotResponse -> lotResponse.getId() == null);
                if (hasNewLots) {
                    log.info("Major lot change: New lots without IDs");
                    return true;
                }

                // Extract lot IDs
                Set<Integer> requestLotIds = dto.getLots().stream().filter(lotResponse -> lotResponse.getId() != null).map(LotResponse::getId).collect(Collectors.toSet());

                Set<Integer> existingLotIds = existingRealisation.getLots().stream().map(Lot::getId).collect(Collectors.toSet());

                // Different lot IDs (additions or removals) = major change
                if (!requestLotIds.equals(existingLotIds)) {
                    log.info("Major lot change: Different lot IDs");
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


    public Paa findByIdPaa(Integer paaId, Integer paaVer) throws PaaNotFound, AcNotFound {
        Integer latestVersion = findLatestVersionById(paaId).orElseThrow(() -> new PaaNotFound("No PAA found for ID", List.of(paaId), List.of(paaVer)));
        Paa paa = findById(paaId, latestVersion).orElseThrow(() -> new PaaNotFound("No PAA found for ID and version", List.of(paaId), List.of(latestVersion)));


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
    private void setRef(Paa paa) throws AcNotFound {
        if (paa == null || paa.getId() == null || paa.getAnnee() == null) {
            throw new IllegalArgumentException("Cannot set reference: paa, id, or year is null.");
        }
        String ref ;
        Ac ac = acService.findById(paa.getAc().getId());
        if (ac.getAbreviation()!=null && ac.getAbreviation().length()>0) {
            ref = "PAA_" + ac.getAbreviation() + "_" + paa.getAnnee();
            paa.setRef(ref);
        }
        else {
             ref = "PAA_" + ac.getNom() + "_" + paa.getAnnee();
            paa.setRef(ref);
        }

    }
    @Override
    public Page<Paa> findCurrentYearPaasByUser(Pageable pageable) {
        log.info("Retrieving PAAs for current year and next year for connected user with pageable: {}", pageable);

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
                log.warn("AC not found with ID: {}", acId);
                return Page.empty(pageable);
            }
        }

        // Get current year and next year
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;

        List<String> allowedYears = List.of(
                String.valueOf(currentYear),
                String.valueOf(nextYear)
        );

        log.info("Allowed PAA years for retrieval: {}", allowedYears);

        // Build specification
        Specification<Paa> spec = Specification
                .where(PaaSpecifications.anneeIn(allowedYears))
                .and(PaaSpecifications.paaAc(userAcIds))
                .and(PaaSpecifications.deleted(false))
                .and(PaaSpecifications.isLatestVersion());

        Page<Paa> paasPage = repository.findAll(spec, pageable);

        if (paasPage.isEmpty()) {
            log.info("No PAAs found for years {} and {} for user ACs {}", currentYear, nextYear, userAcIds);
            return Page.empty(pageable);
        }

        log.info("Retrieved {} PAAs for years {} & {} for user ACs {}",
                paasPage.getContent().size(), currentYear, nextYear, userAcIds);

        return paasPage;
    }


    @Transactional
    @Override
    public List<AcAvailablePaaResponse> getAcsWithoutPaaForCurrentYear() throws AcNotFound {

        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;

        log.info("Checking ACs without PAA for years {} and {}", currentYear, nextYear);

        // 1. Get ACs of user
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            throw new AcNotFound("No AC IDs found for the connected user", Collections.emptyList());
        }

        // Validate each AC exists
        for (Integer acId : userAcIds) {
            acService.findById(acId);
        }

        // 2. Load ALL non-deleted PAAs for current year and next year
        List<Paa> paas = repository.findAll((root, query, cb) -> {
            Predicate yearPredicate = root.get("annee").in(
                    String.valueOf(currentYear),
                    String.valueOf(nextYear)
            );
            Predicate notDeleted = cb.isFalse(root.get("isDeleted"));
            return cb.and(yearPredicate, notDeleted);
        });

        // 3. Group PAAs by AC → Set of existing years
        Map<Integer, Set<Integer>> acExistingYears = new HashMap<>();

        for (Paa paa : paas) {
            if (paa.getAc() != null) {
                acExistingYears
                        .computeIfAbsent(paa.getAc().getId(), k -> new HashSet<>())
                        .add(Integer.parseInt(paa.getAnnee()));
            }
        }

        List<AcAvailablePaaResponse> response = new ArrayList<>();

        // 4. For each AC → determine all allowed years (1 or 2 values)
        for (Integer acId : userAcIds) {

            Set<Integer> years = acExistingYears.getOrDefault(acId, new HashSet<>());
            List<Integer> allowedYears = new ArrayList<>();

            // If AC has NO PAA for current year → allow it
            if (!years.contains(currentYear)) {
                allowedYears.add(currentYear);
            }

            // If AC has NO PAA for next year → allow it
            if (!years.contains(nextYear)) {
                allowedYears.add(nextYear);
            }

            // If AC has none of the allowed years → skip
            if (allowedYears.isEmpty()) {
                continue;
            }

            Ac ac = acService.findById(acId);

            AcAvailablePaaResponse dto = new AcAvailablePaaResponse();
            dto.setAcId(ac.getId());
            dto.setAcName(ac.getNom());
            dto.setAllowedYears(allowedYears);

            response.add(dto);
        }

        if (response.isEmpty()) {
            throw new AllAcsHavePaaException(
                    "Vous ne pouvez pas actuellement créer un nouveau PAA ! " +
                            "Toutes vos AC ont déjà un PAA pour " + currentYear +
                            " et " + nextYear + "."
            );
        }

        return response;
    }

    @Transactional
    @Override
    public Paa createPaaPerAc(PaaRequestGlobal request)
            throws AcNotFound, PaaCreationLimitExceededException, IOException {

        log.info("\n==================[ START CREATE PAA ]==================");

        // --------------------------
        // 1. Validate request year
        // --------------------------
        log.info("[STEP 1] Validating year...");
        String annee = request.getAnnee();
        if (annee == null || annee.isBlank()) {
            throw new IllegalArgumentException("L'année est obligatoire pour créer un PAA.");
        }

        int requestedYear;
        try {
            requestedYear = Integer.parseInt(annee);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Année invalide : " + annee);
        }

        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;

        if (requestedYear != currentYear && requestedYear != nextYear) {
            throw new IllegalArgumentException(
                    "Vous ne pouvez créer un PAA que pour l'année " + currentYear +
                            " ou " + nextYear + ". Année reçue : " + requestedYear
            );
        }
        log.info(" → Year validated = {}", requestedYear);

        // --------------------------
        // 2. Validate AC access
        // --------------------------
        log.info("[STEP 2] Validating AC access...");
        Integer requestAcId = request.getAcId();

        if (requestAcId == null) {
            throw new IllegalArgumentException("AC ID is required to create a PAA.");
        }

        log.info(" → Requested AC = {}", requestAcId);
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        log.info(" → User AC permissions = {}", userAcIds);

        if (!userAcIds.contains(requestAcId)) {
            throw new AcNotFound("User does not have permission for AC ID: " + requestAcId, List.of(requestAcId));
        }

        Ac ac = acService.findById(requestAcId);
        log.info(" ✔ AC found: {}", ac.getId());

        // --------------------------
        // 3. Uniqueness check
        // --------------------------
        log.info("[STEP 3] Checking PAA uniqueness for year {}...", annee);

        Specification<Paa> spec = PaaSpecifications.annee(annee)
                .and(PaaSpecifications.paaAc(requestAcId))
                .and(PaaSpecifications.deleted(false));

        List<Paa> existingPaas = repository.findAll(spec);

        if (!existingPaas.isEmpty()) {
            Paa existingPaa = existingPaas.get(0);
            log.info(" ❌ Existing PAA found: ID={}, VER={}", existingPaa.getId(), existingPaa.getVersion());
            throw new PaaCreationLimitExceededException(
                    "Un PAA existe déjà pour l'AC " + requestAcId + " pour l'année " + annee + ".",
                    existingPaa.getId(),
                    existingPaa.getVersion(),
                    1
            );
        }

        log.info(" ✔ No PAA exists for this AC and year");

        // --------------------------
        // 4. Create new PAA
        // --------------------------
        log.info("[STEP 4] Creating PAA...");

        Integer paaId = sequenceGeneratorService.getNextId(Constants.PAA_ID_SEQ);
        log.info(" → Generated PAA ID = {}", paaId);

        Paa paa = new Paa();
        paa.setId(paaId);
        paa.setVersion(1);
        paa.setNom(request.getNom());
        paa.setModePassation(request.getModePassation());
        paa.setRemarque(request.getRemarque());
        paa.setAnnee(annee);
        paa.setBudgetEstime(request.getBudgetEstime());
        paa.setStatus(ProcessStatus.BROUILLON);
        paa.setAc(ac);
        setRef(paa);
        paa.setActualise(Boolean.FALSE);
        paa.setNbrActualisation(0);

        paa.setRealisations(new ArrayList<>());
        paa.setDocs(new ArrayList<>());

        Paa savedPaa = repository.save(paa);
        log.info(" ✔ PAA SAVED → ID={}, VER=1", savedPaa.getId());

        // --------------------------
        // 5. Documents
        // --------------------------
        log.info("[STEP 5] Processing PAA documents...");
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            log.info(" → {} documents to process", request.getDocIds().size());

            List<Docs> docs = new ArrayList<>();
            for (Integer docId : request.getDocIds()) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));

                log.info("   • Attaching doc ID {}", docId);

                if (doc.getPaas() == null) doc.setPaas(new ArrayList<>());
                doc.getPaas().add(savedPaa);
                docs.add(doc);
                docsRepository.save(doc);
            }
            savedPaa.setDocs(docs);
        }

        if (request.getPaaFiles() != null && !request.getPaaFiles().isEmpty()) {
            log.info(" → Processing {} uploaded PAA files", request.getPaaFiles().size());
            documentCommuneService.processAndSaveDocuments(
                    request.getPaaFiles(),
                    savedPaa,
                    Constants.PATH_ENTREPOT_GED_PAA
            );
        }

        // --------------------------
        // 6. REALISATIONS
        // --------------------------
        log.info("[STEP 6] Processing Realisations...");

        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {

            log.info(" → {} realisations to create", request.getRealisations().size());
            List<Realisation> realisationsToSave = new ArrayList<>();

            for (RealisationRequestForPaa realisationRequest : request.getRealisations()) {

                log.info("\n-----------------[ NEW REALISATION ]-----------------");

                Realisation realisation = new Realisation();

                Integer realisationId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
                realisation.setId(realisationId);
                realisation.setVersion(1);

                log.info(" ✔ REALISATION CREATED → ID={}, VER=1", realisationId);

                realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                realisation.setBeneficiaire(realisationRequest.getBeneficiaire());
                realisation.setImputationBudgetaire(realisationRequest.getImputationBudgetaire());
                realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                realisation.setModePassation(realisationRequest.getModePassation());

                LocalDate lps = realisationRequest.getDatePrevLps();
                realisation.setDatePrevLps(lps);
                realisation.setDatePrevAc(lps != null ? lps.plusDays(30) : null);

                realisation.setIsPublished(realisationRequest.getIsPublished());
                realisation.setPublishedAt(realisationRequest.getPublishedAt());
                realisation.setSort(realisationRequest.getSort());

                log.info("   • Realisation details: envisagée='{}', bénéficiaire='{}'",
                        realisation.getRealisationEnvisagees(), realisation.getBeneficiaire());

                // Files
                if (realisationRequest.getRealisationFiles() != null &&
                        !realisationRequest.getRealisationFiles().isEmpty()) {
                    log.info("   • Processing {} realisation files...", realisationRequest.getRealisationFiles().size());
                    documentCommuneService.processAndSaveDocuments(
                            realisationRequest.getRealisationFiles(),
                            realisation,
                            Constants.PATH_ENTREPOT_GED_PAA + "/" + paa.getId() + Constants.PATH_ENTREPOT_GED_REALISATION
                    );
                }

                if (realisation.getPaas() == null) realisation.setPaas(new ArrayList<>());
                realisation.getPaas().add(savedPaa);

                savedPaa.getRealisations().add(realisation);

                // --------------------------
                // LOTS
                // --------------------------
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    log.info("   • {} lots to create", realisationRequest.getLots().size());

                    List<Lot> lots = new ArrayList<>();
                    for (LotResponse lotRequest : realisationRequest.getLots()) {

                        if (lotRequest.getDescription() == null || lotRequest.getDescription().isEmpty()) continue;

                        Lot lot = new Lot();

                        Integer lotId = lotRequest.getId();
                        if (lotId == null) {
                            lotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                        }

                        log.info("      LOT CREATED → ID={}", lotId);

                        CompositeId compositeId = new CompositeId(lotId, 1);
                        if (lotRepository.existsById(compositeId)) {
                            throw new IllegalStateException("Generated lot ID " + lotId + " already exists");
                        }

                        lot.setId(lotId);
                        lot.setVersion(1);
                        lot.setDescription(lotRequest.getDescription());
                        lot.setBudget(lotRequest.getBudget());

                        lots.add(lotRepository.save(lot));
                    }

                    realisation.setLots(lots);
                } else {
                    log.info("   • No lots provided");
                    realisation.setLots(new ArrayList<>());
                }

                Realisation savedReal = realisationRepository.save(realisation);

                // --------------------------
                // FINANCEMENTS
                // --------------------------
                if (realisationRequest.getFinancements() != null && !realisationRequest.getFinancements().isEmpty()) {

                    log.info("   • {} financements to create", realisationRequest.getFinancements().size());

                    List<Financement> financements = new ArrayList<>();

                    for (FinancementRequest financementRequest : realisationRequest.getFinancements()) {

                        log.info("      → Processing financement: monnaie={}, budget={}, sourceFin={}",
                                financementRequest.getMonnaieId(),
                                financementRequest.getBudgetEstimer(),
                                financementRequest.getSourceFinancement() != null ? financementRequest.getSourceFinancement().getId() : null
                        );

                        if (financementRequest.getMonnaieId() == null) continue;

                        Integer finId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);

                        Financement financement = new Financement();

                        FinancementId financementId = new FinancementId(
                                realisation.getId(),
                                realisation.getVersion(),
                                finId
                        );

                        financement.setId(financementId);
                        financement.setRealisation(realisation);

                        Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
                                .orElseThrow(() -> new EntityNotFoundException("Monnaie not found"));

                        financement.setMonnaie(monnaie);
                        financement.setTauxApplicable(
                                monnaie.getTaux() != null ? monnaie.getTaux() : 0.0
                        );
                        financement.setBudgetEstimer(financementRequest.getBudgetEstimer());

                        log.info("        FINANCEMENT CREATED → realisation={}, financementId={}, taux={}",
                                realisationId, finId, financement.getTauxApplicable());

                        if (financementRequest.getSourceFinancement() != null &&
                                financementRequest.getSourceFinancement().getId() != null) {

                            RefParam src = refParamRepository.findById(
                                            financementRequest.getSourceFinancement().getId())
                                    .orElseThrow(() -> new EntityNotFoundException("Source Financement not found"));

                            financement.setSourceFinancement(src);
                        }

                        financements.add(financement);
                    }

                    realisation.setFinancements(financements);
                    financementRepository.saveAll(financements);

                    BigDecimal budget = financements.stream()
                            .map(f -> {

                                BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null)
                                        ? f.getMonnaie().getTaux()
                                        : 0.0;

                                BigDecimal converted = amt.multiply(BigDecimal.valueOf(taux));

                                log.info("        → conversion: {} * {} = {}", amt, taux, converted);

                                return converted;
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    realisation.setBudgetEstime(budget);
                    log.info("   ✔ Realisation {} BUDGET ESTIME = {}", realisationId, budget);

                } else {
                    log.info("   • No financements");
                    realisation.setFinancements(new ArrayList<>());
                    realisation.setBudgetEstime(BigDecimal.ZERO);
                }

                // --------------------------
                // Seuil
                // --------------------------
                setSeuilApplicableForRealisation(realisation, requestAcId);

                log.info("   ✔ Realisation {} SEUIL = {}", realisationId, realisation.getSeuilApplicable());

                realisationsToSave.add(realisation);
            }

            realisationRepository.saveAll(realisationsToSave);
        }

        // --------------------------
        // 7. Compute global budget
        // --------------------------
        log.info("[STEP 7] Validating and computing global budget...");

        validateRealisations(savedPaa);

        savedPaa.getRealisations().forEach(r ->
                log.info("   • Realisation {} → budget={}", r.getId(), r.getBudgetEstime())
        );

        BigDecimal totalBudget = savedPaa.getRealisations().stream()
                .map(r -> r.getBudgetEstime() != null ? r.getBudgetEstime() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        savedPaa.setBudgetEstime(totalBudget);

        log.info(" ✔ TOTAL PAA BUDGET = {}", totalBudget);

        // Ensure seuil not negative (safety)
        for (Realisation r : savedPaa.getRealisations()) {
            if (r.getSeuilApplicable() != null && r.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
                r.setSeuilApplicable(BigDecimal.ZERO);
            }
        }

        log.info("[FINAL] ✔ PAA CREATED SUCCESSFULLY → ID={} with {} realisations",
                savedPaa.getId(), savedPaa.getRealisations().size());

        log.info("==================[ END CREATE PAA ]==================\n");

        return repository.save(savedPaa);
    }




    @Override
    public Page<Paa> findAllForWebsite(Specification<Paa> spec, Pageable pageable) {
        Specification<Paa> finalSpec = Specification.where(spec)
                .and(PaaSpecifications.isLatestPublishedVersion());
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
    private void validateRealisations(Paa paa, Set<Integer> onlyRealisationIds) {
        if (paa == null || paa.getRealisations() == null) return;

        for (Realisation realisation : paa.getRealisations()) {
            if (Boolean.TRUE.equals(realisation.getIsDeleted())) continue;

            if (onlyRealisationIds != null && !onlyRealisationIds.isEmpty()) {
                Integer rid = realisation.getId();
                if (rid == null || !onlyRealisationIds.contains(rid)) continue;
            }

            if (realisation.getLots() != null && !realisation.getLots().isEmpty()) {
                validateLotBudgetConsistency(realisation);
                validateLotBudgetSum(realisation);
            }
        }
    }

    private void validateRealisations(Paa paa) {
        validateRealisations(paa, null);
    }

    private void setSeuilApplicableForRealisation(Realisation realisation, Integer acId) {
        if (realisation.getTypeMarcher() != null && realisation.getModePassation() != null) {
            try {
                log.info("************** ac id : {} type marche id : {} mode de passation : {}", acId,  realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());
                Specification<AcSeuil> acSeuilSpec = AcSeuilSpecifications.acId(acId)
                        .and(AcSeuilSpecifications.typeMarcherId(realisation.getTypeMarcher().getId()));
                Optional<AcSeuil> acSeuilOpt = acSeuilService.findOne(acSeuilSpec);

                Specification<SeuilGeneral> seuilGeneralSpec = SeuilGeneralSpecifications.typeMarcherId(realisation.getTypeMarcher().getId())
                        .and(SeuilGeneralSpecifications.deleted(false));
                Optional<SeuilGeneral> seuilGeneralOpt = seuilGeneralService.findOne(seuilGeneralSpec);

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
                } else if(seuilGeneralOpt.isPresent()){
                    BigDecimal seuilGeneral = seuilGeneralOpt.get().getMontant();
                    realisation.setSeuilApplicable(seuilGeneral);
                    log.info("Set seuilApplicable to {} for Realisation ID: {} with AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}",
                            realisation.getSeuilApplicable(), realisation.getId(), acId,
                            realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());

                    // Validate Realisation budget against AcSeuil
                    BigDecimal budgetEstime = realisation.getBudgetEstime() != null ? realisation.getBudgetEstime() : BigDecimal.ZERO;
                    if (budgetEstime.compareTo(seuilGeneral) > 0) {
                        log.error("Realisation ID {} budgetEstime {} exceeds Ac seuil {} for PAA",
                                realisation.getId(), budgetEstime, seuilGeneral);
                        throw new RealisationBudgetExceedsAcSeuilException(
                                String.format(
                                        "La réalisation %s a un budget estimé %s supérieur au seuil générale %s. Cette activité est compatible avec le PPM, mais pas avec le PAA.",
                                        realisation.getRealisationEnvisagees(), budgetEstime, seuilGeneral
                                ));
                    }
                    }
                else {
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

    /**
     * Creates a new version of a PAA when the latest version is PUBLIE.
     * - New version = latest + 1
     * - Copies core fields (ref, nom, remarque, annee, AC, modePassation, modelAlfrescoId)
     * - Resets processInstanceId, sets status=BROUILLON, isDeleted=false
     * - Marks isActualisation=true and increments nbrActualisation
     * - Copies only non-deleted Docs
     * - Carries over active Realisations as-is (and re-links the join)
     * - Recomputes PAA budget as the sum of carried Realisation budgets
     */
    @Transactional
    @Override
    public Paa actualise(Integer paaId)
            throws PaaNotFound, PaaInvalidStatusException, PaaNotAssociatedWithUserAcException {

        Integer latestVersion = Optional.ofNullable(repository.findMaxVersionById(paaId))
                .orElseThrow(() -> new PaaNotFound("No PAA found for ID", List.of(paaId), List.of()));

        Paa latest = repository.findById(new CompositeId(paaId, latestVersion))
                .orElseThrow(() -> new PaaNotFound(
                        "No PAA found for ID and version",
                        List.of(paaId), List.of(latestVersion)));

        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        Integer acId = latest.getAc() != null ? latest.getAc().getId() : null;
        if (userAcIds.isEmpty() || acId == null || !userAcIds.contains(acId)) {
            throw new PaaNotAssociatedWithUserAcException(
                    "PAA not associated with one of the user's ACs",
                    List.of(paaId.longValue()), List.of(latestVersion));
        }

        if (!ProcessStatus.PUBLIE.equals(latest.getStatus())) {
            throw new PaaInvalidStatusException(
                    "Actualisation is only allowed when status is PUBLIE",
                    List.of(latest.getId()), List.of(latest.getVersion()), latest.getStatus());
        }

        int nextVersion = latestVersion + 1;

        Paa next = new Paa();
        next.setId(latest.getId());
        next.setVersion(nextVersion);

        next.setRef(latest.getRef());
        next.setNom(latest.getNom());
        next.setRemarque(latest.getRemarque());
        next.setAnnee(latest.getAnnee());
        next.setAc(latest.getAc());
        next.setModePassation(latest.getModePassation());
        next.setModelAlfrescoId(latest.getModelAlfrescoId());

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

        List<Realisation> activeRealisations = (latest.getRealisations() == null) ? List.of()
                : latest.getRealisations().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .collect(Collectors.toList());
        next.setRealisations(new ArrayList<>(activeRealisations));

        next.setBudgetEstime(latest.getBudgetEstime());

        Paa saved = repository.save(next);

        for (Realisation r : activeRealisations) {
            if (r.getPaas() == null) r.setPaas(new ArrayList<>());
            boolean alreadyLinked = r.getPaas().stream()
                    .anyMatch(p -> p.getId().equals(saved.getId()) && p.getVersion().equals(saved.getVersion()));
            if (!alreadyLinked) {
                r.getPaas().add(saved);
                realisationRepository.save(r);
            }
        }

        return createFilteredResponsePaa(saved);
    }




}
