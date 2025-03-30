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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
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

    @GetMapping
    public ResponseEntity<List<PaaResponse>> getAllPaas() {
        List<Paa> paas = paaService.findAll();
        List<PaaResponse> responses = paas.stream()
                .map(PaaMapper.INSTANCE::map) // Updated
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<PaaResponse>> getPaginatedPaas(
            PaaQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Paa> paas = paaService.findAll(query.toSpecification(), pageable);
        Page<PaaResponse> responses = paas.map(PaaMapper.INSTANCE::map); // Updated
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/deleted/paginated")
    public ResponseEntity<Page<PaaResponse>> getPaginatedDeletedPaas(
            PaaQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Paa> deletedPaas = paaService.findAllDeleted(query.toSpecification(), pageable);
        Page<PaaResponse> responses = deletedPaas.map(PaaMapper.INSTANCE::map); // Updated
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<PaaResponse>> getAllDeletedPaas(Pageable pageable) {
        Page<Paa> deletedPaas = paaService.findAllDeleted(pageable);
        Page<PaaResponse> responses = deletedPaas.map(PaaMapper.INSTANCE::map); // Updated
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{paaId}/{paaVer}/deleted")
    public ResponseEntity<PaaResponse> getDeletedPaaById(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) {
        Optional<Paa> paa = paaService.findDeletedById(paaId, paaVer);
        return paa.map(PaaMapper.INSTANCE::map) // Updated
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{paaId}/{paaVer}")
    public ResponseEntity<PaaResponse> getPaaById(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) {
        Optional<Paa> paa = paaService.findById(paaId, paaVer);
        return paa.map(PaaMapper.INSTANCE::map) // Updated
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<PaaResponse> createPaa(@RequestBody PaaRequest request) {
        Paa createdPaa = paaService.create(request);
        PaaResponse response = PaaMapper.INSTANCE.map(createdPaa); // Updated
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{paaId}/{paaVer}")
    public ResponseEntity<PaaResponse> updatePaa(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer,
            @RequestBody PaaRequest request) {
        Optional<Paa> existingPaa = paaService.findById(paaId, paaVer);
        if (existingPaa.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Paa paa = PaaMapper.INSTANCE.toEntity(request); // Updated (assuming toEntity exists)
        paa.setId(paaId);
        paa.setVersion(paaVer);
        Paa updatedPaa = paaService.save(paa); // Fixed: Use transformed Paa, not request directly
        PaaResponse response = PaaMapper.INSTANCE.map(updatedPaa); // Updated
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{paaId}/{paaVer}")
    public ResponseEntity<PaaResponse> deletePaa(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) {
        Optional<Paa> paa = paaService.findById(paaId, paaVer);
        if (paa.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        paaService.delete(paaId, paaVer);
        Paa deletedPaa = paaService.findDeletedById(paaId, paaVer)
                .orElseThrow(() -> null); // Fixed TODO
        PaaResponse response = PaaMapper.INSTANCE.map(deletedPaa); // Already updated as per example
        return ResponseEntity.ok(response);
    }
}