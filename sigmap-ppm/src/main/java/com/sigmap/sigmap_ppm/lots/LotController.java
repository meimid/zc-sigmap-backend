package com.sigmap.sigmap_ppm.lots;

import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotService;
import com.sigmap.common.lots.mappers.LotMapper;
import com.sigmap.common.lots.request.LotQuery;
import com.sigmap.common.lots.request.LotRequest;
import com.sigmap.common.lots.response.LotResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Pageable;



@RestController
@RequestMapping("/api/lots")
public class LotController {
    private final Logger logger = LoggerFactory.getLogger(LotController.class);
    private final LotService lotService;

    public LotController(LotService lotService) {
        this.lotService = lotService;
    }

    /**
     * Return a page of lots based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of lots
     */
    @GetMapping
    public ResponseEntity<Page<LotResponse>> findAll(
            LotQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());


        Page<Lot> lots = lotService.findAll(query.toSpecification(), pageable);
        Page<LotResponse> responses = lots.map(LotMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    /**
     * Return a page of deleted lots based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted lots
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<LotResponse>> findAllDeleted(
            LotQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Lot> deletedLots = lotService.findAllDeleted(query.toSpecification(), pageable);
        Page<LotResponse> responses = deletedLots.map(LotMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    /**
     * Return a lot based on the given composite ID (id and version).
     *
     * @param id the ID of the lot
     * @param version the version of the lot
     * @return the lot
     */
    @GetMapping("/{id}/{version}")
    public ResponseEntity<LotResponse> findById(
            @PathVariable Integer id,
            @PathVariable Integer version) {
        Lot lot = lotService.findById(id, version)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));
        LotResponse response = LotMapper.INSTANCE.map(lot);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new lot.
     *
     * @param request the request to create the lot from
     * @return the created lot
     */
    @PostMapping
    public ResponseEntity<LotResponse> create(@RequestBody LotRequest request) {
        Lot createdLot = lotService.create(request);
        LotResponse response = LotMapper.INSTANCE.map(createdLot);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing lot based on the given id and version.
     *
     * @param lotId    the id of the lot to update
     * @param lotVer   the version of the lot to update
     * @param request  the request containing updated lot data
     * @return the updated lot
     */
    @PutMapping("/{lotId}/{lotVer}")
    public ResponseEntity<LotResponse> update(
            @PathVariable Integer lotId,
            @PathVariable Integer lotVer,
            @RequestBody LotRequest request) {
        Lot updatedLot = lotService.update(lotId, lotVer, request);
        LotResponse response = LotMapper.INSTANCE.map(updatedLot);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a lot based on the given composite ID (id and version).
     *
     * @param id the ID of the lot to delete
     * @param version the version of the lot to delete
     * @return the deleted lot
     */
    @DeleteMapping("/{id}/{version}")
    public ResponseEntity<LotResponse> deleteById(
            @PathVariable Integer id,
            @PathVariable Integer version) {
        lotService.delete(id, version);
        Lot deleted = lotService.findDeletedById(id, version)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deleted Lot not found"));
        LotResponse response = LotMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

}
