package com.sigmap.sigmap_dac.services;

import com.sigmap.common.appel_offre.AppelOffre;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.email_trace.TypeEmail;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe_dac.SocieteDac;
import com.sigmap.common.societe_dac.SocieteDacRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DacShortlistMailService {

    private final SocieteDacRepository societeDacRepository;
    private final DacService dacService;
    private final MailService mailService;

    /**
     * Sends invitation mail to shortlist companies (societe_dac rows).
     */
    public void notifyShortlist(Integer dacId, Integer dacVer) {
        if (dacId == null || dacVer == null) {
            log.warn("[DAC][MAIL] Missing dacId/dacVer -> skip");
            return;
        }

        Optional<Dac> dacOpt = dacService.findById(dacId, dacVer);
        if (dacOpt.isEmpty()) {
            log.warn("[DAC][MAIL] DAC not found id={}, ver={} -> skip", dacId, dacVer);
            return;
        }
        Dac dac = dacOpt.get();

        List<SocieteDac> links = societeDacRepository.findByDacIdAndDacVer(dacId, dacVer);
        if (links == null || links.isEmpty()) {
            log.warn("[DAC][MAIL] No societe_dac rows found for dacId={}, ver={} -> no mails", dacId, dacVer);
            return;
        }

        // ---- Extract AO info (Objet AO + Référence AO) ----
        AppelOffre ao = dac.getAppelOffre();
        String aoObjet = (ao != null && ao.getNom() != null && !ao.getNom().isBlank())
                ? ao.getNom()
                : "Objet AO";

        String aoReference = (ao != null && ao.getReference() != null && !ao.getReference().isBlank())
                ? ao.getReference()
                : ((dac.getReference() != null && !dac.getReference().isBlank())
                ? dac.getReference()
                : ("DAC " + dacId + "/" + dacVer));

        // ---- Date limite soumission formatting ----
        String dateLimiteSoumission = "N/A";
        try {
            if (dac.getDateLimiteSoumission() != null && dac.getDateLimiteSoumission().getTime() != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                dateLimiteSoumission = dac.getDateLimiteSoumission()
                        .getTime()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(fmt);
            }
        } catch (Exception e) {
            log.warn("[DAC][MAIL] Failed formatting dateLimiteSoumission: {}", e.getMessage());
        }


        String acName = dac.getAppelOffre().getPpm().getAc().getNom();


        Set<String> emails = new LinkedHashSet<>();
        for (SocieteDac sd : links) {
            Societe s = (sd != null && sd.getId() != null) ? sd.getId().getSociete() : null;
            String email = (s != null && s.getAddress() != null) ? s.getAddress().getEmail() : null;
            if (email != null && !email.isBlank()) {
                emails.add(email.trim());
            }
        }

        if (emails.isEmpty()) {
            log.warn("[DAC][MAIL] No emails found in societe.address.email for dacId={}, ver={}", dacId, dacVer);
            return;
        }

        // ---- Send ----
        int ok = 0;
        for (String to : emails) {
            try {
                mailService.sendDacShortlistInvitationEmail(
                        to,
                        acName,
                        aoObjet,
                        aoReference,
                        dateLimiteSoumission,
                        "SYSTEM",
                        TypeEmail.DAC_SHORTLIST_INVITATION
                );
                ok++;
            } catch (Exception e) {
                log.warn("[DAC][MAIL] Failed sending to {}: {}", to, e.getMessage());
            }
        }

        log.info("[DAC][MAIL] Sent {}/{} invitation mails for DAC id={}, ver={}",
                ok, emails.size(), dacId, dacVer);
    }
}
