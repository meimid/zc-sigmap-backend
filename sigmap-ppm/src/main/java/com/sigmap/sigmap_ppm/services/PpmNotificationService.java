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
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;

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
    @Autowired private KeycloakAdminService keycloakAdminService;
    @Autowired private WhatsAppNotificationService whatsAppNotificationService;

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

    private String safeAcPhone(Ppm ppm) {
        if (ppm == null || ppm.getAc() == null || ppm.getAc().getAdresse() == null) return null;
        String tel1 = ppm.getAc().getAdresse().getTel1();
        return tel1 != null && !tel1.isBlank() ? tel1 : ppm.getAc().getAdresse().getTel2();
    }

    private UserDTO safeCpmpPresident(Com cpmp) {
        if (cpmp == null || cpmp.getResponsableId() == null || cpmp.getResponsableId().isBlank()) return null;
        try {
            return keycloakAdminService.getBasicUserById(cpmp.getResponsableId());
        } catch (Exception e) {
            log.warn("[WHATSAPP] Impossible de récupérer le président de la commission {}: {}",
                    cpmp.getId(), e.getMessage());
            return null;
        }
    }

    private String userName(UserDTO user, String defaultName) {
        if (user == null) return defaultName;
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return fullName.isBlank() ? defaultName : fullName;
    }

    private boolean isReturnedToCpmp(String rawDecision) {
        if (rawDecision == null) return false;
        String decision = rawDecision.trim().toUpperCase();
        return "REPORT".equals(decision) || "COND_APPROVE".equals(decision);
    }

    private void notifyCommissionPresident(Com commission, String defaultRecipientName,
                                           String senderName, String reference,
                                           String decision, String motif) {
        UserDTO president = safeCpmpPresident(commission);
        whatsAppNotificationService.sendDecisionNotification(
                president != null ? president.getMobile() : null,
                userName(president, defaultRecipientName),
                senderName,
                reference,
                decision,
                motif
        );
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

    private String normalizeDecision(String raw) {
        if (raw == null) return "Avis";
        String d = raw.trim().toUpperCase();
        return switch (d) {
            case "APPROVE", "VALIDATE", "TRUE" -> "Approuvé";
            case "REJECT", "FALSE" -> "Rejeté";
            case "CI" -> "Complément d'information";
            case "REPORT" -> "Renvoyé au CPMP";
            case "COND_APPROVE" -> "Approuvé sous réserve";
            case "REJECT_END" -> "Rejeté définitivement";
            default -> "Avis";
        };
    }
    private String normalizeDecisionCpmp(String raw) {
        if (raw == null) return "Avis";
        String d = raw.trim().toUpperCase();
        return switch (d) {
            case "APPROVE", "VALIDATE", "TRUE" -> "Approuvé";
            case "REJECT", "FALSE" -> "Reporté";
            case "CI" -> "Complément d'information";
            default -> "Avis";
        };
    }

    private String normalizeMotif(String motif) {
        if (motif == null || motif.isBlank()) return "";
        return motif.trim();
    }

    /** AC -> CPMP. Best effort: absence de président/mobile ou erreur Meta ne bloque jamais le workflow. */
    public void notifyCpmpAfterAcSubmission(Integer ppmId, Integer ppmVer) {
        try {
            Ppm ppm = ppmRepository.findById(new CompositeId(ppmId, ppmVer))
                    .orElseThrow(() -> new PpmNotFound("PPM not found", List.of(ppmId), List.of(ppmVer)));
            Integer cpmpId = acServiceImpl.getComIdByAcId(ppm.getAc().getId());
            Com cpmp = cpmpId != null ? comRepository.findById(cpmpId).orElse(null) : null;
            String acName = ppm.getAc() != null && ppm.getAc().getNom() != null
                    ? ppm.getAc().getNom() : "Autorité contractante";

            notifyCommissionPresident(
                    cpmp,
                    "Président de la CPMP",
                    acName,
                    ppm.getRef(),
                    "Dossier transmis au CPMP",
                    "Le PPM a été validé et transmis par l'autorité contractante."
            );
        } catch (Exception e) {
            log.warn("[WHATSAPP] Échec non bloquant AC→CPMP pour PPM {}-{}: {}",
                    ppmId, ppmVer, e.getMessage());
        }
    }



    public void notifyAcAfterCpmpDecision(Integer ppmId, Integer ppmVer) throws PpmNotFound {

        try {
            // ---- PPM ----
            Ppm ppm = ppmRepository.findById(new CompositeId(ppmId, ppmVer))
                    .orElseThrow(() -> new PpmNotFound("PPM not found", List.of(ppmId), List.of(ppmVer)));

            // ---- AC email ----
            String acEmail = ppm.getAc() != null ? ppm.getAc().getEmail() : null;

            // ---- CPMP name ----
            Integer cpmpId = acServiceImpl.getComIdByAcId(ppm.getAc().getId());
            Com cpmp = cpmpId != null ? comRepository.findById(cpmpId).orElse(null) : null;

            String cpmpName = safeName(cpmp, "CPMP");

            // ---- Réunion & décision ----
            String reunionDate = extractReunionDate(reunionRepository.findLatestByPpm(ppmId, ppmVer));
            ReunionPpm rp = extractLatestDecision(ppmId, ppmVer);

            String decision = normalizeDecisionCpmp(rp != null ? rp.getDecision() : null);
            String motif = normalizeMotif(rp != null ? rp.getObservation() : null);

            // CPMP approuve => transmission à la CNCMP. Sinon => retour à l'AC.
            String rawDecision = rp != null ? rp.getDecision() : null;
            if ("APPROVE".equalsIgnoreCase(rawDecision)
                    || "VALIDATE".equalsIgnoreCase(rawDecision)
                    || "TRUE".equalsIgnoreCase(rawDecision)) {
                Com cncmp = comServiceImpl.findCNCMPP();
                notifyCommissionPresident(
                        cncmp,
                        "Président de la CNCMP",
                        cpmpName,
                        ppm.getRef(),
                        decision,
                        motif
                );
            } else {
                whatsAppNotificationService.sendDecisionNotification(
                        safeAcPhone(ppm),
                        ppm.getAc() != null ? ppm.getAc().getNom() : null,
                        cpmpName,
                        ppm.getRef(),
                        decision,
                        motif
                );
            }

            // ---- EMAIL ----
            if (acEmail == null || acEmail.isBlank()) {
                log.warn("❌ AC email missing → no CPMP→AC email sent for PPM {}-{}", ppmId, ppmVer);
                return;
            }
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

            // ---- CNCMP name ----
            Com cncmp = comServiceImpl.findCNCMPP();
            String cncmpName = safeName(cncmp, "CNCMP");

            // ---- Réunion & décision ----
            String reunionDate = extractReunionDate(reunionRepository.findLatestByPpm(ppmId, ppmVer));
            ReunionPpm rp = extractLatestDecision(ppmId, ppmVer);

            String decision = normalizeDecision(rp != null ? rp.getDecision() : null);
            String motif = normalizeMotif(rp != null ? rp.getObservation() : null);

            String rawDecision = rp != null ? rp.getDecision() : null;
            if (isReturnedToCpmp(rawDecision)) {
                notifyCommissionPresident(
                        cpmp,
                        "Président de la CPMP",
                        cncmpName,
                        ppm.getRef(),
                        decision,
                        motif
                );
            } else {
                whatsAppNotificationService.sendDecisionNotification(
                        safeAcPhone(ppm),
                        ppm.getAc() != null ? ppm.getAc().getNom() : null,
                        cncmpName,
                        ppm.getRef(),
                        decision,
                        motif
                );
            }

            // ---- EMAIL ----
            if (cpmpEmail == null || cpmpEmail.isBlank()) {
                log.warn("❌ No CPMP email → no CNCMP→CPMP email sent for PPM {}-{}", ppmId, ppmVer);
                return;
            }
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

            String decision = normalizeDecision(rp != null ? rp.getDecision() : null);
            String motif = normalizeMotif(rp != null ? rp.getObservation() : null);

            notifyCommissionPresident(
                    cncmp,
                    "Président de la CNCMP",
                    scsName,
                    ppm.getRef(),
                    decision,
                    motif
            );

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
