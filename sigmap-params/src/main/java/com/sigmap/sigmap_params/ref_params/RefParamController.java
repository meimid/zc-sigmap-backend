package com.sigmap.sigmap_params.ref_params;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.request.RefParamQuery;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.common.response.PageResponse;
import com.sigmap.sigmap_params.RefParamMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<Page<RefParamResponse>> findAll(
            RefParamQuery query, Pageable pageable
    ) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        System.out.println("User details" +  user.getSubject());

        Page<RefParam> params = refParamService.findAll(query.toSpecification(), pageable);
        Page<RefParamResponse> paramResponses = params.map(RefParamMapper.INSTANCE::map);
        return ResponseEntity.ok(paramResponses);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<RefParam>> findAllDeleted(
            RefParamQuery query, Pageable pageable
    ) {
        Page<RefParam> params = refParamService.findAllDeleted(query.toSpecification(), pageable);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<RefParam> deleteById(@PathVariable Integer id) {
        refParamService.delete(id);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();

        return ResponseEntity.ok(deleted);
    }
}
