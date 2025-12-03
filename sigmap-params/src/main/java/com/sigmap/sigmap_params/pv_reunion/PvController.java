package com.sigmap.sigmap_params.pv_reunion;

import com.sigmap.common.process_verbal_reunion.PvJasperReunionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.io.File;
import java.net.URL;


@RestController
@RequestMapping("/api/reunions")
@Slf4j
public class PvController {


    private final PvJasperReunionService pvJasperReunionService;

    public PvController(PvJasperReunionService pvJasperReunionService) {
        this.pvJasperReunionService = pvJasperReunionService;
    }

    @GetMapping("/pv/{reunionId}")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-RAPPORTEUR','CNCMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR')")

    public ResponseEntity<byte[]> generateReportPv(@PathVariable  Integer reunionId )  throws Exception {

        byte[] pdfBytes = pvJasperReunionService.generateProcesVerbalReunion(reunionId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "pv_reunion_" + reunionId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/pv/{reunionId}/download")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-RAPPORTEUR','CNCMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR')")

    public ResponseEntity<Resource> downloadProcessVerbal(@PathVariable Integer reunionId) throws Exception {


        byte[] pdfBytes = pvJasperReunionService.generateProcesVerbalReunion(reunionId);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pv_reunion_" + reunionId + ".pdf\"");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfBytes.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/pv-draft/{reunionId}")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-RAPPORTEUR','CNCMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR')")

    public ResponseEntity<byte[]> generateReportPvDraft(@PathVariable  Integer reunionId )  throws Exception {

        byte[] pdfBytes = pvJasperReunionService.generateProcesVerbalReunionDraft(reunionId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "pv_reunion_draft_" + reunionId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/pv-draft/{reunionId}/download")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-RAPPORTEUR','CNCMP-SPE-RAPPORTEUR','CPMP-RAPPORTEUR')")

    public ResponseEntity<Resource> downloadProcessVerbalDraft(@PathVariable Integer reunionId) throws Exception {


        byte[] pdfBytes = pvJasperReunionService.generateProcesVerbalReunionDraft(reunionId);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pv_reunion_draft" + reunionId + ".pdf\"");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfBytes.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/extrait-pv-cpmp/{reunionId}/{acId}/download")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CPMP-RAPPORTEUR')")

    public ResponseEntity<Resource> downloadExtraitCPMP(@PathVariable Integer reunionId ,@PathVariable Integer acId) throws Exception {


        byte[] pdfBytes = pvJasperReunionService.generateExtraitProcesVerbalCPMP(reunionId,acId);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Extrait_PV_CPMP_" + reunionId + ".pdf\"");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfBytes.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

}
