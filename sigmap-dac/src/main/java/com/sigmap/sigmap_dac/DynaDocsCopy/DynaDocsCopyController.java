package com.sigmap.sigmap_dac.DynaDocsCopy;

import com.sigmap.common.dyna_docs_copy.DynaDocsCopy;
import com.sigmap.common.dyna_docs_copy.DynaDocsCopyService;
import com.sigmap.common.dyna_docs_copy.request.DynaDocsCopyRequestUpload;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/dyna-docs-copy")
@Slf4j
@Tag(name = "cloned Dyna Docs For DAC", description = "upload modified dyna docs for DAC document by User")
@AllArgsConstructor
public class DynaDocsCopyController {

    private final DynaDocsCopyService dynaDocsCopyService;

    @GetMapping("/active-last-version")
    public ResponseEntity<List<DynaDocsCopy>> getActiveLastVersion( @RequestParam Integer dacId, @RequestParam Integer dacVer ) {
        return ResponseEntity.ok(dynaDocsCopyService.findActiveLastVersion(dacId, dacVer)
        );
    }

    @PostMapping(value = "/upload-modified/{id}")
    public ResponseEntity<String> uploadModifiedFile(@PathVariable Integer id, @RequestParam MultipartFile file) {
        dynaDocsCopyService.uploadModifiedFiles(id, file);
        return ResponseEntity.ok("File updated successfully");
    }
}
