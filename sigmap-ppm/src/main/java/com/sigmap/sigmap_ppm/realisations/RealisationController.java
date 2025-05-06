package com.sigmap.sigmap_ppm.realisations;

import com.sigmap.common.lots.exception.LotNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationService;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.exceptions.RealisationsMinimumRequirement;
import com.sigmap.common.realisations.exceptions.UpdateDeletedRealisation;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.request.RealisationQuery;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.realisations.response.RealisationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/realisations")
public class RealisationController {
    private final Logger logger = LoggerFactory.getLogger(RealisationController.class);

    private final RealisationService realisationService;

    public RealisationController(RealisationService realisationService) {
        this.realisationService = realisationService;
    }

    /**
     * Return a page of realisations based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of realisations
     */
    @GetMapping
    public ResponseEntity<Page<RealisationResponse>> findAll(
            RealisationQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Realisation> realisations = realisationService.findAll(query.toSpecification(), pageable);
        Page<RealisationResponse> responses = realisations.map(RealisationMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    /**
     * Return a page of deleted realisations based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted realisations
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<RealisationResponse>> findAllDeleted(
            RealisationQuery query, Pageable pageable) {
        query.setDeleted(true);
        Page<Realisation> realisations = realisationService.findAllDeleted(query.toSpecification(), pageable);
        Page<RealisationResponse> responses = realisations.map(RealisationMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    /**
     * Return a realisation based on the given id and version.
     *
     * @param realisationId the id of the realisation to return
     * @param realisationVer the version of the realisation to return
     * @return the realisation
     */
    @GetMapping("/{realisationId}/{realisationVer}")
    public ResponseEntity<RealisationResponse> findById(
            @PathVariable Integer realisationId,
            @PathVariable Integer realisationVer) throws RealisationAlreadyExistsException {
        Realisation realisation = realisationService.findById(realisationId, realisationVer);

        RealisationResponse response = RealisationMapper.INSTANCE.map(realisation);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new realisation.
     *
     * @param request the request to create the realisation from
     * @return the created realisation
     */
    @PostMapping
    public ResponseEntity<RealisationResponse> create(@RequestBody RealisationRequest request) throws RealisationAlreadyExistsException, RealisationsMinimumRequirement, PaaNotFound, PpmNotFound, LotNotFound {
        Realisation realisation = realisationService.create(request);
        RealisationResponse response = RealisationMapper.INSTANCE.map(realisation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing realisation based on the given id and version.
     *
     * @param realisationId the id of the realisation to update
     * @param realisationVer the version of the realisation to update
     * @param request the request object containing updated details
     * @return the updated realisation
     */
    @PutMapping("/{realisationId}/{realisationVer}")
    public ResponseEntity<RealisationResponse> update(
            @PathVariable Integer realisationId,
            @PathVariable Integer realisationVer,
            @RequestBody RealisationRequest request) throws RealisationNotFoundException, UpdateDeletedRealisation, RealisationsMinimumRequirement,PaaNotFound, PpmNotFound, LotNotFound {

            Realisation updatedRealisation = realisationService.update(realisationId, realisationVer, request);
            RealisationResponse response = RealisationMapper.INSTANCE.map(updatedRealisation);
            return ResponseEntity.ok(response);
    }

    /**
     * Delete a realisation based on the given id and version.
     *
     * @param realisationId the id of the realisation to delete
     * @param realisationVer the version of the realisation to delete
     * @return the deleted realisation
     */
    @DeleteMapping("/{realisationId}/{realisationVer}")
    public ResponseEntity<RealisationResponse> deleteById(
            @PathVariable Integer realisationId,
            @PathVariable Integer realisationVer) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        realisationService.delete(realisationId, realisationVer);
        Realisation deleted = realisationService.findDeletedById(realisationId, realisationVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deleted Realisation not found"));
        RealisationResponse response = RealisationMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

}
