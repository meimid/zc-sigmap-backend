package com.sigmap.sigmap_dac.dac;

import com.sigmap.common.dac.DacListServiceImpl;
import com.sigmap.common.dac.DacServiceImpl;
import com.sigmap.common.dac.TestDacListService;
import com.sigmap.common.dac.exeption.RequiredDacIDAndVersion;
import com.sigmap.common.dac.exeption.RequiredPath;
import com.sigmap.common.dac.exeption.TemplateFileNameNotFound;
import com.sigmap.common.dac.request.DacRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/dac")
public class DacController {

    private final DacServiceImpl dacService;
    private final TestDacListService dacListService;

    @Value("${templateDac.dac.inputDocPath}")
    private String basePath;

    @Value("${templateDac.dac.outputDocPath}")
    private String customDirPath;

    public DacController(DacServiceImpl dacService, TestDacListService dacListService) {
        this.dacService = dacService;
        this.dacListService = dacListService;
    }

    @PostMapping("/generate-doc")
    public void replaceBookmarksAndGenerateDOC(@RequestBody DacRequest request) {

        if (request.getDacId() == null || request.getDacVer() == null) {
            throw new RequiredDacIDAndVersion();
        }

        if ((basePath == null || basePath.isEmpty()) || (customDirPath == null || customDirPath.isEmpty())) {
            throw new RequiredPath();
        }

        // Step 1: Generate and get the output file
        File pdfFile = dacService.replaceBookmarksAndGenerateDOC(request.getDacId(), request.getDacVer(),this.basePath, customDirPath);

    }


    @PostMapping("/generate-list-doc")
    public ResponseEntity<List<String>> replaceBookmarksAndGenerateListDOC(@RequestBody DacRequest request) {
        if (request.getDacId() == null || request.getDacVer() == null) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonList("Dac ID and version must not be null"));
        }

        try {
            List<String> generatedFiles = dacListService.generateDocumentsWithBookmarks(
                    request.getDacId(), request.getDacVer(), basePath, customDirPath);
            return ResponseEntity.ok(generatedFiles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonList(e.getMessage()));
        } catch (TemplateFileNameNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonList("No active documents found for Dac ID: " +
                            request.getDacId() + ", Version: " + request.getDacVer()));
        } catch (Exception e) {
            //log.error("Error generating documents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList("Failed to generate documents: " + e.getMessage()));
        }
    }

}
