package com.sigmap.sigmap_params.doc;

import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.docs.DownloadDocsService;
import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.docs.Request.AlfrescoIdRequest;
import com.sigmap.common.docs.Response.DocsResponse;

import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.reunion.ReunionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/api/docs")
public class DocController {
    private final DocsService docsService;
    private final DownloadDocsService downloadDocsService;

    public DocController(DocsService docsService, DownloadDocsService downloadDocsService) {
        this.docsService = docsService;

        this.downloadDocsService = downloadDocsService;
    }

  /*  @PostMapping("/alfresco/data")
    public DocumentResponseDTO getDocumentData(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException {
        return docsService.getDocumentInformation(alfrescoId.getId()).orElseThrow(() -> new DocsNotFoundException(alfrescoId.getId()));
    }*/
    @GetMapping("/{id}")
    public ResponseEntity<DocsResponse> findById(@PathVariable Integer id)  {
        Docs doc = docsService.findById(id);
        DocsResponse dto = DocMapper.INSTANCE.map(doc);
        log.info("Found doc: {}", dto);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/alfresco")
    public ResponseEntity<DocsResponse> findByAlfrescoId(@RequestBody AlfrescoIdRequest alfrescoIdRequest) {
        Docs doc = docsService.findByAlfescoId(alfrescoIdRequest.getId())
                .orElseThrow(() -> new DocsNotFoundException("Document not found with alfresco_id: " + alfrescoIdRequest.getId()));
        DocsResponse dto = DocMapper.INSTANCE.map(doc);
        log.info("Found doc: {}", dto);
        return ResponseEntity.ok(dto);
    }
    /**
     * Delete a ac based on the given id.
     *
     * @param id the id of the docs to delete
     * @return the deleted ac
     */
    @DeleteMapping("/alfresco/{id}")
    public ResponseEntity<DocsResponse> deleteById(@PathVariable Integer id) {
        docsService.delete(id);
        Docs deleted = docsService.findDeletedById(id)
                .orElseThrow(() -> new DocsNotFoundException("Document not found with id: " + id));
        DocsResponse response = DocMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/model-agpm")
    public ResponseEntity<DocumentResponseDTO> downloadModelAgpm(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, AgpmNotFound {
        byte[]  fileContent = downloadDocsService.downloadModelAgpm(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/agpm")
    public ResponseEntity<DocumentResponseDTO> downloadAgpmDoc(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, AgpmNotFound, AcNotFound {

        byte[]  fileContent = downloadDocsService.downloadAgpmDoc(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/paa")
    public ResponseEntity<DocumentResponseDTO> downloadPaa(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PaaNotFound, AcNotFound {

        byte[]  fileContent = downloadDocsService.downloadPaaDoc(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/ppm")
    public ResponseEntity<DocumentResponseDTO> downloadPpm(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PpmNotFound, AcNotFound {

        byte[]  fileContent = downloadDocsService.downloadPpmDoc(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/model-paa")
    public ResponseEntity<DocumentResponseDTO> downloadModelPaa(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PaaNotFound {

        byte[]  fileContent = downloadDocsService.downloadModelPaa(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/model-ppm")
    public ResponseEntity<DocumentResponseDTO> downloadModelPpm(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PpmNotFound {
        byte[]  fileContent = downloadDocsService.downloadModelPpm(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);

    }

    @PostMapping("/download/model-dac")
    public ResponseEntity<DocumentResponseDTO> downloadModelDac(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PpmNotFound {

        byte[]  fileContent = downloadDocsService.downloadDacModel(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/pv")
    public ResponseEntity<DocumentResponseDTO> downloadPv(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException {
        byte[]  fileContent = downloadDocsService.downloadPv(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/extrait")
    public ResponseEntity<DocumentResponseDTO> downloadExtrait(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException {

        byte[]  fileContent = downloadDocsService.downloadExtrait(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/meeting-ppm")
    public ResponseEntity<DocumentResponseDTO> downloadPpmArgumentDocumentForPpm(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PpmNotFound, AcNotFound {

        byte[]  fileContent = downloadDocsService.downloadPpmArgumentDocumentForPpm(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/meeting-ppm-for-meeting")
    public ResponseEntity<DocumentResponseDTO> downloadPpmArgumentDocumentMeeting(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PpmNotFound, AcNotFound {

        byte[]  fileContent = downloadDocsService.downloadPpmArgumentDocumentForReunion(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/meeting-dac")
    public ResponseEntity<DocumentResponseDTO> downloadDacArgumentDocument(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PpmNotFound, AcNotFound {

        byte[]  fileContent = downloadDocsService.downloadDacArgumentDocumentForDac(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/meeting-dac-for-meeting")
    public ResponseEntity<DocumentResponseDTO> downloadDacArgumentDocumentForMeeting(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PpmNotFound, AcNotFound {

        byte[]  fileContent = downloadDocsService.downloadDacArgumentDocumentForReunion(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/com")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN')")
    public ResponseEntity<DocumentResponseDTO> downloadComDocument(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PpmNotFound, AcNotFound {

        byte[]  fileContent = downloadDocsService.downloadComDocument(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/download/dac")
    public ResponseEntity<DocumentResponseDTO> downloadDacDocsDocument(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, PpmNotFound, AcNotFound {

        byte[]  fileContent = downloadDocsService.downloadDacDocsDocument(alfrescoId.getId());
        DocumentResponseDTO response = new DocumentResponseDTO(
                fileContent,
                false,
                null,
                alfrescoId.getId()
        );

        return ResponseEntity.ok(response);
    }

}
