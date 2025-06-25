package com.sigmap.sigmap_ppm.pvController;

import com.sigmap.common.process_verbal.pv.PvJasperService;
import com.sigmap.common.process_verbal.pv.dto.PvRequestDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/pv/ppms")
@Slf4j

public class PvController {


    @Value("classpath:pv")
    private Resource jrxmlPath;

    @Value("${jasper.jasperPath}")
    private String jasperPath;

    private final PvJasperService pvJasperService;

    public PvController(PvJasperService pvJasperService) {
        this.pvJasperService = pvJasperService;
    }



    @GetMapping("/proce-verbale")
    public void generateReportComparatifPaie(
            @RequestParam(name = "ppmId", required = false) Integer ppmId,
            @RequestParam(name = "ppmVersion", required = false) Integer ppmVersion,
            @RequestParam(name = "processVerbalId", required = false) String processVerbalId,
                                              HttpServletResponse response) throws IOException, JRException {

        log.debug("---------------- {}", jrxmlPath.getFile().getAbsolutePath());
        log.debug("request generateReportCtrl with {} {} {}  ",ppmId,ppmVersion,processVerbalId);

        byte [] reportBytes = pvJasperService.generateProcesVerbalRep(ppmId,ppmVersion,processVerbalId, jrxmlPath.getFile().getAbsolutePath()+"/", jasperPath);
        // Set the response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=report.pdf");
        // Write the report bytes to the response output stream
        response.getOutputStream().write(reportBytes);
        response.getOutputStream().flush();
    }


}
