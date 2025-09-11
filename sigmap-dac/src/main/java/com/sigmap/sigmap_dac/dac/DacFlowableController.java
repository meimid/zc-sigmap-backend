package com.sigmap.sigmap_dac.dac;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.dac.exeption.DacNotFound;

import com.sigmap.sigmap_dac.services.DacFlowableService;
import com.sigmap.sigmap_dac.services.dto.request.ValidationRequest;
import com.sigmap.sigmap_dac.services.dto.response.HistoricTaskDto;
import com.sigmap.sigmap_dac.services.dto.response.ProcessStartResponseDTO;
import com.sigmap.sigmap_dac.services.dto.response.TaskDTO;
import com.sigmap.sigmap_dac.services.dto.response.TaskDetailsDTO;
import com.sigmap.sigmap_dac.services.dto.response.TaskListResponseDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * REST Controller for managing Flowable process instances and tasks related to DAC entities.
 */
@RestController
@RequestMapping("/api/dacs/flowable")
@Slf4j
public class DacFlowableController {

    @Autowired
    private DacFlowableService dacFlowableService;

    /**
     * Starts a Flowable process instance for a DAC entity using its ID (latest version is inferred).
     *
     * @param id DAC ID
     * @return ProcessStartResponseDTO
     */
    @PostMapping("/start/{id}")
    public ResponseEntity<ProcessStartResponseDTO> startProcessInstance(@PathVariable Integer id)
            throws DacNotFound, AcNotFound {
        log.info("Starting DAC flowable process for DAC ID: {}", id);
        ProcessStartResponseDTO response = dacFlowableService.startProcessInstance(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details for a specific task.
     *
     * @param taskId the task id
     * @return TaskDetailsDTO
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDTO> getTaskDetails(@PathVariable String taskId)
            throws DacNotFound, AcNotFound {
        log.info("Fetching task details for taskId={}", taskId);
        TaskDetailsDTO response = dacFlowableService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a task and (optionally) rectifies DAC data in one-shot using DacDocsService batch.
     * Multipart-friendly so UI can post form fields easily via @ModelAttribute binding.
     *
     * @param taskId  the task id
     * @param request ValidationRequest (contains validate/comment and optional List<DacDocsRequest> updates)
     * @return TaskDTO
     */
    @PostMapping(value = "/tasks/{taskId}/validate", consumes = {"multipart/form-data"})
    public ResponseEntity<TaskDTO> validateTask(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request
    ) throws DacNotFound, DocNotFound, AcNotFound, IOException {
        log.info("Validating DAC taskId={} with request={}", taskId, request);
        TaskDTO response = dacFlowableService.validateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists tasks visible to the current user (based on candidate roles and user's ACs).
     *
     * @return TaskListResponseDTO
     */
    @GetMapping("/tasks/user")
    public ResponseEntity<TaskListResponseDTO> getUserTasks() {
        log.info("Loading DAC tasks for current user");
        TaskListResponseDTO response = dacFlowableService.getUserTasks();
        return ResponseEntity.ok(response);
    }

    /**
     * Loads DAC latest version and (if visible) its active task details.
     *
     * @param dacId DAC id
     * @return TaskDetailsDTO with dacDocs + active task (if any)
     */
    @GetMapping("/dac/{dacId}/with-task")
    public ResponseEntity<TaskDetailsDTO> getDacWithTask(@PathVariable Integer dacId)
            throws DacNotFound, AcNotFound {
        log.info("Fetching DAC {} with active task (if any)", dacId);
        TaskDetailsDTO response = dacFlowableService.getDacWithTasks(dacId);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns finished task history (with comments) for a given process instance id.
     *
     * @param procInstId process instance id
     * @return list of HistoricTaskDto
     */
    @GetMapping("/task-history/{procInstId}")
    public ResponseEntity<List<HistoricTaskDto>> loadHistoric(@PathVariable("procInstId") String procInstId) {
        log.info("Loading DAC historic tasks for processInstance={}", procInstId);
        List<HistoricTaskDto> history = dacFlowableService.loadHistoricTasks(procInstId);
        return ResponseEntity.ok(history);
    }
}
