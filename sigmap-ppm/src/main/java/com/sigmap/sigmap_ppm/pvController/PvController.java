package com.sigmap.sigmap_ppm.pvController;

import com.sigmap.common.process_verbal.pv.PvJasperService;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pv/ppms")
@Slf4j
public class PvController {

  @Value("${jasper.jasperPath}")
  private String jasperPath;

  private final PvJasperService pvJasperService;

  public PvController(PvJasperService pvJasperService) {
    this.pvJasperService = pvJasperService;
  }

  @GetMapping("/proce-verbale")
  public ResponseEntity<byte[]> generateReportComparatifPaie(
      @RequestParam(name = "ppmId", required = false) Integer ppmId,
      @RequestParam(name = "ppmVersion", required = false) Integer ppmVersion,
      @RequestParam(name = "processVerbalId", required = false) String processVerbalId,
      @RequestParam(name = "taskId", required = false) String taskId,
      @RequestParam(name = "processInstanceId", required = false) String processInstanceId
      )
      throws IOException, JRException {

    // log.debug("---------------- {}", jrxmlPath.getFile().getPath());
    log.debug("request generateReportCtrl with {} {} {}  ", ppmId, ppmVersion, processVerbalId);


    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources("classpath:pv/*.jrxml");

    Map<String, InputStream> jasperReports = new HashMap<>();

    for (Resource resource : resources) {
      jasperReports.put(resource.getFilename(), resource.getInputStream());
    }

    log.info("jasperReports {}", jasperReports);

    byte[] reportBytes =
        pvJasperService.generateProcesVerbalRep(
            ppmId,
            ppmVersion,
            processVerbalId,
            taskId,
            processInstanceId,
            // jrxmlPath.getFile().getPath() + "/",
            jasperReports,
            jasperPath);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "report.pdf");

    return ResponseEntity.ok().headers(headers).body(reportBytes);

    // Set the response headers
    // response.setContentType("application/pdf");
    // response.setHeader("Content-Disposition", "inline; filename=report.pdf");
    // // Write the report bytes to the response output stream
    // response.getOutputStream().write(reportBytes);
    // response.getOutputStream().flush();
  }
}
