package com.sigmap.sigmap_dac.dac;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.ppms.exception.MissingAcIdException;
import com.sigmap.common.ppms.exception.MissingComIdException;
import com.sigmap.common.ppms.exception.UserNotAssociatedWithAcException;
import com.sigmap.common.ppms.exception.UserNotAssociatedWithComException;
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
@RequestMapping("/api/flowable")
@Slf4j
public class DacFlowableController {

    @Autowired
    private DacFlowableService dacFlowableService;

    /**
     * Starts a Flowable process instance for a DAC entity (latest version inferred).
     *
     * @param id DAC ID
     * @return ProcessStartResponseDTO
     */
    @PostMapping("/start/{id}")
    public ResponseEntity<ProcessStartResponseDTO> startProcessInstance(@PathVariable Integer id)
            throws DacNotFound, AcNotFound {
        log.info("Starting DAC Flowable process for DAC ID: {}", id);
        ProcessStartResponseDTO response = dacFlowableService.startProcessInstance(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details for a specific active DAC task.
     *
     * @param taskId the task id
     * @return TaskDetailsDTO
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDTO> getTaskDetails(@PathVariable String taskId)
            throws DacNotFound, AcNotFound {
        log.info("Fetching DAC task details for taskId={}", taskId);
        TaskDetailsDTO response = dacFlowableService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Validates (or rectifies) a DAC task — optionally updates DAC documents in batch.
     *
     * Multipart-friendly: UI can post form fields + files via @ModelAttribute binding.
     *
     * @param taskId  the task id
     * @param request ValidationRequest with validate/comment flags and optional DacDocsRequest list
     * @return TaskDTO
     */
    @PostMapping(value = "/tasks/{taskId}/validate", consumes = {"multipart/form-data"})
    public ResponseEntity<TaskDTO> validateTask(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request
    ) throws DacNotFound,
            DocNotFound,
            AcNotFound,
            IOException,
            ComNotFound,
            MissingAcIdException,
            MissingComIdException,
            UserNotAssociatedWithAcException,
            UserNotAssociatedWithComException {
        log.info("Validating DAC taskId={} with request={}", taskId, request);
        TaskDTO response = dacFlowableService.validateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all DAC tasks visible to the current authenticated user
     * (based on candidate roles and user's associated ACs).
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
     * Loads latest DAC version and (if visible) its active Flowable task.
     *
     * @param dacId DAC id
     * @return TaskDetailsDTO including DAC docs and active task info (if any)
     */
    @GetMapping("/dac/{dacId}/with-task")
    public ResponseEntity<TaskDetailsDTO> getDacWithTask(@PathVariable Integer dacId)
            throws DacNotFound, AcNotFound {
        log.info("Fetching DAC {} with active task (if any)", dacId);
        TaskDetailsDTO response = dacFlowableService.getDacWithTasks(dacId);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the history of all finished tasks (with comments) for a DAC process instance.
     *
     * @param processInstanceId process instance id
     * @return list of HistoricTaskDto
     */
    @GetMapping("/task-history/{processInstanceId}")
    public ResponseEntity<List<HistoricTaskDto>> loadHistoric(@PathVariable("processInstanceId") String processInstanceId) {
        log.info("Loading DAC historic tasks for processInstanceId={}", processInstanceId);
        List<HistoricTaskDto> history = dacFlowableService.loadHistoricTasks(processInstanceId);
        return ResponseEntity.ok(history);
    }
}
