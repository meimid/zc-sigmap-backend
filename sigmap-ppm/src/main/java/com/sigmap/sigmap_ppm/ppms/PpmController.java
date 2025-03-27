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

    @GetMapping("/{id}/deleted")
    public ResponseEntity<Ppm> getDeletedPpmById(@PathVariable Integer id) {
        Optional<Ppm> ppm = ppmService.findDeletedById(id);
        return ppm.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ppm> getPpmById(@PathVariable Integer id) {
        Optional<Ppm> ppm = ppmService.findById(id);
        return ppm.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Ppm> createPpm(@RequestBody PpmRequest request) {
        Ppm createdPpm = ppmService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPpm);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ppm> updatePpm(
            @PathVariable Integer id,
            @RequestBody Ppm ppm) {
        Optional<Ppm> existingPpm = ppmService.findById(id);
        if (existingPpm.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ppm.setPpmId(id); // Assumes ppmId is the ID; adjust if composite
        Ppm updatedPpm = ppmService.save(ppm);
        return ResponseEntity.ok(updatedPpm);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePpm(@PathVariable Integer id) {
        Optional<Ppm> ppm = ppmService.findById(id);
        if (ppm.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        ppmService.delete(id);
        return ResponseEntity.noContent().build();
    }
}