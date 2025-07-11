package com.sigmap.sigmap_params.ref_types;

import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;
import com.sigmap.common.ref_types.exceptions.HasAssociatedRefParams;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import com.sigmap.common.ref_types.exceptions.RefTypePositiveIdAndNotEmpty;
import com.sigmap.common.ref_types.mapper.RefTypeMapper;
import com.sigmap.common.ref_types.request.RefTypeQuery;
import com.sigmap.common.ref_types.request.RefTypeRequest;
import java.util.List;
import com.sigmap.common.ref_types.response.RefTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ref-types")
public class RefTypeController {

  private final RefTypeService refTypeService;

  public RefTypeController(RefTypeService refTypeService) {
    this.refTypeService = refTypeService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<List<RefType>> findAll() {
    List<RefType> params = refTypeService.findAll();
    return ResponseEntity.ok(params);
  }


  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<RefType> findById(@PathVariable Integer id) throws RefTypeNotFound {
    RefType params = refTypeService.findById(id);
    return ResponseEntity.ok(params);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<RefType> create(@RequestBody RefTypeRequest request) throws RefTypePositiveIdAndNotEmpty {

    System.out.println("Ref Type Request " + request);
    RefType refType = refTypeService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(refType);
  }
//new code
  @GetMapping("/deleted")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<Page<RefTypeResponse>> findAllDeleted(
          RefTypeQuery query, Pageable pageable) {
    Page<RefType> params = refTypeService.findAllDeleted(query.toSpecification(), pageable);
    Page<RefTypeResponse> refTypeResponses = params.map(RefTypeMapper.INSTANCE::map);
    return ResponseEntity.ok(refTypeResponses);
  }

  @PutMapping
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<RefTypeResponse> update(@RequestBody RefTypeRequest request) throws RefTypeNotFound {
    RefType refType = refTypeService.update(request);
    RefTypeResponse response = RefTypeMapper.INSTANCE.map(refType);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<RefTypeResponse> deleteById(@PathVariable Integer id) throws HasAssociatedRefParams {
    refTypeService.delete(id);

    RefType deleted = refTypeService.findDeletedById(id)
            .orElseThrow();
    RefTypeResponse response = RefTypeMapper.INSTANCE.map(deleted);
    return ResponseEntity.ok(response);
  }
}
