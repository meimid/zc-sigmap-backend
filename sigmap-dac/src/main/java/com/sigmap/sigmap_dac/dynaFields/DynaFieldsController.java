package com.sigmap.sigmap_dac.dynaFields;

import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsService;
import com.sigmap.common.dyna_fields.exceptions.DynaFieldNotFound;
import com.sigmap.common.dyna_fields.mappers.DynaFieldsMapper;
import com.sigmap.common.dyna_fields.request.DynaFieldsQuery;
import com.sigmap.common.dyna_fields.request.DynaFieldsRequest;
import com.sigmap.common.dyna_fields.response.DynaFieldsResponse;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsService;
import com.sigmap.common.dyna_fields_details.mappers.DynaFieldsDetailsMapper;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsQuery;
import com.sigmap.common.dyna_fields_details.request.DynaFieldsDetailsRequest;
import com.sigmap.common.dyna_fields_details.response.DynaFieldsDetailsResponse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.response.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
  private final DynaFieldsDetailsService dynaFieldsDetailsService;

  /**
   * Constructor.
   *
   * @param dynaFieldsService the dynaFields service
   */
  public DynaFieldsController(
      DynaFieldsService dynaFieldsService, DynaFieldsDetailsService dynaFieldsDetailsService) {
    this.dynaFieldsService = dynaFieldsService;
    this.dynaFieldsDetailsService = dynaFieldsDetailsService;
  }

  @GetMapping
  public ResponseEntity<PageResponse<DynaFieldsResponse>> getDynaFields(
      DynaFieldsQuery query, Pageable pageable) {

    if (query.getAll() != null && query.getAll()) {
      List<DynaFields> dynaFields = dynaFieldsService.findAll(query.toSpecification());
      List<DynaFieldsResponse> dynaFieldsResponse = DynaFieldsMapper.INSTANCE.map(dynaFields);
      return ResponseEntity.ok(PageResponse.of(dynaFieldsResponse));
    }

    Page<DynaFields> dynaDocs = dynaFieldsService.findAll(query.toSpecification(), pageable);
    Page<DynaFieldsResponse> dynaDocsResponse = dynaDocs.map(DynaFieldsMapper.INSTANCE::map);

    return ResponseEntity.ok(PageResponse.of(dynaDocsResponse));
  }

  @GetMapping("/{dynaFieldsId}")
  public ResponseEntity<DynaFieldsResponse> getDynaFieldsById(@PathVariable Integer dynaFieldsId) {

    DynaFields dynaFields =
        dynaFieldsService
            .findById(dynaFieldsId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    DynaFieldsResponse dynaFieldsResponse = DynaFieldsMapper.INSTANCE.map(dynaFields);

    return ResponseEntity.ok(dynaFieldsResponse);
  }

  @PostMapping
  public ResponseEntity<DynaFieldsResponse> createDynaFields(
      @RequestBody DynaFieldsRequest request) {
    try {
      DynaFields dynaFields = dynaFieldsService.create(request);
      return ResponseEntity.ok(DynaFieldsMapper.INSTANCE.map(dynaFields));
    } catch (RefParamNotFound e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type ID", e);
    }
  }

  @GetMapping("/{dynaFieldsId}/details")
  public ResponseEntity<List<DynaFieldsDetailsResponse>> getDynaFieldsDetails(
      @PathVariable Integer dynaFieldsId) {

    DynaFieldsDetailsQuery detailsQuery = new DynaFieldsDetailsQuery();
    detailsQuery.setDynaFieldsId(dynaFieldsId);

    List<DynaFieldsDetails> dynaFieldsDetails =
        dynaFieldsDetailsService.findAll(detailsQuery.toSpecification());

    List<DynaFieldsDetailsResponse> dynaFieldsDetailsResponse =
        DynaFieldsDetailsMapper.INSTANCE.map(dynaFieldsDetails);

    return ResponseEntity.ok(dynaFieldsDetailsResponse);
  }

  @PostMapping("/{dynaFieldsId}/details")
  public ResponseEntity<DynaFieldsDetailsResponse> createDynaFieldsDetails(
      @PathVariable Integer dynaFieldsId, @RequestBody DynaFieldsDetailsRequest request) {
    DynaFields dynaFields =
        dynaFieldsService
            .findById(dynaFieldsId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid field ID"));

    try {
      DynaFieldsDetails dynaFieldsDetails = dynaFieldsDetailsService.create(dynaFields, request);
      return ResponseEntity.ok(DynaFieldsDetailsMapper.INSTANCE.map(dynaFieldsDetails));
    } catch (DynaFieldNotFound e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid field ID", e);
    }
  }
}
