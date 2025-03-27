package com.sigmap.sigmap_paa.paas;

import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.PaaSpecifications;
import com.sigmap.common.paas.request.PaaRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/paas")
public class PaaController {

    private final PaaService paaService;

    public PaaController(PaaService paaService) {
        this.paaService = paaService;
    }

    @GetMapping
    public ResponseEntity<List<Paa>> getAllPaas() {
        List<Paa> paas = paaService.findAll();
        return ResponseEntity.ok(paas);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<Paa>> getPaginatedPaas(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Specification<Paa> spec = search != null ? PaaSpecifications.search(search) : null;
        Page<Paa> paas = paaService.findAll(spec, pageable);
        return ResponseEntity.ok(paas);
    }

    @GetMapping("/deleted/paginated")
    public ResponseEntity<Page<Paa>> getPaginatedDeletedPaas(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Specification<Paa> spec = search != null ? PaaSpecifications.search(search) : null;
        Page<Paa> deletedPaas = paaService.findAllDeleted(spec, pageable);
        return ResponseEntity.ok(deletedPaas);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<Paa>> getAllDeletedPaas(Pageable pageable) {
        Page<Paa> deletedPaas = paaService.findAllDeleted(pageable);
        return ResponseEntity.ok(deletedPaas);
    }

    @GetMapping("/{paaId}/{paaVer}/deleted")
    public ResponseEntity<Paa> getDeletedPaaById(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) {
        Optional<Paa> paa = paaService.findDeletedById(paaId, paaVer);
        return paa.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{paaId}/{paaVer}")
    public ResponseEntity<Paa> getPaaById(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) {
        Optional<Paa> paa = paaService.findById(paaId, paaVer);
        return paa.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Paa> createPaa(@RequestBody PaaRequest request) {
        Paa createdPaa = paaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPaa);
    }

    @PutMapping("/{paaId}/{paaVer}")
    public ResponseEntity<Paa> updatePaa(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer,
            @RequestBody Paa paa) {
        Optional<Paa> existingPaa = paaService.findById(paaId, paaVer);
        if (existingPaa.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        paa.setPaaId(paaId);
        paa.setPaaVer(paaVer);
        Paa updatedPaa = paaService.save(paa);
        return ResponseEntity.ok(updatedPaa);
    }

    @DeleteMapping("/{paaId}/{paaVer}")
    public ResponseEntity<Void> deletePaa(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVer) {
        Optional<Paa> paa = paaService.findById(paaId, paaVer);
        if (paa.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        paaService.delete(paaId, paaVer);
        return ResponseEntity.noContent().build();
    }
}