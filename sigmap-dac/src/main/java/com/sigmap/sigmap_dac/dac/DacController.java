package com.sigmap.sigmap_dac.dac;

import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.DacServiceImpl;
import com.sigmap.common.dac.mapper.DacMapper;
import com.sigmap.common.dac.request.CreateDacRequest;
import com.sigmap.common.dac.request.DacQuery;
import com.sigmap.common.dac.request.DacRequest;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.response.DacResponce;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.DacDocsService;
import com.sigmap.common.dac_docs.mappers.DacDocsMapper;
import com.sigmap.common.dac_docs.response.DacDocsResponseFullEntities;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.menu_item.mappers.MenuItemMapper;
import com.sigmap.common.menu_item.request.MenuItemQuery;
import com.sigmap.common.menu_item.response.MenuItemResponse;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.response.RealisationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/dac")
@Slf4j
@Tag(name = "DAC", description = "Dossier d'Appel a Condidature")
public class DacController {

  private final DacService dacService;
  private final DacDocsService dacDocsService;

  @Value("${templateDac.dac.inputDocPath}")
  private String basePath;

  @Value("${templateDac.dac.outputDocPath}")
  private String customDirPath;

  @Value("${documents.convertUrl}")
  private String convertUrl;

  public DacController(DacService dacService, DacDocsService dacDocsService) {
    this.dacService = dacService;
    this.dacDocsService = dacDocsService;

  }


  @GetMapping
  public ResponseEntity<Page<DacResponce>> findAll(DacQuery query, Pageable pageable) {
    log.debug("API for get All dac .... ");
    Page<Dac> dac = dacService.findAll(query.toSpecification(), pageable);
    Page<DacResponce> dacResponse = dac.map(DacMapper.INSTANCE::map);
    return ResponseEntity.ok(dacResponse);
  }

  @GetMapping("/{dacId}/{dacVer}")
  public ResponseEntity<Optional<DacResponce>> findById(@PathVariable Integer dacId,
                                                        @PathVariable Integer dacVer) {
    log.debug("API for get All dac .... ");
    Optional<Dac> dac = dacService.findById(dacId, dacVer);
    Optional<DacResponce> dacResponse = dac.map(DacMapper.INSTANCE::map);
    return ResponseEntity.ok(dacResponse);
  }


  @GetMapping("/docs/{dacId}/{dacVer}")
  public ResponseEntity<DacResponseDTO> getDacDocs(
          @PathVariable Integer dacId,
          @PathVariable Integer dacVer) {
    DacResponseDTO response = dacDocsService.findByDacIdAndVer(dacId, dacVer);
    if (response != null) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.notFound().build();
    }
  }


  @PostMapping("/create")
  public ResponseEntity<DacResponce> createDac(@RequestBody CreateDacRequest request) throws RefParamNotFound, RealisationNotFoundException {
    Dac saved = dacService.create(request);
    DacResponce responce = DacMapper.INSTANCE.map(saved);
    return ResponseEntity.status(HttpStatus.CREATED).body(responce);
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
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<byte[]> response = restTemplate.exchange(
              convertUrl + "/forms/libreoffice/convert",
              HttpMethod.POST,
              requestEntity,
              byte[].class
      );

      // 5. Return the PDF as a download
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.APPLICATION_PDF);
      responseHeaders.setContentDispositionFormData("attachment", "document.pdf");

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
                convertUrl + "/forms/libreoffice/convert",
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
