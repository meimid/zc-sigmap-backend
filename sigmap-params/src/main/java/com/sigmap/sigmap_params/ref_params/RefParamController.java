package com.sigmap.sigmap_params.ref_params;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.request.RefParamQuery;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.sigmap_params.RefParamMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller to manage ref params.
 * 
 * @see RefParam
 * @see RefParamRequest
 * @see RefParamResponse
 * @see RefParamQuery
 * @see RefParamService
 */
@RestController
@RequestMapping("/api/ref-params")
public class RefParamController {

    private final RefParamService refParamService;

    /**
     * Constructor.
     * 
     * @param refParamService the service to use for managing ref params
     */
    public RefParamController(RefParamService refParamService) {
        this.refParamService = refParamService;
    }

    /**
     * Return a page of ref params based on the given specification and pageable.
     * 
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of ref params
     */
    @GetMapping
    public ResponseEntity<Page<RefParamResponse>> findAll(
            RefParamQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        System.out.println("User details" + user.getSubject());

        Page<RefParam> params = refParamService.findAll(query.toSpecification(), pageable);
        Page<RefParamResponse> paramResponses = params.map(RefParamMapper.INSTANCE::map);
        return ResponseEntity.ok(paramResponses);
    }

    /**
     * Return a page of deleted ref params based on the given specification and pageable.
     * 
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted ref params
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<RefParam>> findAllDeleted(
            RefParamQuery query, Pageable pageable) {
        Page<RefParam> params = refParamService.findAllDeleted(query.toSpecification(), pageable);
        return ResponseEntity.ok(params);
    }

    /**
     * Return a ref param based on the given id.
     * 
     * @param id the id of the ref param to return
     * @return the ref param
     */
    @GetMapping("/{id}")
    public ResponseEntity<RefParam> findById(@PathVariable Integer id) {
        RefParam param = refParamService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(param);
    }

    /**
     * Create a new ref param.
     * 
     * @param request the request to create the ref param from
     * @return the created ref param
     */
    @PostMapping
    public ResponseEntity<RefParam> create(@RequestBody RefParamRequest request) {
        RefParam refParam = refParamService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(refParam);
    }

    /**
     * Delete a ref param based on the given id.
     * 
     * @param id the id of the ref param to delete
     * @return the deleted ref param
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RefParam> deleteById(@PathVariable Integer id) {
        refParamService.delete(id);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();

        return ResponseEntity.ok(deleted);
    }
}
