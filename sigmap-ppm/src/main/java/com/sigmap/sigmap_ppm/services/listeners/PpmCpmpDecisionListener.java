package com.sigmap.sigmap_ppm.services.listeners;

import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.sigmap_ppm.services.PpmNotificationService;
import jakarta.annotation.PostConstruct;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ppmNotificationListenerCpmp")
public class PpmCpmpDecisionListener implements ExecutionListener {

    @Autowired
    private PpmNotificationService notificationService;

    @PostConstruct
    public void test() {
        System.out.println("BEAN LOADED: ppmNotificationListenerCpmp");
    }

    @Override
    public void notify(DelegateExecution execution) {
        Integer ppmId  = (Integer) execution.getVariable("id");
        Integer ppmVer = (Integer) execution.getVariable("version");

        try {
            notificationService.notifyAcAfterCpmpDecision(ppmId, ppmVer);
        } catch (PpmNotFound e) {
            throw new RuntimeException(e);
        }
    }
}
