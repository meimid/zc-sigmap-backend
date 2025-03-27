package com.sigmap.sigmap_paa.realisations;

import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationService;
import com.sigmap.common.realisations.RealisationSpecifications;
import com.sigmap.common.realisations.request.RealisationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/realisations")
public class RealisationController {

    private final RealisationService realisationService;

    public RealisationController(RealisationService realisationService) {
        this.realisationService = realisationService;
    }

    @GetMapping
    public ResponseEntity<List<Realisation>> getAllRealisations() {
        List<Realisation> realisations = realisationService.findAll();
        return ResponseEntity.ok(realisations);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<Realisation>> getPaginatedRealisations(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Specification<Realisation> spec = search != null ? RealisationSpecifications.search(search) : null;
        Page<Realisation> realisations = realisationService.findAll(spec, pageable);
        return ResponseEntity.ok(realisations);
    }

    @GetMapping("/deleted/paginated")
    public ResponseEntity<Page<Realisation>> getPaginatedDeletedRealisations(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Specification<Realisation> spec = search != null ? RealisationSpecifications.search(search) : null;
        Page<Realisation> deletedRealisations = realisationService.findAllDeleted(spec, pageable);
        return ResponseEntity.ok(deletedRealisations);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<Realisation>> getAllDeletedRealisations(Pageable pageable) {
        Page<Realisation> deletedRealisations = realisationService.findAllDeleted(pageable);
        return ResponseEntity.ok(deletedRealisations);
    }

    @GetMapping("/{realisationId}/{realisationVer}/deleted")
    public ResponseEntity<Realisation> getDeletedRealisationById(
            @PathVariable Integer realisationId,
            @PathVariable Integer realisationVer) {
        Optional<Realisation> realisation = realisationService.findDeletedById(realisationId, realisationVer);
        return realisation.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{realisationId}/{realisationVer}")
    public ResponseEntity<Realisation> getRealisationById(
            @PathVariable Integer realisationId,
            @PathVariable Integer realisationVer) {
        Optional<Realisation> realisation = realisationService.findById(realisationId, realisationVer);
        return realisation.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Realisation> createRealisation(@RequestBody RealisationRequest request) {
        Realisation createdRealisation = realisationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRealisation);
    }

    @PutMapping("/{realisationId}/{realisationVer}")
    public ResponseEntity<Realisation> updateRealisation(
            @PathVariable Integer realisationId,
            @PathVariable Integer realisationVer,
            @RequestBody Realisation realisation) {
        Optional<Realisation> existingRealisation = realisationService.findById(realisationId, realisationVer);
        if (existingRealisation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        realisation.setRealisationId(realisationId);
        realisation.setRealisationVer(realisationVer);
        Realisation updatedRealisation = realisationService.save(realisation);
        return ResponseEntity.ok(updatedRealisation);
    }

    @DeleteMapping("/{realisationId}/{realisationVer}")
    public ResponseEntity<Void> deleteRealisation(
            @PathVariable Integer realisationId,
            @PathVariable Integer realisationVer) {
        Optional<Realisation> realisation = realisationService.findById(realisationId, realisationVer);
        if (realisation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        realisationService.delete(realisationId, realisationVer);
        return ResponseEntity.noContent().build();
    }
}
