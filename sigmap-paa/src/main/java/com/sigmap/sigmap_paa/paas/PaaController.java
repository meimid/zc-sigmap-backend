package com.sigmap.sigmap_paa.paas;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.exceptions.PaaAlreadyExistsException;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.mappers.PaaDetailedMapper;
import com.sigmap.common.paas.mappers.PaaMapper;
import com.sigmap.common.paas.request.PaaQuery;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.request.PaaRequestGlobal;
import com.sigmap.common.paas.response.PaaResponse;
import com.sigmap.common.paas.response.PaaResponseWithRealisations;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.request.RealisationRequestForPaa;
import com.sigmap.common.realisations.response.RealisationResponse;
import com.sigmap.common.lots.mappers.LotMapper;
import com.sigmap.common.lots.response.LotResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/paas")
public class PaaController {
    private final Logger logger = LoggerFactory.getLogger(PaaController.class);
    private final PaaService paaService;

    public PaaController(PaaService paaService) {
        this.paaService = paaService;
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
    public ResponseEntity<Page<PaaResponse>> findAll(
            PaaQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Paa> paas = paaService.findAll(query.toSpecification(), pageable);
        Page<PaaResponse> responses = paas.map(PaaMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
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
            @PathVariable Integer paaVer) throws PaaNotFound {
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
//    @PostMapping("/with-relationships")
    @PostMapping(value="/with-relationships",consumes = {"multipart/form-data"})

//    public ResponseEntity<PaaResponseWithRealisations> createWithRelationships(@RequestBody PaaRequestGlobal request) throws PaaAlreadyExistsException {
    public ResponseEntity<PaaResponseWithRealisations> createWithRelationships(
            @ModelAttribute("request") PaaRequestGlobal request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson) throws PaaAlreadyExistsException, AcNotFound {
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

//    @PutMapping("/{paaId}/{paaVer}")
@PutMapping(value="/{paaId}/{paaVer}",consumes = {"multipart/form-data"})
public ResponseEntity<PaaResponseWithRealisations> update(
        @PathVariable Integer paaId,
        @PathVariable Integer paaVer,
        @ModelAttribute("request") PaaRequestGlobal request) throws PaaNotFound {
    logger.info("Updating PAA ID: {} version: {} without versionning", paaId, paaVer);
    Paa updatedPaa = paaService.update(paaId, paaVer, request);

    // Use the detailed mapper to create a response with specialised DTOs
    PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(updatedPaa);

    return ResponseEntity.ok(response);
}

    private void processRealisationLotsFromJson(PaaRequestGlobal request, String lotsJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // The JSON should be in format: { "0": [{"id": 1, "description": "lot 1"}, ...], "1": [...] }
            // Where the keys are realisation indices
            Map<String, List<Map<String, Object>>> lotsMap = objectMapper.readValue(lotsJson,
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {});

            // Process each realisation's lots
            for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
                int realisationIndex = Integer.parseInt(entry.getKey());

                // Skip if index is out of bounds
                if (realisationIndex >= request.getRealisations().size()) {
                    logger.warn("Skipping lots for realisation index {} as it's out of bounds", realisationIndex);
                    continue;
                }

                // Get the target realisation
                RealisationRequestForPaa realisation = request.getRealisations().get(realisationIndex);
                Set<LotResponse> lots = new HashSet<>();

                // Process lots for this realisation
                for (Map<String, Object> lotData : entry.getValue()) {
                    LotResponse lot = new LotResponse();

                    if (lotData.containsKey("id")) {
                        lot.setId(((Number) lotData.get("id")).intValue());
                    }

                    if (lotData.containsKey("version")) {
                        lot.setVersion(((Number) lotData.get("version")).intValue());
                    }

                    if (lotData.containsKey("description")) {
                        lot.setDescription((String) lotData.get("description"));
                    }

                    lots.add(lot);
                }

                // Set the processed lots on the realisation
                realisation.setLots(lots);
                logger.info("Added {} lots to realisation at index {}", lots.size(), realisationIndex);
            }
        } catch (Exception e) {
            logger.error("Failed to parse lots JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to parse lots JSON: " + e.getMessage(), e);
        }
    }


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
            @RequestBody PaaRequestGlobal request) throws PaaNotFound {
        logger.info("Updating PAA ID: {} version: {} with versioning", paaId, paaVer);
        Paa updatedPaa = paaService.updateWithVersioning(paaId, paaVer, request);

        // Use the detailed mapper to create a response with specialised DTOs
        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(updatedPaa);

        return ResponseEntity.ok(response);
    }


    @PutMapping(value="/{paaId}/versioning/multi", consumes = {"multipart/form-data"})
    public ResponseEntity<PaaResponseWithRealisations> updateWithVersioningMultipart(
            @PathVariable Integer paaId,
            @ModelAttribute("request") PaaRequestGlobal request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson) throws PaaNotFound {
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

        Paa updatedPaa = paaService.updateWithVersioning(paaId, request);

        // Use the detailed mapper to create a response with specialised DTOs
        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(updatedPaa);

        return ResponseEntity.ok(response);
    }
//    @PutMapping(value="/{paaId}/versioning/multi", consumes = {"multipart/form-data"})
//    public ResponseEntity<PaaResponseWithRealisations> updateWithVersioningMultipart(
//            @PathVariable Integer paaId,
//            @ModelAttribute("request") PaaRequestGlobal request) throws PaaNotFound {
//        logger.info("Updating PAA ID: {} version: {} with versioning", paaId);
//        Paa updatedPaa = paaService.updateWithVersioning(paaId, request);
//
//        // Use the detailed mapper to create a response with specialised DTOs
//        PaaResponseWithRealisations response = PaaDetailedMapper.INSTANCE.mapDetailed(updatedPaa);
//
//        return ResponseEntity.ok(response);
//    }

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
            @RequestBody PaaRequestGlobal request) throws PaaNotFound {
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
            @PathVariable Integer paaVer) {

        paaService.delete(paaId, paaVer);
        Paa deleted = paaService.findDeletedById(paaId, paaVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deleted Paa not found"));
        PaaResponse response = PaaMapper.INSTANCE.map(deleted);
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
            @PathVariable Integer paaVer) throws PaaNotFound {
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
            @PathVariable Integer paaVer) throws PaaNotFound {
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
            @PathVariable Integer realisationId) throws PaaNotFound {
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
}