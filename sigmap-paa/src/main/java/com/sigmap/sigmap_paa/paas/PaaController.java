package com.sigmap.sigmap_paa.paas;

import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.PaaSpecifications;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.mappers.PaaMapper;
import com.sigmap.common.paas.request.PaaQuery;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.response.PaaResponse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/paas")
public class PaaController {
    private final Logger logger = LoggerFactory.getLogger(PaaController.class);
    private final PaaService paaService;

    public PaaController(PaaService paaService) {
        this.paaService = paaService;
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
    public ResponseEntity<PaaResponse> findById(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) {
        Paa paa = paaService.findById(paaId, paaVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        PaaResponse response = PaaMapper.INSTANCE.map(paa);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new paa.
     *
     * @param request the request to create the paa from
     * @return the created paa
     */
    @PostMapping
    public ResponseEntity<PaaResponse> create(@RequestBody PaaRequest request) {
        Paa paa = paaService.create(request);
        PaaResponse response = PaaMapper.INSTANCE.map(paa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

}