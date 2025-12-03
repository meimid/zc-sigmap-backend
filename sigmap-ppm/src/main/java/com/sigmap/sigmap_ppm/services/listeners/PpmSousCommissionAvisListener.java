package com.sigmap.sigmap_ppm.services.listeners;

import com.sigmap.sigmap_ppm.services.PpmNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("ppmNotificationListenerSousCommission")
public class PpmSousCommissionAvisListener implements ExecutionListener {

    @Autowired
    private PpmNotificationService ppmNotificationService;

    @Override
    public void notify(DelegateExecution execution) {
        try {
            Integer ppmId = (Integer) execution.getVariable("id");
            Integer ppmVer = (Integer) execution.getVariable("version");

            log.info(">>> [SOUS-COMMISSION LISTENER] Avis sous-commission terminé pour PPM {}-{}", ppmId, ppmVer);

            // Maintenant on notifie CNCMP
            ppmNotificationService.notifyCncmpAfterSousCommissionAvis(ppmId, ppmVer);

        } catch (Exception e) {
            log.error("Erreur listener sous-commission: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
