package com.sigmap.sigmap_paa.controller;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.response.AgpmWebsiteResponse;
import com.sigmap.common.financement.request.FinancementBaseRequestDto;
import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.financement.response.FinancementResponse;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.exceptions.*;
import com.sigmap.common.paas.mappers.PaaDetailedMapper;
import com.sigmap.common.paas.mappers.PaaMapper;
import com.sigmap.common.paas.request.PaaQuery;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.request.PaaRequestGlobal;
import com.sigmap.common.paas.response.*;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.request.RealisationRequestForPaa;
import com.sigmap.common.realisations.response.RealisationResponse;
import com.sigmap.common.lots.mappers.LotMapper;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.sigmap_paa.service.PaaFlowableService;
import com.sigmap.sigmap_paa.service.dto.response.TaskDetailsDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
@Slf4j
@RestController
@RequestMapping("/api/paas")
public class PaaController {
    private final Logger logger = LoggerFactory.getLogger(PaaController.class);
    private final PaaService paaService;
    private final ObjectMapper objectMapper;
    @Autowired
    private PaaFlowableService flowableService;

    public PaaController(PaaService paaService,ObjectMapper objectMapper) {
        this.paaService = paaService;
        this.objectMapper = objectMapper;
    }

    /**
     * Initialize collections in binding process to avoid "Cannot get element with index 0 from Collection of size 0" errors
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Set.class, new CustomCollectionEditor(Set.class) {
            @Override
            protected Object convertElement(Object element) {
                return super.convertElement(element);
            }
        });
    }

    /**
     * Return a page of paas based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of paas
     */

    @GetMapping
    public ResponseEntity<Page<PaaResponseWithRealisations>> findAll(
            PaaQuery query, Pageable pageable) throws PaaNotFound, AcNotFound {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Paa> paas = paaService.findAll(query.toSpecification(), pageable);
        Page<PaaResponseWithRealisations> responses = paas.map(PaaDetailedMapper.INSTANCE::mapDetailed);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/current_year")
    public ResponseEntity<PaaResponseWithRealisations> getPaaOfCurrentYear() {
        Paa paas = paaService.getPaaOfCurrentYear();
        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(paas);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current_year/user")
    public ResponseEntity<Page<PaaResponseWithRealisations>> getCurrentYearPaasByUser(Pageable pageable) {
        Page<Paa> paas = paaService.findCurrentYearPaasByUser(pageable);
        Page<PaaResponseWithRealisations> responses = paas.map(PaaDetailedMapper.INSTANCE::mapDetailed);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/current_year/user/acs_without_paa")
    public ResponseEntity<List<AcResponseDto>> getAcsWithoutPaaForCurrentYear() throws AcNotFound {
        List<Ac> acsWithoutPaa = paaService.getAcsWithoutPaaForCurrentYear();
        List<AcResponseDto> response = acsWithoutPaa.stream()
                .map(AcMapper.INSTANCE::mapToDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Return a page of deleted paas based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted paas
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<PaaResponse>> findAllDeleted(
            PaaQuery query, Pageable pageable) {
        Page<Paa> paas = paaService.findAllDeleted(query.toSpecification(), pageable);
        System.out.println("the paas are " + paas.getContent());
        Page<PaaResponse> responses = paas.map(PaaMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    /**
     * Return a paa based on the given id and version.
     *
     * @param paaId the id of the paa to return
     * @param paaVer the version of the paa to return
     * @return the paa
     */
    @GetMapping("/{paaId}/{paaVer}")
    public ResponseEntity<PaaResponseWithRealisations> findById(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) throws PaaNotFound, AcNotFound {
        Paa paa = paaService.findById(paaId, paaVer)
                .orElseThrow(() -> new PaaNotFound("Paa with ID " + paaId + " and version " + paaVer + " was not found in the database.",List.of(paaId),List.of(paaVer)));
        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(paa);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{paaId}")
    public ResponseEntity<PaaResponseWithRealisations> getPaaById(@PathVariable Integer paaId) throws  PaaNotFound {
        Paa paa = paaService.findByIdPaa(paaId, null);
        PaaResponseWithRealisations paaResponse = PaaDetailedMapper.INSTANCE.mapDetailed(paa);
        return ResponseEntity.ok(paaResponse);
    }

    /**
     * Create a new paa.
     *
     * @param request the request to create the paa from
     * @return the created paa
     */
    @PostMapping
    public ResponseEntity<PaaResponse> create(@RequestBody PaaRequest request) throws PaaAlreadyExistsException {
        Paa paa = paaService.create(request);
        PaaResponse response = PaaMapper.INSTANCE.map(paa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Create a new PAA with related Realisations and Lots in a single operation.
     * This endpoint allows for creating a complete PAA hierarchy with all its relationships
     * in a single API call.
     *
     * The request uses specialized DTOs:
     * - RealisationRequestForPaa: A simpler version of RealisationRequest without ID and version fields
     * - All IDs and versions will be auto-generated for new entities
     *
     * @param request the global request containing PAA, Realisations, and Lots data
     * @return the created PAA with all established relationships and specialized DTOs
     * @throws PaaAlreadyExistsException if a PAA with the generated ID and version already exists
     */
    @PostMapping(value="/with-relationships",consumes = {"multipart/form-data"})
    public ResponseEntity<PaaResponseWithRealisations> createWithRelationships(
            @ModelAttribute("request") PaaRequestGlobal request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson) throws PaaAlreadyExistsException, AcNotFound, PaaCreationLimitExceededException {
        logger.info("Creating PAA with relationships using RealisationRequestForPaa");

        // Process lots from JSON string if provided
        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsFromJson(request, realisationsLotsJson);
                logger.info("Successfully processed lots from JSON string");
            } catch (Exception e) {
                logger.error("Error processing lots JSON: {}", e.getMessage());
            }
        }
        Paa paa = paaService.createWithRelationships(request);

        // Use the detailed mapper that includes specialized DTOs
        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(paa);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/create_per_ac", consumes = {"multipart/form-data"})
    public ResponseEntity<PaaResponseWithRealisations> createPaaPerAc(
            @ModelAttribute("request") PaaRequestGlobal request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
            @RequestParam(value = "realisationsFinancementsJson", required = false) String realisationsFinancementsJson)  // ADD THIS PARAMETER
            throws PaaAlreadyExistsException, AcNotFound, PaaCreationLimitExceededException, IOException {

        logger.info("Creating PAA per AC with relationships using PaaRequestGlobal");

        // Process lots from JSON string if provided
        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsFromJson(request, realisationsLotsJson);
                logger.info("Successfully processed lots from JSON string");
            } catch (Exception e) {
                logger.error("Error processing lots JSON: {}", e.getMessage());
            }
        }

        // ADDED THIS: Process financements from JSON string if provided
        if (realisationsFinancementsJson != null && !realisationsFinancementsJson.isEmpty()) {
            try {
                processRealisationFinancementsFromJson(request, realisationsFinancementsJson);
                logger.info("Successfully processed financements from JSON string");
            } catch (Exception e) {
                logger.error("Error processing financements JSON: {}", e.getMessage());
            }
        }

        Paa paa = paaService.createPaaPerAc(request);
        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(paa);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    private void processRealisationFinancementsFromJson(PaaRequestGlobal request, String realisationsFinancementsJson) {
        if (realisationsFinancementsJson == null || realisationsFinancementsJson.isEmpty()) {
            return;
        }

        try {
            log.info("Processing financements JSON: {}", realisationsFinancementsJson);
            Map<String, List<Map<String, Object>>> financementMap = objectMapper.readValue(
                    realisationsFinancementsJson,
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {}
            );

            log.info("Parsed {} realisation indices from financements JSON", financementMap.size());

            List<RealisationRequestForPaa> realisations = request.getRealisations();
            if (realisations == null) {
                realisations = new ArrayList<>();
                request.setRealisations(realisations);
            }

            for (Map.Entry<String, List<Map<String, Object>>> entry : financementMap.entrySet()) {
                int index;
                try {
                    index = Integer.parseInt(entry.getKey());
                } catch (NumberFormatException e) {
                    log.warn("Invalid realisation index: {}. Skipping.", entry.getKey());
                    continue;
                }

                if (index >= realisations.size()) {
                    log.warn("Skipping financements for realisation index {} as it's out of bounds (size: {})", index, realisations.size());
                    continue;
                }

                List<FinancementRequest> financementRequests = new ArrayList<>();
                for (Map<String, Object> financementData : entry.getValue()) {
                    FinancementRequest financementRequest = new FinancementRequest();
                    // Explicitly map fields
                    if (financementData.containsKey("id")) {
                        Object id = financementData.get("id");
                        if (id instanceof Number) {
                            financementRequest.setFinancementId(((Number) id).intValue());
                        }
                    }
                    if (financementData.containsKey("monnaieId")) {
                        Object monnaieId = financementData.get("monnaieId");
                        if (monnaieId instanceof Number) {
                            financementRequest.setMonnaieId(((Number) monnaieId).intValue());
                        }
                    }
                    if (financementData.containsKey("budgetEstimer")) {
                        Object budgetEstimer = financementData.get("budgetEstimer");
                        if (budgetEstimer instanceof Number) {
                            financementRequest.setBudgetEstimer(BigDecimal.valueOf(((Number) budgetEstimer).doubleValue()));
                        }
                    }
                    if (financementData.containsKey("sourceFinancement")) {
                        Object sourceFinancement = financementData.get("sourceFinancement");
                        if (sourceFinancement instanceof Map) {
                            Map<String, Object> sourceFinancementMap = (Map<String, Object>) sourceFinancement;
                            if (sourceFinancementMap.containsKey("id")) {
                                Object sourceId = sourceFinancementMap.get("id");
                                if (sourceId instanceof Number) {
                                    RefParam refParam = new RefParam();
                                    refParam.setId(((Number) sourceId).intValue());
                                    financementRequest.setSourceFinancement(refParam);
                                }
                            }
                        }
                    }
                    financementRequests.add(financementRequest);
                }

                log.info("Added {} financements to realisation at index {}", financementRequests.size(), index);
                log.info("{}", financementRequests);
                realisations.get(index).setFinancements(financementRequests);
            }

            log.info("Successfully processed financements from JSON string");
        } catch (IOException e) {
            log.error("Error parsing financements JSON: {}", realisationsFinancementsJson, e);
            throw new IllegalArgumentException("Failed to parse financements JSON: " + e.getMessage());
        }
    }
    //    private void processRealisationFinancementsFromJson(PaaRequestGlobal request, String financementsJson) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            logger.info("Processing financements JSON: {}", financementsJson);
//
//            // Validate input
//            if (financementsJson == null || financementsJson.trim().isEmpty()) {
//                logger.warn("Financements JSON is empty or null, skipping processing");
//                return;
//            }
//
//            // Parse JSON into a map of realisation indices to lists of financement data
//            Map<String, List<Map<String, Object>>> financementsMap = objectMapper.readValue(financementsJson,
//                    new TypeReference<Map<String, List<Map<String, Object>>>>() {});
//            logger.info("Parsed {} realisation indices from financements JSON", financementsMap.size());
//
//            // Ensure realisations list exists
//            if (request.getRealisations() == null) {
//                request.setRealisations(new ArrayList<>());
//                logger.warn("Realisations list was null, initialized as empty");
//            }
//
//            // Process each realisation's financements
//            for (Map.Entry<String, List<Map<String, Object>>> entry : financementsMap.entrySet()) {
//                int realisationIndex = Integer.parseInt(entry.getKey());
//
//                // Skip if index is out of bounds
//                if (realisationIndex >= request.getRealisations().size()) {
//                    logger.warn("Skipping financements for realisation index {} as it's out of bounds (size: {})",
//                            realisationIndex, request.getRealisations().size());
//                    continue;
//                }
//
//                // Get the target realisation
//                RealisationRequestForPaa realisation = request.getRealisations().get(realisationIndex);
//                if (realisation == null) {
//                    logger.warn("Realisation at index {} is null, skipping", realisationIndex);
//                    continue;
//                }
//
//                // CORRECTED: Use FinancementBaseRequestDto for input processing
//                List<FinancementRequest> financements = new ArrayList<>();
//
//                // Process financements for this realisation
//                for (Map<String, Object> financementData : entry.getValue()) {
//                    FinancementRequest financement = new FinancementRequest();
//
//                    if (financementData.containsKey("monnaieId")) {
//                        financement.setMonnaieId(((Number) financementData.get("monnaieId")).intValue());
//                        logger.debug("Set monnaieId to {} for financement", financement.getMonnaieId());
//                    } else {
//                        logger.warn("Financement data missing monnaieId, skipping financement");
//                        continue;
//                    }
//
//                    if (financementData.containsKey("sourceFinancement")) {
//                        Map<String, Object> sourceFinancementObj = (Map<String, Object>) financementData.get("sourceFinancement");
//                        Integer sourceId = ((Number) sourceFinancementObj.get("id")).intValue(); // Use .get("id")
//
//                        // Create RefParam object from ID
//                        RefParam sourceFinancement = new RefParam();
//                        sourceFinancement.setId(sourceId);
//
//                        financement.setSourceFinancement(sourceFinancement);
//                        logger.debug("Set sourceFinancement ID to {} for financement", sourceId);
//                    } else {
//                        logger.debug("Financement data missing sourceFinancement, setting to null");
//                    }
//
//                    if (financementData.containsKey("budgetEstimer")) {
//                        Object budgetObj = financementData.get("budgetEstimer");
//                        if (budgetObj instanceof Number) {
//                            financement.setBudgetEstimer(new BigDecimal(budgetObj.toString()));
//                            logger.debug("Set budgetEstimer to {} for financement with monnaieId: {}",
//                                    financement.getBudgetEstimer(), financement.getMonnaieId());
//                        } else {
//                            logger.warn("Invalid budgetEstimer format for financement with monnaieId: {}, setting to null",
//                                    financement.getMonnaieId());
//                        }
//                    } else {
//                        logger.debug("Financement data missing budgetEstimer, setting to null");
//                    }
//
//                    financements.add(financement);
//                }
//
//                // Set the processed financements on the realisation
//                realisation.setFinancements(financements); // Changed method name
//                logger.info("Added {} financements to realisation at index {}", financements.size(), realisationIndex);
//                logger.info(financements.toString());
//            }
//        } catch (Exception e) {
//            logger.error("Failed to parse financements JSON: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to parse financements JSON: " + e.getMessage(), e);
//        }
//    }


//    @PostMapping(value = "/create_per_ac", consumes = {"multipart/form-data"})
//    public ResponseEntity<PaaResponseWithRealisations> createPaaPerAc(
//            @ModelAttribute("request") PaaRequestGlobal request,
//            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson)
//            throws PaaAlreadyExistsException, AcNotFound, PaaCreationLimitExceededException, IOException, IOException {
//        logger.info("Creating PAA per AC with relationships using PaaRequestGlobal");
//
//        // Process lots from JSON string if provided
//        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
//            try {
//                processRealisationLotsFromJson(request, realisationsLotsJson);
//                logger.info("Successfully processed lots from JSON string");
//            } catch (Exception e) {
//                logger.error("Error processing lots JSON: {}", e.getMessage());
//            }
//        }
//
//        Paa paa = paaService.createPaaPerAc(request);
//
//        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(paa);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

@PutMapping(value="/{paaId}/{paaVer}",consumes = {"multipart/form-data"})
public ResponseEntity<PaaResponseWithRealisations> update(
        @PathVariable Integer paaId,
        @PathVariable Integer paaVer,
        @ModelAttribute("request") PaaRequestGlobal request,
        @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
        @RequestParam(value = "realisationsFinancementsJson", required = false) String realisationsFinancementsJson
) throws PaaNotFound, PaaInvalidStateException {

    logger.info("Updating PAA ID: {} version: {} without versionning", paaId, paaVer);
    if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
        try {
            processRealisationLotsFromJson(request, realisationsLotsJson);
            logger.info("Successfully processed lots from JSON string");
        } catch (Exception e) {
            logger.error("Error processing lots JSON: {}", e.getMessage());
        }
    }

    if (realisationsFinancementsJson != null && !realisationsFinancementsJson.isEmpty()) {
        try {
//            log.info(realisationsFinancementsJson);
            processRealisationFinancementsFromJson(request, realisationsFinancementsJson);
            logger.info("Successfully processed financements from JSON string");
        } catch (Exception e) {
            logger.error("Error processing financements JSON: {}", e.getMessage());
        }
    }

    Paa updatedPaa = paaService.update(paaId, paaVer, request);

    // Use the detailed mapper to create a response with specialised DTOs
    PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(updatedPaa);

    return ResponseEntity.ok(response);
}
    private void processRealisationLotsFromJson(PaaRequestGlobal request, String lotsJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            logger.info("Processing lots JSON: {}", lotsJson);

            // Validate input
            if (lotsJson == null || lotsJson.trim().isEmpty()) {
                logger.warn("Lots JSON is empty or null, skipping processing");
                return;
            }

            // Parse JSON into a map of realisation indices to lists of lot data
            Map<String, List<Map<String, Object>>> lotsMap = objectMapper.readValue(lotsJson,
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {});
            logger.info("Parsed {} realisation indices from lots JSON", lotsMap.size());

            // Ensure realisations list exists
            if (request.getRealisations() == null) {
                request.setRealisations(new ArrayList<>());
                logger.warn("Realisations list was null, initialized as empty");
            }

            // Process each realisation's lots
            for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
                int realisationIndex = Integer.parseInt(entry.getKey());

                // Skip if index is out of bounds
                if (realisationIndex >= request.getRealisations().size()) {
                    logger.warn("Skipping lots for realisation index {} as it's out of bounds (size: {})",
                            realisationIndex, request.getRealisations().size());
                    continue;
                }

                // Get the target realisation
                RealisationRequestForPaa realisation = request.getRealisations().get(realisationIndex);
                if (realisation == null) {
                    logger.warn("Realisation at index {} is null, skipping", realisationIndex);
                    continue;
                }

                List<LotResponse> lots = new ArrayList<>();

                // Process lots for this realisation
                for (Map<String, Object> lotData : entry.getValue()) {
                    LotResponse lot = new LotResponse();

                    if (lotData.containsKey("id")) {
                        lot.setId(((Number) lotData.get("id")).intValue());
                    } else {
                        logger.debug("Lot data missing id, setting to null");
                    }

                    if (lotData.containsKey("version")) {
                        lot.setVersion(((Number) lotData.get("version")).intValue());
                    } else {
                        logger.debug("Lot data missing version, setting to null");
                    }

                    if (lotData.containsKey("description")) {
                        lot.setDescription((String) lotData.get("description"));
                    } else {
                        logger.warn("Lot data missing description, skipping lot");
                        continue;
                    }

                    // MODIFIED: Handle budget field
                    if (lotData.containsKey("budget")) {
                        Object budgetObj = lotData.get("budget");
                        if (budgetObj instanceof Number) {
                            lot.setBudget(new java.math.BigDecimal(budgetObj.toString()));
                            logger.debug("Set budget to {} for lot with description: {}", lot.getBudget(), lot.getDescription());
                        } else {
                            logger.warn("Invalid budget format for lot with description: {}, setting to null", lot.getDescription());
                        }
                    } else {
                        logger.debug("Lot data missing budget, setting to null");
                    }

                    lots.add(lot);
                }

                // Set the processed lots on the realisation
                realisation.setLots(lots);
                logger.info("Added {} lots to realisation at index {}", lots.size(), realisationIndex);
                logger.info(lots.toString());
            }
        } catch (Exception e) {
            logger.error("Failed to parse lots JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse lots JSON: " + e.getMessage(), e);
        }
    }
    //    private void processRealisationLotsFromJson(PaaRequestGlobal request, String lotsJson) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            logger.info("Processing lots JSON: {}", lotsJson);
//
//            // Validate input
//            if (lotsJson == null || lotsJson.trim().isEmpty()) {
//                logger.warn("Lots JSON is empty or null, skipping processing");
//                return;
//            }
//
//            // Parse JSON into a map of realisation indices to lists of lot data
//            Map<String, List<Map<String, Object>>> lotsMap = objectMapper.readValue(lotsJson,
//                    new TypeReference<Map<String, List<Map<String, Object>>>>() {});
//            logger.info("Parsed {} realisation indices from lots JSON", lotsMap.size());
//
//            // Ensure realisations list exists
//            if (request.getRealisations() == null) {
//                request.setRealisations(new ArrayList<>());
//                logger.warn("Realisations list was null, initialized as empty");
//            }
//
//            // Process each realisation's lots
//            for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
//                int realisationIndex = Integer.parseInt(entry.getKey());
//
//                // Skip if index is out of bounds
//                if (realisationIndex >= request.getRealisations().size()) {
//                    logger.warn("Skipping lots for realisation index {} as it's out of bounds (size: {})",
//                            realisationIndex, request.getRealisations().size());
//                    continue;
//                }
//
//                // Get the target realisation
//                RealisationRequestForPaa realisation = request.getRealisations().get(realisationIndex);
//                if (realisation == null) {
//                    logger.warn("Realisation at index {} is null, skipping", realisationIndex);
//                    continue;
//                }
//
//                List<LotResponse> lots = new ArrayList<>();
//
//                // Process lots for this realisation
//                for (Map<String, Object> lotData : entry.getValue()) {
//                    LotResponse lot = new LotResponse();
//
//                    if (lotData.containsKey("id")) {
//                        lot.setId(((Number) lotData.get("id")).intValue());
//                    } else {
//                        logger.debug("Lot data missing id, setting to null");
//                    }
//
//                    if (lotData.containsKey("version")) {
//                        lot.setVersion(((Number) lotData.get("version")).intValue());
//                    } else {
//                        logger.debug("Lot data missing version, setting to null");
//                    }
//
//                    if (lotData.containsKey("description")) {
//                        lot.setDescription((String) lotData.get("description"));
//                    } else {
//                        logger.warn("Lot data missing description, skipping lot");
//                        continue;
//                    }
//
//                    lots.add(lot);
//                }
//
//                // Set the processed lots on the realisation
//                realisation.setLots(lots);
//                logger.info("Added {} lots to realisation at index {}", lots.size(), realisationIndex);
//                log.info(lots.toString());
//            }
//        } catch (Exception e) {
//            logger.error("Failed to parse lots JSON: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to parse lots JSON: " + e.getMessage(), e);
//        }
//    }

    /**
     * Updates a PAA by creating a new version with the updated data.
     * The old version is preserved in the database.
     *
     * @param paaId the ID of the PAA to update
     * @param paaVer the current version of the PAA to update
     * @param request the request containing the updated data
     * @return the newly created version of the PAA with detailed realisation and lot information
     * @throws PaaNotFound if the PAA is not found or is deleted
     */
    @PutMapping("/{paaId}/{paaVer}/versioning")
    public ResponseEntity<PaaResponseWithRealisations> updateWithVersioning(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer,
            @RequestBody PaaRequestGlobal request) throws PaaNotFound, RealisationNotFoundException, RealisationAlreadyExistsException {
        logger.info("Updating PAA ID: {} version: {} with versioning", paaId, paaVer);
        Paa updatedPaa = paaService.updateWithVersioning(paaId, paaVer, request);

        // Use the detailed mapper to create a response with specialised DTOs
        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(updatedPaa);

        return ResponseEntity.ok(response);
    }

//    /**
//     * Convert existing Realisation entities to RealisationRequestForPaa DTOs
//     * This ensures the request object has the proper structure for processing JSON updates
//     */
//    private List<RealisationRequestForPaa> convertExistingRealisationsToRequestDtos(List<Realisation> existingRealisations) {
//        List<RealisationRequestForPaa> requestDtos = new ArrayList<>();
//
//        if (existingRealisations != null) {
//            for (Realisation realisation : existingRealisations) {
//                RealisationRequestForPaa requestDto = new RealisationRequestForPaa();
//
//                // Copy basic fields
//                requestDto.setRealisationEnvisagees(realisation.getRealisationEnvisagees());
//                requestDto.setService(realisation.getService());
//                requestDto.setDirection(realisation.getDirection());
//                requestDto.setSourceFinancement(realisation.getSourceFinancement());
//                requestDto.setImputationBudgetaire(realisation.getImputationBudgetaire());
//                requestDto.setMonnaie(realisation.getMonnaie());
//                requestDto.setTypeMarcher(realisation.getTypeMarcher());
//                requestDto.setModePassation(realisation.getModePassation());
//                requestDto.setDatePrevLps(realisation.getDatePrevLps());
//                requestDto.setDatePrevAc(realisation.getDatePrevAc());
//                requestDto.setDatePrevDp(realisation.getDatePrevDp());
//                requestDto.setDatePrevAp(realisation.getDatePrevAp());
//                requestDto.setBudgetEstime(realisation.getBudgetEstime());
//                requestDto.setIsPublished(realisation.getIsPublished());
//                requestDto.setPublishedAt(realisation.getPublishedAt());
//                requestDto.setSort(realisation.getSort());
//
//                // Initialize empty collections for JSON processing
//                requestDto.setLots(new ArrayList<>());
//                requestDto.setFinancements(new ArrayList<>());
//
//                requestDtos.add(requestDto);
//            }
//        }
//
//        return requestDtos;
//    }


//    @PutMapping(value="/{paaId}/versioning/multi", consumes = {"multipart/form-data"})
//    public ResponseEntity<PaaResponseWithRealisations> updateWithVersioningMultipart(
//            @PathVariable Integer paaId,
//            @ModelAttribute("request") PaaRequestGlobal request,
//            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
//            @RequestParam(value = "realisationsFinancementsJson", required = false) String realisationsFinancementsJson
//    ) throws PaaNotFound, RealisationNotFoundException, RealisationAlreadyExistsException {
//
//        logger.info("Updating PAA ID: {} with versioning (multipart)", paaId);
//
//        // CRITICAL FIX: Populate realisations from existing PAA FIRST
//        if (request.getRealisations() == null || request.getRealisations().isEmpty()) {
//            Paa existingPaa = paaService.findLatestById(paaId).get();
//            request.setRealisations(convertExistingRealisationsToRequestDtos(existingPaa.getRealisations()));
//            logger.info("Populated {} realisations from existing PAA for processing", request.getRealisations().size());
//        }
//
//        // NOW both lots and financements processing will work
//        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
//            try {
//                processRealisationLotsFromJson(request, realisationsLotsJson);
//                logger.info("Successfully processed lots from JSON string");
//            } catch (Exception e) {
//                logger.error("Error processing lots JSON: {}", e.getMessage());
//            }
//        }
//
//        if (realisationsFinancementsJson != null && !realisationsFinancementsJson.isEmpty()) {
//            try {
//                processRealisationFinancementsFromJson(request, realisationsFinancementsJson);
//                logger.info("Successfully processed financements from JSON string");
//            } catch (Exception e) {
//                logger.error("Error processing financements JSON: {}", e.getMessage());
//            }
//        }
//
//        Paa updatedPaa = paaService.updateWithVersioning(paaId, request);
//        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(updatedPaa);
//
//        return ResponseEntity.ok(response);
//    }



    @PutMapping(value="/{paaId}/versioning/multi", consumes = {"multipart/form-data"})
    public ResponseEntity<PaaResponseWithRealisations> updateWithVersioningMultipart(
            @PathVariable Integer paaId,
            @ModelAttribute("request") PaaRequestGlobal request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
            @RequestParam(value = "realisationsFinancementsJson", required = false) String realisationsFinancementsJson
            ) throws PaaNotFound, RealisationNotFoundException, RealisationAlreadyExistsException {
        logger.info("Updating PAA ID: {} with versioning (multipart)", paaId);

        // Process lots from JSON string if provided
        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsFromJson(request, realisationsLotsJson);
                logger.info("Successfully processed lots from JSON string");
            } catch (Exception e) {
                logger.error("Error processing lots JSON: {}", e.getMessage());
            }
        }

//        // ADDED THIS: Process financements from JSON string if provided
        if (realisationsFinancementsJson != null && !realisationsFinancementsJson.isEmpty()) {
            try {
                processRealisationFinancementsFromJson(request, realisationsFinancementsJson);
                logger.info("Successfully processed financements from JSON string");
            } catch (Exception e) {
                logger.error("Error processing financements JSON: {}", e.getMessage());
            }
       }

        Paa updatedPaa = paaService.updateWithVersioning(paaId, request);

        // Use the detailed mapper to create a response with specialised DTOs
        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(updatedPaa);

        return ResponseEntity.ok(response);
    }

    /**
     * Updates a PAA by creating a new version with the updated data using the latest version.
     * The old version is preserved in the database.
     *
     * @param paaId the ID of the PAA to update
     * @param request the request containing the updated data
     * @return the newly created version of the PAA with detailed realisation and lot information
     * @throws PaaNotFound if no versions of the PAA exist or if all versions are deleted
     */
    @PutMapping("/{paaId}/versioning")
    public ResponseEntity<PaaResponseWithRealisations> updateWithVersioningLatest(
            @PathVariable Integer paaId,
            @RequestBody PaaRequestGlobal request) throws PaaNotFound, RealisationNotFoundException, RealisationAlreadyExistsException {
        logger.info("Updating latest version of PAA ID: {} with versioning", paaId);
        Paa updatedPaa = paaService.updateWithVersioning(paaId, request);

        // Use the detailed mapper to create a response with specialised DTOs
        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(updatedPaa);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a paa based on the given id and version.
     *
     * @param paaId the id of the paa to delete
     * @param paaVer the version of the paa to delete
     * @return the deleted paa
     */
    @DeleteMapping("/{paaId}/{paaVer}")
    public ResponseEntity<PaaResponse> deleteById(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) throws PaaNotFound, PaaInvalidStatusException, PaaSoftDelete {

        paaService.delete(paaId, paaVer);
        Paa deleted = paaService.findDeletedById(paaId, paaVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deleted Paa not found"));
        PaaResponse response = PaaMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{paaId}/{paaVer}/deletefronttest")
    public ResponseEntity<PaaResponseWithRealisations> deleteTest(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) throws PaaNotFound, PaaInvalidStatusException, PaaSoftDelete {

        paaService.deleteTest(paaId, paaVer);
        Paa deleted = paaService.findDeletedById(paaId, paaVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deleted Paa not found"));
        PaaResponseWithRealisations response = PaaMapper.INSTANCE.toDtoWithRelations(deleted);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all realisations associated with a specific PAA.
     *
     * @param paaId the id of the PAA
     * @param paaVer the version of the PAA
     * @return list of realisations associated with the PAA
     */
    @GetMapping("/{paaId}/{paaVer}/realisations")
    public ResponseEntity<List<RealisationResponse>> getPaaRealisations(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) throws PaaNotFound, AcNotFound {
        Paa paa = paaService.findById(paaId, paaVer)
                .orElseThrow(() -> new PaaNotFound("PAA not found",List.of(paaId), List.of(paaVer)));

        List<RealisationResponse> realisations = paa.getRealisations().stream()
                .map(RealisationMapper.INSTANCE::map)
                .toList();

        return ResponseEntity.ok(realisations);
    }

    /**
     * Get all realisations associated with the latest version of a specific PAA.
     *
     * @param paaId the id of the PAA
     * @return list of realisations associated with the latest version of the PAA
     */
    @GetMapping("/{paaId}/realisations")
    public ResponseEntity<List<RealisationResponse>> getPaaRealisationsLatest(
            @PathVariable Integer paaId) throws PaaNotFound {
        Paa paa = paaService.findLatestById(paaId)
                .orElseThrow(() -> new PaaNotFound("PAA not found",List.of(paaId), List.of(null)));

        List<RealisationResponse> realisations = paa.getRealisations().stream()
                .map(RealisationMapper.INSTANCE::map)
                .toList();

        return ResponseEntity.ok(realisations);
    }

    /**
     * Get all lots associated with a specific PAA.
     * This collects all lots from all realisations in the PAA.
     *
     * @param paaId the id of the PAA
     * @param paaVer the version of the PAA
     * @return list of lots associated with the PAA
     */
    @GetMapping("/{paaId}/{paaVer}/lots")
    public ResponseEntity<List<LotResponse>> getPaaLots(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) throws PaaNotFound, AcNotFound {
        Paa paa = paaService.findById(paaId, paaVer)
                .orElseThrow(() -> new PaaNotFound("PAA not found",List.of(paaId), List.of(null)));

        // Collect all lots from all realisations
        List<LotResponse> lots = paa.getRealisations().stream()
                .filter(realisation -> realisation.getLots() != null)
                .flatMap(realisation -> realisation.getLots().stream())
                .map(LotMapper.INSTANCE::map)
                .distinct() // Remove duplicates if any
                .toList();

        return ResponseEntity.ok(lots);
    }

    /**
     * Get all lots associated with the latest version of a specific PAA.
     * This collects all lots from all realisations in the latest PAA version.
     *
     * @param paaId the id of the PAA
     * @return list of lots associated with the latest version of the PAA
     */
    @GetMapping("/{paaId}/lots")
    public ResponseEntity<List<LotResponse>> getPaaLotsLatest(
            @PathVariable Integer paaId) throws PaaNotFound {
        Paa paa = paaService.findLatestById(paaId)
                .orElseThrow(() -> new PaaNotFound("PAA not found",List.of(paaId), List.of(null)));

        // Collect all lots from all realisations
        List<LotResponse> lots = paa.getRealisations().stream()
                .filter(realisation -> realisation.getLots() != null)
                .flatMap(realisation -> realisation.getLots().stream())
                .map(LotMapper.INSTANCE::map)
                .distinct() // Remove duplicates if any
                .toList();

        return ResponseEntity.ok(lots);
    }

    /**
     * Get all lots associated with a specific realisation in a PAA.
     *
     * @param paaId the id of the PAA
     * @param paaVer the version of the PAA
     * @param realisationId the id of the realisation
     * @return list of lots associated with the realisation
     */
    @GetMapping("/{paaId}/{paaVer}/realisations/{realisationId}/lots")
    public ResponseEntity<List<LotResponse>> getPaaRealisationLots(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer,
            @PathVariable Integer realisationId) throws PaaNotFound, AcNotFound {
        Paa paa = paaService.findById(paaId, paaVer)
                .orElseThrow(() -> new PaaNotFound("PAA not found",List.of(paaId), List.of(null)));

        // Find the specified realisation
        var realisation = paa.getRealisations().stream()
                .filter(r -> r.getId().equals(realisationId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Realisation not found with ID: " + realisationId));

        // Get lots from the realisation
        List<LotResponse> lots = realisation.getLots() != null ?
                realisation.getLots().stream()
                        .map(LotMapper.INSTANCE::map)
                        .toList() :
                List.of();

        return ResponseEntity.ok(lots);
    }

    /**
     * Get all lots associated with a specific realisation in the latest version of a PAA.
     *
     * @param paaId the id of the PAA
     * @param realisationId the id of the realisation
     * @return list of lots associated with the realisation in the latest PAA version
     */
    @GetMapping("/{paaId}/realisations/{realisationId}/lots")
    public ResponseEntity<List<LotResponse>> getPaaRealisationLotsLatest(
            @PathVariable Integer paaId,
            @PathVariable Integer realisationId) throws PaaNotFound {
        Paa paa = paaService.findLatestById(paaId)
                .orElseThrow(() -> new PaaNotFound("PAA not found",List.of(paaId), List.of(null)));

        // Find the specified realisation
        var realisation = paa.getRealisations().stream()
                .filter(r -> r.getId().equals(realisationId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Realisation not found with ID: " + realisationId));

        // Get lots from the realisation
        List<LotResponse> lots = realisation.getLots() != null ?
                realisation.getLots().stream()
                        .map(LotMapper.INSTANCE::map)
                        .toList() :
                List.of();

        return ResponseEntity.ok(lots);
    }


    /**
     * find page of paas with Ac Id*/

    @GetMapping("/ac/{acId}")
    public ResponseEntity<Page<PaaResponseDto>> findPaasByAcId(@PathVariable Integer acId, Pageable pageable) throws AcNotFound {
        Page<Paa> paas = paaService.findPaasByAcId(acId,pageable);
        Page<PaaResponseDto> responses = paas.map(PaaMapper.INSTANCE::toDto);
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves all versions of an Paa entity by its ID with full details in PaaResponseWithRealisations format.
     *
     * @param paaId   ID of the Paa entity
     * @param pageable Pagination and sorting parameters
     * @return ResponseEntity containing a page of PaaResponseWithRealisations
     * @throws PaaNotFound if no Paa entities are found for the ID
     */
    @GetMapping("/history/full/{paaId}")
    public ResponseEntity<Page<PaaResponseWithRealisations>> getPaaHistoryFull(@PathVariable Integer paaId, Pageable pageable) throws  PaaIdNotFound {
        log.info("Received request to fetch full history for Paa ID {}, page {}, size {}, sort {}",
                paaId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<Paa> paas = paaService.findAllVersionsById(paaId, pageable);
        Page<PaaResponseWithRealisations> paaResponseWithRealisations=paas.map(PaaMapper.INSTANCE::toDtoWithRelations);
        return ResponseEntity.ok(paaResponseWithRealisations);
    }

    /**
     * Retrieves all versions of an Paa entity by its ID with minimal details in PaaDto format.
     *
     * @param paaId   ID of the Paa entity
     * @param pageable Pagination and sorting parameters
     * @return ResponseEntity containing a page of PaaResponseDtoWithMetdata
     * @throws PaaIdNotFound if no Paa entities are found for the ID
     */
    @GetMapping("/history/minimal/{paaId}")
    public ResponseEntity<Page<PaaResponseDtoWithMetdata>> getPaaHistoryMinimal(@PathVariable Integer paaId, Pageable pageable) throws PaaIdNotFound {
        log.info("Received request to fetch full history for Paa ID {}, page {}, size {}, sort {}",
                paaId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<Paa> paas = paaService.findAllVersionsById(paaId, pageable);
        Page<PaaResponseDtoWithMetdata> paaResponseDtoWithMetdata=paas.map(PaaMapper.INSTANCE::toDtoWithMetadata);
        return ResponseEntity.ok(paaResponseDtoWithMetdata);
    }

    @GetMapping("/details/{paaId}")
    public ResponseEntity<TaskDetailsDTO> getPaaWithTasks(@PathVariable Integer paaId) {
        try {
            TaskDetailsDTO response = flowableService.getPaaWithTasks(paaId);
            return ResponseEntity.ok(response);
        } catch (PaaNotFound | AcNotFound e) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


}