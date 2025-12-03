package com.sigmap.sigmap_ppm.services;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.email_trace.TypeEmail;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionRepository;
import com.sigmap.common.reunionPpm.ReunionPpm;
import com.sigmap.common.reunionPpm.ReunionPpmRepository;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.ComServiceImpl;
import com.sigmap.common.ac.AcServiceImpl;

import lombok.extern.slf4j.Slf4j;

import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PpmNotificationService {

    @Autowired private MailService mailService;
    @Autowired private PpmRepository ppmRepository;
    @Autowired private ReunionRepository reunionRepository;
    @Autowired private ReunionPpmRepository reunionPpmRepository;

    @Autowired private AcServiceImpl acServiceImpl;
    @Autowired private ComServiceImpl comServiceImpl;
    @Autowired private ComRepository comRepository;

    @Autowired private RuntimeService runtimeService;

    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    private String safeEmail(Com com) {
        if (com == null) return null;
        if (com.getTheAdresse() == null) return null;
        return com.getTheAdresse().getEmail();
    }

    private String safeName(Com com, String defaultName) {
        if (com == null) return defaultName;
        if (com.getNom() == null || com.getNom().isBlank()) return defaultName;
        return com.getNom();
    }

    private String extractReunionDate(Reunion reunion) {
        if (reunion == null || reunion.getReunionDate() == null) return "N/A";

        LocalDate date = reunion.getReunionDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return date.format(DATE_FR);
    }

    private ReunionPpm extractLatestDecision(Integer ppmId, Integer ppmVer) {
        return reunionPpmRepository.findLatest(ppmId, ppmVer);
    }


    public void notifyAcAfterCpmpDecision(Integer ppmId, Integer ppmVer) throws PpmNotFound {

        try {
            // ---- PPM ----
            Ppm ppm = ppmRepository.findById(new CompositeId(ppmId, ppmVer))
                    .orElseThrow(() -> new PpmNotFound("PPM not found", List.of(ppmId), List.of(ppmVer)));

            // ---- AC email ----
            String acEmail = ppm.getAc() != null ? ppm.getAc().getEmail() : null;

            if (acEmail == null || acEmail.isBlank()) {
                log.warn("❌ AC email missing → no CPMP→AC email sent for PPM {}-{}", ppmId, ppmVer);
                return;
            }

            // ---- CPMP name ----
            Integer cpmpId = acServiceImpl.getComIdByAcId(ppm.getAc().getId());
            Com cpmp = cpmpId != null ? comRepository.findById(cpmpId).orElse(null) : null;

            String cpmpName = safeName(cpmp, "CPMP");

            // ---- Réunion & décision ----
            String reunionDate = extractReunionDate(reunionRepository.findLatestByPpm(ppmId, ppmVer));
            ReunionPpm rp = extractLatestDecision(ppmId, ppmVer);

            String decision = rp != null && rp.getDecision() != null ? rp.getDecision() : "Non renseignée";
            String motif = rp != null && rp.getObservation() != null ? rp.getObservation() : "";

            // ---- EMAIL ----
            mailService.sendCommissionDecisionNotificationEmail(
                    acEmail,
                    ppm.getRef(),
                    cpmpName,
                    reunionDate,
                    decision,
                    motif,
                    "SYSTEM",
                    TypeEmail.PPM_CPMP_DECISION
            );

            log.info("✔ CPMP → AC email sent for PPM {}-{}", ppmId, ppmVer);

        } catch (Exception e) {
            log.error("❌ Error CPMP→AC notif for PPM {}-{}", ppmId, ppmVer, e);
        }
    }


    public void notifyCpmpAfterCncmpDecision(Integer ppmId, Integer ppmVer) throws PpmNotFound {

        try {
            // ---- PPM ----
            Ppm ppm = ppmRepository.findById(new CompositeId(ppmId, ppmVer))
                    .orElseThrow(() -> new PpmNotFound("PPM not found", List.of(ppmId), List.of(ppmVer)));

            // ---- CPMP ----
            Integer cpmpId = acServiceImpl.getComIdByAcId(ppm.getAc().getId());
            Com cpmp = cpmpId != null ? comRepository.findById(cpmpId).orElse(null) : null;

            String cpmpEmail = safeEmail(cpmp);

            if (cpmpEmail == null) {
                log.warn("❌ No CPMP email → no CNCMP→CPMP email sent for PPM {}-{}", ppmId, ppmVer);
                return;
            }

            // ---- CNCMP name ----
            Com cncmp = comServiceImpl.findCNCMPP();
            String cncmpName = safeName(cncmp, "CNCMP");

            // ---- Réunion & décision ----
            String reunionDate = extractReunionDate(reunionRepository.findLatestByPpm(ppmId, ppmVer));

            ReunionPpm rp = extractLatestDecision(ppmId, ppmVer);
            String decision = rp != null ? rp.getDecision() : "Non renseignée";
            String motif = rp != null ? rp.getObservation() : "";

            // ---- EMAIL ----
            mailService.sendCommissionDecisionNotificationEmail(
                    cpmpEmail,
                    ppm.getRef(),
                    cncmpName,
                    reunionDate,
                    decision,
                    motif,
                    "SYSTEM",
                    TypeEmail.PPM_CNCMP_DECISION
            );

            log.info("✔ CNCMP → CPMP email sent for PPM {}-{}", ppmId, ppmVer);

        } catch (Exception e) {
            log.error("❌ Error CNCMP→CPMP email for PPM {}-{}", ppmId, ppmVer, e);
        }
    }


    public void notifyCncmpAfterSousCommissionAvis(Integer ppmId, Integer ppmVer) throws PpmNotFound {
        try {

            // ---- PPM ----
            Ppm ppm = ppmRepository.findById(new CompositeId(ppmId, ppmVer))
                    .orElseThrow(() -> new PpmNotFound("PPM not found", List.of(ppmId), List.of(ppmVer)));

            // ---- SCS from variable ----
            Integer scsId = ppm.getProcessInstanceId() != null
                    ? (Integer) runtimeService.getVariable(ppm.getProcessInstanceId(), "scsId")
                    : null;

            Com scs = scsId != null ? comRepository.findById(scsId).orElse(null) : null;

            String scsName = safeName(scs, "Sous-Commission spécialisée");

            // ---- CNCMP ----
            Com cncmp = comServiceImpl.findCNCMPP();
            String cncmpEmail = safeEmail(cncmp);
            String cncmpName = safeName(cncmp, "CNCMP");

            if (cncmpEmail == null) {
                log.warn("❌ CNCMP email missing → no Sous-Commission→CNCMP email for PPM {}-{}", ppmId, ppmVer);
                return;
            }

            // ---- Réunion & décision ----
            String reunionDate = extractReunionDate(reunionRepository.findLatestByPpm(ppmId, ppmVer));

            ReunionPpm rp = extractLatestDecision(ppmId, ppmVer);
            String decision = rp != null ? rp.getDecision() : "Non renseignée";
            String motif = rp != null ? rp.getObservation() : "";

            // ---- EMAIL ----
            mailService.sendCommissionDecisionNotificationEmail(
                    cncmpEmail,
                    ppm.getRef(),
                    scsName,
                    reunionDate,
                    decision,
                    motif,
                    "SYSTEM",
                    TypeEmail.PPM_SOUS_COMMISSION_AVIS
            );

            log.info("✔ Sous-Commission → CNCMP email sent for PPM {}-{}", ppmId, ppmVer);

        } catch (Exception e) {
            log.error("❌ Error Sous-Commission→CNCMP for PPM {}-{}", ppmId, ppmVer, e);
        }
    }
}
