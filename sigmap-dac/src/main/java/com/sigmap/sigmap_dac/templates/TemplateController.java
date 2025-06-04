package com.sigmap.sigmap_dac.templates;

import com.sigmap.common.dyna_docs.DynaDocsService;
import com.sigmap.common.dyna_docs.request.DynaDocsQuery;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/templates")
@Slf4j
@Tag(name = "Templates", description = "Manage Templates")
public class TemplateController {
  private final TemplateService templateService;
  private final DynaDocsService dynaDocsService;

  /**
   * Constructor.
   *
   * @param templateService the template service
   */
  public TemplateController(TemplateService templateService, DynaDocsService dynaDocsService) {
    this.templateService = templateService;
    this.dynaDocsService = dynaDocsService;
  }

  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<TemplateResponse> uploadTemplate(@RequestPart("file") MultipartFile file) {
    try {
      String fileName = file.getOriginalFilename();

      log.info("Uploading template file: {}", fileName);

      templateService.uploadTemplate(file);

      TemplateResponse templateResponse = templateService.getTemplate(fileName);
      return ResponseEntity.status(HttpStatus.CREATED).body(templateResponse);
    } catch (Exception e) {
      log.error("Error uploading template: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @GetMapping("/{templateName}/download")
  public ResponseEntity<byte[]> downloadTemplate(@PathVariable String templateName) {
    byte[] templateBytes = templateService.downloadTemplate(templateName);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", templateName);

    return ResponseEntity.ok().headers(headers).body(templateBytes);
  }

  @GetMapping
  public ResponseEntity<List<TemplateResponse>> getTemplates() {
    List<TemplateResponse> templateResponses = templateService.getTemplates();

    return ResponseEntity.ok(templateResponses);
  }

  @GetMapping("/{templateName}")
  public ResponseEntity<TemplateResponse> getTemplate(@PathVariable String templateName) {
    TemplateResponse templateResponse = templateService.getTemplate(templateName);

    return ResponseEntity.ok(templateResponse);
  }

  @DeleteMapping("/{templateName}")
  public ResponseEntity<Void> deleteTemplate(@PathVariable String templateName) {

    DynaDocsQuery dynaDocsQuery = new DynaDocsQuery();
    dynaDocsQuery.setTemplateFileName(templateName);
    long count = dynaDocsService.count(dynaDocsQuery.toSpecification());

    if (count > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template is in use");
    }

    templateService.deleteTemplate(templateName);

    return ResponseEntity.noContent().build();
  }
}
