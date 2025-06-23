package com.sigmap.common.ppms;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.lots.request.LotRequest;
import com.sigmap.common.lots.response.LotResponse;
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
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.request.RealisationWithLotsRequestDto;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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
    public PpmServiceImpl(PpmRepository repository, CurrentUserService currentUserService, AcRepository acRepository, RealisationRepository realisationRepository, RefParamRepository refParamRepository, DocsRepository docsRepository, RealisationService realisationService, LotRepository lotRepository, AlfrescoService alfrescoService, DocumentCommuneService documentCommuneService, DocsService docsService, AcService acService, SequenceGeneratorService sequenceGeneratorService) {
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
public Optional<Ppm> findById(Integer ppmId, Integer ppmVer) throws PpmNotFound, AcNotFound, PpmNotAssociatedWithUserAcException {
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
        throw new PpmNotFound("Aucun PPM trouvé pour l'ID", List.of(ppmId), ppmVer != null ? List.of(ppmVer) : Collections.emptyList());
    }
    log.debug("Version cible pour le PPM ID {}: {}", ppmId, targetVersion);

    // Step 5: Retrieve the PPM
    Optional<Ppm> ppmOpt = repository.findById(new CompositeId(ppmId, targetVersion));
    if (ppmOpt.isEmpty()) {
        log.warn("Aucun PPM trouvé pour l'ID: {} et la version: {}", ppmId, targetVersion);
        throw new PpmNotFound("Aucun PPM trouvé pour l'ID et la version", List.of(ppmId), List.of(targetVersion));
    }

    Ppm ppm = ppmOpt.get();
    log.debug("PPM récupéré: ID={}, Version={}, AC={}", ppm.getId(), ppm.getVersion(), ppm.getAc() != null ? ppm.getAc().getId() : "null");

    // Step 6: Check if the PPM is deleted
    if (Boolean.TRUE.equals(ppm.getIsDeleted())) {
        log.warn("PPM avec ID {} est marqué comme supprimé", ppmId);
        throw new PpmNotFound("PPM est supprimé", List.of(ppmId), List.of(targetVersion));
    }

    // Step 7: Validate AC or com association
    Integer ppmAcId = ppm.getAc() != null ? ppm.getAc().getId() : null;
    boolean hasMatchingAc = ppmAcId != null && userAcIds != null && userAcIds.contains(ppmAcId);
    boolean hasMatchingCom = false;

    // Check if any comId in ppm.coms matches user's comIds
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

    // Step 8: Prepare and return the response
    Ppm responsePpm = createFilteredResponsePpm(ppm);
    log.info("PPM avec ID {} récupéré avec succès pour l'utilisateur", ppmId);
    return Optional.ofNullable(responsePpm);
}
//    public Optional<Ppm> findById(Integer ppmId, Integer ppmVer) throws PpmNotFound, AcNotFound, PpmNotAssociatedWithUserAcException {
//        log.info("Recherche du PPM avec ID: {} et version: {}", ppmId, ppmVer);
//
//        // Step 1: Validate input
//        if (ppmId == null) {
//            log.error("PPM ID est null");
//            throw new IllegalArgumentException("PPM ID cannot be null");
//        }
//
//        // Step 2: Get user's AC IDs
//        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
//        log.info(userAcIds+"ee");
//        if (userAcIds == null || userAcIds.isEmpty()) {
//            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté. userAcIdsLong: {}", userAcIds);
//            throw new AcNotFound("Aucun ID AC trouvé pour l'utilisateur connecté", Collections.emptyList());
//        }
//
//        if (userAcIds.isEmpty()) {
//            log.warn("Aucun ID AC valide après conversion pour l'utilisateur connecté");
//            throw new AcNotFound("Aucun ID AC valide après conversion", Collections.emptyList());
//        }
//        log.debug("ACs de l'utilisateur connecté: {}", userAcIds);
//
//        // Step 3: Validate ACs
//        for (Integer acId : userAcIds) {
//            try {
//                log.debug("Validation de l'AC ID: {}", acId);
//                acService.findById(acId);
//            } catch (AcNotFound e) {
//                log.warn("AC non trouvé avec ID: {}", acId);
//                throw e;
//            }
//        }
//
//        // Step 4: Determine the version to use
//        Integer targetVersion = ppmVer != null ? ppmVer : repository.findMaxVersionById(ppmId);
//        if (targetVersion == null) {
//            log.warn("Aucun PPM trouvé pour l'ID: {}", ppmId);
//            throw new PpmNotFound("Aucun PPM trouvé pour l'ID", List.of(ppmId), ppmVer != null ? List.of(ppmVer) : Collections.emptyList());
//        }
//        log.debug("Version cible pour le PPM ID {}: {}", ppmId, targetVersion);
//
//        // Step 5: Retrieve the PPM
//        Optional<Ppm> ppmOpt = repository.findById(new CompositeId(ppmId, targetVersion));
//        if (ppmOpt.isEmpty()) {
//            log.warn("Aucun PPM trouvé pour l'ID: {} et la version: {}", ppmId, targetVersion);
//            throw new PpmNotFound("Aucun PPM trouvé pour l'ID et la version", List.of(ppmId), List.of(targetVersion));
//        }
//
//        Ppm ppm = ppmOpt.get();
//        log.debug("PPM récupéré: ID={}, Version={}, AC={}", ppm.getId(), ppm.getVersion(), ppm.getAc() != null ? ppm.getAc().getId() : "null");
//
//        // Step 6: Check if the PPM is deleted
//        if (Boolean.TRUE.equals(ppm.getIsDeleted())) {
//            log.warn("PPM avec ID {} est marqué comme supprimé", ppmId);
//            throw new PpmNotFound("PPM est supprimé", List.of(ppmId), List.of(targetVersion));
//        }
//
//        // Step 7: Validate AC association
//        Integer ppmAcId = ppm.getAc() != null ? ppm.getAc().getId() : null;
//        log.debug("Validation de l'association AC: PPM AC ID={}, User ACs={}", ppmAcId, userAcIds);
//        if (ppmAcId == null || !userAcIds.contains(ppmAcId)) {
//            log.warn("PPM avec ID {} n'est pas associé à un AC de l'utilisateur. AC du PPM: {}, ACs de l'utilisateur: {}", ppmId, ppmAcId, userAcIds);
//            throw new PpmNotAssociatedWithUserAcException(
//                    "Le PPM avec l'ID " + ppmId + " n'est pas associé à un AC de l'utilisateur connecté",
//                    List.of(ppmId.longValue()),
//                    List.of(targetVersion)
//            );
//        }
//
//        // Step 8: Final validation before return
//        if (ppm.getAc() == null || !userAcIds.contains(ppm.getAc().getId())) {
//            log.warn("Validation finale échouée: PPM avec ID {} n'est pas associé à un AC de l'utilisateur. AC du PPM: {}, ACs de l'utilisateur: {}", ppmId, ppm.getAc() != null ? ppm.getAc().getId() : "null", userAcIds);
//            throw new PpmNotAssociatedWithUserAcException(
//                    "Le PPM avec l'ID " + ppmId + " n'est pas associé à un AC de l'utilisateur connecté (validation finale)",
//                    List.of(ppmId.longValue()),
//                    List.of(targetVersion)
//            );
//        }
//
//        // Step 9: Prepare and return the response
//        Ppm responsePpm = createFilteredResponsePpm(ppm);
//        log.info("PPM avec ID {} récupéré avec succès pour l'utilisateur", ppmId);
//        return Optional.ofNullable(responsePpm);
//    }

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
    public Page<Ppm> findAll(Specification<Ppm> spec, Pageable pageable) throws PpmNotAssociatedWithUserAcException, AcNotFound, PpmNotFound {
        log.info("Recherche de tous les PPMs avec spécification: {}", spec);

        // Get user's AC IDs
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté");
            throw new AcNotFound("Aucun ID AC trouvé pour l'utilisateur connecté", Collections.emptyList());
        }

        // Validate ACs
        for (Integer acId : userAcIds) {
            try {
                acService.findById(acId);
            } catch (AcNotFound e) {
                log.warn("AC non trouvé avec ID: {}", acId);
                throw e;
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
            log.warn("Aucun PPM non supprimé trouvé dans la base de données");
            throw new PpmNotFound("Aucun PPM non supprimé trouvé dans la base de données", Collections.emptyList(), Collections.emptyList());
        }

        // Apply AC filter
        Specification<Ppm> acFilterSpec = (root, query, cb) -> cb.in(root.get("ac").get("id")).value(userAcIds);
        Specification<Ppm> combinedSpec = baseSpec.and(acFilterSpec);
        Page<Ppm> allPpms = repository.findAll(combinedSpec, pageable);

        if (allPpms.isEmpty()) {
            log.warn("Aucun PPM associé aux ACs de l'utilisateur: {}", userAcIds);
            throw new PpmNotAssociatedWithUserAcException(
                    "Aucun PPM associé aux ACs de l'utilisateur: " + userAcIds,
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        // Double-check AC association for each PPM
        List<Integer> invalidPpmIds = new ArrayList<>();
        List<Integer> invalidVersions = new ArrayList<>();
        for (Ppm ppm : allPpms.getContent()) {
            if (ppm.getAc() == null || !userAcIds.contains(ppm.getAc().getId())) {
                invalidPpmIds.add(ppm.getId());
                invalidVersions.add(ppm.getVersion());
            }
        }
        if (!invalidPpmIds.isEmpty()) {
            log.warn("Certains PPMs ne sont pas associés aux ACs de l'utilisateur: IDs={}, Versions={}", invalidPpmIds, invalidVersions);
            throw new PpmNotAssociatedWithUserAcException(
                    "Certains PPMs ne sont pas associés aux ACs de l'utilisateur",
                    invalidPpmIds.stream().map(Integer::longValue).collect(Collectors.toList()),
                    invalidVersions
            );
        }

        // Map the results to filter Docs
        Page<Ppm> ppmResponse = allPpms.map(this::createFilteredResponsePpm);
        log.info("Récupération de {} PPMs pour les AC de l'utilisateur {}", ppmResponse.getContent().size(), userAcIds);

        return ppmResponse;
    }
//    @Override
//    public Optional<Ppm> findLatestById(Integer ppmId) throws PpmNotFound, AcNotFound, PpmNotAssociatedWithUserAcException {
//        log.info("Recherche de la dernière version du PPM avec ID: {}", ppmId);
//
//        // Get user's AC IDs
//        List<Long> userAcIdsLong = currentUserService.getCurrentUserAcIds();
//        if (userAcIdsLong.isEmpty()) {
//            log.warn("Aucun ID AC trouvé pour l'utilisateur connecté");
//            throw new AcNotFound("Aucun ID AC trouvé pour l'utilisateur connecté", Collections.emptyList());
//        }
//        Set<Integer> userAcIds = userAcIdsLong.stream().map(Long::intValue).collect(Collectors.toSet());
//        log.debug("ACs de l'utilisateur connecté: {}", userAcIds);
//
//        // Validate ACs
//        for (Integer acId : userAcIds) {
//            try {
//                acService.findById(acId);
//            } catch (AcNotFound e) {
//                log.warn("AC non trouvé avec ID: {}", acId);
//                throw e;
//            }
//        }
//
//        // Find the latest version
//        Integer latestVersion = repository.findMaxVersionById(ppmId);
//        if (latestVersion == null) {
//            log.warn("Aucun PPM trouvé pour l'ID: {}", ppmId);
//            throw new PpmNotFound("Aucun PPM trouvé pour l'ID", List.of(ppmId), Collections.emptyList());
//        }
//        log.debug("Dernière version du PPM ID {}: {}", ppmId, latestVersion);
//
//        // Retrieve the PPM
//        Optional<Ppm> ppmOpt = repository.findById(new CompositeId(ppmId, latestVersion));
//        if (ppmOpt.isEmpty()) {
//            log.warn("Aucun PPM trouvé pour l'ID: {} et la version: {}", ppmId, latestVersion);
//            throw new PpmNotFound("Aucun PPM trouvé pour l'ID et la version", List.of(ppmId), List.of(latestVersion));
//        }
//
//        Ppm ppm = ppmOpt.get();
//        log.debug("PPM récupéré: ID={}, Version={}, AC={}", ppm.getId(), ppm.getVersion(), ppm.getAc() != null ? ppm.getAc().getId() : "null");
//
//        // Check if the PPM is deleted
//        if (Boolean.TRUE.equals(ppm.getIsDeleted())) {
//            log.warn("PPM avec ID {} est marqué comme supprimé", ppmId);
//            throw new PpmNotFound("PPM est supprimé", List.of(ppmId), List.of(latestVersion));
//        }
//
//        // Validate AC association
//        Integer ppmAcId = ppm.getAc() != null ? ppm.getAc().getId() : null;
//        if (ppmAcId == null || !userAcIds.contains(ppmAcId)) {
//            log.warn("PPM avec ID {} n'est pas associé à un AC de l'utilisateur. AC du PPM: {}, ACs de l'utilisateur: {}", ppmId, ppmAcId, userAcIds);
//            throw new PpmNotAssociatedWithUserAcException(
//                    "Le PPM avec l'ID " + ppmId + " n'est pas associé à un AC de l'utilisateur connecté",
//                    List.of(ppmId.longValue()),
//                    List.of(latestVersion)
//            );
//        }
//
//        Ppm responsePpm = createFilteredResponsePpm(ppm);
//        log.info("Dernière version du PPM avec ID {} récupérée avec succès pour l'utilisateur", ppmId);
//        return Optional.ofNullable(responsePpm);
//    }

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

                // Set up the relationship with PPM
                List<Ppm> ppmList = new ArrayList<>();
                ppmList.add(savedPpm);
                realisation.setPpms(ppmList);

                // Handle Lots if provided
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    Set<Lot> lots = new HashSet<>();

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

        if (isUpdatable) {
            // Create a new version
            Ppm newPpm = new Ppm();
            newPpm.setId(ppmId);
            newPpm.setVersion(ppmVer + 1); // Increment version
            // Set fields from PpmRequest, falling back to existing values if null
//            newPpm.setRef(request.getRef() != null ? request.getRef() : existingPpm.getRef());
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

            return repository.save(newPpm);
        } else {
            // Update existing version
//            existingPpm.setRef(request.getRef() != null ? request.getRef() : existingPpm.getRef());
            existingPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());
            existingPpm.setAnnee( existingPpm.getAnnee());
            existingPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());
            existingPpm.setRemarque(request.getRemarque() != null ? request.getRemarque() : existingPpm.getRemarque());
            // Update status if provided

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
    @Transactional
    @Override
    public Ppm updateWithVersioning(Integer ppmId, Integer ppmVer, PpmBaseRequestDto request) {
        // Log the request for debugging
        logUpdateRequest(ppmId, ppmVer, request);
        for(RealisationBaseRequestDto re : request.getRealisations() ) {
            System.out.println("doc realisation"+ re.getRealisationFiles());
        }

        // Get the existing PPM
        Ppm existingPpm = getExistingPpm(ppmId, ppmVer);

        // Create a temporary PPM with the new data for comparison
        Ppm tempPpm = buildTempPpm(existingPpm, request);

        // Check if the direct PPM fields have changed
        boolean directFieldsChanged = !arePpmFieldsIdentical(existingPpm, tempPpm);
        System.out.println("Direct field changes detected: " + directFieldsChanged);

        // Process realisations
        List<Realisation> updatedRealisations = new ArrayList<>();
        boolean realisationsChanged = processRealisationsWithLotLogic(existingPpm, request, updatedRealisations);

        // Process documents if present
        List<Docs> updatedDocs = new ArrayList<>();
        boolean documentsChanged = processDocuments(existingPpm, request, updatedDocs);

        // Get hash for the current request
        String requestHash = generateRequestHash(request);
        String previousRequestHash = getPreviousRequestHash(ppmId, ppmVer);

        // Check if files are present - if so, we'll always create a new version
        boolean hasFiles = false;
        if (request instanceof PpmWithLotsRequestDto) {
            PpmWithLotsRequestDto lotsRequest = (PpmWithLotsRequestDto) request;
            hasFiles = lotsRequest.getPpmFiles() != null && !lotsRequest.getPpmFiles().isEmpty();

            // Check for files in realisations
            if (!hasFiles && lotsRequest.getRealisationsWithLots() != null) {
                for (RealisationWithLotsRequestDto realisation : lotsRequest.getRealisationsWithLots()) {
                    if (realisation.getRealisationFiles() != null && !realisation.getRealisationFiles().isEmpty()) {
                        hasFiles = true;
                        break;
                    }
                }
            }
        }

        if (hasFiles) {
            System.out.println("Files detected in request - will create new version regardless of other changes");
        } else {
            // Only check hash if no files are present
            System.out.println("Current request hash: " + requestHash);
            System.out.println("Previous request hash: " + previousRequestHash);

            // If this is the exact same request as the previous one, return the existing PPM without changes
            if (requestHash != null && requestHash.equals(previousRequestHash)) {
                System.out.println("Duplicate request detected, returning existing PPM without changes");

                // Create a filtered copy for response
                Ppm copyForResponse = new Ppm();
                BeanUtils.copyProperties(existingPpm, copyForResponse);

                // Filter out deleted documents for the response
                if (existingPpm.getDocs() != null) {
                    copyForResponse.setDocs(
                        existingPpm.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .collect(Collectors.toList())
                    );
                }

                // Also filter realisation documents and lots
                if (copyForResponse.getRealisations() != null) {
                    for (Realisation real : copyForResponse.getRealisations()) {
                        // Filter out deleted documents
                        if (real.getDocs() != null) {
                            real.setDocs(
                                real.getDocs().stream()
                                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                                    .collect(Collectors.toList())
                            );
                        }

                        // Filter out deleted lots
                        if (real.getLots() != null) {
                            real.setLots(
                                real.getLots().stream()
                                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                                    .collect(Collectors.toSet())
                            );
                        }
                    }
                }

                return copyForResponse;
            }
        }

        // Determine if we need to create a new version
        boolean shouldCreateNewVersion = directFieldsChanged || realisationsChanged || documentsChanged || hasFiles;
        logVersionDecision(shouldCreateNewVersion, directFieldsChanged, realisationsChanged, documentsChanged);

        // If no changes, return the existing PPM
        if (!shouldCreateNewVersion) {
            System.out.println("No changes detected, returning existing PPM without creating new version");
            return existingPpm;
        }

        // Store the request hash for the current version
        storePreviousRequestHash(ppmId, ppmVer, requestHash);

        // Process document IDs for the new version
        if (request.getDocIds() != null) {
            updatedDocs = new ArrayList<>();
            Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());

            // Process existing documents first
            if (existingPpm.getDocs() != null) {
                for (Docs existingDoc : existingPpm.getDocs()) {
                    // Get a fresh copy of the document to avoid stale data
                    Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                    if (docToProcess == null) {
                        System.out.println("Warning: Document " + existingDoc.getId() + " no longer exists in database, skipping");
                        continue;
                    }

                    if (requestedDocIds.contains(docToProcess.getId())) {
                        // This doc is in the request, ensure it's not marked as deleted
                        docToProcess.setIsDeleted(false);
                        docToProcess = docsRepository.save(docToProcess);
                        updatedDocs.add(docToProcess);
                        System.out.println("Keeping document with ID " + docToProcess.getId() + " active for new version");

                        // Remove from requested IDs since we've handled it
                        requestedDocIds.remove(docToProcess.getId());
                    } else {
                        // This doc is not in the request, mark it as deleted
                        docToProcess.setIsDeleted(true);
                        docToProcess = docsRepository.save(docToProcess);
                        updatedDocs.add(docToProcess); // Keep it in the collection but as deleted
                        System.out.println("Marked document with ID " + docToProcess.getId() + " as deleted for new version");
                    }
                }
            }

            // Now process any new documents in the request
            for (Integer docId : requestedDocIds) {
                // Skip if we've already processed this document
                if (updatedDocs.stream().anyMatch(doc -> doc.getId().equals(docId))) {
                    continue;
                }

                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));

                // Ensure it's active
                doc.setIsDeleted(false);
                doc = docsRepository.save(doc);

                updatedDocs.add(doc);
                System.out.println("Added new document with ID " + doc.getId() + " for new version");
            }
        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
            // No document IDs provided, but existing docs exist - mark all as deleted
            updatedDocs = new ArrayList<>();
            for (Docs existingDoc : existingPpm.getDocs()) {
                // Get a fresh copy of the document
                Docs docToProcess = docsRepository.findById(existingDoc.getId()).orElse(null);
                if (docToProcess == null) continue;

                // Mark as deleted
                docToProcess.setIsDeleted(true);
                docToProcess = docsRepository.save(docToProcess);
                updatedDocs.add(docToProcess);
                System.out.println("Marked document with ID " + docToProcess.getId() + " as deleted (no docIds provided)");
            }
            // Keep the existing docs list to preserve relationships (just all marked as deleted)
        } else {
            // No docs in request or existing PPM
            updatedDocs = new ArrayList<>();
        }

        // Create a new PPM version
        System.out.println("Creating new PPM version: " + (ppmVer + 1));
        Ppm newPpm = createAndSaveNewPpmVersion(ppmId, ppmVer, tempPpm, updatedRealisations, updatedDocs, existingPpm);

        newPpm.setRef(existingPpm.getRef());


        // Process new files if present (for PpmWithLotsRequestDto)
        if (request instanceof PpmWithLotsRequestDto) {
            PpmWithLotsRequestDto lotsRequest = (PpmWithLotsRequestDto) request;
            if (lotsRequest.getPpmFiles() != null && !lotsRequest.getPpmFiles().isEmpty()) {
                try {
                    System.out.println("Processing " + lotsRequest.getPpmFiles().size() + " new files for PPM");
                    //processAndSaveDocuments(lotsRequest.getPpmFiles(), newPpm);
                    documentCommuneService.processAndSaveDocuments(lotsRequest.getPpmFiles(),newPpm,Constants.PATH_ENTREPOT_GED_PPM);

                    // Save the PPM again after adding documents
                    newPpm = repository.save(newPpm);
                    System.out.println("Saved PPM after processing files");
                } catch (IOException e) {
                    System.err.println("Error processing PPM files: " + e.getMessage());
                    throw new RuntimeException("Error processing PPM files: " + e.getMessage(), e);
                }
            }
        }

        // Update the hash for the new version
        storePreviousRequestHash(ppmId, newPpm.getVersion(), requestHash);

        // Create a filtered copy for the response to exclude deleted items
        Ppm responseVersion = new Ppm();
        BeanUtils.copyProperties(newPpm, responseVersion);

        // Filter out any deleted docs in the response
        if (responseVersion.getDocs() != null) {
            List<Docs> filteredDocs = newPpm.getDocs().stream()
                .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                .collect(Collectors.toList());

            System.out.println("Filtered docs for response: " + filteredDocs.size() + " docs (from " + newPpm.getDocs().size() + " total)");
            System.out.println("Filtered doc IDs: " + filteredDocs.stream().map(Docs::getId).collect(Collectors.toList()));

            responseVersion.setDocs(filteredDocs);
        }

        // Filter out any deleted items in realisations
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
                            .collect(Collectors.toSet())
                    );
                }
            }
        }

        System.out.println("Returning filtered new PPM version: " + responseVersion.getVersion());
        return responseVersion;
    }

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
                baseRealisation.setService(realisationWithLots.getService());
                baseRealisation.setDirection(realisationWithLots.getDirection());
                baseRealisation.setSourceFinancement(realisationWithLots.getSourceFinancement());
                baseRealisation.setMonais(realisationWithLots.getMonais());
                baseRealisation.setTypeMarcher(realisationWithLots.getTypeMarcher());
                baseRealisation.setModeSelection(realisationWithLots.getModeSelection());
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

        tempPpm.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPpm.getAnnee());
        tempPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());
        RefParam requestModePassation = null;
        if (request.getModePassation() != null) {
            requestModePassation = refParamRepository.findById(request.getModePassation().getId()).orElse(null);
        }
        tempPpm.setStatus(existingPpm.getStatus());
        tempPpm.setModePassation(requestModePassation != null ? requestModePassation : existingPpm.getModePassation());
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
    private boolean processRealisationsWithLotLogic(Ppm existingPpm, PpmBaseRequestDto request, List<Realisation> updatedRealisations) {
        System.out.println("============== REALISATION PROCESSING LOG START ==============");
        System.out.println("Processing realisations for PPM ID " + existingPpm.getId() + ", version " + existingPpm.getVersion());

        boolean realisationsChanged = false;
        Map<String, Lot> newLotsMap = new HashMap<>();
        Set<Integer> existingRealisationIdsInRequest = new HashSet<>();

        // Keep track of realizations that have lots being added to them and should not be deleted
        Set<Integer> realizationsToKeep = new HashSet<>();

        // Create a map to track realisations by content hash for duplicate detection
        Map<String, Realisation> realisationHashMap = new HashMap<>();

        // Log file details if present in request
        if (request instanceof PpmWithLotsRequestDto) {
            PpmWithLotsRequestDto withLotsRequest = (PpmWithLotsRequestDto) request;
            if (withLotsRequest.getRealisationsWithLots() != null) {
                System.out.println("Request contains " + withLotsRequest.getRealisationsWithLots().size() + " realisations with lots");

                // Check if any are adding lots to existing realizations
                for (RealisationWithLotsRequestDto realisationDto : withLotsRequest.getRealisationsWithLots()) {
                    if (realisationDto.getId() == null &&
                            realisationDto.getLots() != null &&
                            !realisationDto.getLots().isEmpty()) {
                        System.out.println("Found request with new lots but no realization ID - will try to add to existing realizations");
                    } else if (realisationDto.getId() != null) {
                        // Keep track of IDs in request to avoid marking them as deleted later
                        existingRealisationIdsInRequest.add(realisationDto.getId());
                    }
                }
            }
        }

        // Create a map of existing realisations by ID for easier lookup
        Map<Integer, Realisation> existingRealisationsMap = new HashMap<>();
        if (existingPpm.getRealisations() != null) {
            for (Realisation existingRealisation : existingPpm.getRealisations()) {
                if (Boolean.TRUE.equals(existingRealisation.getIsDeleted())) {
                    continue; // Skip deleted realisations
                }
                existingRealisationsMap.put(existingRealisation.getId(), existingRealisation);

                // Also generate a content hash for each existing realisation to help with duplicate detection
                String contentHash = generateRealisationContentHash(existingRealisation);
                realisationHashMap.put(contentHash, existingRealisation);
                System.out.println("Added existing realisation ID " + existingRealisation.getId() +
                        " ver " + existingRealisation.getVersion() +
                        " to hash map with key " + contentHash);
            }
        }
        System.out.println("Existing realisations map size: " + existingRealisationsMap.size());

        // Track which existing realisations are present in the request
        Set<Integer> existingRealisationIdsInRequest2 = new HashSet<>();

        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            System.out.println("Request contains " + request.getRealisations().size() + " realisations");

            // First pass - generate content hash for each realisation in request
            // and check if we already have an identical realisation
            for (RealisationBaseRequestDto realisationDto : request.getRealisations()) {
                String contentHash = generateRealisationDtoContentHash(realisationDto);
                System.out.println("Realisation DTO content hash: " + contentHash);

                // Check if we've already processed an identical realisation
                if (realisationHashMap.containsKey(contentHash)) {
                    Realisation existingIdenticalRealisation = realisationHashMap.get(contentHash);
                    System.out.println("Found existing identical realisation with ID " +
                            existingIdenticalRealisation.getId() +
                            " ver " + existingIdenticalRealisation.getVersion());

                    // If this is a new realisation (no ID) but identical to an existing one,
                    // set its ID to reuse the existing one
                    if (realisationDto.getId() == null) {
                        System.out.println("Setting new realisation DTO to reuse existing ID " +
                                existingIdenticalRealisation.getId());
                        realisationDto.setId(existingIdenticalRealisation.getId());
                        realisationDto.setVersion(existingIdenticalRealisation.getVersion());
                    }
                }
            }

            // Now process each realisation
            for (RealisationBaseRequestDto realisationDto : request.getRealisations()) {
                try {
                    System.out.println("Processing realisation DTO: " + realisationDto);
                    // Check if this is a RealisationWithLotsRequestDto and if it has files
                    if (realisationDto instanceof RealisationWithLotsRequestDto) {
                        RealisationWithLotsRequestDto withLotsDto = (RealisationWithLotsRequestDto) realisationDto;
                        if (withLotsDto.getRealisationFiles() != null && !withLotsDto.getRealisationFiles().isEmpty()) {
                            System.out.println("Found " + withLotsDto.getRealisationFiles().size() +
                                    " files for realisation " +
                                    (withLotsDto.getId() != null ? withLotsDto.getId() : "new"));
                        }
                    }

                    if (realisationDto.getId() != null) {
                        existingRealisationIdsInRequest2.add(realisationDto.getId());
                        System.out.println("Processing existing realisation with ID: " + realisationDto.getId());

                        // Determine which version to use
                        Integer versionToUse = realisationDto.getVersion();
                        if (versionToUse == null) {
                            Integer maxVersion = realisationRepository.findMaxVersionById(realisationDto.getId());
                            if (maxVersion != null) {
                                versionToUse = maxVersion;
                            } else {
                                throw new RuntimeException("No versions found for Realisation with ID: " + realisationDto.getId());
                            }
                        }
                        System.out.println("Using version " + versionToUse + " for realisation " + realisationDto.getId());

                        // Find the existing realisation
                        CompositeId realisationId = new CompositeId(realisationDto.getId(), versionToUse);
                        Realisation existingRealisation = realisationRepository.findById(realisationId)
                                .orElseThrow(() -> new RuntimeException("Realisation not found with ID: " + realisationId.getId() + " and version: " + realisationId.getVersion()));

                        System.out.println("Processing existing realisation ID " + existingRealisation.getId() + ", version " + existingRealisation.getVersion());

                        // Generate a content hash for both DTO and existing realisation for comparison
                        String dtoContentHash = generateRealisationDtoContentHash(realisationDto);
                        String existingContentHash = generateRealisationContentHash(existingRealisation);

                        System.out.println("DTO content hash: " + dtoContentHash);
                        System.out.println("Existing realisation content hash: " + existingContentHash);

                        // If the hashes match and no files are present, there are no substantive changes
                        boolean hasFiles = false;

                        if (realisationDto instanceof RealisationWithLotsRequestDto
                                || (realisationDto.getRealisationFiles() != null && !realisationDto.getRealisationFiles().isEmpty())) {

                            RealisationBaseRequestDto withLotsDto = (RealisationBaseRequestDto) realisationDto;
                            hasFiles = withLotsDto.getRealisationFiles() != null && !withLotsDto.getRealisationFiles().isEmpty();
                        }

                        boolean contentIdentical = dtoContentHash.equals(existingContentHash);
                        System.out.println("Content identical: " + contentIdentical + ", Has files: " + hasFiles);

                        if (contentIdentical && !hasFiles) {
                            // No changes, use existing realisation
                            System.out.println("No changes detected in realisation " + existingRealisation.getId() +
                                    ", reusing existing version " + existingRealisation.getVersion());
                            updatedRealisations.add(existingRealisation);
                            continue;
                        }

                        // Detailed comparison for better change detection
                        boolean fieldsNeedUpdate = realisationNeedsUpdate(existingRealisation, realisationDto);
                        boolean docsNeedUpdate = checkRealisationDocumentsChanged(existingRealisation, realisationDto);
                        boolean updateNeeded = fieldsNeedUpdate || docsNeedUpdate || hasFiles;

                        System.out.println("Fields need update: " + fieldsNeedUpdate);
                        System.out.println("Docs need update: " + docsNeedUpdate);
                        System.out.println("Has files: " + hasFiles);
                        System.out.println("Update needed: " + updateNeeded);

                        if (updateNeeded) {
                            System.out.println("Realisation " + existingRealisation.getId() + " needs update: fields=" +
                                    fieldsNeedUpdate + ", docs=" + docsNeedUpdate + ", files=" + hasFiles);

                            // Process lots first to get the current set that should be preserved during versioning
                            Set<Lot> updatedLots = new HashSet<>();
                            boolean lotChanges = false;

                            // Only check for lot changes if we have a RealisationWithLotsRequestDto
                            if (realisationDto instanceof RealisationWithLotsRequestDto) {
                                System.out.println("Processing lots before versioning realisation to preserve them in the new version");
                                updatedLots = processLotsForRealisation(existingRealisation, realisationDto, newLotsMap);
                                lotChanges = lotsChanged(existingRealisation, realisationDto, updatedLots);
                                System.out.println("Found " + updatedLots.size() + " lots that will be preserved in new version");
                            } else if (existingRealisation.getLots() != null) {
                                // Copy existing lots
                                updatedLots.addAll(existingRealisation.getLots());
                                System.out.println("Preserving " + updatedLots.size() + " existing lots in new version");
                            }

                            // Use the realisation service to create a new version
                            Realisation updatedRealisation = realisationService.updateWithVersioning(
                                    realisationDto.getId(), versionToUse, realisationDto);

                            System.out.println("Created new version of realisation: " + updatedRealisation.getId() +
                                    ", version: " + updatedRealisation.getVersion());

                            // Preserve lots in the new version
                            if (!updatedLots.isEmpty()) {
                                System.out.println("Setting " + updatedLots.size() + " lots on the new version of realisation");

                                // Log the lots being preserved
                                for (Lot lot : updatedLots) {
                                    System.out.println("  - Preserving Lot ID: " + lot.getId() +
                                            ", Version: " + lot.getVersion() +
                                            ", Description: " + lot.getDescription() +
                                            " in new realisation version");

                                    // Update the bidirectional relationship
                                    if (lot.getRealisations() == null) {
                                        lot.setRealisations(new HashSet<>());
                                    }
                                    // Replace old realisation with new version
                                    lot.getRealisations().remove(existingRealisation);
                                    lot.getRealisations().add(updatedRealisation);
                                    lotRepository.save(lot);
                                }

                                // Set lots on the new realisation
                                updatedRealisation.setLots(updatedLots);
                                updatedRealisation = realisationRepository.save(updatedRealisation);

                                System.out.println("Saved realisation with lots. New version now has " +
                                        (updatedRealisation.getLots() != null ?
                                                updatedRealisation.getLots().size() : 0) + " lots");
                            }

                            // Process files if present
                            System.out.println("------- yes we have files ---------");
                            System.out.println(hasFiles);
                            if (hasFiles) {
                                try {
                                    RealisationBaseRequestDto withLotsDto =  realisationDto;
                                    System.out.println("Processing " + withLotsDto.getRealisationFiles().size() +
                                            " files for updated realisation " + updatedRealisation.getId() +
                                            ", version " + updatedRealisation.getVersion());
                                    //processAndSaveRelisationsDocumentsForUpdate(withLotsDto.getRealisationFiles(), updatedRealisation);
                                    documentCommuneService.processAndSaveDocuments(withLotsDto.getRealisationFiles(),updatedRealisation,Constants.PATH_ENTREPOT_GED_PPM+"/"+existingPpm.getId()+Constants.PATH_ENTREPOT_GED_REALISATION);

                                    // Refresh the realisation to get the updated document list
                                    updatedRealisation = realisationRepository.findById(
                                                    new CompositeId(updatedRealisation.getId(), updatedRealisation.getVersion()))
                                            .orElse(updatedRealisation);

                                    System.out.println("After file processing, realisation has " +
                                            (updatedRealisation.getDocs() != null ?
                                                    updatedRealisation.getDocs().size() : 0) + " documents");
                                } catch (Exception e) {
                                    System.err.println("ERROR processing realisation files: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                            updatedRealisations.add(updatedRealisation);
                            realisationsChanged = true;

                            // Update the hash map with this new version
                            String updatedHash = generateRealisationContentHash(updatedRealisation);
                            realisationHashMap.put(updatedHash, updatedRealisation);
                        } else {
                            // No field or document changes, but check if lots have changed
                            // Lots changes should NOT trigger versioning of the realisation
                            Set<Lot> updatedLots = processLotsForRealisation(existingRealisation, realisationDto, newLotsMap);
                            boolean lotChanges = lotsChanged(existingRealisation, realisationDto, updatedLots);

                            System.out.println("Lot changes detected: " + lotChanges);

                            if (lotChanges) {
                                System.out.println("==== UPDATING LOTS ON EXISTING REALISATION " + existingRealisation.getId() +
                                        " WITHOUT VERSIONING ====");

                                // Log the updated lots before setting them
                                System.out.println("Lots to be set on realisation:");
                                for (Lot lot : updatedLots) {
                                    System.out.println("  - Lot ID: " + lot.getId() +
                                            ", Version: " + lot.getVersion() +
                                            ", Description: " + lot.getDescription());

                                    // Check if lot has realisation relationship
                                    if (lot.getRealisations() == null) {
                                        System.out.println("    WARNING: Lot has null realisations collection, fixing...");
                                        lot.setRealisations(new HashSet<>());
                                        lot.getRealisations().add(existingRealisation);
                                        lotRepository.save(lot);
                                    } else if (!lot.getRealisations().contains(existingRealisation)) {
                                        System.out.println("    WARNING: Lot doesn't have this realisation in its relationships, fixing...");
                                        lot.getRealisations().add(existingRealisation);
                                        lotRepository.save(lot);
                                    } else {
                                        System.out.println("    Lot correctly has this realisation in its relationships");
                                    }
                                }

                                // Set the lots on the realisation
                                existingRealisation.setLots(updatedLots);
                                System.out.println("Set " + updatedLots.size() + " lots on realisation " + existingRealisation.getId());
                                realisationsChanged = true;
                            }

                            // Add the new realisation to our tracking list
                            updatedRealisations.add(existingRealisation);
                            realisationsChanged = true;

                            // Add to hash map for future reference
                            String existingRealisationHash = generateRealisationContentHash(existingRealisation);
                            realisationHashMap.put(existingRealisationHash, existingRealisation);
                            System.out.println("Added existing realisation ID " + existingRealisation.getId() +
                                    " to hash map with key " + existingRealisationHash);
                        }
                    } else {
                        // This is a new realisation - create a new one
                        System.out.println("Processing new realisation (no ID)");

                        // First check if we have an existing one with identical content to avoid duplication
                        String dtoContentHash = generateRealisationDtoContentHash(realisationDto);
                        System.out.println("New realisation DTO content hash: " + dtoContentHash);

                        if (realisationHashMap.containsKey(dtoContentHash)) {
                            // We found an existing realisation with identical content
                            Realisation existingIdenticalRealisation = realisationHashMap.get(dtoContentHash);
                            System.out.println("Found existing identical realisation with ID " +
                                    existingIdenticalRealisation.getId() +
                                    ", version " + existingIdenticalRealisation.getVersion() +
                                    " - reusing instead of creating new");

                            // Use the existing realisation to avoid duplication
                            updatedRealisations.add(existingIdenticalRealisation);

                            // Mark as changed since we're adding a realisation from the request
                            realisationsChanged = true;
                        } else {
                            // Create a new realisation using the realisation service
                            System.out.println("Creating new realisation");
                            RealisationRequest realisationRequest = mapToRealisationRequest(realisationDto);
                            Realisation newRealisation = realisationService.create(realisationRequest);
                            System.out.println("Created new realisation with ID " + newRealisation.getId() +
                                    ", version " + newRealisation.getVersion());

                            // Check if this is a RealisationWithLotsRequestDto and needs lots to be added
                            if (realisationDto instanceof RealisationWithLotsRequestDto) {
                                RealisationWithLotsRequestDto withLotsDto = (RealisationWithLotsRequestDto) realisationDto;
                                if (withLotsDto.getLots() != null && !withLotsDto.getLots().isEmpty()) {
                                    System.out.println("Processing " + withLotsDto.getLots().size() +
                                            " lots for new realisation");

                                    // Process lots for this new realisation
                                    Set<Lot> updatedLots = processLotsForRealisation(newRealisation, withLotsDto, newLotsMap);

                                    // Log the updated lots
                                    System.out.println("Lots to be set on new realisation:");
                                    for (Lot lot : updatedLots) {
                                        System.out.println("  - Lot ID: " + lot.getId() +
                                                ", Version: " + lot.getVersion() +
                                                ", Description: " + lot.getDescription());

                                        // Ensure bidirectional relationship
                                        if (lot.getRealisations() == null) {
                                            System.out.println("    WARNING: Lot has null realisations collection, fixing...");
                                            lot.setRealisations(new HashSet<>());
                                        }
                                        lot.getRealisations().add(newRealisation);
                                        lotRepository.save(lot);
                                    }

                                    // Set the lots on the realisation
                                    newRealisation.setLots(updatedLots);
                                    newRealisation = realisationRepository.save(newRealisation);
                                    System.out.println("Set " + updatedLots.size() + " lots on new realisation " +
                                            newRealisation.getId());
                                }

                                // Process files if present
                                if (withLotsDto.getRealisationFiles() != null && !withLotsDto.getRealisationFiles().isEmpty()) {
                                    try {
                                        System.out.println("Processing " + withLotsDto.getRealisationFiles().size() +
                                                " files for new realisation " + newRealisation.getId());
                                        //processAndSaveRelisationsDocuments(withLotsDto.getRealisationFiles(), newRealisation);
                                            documentCommuneService.processAndSaveDocuments(withLotsDto.getRealisationFiles(),newRealisation,Constants.PATH_ENTREPOT_GED_PPM+"/"+existingPpm.getId()+Constants.PATH_ENTREPOT_GED_REALISATION);
                                        // Refresh the realisation to get the updated document list
                                        newRealisation = realisationRepository.findById(
                                                        new CompositeId(newRealisation.getId(), newRealisation.getVersion()))
                                                .orElse(newRealisation);

                                        System.out.println("After file processing, realisation has " +
                                                (newRealisation.getDocs() != null ?
                                                        newRealisation.getDocs().size() : 0) + " documents");
                                    } catch (Exception e) {
                                        System.err.println("ERROR processing realisation files: " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            }

                            // Add the new realisation to our collections
                            updatedRealisations.add(newRealisation);
                            realisationsChanged = true;

                            // Add to hash map for future reference
                            String newRealisationHash = generateRealisationContentHash(newRealisation);
                            realisationHashMap.put(newRealisationHash, newRealisation);
                            System.out.println("Added new realisation ID " + newRealisation.getId() +
                                    " to hash map with key " + newRealisationHash);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("ERROR processing realisation: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Error processing Realisation: " + e.getMessage(), e);
                }
            }
        }

        // Handle deletion of realisations not in the request
        if (existingPpm.getRealisations() != null) {
            for (Realisation existingRealisation : existingPpm.getRealisations()) {
                if (existingRealisation.getId() != null &&
                        !existingRealisationIdsInRequest.contains(existingRealisation.getId()) &&
                        !Boolean.TRUE.equals(existingRealisation.getIsDeleted())) {

                    // If the request contains a realization with just new lots (no ID), we should NOT
                    // delete existing realizations because this likely means we want to add lots to them
                    boolean hasNewRealizationWithLots = false;
                    if (request.getRealisations() != null) {
                        for (RealisationBaseRequestDto realisationDto : request.getRealisations()) {
                            // Only consider as new if it has no ID but has lots
                            if (realisationDto.getId() == null &&
                                    realisationDto instanceof RealisationWithLotsRequestDto) {
                                RealisationWithLotsRequestDto withLotsDto = (RealisationWithLotsRequestDto) realisationDto;
                                if (withLotsDto.getLots() != null && !withLotsDto.getLots().isEmpty()) {
                                    hasNewRealizationWithLots = true;
                                    System.out.println("Found request with new lots for existing realizations - skipping deletion");
                                    break;
                                }
                            }
                        }
                    }

                    if (!hasNewRealizationWithLots) {
                        System.out.println("Marking realisation ID " + existingRealisation.getId() + " as deleted (not in request)");
                        existingRealisation.setIsDeleted(true);
                        realisationRepository.save(existingRealisation);
                        realisationsChanged = true;
                    } else {
                        System.out.println("Keeping realisation ID " + existingRealisation.getId() +
                                " even though not explicitly in request (new lots being added)");

                        // Add to updated realisations to make sure it gets included
                        if (!updatedRealisations.contains(existingRealisation)) {
                            updatedRealisations.add(existingRealisation);
                        }
                    }
                }
            }
        }

        System.out.println("Realisations changed: " + realisationsChanged);
        if (realisationsChanged) {
            System.out.println("Updated realisations: " + updatedRealisations.size());
            for (Realisation r : updatedRealisations) {
                System.out.println("  - Realisation ID: " + r.getId() + ", version: " + r.getVersion());
            }
        }

        System.out.println("============== REALISATION PROCESSING LOG END ==============");
        return realisationsChanged;
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
            if (dto.getService() != null) sb.append("S").append(dto.getService().getId()).append("|");
            if (dto.getDirection() != null) sb.append("D").append(dto.getDirection().getId()).append("|");
            if (dto.getSourceFinancement() != null) sb.append("SF").append(dto.getSourceFinancement().getId()).append("|");
            if (dto.getMonais() != null) sb.append("M").append(dto.getMonais().getId()).append("|");
            if (dto.getTypeMarcher() != null) sb.append("TM").append(dto.getTypeMarcher().getId()).append("|");
            if (dto.getModeSelection() != null) sb.append("MS").append(dto.getModeSelection().getId()).append("|");

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
            if (realisation.getService() != null) sb.append("S").append(realisation.getService().getId()).append("|");
            if (realisation.getDirection() != null) sb.append("D").append(realisation.getDirection().getId()).append("|");
            if (realisation.getSourceFinancement() != null) sb.append("SF").append(realisation.getSourceFinancement().getId()).append("|");
            if (realisation.getMonais() != null) sb.append("M").append(realisation.getMonais().getId()).append("|");
            if (realisation.getTypeMarcher() != null) sb.append("TM").append(realisation.getTypeMarcher().getId()).append("|");
            if (realisation.getModeSelection() != null) sb.append("MS").append(realisation.getModeSelection().getId()).append("|");

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
    private Set<Lot> processLotsForRealisation(Realisation existingRealisation, RealisationBaseRequestDto realisationDto, Map<String, Lot> newLotsMap) {
        System.out.println("==== PROCESSING LOTS FOR REALISATION ID " + existingRealisation.getId() +
                          ", VERSION " + existingRealisation.getVersion() + " ====");
        Set<Lot> updatedLots = new HashSet<>();

        if (existingRealisation.getLots() != null) {
            // Start with all existing active lots
            updatedLots = existingRealisation.getLots().stream()
                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                    .collect(Collectors.toSet());

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
    private boolean lotsChanged(Realisation existingRealisation, RealisationBaseRequestDto realisationDto, Set<Lot> updatedLots) {
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
        Set<LotResponse> requestLots = realisationWithLotsDto.getLots();

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

        // Check other fields
        boolean serviceChanged = fieldChanged(
            existingRealisation.getService() != null ? existingRealisation.getService().getId() : null,
            realisationDto.getService() != null ? realisationDto.getService().getId() : null
        );

        boolean directionChanged = fieldChanged(
            existingRealisation.getDirection() != null ? existingRealisation.getDirection().getId() : null,
            realisationDto.getDirection() != null ? realisationDto.getDirection().getId() : null
        );

        boolean sourceFinChanged = fieldChanged(
            existingRealisation.getSourceFinancement() != null ? existingRealisation.getSourceFinancement().getId() : null,
            realisationDto.getSourceFinancement() != null ? realisationDto.getSourceFinancement().getId() : null
        );

        boolean monaisChanged = fieldChanged(
            existingRealisation.getMonais() != null ? existingRealisation.getMonais().getId() : null,
            realisationDto.getMonais() != null ? realisationDto.getMonais().getId() : null
        );

        boolean typeMarcherChanged = fieldChanged(
            existingRealisation.getTypeMarcher() != null ? existingRealisation.getTypeMarcher().getId() : null,
            realisationDto.getTypeMarcher() != null ? realisationDto.getTypeMarcher().getId() : null
        );

        boolean modeSelectionChanged = fieldChanged(
            existingRealisation.getModeSelection() != null ? existingRealisation.getModeSelection().getId() : null,
            realisationDto.getModeSelection() != null ? realisationDto.getModeSelection().getId() : null
        );

        // Log field changes for debugging
        if (serviceChanged) System.out.println("  - Service changed");
        if (directionChanged) System.out.println("  - Direction changed");
        if (sourceFinChanged) System.out.println("  - Source financement changed");
        if (monaisChanged) System.out.println("  - Monais changed");
        if (typeMarcherChanged) System.out.println("  - Type marcher changed");
        if (modeSelectionChanged) System.out.println("  - Mode selection changed");

        // Important: Lot changes should NOT trigger a new Realisation version
        // Just checking the actual field changes, not lot relationships
        boolean result = docsChanged || textChanged || serviceChanged || directionChanged ||
                         sourceFinChanged || monaisChanged || typeMarcherChanged || modeSelectionChanged;

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

        return Objects.equals(ppm1.getRef(), ppm2.getRef()) &&
                Objects.equals(ppm1.getNom(), ppm2.getNom()) &&
                Objects.equals(ppm1.getAnnee(), ppm2.getAnnee()) &&
                Objects.equals(ppm1.getBudgetEstime(), ppm2.getBudgetEstime()) &&
                Objects.equals(ppm1.getStatus(), ppm2.getStatus()) &&
                Objects.equals(getRefParamId(ppm1.getModePassation()), getRefParamId(ppm2.getModePassation())) &&
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
                sb.append("service:").append(realisation.getService() != null && realisation.getService().getId() != null ? realisation.getService().getId() : "").append(",");
                sb.append("direction:").append(realisation.getDirection() != null && realisation.getDirection().getId() != null ? realisation.getDirection().getId() : "").append(",");
                sb.append("sourceFin:").append(realisation.getSourceFinancement() != null && realisation.getSourceFinancement().getId() != null ? realisation.getSourceFinancement().getId() : "").append(",");
                sb.append("monais:").append(realisation.getMonais() != null && realisation.getMonais().getId() != null ? realisation.getMonais().getId() : "").append(",");
                sb.append("typeMarcher:").append(realisation.getTypeMarcher() != null && realisation.getTypeMarcher().getId() != null ? realisation.getTypeMarcher().getId() : "").append(",");
                sb.append("modeSelection:").append(realisation.getModeSelection() != null && realisation.getModeSelection().getId() != null ? realisation.getModeSelection().getId() : "").append(",");

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
                    Set<Lot> lots = new HashSet<>();
                    Set<Integer> processedLotIds = new HashSet<>();

                    // Process each lot in the request
                    for (LotResponse lotDto : realisationRequest.getLots()) {
                        System.out.println("Processing lot: " + lotDto);

                        if (lotDto.getId() == null) {
                            // Handle new lot without ID
                            String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                            System.out.println("Processing new lot with description: " + lotHash);

                            // Check for duplicate lots in this transaction
                            if (newLotsMap.containsKey(lotHash)) {
                                System.out.println("Using previously created lot for description: " + lotHash);
                                lots.add(newLotsMap.get(lotHash));
                                processedLotIds.add(newLotsMap.get(lotHash).getId());
                                continue;
                            }

                            // Create a new lot
                            System.out.println("Creating new lot with description: " + lotHash);
                            Lot newLot = new Lot();
                            newLot.setDescription(lotDto.getDescription());
                            newLot.setVersion(1); // First version
                            newLot.setIsDeleted(false); // Ensure it's not deleted

                            // Generate new ID if not provided
//                            Integer maxLotId = lotRepository.findMaxId();
                            Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                            Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                            newLot.setId(lotId);

                            Lot savedLot = lotRepository.save(newLot);
                            lots.add(savedLot);
                            processedLotIds.add(savedLot.getId());
                            newLotsMap.put(lotHash, savedLot);
                            System.out.println("Created new Lot with ID: " + savedLot.getId());
                        } else {
                            // Handle existing lot
                            if (processedLotIds.contains(lotDto.getId())) {
                                System.out.println("Skipping duplicate lot ID: " + lotDto.getId());
                                continue;
                            }

                            Integer lotId = lotDto.getId();
                            Integer lotVersion = lotDto.getVersion();

                            // Try to find the latest version if not specified
                            if (lotVersion == null) {
                                lotVersion = lotRepository.findMaxVersionById(lotId);
                                if (lotVersion == null) {
                                    System.out.println("Lot ID " + lotId + " not found, creating new lot");
                                    // Create a new lot with the specified ID
                                    Lot newLot = new Lot();
                                    newLot.setId(lotId);
                                    newLot.setVersion(1);
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setIsDeleted(false);
                                    Lot savedLot = lotRepository.save(newLot);
                                    lots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    continue;
                                }
                            }

                            // Get the existing lot
                            Optional<Lot> existingLotOpt = lotRepository.findById(new CompositeId(lotId, lotVersion));
                            if (existingLotOpt.isPresent()) {
                                Lot existingLot = existingLotOpt.get();
                                String requestDesc = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                String existingDesc = existingLot.getDescription() != null ? existingLot.getDescription() : "";

                                // If descriptions are different, create a new version
                                if (!requestDesc.equals(existingDesc)) {
                                    Lot newLot = new Lot();
                                    newLot.setId(existingLot.getId());
                                    newLot.setVersion(existingLot.getVersion() + 1);
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setIsDeleted(false);

                                    Lot savedLot = lotRepository.save(newLot);
                                    lots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    System.out.println("Created new version of Lot: " + savedLot.getId() + ", Version: " + savedLot.getVersion());
                                } else {
                                    // Use existing lot
                                    lots.add(existingLot);
                                    processedLotIds.add(existingLot.getId());
                                    System.out.println("Using existing Lot: " + existingLot.getId() + ", Version: " + existingLot.getVersion());
                                }
                            } else {
                                // Lot version not found, create new version 1
                                Lot newLot = new Lot();
                                newLot.setId(lotId);
                                newLot.setVersion(1);
                                newLot.setDescription(lotDto.getDescription());
                                newLot.setIsDeleted(false);
                                Lot savedLot = lotRepository.save(newLot);
                                lots.add(savedLot);
                                processedLotIds.add(savedLot.getId());
                                System.out.println("Created new Lot with provided ID: " + savedLot.getId());
                            }
                        }
                    }

                    // Set the lots on the realisation
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

/*
    private void processAndSaveDocuments(List<MultipartFile> files, Ppm savedPpm) throws IOException {

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    UUID uuid = UUID.randomUUID();
                    try {
                        Tika tika = new Tika();
                        String detectedMimeType = tika.detect(file.getInputStream());
                        String originalFilename = file.getOriginalFilename();

                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
                        if (fileExtension.isEmpty()) {
                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
                        }

                        long fileSize = file.getSize();
                        String formattedSize = documentCommuneService.formatSize(fileSize);

                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_PPM + "/%d/%d",
                                savedPpm.getId(), savedPpm.getVersion());

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        String uniqueFileName = "ppm_" + uuid + "_" + extension;
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);

                        Docs document = new Docs();
                        document.setName(baseName);
                        document.setAlfresco_id(alfrescoId);
                        document.setPathFolder(folderPath);
                        document.setFileSize(formattedSize);
                        document.setContentType(detectedMimeType);
                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
                        document.setAlfrescoName(uniqueFileName);
                        if (savedPpm.getDocs() == null) {
                            savedPpm.setDocs(new ArrayList<>());
                        }
                        savedPpm.getDocs().add(document);

                        docsRepository.save(document);
                    } catch (Exception e) {
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
*/

    private final Object folderCreationLock = new Object();

//    void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
//        List<Docs> docs = new ArrayList<>();
//
//        // Add a file counter to ensure unique filenames even with the same UUID
//        AtomicInteger fileCounter = new AtomicInteger(0);
//
//        if (files != null && !files.isEmpty()) {
//            // Create a completely unique folder name with timestamp to avoid conflicts
//            String timestamp = String.valueOf(System.currentTimeMillis());
//            // Use parent folder path from Constants but avoid using "REALISATION" directly
//             String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION.replace("/SIGMAP-DEV/ARCHIVE/REALISATION/", "/SIGMAP-DEV/ARCHIVE/REAL_DOCS/");
////            String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION;
//            String folderPath = String.format("%s/REL_%d/%d",
//                    baseFolder, savedRealisation.getId(), savedRealisation.getVersion());
//
//            // Create the folder structure first, synchronizing to prevent conflicts
//            String folderId;
//            synchronized(folderCreationLock) {
//                try {
//                    // Get or create the folder first, outside the file processing loop
//                    folderId = alfrescoService.getOrCreateFolderByPath(folderPath);
////                    log.info("Created/Retrieved Realisation folder: Path={}, ID={}", folderPath, folderId);
//                } catch (Exception e) {
////                    log.error("Failed to create folder: Path={}, Error={}", folderPath, e.getMessage());
//                    throw new IOException("Failed to create folder: " + e.getMessage(), e);
//                }
//            }
//
//            // Now process each file using the already-created folder
//            for (MultipartFile file : files) {
//                UUID uuid = UUID.randomUUID();
//                if (file != null && !file.isEmpty()) {
//                    try {
//                        Tika tika = new Tika();
//                        String detectedMimeType = tika.detect(file.getInputStream());
//                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
//
//                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
//                        if (fileExtension.isEmpty()) {
//                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
//                        }
//
//                        long fileSize = file.getSize();
//                        String formattedSize = documentCommuneService.formatSize(fileSize);
//
//                        String[] parts = originalFilename.split("\\.");
//                        String baseName = parts[0];
//                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
//                        // Include timestamp and counter to ensure uniqueness
//                        String uniqueFileName = String.format("realisation_%s_%s_%d%s",
//                                uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
//                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
//
////                        log.info("Uploading file: OriginalName={}, UniqueName={}, FolderPath={}, FolderID={}",
////                                originalFilename, uniqueFileName, folderPath, folderId);
//
//                        // Upload directly to the already-created folder
//                        String alfrescoId = alfrescoService.uploadToExistingFolder(renamedFile, folderId);
////                        log.info("Uploaded document to Alfresco: AlfrescoID={}, FileName={}", alfrescoId, uniqueFileName);
//
//                        Docs document = new Docs();
//                        document.setName(baseName);
//                        document.setAlfresco_id(alfrescoId);
//                        document.setPathFolder(folderPath);
//                        document.setFileSize(formattedSize);
//                        document.setContentType(detectedMimeType);
//                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
//                        document.setAlfrescoName(uniqueFileName);
//
//                        // Ensure collections are initialized
//                        if (document.getRealisations() == null) {
//                            document.setRealisations(new ArrayList<>());
//                        }
//                        if (savedRealisation.getDocs() == null) {
//                            savedRealisation.setDocs(new ArrayList<>());
//                        }
//
//                        // Set up bidirectional relationship
//                        document.getRealisations().add(savedRealisation);
//                        savedRealisation.getDocs().add(document);
//
//                        // Save the document
//                        docsRepository.save(document);
//                        docs.add(document);
//                    } catch (Exception e) {
////                        log.error("Failed to process file: OriginalName={}, Error={}", file.getOriginalFilename(), e.getMessage());
//                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
//                    }
//                }
//            }
//        }
//
//        // Update the realisation's docs
//        savedRealisation.setDocs(docs);
//    }

    /*void processAndSaveRelisationsDocumentsForUpdate(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
        System.out.println("============== DOCUMENT UPLOAD LOG START ==============");
        System.out.println("Starting file upload process for realisation ID: " + savedRealisation.getId() +
                          ", Version: " + savedRealisation.getVersion());

        if (files == null) {
            System.out.println("ERROR: Files collection is NULL!");
            System.out.println("============== DOCUMENT UPLOAD LOG END ==============");
            return;
        }

        if (files.isEmpty()) {
            System.out.println("No files to process - files collection is empty");
            System.out.println("============== DOCUMENT UPLOAD LOG END ==============");
            return;
        }

        System.out.println("Processing " + files.size() + " files");
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file == null) {
                System.out.println("File " + i + " is NULL!");
            } else if (file.isEmpty()) {
                System.out.println("File " + i + " is empty: name=" + file.getOriginalFilename());
            } else {
                System.out.println("File " + i + ": name=" + file.getOriginalFilename() +
                                 ", size=" + file.getSize() +
                                 ", content-type=" + file.getContentType());
            }
        }

        // Check realisation's current docs
        if (savedRealisation.getDocs() != null) {
            System.out.println("Realisation currently has " + savedRealisation.getDocs().size() + " documents");
        } else {
            System.out.println("Realisation has no documents (docs collection is NULL)");
        }

        List<Docs> docs = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        AtomicInteger fileCounter = new AtomicInteger(0);

        if (files != null && !files.isEmpty()) {
            // Create a unique folder path with timestamp to avoid conflicts
            String timestamp = String.valueOf(System.currentTimeMillis());
            String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION.replace("/SIGMAP-DEV/ARCHIVE/REALISATION/", "/SIGMAP-DEV/ARCHIVE/REAL_DOCS/");
            String folderPath = String.format("%s/REL_%d/%d_%s",
                    baseFolder, savedRealisation.getId(), savedRealisation.getVersion(), timestamp);
            System.out.println("Using folder path: " + folderPath);

            // Create a new folder for this upload operation
            String folderId;
            synchronized (folderCreationLock) {
                try {
                    System.out.println("Creating folder in Alfresco...");
                    folderId = alfrescoService.getOrCreateFolderByPath(folderPath);
                    System.out.println("Created/Retrieved Realisation folder: Path=" + folderPath + ", ID=" + folderId);
                } catch (Exception e) {
                    System.err.println("CRITICAL ERROR - Failed to create folder: Path=" + folderPath + ", Error=" + e.getMessage());
                    e.printStackTrace();
                    throw new IOException("Failed to create folder: " + e.getMessage(), e);
                }
            }

            // Process each file
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        System.out.println("Processing file: " + file.getOriginalFilename());
                        Tika tika = new Tika();
                        String detectedMimeType = tika.detect(file.getInputStream());
                        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";

                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
                        if (fileExtension.isEmpty()) {
                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
                            System.out.println("Using extension from filename: " + fileExtension);
                        } else {
                            System.out.println("Using extension from MIME type: " + fileExtension);
                        }

                        long fileSize = file.getSize();
                        String formattedSize = documentCommuneService.formatSize(fileSize);
                        System.out.println("File size: " + fileSize + " bytes (" + formattedSize + ")");

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        String uniqueFileName = String.format("realisation_%s_%s_%d%s",
                                uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
                        System.out.println("Generated unique filename: " + uniqueFileName);

                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
                        System.out.println("Created renamed file object, size: " + renamedFile.getSize());

                        System.out.println("Uploading file to Alfresco...");
                        System.out.println("Upload params: OriginalName=" + originalFilename +
                                        ", UniqueName=" + uniqueFileName +
                                        ", FolderPath=" + folderPath +
                                        ", FolderID=" + folderId);

                        // Try upload with detailed error handling
                        String alfrescoId;
                        try {
                            alfrescoId = alfrescoService.uploadToExistingFolder(renamedFile, folderId);
                            System.out.println("SUCCESS! Uploaded document to Alfresco: AlfrescoID=" + alfrescoId +
                                          ", FileName=" + uniqueFileName);
                        } catch (Exception e) {
                            System.err.println("CRITICAL ERROR during Alfresco upload: " + e.getMessage());
                            e.printStackTrace();
                            throw new IOException("Failed to upload to Alfresco: " + e.getMessage(), e);
                        }

                        System.out.println("Creating document record in database...");
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
                            System.out.println("Initialized realisations collection for document");
                        }
                        if (savedRealisation.getDocs() == null) {
                            savedRealisation.setDocs(new ArrayList<>());
                            System.out.println("Initialized docs collection for realisation");
                        }

                        // Save the document first
                        try {
                            Docs savedDoc = docsRepository.save(document);
                            System.out.println("Saved document to database with ID: " + savedDoc.getId());

                            // Now add it to the realisation - important to do this after saving the document
                            // to ensure it has an ID
                            if (!containsDocumentById(savedRealisation.getDocs(), savedDoc.getId())) {
                                // Set up bidirectional relationship
                                savedDoc.getRealisations().add(savedRealisation);
                                savedRealisation.getDocs().add(savedDoc);
                                System.out.println("Set up bidirectional relationship between document and realisation");
                            } else {
                                System.out.println("Document already associated with realisation, skipping duplicate association");
                            }

                            docs.add(savedDoc);
                        } catch (Exception e) {
                            System.err.println("CRITICAL ERROR saving document to database: " + e.getMessage());
                            e.printStackTrace();
                            throw new IOException("Failed to save document to database: " + e.getMessage(), e);
                        }
                    } catch (Exception e) {
                        System.err.println("ERROR processing file: OriginalName=" +
                                         (file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown") +
                                         ", Error=" + e.getMessage());
                        e.printStackTrace();
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                } else {
                    System.out.println("Skipping null or empty file");
                }
            }
        }

        // Update the realisation's docs
        if (docs.size() > 0) {
            System.out.println("Successfully processed " + docs.size() + " files");
            System.out.println("Final document count: " +
                             (savedRealisation.getDocs() != null ? savedRealisation.getDocs().size() : 0));

            // Save the realisation only once to prevent duplicate association errors
            try {
                // Set up many-to-many Hibernate association manually through join table
                // to avoid duplicate entries in the relation table
                Realisation savedRealisationWithDocs = realisationRepository.save(savedRealisation);
                System.out.println("Saved realisation with updated documents. Final doc count: " +
                                  (savedRealisationWithDocs.getDocs() != null ? savedRealisationWithDocs.getDocs().size() : 0));

                // Check for duplicates in join table
                if (savedRealisationWithDocs.getDocs() != null) {
                    Set<Integer> docIds = new HashSet<>();
                    for (Docs doc : savedRealisationWithDocs.getDocs()) {
                        if (!docIds.add(doc.getId())) {
                            System.out.println("WARNING: Detected duplicate document ID " + doc.getId() +
                                             " in realisation's document collection");
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("CRITICAL ERROR saving realisation with documents: " + e.getMessage());
                e.printStackTrace();
                throw new IOException("Failed to save realisation with documents: " + e.getMessage(), e);
            }
        } else {
            System.out.println("No documents were processed successfully");
        }

        System.out.println("============== DOCUMENT UPLOAD LOG END ==============");
    }*/

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
        // Find the existing PPM
        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        Optional<Ppm> existingPpmOpt = repository.findById(compositeId);
        if (existingPpmOpt.isEmpty()) {
            throw new RuntimeException("Ppm with ID " + ppmId + " and version " + ppmVer + " not found.");
        }

        Ppm existingPpm = existingPpmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            throw new RuntimeException("Cannot update a deleted Ppm with ID " + ppmId + " and version " + ppmVer);
        }
        // Check if the PPM is in draft status
        if (existingPpm.getStatus() == null || existingPpm.getStatus() != ProcessStatus.BROUILLON) {
            throw new InvalidPpmStatusException("PPM can only be updated in draft status. Current status ID: " +
                    (existingPpm.getStatus() != null ? existingPpm.getStatus() : "null"));
        }
        // Update PPM fields
      existingPpm.setRef(existingPpm.getRef());
        existingPpm.setNom(request.getNom() != null ? request.getNom() : existingPpm.getNom());
        existingPpm.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPpm.getAnnee());
        existingPpm.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPpm.getBudgetEstime());
        existingPpm.setProcessInstanceId(existingPpm.getProcessInstanceId());

        existingPpm.setStatus(existingPpm.getStatus());

        // Set Ac if provided
        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
            existingPpm.setAc(ac);
        }

        // Handle documents if provided
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            // Get set of document IDs to add
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());

            // Get existing document IDs
            Set<Integer> existingDocIds = existingPpm.getDocs() != null ?
                    existingPpm.getDocs().stream().map(Docs::getId).collect(Collectors.toSet()) :
                    new HashSet<>();

            // Documents to add (those in request but not in existing)
            Set<Integer> docsToAdd = new HashSet<>(requestDocIds);
            docsToAdd.removeAll(existingDocIds);

            // Documents to remove (those in existing but not in request)
            Set<Integer> docsToRemove = new HashSet<>(existingDocIds);
            docsToRemove.removeAll(requestDocIds);

            // Get current docs list or initialize
            List<Docs> docs = existingPpm.getDocs() != null ?
                    new ArrayList<>(existingPpm.getDocs()) :
                    new ArrayList<>();

            // Remove documents that are no longer in the request
            if (!docsToRemove.isEmpty()) {
                docs.removeIf(doc -> docsToRemove.contains(doc.getId()));

                // Declare final variable outside the lambda
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

            // Add new documents
            for (Integer docId : docsToAdd) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("Document with ID " + docId + " not found"));

                // Set up bidirectional relationship
                if (doc.getPpms() == null) {
                    doc.setPpms(new ArrayList<>());
                }
                doc.getPpms().add(existingPpm);
                docs.add(doc);

                // Save the document with updated relationship
                docsRepository.save(doc);
            }

            // Update PPM with documents
            existingPpm.setDocs(docs);
            System.out.println("Updated documents: " + docs.size() + " total documents");
        } else if (existingPpm.getDocs() != null && !existingPpm.getDocs().isEmpty()) {
            // Clear document associations if request has empty or null docIds
            for (Docs doc : existingPpm.getDocs()) {
                if (doc.getPpms() != null) {
                    doc.getPpms().remove(existingPpm);
                    docsRepository.save(doc);
                }
            }
            existingPpm.setDocs(new ArrayList<>());
        }

        // Process document files if provided
        if(request.getPpmFiles() != null && !request.getPpmFiles().isEmpty()) {
            try {
                System.out.println("Processing " + request.getPpmFiles().size() + " files for PPM");
               // processAndSaveDocuments(request.getPpmFiles(), existingPpm);
                documentCommuneService.processAndSaveDocuments(request.getPpmFiles(),existingPpm,Constants.PATH_ENTREPOT_GED_PPM);

                System.out.println("Processed PPM files successfully");
            } catch (IOException e) {
                System.err.println("Error processing PPM files: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        // Process realisations if provided
        List<Realisation> updatedRealisations = new ArrayList<>();
        Set<Integer> processedRealisationIds = new HashSet<>();

        // Keep track of newly created lots to detect duplicates
        Map<String, Lot> newLotsMap = new HashMap<>();

        // Choose which collection of realisations to process, preferring realisationsWithLots
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

                // Check if updating existing realisation or creating new one
                if (realisationRequest.getId() != null) {
                    // Process existing realisation
                    processedRealisationIds.add(realisationRequest.getId());

                    // Find the existing realisation
                    Integer realisationVersion = realisationRequest.getVersion() != null ?
                            realisationRequest.getVersion() : 1; // Default to version 1

                    CompositeId realisationId = new CompositeId(realisationRequest.getId(), realisationVersion);
                    Optional<Realisation> existingRealisationOpt = realisationRepository.findById(realisationId);

                    if (existingRealisationOpt.isEmpty()) {
                        throw new RuntimeException("Realisation with ID " + realisationRequest.getId() +
                                " and version " + realisationVersion + " not found");
                    }

                    realisation = existingRealisationOpt.get();

                    // Update realisation fields
                    realisation.setRealisationEnvisagees(realisationRequest.getRealisationEnvisagees() != null ?
                            realisationRequest.getRealisationEnvisagees() : realisation.getRealisationEnvisagees());
                    realisation.setService(realisationRequest.getService() != null ?
                            realisationRequest.getService() : realisation.getService());
                    realisation.setDirection(realisationRequest.getDirection() != null ?
                            realisationRequest.getDirection() : realisation.getDirection());
                    realisation.setSourceFinancement(realisationRequest.getSourceFinancement() != null ?
                            realisationRequest.getSourceFinancement() : realisation.getSourceFinancement());
                    realisation.setMonais(realisationRequest.getMonais() != null ?
                            realisationRequest.getMonais() : realisation.getMonais());
                    realisation.setTypeMarcher(realisationRequest.getTypeMarcher() != null ?
                            realisationRequest.getTypeMarcher() : realisation.getTypeMarcher());
                    realisation.setModeSelection(realisationRequest.getModeSelection() != null ?
                            realisationRequest.getModeSelection() : realisation.getModeSelection());
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

                    // Ensure the PPM relationship is maintained
                    if (realisation.getPpms() == null) {
                        realisation.setPpms(new ArrayList<>());
                    }
                    if (!realisation.getPpms().contains(existingPpm)) {
                        realisation.getPpms().add(existingPpm);
                    }

                    // Handle realisation documents
                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        // Get set of document IDs to add
                        Set<Integer> requestDocIds = new HashSet<>(realisationRequest.getDocIds());

                        // Get existing document IDs
                        Set<Integer> existingDocIds = realisation.getDocs() != null ?
                                realisation.getDocs().stream().map(Docs::getId).collect(Collectors.toSet()) :
                                new HashSet<>();

                        // Documents to add (those in request but not in existing)
                        Set<Integer> docsToAdd = new HashSet<>(requestDocIds);
                        docsToAdd.removeAll(existingDocIds);

                        // Documents to remove (those in existing but not in request)
                        Set<Integer> docsToRemove = new HashSet<>(existingDocIds);
                        docsToRemove.removeAll(requestDocIds);

                        // Get current docs list or initialize
                        List<Docs> docs = realisation.getDocs() != null ?
                                new ArrayList<>(realisation.getDocs()) :
                                new ArrayList<>();

                        // Remove documents that are no longer in the request
                        if (!docsToRemove.isEmpty()) {
                            docs.removeIf(doc -> docsToRemove.contains(doc.getId()));

                            // Declare final variable outside the lambda
                            final Realisation realisationRef = realisation;

                            // For each removed doc, update the relationship
                            for (Integer docId : docsToRemove) {
                                docsRepository.findById(docId).ifPresent(doc -> {
                                    if (doc.getRealisations() != null) {
                                        doc.getRealisations().remove(realisationRef);
                                        docsRepository.save(doc);
                                    }
                                });
                            }
                        }

                        // Add new documents
                        for (Integer docId : docsToAdd) {
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

                        // Set docs list for updated realisation
                        realisation.setDocs(docs);
                        System.out.println("Updated docs for realisation ID " + realisation.getId() + ": " + docs.size() + " total docs");
                    } else if (realisation.getDocs() != null && !realisation.getDocs().isEmpty()) {
                        // Clear documents if docIds is null or empty
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
                    // Create a new Realisation
                    realisation = new Realisation();

                    // Generate a new ID for the realisation
//                    Integer maxRealisationId = realisationRepository.findMaxId();
                    Integer maxRealisationId =  sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ);
                    Integer realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;

                    // Always use version 1 for newly created realisations
                    realisation.setId(realisationId);
                    realisation.setVersion(1);

                    // Set all fields from the request
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

                    // Set up the relationship with PPM
                    List<Ppm> ppmList = new ArrayList<>();
                    ppmList.add(existingPpm);
                    realisation.setPpms(ppmList);

                    // Process realisation documents if provided
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
                        System.out.println("Added " + docs.size() + " documents to new realisation");
                    }
                }

                // Save the realisation before processing files to ensure it has an ID
                realisation = realisationRepository.save(realisation);
                System.out.println("Saved realisation ID: " + realisation.getId() + " version: " + realisation.getVersion());

                // Process realisation files AFTER saving the realisation
                if (realisationRequest.getRealisationFiles() != null && !realisationRequest.getRealisationFiles().isEmpty()) {
                    try {
                        System.out.println("Processing " + realisationRequest.getRealisationFiles().size() +
                                " files for realisation ID: " + realisation.getId());

                        // Process all files at once using the new method
                       // processAndSaveRelisationsDocumentsForUpdate(realisationRequest.getRealisationFiles(), realisation);
                        documentCommuneService.processAndSaveDocuments(realisationRequest.getRealisationFiles(),realisation,Constants.PATH_ENTREPOT_GED_PPM+"/"+existingPpm.getId()+Constants.PATH_ENTREPOT_GED_REALISATION);


                        // After processing files, fetch the updated realisation with its documents
                        realisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion()))
                                .orElseThrow(() -> new RuntimeException("Could not find realisation after file upload"));

                        System.out.println("After file processing, realisation has " +
                                (realisation.getDocs() != null ? realisation.getDocs().size() : 0) + " documents");

                        // Save the realisation again
                        realisation = realisationRepository.save(realisation);
                    } catch (IOException e) {
                        System.err.println("Error processing realisation files: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                // Handle Lots if provided
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    System.out.println("Processing " + realisationRequest.getLots().size() +
                            " lots for realisation ID: " + realisation.getId());

                    Set<Lot> updatedLots = new HashSet<>();
                    Set<Integer> processedLotIds = new HashSet<>();

                    // Get existing lot IDs
                    Set<Integer> existingLotIds = realisation.getLots() != null ?
                            realisation.getLots().stream().map(Lot::getId).collect(Collectors.toSet()) :
                            new HashSet<>();

                    // Process each lot in the request
                    for (LotResponse lotDto : realisationRequest.getLots()) {
                        if (lotDto.getId() == null) {
                            // Handle new lot without ID
                            String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";

                            // Check for duplicate lots in this transaction
                            if (newLotsMap.containsKey(lotHash)) {
                                updatedLots.add(newLotsMap.get(lotHash));
                                processedLotIds.add(newLotsMap.get(lotHash).getId());
                                continue;
                            }

                            // Create a new lot
                            Lot newLot = new Lot();
                            newLot.setDescription(lotDto.getDescription());
                            newLot.setVersion(1); // First version
                            newLot.setIsDeleted(false); // Ensure it's not deleted

                            // Generate new ID if not provided
//                            Integer maxLotId = lotRepository.findMaxId();
                            Integer maxLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ);
                            Integer lotId = (maxLotId != null ? maxLotId : 0) + 1;
                            newLot.setId(lotId);

                            Lot savedLot = lotRepository.save(newLot);
                            updatedLots.add(savedLot);
                            processedLotIds.add(savedLot.getId());
                            newLotsMap.put(lotHash, savedLot);
                            System.out.println("Created new Lot with ID: " + savedLot.getId());
                        } else {
                            // Handle existing lot
                            if (processedLotIds.contains(lotDto.getId())) {
                                continue; // Skip duplicate lot IDs
                            }

                            Integer lotId = lotDto.getId();
                            Integer lotVersion = lotDto.getVersion();

                            // Try to find the latest version if not specified
                            if (lotVersion == null) {
                                lotVersion = lotRepository.findMaxVersionById(lotId);
                                if (lotVersion == null) {
                                    System.out.println("Lot ID " + lotId + " not found, creating new lot");
                                    // Create a new lot with the specified ID
                                    Lot newLot = new Lot();
                                    newLot.setId(lotId);
                                    newLot.setVersion(1);
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setIsDeleted(false);
                                    Lot savedLot = lotRepository.save(newLot);
                                    updatedLots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    continue;
                                }
                            }

                            // Get the existing lot
                            Optional<Lot> existingLotOpt = lotRepository.findById(new CompositeId(lotId, lotVersion));
                            if (existingLotOpt.isPresent()) {
                                Lot existingLot = existingLotOpt.get();
                                String requestDesc = lotDto.getDescription() != null ? lotDto.getDescription() : "";
                                String existingDesc = existingLot.getDescription() != null ? existingLot.getDescription() : "";

                                // If descriptions are different, create a new version
                                if (!requestDesc.equals(existingDesc)) {
                                    Lot newLot = new Lot();
                                    newLot.setId(existingLot.getId());
                                    newLot.setVersion(existingLot.getVersion());
                                    newLot.setDescription(lotDto.getDescription());
                                    newLot.setIsDeleted(false);

                                    Lot savedLot = lotRepository.save(newLot);
                                    updatedLots.add(savedLot);
                                    processedLotIds.add(savedLot.getId());
                                    System.out.println("Created new version of Lot: " + savedLot.getId() + ", Version: " + savedLot.getVersion());
                                } else {
                                    // Use existing lot
                                    updatedLots.add(existingLot);
                                    processedLotIds.add(existingLot.getId());
                                }
                            } else {
                                // Lot version not found, create new version 1
                                Lot newLot = new Lot();
                                newLot.setId(lotId);
                                newLot.setVersion(1);
                                newLot.setDescription(lotDto.getDescription());
                                newLot.setIsDeleted(false);
                                Lot savedLot = lotRepository.save(newLot);
                                updatedLots.add(savedLot);
                                processedLotIds.add(savedLot.getId());
                                System.out.println("Created new Lot with provided ID: " + savedLot.getId());
                            }
                        }
                    }

                    // Set the lots on the realisation
                    realisation.setLots(updatedLots);
                    System.out.println("Set " + updatedLots.size() + " lots on realisation ID: " + realisation.getId());

                    // Save the realisation with lots
                    realisation = realisationRepository.save(realisation);

                    // Verify the lots were properly saved
                    Realisation verifyRealisation = realisationRepository.findById(new CompositeId(realisation.getId(), realisation.getVersion()))
                            .orElseThrow(() -> new RuntimeException("Could not find realisation after saving lots"));

                    System.out.println("After saving, realisation ID: " + verifyRealisation.getId() +
                            " has " + (verifyRealisation.getLots() != null ? verifyRealisation.getLots().size() : 0) +
                            " lots");
                } else if (realisation.getLots() != null && !realisation.getLots().isEmpty()) {
                    // Clear lots if none provided in request
                    realisation.setLots(new HashSet<>());
                    realisation = realisationRepository.save(realisation);
                    System.out.println("Cleared lots for realisation ID: " + realisation.getId());
                }

                // Add the realisation to our updated list
                updatedRealisations.add(realisation);
            }
        }

        // Handle removal of realisations not in the request
        if (existingPpm.getRealisations() != null) {
            for (Realisation existingRealisation : existingPpm.getRealisations()) {
                if (!processedRealisationIds.contains(existingRealisation.getId())) {
                    // Remove PPM from realisation's PPMs list
                    if (existingRealisation.getPpms() != null) {
                        existingRealisation.getPpms().remove(existingPpm);
                        realisationRepository.save(existingRealisation);
                    }
                }
            }
        }

        // Update the PPM with the list of realisations
        existingPpm.setRealisations(updatedRealisations);
        System.out.println("Updated PPM with " + updatedRealisations.size() + " realisations");

        // Save PPM again to persist all associations
        existingPpm = repository.save(existingPpm);
        System.out.println("Final save of PPM - ID: " + existingPpm.getId() + ", Version: " + existingPpm.getVersion());

        // Verify the final PPM has all associated entities
        System.out.println("Final PPM has: " +
                (existingPpm.getDocs() != null ? existingPpm.getDocs().size() : 0) + " documents, " +
                (existingPpm.getRealisations() != null ? existingPpm.getRealisations().size() : 0) + " realisations");

        // Print realisation details to verify
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
                                   List<Realisation> updatedRealisations, List<Docs> updatedDocs, Ppm existingPpm) {
        System.out.println("Creating new PPM version: " + (ppmVer + 1));

        // Create new PPM
        Ppm newPpm = new Ppm();
        BeanUtils.copyProperties(tempPpm, newPpm, "id", "version");
        newPpm.setId(ppmId);
        newPpm.setVersion(ppmVer + 1);

        // If the PPM has a created date, keep it
        if (existingPpm.getCreatedAt() != null) {
            newPpm.setCreatedAt(existingPpm.getCreatedAt());
            newPpm.setCreatedBy(existingPpm.getCreatedBy());
        }

        // Log document statuses before saving
        if (updatedDocs != null) {
            System.out.println("Documents before saving new PPM version:");
            for (Docs doc : updatedDocs) {
                System.out.println("  - Doc ID: " + doc.getId() + ", isDeleted: " + doc.getIsDeleted());

                // Ensure isDeleted is properly set
                if (doc.getIsDeleted() == null) {
                    doc.setIsDeleted(false);
                    docsRepository.save(doc);
                }
            }
        }

        // Save and get reference to new PPM
        newPpm = repository.save(newPpm);
        System.out.println("Saved new PPM: " + newPpm.getId() + " version " + newPpm.getVersion());

        // Link realisations
        if (updatedRealisations != null && !updatedRealisations.isEmpty()) {
            newPpm.setRealisations(updatedRealisations);
            for (Realisation realisation : updatedRealisations) {
                // Ensure bidirectional relationship is maintained
                realisation.setPpms(realisation.getPpms() != null ? realisation.getPpms() : new ArrayList<>());
                if (!realisation.getPpms().contains(newPpm)) {
                    realisation.getPpms().add(newPpm);
                }
            }
        }

        // Set updated document list ensuring proper filtering
        if (updatedDocs != null) {
            newPpm.setDocs(new ArrayList<>(updatedDocs));
            // Make sure to maintain bidirectional relationships
            for (Docs doc : updatedDocs) {
                if (doc.getPpms() == null) {
                    doc.setPpms(new ArrayList<>());
                }
                if (!doc.getPpms().contains(newPpm)) {
                    doc.getPpms().add(newPpm);
                    docsRepository.save(doc);
                }
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
        request.setService(dto.getService());
        request.setDirection(dto.getDirection());
        request.setSourceFinancement(dto.getSourceFinancement());
        request.setMonais(dto.getMonais());
        request.setTypeMarcher(dto.getTypeMarcher());
        request.setModeSelection(dto.getModeSelection());
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
        ppm.setBudgetEstime(request.getBudgetEstime());
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
                    Set<Lot> lots = new HashSet<>();
                    for (LotResponse lotRequest : realisationRequest.getLots()) {
                        Lot lot = new Lot();
                        Integer lotId = lotRequest.getId();
                        if (lotId == null) {
                            lotId = nextLotId++;
                        }
                        lot.setId(lotId);
                        lot.setVersion(1);
                        lot.setDescription(lotRequest.getDescription());
                        lot = lotRepository.save(lot);
                        lots.add(lot);
                    }
                    realisation.setLots(lots);
                }

                realisationsToSave.add(realisation);
            }
            realisationRepository.saveAll(realisationsToSave);
        }

        // Step 11: Calculate and set estimated budget based on realisations
        BigDecimal budgetEstime = savedPpm.getRealisations().stream()
                .map(Realisation::getBudgetEstime)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        savedPpm.setBudgetEstime(budgetEstime);

        // Step 12: Save and return the final PPM
        return repository.save(savedPpm);
    }

    @Override
    public List<Ppm> findCurrentYearPpmsByUser() throws AcNotFound, PpmNotFound {
        log.info("Retrieving current year's PPMs for connected user");

        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds.isEmpty()) {
            log.warn("No AC IDs found for the connected user");
            throw new AcNotFound("No AC IDs found for the connected user", Collections.emptyList());
        }

        for (Integer acId : userAcIds) {
            try {
                acService.findById(acId);
            } catch (AcNotFound e) {
                log.warn("AC not found with ID: {}", acId);
                throw e;
            }
        }

        String currentYear = String.valueOf(LocalDate.now().getYear());

        List<Ppm> ppmsList = new ArrayList<>();
        for (Integer acId : userAcIds) {
            Specification<Ppm> spec = PpmSpecifications.annee(currentYear)
                    .and(PpmSpecifications.ppmAc(acId))
                    .and(PpmSpecifications.deleted(false))
                    .and(PpmSpecifications.isLatestVersion());
            Optional<Ppm> ppmOpt = repository.findOne(spec);
            ppmOpt.ifPresent(ppmsList::add);
        }

        if (ppmsList.isEmpty()) {
            log.warn("No PPMs found for the current year {} and user ACs: {}", currentYear, userAcIds);
            throw new PpmNotFound("No PPMs found for the current year and user ACs: " + userAcIds,
                    Collections.emptyList(), Collections.emptyList());
        }

        return ppmsList;
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


}