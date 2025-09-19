package com.sigmap.sigmap_dac.dynaDocs;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsService;
import com.sigmap.common.dyna_docs.mappers.DynaDocsMapper;
import com.sigmap.common.dyna_docs.request.DynaDocsQuery;
import com.sigmap.common.dyna_docs.request.DynaDocsRequest;
import com.sigmap.common.dyna_docs.response.DynaDocsResponse;
import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsService;
import com.sigmap.common.dyna_fields.mappers.DynaFieldsMapper;
import com.sigmap.common.dyna_fields.request.DynaFieldsQuery;
import com.sigmap.common.dyna_fields.response.DynaFieldsResponse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.response.PageResponse;
import com.sigmap.sigmap_dac.templates.TemplateService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dyna-docs")
@Slf4j
@Tag(name = "Dynamic documents", description = "Dynamic documents settings")
public class DynaDocsController {
  private final DynaDocsService dynaDocsService;
  private final DynaFieldsService dynaFieldsService;
  private final TemplateService templateService;

  /**
   * Constructor.
   *
   * @param dynaDocsService the dynaDocs service
   * @param dynaFieldsService the dynaFields service
   * @param templateService the template service
   */
  public DynaDocsController(
      DynaDocsService dynaDocsService,
      DynaFieldsService dynaFieldsService,
      TemplateService templateService) {
    this.dynaDocsService = dynaDocsService;
    this.dynaFieldsService = dynaFieldsService;
    this.templateService = templateService;
  }

  @GetMapping
  public ResponseEntity<PageResponse<DynaDocsResponse>> getDynaDocs(
      DynaDocsQuery query, Pageable pageable) {
    Page<DynaDocs> dynaDocs = dynaDocsService.findAll(query.toSpecification(), pageable);
    Page<DynaDocsResponse> dynaDocsResponse = dynaDocs.map(DynaDocsMapper.INSTANCE::map);
    return ResponseEntity.ok(PageResponse.of(dynaDocsResponse));
  }

  @GetMapping("/{dynaDocsId}")
  public ResponseEntity<DynaDocsResponse> getDynaDocsById(
      @PathVariable Integer dynaDocsId,
      @RequestParam(required = false) Integer version,
      @RequestParam(required = false) Boolean withFields) {

    // DynaFieldsQuery dynaFieldsQuery = new DynaFieldsQuery();
    // dynaFieldsQuery.setAll(true);
    //
    // Specification<DynaFields> dynaFieldsSpec = dynaFieldsQuery.toSpecification();
    //
    //
    // if (version != null) {
    //   dynaFieldsSpec = dynaFieldsSpec.and(DynaFieldsSpecifications.dynaDocVer(version));
    //   dynaFieldsSpec = dynaFieldsSpec.and(DynaFieldsSpecifications.dynaDocId(dynaDocsId));
    // } else {
    //   dynaFieldsSpec =
    // dynaFieldsSpec.and(DynaFieldsSpecifications.latestVersionPerId(dynaDocsId));
    // }

    if (version == null) {
      DynaDocs dynaDocs = dynaDocsService.findLatestById(dynaDocsId).orElseThrow();
      DynaDocsResponse dynaDocsResponse = DynaDocsMapper.INSTANCE.map(dynaDocs);

      if (withFields != null && withFields) {
        DynaFieldsQuery dynaFieldsQuery = new DynaFieldsQuery();
        dynaFieldsQuery.setAll(true);
        dynaFieldsQuery.setDynaDocId(dynaDocsId);
        dynaFieldsQuery.setDynaDocVer(dynaDocs.getVersion());

        List<DynaFields> dynaFields = dynaFieldsService.findAll(dynaFieldsQuery.toSpecification());
        List<DynaFieldsResponse> dynaFieldsResponse = DynaFieldsMapper.INSTANCE.map(
                dynaFields.stream().filter(f -> !"__DEFAULT__".equals(f.getName())).toList()
        );

        dynaDocsResponse.setFields(dynaFieldsResponse);
      }

      return ResponseEntity.ok(dynaDocsResponse);
    }

    DynaDocs dynaDocs = dynaDocsService.findById(dynaDocsId, version).orElseThrow();
    DynaDocsResponse dynaDocsResponse = DynaDocsMapper.INSTANCE.map(dynaDocs);

    if (withFields != null && withFields) {
      DynaFieldsQuery dynaFieldsQuery = new DynaFieldsQuery();
      dynaFieldsQuery.setAll(true);
      dynaFieldsQuery.setDynaDocId(dynaDocsId);
      dynaFieldsQuery.setDynaDocVer(dynaDocs.getVersion());

      List<DynaFields> dynaFields = dynaFieldsService.findAll(dynaFieldsQuery.toSpecification());
      List<DynaFieldsResponse> dynaFieldsResponse = DynaFieldsMapper.INSTANCE.map(
              dynaFields.stream().filter(f -> !"__DEFAULT__".equals(f.getName())).toList()
      );

      dynaDocsResponse.setFields(dynaFieldsResponse);
    }

    return ResponseEntity.ok(dynaDocsResponse);
  }

  @PostMapping
  public ResponseEntity<DynaDocsResponse> createDynaDocs(
      @RequestBody DynaDocsRequest dynaDocsRequest) {
    boolean templateExists = templateService.exists(dynaDocsRequest.getTemplateFileName());
    if (!templateExists) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template file not found");
    }

    if (dynaDocsRequest.getGuideFileName() != null
        && !dynaDocsRequest.getGuideFileName().isBlank()) {
      boolean guideExists = templateService.exists(dynaDocsRequest.getGuideFileName());
      if (!guideExists) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guide file not found");
      }
    }

    try {
      DynaDocs dynaDocs = dynaDocsService.create(dynaDocsRequest);
      DynaDocsResponse dynaDocsResponse = DynaDocsMapper.INSTANCE.map(dynaDocs);
      return ResponseEntity.ok(dynaDocsResponse);
    } catch (RefParamNotFound e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID", e);
    }
  }

  @PutMapping("/{dynaDocId}")
  public ResponseEntity<DynaDocsResponse> updateDynaDoc(
      @PathVariable Integer dynaDocId, @RequestBody DynaDocsRequest dynaDocsRequest) {
    boolean templateExists = templateService.exists(dynaDocsRequest.getTemplateFileName());
    if (!templateExists) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template file not found");
    }

    if (dynaDocsRequest.getGuideFileName() != null
        && !dynaDocsRequest.getGuideFileName().isBlank()) {
      boolean guideExists = templateService.exists(dynaDocsRequest.getGuideFileName());
      if (!guideExists) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guide file not found");
      }
    }

    DynaDocs dynaDocs = dynaDocsService.update(dynaDocId, dynaDocsRequest);
    DynaDocsResponse dynaDocsResponse = DynaDocsMapper.INSTANCE.map(dynaDocs);
    return ResponseEntity.ok(dynaDocsResponse);
  }
}
