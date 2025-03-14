package com.sigmap.sigmap_params.ref_types;

import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;
import com.sigmap.common.ref_types.request.RefTypeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/ref-types")
public class RefTypeController {

    private final RefTypeService refTypeService;

    public RefTypeController(RefTypeService refTypeService) {
        this.refTypeService = refTypeService;
    }

    @GetMapping
    public ResponseEntity<List<RefType>> findAll() {
        List<RefType> params = refTypeService.findAll();
        return ResponseEntity.ok(params);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefType> findById(@PathVariable Integer id) {
        RefType params = refTypeService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(params);
    }

    @PostMapping
    public ResponseEntity<RefType> create(@RequestBody RefTypeRequest request) {
        System.out.println("Ref Type Request " +  request);
        RefType refType = refTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(refType);
    }
}
