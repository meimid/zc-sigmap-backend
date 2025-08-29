package com.sigmap.sigmap_params.pv_reunion;

import com.sigmap.common.process_verbal_reunion.PvJasperReunionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Paths;
import java.io.File;
import java.net.URL;


@RestController
@RequestMapping("/api/reunion")
@Slf4j
public class PvController {


    private final PvJasperReunionService pvJasperReunionService;

    public PvController(PvJasperReunionService pvJasperReunionService) {
        this.pvJasperReunionService = pvJasperReunionService;
    }

    @GetMapping("/proces-verbal_reunion/{reunionId}")
    public ResponseEntity<byte[]> generateReportPv(@PathVariable  Integer reunionId )  throws Exception {
        URL pvFolderUrl = getClass().getClassLoader().getResource("pv/");
        String reportsPath = pvFolderUrl.getPath();
        /*String projectRoot = Paths.get("").toAbsolutePath().toString();
        String reportsPath = projectRoot  + File.separator + "sigmap-params"
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "resources"
                + File.separator + "pv"
                + File.separator;*/
        log.info("reportsPath {} ", reportsPath);

        byte[] pdfBytes = pvJasperReunionService.generateProcesVerbalReunion(reunionId, reportsPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "pv_reunion_" + reunionId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

}
