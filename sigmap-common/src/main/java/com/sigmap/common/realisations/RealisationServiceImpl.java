package com.sigmap.common.realisations;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionRepository;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.financement.Financement;
import com.sigmap.common.financement.FinancementId;
import com.sigmap.common.financement.FinancementRepository;
import com.sigmap.common.financement.request.FinancementBaseRequestDto;
import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.MonnaieRepository;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaRepository;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
public class RealisationServiceImpl implements RealisationService {
    private final RealisationRepository repository;
    private final PaaRepository paaRepository;
    private final PpmRepository ppmRepository;
    private final LotRepository lotRepository;
    private final RefParamRepository refParamRepository;
    private final MonnaieRepository monnaieRepository;
    private final ModeDePassationSelectionRepository modeDePassationSelectionRepository;

    private final AlfrescoService alfrescoService;
    private final DocumentCommuneService documentCommuneService;

    private final DocsRepository docsRepository;
    private final FinancementRepository financementRepository;
    private final SequenceGeneratorService sequenceGeneratorService;



    public RealisationServiceImpl(RealisationRepository repository, ModeDePassationSelectionRepository modeDePassationSelectionRepository, MonnaieRepository monnaieRepository, PaaRepository paaRepository, PpmRepository ppmRepository, LotRepository lotRepository, RefParamRepository refParamRepository, AlfrescoService alfrescoService, DocumentCommuneService documentCommuneService, DocsService docsService, AcService acService, DocsRepository docsRepository, FinancementRepository financementRepository, SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.paaRepository = paaRepository;
        this.ppmRepository = ppmRepository;
        this.lotRepository = lotRepository;
        this.refParamRepository = refParamRepository;
        this.alfrescoService = alfrescoService;
        this.documentCommuneService = documentCommuneService;
        this.docsRepository = docsRepository;
        this.financementRepository = financementRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.monnaieRepository = monnaieRepository;
        this.modeDePassationSelectionRepository = modeDePassationSelectionRepository;
    }

    @Override
    public List<Realisation> findRealisationsNotAttachedToDac(Integer ppmId, Integer ppmVersion) throws PpmNotFound {
        log.info("Recherche des réalisations non attachées à un DAC pour le PPM ID: {}, Version: {}", ppmId, ppmVersion);

        // Validate input
        if (ppmId == null|| ppmVersion == null) {
            log.error("ID ou version du PPM non fourni");
            throw new IllegalArgumentException("PPM ID and version must not be null");
        }

        // Verify PPM exists
        if (!ppmRepository.existsById(new CompositeId(ppmId, ppmVersion))) {
            log.error("PPM non trouvé avec ID: {}, Version: {}", ppmId, ppmVersion);
            throw new PpmNotFound("PPM non trouvé avec ID: " , List.of(ppmId), List.of(ppmVersion));
        }

        // Create specification to filter Realisations not attached to a Dac
        Specification<Realisation> realisationNotInDacSpec = (root, query, cb) -> {
            query.distinct(true);
            // Join with ppm_realisation to ensure the Realisation is linked to the given PPM
            Join<Realisation, Ppm> ppmJoin = root.join("ppms", jakarta.persistence.criteria.JoinType.INNER);
            // Subquery to check if Realisation is linked to any Dac
            Subquery<Long> dacSubquery = query.subquery(Long.class);
            Root<Dac> dacRoot = dacSubquery.from(Dac.class);
            dacSubquery.select(cb.count(dacRoot))
                    .where(
                            cb.equal(dacRoot.get("realisation").get("id"), root.get("id")),
                            cb.equal(dacRoot.get("realisation").get("version"), root.get("version"))
                    );
            // Filter Realisations linked to the specific PPM and not linked to any Dac
            return cb.and(
                    cb.equal(ppmJoin.get("id"), ppmId),
                    cb.equal(ppmJoin.get("version"), ppmVersion),
                    cb.equal(dacSubquery, 0)
            );
        };

        // Fetch Realisations using the specification
        List<Realisation> realisations = repository.findAll(realisationNotInDacSpec);

        if (realisations.isEmpty()) {
            log.info("Aucune réalisation non attachée à un DAC trouvée pour le PPM ID: {}, Version: {}", ppmId, ppmVersion);
        } else {
            log.info("Récupération de {} réalisations non attachées à un DAC pour le PPM ID: {}, Version: {}",
                    realisations.size(), ppmId, ppmVersion);
        }

        return realisations;
    }

    @Override
    public List<Realisation> findAll() {
        Specification<Realisation> spec = RealisationSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Realisation> findAll(Specification<Realisation> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Realisation> findAllDeleted(Specification<Realisation> spec, Pageable pageable) {
        Specification<Realisation> deletedSpec = RealisationSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Realisation> findAllDeleted(Pageable pageable) {
        Specification<Realisation> spec = RealisationSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Realisation> findDeletedById(Integer realisationId, Integer realisationVer) {
        Specification<Realisation> spec = RealisationSpecifications.id(new CompositeId(realisationId, realisationVer))
                .and(RealisationSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Realisation> findOne(Specification<Realisation> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Realisation> findById(Integer realisationId, Integer realisationVer) {
        return repository.findById(new CompositeId(realisationId, realisationVer));
    }

    @Override
    public Realisation save(Realisation realisation) {
        return repository.save(realisation);
    }

    @Transactional
    @Override
    public Realisation create(RealisationRequest request) throws RealisationAlreadyExistsException {
        log.info("Starting create with request: {}", request);
        Integer id = request.getId();
        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        // Generate ID if not provided
        if (id == null) {
            id = sequenceGeneratorService.getNextId(Constants.REALISATION_ID_SEQ) + 1;
            log.info("Generated Realisation ID: {}", id);
        }

        CompositeId compositeId = new CompositeId(id, version);
        log.info("CompositeId: {}", compositeId);

        // Check if realisation already exists
        if (repository.existsById(compositeId)) {
            throw new RealisationAlreadyExistsException(
                    "Realisation with ID " + id + " and version " + version + " already exists in the database.",
                    List.of(id), List.of(version));
        }

        // Map request to entity
        Realisation realisation = RealisationMapper.INSTANCE.toEntity(request);
        realisation.setId(compositeId.getId());
        realisation.setVersion(version);
        log.info("Realisation after mapping: {}", realisation);

        // Set empty associations initially
        realisation.setPaas(new ArrayList<>());
        realisation.setPpms(new ArrayList<>());
        realisation.setLots(new ArrayList<>());
        realisation.setDocs(new ArrayList<>());
        realisation.setFinancements(new ArrayList<>());

        if (request.getImputationBudgetaire() != null) {
            log.info("Fetching ImputationBudgetaire with ID: {}", request.getImputationBudgetaire().getId());
            realisation.setImputationBudgetaire(refParamRepository.findById(request.getImputationBudgetaire().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Imputation Budgetaire not found")));
        }
        if (request.getBeneficiaire() != null) {
            realisation.setBeneficiaire(request.getBeneficiaire());
        }

        // Fetch associated PAAs if needed
        if (request.getPaaIds() != null && !request.getPaaIds().isEmpty()) {
            List<Paa> paas = request.getPaaIds().stream()
                    .peek(paaId -> log.info("Associating PAA ID: {}, Version: {}", paaId.getId(), paaId.getVersion()))
                    .map(paaId -> paaRepository.findById(new CompositeId(paaId.getId(), paaId.getVersion())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toCollection(ArrayList::new));
            realisation.getPaas().addAll(paas);
            log.info("Associated PAAs: {}", paas);
        }

        // Fetch and set PPMs
        if (request.getPpmIds() != null) {
            List<Ppm> ppms = request.getPpmIds().stream()
                    .map(ppmId -> ppmRepository.findById(new CompositeId(ppmId.getId(), ppmId.getVersion())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toCollection(ArrayList::new));
            realisation.getPpms().addAll(ppms);
            log.info("Associated PPMs: {}", ppms);
        }

        // Handle Lots
        if (request.getLots() != null && !request.getLots().isEmpty()) {
            Set<Lot> lots = new HashSet<>();
            for (LotResponse lotResponse : request.getLots()) {
                if (lotResponse.getId() != null) {
                    lotRepository.findById(new CompositeId(lotResponse.getId(), lotResponse.getVersion()))
                            .ifPresent(lots::add);
                } else {
                    Lot newLot = new Lot();
                    newLot.setDescription(lotResponse.getDescription());
                    newLot.setVersion(1);
                    Integer lotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ) + 1;
                    newLot.setId(lotId);

                    if (lotResponse.getBudget() != null) {
                        BigDecimal bd = (lotResponse.getBudget() instanceof BigDecimal)
                                ? (BigDecimal) lotResponse.getBudget()
                                : new BigDecimal(lotResponse.getBudget().toString());
                        newLot.setBudget(bd);
                    }
                    Lot savedLot = lotRepository.save(newLot);
                    lots.add(savedLot);
                }
            }
            realisation.getLots().addAll(lots);
            log.info("Associated Lots: {}", lots);
        }

        log.info("Realisation before save: {}", realisation);
        realisation.setStatut(RealisationStatus.NON_PUBLIE);
        Realisation savedRealisation = repository.save(realisation);
        log.info("Saved Realisation: {}", savedRealisation);

        // Create financements for the saved realisation
        if (request.getFinancementRequests() != null && !request.getFinancementRequests().isEmpty()) {
            log.info("Creating {} Financements for saved Realisation", request.getFinancementRequests().size());

            List<Financement> createdFinancements = new ArrayList<>();

            for (FinancementBaseRequestDto financementRequest : request.getFinancementRequests()) {
                log.info("Processing Financement request: {}", financementRequest);

                // Create FinancementId (composite key)
                FinancementId financementId = new FinancementId();

                // Generate unique financement ID for each financement
                Integer nextFinancementId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ) + 1;
                financementId.setFinancementId(nextFinancementId);

                financementId.setRealisation(savedRealisation);
                financementId.setFinancementId(nextFinancementId);

                log.info("Generated Financement ID: {}", nextFinancementId);

                // Create Financement entity
                Financement financement = new Financement();
                financement.setId(financementId);
                financement.setBudgetEstimer(financementRequest.getBudgetEstimer());

                // Set Monnaie
                if (financementRequest.getMonnaieId() != null) {
                    log.info("Fetching Monnaie with ID: {}", financementRequest.getMonnaieId());
                    Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
                            .orElseThrow(() -> new EntityNotFoundException("Monnaie not found with ID: " + financementRequest.getMonnaieId()));

                    if (monnaie.getTaux() != null) {
                        financement.setTauxApplicable(monnaie.getTaux());
                        System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux() + " from Monnaie ID: " + monnaie.getId());
                    } else {
                        financement.setTauxApplicable(0.0);
                        System.out.println("Monnaie taux is null, set tauxApplicable to 0.0 for Monnaie ID: " + monnaie.getId());
                    }
                    financement.setMonnaie(monnaie);
                } else {
                    throw new IllegalArgumentException("Monnaie ID is required for each Financement");
                }

                // Set source financement if provided
                if (financementRequest.getSourceFinancement() != null && financementRequest.getSourceFinancement().getId() != null) {
                    log.info("Fetching SourceFinancement with ID: {}", financementRequest.getSourceFinancement().getId());
                    RefParam sourceFinancement = refParamRepository.findById(financementRequest.getSourceFinancement().getId())
                            .orElseThrow(() -> new EntityNotFoundException("Source Financement not found with ID: " + financementRequest.getSourceFinancement().getId()));
                    financement.setSourceFinancement(sourceFinancement);
                }

                Financement savedFinancement = financementRepository.save(financement);
                log.info("Created and saved Financement: {}", savedFinancement);

                createdFinancements.add(savedFinancement);
            }

            savedRealisation.setFinancements(createdFinancements);

            BigDecimal totalFx = createdFinancements.stream()
                    .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                    .map(f -> {
                        BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                        double taux = (f.getTauxApplicable() != null) ? f.getTauxApplicable()
                                : ((f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0);
                        return amt.multiply(BigDecimal.valueOf(taux));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            savedRealisation.setBudgetEstime(totalFx);

            savedRealisation = repository.save(savedRealisation);
            log.info("Updated Realisation with {} financements", createdFinancements.size());
        }

        return savedRealisation;
    }

    @Override
    public Realisation update(Integer realisationId, Integer realisationVer, RealisationRequest request) throws RealisationNotFoundException {
        CompositeId compositeId = new CompositeId(realisationId, realisationVer);
        Optional<Realisation> existingRealisationOpt = repository.findById(compositeId);

        if (existingRealisationOpt.isEmpty()) {
            throw new RealisationNotFoundException(
                    "Realisation with ID " + realisationId + " and version " + realisationVer + " was not found in the database.",realisationId, realisationVer);
        }
        Realisation existingRealisation = existingRealisationOpt.get();
        if (Boolean.TRUE.equals(existingRealisation.getIsDeleted())) {
            throw new RealisationNotFoundException(
                    "Cannot update a deleted Realisation with ID " + realisationId + " and version " + realisationVer,realisationId, realisationVer);
        }


        if (request.getImputationBudgetaire() != null) {
            existingRealisation.setImputationBudgetaire(refParamRepository.findById(request.getImputationBudgetaire().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Imputation Budgetaire not found")));
        }

        if (request.getBeneficiaire() != null) {
            existingRealisation.setBeneficiaire(request.getBeneficiaire());
        }
        if (request.getTypeMarcher() != null) {
            existingRealisation.setTypeMarcher(refParamRepository.findById(request.getTypeMarcher().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Type Marcher not found")));
        }

        if (request.getModePassation() != null) {
            existingRealisation.setModePassation(modeDePassationSelectionRepository.findById(request.getModePassation().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Mode Passation not found")));
        }

        // Handle document management by comparing existing docs with requested docIds
        if (request.getDocIds() != null) {
            Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());

            // Get active document IDs
            Set<Integer> activeExistingDocIds = existingRealisation.getDocs() != null ?
                    existingRealisation.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .map(Docs::getId)
                            .collect(Collectors.toSet()) : new HashSet<>();

            // Log document changes
            if (!requestedDocIds.equals(activeExistingDocIds)) {
                System.out.println("Document changes detected for Realisation update ID: " + realisationId);

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

            if (existingRealisation.getDocs() != null) {
                // For each existing document in the Realisation
                for (Docs existingDoc : existingRealisation.getDocs()) {
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

            // Add any new docs from the request that weren't already in existingRealisation
            for (Integer docId : requestedDocIds) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));

                // Set up bidirectional relationship
                if (doc.getRealisations() == null) {
                    doc.setRealisations(new ArrayList<>());
                }
                if (!doc.getRealisations().contains(existingRealisation)) {
                    doc.getRealisations().add(existingRealisation);
                }

                // Ensure doc is active
                doc.setIsDeleted(false);
                docsRepository.save(doc);

                updatedDocs.add(doc);
                System.out.println("Added new document with ID " + doc.getId() + " to Realisation");
            }

            // Update the Realisation with the final docs list
            existingRealisation.setDocs(updatedDocs);
        } else if (existingRealisation.getDocs() != null && !existingRealisation.getDocs().isEmpty()) {
            // No docIds provided - mark all existing documents as deleted
            List<Docs> existingDocs = existingRealisation.getDocs();
            for (Docs doc : existingDocs) {
                if (!Boolean.TRUE.equals(doc.getIsDeleted())) {
                    doc.setIsDeleted(true);
                    docsRepository.save(doc);
                    System.out.println("Marked document with ID " + doc.getId() + " as deleted (no docIds provided)");
                }
            }
            // Keep the existing docs list to preserve relationships (just all marked as deleted)
        }

        // Handle Lot management - similar to document handling
        if (request.getLots() != null) {
            Set<Integer> requestedLotIds = request.getLots().stream()
                .filter(lot -> lot.getId() != null)
                .map(LotResponse::getId)
                .collect(Collectors.toSet());

            List<Lot> updatedLots = new ArrayList<>();

            if (existingRealisation.getLots() != null) {
                for (Lot existingLot : existingRealisation.getLots()) {
                    if (requestedLotIds.contains(existingLot.getId())) {
                        // Lot is in the request, ensure it's not marked as deleted
                        existingLot.setIsDeleted(false);
                        lotRepository.save(existingLot);
                        updatedLots.add(existingLot);

                        // Remove from requested IDs since we've handled it
                        requestedLotIds.remove(existingLot.getId());
                    } else {
                        // Lot is not in the request, mark it as deleted
                        existingLot.setIsDeleted(true);
                        lotRepository.save(existingLot);
                        updatedLots.add(existingLot); // Keep it in the collection but as deleted
                        System.out.println("Marked lot with ID " + existingLot.getId() + " as deleted (not in request)");
                    }
                }
            }

            // Add any new lots from the request
            for (LotResponse lotResponse : request.getLots()) {
                if (lotResponse.getId() == null) {
                    // New lot - create it
                    Lot newLot = new Lot();
                    newLot.setDescription(lotResponse.getDescription());
                    newLot.setVersion(1); // First version

                    // Generate new ID if not provided
//                    Integer lotId = lotRepository.findMaxId() + 1;
                    Integer lotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ) + 1;
                    newLot.setId(lotId);

                    // Save the new lot
                    Lot savedLot = lotRepository.save(newLot);
                    updatedLots.add(savedLot);
                    System.out.println("Created new Lot with ID: " + savedLot.getId());
                } else if (!updatedLots.stream().anyMatch(lot -> lot.getId().equals(lotResponse.getId()))) {
                    // Existing lot not yet in our collection
                    lotRepository.findById(new CompositeId(lotResponse.getId(), lotResponse.getVersion()))
                            .ifPresent(lot -> {
                                lot.setIsDeleted(false);
                                lotRepository.save(lot);
                                updatedLots.add(lot);
                            });
                }
            }

            // Update the Realisation with the final lots set
            existingRealisation.setLots(updatedLots);
        } else if (existingRealisation.getLots() != null && !existingRealisation.getLots().isEmpty()) {
            // No lots provided - mark all existing lots as deleted
            List<Lot> existingLots = existingRealisation.getLots();
            for (Lot lot : existingLots) {
                if (!Boolean.TRUE.equals(lot.getIsDeleted())) {
                    lot.setIsDeleted(true);
                    lotRepository.save(lot);
                    System.out.println("Marked lot with ID " + lot.getId() + " as deleted (no lots provided)");
                }
            }
        }

        Realisation realisation = RealisationMapper.INSTANCE.updateRealisationFromRequest(request, existingRealisation);
        // Save the updated realisation
        Realisation savedRealisation = repository.save(realisation);

        // Create a copy for the response with only non-deleted documents
        Realisation copyForResponse = new Realisation();
        BeanUtils.copyProperties(savedRealisation, copyForResponse);

        // Filter out deleted documents for the response
        if (savedRealisation.getDocs() != null) {
            copyForResponse.setDocs(
                savedRealisation.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList())
            );
        }

        // Also filter lot documents if there are any
        if (copyForResponse.getLots() != null) {
            copyForResponse.setLots(
                copyForResponse.getLots().stream()
                    .filter(lot -> !Boolean.TRUE.equals(lot.getIsDeleted()))
                    .collect(Collectors.toList())
            );
        }

        return copyForResponse;
    }
    @Transactional
    public Realisation updateWithVersioning(Integer realisationId,
                                            Integer realisationVer,
                                            RealisationBaseRequestDto request) throws RealisationNotFoundException {
        CompositeId compositeId = new CompositeId(realisationId, realisationVer);
        Realisation existing = repository.findById(compositeId)
                .orElseThrow(() -> new RealisationNotFoundException(
                        "Realisation with ID " + realisationId + " and version " + realisationVer + " was not found in the database.",
                        realisationId, realisationVer));

        if (Boolean.TRUE.equals(existing.getIsDeleted())) {
            throw new RealisationNotFoundException(
                    "Cannot update a deleted Realisation with ID " + realisationId + " and version " + realisationVer,
                    realisationId, realisationVer);
        }

        log.info("Updating Realisation ID: {}, Current Version: {}", realisationId, realisationVer);
        log.info("Request data: {}", request);

        Realisation probe = new Realisation();
        probe.setRealisationEnvisagees(request.getRealisationEnvisagees() != null ? request.getRealisationEnvisagees() : existing.getRealisationEnvisagees());
        probe.setBeneficiaire(request.getBeneficiaire() != null ? request.getBeneficiaire() : existing.getBeneficiaire());

        RefParam imput = request.getImputationBudgetaire() != null
                ? refParamRepository.findById(request.getImputationBudgetaire().getId()).orElse(existing.getImputationBudgetaire())
                : existing.getImputationBudgetaire();
        probe.setImputationBudgetaire(imput);

        RefParam typeMarcher = request.getTypeMarcher() != null
                ? refParamRepository.findById(request.getTypeMarcher().getId()).orElse(existing.getTypeMarcher())
                : existing.getTypeMarcher();
        probe.setTypeMarcher(typeMarcher);

        ModeDePassationSelection modePassation = request.getModePassation() != null
                ? modeDePassationSelectionRepository.findById(request.getModePassation().getId()).orElse(existing.getModePassation())
                : existing.getModePassation();
        probe.setModePassation(modePassation);

        probe.setDatePrevLps(request.getDatePrevLps() != null ? request.getDatePrevLps() : existing.getDatePrevLps());
        probe.setDatePrevAc(request.getDatePrevAc() != null ? request.getDatePrevAc() : existing.getDatePrevAc());
        probe.setDatePrevDp(request.getDatePrevDp() != null ? request.getDatePrevDp() : existing.getDatePrevDp());
        probe.setDatePrevAp(request.getDatePrevAp() != null ? request.getDatePrevAp() : existing.getDatePrevAp());
        probe.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existing.getBudgetEstime());
        probe.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : existing.getIsPublished());
        probe.setPublishedAt(request.getPublishedAt() != null ? request.getPublishedAt() : existing.getPublishedAt());
        probe.setSort(request.getSort() != null ? request.getSort() : existing.getSort());

        boolean lotChangesDetected = false;
        if (request.getLots() != null) {
            Map<Integer, Lot> byId = Optional.ofNullable(existing.getLots())
                    .orElseGet(List::of).stream()
                    .filter(l -> !Boolean.TRUE.equals(l.getIsDeleted()))
                    .collect(Collectors.toMap(Lot::getId, Function.identity(), (a, b) -> a));

            for (LotResponse lr : request.getLots()) {
                if (lr.getId() == null) { // brand-new
                    lotChangesDetected = true;
                    break;
                }
                Lot old = byId.get(lr.getId());
                if (old == null) {
                    lotChangesDetected = true;
                    break;
                }
                String newDesc = lr.getDescription();
                boolean descChanged = newDesc != null && !newDesc.equals(old.getDescription());

                BigDecimal reqBudget = null;
                if (lr.getBudget() != null) {
                    reqBudget = (lr.getBudget() instanceof BigDecimal)
                            ? (BigDecimal) lr.getBudget()
                            : new BigDecimal(lr.getBudget().toString());
                }
                BigDecimal oldBudget = old.getBudget();
                boolean budgetChanged = (reqBudget != null) && (oldBudget == null || oldBudget.compareTo(reqBudget) != 0);

                if (descChanged || budgetChanged) {
                    lotChangesDetected = true;
                    break;
                }
            }
        }

        // DOC changes by ID set
        boolean documentsChanged = false;
        if (request.getDocIds() != null) {
            Set<Integer> reqSet = new HashSet<>(request.getDocIds());
            Set<Integer> existingActive = Optional.ofNullable(existing.getDocs()).orElseGet(List::of).stream()
                    .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                    .map(Docs::getId)
                    .collect(Collectors.toSet());
            documentsChanged = !existingActive.equals(reqSet);
        }

        // FINANCEMENT changes
        boolean financementChangesDetected = checkFinancementChanges(existing.getFinancements(), request.getFinancements());
        log.info("Financement changes detected: {}", financementChangesDetected);

        boolean fieldChanges = hasChanges(probe, existing, request.getDocIds());
        boolean hasChanges = fieldChanges || lotChangesDetected || documentsChanged || financementChangesDetected;

        log.info("Change summary -> fields: {}, lots: {}, docs: {}, fins: {}, overall: {}",
                fieldChanges, lotChangesDetected, documentsChanged, financementChangesDetected, hasChanges);

        if (!hasChanges) {
            log.info("No changes detected, returning existing version {}", realisationVer);
            Realisation resp = new Realisation();
            BeanUtils.copyProperties(existing, resp);
            if (existing.getDocs() != null) {
                resp.setDocs(existing.getDocs().stream().filter(d -> !Boolean.TRUE.equals(d.getIsDeleted())).collect(Collectors.toList()));
            }
            if (existing.getLots() != null) {
                resp.setLots(existing.getLots().stream().filter(l -> !Boolean.TRUE.equals(l.getIsDeleted())).collect(Collectors.toList()));
            }
            return resp;
        }

        log.info("Creating new Realisation version: {}", realisationVer + 1);
        Realisation created = new Realisation();
        created.setId(realisationId);
        created.setVersion(realisationVer + 1);

        created.setRealisationEnvisagees(probe.getRealisationEnvisagees());
        created.setBeneficiaire(probe.getBeneficiaire());
        created.setImputationBudgetaire(probe.getImputationBudgetaire());
        created.setMonnaie(probe.getMonnaie());
        created.setTypeMarcher(probe.getTypeMarcher());
        created.setModePassation(probe.getModePassation());
        created.setDatePrevLps(probe.getDatePrevLps());
        created.setDatePrevAc(probe.getDatePrevAc());
        created.setDatePrevDp(probe.getDatePrevDp());
        created.setDatePrevAp(probe.getDatePrevAp());
        created.setBudgetEstime(probe.getBudgetEstime());
        created.setIsPublished(probe.getIsPublished());
        created.setPublishedAt(probe.getPublishedAt());
        created.setSort(probe.getSort());

        created.setPaas(new ArrayList<>());
        created.setPpms(new ArrayList<>());
        created.setDocs(new ArrayList<>());
        created.setLots(new ArrayList<>());
        created.setFinancements(new ArrayList<>());

        created = repository.save(created);
        final Realisation finalNew = created;
        log.info("Persisted new Realisation {},{}", finalNew.getId(), finalNew.getVersion());

        if (existing.getPpms() != null && !existing.getPpms().isEmpty()) {
            finalNew.setPpms(new ArrayList<>(existing.getPpms()));
            log.info("Copied {} PPM relationships", existing.getPpms().size());
        }

        List<Lot> newLots = new ArrayList<>();
        if (request.getLots() != null && !request.getLots().isEmpty()) {
            log.info("Processing {} lots from request", request.getLots().size());
            for (LotResponse lr : request.getLots()) {
                String newDesc = lr.getDescription();
                BigDecimal reqBudget = null;
                if (lr.getBudget() != null) {
                    reqBudget = (lr.getBudget() instanceof BigDecimal)
                            ? (BigDecimal) lr.getBudget()
                            : new BigDecimal(lr.getBudget().toString());
                }

                if (lr.getId() == null) {
                    // brand-new lot
                    Lot nl = new Lot();
                    Integer newLotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ) + 1;
                    nl.setId(newLotId);
                    nl.setVersion(1);
                    nl.setDescription(newDesc != null ? newDesc : "");
                    if (reqBudget != null) nl.setBudget(reqBudget); // <<< persist budget
                    nl = lotRepository.save(nl);
                    newLots.add(nl);
                    log.info("Created new Lot id={}, v=1, budget={}", nl.getId(), nl.getBudget());
                } else {
                    Integer lotId = lr.getId();
                    Integer version = lr.getVersion();
                    if (version == null) {
                        version = lotRepository.findMaxVersionById(lotId);
                        if (version == null) {
                            log.warn("Lot {} has no versions; skipping attachment", lotId);
                            continue;
                        }
                    }
                    Lot base = lotRepository.findById(new CompositeId(lotId, version)).orElse(null);
                    if (base == null) {
                        log.warn("Lot {},v{} not found; skipping", lotId, version);
                        continue;
                    }

                    boolean descChanged = newDesc != null && !newDesc.equals(base.getDescription());
                    boolean budgetChanged = (reqBudget != null) && (base.getBudget() == null || base.getBudget().compareTo(reqBudget) != 0);

                    if (descChanged || budgetChanged) {
                        Lot nl = new Lot();
                        nl.setId(base.getId());
                        nl.setVersion(base.getVersion() + 1);
                        nl.setDescription(descChanged ? newDesc : base.getDescription());
                        nl.setBudget(budgetChanged ? reqBudget : base.getBudget()); // keep budget when DTO not providing one
                        nl = lotRepository.save(nl);
                        newLots.add(nl);
                        log.info("Created new Lot version id={}, v={}, budget={}", nl.getId(), nl.getVersion(), nl.getBudget());
                    } else {
                        newLots.add(base);
                    }
                }
            }
        }
        finalNew.setLots(newLots);
        log.info("Attached {} lots to Realisation {},{}", newLots.size(), finalNew.getId(), finalNew.getVersion());

        // Financements
        List<Financement> newFins;
        if (financementChangesDetected) {
            log.info("Creating updated financements for new version");
            newFins = createUpdatedFinancements(existing.getFinancements(), request.getFinancements(), finalNew);
            log.info("Created {} updated financements", newFins.size());
        } else if (existing.getFinancements() != null && !existing.getFinancements().isEmpty()) {
            log.info("Copying existing financements to new version");
            newFins = copyFinancementsToNewVersion(existing.getFinancements(), finalNew);
            log.info("Copied {} existing financements", newFins.size());
        } else {
            newFins = new ArrayList<>();
        }

        for (Financement f : newFins) {
            if (f.getTauxApplicable() == null) {
                Double taux = (f.getMonnaie() != null && f.getMonnaie().getTaux() != null)
                        ? f.getMonnaie().getTaux()
                        : 0.0;
                f.setTauxApplicable(taux);
                financementRepository.save(f);
                log.info("Backfilled tauxApplicable={} for Financement {}", taux, f.getId());
            }
        }

        finalNew.setFinancements(newFins);

        BigDecimal totalFx = Optional.ofNullable(newFins).orElseGet(List::of).stream()
                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                .map(f -> {
                    BigDecimal amt = (f.getBudgetEstimer() != null) ? f.getBudgetEstimer() : BigDecimal.ZERO;
                    double taux = (f.getTauxApplicable() != null) ? f.getTauxApplicable()
                            : ((f.getMonnaie() != null && f.getMonnaie().getTaux() != null) ? f.getMonnaie().getTaux() : 0.0);
                    return amt.multiply(BigDecimal.valueOf(taux));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        finalNew.setBudgetEstime(totalFx);

        // Documents: attach by request or copy active
        List<Docs> docsToAttach = new ArrayList<>();
        if (request.getDocIds() != null) {
            for (Integer id : request.getDocIds()) {
                Docs d = docsRepository.findById(id).orElse(null);
                if (d != null) docsToAttach.add(d);
            }
        } else if (existing.getDocs() != null && !existing.getDocs().isEmpty()) {
            docsToAttach = existing.getDocs().stream().filter(d -> !Boolean.TRUE.equals(d.getIsDeleted())).collect(Collectors.toList());
        }
        finalNew.setDocs(docsToAttach);
        log.info("Attached {} documents to Realisation {},{}", docsToAttach.size(), finalNew.getId(), finalNew.getVersion());

        Realisation saved = repository.save(finalNew);
        log.info("Final save Realisation {},{} with {} lots and {} financements",
                saved.getId(), saved.getVersion(),
                Optional.ofNullable(saved.getLots()).map(List::size).orElse(0),
                Optional.ofNullable(saved.getFinancements()).map(List::size).orElse(0));

        Realisation resp = new Realisation();
        BeanUtils.copyProperties(saved, resp);
        if (saved.getDocs() != null) {
            resp.setDocs(saved.getDocs().stream().filter(d -> !Boolean.TRUE.equals(d.getIsDeleted())).collect(Collectors.toList()));
        }
        if (saved.getLots() != null) {
            resp.setLots(saved.getLots().stream().filter(l -> !Boolean.TRUE.equals(l.getIsDeleted())).collect(Collectors.toList()));
        }
        return resp;
    }

    private boolean hasLotChangesForPaa(Realisation existingReal, List<LotResponse> incomingDtos) {
        // Current active lots
        List<Lot> existingActive = (existingReal.getLots() == null)
                ? Collections.emptyList()
                : existingReal.getLots().stream()
                .filter(l -> !Boolean.TRUE.equals(l.getIsDeleted()))
                .collect(Collectors.toList());

        // If request provides no lots, that means "clear them" -> change iff any exist
        if (incomingDtos == null || incomingDtos.isEmpty()) {
            return !existingActive.isEmpty();
        }

        // Normalize incoming
        List<LotResponse> incoming = incomingDtos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Quick add/delete signals
        // 1) Any brand-new lot (no id)  -> add
        if (incoming.stream().anyMatch(lr -> lr.getId() == null)) return true;

        // 2) Count mismatch (after filtering deleted) -> add or delete
        if (incoming.size() != existingActive.size()) return true;

        // 3) Missing existing ids in the request -> delete
        Set<Integer> existingIds = existingActive.stream().map(Lot::getId).collect(Collectors.toSet());
        Set<Integer> incomingIds = incoming.stream().map(LotResponse::getId).collect(Collectors.toSet());
        if (!incomingIds.containsAll(existingIds)) return true;

        // 4) Compare content (budget/description) for the same ids -> update
        Map<Integer, Lot> byId = existingActive.stream()
                .collect(Collectors.toMap(Lot::getId, x -> x, (a, b) -> a));

        for (LotResponse lr : incoming) {
            Lot ex = byId.get(lr.getId());
            if (ex == null) return true; // references an id not on this realisation

            BigDecimal reqBudget = defaultZero(lr.getBudget());
            BigDecimal exBudget  = defaultZero(ex.getBudget());
            if (reqBudget.compareTo(exBudget) != 0) return true;

            String reqDesc = defaultEmpty(lr.getDescription());
            String exDesc  = defaultEmpty(ex.getDescription());
            if (!reqDesc.equals(exDesc)) return true;
        }

        // No add/update/delete detected
        return false;
    }

    private static BigDecimal defaultZero(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }
    private static String defaultEmpty(String s) { return s == null ? "" : s; }

    private boolean hasChanges(Realisation tempRealisation, Realisation existingRealisation, List<Integer> requestDocIds) {
        // Check realisationEnvisagees
        if (!Objects.equals(tempRealisation.getRealisationEnvisagees(), existingRealisation.getRealisationEnvisagees())) {
            System.out.println("Change detected in realisationEnvisagees: " + existingRealisation.getRealisationEnvisagees() + " -> " + tempRealisation.getRealisationEnvisagees());
            return true;
        }
        if (!Objects.equals(tempRealisation.getBeneficiaire(), existingRealisation.getBeneficiaire())) {
            System.out.println("Change detected in Beneficaire: " + existingRealisation.getBeneficiaire() + " -> " + tempRealisation.getBeneficiaire());
            return true;
        }

//        // Check service
//        if (tempRealisation.getService() != existingRealisation.getService() &&
//                (tempRealisation.getService() == null || existingRealisation.getService() == null ||
//                        !tempRealisation.getService().getId().equals(existingRealisation.getService().getId()))) {
//            System.out.println("Change detected in service");
//            return true;
//        }
//
//        // Check direction
//        if (tempRealisation.getDirection() != existingRealisation.getDirection() &&
//                (tempRealisation.getDirection() == null || existingRealisation.getDirection() == null ||
//                        !tempRealisation.getDirection().getId().equals(existingRealisation.getDirection().getId()))) {
//            System.out.println("Change detected in direction");
//            return true;
//        }

//        // Check sourceFinancement
//        if (tempRealisation.getSourceFinancement() != existingRealisation.getSourceFinancement() &&
//                (tempRealisation.getSourceFinancement() == null || existingRealisation.getSourceFinancement() == null ||
//                        !tempRealisation.getSourceFinancement().getId().equals(existingRealisation.getSourceFinancement().getId()))) {
//            System.out.println("Change detected in sourceFinancement");
//            return true;
//        }

        // Check imputationBudgetaire
        if (tempRealisation.getImputationBudgetaire() != existingRealisation.getImputationBudgetaire() &&
                (tempRealisation.getImputationBudgetaire() == null || existingRealisation.getImputationBudgetaire() == null ||
                        !tempRealisation.getImputationBudgetaire().getId().equals(existingRealisation.getImputationBudgetaire().getId()))) {
            System.out.println("Change detected in imputationBudgetaire");
            return true;
        }

//        // Check monais
//        if (tempRealisation.getMonais() != existingRealisation.getMonais() &&
//                (tempRealisation.getMonais() == null || existingRealisation.getMonais() == null ||
//                        !tempRealisation.getMonais().getId().equals(existingRealisation.getMonais().getId()))) {
//            System.out.println("Change detected in monais");
//            return true;
//        }

        // Check monnaie
        if (tempRealisation.getMonnaie() != existingRealisation.getMonnaie() &&
                (tempRealisation.getMonnaie() == null || existingRealisation.getMonnaie() == null ||
                        !tempRealisation.getMonnaie().getId().equals(existingRealisation.getMonnaie().getId()))) {
            System.out.println("Change detected in monnaie");
            return true;
        }

        // Check typeMarcher
        if (tempRealisation.getTypeMarcher() != existingRealisation.getTypeMarcher() &&
                (tempRealisation.getTypeMarcher() == null || existingRealisation.getTypeMarcher() == null ||
                        !tempRealisation.getTypeMarcher().getId().equals(existingRealisation.getTypeMarcher().getId()))) {
            System.out.println("Change detected in typeMarcher");
            return true;
        }

//        // Check modeSelection
//        if (tempRealisation.getModeSelection() != existingRealisation.getModeSelection() &&
//                (tempRealisation.getModeSelection() == null || existingRealisation.getModeSelection() == null ||
//                        !tempRealisation.getModeSelection().getId().equals(existingRealisation.getModeSelection().getId()))) {
//            System.out.println("Change detected in modeSelection");
//            return true;
//        }

        // Check modePassation
        if (tempRealisation.getModePassation() != existingRealisation.getModePassation() &&
                (tempRealisation.getModePassation() == null || existingRealisation.getModePassation() == null ||
                        !tempRealisation.getModePassation().getId().equals(existingRealisation.getModePassation().getId()))) {
            System.out.println("Change detected in modePassation");
            return true;
        }

        // Check datePrevLps
        if (!Objects.equals(tempRealisation.getDatePrevLps(), existingRealisation.getDatePrevLps())) {
            System.out.println("Change detected in datePrevLps");
            return true;
        }

        // Check datePrevAc
        if (!Objects.equals(tempRealisation.getDatePrevAc(), existingRealisation.getDatePrevAc())) {
            System.out.println("Change detected in datePrevAc");
            return true;
        }

        // Check datePrevDp
        if (!Objects.equals(tempRealisation.getDatePrevDp(), existingRealisation.getDatePrevDp())) {
            System.out.println("Change detected in datePrevDp");
            return true;
        }

        // Check datePrevAp
        if (!Objects.equals(tempRealisation.getDatePrevAp(), existingRealisation.getDatePrevAp())) {
            System.out.println("Change detected in datePrevAp");
            return true;
        }

        BigDecimal oldB = existingRealisation.getBudgetEstime();
        BigDecimal newB = tempRealisation.getBudgetEstime();

        if (!equalByValue(newB, oldB)) {
            System.out.println("Change detected in budgetEstime: " + oldB + " -> " + newB);
            return true;
        }

        // Check isPublished
        if (!Objects.equals(tempRealisation.getIsPublished(), existingRealisation.getIsPublished())) {
            System.out.println("Change detected in isPublished");
            return true;
        }

        // Check publishedAt
        if (!Objects.equals(tempRealisation.getPublishedAt(), existingRealisation.getPublishedAt())) {
            System.out.println("Change detected in publishedAt");
            return true;
        }


        System.out.println("No field changes detected for Realisation ID: " + existingRealisation.getId());
        return false;
    }

    private static boolean equalByValue(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;   // ignores scale
    }

    private Integer getRefParamId(RefParam refParam) {
        return refParam != null ? refParam.getId() : null;
    }

    private void copyProperties(Realisation source, Realisation target) {
        target.setRealisationEnvisagees(source.getRealisationEnvisagees());
        target.setBeneficiaire(source.getBeneficiaire());
//        target.setService(source.getService());
//        target.setDirection(source.getDirection());
//        target.setSourceFinancement(source.getSourceFinancement());
        target.setImputationBudgetaire(source.getImputationBudgetaire());
//        target.setMonais(source.getMonais());
        target.setMonnaie(source.getMonnaie());
        target.setTypeMarcher(source.getTypeMarcher());
//        target.setModeSelection(source.getModeSelection());
        target.setModePassation(source.getModePassation());
        target.setDatePrevLps(source.getDatePrevLps());
        target.setDatePrevAc(source.getDatePrevAc());
        target.setDatePrevDp(source.getDatePrevDp());
        target.setDatePrevAp(source.getDatePrevAp());
        target.setBudgetEstime(source.getBudgetEstime());
        target.setIsPublished(source.getIsPublished());
        target.setPublishedAt(source.getPublishedAt());
        target.setSort(source.getSort());
    }

    @Override
    public void delete(Integer realisationId, Integer realisationVer) {
        repository.softDelete(realisationId, realisationVer);
    }

//    /**
//     * Compares two Realisation objects to determine if there are any changes.
//     *
//     * @param newRealisation The realisation with potentially new values
//     * @param existingRealisation The existing realisation in the database
//     * @param docIds List of document IDs in the request (if any)
//     * @return true if there are changes, false otherwise
//     */
//    private boolean hasChanges(Realisation newRealisation, Realisation existingRealisation, List<Integer> docIds) {
//        boolean fieldChanges = !Objects.equals(newRealisation.getRealisationEnvisagees(), existingRealisation.getRealisationEnvisagees())
//                || !Objects.equals(getRefParamId(newRealisation.getService()), getRefParamId(existingRealisation.getService()))
//                || !Objects.equals(getRefParamId(newRealisation.getDirection()), getRefParamId(existingRealisation.getDirection()))
//                || !Objects.equals(getRefParamId(newRealisation.getSourceFinancement()), getRefParamId(existingRealisation.getSourceFinancement()))
//                || !Objects.equals(getRefParamId(newRealisation.getMonais()), getRefParamId(existingRealisation.getMonais()))
//                || !Objects.equals(getRefParamId(newRealisation.getTypeMarcher()), getRefParamId(existingRealisation.getTypeMarcher()))
//                || !Objects.equals(getRefParamId(newRealisation.getModeSelection()), getRefParamId(existingRealisation.getModeSelection()))
//                || !Objects.equals(newRealisation.getDatePrevLps(), existingRealisation.getDatePrevLps())
//                || !Objects.equals(newRealisation.getDatePrevAc(), existingRealisation.getDatePrevAc())
//                || !Objects.equals(newRealisation.getDatePrevDp(), existingRealisation.getDatePrevDp())
//                || !Objects.equals(newRealisation.getDatePrevAp(), existingRealisation.getDatePrevAp())
//                || !Objects.equals(newRealisation.getBudgetEstime(), existingRealisation.getBudgetEstime())
//                || !Objects.equals(newRealisation.getIsPublished(), existingRealisation.getIsPublished())
//                || !Objects.equals(newRealisation.getPublishedAt(), existingRealisation.getPublishedAt())
//                || !Objects.equals(newRealisation.getSort(), existingRealisation.getSort());
//
//        // Log specific field changes for debugging
//        if (fieldChanges) {
//            System.out.println("Field changes detected in Realisation:");
//            if (!Objects.equals(newRealisation.getRealisationEnvisagees(), existingRealisation.getRealisationEnvisagees()))
//                System.out.println("  - RealisationEnvisagees changed: " + existingRealisation.getRealisationEnvisagees() + " -> " + newRealisation.getRealisationEnvisagees());
//            if (!Objects.equals(getRefParamId(newRealisation.getService()), getRefParamId(existingRealisation.getService())))
//                System.out.println("  - Service changed: " + getRefParamId(existingRealisation.getService()) + " -> " + getRefParamId(newRealisation.getService()));
//            if (!Objects.equals(getRefParamId(newRealisation.getDirection()), getRefParamId(existingRealisation.getDirection())))
//                System.out.println("  - Direction changed: " + getRefParamId(existingRealisation.getDirection()) + " -> " + getRefParamId(newRealisation.getDirection()));
//            // More detailed field change reporting can be added as needed
//        }
//
//        // When there are no other field changes, check if there are document changes
//        boolean docChanges = false;
//        if (docIds != null) {
//            // Get active documents from existing realisation
//            Set<Integer> activeExistingDocIds = existingRealisation.getDocs() != null ?
//                    existingRealisation.getDocs().stream()
//                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
//                            .map(Docs::getId)
//                            .collect(Collectors.toSet()) : new HashSet<>();
//
//            // Check if there are any differences between requested docIds and active existing docIds
//            Set<Integer> requestedDocIdsSet = new HashSet<>(docIds);
//            if (!requestedDocIdsSet.equals(activeExistingDocIds)) {
//                // Document changes should trigger version increment
//                System.out.println("Document changes detected in hasChanges method");
//                System.out.println("  - Requested doc IDs: " + requestedDocIdsSet);
//                System.out.println("  - Existing active doc IDs: " + activeExistingDocIds);
//                docChanges = true;
//            }
//        }
//
//        return fieldChanges || docChanges;
//    }

    /**
     * Updates the lots relationship for a Realisation without incrementing the version.
     *
     * @param realisation The existing realisation to update
     * @param lots The lot responses to associate with the realisation
     */
    private void updateRealisationLots(Realisation realisation, Set<LotResponse> lots) {
        if (lots == null || lots.isEmpty()) {
            return;
        }

        // Create a set to hold the lots entities
        List<Lot> lotEntities = new ArrayList<>();

        // Convert LotResponse to Lot entities
        for (LotResponse lotResponse : lots) {
            // Find the lot by ID and version
            lotRepository.findById(new CompositeId(lotResponse.getId(), lotResponse.getVersion()))
                    .ifPresent(lotEntities::add);
        }

        // Update the realisation's lots
        realisation.setLots(lotEntities);

        // Save the realisation with the updated lots
        repository.save(realisation);
    }

    /**
     * Updates a Realisation with versioning by automatically finding and using the latest version.
     * <p>
     * This method finds the latest version of the Realisation with the given ID and then
     * calls the version-specific updateWithVersioning method.
     *
     * @param realisationId the ID of the Realisation to update
     * @param request the request containing the updated data
     * @return the newly created version of the Realisation
     * @throws RealisationNotFoundException if no versions of the Realisation exist or if all versions are deleted
     */
    @Transactional
    public Realisation updateWithVersioning(Integer realisationId, RealisationBaseRequestDto request) throws RealisationNotFoundException {
        // Find the latest version of the Realisation
        Integer latestVersion = repository.findMaxVersionById(realisationId);

        if (latestVersion == null) {
            throw new RealisationNotFoundException(
                    "No versions found for Realisation with ID: " + realisationId,realisationId, null);
        }

        System.out.println("Found latest version for Realisation ID " + realisationId + ": " + latestVersion);

        // Call the version-specific method with the latest version
        return updateWithVersioning(realisationId, latestVersion, request);
    }


    //TODO: nejah
    private void processAndSaveDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
        UUID uuid = UUID.randomUUID();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
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

                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_PAA+ "/%d/%d",
                                savedRealisation.getId(), savedRealisation.getVersion());

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        String uniqueFileName = "paa_" + uuid + "_" + extension;
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
                        log.info(alfrescoId);

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
                        if (savedRealisation.getDocs() == null) {
                            savedRealisation.setDocs(new ArrayList<>());
                        }
                        document.getRealisations().add(savedRealisation);
                        savedRealisation.getDocs().add(document);

                        docsRepository.save(document);
                    } catch (Exception e) {
                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
                    }
                }
            }
        }
    }


    /**
     * Check if financement data has changed compared to existing financements
     */
    private boolean checkFinancementChanges(List<Financement> existingFinancements, List<FinancementRequest> updates) {
        // If both are null/empty, no changes
        if ((existingFinancements == null || existingFinancements.isEmpty()) &&
                (updates == null || updates.isEmpty())) {
            return false;
        }

        // If one is null/empty and other isn't, there are changes
        if ((existingFinancements == null || existingFinancements.isEmpty()) ||
                (updates == null || updates.isEmpty())) {
            return true;
        }

        // If different sizes, there are changes
        if (existingFinancements.size() != updates.size()) {
            return true;
        }

        // Create maps for easier comparison by monnaie
//  Map<Integer, Financement> existingByMonnaie = existingFinancements.stream()
//          .collect(Collectors.toMap(f -> f.getId().getMonnaie().getId(), f -> f));
        Map<Integer, Financement> existingByMonnaie = existingFinancements.stream()
                .collect(Collectors.toMap(
                        f -> f.getMonnaie().getId(),
                        f -> f,
                        (existing, replacement) -> replacement // ← Keep the newer one
                ));

        Map<Integer, FinancementRequest> updatesByMonnaie = updates.stream()
                .collect(Collectors.toMap(
                        FinancementRequest::getMonnaieId,
                        f -> f,
                        (a, b) -> b
                ));

        // Check if monnaie sets are different
        if (!existingByMonnaie.keySet().equals(updatesByMonnaie.keySet())) {
            return true;
        }

        // Compare each financement
        for (Integer monnaieId : existingByMonnaie.keySet()) {
            Financement existing = existingByMonnaie.get(monnaieId);
            FinancementRequest update = updatesByMonnaie.get(monnaieId);

            // Compare budget
            if (update.getBudgetEstimer() != null &&
                    existing.getBudgetEstimer().compareTo(update.getBudgetEstimer()) != 0) {
                return true;
            }

            // Compare source financement
            if (update.getSourceFinancement() != null && update.getSourceFinancement().getId() != null) {
                Integer existingSourceId = existing.getSourceFinancement() != null ?
                        existing.getSourceFinancement().getId() : null;
                if (!Objects.equals(existingSourceId, update.getSourceFinancement().getId())) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Create updated financements for new realisation version
     */
    private List<Financement> createUpdatedFinancements(List<Financement> existingFinancements,
                                                        List<FinancementRequest> updates,
                                                        Realisation newVersionRealisation) {
        List<Financement> newFinancements = new ArrayList<>();

        if (updates == null || updates.isEmpty()) {
            return newFinancements;
        }

        for (FinancementRequest update : updates) {
            // Create new FinancementId (composite key)
            FinancementId financementId = new FinancementId();
            financementId.setRealisation(newVersionRealisation);
            Financement financement = new Financement();
            // Set Monnaie
            Monnaie monnaie = monnaieRepository.findById(update.getMonnaieId())
                    .orElseThrow(() -> new EntityNotFoundException("Monnaie not found with ID: " + update.getMonnaieId()));
            financement.setMonnaie(monnaie);

            // ADD THIS LOGIC HERE
            if (monnaie.getTaux() != null) {
                financement.setTauxApplicable(monnaie.getTaux());
                System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux() + " from Monnaie ID: " + monnaie.getId());
            } else {
                financement.setTauxApplicable(0.0);
                System.out.println("Monnaie taux is null, set tauxApplicable to 0.0 for Monnaie ID: " + monnaie.getId());
            }

            // Generate new financement ID
            Integer nextFinancementId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ) + 1;
            financementId.setFinancementId(nextFinancementId);

            // Create Financement entity

            financement.setId(financementId);
            financement.setBudgetEstimer(update.getBudgetEstimer());

            // Set source financement if provided
            if (update.getSourceFinancement() != null && update.getSourceFinancement().getId() != null) {
                RefParam sourceFinancement = refParamRepository.findById(update.getSourceFinancement().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Source Financement not found with ID: " + update.getSourceFinancement().getId()));
                financement.setSourceFinancement(sourceFinancement);
            }

            newFinancements.add(financement);
        }

        return newFinancements;
    }

    /**
     * Copy existing financements to new realisation version (when no financement changes)
     */
    private List<Financement> copyFinancementsToNewVersion(List<Financement> existingFinancements,
                                                           Realisation newVersionRealisation) {
        List<Financement> copiedFinancements = new ArrayList<>();

        if (existingFinancements == null || existingFinancements.isEmpty()) {
            return copiedFinancements;
        }

        for (Financement existing : existingFinancements) {
            // Create new FinancementId for new version
            Financement copiedFinancement = new Financement();
            FinancementId newFinancementId = new FinancementId();
            newFinancementId.setRealisation(newVersionRealisation);
            copiedFinancement.setMonnaie(existing.getMonnaie());

            // Generate new financement ID
            Integer nextFinancementId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ) + 1;
            newFinancementId.setFinancementId(nextFinancementId);

            // Create copied financement

            copiedFinancement.setId(newFinancementId);
            copiedFinancement.setBudgetEstimer(existing.getBudgetEstimer());
            copiedFinancement.setSourceFinancement(existing.getSourceFinancement());

            copiedFinancements.add(copiedFinancement);
        }

        return copiedFinancements;
    }



}