package com.sigmap.sigmap_dac.templates;

import com.sigmap.common.docs.exeptions.MaxSizeFileExeption;
import com.sigmap.common.dyna_docs.DynaDocsService;
import com.sigmap.common.dyna_docs.request.DynaDocsQuery;
import com.sigmap.common.docs.file_check.FileSizeConversion;
import com.sigmap.common.docs.exeptions.MaxSizeFileExeption;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
  private final FileSizeConversion fileSizeConversion;
  private final MessageSource messageSource;

  @Value("${max-file-size}")
  private String maxFileSize;

  /**
   * Constructor.
   *
   * @param templateService the template service
   * @param dynaDocsService the dyna docs service
   * @param fileSizeConversion the file size conversion utility
   * @param messageSource the message source for internationalization
   */
  public TemplateController(TemplateService templateService, DynaDocsService dynaDocsService, 
                          FileSizeConversion fileSizeConversion, MessageSource messageSource) {
    this.templateService = templateService;
    this.dynaDocsService = dynaDocsService;
    this.fileSizeConversion = fileSizeConversion;
    this.messageSource = messageSource;
  }

  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<TemplateResponse> uploadTemplate(@RequestPart("file") MultipartFile file) {
    try {
      String fileName = file.getOriginalFilename();

      log.info("Uploading template file: {}", fileName);

      // Log file size for debugging
      double fileSizeMB = fileSizeConversion.getFileSizeInMB(file);
      log.debug("File size: {} MB (Max allowed: {})", fileSizeMB, maxFileSize);

      // Validate file size using business rules
      validateFileSize(file);

      templateService.uploadTemplate(file);

      TemplateResponse templateResponse = templateService.getTemplate(fileName);
      return ResponseEntity.status(HttpStatus.CREATED).body(templateResponse);
    } catch (MaxSizeFileExeption e) {
      log.error("File size validation failed for template: {}", file.getOriginalFilename());
      throw e; // Re-throw to be handled by GlobalExceptionHandler
    } catch (Exception e) {
      log.error("Error uploading template: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error uploading template");
    }
  }

  /**
   * Validates file size against business rules.
   *
   * @param file the file to validate
   * @throws MaxSizeFileExeption if file size exceeds the limit
   */
  private void validateFileSize(MultipartFile file) throws MaxSizeFileExeption {
    long fileSizeBytes = fileSizeConversion.getFileSize(file);
    long maxSizeBytes = parseSizeToBytes(maxFileSize);
    
    if (fileSizeBytes > maxSizeBytes) {
      throw new MaxSizeFileExeption();
    }
  }

  /**
   * Converts size string (e.g., "30MB") to bytes.
   *
   * @param sizeString the size string to parse
   * @return size in bytes
   */
  private long parseSizeToBytes(String sizeString) {
    if (sizeString == null || sizeString.trim().isEmpty()) {
      return 30 * 1024 * 1024; // Default 30MB
    }
    
    String upperSize = sizeString.toUpperCase().trim();
    if (upperSize.endsWith("MB")) {
      long mb = Long.parseLong(upperSize.substring(0, upperSize.length() - 2));
      return mb * 1024 * 1024;
    } else if (upperSize.endsWith("KB")) {
      long kb = Long.parseLong(upperSize.substring(0, upperSize.length() - 2));
      return kb * 1024;
    } else if (upperSize.endsWith("GB")) {
      long gb = Long.parseLong(upperSize.substring(0, upperSize.length() - 2));
      return gb * 1024 * 1024 * 1024;
    } else {
      // Assume bytes if no unit specified
      return Long.parseLong(upperSize);
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
