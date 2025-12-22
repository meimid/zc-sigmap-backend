package com.sigmap.sigmap_dac.services.delegates;

import com.sigmap.common.dac.DacService;
import com.sigmap.common.entity.ProcessStatus;

import com.sigmap.sigmap_dac.services.DacShortlistMailService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("transmissionDaoDelegate")
public class TransmissionDaoDelegate implements JavaDelegate {

    @Autowired private DacService dacService;
    @Autowired private DacShortlistMailService dacShortlistMailService;

    @Override
    public void execute(DelegateExecution execution) {

        Integer dacId = (Integer) execution.getVariable("id");
        Integer dacVer = (Integer) execution.getVariable("version");
        String pid = execution.getProcessInstanceId();

        log.info("[DAC][TRANSMIT] Execute dacId={}, ver={}, pid={}", dacId, dacVer, pid);

        try {
            // publish
            dacService.updateStatus(dacId, dacVer, ProcessStatus.PUBLIE);

            // idempotency against retries
            Boolean alreadySent = (Boolean) execution.getVariable("shortlistMailSent");
            if (Boolean.TRUE.equals(alreadySent)) {
                log.info("[DAC][MAIL] shortlistMailSent=true -> skip (pid={})", pid);
                return;
            }

            // send mails from societe_dac -> societe.address.email
            dacShortlistMailService.notifyShortlist(dacId, dacVer);

            execution.setVariable("shortlistMailSent", true);
            log.info("[DAC][MAIL] Notifications sent -> shortlistMailSent=true (pid={})", pid);

        } catch (Exception e) {
            log.error("[DAC][TRANSMIT] Error for DAC {}: {}", dacId, e.getMessage(), e);
            throw new RuntimeException("Transmission DAO failed: " + e.getMessage(), e);
        }
    }
}
