package com.sigmap.sigmap_paa.dac;

import com.sigmap.common.dac.DacServiceImpl;
import com.sigmap.common.dac.request.PdfDacRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/dac")
@AllArgsConstructor
public class DacController {

    @Autowired
    DacServiceImpl dacService;


    @PostMapping("/pdf")
    public ResponseEntity<InputStreamResource> generatePdfPost(@RequestBody PdfDacRequest request, HttpServletResponse response) {
        try {
            // Step 1: Generate PDF and get the output file
            File pdfFile = dacService.replaceBookmarksAndGeneratePDF(request.getDocPath(), request.getDacId(), request.getDacVer());

            // Step 2: Read generated PDF file
            if (!pdfFile.exists()) {
                throw new RuntimeException("PDF file not found: " + pdfFile.getAbsolutePath());
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));

            /*return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pdfFile.getName())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);*/

            // Step 3: Write to HTTP response
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=dac_" + request.getDacId() + "_" + request.getDacVer() + ".pdf");
            try (FileInputStream fis = new FileInputStream(pdfFile);
                 OutputStream os = response.getOutputStream()) {
                fis.transferTo(os);
                os.flush();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return null;
    }

}
