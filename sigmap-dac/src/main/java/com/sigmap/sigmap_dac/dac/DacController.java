package com.sigmap.sigmap_dac.dac;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.DacServiceImpl;
import com.sigmap.common.dac.exeption.TemplateFileNameNotFound;
import com.sigmap.common.dac.mapper.DacMapper;
import com.sigmap.common.dac.request.CreateDacRequest;
import com.sigmap.common.dac.request.DacRequest;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac.response.DacResponce;
import com.sigmap.common.menu_item.mappers.MenuItemMapper;
import com.sigmap.common.menu_item.response.MenuItemResponse;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/dac")
@Slf4j
public class DacController {

  private final DacService dacService;

  @Value("${templateDac.dac.inputDocPath}")
  private String basePath;

  @Value("${templateDac.dac.outputDocPath}")
  private String customDirPath;

  @Autowired
  private DacRepository dacRepository;
  @Autowired
  private PpmRepository ppmRepository;
  @Autowired
  private RefParamRepository refParamRepository;

  public DacController(DacService dacService) {
    this.dacService = dacService;
  }

  // create a new endpoint for creating a new dac

  @PostMapping("/create")
  public ResponseEntity<DacResponce> createDac(@RequestBody CreateDacRequest request) {
    Dac saved = dacService.create(request);
    DacResponce responce = DacMapper.INSTANCE.map(saved);
    return ResponseEntity.status(HttpStatus.CREATED).body(responce);
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

  @PostMapping("/generate-specific-pdf")
  public ResponseEntity<byte[]> generatePdfWithGotenberg(@RequestBody DacRequest request) {
    try {
      // 1. Generate DOCX file as before
      String generatedFile = dacService.generateDocumentWithBookmarks(
              request.getDacId(), request.getDacVer(), request.getDynaDocsId(), request.getDynaDocsVer(), basePath, customDirPath);

      log.info("Generated file .........: {}", generatedFile);
      // 2. Prepare the file for upload
      FileSystemResource docxResource = new FileSystemResource(new java.io.File(generatedFile));

      // 3. Prepare the multipart request
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("files", docxResource);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      // 4. Send request to Gotenberg Api and get the response
      String gotenbergUrl = "http://localhost:3000";
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<byte[]> response = restTemplate.exchange(
              gotenbergUrl + "/forms/libreoffice/convert",
              HttpMethod.POST,
              requestEntity,
              byte[].class
      );

      // 5. Return the PDF as a download
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.APPLICATION_PDF);
      responseHeaders.setContentDispositionFormData("attachment", "filled-document.pdf");

      return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PostMapping("/generate-list-pdf")
  public ResponseEntity<byte[]> replaceBookmarksAndGenerateListDOCpdf(@RequestBody DacRequest request) {
    try {
      // . Generate multiple DOCX files as before
      List<String> generatedFiles = dacService.generateListDocumentsWithBookmarks(request.getDacId(), request.getDacVer(), basePath, customDirPath);

      log.info("Generated files .........: {}", generatedFiles);

      // . Convert each DOCX to PDF using Gotenberg
      String gotenbergUrl = "http://localhost:3000";
      RestTemplate restTemplate = new RestTemplate();
      List<InputStream> pdfStreams = new ArrayList<>();
      for (String docxPath : generatedFiles) {
        FileSystemResource docxResource = new FileSystemResource(new java.io.File(docxPath));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", docxResource);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(
            gotenbergUrl + "/forms/libreoffice/convert",
            HttpMethod.POST,
            requestEntity,
            byte[].class
        );
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
          pdfStreams.add(new java.io.ByteArrayInputStream(response.getBody()));
        } else {
          throw new RuntimeException("Failed to convert DOCX to PDF for file: " + docxPath);
        }
      }

      // . Merge the PDFs using DacServiceImpl.merge
      byte[] mergedPdf;
      try {
        ByteArrayOutputStream mergedOut = DacServiceImpl.merge(pdfStreams);
        mergedPdf = mergedOut.toByteArray();
      } finally {
        for (java.io.InputStream is : pdfStreams) {
          try { is.close(); } catch (Exception ignore) {}
        }
      }

      // 4. Return the merged PDF as a single download
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.APPLICATION_PDF);
      responseHeaders.setContentDispositionFormData("attachment", "merged-documents.pdf");

      return new ResponseEntity<>(mergedPdf, responseHeaders, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(("Failed to generate documents: " + e.getMessage()).getBytes());
    }
  }


}
