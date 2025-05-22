package com.sigmap.sigmap_ppm.ppms;

import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.request.ValidationRequest;
import com.sigmap.common.response.ProcessStartResponseDTO;
import com.sigmap.common.response.TaskDTO;
import com.sigmap.common.response.TaskListResponseDTO;
import com.sigmap.sigmap_ppm.ppms.dto.TaskDetailsDTO;
import com.sigmap.sigmap_ppm.ppms.services.PpmFlowableService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ppms/flowable")
public class PpmFlowableController {

    @Value("${tenant.id}")
    private String TENANT_ID;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private PpmService ppmService;

    @Autowired
    private PpmFlowableService ppmFlowableService;

    private final Logger log = LoggerFactory.getLogger(PpmFlowableController.class);

    @PostMapping("/start/{id}/{version}")
    public ResponseEntity<ProcessStartResponseDTO> startProcessInstance(@PathVariable Integer id, @PathVariable Integer version) {
        ProcessStartResponseDTO response = ppmFlowableService.startProcessInstance(id, version);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    public ResponseEntity<TaskListResponseDTO> getCurrentTasks() {
        TaskListResponseDTO response = ppmFlowableService.getCurrentTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDTO> getTaskDetails(@PathVariable String taskId) {
        TaskDetailsDTO response = ppmFlowableService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{taskId}/validate")
    public ResponseEntity<TaskDTO> validateTask(
            @PathVariable String taskId,
            @RequestBody ValidationRequest request) {
        TaskDTO response = ppmFlowableService.validateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/user")
    public ResponseEntity<TaskListResponseDTO> getUserTasks() {
        TaskListResponseDTO response = ppmFlowableService.getUserTasks();
        return ResponseEntity.ok(response);
    }
}