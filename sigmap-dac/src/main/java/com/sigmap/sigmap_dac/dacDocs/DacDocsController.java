package com.sigmap.sigmap_dac.dacDocs;

import com.sigmap.common.dac.request.DacRequest;
import com.sigmap.common.dac_docs.DacDocsService;
import com.sigmap.common.dac_docs.mappers.DacDocsMapper;
import com.sigmap.common.dac_docs.request.DacDoccsAndDacRequest;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.DacDocsResponse;
import com.sigmap.common.dac_docs.response.DacDocsResponseFullEntities;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;
import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/dac-docs")
@Tag(name = "DAC Docs", description = "Operations for DAC Docs")
public class DacDocsController {

    private final DacDocsService dacDocsService;

    public DacDocsController(DacDocsService dacDocsService) {
        this.dacDocsService = dacDocsService;
    }


    @PostMapping
    public ResponseEntity<DacDocsResponseFullEntities> createDacDocs(@RequestBody DacDocsRequest request) {
        DacDocs dacDocs = dacDocsService.createDacDocs(request);
        DacDocsResponseFullEntities response = DacDocsMapper.INSTANCE.entityToResponseMap(dacDocs);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-versionning")
    public ResponseEntity<List<DacDocsResponseFullEntities>> createDacDocsBatchWithVersionning(@RequestBody List<DacDocsRequest> requests) {
        List<DacDocs> dacDocsList = dacDocsService.createDacDocsBatch(requests);
        List<DacDocsResponseFullEntities> response = DacDocsMapper.INSTANCE.entityToResponse(dacDocsList);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<DacDocsResponseFullEntities>> createDacDocsBatchWithoutVersionning(@RequestBody DacDoccsAndDacRequest dacDocsAndDacRequest)throws MonnaieNotFound {
        List<DacDocs> dacDocsList = dacDocsService.createDacDocsList(dacDocsAndDacRequest);
        List<DacDocsResponseFullEntities> response = DacDocsMapper.INSTANCE.entityToResponse(dacDocsList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{dacId}/{dacVer}/{dynaDocsId}/{dynaDocsVer}/{dynaFieldsId}")
    public ResponseEntity<DacDocsResponse> getDacDocs(
            @PathVariable Integer dacId,
            @PathVariable Integer dacVer,
            @PathVariable Integer dynaDocsId,
            @PathVariable Integer dynaDocsVer,
            @PathVariable Integer dynaFieldsId) {
        Optional<DacDocs> dacDocs = dacDocsService.findById(dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId);
        if (dacDocs.isPresent()) {
            DacDocsResponse response = DacDocsMapper.INSTANCE.toResponse(dacDocs.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{dacId}/{dacVer}")
    public ResponseEntity<List<DacDocResponseDTO>> getDacDocsByDacIdAndVer(
            @PathVariable Integer dacId,
            @PathVariable Integer dacVer) {
        List<DacDocResponseDTO> response = dacDocsService.findByDacIdAndVer(dacId, dacVer);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<DacDocsResponse> updateDacDocs(@RequestBody DacDocsRequest request) {
        DacDocs dacDocs = dacDocsService.updateDacDocs(request);
        DacDocsResponse response = DacDocsMapper.INSTANCE.toResponse(dacDocs);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/upload-static", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DacDocsResponseFullEntities> uploadStaticDoc(
            @RequestParam Integer dacId,
            @RequestParam Integer dynaDocsId,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        DacDocs dacDocs = dacDocsService.uploadStaticDoc(dacId, dynaDocsId, file);
        DacDocsResponseFullEntities response = DacDocsMapper.INSTANCE.entityToResponseMap(dacDocs);
        return ResponseEntity.ok(response);
    }

} 