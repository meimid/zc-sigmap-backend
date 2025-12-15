package com.sigmap.sigmap_dac.dac;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.appel_offre.exception.AppelOffreNotFound;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.DacServiceImpl;
import com.sigmap.common.dac.exeption.InvalidDacRequestException;
import com.sigmap.common.dac.mapper.DacMapper;
import com.sigmap.common.dac.request.*;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.response.DacResponce;
import com.sigmap.common.dac.response.DacWithAoReferenceResponse;
import com.sigmap.common.dac.response.ParticipantResponse;
import com.sigmap.common.dac_docs.DacDocsService;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/dac")
@Slf4j
@Tag(name = "DAC", description = "Dossier d'Appel a Condidature")
public class DacController {

  private final DacService dacService;
  private final DacDocsService dacDocsService;
  private final DynaDocsRepository dynaDocsRepository;

  @Value("${templateDac.dac.inputDocPath}")
  private String basePath;

  @Value("${templateDac.dac.outputDocPath}")
  private String customDirPath;

  @Value("${documents.convertUrl}")
  private String convertUrl;

  public DacController(DacService dacService, DacDocsService dacDocsService, DynaDocsRepository dynaDocsRepository) {
    this.dacService = dacService;
    this.dacDocsService = dacDocsService;
    this.dynaDocsRepository = dynaDocsRepository;

  }


  @GetMapping
  public ResponseEntity<Page<DacResponce>> findAll(DacQuery query, Pageable pageable) {
    log.debug("API for get All dac .... ");
    // Return only latest version per DAC id
    Page<Dac> dac = dacService.findAllLatest(pageable);
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
    DacResponseDTO response = dacService.findByDacIdAndVer(dacId, dacVer);
    if (response != null) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/sync/{dacId}/{dacVer}")
  public ResponseEntity<DacResponseDTO> synchronizeDacToLatest(@PathVariable Integer dacId,
                                                               @PathVariable Integer dacVer) {
    DacResponseDTO response = dacService.synchronizeToLatestDynaDocs(dacId, dacVer);
    return ResponseEntity.ok(response);
  }


  @PostMapping("/create")
  @PreAuthorize("hasAnyRole('REDACTEUR_AC')")
  public ResponseEntity<DacResponce> createDac(@RequestBody CreateDacRequest request) throws RefParamNotFound, RealisationNotFoundException, ModeDePassationSelectionNotFound {
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

  @PostMapping("/previsualiser-specific-pdf")
  public ResponseEntity<Map<String, String>> generatePdfForVisualise(@RequestBody DacRequest request) {
    try {
      // 1. Generate DOCX file
      String generatedFile = dacService.generateDocumentWithBookmarks(
              request.getDacId(), request.getDacVer(), request.getDynaDocsId(), request.getDynaDocsVer(), basePath, customDirPath);

      log.info("Generated DOCX file: {}", generatedFile);

      // 2. Prepare the file for upload
      FileSystemResource docxResource = new FileSystemResource(new java.io.File(generatedFile));

      // 3. Prepare the multipart request
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("files", docxResource);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      // 4. Send request to Gotenberg API and get the PDF response
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<byte[]> response = restTemplate.exchange(
              convertUrl + "/forms/libreoffice/convert",
              HttpMethod.POST,
              requestEntity,
              byte[].class
      );

      // 5. Convert PDF bytes to base64 for Angular visualization
      String base64Pdf = Base64.getEncoder().encodeToString(response.getBody());

      // get file name from data base
      String fileName = dynaDocsRepository.getDynaDocName(request.getDynaDocsId(), request.getDynaDocsVer());

      // 6. Prepare JSON response with base64 string
      Map<String, String> responseBody = new HashMap<>();
      responseBody.put("pdfBase64", base64Pdf);
      responseBody.put("filename", fileName + ".pdf");
      responseBody.put("extension", "PDF");

      return new ResponseEntity<>(responseBody, HttpStatus.OK);

    } catch (Exception e) {
      log.error("Error generating PDF: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PostMapping("/generate-list-pdf")
  public ResponseEntity<byte[]> replaceBookmarksAndGenerateListDOCpdf(@RequestBody DacListRequest request) {
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

  @GetMapping("/get-dac-pdf/{dacId}/{dacVer}")
  public ResponseEntity<byte[]> generateLatestMergedPdf(@PathVariable Integer dacId,Integer dacVer) {
    try {
      Optional<Dac> dacOpt = dacService.findById(dacId, dacVer);
      if (dacOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
      Dac dac = dacOpt.get();

      List<String> generatedFiles = dacService.generateListDocumentsWithBookmarks(dac.getDacId(), dac.getDacVer(), basePath, customDirPath);

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

      byte[] mergedPdf;
      try {
        ByteArrayOutputStream mergedOut = DacServiceImpl.merge(pdfStreams);
        mergedPdf = mergedOut.toByteArray();
      } finally {
        for (java.io.InputStream is : pdfStreams) {
          try { is.close(); } catch (Exception ignore) {}
        }
      }

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.APPLICATION_PDF);
      responseHeaders.setContentDispositionFormData("attachment", "dac-" + dac.getDacId() + "-v" + dac.getDacVer() + ".pdf");

      return new ResponseEntity<>(mergedPdf, responseHeaders, HttpStatus.OK);
    } catch (Exception e) {
      log.error("Failed to generate latest merged PDF for dacId={}: {}", dacId, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PostMapping("/generate-aao-pdf")
  public ResponseEntity<byte[]> generatePdfforAAO(@RequestBody ModeleAAORequest request) {
    try {
      // 1. Generate DOCX file as before
      String generatedFile = dacService.generateDAODocumentWithBookmarks(
              request.getDacId(), request.getDacVersion(), request.getSignataire(), request.getFonction(), basePath, customDirPath);

      log.info("Generated file .........: {}", generatedFile);
      // 2. Prepare the file for upload
      FileSystemResource docxResource = new FileSystemResource(new File(generatedFile));

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
      responseHeaders.setContentDispositionFormData("attachment", "AAO.pdf");

      return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/published/{realisationId}/{realisationVer}")
  public ResponseEntity<List<DacResponce>> getPublishedDacsByRealisation(
          @PathVariable Integer realisationId,
          @PathVariable Integer realisationVer) {
    log.debug("REST request to get published DACs for realisation ID: {} and version: {}", realisationId, realisationVer);
    List<Dac> dacs = dacService.findPublishedDacsByRealisationIdAndVersion(realisationId, realisationVer);
    List<DacResponce> dacResponse = dacs.stream().map(DacMapper.INSTANCE::map).toList();
    return ResponseEntity.ok(dacResponse);
  }

  @PostMapping("/create-from-ao")
  @PreAuthorize("hasAnyRole('REDACTEUR_AC')")
  public ResponseEntity<?> createDacFromAo(@Valid @RequestBody CreateDacFromAoRequest request) {
    try {
      log.debug("REST request to create DAC(s) from AO with ID: {}", request.getAppelOffreId());
      List<Dac> createdDacs = dacService.createFromAppelOffre(request);
      List<DacResponce> response = createdDacs.stream()
              .map(DacMapper.INSTANCE::map)
              .toList();
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (InvalidDacRequestException e) {
      log.error("Validation error while creating DAC from AO: {}", e.getMessage());
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (AppelOffreNotFound e) {
      log.error("AppelOffre not found: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Error creating DAC from AO: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("error", "An error occurred while creating DAC"));
    }
  }

  @GetMapping("/{dacId}/{dacVer}/with-ao-reference")
  public ResponseEntity<?> getDacWithAoReference(
          @PathVariable Integer dacId,
          @PathVariable Integer dacVer) {
    try {
      log.debug("REST request to get DAC with AO reference for ID: {}, Version: {}", dacId, dacVer);
      DacWithAoReferenceResponse response = dacService.getDacWithAoReference(dacId, dacVer);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error getting DAC with AO reference: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(Map.of("error", "DAC not found"));
    }
  }

  @GetMapping("/cpmp-members/{acId}")
  public ResponseEntity<?> getCpmpMembers(@PathVariable Integer acId) {
    try {
      log.debug("REST request to get CPMP members for AC ID: {}", acId);
      List<ParticipantResponse> members = dacService.getCpmpMembersByAc(acId);
      return ResponseEntity.ok(members);
    } catch (AcNotFound e) {
      log.error("AC not found: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Error getting CPMP members: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("error", "An error occurred while fetching CPMP members"));
    }
  }
}
