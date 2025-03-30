package com.sigmap.sigmap_ppm.ppms;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.request.PpmRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/ppms")
public class PpmController {

    private final PpmService ppmService;

    public PpmController(PpmService ppmService) {
        this.ppmService = ppmService;
    }

    @GetMapping
    public ResponseEntity<List<Ppm>> getAllPpms() {
        List<Ppm> ppms = ppmService.findAll();
        return ResponseEntity.ok(ppms);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<Ppm>> getPaginatedPpms(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Specification<Ppm> spec = search != null ? PpmSpecifications.search(search) : null;
        Page<Ppm> ppms = ppmService.findAll(spec, pageable);
        return ResponseEntity.ok(ppms);
    }

    @GetMapping("/deleted/paginated")
    public ResponseEntity<Page<Ppm>> getPaginatedDeletedPpms(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Specification<Ppm> spec = search != null ? PpmSpecifications.search(search) : null;
        Page<Ppm> deletedPpms = ppmService.findAllDeleted(spec, pageable);
        return ResponseEntity.ok(deletedPpms);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<Ppm>> getAllDeletedPpms(Pageable pageable) {
        Page<Ppm> deletedPpms = ppmService.findAllDeleted(pageable);
        return ResponseEntity.ok(deletedPpms);
    }

    @GetMapping("/{ppmId}/{ppmVer}/deleted")
    public ResponseEntity<Ppm> getDeletedPpmById(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer) {
        Optional<Ppm> ppm = ppmService.findDeletedById(ppmId, ppmVer);
        return ppm.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{ppmId}/{ppmVer}")
    public ResponseEntity<Ppm> getPpmById(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer) {
        Optional<Ppm> ppm = ppmService.findById(ppmId, ppmVer);
        return ppm.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Ppm> createPpm(@RequestBody PpmRequest request) {
        Ppm createdPpm = ppmService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPpm);
    }

    @PutMapping("/{ppmId}/{ppmVer}")
    public ResponseEntity<Ppm> updatePpm(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer,
            @RequestBody Ppm ppm) {
        Optional<Ppm> existingPpm = ppmService.findById(ppmId, ppmVer);
        if (existingPpm.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ppm.setId(ppmId); // Set composite key part 1
        ppm.setVersion(ppmVer); // Set composite key part 2
        Ppm updatedPpm = ppmService.save(ppm);
        return ResponseEntity.ok(updatedPpm);
    }

    @DeleteMapping("/{ppmId}/{ppmVer}")
    public ResponseEntity<Void> deletePpm(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVer) {
        Optional<Ppm> ppm = ppmService.findById(ppmId, ppmVer);
        if (ppm.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ppmService.delete(ppmId, ppmVer);
        return ResponseEntity.noContent().build();
    }
}