package com.sigmap.sigmap_params.ref_params;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.request.RefParamRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/api/ref-params")
public class RefParamController {

    private final RefParamService refParamService;

    public RefParamController(RefParamService refParamService) {
        this.refParamService = refParamService;
    }

    @GetMapping
    public ResponseEntity<List<RefParam>> findAll() {
        List<RefParam> params = refParamService.findAll();
        return ResponseEntity.ok(params);
    }


    @GetMapping("/{id}")
    public ResponseEntity<RefParam> findById(@PathVariable Integer id) {
        RefParam param = refParamService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(param);
    }

    @PostMapping
    public ResponseEntity<RefParam> create(@RequestBody RefParamRequest request) {
        RefParam refParam = refParamService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(refParam);
    }
}
