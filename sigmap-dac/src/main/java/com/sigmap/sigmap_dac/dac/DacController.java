package com.sigmap.sigmap_dac.dac;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.DacServiceImpl;
import com.sigmap.common.dac.mapper.DacMapper;
import com.sigmap.common.dac.request.*;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.response.DacDropListResponse;
import com.sigmap.common.dac.response.DacResponce;
import com.sigmap.common.dac.response.DacWithAoReferenceResponse;
import com.sigmap.common.dac_docs.DacDocsService;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_docs_copy.DynaDocsCopy;
import com.sigmap.common.dyna_docs_copy.DynaDocsCopyRepository;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.request.PpmQuery;
import com.sigmap.common.ppms.response.PpmDropListDto;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/dac")
@Slf4j
@Tag(name = "DAC", description = "Dossier d'Appel a Condidature")
public class DacController {

  private final DacService dacService;
  private final DynaDocsRepository dynaDocsRepository;
    private final DacRepository dacRepository;
    private final DynaDocsCopyRepository dynaDocsCopyRepository;

  @Value("${templateDac.dac.inputDocPath}")
  private String basePath;

  @Value("${templateDac.dac.inputModifiedDocPath}")
  private String inputModifiedDocPath;

  @Value("${templateDac.dac.outputDocPath}")
  private String customDirPath;

  @Value("${documents.convertUrl}")
  private String convertUrl;

  public DacController(DacService dacService, DynaDocsRepository dynaDocsRepository, DacRepository dacRepository,DynaDocsCopyRepository dynaDocsCopyRepository) {
    this.dacService = dacService;
    this.dynaDocsRepository = dynaDocsRepository;
      this.dacRepository = dacRepository;
      this.dynaDocsCopyRepository = dynaDocsCopyRepository;
  }


  @GetMapping
  public ResponseEntity<Page<DacResponce>> findAll(DacQuery query, Pageable pageable) {
    log.debug("API for get All dac .... ");
    // Return only latest version per DAC id
    Page<Dac> dac = dacService.findAllLatestWithQuery(query, pageable);
    Page<DacResponce> dacResponse = dac.map(DacMapper.INSTANCE::map);
    return ResponseEntity.ok(dacResponse);
  }

  @GetMapping("/appel-offre/{appelOffreId}/latest")
  public ResponseEntity<Page<DacResponce>> getDacsByAppelOffre(
          @PathVariable Integer appelOffreId,
          Pageable pageable) {
    log.debug("REST request to get latest DACs for Appel d'Offre ID: {}", appelOffreId);
    DacQuery query = new DacQuery();
    query.setAppelOffreId(appelOffreId);
    Page<Dac> dac = dacService.findAllLatestWithQuery(query, pageable);
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
      log.info("Generating PDF for DAC ID: {} and Version: {}", request.getDacId(), request.getDacVer());
      // get the dynaDocCopy
      DynaDocsCopy dynaDocCopy = dynaDocsCopyRepository.findByIdDynaDocsIdAndDynaDocsVer(request.getDynaDocsId(), request.getDynaDocsVer(),request.getDacId(),request.getDacVer())
              .orElseThrow(() -> new IllegalArgumentException("DynaDocsCopy not found for ID: " + request.getDynaDocsId() + ", Version: " + request.getDynaDocsVer()));
      // 1. Generate DOCX file as before
      String generatedFile = dacService.generateDocumentWithBookmarks( request.getDacId(), request.getDacVer(), dynaDocCopy.getDynaDocs().getId(), dynaDocCopy.getDynaDocs().getVersion(), inputModifiedDocPath, customDirPath);

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


  @PostMapping("/generate-specific-docx")
  public ResponseEntity<byte[]> downloadDocx(@RequestBody DacRequest request) {
    try {

      // get the list of dynadocs from dynaDocsCopy
      DynaDocsCopy dynaDocCopy = dynaDocsCopyRepository.findByIdDynaDocsIdAndDynaDocsVer(request.getDynaDocsId(), request.getDynaDocsVer(),request.getDacId(),request.getDacVer())
              .orElseThrow(() -> new IllegalArgumentException("DynaDocsCopy not found for ID: " + request.getDynaDocsId() + ", Version: " + request.getDynaDocsVer()));

      // 1. Generate DOCX file
      String generatedFile = dacService.generateDocumentWithBookmarks(request.getDacId(), request.getDacVer(), dynaDocCopy.getDynaDocs().getId(), dynaDocCopy.getDynaDocs().getVersion(), inputModifiedDocPath, customDirPath);

      log.info("Generated DOCX file: {}", generatedFile);

      if (Boolean.TRUE.equals(request.getGenerateDocx())) {
        File file = new File(generatedFile); // only one docx
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(file.getName()).build() );

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
      }


    } catch (Exception e) {
      log.error("Error generating PDF: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    return null;
  }


  @PostMapping("/previsualiser-specific-pdf")
  public ResponseEntity<Map<String, String>> generatePdfForVisualise(@RequestBody DacRequest request) {
    try {

      // get the dynadoc from dynaDocsCopy
      DynaDocsCopy dynaDocCopy = dynaDocsCopyRepository.findByIdDynaDocsIdAndDynaDocsVer(request.getDynaDocsId(), request.getDynaDocsVer(),request.getDacId(),request.getDacVer())
              .orElseThrow(() -> new IllegalArgumentException("DynaDocsCopy not found for ID: " + request.getDynaDocsId() + ", Version: " + request.getDynaDocsVer()));

      log.debug("DynaDocsCopy --------- ID: {} , Version: {}", dynaDocCopy.getDynaDocs().getId(), dynaDocCopy.getDynaDocs().getVersion());

      // 1. Generate DOCX file
      String generatedFile = dacService.generateDocumentWithBookmarks(
              request.getDacId(), request.getDacVer(), dynaDocCopy.getDynaDocs().getId(), dynaDocCopy.getDynaDocs().getVersion(), inputModifiedDocPath, customDirPath);

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
      log.error("Error generating PDF: {}", e.getStackTrace());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }



  @PostMapping("/generate-list-pdf")
  public ResponseEntity<byte[]> replaceBookmarksAndGenerateListDOCpdf(@RequestBody DacListRequest request) {
    try {
      // . Generate multiple DOCX files as before
      List<String> generatedFiles = dacService.generateListDocumentsWithBookmarks(request.getDacId(), request.getDacVer(), inputModifiedDocPath, customDirPath);

      log.info("Generated files .........: {}", generatedFiles);

      if (Boolean.TRUE.equals(request.getGenerateDocx())) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
          for (String docxPath : generatedFiles) {
            File file = new File(docxPath);
            try (FileInputStream fis = new FileInputStream(file)) {
              ZipEntry zipEntry = new ZipEntry(file.getName());
              zos.putNextEntry(zipEntry);
              byte[] bytes = new byte[1024];
              int length;
              while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
              }
              zos.closeEntry();
            }
          }
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType("application/zip"));
        responseHeaders.setContentDispositionFormData("attachment", "documents.zip");

        return new ResponseEntity<>(baos.toByteArray(), responseHeaders, HttpStatus.OK);
      }

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

  @GetMapping("/get-dac-pdf/{dacId}/{type}")
  public ResponseEntity<byte[]> generateLatestMergedPdf(@PathVariable Integer dacId, @PathVariable String type) {
    try {
      Dac dac = dacRepository.findDacWithLatestVersionByDacId(dacId);
      if (dac==null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }

      List<String> generatedFiles = dacService.generateListDocumentsWithBookmarks(dac.getDacId(), dac.getDacVer(), inputModifiedDocPath, customDirPath);

      /* ======================================================
           CASE 1: DOCX → ZIP DOWNLOAD
           ====================================================== */

      if ("docx".equalsIgnoreCase(type)) {

        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
          for (String docxPath : generatedFiles) {
            File file = new File(docxPath);

            zos.putNextEntry(new ZipEntry(file.getName()));
            Files.copy(file.toPath(), zos);
            zos.closeEntry();
          }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        String zipFileName = String.format(
                "dac-%d-v%d-documents.zip",
                dac.getDacId(),
                dac.getDacVer()
        );

        headers.setContentDispositionFormData("attachment", zipFileName);

        return new ResponseEntity<>(zipOut.toByteArray(), headers, HttpStatus.OK);
      }

      /* ======================================================
           CASE 2: PDF → CONVERT + MERGE (existing logic)
           ====================================================== */

      if ("pdf".equalsIgnoreCase(type)) {
        RestTemplate restTemplate = new RestTemplate();
        List<InputStream> pdfStreams = new ArrayList<>();

        for (String docxPath : generatedFiles) {

          FileSystemResource docxResource =
                  new FileSystemResource(new File(docxPath));

          MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
          body.add("files", docxResource);

          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.MULTIPART_FORM_DATA);

          HttpEntity<MultiValueMap<String, Object>> requestEntity =
                  new HttpEntity<>(body, headers);

          ResponseEntity<byte[]> response = restTemplate.exchange(
                  convertUrl + "/forms/libreoffice/convert",
                  HttpMethod.POST,
                  requestEntity,
                  byte[].class
          );

          if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            pdfStreams.add(new ByteArrayInputStream(response.getBody()));
          } else {
            throw new RuntimeException("Failed to convert DOCX to PDF: " + docxPath);
          }
        }

        byte[] mergedPdf;
        try {
          ByteArrayOutputStream mergedOut = DacServiceImpl.merge(pdfStreams);
          mergedPdf = mergedOut.toByteArray();
        } finally {
          for (InputStream is : pdfStreams) {
            try {
              is.close();
            } catch (Exception ignore) {
            }
          }
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_PDF);

        String pdfFileName = String.format(
                "dac-%d-v%d.pdf",
                dac.getDacId(),
                dac.getDacVer()
        );

        responseHeaders.setContentDispositionFormData("attachment", pdfFileName);

        return new ResponseEntity<>(mergedPdf, responseHeaders, HttpStatus.OK);
      }
    /* ======================================================
           INVALID TYPE
           ====================================================== */
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body(("Invalid type: " + type).getBytes());

    } catch (Exception e) {
      log.error("Failed to generate document for dacId={}: {}", dacId, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PostMapping("/generate-aao-pdf")
  public ResponseEntity<byte[]> generateAAODocument(@RequestBody ModeleAAORequest request) {
    try {

      // 1. Generate DOCX
      String generatedFilePath = dacService.generateDAODocumentWithBookmarks(request.getDacId(),request.getDacVersion(),request.getSignataire(),request.getFonction(),inputModifiedDocPath,customDirPath);

      File docxFile = new File(generatedFilePath);

      // 2. If generateDocx = true → download DOCX
      if (Boolean.TRUE.equals(request.getGenerateDocx())) {

        byte[] docxBytes = Files.readAllBytes(docxFile.toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
        );
        headers.setContentDispositionFormData("attachment", docxFile.getName());
        return new ResponseEntity<>(docxBytes, headers, HttpStatus.OK);
      }

      // 3. Else → convert DOCX to PDF using Gotenberg
      FileSystemResource docxResource = new FileSystemResource(docxFile);

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("files", docxResource);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, Object>> requestEntity =
              new HttpEntity<>(body, headers);

      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<byte[]> response = restTemplate.exchange(
              convertUrl + "/forms/libreoffice/convert",
              HttpMethod.POST,
              requestEntity,
              byte[].class
      );

      // 4. Return PDF
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.setContentType(MediaType.APPLICATION_PDF);
      responseHeaders.setContentDispositionFormData("attachment", "AAO.pdf");

      return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);

    } catch (Exception e) {
      log.error("Error generating document", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
  public ResponseEntity<DacResponce> createDacFromAo(@Valid @RequestBody CreateDacFromAoRequest request) {
      log.debug("REST request to create DAC(s) from AO with ID: {}", request.getAppelOffreId());
      Dac createdDac = dacService.createFromAppelOffre(request);
      DacResponce response = DacMapper.INSTANCE.map(createdDac);

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

  @GetMapping("/drop-list")
  public ResponseEntity<List<DacDropListResponse>> findAllForWebsite(DacQuery query) {
    Specification specification = query.toSpecification();
    List<Dac> dacs = dacService.findAllDacs(specification);
    List<DacDropListResponse> responses = dacs.stream().map(dac->{
      DacDropListResponse response = new DacDropListResponse();
      response.setId(dac.getDacId());
      response.setRef(dac.getReference());
      return response;})
            .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }
}
