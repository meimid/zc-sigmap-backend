package com.sigmap.common.process_verbal_reunion;

import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

@Slf4j
@Service
@AllArgsConstructor
public class PvJasperReunionService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReunionRepository reunionRepository;
    @Autowired
    private KeycloakAdminService keycloakAdminService;
    public byte[] generateProcesVerbalReunion(Integer reunionId, String reportsPath) throws Exception {
        Reunion reunion = reunionRepository.findById(reunionId).orElse(null);
        String idPresedent = reunion.getCom().getResponsableId();
        String fullNamePres = keycloakAdminService.getUserById(idPresedent).getFirstName() +" "+ keycloakAdminService.getUserById(idPresedent).getLastName();

            File reportFile = new File(reportsPath, "pv_reunion.jasper");
            InputStream mainReportStream = new FileInputStream(reportFile);
            if (mainReportStream == null) {
                throw new FileNotFoundException("Fichier pv_reunion.jasper introuvable !");
            }


            Map<String, Object> parametres = new HashMap<>();
            parametres.put("pathImg", reportsPath +"/logo.png");
            parametres.put("reunion", reunionId);
            parametres.put("SUBREPORT_DIR", reportsPath);
            parametres.put("president", fullNamePres);


        JasperPrint jasperPrint = JasperFillManager.fillReport(
                mainReportStream, parametres, dataSource.getConnection());

        // Export PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);

    }

}
