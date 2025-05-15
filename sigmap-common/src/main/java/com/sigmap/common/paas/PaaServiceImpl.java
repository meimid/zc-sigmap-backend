package com.sigmap.common.paas;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.AlfrescoConf.RenamedMultipartFile;
import com.sigmap.common.Constants;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.paas.exceptions.PaaAlreadyExistsException;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.mappers.PaaMapper;
import com.sigmap.common.paas.request.PaaBaseRequestDto;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.request.PaaRequestGlobal;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.RealisationService;
import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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
    private final DocsService docsService;
    private final AcService acService;

    @Value("${alfresco.url}")
    private String alfrescoUrl;

    public PaaServiceImpl(PaaRepository repository, RealisationRepository realisationRepository,
                          RefParamRepository refParamRepository, DocsRepository docsRepository,
                          AcRepository acRepository, LotRepository lotRepository, RealisationService realisationService,
                          DocumentCommuneService documentCommuneService,
                          DocsService docsService,
                          AcService acService,
                          AlfrescoService alfrescoService


    ) {
        this.repository = repository;
        this.realisationRepository = realisationRepository;
        this.refParamRepository = refParamRepository;
        this.docsRepository = docsRepository;
        this.acRepository = acRepository;
        this.lotRepository = lotRepository;
        this.realisationService = realisationService;
        this.alfrescoService = alfrescoService;
        this.docsService = docsService;
        this.documentCommuneService = documentCommuneService;
        this.acService=acService;
    }

    @Override
    public List<Paa> findAll() {
        Specification<Paa> spec = PaaSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Paa> findAll(Specification<Paa> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Paa> findAllDeleted(Specification<Paa> spec, Pageable pageable) {
        Specification<Paa> deletedSpec = PaaSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Paa> findAllDeleted(Pageable pageable) {
        Specification<Paa> spec = PaaSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Paa> findDeletedById(Integer paaId, Integer paaVer) {
        Specification<Paa> spec = PaaSpecifications.id(new CompositeId(paaId, paaVer))
                .and(PaaSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Paa> findOne(Specification<Paa> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Paa> findById(Integer paaId, Integer paaVer) {
        return repository.findById(new CompositeId(paaId, paaVer));
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

    @Override
    public Paa create(PaaRequest request) throws PaaAlreadyExistsException {
        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        Integer id = request.getId();
        if (id == null) {
            id = repository.findMaxId();
            if (id == null) {
                id = 1;
            } else {
                id++;
            }
        }

        Optional<Paa> existingPaa = repository.findById(new CompositeId(id, version));
        if (existingPaa.isPresent()) {
            throw new PaaAlreadyExistsException(
                    "Paa with ID " + id + " and version " + version + " already exists.",id, version);
        }

        Paa paa = PaaMapper.INSTANCE.toEntity(request);
        paa.setRealisations(new ArrayList<>());

        // Set status from RefParam
        if (request.getStatus() != null) {
            paa.setStatus(refParamRepository.findById(request.getStatus().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Status not found")));
        }

        paa.setId(id);
        paa.setVersion(version);

        Paa savedPaa = repository.save(paa);

        if (request.getRealisationIds() != null && !request.getRealisationIds().isEmpty()) {
            List<Realisation> realisations = realisationRepository.findAllById(request.getRealisationIds());
            if (realisations.size() != request.getRealisationIds().size()) {
                throw new RuntimeException("Some realisations were not found");
            }

            // Update both sides of the relationship in memory
            for (Realisation realisation : realisations) {
                if (realisation.getPaas() == null) {
                    realisation.setPaas(new ArrayList<>());
                }
                if (!realisation.getPaas().contains(savedPaa)) {
                    realisation.getPaas().add(savedPaa);
                }
            }

            // Save all realisations in a single batch operation
            realisationRepository.saveAll(realisations);

            // Update and save PAA with the realisations
            savedPaa.setRealisations(realisations);
            savedPaa = repository.save(savedPaa);
        }

        return savedPaa;
    }

    @Override
    public void delete(Integer paaId, Integer paaVer) {
        repository.softDelete(paaId, paaVer);
    }

    @Override
    public Paa update(Integer paaId, Integer paaVer, PaaRequestGlobal request) throws PaaNotFound {
        System.out.println("calling the mehtod");
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

        // Update fields of the existing PAA
        existingPaa.setRef(request.getRef());
        existingPaa.setNom(request.getNom());
        existingPaa.setAnnee(request.getAnnee());
        existingPaa.setBudgetEstime(request.getBudgetEstime());

        // Set Ac if provided
        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new EntityNotFoundException("Ac with ID " + request.getAcId() + " not found"));
            existingPaa.setAc(ac);
        }

        // Handle document management by comparing existing docs with requested docIds
        if (request.getDocIds() != null) {
            Set<Integer> requestedDocIds = new HashSet<>(request.getDocIds());
            
            // Create a list to hold updated documents
            List<Docs> updatedDocs = new ArrayList<>();
            
            if (existingPaa.getDocs() != null) {
                // For each existing document in the PAA
                for (Docs existingDoc : existingPaa.getDocs()) {
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
            
            // Add any new docs from the request that weren't already in existingPaa
            for (Integer docId : requestedDocIds) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                
                // Set up bidirectional relationship
                if (doc.getPaas() == null) {
                    doc.setPaas(new ArrayList<>());
                }
                doc.getPaas().add(existingPaa);
                
                // Ensure doc is active
                doc.setIsDeleted(false);
                docsRepository.save(doc);
                
                updatedDocs.add(doc);
            }
            
            // Update the PAA with the final docs list
            existingPaa.setDocs(updatedDocs);
        }

        // Handle PAA files if provided
        if (request.getPaaFiles() != null) {
            try {
                processAndSaveDocuments(request.getPaaFiles(), existingPaa);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Process realisations if provided
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            List<Realisation> realisations = new ArrayList<>();

            for (RealisationRequestForPaa realisationRequest : request.getRealisations()) {
                Realisation realisation;

                if (realisationRequest.getId() != null) {
                    // Update existing realisation
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

                    // Handle realisation document management - same approach as for PAA docs
                    if (realisationRequest.getDocIds() != null) {
                        Set<Integer> requestedRealisationDocIds = new HashSet<>(realisationRequest.getDocIds());
                        
                        // Create a list for updated realisation docs
                        List<Docs> updatedRealisationDocs = new ArrayList<>();
                        
                        if (realisation.getDocs() != null) {
                            // For each existing document in the realisation
                            for (Docs existingDoc : realisation.getDocs()) {
                                if (requestedRealisationDocIds.contains(existingDoc.getId())) {
                                    // This doc is in the request, ensure it's not marked as deleted
                                    existingDoc.setIsDeleted(false);
                                    docsRepository.save(existingDoc);
                                    updatedRealisationDocs.add(existingDoc);
                                    
                                    // Remove from requested IDs since we've handled it
                                    requestedRealisationDocIds.remove(existingDoc.getId());
                                } else {
                                    // This doc is not in the request, mark it as deleted
                                    existingDoc.setIsDeleted(true);
                                    docsRepository.save(existingDoc);
                                    updatedRealisationDocs.add(existingDoc); // Keep it in the collection
                                    System.out.println("Marked realisation document with ID " + existingDoc.getId() + " as deleted (not in request)");
                                }
                            }
                        }
                        
                        // Add any new docs from the request
                        for (Integer docId : requestedRealisationDocIds) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                            
                            // Set up bidirectional relationship
                            if (doc.getRealisations() == null) {
                                doc.setRealisations(new ArrayList<>());
                            }
                            doc.getRealisations().add(realisation);
                            
                            // Ensure doc is active
                            doc.setIsDeleted(false);
                            docsRepository.save(doc);
                            
                            updatedRealisationDocs.add(doc);
                        }
                        
                        // Update the realisation's docs
                        realisation.setDocs(updatedRealisationDocs);
                    }

                    // Handle realisation files if provided
                    if (realisationRequest.getRealisationFiles() != null) {
                        try {
                            processAndSaveRelisationsDocumentsForUpdate(realisationRequest.getRealisationFiles(), realisation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Ensure the relationship with PAA
                    if (realisation.getPaas() == null) {
                        realisation.setPaas(new ArrayList<>());
                    }
                    if (!realisation.getPaas().contains(existingPaa)) {
                        realisation.getPaas().add(existingPaa);
                    }

                    // Handle Lots if provided
                    if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                        Set<Lot> lots = new HashSet<>();
                        for (LotResponse lotRequest : realisationRequest.getLots()) {
                            Lot lot = new Lot();
                            Integer lotId = lotRequest.getId();
                            if (lotId == null) {
                                Integer maxLotId = lotRepository.findMaxId();
                                lotId = (maxLotId != null) ? maxLotId + 1 : 1;
                            }
                            lot.setId(lotId);
                            lot.setVersion(1); // Always version 1 for new lots
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
                    Integer maxRealisationId = realisationRepository.findMaxId();
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
                    if (realisationRequest.getRealisationFiles() != null) {
                        try {
                            processAndSaveRelisationsDocuments(realisationRequest.getRealisationFiles(), realisation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Set up the relationship with PAA
                    List<Paa> paaList = new ArrayList<>();
                    paaList.add(existingPaa);
                    realisation.setPaas(paaList);

                    // Handle Realisation documents if docIds are provided
                    if (realisationRequest.getDocIds() != null && !realisationRequest.getDocIds().isEmpty()) {
                        List<Docs> docs = new ArrayList<>();
                        for (Integer docId : realisationRequest.getDocIds()) {
                            Docs doc = docsRepository.findById(docId)
                                    .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));
                            
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
                                Integer maxLotId = lotRepository.findMaxId();
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

            // Update the PAA with the list of realisations
            existingPaa.setRealisations(realisations);
        } else {
            existingPaa.getRealisations().clear(); // Clear if no new realisations provided
        }

        // Save the updated PAA
        return repository.save(existingPaa);
    }


     @Transactional
     @Override
     public Paa updateWithVersioning(Integer paaId, Integer paaVer, PaaRequestGlobal request) throws PaaNotFound {
         // Find the existing PAA
         CompositeId compositeId = new CompositeId(paaId, paaVer);
         Paa existingPaa = repository.findById(compositeId)
                 .orElseThrow(() -> new PaaNotFound(
                         "PAA with ID " + paaId + " and version " + paaVer + " not found",List.of(paaId),List.of(paaVer)));

         if (Boolean.TRUE.equals(existingPaa.getIsDeleted())) {
             throw new PaaNotFound(
                     "Cannot update a deleted PAA with ID " + paaId + " and version " + paaVer,List.of(paaId),List.of(paaVer));
         }

         // Log the current state and request
         System.out.println("Updating PAA ID: " + paaId + ", Current Version: " + paaVer);
         System.out.println("Request data: " + request);

         // Use a request hash to check for duplicate submissions
         String requestHash = generateRequestHash(request);
         String previousRequestHash = getPreviousRequestHash(paaId, paaVer);
         System.out.println("Current request hash: " + requestHash);
         System.out.println("Previous request hash: " + previousRequestHash);

         // If this is the exact same request as the previous one, return the existing PAA without changes
         if (requestHash != null && requestHash.equals(previousRequestHash)) {
             System.out.println("Duplicate request detected, returning existing PAA without changes");
             return existingPaa;
         }

         // Create a temporary PAA to check for changes in direct fields
         Paa tempPaa = new Paa();

         // Only use request values when they're not null, otherwise keep existing values
         tempPaa.setRef(request.getRef() != null ? request.getRef() : existingPaa.getRef());
         tempPaa.setNom(request.getNom() != null ? request.getNom() : existingPaa.getNom());
         tempPaa.setAnnee(request.getAnnee() != null ? request.getAnnee() : existingPaa.getAnnee());
         tempPaa.setBudgetEstime(request.getBudgetEstime() != null ? request.getBudgetEstime() : existingPaa.getBudgetEstime());

         // Handle status
         RefParam requestStatus = null;
         if (request.getStatus() != null) {
             requestStatus = refParamRepository.findById(request.getStatus().getId()).orElse(null);
         }
         tempPaa.setStatus(requestStatus != null ? requestStatus : existingPaa.getStatus());

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

         // Track whether any documents have changed
         boolean documentsChanged = false;

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
                     System.out.println("Realisation ID: " + realisationDto.getId() + ", Lots: " +
                             (realisationDto.getLots() != null ? realisationDto.getLots().size() : "null"));

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
                         Realisation existingRealisation = realisationRepository.findById(realisationId)
                                 .orElseThrow(() -> new RuntimeException("Realisation not found with ID: " +
                                         realisationId.getId() + " and version: " + realisationId.getVersion()));

                         System.out.println("Found existing Realisation: " + existingRealisation.getId() + " version " + existingRealisation.getVersion());
                         System.out.println("Current lots: " + (existingRealisation.getLots() != null ? existingRealisation.getLots().size() : "null"));

                         boolean updateNeeded = realisationNeedsUpdate(existingRealisation, realisationDto);
                         System.out.println("Realisation update needed: " + updateNeeded);

                         if (updateNeeded) {
                             System.out.println("Updating Realisation with versioning...");
                             Realisation updatedRealisation = realisationService.updateWithVersioning(
                                     realisationDto.getId(), versionToUse, realisationDto);
                             System.out.println("Updated Realisation: " + updatedRealisation.getId() + " version " + updatedRealisation.getVersion());
                             updatedRealisations.add(updatedRealisation);
                             realisationsChanged = true;
                         } else {
                             System.out.println("Using existing Realisation without changes");

                             if (realisationDto.getLots() != null && !realisationDto.getLots().isEmpty()) {
                                 System.out.println("Processing " + realisationDto.getLots().size() + " lots in DTO");

                                 Set<Lot> updatedLots = new HashSet<>();
                                 Set<Integer> processedLotIds = new HashSet<>();
                                 boolean lotChanges = false;

                                 Set<Integer> existingLotIds = existingRealisation.getLots() != null ?
                                         existingRealisation.getLots().stream().map(Lot::getId).collect(Collectors.toSet()) : new HashSet<>();
                                 Map<Integer, String> existingLotDescriptions = existingRealisation.getLots() != null ?
                                         existingRealisation.getLots().stream().collect(
                                                 Collectors.toMap(Lot::getId, lot -> lot.getDescription() != null ? lot.getDescription() : "", (a, b) -> a)) :
                                         new HashMap<>();

                                 for (LotResponse lotDto : realisationDto.getLots()) {
                                     if (lotDto.getId() == null) {
                                         String lotHash = lotDto.getDescription() != null ? lotDto.getDescription() : "";
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
                                         newLot.setVersion(1);
                                         Integer maxLotId = lotRepository.findMaxId();
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

                                 Set<Integer> requestLotIds = realisationDto.getLots().stream()
                                         .filter(lotDto -> lotDto.getId() != null)
                                         .map(LotResponse::getId)
                                         .collect(Collectors.toSet());
                                 requestLotIds.addAll(processedLotIds.stream()
                                         .filter(id -> !existingLotIds.contains(id))
                                         .collect(Collectors.toSet()));

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
                         updatedRealisations.add(newRealisation);
                         realisationsChanged = true;
                     }
                 } catch (Exception e) {
                     System.err.println("Failed to process Realisation: " + e.getMessage());
                     e.printStackTrace();
                     throw new RuntimeException("Failed to process Realisation: " + e.getMessage(), e);
                 }
             }

             if (!realisationsChanged && existingPaa.getRealisations() != null) {
                 if (existingPaa.getRealisations().size() != updatedRealisations.size()) {
                     System.out.println("Realisation count changed: " + existingPaa.getRealisations().size() + " -> " + updatedRealisations.size());
                     realisationsChanged = true;
                 } else {
                     Map<Integer, Integer> existingRealisationVersions = existingPaa.getRealisations().stream()
                             .collect(Collectors.toMap(Realisation::getId, Realisation::getVersion, (a, b) -> a));

                     for (Realisation updatedRealisation : updatedRealisations) {
                         Integer existingVersion = existingRealisationVersions.get(updatedRealisation.getId());
                         if (existingVersion == null || !existingVersion.equals(updatedRealisation.getVersion())) {
                             System.out.println("Realisation changed: " + updatedRealisation.getId() + " version " + updatedRealisation.getVersion());
                             realisationsChanged = true;
                             break;
                         }
                     }
                 }
             }
         } else if (existingPaa.getRealisations() != null && !existingPaa.getRealisations().isEmpty()) {
             System.out.println("Realisations removed from PAA");
             realisationsChanged = true;
         }

         // Process document IDs - improved document handling
         if (request.getDocIds() != null) {
            Set<Integer> requestDocIds = new HashSet<>(request.getDocIds());
            
            // Get only the active (non-deleted) existing document IDs
            Set<Integer> activeExistingDocIds = existingPaa.getDocs() != null ?
                    existingPaa.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .map(Docs::getId)
                            .collect(Collectors.toSet()) : new HashSet<>();
            
            // First check if there are document changes between active docs and request
            documentsChanged = !requestDocIds.equals(activeExistingDocIds);
            
            // If we're going to create a new version, prepare the document collection
            if (documentsChanged) {
                // Start with a copy of all existing documents
                if (existingPaa.getDocs() != null) {
                    updatedDocs = new ArrayList<>(existingPaa.getDocs());
                } else {
                    updatedDocs = new ArrayList<>();
                }
                
                // For docs in the request, make sure they're active (not deleted)
                for (Docs doc : updatedDocs) {
                    if (requestDocIds.contains(doc.getId()) && Boolean.TRUE.equals(doc.getIsDeleted())) {
                        // Document is in the request but currently deleted, make it active
                        doc.setIsDeleted(false);
                        docsRepository.save(doc);
                        System.out.println("Activated document with ID " + doc.getId() + " for new version");
                        // Remove from request since we've handled it
                        requestDocIds.remove(doc.getId());
                    }
                }
                
                // Add any new documents from the request
                for (Integer docId : requestDocIds) {
                    Docs doc = docsRepository.findById(docId)
                            .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
                    if (!updatedDocs.contains(doc)) {
                        updatedDocs.add(doc);
                    }
                }
            } else {
                // No document changes, just copy the existing docs collection
                updatedDocs = existingPaa.getDocs() != null ? new ArrayList<>(existingPaa.getDocs()) : new ArrayList<>();
            }
        } else {
            // No document IDs provided, just copy existing documents
            updatedDocs = existingPaa.getDocs() != null ? new ArrayList<>(existingPaa.getDocs()) : new ArrayList<>();
        }

         // Determine if we need to create a new PAA version
         boolean shouldCreateNewVersion = hasDirectChanges || realisationsChanged || documentsChanged;
         System.out.println("Should create new PAA version: " + shouldCreateNewVersion);
         System.out.println("  - Direct changes: " + hasDirectChanges);
         System.out.println("  - Realisations changed: " + realisationsChanged);
         System.out.println("  - Documents changed: " + documentsChanged);

         // Store the request hash only if we're creating a new version
         if (shouldCreateNewVersion) {
             storePreviousRequestHash(paaId, paaVer, requestHash);
         } else {
             System.out.println("No changes detected, returning existing PAA version: " + paaVer);
             return existingPaa;
         }

         // Create a new PAA version
         System.out.println("Changes detected, creating new PAA version: " + (paaVer + 1));
         Paa newPaa = new Paa();
         newPaa.setId(paaId);
         newPaa.setVersion(paaVer + 1);

         // Copy properties from temp PAA
         newPaa.setRef(tempPaa.getRef());
         newPaa.setNom(tempPaa.getNom());
         newPaa.setAnnee(tempPaa.getAnnee());
         newPaa.setBudgetEstime(tempPaa.getBudgetEstime());
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
             for (Realisation realisation : updatedRealisations) {
                 List<Paa> paaList = realisation.getPaas();
                 if (paaList == null) {
                     paaList = new ArrayList<>();
                     realisation.setPaas(paaList);
                 }
                 if (!paaList.contains(newPaa)) {
                     paaList.add(newPaa);
                 }
                 realisation = realisationRepository.save(realisation);
                 System.out.println("Associated Realisation " + realisation.getId() +
                         " version " + realisation.getVersion() + " with new PAA");
             }
             newPaa.setRealisations(updatedRealisations);
             System.out.println("Set " + updatedRealisations.size() + " realisations on the new PAA");
         } else if (existingPaa.getRealisations() != null) {
             for (Realisation existingRealization : existingPaa.getRealisations()) {
                 List<Paa> paaList = existingRealization.getPaas();
                 if (paaList == null) {
                     paaList = new ArrayList<>();
                     existingRealization.setPaas(paaList);
                 }
                 if (!paaList.contains(newPaa)) {
                     paaList.add(newPaa);
                 }
                 existingRealization = realisationRepository.save(existingRealization);
                 System.out.println("Associated existing Realisation " + existingRealization.getId() +
                         " version " + existingRealization.getVersion() + " with new PAA");
             }
             newPaa.setRealisations(new ArrayList<>(existingPaa.getRealisations()));
             System.out.println("Copied " + existingPaa.getRealisations().size() +
                     " existing realisations to the new PAA");
         }

         // Handle documents - copy all documents to the new PAA and update their associations
         if (!updatedDocs.isEmpty()) {
             for (Docs doc : updatedDocs) {
                 List<Paa> paaList = doc.getPaas();
                 if (paaList == null) {
                     paaList = new ArrayList<>();
                     doc.setPaas(paaList);
                 }
                 if (!paaList.contains(newPaa)) {
                     paaList.add(newPaa);
                 }
                 docsRepository.save(doc);
             }
             newPaa.setDocs(updatedDocs);
         } else if (existingPaa.getDocs() != null && !existingPaa.getDocs().isEmpty()) {
             // Copy all existing documents to new PAA
             List<Docs> existingDocs = existingPaa.getDocs();
             for (Docs existingDoc : existingDocs) {
                 List<Paa> paaList = existingDoc.getPaas();
                 if (paaList == null) {
                     paaList = new ArrayList<>();
                     existingDoc.setPaas(paaList);
                 }
                 if (!paaList.contains(newPaa)) {
                     paaList.add(newPaa);
                 }
                 docsRepository.save(existingDoc);
             }
             newPaa.setDocs(new ArrayList<>(existingDocs));
         }

         // Final save
         newPaa = repository.save(newPaa);
         System.out.println("Final save of PAA: " + newPaa.getId() + " version " + newPaa.getVersion());

         // Update the hash for the new version
         storePreviousRequestHash(paaId, newPaa.getVersion(), requestHash);

         return newPaa;
     }

    /**
     * Updates a PAA with versioning by automatically finding and using the latest version.
     * <p>
     * This method finds the latest version of the PAA with the given ID and then
     * calls the version-specific updateWithVersioning method.
     *
     * @param paaId the ID of the PAA to update
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
            throw new PaaNotFound( "No versions found for PAA with ID: " + paaId,List.of(paaId),List.of(null));
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

        return Objects.equals(paa1.getRef(), paa2.getRef()) &&
                Objects.equals(paa1.getNom(), paa2.getNom()) &&
                Objects.equals(paa1.getAnnee(), paa2.getAnnee()) &&
                Objects.equals(paa1.getBudgetEstime(), paa2.getBudgetEstime()) &&
                Objects.equals(getRefParamId(paa1.getStatus()), getRefParamId(paa2.getStatus())) &&
                Objects.equals(getAcId(paa1.getAc()), getAcId(paa2.getAc()));
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
     * @param paaId The PAA ID
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
     * @param paaId The PAA ID
     * @param paaVer The PAA version
     * @param hash The hash string to store
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
    public Paa createWithRelationships(PaaRequestGlobal request) throws PaaAlreadyExistsException {
        // Generate PAA ID if not provided
        Integer paaId = repository.findMaxId();
        paaId = (paaId != null) ? paaId + 1 : 1;

        // Always use version 1 for new PAAs
        Integer paaVersion = 1;

        // Check if PAA already exists
        Optional<Paa> existingPaa = repository.findById(new CompositeId(paaId, paaVersion));
        if (existingPaa.isPresent()) {
            throw new PaaAlreadyExistsException(
                    "Paa with ID " + paaId + " and version " + paaVersion + " already exists.",paaId, paaVersion);
        }

        // Create new PAA
        Paa paa = new Paa();
        paa.setId(paaId);
        paa.setVersion(paaVersion);
        paa.setRef(request.getRef());
        paa.setNom(request.getNom());
        paa.setAnnee(request.getAnnee());
        paa.setBudgetEstime(request.getBudgetEstime());
        RefParam defaultStatus = null;
        try {
            defaultStatus = refParamRepository.findById(1).get();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        paa.setStatus(defaultStatus);

        // Set status if provided
        if (request.getStatus() != null) {
            paa.setStatus(request.getStatus());
        }

        // Set Ac if provided
        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new EntityNotFoundException("Ac with ID " + request.getAcId() + " not found"));
            paa.setAc(ac);
        }

        // Set empty collections to avoid NPEs
        paa.setRealisations(new ArrayList<>());
        paa.setDocs(new ArrayList<>());

        // Save PAA initially without associations
        Paa savedPaa = repository.save(paa);

        // Handle documents if provided
        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = new ArrayList<>();
            for (Integer docId : request.getDocIds()) {
                Docs doc = docsRepository.findById(docId)
                        .orElseThrow(() -> new EntityNotFoundException("Document with ID " + docId + " not found"));

                // Set up bidirectional relationship
                if (doc.getPaas() == null) {
                    doc.setPaas(new ArrayList<>());
                }
                doc.getPaas().add(savedPaa);
                docs.add(doc);

                // Save the document with updated relationship
                docsRepository.save(doc);
            }

            // Update PAA with documents
            savedPaa.setDocs(docs);
        }

        if(request.getPaaFiles() != null) {
            try {
                processAndSaveDocuments(request.getPaaFiles(), savedPaa);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Process realisations if provided
        if (request.getRealisations() != null && !request.getRealisations().isEmpty()) {
            List<Realisation> realisations = new ArrayList<>();

            for (RealisationRequestForPaa realisationRequest : request.getRealisations()) {
                // Create a new Realisation
                Realisation realisation = new Realisation();

                // Generate a new ID for the realisation
                Integer maxRealisationId = realisationRepository.findMaxId();
                Integer realisationId = (maxRealisationId != null) ? maxRealisationId + 1 : 1;

                // Always use version 1 for newly created realisations
                realisation.setId(realisationId);
                realisation.setVersion(1);
//                realisation.setDocs(new ArrayList<>());
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
                System.out.println("the relisations have files");
                System.out.println(realisationRequest.getRealisationFiles());
                if(realisationRequest.getRealisationFiles() != null) {
                    try {
                        processAndSaveRelisationsDocuments(realisationRequest.getRealisationFiles(), realisation);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                // Set up the relationship with PAA
                List<Paa> paaList = new ArrayList<>();
                paaList.add(savedPaa);
                realisation.setPaas(paaList);

                // Handle Lots if provided
                if (realisationRequest.getLots() != null && !realisationRequest.getLots().isEmpty()) {
                    Set<Lot> lots = new HashSet<>();

                    for (LotResponse lotRequest : realisationRequest.getLots()) {
                        Lot lot = new Lot();

                        // Generate ID if not provided
                        Integer lotId = lotRequest.getId();
                        if (lotId == null) {
                            Integer maxLotId = lotRepository.findMaxId();
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

            // Update the PAA with the list of realisations
            savedPaa.setRealisations(realisations);
        }

        // Save again to persist all associations
        return repository.save(savedPaa);
    }

    private boolean realisationNeedsUpdate(Realisation existingRealisation, RealisationRequestForPaa dto) {
        // Check if any field is different
        if (dto.getRealisationEnvisagees() != null &&
                !dto.getRealisationEnvisagees().equals(existingRealisation.getRealisationEnvisagees())) {
            return true;
        }

        // Check service
        if (dto.getService() != null && (existingRealisation.getService() == null ||
                !dto.getService().getId().equals(existingRealisation.getService().getId()))) {
            return true;
        }

        // Check direction
        if (dto.getDirection() != null && (existingRealisation.getDirection() == null ||
                !dto.getDirection().getId().equals(existingRealisation.getDirection().getId()))) {
            return true;
        }

        // Add more field checks as needed...

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
                List<Integer> existingDocIds = existingRealisation.getDocs().stream()
                        .map(Docs::getId)
                        .toList();

                for (Integer docId : dto.getDocIds()) {
                    if (!existingDocIds.contains(docId)) {
                        return true;
                    }
                }
            }
        }

        // Check for lot changes that require Realisation version increment (adding/removing)
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
                Set<Integer> requestLotIds = dto.getLots().stream()
                        .filter(lotResponse -> lotResponse.getId() != null)
                        .map(LotResponse::getId)
                        .collect(Collectors.toSet());

                Set<Integer> existingLotIds = existingRealisation.getLots().stream()
                        .map(Lot::getId)
                        .collect(Collectors.toSet());

                // Different lot IDs (additions or removals) = major change
                if (!requestLotIds.equals(existingLotIds)) {
                    System.out.println("Major lot change: Different lot IDs");
                    return true;
                }

                // Description changes are considered minor and don't require Realisation version increment
                // That is handled separately in the updateWithVersioningWithLots method
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
            if (lotDto.getId() != null && lotDto.getId().equals(existingLot.getId()) &&
                    lotDto.getDescription() != null && !lotDto.getDescription().equals(existingLot.getDescription())) {
                return true;
            }
        }

        return false;
    }


    // Solution that modifies document processing with synchronized folder creation
// Add a synchronization object

    // Add a synchronization object
    private final Object folderCreationLock = new Object();

    private void processAndSaveDocuments(List<MultipartFile> files, Paa savedPaa) throws IOException {
        UUID uuid = UUID.randomUUID();
        // Add a file counter to ensure unique filenames even with the same UUID
        AtomicInteger fileCounter = new AtomicInteger(0);
        
        if (files != null && !files.isEmpty()) {
            // Create a unique folder path with timestamp to prevent any possible conflict
            String timestamp = String.valueOf(System.currentTimeMillis());
            // Use parent folder path from Constants but add our unique structure
            String baseFolder = Constants.PATH_ENTREPOT_GED_PAA.replace("/SIGMAP-DEV/ARCHIVE/PAA/", "/SIGMAP-DEV/ARCHIVE/PAA_DOCS/");
            //String baseFolder = Constants.PATH_ENTREPOT_GED_PAA;
//            String folderPath = String.format("%s/PAA_%d_%d_%s",
//                    baseFolder, savedPaa.getId(), savedPaa.getVersion(), timestamp);

            String folderPath = String.format("%s/PAA_%d/%d_%s",
                    baseFolder, savedPaa.getId(), savedPaa.getVersion(), timestamp);

            // Create the folder structure first, synchronizing to prevent conflicts
            String folderId;
            synchronized(folderCreationLock) {
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
                        String uniqueFileName = String.format("paa_%s_%s_%d%s",
                                uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        log.info("Uploading file: OriginalName={}, UniqueName={}, FolderPath={}, FolderID={}",
                                originalFilename, uniqueFileName, folderPath, folderId);

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
    }


    void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
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
            String folderPath = String.format("%s/REL_%d/%d_%s",
                    baseFolder, savedRealisation.getId(), savedRealisation.getVersion(), timestamp);

            // Create the folder structure first, synchronizing to prevent conflicts
            String folderId;
            synchronized(folderCreationLock) {
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
                        String uniqueFileName = String.format("realisation_%s_%s_%d%s",
                                uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        log.info("Uploading file: OriginalName={}, UniqueName={}, FolderPath={}, FolderID={}",
                                originalFilename, uniqueFileName, folderPath, folderId);

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
    }


    void processAndSaveRelisationsDocumentsForUpdate(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
        List<Docs> docs = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        AtomicInteger fileCounter = new AtomicInteger(0);

        if (files != null && !files.isEmpty()) {
            // Create a unique folder path with timestamp to avoid conflicts
            String timestamp = String.valueOf(System.currentTimeMillis());
            String baseFolder = Constants.PATH_ENTREPOT_GED_REALISATION.replace("/SIGMAP-DEV/ARCHIVE/REALISATION/", "/SIGMAP-DEV/ARCHIVE/REAL_DOCS/");
            String folderPath = String.format("%s/REL_%d/%d_%s",
                    baseFolder, savedRealisation.getId(), savedRealisation.getVersion(), timestamp);

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
//                        if (fileExtension.isEmpty()) {
//                            fileExtension = documentCommuneService.detectExtensionFromFileName(originalFilename);
//                        }

                        long fileSize = file.getSize();
                        String formattedSize = documentCommuneService.formatSize(fileSize);

                        String[] parts = originalFilename.split("\\.");
                        String baseName = parts[0];
                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
                        String uniqueFileName = String.format("realisation_%s_%s_%d%s",
                                uuid.toString(), timestamp, fileCounter.incrementAndGet(), extension);
                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);

                        log.info("Uploading file: OriginalName={}, UniqueName={}, FolderPath={}, FolderID={}",
                                originalFilename, uniqueFileName, folderPath, folderId);

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
    }



    // Helper method to upload a file to an existing folder by ID, not by path
//    private String uploadToExistingFolder(MultipartFile file, String folderId) throws IOException {
//        try {
//            // Create a modified version of alfrescoService.uploadDocument that takes folderId directly
//            // instead of folderPath, thus skipping the getOrCreateFolderByPath step
//
//            // Construct the URL for file upload to Alfresco
//            String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/"
//                    + folderId + "/children"
//                    + "?majorVersion=true&alf_ticket=" + alfrescoService.getTokenHolder().getTicket();
//
//            // Create HTTP headers for the request
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            // Convert the file to ByteArrayResource
//            ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
//                @Override
//                public String getFilename() {
//                    return file.getOriginalFilename();
//                }
//            };
//
//            // Create multipart request body
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("filedata", fileAsResource);
//            body.add("name", file.getOriginalFilename());
//            body.add("nodeType", "cm:content");
//
//            // Create request entity with headers and body
//            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
//
//            // Send POST request to upload the file
//            ResponseEntity<String> response = alfrescoService.getRestTemplate().exchange(url, HttpMethod.POST, request, String.class);
//
//            // Parse response to get document ID
//            String responseBody = response.getBody();
//            JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
//            String nodeId = jsonNode.path("entry").path("id").asText();
//            String versionLabel = jsonNode.path("entry").path("versionLabel").asText();
//
//            // Update document properties if needed
//            alfrescoService.updateProperties(nodeId);
//
//            // Return document ID and version
//            return nodeId + ";" + versionLabel;
//        } catch (Exception e) {
//            throw new IOException("Error uploading document to existing folder: " + e.getMessage(), e);
//        }
//    }

//    private void processAndSaveDocuments(List<MultipartFile> files, Paa savedPaa) throws IOException {
//        UUID uuid = UUID.randomUUID();
//        if (files != null && !files.isEmpty()) {
//            for (MultipartFile file : files) {
//                if (file != null && !file.isEmpty()) {
//                    try {
//                        Tika tika = new Tika();
//                        String detectedMimeType = tika.detect(file.getInputStream());
//                        String originalFilename = file.getOriginalFilename();
//
//                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
//                        if (fileExtension.isEmpty()) {
//                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
//                        }
//
//                        long fileSize = file.getSize();
//                        String formattedSize = documentCommuneService.formatSize(fileSize);
//
//                        // Use a more specific folder path with PAA identifier
//                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_PAA + "/%d/%d",
//                                savedPaa.getId(), savedPaa.getVersion());
//
//                        String[] parts = originalFilename.split("\\.");
//                        String baseName = parts[0];
//                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
//                        String uniqueFileName = "paa_" + uuid + "_" + extension;
//                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
//
//                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
//                        log.info(alfrescoId);
//
//                        Docs document = new Docs();
//                        document.setName(baseName);
//                        document.setAlfresco_id(alfrescoId);
//                        document.setPathFolder(folderPath);
//                        document.setFileSize(formattedSize);
//                        document.setContentType(detectedMimeType);
//                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
//                        document.setAlfrescoName(uniqueFileName);
//                        if (document.getPaas() == null) {
//                            document.setPaas(new ArrayList<>());
//                        }
//                        if (savedPaa.getDocs() == null) {
//                            savedPaa.setDocs(new ArrayList<>());
//                        }
//                        document.getPaas().add(savedPaa);
//                        savedPaa.getDocs().add(document);
//
//                        docsRepository.save(document);
//                    } catch (Exception e) {
//                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
//                    }
//                }
//            }
//        }
//    }


//    void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
//        List<Docs> docs = new ArrayList<>(); // Initialize collection for documents
//        UUID uuid = UUID.randomUUID();
//
//        if (files != null && !files.isEmpty()) {
//            for (MultipartFile file : files) {
//                if (file != null && !file.isEmpty()) {
//                    try {
//                        Tika tika = new Tika();
//                        String detectedMimeType = tika.detect(file.getInputStream());
//                        String originalFilename = file.getOriginalFilename();
//
//                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
//                        if (fileExtension.isEmpty()) {
//                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
//                        }
//
//                        long fileSize = file.getSize();
//                        String formattedSize = documentCommuneService.formatSize(fileSize);
//
//                        // Add an additional timestamp to make the folder path unique for each realisation upload
//                        String timestamp = String.valueOf(System.currentTimeMillis());
//                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_REALISATION + "/%d/%d/%s",
//                                savedRealisation.getId(), savedRealisation.getVersion(), timestamp);
//
//                        String[] parts = originalFilename.split("\\.");
//                        String baseName = parts[0];
//                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
//                        String uniqueFileName = "realisation_" + uuid + "_" + extension;
//                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
//
//                        // Log the path before uploading to help with debugging
//                        log.info("Uploading to Alfresco folder path: {}", folderPath);
//
//                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
//                        log.info("Uploaded document to Alfresco with ID: {}", alfrescoId);
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
//                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
//                    }
//                }
//            }
//        }
//
//        // Update the realisation's docs
//        savedRealisation.setDocs(docs);
//    }

    //TODO: nejah
//    private void processAndSaveDocuments(List<MultipartFile> files, Paa savedPaa) throws IOException {
//        UUID uuid = UUID.randomUUID();
//        if (files != null && !files.isEmpty()) {
//            for (MultipartFile file : files) {
//                if (file != null && !file.isEmpty()) {
//                    try {
//                        Tika tika = new Tika();
//                        String detectedMimeType = tika.detect(file.getInputStream());
//                        String originalFilename = file.getOriginalFilename();
//
//                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
//                        if (fileExtension.isEmpty()) {
//                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
//                        }
//
//                        long fileSize = file.getSize();
//                        String formattedSize = documentCommuneService.formatSize(fileSize);
//
//                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_PAA+ "/%d/%d",
//                                savedPaa.getId(), savedPaa.getVersion());
//
//                        String[] parts = originalFilename.split("\\.");
//                        String baseName = parts[0];
//                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
//                        String uniqueFileName = "paa_" + uuid + "_" + extension;
//                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
//
//                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
//                        log.info(alfrescoId);
//
//                        Docs document = new Docs();
//                        document.setName(baseName);
//                        document.setAlfresco_id(alfrescoId);
//                        document.setPathFolder(folderPath);
//                        document.setFileSize(formattedSize);
//                        document.setContentType(detectedMimeType);
//                        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
//                        document.setAlfrescoName(uniqueFileName);
//                        if (document.getPaas() == null) {
//                            document.setPaas(new ArrayList<>());
//                        }
//                        if (savedPaa.getDocs() == null) {
//                            savedPaa.setDocs(new ArrayList<>());
//                        }
//                        document.getPaas().add(savedPaa);
//                        savedPaa.getDocs().add(document);
//
//                        docsRepository.save(document);
//                    } catch (Exception e) {
//                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
//                    }
//                }
//            }
//        }
//    }
//
//    void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
//        List<Docs> docs = new ArrayList<>(); // Initialize collection for documents
//        UUID uuid = UUID.randomUUID();
//
//        if (files != null && !files.isEmpty()) {
//            for (MultipartFile file : files) {
//                if (file != null && !file.isEmpty()) {
//                    try {
//                        Tika tika = new Tika();
//                        String detectedMimeType = tika.detect(file.getInputStream());
//                        String originalFilename = file.getOriginalFilename();
//
//                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
//                        if (fileExtension.isEmpty()) {
//                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
//                        }
//
//                        long fileSize = file.getSize();
//                        String formattedSize = documentCommuneService.formatSize(fileSize);
//
//                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_REALISATION + "/%d/%d",
//                                savedRealisation.getId(), savedRealisation.getVersion());
//
//                        String[] parts = originalFilename.split("\\.");
//                        String baseName = parts[0];
//                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
//                        String uniqueFileName = "realisation_" + uuid + "_" + extension;
//                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
//
//                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
//                        log.info("Uploaded document to Alfresco with ID: {}", alfrescoId);
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
//                        throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
//                    }
//                }
//            }
//        }
//
//        // Update the realisation's docs
//        savedRealisation.setDocs(docs);
//    }


//    void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
//        List<Docs> docs = new ArrayList<>();
//        UUID uuid = UUID.randomUUID();
//
//        if (files != null && !files.isEmpty()) {
//            for (MultipartFile file : files) {
//                if (file != null && !file.isEmpty()) {
//                    try {
//                        Tika tika = new Tika();
//                        String detectedMimeType = tika.detect(file.getInputStream());
//                        String originalFilename = file.getOriginalFilename();
//
//                        String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
//                        if (fileExtension.isEmpty()) {
//                            fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
//                        }
//
//                        long fileSize = file.getSize();
//                        String formattedSize = documentCommuneService.formatSize(fileSize);
//
//                        String folderPath = String.format(Constants.PATH_ENTREPOT_GED_REALISATION + "/%d/%d",
//                                savedRealisation.getId(), savedRealisation.getVersion());
//
//                        String[] parts = originalFilename.split("\\.");
//                        String baseName = parts[0];
//                        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
//                        String uniqueFileName = "realisation_" + uuid + "_" + extension;
//                        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
//
//                        // Log the path before uploading to help with debugging
//                        log.info("Uploading to Alfresco folder path: {}", folderPath);
//
//                        String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
//                        log.info("Uploaded document to Alfresco with ID: {}", alfrescoId);
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
//                        log.info("Saved document with ID: {} for Realisation ID: {}", document.getId(), savedRealisation.getId());
//                    } catch (Exception e) {
////                        log.error("Failed to process or upload document '{}': {}", originalFilename, e.getMessage(), e);
////                        throw new IOException("Failed to process or upload document: " + originalFilename + " - " + e.getMessage(), e);
//                    }
//                } else {
//                    log.warn("Skipping empty or null file");
//                }
//            }
//        } else {
//            log.info("No files provided for Realisation ID: {}", savedRealisation.getId());
//        }
//
//        // Update the realisation's docs
//        savedRealisation.setDocs(docs);
//    }


//void processAndSaveRelisationsDocuments(List<MultipartFile> files, Realisation savedRealisation) throws IOException {
//    List<Docs> docs = new ArrayList<>();
//    UUID uuid = UUID.randomUUID();
//
//    if (files != null && !files.isEmpty()) {
//        for (MultipartFile file : files) {
//            if (file != null && !file.isEmpty()) {
//                try {
//                    Tika tika = new Tika();
//                    String detectedMimeType = tika.detect(file.getInputStream());
//                    String originalFilename = file.getOriginalFilename();
//
//                    String fileExtension = documentCommuneService.detectExtensionFromMimeType(detectedMimeType);
//                    if (fileExtension.isEmpty()) {
//                        fileExtension = documentCommuneService.detectExtensionFromMimeType(originalFilename);
//                    }
//
//                    long fileSize = file.getSize();
//                    String formattedSize = documentCommuneService.formatSize(fileSize);
//
//                    String folderPath = String.format(Constants.PATH_ENTREPOT_GED_REALISATION + "/%d/%d",
//                            savedRealisation.getId(), savedRealisation.getVersion());
//
//                    String[] parts = originalFilename.split("\\.");
//                    String baseName = parts[0];
//                    String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
//                    String uniqueFileName = "realisation_" + uuid + "_" + System.currentTimeMillis() + extension; // Add timestamp for extra uniqueness
//                    MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
//
//                    log.info("Uploading file: {} to folder: {}", uniqueFileName, folderPath);
//
//                    // Check for existing file in Alfresco (optional, requires alfrescoService method)
//                    // if (alfrescoService.fileExists(folderPath, uniqueFileName)) {
//                    //     throw new IOException("File already exists: " + uniqueFileName);
//                    // }
//
//                    String alfrescoId = alfrescoService.uploadDocument(renamedFile, folderPath);
//                    log.info("Uploaded document to Alfresco with ID: {}", alfrescoId);
//
//                    Docs document = new Docs();
//                    document.setName(baseName);
//                    document.setAlfresco_id(alfrescoId);
//                    document.setPathFolder(folderPath);
//                    document.setFileSize(formattedSize);
//                    document.setContentType(detectedMimeType);
//                    document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
//                    document.setAlfrescoName(uniqueFileName);
//
//                    if (document.getRealisations() == null) {
//                        document.setRealisations(new ArrayList<>());
//                    }
//                    if (savedRealisation.getDocs() == null) {
//                        savedRealisation.setDocs(new ArrayList<>());
//                    }
//
//                    document.getRealisations().add(savedRealisation);
//                    savedRealisation.getDocs().add(document);
//
//                    docsRepository.save(document);
//                    docs.add(document);
//                } catch (Exception e) {
//                    log.error("Failed to process or upload document: {}", e.getMessage(), e);
//                    throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
//                }
//            }
//        }
//    }
//
//    savedRealisation.setDocs(docs);
//}


}