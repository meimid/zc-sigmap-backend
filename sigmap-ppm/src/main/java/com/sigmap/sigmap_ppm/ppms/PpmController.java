package com.sigmap.sigmap_ppm.ppms;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.request.PpmQuery;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.ppms.response.PpmResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RestController
@RequestMapping("/api/ppms")
public class PpmController {

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
        Page<Ppm> ppms = ppmService.findAllDeleted(query.toSpecification(), pageable);
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
}