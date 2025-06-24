package com.sigmap.sigmap_ppm.pvController;

import com.sigmap.common.process_verbal.pv.PvJasperService;
import com.sigmap.common.process_verbal.pv.dto.PvRequestDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/pv/ppms")
@Slf4j

public class PvController {

    @Value("${jasper.jrxmlPath}")
    private String jrxmlPath;

    @Value("${jasper.jasperPath}")
    private String jasperPath;

    private final PvJasperService pvJasperService;

    public PvController(PvJasperService pvJasperService) {
        this.pvJasperService = pvJasperService;
    }



    @GetMapping("/proce-verbale")
    public void generateReportComparatifPaie( @RequestBody PvRequestDTO request,
                                              HttpServletResponse response) throws IOException, JRException {

        log.debug("request generateReportCtrl with {} {} {}  ",request.getPpmId(),request.getPpmVersion(),request.getProcessVerbalId());

        byte [] reportBytes = pvJasperService.generateProcesVerbalRep(request.getPpmId(),request.getPpmVersion(),request.getProcessVerbalId(), jrxmlPath, jasperPath);
        // Set the response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=report.pdf");
        // Write the report bytes to the response output stream
        response.getOutputStream().write(reportBytes);
        response.getOutputStream().flush();
    }


}
