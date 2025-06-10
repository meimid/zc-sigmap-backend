package com.sigmap.sigmap_dac.dac;

import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.exeption.TemplateFileNameNotFound;
import com.sigmap.common.dac.request.DacRequest;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/dac")
@Slf4j
public class DacController {

  private final DacService dacService;

  @Value("${templateDac.dac.inputDocPath}")
  private String basePath;

  @Value("${templateDac.dac.outputDocPath}")
  private String customDirPath;

  public DacController(DacService dacService) {
    this.dacService = dacService;
  }

  @PostMapping("/generate-doc")
  public ResponseEntity<List<String>> replaceBookmarksAndGenerateListDOC( @RequestBody DacRequest request) {
    if (request.getDacId() == null || request.getDacVer() == null) {
      return ResponseEntity.badRequest()
          .body(Collections.singletonList("Dac ID and version must not be null"));
    }

    try {
      List<String> generatedFiles = dacService.generateListDocumentsWithBookmarks( request.getDacId(), request.getDacVer(), basePath, customDirPath);
      return ResponseEntity.ok(generatedFiles);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Collections.singletonList(e.getMessage()));
    } catch (TemplateFileNameNotFound e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              Collections.singletonList(
                  "No active documents found for Dac ID: "
                      + request.getDacId()
                      + ", Version: "
                      + request.getDacVer()));
    } catch (Exception e) {
      // log.error("Error generating documents: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Collections.singletonList("Failed to generate documents: " + e.getMessage()));
    }
  }


  @PostMapping("/generate-specific-doc")
  public ResponseEntity<byte[]> downloadGenerateSpecificDocument(@RequestBody DacRequest request) {
    try {
      String generatedFile = dacService.generateDocumentWithBookmarks(request.getDacId(), request.getDacVer(), request.getDynaDocsId(), request.getDynaDocsVer(), basePath, customDirPath);
      log.info("Generated file .........: {}", generatedFile);

      Path filePath = Paths.get(generatedFile);
      byte[] fileBytes = Files.readAllBytes(filePath);
      String fileName = filePath.getFileName().toString();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDispositionFormData("attachment", fileName);
      return ResponseEntity.ok()
              .headers(headers)
              .body(fileBytes);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(null);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

 /* @PostMapping("/generate-specific-doc")
  public ResponseEntity<byte[]> downloadGenerateSpecificDocument(@RequestBody DacRequest request) {
    try {
      String generatedFile = dacService.generateDocumentWithBookmarks(request.getDacId(), request.getDacVer(), request.getDynaDocsId(), request.getDynaDocsVer(), basePath, customDirPath);
      log.info("Generated file .........: {}", generatedFile);
      // Convert DOCX to PDF using DOC2PDF helper
      String pdfFile = generatedFile.replaceAll("(?i)\\.docx$", ".pdf");
      DOC2PDF.convert(generatedFile, pdfFile);
      Path filePath = Paths.get(pdfFile);
      byte[] fileBytes = Files.readAllBytes(filePath);
      String fileName = filePath.getFileName().toString();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDispositionFormData("attachment", fileName);
      return ResponseEntity.ok()
              .headers(headers)
              .body(fileBytes);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(null);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }*/


}
