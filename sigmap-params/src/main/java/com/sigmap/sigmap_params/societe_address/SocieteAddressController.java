package com.sigmap.sigmap_params.societe_address;

import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.SocieteAddressService;
import com.sigmap.common.societe_address.mappers.SocieteAddressMapper;
import com.sigmap.common.societe_address.request.SocieteAddressQuery;
import com.sigmap.common.societe_address.request.SocieteAddressRequest;
import com.sigmap.common.societe_address.response.SocieteAddressResponse;
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
@RequestMapping("/api/societe_address")
public class SocieteAddressController {
    private final Logger logger = LoggerFactory.getLogger(SocieteAddressController.class);
    private final SocieteAddressService societeAddressService;

    public SocieteAddressController(SocieteAddressService societeAddressService) {
        this.societeAddressService = societeAddressService;
    }

    /**
     * Return a page of SocieteAddress based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of SocieteAddress
     */
    @GetMapping
    public ResponseEntity<Page<SocieteAddressResponse>> findAll(
            SocieteAddressQuery query, Pageable pageable) {
        Page<SocieteAddress> params = societeAddressService.findAll(query.toSpecification(), pageable);
        Page<SocieteAddressResponse> SocieteAddressResponses = params.map(SocieteAddressMapper.INSTANCE::map);
        return ResponseEntity.ok(SocieteAddressResponses);
    }

    /**
     * Return a page of deleted SocieteAddress based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted SocieteAddress
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<SocieteAddressResponse>> findAllDeleted(
            SocieteAddressQuery query, Pageable pageable) {
        Page<SocieteAddress> params = societeAddressService.findAllDeleted(query.toSpecification(), pageable);
        Page<SocieteAddressResponse> SocieteAddressResponses = params.map(SocieteAddressMapper.INSTANCE::map);
        return ResponseEntity.ok(SocieteAddressResponses);
    }

    /**
     * Return a SocieteAddress based on the given id.
     *
     * @param id the id of the SocieteAddress to return
     * @return the SocieteAddress
     */
    @GetMapping("/{id}")
    public ResponseEntity<SocieteAddressResponse> findById(@PathVariable Integer id) {
        SocieteAddress param = societeAddressService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        SocieteAddressResponse response = SocieteAddressMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new SocieteAddress.
     *
     * @param request the request to create the SocieteAddress from
     * @return the created SocieteAddress
     */
    @PostMapping
    public ResponseEntity<SocieteAddressResponse> create(@RequestBody SocieteAddressRequest request) {
        SocieteAddress SocieteAddress = societeAddressService.create(request);
        SocieteAddressResponse response = SocieteAddressMapper.INSTANCE.map(SocieteAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Update  SocieteAddress.
     *
     * @param request the request to update the SocieteAddress from
     * @return the updated SocieteAddress
     */
    @PutMapping
    public ResponseEntity<SocieteAddressResponse> update(@RequestBody SocieteAddressRequest request) {
        SocieteAddress SocieteAddress = societeAddressService.update(request);
        SocieteAddressResponse response = SocieteAddressMapper.INSTANCE.map(SocieteAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Delete a SocieteAddress based on the given id.
     *
     * @param id the id of the SocieteAddress to delete
     * @return the deleted SocieteAddress
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SocieteAddressResponse> deleteById(@PathVariable Integer id) {
        societeAddressService.delete(id);
        SocieteAddress deleted = societeAddressService.findDeletedById(id)
                .orElseThrow();
        SocieteAddressResponse response = SocieteAddressMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
