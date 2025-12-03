package com.sigmap.sigmap_ppm.services.listeners;

import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.sigmap_ppm.services.PpmNotificationService;
import jakarta.annotation.PostConstruct;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ppmNotificationListenerCncmp")
public class PpmCncmpDecisionListener implements ExecutionListener {

    @Autowired
    private PpmNotificationService notificationService;

    @Override
    public void notify(DelegateExecution execution) {
        Integer ppmId  = (Integer) execution.getVariable("id");
        Integer ppmVer = (Integer) execution.getVariable("version");

        try {
            notificationService.notifyCpmpAfterCncmpDecision(ppmId, ppmVer);
        } catch (PpmNotFound e) {
            throw new RuntimeException(e);
        }
    }
}
