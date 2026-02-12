package com.sigmap.sigmap_dac.modified_dyna_docs_for_dac;

import com.sigmap.common.modified_dyna_docs_for_dac.ModifiedDynaDocsServiceImpl;
import com.sigmap.common.modified_dyna_docs_for_dac.request.ModifiedDynaDocsRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/modified-dyna-docs-for-dac")
@Slf4j
@Tag(name = "Modified Dyna Docs For DAC", description = "upload modified dyna docs for DAC document by User")

public class ModifiedDynaDocsController {
    private final ModifiedDynaDocsServiceImpl modifiedDynaDocsService;

    public ModifiedDynaDocsController(ModifiedDynaDocsServiceImpl modifiedDynaDocsService) {
        this.modifiedDynaDocsService = modifiedDynaDocsService;
    }

    @PostMapping( value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity<Void> uploadModifiedFiles(
            @RequestParam("dacId") Integer dacId,
            @RequestParam("dacVer") Integer dacVer,
            @RequestPart("aaoFile") MultipartFile aaoFile,
            @RequestPart("files") List<MultipartFile> files
    ) {
        ModifiedDynaDocsRequest request = new ModifiedDynaDocsRequest();
        request.setDacId(dacId);
        request.setDacVer(dacVer);

        modifiedDynaDocsService.uploadModifiedFiles(request, aaoFile, files);

        return ResponseEntity.ok().build();
    }
}
