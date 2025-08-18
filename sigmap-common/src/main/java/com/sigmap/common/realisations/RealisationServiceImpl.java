package com.sigmap.common.realisations;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionRepository;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.config.Constants;
import com.sigmap.common.config.RealisationStatus;
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
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.util.*;
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
//            id = repository.findMaxId() + 1;
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

//        // Fetch and set RefParam associations
//        if (request.getService() != null) {
//            log.info("Fetching Service with ID: {}", request.getService().getId());
//            realisation.setService(refParamRepository.findById(request.getService().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Service not found")));
//        }
//        if (request.getDirection() != null) {
//            log.info("Fetching Direction with ID: {}", request.getDirection().getId());
//            realisation.setDirection(refParamRepository.findById(request.getDirection().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Direction not found")));
//        }
//        if (request.getSourceFinancement() != null) {
//            log.info("Fetching SourceFinancement with ID: {}", request.getSourceFinancement().getId());
//            realisation.setSourceFinancement(refParamRepository.findById(request.getSourceFinancement().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Source Financement not found")));
//        }

        // Handle MULTIPLE Financements creation after realisation is saved
//        if (request.getFinancementRequests() != null && !request.getFinancementRequests().isEmpty()) {
//            log.info("Creating {} Financements", request.getFinancementRequests().size());
//
//            List<Financement> createdFinancements = new ArrayList<>();
//
//            for (FinancementBaseRequestDto financementRequest : request.getFinancementRequests()) {
//                log.info("Processing Financement request: {}", financementRequest);
//
//                // Create FinancementId (composite key)
//                FinancementId financementId = new FinancementId();
////                financementId.setRealisation(savedRealisation);
//                Financement financement = new Financement();
//                // Set Monnaie from individual financement request
//                if (financementRequest.getMonnaieId() != null) {
//                    log.info("Fetching Monnaie with ID: {}", financementRequest.getMonnaieId());
//                    Monnaie monnaie = monnaieRepository.findById(financementRequest.getMonnaieId())
//                            .orElseThrow(() -> new EntityNotFoundException("Monnaie not found with ID: " + financementRequest.getMonnaieId()));
//                    // ADD THIS LOGIC HERE
//                    if (monnaie.getTaux() != null) {
//                        financement.setTauxApplicable(monnaie.getTaux());
//                        System.out.println("Auto-assigned tauxApplicable: " + monnaie.getTaux() + " from Monnaie ID: " + monnaie.getId());
//                    } else {
//                        financement.setTauxApplicable(0.0);
//                        System.out.println("Monnaie taux is null, set tauxApplicable to 0.0 for Monnaie ID: " + monnaie.getId());
//                    }
//                    financement.setMonnaie(monnaie);
//                } else {
//                    throw new IllegalArgumentException("Monnaie ID is required for each Financement");
//                }
//
//                // Generate unique financement ID for each financement
//                Integer nextFinancementId = sequenceGeneratorService.getNextId(Constants.FINANCEMENT_ID_SEQ) + 1;
//                financementId.setFinancementId(nextFinancementId);
//
//                log.info("Generated Financement ID: {}", nextFinancementId);
//
//                // Create Financement entity
//
//                financement.setId(financementId);
//                financement.setBudgetEstimer(financementRequest.getBudgetEstimer());
//
//                // Set source financement if provided
//                if (financementRequest.getSourceFinancement().getId() != null) {
//                    log.info("Fetching SourceFinancement with ID: {}", financementRequest.getSourceFinancement().getId());
//                    RefParam sourceFinancement = refParamRepository.findById(financementRequest.getSourceFinancement().getId())
//                            .orElseThrow(() -> new EntityNotFoundException("Source Financement not found with ID: " + financementRequest.getSourceFinancement().getId()));
//                    financement.setSourceFinancement(sourceFinancement);
//                }
//
//                // Save individual financement
//                Financement savedFinancement = financementRepository.save(financement);
//                log.info("Created and saved Financement: {}", savedFinancement);
//
//                createdFinancements.add(savedFinancement);
//            }
//
//            // Associate ALL created financements with the realisation (bidirectional)
//            realisation.setFinancements(createdFinancements);
//            log.info("Associated {} financements with realisation", createdFinancements.size());
//        }

        if (request.getImputationBudgetaire() != null) {
            log.info("Fetching ImputationBudgetaire with ID: {}", request.getImputationBudgetaire().getId());
            realisation.setImputationBudgetaire(refParamRepository.findById(request.getImputationBudgetaire().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Imputation Budgetaire not found")));
        }
        if (request.getBeneficiaire() != null) {
       realisation.setBeneficiaire(request.getBeneficiaire());
        }
        // Similar logging for monais, typeMarcher, modeSelection

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
//                    Integer lotId = lotRepository.findMaxId() + 1;
                    Integer lotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ) + 1;
                    newLot.setId(lotId);
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

                // Set Monnaie from individual financement request
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

//                financement.setRealisation(savedRealisation);

                // Save individual financement
                Financement savedFinancement = financementRepository.save(financement);
                log.info("Created and saved Financement: {}", savedFinancement);

                createdFinancements.add(savedFinancement);
            }

//            savedRealisation.setStatut(RealisationStatus.NON_PUBLIE);
            savedRealisation.setFinancements(createdFinancements);
            savedRealisation = repository.save(savedRealisation);
            log.info("Updated Realisation with {} financements", createdFinancements.size());
        }

        // Save entity with all associations
//        Realisation savedRealisation = repository.save(realisation);
//        log.info("Saved Realisation: {}", savedRealisation);

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

//        // Update RefParam associations if provided
//        if (request.getService() != null) {
//            existingRealisation.setService(refParamRepository.findById(request.getService().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Service not found")));
//        }
//        if (request.getDirection() != null) {
//            existingRealisation.setDirection(refParamRepository.findById(request.getDirection().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Direction not found")));
//        }
//        if (request.getSourceFinancement() != null) {
//            existingRealisation.setSourceFinancement(refParamRepository.findById(request.getSourceFinancement().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Source Financement not found")));
//        }
        if (request.getImputationBudgetaire() != null) {
            existingRealisation.setImputationBudgetaire(refParamRepository.findById(request.getImputationBudgetaire().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Imputation Budgetaire not found")));
        }
//        if (request.getMonais() != null) {
//            existingRealisation.setMonais(refParamRepository.findById(request.getMonais().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Monais not found")));
//        }
//        if (request.getMonnaie() != null) {
//            existingRealisation.setMonnaie(monnaieRepository.findById(request.getMonnaie().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Monais not found")));
//        }
        if (request.getBeneficiaire() != null) {
            existingRealisation.setBeneficiaire(request.getBeneficiaire());
        }
        if (request.getTypeMarcher() != null) {
            existingRealisation.setTypeMarcher(refParamRepository.findById(request.getTypeMarcher().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Type Marcher not found")));
        }
//        if (request.getModeSelection() != null) {
//            existingRealisation.setModeSelection(refParamRepository.findById(request.getModeSelection().getId())
//                    .orElseThrow(() -> new EntityNotFoundException("Mode Selection not found")));
//        }

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
            // Keep the existing lots set to preserve relationships (just all marked as deleted)
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
public Realisation updateWithVersioning(Integer realisationId, Integer realisationVer, RealisationBaseRequestDto request) throws RealisationNotFoundException {
    // Find the existing realisation
    CompositeId compositeId = new CompositeId(realisationId, realisationVer);
    Optional<Realisation> existingRealisationOpt = repository.findById(compositeId);

    if (existingRealisationOpt.isEmpty()) {
        throw new RealisationNotFoundException(
                "Realisation with ID " + realisationId + " and version " + realisationVer + " was not found in the database.",
                realisationId, realisationVer);
    }

    Realisation existingRealisation = existingRealisationOpt.get();
    if (Boolean.TRUE.equals(existingRealisation.getIsDeleted())) {
        throw new RealisationNotFoundException(
                "Cannot update a deleted Realisation with ID " + realisationId + " and version " + realisationVer,
                realisationId, realisationVer);
    }

    // Log the current state and request
    System.out.println("Updating Realisation ID: " + realisationId + ", Current Version: " + realisationVer);
    System.out.println("Request data: " + request);
    System.out.println("Current realisationEnvisagees: " + existingRealisation.getRealisationEnvisagees());
    System.out.println("New realisationEnvisagees: " + request.getRealisationEnvisagees());
    System.out.println("Current Beneficiaire: " + existingRealisation.getBeneficiaire());
    System.out.println("new Beneficiaire: " + request.getBeneficiaire());
    System.out.println("Current budgetEstime: " + existingRealisation.getBudgetEstime());
    System.out.println("New budgetEstime: " + request.getBudgetEstime());

    // Check for Lot changes - if lots are provided, consider it a change
    boolean lotChangesDetected = request.getLots() != null && !request.getLots().isEmpty();

    // Create a temporary Realisation to check for changes
    Realisation tempRealisation = new Realisation();

    // Set all fields from request or existing realisation
    tempRealisation.setRealisationEnvisagees(request.getRealisationEnvisagees() != null ?
            request.getRealisationEnvisagees() : existingRealisation.getRealisationEnvisagees());

    tempRealisation.setBeneficiaire(request.getBeneficiaire() != null ?
            request.getBeneficiaire() : existingRealisation.getBeneficiaire());

//    // Handle RefParam fields
//    RefParam serviceParam = null;
//    if (request.getService() != null) {
//        serviceParam = refParamRepository.findById(request.getService().getId())
//                .orElse(existingRealisation.getService());
//    } else {
//        serviceParam = existingRealisation.getService();
//    }
//    tempRealisation.setService(serviceParam);
//
//    RefParam directionParam = null;
//    if (request.getDirection() != null) {
//        directionParam = refParamRepository.findById(request.getDirection().getId())
//                .orElse(existingRealisation.getDirection());
//    } else {
//        directionParam = existingRealisation.getDirection();
//    }
//    tempRealisation.setDirection(directionParam);

//    RefParam sourceFinancementParam = null;
//    if (request.getSourceFinancement() != null) {
//        sourceFinancementParam = refParamRepository.findById(request.getSourceFinancement().getId())
//                .orElse(existingRealisation.getSourceFinancement());
//    } else {
//        sourceFinancementParam = existingRealisation.getSourceFinancement();
//    }
//    tempRealisation.setSourceFinancement(sourceFinancementParam);

    RefParam imputationBudgetaireParam = null;
    if (request.getImputationBudgetaire() != null) {
        imputationBudgetaireParam = refParamRepository.findById(request.getImputationBudgetaire().getId())
                .orElse(existingRealisation.getImputationBudgetaire());
    } else {
        imputationBudgetaireParam = existingRealisation.getImputationBudgetaire();
    }
    tempRealisation.setImputationBudgetaire(imputationBudgetaireParam);

//    RefParam monaisParam = null;
//    if (request.getMonais() != null) {
//        monaisParam = refParamRepository.findById(request.getMonais().getId())
//                .orElse(existingRealisation.getMonais());
//    } else {
//        monaisParam = existingRealisation.getMonais();
//    }
//    tempRealisation.setMonais(monaisParam);

//    Monnaie monaisParam = null;
//    if (request.getMonnaie() != null) {
//        log.info("*************************monnaie :" + request.getMonnaie());
//                monaisParam = monnaieRepository.findById(request.getMonnaie().getId())
//                .orElse(existingRealisation.getMonnaie());
//    } else {
//        monaisParam = existingRealisation.getMonnaie();
//    }
//    tempRealisation.setMonnaie(monaisParam);

    RefParam typeMarcherParam = null;
    if (request.getTypeMarcher() != null) {
        typeMarcherParam = refParamRepository.findById(request.getTypeMarcher().getId())
                .orElse(existingRealisation.getTypeMarcher());
    } else {
        typeMarcherParam = existingRealisation.getTypeMarcher();
    }
    tempRealisation.setTypeMarcher(typeMarcherParam);

//    RefParam modeSelectionParam = null;
//    if (request.getModeSelection() != null) {
//        modeSelectionParam = refParamRepository.findById(request.getModeSelection().getId())
//                .orElse(existingRealisation.getModeSelection());
//    } else {
//        modeSelectionParam = existingRealisation.getModeSelection();
//    }
//    tempRealisation.setModeSelection(modeSelectionParam);

    ModeDePassationSelection modePassationParam = null;
    if (request.getModePassation() != null) {
        modePassationParam = modeDePassationSelectionRepository.findById(request.getModePassation().getId())
                .orElse(existingRealisation.getModePassation());
    } else {
        modePassationParam = existingRealisation.getModePassation();
    }
    tempRealisation.setModePassation(modePassationParam);

    // FINANCEMENT HANDLING HERE:
// Handle financements - create temporary list for comparison
    List<Financement> tempFinancements = new ArrayList<>();

    if (request.getFinancements() != null && !request.getFinancements().isEmpty()) {
        for (FinancementRequest financementUpdate : request.getFinancements()) {
            // Create temporary financement for comparison purposes only
            Financement tempFinancement = new Financement();

            // Set basic properties for comparison
            if (financementUpdate.getBudgetEstimer() != null) {
                tempFinancement.setBudgetEstimer(financementUpdate.getBudgetEstimer());
            }

            // Set monnaie for comparison
            if (financementUpdate.getMonnaieId() != null) {
                Monnaie monnaie = monnaieRepository.findById(financementUpdate.getMonnaieId()).orElse(null);

                if (monnaie != null) {
                    FinancementId tempId = new FinancementId();
                    tempFinancement.setMonnaie(monnaie);
                    tempFinancement.setId(tempId);
                }
            }

            // Set source financement for comparison
            if (financementUpdate.getSourceFinancement() != null && financementUpdate.getSourceFinancement().getId() != null) {
                RefParam sourceFinancement = refParamRepository.findById(financementUpdate.getSourceFinancement().getId()).orElse(null);
                tempFinancement.setSourceFinancement(sourceFinancement);
            }

            tempFinancements.add(tempFinancement);
        }
    }
    tempRealisation.setFinancements(tempFinancements);

    // Set other properties
    tempRealisation.setDatePrevLps(request.getDatePrevLps() != null ?
            request.getDatePrevLps() : existingRealisation.getDatePrevLps());
    tempRealisation.setDatePrevAc(request.getDatePrevAc() != null ?
            request.getDatePrevAc() : existingRealisation.getDatePrevAc());
    tempRealisation.setDatePrevDp(request.getDatePrevDp() != null ?
            request.getDatePrevDp() : existingRealisation.getDatePrevDp());
    tempRealisation.setDatePrevAp(request.getDatePrevAp() != null ?
            request.getDatePrevAp() : existingRealisation.getDatePrevAp());
    tempRealisation.setBudgetEstime(request.getBudgetEstime() != null ?
            request.getBudgetEstime() : existingRealisation.getBudgetEstime());
    tempRealisation.setIsPublished(request.getIsPublished() != null ?
            request.getIsPublished() : existingRealisation.getIsPublished());
    tempRealisation.setPublishedAt(request.getPublishedAt() != null ?
            request.getPublishedAt() : existingRealisation.getPublishedAt());
    tempRealisation.setSort(request.getSort() != null ?
            request.getSort() : existingRealisation.getSort());

    // Track document changes
    boolean documentsChanged = false;
    List<Docs> updatedDocs = new ArrayList<>();

    // Handle document management by comparing existing docs with requested docIds
    if (request.getDocIds() != null) {
        Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());

        // Get only the active (non-deleted) existing document IDs
        Set<Integer> activeExistingDocIds = existingRealisation.getDocs() != null ?
                existingRealisation.getDocs().stream()
                        .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                        .map(Docs::getId)
                        .collect(Collectors.toSet()) : new HashSet<>();

        // Check if there are document changes between active docs and request
        documentsChanged = !requestedDocIds.equals(activeExistingDocIds);

        // Log document changes for clarity
        if (documentsChanged) {
            System.out.println("Document changes detected for Realisation ID: " + realisationId);

            // Find new documents (in request but not existing)
            Set<Integer> newDocIds = new HashSet<>(requestedDocIds);
            newDocIds.removeAll(activeExistingDocIds);
            if (!newDocIds.isEmpty()) {
                System.out.println("  - New documents added: " + newDocIds);
            }

            // Find removed documents (existing but not in request)
            Set<Integer> removedDocIds = new HashSet<>(activeExistingDocIds);
            removedDocIds.removeAll(requestedDocIds);
            if (!removedDocIds.isEmpty()) {
                System.out.println("  - Documents marked as deleted: " + removedDocIds);
            }
        }

        // If preparing for a new version, prepare the document collection
        if (documentsChanged) {
            // Start with all existing documents (both active and deleted)
            updatedDocs = new ArrayList<>(existingRealisation.getDocs());

            // Process each existing document
            for (Docs doc : updatedDocs) {
                if (requestedDocIds.contains(doc.getId())) {
                    // Document is in the request, ensure it's not marked as deleted
                    if (Boolean.TRUE.equals(doc.getIsDeleted())) {
                        doc.setIsDeleted(false);
                        docsRepository.save(doc);
                        System.out.println("Activated document with ID " + doc.getId() + " for new Realisation version");
                        documentsChanged = true;
                    }
                    // Remove from request since we've handled it
                    requestedDocIds.remove(doc.getId());
                } else {
                    // Document is not in the request, mark it as deleted
                    if (!Boolean.TRUE.equals(doc.getIsDeleted())) {
                        doc.setIsDeleted(true);
                        docsRepository.save(doc);
                        System.out.println("Marked document with ID " + doc.getId() + " as deleted (not in request)");
                        documentsChanged = true;
                    }
                }
            }

            // Add any new documents from the request
            for (Integer docId : requestedDocIds) {
                try {
                    Docs doc = docsRepository.findById(docId)
                            .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));

                    // Set up bidirectional relationship if this is a new document for this realisation
                    if (!updatedDocs.contains(doc)) {
                        if (doc.getRealisations() == null) {
                            doc.setRealisations(new ArrayList<>());
                        }

                        // Ensure it's active
                        doc.setIsDeleted(false);
                        docsRepository.save(doc);
                        updatedDocs.add(doc);
                        System.out.println("Added new document with ID " + doc.getId() + " for new Realisation version");
                        documentsChanged = true;
                    }
                } catch (Exception e) {
                    System.err.println("Error processing document ID " + docId + ": " + e.getMessage());
                    continue;
                }
            }
        } else {
            // No existing docs, just add the requested ones
            for (Integer docId : requestedDocIds) {
                try {
                    Docs doc = docsRepository.findById(docId)
                            .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));

                    if (doc.getRealisations() == null) {
                        doc.setRealisations(new ArrayList<>());
                    }

                    doc.setIsDeleted(false);
                    docsRepository.save(doc);
                    updatedDocs.add(doc);
                    System.out.println("Added new document with ID " + doc.getId() + " for new Realisation version");
                } catch (Exception e) {
                    System.err.println("Error processing document ID " + docId + ": " + e.getMessage());
                    continue;
                }
            }
            documentsChanged = !requestedDocIds.isEmpty();
        }
    } else if (existingRealisation.getDocs() != null && !existingRealisation.getDocs().isEmpty()) {
        // No document IDs provided, but existing docs exist - mark all as deleted
        updatedDocs = new ArrayList<>(existingRealisation.getDocs());
        boolean anyActive = false;
        for (Docs doc : updatedDocs) {
            if (!Boolean.TRUE.equals(doc.getIsDeleted())) {
                doc.setIsDeleted(true);
                docsRepository.save(doc);
                anyActive = true;
            }
        }
        // Only consider this a change if we had to mark any active docs as deleted
        if (anyActive) {
            documentsChanged = true;
            System.out.println("All documents marked as deleted (empty docIds in request)");
        }
    } else {
        // No docs in request or existing realisation
        updatedDocs = new ArrayList<>();
    }

    // Check for Financement changes - ADD THIS SECTION
    boolean financementChangesDetected = checkFinancementChanges(
            existingRealisation.getFinancements(),
            request.getFinancements()
    );

    System.out.println("Financement changes detected: " + financementChangesDetected);

    // Check if there are actual changes
    boolean hasChanges = hasChanges(tempRealisation, existingRealisation, request.getDocIds()) ||
            lotChangesDetected || documentsChanged | financementChangesDetected;

    System.out.println("Has changes: " + hasChanges);
    System.out.println("  - Field changes: " + hasChanges(tempRealisation, existingRealisation, request.getDocIds()));
    System.out.println("  - Lot changes: " + lotChangesDetected);
    System.out.println("  - Document changes: " + documentsChanged);
    System.out.println("  - Financement changes: " + financementChangesDetected);
    if (!hasChanges) {
        System.out.println("No changes detected, returning existing version: " + realisationVer);

        // Create a filtered copy for response even when returning existing version
        Realisation copyForResponse = new Realisation();
        BeanUtils.copyProperties(existingRealisation, copyForResponse);

        // Filter out deleted documents for the response
        if (existingRealisation.getDocs() != null) {
            copyForResponse.setDocs(
                    existingRealisation.getDocs().stream()
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

    // If there are changes, create a new version
    System.out.println("Changes detected, creating new version: " + (realisationVer + 1));

    Realisation newRealisation = new Realisation();
    newRealisation.setId(realisationId);
    newRealisation.setVersion(realisationVer + 1);

    // Copy all properties from the temp realisation
    newRealisation.setRealisationEnvisagees(tempRealisation.getRealisationEnvisagees());
    newRealisation.setBeneficiaire(tempRealisation.getBeneficiaire());
//    newRealisation.setService(tempRealisation.getService());
//    newRealisation.setDirection(tempRealisation.getDirection());
//    newRealisation.setSourceFinancement(tempRealisation.getSourceFinancement());
    newRealisation.setImputationBudgetaire(tempRealisation.getImputationBudgetaire());
//    newRealisation.setMonais(tempRealisation.getMonais());
    newRealisation.setMonnaie(tempRealisation.getMonnaie());
    newRealisation.setTypeMarcher(tempRealisation.getTypeMarcher());
//    newRealisation.setModeSelection(tempRealisation.getModeSelection());
    newRealisation.setModePassation(tempRealisation.getModePassation());
    newRealisation.setDatePrevLps(tempRealisation.getDatePrevLps());
    newRealisation.setDatePrevAc(tempRealisation.getDatePrevAc());
    newRealisation.setDatePrevDp(tempRealisation.getDatePrevDp());
    newRealisation.setDatePrevAp(tempRealisation.getDatePrevAp());
    newRealisation.setBudgetEstime(tempRealisation.getBudgetEstime());
    newRealisation.setIsPublished(tempRealisation.getIsPublished());
    newRealisation.setPublishedAt(tempRealisation.getPublishedAt());
    newRealisation.setSort(tempRealisation.getSort());

    // Initialize empty collections to avoid null pointer exceptions
    newRealisation.setPaas(new ArrayList<>());
    newRealisation.setPpms(new ArrayList<>());
    newRealisation.setDocs(new ArrayList<>());
    newRealisation.setLots(new ArrayList<>());
    newRealisation.setFinancements(new ArrayList<>());
    // IMPORTANT CHANGE: Save the new realisation FIRST to ensure it exists in the database
    // before establishing any relationships
    System.out.println("Saving new Realisation entity to database before setting up relationships");
    // Create a final reference to newRealisation that can be used in lambda expressions
    final Realisation finalNewRealisation = repository.save(newRealisation);

    // Now it's safe to set up relationships
    System.out.println("Setting up relationships for persisted Realisation entity");

    // Copy relationships
    if (existingRealisation.getPaas() != null && !existingRealisation.getPaas().isEmpty()) {
        System.out.println("Copying PAA relationships to new Realisation version");
        List<Paa> paas = new ArrayList<>();

        // For each PAA, ensure we use the latest version
        for (Paa existingPaa : existingRealisation.getPaas()) {
            try {
                // Find the latest version of this PAA
                Integer latestPaaVersion = paaRepository.findMaxVersionById(existingPaa.getId());
                if (latestPaaVersion == null) {
                    System.err.println("Warning: No versions found for PAA ID " + existingPaa.getId());
                    // Use the existing version as fallback
                    latestPaaVersion = existingPaa.getVersion();
                }

                // Retrieve the latest PAA version
                Paa latestPaa = paaRepository.findById(new CompositeId(existingPaa.getId(), latestPaaVersion))
                        .orElse(null);

                if (latestPaa == null) {
                    System.err.println("Warning: Latest PAA " + existingPaa.getId() +
                            " version " + latestPaaVersion +
                            " not found in database. Using existing version.");
                    paas.add(existingPaa); // Use existing as fallback
                } else {
                    paas.add(latestPaa);

                    // Update the PAA's relationship to the new Realisation version
                    if (latestPaa.getRealisations() == null) {
                        latestPaa.setRealisations(new ArrayList<>());
                    }

                    // Check if we need to update the PAA's realisations list
                    boolean alreadyContainsNewRealisation = latestPaa.getRealisations().stream()
                            .anyMatch(r -> r.getId().equals(finalNewRealisation.getId()) &&
                                    r.getVersion().equals(finalNewRealisation.getVersion()));

                    if (!alreadyContainsNewRealisation) {
                        // Add the new realisation version to the PAA
                        latestPaa.getRealisations().add(finalNewRealisation);
                        paaRepository.save(latestPaa);
                        System.out.println("Updated PAA " + latestPaa.getId() +
                                " version " + latestPaa.getVersion() +
                                " to reference new Realisation version");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error handling PAA relationship: " + e.getMessage());
                paas.add(existingPaa); // Use existing as fallback
            }
        }

        finalNewRealisation.setPaas(paas);
        System.out.println("Associated " + paas.size() + " PAAs with new Realisation version");
    }

    // Copy relationships with PPMs
    if (existingRealisation.getPpms() != null) {
        finalNewRealisation.setPpms(new ArrayList<>(existingRealisation.getPpms()));
        System.out.println("Copied " + existingRealisation.getPpms().size() + " PPM relationships");
    }

    // Handle Lots - create/update lots properly
    List<Lot> newLots = new ArrayList<>();
    Map<String, Lot> newLotByDescription = new HashMap<>(); // Track new lots by description

    // Process new or updated lots from request - only add the lots explicitly in the request
    Set<Integer> processedLotIds = new HashSet<>();
    if (request.getLots() != null && !request.getLots().isEmpty()) {
        System.out.println("Processing " + request.getLots().size() + " lots from request");

        for (LotResponse lotDto : request.getLots()) {
            String lotDescription = lotDto.getDescription() != null ? lotDto.getDescription() : "";
            if (lotDto.getId() == null) {
                // Create new Lot
                if (newLotByDescription.containsKey(lotDescription)) {
                    System.out.println("Using existing new lot for description: " + lotDescription);
                    newLots.add(newLotByDescription.get(lotDescription));
                    processedLotIds.add(newLotByDescription.get(lotDescription).getId());
                } else {
                    Lot newLot = new Lot();
                    newLot.setDescription(lotDescription);

                    // Generate new ID
                    Integer lotId = sequenceGeneratorService.getNextId(Constants.LOT_ID_SEQ) + 1;
                    newLot.setId(lotId);
                    newLot.setVersion(1); // First version

                    // Save the Lot
                    try {
                        Lot savedLot = lotRepository.save(newLot);
                        newLots.add(savedLot);
                        processedLotIds.add(savedLot.getId());
                        newLotByDescription.put(lotDescription, savedLot);
                        System.out.println("Created new Lot with ID: " + savedLot.getId());
                    } catch (Exception e) {
                        System.err.println("Error saving new lot: " + e.getMessage());
                        continue;
                    }
                }
            } else {
                // Skip if we've already processed a lot with this ID
                if (processedLotIds.contains(lotDto.getId())) {
                    System.out.println("Skipping duplicate lot ID: " + lotDto.getId());
                    continue;
                }

                // Process existing Lot - check if description changed
                Integer lotId = lotDto.getId();
                Integer lotVersion = lotDto.getVersion();

                // If version not specified, find latest version
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

                    // Check if the description changed
                    if (lotDescription != null && !lotDescription.equals(existingLot.getDescription())) {
                        // Create a new version of the Lot
                        Lot newLot = new Lot();
                        newLot.setId(existingLot.getId());
                        newLot.setVersion(existingLot.getVersion() + 1);
                        newLot.setDescription(lotDescription);

                        // Save the new version
                        try {
                            Lot savedLot = lotRepository.save(newLot);
                            newLots.add(savedLot);
                            processedLotIds.add(savedLot.getId());
                            System.out.println("Created new version of Lot: " + savedLot.getId() + ", Version: " + savedLot.getVersion());
                        } catch (Exception e) {
                            System.err.println("Error saving new lot version: " + e.getMessage());
                            continue;
                        }
                    } else {
                        // No changes, use existing lot
                        newLots.add(existingLot);
                        processedLotIds.add(existingLot.getId());
                    }
                }
            }
        }

        System.out.println("Final lot count: " + newLots.size());
    } else {
        // If no lots provided, use empty set (remove all previous lots)
        System.out.println("No lots provided in request, using empty set");
    }

    // Handle existing lots not in the request - mark them as deleted
    if (existingRealisation.getLots() != null) {
        for (Lot existingLot : existingRealisation.getLots()) {
            if (!processedLotIds.contains(existingLot.getId())) {
                // Lot not included in request, mark as deleted
                existingLot.setIsDeleted(true);
                try {
                    lotRepository.save(existingLot);
                    System.out.println("Marked Lot ID " + existingLot.getId() + " as deleted");

                    // Add to the new lots collection to maintain association
                    newLots.add(existingLot);
                } catch (Exception e) {
                    System.err.println("Error marking lot as deleted: " + e.getMessage());
                }
            }
        }
    }

    // Set the new/updated lots on the new Realisation
    finalNewRealisation.setLots(newLots);

    // ADD THIS SECTION - This is likely what's missing:
// Handle Financements for new version
    List<Financement> newFinancements = new ArrayList<>();

    if (financementChangesDetected) {
        System.out.println("Creating updated financements for new version");
        newFinancements = createUpdatedFinancements(
                existingRealisation.getFinancements(),
                request.getFinancements(),
                finalNewRealisation
        );
        log.info("Created {} updated financements", newFinancements.size());
    } else if (existingRealisation.getFinancements() != null && !existingRealisation.getFinancements().isEmpty()) {
        System.out.println("Copying existing financements to new version");
        newFinancements = copyFinancementsToNewVersion(
                existingRealisation.getFinancements(),
                finalNewRealisation
        );
        log.info("Copied {} existing financements", newFinancements.size());
    }
    finalNewRealisation.setFinancements(newFinancements);
   log.info("Associated {} financements with new Realisation version", newFinancements.size());
    // Handle documents - establish bidirectional relationships
    if (!updatedDocs.isEmpty()) {
        System.out.println("Setting up document relationships for new Realisation version");
        List<Docs> confirmedDocs = new ArrayList<>();

        for (Docs doc : updatedDocs) {
            try {
                // Verify the document still exists in the database
                Docs confirmedDoc = docsRepository.findById(doc.getId()).orElse(null);
                if (confirmedDoc == null) {
                    System.err.println("Warning: Document " + doc.getId() + " not found in database. Skipping.");
                    continue;
                }

                // Update the bidirectional relationship
                List<Realisation> realisationList = confirmedDoc.getRealisations();
                if (realisationList == null) {
                    realisationList = new ArrayList<>();
                    confirmedDoc.setRealisations(realisationList);
                }

                // Add the new realisation if not already present
                if (realisationList.stream().noneMatch(r ->
                        r.getId().equals(finalNewRealisation.getId()) && r.getVersion().equals(finalNewRealisation.getVersion()))) {
                    realisationList.add(finalNewRealisation);
                    confirmedDoc = docsRepository.save(confirmedDoc);
                    System.out.println("Updated Document " + confirmedDoc.getId() + " to reference new Realisation version");
                }

                confirmedDocs.add(confirmedDoc);
            } catch (Exception e) {
                System.err.println("Error setting up document relationship: " + e.getMessage());
                e.printStackTrace();
            }
        }

        finalNewRealisation.setDocs(confirmedDocs);
        System.out.println("Associated " + confirmedDocs.size() + " documents with new Realisation version");
    } else if (existingRealisation.getDocs() != null && !existingRealisation.getDocs().isEmpty()) {
        System.out.println("Copying " + existingRealisation.getDocs().size() + " document relationships from existing Realisation");
        List<Docs> confirmedDocs = new ArrayList<>();

        for (Docs existingDoc : existingRealisation.getDocs()) {
            try {
                // Verify the document still exists
                Docs confirmedDoc = docsRepository.findById(existingDoc.getId()).orElse(null);
                if (confirmedDoc == null) {
                    System.err.println("Warning: Document " + existingDoc.getId() + " not found. Skipping.");
                    continue;
                }

                // Update the bidirectional relationship
                List<Realisation> realisationList = confirmedDoc.getRealisations();
                if (realisationList == null) {
                    realisationList = new ArrayList<>();
                    confirmedDoc.setRealisations(realisationList);
                }

                // Add the new realisation if not already present
                if (realisationList.stream().noneMatch(r ->
                        r.getId().equals(finalNewRealisation.getId()) && r.getVersion().equals(finalNewRealisation.getVersion()))) {
                    realisationList.add(finalNewRealisation);
                    confirmedDoc = docsRepository.save(confirmedDoc);
                    System.out.println("Updated Document " + confirmedDoc.getId() + " to reference new Realisation version");
                }

                confirmedDocs.add(confirmedDoc);
            } catch (Exception e) {
                System.err.println("Error handling document relationship: " + e.getMessage());
                e.printStackTrace();
            }
        }

        finalNewRealisation.setDocs(confirmedDocs);
        System.out.println("Associated " + confirmedDocs.size() + " documents with new Realisation version");
    }

    // Final save after setting up all relationships
    Realisation updatedRealisation = repository.save(finalNewRealisation);
    System.out.println("Final save of Realisation with all relationships: ID=" + updatedRealisation.getId() + ", Version=" + updatedRealisation.getVersion() + ", BudgetEstime=" + updatedRealisation.getBudgetEstime());

    // Create a copy for the response with only non-deleted documents
    Realisation copyForResponse = new Realisation();
    BeanUtils.copyProperties(updatedRealisation, copyForResponse);

    // Filter out deleted documents for the response
    if (updatedRealisation.getDocs() != null) {
        copyForResponse.setDocs(
                updatedRealisation.getDocs().stream()
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

        // Check budgetEstime
        if (!Objects.equals(tempRealisation.getBudgetEstime(), existingRealisation.getBudgetEstime())) {
            System.out.println("Change detected in budgetEstime: " + existingRealisation.getBudgetEstime() + " -> " + tempRealisation.getBudgetEstime());
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

        // Check sort
        if (!Objects.equals(tempRealisation.getSort(), existingRealisation.getSort())) {
            System.out.println("Change detected in sort");
            return true;
        }

        System.out.println("No field changes detected for Realisation ID: " + existingRealisation.getId());
        return false;
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
//        Map<Integer, Financement> existingByMonnaie = existingFinancements.stream()
//                .collect(Collectors.toMap(f -> f.getId().getMonnaie().getId(), f -> f));
        Map<Integer, Financement> existingByMonnaie = existingFinancements.stream()
                .collect(Collectors.toMap(
                        f -> f.getMonnaie().getId(),
                        f -> f,
                        (existing, replacement) -> replacement // ← Keep the newer one
                ));

        Map<Integer, FinancementRequest> updatesByMonnaie = updates.stream()
                .collect(Collectors.toMap(FinancementRequest::getMonnaieId, f -> f));

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