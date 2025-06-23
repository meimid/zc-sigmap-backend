package com.sigmap.sigmap_dac.dacDocs;

import com.sigmap.common.dac_docs.DacDocsService;
import com.sigmap.common.dac_docs.mappers.DacDocsMapper;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.DacDocsResponse;
import com.sigmap.common.dac_docs.DacDocs;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/dac-docs")
@Tag(name = "DAC Docs", description = "Operations for DAC Docs")
public class DacDocsController {

    private final DacDocsService dacDocsService;

    public DacDocsController(DacDocsService dacDocsService) {
        this.dacDocsService = dacDocsService;
    }

    @GetMapping("/dac/{dacId}/{dacVer}")
    public ResponseEntity<List<DacDocsResponse>> getDacDocsByDac(
            @PathVariable Integer dacId,
            @PathVariable Integer dacVer) {
        List<DacDocs> dacDocsList = dacDocsService.findByDacIdAndDacVer(dacId, dacVer);
        List<DacDocsResponse> responses = dacDocsList.stream()
                .map(DacDocsMapper.INSTANCE::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<DacDocsResponse> createDacDocs(@RequestBody DacDocsRequest request) {
        DacDocs dacDocs = dacDocsService.createDacDocs(request);
        DacDocsResponse response = DacDocsMapper.INSTANCE.toResponse(dacDocs);
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

    @PutMapping
    public ResponseEntity<DacDocsResponse> updateDacDocs(@RequestBody DacDocsRequest request) {
        DacDocs dacDocs = dacDocsService.updateDacDocs(request);
        DacDocsResponse response = DacDocsMapper.INSTANCE.toResponse(dacDocs);
        return ResponseEntity.ok(response);
    }


} 