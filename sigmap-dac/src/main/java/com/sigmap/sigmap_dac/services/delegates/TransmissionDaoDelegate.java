package com.sigmap.sigmap_dac.services.delegates;

import com.sigmap.common.dac.DacService;
import com.sigmap.common.entity.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Flowable delegate that runs during the sendTask:
 * "Transmission DAO + Lettre d’invitation aux candidats"
 * Appears twice in dacProcess.bpmn (Activity_0u7tuz6, Activity_0z9wkoj).
 */
@Slf4j
@Service("transmissionDaoDelegate")
public class TransmissionDaoDelegate implements JavaDelegate {

    @Autowired
    private DacService dacService;
    @Override
    public void execute(DelegateExecution execution) {
        Integer dacId = (Integer) execution.getVariable("id");
        Integer dacVer = (Integer) execution.getVariable("version");
        String processInstanceId = execution.getProcessInstanceId();

        log.info("Executing TransmissionDaoDelegate for DAC id={}, ver={}, process={}", dacId, dacVer, processInstanceId);

        // 🔹 Example: simulate transmission
        try {
            // TODO: Implement actual transmission logic, e.g.:
            //  - generate DAO document
            //  - send email / letter / notification
            //  - update DAC status or attach document
            dacService.updateStatus(dacId, dacVer, ProcessStatus.PUBLIE);
            log.info("Transmission logic executed successfully for DAC {}", dacId);
        } catch (Exception e) {
            log.error("Error during DAO transmission for DAC {}: {}", dacId, e.getMessage(), e);
            throw new RuntimeException("Transmission DAO failed: " + e.getMessage(), e);
        }
    }
}
