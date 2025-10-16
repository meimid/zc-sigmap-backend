package com.sigmap.sigmap_params.DynaForm;

import com.sigmap.common.dyna_form.DynaFormService;
import com.sigmap.common.dyna_form.request.DynaFormRequest;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import com.sigmap.common.entity.TypeDossier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dyna-forms")
public class DynaFormController {

    private final DynaFormService dynaFormService;

    public DynaFormController(DynaFormService dynaFormService) {
        this.dynaFormService = dynaFormService;
    }

    /**
     * Create a new dynamic form (version starts at 1).
     * Any fields included will be created under version 1.
     */
    @PostMapping
    public ResponseEntity<DynaFormResponse> create(@RequestBody DynaFormRequest request) {
        DynaFormResponse created = dynaFormService.create(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Get a specific form version.
     */
    @GetMapping("/{id}/versions/{version}")
    public ResponseEntity<DynaFormResponse> get(
            @PathVariable Integer id,
            @PathVariable Integer version
    ) {
        DynaFormResponse dto = dynaFormService.get(id, version);
        return ResponseEntity.ok(dto);
    }

    /**
     * List latest versions (service returns whatever your implementation decides:
     * either all versions or only latest; adjust service if needed).
     */
    @GetMapping
    public ResponseEntity<List<DynaFormResponse>> list() {
        List<DynaFormResponse> list = dynaFormService.list();
        return ResponseEntity.ok(list);
    }

    /**
     * Update with versioning:
     * - Compares header + fields against latest version.
     * - If NO change: returns latest without bumping.
     * - If ANY change: creates exactly one new version (copies untouched fields,
     *   excludes modified/removed ones, adds new ones).
     */
    @PutMapping("/{id}/versioning")
    public ResponseEntity<DynaFormResponse> updateWithVersioning(
            @PathVariable Integer id,
            @RequestBody DynaFormRequest request
    ) {
        DynaFormResponse updated = dynaFormService.updateWithVersioning(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Soft delete a specific version (if that’s your model).
     * If you prefer deleting latest or all versions, adapt service accordingly.
     */
    @DeleteMapping("/{id}/versions/{version}")
    public ResponseEntity<Void> softDelete(
            @PathVariable Integer id,
            @PathVariable Integer version
    ) {
        dynaFormService.softDelete(id, version);
        return ResponseEntity.noContent().build();
    }

    /**
     * Page through forms by dossier type.
     * Example: GET /api/dyna-forms/by-type?type=ADMINISTRATIVE&page=0&size=20
     */
    @GetMapping("/by-type")
    public ResponseEntity<Page<DynaFormResponse>> findByType(
            @RequestParam("type") TypeDossier type,
            Pageable pageable
    ) {
        Page<DynaFormResponse> page = dynaFormService.findByTypeDossier(type, pageable);
        return ResponseEntity.ok(page);
    }
}
