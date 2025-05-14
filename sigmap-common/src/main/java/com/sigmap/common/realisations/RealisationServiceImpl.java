package com.sigmap.common.realisations;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.Constants;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.lots.response.LotResponse;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    private final AlfrescoService alfrescoService;
    private final DocumentCommuneService documentCommuneService;
    private final DocsService docsService;
    private final AcService acService;

    private final DocsRepository docsRepository;



    public RealisationServiceImpl(RealisationRepository repository, PaaRepository paaRepository, PpmRepository ppmRepository, LotRepository lotRepository, RefParamRepository refParamRepository, AlfrescoService alfrescoService, DocumentCommuneService documentCommuneService, DocsService docsService, AcService acService, DocsRepository docsRepository) {
        this.repository = repository;
        this.paaRepository = paaRepository;
        this.ppmRepository = ppmRepository;
        this.lotRepository = lotRepository;
        this.refParamRepository = refParamRepository;
        this.alfrescoService = alfrescoService;
        this.documentCommuneService = documentCommuneService;
        this.docsService = docsService;
        this.acService = acService;
        this.docsRepository = docsRepository;
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
        Integer id = request.getId();
        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        // Generate ID if not provided
        if (id == null) {
            id = repository.findMaxId() + 1;
            System.out.println("Generated Realisation ID: " + id);
        }

        CompositeId compositeId = new CompositeId(id, version);

        // Check if realisation already exists
        if (repository.existsById(compositeId)) {
            throw new RealisationAlreadyExistsException(
                    "Realisation with ID " + id + " and version " + version + " already exists in the database.",List.of(id), List.of(version));
        }

        // Map request to entity
        Realisation realisation = RealisationMapper.INSTANCE.toEntity(request);
        realisation.setId(compositeId.getId());
        realisation.setVersion(version);

        // Set empty associations initially
        realisation.setPaas(new ArrayList<>());
        realisation.setPpms(new ArrayList<>());
        realisation.setLots(new HashSet<>());
        realisation.setDocs(new ArrayList<>());

        // Fetch and set RefParam associations
        if (request.getService() != null) {
            realisation.setService(refParamRepository.findById(request.getService().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found")));
        }
        if (request.getDirection() != null) {
            realisation.setDirection(refParamRepository.findById(request.getDirection().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Direction not found")));
        }
        if (request.getSourceFinancement() != null) {
            realisation.setSourceFinancement(refParamRepository.findById(request.getSourceFinancement().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Source Financement not found")));
        }
        if (request.getMonais() != null) {
            realisation.setMonais(refParamRepository.findById(request.getMonais().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Monais not found")));
        }
        if (request.getTypeMarcher() != null) {
            realisation.setTypeMarcher(refParamRepository.findById(request.getTypeMarcher().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Type Marcher not found")));
        }
        if (request.getModeSelection() != null) {
            realisation.setModeSelection(refParamRepository.findById(request.getModeSelection().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Mode Selection not found")));
        }

        // Save entity first (without associations)
        Realisation savedRealisation = repository.save(realisation);

        // Fetch associated PAAs if needed
        if (request.getPaaIds() != null && !request.getPaaIds().isEmpty()) {
            List<Paa> paas = request.getPaaIds().stream()
                    .peek(paaId -> System.out.println("Associating PAA ID: " + paaId.getId() + ", Version: " + paaId.getVersion()))
                    .map(paaId -> paaRepository.findById(new CompositeId(paaId.getId(), paaId.getVersion())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            savedRealisation.setPaas(paas);
        }

        // Fetch and set PPMs
        if (request.getPpmIds() != null) {
            List<Ppm> ppms = request.getPpmIds().stream()
                    .map(ppmId -> ppmRepository.findById(new CompositeId(ppmId.getId(), ppmId.getVersion())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            savedRealisation.setPpms(ppms);
        }

        // Handle Lots - both existing and new ones
        if (request.getLots() != null && !request.getLots().isEmpty()) {
            Set<Lot> lots = new HashSet<>();

            for (LotResponse lotResponse : request.getLots()) {
                if (lotResponse.getId() != null) {
                    // Existing lot - fetch it
                    lotRepository.findById(new CompositeId(lotResponse.getId(), lotResponse.getVersion()))
                            .ifPresent(lots::add);
                } else {
                    // New lot - create it
                    Lot newLot = new Lot();
                    newLot.setDescription(lotResponse.getDescription());
                    newLot.setVersion(1); // First version

                    // Generate new ID if not provided
                    Integer lotId = lotRepository.findMaxId() + 1;
                    newLot.setId(lotId);

                    // Save the new lot
                    Lot savedLot = lotRepository.save(newLot);
                    lots.add(savedLot);
                }
            }

            savedRealisation.setLots(lots);
        }

        ///TODO:add DOCs

        // Save again with associations
        return repository.save(savedRealisation);
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

        // Update RefParam associations if provided
        if (request.getService() != null) {
            existingRealisation.setService(refParamRepository.findById(request.getService().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found")));
        }
        if (request.getDirection() != null) {
            existingRealisation.setDirection(refParamRepository.findById(request.getDirection().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Direction not found")));
        }
        if (request.getSourceFinancement() != null) {
            existingRealisation.setSourceFinancement(refParamRepository.findById(request.getSourceFinancement().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Source Financement not found")));
        }
        if (request.getMonais() != null) {
            existingRealisation.setMonais(refParamRepository.findById(request.getMonais().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Monais not found")));
        }
        if (request.getTypeMarcher() != null) {
            existingRealisation.setTypeMarcher(refParamRepository.findById(request.getTypeMarcher().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Type Marcher not found")));
        }
        if (request.getModeSelection() != null) {
            existingRealisation.setModeSelection(refParamRepository.findById(request.getModeSelection().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Mode Selection not found")));
        }

        Realisation realisation = RealisationMapper.INSTANCE.updateRealisationFromRequest(request, existingRealisation);
        return repository.save(realisation);
    }

//    @Transactional
//    public Realisation updateWithVersioning(Integer realisationId, Integer realisationVer, RealisationBaseRequestDto request) throws RealisationNotFoundException {
//        // Find the existing realisation
//        CompositeId compositeId = new CompositeId(realisationId, realisationVer);
//        Optional<Realisation> existingRealisationOpt = repository.findById(compositeId);
//
//        if (existingRealisationOpt.isEmpty()) {
//            throw new RealisationNotFoundException(realisationId, realisationVer,
//                    "Realisation with ID " + realisationId + " and version " + realisationVer + " was not found in the database.");
//        }
//
//        Realisation existingRealisation = existingRealisationOpt.get();
//        if (Boolean.TRUE.equals(existingRealisation.getIsDeleted())) {
//            throw new RealisationNotFoundException(realisationId, realisationVer,
//                    "Cannot update a deleted Realisation with ID " + realisationId + " and version " + realisationVer);
//        }
//
//        // Log the current state and request
//        System.out.println("Updating Realisation ID: " + realisationId + ", Current Version: " + realisationVer);
//        System.out.println("Request data: " + request);
//        System.out.println("Current realisationEnvisagees: " + existingRealisation.getRealisationEnvisagees());
//        System.out.println("New realisationEnvisagees: " + request.getRealisationEnvisagees());
//
//        // Check for Lot changes - if lots are provided, consider it a change
//        boolean lotChangesDetected = request.getLots() != null && !request.getLots().isEmpty();
//
//        // Create a temporary Realisation to check for changes
//        Realisation tempRealisation = new Realisation();
//
//        // Set all fields from request or existing realisation
//        tempRealisation.setRealisationEnvisagees(request.getRealisationEnvisagees() != null ?
//                request.getRealisationEnvisagees() : existingRealisation.getRealisationEnvisagees());
//
//        // Handle RefParam fields
//        RefParam serviceParam = null;
//        if (request.getService() != null) {
//            serviceParam = refParamRepository.findById(request.getService().getId())
//                    .orElse(existingRealisation.getService());
//        } else {
//            serviceParam = existingRealisation.getService();
//        }
//        tempRealisation.setService(serviceParam);
//
//        RefParam directionParam = null;
//        if (request.getDirection() != null) {
//            directionParam = refParamRepository.findById(request.getDirection().getId())
//                    .orElse(existingRealisation.getDirection());
//        } else {
//            directionParam = existingRealisation.getDirection();
//        }
//        tempRealisation.setDirection(directionParam);
//
//        RefParam sourceFinancementParam = null;
//        if (request.getSourceFinancement() != null) {
//            sourceFinancementParam = refParamRepository.findById(request.getSourceFinancement().getId())
//                    .orElse(existingRealisation.getSourceFinancement());
//        } else {
//            sourceFinancementParam = existingRealisation.getSourceFinancement();
//        }
//        tempRealisation.setSourceFinancement(sourceFinancementParam);
//
//        RefParam monaisParam = null;
//        if (request.getMonais() != null) {
//            monaisParam = refParamRepository.findById(request.getMonais().getId())
//                    .orElse(existingRealisation.getMonais());
//        } else {
//            monaisParam = existingRealisation.getMonais();
//        }
//        tempRealisation.setMonais(monaisParam);
//
//        RefParam typeMarcherParam = null;
//        if (request.getTypeMarcher() != null) {
//            typeMarcherParam = refParamRepository.findById(request.getTypeMarcher().getId())
//                    .orElse(existingRealisation.getTypeMarcher());
//        } else {
//            typeMarcherParam = existingRealisation.getTypeMarcher();
//        }
//        tempRealisation.setTypeMarcher(typeMarcherParam);
//
//        RefParam modeSelectionParam = null;
//        if (request.getModeSelection() != null) {
//            modeSelectionParam = refParamRepository.findById(request.getModeSelection().getId())
//                    .orElse(existingRealisation.getModeSelection());
//        } else {
//            modeSelectionParam = existingRealisation.getModeSelection();
//        }
//        tempRealisation.setModeSelection(modeSelectionParam);
//
//        // Set other properties
//        tempRealisation.setDatePrevLps(request.getDatePrevLps() != null ?
//                request.getDatePrevLps() : existingRealisation.getDatePrevLps());
//        tempRealisation.setDatePrevAc(request.getDatePrevAc() != null ?
//                request.getDatePrevAc() : existingRealisation.getDatePrevAc());
//        tempRealisation.setDatePrevDp(request.getDatePrevDp() != null ?
//                request.getDatePrevDp() : existingRealisation.getDatePrevDp());
//        tempRealisation.setDatePrevAp(request.getDatePrevAp() != null ?
//                request.getDatePrevAp() : existingRealisation.getDatePrevAp());
//        tempRealisation.setBudgetEstime(request.getBudgetEstime() != null ?
//                request.getBudgetEstime() : existingRealisation.getBudgetEstime());
//        tempRealisation.setIsPublished(request.getIsPublished() != null ?
//                request.getIsPublished() : existingRealisation.getIsPublished());
//        tempRealisation.setPublishedAt(request.getPublishedAt() != null ?
//                request.getPublishedAt() : existingRealisation.getPublishedAt());
//        tempRealisation.setSort(request.getSort() != null ?
//                request.getSort() : existingRealisation.getSort());
//
//        // Check if there are actual changes
//        boolean hasChanges = hasChanges(tempRealisation, existingRealisation, request.getDocIds()) || lotChangesDetected;
//
//        System.out.println("Has changes: " + hasChanges);
//
//        if (!hasChanges) {
//            System.out.println("No changes detected, returning existing version: " + realisationVer);
//            return existingRealisation;
//        }
//
//        // If there are changes, create a new version
//        System.out.println("Changes detected, creating new version: " + (realisationVer + 1));
//
//        Realisation newRealisation = new Realisation();
//        newRealisation.setId(realisationId);
//        newRealisation.setVersion(realisationVer + 1);
//
//        // Copy all properties from the temp realisation
//        newRealisation.setRealisationEnvisagees(tempRealisation.getRealisationEnvisagees());
//        newRealisation.setService(tempRealisation.getService());
//        newRealisation.setDirection(tempRealisation.getDirection());
//        newRealisation.setSourceFinancement(tempRealisation.getSourceFinancement());
//        newRealisation.setMonais(tempRealisation.getMonais());
//        newRealisation.setTypeMarcher(tempRealisation.getTypeMarcher());
//        newRealisation.setModeSelection(tempRealisation.getModeSelection());
//        newRealisation.setDatePrevLps(tempRealisation.getDatePrevLps());
//        newRealisation.setDatePrevAc(tempRealisation.getDatePrevAc());
//        newRealisation.setDatePrevDp(tempRealisation.getDatePrevDp());
//        newRealisation.setDatePrevAp(tempRealisation.getDatePrevAp());
//        newRealisation.setBudgetEstime(tempRealisation.getBudgetEstime());
//        newRealisation.setIsPublished(tempRealisation.getIsPublished());
//        newRealisation.setPublishedAt(tempRealisation.getPublishedAt());
//        newRealisation.setSort(tempRealisation.getSort());
//
//        // Copy relationships
//        newRealisation.setPaas(new ArrayList<>(existingRealisation.getPaas()));
//        newRealisation.setPpms(new ArrayList<>(existingRealisation.getPpms()));
//
//        // Handle Lots - create/update lots and delete those not provided
//        Set<Lot> newLots = new HashSet<>();
//        Set<Integer> processedLotIds = new HashSet<>();
//
//        // Step 1: Process new or updated lots from request
//        if (request.getLots() != null && !request.getLots().isEmpty()) {
//            System.out.println("Processing " + request.getLots().size() + " lots from request");
//
//            for (LotResponse lotDto : request.getLots()) {
//                if (lotDto.getId() == null) {
//                    // Create new Lot
//                    Lot newLot = new Lot();
//                    newLot.setDescription(lotDto.getDescription());
//                    Integer lotId = lotRepository.findMaxId() + 1;
//                    newLot.setId(lotId);
//                    newLot.setVersion(1);
//
//                    Lot savedLot = lotRepository.save(newLot);
//                    newLots.add(savedLot);
//                    processedLotIds.add(savedLot.getId());
//
//                    System.out.println("Created new Lot with ID: " + savedLot.getId());
//                } else {
//                    // Skip duplicates
//                    if (processedLotIds.contains(lotDto.getId())) {
//                        System.out.println("Skipping duplicate lot ID: " + lotDto.getId());
//                        continue;
//                    }
//
//                    // Process existing Lot
//                    Integer lotId = lotDto.getId();
//                    Integer lotVersion = lotDto.getVersion() != null ? lotDto.getVersion() : lotRepository.findMaxVersionById(lotId);
//
//                    if (lotVersion == null) {
//                        System.out.println("Lot ID " + lotId + " not found, skipping");
//                        continue;
//                    }
//
//                    Optional<Lot> existingLotOpt = lotRepository.findById(new CompositeId(lotId, lotVersion));
//                    if (existingLotOpt.isPresent()) {
//                        Lot existingLot = existingLotOpt.get();
//
//                        if (lotDto.getDescription() != null && !lotDto.getDescription().equals(existingLot.getDescription())) {
//                            // Create new version of Lot
//                            Lot newLot = new Lot();
//                            newLot.setId(existingLot.getId());
//                            newLot.setVersion(existingLot.getVersion() + 1);
//                            newLot.setDescription(lotDto.getDescription());
//
//                            Lot savedLot = lotRepository.save(newLot);
//                            newLots.add(savedLot);
//                            processedLotIds.add(savedLot.getId());
//
//                            System.out.println("Created new version of Lot: " + savedLot.getId() + ", Version: " + savedLot.getVersion());
//                        } else {
//                            // No changes, use existing lot
//                            newLots.add(existingLot);
//                            processedLotIds.add(existingLot.getId());
//                        }
//                    }
//                }
//            }
//        } else {
//            System.out.println("No lots provided in request, will delete all existing lots");
//        }
//
//        // Step 2: Identify and delete lots not in the request
//        Set<Lot> existingLots = existingRealisation.getLots();
//        for (Lot existingLot : existingLots) {
//            if (!processedLotIds.contains(existingLot.getId())) {
//                // Lot not included in request, mark as deleted or remove
//                // Option 1: Mark as deleted (if your Lot entity has an isDeleted flag)
//            /*
//            existingLot.setIsDeleted(true);
//            lotRepository.save(existingLot);
//            System.out.println("Marked Lot ID " + existingLot.getId() + " as deleted");
//            */
//
//                // Option 2: Delete from database (if you want to physically delete)
////                lotRepository.delete(existingLot);
//                lotRepository.softDelete(existingLot.getId(),existingLot.getVersion());
//                System.out.println("Deleted Lot ID " + existingLot.getId() + " from database");
//            }
//        }
//
//        // Set the new/updated lots on the new Realisation
//        newRealisation.setLots(newLots);
//        System.out.println("Final lot count: " + newLots.size());
//
//        // Handle documents if docIds are provided
//        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
//            // You'll need to implement a method to fetch Docs by ID
//            // This is a placeholder - uncomment and implement with your actual Docs repository
//        /*
//        List<Docs> docs = new ArrayList<>();
//        for (Integer docId : request.getDocIds()) {
//            docsRepository.findById(docId).ifPresent(docs::add);
//        }
//        newRealisation.setDocs(docs);
//        */
//            // For now, just copy the existing docs
//            newRealisation.setDocs(new ArrayList<>(existingRealisation.getDocs()));
//        } else {
//            newRealisation.setDocs(new ArrayList<>(existingRealisation.getDocs()));
//        }
//
//        // Save and return the new version
//        return repository.save(newRealisation);
//    }



    @Transactional
    public Realisation updateWithVersioning(Integer realisationId, Integer realisationVer, RealisationBaseRequestDto request) throws RealisationNotFoundException {
        // Find the existing realisation
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

        // Log the current state and request
        System.out.println("Updating Realisation ID: " + realisationId + ", Current Version: " + realisationVer);
        System.out.println("Request data: " + request);
        System.out.println("Current realisationEnvisagees: " + existingRealisation.getRealisationEnvisagees());
        System.out.println("New realisationEnvisagees: " + request.getRealisationEnvisagees());

        // Check for Lot changes - if lots are provided, consider it a change
        boolean lotChangesDetected = request.getLots() != null && !request.getLots().isEmpty();

        // Create a temporary Realisation to check for changes
        Realisation tempRealisation = new Realisation();

        // Set all fields from request or existing realisation
        tempRealisation.setRealisationEnvisagees(request.getRealisationEnvisagees() != null ?
                request.getRealisationEnvisagees() : existingRealisation.getRealisationEnvisagees());

        // Handle RefParam fields
        RefParam serviceParam = null;
        if (request.getService() != null) {
            serviceParam = refParamRepository.findById(request.getService().getId())
                    .orElse(existingRealisation.getService());
        } else {
            serviceParam = existingRealisation.getService();
        }
        tempRealisation.setService(serviceParam);

        RefParam directionParam = null;
        if (request.getDirection() != null) {
            directionParam = refParamRepository.findById(request.getDirection().getId())
                    .orElse(existingRealisation.getDirection());
        } else {
            directionParam = existingRealisation.getDirection();
        }
        tempRealisation.setDirection(directionParam);

        RefParam sourceFinancementParam = null;
        if (request.getSourceFinancement() != null) {
            sourceFinancementParam = refParamRepository.findById(request.getSourceFinancement().getId())
                    .orElse(existingRealisation.getSourceFinancement());
        } else {
            sourceFinancementParam = existingRealisation.getSourceFinancement();
        }
        tempRealisation.setSourceFinancement(sourceFinancementParam);

        RefParam monaisParam = null;
        if (request.getMonais() != null) {
            monaisParam = refParamRepository.findById(request.getMonais().getId())
                    .orElse(existingRealisation.getMonais());
        } else {
            monaisParam = existingRealisation.getMonais();
        }
        tempRealisation.setMonais(monaisParam);

        RefParam typeMarcherParam = null;
        if (request.getTypeMarcher() != null) {
            typeMarcherParam = refParamRepository.findById(request.getTypeMarcher().getId())
                    .orElse(existingRealisation.getTypeMarcher());
        } else {
            typeMarcherParam = existingRealisation.getTypeMarcher();
        }
        tempRealisation.setTypeMarcher(typeMarcherParam);

        RefParam modeSelectionParam = null;
        if (request.getModeSelection() != null) {
            modeSelectionParam = refParamRepository.findById(request.getModeSelection().getId())
                    .orElse(existingRealisation.getModeSelection());
        } else {
            modeSelectionParam = existingRealisation.getModeSelection();
        }
        tempRealisation.setModeSelection(modeSelectionParam);

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

        // Check if there are actual changes
        boolean hasChanges = hasChanges(tempRealisation, existingRealisation, request.getDocIds()) || lotChangesDetected;

        System.out.println("Has changes: " + hasChanges);

        if (!hasChanges) {
            System.out.println("No changes detected, returning existing version: " + realisationVer);
            return existingRealisation;
        }

        // If there are changes, create a new version
        System.out.println("Changes detected, creating new version: " + (realisationVer + 1));

        Realisation newRealisation = new Realisation();
        newRealisation.setId(realisationId);
        newRealisation.setVersion(realisationVer + 1);

        // Copy all properties from the temp realisation
        newRealisation.setRealisationEnvisagees(tempRealisation.getRealisationEnvisagees());
        newRealisation.setService(tempRealisation.getService());
        newRealisation.setDirection(tempRealisation.getDirection());
        newRealisation.setSourceFinancement(tempRealisation.getSourceFinancement());
        newRealisation.setMonais(tempRealisation.getMonais());
        newRealisation.setTypeMarcher(tempRealisation.getTypeMarcher());
        newRealisation.setModeSelection(tempRealisation.getModeSelection());
        newRealisation.setDatePrevLps(tempRealisation.getDatePrevLps());
        newRealisation.setDatePrevAc(tempRealisation.getDatePrevAc());
        newRealisation.setDatePrevDp(tempRealisation.getDatePrevDp());
        newRealisation.setDatePrevAp(tempRealisation.getDatePrevAp());
        newRealisation.setBudgetEstime(tempRealisation.getBudgetEstime());
        newRealisation.setIsPublished(tempRealisation.getIsPublished());
        newRealisation.setPublishedAt(tempRealisation.getPublishedAt());
        newRealisation.setSort(tempRealisation.getSort());

        // Copy relationships
        newRealisation.setPaas(new ArrayList<>(existingRealisation.getPaas()));
        newRealisation.setPpms(new ArrayList<>(existingRealisation.getPpms()));

        // Handle Lots - create/update lots properly
        Set<Lot> newLots = new HashSet<>();

        // Process new or updated lots from request - only add the lots explicitly in the request
        Set<Integer> processedLotIds = new HashSet<>();
        if (request.getLots() != null && !request.getLots().isEmpty()) {
            System.out.println("Processing " + request.getLots().size() + " lots from request");

            // Track lot IDs to avoid duplicates


            for (LotResponse lotDto : request.getLots()) {
                if (lotDto.getId() == null) {
                    // Create new Lot
                    Lot newLot = new Lot();
                    newLot.setDescription(lotDto.getDescription());

                    // Generate new ID
                    Integer lotId = lotRepository.findMaxId() + 1;
                    newLot.setId(lotId);
                    newLot.setVersion(1); // First version

                    // Save the Lot
                    Lot savedLot = lotRepository.save(newLot);
                    newLots.add(savedLot);
                    processedLotIds.add(savedLot.getId());

                    System.out.println("Created new Lot with ID: " + savedLot.getId());
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
                        if (lotDto.getDescription() != null && !lotDto.getDescription().equals(existingLot.getDescription())) {
                            // Create a new version of the Lot
                            Lot newLot = new Lot();
                            newLot.setId(existingLot.getId());
                            newLot.setVersion(existingLot.getVersion() + 1);
                            newLot.setDescription(lotDto.getDescription());

                            // Save the new version
                            Lot savedLot = lotRepository.save(newLot);
                            newLots.add(savedLot);
                            processedLotIds.add(savedLot.getId());

                            System.out.println("Created new version of Lot: " + savedLot.getId() + ", Version: " + savedLot.getVersion());
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


        Set<Lot> existingLots = existingRealisation.getLots();
        for (Lot existingLot : existingLots) {
            System.out.println("the list of lots excited");
            System.out.println(existingLot.toString());
            if (!processedLotIds.contains(existingLot.getId())) {
                // Lot not included in request, mark as deleted or remove
                // Option 1: Mark as deleted (if your Lot entity has an isDeleted flag)

                existingLot.setIsDeleted(true);
                lotRepository.save(existingLot);
                System.out.println("Marked Lot ID " + existingLot.getId() + " as deleted");


                // Option 2: Delete from database (this cause an error )
//                lotRepository.delete(existingLot);
//                lotRepository.softDelete(existingLot.getId(),existingLot.getVersion());
                System.out.println("Deleted Lot ID " + existingLot.getId() + " from database");
            }
        }


        // Set the new/updated lots on the new Realisation
        newRealisation.setLots(newLots);

        // Handle documents if docIds are provided
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            // You'll need to implement a method to fetch Docs by ID
            // This is a placeholder - uncomment and implement with your actual Docs repository
            /*
            List<Docs> docs = new ArrayList<>();
            for (Integer docId : request.getDocIds()) {
                docsRepository.findById(docId).ifPresent(docs::add);
            }
            newRealisation.setDocs(docs);
            */
            // For now, just copy the existing docs
            newRealisation.setDocs(new ArrayList<>(existingRealisation.getDocs()));
        } else {
            newRealisation.setDocs(new ArrayList<>(existingRealisation.getDocs()));
        }

        // Save and return the new version
        return repository.save(newRealisation);
    }

    private Integer getRefParamId(RefParam refParam) {
        return refParam != null ? refParam.getId() : null;
    }

    private void copyProperties(Realisation source, Realisation target) {
        target.setRealisationEnvisagees(source.getRealisationEnvisagees());
        target.setService(source.getService());
        target.setDirection(source.getDirection());
        target.setSourceFinancement(source.getSourceFinancement());
        target.setMonais(source.getMonais());
        target.setTypeMarcher(source.getTypeMarcher());
        target.setModeSelection(source.getModeSelection());
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

    /**
     * Compares two Realisation objects to determine if there are any changes.
     *
     * @param newRealisation The realisation with potentially new values
     * @param existingRealisation The existing realisation in the database
     * @param docIds List of document IDs in the request (if any)
     * @return true if there are changes, false otherwise
     */
    private boolean hasChanges(Realisation newRealisation, Realisation existingRealisation, List<Integer> docIds) {
        boolean changes = !Objects.equals(newRealisation.getRealisationEnvisagees(), existingRealisation.getRealisationEnvisagees())
                || !Objects.equals(getRefParamId(newRealisation.getService()), getRefParamId(existingRealisation.getService()))
                || !Objects.equals(getRefParamId(newRealisation.getDirection()), getRefParamId(existingRealisation.getDirection()))
                || !Objects.equals(getRefParamId(newRealisation.getSourceFinancement()), getRefParamId(existingRealisation.getSourceFinancement()))
                || !Objects.equals(getRefParamId(newRealisation.getMonais()), getRefParamId(existingRealisation.getMonais()))
                || !Objects.equals(getRefParamId(newRealisation.getTypeMarcher()), getRefParamId(existingRealisation.getTypeMarcher()))
                || !Objects.equals(getRefParamId(newRealisation.getModeSelection()), getRefParamId(existingRealisation.getModeSelection()))
                || !Objects.equals(newRealisation.getDatePrevLps(), existingRealisation.getDatePrevLps())
                || !Objects.equals(newRealisation.getDatePrevAc(), existingRealisation.getDatePrevAc())
                || !Objects.equals(newRealisation.getDatePrevDp(), existingRealisation.getDatePrevDp())
                || !Objects.equals(newRealisation.getDatePrevAp(), existingRealisation.getDatePrevAp())
                || !Objects.equals(newRealisation.getBudgetEstime(), existingRealisation.getBudgetEstime())
                || !Objects.equals(newRealisation.getIsPublished(), existingRealisation.getIsPublished())
                || !Objects.equals(newRealisation.getPublishedAt(), existingRealisation.getPublishedAt())
                || !Objects.equals(newRealisation.getSort(), existingRealisation.getSort());

        // Check for doc changes
        boolean docsChanged = docIds != null && !docIds.isEmpty();

        return changes || docsChanged;
    }

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
        Set<Lot> lotEntities = new HashSet<>();

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

}