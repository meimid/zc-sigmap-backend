package com.sigmap.sigmap_params.societe_address;

import com.sigmap.common.adresse.mappers.AdresseMapper;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.SocieteAddressService;
import com.sigmap.common.societe_address.exception.SocieteAdresseNotFound;
import com.sigmap.common.societe_address.mappers.SocieteAddressMapper;
import com.sigmap.common.societe_address.request.SocieteAddressQuery;
import com.sigmap.common.societe_address.request.SocieteAddressRequest;
import com.sigmap.common.societe_address.request.SocieteAddressUpdateRequest;
import com.sigmap.common.societe_address.response.AddressAndTypeResponse;
import com.sigmap.common.societe_address.response.SocieteAddressResponse;
import com.sigmap.common.societe_address.response.SocieteAddressResponseForGetWithSocieteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

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
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<Page<SocieteAddressResponse>> findAll(
            SocieteAddressQuery query, Pageable pageable) {
        Page<SocieteAddress> params = societeAddressService.findAll(query.toSpecification(), pageable);
        Page<SocieteAddressResponse> SocieteAddressResponses = params.map(SocieteAddressMapper.INSTANCE::map);
        return ResponseEntity.ok(SocieteAddressResponses);
    }


    /**
     * Return a SocieteAddress based on the given id.
     *
     * @param societeId the id of the SocieteAddress to return
     * @return the SocieteAddress
     */
    @GetMapping("/societe/{societeId}/address/{addressId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteAddressResponse> findById(@PathVariable Integer societeId,@PathVariable Integer addressId) throws SocieteAdresseNotFound {
        SocieteAddress param = societeAddressService.findById(societeId,addressId);
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
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteAddressResponse> create(@RequestBody SocieteAddressRequest request) throws RefParamNotFound, SocieteNotFound {
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
    @PutMapping("/societe/{societeId}/address/{addressId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteAddressResponse> update(@PathVariable Integer societeId,@PathVariable Integer addressId,@RequestBody SocieteAddressUpdateRequest request) throws RefParamNotFound, SocieteNotFound, SocieteAdresseNotFound {
        SocieteAddress SocieteAddress = societeAddressService.update(societeId,addressId,request);
        SocieteAddressResponse response = SocieteAddressMapper.INSTANCE.map(SocieteAddress);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/societe/{societeId}/societe_address")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteAddressResponseForGetWithSocieteId> findSocieteAddressBySocieteId(
            @PathVariable Integer societeId) throws SocieteNotFound {

        List<SocieteAddress> params = societeAddressService.findSocieteAddressBySocieteId(societeId);

        List<AddressAndTypeResponse> content = params.stream()
                .map(sa -> {
                    AddressAndTypeResponse resp = new AddressAndTypeResponse();
                    resp.setAddress(AdresseMapper.INSTANCE.toDto(sa.getAddress()));
                    resp.setType(RefParamMapper.INSTANCE.mapToDto(sa.getType()));
                    return resp;
                })
                .collect(Collectors.toList());

        SocieteAddressResponseForGetWithSocieteId response = SocieteAddressResponseForGetWithSocieteId.builder()
                .societe(SocieteMapper.INSTANCE.toResponseDto(params.get(0).getSociete()))
                .content(content)
                .build();

        return ResponseEntity.ok(response);
    }

}
