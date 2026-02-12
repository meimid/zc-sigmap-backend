package com.sigmap.sigmap_params.doc;

import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.docs.Request.AlfrescoIdRequest;
import com.sigmap.common.docs.Response.DocsResponse;

import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
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
    private final AgpmService agpmService;
    private final PaaService paaService;
    public DocController(DocsService docsService, AgpmService agpmService, PaaService paaService) {
        this.docsService = docsService;
        this.agpmService = agpmService;
        this.paaService = paaService;
    }

   /* @PostMapping("/alfresco/data")
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

    @PostMapping("/download/model/agpm")
    public ResponseEntity<Resource> downloadAgpm(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, AgpmNotFound {
        Docs docInfo = docsService.findByAlfescoId(alfrescoId.getId())
                .orElseThrow(() -> new DocsNotFoundException(alfrescoId.getId()));
        byte[]  fileContent = agpmService.downloadModelAgpm(alfrescoId.getId());
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docInfo.getName() + "\"");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers)
                .contentLength(fileContent.length)
                .contentType(MediaType.parseMediaType(docInfo.getContentType()))
                .body(resource);
    }

    @PostMapping("/download/model/paa")
    public ResponseEntity<Resource> downloadPaa(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException, IOException, AgpmNotFound, PaaNotFound {
        Docs docInfo = docsService.findByAlfescoId(alfrescoId.getId())
                .orElseThrow(() -> new DocsNotFoundException(alfrescoId.getId()));
        byte[]  fileContent = paaService.downloadModelPaa(alfrescoId.getId());
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docInfo.getName() + "\"");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers)
                .contentLength(fileContent.length)
                .contentType(MediaType.parseMediaType(docInfo.getContentType()))
                .body(resource);
    }



}
