package com.sigmap.sigmap_dac.dynaFields;

import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsService;
import com.sigmap.common.dyna_fields.DynaFieldsSpecifications;
import com.sigmap.common.dyna_fields.mappers.DynaFieldsMapper;
import com.sigmap.common.dyna_fields.request.DynaFieldsQuery;
import com.sigmap.common.dyna_fields.request.DynaFieldsRequest;
import com.sigmap.common.dyna_fields.response.DynaFieldsResponse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.response.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dyna-fields")
@Slf4j
@Tag(name = "Dynamic Fields", description = "Dynamic fields settings")
public class DynaFieldsController {
  private final DynaFieldsService dynaFieldsService;

  /**
   * Constructor.
   *
   * @param dynaFieldsService the dynaFields service
   */
  public DynaFieldsController(DynaFieldsService dynaFieldsService) {
    this.dynaFieldsService = dynaFieldsService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<PageResponse<DynaFieldsResponse>> getDynaFields(
      DynaFieldsQuery query, Pageable pageable) {

    Specification<DynaFields> spec = query.toSpecification();
    if (query.getDynaDocVer() == null) {
      spec = spec.and(DynaFieldsSpecifications.latestVersionPerId());
    }

    if (query.getAll() != null && query.getAll()) {
      List<DynaFields> dynaFields = dynaFieldsService.findAll(spec);
      List<DynaFieldsResponse> dynaFieldsResponse = DynaFieldsMapper.INSTANCE.map(dynaFields);
      return ResponseEntity.ok(PageResponse.of(dynaFieldsResponse));
    }

    Page<DynaFields> dynaDocs = dynaFieldsService.findAll(spec, pageable);
    Page<DynaFieldsResponse> dynaDocsResponse = dynaDocs.map(DynaFieldsMapper.INSTANCE::map);

    return ResponseEntity.ok(PageResponse.of(dynaDocsResponse));
  }

  @GetMapping("/{dynaFieldsId}")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<DynaFieldsResponse> getDynaFieldsById(@PathVariable Integer dynaFieldsId) {

    DynaFields dynaFields =
        dynaFieldsService
            .findById(dynaFieldsId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    DynaFieldsResponse dynaFieldsResponse = DynaFieldsMapper.INSTANCE.map(dynaFields);

    return ResponseEntity.ok(dynaFieldsResponse);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<DynaFieldsResponse> createDynaFields(
      @RequestBody DynaFieldsRequest request) {
    try {
      DynaFields dynaFields = dynaFieldsService.create(request);
      return ResponseEntity.ok(DynaFieldsMapper.INSTANCE.map(dynaFields));
    } catch (RefParamNotFound e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type ID", e);
    }
  }

  @PutMapping("/{dynaFieldId}")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<DynaFieldsResponse> updateDynaFields(
      @PathVariable Integer dynaFieldId, @RequestBody DynaFieldsRequest request) {
    try {
      DynaFields dynaFields = dynaFieldsService.update(dynaFieldId, request);
      return ResponseEntity.ok(DynaFieldsMapper.INSTANCE.map(dynaFields));
    } catch (RefParamNotFound e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type ID", e);
    }
  }

  @DeleteMapping("/{dynaFieldId}")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<DynaFieldsResponse> deleteDynaFields(@PathVariable Integer dynaFieldId) {

    DynaFields dynaFields =
        dynaFieldsService
            .findById(dynaFieldId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    dynaFieldsService.delete(dynaFieldId);
    DynaFieldsResponse dynaFieldsResponse = DynaFieldsMapper.INSTANCE.map(dynaFields);
    return ResponseEntity.ok(dynaFieldsResponse);
  }
}
