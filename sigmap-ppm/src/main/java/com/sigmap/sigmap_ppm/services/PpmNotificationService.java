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

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    /**
     * CPMP → AC notification après décision CPMP
     */
    public void notifyAcAfterCpmpDecision(Integer ppmId, Integer ppmVer) throws PpmNotFound {

        try {
            Ppm ppm = ppmRepository.findById(new CompositeId(ppmId, ppmVer))
                    .orElseThrow(() -> new PpmNotFound("PPM not found", List.of(ppmId), List.of(ppmVer)));

            // 1️⃣ Email du AC
            if (ppm== null || ppm.getAc()==null || ppm.getAc().getEmail()==null){
                log.info("Ppm not found or email not found {} " , ppm.getAc().getEmail());
                throw new RuntimeException("Ppm not found or email not found");
            }

            String acEmail = ppm.getAc().getEmail();
            log.info("AC email: {}", acEmail);

            // 2️⃣ Date de réunion
            Reunion reunion = reunionRepository.findLatestByPpm(ppmId, ppmVer);
            String reunionDate = extractReunionDate(reunion);

            // 3️⃣ Décision CPMP
            ReunionPpm rp = extractLatestDecision(ppmId, ppmVer);
            String decision = rp != null && rp.getDecision() != null ? rp.getDecision() : "Non renseignée";
            String motif = rp != null && rp.getObservation() != null ? rp.getObservation() : "";

            // 4️⃣ Envoi email
            mailService.sendCommissionDecisionNotificationEmail(
                    acEmail,
                    ppm.getRef(),
                    "CPMP",
                    reunionDate,
                    decision,
                    motif,
                    "SYSTEM",
                    TypeEmail.PPM_CPMP_DECISION
            );

            log.info("Notification CPMP→AC envoyée pour PPM {}-{}", ppmId, ppmVer);

        } catch (MessagingException | IOException e) {
            log.error("Failed to send CPMP→AC notification for PPM {}-{}", ppmId, ppmVer, e);
        }
    }


    /**
     * CNCMP → CPMP notification après décision CNCMP
     */
    public void notifyCpmpAfterCncmpDecision(Integer ppmId, Integer ppmVer) throws PpmNotFound {

        try {
            Ppm ppm = ppmRepository.findById(new CompositeId(ppmId, ppmVer))
                    .orElseThrow(() -> new PpmNotFound("PPM not found", List.of(ppmId), List.of(ppmVer)));

            // 1️⃣ Trouver CPMP via l'AC de ce PPM
            Integer acId = ppm.getAc() != null ? ppm.getAc().getId() : null;
            if (acId == null) {
                log.warn("AC not found on PPM {}-{}", ppmId, ppmVer);
                return;
            }

            Integer cpmpId = acServiceImpl.getComIdByAcId(acId);
            if (cpmpId == null) {
                log.warn("No CPMP found for AC {} when notifying CNCMP→CPMP", acId);
                return;
            }

            Com cpmp = comRepository.findById(cpmpId).orElse(null);
            if (cpmp == null || cpmp.getTheAdresse() == null || cpmp.getTheAdresse().getEmail() == null) {
                log.warn("No CPMP email configured (comId={}) for ppm {}-{}", cpmpId, ppmId, ppmVer);
                return;
            }

            String cpmpEmail = cpmp.getTheAdresse().getEmail();

            // 2️⃣ Date réunion CNCMP
            Reunion reunion = reunionRepository.findLatestByPpm(ppmId, ppmVer);
            String reunionDate = extractReunionDate(reunion);

            // 3️⃣ Décision CNCMP
            ReunionPpm rp = extractLatestDecision(ppmId, ppmVer);
            String decision = rp != null && rp.getDecision() != null ? rp.getDecision() : "Non renseignée";
            String motif = rp != null && rp.getObservation() != null ? rp.getObservation() : "";

            // 4️⃣ Envoi email
            mailService.sendCommissionDecisionNotificationEmail(
                    cpmpEmail,
                    ppm.getRef(),
                    "CNCMP",
                    reunionDate,
                    decision,
                    motif,
                    "SYSTEM",
                    TypeEmail.PPM_CNCMP_DECISION
            );

            log.info("Notification CNCMP→CPMP envoyée pour PPM {}-{}", ppmId, ppmVer);

        } catch (MessagingException | IOException e) {
            log.error("Failed to send CNCMP→CPMP notification for PPM {}-{}", ppmId, ppmVer, e);
        }
    }

    public void notifyCncmpAfterSousCommissionAvis(Integer ppmId, Integer ppmVer) throws PpmNotFound {
        try {
            Ppm ppm = ppmRepository.findById(new CompositeId(ppmId, ppmVer))
                    .orElseThrow(() -> new PpmNotFound("PPM not found", List.of(ppmId), List.of(ppmVer)));

            // 🔎 Trouver CNCMP (unique commission permanente)
            Com cncmp = comServiceImpl.findCNCMPP();

            if (cncmp == null ||
                    cncmp.getTheAdresse() == null ||
                    cncmp.getTheAdresse().getEmail() == null) {

                log.warn("❌ Aucun email CNCMP disponible pour notifier l'avis sous-commission du PPM {}-{}", ppmId, ppmVer);
                return;
            }

            String cncmpEmail = cncmp.getTheAdresse().getEmail();

            // 📅 Dernière réunion liée au PPM
            Reunion reunion = reunionRepository.findLatestByPpm(ppmId, ppmVer);

            String reunionDate = "N/A";
            if (reunion != null && reunion.getReunionDate() != null) {
                LocalDate date = reunion.getReunionDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                reunionDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            // 📝 Dernier avis de la sous-commission spécialisée
            ReunionPpm rp = reunionPpmRepository.findLatest(ppmId, ppmVer);

            String decision = (rp != null && rp.getDecision() != null)
                    ? rp.getDecision()
                    : "Non renseignée";

            String motif = (rp != null && rp.getObservation() != null)
                    ? rp.getObservation()
                    : "";

            // 📧 ENVOI EMAIL → CNCMP
            mailService.sendCommissionDecisionNotificationEmail(
                    cncmpEmail,
                    ppm.getRef(),
                    "SOUS-COMMISSION SPÉCIALISÉE",
                    reunionDate,
                    decision,
                    motif,
                    "SYSTEM",
                    TypeEmail.PPM_SOUS_COMMISSION_AVIS
            );

            log.info("✔ Email Avis Sous-Commission envoyé à CNCMP pour PPM {}-{}", ppmId, ppmVer);

        } catch (Exception e) {
            log.error("❌ Erreur envoi email sous-commission → CNCMP pour PPM {}-{}", ppmId, ppmVer, e);
            throw new RuntimeException(e);
        }
    }

}
