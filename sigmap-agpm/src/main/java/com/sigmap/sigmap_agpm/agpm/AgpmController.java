package com.sigmap.sigmap_agpm.agpm;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
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

/**
 * Controller to manage AGPM entities.
 *
 * @see Agpm
 * @see AgpmRequest
 * @see AgpmResponse
 * @see AgpmQuery
 * @see AgpmService
 */
@RestController
@RequestMapping("/api/agpm")
public class AgpmController {
    private final Logger logger = LoggerFactory.getLogger(AgpmController.class);

    private final AgpmService agpmService;

    /**
     * Constructor.
     *
     * @param agpmService the service to use for managing AGPM entities
     */
    public AgpmController(AgpmService agpmService) {
        this.agpmService = agpmService;
    }

    /**
     * Return a page of AGPM entities based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of AGPM entities
     */
    @GetMapping
    public ResponseEntity<Page<AgpmResponse>> findAll(
            AgpmQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        logger.info("User {}", user.getSubject());

        Page<Agpm> agpms = agpmService.findAll(query.toSpecification(), pageable);
        Page<AgpmResponse> agpmResponses = agpms.map(AgpmMapper.INSTANCE::map);
        return ResponseEntity.ok(agpmResponses);
    }

    /**
     * Return a page of deleted AGPM entities based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted AGPM entities
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<AgpmResponse>> findAllDeleted(
            AgpmQuery query, Pageable pageable) {
        Page<Agpm> agpms = agpmService.findAllDeleted(query.toSpecification(), pageable);
        Page<AgpmResponse> agpmResponses = agpms.map(AgpmMapper.INSTANCE::map);
        return ResponseEntity.ok(agpmResponses);
    }

    /**
     * Return an AGPM entity based on the given id.
     *
     * @param id the id of the AGPM entity to return
     * @return the AGPM entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<AgpmResponse> findById(@PathVariable Integer id) {
        Agpm agpm = agpmService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        AgpmResponse response = AgpmMapper.INSTANCE.map(agpm);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new AGPM entity.
     *
     * @param request the request to create the AGPM entity from
     * @return the created AGPM entity
     */
    @PostMapping
    public ResponseEntity<AgpmResponse> create(@RequestBody AgpmRequest request) {
        Agpm agpm = agpmService.create(request);
        AgpmResponse response = AgpmMapper.INSTANCE.map(agpm);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete an AGPM entity based on the given id.
     *
     * @param id the id of the AGPM entity to delete
     * @return the deleted AGPM entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<AgpmResponse> deleteById(@PathVariable Integer id) {
        agpmService.delete(id);

        Agpm deleted = agpmService.findDeletedById(id)
                .orElseThrow();
        AgpmResponse response = AgpmMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}