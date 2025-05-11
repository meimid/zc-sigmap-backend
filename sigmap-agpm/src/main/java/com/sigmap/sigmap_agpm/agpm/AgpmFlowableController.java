package com.sigmap.sigmap_agpm.agpm;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.sigmap_agpm.agpm.request.ValidationRequest;
import com.sigmap.sigmap_agpm.agpm.response.ProcessStartResponseDTO;
import com.sigmap.sigmap_agpm.agpm.response.TaskDTO;
import com.sigmap.sigmap_agpm.agpm.response.TaskDetailsDTO;
import com.sigmap.sigmap_agpm.agpm.response.TaskListResponseDTO;
import com.sigmap.sigmap_agpm.agpm.service.AgpmFlowableService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/agpms/flowable")
public class AgpmFlowableController {

    @Autowired
    private AgpmFlowableService agpmFlowableService;

    private final Logger log = LoggerFactory.getLogger(AgpmFlowableController.class);

    @PostMapping("/start/{id}/{version}")
    public ResponseEntity<ProcessStartResponseDTO> startProcessInstance(@PathVariable Integer id, @PathVariable Integer version) throws AgpmNotFound {
        ProcessStartResponseDTO response = agpmFlowableService.startProcessInstance(id, version);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    public ResponseEntity<TaskListResponseDTO> getCurrentTasks() {
        TaskListResponseDTO response = agpmFlowableService.getCurrentTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDTO> getTaskDetails(@PathVariable String taskId) throws AgpmNotFound {
        TaskDetailsDTO response = agpmFlowableService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{taskId}/validate")
    public ResponseEntity<TaskDTO> validateTask(
            @PathVariable String taskId,
            @RequestBody ValidationRequest request) throws AgpmNotFound, DocNotFound, AcNotFound {
        TaskDTO response = agpmFlowableService.validateTask(taskId, request) ;
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/user")
    public ResponseEntity<TaskListResponseDTO> getUserTasks() {
        TaskListResponseDTO response = agpmFlowableService.getUserTasks();
        return ResponseEntity.ok(response);
    }
}