package com.sigmap.sigmap_params.dac_dyna_form;

import com.sigmap.common.dac_dyna_form.DacDynaFormId;
import com.sigmap.common.dac_dyna_form.DacDynaFormService;
import com.sigmap.common.dac_dyna_form.request.DacDynaFormRequest;
import com.sigmap.common.dac_dyna_form.response.DacDynaFormResponse;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dac-dyna-form")
@RequiredArgsConstructor
public class DacDynaFormController {

    private final DacDynaFormService service;

    // ------------------------------------------------------------
    //  Attach a DynaForm (and all its fields) to a DAC
    // ------------------------------------------------------------
    @PostMapping("/attach")
    public ResponseEntity<List<DacDynaFormResponse>> attachFormToDac(
            @RequestBody DacDynaFormRequest request) {
        List<DacDynaFormResponse> responses = service.attachFormToDac(request);
        return ResponseEntity.ok(responses);
    }

    // ------------------------------------------------------------
    // Update which fields are displayed (true/false)
    // ------------------------------------------------------------
    @PutMapping("/update-display")
    public ResponseEntity<Void> updateFieldDisplayForDac(
            @RequestBody DacDynaFormRequest request) {
        service.updateFieldDisplayForDac(
                request.getDacId(),
                request.getDacVer(),
                request.getDynaFormId(),
                request.getDynaFormVer(),
                request.getDynaFormFieldsId()
        );
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------
    //  Retrieve distinct attached forms (one per form, not per field)
    // ------------------------------------------------------------
    @PostMapping("/attached")
    public ResponseEntity<Page<DynaFormResponse>> getAttachedForms(
            @RequestBody DacDynaFormRequest request,
            Pageable pageable) {
        Page<DynaFormResponse> page =
                service.findDistinctFormsByDac(request.getDacId(), request.getDacVer(), pageable);
        return ResponseEntity.ok(page);
    }

    // ------------------------------------------------------------
    //  Retrieve unattached forms filtered by TypeDossier
    // ------------------------------------------------------------
    @PostMapping("/unattached")
    public ResponseEntity<List<DynaFormResponse>> getUnattachedForms(
            @RequestBody DacDynaFormRequest request,
            @RequestParam(required = false) Integer typeDossierId,
            Pageable pageable) {
        List<DynaFormResponse> page =
                service.findUnattachedFormsByTypeDossier(request.getDacId(), request.getDacVer(), typeDossierId, pageable);
        return ResponseEntity.ok(page);
    }

    // ------------------------------------------------------------
    //  Retrieve all DAC–Form–Field rows (optional)
    // ------------------------------------------------------------
    @PostMapping("/all")
    public ResponseEntity<Page<DacDynaFormResponse>> findAllByDac(
            @RequestBody DacDynaFormRequest request,
            Pageable pageable) {
        Page<DacDynaFormResponse> page =
                service.findByDac(request.getDacId(), request.getDacVer(), pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/fields")
    public ResponseEntity<List<DacDynaFormResponse>> getDacDynaFormFields(
            @RequestBody DacDynaFormRequest request) {
        List<DacDynaFormResponse> page =
                service.getDacDynaFormFields(request.getDacId(), request.getDacVer(), request.getDynaFormId(), request.getDynaFormVer());
        return ResponseEntity.ok(page);
    }

    // ------------------------------------------------------------
    //  Filter attached DAC–Form associations by TypeDossier
    // ------------------------------------------------------------
    @GetMapping("/filter-by-type")
    public ResponseEntity<Page<DacDynaFormResponse>> filterByTypeDossier(
            @RequestParam Integer typeDossierId,
            Pageable pageable) {
        Page<DacDynaFormResponse> page = service.findByTypeDossier(typeDossierId, pageable);
        return ResponseEntity.ok(page);
    }

    // ------------------------------------------------------------
    //  Detach (delete) a DAC–Form–Field association
    // ------------------------------------------------------------
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody DacDynaFormId id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
