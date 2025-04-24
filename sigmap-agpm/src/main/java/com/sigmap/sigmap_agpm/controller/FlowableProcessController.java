package com.sigmap.sigmap_agpm.controller;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for managing Flowable process definitions, instances, and tasks for the "agpm" tenant.
 * Provides endpoints to deploy processes, start instances, manage tasks, and query historical data.
 */
@RestController
@RequestMapping("/api/process")
public class FlowableProcessController {

    /** Tenant ID injected from application.yml . */
    @Value("${tenant.id}")
    private String TENANT_ID;


    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    /**
     * Deploys a BPMN process definition file for the "agpm" tenant.
     *
     * @param file The BPMN file to deploy (multipart/form-data).
     * @return ResponseEntity containing the deployment ID on success (200 OK) or an error message (400 Bad Request).
     * @throws Exception If the file is invalid or deployment fails.
     *
     * Example Response (Success):
     * {
     *   "Deployed: <deploymentId>"
     * }
     * Example Response (Error):
     * {
     *   "Deployment failed: <error message>"
     * }
     */
    @PostMapping("/deploy")
    public ResponseEntity<String> deployProcess(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Deployment deployment = repositoryService.createDeployment()
                    .tenantId(TENANT_ID)
                    .name(file.getOriginalFilename())
                    .addInputStream(file.getOriginalFilename(), is)
                    .deploy();
            return ResponseEntity.ok("Deployed: " + deployment.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Deployment failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves all process definitions for the "agpm" tenant.
     *
     * @return List of maps containing process definition details (id, key, version, name, deploymentId, tenantId, suspended).
     *         Returns 200 OK with an empty list if no definitions exist.
     *
     * Example Response:
     * [
     *   {
     *     "id": "myProcess:1",
     *     "key": "myProcess",
     *     "version": 1,
     *     "name": "My Process",
     *     "deploymentId": "12345",
     *     "tenantId": "agpm",
     *     "suspended": false
     *   }
     * ]
     */
    @GetMapping("/definitions")
    public List<Map<String, Object>> getProcessDefinitions() {
        List<ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(TENANT_ID)
                .orderByProcessDefinitionKey().asc()
                .list();
        return defs.stream()
                .map(pd -> {
                    Map<String, Object> definition = new HashMap<>();
                    definition.put("id", pd.getId());
                    definition.put("key", pd.getKey());
                    definition.put("version", pd.getVersion());
                    definition.put("name", pd.getName());
                    definition.put("deploymentId", pd.getDeploymentId());
                    definition.put("tenantId", pd.getTenantId());
                    definition.put("suspended", pd.isSuspended());
                    return definition;
                })
                .collect(Collectors.toList());
    }

    /**
     * Starts a new process instance for the specified process definition key and "agpm" tenant.
     *
     * @param processDefinitionKey The key of the process definition to start (path variable).
     * @param variables A map of process variables (optional, JSON body).
     * @return ResponseEntity containing process instance details on success (200 OK) or an error message (400 Bad Request).
     *
     * Example Request Body:
     * {
     *   "initiator": "john.doe",
     *   "priority": 5
     * }
     * Example Response (Success):
     * {
     *   "processInstanceId": "25001",
     *   "processDefinitionId": "myProcess:1",
     *   "status": "Process started successfully"
     * }
     * Example Response (Error):
     * {
     *   "error": "Failed to start process: No process definition found"
     * }
     */
    @PostMapping("/start/{processDefinitionKey}")
    public ResponseEntity<Map<String, String>> startProcessInstance(@PathVariable String processDefinitionKey,
                                                                    @RequestBody Map<String, Object> variables) {
        try {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId(
                    processDefinitionKey, variables, TENANT_ID);
            Map<String, String> response = new HashMap<>();
            response.put("processInstanceId", processInstance.getId());
            response.put("processDefinitionId", processInstance.getProcessDefinitionId());
            response.put("status", "Process started successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to start process: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all active process instances for the "agpm" tenant.
     *
     * @return List of maps containing process instance details (id, processDefinitionId, processDefinitionKey, startTime, status).
     *         Returns 200 OK with an empty list if no instances exist.
     *
     * Example Response:
     * [
     *   {
     *     "id": "25001",
     *     "processDefinitionId": "myProcess:1",
     *     "processDefinitionKey": "myProcess",
     *     "startTime": "2025-04-23T10:00:00.000+00:00",
     *     "status": "Active"
     *   }
     * ]
     */
    @GetMapping("/instances")
    public List<Map<String, Object>> getProcessInstances() {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .processInstanceTenantId(TENANT_ID)
                .orderByProcessInstanceId().asc()
                .list();
        return instances.stream().map(pi -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", pi.getId());
            info.put("processDefinitionId", pi.getProcessDefinitionId());
            info.put("processDefinitionKey", pi.getProcessDefinitionKey());
            info.put("startTime", pi.getStartTime());
            info.put("status", pi.isEnded() ? "Ended" : "Active");
            return info;
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves all active tasks for a specific process instance in the "agpm" tenant.
     *
     * @param processInstanceId The ID of the process instance (path variable).
     * @return List of maps containing task details (id, name, assignee, createTime, dueDate, processInstanceId, taskDefinitionKey, status).
     *         Returns 200 OK with an empty list if no tasks exist.
     *
     * Example Response:
     * [
     *   {
     *     "id": "50001",
     *     "name": "First Task",
     *     "assignee": "john.doe",
     *     "createTime": "2025-04-23T10:15:30.000+00:00",
     *     "dueDate": null,
     *     "processInstanceId": "25001",
     *     "taskDefinitionKey": "task1",
     *     "status": "Pending"
     *   }
     * ]
     */
    @GetMapping("/instances/{processInstanceId}/tasks")
    public List<Map<String, Object>> getTasksForProcessInstance(@PathVariable String processInstanceId) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskTenantId(TENANT_ID)
                .orderByTaskCreateTime().asc()
                .list();
        return tasks.stream().map(this::mapTaskToResponse).collect(Collectors.toList());
    }

    /**
     * Retrieves all active tasks for the "agpm" tenant, optionally filtered by assignee.
     *
     * @param assignee Optional assignee to filter tasks (query parameter).
     * @return List of maps containing task details (id, name, assignee, createTime, dueDate, processInstanceId, taskDefinitionKey, status).
     *         Returns 200 OK with an empty list if no tasks exist.
     *
     * Example Request: GET /api/process/tasks?assignee=john.doe
     * Example Response:
     * [
     *   {
     *     "id": "50001",
     *     "name": "First Task",
     *     "assignee": "john.doe",
     *     "createTime": "2025-04-23T10:15:30.000+00:00",
     *     "dueDate": null,
     *     "processInstanceId": "25001",
     *     "taskDefinitionKey": "task1",
     *     "status": "Pending"
     *   }
     * ]
     */
    @GetMapping("/tasks")
    public List<Map<String, Object>> getAllTasks(@RequestParam(required = false) String assignee) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskTenantId(TENANT_ID)
                .orderByTaskCreateTime().asc();
        if (assignee != null && !assignee.isEmpty()) {
            taskQuery.taskAssignee(assignee);
        }
        List<Task> tasks = taskQuery.list();
        return tasks.stream().map(this::mapTaskToResponse).collect(Collectors.toList());
    }

    /**
     * Retrieves details of a specific task by its ID in the "agpm" tenant.
     *
     * @param taskId The ID of the task (path variable).
     * @return ResponseEntity containing task details and variables (200 OK) or 404 Not Found if the task doesn't exist.
     *
     * Example Response:
     * {
     *   "id": "50001",
     *   "name": "First Task",
     *   "assignee": "john.doe",
     *   "createTime": "2025-04-23T10:15:30.000+00:00",
     *   "dueDate": null,
     *   "processInstanceId": "25001",
     *   "taskDefinitionKey": "task1",
     *   "status": "Pending",
     *   "variables": {
     *     "initiator": "john.doe"
     *   }
     * }
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskDetails(@PathVariable String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskTenantId(TENANT_ID)
                .singleResult();
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> response = mapTaskToResponse(task);
        Map<String, Object> variables = taskService.getVariables(taskId);
        response.put("variables", variables);
        return ResponseEntity.ok(response);
    }

    /**
     * Completes a task by its ID in the "agpm" tenant.
     *
     * @param taskId The ID of the task to complete (path variable).
     * @param variables A map of variables to pass during completion (JSON body).
     * @return ResponseEntity indicating success (200 OK) or an error message (400 Bad Request).
     *
     * Example Request Body:
     * {
     *   "approved": true
     * }
     * Example Response (Success):
     * {
     *   "status": "Task completed successfully"
     * }
     * Example Response (Error):
     * {
     *   "error": "Failed to complete task: Task not found"
     * }
     */
    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Map<String, String>> completeTask(@PathVariable String taskId,
                                                            @RequestBody Map<String, Object> variables) {
        try {
            taskService.complete(taskId, variables);
            return ResponseEntity.ok(Map.of("status", "Task completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to complete task: " + e.getMessage()));
        }
    }

    /**
     * Claims a task for a specific user in the "agpm" tenant.
     *
     * @param taskId The ID of the task to claim (path variable).
     * @param userId The ID of the user claiming the task (query parameter).
     * @return ResponseEntity indicating success (200 OK) or an error message (400 Bad Request).
     *
     * Example Request: POST /api/process/tasks/50001/claim?userId=john.doe
     * Example Response (Success):
     * {
     *   "status": "Task claimed successfully"
     * }
     * Example Response (Error):
     * {
     *   "error": "Failed to claim task: Task not found"
     * }
     */
    @PostMapping("/tasks/{taskId}/claim")
    public ResponseEntity<Map<String, String>> claimTask(@PathVariable String taskId,
                                                         @RequestParam String userId) {
        try {
            taskService.claim(taskId, userId);
            return ResponseEntity.ok(Map.of("status", "Task claimed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to claim task: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all completed tasks for the "agpm" tenant, optionally filtered by assignee.
     *
     * @param assignee Optional assignee to filter completed tasks (query parameter).
     * @return List of maps containing completed task details (id, name, assignee, createTime, endTime, dueDate, processInstanceId, taskDefinitionKey, status).
     *         Returns 200 OK with an empty list if no completed tasks exist.
     *
     * Example Request: GET /api/process/completed-tasks?assignee=john.doe
     * Example Response:
     * [
     *   {
     *     "id": "50001",
     *     "name": "First Task",
     *     "assignee": "john.doe",
     *     "createTime": "2025-04-23T10:15:30.000+00:00",
     *     "endTime": "2025-04-23T10:20:45.000+00:00",
     *     "dueDate": null,
     *     "processInstanceId": "25001",
     *     "taskDefinitionKey": "task1",
     *     "status": "Completed"
     *   }
     * ]
     */
    @GetMapping("/completedtasks")
    public List<Map<String, Object>> getCompletedTasks(@RequestParam(required = false) String assignee) {
        HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery()
                .taskTenantId(TENANT_ID)
                .finished()
                .orderByHistoricTaskInstanceEndTime().asc();
        if (assignee != null && !assignee.isEmpty()) {
            taskQuery.taskAssignee(assignee);
        }
        List<HistoricTaskInstance> tasks = taskQuery.list();
        return tasks.stream().map(this::mapHistoricTaskToResponse).collect(Collectors.toList());
    }

    /**
     * Maps an active Task to a response format.
     *
     * @param task The Task object to map.
     * @return A map containing task details (id, name, assignee, createTime, dueDate, processInstanceId, taskDefinitionKey, status).
     */
    private Map<String, Object> mapTaskToResponse(Task task) {
        Map<String, Object> taskInfo = new HashMap<>();
        taskInfo.put("id", task.getId());
        taskInfo.put("name", task.getName());
        taskInfo.put("assignee", task.getAssignee());
        taskInfo.put("createTime", task.getCreateTime());
        taskInfo.put("dueDate", task.getDueDate());
        taskInfo.put("processInstanceId", task.getProcessInstanceId());
        taskInfo.put("taskDefinitionKey", task.getTaskDefinitionKey());
        taskInfo.put("status", task.getDelegationState() != null ? task.getDelegationState().toString() : "Pending");
        return taskInfo;
    }

    /**
     * Maps a completed HistoricTaskInstance to a response format.
     *
     * @param task The HistoricTaskInstance object to map.
     * @return A map containing task details (id, name, assignee, createTime, endTime, dueDate, processInstanceId, taskDefinitionKey, status).
     */
    private Map<String, Object> mapHistoricTaskToResponse(HistoricTaskInstance task) {
        Map<String, Object> taskInfo = new HashMap<>();
        taskInfo.put("id", task.getId());
        taskInfo.put("name", task.getName());
        taskInfo.put("assignee", task.getAssignee());
        taskInfo.put("createTime", task.getCreateTime());
        taskInfo.put("endTime", task.getEndTime());
        taskInfo.put("dueDate", task.getDueDate());
        taskInfo.put("processInstanceId", task.getProcessInstanceId());
        taskInfo.put("taskDefinitionKey", task.getTaskDefinitionKey());
        taskInfo.put("status", "Completed");
        return taskInfo;
    }
}