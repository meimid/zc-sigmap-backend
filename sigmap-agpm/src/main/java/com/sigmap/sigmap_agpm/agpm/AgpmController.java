package com.sigmap.sigmap_agpm.agpm;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmAlreadyExistsException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
import com.sigmap.common.agpm.response.AgpmResponseDto;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.flowable.task.api.Task;
import jakarta.validation.Valid;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for managing Agpm entities.
 * Provides endpoints for CRUD operations on Agpm.
 */
@Slf4j
@RestController
@RequestMapping("/api/agpms")
public class AgpmController {

    @Value("${tenant.id}")
    private String TENANT_ID;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    private final AgpmService agpmService;

    public AgpmController(AgpmService agpmService) {
        this.agpmService = agpmService;
    }

    @GetMapping
    public ResponseEntity<Page<AgpmResponse>> getAllAgpms(AgpmQuery query, Pageable pageable) {
        Page<Agpm> agpmPage = agpmService.findAll(query.toSpecification(), pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        return new ResponseEntity<>(agpmResponsePage, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<AgpmResponse>> getAllDeletedAgpms(Pageable pageable) {
        Page<Agpm> agpmPage = agpmService.findAllDeleted(pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        return new ResponseEntity<>(agpmResponsePage, HttpStatus.OK);
    }

    @GetMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<AgpmResponse> getAgpmById(@PathVariable Integer agpmId, @PathVariable Integer agpmVer) {
        Optional<Agpm> agpm = agpmService.findById(agpmId, agpmVer);
        return agpm.map(a -> new ResponseEntity<>(AgpmMapper.INSTANCE.map(a), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<AgpmResponse> createAgpm(@Valid @RequestBody AgpmRequest request) throws AgpmAlreadyExistsException {
        Agpm createdAgpm = agpmService.create(request);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(createdAgpm);
        return new ResponseEntity<>(agpmResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<AgpmResponse> updateAgpm(@PathVariable Integer agpmId, @PathVariable Integer agpmVer, @Valid @RequestBody AgpmRequest request) throws AgpmNotFound {
        Agpm updatedAgpm = agpmService.update(agpmId, agpmVer, request);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(updatedAgpm);
        return new ResponseEntity<>(agpmResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<Void> deleteAgpm(@PathVariable Integer agpmId, @PathVariable Integer agpmVer) throws AgpmNotFound {
        agpmService.delete(agpmId, agpmVer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/status/{agpmId}/{agpmVer}/{statusId}")
    public ResponseEntity<AgpmResponse> updateAgpmStatus(@PathVariable Integer agpmId, @PathVariable Integer agpmVer, @PathVariable Integer statusId) throws AgpmNotFound {
        Agpm updated = agpmService.updateStatus(agpmId, agpmVer, statusId);
        AgpmResponse response = AgpmMapper.INSTANCE.map(updated);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/start/{id}/{version}")
//    public ResponseEntity<Map<String, String>> startProcessInstance(@PathVariable Integer id, @PathVariable Integer version) {
//        try {
//            Agpm updated = agpmService.updateStatus(id, version, 2); // BROUILLON
//            Map<String, Object> variables = new HashMap<>();
//            variables.put("id", id);
//            variables.put("version", version);
//            ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("agpmProcess", variables, TENANT_ID);
//            List<Task> tasks = taskService.createTaskQuery()
//                    .processInstanceId(processInstance.getId())
//                    .taskTenantId(TENANT_ID)
//                    .taskDefinitionKey("saisieTask")
//                    .active()
//                    .list();
//            if (tasks.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Saisie task not found for the process"));
//            }
//            if (tasks.size() > 1) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Multiple saisie tasks found"));
//            }
//            Task saisieTask = tasks.get(0);
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (!(authentication instanceof JwtAuthenticationToken)) {
//                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing JWT authentication"));
//            }
//            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//            List<String> candidateGroups = taskService.getIdentityLinksForTask(saisieTask.getId()).stream()
//                    .filter(link -> "candidate".equals(link.getType()))
//                    .map(link -> link.getGroupId())
//                    .collect(Collectors.toList());
//            boolean hasMatchingRole = jwtAuth.getAuthorities().stream()
//                    .map(authority -> authority.getAuthority())
//                    .anyMatch(auth -> candidateGroups.contains(auth.replace("ROLE_", "")));
//            if (!hasMatchingRole) {
//                return ResponseEntity.status(403).body(Map.of("error", "User does not have the required role (REDACTEUR_AC) to complete saisie task"));
//            }
//            taskService.complete(saisieTask.getId());
//            Map<String, String> response = new HashMap<>();
//            response.put("processInstanceId", processInstance.getId());
//            response.put("processDefinitionId", processInstance.getProcessDefinitionId());
//            response.put("taskId", saisieTask.getId());
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Failed to start process or complete saisie task: " + e.getMessage()));
//        }
//    }

    @PostMapping("/start/{id}/{version}")
    public ResponseEntity<Map<String, String>> startProcessInstance(@PathVariable Integer id, @PathVariable Integer version) {
        try {
            // Perform authentication and role check first
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing JWT authentication"));
            }
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            boolean hasRedacteurRole = jwtAuth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .anyMatch(auth -> auth.equals("ROLE_REDACTEUR_AC"));
            if (!hasRedacteurRole) {
                return ResponseEntity.status(403).body(Map.of("error", "User does not have the required role (REDACTEUR_AC) to start the process"));
            }

            // Proceed with starting the process only if the user has the required role
            Agpm updated = agpmService.updateStatus(id, version, 2); // BROUILLON
            if (updated == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Failed to update AGPM status to Brouillon"));
            }
            Map<String, Object> variables = new HashMap<>();
            variables.put("id", id);
            variables.put("version", version);
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("agpmProcess", variables, TENANT_ID);
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstance.getId())
                    .taskTenantId(TENANT_ID)
                    .taskDefinitionKey("saisieTask")
                    .active()
                    .list();
            if (tasks.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Saisie task not found for the process"));
            }
            if (tasks.size() > 1) {
                return ResponseEntity.badRequest().body(Map.of("error", "Multiple saisie tasks found"));
            }
            Task saisieTask = tasks.get(0);
            List<String> candidateGroups = taskService.getIdentityLinksForTask(saisieTask.getId()).stream()
                    .filter(link -> "candidate".equals(link.getType()))
                    .map(link -> link.getGroupId())
                    .collect(Collectors.toList());
            // Double-check the task's candidate group (redundant but ensures consistency)
            if (!candidateGroups.contains("REDACTEUR_AC")) {
                return ResponseEntity.status(403).body(Map.of("error", "Task does not belong to REDACTEUR_AC group"));
            }
            taskService.complete(saisieTask.getId());
            Map<String, String> response = new HashMap<>();
            response.put("processInstanceId", processInstance.getId());
            response.put("processDefinitionId", processInstance.getProcessDefinitionId());
            response.put("taskId", saisieTask.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to start process or complete saisie task: " + e.getMessage()));
        }
    }
    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getCurrentTasks() {
        try {
            List<Task> tasks = taskService.createTaskQuery()
                    .taskTenantId(TENANT_ID)
                    .active()
                    .orderByTaskCreateTime().asc()
                    .list();
            List<Map<String, String>> taskList = tasks.stream().map(task -> {
                Map<String, String> taskInfo = new HashMap<>();
                taskInfo.put("taskId", task.getId());
                taskInfo.put("taskName", task.getName());
                taskInfo.put("taskDefinitionKey", task.getTaskDefinitionKey());
                taskInfo.put("assignee", task.getAssignee() != null ? task.getAssignee() : "");
                taskInfo.put("processInstanceId", task.getProcessInstanceId());
                taskInfo.put("created", task.getCreateTime().toString());
                Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
                taskInfo.put("id", String.valueOf(processVariables.getOrDefault("id", "")));
                taskInfo.put("version", String.valueOf(processVariables.getOrDefault("version", "")));
                return taskInfo;
            }).collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("tenantId", TENANT_ID);
            response.put("tasks", taskList);
            response.put("status", "Tasks retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to retrieve tasks: " + e.getMessage()));
        }
    }

    @GetMapping("/tasks/redacteur")
    public ResponseEntity<Map<String, Object>> getRedacteurTasks() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing JWT authentication"));
            }
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            boolean hasRedacteurRole = jwtAuth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .anyMatch(auth -> auth.equals("ROLE_REDACTEUR_AC"));
            if (!hasRedacteurRole) {
                return ResponseEntity.status(403).body(Map.of("error", "User does not have the required role (REDACTEUR_AC) to view tasks"));
            }
            List<Task> tasks = taskService.createTaskQuery()
                    .taskTenantId(TENANT_ID)
                    .taskCandidateGroup("REDACTEUR_AC")
                    .active()
                    .orderByTaskCreateTime().asc()
                    .list();
            List<Map<String, String>> taskList = tasks.stream().map(task -> {
                Map<String, String> taskInfo = new HashMap<>();
                taskInfo.put("taskId", task.getId());
                taskInfo.put("taskName", task.getName());
                taskInfo.put("taskDefinitionKey", task.getTaskDefinitionKey());
                taskInfo.put("assignee", task.getAssignee() != null ? task.getAssignee() : "");
                taskInfo.put("processInstanceId", task.getProcessInstanceId());
                taskInfo.put("created", task.getCreateTime().toString());
                Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
                taskInfo.put("id", String.valueOf(processVariables.getOrDefault("id", "")));
                taskInfo.put("version", String.valueOf(processVariables.getOrDefault("version", "")));
                return taskInfo;
            }).collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("tenantId", TENANT_ID);
            response.put("tasks", taskList);
            response.put("status", "Tasks retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to retrieve redacteur tasks: " + e.getMessage()));
        }
    }

    @GetMapping("/tasks/validateur")
    public ResponseEntity<Map<String, Object>> getValidateurTasks() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing JWT authentication"));
            }
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            boolean hasValidateurRole = jwtAuth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .anyMatch(auth -> auth.equals("ROLE_VALIDATEUR_AC"));
            if (!hasValidateurRole) {
                return ResponseEntity.status(403).body(Map.of("error", "User does not have the required role (VALIDATEUR_AC) to view tasks"));
            }
            List<Task> tasks = taskService.createTaskQuery()
                    .taskTenantId(TENANT_ID)
                    .taskCandidateGroup("VALIDATEUR_AC")
                    .active()
                    .orderByTaskCreateTime().asc()
                    .list();
            List<Map<String, String>> taskList = tasks.stream().map(task -> {
                Map<String, String> taskInfo = new HashMap<>();
                taskInfo.put("taskId", task.getId());
                taskInfo.put("taskName", task.getName());
                taskInfo.put("taskDefinitionKey", task.getTaskDefinitionKey());
                taskInfo.put("assignee", task.getAssignee() != null ? task.getAssignee() : "");
                taskInfo.put("processInstanceId", task.getProcessInstanceId());
                taskInfo.put("created", task.getCreateTime().toString());
                Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
                taskInfo.put("id", String.valueOf(processVariables.getOrDefault("id", "")));
                taskInfo.put("version", String.valueOf(processVariables.getOrDefault("version", "")));
                return taskInfo;
            }).collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("tenantId", TENANT_ID);
            response.put("tasks", taskList);
            response.put("status", "Tasks retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to retrieve validateur tasks: " + e.getMessage()));
        }
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskDetails(@PathVariable String taskId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing JWT authentication"));
            }
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            boolean hasRedacteurRole = jwtAuth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .anyMatch(auth -> auth.equals("ROLE_REDACTEUR_AC"));
            boolean hasValidateurRole = jwtAuth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .anyMatch(auth -> auth.equals("ROLE_VALIDATEUR_AC"));
            if (!hasRedacteurRole && !hasValidateurRole) {
                return ResponseEntity.status(403).body(Map.of("error", "User does not have the required role (REDACTEUR_AC or VALIDATEUR_AC) to view task details"));
            }
            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .taskTenantId(TENANT_ID)
                    .active()
                    .singleResult();
            if (task == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Task not found or not active"));
            }
            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
            String candidateGroup = null;
            for (IdentityLink link : identityLinks) {
                if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
                    candidateGroup = link.getGroupId();
                    break;
                }
            }
            if (candidateGroup == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Task does not have a candidate group"));
            }
            if (candidateGroup.equals("REDACTEUR_AC") && !hasRedacteurRole) {
                return ResponseEntity.status(403).body(Map.of("error", "User does not have ROLE_REDACTEUR_AC to view this task"));
            }
            if (candidateGroup.equals("VALIDATEUR_AC") && !hasValidateurRole) {
                return ResponseEntity.status(403).body(Map.of("error", "User does not have ROLE_VALIDATEUR_AC to view this task"));
            }
            if (!candidateGroup.equals("REDACTEUR_AC") && !candidateGroup.equals("VALIDATEUR_AC")) {
                return ResponseEntity.status(403).body(Map.of("error", "Task does not belong to REDACTEUR_AC or VALIDATEUR_AC groups"));
            }
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("taskId", task.getId());
            taskInfo.put("taskName", task.getName());
            taskInfo.put("taskDefinitionKey", task.getTaskDefinitionKey());
            taskInfo.put("assignee", task.getAssignee() != null ? task.getAssignee() : "");
            taskInfo.put("processInstanceId", task.getProcessInstanceId());
            taskInfo.put("created", task.getCreateTime().toString());
            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
            Integer id = (Integer) processVariables.getOrDefault("id", null);
            Integer version = (Integer) processVariables.getOrDefault("version", null);
            List<AgpmResponseDto> agpmResponses = List.of();
            if (id != null && version != null) {
                Optional<Agpm> relatedAgpms = agpmService.findById(id, version);
                agpmResponses = relatedAgpms.stream()
                        .map(AgpmMapper.INSTANCE::mapToDto)
                        .collect(Collectors.toList());
            }
            taskInfo.put("agpms", agpmResponses);
            Map<String, Object> response = new HashMap<>();
            response.put("tenantId", TENANT_ID);
            response.put("task", taskInfo);
            response.put("status", "Task details retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to retrieve task details: " + e.getMessage()));
        }
    }

    @PostMapping("/tasks/{taskId}/validate")
    @Transactional
    public ResponseEntity<Map<String, Object>> validateTask(@PathVariable String taskId, @RequestBody Map<String, Boolean> requestBody) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing JWT authentication"));
            }
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            boolean hasValidateurRole = jwtAuth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .anyMatch(auth -> auth.equals("ROLE_VALIDATEUR_AC"));
            if (!hasValidateurRole) {
                return ResponseEntity.status(403).body(Map.of("error", "User does not have the required role (VALIDATEUR_AC) to validate tasks"));
            }
            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .taskTenantId(TENANT_ID)
                    .active()
                    .singleResult();
            if (task == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Task not found or not active"));
            }
            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
            String candidateGroup = null;
            for (IdentityLink link : identityLinks) {
                if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
                    candidateGroup = link.getGroupId();
                    break;
                }
            }
            if (candidateGroup == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Task does not have a candidate group"));
            }
            if (!candidateGroup.equals("VALIDATEUR_AC")) {
                return ResponseEntity.status(403).body(Map.of("error", "Task does not belong to VALIDATEUR_AC group"));
            }
            Boolean validate = requestBody.get("validate");
            if (validate == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Validation value 'validate' is required in the request body"));
            }
            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
            Integer id = (Integer) processVariables.getOrDefault("id", null);
            Integer version = (Integer) processVariables.getOrDefault("version", null);
            if (id == null) {
                return ResponseEntity.status(400).body(Map.of("error", "No associated AGPM ID found in process variables"));
            }
            Optional<Agpm> relatedAgpms = agpmService.findById(id, version);
            if (relatedAgpms.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "No AGPM entities found for id " + id));
            }
            Agpm latestAgpm = relatedAgpms.stream()
                    .max((a1, a2) -> Integer.compare(a1.getVersion(), a2.getVersion()))
                    .orElse(null);
            if (latestAgpm == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Could not determine the latest AGPM entity for id " + id));
            }
            if (validate) {
                Agpm updatedAgpm = agpmService.updateStatus(id, latestAgpm.getVersion(), 4); // A_PUBLIE
                if (updatedAgpm == null) {
                    return ResponseEntity.status(500).body(Map.of("error", "Failed to update AGPM status to À publiee"));
                }
                runtimeService.setVariable(task.getProcessInstanceId(), "valide", true);
            } else {
                Agpm updatedAgpm = agpmService.updateStatus(id, latestAgpm.getVersion(), 5); // A_RECTIFIEE
                if (updatedAgpm == null) {
                    return ResponseEntity.status(500).body(Map.of("error", "Failed to update AGPM status to À rectifiée"));
                }
                runtimeService.setVariable(task.getProcessInstanceId(), "valide", false);
            }
            taskService.complete(taskId);
            Optional<Agpm> updatedAgpms = agpmService.findById(id, version);
            List<AgpmResponseDto> agpmResponses = updatedAgpms.stream()
                    .map(AgpmMapper.INSTANCE::mapToDto)
                    .collect(Collectors.toList());
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("taskId", task.getId());
            taskInfo.put("taskName", task.getName());
            taskInfo.put("taskDefinitionKey", task.getTaskDefinitionKey());
            taskInfo.put("assignee", task.getAssignee() != null ? task.getAssignee() : "");
            taskInfo.put("processInstanceId", task.getProcessInstanceId());
            taskInfo.put("created", task.getCreateTime().toString());
            taskInfo.put("agpms", agpmResponses);
            Map<String, Object> response = new HashMap<>();
            response.put("tenantId", TENANT_ID);
            response.put("task", taskInfo);
            response.put("status", validate ? "Task validated and AGPM set to À publiee" : "Task rejected and AGPM set to À rectifiée");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to validate task with taskId {}", taskId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to validate task: " + e.getMessage()));
        }
    }

    @PostMapping("/tasks/{taskId}/rectify")
    @Transactional
    public ResponseEntity<Map<String, Object>> rectifyTask(@PathVariable String taskId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing JWT authentication"));
            }
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            boolean hasRedacteurRole = jwtAuth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .anyMatch(auth -> auth.equals("ROLE_REDACTEUR_AC"));
            if (!hasRedacteurRole) {
                return ResponseEntity.status(403).body(Map.of("error", "User does not have the required role (REDACTEUR_AC) to rectify tasks"));
            }
            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .taskTenantId(TENANT_ID)
                    .active()
                    .singleResult();
            if (task == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Task not found or not active"));
            }
            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
            String candidateGroup = null;
            for (IdentityLink link : identityLinks) {
                if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
                    candidateGroup = link.getGroupId();
                    break;
                }
            }
            if (candidateGroup == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Task does not have a candidate group"));
            }
            if (!candidateGroup.equals("REDACTEUR_AC")) {
                return ResponseEntity.status(403).body(Map.of("error", "Task does not belong to REDACTEUR_AC group"));
            }
            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
            Integer id = (Integer) processVariables.getOrDefault("id", null);
            Integer version = (Integer) processVariables.getOrDefault("version", null);
            if (id == null) {
                return ResponseEntity.status(400).body(Map.of("error", "No associated AGPM ID found in process variables"));
            }
            Optional<Agpm> relatedAgpms = agpmService.findById(id, version);
            if (relatedAgpms.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "No AGPM entities found for id " + id));
            }
            Agpm latestAgpm = relatedAgpms.stream()
                    .max((a1, a2) -> Integer.compare(a1.getVersion(), a2.getVersion()))
                    .orElse(null);
            if (latestAgpm == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Could not determine the latest AGPM entity for id " + id));
            }
            Agpm updatedAgpm = agpmService.updateStatus(id, latestAgpm.getVersion(), 2); // A_VALIDE
            if (updatedAgpm == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Failed to update AGPM status to À valide"));
            }
            taskService.complete(taskId);
            Optional<Agpm> updatedAgpms = agpmService.findById(id, version);
            List<AgpmResponseDto> agpmResponses = updatedAgpms.stream()
                    .map(AgpmMapper.INSTANCE::mapToDto)
                    .collect(Collectors.toList());
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("taskId", task.getId());
            taskInfo.put("taskName", task.getName());
            taskInfo.put("taskDefinitionKey", task.getTaskDefinitionKey());
            taskInfo.put("assignee", task.getAssignee() != null ? task.getAssignee() : "");
            taskInfo.put("processInstanceId", task.getProcessInstanceId());
            taskInfo.put("created", task.getCreateTime().toString());
            taskInfo.put("agpms", agpmResponses);
            Map<String, Object> response = new HashMap<>();
            response.put("tenantId", TENANT_ID);
            response.put("task", taskInfo);
            response.put("status", "Task rectified and AGPM set to À valide");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to rectify task with taskId {}", taskId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to rectify task: " + e.getMessage()));
        }
    }

    @PostMapping("/{agpmId}/{agpmVer}/publish")
    @Transactional
    public ResponseEntity<Map<String, Object>> publishAgpm(@PathVariable Integer agpmId, @PathVariable Integer agpmVer) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing JWT authentication"));
            }
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            boolean hasPubRole = jwtAuth.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .anyMatch(auth -> auth.equals("ROLE_PUB_AC"));
            if (!hasPubRole) {
                return ResponseEntity.status(403).body(Map.of("error", "User does not have the required role (ROLE_PUB_AC) to publish AGPM"));
            }
            Optional<Agpm> agpmOptional = agpmService.findById(agpmId, agpmVer);
            if (agpmOptional.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "AGPM not found for id " + agpmId + " and version " + agpmVer));
            }
            Agpm agpm = agpmOptional.get();
            if (agpm.getStatus() != null && agpm.getStatus().getId() == 3) { // PUBLIEE
                return ResponseEntity.status(400).body(Map.of("error", "AGPM with id " + agpmId + " and version " + agpmVer + " is already published"));
            }
            List<Task> tasks = taskService.createTaskQuery()
                    .taskTenantId(TENANT_ID)
                    .processVariableValueEquals("id", agpmId)
                    .processVariableValueEquals("version", agpmVer)
                    .active()
                    .list();
            if (tasks.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "No active task found for AGPM with id " + agpmId + " and version " + agpmVer));
            }
            if (tasks.size() > 1) {
                return ResponseEntity.status(400).body(Map.of("error", "Multiple active tasks found for AGPM with id " + agpmId + " and version " + agpmVer));
            }
            Task task = tasks.get(0);
            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
            String candidateGroup = null;
            for (IdentityLink link : identityLinks) {
                if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
                    candidateGroup = link.getGroupId();
                    break;
                }
            }
            if (candidateGroup == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Task does not have a candidate group"));
            }
            if (!candidateGroup.equals("PUB_AC")) {
                return ResponseEntity.status(403).body(Map.of("error", "Task does not belong to PUB_AC group"));
            }
            Agpm updatedAgpm = agpmService.updateStatus(agpmId, agpmVer, 3); // PUBLIEE
            if (updatedAgpm == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Failed to update AGPM status to Publiée"));
            }
            taskService.complete(task.getId());
            log.info("Task {} for AGPM {}/{} completed successfully during publishing", task.getId(), agpmId, agpmVer);
            Map<String, Object> response = new HashMap<>();
            response.put("agpm", AgpmMapper.INSTANCE.map(updatedAgpm));
            response.put("taskId", task.getId());
            response.put("status", "AGPM published and task completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to publish AGPM with id {} and version {}", agpmId, agpmVer, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to publish AGPM: " + e.getMessage()));
        }
    }
}