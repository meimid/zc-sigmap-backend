package com.sigmap.sigmap_ppm.controller;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.lots.mappers.LotMapper;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.request.PpmBaseRequestDto;
import com.sigmap.common.ppms.request.PpmQuery;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import com.sigmap.common.ppms.response.PpmResponse;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.request.RealisationWithLotsRequestDto;
import com.sigmap.common.realisations.response.RealisationResponse;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/ppms")
public class PpmController {
    @Value("${tenant.id}")
    private String TENANT_ID;


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;
    private final PpmService ppmService;
    private final Logger logger = LoggerFactory.getLogger(PpmController.class);

    public PpmController(PpmService ppmService) {
        this.ppmService = ppmService;
    }

    /**
     * Return a page of ppms based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of ppms
     */
    @GetMapping
    public ResponseEntity<Page<PpmResponseDto>> findAll(
            PpmQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Ppm> ppms = ppmService.findAll(query.toSpecification(), pageable);
        Page<PpmResponseDto> responses = ppms.map(PpmMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(responses);
    }

    /**
     * Return a page of deleted ppms based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted ppms
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<PpmResponseDto>> findAllDeleted(
            PpmQuery query, Pageable pageable) {
        Page<Ppm> ppms = ppmService.findAllDeleted(pageable);
        Page<PpmResponseDto> responses = ppms.map(PpmMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(responses);
    }

    /**
     * Return a ppm based on the given id and version.
     *
     * @param ppmId the id of the ppm to return
     * @param ppmVer the version of the ppm to return
     * @return the ppm
     */
    @GetMapping("/{ppmId}/{ppmVer}")
    public ResponseEntity<PpmResponseDto> findById(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer) {
        Ppm ppm = ppmService.findById(ppmId, ppmVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        PpmResponseDto response = PpmMapper.INSTANCE.mapToDto(ppm);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new ppm.
     *
     * @param request the request to create the ppm from
     * @return the created ppm
     */
    @PostMapping
    public ResponseEntity<PpmResponseDto> create(@RequestBody PpmRequest request) {
        Ppm ppm = ppmService.create(request);
        PpmResponseDto response = PpmMapper.INSTANCE.mapToDto(ppm);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Create a new PPM with full support for relationships including documents.
     * This method allows uploading documents for both the PPM and its realisations.
     *
     * @param request the global request containing PPM, realisations, lots, and document data
     * @return the created PPM with all relationships established
     */
    @PostMapping("/with-relationships")
    public ResponseEntity<PpmResponseDto> createWithRelationships(
            @ModelAttribute("request") PpmWithLotsRequestDto request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson
    ) throws AcNotFound {
        logger.info("Creating PPM with relationships (documents, realisations, and lots)");

        // DIRECT CHECK FOR PARAMETER: Log the exact parameter value
        logger.info("######## PARAMETER CHECK ########");
        logger.info("realisationsLotsJson parameter received: {}", realisationsLotsJson != null ? "YES" : "NO");
        if (realisationsLotsJson != null) {
            logger.info("Parameter length: {}", realisationsLotsJson.length());
            logger.info("Parameter value: '{}'", realisationsLotsJson);

            // Try to manually parse the JSON here
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> parsed = mapper.readValue(realisationsLotsJson, Map.class);
                logger.info("Successfully parsed JSON with {} keys", parsed.size());
                for (String key : parsed.keySet()) {
                    logger.info("Key: '{}', Value type: {}", key, parsed.get(key).getClass().getName());
                }

                // Check if we have lots data in the expected format
                if (parsed.containsKey("0") && parsed.get("0") instanceof List) {
                    List<?> lots = (List<?>) parsed.get("0");
                    logger.info("Found {} lots for realisation index 0", lots.size());

                    // If we successfully parsed lots here, but they're not showing up in the entity
                    // Manually create and set them as a test
                    if (lots.size() > 0 && request.getRealisationsWithLots() != null &&
                            !request.getRealisationsWithLots().isEmpty()) {
                        RealisationWithLotsRequestDto realisation = request.getRealisationsWithLots().get(0);

                        // Create a test lot if lots is empty
                        if (realisation.getLots() == null || realisation.getLots().isEmpty()) {
                            logger.info("Creating a test lot for realisation 0");
                            Set<LotResponse> testLots = new HashSet<>();
                            LotResponse testLot = new LotResponse();
                            testLot.setDescription("Test Lot - Manually Added");
                            testLots.add(testLot);
                            realisation.setLots(testLots);
                            logger.info("Test lot added to realisation 0");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to manually parse JSON: {}", e.getMessage());
            }
        }
        logger.info("######## END PARAMETER CHECK ########");

        // Debug logs for request state before processing
        logger.info("Initial request state: realisations={}, realisationsWithLots={}, ppmFiles={}",
            (request.getRealisations() != null ? request.getRealisations().size() : "null"),
            (request.getRealisationsWithLots() != null ? request.getRealisationsWithLots().size() : "null"),
            (request.getPpmFiles() != null ? request.getPpmFiles().size() : "null"));

        // Process lots from JSON string if provided
        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsFromJson(request, realisationsLotsJson);
                logger.info("Successfully processed lots from JSON string");

                // Check if lots were actually added
                if (request.getRealisationsWithLots() != null) {
                    for (int i = 0; i < request.getRealisationsWithLots().size(); i++) {
                        RealisationWithLotsRequestDto realisation = request.getRealisationsWithLots().get(i);
                        logger.info("After processing: Realisation[{}] has {} lots",
                            i, (realisation.getLots() != null ? realisation.getLots().size() : "null"));

                        // If lots exist, log details of each lot
                        if (realisation.getLots() != null && !realisation.getLots().isEmpty()) {
                            for (LotResponse lot : realisation.getLots()) {
                                logger.info("  - Lot: id={}, description='{}'", lot.getId(), lot.getDescription());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing lots JSON: {}", e.getMessage(), e);
            }
        }

        Ppm ppm = ppmService.createWithRelationships(request);

        // Map the result to a response
        PpmResponseDto response = PpmMapper.INSTANCE.mapToDto(ppm);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing ppm based on the given id and version.
     *
     * @param ppmId   the id of the ppm to update
     * @param ppmVer  the version of the ppm to update
     * @param request the request containing updated ppm data
     * @return the updated ppm
     */

    @PutMapping("/{ppmId}/{ppmVer}")
    public ResponseEntity<PpmResponseDto> update(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer,
            @RequestBody PpmRequest request) {
        Ppm updatedPpm = ppmService.update(ppmId, ppmVer, request);
        PpmResponseDto response = PpmMapper.INSTANCE.mapToDto(updatedPpm);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a ppm based on the given id and version.
     *
     * @param ppmId the id of the ppm to delete
     * @param ppmVer the version of the ppm to delete
     * @return the deleted ppm
     */
    @DeleteMapping("/{ppmId}/{ppmVer}")
    public ResponseEntity<PpmResponseDto> deleteById(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer) {
        ppmService.delete(ppmId, ppmVer);
        Ppm deleted = ppmService.findDeletedById(ppmId, ppmVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deleted Ppm not found"));
        PpmResponseDto response = PpmMapper.INSTANCE.mapToDto(deleted);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all realisations associated with a specific PPM.
     *
     * @param ppmId the id of the PPM
     * @param ppmVer the version of the PPM
     * @return list of realisations associated with the PPM
     */
    @GetMapping("/{ppmId}/{ppmVer}/realisations")
    public ResponseEntity<List<RealisationResponse>> getPpmRealisations(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer) {
        Ppm ppm = ppmService.findById(ppmId, ppmVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PPM not found"));

        List<RealisationResponse> realisations = ppm.getRealisations().stream()
                .map(RealisationMapper.INSTANCE::map)
                .toList();

        return ResponseEntity.ok(realisations);
    }

    /**
     * Get all realisations associated with the latest version of a specific PPM.
     *
     * @param ppmId the id of the PPM
     * @return list of realisations associated with the latest version of the PPM
     */
    @GetMapping("/{ppmId}/realisations")
    public ResponseEntity<List<RealisationResponse>> getPpmRealisationsLatest(
            @PathVariable Integer ppmId) {
        Ppm ppm = ppmService.findLatestById(ppmId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PPM not found"));

        List<RealisationResponse> realisations = ppm.getRealisations().stream()
                .map(RealisationMapper.INSTANCE::map)
                .toList();

        return ResponseEntity.ok(realisations);
    }

    /**
     * Get all lots associated with a specific PPM.
     * This collects all lots from all realisations in the PPM.
     *
     * @param ppmId the id of the PPM
     * @param ppmVer the version of the PPM
     * @return list of lots associated with the PPM
     */
    @GetMapping("/{ppmId}/{ppmVer}/lots")
    public ResponseEntity<List<LotResponse>> getPpmLots(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer) {
        Ppm ppm = ppmService.findById(ppmId, ppmVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PPM not found"));

        // Collect all lots from all realisations
        List<LotResponse> lots = ppm.getRealisations().stream()
                .filter(realisation -> realisation.getLots() != null)
                .flatMap(realisation -> realisation.getLots().stream())
                .map(LotMapper.INSTANCE::map)
                .distinct() // Remove duplicates if any
                .toList();

        return ResponseEntity.ok(lots);
    }

    /**
     * Get all lots associated with the latest version of a specific PPM.
     * This collects all lots from all realisations in the latest PPM version.
     *
     * @param ppmId the id of the PPM
     * @return list of lots associated with the latest version of the PPM
     */
    @GetMapping("/{ppmId}/lots")
    public ResponseEntity<List<LotResponse>> getPpmLotsLatest(
            @PathVariable Integer ppmId) {
        Ppm ppm = ppmService.findLatestById(ppmId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PPM not found"));

        // Collect all lots from all realisations
        List<LotResponse> lots = ppm.getRealisations().stream()
                .filter(realisation -> realisation.getLots() != null)
                .flatMap(realisation -> realisation.getLots().stream())
                .map(LotMapper.INSTANCE::map)
                .distinct() // Remove duplicates if any
                .toList();

        return ResponseEntity.ok(lots);
    }

    /**
     * Get all lots associated with a specific realisation in a PPM.
     *
     * @param ppmId the id of the PPM
     * @param ppmVer the version of the PPM
     * @param realisationId the id of the realisation
     * @return list of lots associated with the realisation
     */
    @GetMapping("/{ppmId}/{ppmVer}/realisations/{realisationId}/lots")
    public ResponseEntity<List<LotResponse>> getPpmRealisationLots(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer,
            @PathVariable Integer realisationId) {
        Ppm ppm = ppmService.findById(ppmId, ppmVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PPM not found"));

        // Find the specified realisation
        var realisation = ppm.getRealisations().stream()
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
     * Get all lots associated with a specific realisation in the latest version of a PPM.
     *
     * @param ppmId the id of the PPM
     * @param realisationId the id of the realisation
     * @return list of lots associated with the realisation in the latest PPM version
     */
    @GetMapping("/{ppmId}/realisations/{realisationId}/lots")
    public ResponseEntity<List<LotResponse>> getPpmRealisationLotsLatest(
            @PathVariable Integer ppmId,
            @PathVariable Integer realisationId) {
        Ppm ppm = ppmService.findLatestById(ppmId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PPM not found"));

        // Find the specified realisation
        var realisation = ppm.getRealisations().stream()
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
     * Update a PPM by creating a new version with updated data.
     * <p>
     * This endpoint handles hierarchical versioning:
     * - If any PPM field changes, a new PPM version is created
     * - If any Realisation is added, modified, or removed, a new PPM version is created
     * - If a Lot within a Realisation changes, both Realisation and PPM versions increment
     * - If any document is added/modified/removed, a new PPM version is created
     *
     * @param ppmId the ID of the PPM to update
     * @param ppmVer the current version of the PPM
     * @param request the request containing fields to update (can be PpmWithLotsRequestDto or PpmBaseRequestDto)
     * @return the newly created version of the PPM, or the existing version if no changes
     */
    @PutMapping("/{ppmId}/{ppmVer}/version")
    public ResponseEntity<PpmResponseDto> updateWithVersioning(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer,
            @RequestBody PpmBaseRequestDto request) {

        logger.info("Updating PPM " + ppmId + " version " + ppmVer + " with versioning");
        logger.info("Request: " + request);

        Ppm updatedPpm = ppmService.updateWithVersioning(ppmId, ppmVer, request);

        PpmResponseDto response = PpmMapper.INSTANCE.mapToDto(updatedPpm);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a PPM by creating a new version with updated data, including document files.
     * Uses the latest version of the PPM and supports multipart form data for file uploads.
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing fields to update with support for file uploads
     * @param realisationsLotsJson optional JSON string for realisation lots if needed
     * @return the newly created version of the PPM
     */

    //TODO:fix processRealisationLotsFromJson(request, realisationsLotsJson);
    @PutMapping(value="/{ppmId}/versioning/multi", consumes = {"multipart/form-data"})
    public ResponseEntity<PpmResponseDto> updateWithVersioningMultipart(
            @PathVariable Integer ppmId,
            @ModelAttribute("request") PpmWithLotsRequestDto request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson) {
        logger.info("Updating PPM ID: {} with versioning (multipart)", ppmId);

        // Process lots from JSON string if provided
        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsFromJson(request, realisationsLotsJson);
                logger.info("Successfully processed lots from JSON string");
            } catch (Exception e) {
                logger.error("Error processing lots JSON: {}", e.getMessage());
            }
        }

        Ppm updatedPpm = ppmService.updateWithVersioning(ppmId, request);

        PpmResponseDto response = PpmMapper.INSTANCE.mapToDto(updatedPpm);
        return ResponseEntity.ok(response);
    }

    /**
     * Process realisation lots from a JSON string.
     * This method is used to handle complex lots data that might be difficult to send
     * directly in a multipart form.
     *
     * @param request the request to update with the parsed lots
     * @param lotsJson the JSON string containing lots data
     */

    private void processRealisationLotsFromJson(PpmWithLotsRequestDto request, String lotsJson) {
        try {
            logger.info("Starting to process realisationLotsJson: '{}'", lotsJson);

            // Check if it's just whitespace or empty braces
            if (lotsJson.trim().isEmpty() || lotsJson.trim().equals("{}")) {
                logger.warn("realisationsLotsJson is empty or just contains '{}'");
                return;
            }

            // Check if realisationsWithLots is initialized
            if (request.getRealisationsWithLots() == null || request.getRealisationsWithLots().isEmpty()) {
                logger.error("RealisationsWithLots list is null or empty - cannot process lots");
                return;
            }

            logger.info("RealisationsWithLots size before processing: {}", request.getRealisationsWithLots().size());

            ObjectMapper objectMapper = new ObjectMapper();
            // Try to log the raw parsed structure first
            try {
                Map<String, Object> rawParsed = objectMapper.readValue(lotsJson, new TypeReference<Map<String, Object>>() {});
                logger.info("Raw parsed JSON structure: {}", rawParsed);

                // Check if we have any realisation index keys
                if (rawParsed.isEmpty()) {
                    logger.warn("Parsed JSON is empty - no lots to process");
                    return;
                }

                // Check sample values to debug type issues
                for (Map.Entry<String, Object> entry : rawParsed.entrySet()) {
                    logger.info("Key '{}' has value of type: {}",
                        entry.getKey(), entry.getValue() != null ? entry.getValue().getClass().getName() : "null");
                }
            } catch (Exception e) {
                logger.error("Failed to parse raw JSON structure: {}", e.getMessage());
                // Continue with normal parsing to see if it works
            }

            // The JSON should be in format: { "0": [{"id": 1, "description": "lot 1"}, ...], "1": [...] }
            // Where the keys are realisation indices
            Map<String, List<Map<String, Object>>> lotsMap = objectMapper.readValue(lotsJson,
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {});

            logger.info("Parsed lotsMap size: {}", lotsMap.size());

            if (lotsMap.isEmpty()) {
                logger.warn("Parsed lotsMap is empty - no lots to process");
                return;
            }

            // Log each key-value entry in lotsMap
            for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
                logger.info("lotsMap key: '{}', value size: {}", entry.getKey(), entry.getValue().size());
            }

            // Create a modified copy of the request with lots
            // This ensures that the lots don't get lost during processing
            List<RealisationWithLotsRequestDto> modifiedRealisations = new ArrayList<>();
            for (int i = 0; i < request.getRealisationsWithLots().size(); i++) {
                RealisationWithLotsRequestDto originalRealisation = request.getRealisationsWithLots().get(i);
                RealisationWithLotsRequestDto copiedRealisation = new RealisationWithLotsRequestDto();

                // Copy all properties from original to copy
                copiedRealisation.setId(originalRealisation.getId());
                copiedRealisation.setVersion(originalRealisation.getVersion());
                copiedRealisation.setRealisationEnvisagees(originalRealisation.getRealisationEnvisagees());
                copiedRealisation.setService(originalRealisation.getService());
                copiedRealisation.setDirection(originalRealisation.getDirection());
                copiedRealisation.setSourceFinancement(originalRealisation.getSourceFinancement());
                copiedRealisation.setMonais(originalRealisation.getMonais());
                copiedRealisation.setTypeMarcher(originalRealisation.getTypeMarcher());
                copiedRealisation.setModeSelection(originalRealisation.getModeSelection());
                copiedRealisation.setDatePrevLps(originalRealisation.getDatePrevLps());
                copiedRealisation.setDatePrevAc(originalRealisation.getDatePrevAc());
                copiedRealisation.setDatePrevDp(originalRealisation.getDatePrevDp());
                copiedRealisation.setDatePrevAp(originalRealisation.getDatePrevAp());
                copiedRealisation.setBudgetEstime(originalRealisation.getBudgetEstime());
                copiedRealisation.setIsPublished(originalRealisation.getIsPublished());
                copiedRealisation.setPublishedAt(originalRealisation.getPublishedAt());
                copiedRealisation.setSort(originalRealisation.getSort());
                copiedRealisation.setDocIds(originalRealisation.getDocIds());
                copiedRealisation.setRealisationFiles(originalRealisation.getRealisationFiles());

                // Initialize lots collection even if there are no lots yet
                copiedRealisation.setLots(new HashSet<>());

                modifiedRealisations.add(copiedRealisation);
            }

            // Process each realisation's lots
            for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
                try {
                    int realisationIndex = Integer.parseInt(entry.getKey());

                    logger.info("Processing lots for realisation index: {} (entries: {})",
                        realisationIndex, entry.getValue().size());

                    // Skip if index is out of bounds
                    if (realisationIndex >= modifiedRealisations.size()) {
                        logger.warn("Skipping lots for realisation index {} as it's out of bounds", realisationIndex);
                        continue;
                    }

                    // Get the target realisation from our copied collection
                    RealisationWithLotsRequestDto realisation = modifiedRealisations.get(realisationIndex);
                    Set<LotResponse> lots = realisation.getLots(); // Already initialized

                    // Process lots for this realisation
                    for (Map<String, Object> lotData : entry.getValue()) {
                        LotResponse lot = new LotResponse();

                        logger.info("Processing lot data: {}", lotData);

                        // Safely handle ID - can be null, which is fine for new lots
                        Object idValue = lotData.get("id");
                        if (idValue != null) {
                            if (idValue instanceof Number) {
                                lot.setId(((Number) idValue).intValue());
                            } else if (idValue instanceof String) {
                                try {
                                    lot.setId(Integer.parseInt((String) idValue));
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid lot ID format: {}", idValue);
                                    // Leave ID as null
                                }
                            } else {
                                logger.warn("Unexpected lot ID type: {}", idValue.getClass().getName());
                                // Leave ID as null
                            }
                        } else {
                            logger.info("Lot ID is null (new lot)");
                        }

                        // Safely handle version - can be null for new lots
                        Object versionValue = lotData.get("version");
                        if (versionValue != null) {
                            if (versionValue instanceof Number) {
                                lot.setVersion(((Number) versionValue).intValue());
                            } else if (versionValue instanceof String) {
                                try {
                                    lot.setVersion(Integer.parseInt((String) versionValue));
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid lot version format: {}", versionValue);
                                    // Leave version as null
                                }
                            } else {
                                logger.warn("Unexpected lot version type: {}", versionValue.getClass().getName());
                                // Leave version as null
                            }
                        }

                        // Handle description - required field
                        Object descriptionValue = lotData.get("description");
                        if (descriptionValue != null) {
                            lot.setDescription(descriptionValue.toString());
                        } else {
                            logger.warn("Lot is missing required description field");
                            lot.setDescription(""); // Set empty string instead of null
                        }

                        lots.add(lot);
                        logger.info("Added lot: id={}, description='{}'",
                            lot.getId(), lot.getDescription());
                    }

                    // Set the processed lots on the realisation (shouldn't be necessary since we're modifying the collection directly)
                    // realisation.setLots(lots);
                    logger.info("Added {} lots to realisation at index {}", lots.size(), realisationIndex);
                } catch (NumberFormatException e) {
                    logger.error("Invalid realisation index format: {}", entry.getKey(), e);
                } catch (Exception e) {
                    logger.error("Error processing lots for realisation: {}", e.getMessage(), e);
                }
            }

            // Replace the realisations in the request with our modified copies
            // This is the key step - we're updating the request with our new copies that have the lots properly set
            request.setRealisationsWithLots(modifiedRealisations);

            // Log the state of all realisations after processing
            for (int i = 0; i < request.getRealisationsWithLots().size(); i++) {
                RealisationWithLotsRequestDto realisation = request.getRealisationsWithLots().get(i);
                logger.info("Final state: Realisation[{}] has {} lots",
                    i, (realisation.getLots() != null ? realisation.getLots().size() : "null"));
            }

        } catch (Exception e) {
            logger.error("Failed to parse lots JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse lots JSON: " + e.getMessage(), e);
        }
    }

//    /**
//     * Update a PPM by creating a new version with updated data.
//     * Automatically uses the latest version of the PPM.
//     * <p>
//     * This endpoint handles hierarchical versioning:
//     * - If any PPM field changes, a new PPM version is created
//     * - If any Realisation is added, modified, or removed, a new PPM version is created
//     * - If a Lot within a Realisation changes, both Realisation and PPM versions increment
//     * - If any document is added/modified/removed, a new PPM version is created
//     *
//     * @param ppmId the ID of the PPM to update
//     * @param request the request containing fields to update (can be PpmWithLotsRequestDto or PpmBaseRequestDto)
//     * @return the newly created version of the PPM, or the existing version if no changes
//     */
//    @PutMapping("/{ppmId}/version")
//    public ResponseEntity<PpmResponse> updateWithVersioningLatest(
//            @PathVariable Integer ppmId,
//            @RequestBody PpmBaseRequestDto request) {
//
//        Ppm updatedPpm;
//
//        // Check if this is a PpmWithLotsRequestDto
//        if (request instanceof PpmWithLotsRequestDto) {
//            // If it's a PpmWithLotsRequestDto, use the specialized method
//            updatedPpm = ppmService.updateWithVersioningWithLots(ppmId, (PpmWithLotsRequestDto) request);
//        } else {
//            // Otherwise use the standard method
//            updatedPpm = ppmService.updateWithVersioning(ppmId, request);
//        }
//
//        PpmResponse response = PpmMapper.INSTANCE.map(updatedPpm);
//        return ResponseEntity.ok(response);
//    }

    /**
     * Update a PPM by creating a new version with updated data.
     * This endpoint explicitly supports Lot creation and versioning.
     * <p>
     * This endpoint handles hierarchical versioning:
     * - If any PPM field changes, a new PPM version is created
     * - If any Realisation is added, modified, or removed, a new PPM version is created
     * - If a Lot within a Realisation changes, both Realisation and PPM versions increment
     * - New Lots without IDs are created and associated with their Realisations
     * - If any document is added/modified/removed, a new PPM version is created
     *
     * @param ppmId the ID of the PPM to update
     * @param ppmVer the current version of the PPM
     * @param request the request containing fields to update with explicit Lot support
     * @return the newly created version of the PPM, or the existing version if no changes
     */
    @PutMapping("/version-with-lots/{ppmId}/{ppmVer}")
    public ResponseEntity<PpmResponse> updateWithVersioningWithLots(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer,
            @RequestBody PpmWithLotsRequestDto request) {

        logger.info("Updating PPM " + ppmId + " version " + ppmVer + " with versioning and lots support");
        logger.info("Request class: " + request.getClass().getName());
        logger.info("Request: " + request);

        // Explicitly log the realisations with lots
        if (request.getRealisationsWithLots() != null) {
            logger.info("Realisations with lots: " + request.getRealisationsWithLots().size());
            for (int i = 0; i < request.getRealisationsWithLots().size(); i++) {
                logger.info("Realisation " + i + ": " + request.getRealisationsWithLots().get(i));
                logger.info("Lots for realisation " + i + ": " +
                        (request.getRealisationsWithLots().get(i).getLots() != null ?
                                request.getRealisationsWithLots().get(i).getLots().size() : "null"));
            }
        } else {
            logger.info("No realisations with lots in request");

            // Check regular realisations
            if (request.getRealisations() != null) {
                logger.info("Regular realisations: " + request.getRealisations().size());
                for (int i = 0; i < request.getRealisations().size(); i++) {
                    logger.info("Realisation " + i + ": " + request.getRealisations().get(i));
                    logger.info("Lots for realisation " + i + ": " +
                            (request.getRealisations().get(i).getLots() != null ?
                                    request.getRealisations().get(i).getLots().size() : "null"));
                }
            } else {
                logger.info("No regular realisations in request either");
            }
        }

        Ppm updatedPpm = ppmService.updateWithVersioningWithLots(ppmId, ppmVer, request);

        PpmResponse response = PpmMapper.INSTANCE.map(updatedPpm);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a PPM by creating a new version with updated data including Lots.
     * Automatically uses the latest version of the PPM.
     * <p>
     * This endpoint handles hierarchical versioning:
     * - If any PPM field changes, a new PPM version is created
     * - If any Realisation is added, modified, or removed, a new PPM version is created
     * - If a Lot within a Realisation changes, both Realisation and PPM versions increment
     * - New Lots without IDs are created and associated with their Realisations
     * - If any document is added/modified/removed, a new PPM version is created
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing fields to update with explicit Lot support
     * @return the newly created version of the PPM, or the existing version if no changes
     */
    @PutMapping("/version-with-lots/{ppmId}")
    public ResponseEntity<PpmResponse> updateWithVersioningWithLotsLatest(
            @PathVariable Integer ppmId,
            @RequestBody PpmWithLotsRequestDto request) {

        logger.info("Updating PPM " + ppmId + " with versioning and lots support (latest version)");
        logger.info("Request class: " + request.getClass().getName());
        logger.info("Request: " + request);

        // Add debug for raw request payload
        logger.info("Raw Request JSON: " + request);

        // EXPLICITLY HANDLE LOTS FOR DEBUGGING
        boolean lotsDetectedAndHandled = false;

        // Explicitly log the realisations with lots
        if (request.getRealisationsWithLots() != null) {
            logger.info("Realisations with lots: " + request.getRealisationsWithLots().size());
            for (int i = 0; i < request.getRealisationsWithLots().size(); i++) {
                logger.info("Realisation " + i + ": " + request.getRealisationsWithLots().get(i));
                logger.info("Lots for realisation " + i + ": " +
                        (request.getRealisationsWithLots().get(i).getLots() != null ?
                                request.getRealisationsWithLots().get(i).getLots().size() : "null"));

                // If we have lots in this realisation, flag for debug
                if (request.getRealisationsWithLots().get(i).getLots() != null &&
                        !request.getRealisationsWithLots().get(i).getLots().isEmpty()) {
                    lotsDetectedAndHandled = true;
                }
            }
        } else {
            logger.info("No realisations with lots in request");

            // Check regular realisations
            if (request.getRealisations() != null) {
                logger.info("Regular realisations: " + request.getRealisations().size());
                for (int i = 0; i < request.getRealisations().size(); i++) {
                    logger.info("Realisation " + i + ": " + request.getRealisations().get(i));
                    logger.info("Lots for realisation " + i + ": " +
                            (request.getRealisations().get(i).getLots() != null ?
                                    request.getRealisations().get(i).getLots().size() : "null"));

                    // If we have lots in this realisation, flag for debug
                    if (request.getRealisations().get(i).getLots() != null &&
                            !request.getRealisations().get(i).getLots().isEmpty()) {
                        lotsDetectedAndHandled = true;
                    }
                }
            } else {
                logger.info("No regular realisations in request either");
            }
        }

        // Log debug status
        logger.info("Lots detected and will be handled: " + lotsDetectedAndHandled);

        Ppm newVersionPpm = ppmService.updateWithVersioningWithLots(ppmId, request);
        PpmResponse response = PpmMapper.INSTANCE.map(newVersionPpm);
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint to directly create a lot for a realisation.
     * This is for debugging purposes only.
     */
    @PutMapping("/test-create-lot/{realisationId}/{realisationVer}")
    public ResponseEntity<String> testCreateLot(
            @PathVariable Integer realisationId,
            @PathVariable Integer realisationVer) {

        logger.info("Test creating lot for Realisation ID: " + realisationId + ", Version: " + realisationVer);

        try {
            // Get the RealisationService via ReflectionUtils from PpmService
            Object realisationService = java.lang.reflect.Field.class
                    .getDeclaredField("realisationService")
                    .get(ppmService);

            // Get the LotRepository via ReflectionUtils from PpmService
            Object lotRepository = java.lang.reflect.Field.class
                    .getDeclaredField("lotRepository")
                    .get(ppmService);

            // Get the RealisationRepository via ReflectionUtils from PpmService
            Object realisationRepository = java.lang.reflect.Field.class
                    .getDeclaredField("realisationRepository")
                    .get(ppmService);

            // Find the Realisation
            Method findByIdMethod = realisationRepository.getClass().getMethod("findById", com.sigmap.common.entity.CompositeId.class);
            Object realisationOpt = findByIdMethod.invoke(realisationRepository, new com.sigmap.common.entity.CompositeId(realisationId, realisationVer));

            if (((java.util.Optional<?>)realisationOpt).isEmpty()) {
                return ResponseEntity.badRequest().body("Realisation not found");
            }

            com.sigmap.common.realisations.Realisation realisation =
                    (com.sigmap.common.realisations.Realisation)((java.util.Optional<?>)realisationOpt).get();

            // Create a new Lot
            com.sigmap.common.lots.Lot newLot = new com.sigmap.common.lots.Lot();
            newLot.setDescription("Test Lot from Controller");
            newLot.setVersion(1);

            // Generate new ID
            Method findMaxIdMethod = lotRepository.getClass().getMethod("findMaxId");
            Integer maxId = (Integer)findMaxIdMethod.invoke(lotRepository);
            Integer lotId = maxId + 1;
            newLot.setId(lotId);

            // Save the new lot
            Method saveMethod = lotRepository.getClass().getMethod("save", com.sigmap.common.lots.Lot.class);
            com.sigmap.common.lots.Lot savedLot = (com.sigmap.common.lots.Lot)saveMethod.invoke(lotRepository, newLot);

            // Associate with existing realisation
            java.util.Set<com.sigmap.common.lots.Lot> realisationLots = realisation.getLots();
            if (realisationLots == null) {
                realisationLots = new java.util.HashSet<>();
            }
            realisationLots.add(savedLot);
            realisation.setLots(realisationLots);

            // Update the realisation
            Method realisationSaveMethod = realisationRepository.getClass().getMethod("save", com.sigmap.common.realisations.Realisation.class);
            com.sigmap.common.realisations.Realisation savedRealisation =
                    (com.sigmap.common.realisations.Realisation)realisationSaveMethod.invoke(realisationRepository, realisation);

            return ResponseEntity.ok("Lot created with ID: " + savedLot.getId() + " and associated with Realisation");

        } catch (Exception e) {
            logger.error("Error creating test lot", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Update a PPM and all its relationships (realizations, documents, lots) without versioning.
     * This method always updates version 1 of the PPM.
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing updated PPM data with documents and relationships
     * @return the updated PPM
     */
    @PutMapping(value="/{ppmId}/global", consumes = {"multipart/form-data"})
    public ResponseEntity<PpmResponse> updateGlobalWithoutVersionning(
            @PathVariable Integer ppmId,
            @ModelAttribute("request") PpmWithLotsRequestDto request) {
        logger.info("Updating PPM ID: {} without versioning (global update with docs and relationships)", ppmId);
        Ppm updatedPpm = ppmService.updateGlobalWithoutVersionning(ppmId, request);

        PpmResponse response = PpmMapper.INSTANCE.map(updatedPpm);
        return ResponseEntity.ok(response);
    }

    /**
     * Process the JSON string containing lot data for realisations specifically for update operations.
     * This method maps realisation indices to lots and adds them to the request.
     *
     * @param request             the request object to update
     * @param realisationsLotsJson the JSON string containing lot data keyed by realisation index
     */
    private void processRealisationLotsJsonForUpdate(PpmWithLotsRequestDto request, String realisationsLotsJson) {
        if (realisationsLotsJson == null || realisationsLotsJson.isEmpty()) {
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Parse the JSON string into a Map where keys are realisation indices (as strings)
            // and values are arrays of lot objects
            Map<String, List<Map<String, Object>>> lotsMap =
                    mapper.readValue(realisationsLotsJson, new TypeReference<Map<String, List<Map<String, Object>>>>() {});

            // Ensure we have realisationsWithLots in the request
            if (request.getRealisationsWithLots() == null) {
                logger.info("No realisationsWithLots in request, creating empty list");
                request.setRealisationsWithLots(new ArrayList<>());
            }

            // Process each realisation index and its lots
            for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
                try {
                    // Convert the key to an integer index
                    int realisationIndex = Integer.parseInt(entry.getKey());
                    List<Map<String, Object>> lotsData = entry.getValue();

                    // Check if the realisation at this index exists
                    if (realisationIndex >= 0 && realisationIndex < request.getRealisationsWithLots().size()) {
                        RealisationWithLotsRequestDto realisation = request.getRealisationsWithLots().get(realisationIndex);

                        // Initialize lots collection if needed
                        if (realisation.getLots() == null) {
                            realisation.setLots(new HashSet<>());
                        }

                        // Convert each lot data to a LotResponse object and add to the realisation
                        for (Map<String, Object> lotData : lotsData) {
                            LotResponse lot = new LotResponse();

                            // Set properties from the map
                            if (lotData.containsKey("id") && lotData.get("id") != null) {
                                if (lotData.get("id") instanceof Integer) {
                                    lot.setId((Integer) lotData.get("id"));
                                } else if (lotData.get("id") instanceof String && !((String) lotData.get("id")).isEmpty()) {
                                    try {
                                        lot.setId(Integer.parseInt((String) lotData.get("id")));
                                    } catch (NumberFormatException e) {
                                        logger.warn("Could not parse lot ID: {}", lotData.get("id"));
                                    }
                                }
                            }

                            if (lotData.containsKey("version") && lotData.get("version") != null) {
                                if (lotData.get("version") instanceof Integer) {
                                    lot.setVersion((Integer) lotData.get("version"));
                                } else if (lotData.get("version") instanceof String && !((String) lotData.get("version")).isEmpty()) {
                                    try {
                                        lot.setVersion(Integer.parseInt((String) lotData.get("version")));
                                    } catch (NumberFormatException e) {
                                        logger.warn("Could not parse lot version: {}", lotData.get("version"));
                                    }
                                }
                            }

                            if (lotData.containsKey("description")) {
                                lot.setDescription(lotData.get("description") != null ? lotData.get("description").toString() : null);
                            }

                            // Add the lot to the realisation
                            realisation.getLots().add(lot);
                            logger.info("Added lot to realisation index {}: {}", realisationIndex, lot);
                        }
                    } else {
                        logger.warn("Realisation index {} out of bounds (size: {})",
                                realisationIndex, request.getRealisationsWithLots().size());
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid realisation index: {}", entry.getKey(), e);
                }
            }

            logger.info("Processed JSON for {} realisation indices", lotsMap.size());

        } catch (Exception e) {
            logger.error("Error processing realisationsLotsJson: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process lots JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing PPM with full support for relationships including documents.
     * This method allows updating a PPM along with its realisations, lots, and document data.
     *
     * @param ppmId the ID of the PPM to update
     * @param ppmVer the version of the PPM to update
     * @param request the request containing updated PPM data, realisations, lots, and document files
     * @param realisationsLotsJson optional JSON string containing lot data for realisations
     * @return the updated PPM with all relationships established
     */
    @PutMapping("/{ppmId}/{ppmVer}/with-relationships")
    public ResponseEntity<PpmResponseDto> updateWithRelations(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer,
            @ModelAttribute("request") PpmWithLotsRequestDto request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson
    ) {
        logger.info("Updating PPM ID: {}, Version: {} with relationships (documents, realisations, and lots)", ppmId, ppmVer);

        // DIRECT CHECK FOR PARAMETER: Log the exact parameter value
        logger.info("######## PARAMETER CHECK ########");
        logger.info("realisationsLotsJson parameter received: {}", realisationsLotsJson != null ? "YES" : "NO");
        if (realisationsLotsJson != null) {
            logger.info("Parameter length: {}", realisationsLotsJson.length());
            logger.info("Parameter value: '{}'", realisationsLotsJson);

            // Try to manually parse the JSON here
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> parsed = mapper.readValue(realisationsLotsJson, Map.class);
                logger.info("Successfully parsed JSON with {} keys", parsed.size());
                for (String key : parsed.keySet()) {
                    logger.info("Key: '{}', Value type: {}", key, parsed.get(key).getClass().getName());
                }

                // Check if we have lots data in the expected format
                if (parsed.containsKey("0") && parsed.get("0") instanceof List) {
                    List<?> lots = (List<?>) parsed.get("0");
                    logger.info("Found {} lots for realisation index 0", lots.size());

                    // If we successfully parsed lots here, but they're not showing up in the entity
                    // Manually create and set them as a test
                    if (lots.size() > 0 && request.getRealisationsWithLots() != null &&
                            !request.getRealisationsWithLots().isEmpty()) {
                        RealisationWithLotsRequestDto realisation = request.getRealisationsWithLots().get(0);

                        // Create a test lot if lots is empty
                        if (realisation.getLots() == null || realisation.getLots().isEmpty()) {
                            logger.info("Creating a test lot for realisation 0");
                            Set<LotResponse> testLots = new HashSet<>();
                            LotResponse testLot = new LotResponse();
                            testLot.setDescription("Test Lot - Manually Added");
                            testLots.add(testLot);
                            realisation.setLots(testLots);
                            logger.info("Test lot added to realisation 0");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to manually parse JSON: {}", e.getMessage());
            }
        }
        logger.info("######## END PARAMETER CHECK ########");

        // Debug logs for request state before processing
        logger.info("Initial request state: realisations={}, realisationsWithLots={}, ppmFiles={}",
            (request.getRealisations() != null ? request.getRealisations().size() : "null"),
            (request.getRealisationsWithLots() != null ? request.getRealisationsWithLots().size() : "null"),
            (request.getPpmFiles() != null ? request.getPpmFiles().size() : "null"));

        // Process lots from JSON string if provided
        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsJsonForUpdate(request, realisationsLotsJson);
                logger.info("Successfully processed lots from JSON string");

                // Check if lots were actually added
                if (request.getRealisationsWithLots() != null) {
                    for (int i = 0; i < request.getRealisationsWithLots().size(); i++) {
                        RealisationWithLotsRequestDto realisation = request.getRealisationsWithLots().get(i);
                        logger.info("After processing: Realisation[{}] has {} lots",
                            i, (realisation.getLots() != null ? realisation.getLots().size() : "null"));

                        // If lots exist, log details of each lot
                        if (realisation.getLots() != null && !realisation.getLots().isEmpty()) {
                            for (LotResponse lot : realisation.getLots()) {
                                logger.info("  - Lot: id={}, description='{}'", lot.getId(), lot.getDescription());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing lots JSON: {}", e.getMessage(), e);
            }
        }

        Ppm updatedPpm = ppmService.updateWithRelationships(ppmId, ppmVer, request);
        PpmResponseDto response = PpmMapper.INSTANCE.mapToDto(updatedPpm);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a detailed PPM by its ID, including all its realisations.
     * This endpoint returns the latest version of the PPM.
     *
     * @param ppmId the ID of the PPM to retrieve
     * @return the detailed PPM response with realisations
     * @throws ResponseStatusException if the PPM is not found
     */
    @GetMapping("/{ppmId}")
    public ResponseEntity<PpmResponseDto> getPpmById(@PathVariable Integer ppmId) {
        logger.info("Getting detailed PPM with ID: {}", ppmId);
        
        Ppm ppm = ppmService.findByIdPpm(ppmId, null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PPM not found with ID: " + ppmId));

        PpmResponseDto response = PpmMapper.INSTANCE.mapToDto(ppm);
        return ResponseEntity.ok(response);
    }

}