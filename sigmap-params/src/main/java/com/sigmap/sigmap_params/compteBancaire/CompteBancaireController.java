package com.sigmap.sigmap_params.compteBancaire;

import com.sigmap.common.compte_bancaire.CompteBancaire;
import com.sigmap.common.compte_bancaire.CompteBancaireService;

import com.sigmap.common.compte_bancaire.mapper.CompteBancaireMapper;
import com.sigmap.common.compte_bancaire.request.CompteBancaireQuery;
import com.sigmap.common.compte_bancaire.request.CompteBancaireRequest;
import com.sigmap.common.compte_bancaire.response.CompteBancaireResponse;
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
@RequestMapping("/api/compte_bancaire")
public class CompteBancaireController {
    private final Logger logger = LoggerFactory.getLogger(CompteBancaireController.class);
    private final CompteBancaireService compteBancaireService;

    public CompteBancaireController(CompteBancaireService compteBancaireService) {
        this.compteBancaireService = compteBancaireService;
    }
    /**
     * Return a page of CompteBancaire based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of CompteBancaire
     */
    @GetMapping
    public ResponseEntity<Page<CompteBancaireResponse>> findAll(
            CompteBancaireQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        logger.info("User {}", user.getSubject());

        Page<CompteBancaire> params = compteBancaireService.findAll(query.toSpecification(), pageable);
        Page<CompteBancaireResponse> compteBancaireResponses = params.map(CompteBancaireMapper.INSTANCE::map);
        return ResponseEntity.ok(compteBancaireResponses);
    }

    /**
     * Return a page of deleted CompteBancaire based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted CompteBancaire
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<CompteBancaireResponse>> findAllDeleted(
            CompteBancaireQuery query, Pageable pageable) {
        Page<CompteBancaire> params = compteBancaireService.findAllDeleted(query.toSpecification(), pageable);
        Page<CompteBancaireResponse> compteBancaireResponses = params.map(CompteBancaireMapper.INSTANCE::map);
        return ResponseEntity.ok(compteBancaireResponses);
    }

    /**
     * Return a CompteBancaire based on the given id.
     *
     * @param id the id of the CompteBancaire to return
     * @return the CompteBancaire
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompteBancaireResponse> findById(@PathVariable Integer id) {
        CompteBancaire param = compteBancaireService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        CompteBancaireResponse response = CompteBancaireMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new CompteBancaire.
     *
     * @param request the request to create the CompteBancaire from
     * @return the created CompteBancaire
     */
    @PostMapping
    public ResponseEntity<CompteBancaireResponse> create(@RequestBody CompteBancaireRequest request) {
        CompteBancaire CompteBancaire = compteBancaireService.create(request);
        CompteBancaireResponse response = CompteBancaireMapper.INSTANCE.map(CompteBancaire);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Update  CompteBancaire.
     *
     * @param request the request to update the CompteBancaire from
     * @return the updated CompteBancaire
     */
    @PutMapping
    public ResponseEntity<CompteBancaireResponse> update(@RequestBody CompteBancaireRequest request) {
        CompteBancaire CompteBancaire = compteBancaireService.update(request);
        CompteBancaireResponse response = CompteBancaireMapper.INSTANCE.map(CompteBancaire);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Delete a CompteBancaire based on the given id.
     *
     * @param id the id of the CompteBancaire to delete
     * @return the deleted CompteBancaire
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<CompteBancaireResponse> deleteById(@PathVariable Integer id) {
        compteBancaireService.delete(id);

        CompteBancaire deleted = compteBancaireService.findDeletedById(id)
                .orElseThrow();
        CompteBancaireResponse response = CompteBancaireMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
