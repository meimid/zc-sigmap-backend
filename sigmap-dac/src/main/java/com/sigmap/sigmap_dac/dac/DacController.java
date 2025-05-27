package com.sigmap.sigmap_dac.dac;

import com.sigmap.common.dac.DacServiceImpl;
import com.sigmap.common.dac.exeption.RequiredDacIDAndVersion;
import com.sigmap.common.dac.exeption.RequiredPath;
import com.sigmap.common.dac.request.DacRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/api/dac")
public class DacController {

    private final DacServiceImpl dacService;

    @Value("${templateDac.dac.inputDocPath}")
    private String basePath;

    @Value("${templateDac.dac.outputDocPath}")
    private String customDirPath;

    public DacController(DacServiceImpl dacService) {
        this.dacService = dacService;
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

}
