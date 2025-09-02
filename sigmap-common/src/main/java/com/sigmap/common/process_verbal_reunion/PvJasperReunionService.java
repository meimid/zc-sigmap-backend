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
import java.net.URL;
import java.util.Base64;
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
    public byte[] generateProcesVerbalReunion(Integer reunionId) throws Exception {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("Reunion introuvable : " + reunionId));

        String idPresedent = reunion.getCom().getResponsableId();
        String fullNamePres = keycloakAdminService.getUserById(idPresedent).getFirstName()
                + " " + keycloakAdminService.getUserById(idPresedent).getLastName();

        try (InputStream mainReportStream = getClass()
                .getClassLoader()
                .getResourceAsStream("pv/pv_reunion.jasper")) {

            if (mainReportStream == null) {
                throw new FileNotFoundException("Fichier pv_reunion.jasper introuvable !");
            }

            // logo
            InputStream logoStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/logo.png");

            if (logoStream == null) {
                throw new FileNotFoundException("Logo introuvable !");
            }

            InputStream sub1Stream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/pv_reunion_presents.jasper");

            if (sub1Stream == null) {
                throw new FileNotFoundException("pv_reunion_presents.jasper introuvable !");
            }

            InputStream sub2Stream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/pv_reunion_absents.jasper");

            if (sub2Stream == null) {
                throw new FileNotFoundException("pv_reunion_absents introuvable !");
            }

            InputStream sub3Stream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/pv_reunion_sub1.jasper");

            if (sub3Stream == null) {
                throw new FileNotFoundException("pv_reunion_sub1 introuvable !");
            }

            InputStream sub4Stream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/pv_reunion_sub2.jasper");

            if (sub4Stream == null) {
                throw new FileNotFoundException("pv_reunion_sub2.jasper introuvable !");
            }

            Map<String, Object> parametres = new HashMap<>();
            parametres.put("pathImg", logoStream);  // si .jasper attend InputStream
            parametres.put("reunion", reunionId);
            parametres.put("president", fullNamePres);
            parametres.put("SUBREPORT_DIR1", sub1Stream);
            parametres.put("SUBREPORT_DIR2", sub2Stream);
            parametres.put("SUBREPORT_DIR3", sub3Stream);
            parametres.put("SUBREPORT_DIR4", sub4Stream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    mainReportStream, parametres, dataSource.getConnection());

            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }

    public byte[] generateProcesVerbalReunionDraft(Integer reunionId) throws Exception {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("Reunion introuvable : " + reunionId));

        String idPresedent = reunion.getCom().getResponsableId();
        String fullNamePres = keycloakAdminService.getUserById(idPresedent).getFirstName()
                + " " + keycloakAdminService.getUserById(idPresedent).getLastName();

        try (InputStream mainReportStream = getClass()
                .getClassLoader()
                .getResourceAsStream("pv/pv_reunion_draft.jasper")) {

            if (mainReportStream == null) {
                throw new FileNotFoundException("Fichier pv_reunion_draft.jasper introuvable !");
            }

            // logo
            InputStream logoStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/logo.png");

            if (logoStream == null) {
                throw new FileNotFoundException("Logo introuvable !");
            }

            InputStream logoStream2 = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/draft.png");

            if (logoStream2 == null) {
                throw new FileNotFoundException("Logo draft introuvable !");
            }

            InputStream sub1Stream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/pv_reunion_presents.jasper");

            if (sub1Stream == null) {
                throw new FileNotFoundException("pv_reunion_presents.jasper introuvable !");
            }

            InputStream sub2Stream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/pv_reunion_absents.jasper");

            if (sub2Stream == null) {
                throw new FileNotFoundException("pv_reunion_absents introuvable !");
            }

            InputStream sub3Stream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/pv_reunion_sub1.jasper");

            if (sub3Stream == null) {
                throw new FileNotFoundException("pv_reunion_sub1 introuvable !");
            }

            InputStream sub4Stream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("pv/pv_reunion_sub2.jasper");

            if (sub4Stream == null) {
                throw new FileNotFoundException("pv_reunion_sub2.jasper introuvable !");
            }

            Map<String, Object> parametres = new HashMap<>();
            parametres.put("pathImg", logoStream);  // si .jasper attend InputStream
            parametres.put("reunion", reunionId);
            parametres.put("president", fullNamePres);
            parametres.put("SUBREPORT_DIR1", sub1Stream);
            parametres.put("SUBREPORT_DIR2", sub2Stream);
            parametres.put("SUBREPORT_DIR3", sub3Stream);
            parametres.put("SUBREPORT_DIR4", sub4Stream);
            parametres.put("pathImgDraft", logoStream2);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    mainReportStream, parametres, dataSource.getConnection());

            return JasperExportManager.exportReportToPdf(jasperPrint);
        }

    }

}
