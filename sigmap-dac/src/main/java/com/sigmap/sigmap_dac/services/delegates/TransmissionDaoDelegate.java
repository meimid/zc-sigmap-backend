package com.sigmap.sigmap_dac.services.delegates;

import com.sigmap.common.dac.DacService;
import com.sigmap.common.entity.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Flowable delegate used in:
 * "Transmission DAO + Lettre d’invitation aux candidats"
 * Appears in dacProcess.bpmn using:
 * flowable:delegateExpression="${transmissionDaoDelegate}"
 */
@Slf4j
@Component("transmissionDaoDelegate")
public class TransmissionDaoDelegate implements JavaDelegate {

    @Autowired
    private DacService dacService;

    @PostConstruct
    public void init() {
        log.info(">>> [TransmissionDaoDelegate] Bean created by Spring.");
        log.info(">>> [TransmissionDaoDelegate] dacService injected? {}", dacService != null);
    }

    @Override
    public void execute(DelegateExecution execution) {

        Integer dacId = (Integer) execution.getVariable("id");
        Integer dacVer = (Integer) execution.getVariable("version");
        String processInstanceId = execution.getProcessInstanceId();

        log.info("Executing TransmissionDaoDelegate for DAC id={}, ver={}, process={}",
                dacId, dacVer, processInstanceId);

        try {
            // TODO: Replace with real transmission logic
            dacService.updateStatus(dacId, dacVer, ProcessStatus.PUBLIE);

            log.info("Transmission logic executed successfully for DAC {}", dacId);

        } catch (Exception e) {
            log.error("Error during DAO transmission for DAC {}: {}", dacId, e.getMessage(), e);
            throw new RuntimeException("Transmission DAO failed: " + e.getMessage(), e);
        }
    }
}
