package com.sigmap.sigmap_ppm.ppms;
import com.sigmap.common.lots.mappers.LotMapper;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.request.PpmBaseRequestDto;
import com.sigmap.common.ppms.request.PpmQuery;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import com.sigmap.common.ppms.response.PpmResponse;
import com.sigmap.common.realisations.mappers.RealisationMapper;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;

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
    public ResponseEntity<Page<PpmResponse>> findAll(
            PpmQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Ppm> ppms = ppmService.findAll(query.toSpecification(), pageable);
        Page<PpmResponse> responses = ppms.map(PpmMapper.INSTANCE::map);
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
    public ResponseEntity<Page<PpmResponse>> findAllDeleted(
            PpmQuery query, Pageable pageable) {
        Page<Ppm> ppms = ppmService.findAllDeleted(pageable);
        Page<PpmResponse> responses = ppms.map(PpmMapper.INSTANCE::map);
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
    public ResponseEntity<PpmResponse> findById(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer) {
        Ppm ppm = ppmService.findById(ppmId, ppmVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        PpmResponse response = PpmMapper.INSTANCE.map(ppm);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new ppm.
     *
     * @param request the request to create the ppm from
     * @return the created ppm
     */
    @PostMapping
    public ResponseEntity<PpmResponse> create(@RequestBody PpmRequest request) {
        Ppm ppm = ppmService.create(request);
        PpmResponse response = PpmMapper.INSTANCE.map(ppm);
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
    public ResponseEntity<PpmResponse> update(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer,
            @RequestBody PpmRequest request) {
        Ppm updatedPpm = ppmService.update(ppmId, ppmVer, request);
        PpmResponse response = PpmMapper.INSTANCE.map(updatedPpm);
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
    public ResponseEntity<PpmResponse> deleteById(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer) {
        ppmService.delete(ppmId, ppmVer);
        Ppm deleted = ppmService.findDeletedById(ppmId, ppmVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deleted Ppm not found"));
        PpmResponse response = PpmMapper.INSTANCE.map(deleted);
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
    public ResponseEntity<PpmResponse> updateWithVersioning(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer,
            @RequestBody PpmBaseRequestDto request) {

        logger.info("Updating PPM " + ppmId + " version " + ppmVer + " with versioning");
        logger.info("Request: " + request);

        Ppm updatedPpm = ppmService.updateWithVersioning(ppmId, ppmVer, request);

        PpmResponse response = PpmMapper.INSTANCE.map(updatedPpm);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a PPM by creating a new version with updated data.
     * Automatically uses the latest version of the PPM.
     * <p>
     * This endpoint handles hierarchical versioning:
     * - If any PPM field changes, a new PPM version is created
     * - If any Realisation is added, modified, or removed, a new PPM version is created
     * - If a Lot within a Realisation changes, both Realisation and PPM versions increment
     * - If any document is added/modified/removed, a new PPM version is created
     *
     * @param ppmId the ID of the PPM to update
     * @param request the request containing fields to update (can be PpmWithLotsRequestDto or PpmBaseRequestDto)
     * @return the newly created version of the PPM, or the existing version if no changes
     */
    @PutMapping("/{ppmId}/version")
    public ResponseEntity<PpmResponse> updateWithVersioningLatest(
            @PathVariable Integer ppmId,
            @RequestBody PpmBaseRequestDto request) {

        Ppm updatedPpm;

        // Check if this is a PpmWithLotsRequestDto
        if (request instanceof PpmWithLotsRequestDto) {
            // If it's a PpmWithLotsRequestDto, use the specialized method
            updatedPpm = ppmService.updateWithVersioningWithLots(ppmId, (PpmWithLotsRequestDto) request);
        } else {
            // Otherwise use the standard method
            updatedPpm = ppmService.updateWithVersioning(ppmId, request);
        }

        PpmResponse response = PpmMapper.INSTANCE.map(updatedPpm);
        return ResponseEntity.ok(response);
    }

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

}