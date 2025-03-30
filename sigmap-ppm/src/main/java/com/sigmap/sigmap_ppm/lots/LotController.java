package com.sigmap.sigmap_ppm.lots;

import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotService;
import com.sigmap.common.lots.mappers.LotMapper;
import com.sigmap.common.lots.request.LotQuery;
import com.sigmap.common.lots.request.LotRequest;
import com.sigmap.common.lots.response.LotResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lots")
public class LotController {
    private final Logger logger = LoggerFactory.getLogger(LotController.class);
    private final LotService lotService;

    public LotController(LotService lotService) {
        this.lotService = lotService;
    }

    @GetMapping
    public ResponseEntity<List<LotResponse>> getAllLots() {
        List<Lot> lots = lotService.findAll();
        List<LotResponse> responses = lots.stream()
                .map(LotMapper.INSTANCE::map) // Updated to use INSTANCE
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<LotResponse>> getPaginatedLots(
            LotQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Lot> lots = lotService.findAll(query.toSpecification(), pageable);
        Page<LotResponse> responses = lots.map(LotMapper.INSTANCE::map); // Updated to use INSTANCE
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/deleted/paginated")
    public ResponseEntity<Page<LotResponse>> getPaginatedDeletedLots(
            LotQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Lot> deletedLots = lotService.findAllDeleted(query.toSpecification(), pageable);
        Page<LotResponse> responses = deletedLots.map(LotMapper.INSTANCE::map); // Updated to use INSTANCE
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<LotResponse>> getAllDeletedLots(Pageable pageable) {
        Page<Lot> deletedLots = lotService.findAllDeleted(pageable);
        Page<LotResponse> responses = deletedLots.map(LotMapper.INSTANCE::map); // Updated to use INSTANCE
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/{version}/deleted")
    public ResponseEntity<LotResponse> getDeletedLotById(
            @PathVariable Integer id,
            @PathVariable Integer version) {
        Optional<Lot> lot = lotService.findDeletedById(id, version);
        return lot.map(LotMapper.INSTANCE::map) // Updated to use INSTANCE
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{id}/{version}")
    public ResponseEntity<LotResponse> getLotById(
            @PathVariable Integer id,
            @PathVariable Integer version) {
        Optional<Lot> lot = lotService.findById(id, version);
        return lot.map(LotMapper.INSTANCE::map) // Updated to use INSTANCE
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<LotResponse> createLot(@RequestBody LotRequest request) {
        Lot createdLot = lotService.create(request);
        LotResponse response = LotMapper.INSTANCE.map(createdLot); // Updated to use INSTANCE
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/{version}")
    public ResponseEntity<LotResponse> updateLot(
            @PathVariable Integer id,
            @PathVariable Integer version,
            @RequestBody LotRequest request) {
        Optional<Lot> existingLot = lotService.findById(id, version);
        if (existingLot.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Lot lot = LotMapper.INSTANCE.toEntity(request); // Updated to use INSTANCE
        lot.setId(id);
        lot.setVersion(version);
        Lot updatedLot = lotService.save(lot);
        LotResponse response = LotMapper.INSTANCE.map(updatedLot); // Updated to use INSTANCE
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/{version}")
    public ResponseEntity<LotResponse> deleteLot(
            @PathVariable Integer id,
            @PathVariable Integer version) {
        Optional<Lot> lot = lotService.findById(id, version);
        if (lot.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        lotService.delete(id, version);
        Lot deletedLot = lotService.findDeletedById(id, version)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        LotResponse response = LotMapper.INSTANCE.map(deletedLot); // Updated to use INSTANCE
        return ResponseEntity.ok(response);
    }
}
