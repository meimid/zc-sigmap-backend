package com.sigmap.common.process_verbal_reunion;

import com.sigmap.common.entity.ExtraitType;
import com.sigmap.common.entity.ParticipantFonction;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionRepository;
import com.sigmap.common.reunion.ReunionService;
import com.sigmap.common.reunion.ReunionServiceImpl;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
    @Autowired
    ReunionService reunionService;
    public byte[] generateProcesVerbalReunion(Integer reunionId) throws Exception {
        // Validation initiale
        if (reunionId == null) {
            throw new IllegalArgumentException("L'ID de la réunion ne peut pas être null");
        }
        // Récupération de la réunion
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("Réunion introuvable : " + reunionId));

        // Récupération du président
        String idPresedent = reunion.getParticipants().stream().filter(p -> p.getFonctionReunion().equals(ParticipantFonction.PRESIDENT)).findFirst().get().getUserId();
        String fullNamePres = keycloakAdminService.getUserById(idPresedent).getFirstName()
                + " " + keycloakAdminService.getUserById(idPresedent).getLastName();
        Optional<String> optionalInterim=reunionService.findInterimParticipant(reunionId);

        String interim;

        interim = optionalInterim.orElse(null);

        // Map des paramètres
        Map<String, Object> parameters = new HashMap<>();

        // Gestion des ressources avec try-with-resources
        try (Connection conn = dataSource.getConnection();
             InputStream mainReportStream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion.jasper");
             InputStream logoStream = getClass().getClassLoader().getResourceAsStream("pv/logo.png");
             InputStream sub1Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_presents.jasper");
             InputStream sub2Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_absents.jasper");
             InputStream sub3Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_sub1.jasper");
             InputStream sub4Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_sub2.jasper"))
        {

            // Vérification des ressources
            validateResource(mainReportStream, "pv_reunion.jasper");
            validateResource(logoStream, "logo.png");
            validateResource(sub1Stream, "pv_reunion_presents.jasper");
            validateResource(sub2Stream, "pv_reunion_absents.jasper");
            validateResource(sub3Stream, "pv_reunion_sub1.jasper");
            validateResource(sub4Stream, "pv_reunion_sub2.jasper");

            // Configuration des paramètres
            parameters.put("reunion", reunionId);
            parameters.put("president", fullNamePres);
            parameters.put("pathImg", logoStream);
            parameters.put("interim", interim);
            parameters.put("SUBREPORT_DIR1", sub1Stream);
            parameters.put("SUBREPORT_DIR2", sub2Stream);
            parameters.put("SUBREPORT_DIR3", sub3Stream);
            parameters.put("SUBREPORT_DIR4", sub4Stream);
            parameters.put("REPORT_CONNECTION", conn);

            // Génération du rapport
            JasperPrint jasperPrint = JasperFillManager.fillReport(mainReportStream, parameters, conn);
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException e) {
            throw new RuntimeException("Erreur lors de la génération du rapport Jasper: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de la génération du PV: " + e.getMessage(), e);
        }
    }

    public byte[] generateProcesVerbalReunionDraft(Integer reunionId) throws Exception {
        // Validation initiale
        if (reunionId == null) {
            throw new IllegalArgumentException("L'ID de la réunion ne peut pas être null");
        }
        // Récupération de la réunion
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("Réunion introuvable : " + reunionId));

        // Récupération du président
        String idPresedent = reunion.getParticipants().stream().filter(p -> p.getFonctionReunion().equals(ParticipantFonction.PRESIDENT)).findFirst().get().getUserId();
        String fullNamePres = keycloakAdminService.getUserById(idPresedent).getFirstName()
                + " " + keycloakAdminService.getUserById(idPresedent).getLastName();

        String interim;
        Optional<String> optionalInterim=reunionService.findInterimParticipant(reunionId);

        interim = optionalInterim.orElse(null);

        // Map des paramètres
        Map<String, Object> parameters = new HashMap<>();

        // Gestion des ressources avec try-with-resources
        try (Connection conn = dataSource.getConnection();
             InputStream mainReportStream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_draft.jasper");
             InputStream logoStream = getClass().getClassLoader().getResourceAsStream("pv/logo.png");
             InputStream logoStream2 = getClass().getClassLoader().getResourceAsStream("pv/draft.png");
             InputStream sub1Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_presents.jasper");
             InputStream sub2Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_absents.jasper");
             InputStream sub3Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_sub1.jasper");
             InputStream sub4Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_sub2.jasper")) {

            // Vérification des ressources
            validateResource(mainReportStream, "pv_reunion_draft.jasper");
            validateResource(logoStream, "logo.png");
            validateResource(logoStream2, "draft.png");
            validateResource(sub1Stream, "pv_reunion_presents.jasper");
            validateResource(sub2Stream, "pv_reunion_absents.jasper");
            validateResource(sub3Stream, "pv_reunion_sub1.jasper");
            validateResource(sub4Stream, "pv_reunion_sub2.jasper");

            // Configuration des paramètres
            parameters.put("reunion", reunionId);
            parameters.put("president", fullNamePres);
            parameters.put("pathImg", logoStream);
            parameters.put("interim", interim);
            parameters.put("pathImgDraft", logoStream2);
            parameters.put("SUBREPORT_DIR1", sub1Stream);
            parameters.put("SUBREPORT_DIR2", sub2Stream);
            parameters.put("SUBREPORT_DIR3", sub3Stream);
            parameters.put("SUBREPORT_DIR4", sub4Stream);
            parameters.put("REPORT_CONNECTION", conn);


            // Génération du rapport
            JasperPrint jasperPrint = JasperFillManager.fillReport(mainReportStream, parameters, conn);
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException e) {
            throw new RuntimeException("Erreur lors de la génération du rapport Jasper: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de la génération du PV: " + e.getMessage(), e);
        }
    }

    public byte[] generateExtraitProcesVerbalCPMP(Integer reunionId, Integer acId) throws Exception {
        // Validation initiale
        if (reunionId == null) {
            throw new IllegalArgumentException("L'ID de la réunion ne peut pas être null");
        }
        // Récupération de la réunion
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("Réunion introuvable : " + reunionId));

        // Récupération du président
        String idPresedent = reunion.getParticipants().stream().filter(p -> p.getFonctionReunion().equals(ParticipantFonction.PRESIDENT)).findFirst().get().getUserId();
        String fullNamePres = keycloakAdminService.getUserById(idPresedent).getFirstName()
                + " " + keycloakAdminService.getUserById(idPresedent).getLastName();
        Optional<String> optionalInterim=reunionService.findInterimParticipant(reunionId);

        String interim;

        interim = optionalInterim.orElse(null);

        // Map des paramètres
        Map<String, Object> parameters = new HashMap<>();

        // Gestion des ressources avec try-with-resources
        try (Connection conn = dataSource.getConnection();
             InputStream mainReportStream = getClass().getClassLoader().getResourceAsStream("pv/extrait_pv_CPMP.jasper");
             InputStream logoStream = getClass().getClassLoader().getResourceAsStream("pv/logo.png");
             InputStream sub1Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_presents.jasper");
             InputStream sub2Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_absents.jasper");
             InputStream sub3Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_sub1AC.jasper")) {

            // Vérification des ressources
            validateResource(mainReportStream, "extrait_pv_CPMP.jasper");
            validateResource(logoStream, "logo.png");
            validateResource(sub1Stream, "pv_reunion_presents.jasper");
            validateResource(sub2Stream, "pv_reunion_absents.jasper");
            validateResource(sub3Stream, "pv_reunion_sub1AC.jasper");

            // Configuration des paramètres
            parameters.put("reunion", reunionId);
            parameters.put("president", fullNamePres);
            parameters.put("pathImg", logoStream);
            parameters.put("interim", interim);
            parameters.put("acId", acId);
            parameters.put("SUBREPORT_DIR1", sub1Stream);
            parameters.put("SUBREPORT_DIR2", sub2Stream);
            parameters.put("SUBREPORT_DIR3", sub3Stream);
            parameters.put("REPORT_CONNECTION", conn);


            // Génération du rapport
            JasperPrint jasperPrint = JasperFillManager.fillReport(mainReportStream, parameters, conn);
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException e) {
            throw new RuntimeException("Erreur lors de la génération du rapport Jasper: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de la génération du PV: " + e.getMessage(), e);
        }
    }

    public byte[] generateExtraitProcesVerbalCNCMP(Integer reunionId, Integer acId, Integer ppmId, Integer ppmVer, Integer dacId, Integer dacVer) throws Exception {
        // Validation initiale
        if (reunionId == null) {
            throw new IllegalArgumentException("L'ID de la réunion ne peut pas être null");
        }
        // Récupération de la réunion
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("Réunion introuvable : " + reunionId));

        // Récupération du président
        String idPresedent = reunion.getParticipants().stream().filter(p -> p.getFonctionReunion().equals(ParticipantFonction.PRESIDENT)).findFirst().get().getUserId();
        String fullNamePres = keycloakAdminService.getUserById(idPresedent).getFirstName()
                + " " + keycloakAdminService.getUserById(idPresedent).getLastName();
        Optional<String> optionalInterim=reunionService.findInterimParticipant(reunionId);

        String interim;

        interim = optionalInterim.orElse(null);

        // Map des paramètres
        Map<String, Object> parameters = new HashMap<>();

        // Gestion des ressources avec try-with-resources
        try (Connection conn = dataSource.getConnection();
             InputStream mainReportStream = getClass().getClassLoader().getResourceAsStream("pv/extrait_pv_CNCMP.jasper");
             InputStream logoStream = getClass().getClassLoader().getResourceAsStream("pv/logo.png");
             InputStream sub1Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_presents.jasper");
             InputStream sub2Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_absents.jasper");
             InputStream sub4Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_sub2AC.jasper")) {

            // Vérification des ressources
            validateResource(mainReportStream, "extrait_pv_CNCMP.jasper");
            validateResource(logoStream, "logo.png");
            validateResource(sub1Stream, "pv_reunion_presents.jasper");
            validateResource(sub2Stream, "pv_reunion_absents.jasper");
            validateResource(sub4Stream, "pv_reunion_sub2AC.jasper");

            // Configuration des paramètres
            parameters.put("reunion", reunionId);
            parameters.put("president", fullNamePres);
            parameters.put("pathImg", logoStream);
            parameters.put("interim", interim);
            parameters.put("acId", acId);
            parameters.put("ppm_id", ppmId);
            parameters.put("ppm_ver", ppmVer);
            parameters.put("dac_id", dacId);
            parameters.put("dac_ver", dacVer);
            parameters.put("SUBREPORT_DIR1", sub1Stream);
            parameters.put("SUBREPORT_DIR2", sub2Stream);
            parameters.put("SUBREPORT_DIR4", sub4Stream);
            parameters.put("REPORT_CONNECTION", conn);


            // Génération du rapport
            JasperPrint jasperPrint = JasperFillManager.fillReport(mainReportStream, parameters, conn);
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException e) {
            throw new RuntimeException("Erreur lors de la génération du rapport Jasper: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de la génération du PV: " + e.getMessage(), e);
        }
    }

    public byte[] generateExtraitProcesVerbalCNCMPGlob(Integer reunionId, Integer acId) throws Exception {
        // Validation initiale
        if (reunionId == null) {
            throw new IllegalArgumentException("L'ID de la réunion ne peut pas être null");
        }
        // Récupération de la réunion
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("Réunion introuvable : " + reunionId));

        // Récupération du président
        String idPresedent = reunion.getParticipants().stream().filter(p -> p.getFonctionReunion().equals(ParticipantFonction.PRESIDENT)).findFirst().get().getUserId();
        String fullNamePres = keycloakAdminService.getUserById(idPresedent).getFirstName()
                + " " + keycloakAdminService.getUserById(idPresedent).getLastName();
        Optional<String> optionalInterim=reunionService.findInterimParticipant(reunionId);

        String interim;

        interim = optionalInterim.orElse(null);

        // Map des paramètres
        Map<String, Object> parameters = new HashMap<>();

        // Gestion des ressources avec try-with-resources
        try (Connection conn = dataSource.getConnection();
             InputStream mainReportStream = getClass().getClassLoader().getResourceAsStream("pv/extrait_pv_CNCMPGlob.jasper");
             InputStream logoStream = getClass().getClassLoader().getResourceAsStream("pv/logo.png");
             InputStream sub1Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_presents.jasper");
             InputStream sub2Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_absents.jasper");
             InputStream sub4Stream = getClass().getClassLoader().getResourceAsStream("pv/pv_reunion_sub2ACGlob.jasper")) {

            // Vérification des ressources
            validateResource(mainReportStream, "extrait_pv_CNCMPGlob.jasper");
            validateResource(logoStream, "logo.png");
            validateResource(sub1Stream, "pv_reunion_presents.jasper");
            validateResource(sub2Stream, "pv_reunion_absents.jasper");
            validateResource(sub4Stream, "pv_reunion_sub2ACGlob.jasper");

            // Configuration des paramètres
            parameters.put("reunion", reunionId);
            parameters.put("president", fullNamePres);
            parameters.put("pathImg", logoStream);
            parameters.put("interim", interim);
            parameters.put("acId", acId);
            parameters.put("SUBREPORT_DIR1", sub1Stream);
            parameters.put("SUBREPORT_DIR2", sub2Stream);
            parameters.put("SUBREPORT_DIR4", sub4Stream);
            parameters.put("REPORT_CONNECTION", conn);


            // Génération du rapport
            JasperPrint jasperPrint = JasperFillManager.fillReport(mainReportStream, parameters, conn);
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (JRException e) {
            throw new RuntimeException("Erreur lors de la génération du rapport Jasper: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de la génération du PV: " + e.getMessage(), e);
        }
    }

    // Méthode utilitaire pour valider les ressources
    private void validateResource(InputStream stream, String resourceName) throws FileNotFoundException {
        if (stream == null) {
            throw new FileNotFoundException("Resource introuvable : " + resourceName);
        }
    }

}
