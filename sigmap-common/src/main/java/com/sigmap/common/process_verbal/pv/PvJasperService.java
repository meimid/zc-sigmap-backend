package com.sigmap.common.process_verbal.pv;

import com.sigmap.common.process_verbal.ProcessVerbalRepository;
import com.sigmap.common.process_verbal.pv.dto.MembreCommision;
import com.sigmap.common.process_verbal.pv.dto.MembreCommisionDTO;
import com.sigmap.common.pv_membre_present.PvMembrePresentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class PvJasperService {


    private final ProcessVerbalRepository processVerbalRepository;
    private final PvMembrePresentRepository pvMembrePresentRepository;
    private final PvService pvService;

    public byte[] generateProcesVerbalRep(Integer ppmId, Integer ppmVersion, String processVerbalId,String taskId,String processInstanceId,Map<String, InputStream> jrxmlFiles, String jasperPath) throws JRException {

        List<MembreCommisionDTO> membreCommisionsDTOList = pvMembrePresentRepository.getAllMembreCommision(ppmId, ppmVersion, processVerbalId);
        log.debug("total records {} " , membreCommisionsDTOList.size());
        if (!membreCommisionsDTOList.isEmpty()) {

            // Fetch ProcessVerbal entity to get observation
            var processVerbal = processVerbalRepository.findTopByPpmIdAndPpmVersionAndTaskIdAndProcessInstanceIdAndIsDeletedFalse(ppmId, ppmVersion,taskId,processInstanceId).get();
/*
            String observation = processVerbal != null ? processVerbal.getObservation() : null;
*/

            // Fetch ProcessVerbal entity to get dateDebutReunion and heureDebutReunion
            LocalDateTime dateDebutReunion = processVerbal != null ? processVerbal.getDateDebutReunion() : null;
            String dateDebutReunionStr = dateDebutReunion != null ? dateDebutReunion.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null;
            String heureDebutReunionStr = dateDebutReunion != null ? dateDebutReunion.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null;

            String processVerbalIdStr = processVerbal != null ? processVerbal.getProcessVerbalId() : null;

            Calendar dateCloture = processVerbal != null ? processVerbal.getUpdatedAt() : null;
            String dateCloturestrg = "La séance est levée à ";
            if (dateCloture != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                dateCloturestrg += sdf.format(dateCloture.getTime());
            } else {
                dateCloturestrg += "........";
            }

            //fetch décision
/*
            boolean decision = processVerbal != null && processVerbal.isDecision();
*/
/*
            String decisionStr = decision ? "Approuvé" : "Rejeté";
*/

            // Fetch ProcessVerbal entity to get acNom
            String acNom = pvService.getAcName(ppmId, ppmVersion);
            // set the report name
            String templatePath = "proces_verbal_ppm";
            InputStream templateStream = jrxmlFiles.get(templatePath + ".jrxml");

            String processVerbalNum = "PROCÈS-VERBAL N° " + processVerbalIdStr;
            String date = "Date : " + dateDebutReunionStr + " Heure : " + heureDebutReunionStr;
            String nomAutorite = acNom + " pour l'exercice budgétaire 2025. ";

            // set the report parameters
            Map<String, Object> parametres = new HashMap<>();
            parametres.put("processVerbalNum", processVerbalNum);
/*
            parametres.put("observation", observation);
*/
            parametres.put("date", date);
            parametres.put("acNom", nomAutorite);
/*
            parametres.put("decision", decision);
*/
            parametres.put("dateCloture", dateCloturestrg);
            /*parametres.put("decisionStr", decisionStr);*/

            List<String> docNames = pvService.getPvDocsNames(ppmId, ppmVersion, processVerbalId);
            JRBeanCollectionDataSource docNamesDataSource = new JRBeanCollectionDataSource(docNames);
            parametres.put("docNames", docNamesDataSource);

            String formattedDocNames = String.join("\n",
                    docNames.stream().map(name -> "* " + name).toList());
            parametres.put("formattedDocNames", formattedDocNames);

            //sub report 1



            // compileAndSaveJasperSubReport(jrxmlPath+"sub_proces_verbal_ppm.jrxml",jasperPath+"sub_proces_verbal_ppm.jasper");
            compileAndSaveJasperSubReport(jrxmlFiles.get("sub_proces_verbal_ppm.jrxml"),jasperPath+"sub_proces_verbal_ppm.jasper");

            parametres.put("SUBREPORT_1_PATH", jasperPath+"sub_proces_verbal_ppm.jasper");

            //sub report 2

            // compileAndSaveJasperSubReport(jrxmlPath+"sub_proces_verbal_ppm_3.jrxml",jasperPath+"sub_proces_verbal_ppm_3.jasper");
             compileAndSaveJasperSubReport(jrxmlFiles.get( "sub_proces_verbal_ppm_3.jrxml"),jasperPath+"sub_proces_verbal_ppm_3.jasper");
             parametres.put("SUBREPORT_3_PATH", jasperPath+"sub_proces_verbal_ppm_3.jasper");

            List<MembreCommision> membre = pvService.membreCommision(ppmId, ppmVersion, processVerbalId);
            JRBeanCollectionDataSource source = new JRBeanCollectionDataSource(membre);
            parametres.put("membreList", source);


            JRBeanCollectionDataSource source2 = new JRBeanCollectionDataSource(membre);
            parametres.put("membreList2", source2);

            parametres.put("membreList3", source);

            return generateReport(templateStream, parametres, source);

        } else {
            throw new IllegalArgumentException("No records found");
        }
    }

    private void compileAndSaveJasperSubReport(InputStream jrxmlFile, String jasperPath) {
        // try (FileInputStream fis = new FileInputStream(new File(jrxmlPath))) {
        try {
            JasperReport report = JasperCompileManager.compileReport(jrxmlFile);
            JRSaver.saveObject(report, jasperPath);
            // log.debug("Compiled and saved JasperReport: {} -> {}", jrxmlPath, jasperPath);
        } catch (Exception e) {
            log.error("Failed to compile or save JasperReport:  -> {}",  jasperPath, e);
            throw new RuntimeException("Failed to compile or save JasperReport: " + jasperPath, e);
        }
    }


    public byte[] generateReport(InputStream template, Map<String, Object> parameters, JRBeanCollectionDataSource dataSource) throws JRException {
        log.debug("Generating report ......");
        // Check templatePath
        // InputStream jasperStream = this.getClass().getResourceAsStream("/pv/" + templatePath + ".jrxml");
        // log.debug("Finding templatePath ......");

        try  {
            JasperReport jasperReport = compileReport(template);
            log.debug("Compile report jasperStream ......");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            final Exporter exporter = new JRPdfExporter();
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.exportReport();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error loading JRXML file: {}", e.getMessage());
            throw new JRException("Error loading JRXML file", e);
        }
    }

    // Helper method to compile the JasperReport
    private JasperReport compileReport(InputStream jasperStream) throws JRException {
        if (jasperStream == null) {
            throw new JRException("JasperStream is null");
        }
        return JasperCompileManager.compileReport(jasperStream);
    }
}
