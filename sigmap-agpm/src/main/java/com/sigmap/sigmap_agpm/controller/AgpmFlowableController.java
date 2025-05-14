package com.sigmap.sigmap_agpm.controller;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.sigmap_agpm.service.dto.request.ValidationRequest;
import com.sigmap.sigmap_agpm.service.dto.response.ProcessStartResponseDTO;
import com.sigmap.sigmap_agpm.service.dto.response.TaskDTO;
import com.sigmap.sigmap_agpm.service.dto.response.TaskDetailsDTO;
import com.sigmap.sigmap_agpm.service.dto.response.TaskListResponseDTO;
import com.sigmap.sigmap_agpm.service.AgpmFlowableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST Controller for managing Flowable process instances and tasks related to Agpm entities.
 */
@RestController
@RequestMapping("/api/agpms/flowable")
public class AgpmFlowableController {

    @Autowired
    private AgpmFlowableService agpmFlowableService;

    private final Logger log = LoggerFactory.getLogger(AgpmFlowableController.class);

    /**
     * Starts a Flowable process instance for an Agpm entity using its ID.
     * Automatically retrieves the latest version of the Agpm entity.
     *
     * @param id The Agpm ID
     * @return ResponseEntity with ProcessStartResponseDTO
     * @throws AgpmNotFound if the Agpm entity is not found
     */
    @PostMapping("/start/{id}")
    public ResponseEntity<ProcessStartResponseDTO> startProcessInstance(@PathVariable Integer id) throws AgpmNotFound {
        log.info("Starting process instance for Agpm ID: {}", id);
        ProcessStartResponseDTO response = agpmFlowableService.startProcessInstance(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all active tasks for the tenant.
     *
     * @return ResponseEntity with TaskListResponseDTO
     */
    @GetMapping("/tasks")
    public ResponseEntity<TaskListResponseDTO> getCurrentTasks() {
        log.info("Retrieving all current tasks");
        TaskListResponseDTO response = agpmFlowableService.getCurrentTasks();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details of a specific task.
     *
     * @param taskId The ID of the task
     * @return ResponseEntity with TaskDetailsDTO
     * @throws AgpmNotFound if the associated Agpm entity is not found
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDTO> getTaskDetails(@PathVariable String taskId) throws AgpmNotFound {
        log.info("Retrieving details for task ID: {}", taskId);
        TaskDetailsDTO response = agpmFlowableService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a task and updates the associated Agpm entity.
     *
     * @param taskId  The ID of the task to validate
     * @param request The validation request
     * @return ResponseEntity with TaskDTO
     * @throws AgpmNotFound if the Agpm entity is not found
     * @throws DocNotFound if a document is not found
     * @throws AcNotFound if the associated Ac entity is not found
     * @throws IOException if an error occurs during file processing
     */
    @PostMapping(consumes = {"multipart/form-data"}, value ="/tasks/{taskId}/validate")
    public ResponseEntity<TaskDTO> validateTask(
            @PathVariable String taskId,
            @ModelAttribute("request")  ValidationRequest request) throws AgpmNotFound, DocNotFound, AcNotFound, IOException {
        log.info("Validating task ID: {} with request: {}", taskId, request);
        TaskDTO response = agpmFlowableService.validateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves tasks assigned to the current user based on their candidate groups.
     *
     * @return ResponseEntity with TaskListResponseDTO
     */
    @GetMapping("/tasks/user")
    public ResponseEntity<TaskListResponseDTO> getUserTasks() {
        log.info("Retrieving tasks for current user");
        TaskListResponseDTO response = agpmFlowableService.getUserTasks();
        return ResponseEntity.ok(response);
    }
}