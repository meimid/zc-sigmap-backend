package com.sigmap.sigmap_params.com;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComService;
import com.sigmap.common.com.exception.ComHasAssociatedAc;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.com.mapper.ComMapper;
import com.sigmap.common.com.request.ComQuery;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.com.response.ComResponse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/com")
public class ComController {
    private final Logger logger = LoggerFactory.getLogger(ComController.class);

    private final ComService comService;

    public ComController(ComService comService) {
        this.comService = comService;
    }

    /**
     * Return a page of Com based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of com
     */
    @GetMapping
    public ResponseEntity<Page<ComResponse>> findAll(
            ComQuery query, Pageable pageable) {
        Page<Com> params = comService.findAll(query.toSpecification(), pageable);
        Page<ComResponse> acResponses = params.map(ComMapper.INSTANCE::map);
        return ResponseEntity.ok(acResponses);
    }

    /**
     * Return a page of deleted Com based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted Com
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<ComResponse>> findAllDeleted(
            ComQuery query, Pageable pageable) {
        Page<Com> params = comService.findAllDeleted(query.toSpecification(), pageable);
        Page<ComResponse> acResponses = params.map(ComMapper.INSTANCE::map);
        return ResponseEntity.ok(acResponses);
    }

    /**
     * Return a Com based on the given id.
     *
     * @param id the id of the Com to return
     * @return the Com
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComResponse> findById(@PathVariable Integer id) throws ComNotFound {
        Com param = comService.findById(id);
        ComResponse response = ComMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new Com.
     *
     * @param request the request to create the Com from
     * @return the created Com
     */
    @PostMapping
    public ResponseEntity<ComResponse> create(@RequestBody ComRequest request) throws  AcNotFound, RefParamNotFound {
        Com com = comService.create(request);
        ComResponse response = ComMapper.INSTANCE.map(com);
        return ResponseEntity.ok(response);
    }
    /**
     * Update  com.
     *
     * @param request the request to update the com from
     * @return the updated com
     */
    @PutMapping("/{id}")
    public ResponseEntity<ComResponse> update(@PathVariable Integer id,@RequestBody ComRequest request) throws ComNotFound, RefParamNotFound, AcNotFound {
        Com com = comService.update(id,request);
        ComResponse response = ComMapper.INSTANCE.map(com);
        return ResponseEntity.ok(response);
    }
    /**
     * Delete a Com based on the given id.
     *
     * @param id the id of the Com to delete
     * @return the deleted Com
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ComResponse> deleteById(@PathVariable Integer id) throws ComHasAssociatedAc {
        comService.delete(id);

        Com deleted = comService.findDeletedById(id)
                .orElseThrow();
        ComResponse response = ComMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
