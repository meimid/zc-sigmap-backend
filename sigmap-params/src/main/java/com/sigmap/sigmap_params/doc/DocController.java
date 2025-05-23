package com.sigmap.sigmap_params.doc;

import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.docs.Request.AlfrescoIdRequest;
import com.sigmap.common.docs.Response.DocsResponse;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.docs.exeptions.BadRequestException;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/docs")
public class DocController {
    private final DocsService docsService;
    public DocController(DocsService docsService) {
        this.docsService = docsService;
    }
    @PostMapping("/alfresco-data")
    public DocumentResponseDTO getDocumentInformation(@RequestBody AlfrescoIdRequest alfrescoId) throws DocsNotFoundException {
        return docsService.getDocumentInformation(alfrescoId.getId()).orElseThrow(() -> new DocsNotFoundException(alfrescoId.getId()));
    }
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
    @GetMapping("/by-paa/{paaId}/{paaVersion}")
    public ResponseEntity<List<DocsResponseDTO>> findDocsByPaasIdAndPaasVersion(
            @PathVariable Integer paaId,
            @PathVariable Integer paaVersion) {
        log.info("Received paa_id: {}, paa_version: {}", paaId, paaVersion);
        // Validate input
        if (paaId == null || paaId <= 0) {
            throw new BadRequestException("paaId must be a positive integer");
        }
        if (paaVersion == null || paaVersion <= 0) {
            throw new BadRequestException("paaVersion must be a positive integer");
        }
        // Fetch documents
        List<Docs> docs = docsService.findDocsByPaasIdAndPaasVersion(paaId, paaVersion);
        // Handle empty list
        if (docs.isEmpty()) {
            throw new DocsNotFoundException(
                    "No documents found for paa_id: " + paaId + " and paa_version: " + paaVersion);
        }
        // Map to DTOs
        List<DocsResponseDTO> dtos = docs.stream()
                .map(DocMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        log.info("Found {} documents", dtos.size());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-realisation/{realisationId}/{realisationVersion}")
    public ResponseEntity<List<DocsResponseDTO>> findDocsByRealisationIdAndRealisationVersion(
            @PathVariable Integer realisationId,
            @PathVariable Integer realisationVersion) {
        log.info("Received realisation_id: {}, realisation_version: {}", realisationId, realisationVersion);
        // Validate input
        if (realisationId == null || realisationId <= 0) {
            throw new BadRequestException( "realisationId must be a positive integer");
        }
        if (realisationVersion == null || realisationVersion <= 0) {
            throw new BadRequestException( "realisationVersion must be a positive integer");
        }
        // Fetch documents
        List<Docs> docs = docsService.findDocsByRealisationIdAndRealisationVersion(realisationId,realisationVersion);
        // Handle empty list
        if (docs.isEmpty()) {
            throw new DocsNotFoundException(
                    "No documents found for realisation_id: " + realisationId + " and realisation_version: " + realisationVersion);
        }
        // Map to DTOs
        List<DocsResponseDTO> dtos = docs.stream()
                .map(DocMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        log.info("Found {} documents", dtos.size());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-ppm/{ppmId}/{ppmVersion}")
    public ResponseEntity<List<DocsResponseDTO>> findDocsByPpmIdAndPpmVersion(
            @PathVariable Integer ppmId,
            @PathVariable Integer ppmVersion) {
        log.info("Received ppm_id: {}, ppm_version: {}", ppmId, ppmVersion);
        // Validate input
        if (ppmId == null || ppmId <= 0) {
            throw new BadRequestException( "ppmId must be a positive integer");
        }
        if (ppmVersion == null || ppmVersion <= 0) {
            throw new BadRequestException("ppmVersion must be a positive integer");
        }
        // Fetch documents
        List<Docs> docs = docsService.findDocsByPpmIdAndPpmVersion(ppmId,ppmVersion);
        // Handle empty list
        if (docs.isEmpty()) {
            throw new DocsNotFoundException(
                    "No documents found for ppm_id: " + ppmId + " and ppm_version: " + ppmVersion);
        }
        // Map to DTOs
        List<DocsResponseDTO> dtos = docs.stream()
                .map(DocMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        log.info("Found {} documents", dtos.size());
        return ResponseEntity.ok(dtos);
    }
    /**
     * Delete a ac based on the given id.
     *
     * @param id the id of the docs to delete
     * @return the deleted ac
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DocsResponse> deleteById(@PathVariable Integer id) {
        docsService.delete(id);
        Docs deleted = docsService.findDeletedById(id)
                .orElseThrow(() -> new DocsNotFoundException("Document not found with id: " + id));
        DocsResponse response = DocMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
