package com.sigmap.sigmap_dac.dynaDocs;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsService;
import com.sigmap.common.dyna_docs.mappers.DynaDocsMapper;
import com.sigmap.common.dyna_docs.request.DynaDocsQuery;
import com.sigmap.common.dyna_docs.request.DynaDocsRequest;
import com.sigmap.common.dyna_docs.response.DynaDocsResponse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.sigmap_dac.templates.TemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dyna-docs")
@Slf4j
@Tag(name = "Dynamic documents", description = "Dynamic documents settings")
public class DynaDocsController {
  private final DynaDocsService dynaDocsService;
  private final TemplateService templateService;

  /**
   * Constructor.
   *
   * @param dynaDocsService the dynaDocs service
   * @param templateService the template service
   */
  public DynaDocsController(DynaDocsService dynaDocsService, TemplateService templateService) {
    this.dynaDocsService = dynaDocsService;
    this.templateService = templateService;
  }

  @GetMapping
  public ResponseEntity<Page<DynaDocsResponse>> getDynaDocs(
      DynaDocsQuery query, Pageable pageable) {
    Page<DynaDocs> dynaDocs = dynaDocsService.findAll(query.toSpecification(), pageable);
    Page<DynaDocsResponse> dynaDocsResponse = dynaDocs.map(DynaDocsMapper.INSTANCE::map);

    return ResponseEntity.ok(dynaDocsResponse);
  }

  @PostMapping
  public ResponseEntity<DynaDocsResponse> createDynaDocs(
      @RequestBody DynaDocsRequest dynaDocsRequest) {
    boolean templateExists = templateService.exists(dynaDocsRequest.getTemplateFileName());
    if (!templateExists) {
      throw new RuntimeException("Template not found");
    }

    try {
      DynaDocs dynaDocs = dynaDocsService.create(dynaDocsRequest);
      DynaDocsResponse dynaDocsResponse = DynaDocsMapper.INSTANCE.map(dynaDocs);
      return ResponseEntity.ok(dynaDocsResponse);
    } catch (RefParamNotFound e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID", e);
    }
  }
}
