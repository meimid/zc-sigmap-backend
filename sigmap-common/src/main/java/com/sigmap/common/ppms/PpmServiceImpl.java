package com.sigmap.common.ppms;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac_seuil.AcSeuil;
import com.sigmap.common.ac_seuil.AcSeuilService;
import com.sigmap.common.ac_seuil.AcSeuilSpecifications;
import com.sigmap.common.com.Com;
import com.sigmap.common.config.Constants;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.financement.Financement;
import com.sigmap.common.financement.FinancementId;
import com.sigmap.common.financement.FinancementRepository;
import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.lots.exception.LotBudgetConsistencyException;
import com.sigmap.common.lots.exception.LotBudgetExceedsRealisationException;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.model_ppm.ModelPpmRepository;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.MonnaieRepository;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaSpecifications;
import com.sigmap.common.paas.exceptions.PaaDefaultStatus;
import com.sigmap.common.ppms.exception.*;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.request.PpmBaseRequestDto;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.RealisationService;
import com.sigmap.common.realisations.exceptions.RealisationBudgetBelowAcSeuilForPpmException;
import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.request.RealisationWithLotsRequestDto;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static jakarta.persistence.criteria.JoinType.LEFT;

@Slf4j
@Service
public class PpmServiceImpl implements PpmService {
    private final PpmRepository repository;
    private final AcRepository acRepository;
    private final RealisationRepository realisationRepository;
    private final RefParamRepository refParamRepository;
    private final DocsRepository docsRepository;
    private final RealisationService realisationService;
    private final LotRepository lotRepository;
    private final AlfrescoService alfrescoService;
    private final DocumentCommuneService documentCommuneService;
    private final DocsService docsService;
    private final AcService acService;
    private final CurrentUserService currentUserService;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final DacRepository dacRepository;
    private final AcSeuilService acSeuilService;
    private final MonnaieRepository monnaieRepository;
    private final FinancementRepository financementRepository;
    private final ModelPpmRepository modelPpmRepository;

    /**
     * Constructor.
     *
     * @param repository             ppm repository
     *                               //TODO
     *                               //     * @param acService ac service
     * @param realisationRepository
     * @param alfrescoService
     * @param documentCommuneService
     * @param docsService
     * @param acService
     */
    public PpmServiceImpl(PpmRepository repository, AcSeuilService acSeuilService, CurrentUserService currentUserService, AcRepository acRepository, RealisationRepository realisationRepository, RefParamRepository refParamRepository, DocsRepository docsRepository, RealisationService realisationService, LotRepository lotRepository, AlfrescoService alfrescoService, DocumentCommuneService documentCommuneService, DocsService docsService, AcService acService, SequenceGeneratorService sequenceGeneratorService, DacRepository dacRepository, MonnaieRepository monnaieRepository, FinancementRepository financementRepository, ModelPpmRepository modelPpmRepository) {
        this.repository = repository;
        this.acRepository = acRepository;
        this.realisationRepository = realisationRepository;
        this.refParamRepository = refParamRepository;
        this.docsRepository = docsRepository;
        this.realisationService = realisationService;
        this.lotRepository = lotRepository;
        this.alfrescoService = alfrescoService;
        this.documentCommuneService = documentCommuneService;
        this.docsService = docsService;
        this.acService = acService;
        this.currentUserService = currentUserService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.dacRepository = dacRepository;
        this.acSeuilService = acSeuilService;
        this.monnaieRepository = monnaieRepository;
        this.financementRepository = financementRepository;
        this.modelPpmRepository = modelPpmRepository;
    }

    @Override
    public List<Ppm> findAll() {
        Specification<Ppm> spec = PpmSpecifications.deleted(false);
        return repository.findAll(spec);
    }

//    @Override
//    public Page<Ppm> findAll(Specification<Ppm> spec, Pageable pageable) {
//        return repository.findAll(spec, pageable);
//    }

    @Override
    public Page<Ppm> findAllDeleted(Specification<Ppm> spec, Pageable pageable) {
        Specification<Ppm> deletedSpec = PpmSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }



    @Override
    public Page<Ppm> findAllDeleted(Pageable pageable) {
        Specification<Ppm> spec = PpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Ppm> findDeletedById(Integer ppmId, Integer ppmVer) {
        Specification<Ppm> specification = PpmSpecifications.id(new CompositeId(ppmId, ppmVer))
                .and(PpmSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Ppm> findOne(Specification<Ppm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Ppm> findById(Integer ppmId, Integer ppmVer)
            throws PpmNotFound, AcNotFound, PpmNotAssociatedWithUserAcException {
        log.info("Recherche du PPM avec ID: {} et version: {}", ppmId, ppmVer);

        // Step 1: Validate input
        if (ppmId == null) {
            log.error("PPM ID est null");
            throw new IllegalArgumentException("PPM ID cannot be null");
        }

        // Step 2: Get user's AC IDs and comIds
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
        log.info("User AC IDs: {}, User comIds: {}", userAcIds, userComIds);

        if ((userAcIds == null || userAcIds.isEmpty()) && (userComIds == null || userComIds.isEmpty())) {
            log.warn("Aucun ID AC ou comId trouvé pour l'utilisateur connecté. userAcIds: {}, userComIds: {}", userAcIds, userComIds);
            throw new AcNotFound("Aucun ID AC ou comId trouvé pour l'utilisateur connecté", Collections.emptyList());
        }

        // Step 3: Validate ACs if any exist
        if (userAcIds != null && !userAcIds.isEmpty()) {
            for (Integer acId : userAcIds) {
                try {
                    log.debug("Validation de l'AC ID: {}", acId);
                    acService.findById(acId);
                } catch (AcNotFound e) {
                    log.warn("AC non trouvé avec ID: {}", acId);
                    throw e;
                }
            }
        }

        // Step 4: Determine the version to use
        Integer targetVersion = ppmVer != null ? ppmVer : repository.findMaxVersionById(ppmId);
        if (targetVersion == null) {
            log.warn("Aucun PPM trouvé pour l'ID: {}", ppmId);
            throw new PpmNotFound("Aucun PPM trouvé pour l'ID", List.of(ppmId),
                    ppmVer != null ? List.of(ppmVer) : Collections.emptyList());
        }
        log.debug("Version cible pour le PPM ID {}: {}", ppmId, targetVersion);

        // Step 5: Retrieve the PPM
        Optional<Ppm> ppmOpt = repository.findById(new CompositeId(ppmId, targetVersion));
        if (ppmOpt.isEmpty()) {
            log.warn("Aucun PPM trouvé pour l'ID: {} et la version: {}", ppmId, targetVersion);
            throw new PpmNotFound("Aucun PPM trouvé pour l'ID et la version", List.of(ppmId), List.of(targetVersion));
        }

        Ppm ppm = ppmOpt.get();
        log.debug("PPM récupéré: ID={}, Version={}, AC={}",
                ppm.getId(), ppm.getVersion(), ppm.getAc() != null ? ppm.getAc().getId() : "null");

        // Step 6: Check if the PPM is deleted
        if (Boolean.TRUE.equals(ppm.getIsDeleted())) {
            log.warn("PPM avec ID {} est marqué comme supprimé", ppmId);
            throw new PpmNotFound("PPM est supprimé", List.of(ppmId), List.of(targetVersion));
        }

        // Step 7: Validate AC or com association
        Integer ppmAcId = ppm.getAc() != null ? ppm.getAc().getId() : null;
        boolean hasMatchingAc = ppmAcId != null && userAcIds != null && userAcIds.contains(ppmAcId);
        boolean hasMatchingCom = false;

        if (ppm.getComs() != null && !ppm.getComs().isEmpty() && userComIds != null && !userComIds.isEmpty()) {
            for (Com com : ppm.getComs()) {
                if (com != null && userComIds.contains(com.getId())) {
                    hasMatchingCom = true;
                    log.debug("Match trouvé pour comId: {} dans ppmId: {}", com.getId(), ppmId);
                    break;
                }
            }
        }

        if (!hasMatchingAc && !hasMatchingCom) {
            log.warn("PPM avec ID {} n'est pas associé à un AC ou comId de l'utilisateur. AC du PPM: {}, ACs de l'utilisateur: {}, comIds de l'utilisateur: {}",
                    ppmId, ppmAcId, userAcIds, userComIds);
            throw new PpmNotAssociatedWithUserAcException(
                    "Le PPM avec l'ID " + ppmId + " n'est pas associé à un AC ou comId de l'utilisateur connecté",
                    List.of(ppmId.longValue()),
                    List.of(targetVersion)
            );
        }
        log.debug("Validation réussie: PPM avec ID {} est associé à un AC ou comId de l'utilisateur", ppmId);

        Integer effectiveVer = targetVersion;
        String modelAlfrescoId = modelPpmRepository
                .findModelAlfrescoId(ppmId, effectiveVer)
                .orElse(null);
        if (modelAlfrescoId != null && !modelAlfrescoId.isBlank()) {
            ppm.setModelAlfrescoId(modelAlfrescoId);
        }

        if (ppm.getRealisations() != null) {
            ppm.getRealisations().forEach(realisation -> {
                if (realisation.getFinancements() != null) {
                    realisation.setFinancements(
                            realisation.getFinancements()
                                    .stream()
                                    .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                                    .collect(java.util.stream.Collectors.toList())
                    );
                }
            });
        }


        Ppm responsePpm = createFilteredResponsePpm(ppm);
        log.info("PPM avec ID {} récupéré avec succès pour l'utilisateur", ppmId);
        return Optional.ofNullable(responsePpm);
    }


    public Optional<Ppm> findLatestById(Integer ppmId) throws PpmNotFound, AcNotFound, PpmNotAssociatedWithUserAcException {
        log.info("Recherche de la dernière version du PPM avec ID: {}", ppmId);
        // Delegate to findById with null version to fetch the latest
        return findById(ppmId, null);
    }
    private Ppm createFilteredResponsePpm(Ppm ppm) {
        if (ppm == null) {
            log.warn("PPM fourni est null");
            return null;
        }

        Ppm filteredPpm = new Ppm();
        filteredPpm.setId(ppm.getId());
        filteredPpm.setVersion(ppm.getVersion());
        filteredPpm.setAc(ppm.getAc());
        filteredPpm.setStatus(ppm.getStatus());
        filteredPpm.setAnnee(ppm.getAnnee());
        filteredPpm.setNom(ppm.getNom());
        filteredPpm.setRef(ppm.getRef());
        filteredPpm.setBudgetEstime(ppm.getBudgetEstime());
        filteredPpm.setModePassation(ppm.getModePassation());
        filteredPpm.setRemarque(ppm.getRemarque());
        filteredPpm.setModelAlfrescoId(ppm.getModelAlfrescoId());
        filteredPpm.setCreatedAt(ppm.getCreatedAt());
        filteredPpm.setCreatedBy(ppm.getCreatedBy());
        filteredPpm.setUpdatedAt(ppm.getUpdatedAt());
        filteredPpm.setUpdatedBy(ppm.getUpdatedBy());
        filteredPpm.setIsDeleted(ppm.getIsDeleted());
        filteredPpm.setDeletedBy(ppm.getDeletedBy());
        filteredPpm.setDeletedAt(ppm.getDeletedAt());
        filteredPpm.setProcessInstanceId(ppm.getProcessInstanceId());
        filteredPpm.setRealisations(ppm.getRealisations());

        if (ppm.getDocs() != null) {
            List<Docs> nonDeletedDocs = ppm.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList());
            filteredPpm.setDocs(nonDeletedDocs);
        }

        return filteredPpm;
    }

    @Override
    public Page<Ppm> findAllForDac(Specification<Ppm> spec, Pageable pageable) {
        log.info("Recherche des PPMs publiés pour DAC avec spécification: {}", spec);

        // Fetch user access scopes
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();

        if ((userAcIds == null || userAcIds.isEmpty()) && (userComIds == null || userComIds.isEmpty())) {
            log.info("Aucun ID AC ou COM trouvé pour l'utilisateur connecté");
            return Page.empty(pageable);
        }

        // Validate ACs (if any)
        if (userAcIds != null && !userAcIds.isEmpty()) {
            for (Integer acId : userAcIds) {
                try {
                    acService.findById(acId);
                } catch (Exception e) {
                    log.info("AC non trouvé avec ID: {}", acId);
                    return Page.empty(pageable);
                }
            }
        }

        // Base specification: non-deleted, latest version, status published
        Specification<Ppm> deletedSpec = (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
        Specification<Ppm> latestVersionSpec = (root, query, cb) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Ppm> subRoot = subquery.from(Ppm.class);
            subquery.select(cb.max(subRoot.get("version")))
                    .where(cb.equal(subRoot.get("id"), root.get("id")));
            return cb.equal(root.get("version"), subquery);
        };
        // Add status = PUBLIE filter
        Specification<Ppm> publishedSpec = (root, query, cb) -> cb.equal(root.get("status"), ProcessStatus.PUBLIE);

        Specification<Ppm> baseSpec = deletedSpec.and(latestVersionSpec).and(publishedSpec);
        if (spec != null) {
            baseSpec = baseSpec.and(spec);
        }

        // Access filters
        Specification<Ppm> acFilterSpec = null;
        if (userAcIds != null && !userAcIds.isEmpty()) {
            acFilterSpec = (root, query, cb) -> cb.in(root.get("ac").get("id")).value(userAcIds);
        }

        Specification<Ppm> comFilterSpec = null;
        if (userComIds != null && !userComIds.isEmpty()) {
            comFilterSpec = (root, query, cb) -> {
                query.distinct(true);
                return root.join("coms", LEFT).get("id").in(userComIds);
            };
        }

        Specification<Ppm> accessSpec;
        if (acFilterSpec != null && comFilterSpec != null) {
            accessSpec = acFilterSpec.or(comFilterSpec);
        } else if (acFilterSpec != null) {
            accessSpec = acFilterSpec;
        } else {
            accessSpec = comFilterSpec;
        }

        Specification<Ppm> combinedSpec = baseSpec.and(accessSpec);

        Page<Ppm> publishedPpms = repository.findAll(combinedSpec, pageable);
        if (publishedPpms.isEmpty()) {
            return Page.empty(pageable);
        }

        // Build a set of realisation keys already attached to a DAC
        List<Object[]> realisationKeysWithDac = dacRepository.findRealisationKeysWithDac();
        Set<String> realisationsWithDac = realisationKeysWithDac.stream()
                .map(o -> o[0] + ":" + o[1])
                .collect(Collectors.toSet());

        // For each PPM, keep only unattached realisations; drop PPMs with none
        List<Ppm> filteredPpms = publishedPpms.getContent().stream()
                .map(ppm -> {
                    Ppm copy = createFilteredResponsePpm(ppm);
                    if (copy.getRealisations() != null) {
                        List<Realisation> unattached = copy.getRealisations().stream()
                                .filter(r -> r != null)
                                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                                .filter(r -> !realisationsWithDac.contains(r.getId() + ":" + r.getVersion()))
                                .collect(Collectors.toList());
                        copy.setRealisations(unattached);
                    }
                    return copy;
                })
                .filter(ppm -> ppm.getRealisations() != null && !ppm.getRealisations().isEmpty())
                .collect(Collectors.toList());

        if (filteredPpms.isEmpty()) {
            return Page.empty(pageable);
        }

        return new PageImpl<>(filteredPpms, pageable, filteredPpms.size());
    }


    @Override
    public Page<Ppm> findAll(Specification<Ppm> spec, Pageable pageable) {
        log.info("Recherche de tous les PPMs avec spécification: {}", spec);

        // Get user's AC IDs and COM IDs
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();

        if ((userAcIds == null || userAcIds.isEmpty()) && (userComIds == null || userComIds.isEmpty())) {
            log.info("Aucun ID AC ou COM trouvé pour l'utilisateur connecté");
            return Page.empty(pageable);
        }

        // Validate ACs
        if (userAcIds != null && !userAcIds.isEmpty()) {
            for (Integer acId : userAcIds) {
                try {
                    acService.findById(acId);
                } catch (Exception e) {
                    log.info("AC non trouvé avec ID: {}", acId);
                    return Page.empty(pageable); // Return empty page instead of throwing AcNotFound
                }
            }
        }

        // Base specification: non-deleted and latest version
        Specification<Ppm> deletedSpec = (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
        Specification<Ppm> latestVersionSpec = (root, query, cb) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Ppm> subRoot = subquery.from(Ppm.class);
            subquery.select(cb.max(subRoot.get("version")))
                    .where(cb.equal(subRoot.get("id"), root.get("id")));
            return cb.equal(root.get("version"), subquery);
        };
        Specification<Ppm> baseSpec = deletedSpec.and(latestVersionSpec);
        if (spec != null) {
            baseSpec = baseSpec.and(spec);
        }

        // Check if any non-deleted PPMs exist
        Page<Ppm> allPpmsWithoutAcFilter = repository.findAll(baseSpec, pageable);
        if (allPpmsWithoutAcFilter.isEmpty()) {
            log.info("Aucun PPM non supprimé trouvé dans la base de données");
            return Page.empty(pageable); // Return empty page instead of throwing PpmNotFound
        }

        // AC filter (only if user has ACs)
        Specification<Ppm> acFilterSpec = null;
        if (userAcIds != null && !userAcIds.isEmpty()) {
            acFilterSpec = (root, query, cb) -> cb.in(root.get("ac").get("id")).value(userAcIds);
        }

        // COM filter via association table (only if user has COMs)
        Specification<Ppm> comFilterSpec = null;
        if (userComIds != null && !userComIds.isEmpty()) {
            comFilterSpec = (root, query, cb) -> {
                // Joining coms; distinct to avoid duplicates
                query.distinct(true);
                return root.join("coms", LEFT)
                        .get("id").in(userComIds);
            };
        }

        // Combine access filters with OR (AC OR COM)
        Specification<Ppm> accessSpec;
        if (acFilterSpec != null && comFilterSpec != null) {
            accessSpec = acFilterSpec.or(comFilterSpec);
        } else if (acFilterSpec != null) {
            accessSpec = acFilterSpec;
        } else {
            accessSpec = comFilterSpec; // user has only COMs
        }

        Specification<Ppm> combinedSpec = baseSpec.and(accessSpec);
        Page<Ppm> allPpms = repository.findAll(combinedSpec, pageable);

        if (allPpms.isEmpty()) {
            log.info("Aucun PPM associé aux ACs/COMs de l'utilisateur. ACs: {}, COMs: {}", userAcIds, userComIds);
            return Page.empty(pageable);
        }

        // Map the results to filter Docs
        Page<Ppm> ppmResponse = allPpms.map(this::createFilteredResponsePpm);
        log.info("Récupération de {} PPMs pour l'utilisateur. ACs: {}, COMs: {}",
                ppmResponse.getContent().size(), userAcIds, userComIds);

        return ppmResponse;
    }


    @Override
    public Ppm save(Ppm ppm) {
        return repository.save(ppm);
    }

    @Transactional
    @Override
    public Ppm create(PpmRequest request) {
        Integer id = request.getId();
        if (id == null) {
//            Integer maxId = repository.findMaxId();
            Integer maxId = sequenceGeneratorService.getNextId(Constants.PPM_ID_SEQ);
            id = (maxId != null) ? maxId + 1 : 1;
            request.setId(id);
        }
        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        Ppm ppm = new Ppm();
        ppm.setId(id);
        ppm.setVersion(version);
//        pos(request.getRef());
        ppm.setNom(request.getNom());
        ppm.setBudgetEstime(request.getBudgetEstime());
        setRef(ppm);



        // Set empty realisations and docs initially
        ppm.setRealisations(new ArrayList<>());
        ppm.setDocs(new ArrayList<>());

        // Save Ppm without associations
        Ppm savedPpm = repository.save(ppm);

        // Create and set realisations if provided
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            List<Realisation> realisations = new ArrayList<>();

            for (RealisationRequest realisationRequest : request.getRealisations()) {
                // Create a new Realisation
                Realisation realisation = new Realisation();

                // Generate ID if not provided
                Integer realisationId = realisationRequest.getId();
                if (realisationId == null) {
//                    Integer maxRealisationId = realisationRepository.findMaxId();
                    Integer maxRealisationId =  sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
                    realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;
                }

                // Always use version 1 for newly created realisations
                realisation.setId(realisationId);
                realisation.setVersion(1);

                // Set all fields from the request
                realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                realisation.setBeneficiaire(realisationRequest.getBeneficiaire());
//                realisation.setService(realisationRequest.getService());
//                realisation.setDirection(realisationRequest.getDirection());
//                realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
//                realisation.setMonais(realisationRequest.getMonais());
                realisation.setMonnaie(realisationRequest.getMonnaie());
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

                // Set up the relationship with PPM
                List<Ppm> ppmList = new ArrayList<>();
                ppmList.add(savedPpm);
                realisation.setPpms(ppmList);

                // Handle Lots if provided
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    List<Lot> lots = new ArrayList<>();

                    for (LotResponse lotRequest : realisationRequest.getLots()) {
                        Lot lot = new Lot();

                        // Generate ID if not provided
                        Integer lotId = lotRequest.getId();
                        if (lotId == null) {
//                            Integer maxLotId = lotRepository.findMaxId();
                            Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                            lotId = (maxLotId != null) ? maxLotId + 1 : 1;
                        }

                        // Always use version 1 for newly created lots
                        lot.setId(lotId);
                        lot.setVersion(1);
                        lot.setDescription(lotRequest.getDescription());

                        // Save the lot
                        lot = lotRepository.save(lot);
                        lots.add(lot);
                    }

                    realisation.setLots(lots);
                }

                // Save the realisation
                realisation = realisationRepository.save(realisation);
                realisations.add(realisation);
            }

            // Update the PPM with the list of realisations
            savedPpm.setRealisations(realisations);
        }

        // Save again to persist associations
        return repository.save(savedPpm);
    }

    @Override
    public Ppm update(Integer ppmId, Integer ppmVer, PpmRequest request) {
        return update(ppmId, ppmVer, request, false);
    }
    @Transactional
    @Override
    public Ppm update(Integer ppmId, Integer ppmVer, PpmRequest request, boolean isUpdatable) {
        // Find the existing PPM
        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        Optional<Ppm> existingPpmOpt = repository.findById(compositeId);
        if (existingPpmOpt.isEmpty()) {
            throw new RuntimeException("Ppm with ID " + ppmId + " and version " + ppmVer + " was not found in the database.");
        }

        Ppm existingPpm = existingPpmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            throw new RuntimeException("Cannot update a deleted Ppm with ID " + ppmId + " and version " + ppmVer);
        }

        // Determine acId for seuilApplicable
        Integer requestAcId = request.getAcId() != null ? request.getAcId() : existingPpm.getAc().getId();

        if (isUpdatable) {
            // Create a new version
            Ppm newPpm = new Ppm();
            newPpm.setId(ppmId);
            newPpm.setVersion(ppmVer + 1); // Increment version
            // Set fields from PpmRequest, falling back to existing values if null
            newPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());
            newPpm.setAnnee(existingPpm.getAnnee());
            newPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());
            newPpm.setIsDeleted(false);
            newPpm.setRemarque(request.getRemarque() != null ? request.getRemarque() : existingPpm.getRemarque());
            newPpm.setStatus(existingPpm.getStatus()); // Preserve existing status

            if (request.getModePassation() != null) {
                newPpm.setModePassation(refParamRepository.findById(request.getModePassation().getId())
                        .orElseThrow(() -> new RuntimeException("Status with ID " + request.getModePassation().getId() + " not found")));
            } else {
                newPpm.setModePassation(existingPpm.getModePassation()); // Preserve existing status
            }

            if (request.getAcId() != null) {
                Ac ac = acRepository.findById(request.getAcId())
                        .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
                newPpm.setAc(ac);
            } else {
                newPpm.setAc(existingPpm.getAc()); // Preserve existing Ac
            }

            // Copy existing realisations to the new PPM
            newPpm.setRealisations(existingPpm.getRealisations() != null ? new ArrayList<>(existingPpm.getRealisations()) : new ArrayList<>());

            // === ADDED: recompute budgetEstime per Realisation from its financements, then apply PPM seuil control
            if (newPpm.getRealisations() != null) {
                for (Realisation realisation : newPpm.getRealisations()) {
                    // Recompute budget from non-deleted financements
                    BigDecimal rb = (realisation.getFinancements() == null) ? BigDecimal.ZERO
                            : realisation.getFinancements().stream()
                            .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                            .map(f -> f.getBudgetEstimer() != null ? f.getBudgetEstimer() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    realisation.setBudgetEstime(rb);

                    // PPM-specific seuil control (throws if budget < seuil)
                    setSeuilApplicableForRealisationInPpm(realisation, requestAcId);
                }
            }

            // Validate realisations for budget consistency
            validateRealisations(newPpm);

            return repository.save(newPpm);
        } else {
            // Update existing version
            existingPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());
            existingPpm.setAnnee(existingPpm.getAnnee());
            existingPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());
            existingPpm.setRemarque(request.getRemarque() != null ? request.getRemarque() : existingPpm.getRemarque());
            existingPpm.setStatus(existingPpm.getStatus()); // Preserve existing status

            if (request.getModePassation() != null) {
                existingPpm.setModePassation(refParamRepository.findById(request.getModePassation().getId())
                        .orElseThrow(() -> new RuntimeException("Status with ID " + request.getModePassation().getId() + " not found")));
            } else {
                existingPpm.setModePassation(existingPpm.getModePassation()); // Preserve existing status
            }

            if (request.getAcId() != null) {
                Ac ac = acRepository.findById(request.getAcId())
                        .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
                existingPpm.setAc(ac);
            } else {
                existingPpm.setAc(existingPpm.getAc()); // Preserve existing Ac
            }

            // === ADDED: recompute budgetEstime per Realisation from its financements, then apply PPM seuil control
            if (existingPpm.getRealisations() != null) {
                for (Realisation realisation : existingPpm.getRealisations()) {
                    // Recompute budget from non-deleted financements
                    BigDecimal rb = (realisation.getFinancements() == null) ? BigDecimal.ZERO
                            : realisation.getFinancements().stream()
                            .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                            .map(f -> f.getBudgetEstimer() != null ? f.getBudgetEstimer() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    realisation.setBudgetEstime(rb);

                    // PPM-specific seuil control (throws if budget < seuil)
                    setSeuilApplicableForRealisationInPpm(realisation, requestAcId);
                }
            }

            // Validate realisations for budget consistency
            validateRealisations(existingPpm);

            return repository.save(existingPpm);
        }
    }

    @Override
    public void delete(Integer ppmId, Integer ppmVer) {
        // Fetch the PPM entity
        Optional<Ppm> ppmOptional = repository.findById(new CompositeId(ppmId, ppmVer));
        if (ppmOptional.isEmpty()) {
            throw new RuntimeException("PPM not found with ID: " + ppmId + " and version: " + ppmVer);
        }

        Ppm ppm = ppmOptional.get();

        // Check if the PPM is in draft status
        if (ppm.getStatus() == null || ppm.getStatus() != ProcessStatus.BROUILLON ) {
            throw new IllegalStateException("PPM can only be deleted in draft status. Current status ID: " +
                    (ppm.getStatus() != null ? ppm.getStatus() : "null"));
        }

        // Proceed with soft deletion
        repository.softDelete(ppmId, ppmVer);
    }


    @Override
    public Ppm updateStatus(Integer ppmId, Integer ppmVer, ProcessStatus status) throws PpmNotFound, AcNotFound {
        Optional<Ppm> ppmOpt = findById(ppmId, ppmVer);
        if (ppmOpt.isEmpty()) {
            return null;
        }

        Ppm existingPpm = ppmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            return null;
        }

        log.info("Updating status with ID: " + ppmId + " and version: " + ppmVer + " with status: " + status);
        existingPpm.setStatus(status);
        return repository.save(existingPpm);

    }

    /**
     * Updates a PPM by creating a new version with the updated data.
     * The old version is preserved in the database.
     * <p>
     * This method handles hierarchical versioning:
     * - If any PPM field changes, a new PPM version is created
     * - If any document is added, modified, or removed, a new PPM version is created
     * - If any Realisation is added, modified, or removed, a new PPM version is created
     * - If a Lot within a Realisation changes, both the Realisation and PPM versions are incremented
     * <p>
     * This method handles various Realisation scenarios:
     * - Existing Realisation with ID and version: uses that specific version
     * - Existing Realisation with only ID: finds and uses the latest version
     * - New Realisation without ID: creates a new Realisation
     *
     * @param ppmId the ID of the PPM to update
     * @param ppmVer the current version of the PPM
     * @param request the request containing the updated data
     * @return the newly created version of the PPM
     * @throws RuntimeException if the PPM is not found or is deleted
     */
//    @Transactional
//    public Ppm updateWithVersioning(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
//        // Log the request for debugging
//        logUpdateRequest(ppmId, ppmVer, request);
//        if (request.getRealisations() != null) {
//            for (RealisationBaseRequestDto re : request.getRealisations()) {
//                System.out.println("doc realisation" + re.getRealisationFiles());
//            }
//        }
//
//        // Validate lot budget consistency in the request if it's a PpmWithLotsRequestDto
//        if (request instanceof PpmWithLotsRequestDto) {
//            validateRequestLotsConsistency((PpmWithLotsRequestDto) request);
//        }
//
//        // Get the existing PPM
//        Ppm existingPpm = getExistingPpm(ppmId, ppmVer);
//
//        // Determine acId for seuilApplicable
//        Integer requestAcId = request.getAcId() != null
//                ? request.getAcId()
//                : (existingPpm.getAc() != null ? existingPpm.getAc().getId() : null);
//
//        // Create a temporary PPM with the new data for comparison
//        Ppm tempPpm = buildTempPpm(existingPpm, request);
//
//        // Check if the direct PPM fields have changed (do not include auto-set annee/budget here)
//        boolean directFieldsChanged = !arePpmFieldsIdentical(existingPpm, tempPpm);
//        System.out.println("Direct field changes detected: " + directFieldsChanged);
//
//        // --- Realisations (scalars + lots) handled ONLY here ---
//        List<Realisation> updatedRealisations = new ArrayList<>();
//        boolean realisationsPhaseChanged = processRealisationsWithLotLogic(existingPpm, request, updatedRealisations);
//
//        // Build request list for later matching (used by docs/files + financements)
//        List<RealisationBaseRequestDto> reqReals = null;
//        if (request instanceof PpmWithLotsRequestDto) {
//            PpmWithLotsRequestDto lotsReq = (PpmWithLotsRequestDto) request;
//            if (lotsReq.getRealisationsWithLots() != null && !lotsReq.getRealisationsWithLots().isEmpty()) {
//                reqReals = new ArrayList<>(lotsReq.getRealisationsWithLots());
//            } else if (request.getRealisations() != null) {
//                reqReals = new ArrayList<>(request.getRealisations());
//            }
//        } else if (request.getRealisations() != null) {
//            reqReals = new ArrayList<>(request.getRealisations());
//        }
//
//        // --- Docs & files sync ONLY (NO LOT TOUCHING) ---
//        if (reqReals != null && !reqReals.isEmpty()) {
//            for (int i = 0; i < updatedRealisations.size(); i++) {
//                Realisation real = updatedRealisations.get(i);
//                RealisationBaseRequestDto realReq = (i < reqReals.size()) ? reqReals.get(i) : null;
//                if (realReq == null) continue;
//
//                // ----- Docs association sync -----
//                if (realReq.getDocIds() != null && !realReq.getDocIds().isEmpty()) {
//                    Set<Integer> requestDocIds = new HashSet<>(realReq.getDocIds());
//                    Set<Integer> existingDocIds = real.getDocs() != null
//                            ? real.getDocs().stream().map(Docs::getId).collect(Collectors.toSet())
//                            : new HashSet<>();
//
//                    Set<Integer> docsToAdd = new HashSet<>(requestDocIds);
//                    docsToAdd.removeAll(existingDocIds);
//
//                    Set<Integer> docsToRemove = new HashSet<>(existingDocIds);
//                    docsToRemove.removeAll(requestDocIds);
//
//                    List<Docs> docs = real.getDocs() != null ? new ArrayList<>(real.getDocs()) : new ArrayList<>();
//
//                    if (!docsToRemove.isEmpty()) {
//                        docs.removeIf(doc -> docsToRemove.contains(doc.getId()));
//                        final Realisation realRef = real;
//                        for (Integer docId : docsToRemove) {
//                            docsRepository.findById(docId).ifPresent(doc -> {
//                                if (doc.getRealisations() != null) {
//                                    doc.getRealisations().remove(realRef);
//                                    docsRepository.save(doc);
//                                }
//                            });
//                        }
//                    }
//
//                    for (Integer docId : docsToAdd) {
//                        Docs doc = docsRepository.findById(docId)
//                                .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));
//                        if (doc.getRealisations() == null) doc.setRealisations(new ArrayList<>());
//                        if (!doc.getRealisations().contains(real)) doc.getRealisations().add(real);
//                        doc.setIsDeleted(false);
//                        docsRepository.save(doc);
//                        docs.add(doc);
//                    }
//
//                    real.setDocs(docs);
//                    real = realisationRepository.save(real);
//                    System.out.println("Updated docs for realisation ID " + real.getId() + ": " + docs.size() + " total docs");
//                } else if (real.getDocs() != null && !real.getDocs().isEmpty()) {
//                    for (Docs doc : real.getDocs()) {
//                        if (doc.getRealisations() != null) {
//                            doc.getRealisations().remove(real);
//                            docsRepository.save(doc);
//                        }
//                    }
//                    real.setDocs(new ArrayList<>());
//                    real = realisationRepository.save(real);
//                    System.out.println("Cleared docs for realisation ID " + real.getId());
//                }
//
//                // ----- File uploads per realisation -----
//                if (realReq.getRealisationFiles() != null && !realReq.getRealisationFiles().isEmpty()) {
//                    try {
//                        System.out.println("Processing " + realReq.getRealisationFiles().size() +
//                                " files for realisation ID: " + real.getId());
//
//                        documentCommuneService.processAndSaveDocuments(
//                                realReq.getRealisationFiles(),
//                                real,
//                                Constants.PATH_ENTREPOT_GED_PPM + "/" + existingPpm.getId() + Constants.PATH_ENTREPOT_GED_REALISATION
//                        );
//
//                        real = realisationRepository.findById(new CompositeId(real.getId(), real.getVersion()))
//                                .orElseThrow(() -> new RuntimeException("Could not find realisation after file upload"));
//
//                        if (real.getDocs() != null && !real.getDocs().isEmpty()) {
//                            for (Docs d : real.getDocs()) {
//                                if (d.getRealisations() == null) d.setRealisations(new ArrayList<>());
//                                if (!d.getRealisations().contains(real)) d.getRealisations().add(real);
//                                if (d.getIsDeleted() == null || Boolean.TRUE.equals(d.getIsDeleted())) d.setIsDeleted(false);
//                                docsRepository.save(d);
//                            }
//                            real = realisationRepository.save(real);
//                        }
//
//                        System.out.println("After file processing, realisation has " +
//                                (real.getDocs() != null ? real.getDocs().size() : 0) + " documents");
//                    } catch (IOException e) {
//                        System.err.println("Error processing realisation files: " + e.getMessage());
//                        e.printStackTrace();
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        }
//
//        // (Intentionally removed) Secondary lots re-sync. All lot creation/versioning is in processRealisationsWithLotLogic.
//        System.out.println("Skipping secondary lots re-sync (disabled by design).");
//
//        // --- Financements (AFTER lots/version bump) ---
//        boolean financementsChanged = false;
//
//        for (int i = 0; i < updatedRealisations.size(); i++) {
//            Realisation real = updatedRealisations.get(i);
//
//            // Match request DTO for THIS realisation (by ID, else fallback by index)
//            RealisationBaseRequestDto reqDto = null;
//
//            if (request instanceof PpmWithLotsRequestDto) {
//                PpmWithLotsRequestDto wr = (PpmWithLotsRequestDto) request;
//                if (wr.getRealisationsWithLots() != null) {
//                    for (RealisationWithLotsRequestDto d : wr.getRealisationsWithLots()) {
//                        if (d.getId() != null && d.getId().equals(real.getId())) { reqDto = d; break; }
//                    }
//                }
//            }
//            if (reqDto == null && request.getRealisations() != null) {
//                for (RealisationBaseRequestDto d : request.getRealisations()) {
//                    if (d.getId() != null && d.getId().equals(real.getId())) { reqDto = d; break; }
//                }
//            }
//            if (reqDto == null && reqReals != null && i < reqReals.size()) {
//                reqDto = reqReals.get(i);
//            }
//
//            // Snapshot BEFORE (active, not deleted)
//            List<Financement> before = financementRepository
//                    .findByRealisationIdAndVersion(real.getId(), real.getVersion())
//                    .stream().filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
//                    .collect(Collectors.toList());
//
//            List<FinancementRequest> finReqs = (reqDto != null) ? reqDto.getFinancements() : null;
//
//            if (finReqs == null) {
//                // null ⇒ delete-all on this version
//                List<Financement> existingAll = financementRepository.findByRealisationIdAndVersion(real.getId(), real.getVersion());
//                boolean any = false;
//                for (Financement f : existingAll) {
//                    if (!Boolean.TRUE.equals(f.getIsDeleted())) { f.setIsDeleted(true); any = true; }
//                }
//                if (any) {
//                    financementRepository.saveAll(existingAll);
//                }
//            } else {
//                System.out.println("=== PROCESSING FINANCEMENTS (existing version) ===");
//                System.out.println("Realisation ID: " + real.getId() + ", Version: " + real.getVersion());
//                processFinancementsOnExistingVersion(real, reqDto);
//                System.out.println("Processed " + finReqs.size() + " financements for realisation " + real.getId() + " v" + real.getVersion());
//                System.out.println("=== FINANCEMENTS DONE ===");
//
//                // Mark missing financements as deleted (by ID when present, else by signature)
//                Set<Integer> requestedIds = finReqs.stream()
//                        .filter(fr -> fr.getFinancementId() != null)
//                        .map(FinancementRequest::getFinancementId)
//                        .collect(Collectors.toSet());
//
//                Function<FinancementRequest, String> reqSig = fr ->
//                        (fr.getSourceFinancement() != null ? String.valueOf(fr.getSourceFinancement().getId()) : "null")
//                                + "|" + (fr.getMonnaieId() != null ? String.valueOf(fr.getMonnaieId()) : "null")
//                                + "|" + (fr.getBudgetEstimer() != null ? fr.getBudgetEstimer().toPlainString() : "0");
//
//                Set<String> requestedSigs = finReqs.stream().map(reqSig).collect(Collectors.toSet());
//
//                List<Financement> existingThisVersion = financementRepository.findByRealisationIdAndVersion(real.getId(), real.getVersion());
//                boolean anyDeleted = false;
//                for (Financement f : existingThisVersion) {
//                    Integer fid = (f.getId() != null) ? f.getId().getFinancementId() : null;
//                    String fSig =
//                            (f.getSourceFinancement() != null ? String.valueOf(f.getSourceFinancement().getId()) : "null")
//                                    + "|" + (f.getMonnaie() != null ? String.valueOf(f.getMonnaie().getId()) : "null")
//                                    + "|" + (f.getBudgetEstimer() != null ? f.getBudgetEstimer().toPlainString() : "0");
//
//                    boolean keep = (fid != null && requestedIds.contains(fid)) || requestedSigs.contains(fSig);
//                    if (!keep && !Boolean.TRUE.equals(f.getIsDeleted())) {
//                        f.setIsDeleted(true);
//                        anyDeleted = true;
//                    }
//                }
//                if (anyDeleted) {
//                    financementRepository.saveAll(existingThisVersion);
//                }
//            }
//
//            // Snapshot AFTER (active)
//            List<Financement> after = financementRepository
//                    .findByRealisationIdAndVersion(real.getId(), real.getVersion())
//                    .stream().filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
//                    .collect(Collectors.toList());
//
//            boolean changedThis =
//                    (finReqs == null)
//                            || before.size() != after.size()
//                            || !after.stream().allMatch(a -> before.stream().anyMatch(b ->
//                            Objects.equals(a.getId().getFinancementId(), b.getId().getFinancementId()) &&
//                                    Objects.equals(a.getBudgetEstimer(), b.getBudgetEstimer()) &&
//                                    Objects.equals(a.getMonnaie() != null ? a.getMonnaie().getId() : null,
//                                            b.getMonnaie() != null ? b.getMonnaie().getId() : null) &&
//                                    Objects.equals(a.getSourceFinancement() != null ? a.getSourceFinancement().getId() : null,
//                                            b.getSourceFinancement() != null ? b.getSourceFinancement().getId() : null)
//                    ));
//
//            financementsChanged |= changedThis;
//
//            // Recompute budget from financements (default taux=1.0 if null/zero)
//            BigDecimal rb = after.stream()
//                    .map(f -> {
//                        BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
//                        Double raw = (f.getMonnaie() != null) ? f.getMonnaie().getTaux() : null;
//                        double taux = (raw != null && raw > 0.0) ? raw : 1.0;
//                        return amt.multiply(BigDecimal.valueOf(taux));
//                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
//            real.setBudgetEstime(rb);
//
//            // Re-apply seuil
//            setSeuilApplicableForRealisationInPpm(real, requestAcId);
//        }
//
//        boolean realisationsChanged = realisationsPhaseChanged || financementsChanged;
//
//        // --- PPM documents (root) ---
//        List<Docs> updatedDocs = new ArrayList<>();
//        boolean documentsChanged = processDocuments(existingPpm, request, updatedDocs);
//
//        // --- Request hash (dedupe) ---
//        String requestHash = generateRequestHash(request);
//        String previousRequestHash = getPreviousRequestHash(ppmId, ppmVer);
//
//        boolean hasFiles = false;
//        if (request instanceof PpmWithLotsRequestDto) {
//            PpmWithLotsRequestDto lotsRequest = (PpmWithLotsRequestDto) request;
//            hasFiles = lotsRequest.getPpmFiles() != null && !lotsRequest.getPpmFiles().isEmpty();
//
//            if (!hasFiles && lotsRequest.getRealisationsWithLots() != null) {
//                for (RealisationWithLotsRequestDto realisation : lotsRequest.getRealisationsWithLots()) {
//                    if (realisation.getRealisationFiles() != null && !realisation.getRealisationFiles().isEmpty()) {
//                        hasFiles = true; break;
//                    }
//                }
//            }
//        }
//
//        // DEDUPE GUARD (only when absolutely no change)
//        System.out.printf(
//                "DEDUPE GUARD: hasFiles=%s, direct=%s, realisations=%s, docs=%s%n",
//                hasFiles, directFieldsChanged, realisationsChanged, documentsChanged
//        );
//
//        if (!hasFiles && !directFieldsChanged && !realisationsChanged && !documentsChanged) {
//            System.out.println("Current request hash: " + requestHash);
//            System.out.println("Previous request hash: " + previousRequestHash);
//
//            if (requestHash != null && requestHash.equals(previousRequestHash)) {
//                System.out.println("Duplicate request detected (no changes + same hash), returning existing PPM without changes");
//
//                // Create a filtered copy for response
//                Ppm copyForResponse = new Ppm();
//                BeanUtils.copyProperties(existingPpm, copyForResponse);
//
//                // Filter out deleted documents for the response
//                if (existingPpm.getDocs() != null) {
//                    copyForResponse.setDocs(
//                            existingPpm.getDocs().stream()
//                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
//                                    .collect(Collectors.toList())
//                    );
//                }
//
//                // Also filter realisation documents and lots
//                if (copyForResponse.getRealisations() != null) {
//                    for (Realisation real : copyForResponse.getRealisations()) {
//                        if (real.getDocs() != null) {
//                            real.setDocs(
//                                    real.getDocs().stream()
//                                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
//                                            .collect(Collectors.toList())
//                            );
//                        }
//
//                        if (real.getLots() != null) {
//                            real.setLots(
//                                    real.getLots().stream()
//                                            .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
//                                            .collect(Collectors.toList())
//                            );
//                        }
//
//                        if (real.getSeuilApplicable() != null && real.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
//                            log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", real.getId());
//                            real.setSeuilApplicable(BigDecimal.ZERO);
//                        }
//                    }
//                }
//
//                return copyForResponse;
//            }
//        }
//
//        boolean shouldCreateNewVersion = directFieldsChanged || realisationsChanged || documentsChanged || hasFiles;
//
//        // Detailed inputs for decision
//        System.out.println("Versioning decision inputs:");
//        System.out.println("  - Direct changes: " + directFieldsChanged);
//        System.out.println("  - Realisations changed: " + realisationsChanged + " (phase=" + realisationsPhaseChanged + ", financements=" + financementsChanged + ")");
//        System.out.println("  - Documents changed: " + documentsChanged);
//        System.out.println("  - Has files: " + hasFiles);
//
//        logVersionDecision(shouldCreateNewVersion, directFieldsChanged, realisationsChanged, documentsChanged);
//
//        if (!shouldCreateNewVersion) {
//            System.out.println("No changes detected, returning existing PPM without creating new version");
//            return existingPpm;
//        }
//
//        // We will create a new version; keep request hash for this source version
//        storePreviousRequestHash(ppmId, ppmVer, requestHash);
//
//        // ---- Rebuild root docs for the new version ----
//        List<Docs> rootDocsForNewVersion;
//        if (request.getDocIds() != null) {
//            rootDocsForNewVersion = new ArrayList<>();
//            Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());
//
//            if (existingPpm.getDocs() != null) {
//                for (Docs existingDoc : existingPpm.getDocs()) {
//                    Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
//                    if (docToProcess == null) {
//                        System.out.println("Warning: Document " + existingDoc.getId() + " no longer exists in database, skipping");
//                        continue;
//                    }
//                    if (requestedDocIds.contains(docToProcess.getId())) {
//                        docToProcess.setIsDeleted(false);
//                        docToProcess = docsRepository.save(docToProcess);
//                        rootDocsForNewVersion.add(docToProcess);
//                        requestedDocIds.remove(docToProcess.getId());
//                    } else {
//                        docToProcess.setIsDeleted(true);
//                        docToProcess = docsRepository.save(docToProcess);
//                        rootDocsForNewVersion.add(docToProcess);
//                    }
//                }
//            }
//
//            for (Integer docId : requestedDocIds) {
//                if (rootDocsForNewVersion.stream().anyMatch(doc -> doc.getId().equals(docId))) {
//                    continue;
//                }
//                Docs doc = docsRepository.findById(docId)
//                        .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
//                doc.setIsDeleted(false);
//                doc = docsRepository.save(doc);
//                rootDocsForNewVersion.add(doc);
//            }
//        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
//            rootDocsForNewVersion = new ArrayList<>();
//            for (Docs existingDoc : existingPpm.getDocs()) {
//                Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
//                if (docToProcess == null) continue;
//                docToProcess.setIsDeleted(true);
//                docToProcess = docsRepository.save(docToProcess);
//                rootDocsForNewVersion.add(docToProcess);
//                System.out.println("Marked document with ID " + docToProcess.getId() + " as deleted (no docIds provided)");
//            }
//        } else {
//            rootDocsForNewVersion = new ArrayList<>();
//        }
//
//        // Final validation before persisting new PPM
//        Ppm tempPpmForValidation = new Ppm();
//        tempPpmForValidation.setRealisations(updatedRealisations);
//        validateRealisations(tempPpmForValidation);
//
//        System.out.println("Creating new PPM version: " + (ppmVer + 1));
//        Ppm newPpm = createAndSaveNewPpmVersion(ppmId, ppmVer, tempPpm, updatedRealisations, rootDocsForNewVersion, existingPpm);
//
//        // Carry over persistent fields
//        newPpm.setRef(existingPpm.getRef());
//        newPpm.setProcessInstanceId(existingPpm.getProcessInstanceId());
//
//        // Set PPM annee and budgetEstime after versioning decision
//        BigDecimal ppmBudgetFromRealisations = (updatedRealisations == null) ? BigDecimal.ZERO
//                : updatedRealisations.stream()
//                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
//                .map(r -> r.getBudgetEstime() != null ? r.getBudgetEstime() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        newPpm.setAnnee(existingPpm.getAnnee());
//        newPpm.setBudgetEstime(ppmBudgetFromRealisations);
//        newPpm = repository.save(newPpm);
//
//        System.out.println("PPM post-versioning fields set: annee=" + newPpm.getAnnee()
//                + ", budgetEstime=" + newPpm.getBudgetEstime()
//                + " (computed from " + (updatedRealisations != null ? updatedRealisations.size() : 0) + " realisations)");
//
//        // PPM-level files
//        if (request instanceof PpmWithLotsRequestDto) {
//            PpmWithLotsRequestDto lotsRequest = (PpmWithLotsRequestDto) request;
//            if (lotsRequest.getPpmFiles() != null && !lotsRequest.getPpmFiles().isEmpty()) {
//                try {
//                    System.out.println("Processing " + lotsRequest.getPpmFiles().size() + " new files for PPM");
//                    documentCommuneService.processAndSaveDocuments(lotsRequest.getPpmFiles(), newPpm, Constants.PATH_ENTREPOT_GED_PPM);
//                    newPpm = repository.save(newPpm);
//
//                    if (newPpm.getDocs() != null && !newPpm.getDocs().isEmpty()) {
//                        for (Docs d : newPpm.getDocs()) {
//                            if (d.getPpms() == null) d.setPpms(new ArrayList<>());
//                            if (!d.getPpms().contains(newPpm)) d.getPpms().add(newPpm);
//                            if (d.getIsDeleted() == null || Boolean.TRUE.equals(d.getIsDeleted())) d.setIsDeleted(false);
//                            docsRepository.save(d);
//                        }
//                        newPpm = repository.save(newPpm);
//                    }
//                } catch (IOException e) {
//                    System.err.println("Error processing PPM files: " + e.getMessage());
//                    throw new RuntimeException("Error processing PPM files: " + e.getMessage(), e);
//                }
//            }
//        }
//
//        // Store hash for the new version so an identical replay can be deduped cleanly next time
//        storePreviousRequestHash(ppmId, newPpm.getVersion(), requestHash);
//
//        // Prepare response copy (filter deleted docs/lots)
//        Ppm responseVersion = new Ppm();
//        BeanUtils.copyProperties(newPpm, responseVersion);
//
//        if (responseVersion.getDocs() != null) {
//            List<Docs> filteredDocs = newPpm.getDocs() == null ? Collections.emptyList()
//                    : newPpm.getDocs().stream()
//                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
//                    .collect(Collectors.toList());
//
//            System.out.println("Filtered docs for response: " + filteredDocs.size() + " docs (from " + (newPpm.getDocs() != null ? newPpm.getDocs().size() : 0) + " total)");
//            System.out.println("Filtered doc IDs: " + filteredDocs.stream().map(Docs::getId).collect(Collectors.toList()));
//
//            responseVersion.setDocs(filteredDocs);
//        }
//
//        if (responseVersion.getRealisations() != null) {
//            for (Realisation real : responseVersion.getRealisations()) {
//                if (real.getDocs() != null) {
//                    real.setDocs(
//                            real.getDocs().stream()
//                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
//                                    .collect(Collectors.toList())
//                    );
//                }
//
//                if (real.getLots() != null) {
//                    real.setLots(
//                            real.getLots().stream()
//                                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
//                                    .collect(Collectors.toList())
//                    );
//                }
//
//                if (real.getSeuilApplicable() != null && real.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
//                    log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", real.getId());
//                    real.setSeuilApplicable(BigDecimal.ZERO);
//                }
//            }
//        }
//
//        if (responseVersion.getRealisations() != null && !responseVersion.getRealisations().isEmpty()) {
//            List<Realisation> latestRealisations = filterLatestRealisationVersions(responseVersion.getRealisations());
//            responseVersion.setRealisations(latestRealisations);
//            System.out.println("FINAL FILTER: Showing only latest versions");
//            for (Realisation r : latestRealisations) {
//                System.out.println("  - Final version: Realisation ID: " + r.getId() + ", Version: " + r.getVersion());
//            }
//        }
//
//        System.out.println("Returning filtered PPM version: " + responseVersion.getVersion());
//        System.out.println("Returning filtered new PPM version: " + responseVersion.getVersion());
//        return responseVersion;
//    }

    @Transactional
    public Ppm updateWithVersioning(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
        // Log the request for debugging
        logUpdateRequest(ppmId, ppmVer, request);
        if (request.getRealisations() != null) {
            for (RealisationBaseRequestDto re : request.getRealisations()) {
                System.out.println("doc realisation" + re.getRealisationFiles());
            }
        }

        if (request instanceof PpmWithLotsRequestDto) {
            validateRequestLotsConsistency((PpmWithLotsRequestDto) request);
        }

        Ppm existingPpm = getExistingPpm(ppmId, ppmVer);

        Integer requestAcId = request.getAcId() != null
                ? request.getAcId()
                : (existingPpm.getAc() != null ? existingPpm.getAc().getId() : null);

        Ppm tempPpm = buildTempPpm(existingPpm, request);

        boolean directFieldsChanged = !arePpmFieldsIdentical(existingPpm, tempPpm);
        System.out.println("Direct field changes detected: " + directFieldsChanged);

        // --- Realisations (scalars + lots) handled ONLY here ---
        List<Realisation> updatedRealisations = new ArrayList<>();
        boolean realisationsPhaseChanged = processRealisationsWithLotLogic(existingPpm, request, updatedRealisations);

        // Build request list for later matching (used by docs/files + financements)
        List<RealisationBaseRequestDto> reqReals = null;
        if (request instanceof PpmWithLotsRequestDto) {
            PpmWithLotsRequestDto lotsReq = (PpmWithLotsRequestDto) request;
            if (lotsReq.getRealisationsWithLots() != null && !lotsReq.getRealisationsWithLots().isEmpty()) {
                reqReals = new ArrayList<>(lotsReq.getRealisationsWithLots());
            } else if (request.getRealisations() != null) {
                reqReals = new ArrayList<>(request.getRealisations());
            }
        } else if (request.getRealisations() != null) {
            reqReals = new ArrayList<>(request.getRealisations());
        }

        // --- Docs & files sync ONLY (NO LOT TOUCHING) ---
        if (reqReals != null && !reqReals.isEmpty()) {
            for (int i = 0; i < updatedRealisations.size(); i++) {
                Realisation real = updatedRealisations.get(i);
                RealisationBaseRequestDto realReq = (i < reqReals.size()) ? reqReals.get(i) : null;
                if (realReq == null) continue;

                // ----- Docs association sync -----
                if (realReq.getDocIds() != null && !realReq.getDocIds().isEmpty()) {
                    Set<Integer> requestDocIds = new HashSet<>(realReq.getDocIds());
                    Set<Integer> existingDocIds = real.getDocs() != null
                            ? real.getDocs().stream().map(Docs::getId).collect(Collectors.toSet())
                            : new HashSet<>();

                    Set<Integer> docsToAdd = new HashSet<>(requestDocIds);
                    docsToAdd.removeAll(existingDocIds);

                    Set<Integer> docsToRemove = new HashSet<>(existingDocIds);
                    docsToRemove.removeAll(requestDocIds);

                    List<Docs> docs = real.getDocs() != null ? new ArrayList<>(real.getDocs()) : new ArrayList<>();

                    if (!docsToRemove.isEmpty()) {
                        docs.removeIf(doc -> docsToRemove.contains(doc.getId()));
                        final Realisation realRef = real;
                        for (Integer docId : docsToRemove) {
                            docsRepository.findById(docId).ifPresent(doc -> {
                                if (doc.getRealisations() != null) {
                                    doc.getRealisations().remove(realRef);
                                    docsRepository.save(doc);
                                }
                            });
                        }
                    }

                    for (Integer docId : docsToAdd) {
                        Docs doc = docsRepository.findById(docId)
                                .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));
                        if (doc.getRealisations() == null) doc.setRealisations(new ArrayList<>());
                        if (!doc.getRealisations().contains(real)) doc.getRealisations().add(real);
                        doc.setIsDeleted(false);
                        docsRepository.save(doc);
                        docs.add(doc);
                    }

                    real.setDocs(docs);
                    real = realisationRepository.save(real);
                    System.out.println("Updated docs for realisation ID " + real.getId() + ": " + docs.size() + " total docs");
                } else if (real.getDocs() != null && !real.getDocs().isEmpty()) {
                    for (Docs doc : real.getDocs()) {
                        if (doc.getRealisations() != null) {
                            doc.getRealisations().remove(real);
                            docsRepository.save(doc);
                        }
                    }
                    real.setDocs(new ArrayList<>());
                    real = realisationRepository.save(real);
                    System.out.println("Cleared docs for realisation ID " + real.getId());
                }

                // ----- File uploads per realisation -----
                if (realReq.getRealisationFiles() != null && !realReq.getRealisationFiles().isEmpty()) {
                    try {
                        System.out.println("Processing " + realReq.getRealisationFiles().size() +
                                " files for realisation ID: " + real.getId());

                        documentCommuneService.processAndSaveDocuments(
                                realReq.getRealisationFiles(),
                                real,
                                Constants.PATH_ENTREPOT_GED_PPM + "/" + existingPpm.getId() + Constants.PATH_ENTREPOT_GED_REALISATION
                        );

                        real = realisationRepository.findById(new CompositeId(real.getId(), real.getVersion()))
                                .orElseThrow(() -> new RuntimeException("Could not find realisation after file upload"));

                        if (real.getDocs() != null && !real.getDocs().isEmpty()) {
                            for (Docs d : real.getDocs()) {
                                if (d.getRealisations() == null) d.setRealisations(new ArrayList<>());
                                if (!d.getRealisations().contains(real)) d.getRealisations().add(real);
                                if (d.getIsDeleted() == null || Boolean.TRUE.equals(d.getIsDeleted())) d.setIsDeleted(false);
                                docsRepository.save(d);
                            }
                            real = realisationRepository.save(real);
                        }

                        System.out.println("After file processing, realisation has " +
                                (real.getDocs() != null ? real.getDocs().size() : 0) + " documents");
                    } catch (IOException e) {
                        System.err.println("Error processing realisation files: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        // (Intentionally removed) Secondary lots re-sync. All lot creation/versioning is in processRealisationsWithLotLogic.
        System.out.println("Skipping secondary lots re-sync (disabled by design).");

        // --- Financements (AFTER lots/version bump) ---
        boolean financementsChanged = false;

        for (int i = 0; i < updatedRealisations.size(); i++) {
            Realisation real = updatedRealisations.get(i);

            // Match request DTO for THIS realisation (by ID, else fallback by index)
            RealisationBaseRequestDto reqDto = null;

            if (request instanceof PpmWithLotsRequestDto) {
                PpmWithLotsRequestDto wr = (PpmWithLotsRequestDto) request;
                if (wr.getRealisationsWithLots() != null) {
                    for (RealisationWithLotsRequestDto d : wr.getRealisationsWithLots()) {
                        if (d.getId() != null && d.getId().equals(real.getId())) { reqDto = d; break; }
                    }
                }
            }
            if (reqDto == null && request.getRealisations() != null) {
                for (RealisationBaseRequestDto d : request.getRealisations()) {
                    if (d.getId() != null && d.getId().equals(real.getId())) { reqDto = d; break; }
                }
            }
            if (reqDto == null && reqReals != null && i < reqReals.size()) {
                reqDto = reqReals.get(i);
            }

            // BEFORE snapshot for CURRENT version
            List<Financement> beforeCurrent = financementRepository
                    .findByRealisationIdAndVersion(real.getId(), real.getVersion())
                    .stream().filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                    .collect(Collectors.toList());

            List<FinancementRequest> finReqs = (reqDto != null) ? reqDto.getFinancements() : null;

            // Decide if financements WOULD change -> bump version first
            boolean willChange = willFinancementsChange(beforeCurrent, finReqs);
            Realisation targetReal = real;

            if (willChange) {
                Realisation newVer = new Realisation();
                newVer.setId(real.getId());
                newVer.setVersion(real.getVersion() + 1);
                // copy scalar fields
                newVer.setBeneficiaire(real.getBeneficiaire());
                newVer.setRealisationEnvisagees(real.getRealisationEnvisagees());
                newVer.setTypeMarcher(real.getTypeMarcher());
                newVer.setModePassation(real.getModePassation());
                newVer.setDatePrevLps(real.getDatePrevLps());
                newVer.setDatePrevAc(real.getDatePrevAc());
                newVer.setDatePrevDp(real.getDatePrevDp());
                newVer.setDatePrevAp(real.getDatePrevAp());
                newVer.setBudgetEstime(real.getBudgetEstime());
                newVer.setSeuilApplicable(real.getSeuilApplicable());
                newVer.setIsDeleted(false);
                newVer.setPpms(new ArrayList<>()); // linked by PPM versioning later
                // keep same (already-processed) lots
                newVer.setLots(real.getLots());

                newVer = realisationRepository.save(newVer);
                updatedRealisations.set(i, newVer);
                targetReal = newVer;

                System.out.println("Saved NEW VERSION for financement change. Realisation ID " + newVer.getId() +
                        " version " + newVer.getVersion());

                financementsChanged = true;
            }

            // Apply financements ON target version
            if (finReqs == null) {
                // null ⇒ delete-all on this (target) version
                List<Financement> existingAll = financementRepository.findByRealisationIdAndVersion(targetReal.getId(), targetReal.getVersion());
                boolean any = false;
                for (Financement f : existingAll) {
                    if (!Boolean.TRUE.equals(f.getIsDeleted())) { f.setIsDeleted(true); any = true; }
                }
                if (any) {
                    financementRepository.saveAll(existingAll);
                }
            } else {
                System.out.println("=== PROCESSING FINANCEMENTS (existing version) ===");
                System.out.println("Realisation ID: " + targetReal.getId() + ", Version: " + targetReal.getVersion());
                // NOTE: this method should upsert rows for the target version (including same financementId)
                processFinancementsOnExistingVersion(targetReal, reqDto);
                System.out.println("Processed " + finReqs.size() + " financements for realisation " + targetReal.getId() + " v" + targetReal.getVersion());
                System.out.println("=== FINANCEMENTS DONE ===");

                // Mark missing financements as deleted (by ID when present, else by signature)
                Set<Integer> requestedIds = finReqs.stream()
                        .filter(fr -> fr.getFinancementId() != null)
                        .map(FinancementRequest::getFinancementId)
                        .collect(Collectors.toSet());

                Function<FinancementRequest, String> reqSig = fr ->
                        (fr.getSourceFinancement() != null ? String.valueOf(fr.getSourceFinancement().getId()) : "null")
                                + "|" + (fr.getMonnaieId() != null ? String.valueOf(fr.getMonnaieId()) : "null")
                                + "|" + (fr.getBudgetEstimer() != null ? fr.getBudgetEstimer().toPlainString() : "0");

                Set<String> requestedSigs = finReqs.stream().map(reqSig).collect(Collectors.toSet());

                List<Financement> existingThisVersion = financementRepository.findByRealisationIdAndVersion(targetReal.getId(), targetReal.getVersion());
                boolean anyDeleted = false;
                for (Financement f : existingThisVersion) {
                    Integer fid = (f.getId() != null) ? f.getId().getFinancementId() : null;
                    String fSig =
                            (f.getSourceFinancement() != null ? String.valueOf(f.getSourceFinancement().getId()) : "null")
                                    + "|" + (f.getMonnaie() != null ? String.valueOf(f.getMonnaie().getId()) : "null")
                                    + "|" + (f.getBudgetEstimer() != null ? f.getBudgetEstimer().toPlainString() : "0");

                    boolean keep = (fid != null && requestedIds.contains(fid)) || requestedSigs.contains(fSig);
                    if (!keep && !Boolean.TRUE.equals(f.getIsDeleted())) {
                        f.setIsDeleted(true);
                        anyDeleted = true;
                    }
                }
                if (anyDeleted) {
                    financementRepository.saveAll(existingThisVersion);
                }
            }

            // AFTER snapshot (active) on TARGET version
            List<Financement> after = financementRepository
                    .findByRealisationIdAndVersion(targetReal.getId(), targetReal.getVersion())
                    .stream().filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                    .collect(Collectors.toList());

            // Recompute budget from financements (default taux=1.0 if null/zero)
            BigDecimal rb = after.stream()
                    .map(f -> {
                        BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                        Double raw = (f.getMonnaie() != null) ? f.getMonnaie().getTaux() : null;
                        double taux = (raw != null && raw > 0.0) ? raw : 1.0;
                        return amt.multiply(BigDecimal.valueOf(taux));
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
            targetReal.setBudgetEstime(rb);

            // Re-apply seuil
            setSeuilApplicableForRealisationInPpm(targetReal, requestAcId);
        }

        boolean realisationsChanged = realisationsPhaseChanged || financementsChanged;

        // --- PPM documents (root) ---
        List<Docs> updatedDocs = new ArrayList<>();
        boolean documentsChanged = processDocuments(existingPpm, request, updatedDocs);

        // --- Request hash (dedupe) ---
        String requestHash = generateRequestHash(request);
        String previousRequestHash = getPreviousRequestHash(ppmId, ppmVer);

        boolean hasFiles = false;
        if (request instanceof PpmWithLotsRequestDto) {
            PpmWithLotsRequestDto lotsRequest = (PpmWithLotsRequestDto) request;
            hasFiles = lotsRequest.getPpmFiles() != null && !lotsRequest.getPpmFiles().isEmpty();

            if (!hasFiles && lotsRequest.getRealisationsWithLots() != null) {
                for (RealisationWithLotsRequestDto realisation : lotsRequest.getRealisationsWithLots()) {
                    if (realisation.getRealisationFiles() != null && !realisation.getRealisationFiles().isEmpty()) {
                        hasFiles = true; break;
                    }
                }
            }
        }

        System.out.printf("DEDUPE GUARD: hasFiles=%s, direct=%s, realisations=%s, docs=%s%n",
                hasFiles, directFieldsChanged, realisationsChanged, documentsChanged);

        if (!hasFiles && !directFieldsChanged && !realisationsChanged && !documentsChanged) {
            System.out.println("Current request hash: " + requestHash);
            System.out.println("Previous request hash: " + previousRequestHash);

            if (requestHash != null && requestHash.equals(previousRequestHash)) {
                System.out.println("Duplicate request detected (no changes + same hash), returning existing PPM without changes");

                Ppm copyForResponse = new Ppm();
                BeanUtils.copyProperties(existingPpm, copyForResponse);

                if (existingPpm.getDocs() != null) {
                    copyForResponse.setDocs(
                            existingPpm.getDocs().stream()
                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                    .collect(Collectors.toList())
                    );
                }

                if (copyForResponse.getRealisations() != null) {
                    for (Realisation real : copyForResponse.getRealisations()) {
                        if (real.getDocs() != null) {
                            real.setDocs(
                                    real.getDocs().stream()
                                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                            .collect(Collectors.toList())
                            );
                        }
                        if (real.getLots() != null) {
                            real.setLots(
                                    real.getLots().stream()
                                            .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                            .collect(Collectors.toList())
                            );
                        }
                        if (real.getSeuilApplicable() != null && real.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
                            log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", real.getId());
                            real.setSeuilApplicable(BigDecimal.ZERO);
                        }
                    }
                }

                return copyForResponse;
            }
        }

        boolean shouldCreateNewVersion = directFieldsChanged || realisationsChanged || documentsChanged || hasFiles;

        System.out.println("Versioning decision inputs:");
        System.out.println("  - Direct changes: " + directFieldsChanged);
        System.out.println("  - Realisations changed: " + realisationsChanged + " (phase=" + realisationsPhaseChanged + ", financements=" + financementsChanged + ")");
        System.out.println("  - Documents changed: " + documentsChanged);
        System.out.println("  - Has files: " + hasFiles);

        logVersionDecision(shouldCreateNewVersion, directFieldsChanged, realisationsChanged, documentsChanged);

        if (!shouldCreateNewVersion) {
            System.out.println("No changes detected, returning existing PPM without creating new version");
            return existingPpm;
        }

        // We will create a new version; keep request hash for this source version
        storePreviousRequestHash(ppmId, ppmVer, requestHash);

        // ---- Rebuild root docs for the new version ----
        List<Docs> rootDocsForNewVersion;
        if (request.getDocIds() != null) {
            rootDocsForNewVersion = new ArrayList<>();
            Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());

            if (existingPpm.getDocs() != null) {
                for (Docs existingDoc : existingPpm.getDocs()) {
                    Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                    if (docToProcess == null) { System.out.println("Warning: Document " + existingDoc.getId() + " no longer exists in database, skipping"); continue; }
                    if (requestedDocIds.contains(docToProcess.getId())) {
                        docToProcess.setIsDeleted(false);
                        docToProcess = docsRepository.save(docToProcess);
                        rootDocsForNewVersion.add(docToProcess);
                        requestedDocIds.remove(docToProcess.getId());
                    } else {
                        docToProcess.setIsDeleted(true);
                        docToProcess = docsRepository.save(docToProcess);
                        rootDocsForNewVersion.add(docToProcess);
                    }
                }
            }

            for (Integer docId : requestedDocIds) {
                if (rootDocsForNewVersion.stream().anyMatch(doc -> doc.getId().equals(docId))) continue;
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
                doc.setIsDeleted(false);
                doc = docsRepository.save(doc);
                rootDocsForNewVersion.add(doc);
            }
        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
            rootDocsForNewVersion = new ArrayList<>();
            for (Docs existingDoc : existingPpm.getDocs()) {
                Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                if (docToProcess == null) continue;
                docToProcess.setIsDeleted(true);
                docToProcess = docsRepository.save(docToProcess);
                rootDocsForNewVersion.add(docToProcess);
                System.out.println("Marked document with ID " + docToProcess.getId() + " as deleted (no docIds provided)");
            }
        } else {
            rootDocsForNewVersion = new ArrayList<>();
        }

        // Final validation before persisting new PPM
        Ppm tempPpmForValidation = new Ppm();
        tempPpmForValidation.setRealisations(updatedRealisations);
        validateRealisations(tempPpmForValidation);

        System.out.println("Creating new PPM version: " + (ppmVer + 1));
        Ppm newPpm = createAndSaveNewPpmVersion(ppmId, ppmVer, tempPpm, updatedRealisations, rootDocsForNewVersion, existingPpm);

        // Carry over persistent fields
        newPpm.setRef(existingPpm.getRef());
        newPpm.setProcessInstanceId(existingPpm.getProcessInstanceId());

        // Compute PPM budget from realisations
        BigDecimal ppmBudgetFromRealisations = (updatedRealisations == null) ? BigDecimal.ZERO
                : updatedRealisations.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .map(r -> r.getBudgetEstime() != null ? r.getBudgetEstime() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        newPpm.setAnnee(existingPpm.getAnnee());
        newPpm.setBudgetEstime(ppmBudgetFromRealisations);
        newPpm = repository.save(newPpm);

        System.out.println("PPM post-versioning fields set: annee=" + newPpm.getAnnee()
                + ", budgetEstime=" + newPpm.getBudgetEstime()
                + " (computed from " + (updatedRealisations != null ? updatedRealisations.size() : 0) + " realisations)");

        // PPM-level files
        if (request instanceof PpmWithLotsRequestDto) {
            PpmWithLotsRequestDto lotsRequest = (PpmWithLotsRequestDto) request;
            if (lotsRequest.getPpmFiles() != null && !lotsRequest.getPpmFiles().isEmpty()) {
                try {
                    System.out.println("Processing " + lotsRequest.getPpmFiles().size() + " new files for PPM");
                    documentCommuneService.processAndSaveDocuments(lotsRequest.getPpmFiles(), newPpm, Constants.PATH_ENTREPOT_GED_PPM);
                    newPpm = repository.save(newPpm);

                    if (newPpm.getDocs() != null && !newPpm.getDocs().isEmpty()) {
                        for (Docs d : newPpm.getDocs()) {
                            if (d.getPpms() == null) d.setPpms(new ArrayList<>());
                            if (!d.getPpms().contains(newPpm)) d.getPpms().add(newPpm);
                            if (d.getIsDeleted() == null || Boolean.TRUE.equals(d.getIsDeleted())) d.setIsDeleted(false);
                            docsRepository.save(d);
                        }
                        newPpm = repository.save(newPpm);
                    }
                } catch (IOException e) {
                    System.err.println("Error processing PPM files: " + e.getMessage());
                    throw new RuntimeException("Error processing PPM files: " + e.getMessage(), e);
                }
            }
        }

        // Store hash for the new version
        storePreviousRequestHash(ppmId, newPpm.getVersion(), requestHash);

        // Prepare response copy (filter deleted docs/lots)
        Ppm responseVersion = new Ppm();
        BeanUtils.copyProperties(newPpm, responseVersion);

        if (responseVersion.getDocs() != null) {
            List<Docs> filteredDocs = newPpm.getDocs() == null ? Collections.emptyList()
                    : newPpm.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList());

            System.out.println("Filtered docs for response: " + filteredDocs.size() + " docs (from " + (newPpm.getDocs() != null ? newPpm.getDocs().size() : 0) + " total)");
            System.out.println("Filtered doc IDs: " + filteredDocs.stream().map(Docs::getId).collect(Collectors.toList()));

            responseVersion.setDocs(filteredDocs);
        }

        if (responseVersion.getRealisations() != null) {
            for (Realisation real : responseVersion.getRealisations()) {
                if (real.getDocs() != null) {
                    real.setDocs(
                            real.getDocs().stream()
                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                    .collect(Collectors.toList())
                    );
                }
                if (real.getLots() != null) {
                    real.setLots(
                            real.getLots().stream()
                                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                    .collect(Collectors.toList())
                    );
                }
                if (real.getSeuilApplicable() != null && real.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
                    log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", real.getId());
                    real.setSeuilApplicable(BigDecimal.ZERO);
                }
            }
        }

        if (responseVersion.getRealisations() != null && !responseVersion.getRealisations().isEmpty()) {
            List<Realisation> latestRealisations = filterLatestRealisationVersions(responseVersion.getRealisations());
            responseVersion.setRealisations(latestRealisations);
            System.out.println("FINAL FILTER: Showing only latest versions");
            for (Realisation r : latestRealisations) {
                System.out.println("  - Final version: Realisation ID: " + r.getId() + ", Version: " + r.getVersion());
            }
        }

        System.out.println("Returning filtered PPM version: " + responseVersion.getVersion());
        System.out.println("Returning filtered new PPM version: " + responseVersion.getVersion());
        return responseVersion;
    }

    /** Decide if financements would change for a realisation version given the request. */
    private boolean willFinancementsChange(List<Financement> beforeActive, List<FinancementRequest> finReqs) {
        if (finReqs == null) {
            // null request means "clear all": only a change if something exists now
            return !beforeActive.isEmpty();
        }

        // Map existing by financementId (active only)
        Map<Integer, Financement> byId = beforeActive.stream()
                .filter(f -> f.getId() != null && f.getId().getFinancementId() != null)
                .collect(Collectors.toMap(f -> f.getId().getFinancementId(), f -> f, (a,b) -> a));

        // Any new items (no id) OR modifications to existing?
        for (FinancementRequest fr : finReqs) {
            if (fr.getFinancementId() == null) {
                return true; // brand new financement
            }
            Financement ex = byId.get(fr.getFinancementId());
            if (ex == null) {
                return true; // new ID not present before
            }
            BigDecimal reqAmt = fr.getBudgetEstimer() != null ? fr.getBudgetEstimer() : BigDecimal.ZERO;
            BigDecimal exAmt  = ex.getBudgetEstimer() != null ? ex.getBudgetEstimer() : BigDecimal.ZERO;

            Integer reqMon  = fr.getMonnaieId();
            Integer exMon   = (ex.getMonnaie() != null) ? ex.getMonnaie().getId() : null;

            Integer reqSrc  = (fr.getSourceFinancement() != null) ? fr.getSourceFinancement().getId() : null;
            Integer exSrc   = (ex.getSourceFinancement() != null) ? ex.getSourceFinancement().getId() : null;

            if (reqAmt.compareTo(exAmt) != 0
                    || !Objects.equals(reqMon, exMon)
                    || !Objects.equals(reqSrc, exSrc)) {
                return true; // value change
            }
        }

        // Any deletions? (existing not present in request)
        Set<Integer> requestedIds = finReqs.stream()
                .filter(fr -> fr.getFinancementId() != null)
                .map(FinancementRequest::getFinancementId)
                .collect(Collectors.toSet());

        for (Financement ex : beforeActive) {
            Integer fid = (ex.getId() != null) ? ex.getId().getFinancementId() : null;
            if (fid != null && !requestedIds.contains(fid)) {
                return true; // deletion
            }
        }

        return false; // identical
    }


//    @Transactional
//    public Ppm updateWithVersioning(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
//        // Log the request for debugging
//        logUpdateRequest(ppmId, ppmVer, request);
//        if (request.getRealisations() != null) {
//            for (RealisationBaseRequestDto re : request.getRealisations()) {
//                System.out.println("doc realisation" + re.getRealisationFiles());
//            }
//        }
//
//        if (request instanceof PpmWithLotsRequestDto) {
//            validateRequestLotsConsistency((PpmWithLotsRequestDto) request);
//        }
//
//        Ppm existingPpm = getExistingPpm(ppmId, ppmVer);
//
//        Integer requestAcId = request.getAcId() != null
//                ? request.getAcId()
//                : (existingPpm.getAc() != null ? existingPpm.getAc().getId() : null);
//
//        Ppm tempPpm = buildTempPpm(existingPpm, request);
//
//        boolean directFieldsChanged = !arePpmFieldsIdentical(existingPpm, tempPpm);
//        System.out.println("Direct field changes detected: " + directFieldsChanged);
//
//        // --- Realisations phase (prefers with-lots) ---
//        List<Realisation> updatedRealisations = new ArrayList<>();
//        boolean realisationsPhaseChanged = processRealisationsWithLotLogic(existingPpm, request, updatedRealisations);
//
//        // --- Lot re-sync: ALWAYS run to ensure lots are attached correctly ---
//        List<RealisationBaseRequestDto> reqReals = null;
//        if (request instanceof PpmWithLotsRequestDto) {
//            PpmWithLotsRequestDto lotsReq = (PpmWithLotsRequestDto) request;
//            if (lotsReq.getRealisationsWithLots() != null && !lotsReq.getRealisationsWithLots().isEmpty()) {
//                reqReals = new ArrayList<>(lotsReq.getRealisationsWithLots());
//            } else if (request.getRealisations() != null) {
//                reqReals = new ArrayList<>(request.getRealisations());
//            }
//        } else if (request.getRealisations() != null) {
//            reqReals = new ArrayList<>(request.getRealisations());
//        }
//
//        boolean lotResyncChanged = false;
//        if (reqReals != null && !reqReals.isEmpty()) {
//            Map<String, Lot> newLotsMap = new HashMap<>();
//
//            for (int i = 0; i < updatedRealisations.size(); i++) {
//                Realisation real = updatedRealisations.get(i);
//                RealisationBaseRequestDto realReq = (i < reqReals.size()) ? reqReals.get(i) : null;
//                if (realReq == null) continue;
//
//                // ----- Docs sync -----
//                if (realReq.getDocIds() != null && !realReq.getDocIds().isEmpty()) {
//                    Set<Integer> requestDocIds = new HashSet<>(realReq.getDocIds());
//                    Set<Integer> existingDocIds = real.getDocs() != null
//                            ? real.getDocs().stream().map(Docs::getId).collect(Collectors.toSet())
//                            : new HashSet<>();
//
//                    Set<Integer> docsToAdd = new HashSet<>(requestDocIds);
//                    docsToAdd.removeAll(existingDocIds);
//
//                    Set<Integer> docsToRemove = new HashSet<>(existingDocIds);
//                    docsToRemove.removeAll(requestDocIds);
//
//                    List<Docs> docs = real.getDocs() != null ? new ArrayList<>(real.getDocs()) : new ArrayList<>();
//
//                    if (!docsToRemove.isEmpty()) {
//                        docs.removeIf(doc -> docsToRemove.contains(doc.getId()));
//                        final Realisation realRef = real;
//                        for (Integer docId : docsToRemove) {
//                            docsRepository.findById(docId).ifPresent(doc -> {
//                                if (doc.getRealisations() != null) {
//                                    doc.getRealisations().remove(realRef);
//                                    docsRepository.save(doc);
//                                }
//                            });
//                        }
//                    }
//
//                    for (Integer docId : docsToAdd) {
//                        Docs doc = docsRepository.findById(docId)
//                                .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));
//                        if (doc.getRealisations() == null) doc.setRealisations(new ArrayList<>());
//                        if (!doc.getRealisations().contains(real)) doc.getRealisations().add(real);
//                        doc.setIsDeleted(false);
//                        docsRepository.save(doc);
//                        docs.add(doc);
//                    }
//
//                    real.setDocs(docs);
//                    real = realisationRepository.save(real);
//                    System.out.println("Updated docs for realisation ID " + real.getId() + ": " + docs.size() + " total docs");
//                } else if (real.getDocs() != null && !real.getDocs().isEmpty()) {
//                    for (Docs doc : real.getDocs()) {
//                        if (doc.getRealisations() != null) {
//                            doc.getRealisations().remove(real);
//                            docsRepository.save(doc);
//                        }
//                    }
//                    real.setDocs(new ArrayList<>());
//                    real = realisationRepository.save(real);
//                    System.out.println("Cleared docs for realisation ID " + real.getId());
//                }
//
//                // ----- File uploads per realisation -----
//                if (realReq.getRealisationFiles() != null && !realReq.getRealisationFiles().isEmpty()) {
//                    try {
//                        System.out.println("Processing " + realReq.getRealisationFiles().size() +
//                                " files for realisation ID: " + real.getId());
//
//                        documentCommuneService.processAndSaveDocuments(
//                                realReq.getRealisationFiles(),
//                                real,
//                                Constants.PATH_ENTREPOT_GED_PPM + "/" + existingPpm.getId() + Constants.PATH_ENTREPOT_GED_REALISATION
//                        );
//
//                        real = realisationRepository.findById(new CompositeId(real.getId(), real.getVersion()))
//                                .orElseThrow(() -> new RuntimeException("Could not find realisation after file upload"));
//
//                        if (real.getDocs() != null && !real.getDocs().isEmpty()) {
//                            for (Docs d : real.getDocs()) {
//                                if (d.getRealisations() == null) d.setRealisations(new ArrayList<>());
//                                if (!d.getRealisations().contains(real)) d.getRealisations().add(real);
//                                if (d.getIsDeleted() == null || Boolean.TRUE.equals(d.getIsDeleted())) d.setIsDeleted(false);
//                                docsRepository.save(d);
//                            }
//                            real = realisationRepository.save(real);
//                        }
//
//                        System.out.println("After file processing, realisation has " +
//                                (real.getDocs() != null ? real.getDocs().size() : 0) + " documents");
//                    } catch (IOException e) {
//                        System.err.println("Error processing realisation files: " + e.getMessage());
//                        e.printStackTrace();
//                        throw new RuntimeException(e);
//                    }
//                }
//
//                // ----- Lots re-sync (value-aware) -----
//                List<LotResponse> lotDtos = null;
//                if (realReq instanceof RealisationWithLotsRequestDto) {
//                    RealisationWithLotsRequestDto rl = (RealisationWithLotsRequestDto) realReq;
//                    if (rl.getLots() != null && !rl.getLots().isEmpty()) {
//                        lotDtos = rl.getLots();
//                    }
//                }
//
//                if (lotDtos != null && !lotDtos.isEmpty()) {
//                    List<Lot> updatedLotsForThisReal = new ArrayList<>();
//                    Set<Integer> processedLotIds = new HashSet<>();
//                    boolean anyLotChangedForThisReal = false;
//
//                    for (LotResponse lotDto : lotDtos) {
//                        if (lotDto.getId() == null) {
//                            String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
//
//                            if (newLotsMap.containsKey(lotHash)) {
//                                updatedLotsForThisReal.add(newLotsMap.get(lotHash));
//                                processedLotIds.add(newLotsMap.get(lotHash).getId());
//                                continue;
//                            }
//
//                            Lot newLot = new Lot();
//                            newLot.setDescription(lotDto.getDescription());
//                            newLot.setVersion(1);
//                            newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO);
//                            newLot.setIsDeleted(false);
//
//                            Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
//                            Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
//                            newLot.setId(lotId);
//
//                            Lot savedLot = lotRepository.save(newLot);
//                            updatedLotsForThisReal.add(savedLot);
//                            processedLotIds.add(savedLot.getId());
//                            newLotsMap.put(lotHash, savedLot);
//                            lotResyncChanged = true;
//                            anyLotChangedForThisReal = true;
//                            System.out.println("Created new Lot with ID: " + savedLot.getId() + ", Budget: " + savedLot.getBudget());
//                        } else {
//                            if (processedLotIds.contains(lotDto.getId())) continue;
//
//                            Integer lotId = lotDto.getId();
//                            Integer lotVersion = lotDto.getVersion();
//
//                            if (lotVersion == null) {
//                                lotVersion = lotRepository.findMaxVersionById(lotId);
//                            }
//
//                            Optional<Lot> existingLotOpt = (lotVersion != null)
//                                    ? lotRepository.findById(new CompositeId(lotId, lotVersion))
//                                    : Optional.empty();
//
//                            if (existingLotOpt.isPresent()) {
//                                Lot existingLot = existingLotOpt.get();
//
//                                String reqDesc = lotDto.getDescription() != null ? lotDto.getDescription() : "";
//                                String exDesc  = existingLot.getDescription() != null ? existingLot.getDescription() : "";
//
//                                BigDecimal reqBudget = (lotDto.getBudget() != null) ? lotDto.getBudget() : BigDecimal.ZERO;
//                                BigDecimal exBudget  = (existingLot.getBudget() != null) ? existingLot.getBudget() : BigDecimal.ZERO;
//
//                                boolean descDiffers   = !reqDesc.equals(exDesc);
//                                boolean budgetDiffers = reqBudget.compareTo(exBudget) != 0;
//
//                                if (descDiffers || budgetDiffers) {
//                                    Lot newLotVer = new Lot();
//                                    newLotVer.setId(existingLot.getId());
//                                    newLotVer.setVersion(existingLot.getVersion() + 1);
//                                    newLotVer.setDescription(lotDto.getDescription());
//                                    newLotVer.setBudget(reqBudget);
//                                    newLotVer.setIsDeleted(false);
//
//                                    Lot savedLot = lotRepository.save(newLotVer);
//                                    updatedLotsForThisReal.add(savedLot);
//                                    processedLotIds.add(savedLot.getId());
//                                    lotResyncChanged = true;
//                                    anyLotChangedForThisReal = true;
//                                    System.out.println("Created NEW LOT VERSION because " +
//                                            (descDiffers && budgetDiffers ? "description & budget changed"
//                                                    : descDiffers ? "description changed" : "budget changed") +
//                                            ": id=" + savedLot.getId() + ", ver=" + savedLot.getVersion() + ", budget=" + savedLot.getBudget());
//                                } else {
//                                    updatedLotsForThisReal.add(existingLot);
//                                    processedLotIds.add(existingLot.getId());
//                                    System.out.println("Using existing Lot: " + existingLot.getId() + ", Version: " + existingLot.getVersion() + ", Budget: " + existingLot.getBudget());
//                                }
//                            } else {
//                                Lot newLot = new Lot();
//                                newLot.setId(lotId);
//                                newLot.setVersion(1);
//                                newLot.setDescription(lotDto.getDescription());
//                                newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO);
//                                newLot.setIsDeleted(false);
//                                Lot savedLot = lotRepository.save(newLot);
//                                updatedLotsForThisReal.add(savedLot);
//                                processedLotIds.add(savedLot.getId());
//                                lotResyncChanged = true;
//                                anyLotChangedForThisReal = true;
//                                System.out.println("Created new Lot with provided ID: " + savedLot.getId() + ", Budget: " + savedLot.getBudget());
//                            }
//                        }
//                    }
//
//                    if (anyLotChangedForThisReal) {
//                        Realisation newVer = new Realisation();
//                        newVer.setId(real.getId());
//                        newVer.setVersion(real.getVersion() + 1);
//                        // copy scalar fields
//                        newVer.setBeneficiaire(real.getBeneficiaire());
//                        newVer.setRealisationEnvisagees(real.getRealisationEnvisagees());
//                        newVer.setTypeMarcher(real.getTypeMarcher());
//                        newVer.setModePassation(real.getModePassation());
//                        newVer.setDatePrevLps(real.getDatePrevLps());
//                        newVer.setDatePrevAc(real.getDatePrevAc());
//                        newVer.setDatePrevDp(real.getDatePrevDp());
//                        newVer.setDatePrevAp(real.getDatePrevAp());
//                        newVer.setBudgetEstime(real.getBudgetEstime());
//                        newVer.setSeuilApplicable(real.getSeuilApplicable());
//                        newVer.setIsDeleted(false);
//                        newVer.setPpms(new ArrayList<>());
//                        newVer.setLots(updatedLotsForThisReal);
//
//                        newVer = realisationRepository.save(newVer);
//                        updatedRealisations.set(i, newVer);
//
//                        System.out.println("Saved NEW VERSION realisation ID " + newVer.getId() +
//                                " version " + newVer.getVersion() + " with " +
//                                (newVer.getLots() != null ? newVer.getLots().size() : 0) + " lots");
//                    } else {
//                        real.setLots(updatedLotsForThisReal);
//                        real = realisationRepository.save(real);
//                        System.out.println("After saving, realisation ID: " + real.getId() +
//                                " has " + (real.getLots() != null ? real.getLots().size() : 0) + " lots");
//                    }
//                } else if (real.getLots() != null && !real.getLots().isEmpty()) {
//                    // Clearing lots -> bump version
//                    List<Lot> empty = new ArrayList<>();
//                    Realisation newVer = new Realisation();
//                    newVer.setId(real.getId());
//                    newVer.setVersion(real.getVersion() + 1);
//                    newVer.setBeneficiaire(real.getBeneficiaire());
//                    newVer.setRealisationEnvisagees(real.getRealisationEnvisagees());
//                    newVer.setTypeMarcher(real.getTypeMarcher());
//                    newVer.setModePassation(real.getModePassation());
//                    newVer.setDatePrevLps(real.getDatePrevLps());
//                    newVer.setDatePrevAc(real.getDatePrevAc());
//                    newVer.setDatePrevDp(real.getDatePrevDp());
//                    newVer.setDatePrevAp(real.getDatePrevAp());
//                    newVer.setBudgetEstime(real.getBudgetEstime());
//                    newVer.setSeuilApplicable(real.getSeuilApplicable());
//                    newVer.setIsDeleted(false);
//                    newVer.setPpms(new ArrayList<>());
//                    newVer.setLots(empty);
//
//                    newVer = realisationRepository.save(newVer);
//                    updatedRealisations.set(i, newVer);
//                    lotResyncChanged = true;
//
//                    System.out.println("Cleared lots → Saved NEW VERSION realisation ID: " + newVer.getId() +
//                            ", version: " + newVer.getVersion());
//                }
//            }
//        }
//
//        // --- Financements (AFTER lots/version bump) ---
//        boolean financementsChanged = false;
//
//        for (int i = 0; i < updatedRealisations.size(); i++) {
//            Realisation real = updatedRealisations.get(i);
//
//            // Match request DTO by ID first, else fallback by index (covers new reals)
//            RealisationBaseRequestDto reqDto = null;
//            if (request instanceof PpmWithLotsRequestDto) {
//                PpmWithLotsRequestDto wr = (PpmWithLotsRequestDto) request;
//                if (wr.getRealisationsWithLots() != null) {
//                    for (RealisationWithLotsRequestDto d : wr.getRealisationsWithLots()) {
//                        if (d.getId() != null && d.getId().equals(real.getId())) { reqDto = d; break; }
//                    }
//                }
//            }
//            if (reqDto == null && request.getRealisations() != null) {
//                for (RealisationBaseRequestDto d : request.getRealisations()) {
//                    if (d.getId() != null && d.getId().equals(real.getId())) { reqDto = d; break; }
//                }
//            }
//            if (reqDto == null && reqReals != null && i < reqReals.size()) {
//                reqDto = reqReals.get(i);
//            }
//
//            // BEFORE snapshot
//            List<Financement> before = financementRepository
//                    .findByRealisationIdAndVersion(real.getId(), real.getVersion())
//                    .stream().filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
//                    .collect(Collectors.toList());
//
//            List<FinancementRequest> finReqs = (reqDto != null) ? reqDto.getFinancements() : null;
//
//            if (finReqs == null) {
//                // null ⇒ delete-all on this version
//                List<Financement> existingAll = financementRepository.findByRealisationIdAndVersion(real.getId(), real.getVersion());
//                boolean any = false;
//                for (Financement f : existingAll) {
//                    if (!Boolean.TRUE.equals(f.getIsDeleted())) { f.setIsDeleted(true); any = true; }
//                }
//                if (any) financementRepository.saveAll(existingAll);
//            } else {
//                // Upsert on this version (non-throwing)
//                System.out.println("=== PROCESSING FINANCEMENTS (existing version) ===");
//                System.out.println("Realisation ID: " + real.getId() + ", Version: " + real.getVersion());
//                processFinancementsOnExistingVersion(real, reqDto);
//                System.out.println("Processed " + finReqs.size() + " financements for realisation " + real.getId() + " v" + real.getVersion());
//                System.out.println("=== FINANCEMENTS DONE ===");
//
//                // Value-aware deletion for rows not present in request (ID OR value signature)
//                Set<Integer> requestedIds = finReqs.stream()
//                        .filter(fr -> fr.getFinancementId() != null)
//                        .map(FinancementRequest::getFinancementId)
//                        .collect(Collectors.toSet());
//
//                Function<FinancementRequest, String> reqSig = fr ->
//                        (fr.getSourceFinancement() != null ? String.valueOf(fr.getSourceFinancement().getId()) : "null")
//                                + "|" + (fr.getMonnaieId() != null ? String.valueOf(fr.getMonnaieId()) : "null")
//                                + "|" + (fr.getBudgetEstimer() != null ? fr.getBudgetEstimer().toPlainString() : "0");
//
//                Set<String> requestedSigs = finReqs.stream().map(reqSig).collect(Collectors.toSet());
//
//                List<Financement> existingThisVersion = financementRepository.findByRealisationIdAndVersion(real.getId(), real.getVersion());
//                boolean anyDeleted = false;
//                for (Financement f : existingThisVersion) {
//                    Integer fid = (f.getId() != null) ? f.getId().getFinancementId() : null;
//                    String fSig =
//                            (f.getSourceFinancement() != null ? String.valueOf(f.getSourceFinancement().getId()) : "null")
//                                    + "|" + (f.getMonnaie() != null ? String.valueOf(f.getMonnaie().getId()) : "null")
//                                    + "|" + (f.getBudgetEstimer() != null ? f.getBudgetEstimer().toPlainString() : "0");
//
//                    boolean keep = (fid != null && requestedIds.contains(fid)) || requestedSigs.contains(fSig);
//                    if (!keep && !Boolean.TRUE.equals(f.getIsDeleted())) {
//                        f.setIsDeleted(true);
//                        anyDeleted = true;
//                    }
//                }
//                if (anyDeleted) {
//                    financementRepository.saveAll(existingThisVersion);
//                }
//            }
//
//            // AFTER snapshot (active)
//            List<Financement> after = financementRepository
//                    .findByRealisationIdAndVersion(real.getId(), real.getVersion())
//                    .stream().filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
//                    .collect(Collectors.toList());
//
//            boolean changedThis =
//                    (finReqs == null)
//                            || before.size() != after.size()
//                            || !after.stream().allMatch(a -> before.stream().anyMatch(b ->
//                            Objects.equals(a.getId().getFinancementId(), b.getId().getFinancementId()) &&
//                                    Objects.equals(a.getBudgetEstimer(), b.getBudgetEstimer()) &&
//                                    Objects.equals(a.getMonnaie() != null ? a.getMonnaie().getId() : null,
//                                            b.getMonnaie() != null ? b.getMonnaie().getId() : null) &&
//                                    Objects.equals(a.getSourceFinancement() != null ? a.getSourceFinancement().getId() : null,
//                                            b.getSourceFinancement() != null ? b.getSourceFinancement().getId() : null)
//                    ));
//
//            financementsChanged |= changedThis;
//
//            // Recompute budget from financements (default taux=1.0 if null/zero)
//            BigDecimal rb = after.stream()
//                    .map(f -> {
//                        BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
//                        Double raw = (f.getMonnaie() != null) ? f.getMonnaie().getTaux() : null;
//                        double taux = (raw != null && raw > 0.0) ? raw : 1.0;
//                        return amt.multiply(BigDecimal.valueOf(taux));
//                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
//            real.setBudgetEstime(rb);
//
//            // Re-apply seuil for this realisation
//            setSeuilApplicableForRealisationInPpm(real, requestAcId);
//        }
//
//        boolean realisationsChanged = realisationsPhaseChanged || lotResyncChanged || financementsChanged;
//
//        // --- PPM documents (root) ---
//        List<Docs> updatedDocs = new ArrayList<>();
//        boolean documentsChanged = processDocuments(existingPpm, request, updatedDocs);
//
//        // --- Request hash (dedupe) ---
//        String requestHash = generateRequestHash(request);
//        String previousRequestHash = getPreviousRequestHash(ppmId, ppmVer);
//
//        boolean hasFiles = false;
//        if (request instanceof PpmWithLotsRequestDto) {
//            PpmWithLotsRequestDto lotsRequest = (PpmWithLotsRequestDto) request;
//            hasFiles = lotsRequest.getPpmFiles() != null && !lotsRequest.getPpmFiles().isEmpty();
//
//            if (!hasFiles && lotsRequest.getRealisationsWithLots() != null) {
//                for (RealisationWithLotsRequestDto realisation : lotsRequest.getRealisationsWithLots()) {
//                    if (realisation.getRealisationFiles() != null && !realisation.getRealisationFiles().isEmpty()) {
//                        hasFiles = true; break;
//                    }
//                }
//            }
//        }
//
//        System.out.printf("DEDUPE GUARD: hasFiles=%s, direct=%s, realisations=%s, docs=%s%n",
//                hasFiles, directFieldsChanged, realisationsChanged, documentsChanged);
//
//        if (!hasFiles && !directFieldsChanged && !realisationsChanged && !documentsChanged) {
//            System.out.println("Current request hash: " + requestHash);
//            System.out.println("Previous request hash: " + previousRequestHash);
//
//            if (requestHash != null && requestHash.equals(previousRequestHash)) {
//                System.out.println("Duplicate request detected (no changes + same hash), returning existing PPM without changes");
//
//                Ppm copyForResponse = new Ppm();
//                BeanUtils.copyProperties(existingPpm, copyForResponse);
//
//                if (existingPpm.getDocs() != null) {
//                    copyForResponse.setDocs(
//                            existingPpm.getDocs().stream()
//                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
//                                    .collect(Collectors.toList())
//                    );
//                }
//
//                if (copyForResponse.getRealisations() != null) {
//                    for (Realisation real : copyForResponse.getRealisations()) {
//                        if (real.getDocs() != null) {
//                            real.setDocs(
//                                    real.getDocs().stream()
//                                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
//                                            .collect(Collectors.toList())
//                            );
//                        }
//                        if (real.getLots() != null) {
//                            real.setLots(
//                                    real.getLots().stream()
//                                            .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
//                                            .collect(Collectors.toList())
//                            );
//                        }
//                        if (real.getSeuilApplicable() != null && real.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
//                            log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", real.getId());
//                            real.setSeuilApplicable(BigDecimal.ZERO);
//                        }
//                    }
//                }
//
//                return copyForResponse;
//            }
//        }
//
//        boolean shouldCreateNewVersion = directFieldsChanged || realisationsChanged || documentsChanged || hasFiles;
//
//        System.out.println("Versioning decision inputs:");
//        System.out.println("  - Direct changes: " + directFieldsChanged);
//        System.out.println("  - Realisations changed: " + realisationsChanged + " (phase=" + realisationsPhaseChanged + ", lotsResync=" + lotResyncChanged + ", financements=" + financementsChanged + ")");
//        System.out.println("  - Documents changed: " + documentsChanged);
//        System.out.println("  - Has files: " + hasFiles);
//
//        logVersionDecision(shouldCreateNewVersion, directFieldsChanged, realisationsChanged, documentsChanged);
//
//        if (!shouldCreateNewVersion) {
//            System.out.println("No changes detected, returning existing PPM without creating new version");
//            return existingPpm;
//        }
//
//        storePreviousRequestHash(ppmId, ppmVer, requestHash);
//
//        // ---- Rebuild root docs for the new version ----
//        List<Docs> rootDocsForNewVersion;
//        if (request.getDocIds() != null) {
//            rootDocsForNewVersion = new ArrayList<>();
//            Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());
//
//            if (existingPpm.getDocs() != null) {
//                for (Docs existingDoc : existingPpm.getDocs()) {
//                    Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
//                    if (docToProcess == null) { System.out.println("Warning: Document " + existingDoc.getId() + " no longer exists in database, skipping"); continue; }
//                    if (requestedDocIds.contains(docToProcess.getId())) {
//                        docToProcess.setIsDeleted(false);
//                        docToProcess = docsRepository.save(docToProcess);
//                        rootDocsForNewVersion.add(docToProcess);
//                        requestedDocIds.remove(docToProcess.getId());
//                    } else {
//                        docToProcess.setIsDeleted(true);
//                        docToProcess = docsRepository.save(docToProcess);
//                        rootDocsForNewVersion.add(docToProcess);
//                    }
//                }
//            }
//
//            for (Integer docId : requestedDocIds) {
//                if (rootDocsForNewVersion.stream().anyMatch(doc -> doc.getId().equals(docId))) continue;
//                Docs doc = docsRepository.findById(docId)
//                        .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
//                doc.setIsDeleted(false);
//                doc = docsRepository.save(doc);
//                rootDocsForNewVersion.add(doc);
//            }
//        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
//            rootDocsForNewVersion = new ArrayList<>();
//            for (Docs existingDoc : existingPpm.getDocs()) {
//                Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
//                if (docToProcess == null) continue;
//                docToProcess.setIsDeleted(true);
//                docToProcess = docsRepository.save(docToProcess);
//                rootDocsForNewVersion.add(docToProcess);
//                System.out.println("Marked document with ID " + docToProcess.getId() + " as deleted (no docIds provided)");
//            }
//        } else {
//            rootDocsForNewVersion = new ArrayList<>();
//        }
//
//        // Final validation before persisting new PPM
//        Ppm tempPpmForValidation = new Ppm();
//        tempPpmForValidation.setRealisations(updatedRealisations);
//        validateRealisations(tempPpmForValidation);
//
//        System.out.println("Creating new PPM version: " + (ppmVer + 1));
//        Ppm newPpm = createAndSaveNewPpmVersion(ppmId, ppmVer, tempPpm, updatedRealisations, rootDocsForNewVersion, existingPpm);
//
//        // Carry over persistent fields
//        newPpm.setRef(existingPpm.getRef());
//        newPpm.setProcessInstanceId(existingPpm.getProcessInstanceId());
//
//        // Compute PPM budget from realisations
//        BigDecimal ppmBudgetFromRealisations = (updatedRealisations == null) ? BigDecimal.ZERO
//                : updatedRealisations.stream()
//                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
//                .map(r -> r.getBudgetEstime() != null ? r.getBudgetEstime() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        newPpm.setAnnee(existingPpm.getAnnee());
//        newPpm.setBudgetEstime(ppmBudgetFromRealisations);
//        newPpm = repository.save(newPpm);
//
//        System.out.println("PPM post-versioning fields set: annee=" + newPpm.getAnnee()
//                + ", budgetEstime=" + newPpm.getBudgetEstime()
//                + " (computed from " + (updatedRealisations != null ? updatedRealisations.size() : 0) + " realisations)");
//
//        // PPM-level files
//        if (request instanceof PpmWithLotsRequestDto) {
//            PpmWithLotsRequestDto lotsRequest = (PpmWithLotsRequestDto) request;
//            if (lotsRequest.getPpmFiles() != null && !lotsRequest.getPpmFiles().isEmpty()) {
//                try {
//                    System.out.println("Processing " + lotsRequest.getPpmFiles().size() + " new files for PPM");
//                    documentCommuneService.processAndSaveDocuments(lotsRequest.getPpmFiles(), newPpm, Constants.PATH_ENTREPOT_GED_PPM);
//                    newPpm = repository.save(newPpm);
//
//                    if (newPpm.getDocs() != null && !newPpm.getDocs().isEmpty()) {
//                        for (Docs d : newPpm.getDocs()) {
//                            if (d.getPpms() == null) d.setPpms(new ArrayList<>());
//                            if (!d.getPpms().contains(newPpm)) d.getPpms().add(newPpm);
//                            if (d.getIsDeleted() == null || Boolean.TRUE.equals(d.getIsDeleted())) d.setIsDeleted(false);
//                            docsRepository.save(d);
//                        }
//                        newPpm = repository.save(newPpm);
//                    }
//                } catch (IOException e) {
//                    System.err.println("Error processing PPM files: " + e.getMessage());
//                    throw new RuntimeException("Error processing PPM files: " + e.getMessage(), e);
//                }
//            }
//        }
//
//        // Store hash for the new version
//        storePreviousRequestHash(ppmId, newPpm.getVersion(), requestHash);
//
//        // Prepare response copy (filter deleted docs/lots)
//        Ppm responseVersion = new Ppm();
//        BeanUtils.copyProperties(newPpm, responseVersion);
//
//        if (responseVersion.getDocs() != null) {
//            List<Docs> filteredDocs = newPpm.getDocs() == null ? Collections.emptyList()
//                    : newPpm.getDocs().stream()
//                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
//                    .collect(Collectors.toList());
//
//            System.out.println("Filtered docs for response: " + filteredDocs.size() + " docs (from " + (newPpm.getDocs() != null ? newPpm.getDocs().size() : 0) + " total)");
//            System.out.println("Filtered doc IDs: " + filteredDocs.stream().map(Docs::getId).collect(Collectors.toList()));
//
//            responseVersion.setDocs(filteredDocs);
//        }
//
//        if (responseVersion.getRealisations() != null) {
//            for (Realisation real : responseVersion.getRealisations()) {
//                if (real.getDocs() != null) {
//                    real.setDocs(
//                            real.getDocs().stream()
//                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
//                                    .collect(Collectors.toList())
//                    );
//                }
//                if (real.getLots() != null) {
//                    real.setLots(
//                            real.getLots().stream()
//                                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
//                                    .collect(Collectors.toList())
//                    );
//                }
//                if (real.getSeuilApplicable() != null && real.getSeuilApplicable().compareTo(BigDecimal.ZERO) < 0) {
//                    log.warn("seuilApplicable is negative for Realisation ID: {}. Setting to 0.", real.getId());
//                    real.setSeuilApplicable(BigDecimal.ZERO);
//                }
//            }
//        }
//
//        if (responseVersion.getRealisations() != null && !responseVersion.getRealisations().isEmpty()) {
//            List<Realisation> latestRealisations = filterLatestRealisationVersions(responseVersion.getRealisations());
//            responseVersion.setRealisations(latestRealisations);
//            System.out.println("FINAL FILTER: Showing only latest versions");
//            for (Realisation r : latestRealisations) {
//                System.out.println("  - Final version: Realisation ID: " + r.getId() + ", Version: " + r.getVersion());
//            }
//        }
//
//        System.out.println("Returning filtered PPM version: " + responseVersion.getVersion());
//        System.out.println("Returning filtered new PPM version: " + responseVersion.getVersion());
//        return responseVersion;
//    }





    /**
     * Updates a PPM with versioning by automatically finding and using the latest version.
     * <p>
     * This method finds the latest version of the PPM with the given ID and then
     * calls the version-specific updateWithVersioning method.
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing the updated data
     * @return the newly created version of the PPM
     * @throws RuntimeException if no versions of the PPM exist or if all versions are deleted
     */
    @Transactional
    @Override
    public Ppm updateWithVersioning(Integer ppmId, PpmBaseRequestDto request) {
        System.out.println("the request is");
        System.out.println(request.toString());
        Integer latestVersion = repository.findMaxVersionById(ppmId);

        if (latestVersion == null) {
            throw new RuntimeException("No versions found for PPM with ID: " + ppmId);
        }

        System.out.println("Found latest version for PPM ID " + ppmId + ": " + latestVersion);

        // Call the version-specific method with the latest version
        return updateWithVersioning(ppmId, latestVersion, request);
    }

    /**
     * Updates a PPM by creating a new version with the updated data including lots.
     * Automatically finds and uses the latest version of the PPM.
     * <p>
     * This method handles hierarchical versioning with explicit support for Lot entities:
     * - If any PPM field changes, a new PPM version is created
     * - If any Realisation is added, modified, or removed, a new PPM version is created
     * - If a Lot within a Realisation changes or is added, both the Realisation and PPM versions are incremented
     * <p>
     * This method is specifically designed to handle requests that include Lot data within Realisations.
     *
     * @param ppmId the ID of the PPM to update
     * @param ppmVer the current version of the PPM
     * @param request the request containing the updated data with Lots
     * @return the newly created version of the PPM
     * @throws RuntimeException if the PPM is not found or if all versions are deleted
     */
    @Transactional
    @Override
    public Ppm updateWithVersioningWithLots(Integer ppmId, Integer ppmVer, PpmWithLotsRequestDto request) {
        System.out.println("============== UPDATE WITH VERSIONING WITH LOTS START ==============");
        System.out.println("Updating PPM ID: " + ppmId + ", Version: " + ppmVer + " with lots");

        // Log request structure
        System.out.println("Request details:");
        System.out.println(" - Has PPM Files: " + (request.getPpmFiles() != null ? request.getPpmFiles().size() : "none"));

        // Log realisation files in the request
        if (request.getRealisationsWithLots() != null) {
            System.out.println(" - RealisationsWithLots count: " + request.getRealisationsWithLots().size());
            for (int i = 0; i < request.getRealisationsWithLots().size(); i++) {
                RealisationWithLotsRequestDto real = request.getRealisationsWithLots().get(i);
                System.out.println("   - Realisation " + i + " with ID " + (real.getId() != null ? real.getId() : "new"));
                System.out.println("     - Has files: " + (real.getRealisationFiles() != null ?
                        (!real.getRealisationFiles().isEmpty() ?
                                real.getRealisationFiles().size() + " files" : "empty list") :
                        "null"));

                // Detail each file
                if (real.getRealisationFiles() != null && !real.getRealisationFiles().isEmpty()) {
                    for (int j = 0; j < real.getRealisationFiles().size(); j++) {
                        MultipartFile file = real.getRealisationFiles().get(j);
                        if (file != null) {
                            System.out.println("       - File " + j + ": name=" + file.getOriginalFilename() +
                                    ", size=" + file.getSize() +
                                    ", content-type=" + file.getContentType());
                        } else {
                            System.out.println("       - File " + j + ": NULL");
                        }
                    }
                }
            }
        } else if (request.getRealisations() != null) {
            System.out.println(" - Regular Realisations count: " + request.getRealisations().size());
            // Check if any are actually RealisationWithLotsRequestDto with files
            boolean hasRealisationsWithFiles = false;
            for (RealisationBaseRequestDto real : request.getRealisations()) {
                if (real instanceof RealisationWithLotsRequestDto) {
                    RealisationWithLotsRequestDto withLots = (RealisationWithLotsRequestDto) real;
                    if (withLots.getRealisationFiles() != null && !withLots.getRealisationFiles().isEmpty()) {
                        hasRealisationsWithFiles = true;
                        System.out.println("   - Found RealisationWithLotsRequestDto with " +
                                withLots.getRealisationFiles().size() + " files in regular realisations list");
                        break;
                    }
                }
            }
            if (!hasRealisationsWithFiles) {
                System.out.println("   - No realisations with files found in regular realisations list");
            }
        } else {
            System.out.println(" - No realisations in request");
        }

        // Convert PpmWithLotsRequestDto to PpmBaseRequestDto for core functionality
        PpmBaseRequestDto baseRequest = new PpmBaseRequestDto();

        // Copy all basic fields
        baseRequest.setRef(request.getRef());
        baseRequest.setNom(request.getNom());
        baseRequest.setAnnee(request.getAnnee());
        baseRequest.setBudgetEstime(request.getBudgetEstime());
        baseRequest.setStatus(request.getStatus());
        baseRequest.setModePassation(request.getModePassation());
        baseRequest.setRemarque(request.getRemarque());
        baseRequest.setAcId(request.getAcId());
        baseRequest.setDocIds(request.getDocIds());

        // Get realisations with lots
        List<RealisationWithLotsRequestDto> realisationsWithLots = request.getRealisationsWithLots();

        // Convert RealisationWithLotsRequestDto to RealisationBaseRequestDto
        if (realisationsWithLots != null && !realisationsWithLots.isEmpty()) {
            List<RealisationBaseRequestDto> baseRealisations = new ArrayList<>();

            for (RealisationWithLotsRequestDto realisationWithLots : realisationsWithLots) {
                // Verify if files are present in the DTO
                if (realisationWithLots.getRealisationFiles() != null && !realisationWithLots.getRealisationFiles().isEmpty()) {
                    System.out.println("Realisation with ID " +
                            (realisationWithLots.getId() != null ? realisationWithLots.getId() : "new") +
                            " has " + realisationWithLots.getRealisationFiles().size() + " files:");
                    for (int i = 0; i < realisationWithLots.getRealisationFiles().size(); i++) {
                        MultipartFile file = realisationWithLots.getRealisationFiles().get(i);
                        if (file != null) {
                            System.out.println(" - File " + i + ": " + file.getOriginalFilename() +
                                    ", size=" + file.getSize() +
                                    ", empty=" + file.isEmpty());
                        } else {
                            System.out.println(" - File " + i + ": NULL");
                        }
                    }
                }

                // Create a new BaseRequestDto and copy fields
                RealisationBaseRequestDto baseRealisation = new RealisationBaseRequestDto();

                // Copy all fields from the WithLots DTO to the Base DTO
                baseRealisation.setId(realisationWithLots.getId());
                baseRealisation.setVersion(realisationWithLots.getVersion());
                baseRealisation.setRealisationEnvisagees(realisationWithLots.getRealisationEnvisagees());
                baseRealisation.setBeneficiaire(realisationWithLots.getBeneficiaire());
//                baseRealisation.setService(realisationWithLots.getService());
//                baseRealisation.setDirection(realisationWithLots.getDirection());
//                baseRealisation.setSourceFinancement(realisationWithLots.getSourceFinancement());
//                baseRealisation.setMonais(realisationWithLots.getMonais());
//                baseRealisation.setMonnaie(realisationWithLots.getMonnaie());
                baseRealisation.setTypeMarcher(realisationWithLots.getTypeMarcher());
//                baseRealisation.setModeSelection(realisationWithLots.getModeSelection());
                baseRealisation.setModePassation(realisationWithLots.getModePassation());
                baseRealisation.setDatePrevLps(realisationWithLots.getDatePrevLps());
                baseRealisation.setDatePrevAc(realisationWithLots.getDatePrevAc());
                baseRealisation.setDatePrevDp(realisationWithLots.getDatePrevDp());
                baseRealisation.setDatePrevAp(realisationWithLots.getDatePrevAp());
                baseRealisation.setBudgetEstime(realisationWithLots.getBudgetEstime());
                baseRealisation.setIsPublished(realisationWithLots.getIsPublished());
                baseRealisation.setPublishedAt(realisationWithLots.getPublishedAt());
                baseRealisation.setSort(realisationWithLots.getSort());
                baseRealisation.setDocIds(realisationWithLots.getDocIds());

                // Most importantly - set the lots to make sure they're included
                baseRealisation.setLots(realisationWithLots.getLots());

                // Critical: Make sure we also set the files!
                baseRealisation.setRealisationFiles(realisationWithLots.getRealisationFiles());

                if (baseRealisation.getRealisationFiles() != null) {
                    System.out.println("Files successfully copied to base realisation DTO: " +
                            baseRealisation.getRealisationFiles().size() + " files");
                } else {
                    System.out.println("No files in base realisation DTO after copying");
                }

                baseRealisations.add(baseRealisation);
            }

            baseRequest.setRealisations(baseRealisations);
            System.out.println("Set " + baseRealisations.size() + " realisations on base request");
        } else {
            // If no realisationsWithLots provided, just use the regular realisations
            baseRequest.setRealisations(request.getRealisations());
            System.out.println("Using regular realisations: " +
                    (request.getRealisations() != null ? request.getRealisations().size() : 0));
        }

        // Use the existing updateWithVersioning method with the populated base request
        System.out.println("Calling core updateWithVersioning method");
        Ppm result = updateWithVersioning(ppmId, ppmVer, baseRequest);
        System.out.println("Update complete, returning PPM version: " + result.getVersion());

        // Log the doc counts in the returned PPM
        System.out.println("Returned PPM has " + (result.getDocs() != null ? result.getDocs().size() : 0) + " documents");
        if (result.getRealisations() != null) {
            System.out.println("Returned PPM has " + result.getRealisations().size() + " realisations:");
            for (Realisation real : result.getRealisations()) {
                System.out.println(" - Realisation " + real.getId() + ", version " + real.getVersion() +
                        " has " + (real.getDocs() != null ? real.getDocs().size() : 0) + " documents");
            }
        }

        System.out.println("============== UPDATE WITH VERSIONING WITH LOTS END ==============");
        return result;
    }

    @Override
    public Ppm updateWithVersioningWithLots(Integer ppmId, PpmWithLotsRequestDto request) {
        return null;
    }

    /**
     * Helper method to compare two sets of Lots to determine if they are equal.
     *
     * @param set1 First set of Lots
     * @param set2 Second set of Lots
     * @return true if the sets contain the same Lot IDs, false otherwise
     */
    private boolean areLotSetsEqual(Set<Lot> set1, Set<Lot> set2) {
        // Handle null cases
        if (set1 == null && set2 == null) return true;
        if (set1 == null || set2 == null) return false;
        if (set1.size() != set2.size()) return false;

        // Extract IDs for comparison
        Set<Integer> ids1 = set1.stream().map(Lot::getId).collect(Collectors.toSet());
        Set<Integer> ids2 = set2.stream().map(Lot::getId).collect(Collectors.toSet());

        return ids1.equals(ids2);
    }

    // --- Helper Methods for updateWithVersioning ---

    private void logUpdateRequest(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
        System.out.println("Updating PPM ID: " + ppmId + ", Current Version: " + ppmVer);
        System.out.println("Request data: " + request);
    }

    private void logVersionDecision(boolean shouldCreate, boolean direct, boolean real, boolean docs) {
        System.out.println("Should create new PPM version: " + shouldCreate);
        System.out.println("  - Direct changes: " + direct);
        System.out.println("  - Realisations changed: " + real);
        System.out.println("  - Documents changed: " + docs);
    }

    private boolean isDuplicateRequest(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
        String requestHash = generateRequestHash(request);
        String previousRequestHash = getPreviousRequestHash(ppmId, ppmVer);
        return requestHash != null && requestHash.equals(previousRequestHash);
    }

    private void storeRequestHash(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
        String requestHash = generateRequestHash(request);
        storePreviousRequestHash(ppmId, ppmVer, requestHash);
    }

    private Ppm getExistingPpm(Integer ppmId, Integer ppmVer) {
        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        return repository.findById(compositeId)
                .orElseThrow(() -> new RuntimeException("PPM with ID " + ppmId + " and version " + ppmVer + " not found"));
    }

    private Ppm buildTempPpm(Ppm existingPpm, PpmBaseRequestDto request) {
        Ppm tempPpm = new Ppm();
//        tempPpm.setRef(request.getRef() != null ? request.getRef() : existingPpm.getRef());
        tempPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());

        tempPpm.setRemarque(request.getRemarque() != null ? request.getRemarque() : existingPpm.getRemarque());
        Ac requestAc = null;
        if (request.getAcId() != null) {
            requestAc = acRepository.findById(request.getAcId()).orElse(null);
        }
        tempPpm.setAc(requestAc != null ? requestAc : existingPpm.getAc());
        return tempPpm;
    }

    /**
     * Processes realisations and their lots, preserving the original versioning logic:
     * - Only the PPM version is incremented when Lots are added/removed/changed.
     * - Lot version is incremented if its own fields change.
     * - Realisation version is incremented only if its own fields change.
     *
     * @param existingPpm The existing PPM
     * @param request The update request
     * @param updatedRealisations The list to populate with updated realisations
     * @return true if realisations changed (for PPM versioning), false otherwise
     */
    @Transactional
    public boolean processRealisationsWithLotLogic(Ppm existingPpm, PpmBaseRequestDto request, List<Realisation> updatedRealisations)   {
        System.out.println("============== REALISATION PROCESSING LOG START ==============");
        System.out.println("Processing realisations for PPM ID " + existingPpm.getId() + ", version " + existingPpm.getVersion());

        boolean realisationsChanged = false;
        Map<String, Lot> newLotsMap = new HashMap<>();
        Set<Integer> existingRealisationIdsInRequest = new HashSet<>();
        Set<Integer> processedRealisationIds = new HashSet<>();
        Map<Integer, Realisation> existingRealisationsMap = new HashMap<>();

        // Log payload kind
        boolean hasBaseRealisations = request.getRealisations() != null && !request.getRealisations().isEmpty();
        boolean hasWithLotsRealisations = (request instanceof PpmWithLotsRequestDto)
                && ((PpmWithLotsRequestDto) request).getRealisationsWithLots() != null
                && !((PpmWithLotsRequestDto) request).getRealisationsWithLots().isEmpty();
        System.out.println("Payload: hasBaseRealisations=" + hasBaseRealisations + ", hasWithLotsRealisations=" + hasWithLotsRealisations);

        if (request instanceof PpmWithLotsRequestDto) {
            PpmWithLotsRequestDto withLotsRequest = (PpmWithLotsRequestDto) request;
            if (withLotsRequest.getRealisationsWithLots() != null) {
                System.out.println("Request contains " + withLotsRequest.getRealisationsWithLots().size() + " realisations with lots");
                for (RealisationWithLotsRequestDto realisationDto : withLotsRequest.getRealisationsWithLots()) {
                    if (realisationDto.getId() != null) {
                        existingRealisationIdsInRequest.add(realisationDto.getId());
                    }
                }
            }
        }

        if (existingPpm.getRealisations() != null) {
            for (Realisation existingRealisation : existingPpm.getRealisations()) {
                if (Boolean.TRUE.equals(existingRealisation.getIsDeleted())) continue;
                existingRealisationsMap.put(existingRealisation.getId(), existingRealisation);
            }
        }
        System.out.println("Existing realisations map size: " + existingRealisationsMap.size());

        List<? extends RealisationBaseRequestDto> realisationsToProcess = null;
        if (request instanceof PpmWithLotsRequestDto) {
            PpmWithLotsRequestDto wr = (PpmWithLotsRequestDto) request;
            if (wr.getRealisationsWithLots() != null && !wr.getRealisationsWithLots().isEmpty()) {
                realisationsToProcess = wr.getRealisationsWithLots();
            }
        }
        if (realisationsToProcess == null) {
            realisationsToProcess = request.getRealisations();
        }

        if (realisationsToProcess != null && !realisationsToProcess.isEmpty()) {
            System.out.println("Request contains " + realisationsToProcess.size() + " realisations");

            for (RealisationBaseRequestDto realisationDto : realisationsToProcess) {
                Integer realisationId = realisationDto.getId();
                System.out.println("-- Realisation loop start: requestedId=" + realisationId);

                if (realisationId != null && processedRealisationIds.contains(realisationId)) {
                    System.out.println("   Skipping duplicate realisation ID: " + realisationId);
                    continue;
                }

                if (realisationId != null) {
                    existingRealisationIdsInRequest.add(realisationId);
                    Realisation existingReal = existingRealisationsMap.get(realisationId);
                    if (existingReal == null) {
                        throw new RuntimeException("Realisation with ID " + realisationId + " not found");
                    }

                    boolean changed = false;
                    boolean chRealisationEnvisagees = !Objects.equals(realisationDto.getRealisationEnvisagees(), existingReal.getRealisationEnvisagees());
                    boolean chBeneficiaire          = !Objects.equals(realisationDto.getBeneficiaire(), existingReal.getBeneficiaire());
                    boolean chTypeMarcher           = !Objects.equals(realisationDto.getTypeMarcher().getId(), existingReal.getTypeMarcher().getId());
                    boolean chModePassation         = !Objects.equals(realisationDto.getModePassation().getId(), existingReal.getModePassation().getId());
                    boolean chDatePrevLps           = !Objects.equals(realisationDto.getDatePrevLps(), existingReal.getDatePrevLps());
                    boolean chDatePrevAc            = !Objects.equals(realisationDto.getDatePrevAc(), existingReal.getDatePrevAc());
                    boolean chDatePrevDp            = !Objects.equals(realisationDto.getDatePrevDp(), existingReal.getDatePrevDp());
                    boolean chDatePrevAp            = !Objects.equals(realisationDto.getDatePrevAp(), existingReal.getDatePrevAp());

                    BigDecimal reqBudget = realisationDto.getBudgetEstime();
                    BigDecimal exBudget  = existingReal.getBudgetEstime();
                    boolean budgetEqual = (reqBudget == null && exBudget == null) ||
                            (reqBudget != null && exBudget != null && reqBudget.compareTo(exBudget) == 0);
                    boolean chBudget = !budgetEqual;

                    System.out.println("   Scalar compare: chRealisationEnvisagees=" + chRealisationEnvisagees
                            + ", chBeneficiaire=" + chBeneficiaire
                            + ", chTypeMarcher=" + chTypeMarcher
                            + ", chModePassation=" + chModePassation
                            + ", chDatePrevLps=" + chDatePrevLps
                            + ", chDatePrevAc=" + chDatePrevAc
                            + ", chDatePrevDp=" + chDatePrevDp
                            + ", chDatePrevAp=" + chDatePrevAp
                            + ", chBudget=" + chBudget + " (req=" + reqBudget + ", ex=" + exBudget + ")");

                    // --------- LOTS CHANGE DETECTION ---------
                    List<Lot> existingLotsActive = (existingReal.getLots() == null)
                            ? new ArrayList<>()
                            : existingReal.getLots().stream()
                            .filter(l -> !Boolean.TRUE.equals(l.getIsDeleted()))
                            .collect(Collectors.toList());

                    List<LotResponse> lotDtos = null;
                    if (realisationDto instanceof RealisationWithLotsRequestDto) {
                        RealisationWithLotsRequestDto w = (RealisationWithLotsRequestDto) realisationDto;
                        if (w.getLots() != null && !w.getLots().isEmpty()) {
                            lotDtos = w.getLots();
                        }
                    }

                    boolean hasLotsInRequest = (lotDtos != null);
                    System.out.println("   Lots section: hasLotsInRequest=" + hasLotsInRequest
                            + ", existingActiveLots=" + existingLotsActive.size()
                            + ", incomingLots=" + (lotDtos != null ? lotDtos.size() : 0));

                    if (lotDtos != null) {
                        boolean chLotsCount = lotDtos.size() != existingLotsActive.size();
                        boolean chLotsContent = false;

                        if (!chLotsCount) {
                            Map<Integer, Lot> byId = existingLotsActive.stream()
                                    .collect(Collectors.toMap(Lot::getId, x -> x, (a, b) -> a));

                            for (LotResponse ld : lotDtos) {
                                Integer ldId = ld.getId();
                                if (ldId == null) { chLotsContent = true; break; }
                                Lot ex = byId.get(ldId);
                                if (ex == null) { chLotsContent = true; break; }

                                BigDecimal rb = (ld.getBudget() != null) ? ld.getBudget() : BigDecimal.ZERO;
                                BigDecimal eb = (ex.getBudget() != null) ? ex.getBudget() : BigDecimal.ZERO;
                                boolean eqBudget = rb.compareTo(eb) == 0;

                                String rd = (ld.getDescription() != null) ? ld.getDescription() : "";
                                String ed = (ex.getDescription() != null) ? ex.getDescription() : "";
                                boolean eqDescription = rd.equals(ed);

                                if (!eqBudget || !eqDescription) { chLotsContent = true; break; }
                            }
                        }

                        if (chRealisationEnvisagees || chBeneficiaire || chTypeMarcher || chModePassation
                                || chDatePrevLps || chDatePrevAc || chDatePrevDp || chDatePrevAp || chBudget
                                || chLotsCount || chLotsContent) {
                            // any change (scalar or lots)
                            changed = true;
                        }
                    } else {
                        // only scalars considered above
                        if (chRealisationEnvisagees || chBeneficiaire || chTypeMarcher || chModePassation
                                || chDatePrevLps || chDatePrevAc || chDatePrevDp || chDatePrevAp || chBudget) {
                            changed = true;
                        }
                    }

                    System.out.println("   FINAL changed for realisationId=" + realisationId + " => " + changed);

                    if (!changed) {
                        System.out.println("   NO CHANGE: Reusing existing realisation [id=" + existingReal.getId() + ", ver=" + existingReal.getVersion() + "]");
                        updatedRealisations.add(existingReal);
                        processedRealisationIds.add(realisationId);
                        continue;
                    }

                    // --------- CREATE NEW VERSION (only when changed) ---------
                    Realisation realisation = new Realisation();
                    realisation.setId(realisationId);
                    realisation.setVersion(existingReal.getVersion() + 1);
                    updateRealisationFields(realisation, realisationDto);
                    realisation.setPpms(new ArrayList<>());
                    realisation.setIsDeleted(false);

                    List<Lot> updatedLots = new ArrayList<>();
                    if (lotDtos != null && !lotDtos.isEmpty()) {
                        for (LotResponse lotDto : lotDtos) {
                            Lot lot = processLot(lotDto, newLotsMap);
                            updatedLots.add(lot);
                        }
                    } else if (existingReal.getLots() != null) {
                        List<Lot> retained = existingReal.getLots().stream()
                                .filter(l -> !Boolean.TRUE.equals(l.getIsDeleted()))
                                .collect(Collectors.toList());
                        updatedLots.addAll(retained);
                    }
                    realisation.setLots(updatedLots);

                    realisation = realisationRepository.save(realisation);
                    System.out.println("   Saved NEW VERSION realisation ID " + realisation.getId() + " version " + realisation.getVersion() +
                            " with " + (realisation.getLots() != null ? realisation.getLots().size() : 0) + " lots");

                    updatedRealisations.add(realisation);
                    processedRealisationIds.add(realisationId);
                    realisationsChanged = true;
                    System.out.println("   Marked realisationsChanged=true (updated existing realisation)");

                } else {
                    // --------- NEW REALISATION (no id) ---------
                    System.out.println("   NEW realisation (no id provided)");

                    Integer newRealId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
                    newRealId = (newRealId != null) ? newRealId + 1 : 1;

                    Realisation realisation = new Realisation();
                    realisation.setId(newRealId);
                    realisation.setVersion(1);
                    updateRealisationFields(realisation, realisationDto);
                    realisation.setPpms(new ArrayList<>());
                    realisation.setIsDeleted(false);

                    List<Lot> updatedLots = new ArrayList<>();
                    if (realisationDto instanceof RealisationWithLotsRequestDto) {
                        RealisationWithLotsRequestDto withLotsDto = (RealisationWithLotsRequestDto) realisationDto;
                        if (withLotsDto.getLots() != null && !withLotsDto.getLots().isEmpty()) {
                            for (LotResponse lotDto : withLotsDto.getLots()) {
                                Lot lot = processLot(lotDto, newLotsMap);
                                updatedLots.add(lot);
                            }
                        }
                    }
                    realisation.setLots(updatedLots);

                    realisation = realisationRepository.save(realisation);
                    System.out.println("   Saved NEW realisation ID " + realisation.getId() + " version " + realisation.getVersion() +
                            " with " + (realisation.getLots() != null ? realisation.getLots().size() : 0) + " lots");

                    updatedRealisations.add(realisation);
                    processedRealisationIds.add(realisation.getId());
                    realisationsChanged = true;
                    System.out.println("   Marked realisationsChanged=true (created new realisation)");
                }

                System.out.println("-- Realisation loop end: requestedId=" + realisationId);
            }
        } else {
            System.out.println("No realisations provided in the request payload.");
        }

        // Detach associations that disappeared from request
        if (existingPpm.getRealisations() != null) {
            for (Realisation existingRealisation : existingPpm.getRealisations()) {
                boolean missingFromRequest = !existingRealisationIdsInRequest.contains(existingRealisation.getId());
                boolean notDeleted = !Boolean.TRUE.equals(existingRealisation.getIsDeleted());
                if (missingFromRequest && notDeleted) {
                    System.out.println("Detaching existing realisation id=" + existingRealisation.getId() + " from current PPM (not present in request).");
                    realisationRepository.save(existingRealisation);
                    realisationsChanged = true;
                }
            }
        }

        System.out.println("RealisationsChanged (final) = " + realisationsChanged);
        System.out.println("============== REALISATION PROCESSING LOG END ==============");
        return realisationsChanged;
    }



    private Lot processLot(LotResponse lotDto, Map<String, Lot> newLotsMap) {
        if (lotDto.getId() != null) {
            // Find the latest version of the existing lot
            Integer lotId = lotDto.getId();
            Integer maxVersion = lotRepository.findMaxVersionById(lotId);
            if (maxVersion == null) {
                maxVersion = 0; // Start with version 1 if no versions exist
            }
            Optional<Lot> existingLotOpt = lotRepository.findById(new CompositeId(lotId, maxVersion));
            Lot existingLot = existingLotOpt.orElse(null);

            if (existingLot != null) {
                // Prepare default values for comparison
                BigDecimal requestBudget = lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO;
                String requestDesc = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                BigDecimal existingBudget = existingLot.getBudget() != null ? existingLot.getBudget() : BigDecimal.ZERO;
                String existingDesc = existingLot.getDescription() != null ? existingLot.getDescription() : "";

                // Compare with precise equality for BigDecimal
                boolean budgetChanged = requestBudget.compareTo(existingBudget) != 0;
                boolean descChanged = !requestDesc.equals(existingDesc);

                if (budgetChanged || descChanged) {
                    Lot newLot = new Lot();
                    newLot.setId(lotId);
                    newLot.setVersion(maxVersion + 1);
                    newLot.setDescription(requestDesc);
                    newLot.setBudget(requestBudget);
                    newLot.setIsDeleted(false);
                    Lot savedLot = lotRepository.save(newLot);
                    System.out.println("Created new version of Lot: " + savedLot.getId() + ", Version: " + savedLot.getVersion() +
                            ", Description: " + savedLot.getDescription() + ", Budget: " + savedLot.getBudget());
                    return savedLot;
                } else {
                    System.out.println("No changes detected, using existing Lot: " + existingLot.getId() + ", Version: " + existingLot.getVersion() +
                            ", Description: " + existingLot.getDescription() + ", Budget: " + existingLot.getBudget());
                    return existingLot;
                }
            } else {
                // Create new lot if no existing version found
                Lot newLot = new Lot();
                newLot.setId(lotId);
                newLot.setVersion(1);
                newLot.setDescription(lotDto.getDescription());
                newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO);
                newLot.setIsDeleted(false);
                Lot savedLot = lotRepository.save(newLot);
                System.out.println("Created new Lot with provided ID: " + savedLot.getId() + ", Budget: " + savedLot.getBudget());
                return savedLot;
            }
        } else {
            // Create new lot
            String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
            if (newLotsMap.containsKey(lotHash)) {
                return newLotsMap.get(lotHash);
            }
            Lot newLot = new Lot();
            newLot.setDescription(lotDto.getDescription());
            newLot.setVersion(1);
            newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO);
            newLot.setIsDeleted(false);
            Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
            Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
            newLot.setId(lotId);
            Lot savedLot = lotRepository.save(newLot);
            newLotsMap.put(lotHash, savedLot);
            System.out.println("Created new Lot with ID: " + savedLot.getId() + ", Budget: " + savedLot.getBudget());
            return savedLot;
        }
    }

    private Realisation createNewRealisation(RealisationBaseRequestDto realisationDto, Ppm existingPpm) {
        Realisation realisation = new Realisation();
        Integer maxRealisationId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
        realisation.setId(maxRealisationId != null ? maxRealisationId : 1);
        realisation.setVersion(1);
        updateRealisationFields(realisation, realisationDto);
        realisation.setPpms(new ArrayList<>());
        realisation.getPpms().add(existingPpm);
        return realisation;
    }

    private void updateRealisationFields(Realisation realisation, RealisationBaseRequestDto realisationDto) {
        realisation.setRealisationEnvisagees(realisationDto.getRealisationEnvisagees());
        realisation.setBeneficiaire(realisationDto.getBeneficiaire());
//        realisation.setService(realisationDto.getService());
//        realisation.setDirection(realisationDto.getDirection());
//        realisation.setSourceFinancement(realisationDto.getSourceFinancement());
//        realisation.setMonais(realisationDto.getMonais());
//        realisation.setMonnaie(realisationDto.getMonnaie());
        realisation.setTypeMarcher(realisationDto.getTypeMarcher());
//        realisation.setModeSelection(realisationDto.getModeSelection());
        realisation.setModePassation(realisationDto.getModePassation());
        realisation.setDatePrevLps(realisationDto.getDatePrevLps());
        realisation.setDatePrevAc(realisationDto.getDatePrevAc());
        realisation.setDatePrevDp(realisationDto.getDatePrevDp());
        realisation.setDatePrevAp(realisationDto.getDatePrevAp());
        realisation.setBudgetEstime(realisationDto.getBudgetEstime());
    }

    /**
     * Generates a content hash for a RealisationBaseRequestDto.
     * This is used to detect duplicates by comparing content rather than IDs.
     */
    private String generateRealisationDtoContentHash(RealisationBaseRequestDto dto) {
        try {
            StringBuilder sb = new StringBuilder();

            // Add basic fields
            sb.append(dto.getRealisationEnvisagees()).append("|");
            sb.append(dto.getBudgetEstime()).append("|");

            // Add dates
            sb.append(dto.getDatePrevLps()).append("|");
            sb.append(dto.getDatePrevAc()).append("|");
            sb.append(dto.getDatePrevDp()).append("|");
            sb.append(dto.getDatePrevAp()).append("|");

            // Add reference IDs
//            if (dto.getService() != null) sb.append("S").append(dto.getService().getId()).append("|");
//            if (dto.getDirection() != null) sb.append("D").append(dto.getDirection().getId()).append("|");
//            if (dto.getSourceFinancement() != null) sb.append("SF").append(dto.getSourceFinancement().getId()).append("|");
//            if (dto.getMonais() != null) sb.append("M").append(dto.getMonais().getId()).append("|");
//            if (dto.getMonnaie() != null) sb.append("M").append(dto.getMonnaie().getId()).append("|");
            if (dto.getTypeMarcher() != null) sb.append("TM").append(dto.getTypeMarcher().getId()).append("|");
//            if (dto.getModeSelection() != null) sb.append("MS").append(dto.getModeSelection().getId()).append("|");
            if (dto.getModePassation() != null) sb.append("MP").append(dto.getModePassation().getId()).append("|");


            // Add flags
            sb.append(dto.getIsPublished()).append("|");
            sb.append(dto.getPublishedAt()).append("|");
            sb.append(dto.getSort()).append("|");

            // Add lots if applicable
            if (dto instanceof RealisationWithLotsRequestDto) {
                RealisationWithLotsRequestDto lotsDto = (RealisationWithLotsRequestDto) dto;
                if (lotsDto.getLots() != null) {
                    lotsDto.getLots().stream()
                            .sorted(Comparator.comparing(lot -> lot.getDescription(), Comparator.nullsFirst(String::compareTo)))
                            .forEach(lot -> sb.append("L|").append(lot.getDescription()).append("|"));
                }
            }

            // Add document IDs if available
            if (dto.getDocIds() != null) {
                dto.getDocIds().stream()
                        .sorted()
                        .forEach(docId -> sb.append("D").append(docId).append("|"));
            }

            // Compute and return hash
            return DigestUtils.md5Hex(sb.toString()).toLowerCase();
        } catch (Exception e) {
            System.err.println("Error generating realisation DTO hash: " + e.getMessage());
            return "hash_error_" + UUID.randomUUID().toString();
        }
    }

    /**
     * Generates a content hash for a Realisation entity.
     * This is used to detect duplicates by comparing content rather than IDs.
     */
    private String generateRealisationContentHash(Realisation realisation) {
        try {
            StringBuilder sb = new StringBuilder();

            // Add basic fields
            sb.append(realisation.getRealisationEnvisagees()).append("|");
            sb.append(realisation.getBudgetEstime()).append("|");

            // Add dates
            sb.append(realisation.getDatePrevLps()).append("|");
            sb.append(realisation.getDatePrevAc()).append("|");
            sb.append(realisation.getDatePrevDp()).append("|");
            sb.append(realisation.getDatePrevAp()).append("|");

            // Add reference IDs
//            if (realisation.getService() != null) sb.append("S").append(realisation.getService().getId()).append("|");
//            if (realisation.getDirection() != null) sb.append("D").append(realisation.getDirection().getId()).append("|");
//            if (realisation.getSourceFinancement() != null) sb.append("SF").append(realisation.getSourceFinancement().getId()).append("|");
//            if (realisation.getMonais() != null) sb.append("M").append(realisation.getMonais().getId()).append("|");
            if (realisation.getMonnaie() != null) sb.append("M").append(realisation.getMonnaie().getId()).append("|");
            if (realisation.getTypeMarcher() != null) sb.append("TM").append(realisation.getTypeMarcher().getId()).append("|");
//            if (realisation.getModeSelection() != null) sb.append("MS").append(realisation.getModeSelection().getId()).append("|");
            if (realisation.getModePassation() != null) sb.append("MP").append(realisation.getModePassation().getId()).append("|");

            // Add flags
            sb.append(realisation.getIsPublished()).append("|");
            sb.append(realisation.getPublishedAt()).append("|");
            sb.append(realisation.getSort()).append("|");

            // Add lots if available
            if (realisation.getLots() != null) {
                realisation.getLots().stream()
                        .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                        .sorted(Comparator.comparing(Lot::getDescription, Comparator.nullsFirst(String::compareTo)))
                        .forEach(lot -> sb.append("L|").append(lot.getDescription()).append("|"));
            }

            // Add document IDs if available, filtering out deleted docs
            if (realisation.getDocs() != null) {
                realisation.getDocs().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                        .map(Docs::getId)
                        .sorted()
                        .forEach(docId -> sb.append("D").append(docId).append("|"));
            }

            // Compute and return hash
            return DigestUtils.md5Hex(sb.toString()).toLowerCase();
        } catch (Exception e) {
            System.err.println("Error generating realisation entity hash: " + e.getMessage());
            return "hash_error_" + UUID.randomUUID().toString();
        }
    }

    /**
     * Checks if a lot is being replaced by a new version in the request.
     *
     * @param requestLots List of lot DTOs from the request
     * @param existingLot The existing lot to check
     * @return true if the lot is being replaced, false otherwise
     */
    private boolean isLotBeingReplaced(Set<LotResponse> requestLots, Lot existingLot) {
        if (requestLots == null || existingLot == null) return false;

        for (LotResponse lotDto : requestLots) {
            // If the DTO has the same ID but a different description, it's replacing the existing lot
            if (lotDto.getId() != null && lotDto.getId().equals(existingLot.getId()) &&
                    lotDto.getDescription() != null && !lotDto.getDescription().equals(existingLot.getDescription())) {
                System.out.println("Lot " + existingLot.getId() + " description changed: "
                        + existingLot.getDescription() + " -> " + lotDto.getDescription());
                return true;
            }
        }
        return false;
    }

    /**
     * Processes lots for a realisation, preserving the original versioning logic.
     * - Only the PPM version is incremented when Lots are added/removed/changed.
     * - Lot version is incremented if its own fields change.
     * - Lots that are removed are soft deleted (isDeleted=true)
     *
     * @param existingRealisation The existing Realisation
     * @param realisationDto The DTO for the realisation
     * @param newLotsMap Map to track newly created lots
     * @return The updated set of lots
     */
    private List<Lot> processLotsForRealisation(Realisation existingRealisation, RealisationBaseRequestDto realisationDto, Map<String, Lot> newLotsMap) {
        System.out.println("==== PROCESSING LOTS FOR REALISATION ID " + existingRealisation.getId() +
                ", VERSION " + existingRealisation.getVersion() + " ====");
        List<Lot> updatedLots = new ArrayList<>();

        if (existingRealisation.getLots() != null) {
            // Start with all existing active lots
            updatedLots = existingRealisation.getLots().stream()
                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                    .collect(Collectors.toList());

            System.out.println("Starting with " + updatedLots.size() + " existing active lots for realisation " +
                    existingRealisation.getId() + ", version " + existingRealisation.getVersion());
            // Log details of existing lots
            updatedLots.forEach(lot ->
                    System.out.println("  - Existing lot: ID=" + lot.getId() + ", version=" + lot.getVersion() +
                            ", description=" + lot.getDescription() +
                            ", realisations=" + (lot.getRealisations() != null ? lot.getRealisations().size() : "none"))
            );
        } else {
            System.out.println("No existing lots found for realisation " + existingRealisation.getId());
        }

        // Process the lots from the request
        if (realisationDto instanceof RealisationWithLotsRequestDto) {
            RealisationWithLotsRequestDto withLotsDto = (RealisationWithLotsRequestDto) realisationDto;

            if (withLotsDto.getLots() != null && !withLotsDto.getLots().isEmpty()) {
                System.out.println("Processing " + withLotsDto.getLots().size() + " lots from request:");
                // Log details of lots in request
                withLotsDto.getLots().forEach(lotResponse ->
                        System.out.println("  - Request lot: ID=" + lotResponse.getId() +
                                ", version=" + lotResponse.getVersion() +
                                ", description=" + lotResponse.getDescription())
                );

                // Track lots to keep
                Set<Integer> requestLotIds = withLotsDto.getLots().stream()
                        .filter(lot -> lot.getId() != null)
                        .map(LotResponse::getId)
                        .collect(Collectors.toSet());

                System.out.println("Lot IDs in request: " + requestLotIds);

                // Mark lots for deletion that aren't in the request anymore
                if (existingRealisation.getLots() != null) {
                    for (Lot existingLot : existingRealisation.getLots()) {
                        if (!requestLotIds.contains(existingLot.getId()) && !Boolean.TRUE.equals(existingLot.getIsDeleted())) {
                            System.out.println("Marking lot " + existingLot.getId() + " as deleted (not in request)");
                            existingLot.setIsDeleted(true);
                            lotRepository.save(existingLot);

                            // Remove it from the updated lots
                            updatedLots.removeIf(lot -> lot.getId().equals(existingLot.getId()));
                        }
                    }
                }

                // Process each lot from the request
                for (LotResponse lotResponse : withLotsDto.getLots()) {
                    System.out.println("Processing lot from request: ID=" + lotResponse.getId() +
                            ", description=" + lotResponse.getDescription());

                    if (lotResponse.getId() != null) {
                        // This is an existing lot - check if it needs updating
                        Integer lotId = lotResponse.getId();
                        Integer lotVersion = lotResponse.getVersion();

                        if (lotVersion == null) {
                            // Find the latest version
                            lotVersion = lotRepository.findMaxVersionById(lotId);
                            if (lotVersion == null) {
                                System.err.println("No versions found for lot ID " + lotId);
                                continue;
                            }
                            System.out.println("Found latest version for lot ID " + lotId + ": version=" + lotVersion);
                        }

                        // Find the existing lot
                        Lot existingLot = lotRepository.findById(new CompositeId(lotId, lotVersion))
                                .orElse(null);

                        if (existingLot == null) {
                            System.err.println("Lot not found: ID " + lotId + ", version " + lotVersion);
                            continue;
                        }

                        System.out.println("Found existing lot: ID=" + existingLot.getId() +
                                ", version=" + existingLot.getVersion() +
                                ", description=" + existingLot.getDescription());

                        // Check if the lot needs updating (description changed)
                        boolean descriptionChanged = !Objects.equals(existingLot.getDescription(), lotResponse.getDescription());

                        if (descriptionChanged) {
                            System.out.println("Lot description changed for lot " + existingLot.getId() +
                                    ": " + existingLot.getDescription() + " -> " + lotResponse.getDescription());

                            // Create a new version of the lot
                            Integer newVersion = existingLot.getVersion() + 1;
                            Lot newLot = new Lot();
                            newLot.setId(existingLot.getId());
                            newLot.setVersion(newVersion);
                            newLot.setDescription(lotResponse.getDescription());

                            // Set up relationships
                            Set<Realisation> realisations = new HashSet<>();
                            if (existingLot.getRealisations() != null) {
                                System.out.println("Copying existing relationships from lot " + existingLot.getId() +
                                        ": " + existingLot.getRealisations().size() + " realisations");
                                existingLot.getRealisations().forEach(real ->
                                        System.out.println("  - Existing relationship with realisation: ID=" +
                                                real.getId() + ", version=" + real.getVersion())
                                );
                                realisations.addAll(existingLot.getRealisations());
                            }

                            // Ensure this realization is included
                            if (!realisations.contains(existingRealisation)) {
                                System.out.println("Adding current realisation " + existingRealisation.getId() +
                                        " to relationships of lot " + existingLot.getId());
                                realisations.add(existingRealisation);
                            } else {
                                System.out.println("Current realisation " + existingRealisation.getId() +
                                        " is already in relationships of lot " + existingLot.getId());
                            }

                            newLot.setRealisations(realisations);

                            // Save the new lot version
                            newLot = lotRepository.save(newLot);
                            System.out.println("Created new lot version: ID " + newLot.getId() +
                                    ", version " + newLot.getVersion() +
                                    ", has " + newLot.getRealisations().size() + " realisation relationships");

                            // Replace old version with new
                            updatedLots.removeIf(lot -> lot.getId().equals(existingLot.getId()));
                            updatedLots.add(newLot);
                        } else {
                            // No changes needed, just make sure it's in the updated lots
                            if (!updatedLots.contains(existingLot)) {
                                updatedLots.add(existingLot);
                                System.out.println("Keeping unchanged lot " + existingLot.getId());

                                // Ensure the relationship with this realisation exists
                                if (existingLot.getRealisations() == null) {
                                    System.out.println("Lot " + existingLot.getId() + " has no realisations set, creating new set");
                                    existingLot.setRealisations(new HashSet<>());
                                    existingLot.getRealisations().add(existingRealisation);
                                    lotRepository.save(existingLot);
                                } else if (!existingLot.getRealisations().contains(existingRealisation)) {
                                    System.out.println("Adding current realisation " + existingRealisation.getId() +
                                            " to relationships of unchanged lot " + existingLot.getId());
                                    existingLot.getRealisations().add(existingRealisation);
                                    lotRepository.save(existingLot);
                                }
                            }
                        }
                    } else {
                        // This is a new lot - add it to the existing realization
                        System.out.println("Creating new lot with description: " + lotResponse.getDescription());

                        // First check if we already created this lot (by description)
                        Lot existingNewLot = newLotsMap.get(lotResponse.getDescription());

                        if (existingNewLot != null) {
                            // We already created this lot, just add it if not already in the list
                            if (!updatedLots.contains(existingNewLot)) {
                                updatedLots.add(existingNewLot);
                                System.out.println("Reusing existing new lot: ID " + existingNewLot.getId() +
                                        ", version " + existingNewLot.getVersion());

                                // Make sure realisation relationship is set
                                if (existingNewLot.getRealisations() == null) {
                                    existingNewLot.setRealisations(new HashSet<>());
                                }

                                // Ensure this realization is included
                                if (!existingNewLot.getRealisations().contains(existingRealisation)) {
                                    existingNewLot.getRealisations().add(existingRealisation);
                                    System.out.println("Adding realisation " + existingRealisation.getId() +
                                            " to reused lot " + existingNewLot.getId());
                                    lotRepository.save(existingNewLot);
                                } else {
                                    System.out.println("Realisation " + existingRealisation.getId() +
                                            " already associated with reused lot " + existingNewLot.getId());
                                }
                            }
                        } else {
                            // Create a brand new lot
                            Lot newLot = new Lot();

                            // Generate ID if not provided
//                            Integer maxLotId = lotRepository.findMaxId();
                            Integer maxLotId =  sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                            Integer newLotId = (maxLotId != null) ? maxLotId + 1 : 1;
                            newLot.setId(newLotId);
                            newLot.setVersion(1); // Initial version
                            newLot.setDescription(lotResponse.getDescription());

                            System.out.println("Generated new lot ID: " + newLotId);

                            // Set up relationship with existing realization
                            Set<Realisation> realisations = new HashSet<>();
                            realisations.add(existingRealisation);
                            newLot.setRealisations(realisations);

                            // Save the new lot
                            Lot savedLot = lotRepository.save(newLot);
                            System.out.println("Created brand new lot: ID " + savedLot.getId() +
                                    ", version " + savedLot.getVersion() +
                                    ", has realisations: " +
                                    (savedLot.getRealisations() != null ?
                                            savedLot.getRealisations().size() : "none"));

                            // Add to our tracking collections
                            updatedLots.add(savedLot);
                            newLotsMap.put(lotResponse.getDescription(), savedLot);
                        }
                    }
                }
            }
        }

        System.out.println("Final lot count for realisation " + existingRealisation.getId() +
                ", version " + existingRealisation.getVersion() + ": " + updatedLots.size());

        // Log the final set of lots
        System.out.println("Final lots for realisation " + existingRealisation.getId() + ":");
        updatedLots.forEach(lot ->
                System.out.println("  - Lot: ID=" + lot.getId() +
                        ", version=" + lot.getVersion() +
                        ", description=" + lot.getDescription() +
                        ", realisations=" + (lot.getRealisations() != null ? lot.getRealisations().size() : "none"))
        );

        System.out.println("==== FINISHED PROCESSING LOTS FOR REALISATION ID " + existingRealisation.getId() + " ====");
        return updatedLots;
    }

    /**
     * Checks if the lots have changed for a realisation (add/remove/change).
     * @return true if lots have changed, false otherwise
     */
    private boolean lotsChanged(Realisation existingRealisation, RealisationBaseRequestDto realisationDto, List<Lot> updatedLots) {
        // Early exit if one has lots and the other doesn't
        boolean existingHasLots = existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty();
        boolean dtoHasLots = realisationDto instanceof RealisationWithLotsRequestDto &&
                ((RealisationWithLotsRequestDto) realisationDto).getLots() != null &&
                !((RealisationWithLotsRequestDto) realisationDto).getLots().isEmpty();

        if (existingHasLots != dtoHasLots) {
            System.out.println("Lots changed: one has lots, the other doesn't");
            return true;
        }

        // If neither has lots, no change
        if (!existingHasLots && !dtoHasLots) {
            return false;
        }

        // At this point, both have lots
        RealisationWithLotsRequestDto realisationWithLotsDto = (RealisationWithLotsRequestDto) realisationDto;
        List<LotResponse> requestLots = realisationWithLotsDto.getLots();

        // Create maps for both existing and request lots using description as key for content-based comparison
        Map<String, Lot> existingLotsMap = new HashMap<>();
        if (existingRealisation.getLots() != null) {
            for (Lot lot : existingRealisation.getLots()) {
                if (!Boolean.TRUE.equals(lot.getIsDeleted())) {
                    existingLotsMap.put(lot.getDescription(), lot);
                }
            }
        }

        Map<String, LotResponse> requestLotsMap = new HashMap<>();
        for (LotResponse lotResponse : requestLots) {
            requestLotsMap.put(lotResponse.getDescription(), lotResponse);
        }

        // Compare lot descriptions (content) rather than just IDs
        if (existingLotsMap.size() != requestLotsMap.size()) {
            System.out.println("Lot count differs: " + existingLotsMap.size() + " vs " + requestLotsMap.size());
            return true;
        }

        // Check if the lot descriptions match
        for (String description : existingLotsMap.keySet()) {
            if (!requestLotsMap.containsKey(description)) {
                System.out.println("Lot description not found in request: " + description);
                return true;
            }
        }

        // If we get here, both have the same lot descriptions - they are content-equivalent
        return false;
    }

    /**
     * Processes documents and determines if they have changed.
     * @return true if documents have changed, false otherwise
     */
    private boolean processDocuments(Ppm existingPpm, PpmBaseRequestDto request, List<Docs> updatedDocs) {
        boolean documentsChanged = false;

        // Get only the active (non-deleted) existing document IDs
        Set<Integer> activeExistingDocIds = existingPpm.getDocs() != null ?
                existingPpm.getDocs().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                        .map(Docs::getId)
                        .collect(Collectors.toSet()) : new HashSet<>();

        // Handle null docIds as empty set
        Set<Integer> requestDocIds = request.getDocIds() != null ?
                new HashSet<>(request.getDocIds()) :
                new HashSet<>();

        // Compare document sets - sort both sets before comparison to ensure consistent order
        List<Integer> sortedExistingIds = new ArrayList<>(activeExistingDocIds);
        List<Integer> sortedRequestIds = new ArrayList<>(requestDocIds);
        Collections.sort(sortedExistingIds);
        Collections.sort(sortedRequestIds);

        documentsChanged = !sortedExistingIds.equals(sortedRequestIds);

        if (documentsChanged) {
            System.out.println("Document changes detected for PPM " + existingPpm.getId() + ":");
            System.out.println("  - Active existing docs: " + sortedExistingIds);
            System.out.println("  - Requested docs: " + sortedRequestIds);

            // Log details about what's changed for better debugging
            if (sortedExistingIds.size() != sortedRequestIds.size()) {
                System.out.println("  - Document count changed: " + sortedExistingIds.size() + " -> " + sortedRequestIds.size());
            }

            List<Integer> added = new ArrayList<>(sortedRequestIds);
            added.removeAll(sortedExistingIds);
            if (!added.isEmpty()) {
                System.out.println("  - Documents added: " + added);
            }

            List<Integer> removed = new ArrayList<>(sortedExistingIds);
            removed.removeAll(sortedRequestIds);
            if (!removed.isEmpty()) {
                System.out.println("  - Documents removed: " + removed);
            }

            // Clear the list and add the requested documents
            updatedDocs.clear();

            // First, handle existing documents - mark some as deleted
            if (existingPpm.getDocs() != null) {
                for (Docs existingDoc : existingPpm.getDocs()) {
                    // Get a fresh copy to avoid stale data
                    Docs doc = docsRepository.findById(existingDoc.getId()).orElse(null);
                    if (doc == null) continue;

                    if (requestDocIds.contains(doc.getId())) {
                        // Keep document active
                        doc.setIsDeleted(false);
                        doc = docsRepository.save(doc);
                        updatedDocs.add(doc);
                        System.out.println("Keeping document " + doc.getId() + " active");
                    } else {
                        // Mark document as deleted
                        doc.setIsDeleted(true);
                        doc = docsRepository.save(doc);
                        updatedDocs.add(doc);
                        System.out.println("Marked document " + doc.getId() + " as DELETED");
                    }
                }
            }

            // Add any new documents that didn't exist before
            for (Integer docId : requestDocIds) {
                // Skip if we've already processed this document
                if (updatedDocs.stream().anyMatch(doc -> doc.getId().equals(docId))) {
                    continue;
                }

                // This is a new document to add
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
                doc.setIsDeleted(false);
                doc = docsRepository.save(doc);
                updatedDocs.add(doc);
                System.out.println("Added new document " + doc.getId() + " to list");
            }
        } else {
            System.out.println("No document changes detected for PPM " + existingPpm.getId());
            System.out.println("  - Documents: " + sortedExistingIds);

            // No changes detected, just use existing active documents
            updatedDocs.clear();
            if (existingPpm.getDocs() != null) {
                existingPpm.getDocs().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                        .forEach(updatedDocs::add);
            }
        }

        return documentsChanged;
    }

    /**
     * Processes realisation documents and determines if they have changed.
     * This method is called during the versioning process to check if documents
     * associated with realisations have changed.
     *
     * @param existingRealisation The existing realisation to check
     * @param realisationDto The DTO containing updated document IDs
     * @param updatedDocs List to populate with updated document objects
     * @return true if documents have changed, false otherwise
     */
    private boolean processRealisationDocuments(Realisation existingRealisation, RealisationBaseRequestDto realisationDto, List<Docs> updatedDocs) {
        boolean documentsChanged = false;

        // Get only the active (non-deleted) existing document IDs
        Set<Integer> activeExistingDocIds = existingRealisation.getDocs() != null ?
                existingRealisation.getDocs().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                        .map(Docs::getId)
                        .collect(Collectors.toSet()) : new HashSet<>();

        // Handle null docIds as empty set
        Set<Integer> requestDocIds = realisationDto.getDocIds() != null ?
                new HashSet<>(realisationDto.getDocIds()) :
                new HashSet<>();

        // Compare document sets
        boolean docsAdded = !requestDocIds.isEmpty() &&
                !activeExistingDocIds.containsAll(requestDocIds);

        boolean docsRemoved = !activeExistingDocIds.isEmpty() &&
                !requestDocIds.containsAll(activeExistingDocIds);

        documentsChanged = docsAdded || docsRemoved;

        if (documentsChanged) {
            System.out.println("Document changes detected for Realisation " + existingRealisation.getId() + ":");
            System.out.println("  - Active existing docs: " + activeExistingDocIds);
            System.out.println("  - Requested docs: " + requestDocIds);
            System.out.println("  - Documents added: " + docsAdded);
            System.out.println("  - Documents removed: " + docsRemoved);

            // Clear the list and add the requested documents
            updatedDocs.clear();
            for (Integer docId : requestDocIds) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
                // Ensure the document is marked as active
                doc.setIsDeleted(false);
                updatedDocs.add(doc);
            }
        } else {
            // No changes detected, just use existing active documents
            updatedDocs.clear();
            if (existingRealisation.getDocs() != null) {
                existingRealisation.getDocs().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                        .forEach(updatedDocs::add);
            }
        }

        return documentsChanged;
    }

    /**
     * Helper method to check if realisation documents have been modified.
     * This is called during realisation processing to determine if document changes
     * should trigger a version update of the realisation.
     */
    private boolean checkRealisationDocumentsChanged(Realisation existingRealisation, RealisationBaseRequestDto realisationDto) {
        System.out.println("Checking document changes for realisation " + existingRealisation.getId() + ":");

        // Get the active document IDs from the existing realisation
        Set<Integer> existingDocIds = new HashSet<>();
        if (existingRealisation.getDocs() != null) {
            for (Docs doc : existingRealisation.getDocs()) {
                if (!Boolean.TRUE.equals(doc.getIsDeleted())) {
                    existingDocIds.add(doc.getId());
                }
            }
        }

        // Get document IDs from the request
        Set<Integer> requestedDocIds = new HashSet<>();
        if (realisationDto.getDocIds() != null) {
            requestedDocIds.addAll(realisationDto.getDocIds());
        }

        // Sort both sets for deterministic comparison
        List<Integer> sortedExistingIds = new ArrayList<>(existingDocIds);
        List<Integer> sortedRequestedIds = new ArrayList<>(requestedDocIds);
        Collections.sort(sortedExistingIds);
        Collections.sort(sortedRequestedIds);

        // Check if there are changes
        boolean changed = !sortedExistingIds.equals(sortedRequestedIds);

        System.out.println("  - Existing docs: " + sortedExistingIds);
        System.out.println("  - Requested docs: " + sortedRequestedIds);
        System.out.println("  - Documents changed: " + changed);

        if (changed && sortedExistingIds.size() != sortedRequestedIds.size()) {
            System.out.println("  - Document count changed: " + sortedExistingIds.size() + " -> " + sortedRequestedIds.size());
        }

        if (changed) {
            List<Integer> added = new ArrayList<>(sortedRequestedIds);
            added.removeAll(sortedExistingIds);
            if (!added.isEmpty()) {
                System.out.println("  - Documents added: " + added);
            }

            List<Integer> removed = new ArrayList<>(sortedExistingIds);
            removed.removeAll(sortedRequestedIds);
            if (!removed.isEmpty()) {
                System.out.println("  - Documents removed: " + removed);
            }
        }

        return changed;
    }

    /**
     * Modified realisationNeedsUpdate to do a more complete comparison of all fields.
     */
    private boolean realisationNeedsUpdate(Realisation existingRealisation, RealisationBaseRequestDto realisationDto) {
        System.out.println("==== CHECKING IF REALISATION " + existingRealisation.getId() + " NEEDS UPDATE ====");

        // Check if documents have changed
        boolean docsChanged = checkRealisationDocumentsChanged(existingRealisation, realisationDto);

        // Check if text has changed
        boolean textChanged = fieldChanged(
                existingRealisation.getRealisationEnvisagees(),
                realisationDto.getRealisationEnvisagees()
        );

        if (textChanged) {
            System.out.println("  - Text changed");
            System.out.println("    - Existing: " + (existingRealisation.getRealisationEnvisagees() != null ? existingRealisation.getRealisationEnvisagees().substring(0, Math.min(existingRealisation.getRealisationEnvisagees().length(), 50)) + "..." : "null"));
            System.out.println("    - New: " + (realisationDto.getRealisationEnvisagees() != null ? realisationDto.getRealisationEnvisagees().substring(0, Math.min(realisationDto.getRealisationEnvisagees().length(), 50)) + "..." : "null"));
        }

//        // Check other fields
//        boolean serviceChanged = fieldChanged(
//                existingRealisation.getService() != null ? existingRealisation.getService().getId() : null,
//                realisationDto.getService() != null ? realisationDto.getService().getId() : null
//        );
//
//        boolean directionChanged = fieldChanged(
//                existingRealisation.getDirection() != null ? existingRealisation.getDirection().getId() : null,
//                realisationDto.getDirection() != null ? realisationDto.getDirection().getId() : null
//        );

//        boolean sourceFinChanged = fieldChanged(
//                existingRealisation.getSourceFinancement() != null ? existingRealisation.getSourceFinancement().getId() : null,
//                realisationDto.getSourceFinancement() != null ? realisationDto.getSourceFinancement().getId() : null
//        );

//        boolean monaisChanged = fieldChanged(
//            existingRealisation.getMonais() != null ? existingRealisation.getMonais().getId() : null,
//            realisationDto.getMonais() != null ? realisationDto.getMonais().getId() : null
//        );
//        boolean monaisChanged = fieldChanged(
//                existingRealisation.getMonnaie() != null ? existingRealisation.getMonnaie().getId() : null,
//                realisationDto.getMonnaie() != null ? realisationDto.getMonnaie().getId() : null
//        );

        boolean typeMarcherChanged = fieldChanged(
                existingRealisation.getTypeMarcher() != null ? existingRealisation.getTypeMarcher().getId() : null,
                realisationDto.getTypeMarcher() != null ? realisationDto.getTypeMarcher().getId() : null
        );

//        boolean modeSelectionChanged = fieldChanged(
//            existingRealisation.getModeSelection() != null ? existingRealisation.getModeSelection().getId() : null,
//            realisationDto.getModeSelection() != null ? realisationDto.getModeSelection().getId() : null
//        );

        boolean modePassationChanged = fieldChanged(
                existingRealisation.getModePassation() != null ? existingRealisation.getModePassation().getId() : null,
                realisationDto.getModePassation() != null ? realisationDto.getModePassation().getId() : null
        );

        // Log field changes for debugging
//        if (serviceChanged) System.out.println("  - Service changed");
//        if (directionChanged) System.out.println("  - Direction changed");
//        if (sourceFinChanged) System.out.println("  - Source financement changed");
//        if (monaisChanged) System.out.println("  - Monais changed");
        if (typeMarcherChanged) System.out.println("  - Type marcher changed");
        if (modePassationChanged) System.out.println("  - Mode passation changed");

        // Important: Lot changes should NOT trigger a new Realisation version
        // Just checking the actual field changes, not lot relationships
        boolean result = docsChanged || textChanged  || typeMarcherChanged || modePassationChanged;

        System.out.println("Realisation needs update: " + result);
        return result;
    }

    /**
     * Helper method to check if a field has changed
     */
    private <T> boolean fieldChanged(T existingValue, T newValue) {
        if (existingValue == null && newValue == null) return false;
        if (existingValue == null || newValue == null) return true;
        return !existingValue.equals(newValue);
    }

    /**
     * Check if two PPMs have identical direct fields.
     *
     * @param ppm1 First PPM
     * @param ppm2 Second PPM
     * @return true if all direct fields are identical, false otherwise
     */
    private boolean arePpmFieldsIdentical(Ppm ppm1, Ppm ppm2) {
        if (ppm1 == ppm2) return true;
        if (ppm1 == null || ppm2 == null) return false;

        return  Objects.equals(ppm1.getNom(), ppm2.getNom()) &&
                Objects.equals(ppm1.getRemarque(), ppm2.getRemarque()) &&
                Objects.equals(getAcId(ppm1.getAc()), getAcId(ppm2.getAc()));
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
    private String generateRequestHash(PpmBaseRequestDto request) {
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

        // Document IDs - sort them to ensure consistent ordering
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

        // Include realisations in the hash
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            sb.append("realisations:[");
            for (RealisationBaseRequestDto realisation : request.getRealisations()) {
                sb.append("real[");
                sb.append("id:").append(realisation.getId() != null ? realisation.getId() : "").append(",");
                sb.append("ver:").append(realisation.getVersion() != null ? realisation.getVersion() : "").append(",");
                sb.append("text:").append(realisation.getRealisationEnvisagees() != null ? realisation.getRealisationEnvisagees().substring(0, Math.min(realisation.getRealisationEnvisagees().length(), 20)) : "").append(",");

                // Include other fields that should trigger a version change - using ID values
//                sb.append("service:").append(realisation.getService() != null && realisation.getService().getId() != null ? realisation.getService().getId() : "").append(",");
//                sb.append("direction:").append(realisation.getDirection() != null && realisation.getDirection().getId() != null ? realisation.getDirection().getId() : "").append(",");
//                sb.append("sourceFin:").append(realisation.getSourceFinancement() != null && realisation.getSourceFinancement().getId() != null ? realisation.getSourceFinancement().getId() : "").append(",");
//                sb.append("monais:").append(realisation.getMonais() != null && realisation.getMonais().getId() != null ? realisation.getMonais().getId() : "").append(",");
//                sb.append("monnaie:").append(realisation.getMonnaie() != null && realisation.getMonnaie().getId() != null ? realisation.getMonnaie().getId() : "").append(",");
                sb.append("typeMarcher:").append(realisation.getTypeMarcher() != null && realisation.getTypeMarcher().getId() != null ? realisation.getTypeMarcher().getId() : "").append(",");
//                sb.append("modeSelection:").append(realisation.getModeSelection() != null && realisation.getModeSelection().getId() != null ? realisation.getModeSelection().getId() : "").append(",");
                sb.append("modePassation:").append(realisation.getModePassation() != null && realisation.getModePassation().getId() != null ? realisation.getModePassation().getId() : "").append(",");

                // Add document IDs in sorted order
                if (realisation.getDocIds() != null && !realisation.getDocIds().isEmpty()) {
                    sb.append("docIds:");
                    List<Integer> sortedDocIds = new ArrayList<>(realisation.getDocIds());
                    Collections.sort(sortedDocIds);
                    for (Integer docId : sortedDocIds) {
                        sb.append(docId).append(",");
                    }
                    sb.append(";");
                }

                if (realisation instanceof RealisationWithLotsRequestDto) {
                    RealisationWithLotsRequestDto withLots = (RealisationWithLotsRequestDto) realisation;
                    if (withLots.getLots() != null && !withLots.getLots().isEmpty()) {
                        // Sort lots by description for stable order and to ignore auto-assigned IDs
                        List<LotResponse> sortedLots = new ArrayList<>(withLots.getLots());
                        sortedLots.sort(Comparator.comparing(l -> l.getDescription() != null ? l.getDescription() : ""));

                        sb.append("lots:[");
                        for (LotResponse lot : sortedLots) {
                            sb.append("{");
                            // Include ID if present
                            if (lot.getId() != null) {
                                sb.append("id:").append(lot.getId()).append(",");
                            }
                            sb.append("desc:").append(lot.getDescription() != null ? lot.getDescription() : "");
                            sb.append("},");
                        }
                        sb.append("]");
                    } else {
                        sb.append("lots:[]");
                    }
                }

                // Process financements for hash calculation
                if (realisation.getFinancements() != null && !realisation.getFinancements().isEmpty()) {
                    sb.append("financements:[");
                    for (FinancementRequest financement : realisation.getFinancements()) {
                        sb.append("{");
                        if (financement.getFinancementId() != null) {
                            sb.append("id:").append(financement.getFinancementId()).append(",");
                        } else {
                            // Make each new financement request unique with timestamp
                            sb.append("id:NEW-").append(System.currentTimeMillis()).append(",");
                        }
                        sb.append("monnaie:").append(financement.getMonnaieId() != null ? financement.getMonnaieId() : "").append(",");
                        sb.append("budget:").append(financement.getBudgetEstimer() != null ? financement.getBudgetEstimer() : "").append(",");
                        sb.append("source:").append(financement.getSourceFinancement() != null &&
                                financement.getSourceFinancement().getId() != null ?
                                financement.getSourceFinancement().getId() : "null");
                        sb.append("},");
                    }
                    sb.append("];");
                } else {
                    sb.append("financements:[];");
                }
                sb.append("];");
            }
            sb.append("]");
        } else {
            sb.append("realisations:[];");
        }

        String hashString = sb.toString();
        System.out.println("Generated request hash from string: " + hashString.substring(0, Math.min(100, hashString.length())) + "...");
        return hashString;
    }

//    private List<Realisation> filterLatestRealisationVersions(List<Realisation> realisations) {
//        if (realisations == null || realisations.isEmpty()) {
//            return realisations;
//        }
//
//        // Group realisations by ID and keep only the latest version
//        Map<Integer, Realisation> latestVersionMap = new HashMap<>();
//
//        for (Realisation realisation : realisations) {
//            Integer realisationId = realisation.getId();
//            Integer version = realisation.getVersion();
//
//            if (!latestVersionMap.containsKey(realisationId) ||
//                    version > latestVersionMap.get(realisationId).getVersion()) {
//                latestVersionMap.put(realisationId, realisation);
//            }
//        }
//
//        // Return only the latest versions, sorted by ID for consistency
//        return latestVersionMap.values().stream()
//                .sorted(Comparator.comparing(Realisation::getId))
//                .collect(Collectors.toList());
//    }

    private List<Realisation> filterLatestRealisationVersions(List<Realisation> realisations) {
        if (realisations == null || realisations.isEmpty()) {
            return realisations;
        }

        System.out.println("Filtering " + realisations.size() + " realisation versions");

        // Group realisations by ID and keep only the latest version
        Map<Integer, Realisation> latestVersionMap = new HashMap<>();

        for (Realisation realisation : realisations) {
            Integer realisationId = realisation.getId();
            Integer version = realisation.getVersion();

            System.out.println("Processing Realisation ID: " + realisationId + ", Version: " + version);

            if (!latestVersionMap.containsKey(realisationId) ||
                    version > latestVersionMap.get(realisationId).getVersion()) {
                latestVersionMap.put(realisationId, realisation);
                System.out.println("  - Updated latest version for ID " + realisationId + " to version " + version);
            } else {
                System.out.println("  - Skipping older version " + version + " for ID " + realisationId);
            }
        }

        // Return only the latest versions, sorted by ID for consistency
        List<Realisation> result = latestVersionMap.values().stream()
                .sorted(Comparator.comparing(Realisation::getId))
                .collect(Collectors.toList());

        System.out.println("Final filtered result: " + result.size() + " realisations");
        return result;
    }

    // Simple in-memory cache for request hashes
    private static final Map<String, String> requestHashCache = new HashMap<>();

    /**
     * Gets the hash of the previous request for a PPM ID and version.
     *
     * @param ppmId The PPM ID
     * @param ppmVer The PPM version
     * @return The hash string or null if no previous request
     */
    private String getPreviousRequestHash(Integer ppmId, Integer ppmVer) {
        String key = ppmId + "-" + ppmVer;
        return requestHashCache.get(key);
    }

    /**
     * Stores the hash of a request for a PPM ID and version.
     *
     * @param ppmId The PPM ID
     * @param ppmVer The PPM version
     * @param hash The hash string to store
     */
    private void storePreviousRequestHash(Integer ppmId, Integer ppmVer, String hash) {
        String key = ppmId + "-" + ppmVer;
        requestHashCache.put(key, hash);
    }

    /**
     * Creates a new PPM with full support for relationships including documents.
     * This method creates a PPM with its realisations, lots, and documents.
     *
     * @param request the request containing PPM data, realisations, lots, and document files
     * @return the created PPM with all relationships established
     */

    @Transactional
    @Override
    public Ppm createWithRelationships(PpmWithLotsRequestDto request) throws AcNotFound {
//        Integer ppmId = repository.findMaxId();
        Integer ppmId =  sequenceGeneratorService.getNextId(Constants.PPM_ID_SEQ);
        ppmId = (ppmId != null) ? ppmId + 1 : 1;
        Integer ppmVersion = 1;

        Ppm ppm = new Ppm();
        ppm.setId(ppmId);
        ppm.setVersion(ppmVersion);
        ppm.setNom(request.getNom());
        ppm.setBudgetEstime(request.getBudgetEstime());
        String currentYearStr = String.valueOf(LocalDate.now().getYear());
        ppm.setAnnee(currentYearStr);


        ppm.setStatus(ProcessStatus.BROUILLON);
        if (request.getModePassation() != null && request.getModePassation().getId() != null) {
            RefParam modePassation = refParamRepository.findById(request.getModePassation().getId())
                    .orElseThrow(() -> new PaaDefaultStatus("Mode Passation not found with ID: " + request.getModePassation().getId()));
            ppm.setModePassation(modePassation);
        } else {
            System.out.println("Mode Passation is not provided in the request; proceeding with null value");
            ppm.setModePassation(null);
        }

        ppm.setRemarque(request.getRemarque());
        setRef(ppm);


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
            ppm.setAc(acService.findById(requestAcId));
        } else {
            Optional<UserAc> piloteAc = userAcs.stream()
                    .filter(UserAc::getAcPilote)
                    .findFirst();
            if (piloteAc.isPresent()) {
                ppm.setAc(acService.findById(piloteAc.get().getAcId()));
            } else {
                if (!userAcs.isEmpty()) {
                    ppm.setAc(acService.findById(userAcs.get(0).getAcId()));
                } else {
                    throw new NotFoundException("No AC IDs available to assign");
                }
            }
        }

        // Set empty collections to avoid NPEs
        ppm.setRealisations(new ArrayList<>());
        ppm.setDocs(new ArrayList<>());

        // Save PPM initially without associations
        Ppm savedPpm = repository.save(ppm);
        System.out.println("Initial save of PPM - ID: " + savedPpm.getId() + ", Version: " + savedPpm.getVersion());

        // Handle documents if provided
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = new ArrayList<>();
            for (Integer docId : request.getDocIds()) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                // Set up bidirectional relationship
                if (doc.getPpms() == null) {
                    doc.setPpms(new ArrayList<>());
                }
                doc.getPpms().add(savedPpm);
                docs.add(doc);

                // Save the document with updated relationship
                docsRepository.save(doc);
            }

            // Update PPM with documents
            savedPpm.setDocs(docs);
            System.out.println("Added " + docs.size() + " documents to PPM");
        }

        // Process document files if provided
        if (request.getPpmFiles() != null) {
            try {
                System.out.println("Processing " + request.getPpmFiles().size() + " files for PPM");
                // processAndSaveDocuments(request.getPpmFiles(), savedPpm);
                documentCommuneService.processAndSaveDocuments(request.getPpmFiles(),savedPpm,Constants.PATH_ENTREPOT_GED_PPM);

                System.out.println("Processed PPM files successfully");
            } catch (IOException e) {
                System.err.println("Error processing PPM files: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        List<Realisation> realisationsToSave = new ArrayList<>();

        // Choose which collection of realisations to process, preferring realisationsWithLots
        List<? extends RealisationBaseRequestDto> realisationsToProcess = null;
        if (request.getRealisationsWithLots() != null && !request.getRealisationsWithLots().isEmpty()) {
            realisationsToProcess = request.getRealisationsWithLots();
            System.out.println("Processing realisationsWithLots: " + realisationsToProcess.size() + " items");

            // Log the lots in each realisation
            for (int i = 0; i < realisationsToProcess.size(); i++) {
                RealisationBaseRequestDto r = realisationsToProcess.get(i);
                System.out.println("Realisation " + i + ": " + r);
                if (r.getLots() != null) {
                    System.out.println("  - Has " + r.getLots().size() + " lots");
                    for (LotResponse lot : r.getLots()) {
                        System.out.println("    - Lot: id=" + lot.getId() + ", desc=" + lot.getDescription());
                    }
                } else {
                    System.out.println("  - No lots found");
                }
            }
        } else if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            realisationsToProcess = request.getRealisations();
            System.out.println("Processing standard realisations: " + realisationsToProcess.size() + " items");
        }

        // Keep track of newly created lots to detect duplicates
        Map<String, Lot> newLotsMap = new HashMap<>();

        // Get the current max ID for Realisation once before the loop
//        Integer currentMaxRealisationId = realisationRepository.findMaxId();
        Integer currentMaxRealisationId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
        Integer nextRealisationId = (currentMaxRealisationId != null) ? currentMaxRealisationId + 1 : 1;

        if (realisationsToProcess != null && !realisationsToProcess.isEmpty()) {
            for (RealisationBaseRequestDto realisationRequest : realisationsToProcess) {
                // Create a new Realisation
                Realisation realisation = new Realisation();

                // Assign the next available ID and then increment it for the next iteration
                realisation.setId(nextRealisationId++);
                realisation.setVersion(1);

                // Set all fields from the request
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

                // Set up the relationship with PPM
                if (realisation.getPpms() == null) {
                    realisation.setPpms(new ArrayList<>());
                }
                realisation.getPpms().add(savedPpm);

                // Process realisation files
                if (realisationRequest.getRealisationFiles() != null && !realisationRequest.getRealisationFiles().isEmpty()) {
                    try {
                        System.out.println("Processing " + realisationRequest.getRealisationFiles().size() +
                                " files for realisation ID: " + realisation.getId());
/*
                        processAndSaveRelisationsDocumentsForUpdate(realisationRequest.getRealisationFiles(), realisation);
*/
                        documentCommuneService.processAndSaveDocuments(realisationRequest.getRealisationFiles(),realisation,Constants.PATH_ENTREPOT_GED_PPM+"/"+ppm.getId()+Constants.PATH_ENTREPOT_GED_REALISATION);

                        System.out.println("Processed realisation files successfully");
                    } catch (IOException e) {
                        System.err.println("Error processing realisation files: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                // Process realisation documents if provided
                if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                    List<Docs> docs = new ArrayList<>();
                    for (Integer docId : request.getDocIds()) {
                        Docs doc = docsRepository.findById(docId)
                                .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                        // Set up bidirectional relationship
                        if (doc.getRealisations() == null) {
                            doc.setRealisations(new ArrayList<>());
                        }
                        if (!doc.getRealisations().contains(realisation)) {
                            doc.getRealisations().add(realisation);
                        }

                        // Ensure doc is active
                        doc.setIsDeleted(false);
                        docsRepository.save(doc);
                        docs.add(doc);
                    }

                    // Set docs list for realisation
                    if (realisation.getDocs() == null) {
                        realisation.setDocs(docs);
                    } else {
                        realisation.getDocs().addAll(docs);
                    }
                    System.out.println("Added " + docs.size() + " documents to realisation");
                }

                // Handle Lots if provided
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    System.out.println("Processing " + realisationRequest.getLots().size() + " lots for realisation ID: " + realisation.getId());
                    List<Lot> lots = new ArrayList<>();
                    Set<Integer> processedLotIds = new HashSet<>();

                    for (LotResponse lotDto : realisationRequest.getLots()) {
                        System.out.println("Processing lot: " + lotDto);

                        if (lotDto.getId() == null) {
                            String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                            System.out.println("Processing new lot with description: " + lotHash);

                            if (newLotsMap.containsKey(lotHash)) {
                                System.out.println("Using previously created lot for description: " + lotHash);
                                lots.add(newLotsMap.get(lotHash));
                                processedLotIds.add(newLotsMap.get(lotHash).getId());
                                continue;
                            }

                            System.out.println("Creating new lot with description: " + lotHash);
                            Lot newLot = new Lot();
                            newLot.setDescription(lotDto.getDescription());
                            newLot.setVersion(1);
                            newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO); // Set budget
                            newLot.setIsDeleted(false);

                            Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                            Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                            newLot.setId(lotId);

                            Lot savedLot = lotRepository.save(newLot);
                            lots.add(savedLot);
                            processedLotIds.add(savedLot.getId());
                            newLotsMap.put(lotHash, savedLot);
                            System.out.println("Created new Lot with ID: " + savedLot.getId() + ", Budget: " + savedLot.getBudget());
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
                                    System.out.println("Lot ID " + lotId + " not found, creating new lot");
                                    Lot newLot = new Lot();
                                    newLot.setId(lotId);
                                    newLot.setVersion(1);
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO); // Set budget
                                    newLot.setIsDeleted(false);
                                    Lot savedLot = lotRepository.save(newLot);
                                    lots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
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
                                    newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO); // Set budget
                                    newLot.setIsDeleted(false);

                                    Lot savedLot = lotRepository.save(newLot);
                                    lots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    System.out.println("Created new version of Lot: " + savedLot.getId() + ", Version: " + savedLot.getVersion() + ", Budget: " + savedLot.getBudget());
                                } else {
                                    lots.add(existingLot);
                                    processedLotIds.add(existingLot.getId());
                                    System.out.println("Using existing Lot: " + existingLot.getId() + ", Version: " + existingLot.getVersion() + ", Budget: " + existingLot.getBudget());
                                }
                            } else {
                                Lot newLot = new Lot();
                                newLot.setId(lotId);
                                newLot.setVersion(1);
                                newLot.setDescription(lotDto.getDescription());
                                newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO); // Set budget
                                newLot.setIsDeleted(false);
                                Lot savedLot = lotRepository.save(newLot);
                                lots.add(savedLot);
                                processedLotIds.add(savedLot.getId());
                                System.out.println("Created new Lot with provided ID: " + savedLot.getId() + ", Budget: " + savedLot.getBudget());
                            }
                        }
                    }

                    realisation.setLots(lots);
                    System.out.println("Set " + lots.size() + " lots on realisation ID: " + realisation.getId());
                }

                // Add the realisation to the list to save later
                realisationsToSave.add(realisation);
            }

            // Save all realisations in a batch
            List<Realisation> savedRealisations = realisationRepository.saveAll(realisationsToSave);
            System.out.println("Saved " + savedRealisations.size() + " realisations in batch");

            // Update the PPM with the list of saved realisations
            savedPpm.setRealisations(savedRealisations);
            System.out.println("Added " + savedRealisations.size() + " realisations to PPM");
        }

        // Save PPM again to persist all associations
        savedPpm = repository.save(savedPpm);
        System.out.println("Final save of PPM - ID: " + savedPpm.getId() + ", Version: " + savedPpm.getVersion());

        // Verify the final PPM has all associated entities
        System.out.println("Final PPM has: " +
                (savedPpm.getDocs() != null ? savedPpm.getDocs().size() : 0) + " documents, " +
                (savedPpm.getRealisations() != null ? savedPpm.getRealisations().size() : 0) + " realisations");

        // Print realisation details to verify
        if (savedPpm.getRealisations() != null) {
            for (Realisation r : savedPpm.getRealisations()) {
                System.out.println("Realisation ID: " + r.getId() +
                        ", Docs: " + (r.getDocs() != null ? r.getDocs().size() : 0) +
                        ", Lots: " + (r.getLots() != null ? r.getLots().size() : 0));
            }
        }
        System.out.println("the remarque is " + savedPpm.getRemarque());
        return savedPpm;
    }

    private final Object folderCreationLock = new Object();

    /**
     * Utility method to check if a document with the given ID is already in the collection
     * @param docs List of documents to check
     * @param docId ID of the document to look for
     * @return true if document with that ID exists in the collection, false otherwise
     */
    private boolean containsDocumentById(List<Docs> docs, Integer docId) {
        if (docs == null || docId == null) return false;

        for (Docs doc : docs) {
            if (doc.getId() != null && doc.getId().equals(docId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process and save document files for a Realisation.
     *
     * @param files The document files to process
     * @param savedRealisation The Realisation to associate the documents with
     * @return List of created Doc entities
     * @throws IOException If file processing fails
     */
    private List<Docs> processAndSaveRealisationDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
        List<Docs> docs = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
                        String contentType = file.getContentType();

                        // Create a new document
                        Docs document = new Docs();
                        document.setName(originalFilename);
                        document.setContentType(contentType);

                        // You would add file upload logic here, similar to PaaServiceImpl
                        // For example, upload to Alfresco or other document storage

                        // Set up bidirectional relationship
                        if (document.getRealisations() == null) {
                            document.setRealisations(new ArrayList<>());
                        }
                        document.getRealisations().add(savedRealisation);

                        // Save the document
                        document = docsRepository.save(document);
                        docs.add(document);
                    } catch (Exception e) {
                        throw new IOException("Failed to process document: " + e.getMessage(), e);
                    }
                }
            }
        }

        return docs;
    }

    /**
     * Update an existing PPM (always version 1) and all its relationships without versioning.
     * This method updates a PPM along with its realisations, lots, and documents.
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing updated PPM data, realisations, lots, and document files
     * @return the updated PPM with all relationships established
     * @throws RuntimeException if the PPM is not found or is deleted
     */
    @Transactional
    @Override
    public Ppm updateGlobalWithoutVersionning(Integer ppmId, PpmWithLotsRequestDto request) {
        // Always use version 1
        Integer ppmVer = 1;

        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        Optional<Ppm> existingPpmOpt = repository.findById(compositeId);

        if (existingPpmOpt.isEmpty()) {
            throw new RuntimeException(
                    "Ppm with ID " + ppmId + " and version " + ppmVer + " was not found in the database.");
        }

        Ppm existingPpm = existingPpmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            throw new RuntimeException(
                    "Cannot update a deleted PPM with ID " + ppmId + " and version " + ppmVer);
        }

        // Update fields of the existing PPM
//        existingPpm.setRef(request.getRef());
        existingPpm.setNom(request.getNom());
        existingPpm.setAnnee(request.getAnnee());
        existingPpm.setBudgetEstime(request.getBudgetEstime());

        // Handle document management by comparing existing docs with requested docIds
        if (request.getDocIds() != null) {
            Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());

            // Get active document IDs
            Set<Integer> activeExistingDocIds = existingPpm.getDocs() != null ?
                    existingPpm.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .map(Docs::getId)
                            .collect(Collectors.toSet()) : new HashSet<>();

            // Log document changes
            if (!requestedDocIds.equals(activeExistingDocIds)) {
                System.out.println("Document changes detected for PPM update ID: " + ppmId);

                // Find new documents
                Set<Integer> newDocIds = new HashSet<>(requestedDocIds);
                newDocIds.removeAll(activeExistingDocIds);
                if (!newDocIds.isEmpty()) {
                    System.out.println("  - New documents added: " + newDocIds);
                }

                // Find removed documents
                Set<Integer> removedDocIds = new HashSet<>(activeExistingDocIds);
                removedDocIds.removeAll(requestedDocIds);
                if (!removedDocIds.isEmpty()) {
                    System.out.println("  - Documents marked as deleted: " + removedDocIds);
                }
            }

            // Create a list to hold updated documents
            List<Docs> updatedDocs = new ArrayList<>();

            if (existingPpm.getDocs() != null) {
                // For each existing document in the PPM
                for (Docs existingDoc : existingPpm.getDocs()) {
                    if (requestedDocIds.contains(existingDoc.getId())) {
                        // This doc is in the request, ensure it's not marked as deleted
                        existingDoc.setIsDeleted(false);
                        docsRepository.save(existingDoc);
                        updatedDocs.add(existingDoc);

                        // Remove from requested IDs since we've handled it
                        requestedDocIds.remove(existingDoc.getId());
                    } else {
                        // This doc is not in the request, mark it as deleted
                        existingDoc.setIsDeleted(true);
                        docsRepository.save(existingDoc);
                        updatedDocs.add(existingDoc); // Keep it in the collection but as deleted
                        System.out.println("Marked document with ID " + existingDoc.getId() + " as deleted (not in request)");
                    }
                }
            }

            // Add any new docs from the request
            for (Integer docId : requestedDocIds) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                // Set up bidirectional relationship
                if (doc.getPpms() == null) {
                    doc.setPpms(new ArrayList<>());
                }
                if (!doc.getPpms().contains(existingPpm)) {
                    doc.getPpms().add(existingPpm);
                }

                // Ensure doc is active
                doc.setIsDeleted(false);
                docsRepository.save(doc);

                updatedDocs.add(doc);
                System.out.println("Added new document with ID " + doc.getId() + " to PPM");
            }

            // Update the PPM with the final docs list
            existingPpm.setDocs(updatedDocs);
        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
            // No docIds provided - mark all existing documents as deleted
            List<Docs> existingDocs = existingPpm.getDocs();
            for (Docs doc : existingDocs) {
                if (!Boolean.TRUE.equals(doc.getIsDeleted())) {
                    doc.setIsDeleted(true);
                    docsRepository.save(doc);
                    System.out.println("Marked document with ID " + doc.getId() + " as deleted (no docIds provided)");
                }
            }
        }



        // Process realisations if provided
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            List<Realisation> realisations = new ArrayList<>();

            for (RealisationBaseRequestDto realisationRequest : request.getRealisations()) {
                Realisation realisation;

                if (realisationRequest.getId() != null) {
                    // Update existing realisation - always use version 1
                    Integer versionToUse = 1;

                    CompositeId realisationId = new CompositeId(realisationRequest.getId(), versionToUse);
                    Optional<Realisation> existingRealisationOpt = realisationRepository.findById(realisationId);

                    if (existingRealisationOpt.isEmpty()) {
                        throw new RuntimeException("Realisation not found with ID: " + realisationId.getId());
                    }

                    realisation = existingRealisationOpt.get();

                    // Update realisation fields
                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                    realisation.setBeneficiaire(realisationRequest.getBeneficiaire());
//                    realisation.setService(realisationRequest.getService());
//                    realisation.setDirection(realisationRequest.getDirection());
//                    realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
//                    realisation.setMonais(realisationRequest.getMonais());
//                    realisation.setMonnaie(realisationRequest.getMonnaie());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
//                    realisation.setModeSelection(realisationRequest.getModeSelection());
                    realisation.setModePassation(realisationRequest.getModePassation());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort());

                    // Handle realisation document management - same approach as for PPM docs
                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        List<Docs> docs = new ArrayList<>();
                        for (Integer docId : realisationRequest.getDocIds()) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                            // Set up bidirectional relationship
                            if (doc.getRealisations() == null) {
                                doc.setRealisations(new ArrayList<>());
                            }
                            doc.getRealisations().add(realisation);

                            // Ensure doc is active
                            doc.setIsDeleted(false);
                            docsRepository.save(doc);

                            docs.add(doc);
                        }

                        // Set docs list for new realisation
                        realisation.setDocs(docs);
                    }

                    // Handle Lots if provided
                    if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                        List<Lot> lots = new ArrayList<>();
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

                    // Save the realisation
                    realisation = realisationRepository.save(realisation);
                } else {
                    // Create new realisation
                    realisation = new Realisation();
//                    Integer maxRealisationId = realisationRepository.findMaxId();
                    Integer maxRealisationId =  sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);;
                    Integer realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;
                    realisation.setId(realisationId);
                    realisation.setVersion(1); // Always version 1 for new realisations

                    // Set all fields from the request
                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                    realisation.setBeneficiaire(realisationRequest.getBeneficiaire());
//                    realisation.setService(realisationRequest.getService());
//                    realisation.setDirection(realisationRequest.getDirection());
//                    realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
//                    realisation.setMonais(realisationRequest.getMonais());
//                    realisation.setMonnaie(realisationRequest.getMonnaie());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
//                    realisation.setModeSelection(realisationRequest.getModeSelection());
                    realisation.setModePassation(realisationRequest.getModePassation());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort());

                    // Handle realisation files if provided
                    if (realisationRequest instanceof RealisationWithLotsRequestDto) {
                        RealisationWithLotsRequestDto realisationWithLots = (RealisationWithLotsRequestDto) realisationRequest;
                        if (realisationWithLots.getRealisationFiles() != null && !realisationWithLots.getRealisationFiles().isEmpty()) {
                            try {
                                // processAndSaveRelisationsDocumentsForUpdate(realisationWithLots.getRealisationFiles(), realisation);
                                documentCommuneService.processAndSaveDocuments(realisationWithLots.getRealisationFiles(),realisation,Constants.PATH_ENTREPOT_GED_PPM+"/"+existingPpm.getId()+Constants.PATH_ENTREPOT_GED_REALISATION);

                            } catch (Exception e) {
                                throw new RuntimeException("Failed to process realisation document files: " + e.getMessage(), e);
                            }
                        }
                    }

                    // Set up the relationship with PPM
                    List<Ppm> ppmList = new ArrayList<>();
                    ppmList.add(existingPpm);
                    realisation.setPpms(ppmList);

                    // Handle Realisation documents if docIds are provided
                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        List<Docs> docs = new ArrayList<>();
                        for (Integer docId : realisationRequest.getDocIds()) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                            // Set up bidirectional relationship
                            if (doc.getRealisations() == null) {
                                doc.setRealisations(new ArrayList<>());
                            }
                            if (!doc.getRealisations().contains(realisation)) {
                                doc.getRealisations().add(realisation);
                            }

                            // Ensure doc is active
                            doc.setIsDeleted(false);
                            docsRepository.save(doc);

                            docs.add(doc);
                        }

                        // Set docs list for new realisation
                        realisation.setDocs(docs);
                    }

                    // Handle Lots if provided
                    if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                        List<Lot> lots = new ArrayList<>();
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

                    // Save the realisation
                    realisation = realisationRepository.save(realisation);
                }

                realisations.add(realisation);
            }

            // Update the PPM with the list of realisations
            existingPpm.setRealisations(realisations);
        } else if (existingPpm.getRealisations() != null) {
            existingPpm.getRealisations().clear(); // Clear if no new realisations provided
        }

        // Save the updated PPM
        Ppm savedPpm = repository.save(existingPpm);

        // Create a copy for the response with only non-deleted documents
        Ppm copyForResponse = new Ppm();
        BeanUtils.copyProperties(savedPpm, copyForResponse);

        // Filter out deleted documents for the response
        if (savedPpm.getDocs() != null) {
            copyForResponse.setDocs(
                    savedPpm.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .collect(Collectors.toList())
            );
        }

        // Also filter realisation documents
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

    /**
     * Update an existing PPM with all its relationships.
     * This method updates a PPM along with its realisations, lots, and documents.
     *
     * @param ppmId   the ID of the PPM to update
     * @param ppmVer  the version of the PPM to update
     * @param request the request containing updated PPM data
     * @return the updated PPM with all relationships established
     * @throws RuntimeException if the PPM is not found or is deleted
     */
    @Transactional
    @Override
    public Ppm updateWithRelationships(Integer ppmId, Integer ppmVer, PpmWithLotsRequestDto request) {
        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        Optional<Ppm> existingPpmOpt = repository.findById(compositeId);

        System.out.println("the request from the update method ");
        System.out.println(request);
        if (existingPpmOpt.isEmpty()) {
            throw new RuntimeException("Ppm with ID " + ppmId + " and version " + ppmVer + " not found.");
        }

        Ppm existingPpm = existingPpmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            throw new RuntimeException("Cannot update a deleted Ppm with ID " + ppmId + " and version " + ppmVer);
        }
        if (existingPpm.getStatus() == null || existingPpm.getStatus() != ProcessStatus.BROUILLON) {
            throw new InvalidPpmStatusException("PPM can only be updated in draft status. Current status ID: " +
                    (existingPpm.getStatus() != null ? existingPpm.getStatus() : "null"));
        }

        existingPpm.setRef(existingPpm.getRef());
        existingPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());
        existingPpm.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPpm.getAnnee());
        existingPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());
        existingPpm.setProcessInstanceId(existingPpm.getProcessInstanceId());
        existingPpm.setStatus(existingPpm.getStatus());

        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
            existingPpm.setAc(ac);
        }

        // determine acId to use for seuil control
        Integer requestAcId = request.getAcId() != null
                ? request.getAcId()
                : (existingPpm.getAc() != null ? existingPpm.getAc().getId() : null);

        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());
            Set<Integer> existingDocIds = existingPpm.getDocs() != null ?
                    existingPpm.getDocs().stream().map(Docs::getId).collect(Collectors.toSet()) :
                    new HashSet<>();

            Set<Integer> docsToAdd = new HashSet<>(requestDocIds);
            docsToAdd.removeAll(existingDocIds);

            Set<Integer> docsToRemove = new HashSet<>(existingDocIds);
            docsToRemove.removeAll(requestDocIds);

            List<Docs> docs = existingPpm.getDocs() != null ?
                    new ArrayList<>(existingPpm.getDocs()) :
                    new ArrayList<>();

            if (!docsToRemove.isEmpty()) {
                docs.removeIf(doc -> docsToRemove.contains(doc.getId()));

                final Ppm ppmRef = existingPpm;

                for (Integer docId : docsToRemove) {
                    docsRepository.findById(docId).ifPresent(doc -> {
                        if (doc.getPpms() != null) {
                            doc.getPpms().remove(ppmRef);
                            docsRepository.save(doc);
                        }
                    });
                }
            }

            for (Integer docId : docsToAdd) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                if (doc.getPpms() == null) {
                    doc.setPpms(new ArrayList<>());
                }
                doc.getPpms().add(existingPpm);
                docs.add(doc);

                docsRepository.save(doc);
            }

            existingPpm.setDocs(docs);
            System.out.println("Updated documents: " + docs.size() + " total documents");
        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
            for (Docs doc : existingPpm.getDocs()) {
                if (doc.getPpms() != null) {
                    doc.getPpms().remove(existingPpm);
                    docsRepository.save(doc);
                }
            }
            existingPpm.setDocs(new ArrayList<>());
        }

        if (request.getPpmFiles() != null && !request.getPpmFiles().isEmpty()) {
            try {
                System.out.println("Processing " + request.getPpmFiles().size() + " files for PPM");
                documentCommuneService.processAndSaveDocuments(request.getPpmFiles(), existingPpm, Constants.PATH_ENTREPOT_GED_PPM);
                System.out.println("Processed PPM files successfully");
            } catch (IOException e) {
                System.err.println("Error processing PPM files: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        List<Realisation> updatedRealisations = new ArrayList<>();
        Set<Integer> processedRealisationIds = new HashSet<>();

        List<? extends RealisationBaseRequestDto> realisationsToProcess = null;
        if (request.getRealisationsWithLots() != null && !request.getRealisationsWithLots().isEmpty()) {
            realisationsToProcess = request.getRealisationsWithLots();
            System.out.println("Processing realisationsWithLots: " + realisationsToProcess.size() + " items");
        } else if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            realisationsToProcess = request.getRealisations();
            System.out.println("Processing standard realisations: " + realisationsToProcess.size() + " items");
        }

        if (realisationsToProcess != null && !realisationsToProcess.isEmpty()) {
            for (RealisationBaseRequestDto realisationRequest : realisationsToProcess) {
                Realisation realisation;

                if (realisationRequest.getId() != null) {
                    processedRealisationIds.add(realisationRequest.getId());

                    Integer realisationVersion = realisationRequest.getVersion() != null ?
                            realisationRequest.getVersion() : 1;

                    CompositeId realisationId = new CompositeId(realisationRequest.getId(), realisationVersion);
                    Optional<Realisation> existingRealisationOpt = realisationRepository.findById(realisationId);

                    if (existingRealisationOpt.isEmpty()) {
                        throw new RuntimeException("Realisation with ID " + realisationRequest.getId() +
                                " and version " + realisationVersion + " not found");
                    }

                    realisation = existingRealisationOpt.get();

                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees() != null ?
                            realisationRequest.getRealisationEnvisagees() : realisation.getRealisationEnvisagees());

                    realisation.setBeneficiaire(realisationRequest.getBeneficiaire() != null ?
                            realisationRequest.getBeneficiaire() : realisation.getBeneficiaire());
                    // unchanged scalar setters omitted for brevity
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher() != null ?
                            realisationRequest.getTypeMarcher() : realisation.getTypeMarcher());
                    realisation.setModePassation(realisationRequest.getModePassation() != null ?
                            realisationRequest.getModePassation() : realisation.getModePassation());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps() != null ?
                            realisationRequest.getDatePrevLps() : realisation.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc() != null ?
                            realisationRequest.getDatePrevAc() : realisation.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp() != null ?
                            realisationRequest.getDatePrevDp() : realisation.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp() != null ?
                            realisationRequest.getDatePrevAp() : realisation.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime() != null ?
                            realisationRequest.getBudgetEstime() : realisation.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished() != null ?
                            realisationRequest.getIsPublished() : realisation.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt() != null ?
                            realisationRequest.getPublishedAt() : realisation.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort() != null ?
                            realisationRequest.getSort() : realisation.getSort());

                    if (realisation.getPpms() == null) {
                        realisation.setPpms(new ArrayList<>());
                    }
                    if (!realisation.getPpms().contains(existingPpm)) {
                        realisation.getPpms().add(existingPpm);
                    }

                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        Set<Integer> requestDocIds = new HashSet<>(realisationRequest.getDocIds());
                        Set<Integer> existingDocIds = realisation.getDocs() != null ?
                                realisation.getDocs().stream().map(Docs::getId).collect(Collectors.toSet()) :
                                new HashSet<>();

                        Set<Integer> docsToAdd = new HashSet<>(requestDocIds);
                        docsToAdd.removeAll(existingDocIds);

                        Set<Integer> docsToRemove = new HashSet<>(existingDocIds);
                        docsToRemove.removeAll(requestDocIds);

                        List<Docs> docs = realisation.getDocs() != null ?
                                new ArrayList<>(realisation.getDocs()) :
                                new ArrayList<>();

                        if (!docsToRemove.isEmpty()) {
                            docs.removeIf(doc -> docsToRemove.contains(doc.getId()));

                            final Realisation realisationRef = realisation;

                            for (Integer docId : docsToRemove) {
                                docsRepository.findById(docId).ifPresent(doc -> {
                                    if (doc.getRealisations() != null) {
                                        doc.getRealisations().remove(realisationRef);
                                        docsRepository.save(doc);
                                    }
                                });
                            }
                        }

                        for (Integer docId : docsToAdd) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

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
                        System.out.println("Updated docs for realisation ID " + realisation.getId() + ": " + docs.size() + " total docs");
                    } else if (realisation.getDocs() != null && !realisation.getDocs().isEmpty()) {
                        for (Docs doc : realisation.getDocs()) {
                            if (doc.getRealisations() != null) {
                                doc.getRealisations().remove(realisation);
                                docsRepository.save(doc);
                            }
                        }
                        realisation.setDocs(new ArrayList<>());
                        System.out.println("Cleared docs for realisation ID " + realisation.getId());
                    }
                } else {
                    realisation = new Realisation();

                    Integer maxRealisationId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
                    Integer realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;

                    realisation.setId(realisationId);
                    realisation.setVersion(1);

                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                    realisation.setBeneficiaire(realisationRequest.getBeneficiaire());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                    realisation.setModePassation(realisationRequest.getModePassation());
                    realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                    realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                    realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                    realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                    realisation.setBudgetEstime(realisationRequest.getBudgetEstime());
                    realisation.setIsPublished(realisationRequest.getIsPublished());
                    realisation.setPublishedAt(realisationRequest.getPublishedAt());
                    realisation.setSort(realisationRequest.getSort());

                    List<Ppm> ppmList = new ArrayList<>();
                    ppmList.add(existingPpm);
                    realisation.setPpms(ppmList);

                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        List<Docs> docs = new ArrayList<>();
                        for (Integer docId : realisationRequest.getDocIds()) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

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
                        System.out.println("Added " + docs.size() + " documents to new realisation");
                    }
                }

                realisation = realisationRepository.save(realisation);
                System.out.println("Saved realisation ID: " + realisation.getId() + " version: " + realisation.getVersion());

                processedRealisationIds.add(realisation.getId());
                if (existingPpm.getRealisations() == null) {
                    existingPpm.setRealisations(new ArrayList<>());
                }
                if (!existingPpm.getRealisations().contains(realisation)) {
                    existingPpm.getRealisations().add(realisation);
                }
                if (realisation.getPpms() == null) {
                    realisation.setPpms(new ArrayList<>());
                }
                if (!realisation.getPpms().contains(existingPpm)) {
                    realisation.getPpms().add(existingPpm);
                }
                realisation = realisationRepository.save(realisation);

                System.out.println("the relisations request are");
                System.out.println(realisationRequest.getFinancements());


                if (realisationRequest.getRealisationFiles() != null && !realisationRequest.getRealisationFiles().isEmpty()) {
                    try {
                        System.out.println("Processing " + realisationRequest.getRealisationFiles().size() +
                                " files for realisation ID: " + realisation.getId());
                        documentCommuneService.processAndSaveDocuments(realisationRequest.getRealisationFiles(), realisation, Constants.PATH_ENTREPOT_GED_PPM + "/" + existingPpm.getId() + Constants.PATH_ENTREPOT_GED_REALISATION);

                        realisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion()))
                                .orElseThrow(() -> new RuntimeException("Could not find realisation after file upload"));

                        System.out.println("After file processing, realisation has " +
                                (realisation.getDocs() != null ? realisation.getDocs().size() : 0) + " documents");

                        realisation = realisationRepository.save(realisation);
                    } catch (IOException e) {
                        System.err.println("Error processing realisation files: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                Map<String, Lot> newLotsMap = new HashMap<>();

                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    System.out.println("Processing " + realisationRequest.getLots().size() +
                            " lots for realisation ID: " + realisation.getId());

                    List<Lot> updatedLots = new ArrayList<>();
                    Set<Integer> processedLotIds = new HashSet<>();

                    Set<Integer> existingLotIds = realisation.getLots() != null ?
                            realisation.getLots().stream().map(Lot::getId).collect(Collectors.toSet()) :
                            new HashSet<>();

                    for (LotResponse lotDto : realisationRequest.getLots()) {
                        if (lotDto.getId() == null) {
                            String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";

                            if (newLotsMap.containsKey(lotHash)) {
                                updatedLots.add(newLotsMap.get(lotHash));
                                processedLotIds.add(newLotsMap.get(lotHash).getId());
                                continue;
                            }

                            Lot newLot = new Lot();
                            newLot.setDescription(lotDto.getDescription());
                            newLot.setVersion(1);
                            newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO);
                            newLot.setIsDeleted(false);

                            Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                            Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                            newLot.setId(lotId);

                            Lot savedLot = lotRepository.save(newLot);
                            updatedLots.add(savedLot);
                            processedLotIds.add(savedLot.getId());
                            newLotsMap.put(lotHash, savedLot);
                            System.out.println("Created new Lot with ID: " + savedLot.getId() + ", Budget: " + savedLot.getBudget());
                        } else {
                            if (processedLotIds.contains(lotDto.getId())) {
                                continue;
                            }

                            Integer lotId = lotDto.getId();
                            Integer lotVersion = lotDto.getVersion();

                            if (lotVersion == null) {
                                lotVersion = lotRepository.findMaxVersionById(lotId);
                                if (lotVersion == null) {
                                    System.out.println("Lot ID " + lotId + " not found, creating new lot");
                                    Lot newLot = new Lot();
                                    newLot.setId(lotId);
                                    newLot.setVersion(1);
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO);
                                    newLot.setIsDeleted(false);
                                    Lot savedLot = lotRepository.save(newLot);
                                    updatedLots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    continue;
                                }
                            }

                            Optional<Lot> existingLotOpt = lotRepository.findById(new CompositeId(lotId, lotVersion));
                            if (existingLotOpt.isPresent()) {
                                Lot existingLot = existingLotOpt.get();
                                String requestDesc = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                String existingDesc = existingLot.getDescription() != null ? existingLot.getDescription() : "";

                                boolean budgetProvided = (lotDto.getBudget() != null);
                                boolean budgetChanged = budgetProvided && (existingLot.getBudget() == null
                                        || lotDto.getBudget().compareTo(existingLot.getBudget()) != 0);
                                boolean descChanged = !requestDesc.equals(existingDesc);

                                if (budgetChanged || descChanged) {
                                    if (descChanged) {
                                        existingLot.setDescription(requestDesc);
                                    }
                                    if (budgetChanged) {
                                        existingLot.setBudget(lotDto.getBudget());
                                    }
                                    existingLot = lotRepository.save(existingLot);
                                    updatedLots.add(existingLot);
                                    processedLotIds.add(existingLot.getId());
                                    System.out.println("Updated existing Lot (in place): " + existingLot.getId()
                                            + ", Version: " + existingLot.getVersion()
                                            + ", Desc: " + existingLot.getDescription()
                                            + ", Budget: " + existingLot.getBudget());
                                } else {
                                    updatedLots.add(existingLot);
                                    processedLotIds.add(existingLot.getId());
                                    System.out.println("Using existing Lot: " + existingLot.getId()
                                            + ", Version: " + existingLot.getVersion()
                                            + ", Budget: " + existingLot.getBudget());
                                }
                            } else {
                                Lot newLot = new Lot();
                                newLot.setId(lotId);
                                newLot.setVersion(1);
                                newLot.setDescription(lotDto.getDescription());
                                newLot.setBudget(lotDto.getBudget() != null ? lotDto.getBudget() : BigDecimal.ZERO);
                                newLot.setIsDeleted(false);
                                Lot savedLot = lotRepository.save(newLot);
                                updatedLots.add(savedLot);
                                processedLotIds.add(savedLot.getId());
                                System.out.println("Created new Lot with provided ID: " + savedLot.getId() + ", Budget: " + savedLot.getBudget());
                            }
                        }
                    }

                    realisation.setLots(updatedLots);
                    System.out.println("Set " + updatedLots.size() + " lots on realisation ID: " + realisation.getId());

                    realisation = realisationRepository.save(realisation);

                    Realisation verifyRealisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion()))
                            .orElseThrow(() -> new RuntimeException("Could not find realisation after saving lots"));

                    System.out.println("After saving, realisation ID: " + verifyRealisation.getId() +
                            " has " + (verifyRealisation.getLots() != null ? verifyRealisation.getLots().size() : 0) +
                            " lots");
                } else if (realisation.getLots() != null && !realisation.getLots().isEmpty()) {
                    realisation.setLots(new ArrayList<>());
                    realisation = realisationRepository.save(realisation);
                    System.out.println("Cleared lots for realisation ID: " + realisation.getId());
                }

                processFinancementsForRealisation(realisation, realisationRequest.getFinancements());

                BigDecimal recalculatedBudget = (realisation.getFinancements() == null)
                        ? BigDecimal.ZERO
                        : realisation.getFinancements().stream()
                        .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                        .map(f -> {
                            BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                            Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0;

                            return amt.multiply(BigDecimal.valueOf(taux));
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                realisation.setBudgetEstime(recalculatedBudget);

                setSeuilApplicableForRealisationInPpm(realisation, requestAcId);

                updatedRealisations.add(realisation);
            }
        }

        if (existingPpm.getRealisations() != null) {
            for (Realisation existingRealisation : existingPpm.getRealisations()) {
                if (!processedRealisationIds.contains(existingRealisation.getId())) {
                    if (existingRealisation.getPpms() != null) {
                        existingRealisation.getPpms().remove(existingPpm);
                        realisationRepository.save(existingRealisation);
                    }
                }
            }
        }

        existingPpm.setRealisations(updatedRealisations);
        System.out.println("Updated PPM with " + updatedRealisations.size() + " realisations");

        existingPpm = repository.save(existingPpm);
        System.out.println("Final save of PPM - ID: " + existingPpm.getId() + ", Version: " + existingPpm.getVersion());

        System.out.println("Final PPM has: " +
                (existingPpm.getDocs() != null ? existingPpm.getDocs().size() : 0) + " documents, " +
                (existingPpm.getRealisations() != null ? existingPpm.getRealisations().size() : 0) + " realisations");

        if (existingPpm.getRealisations() != null) {
            for (Realisation r : existingPpm.getRealisations()) {
                System.out.println("Realisation ID: " + r.getId() +
                        ", Docs: " + (r.getDocs() != null ? r.getDocs().size() : 0) +
                        ", Lots: " + (r.getLots() != null ? r.getLots().size() : 0));
            }
        }

        return existingPpm;
    }

    /**
     * Creates and saves a new PPM version with all relationships established.
     *
     * @param ppmId The PPM ID
     * @param ppmVer The current PPM version (to be incremented)
     * @param tempPpm The temporary PPM with updated fields
     * @param updatedRealisations The list of updated/new realisations
     * @param updatedDocs The list of updated/new documents
     * @param existingPpm The existing PPM for reference
     * @return The newly created PPM version
     */
    private Ppm createAndSaveNewPpmVersion(Integer ppmId, Integer ppmVer, Ppm tempPpm,
                                           List<Realisation> updatedRealisations,
                                           List<Docs> updatedDocs,
                                           Ppm existingPpm) {
        System.out.println("Creating new PPM version: " + (ppmVer + 1));

        Ppm newPpm = new Ppm();
        BeanUtils.copyProperties(tempPpm, newPpm, "id", "version");
        newPpm.setId(ppmId);
        newPpm.setVersion(ppmVer + 1);

        // Preserve audit from existing
        if (existingPpm.getCreatedAt() != null) {
            newPpm.setCreatedAt(existingPpm.getCreatedAt());
            newPpm.setCreatedBy(existingPpm.getCreatedBy());
        }

        newPpm.setRef(existingPpm.getRef());
        newPpm.setProcessInstanceId(existingPpm.getProcessInstanceId());
        newPpm.setStatus(existingPpm.getStatus());

        if (updatedDocs != null) {
            System.out.println("Documents before saving new PPM version:");
            for (Docs doc : updatedDocs) {
                System.out.println("  - Doc ID: " + doc.getId() + ", isDeleted: " + doc.getIsDeleted());
                if (doc.getIsDeleted() == null) {
                    doc.setIsDeleted(false);
                    docsRepository.save(doc);
                }
            }
        }

        newPpm = repository.save(newPpm);
        System.out.println("Saved new PPM: " + newPpm.getId() + " version " + newPpm.getVersion());

        if (updatedRealisations != null && !updatedRealisations.isEmpty()) {
            newPpm.setRealisations(updatedRealisations);
            for (Realisation realisation : updatedRealisations) {
                if (realisation.getPpms() == null) realisation.setPpms(new ArrayList<>());
                if (!realisation.getPpms().contains(newPpm)) {
                    realisation.getPpms().add(newPpm);
                }
            }
        }

        if (updatedDocs != null) {
            newPpm.setDocs(new ArrayList<>(updatedDocs));
            for (Docs doc : updatedDocs) {
                if (doc.getPpms() == null) doc.setPpms(new ArrayList<>());
                if (!doc.getPpms().contains(newPpm)) {
                    doc.getPpms().add(newPpm);
                }
            }
        }

        System.out.println("Saved new PPM: " + newPpm.getId() + " version " + newPpm.getVersion());

        if (updatedRealisations != null && !updatedRealisations.isEmpty()) {
            for (Realisation realisation : updatedRealisations) {
                realisationRepository.save(realisation);
            }
        }
        if (updatedDocs != null && !updatedDocs.isEmpty()) {
            for (Docs doc : updatedDocs) {
                docsRepository.save(doc);
            }
        }

        return newPpm;
    }




    /**
     * Maps a RealisationBaseRequestDto to a RealisationRequest.
     *
     * @param dto The DTO to map
     * @return The mapped RealisationRequest
     */
    private RealisationRequest mapToRealisationRequest(RealisationBaseRequestDto dto) {
        RealisationRequest request = new RealisationRequest();
        request.setRealisationEnvisagees(dto.getRealisationEnvisagees());
        request.setBeneficiaire(dto.getBeneficiaire());
//        request.setService(dto.getService());
//        request.setDirection(dto.getDirection());
//        request.setSourceFinancement(dto.getSourceFinancement());
//        request.setMonais(dto.getMonais());
//        request.setMonnaie(dto.getMonnaie());
        request.setTypeMarcher(dto.getTypeMarcher());
//        request.setModeSelection(dto.getModeSelection());
        request.setModePassation(dto.getModePassation());
        request.setDatePrevLps(dto.getDatePrevLps());
        request.setDatePrevAc(dto.getDatePrevAc());
        request.setDatePrevDp(dto.getDatePrevDp());
        request.setDatePrevAp(dto.getDatePrevAp());
        request.setBudgetEstime(dto.getBudgetEstime());
        request.setIsPublished(dto.getIsPublished());
        request.setPublishedAt(dto.getPublishedAt());
        request.setSort(dto.getSort());
        request.setDocIds(dto.getDocIds());

        // Set Lots from the DTO
        if (dto.getLots() != null && !dto.getLots().isEmpty()) {
            request.setLots(dto.getLots());
        }

        return request;
    }
    /**
     * Retrieves the latest version number for a given Agpm ID.
     *
     * @param ppmId ID of the Ppm entity
     * @return Optional containing the latest version, or empty if not found
     */

    @Override
    public Integer findLatestVersionById(Integer ppmId) {
        return repository.findMaxVersionById(ppmId);
    }

    /**
     * Helper to check if dates differ
     */
    private boolean areDatesChanged(LocalDate date1, LocalDate date2) {
        if (date1 == null && date2 == null) return false;
        if (date1 == null || date2 == null) return true;
        return !date1.equals(date2);
    }

    @Override
    public Optional<Ppm> findByIdPpm(Integer ppmId, Integer ppmVer) throws PpmNotFound, AcNotFound {
        if (ppmVer == null) {
            return findLatestById(ppmId);
        }
        return findById(ppmId, ppmVer);
    }

    @Override
    public Page<Ppm> findPpmsByAcId(Integer acId, Pageable pageable) throws AcNotFound {
        acService.findById(acId);
        Specification<Ppm> ppmSpec = Specification.where(
                PpmSpecifications.deleted(false)
                        .and(PpmSpecifications.isLatestVersion().and(PpmSpecifications.ppmAc(acId)))
        );
        return repository.findAll(ppmSpec, pageable);
    }

    @Override
    public Page<Ppm> findAllVersionsById(Integer ppmId, Pageable pageable) throws PpmIdNotFound {
        if (ppmId == null || ppmId < 0) {
            throw new IllegalArgumentException("Invalid Ppm ID: " + ppmId);
        }
        Integer maxVersion = findLatestVersionById(ppmId);
        if (maxVersion==null) {
            throw new PpmIdNotFound("No Ppm found for ID", ppmId);
        }
        Page<Ppm> ppmPage = repository.findAllById(ppmId, pageable);
        if (ppmPage.isEmpty()) {
            throw new PpmIdNotFound("No Ppm versions found for ID", ppmId);
        }
        return ppmPage;
    }

    /**
     * Generates and sets the reference string for the Ppm entity.
     * Format: PPM_<YEAR>_<ID>
     *
     * @param ppm the Ppm entity to set the reference for
     */
    private void setRef(Ppm ppm) {
        if (ppm == null || ppm.getId() == null || ppm.getAnnee() == null) {
            throw new IllegalArgumentException("Cannot set reference: ppm, id, or year is null.");
        }
        String ref = "PPM_" + ppm.getAnnee() + "_" + ppm.getId();
        ppm.setRef(ref);
    }

    @Override
    public Page<Realisation> getRealisationsWithoutDac(Integer ppmId, Integer ppmVer, Pageable pageable) {
        List<Object[]> realisationKeysWithDac = dacRepository.findRealisationKeysWithDac();
        // Get all realisations for the given PPM and version, not in DAC
        List<Realisation> allRealisationsForPpm = realisationRepository.findAll().stream()
                .filter(r -> r.getPpms() != null && r.getPpms().stream().anyMatch(ppm ->
                        ppm.getId().equals(ppmId) && ppm.getVersion().equals(ppmVer)))
                .filter(r -> realisationKeysWithDac.stream().noneMatch(key ->
                        key[0] != null && key[1] != null &&
                                r.getId().equals(key[0]) && r.getVersion().equals(key[1])
                ))
                .collect(Collectors.toList());
        // Convert the list to a Page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRealisationsForPpm.size());
        List<Realisation> pageContent = allRealisationsForPpm.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allRealisationsForPpm.size());
    }

    @Transactional
    @Override
    public Ppm createPpmPerAc(PpmWithLotsRequestDto request) throws PpmCreationLimitExceededException, AcNotFound, IOException {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        log.info("Creating PPM for year: {}", currentYear);

        // Step 1: Validate the AC in the request
        Integer requestAcId = request.getAcId();
        if (requestAcId == null) {
            throw new IllegalArgumentException("AC ID is required to create a PPM.");
        }

        // Step 2: Verify that the user is associated with the requested AC
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            log.warn("No AC IDs found for the connected user");
            throw new AcNotFound("No AC IDs found for the connected user", Collections.emptyList());
        }
        if (!userAcIds.contains(requestAcId)) {
            log.warn("User does not have permission for AC ID: {}", requestAcId);
            throw new AcNotFound("User does not have permission for AC ID: " + requestAcId, List.of(requestAcId));
        }

        // Step 3: Verify that the AC exists
        Ac ac = acService.findById(requestAcId);

        // Step 4: Restrict creation to one PPM per AC per year
        Specification<Ppm> spec = PpmSpecifications.annee(currentYear)
                .and(PpmSpecifications.ppmAc(requestAcId))
                .and(PpmSpecifications.deleted(false));
        List<Ppm> existingPpms = repository.findAll(spec);
        if (!existingPpms.isEmpty()) {
            Ppm existingPpm = existingPpms.get(0);
            throw new PpmCreationLimitExceededException(
                    "A PPM already exists for AC ID " + requestAcId + " for year " + currentYear + ".",
                    existingPpm.getId(),
                    existingPpm.getVersion(),
                    1
            );
        }

        // Step 5: Generate new PPM ID and set version
        Integer ppmId = sequenceGeneratorService.getNextId(Constants.PPM_ID_SEQ);
        Integer ppmVersion = 1;

        // Step 6: Create and populate PPM entity
        Ppm ppm = new Ppm();
        ppm.setId(ppmId);
        ppm.setVersion(ppmVersion);
        ppm.setNom(request.getNom());
        ppm.setModePassation(request.getModePassation());
        ppm.setRemarque(request.getRemarque());
        ppm.setAnnee(currentYear);
        setRef(ppm);
//        ppm.setBudgetEstime(request.getBudgetEstime());
        ppm.setStatus(ProcessStatus.BROUILLON);
        ppm.setAc(ac);
        ppm.setRealisations(new ArrayList<>());
        ppm.setDocs(new ArrayList<>());

        // Step 7: Validate annee
        if (!currentYear.equals(ppm.getAnnee())) {
            throw new IllegalStateException("Annee field is not set to the current year (" + currentYear + "). Current value: " + ppm.getAnnee());
        }
        log.info("Annee validation passed: {}", ppm.getAnnee());

        // Step 8: Save PPM initially
        Ppm savedPpm = repository.save(ppm);

        // Step 9: Handle documents for PPM
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = new ArrayList<>();
            for (Integer docId : request.getDocIds()) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                if (doc.getPpms() == null) {
                    doc.setPpms(new ArrayList<>());
                }
                doc.getPpms().add(savedPpm);
                docs.add(doc);
                docsRepository.save(doc);
            }
            savedPpm.setDocs(docs);
        }

        if (request.getPpmFiles() != null && !request.getPpmFiles().isEmpty()) {
            documentCommuneService.processAndSaveDocuments(
                    request.getPpmFiles(),
                    savedPpm,
                    Constants.PATH_ENTREPOT_GED_PPM
            );
        }

        // Step 10: Process realisations if provided
        if (request.getRealisationsWithLots() != null && !request.getRealisationsWithLots().isEmpty()) {
            Integer currentMaxRealisationId = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
            Integer nextRealisationId = (currentMaxRealisationId != null) ? currentMaxRealisationId : 1;

            List<Realisation> realisationsToSave = new ArrayList<>();
            for (RealisationWithLotsRequestDto realisationRequest : request.getRealisationsWithLots()) {
                Realisation realisation = new Realisation();
                realisation.setId(nextRealisationId++);
                realisation.setVersion(1);
                realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees());
                realisation.setBeneficiaire(realisationRequest.getBeneficiaire());
//          realisation.setService(realisationRequest.getService());
//          realisation.setDirection(realisationRequest.getDirection());
//          realisation.setSourceFinancement(realisationRequest.getSourceFinancement());
//          realisation.setMonnaie(realisationRequest.getMonnaie());
                realisation.setTypeMarcher(realisationRequest.getTypeMarcher());
                realisation.setModePassation(realisationRequest.getModePassation());
                realisation.setDatePrevLps(realisationRequest.getDatePrevLps());
                realisation.setDatePrevAc(realisationRequest.getDatePrevAc());
                realisation.setDatePrevDp(realisationRequest.getDatePrevDp());
                realisation.setDatePrevAp(realisationRequest.getDatePrevAp());
                // NOTE: do NOT set budgetEstime from request; we compute it from financements below
                realisation.setIsPublished(realisationRequest.getIsPublished());
                realisation.setPublishedAt(realisationRequest.getPublishedAt());
                realisation.setSort(realisationRequest.getSort());

                // Handle realisation files
                if (realisationRequest.getRealisationFiles() != null && !realisationRequest.getRealisationFiles().isEmpty()) {
                    documentCommuneService.processAndSaveDocuments(
                            realisationRequest.getRealisationFiles(),
                            realisation,
                            Constants.PATH_ENTREPOT_GED_PPM + "/" + ppm.getId() + Constants.PATH_ENTREPOT_GED_REALISATION
                    );
                }

                // Set bidirectional relationship with PPM
                if (realisation.getPpms() == null) {
                    realisation.setPpms(new ArrayList<>());
                }
                realisation.getPpms().add(savedPpm);
                savedPpm.getRealisations().add(realisation);

                // Handle Lots
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    Integer currentMaxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                    Integer nextLotId = (currentMaxLotId != null) ? currentMaxLotId : 1;
                    List<Lot> lots = new ArrayList<>();
                    for (LotResponse lotRequest : realisationRequest.getLots()) {
                        Lot lot = new Lot();
                        Integer lotId = lotRequest.getId();
                        if (lotId == null) {
                            lotId = nextLotId++;
                        }
                        lot.setId(lotId);
                        lot.setVersion(1);
                        lot.setDescription(lotRequest.getDescription());
                        lot.setBudget(lotRequest.getBudget());
                        lot = lotRepository.save(lot);
                        lots.add(lot);
                    }
                    realisation.setLots(lots);
                }

                // ADD: Handle Financements
                Integer currentMaxFinancementId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);
                Integer nextFinancementId = (currentMaxFinancementId != null) ? currentMaxFinancementId + 1 : 1;

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

                        // assign simple fields
                        financementId.setFinancementId(nextFinancementId++);
                        financement.setBudgetEstimer(financementRequest.getBudgetEstimer());

                        Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
                                .orElseThrow(() -> new EntityNotFoundException("Monnaie not found with ID: " + financementRequest.getMonnaieId()));

                        if (monnaie.getTaux() != null) {
                            financement.setTauxApplicable(monnaie.getTaux());
                            System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux() +
                                    " from Monnaie ID: " + monnaie.getId());
                        } else {
                            financement.setTauxApplicable(0.0);
                            System.out.println("Monnaie taux is null, set tauxApplicable to 0.0 for Monnaie ID: " + monnaie.getId());
                        }

                        if (financementRequest.getSourceFinancement() != null && financementRequest.getSourceFinancement().getId() != null) {
                            RefParam sourceFinancement = refParamRepository.findById(financementRequest.getSourceFinancement().getId())
                                    .orElseThrow(() -> new EntityNotFoundException("Source Financement not found with ID: " + financementRequest.getSourceFinancement().getId()));
                            financement.setSourceFinancement(sourceFinancement);
                        }

                        // relationships last
                        financement.setMonnaie(monnaie);
                        financementId.setRealisation(realisation);
                        financement.setId(financementId);

                        financements.add(financement);

                        log.info("Created financement with ID: {}, MonnaieId: {}, Budget: {}",
                                financementId.getFinancementId(),
                                financementRequest.getMonnaieId(),
                                financementRequest.getBudgetEstimer());
                    }

                    realisation.setFinancements(financements);
                    log.info("Assigned {} financements to realisation ID: {}", financements.size(), realisation.getId());

                    // >>> CHANGED: compute converted budget Σ(budgetEstimer × Monnaie.taux)
                    BigDecimal realisationBudgetFx = financements.stream()
                            .map(f -> {
                                BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                                Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0;
                                return amt.multiply(BigDecimal.valueOf(taux));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Set converted sum so the seuil check uses it
                    realisation.setBudgetEstime(realisationBudgetFx);

                    // >>> Validate for PPM using the converted amounts
                    setSeuilApplicableForRealisationInPpm(realisation, requestAcId);

                    // Keep the converted sum as the realisation budget (as requested)
                    // (No other logic changed)

                } else {
                    log.info("No financements provided for realisation ID: {}", realisation.getId());
                    realisation.setFinancements(new ArrayList<>());
                    realisation.setBudgetEstime(BigDecimal.ZERO);

                    // >>> still validate for PPM (uses 0)
                    setSeuilApplicableForRealisationInPpm(realisation, requestAcId);
                }

                realisationsToSave.add(realisation);
            }
            realisationRepository.saveAll(realisationsToSave);

        }

        // Step 11: Validate realisations for budget consistency
        validateRealisations(savedPpm);

        // Step 12: Calculate and set estimated budget based on realisations
        BigDecimal budgetEstime = savedPpm.getRealisations().stream()
                .map(Realisation::getBudgetEstime)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        savedPpm.setBudgetEstime(budgetEstime);

        // Step 13: Save and prepare the final PPM for return
        Ppm finalPpm = repository.save(savedPpm);

        return finalPpm;
    }


    @Override
    public Page<Ppm> findCurrentYearPpmsByUser(Pageable pageable) {
        log.info("Retrieving current year's PPMs for connected user with pageable: {}", pageable);

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
                return Page.empty(pageable); // Return empty page instead of throwing AcNotFound
            }
        }

        // Get current year
        String currentYear = String.valueOf(LocalDate.now().getYear());

        // Build specification
        Specification<Ppm> spec = Specification.where(PpmSpecifications.annee(currentYear))
                .and(PpmSpecifications.ppmAc(userAcIds))
                .and(PpmSpecifications.deleted(false))
                .and(PpmSpecifications.isLatestVersion());

        // Query PPMs with pagination
        Page<Ppm> ppmsPage = repository.findAll(spec, pageable);

        if (ppmsPage.isEmpty()) {
            log.info("No PPMs found for the current year {} and user ACs {}", currentYear, userAcIds);
            return Page.empty(pageable);
        }

        log.info("Retrieved {} PPMs for the current year {} and user ACs {}", ppmsPage.getContent().size(), currentYear, userAcIds);
        return ppmsPage;
    }

    @Override
    public List<Ac> getAcsWithoutPpmForCurrentYear() throws AcNotFound {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        log.info("Checking ACs without PPM for current year: {}", currentYear);

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

        Specification<Ppm> spec = (root, query, criteriaBuilder) -> {
            Predicate yearPredicate = criteriaBuilder.equal(root.get("annee"), currentYear);
            Predicate notDeletedPredicate = criteriaBuilder.isFalse(root.get("isDeleted"));
            return criteriaBuilder.and(yearPredicate, notDeletedPredicate);
        };
        List<Ppm> ppms = repository.findAll(spec);
        log.info("Found {} PPMs for year {} (non-deleted)", ppms.size(), currentYear);

        Set<Integer> acIdsWithPpm = ppms.stream()
                .filter(ppm -> ppm.getAc() != null)
                .map(ppm -> ppm.getAc().getId())
                .collect(Collectors.toSet());
        log.info("ACs with non-deleted PPM IDs: {}", acIdsWithPpm);

        List<Integer> acIdsWithoutPpm = userAcIds.stream()
                .filter(acId -> !acIdsWithPpm.contains(acId))
                .collect(Collectors.toList());
        log.info("ACs without non-deleted PPM IDs: {}", acIdsWithoutPpm);

        if (acIdsWithoutPpm.isEmpty()) {
            log.info("All ACs for user have non-deleted PPMs for year {}", currentYear);
            throw new AllAcsHavePpmException("Vous ne pouvez pas actuellement créer un nouveau PPM ! Toutes les AC dont vous êtes membre ont des PPM en cours pour l'année " + currentYear + ".");
        }

        List<Ac> acsWithoutPpm = acRepository.findAllById(acIdsWithoutPpm);
        log.info("Found {} ACs without non-deleted PPM for user", acsWithoutPpm.size());
        return acsWithoutPpm;
    }

    @Override
    public Page<Ppm> findAllForWebsite(Specification<Ppm> spec, Pageable pageable) {
        Specification<Ppm> finalSpec = Specification.where(spec)
                .and(PpmSpecifications.isLatestVersion())
                .and(PpmSpecifications.isPublished())
                .and(PpmSpecifications.isNotDeleted());
        return repository.findAll(finalSpec, pageable);
    }

    /**
     * Validates that all lots within a realisation in the request have consistent budgets (all null or all non-null).
     */
    private void validateRequestLotsConsistency(PpmWithLotsRequestDto request) {
        if (request.getRealisationsWithLots() != null) {
            for (RealisationWithLotsRequestDto realisation : request.getRealisationsWithLots()) {
                List<LotResponse> lots = realisation.getLots();
                if (lots != null && !lots.isEmpty()) {
                    boolean hasBudget = lots.get(0).getBudget() != null;
                    boolean allConsistent = lots.stream()
                            .allMatch(lot -> (lot.getBudget() != null) == hasBudget);

                    if (!allConsistent) {
                        throw new LotBudgetConsistencyException(
                                "Inconsistent budgets in lots for Realisation: " + realisation.getRealisationEnvisagees() +
                                        ": all lots must have either non-null or null budgets.");
                    }
                }
            }
        }
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
     * Validates all realisations associated with a PPM.
     */
    private void validateRealisations(Ppm ppm) {
        if (ppm.getRealisations() != null) {
            for (Realisation realisation : ppm.getRealisations()) {
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
                Specification<AcSeuil> acSeuilSpec = AcSeuilSpecifications.acId(acId)
                        .and(AcSeuilSpecifications.typeMarcherId(realisation.getTypeMarcher().getId()))
                        .and(AcSeuilSpecifications.modePassationId(realisation.getModePassation().getId()));
                Optional<AcSeuil> acSeuilOpt = acSeuilService.findOne(acSeuilSpec);
                if (acSeuilOpt.isPresent()) {
                    realisation.setSeuilApplicable(acSeuilOpt.get().getSeuil());
                    log.info("Set seuilApplicable to {} for Realisation ID: {} with AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}",
                            realisation.getSeuilApplicable(), realisation.getId(), acId,
                            realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());
                } else {
                    realisation.setSeuilApplicable(BigDecimal.ZERO);
                    log.warn("No AcSeuil found for AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}. Setting seuilApplicable to 0.",
                            acId, realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());
                }
            } catch (Exception e) {
                realisation.setSeuilApplicable(BigDecimal.ZERO);
                log.error("Failed to fetch AcSeuil for AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}. Setting seuilApplicable to 0. Error: {}",
                        acId, realisation.getTypeMarcher().getId(), realisation.getModePassation().getId(), e.getMessage());
            }
        } else {
            realisation.setSeuilApplicable(BigDecimal.ZERO);
            log.warn("TypeMarcher or ModePassation is null for Realisation ID: {}. Setting seuilApplicable to 0.", realisation.getId());
        }
    }


    private void processFinancementsForRealisation(Realisation realisation, List<FinancementRequest> financementRequests) {
        System.out.println("=== PROCESSING FINANCEMENTS FOR REALISATION ===");
        System.out.println("Realisation ID: " + realisation.getId() + ", Version: " + realisation.getVersion());

        if (financementRequests != null && !financementRequests.isEmpty()) {
            System.out.println("Found " + financementRequests.size() + " financements to process");

            List<Financement> financements = new ArrayList<>();

            List<Financement> existingFinancements = financementRepository.findByRealisationIdAndVersion(
                    realisation.getId(), realisation.getVersion());

            Set<Integer> requestedFinancementIds = financementRequests.stream()
                    .filter(financementRequest -> financementRequest.getFinancementId() != null)
                    .map(FinancementRequest::getFinancementId)
                    .collect(Collectors.toSet());

            for (FinancementRequest financementRequest : financementRequests) {
                if (financementRequest.getMonnaieId() == null) {
                    System.out.println("Skipping financement with null monnaieId");
                    continue;
                }

                Financement financement;
                Integer financementId = financementRequest.getFinancementId();

                System.out.println("Processing financement with ID: " + financementId);

                if (financementId != null && financementId > 0) {
                    System.out.println("CASE: UPDATING existing financement with ID: " + financementId);

                    Optional<Financement> existingFinancementOpt = existingFinancements.stream()
                            .filter(f -> f.getId().getFinancementId().equals(financementId) &&
                                    !Boolean.TRUE.equals(f.getIsDeleted()))
                            .findFirst();

                    if (existingFinancementOpt.isPresent()) {
                        financement = existingFinancementOpt.get();
                        System.out.println("Found existing financement - UPDATING");

                        // Update fields
                        financement.setBudgetEstimer(financementRequest.getBudgetEstimer());

                        // Update monnaie and auto-assign taux
                        Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
                                .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " +
                                        financementRequest.getMonnaieId() + " not found"));
                        financement.setMonnaie(monnaie);

                        // AUTO-ASSIGN TAUX FROM MONNAIE
                        if (monnaie.getTaux() != null) {
                            financement.setTauxApplicable(monnaie.getTaux());
                            System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux() +
                                    " from Monnaie ID: " + monnaie.getId());
                        } else {
                            financement.setTauxApplicable(0.0);
                            System.out.println("Monnaie taux is null, set tauxApplicable to 0.0");
                        }

                        // Update source financement
                        if (financementRequest.getSourceFinancement() != null &&
                                financementRequest.getSourceFinancement().getId() != null) {
                            RefParam sourceFinancement = refParamRepository.findById(
                                            financementRequest.getSourceFinancement().getId())
                                    .orElseThrow(() -> new EntityNotFoundException(
                                            "Source Financement with ID " +
                                                    financementRequest.getSourceFinancement().getId() + " not found"));
                            financement.setSourceFinancement(sourceFinancement);
                        } else {
                            financement.setSourceFinancement(null);
                        }

                        financement.setIsDeleted(false);
                        financement = financementRepository.save(financement);
                        System.out.println("UPDATED existing financement ID: " + financementId);

                    } else {
                        throw new EntityNotFoundException("Financement with ID " + financementId +
                                " not found for Realisation ID: " + realisation.getId());
                    }

                } else {
                    System.out.println("CASE: CREATING new financement (no ID provided)");

                    // Create new financement
                    Integer newFinancementId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);
                    newFinancementId = (newFinancementId != null) ? newFinancementId + 1 : 1;

                    financement = new Financement();
                    FinancementId compositeFinancementId = new FinancementId();
                    compositeFinancementId.setFinancementId(newFinancementId);
                    compositeFinancementId.setRealisation(realisation);

                    Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
                            .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " +
                                    financementRequest.getMonnaieId() + " not found"));
                    financement.setMonnaie(monnaie);
                    financement.setId(compositeFinancementId);
                    financement.setBudgetEstimer(financementRequest.getBudgetEstimer());

                    // AUTO-ASSIGN TAUX FROM MONNAIE
                    if (monnaie.getTaux() != null) {
                        financement.setTauxApplicable(monnaie.getTaux());
                        System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux() +
                                " from Monnaie ID: " + monnaie.getId());
                    } else {
                        financement.setTauxApplicable(0.0);
                        System.out.println("Monnaie taux is null, set tauxApplicable to 0.0");
                    }

                    if (financementRequest.getSourceFinancement() != null &&
                            financementRequest.getSourceFinancement().getId() != null) {
                        RefParam sourceFinancement = refParamRepository.findById(
                                        financementRequest.getSourceFinancement().getId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "Source Financement with ID " +
                                                financementRequest.getSourceFinancement().getId() + " not found"));
                        financement.setSourceFinancement(sourceFinancement);
                    } else {
                        financement.setSourceFinancement(null);
                    }

                    financement.setIsDeleted(false);
                    financement = financementRepository.save(financement);
                    System.out.println("CREATED new financement with ID: " + newFinancementId);
                }

                financements.add(financement);
            }

            for (Financement existingFinancement : existingFinancements) {
                if (!requestedFinancementIds.contains(existingFinancement.getId().getFinancementId()) &&
                        !Boolean.TRUE.equals(existingFinancement.getIsDeleted())) {
                    existingFinancement.setIsDeleted(true);
                    financements.add(existingFinancement);
                    System.out.println("Marked financement with ID: " +
                            existingFinancement.getId().getFinancementId() +
                            " as deleted (not in request)");
                }
            }

            realisation.setFinancements(financements);
            financementRepository.saveAll(financements);
            System.out.println("Processed " + financements.size() + " financements for realisation ID: " + realisation.getId());

        } else {
            // Clear financements if none provided
            if (realisation.getFinancements() != null && !realisation.getFinancements().isEmpty()) {
                for (Financement existingFinancement : realisation.getFinancements()) {
                    if (!Boolean.TRUE.equals(existingFinancement.getIsDeleted())) {
                        existingFinancement.setIsDeleted(true);
                        System.out.println("Marked financement with ID: " +
                                existingFinancement.getId().getFinancementId() +
                                " as deleted (no financements in request)");
                    }
                }
                financementRepository.saveAll(realisation.getFinancements());
            }
        }

        System.out.println("=== FINANCEMENT PROCESSING COMPLETE ===\n");
    }


//    private boolean processRealisationsWithFinancementLogic(Ppm existingPpm, PpmBaseRequestDto request, List<Realisation> updatedRealisations) {
//        System.out.println("\n=== STARTING FINANCEMENT LOGIC PROCESSING ===");
//        boolean financementsChanged = false;
//
//        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
//            System.out.println("Processing " + updatedRealisations.size() + " realisations for financement changes");
//
//            // Process each realisation for financement changes
//            for (int i = 0; i < updatedRealisations.size(); i++) {
//                Realisation realisation = updatedRealisations.get(i);
//                RealisationBaseRequestDto realisationRequest = request.getRealisations().get(i);
//
//                System.out.println("\n--- Checking realisation " + i + " (ID: " + realisation.getId() +
//                        ", Version: " + realisation.getVersion() + ") ---");
//
//                // Check if ANY financements changed for this realisation
//                boolean currentRealisationFinancementsChanged = areFinancementsChangedForRealisation(
//                        realisation, realisationRequest);
//
//                System.out.println("Financements changed for realisation " + realisation.getId() + ": " + currentRealisationFinancementsChanged);
//
//                if (currentRealisationFinancementsChanged) {
//                    financementsChanged = true;
//
//                    System.out.println("CREATING SINGLE NEW REALISATION VERSION due to financement changes");
//
//                    // Create ONE new realisation version for ALL financement changes
//                    Realisation newRealisationVersion = createNewRealisationVersionForFinancementBatch(
//                            realisation, realisationRequest);
//
//                    // Replace the realisation in the list with the new version
//                    updatedRealisations.set(i, newRealisationVersion);
//
//                    System.out.println("Created single realisation version " + newRealisationVersion.getVersion() +
//                            " for realisation ID " + newRealisationVersion.getId() +
//                            " (processed ALL financement changes in one version)");
//                } else {
//                    System.out.println("No financement changes detected for realisation " + realisation.getId());
//                }
//            }
//        }
//
//        System.out.println("=== FINANCEMENT LOGIC PROCESSING COMPLETE ===");
//        System.out.println("Overall financements changed: " + financementsChanged + "\n");
//        return financementsChanged;
//    }


//    private Realisation createNewRealisationVersionForFinancementBatch(Realisation existingRealisation, RealisationBaseRequestDto realisationRequest) {
//        // Create new realisation version (increment by 1 only)
//        Realisation newRealisation = new Realisation();
//
//        // Copy all basic fields from existing realisation
//        BeanUtils.copyProperties(existingRealisation, newRealisation);
//
//        // Increment version by 1 (regardless of how many financements change)
//        newRealisation.setVersion(existingRealisation.getVersion() + 1);
//
//        // Update fields from request
//        updateRealisationFields(newRealisation, realisationRequest);
//
//        // Save the new realisation version first
//        newRealisation = realisationRepository.save(newRealisation);
//
//        // Copy existing lots to new version (lots don't change in financement processing)
//        if (existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty()) {
//            List<Lot> existingLots = existingRealisation.getLots().stream()
//                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
//                    .collect(Collectors.toList());
//            newRealisation.setLots(existingLots);
//        }
//
//        // Process ALL financements for the new realisation version in ONE batch
//        System.out.println("Processing ALL financements in SINGLE version increment");
//        processAllFinancementsForRealisationVersion(newRealisation, realisationRequest);
//
//        // Save and return the new realisation version
//        return realisationRepository.save(newRealisation);
//    }


//    private void processAllFinancementsForRealisationVersion(Realisation realisation, RealisationBaseRequestDto realisationRequest) {
//        System.out.println("=== BATCH FINANCEMENT PROCESSING START ===");
//        System.out.println("Processing ALL financements for Realisation ID: " + realisation.getId() + ", Version: " + realisation.getVersion());
//
//        if (realisationRequest.getFinancements() != null && !realisationRequest.getFinancements().isEmpty()) {
//            System.out.println("Found " + realisationRequest.getFinancements().size() + " financements to process in SINGLE batch");
//
//            List<Financement> financements = new ArrayList<>();
//
//            // Process ALL financements in one go
//            for (FinancementRequest financementRequest : realisationRequest.getFinancements()) {
//                if (financementRequest.getMonnaieId() == null) {
//                    System.out.println("Skipping financement with null monnaieId");
//                    continue;
//                }
//
//                Financement financement;
//                Integer financementId = financementRequest.getFinancementId();
//
//                if (financementId != null && financementId > 0) {
//                    System.out.println("PRESERVING existing financement ID: " + financementId + " in new version");
//
//                    // Preserve existing financement ID in new realisation version
//                    financement = new Financement();
//                    FinancementId compositeFinancementId = new FinancementId();
//                    compositeFinancementId.setFinancementId(financementId);  // PRESERVE ID
//                    compositeFinancementId.setRealisation(realisation);
//                    financement.setId(compositeFinancementId);
//
//                    // Update fields
//                    financement.setBudgetEstimer(financementRequest.getBudgetEstimer());
//
//                    // Set monnaie and auto-assign taux
//                    Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
//                            .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " +
//                                    financementRequest.getMonnaieId() + " not found"));
//                    financement.setMonnaie(monnaie);
//
//                    // AUTO-ASSIGN TAUX FROM MONNAIE
//                    if (monnaie.getTaux() != null) {
//                        financement.setTauxApplicable(monnaie.getTaux());
//                        System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux() +
//                                " from Monnaie ID: " + monnaie.getId());
//                    } else {
//                        financement.setTauxApplicable(0.0);
//                        System.out.println("Monnaie taux is null, set tauxApplicable to 0.0");
//                    }
//
//                    // Set source financement
//                    if (financementRequest.getSourceFinancement() != null &&
//                            financementRequest.getSourceFinancement().getId() != null) {
//                        RefParam sourceFinancement = refParamRepository.findById(
//                                        financementRequest.getSourceFinancement().getId())
//                                .orElseThrow(() -> new EntityNotFoundException(
//                                        "Source Financement with ID " +
//                                                financementRequest.getSourceFinancement().getId() + " not found"));
//                        financement.setSourceFinancement(sourceFinancement);
//                    } else {
//                        financement.setSourceFinancement(null);
//                    }
//
//                    financement.setIsDeleted(false);
//                    financement = financementRepository.save(financement);
//
//                    System.out.println("PRESERVED financement ID " + financementId + " in batch");
//
//                } else {
//                    System.out.println("CREATING new financement in batch");
//
//                    // Create new financement
//                    Integer newFinancementId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);
//                    newFinancementId = (newFinancementId != null) ? newFinancementId + 1 : 1;
//
//                    financement = new Financement();
//                    FinancementId compositeFinancementId = new FinancementId();
//                    compositeFinancementId.setFinancementId(newFinancementId);
//                    compositeFinancementId.setRealisation(realisation);
//
//                    // ... rest of new financement creation logic (same as before)
//                    Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
//                            .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " +
//                                    financementRequest.getMonnaieId() + " not found"));
//                    financement.setMonnaie(monnaie);
//                    financement.setId(compositeFinancementId);
//                    financement.setBudgetEstimer(financementRequest.getBudgetEstimer());
//
//                    // AUTO-ASSIGN TAUX FROM MONNAIE
//                    if (monnaie.getTaux() != null) {
//                        financement.setTauxApplicable(monnaie.getTaux());
//                        System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux());
//                    } else {
//                        financement.setTauxApplicable(0.0);
//                    }
//
//                    if (financementRequest.getSourceFinancement() != null &&
//                            financementRequest.getSourceFinancement().getId() != null) {
//                        RefParam sourceFinancement = refParamRepository.findById(
//                                        financementRequest.getSourceFinancement().getId())
//                                .orElseThrow(() -> new EntityNotFoundException(
//                                        "Source Financement with ID " +
//                                                financementRequest.getSourceFinancement().getId() + " not found"));
//                        financement.setSourceFinancement(sourceFinancement);
//                    } else {
//                        financement.setSourceFinancement(null);
//                    }
//
//                    financement.setIsDeleted(false);
//                    financement = financementRepository.save(financement);
//                    System.out.println("CREATED new financement with ID: " + newFinancementId + " in batch");
//                }
//
//                financements.add(financement);
//            }
//
//            realisation.setFinancements(financements);
//            System.out.println("=== BATCH RESULT ===");
//            System.out.println("Processed " + financements.size() + " financements in SINGLE version " + realisation.getVersion());
//            for (Financement f : financements) {
//                System.out.println("  - FinancementId: " + f.getId().getFinancementId() +
//                        ", Budget: " + f.getBudgetEstimer() +
//                        ", Version: " + f.getId().getRealisation().getVersion());
//            }
//
//        } else {
//            System.out.println("No financements in request - clearing existing financements");
//            realisation.setFinancements(new ArrayList<>());
//        }
//
//        System.out.println("=== BATCH FINANCEMENT PROCESSING END ===\n");
//    }





//    private boolean areFinancementsChangedForRealisation(Realisation realisation, RealisationBaseRequestDto realisationRequest) {
//        List<FinancementRequest> requestFinancements = realisationRequest.getFinancements();
//        List<Financement> existingFinancements = realisation.getFinancements();
//
//        // If no financements in request but existing ones exist
//        if (requestFinancements == null || requestFinancements.isEmpty()) {
//            return existingFinancements != null &&
//                    existingFinancements.stream().anyMatch(f -> !Boolean.TRUE.equals(f.getIsDeleted()));
//        }
//
//        // If no existing financements but request has some
//        if (existingFinancements == null || existingFinancements.isEmpty()) {
//            return true; // New financements being added
//        }
//
//        // Check if count of active financements is different
//        long activeExistingCount = existingFinancements.stream()
//                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
//                .count();
//
//        if (requestFinancements.size() != activeExistingCount) {
//            System.out.println("Financement count changed: existing=" + activeExistingCount +
//                    ", requested=" + requestFinancements.size());
//            return true;
//        }
//
//        // Check each financement for changes
//        for (FinancementRequest requestFinancement : requestFinancements) {
//            if (requestFinancement.getFinancementId() != null) {
//                // Find existing financement
//                Optional<Financement> existingOpt = existingFinancements.stream()
//                        .filter(f -> f.getId().getFinancementId().equals(requestFinancement.getFinancementId())
//                                && !Boolean.TRUE.equals(f.getIsDeleted()))
//                        .findFirst();
//
//                if (existingOpt.isPresent()) {
//                    Financement existing = existingOpt.get();
//                    // Check if any field changed
//                    if (!Objects.equals(existing.getBudgetEstimer(), requestFinancement.getBudgetEstimer()) ||
//                            !Objects.equals(existing.getMonnaie().getId(), requestFinancement.getMonnaieId()) ||
//                            !Objects.equals(
//                                    existing.getSourceFinancement() != null ? existing.getSourceFinancement().getId() : null,
//                                    requestFinancement.getSourceFinancement() != null ? requestFinancement.getSourceFinancement().getId() : null)) {
//                        System.out.println("Financement ID " + requestFinancement.getFinancementId() + " has field changes");
//                        return true;
//                    }
//                } else {
//                    System.out.println("Financement ID " + requestFinancement.getFinancementId() + " not found in existing financements");
//                    return true; // Financement ID provided but not found
//                }
//            } else {
//                System.out.println("New financement detected (no ID provided)");
//                return true; // New financement (no ID)
//            }
//        }
//
//        return false; // No changes detected
//    }



//    private Realisation createNewRealisationVersionForFinancementChange(Realisation existingRealisation, RealisationBaseRequestDto realisationRequest) {
//        // Create new realisation version
//        Realisation newRealisation = new Realisation();
//
//        // Copy all basic fields from existing realisation
//        BeanUtils.copyProperties(existingRealisation, newRealisation);
//
//        // Increment version
//        newRealisation.setVersion(existingRealisation.getVersion() + 1);
//
//        // Update fields from request
//        updateRealisationFields(newRealisation, realisationRequest);
//
//        // Save the new realisation version first
//        newRealisation = realisationRepository.save(newRealisation);
//
//        // Copy existing lots to new version (lots don't change in financement processing)
//        if (existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty()) {
//            List<Lot> existingLots = existingRealisation.getLots().stream()
//                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
//                    .collect(Collectors.toList());
//            newRealisation.setLots(existingLots);
//        }
//
//        // Process financements for the new realisation version
//        processFinancementsForRealisationVersion(newRealisation, realisationRequest);
//
//        // Save and return the new realisation version
//        return realisationRepository.save(newRealisation);
//    }


//    private void processFinancementsForRealisationVersion(Realisation realisation, RealisationBaseRequestDto realisationRequest) {
//        System.out.println("=== ENHANCED FINANCEMENT PROCESSING START ===");
//        System.out.println("Processing financements for Realisation ID: " + realisation.getId() + ", Version: " + realisation.getVersion());
//
//        if (realisationRequest.getFinancements() != null && !realisationRequest.getFinancements().isEmpty()) {
//            System.out.println("Found " + realisationRequest.getFinancements().size() + " financements in request");
//
//            // Log each request in detail
//            for (int i = 0; i < realisationRequest.getFinancements().size(); i++) {
//                FinancementRequest req = realisationRequest.getFinancements().get(i);
//                System.out.println("=== FINANCEMENT REQUEST [" + i + "] DETAILS ===");
//                System.out.println("  Raw financementId from DTO: " + req.getFinancementId());
//                System.out.println("  MonnaieId: " + req.getMonnaieId());
//                System.out.println("  BudgetEstimer: " + req.getBudgetEstimer());
//
//                // Use reflection to check all fields
//                try {
//                    Field[] fields = FinancementRequest.class.getDeclaredFields();
//                    for (Field field : fields) {
//                        field.setAccessible(true);
//                        Object value = field.get(req);
//                        System.out.println("  Field '" + field.getName() + "': " + value);
//                    }
//                } catch (Exception e) {
//                    System.err.println("  Error inspecting fields: " + e.getMessage());
//                }
//            }
//
//            List<Financement> financements = new ArrayList<>();
//
//            for (FinancementRequest financementRequest : realisationRequest.getFinancements()) {
//                if (financementRequest.getMonnaieId() == null) {
//                    System.out.println("SKIPPING: MonnaieId is null");
//                    continue;
//                }
//
//                Integer financementId = financementRequest.getFinancementId();
//                System.out.println("\n=== PROCESSING FINANCEMENT ===");
//                System.out.println("Extracted financementId: " + financementId);
//                System.out.println("Is financementId null? " + (financementId == null));
//                System.out.println("Is financementId zero? " + (financementId != null && financementId == 0));
//
//                Financement financement;
//
//                if (financementId != null && financementId > 0) {
//                    System.out.println("CASE: PRESERVING EXISTING FINANCEMENT ID: " + financementId);
//
//                    // Create new financement with PRESERVED ID for new realisation version
//                    financement = new Financement();
//
//                    // Use the EXISTING ID - don't generate new one
//                    FinancementId compositeFinancementId = new FinancementId();
//                    compositeFinancementId.setFinancementId(financementId);  // ← PRESERVE EXISTING ID
//                    compositeFinancementId.setRealisation(realisation);
//                    financement.setId(compositeFinancementId);
//
//                    System.out.println("Created composite ID with PRESERVED financementId: " + financementId);
//
//                    // Set other fields
//                    financement.setBudgetEstimer(financementRequest.getBudgetEstimer());
//
//                    // Set monnaie and auto-assign taux
//                    Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
//                            .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " +
//                                    financementRequest.getMonnaieId() + " not found"));
//                    financement.setMonnaie(monnaie);
//
//                    // AUTO-ASSIGN TAUX FROM MONNAIE
//                    if (monnaie.getTaux() != null) {
//                        financement.setTauxApplicable(monnaie.getTaux());
//                        System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux() +
//                                " from Monnaie ID: " + monnaie.getId());
//                    } else {
//                        financement.setTauxApplicable(0.0);
//                        System.out.println("Monnaie taux is null, set tauxApplicable to 0.0");
//                    }
//
//                    // Set source financement
//                    if (financementRequest.getSourceFinancement() != null &&
//                            financementRequest.getSourceFinancement().getId() != null) {
//                        RefParam sourceFinancement = refParamRepository.findById(
//                                        financementRequest.getSourceFinancement().getId())
//                                .orElseThrow(() -> new EntityNotFoundException(
//                                        "Source Financement with ID " +
//                                                financementRequest.getSourceFinancement().getId() + " not found"));
//                        financement.setSourceFinancement(sourceFinancement);
//                    } else {
//                        financement.setSourceFinancement(null);
//                    }
//
//                    financement.setIsDeleted(false);
//                    financement = financementRepository.save(financement);
//
//                    System.out.println("SUCCESS: PRESERVED financement ID " + financementId +
//                            " for realisation version " + realisation.getVersion());
//
//                } else {
//                    System.out.println("CASE: CREATING NEW FINANCEMENT (no valid ID provided)");
//
//                    // Generate new ID only when truly needed
//                    Integer newFinancementId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);
//                    newFinancementId = (newFinancementId != null) ? newFinancementId + 1 : 1;
//
//                    System.out.println("Generated NEW financement ID: " + newFinancementId);
//
//                    financement = new Financement();
//                    FinancementId compositeFinancementId = new FinancementId();
//                    compositeFinancementId.setFinancementId(newFinancementId);
//                    compositeFinancementId.setRealisation(realisation);
//
//                    // ... rest of new financement creation logic (same as before)
//                    Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
//                            .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " +
//                                    financementRequest.getMonnaieId() + " not found"));
//                    financement.setMonnaie(monnaie);
//                    financement.setId(compositeFinancementId);
//                    financement.setBudgetEstimer(financementRequest.getBudgetEstimer());
//
//                    // AUTO-ASSIGN TAUX FROM MONNAIE
//                    if (monnaie.getTaux() != null) {
//                        financement.setTauxApplicable(monnaie.getTaux());
//                        System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux());
//                    } else {
//                        financement.setTauxApplicable(0.0);
//                    }
//
//                    if (financementRequest.getSourceFinancement() != null &&
//                            financementRequest.getSourceFinancement().getId() != null) {
//                        RefParam sourceFinancement = refParamRepository.findById(
//                                        financementRequest.getSourceFinancement().getId())
//                                .orElseThrow(() -> new EntityNotFoundException(
//                                        "Source Financement with ID " +
//                                                financementRequest.getSourceFinancement().getId() + " not found"));
//                        financement.setSourceFinancement(sourceFinancement);
//                    } else {
//                        financement.setSourceFinancement(null);
//                    }
//
//                    financement.setIsDeleted(false);
//                    financement = financementRepository.save(financement);
//                    System.out.println("CREATED truly new financement with ID: " + newFinancementId);
//                }
//
//                financements.add(financement);
//                System.out.println("Added financement to list. Final ID: " +
//                        financement.getId().getFinancementId());
//            }
//
//            realisation.setFinancements(financements);
//            System.out.println("=== FINAL RESULT ===");
//            System.out.println("Total financements processed: " + financements.size());
//            for (Financement f : financements) {
//                System.out.println("  - FinancementId: " + f.getId().getFinancementId() +
//                        ", Budget: " + f.getBudgetEstimer() +
//                        ", RealisationVersion: " + f.getId().getRealisation().getVersion());
//            }
//
//        } else {
//            System.out.println("No financements in request");
//            realisation.setFinancements(new ArrayList<>());
//        }
//
//        System.out.println("=== ENHANCED FINANCEMENT PROCESSING END ===\n");
//    }

    private boolean processFinancementsOnExistingVersion(Realisation realisation,
                                                         RealisationBaseRequestDto realisationRequest) {
        System.out.println("=== PROCESSING FINANCEMENTS (existing version) ===");
        System.out.println("Realisation ID: " + realisation.getId() + ", Version: " + realisation.getVersion());

        // If there is no DTO for this position, do nothing.
        if (realisationRequest == null) {
            System.out.println("No DTO for this realisation index → skipping financements");
            return false;
        }

        // Load financements for THIS (id, version)
        List<Financement> existing = financementRepository
                .findByRealisationIdAndVersion(realisation.getId(), realisation.getVersion());
        Map<Integer, Financement> existingById = existing.stream()
                .collect(Collectors.toMap(f -> f.getId().getFinancementId(), f -> f, (a,b) -> a));

        List<FinancementRequest> reqs = realisationRequest.getFinancements();
        boolean hasChanges = false;

        // Explicit “clear all” semantics when null OR empty
        if (reqs == null || reqs.isEmpty()) {
            boolean anyActive = false;
            for (Financement f : existing) {
                if (!Boolean.TRUE.equals(f.getIsDeleted())) {
                    f.setIsDeleted(true);
                    anyActive = true;
                }
            }
            if (anyActive) {
                financementRepository.saveAll(existing);
                realisation.setFinancements(existing);
            }
            System.out.println((reqs == null ? "Null" : "Empty") + " financements → soft-deleted all on this version");
            return anyActive;
        }

        // Process non-empty list
        Set<Integer> requestedIds = new HashSet<>();
        List<Financement> result = new ArrayList<>();

        for (FinancementRequest fr : reqs) {
            if (fr.getMonnaieId() == null) {
                System.out.println("Skipping financement with null monnaieId");
                continue;
            }

            Integer fid = fr.getFinancementId();
            Financement target = null;

            if (fid != null) {
                requestedIds.add(fid);
                target = existingById.get(fid); // only in THIS version
            }

            if (target == null) {
                // Create for this version (use provided id or generate a new one)
                if (fid == null) {
                    Integer next = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ);
                    fid = (next != null) ? next + 1 : 1;
                }
                target = new Financement();
                FinancementId cid = new FinancementId();
                cid.setRealisation(realisation);
                cid.setFinancementId(fid);
                target.setId(cid);
                hasChanges = true; // new row for this version
            }

            // Capture old values for change detection
            BigDecimal oldBudget = target.getBudgetEstimer();
            Integer oldMonnaieId = (target.getMonnaie() != null) ? target.getMonnaie().getId() : null;
            Integer oldSourceId = (target.getSourceFinancement() != null) ? target.getSourceFinancement().getId() : null;
            boolean oldDeleted = Boolean.TRUE.equals(target.getIsDeleted());

            // Apply new values
            target.setBudgetEstimer(fr.getBudgetEstimer());

            Monnaie monnaie = monnaieRepository.findById(fr.getMonnaieId())
                    .orElseThrow(() -> new EntityNotFoundException("Monnaie with ID " + fr.getMonnaieId() + " not found"));
            target.setMonnaie(monnaie);
            target.setTauxApplicable(monnaie.getTaux() != null ? monnaie.getTaux() : 0.0);

            if (fr.getSourceFinancement() != null && fr.getSourceFinancement().getId() != null) {
                RefParam src = refParamRepository.findById(fr.getSourceFinancement().getId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Source Financement with ID " + fr.getSourceFinancement().getId() + " not found"));
                target.setSourceFinancement(src);
            } else {
                target.setSourceFinancement(null);
            }

            target.setIsDeleted(false);

            // Detect field changes (budget/monnaie/source/isDeleted)
            boolean changed =
                    (oldBudget == null && target.getBudgetEstimer() != null)
                            || (oldBudget != null && target.getBudgetEstimer() != null
                            && oldBudget.compareTo(target.getBudgetEstimer()) != 0)
                            || !Objects.equals(oldMonnaieId, monnaie.getId())
                            || !Objects.equals(oldSourceId,
                            target.getSourceFinancement() != null ? target.getSourceFinancement().getId() : null)
                            || oldDeleted;

            if (changed) hasChanges = true;

            target = financementRepository.save(target);
            result.add(target);
        }

        // Soft-delete financements on this version that were not requested
        for (Financement f : existing) {
            Integer id = f.getId().getFinancementId();
            if (!requestedIds.contains(id) && !Boolean.TRUE.equals(f.getIsDeleted())) {
                f.setIsDeleted(true);
                financementRepository.save(f);
                result.add(f);
                hasChanges = true;
                System.out.println("Marked financement " + id + " as deleted (not in request)");
            }
        }

        realisation.setFinancements(result);
        System.out.println("Processed " + result.size() + " financements for realisation " + realisation.getId()
                + " v" + realisation.getVersion());
        System.out.println("=== FINANCEMENTS DONE ===");
        return hasChanges;
    }

    /**
     * PPM rule:
     * - Fetch AC seuil by (acId, typeMarcher, modePassation).
     * - Set realisation.seuilApplicable.
     * - If budgetEstime < seuil  -> throw RealisationBudgetBelowAcSeuilForPpmException
     *   (activity is compatible with PAA, not PPM).
     * - If no seuil or missing params -> set 0 and warn (same behavior as PAA helper).
     *
     * IMPORTANT: Call this only AFTER you finalized the realisation's financements/lots
     * and recomputed budgetEstime, to avoid auto-flush issues during the query.
     */
    private void setSeuilApplicableForRealisationInPpm(Realisation realisation, Integer acId) {
        if (acId == null) {
            realisation.setSeuilApplicable(BigDecimal.ZERO);
            log.warn("[PPM] AC ID is null for Realisation ID: {}. Setting seuilApplicable to 0.", realisation.getId());
            return;
        }

        if (realisation.getTypeMarcher() != null && realisation.getModePassation() != null) {
            try {
                log.info("[PPM] ************** ac id : {} type marche id : {} mode de passation : {}",
                        acId,
                        realisation.getTypeMarcher().getId(),
                        realisation.getModePassation().getId());

                Specification<AcSeuil> acSeuilSpec = AcSeuilSpecifications.acId(acId)
                        .and(AcSeuilSpecifications.typeMarcherId(realisation.getTypeMarcher().getId()))
                        .and(AcSeuilSpecifications.modePassationId(realisation.getModePassation().getId()));

                Optional<AcSeuil> acSeuilOpt = acSeuilService.findOne(acSeuilSpec);

                if (acSeuilOpt.isPresent()) {
                    BigDecimal acSeuil = acSeuilOpt.get().getSeuil();
                    realisation.setSeuilApplicable(acSeuil);

                    BigDecimal budgetEstime = realisation.getBudgetEstime() != null
                            ? realisation.getBudgetEstime()
                            : BigDecimal.ZERO;

                    log.info("[PPM] Set seuilApplicable={} for Realisation ID: {} (budgetEstime={}) with AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}",
                            acSeuil, realisation.getId(), budgetEstime, acId,
                            realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());

                    // PPM control: reject if budget is BELOW the seuil
                    if (budgetEstime.compareTo(acSeuil) < 0) {
                        String msg = String.format(
                                "La réalisation %s a un budget estimé %s inférieur au seuil AC %s. Cette activité est compatible avec le PAA, mais pas avec le PPM.",
                                realisation.getRealisationEnvisagees(), budgetEstime, acSeuil
                        );
                        log.error("[PPM] {}", msg);
                        throw new RealisationBudgetBelowAcSeuilForPpmException(msg);
                    }

                } else {
                    realisation.setSeuilApplicable(BigDecimal.ZERO);
                    log.warn("[PPM] No AcSeuil found for AC ID: {}, TypeMarcher ID: {}, ModePassation ID: {}. Setting seuilApplicable to 0.",
                            acId, realisation.getTypeMarcher().getId(), realisation.getModePassation().getId());
                }
            } catch (RealisationBudgetBelowAcSeuilForPpmException e) {
                // bubble up to stop processing this realisation in PPM flow
                throw e;
            }
        } else {
            realisation.setSeuilApplicable(BigDecimal.ZERO);
            log.warn("[PPM] TypeMarcher or ModePassation is null for Realisation ID: {}. Setting seuilApplicable to 0.",
                    realisation.getId());
        }
    }


}