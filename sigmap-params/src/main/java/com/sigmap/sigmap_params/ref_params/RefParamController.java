package com.sigmap.sigmap_params.ref_params;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.*;
import com.sigmap.common.ref_params.request.RefParamQuery;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final Logger logger = LoggerFactory.getLogger(RefParamController.class);

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
    public ResponseEntity<Page<RefParamResponse>> findAllDeleted(
            RefParamQuery query, Pageable pageable) {
        Page<RefParam> params = refParamService.findAllDeleted(query.toSpecification(), pageable);
        Page<RefParamResponse> paramResponses = params.map(RefParamMapper.INSTANCE::map);
        return ResponseEntity.ok(paramResponses);
    }

    /**
     * Return a ref param based on the given id.
     *
     * @param id the id of the ref param to return
     * @return the ref param
     */
    @GetMapping("/{id}")
    public ResponseEntity<RefParamResponse> findById(@PathVariable Integer id) throws RefParamNotFound {
        RefParam param = refParamService.findById(id);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new ref param.
     *
     * @param request the request to create the ref param from
     * @return the created ref param
     */
    @PostMapping
    public ResponseEntity<RefParamResponse> create(@RequestBody RefParamRequest request)throws RefParamNotFound {

        RefParam refParam = refParamService.create(request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update  RefParam.
     *
     * @param request the request to update the RefParam from
     * @return the updated RefParam
     */
    @PutMapping("/{id}")
    public ResponseEntity<RefParamResponse> update(@PathVariable Integer id,@RequestBody RefParamRequest request) throws RefParamNotFound, RefTypeNotFound{
        RefParam refParam = refParamService.update(id,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Delete a ref param based on the given id.
     *
     * @param id the id of the ref param to delete
     * @return the deleted ref param
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RefParamResponse> deleteById(@PathVariable Integer id)throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress {
        refParamService.delete(id);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }



}
