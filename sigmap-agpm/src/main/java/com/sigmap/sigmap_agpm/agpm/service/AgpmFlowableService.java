package com.sigmap.sigmap_agpm.agpm.service;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.response.AgpmResponseDto;
import com.sigmap.sigmap_agpm.agpm.request.ValidationRequest;
import com.sigmap.sigmap_agpm.agpm.response.ProcessStartResponseDTO;
import com.sigmap.sigmap_agpm.agpm.response.TaskDTO;
import com.sigmap.sigmap_agpm.agpm.response.TaskDetailsDTO;
import com.sigmap.sigmap_agpm.agpm.response.TaskListResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.flowable.engine.TaskService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
@Slf4j

@Service
public class AgpmFlowableService {
    @Value("${tenant.id}")
    private String TENANT_ID;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AgpmService agpmService;

    @Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id, Integer version) throws AgpmNotFound {
        // Proceed with starting the process only if the user has the required role
        Agpm updated = agpmService.updateStatus(id, version, 2); // BROUILLON
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
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("Saisie task not found for the process");
            return errorResponse;
        }

        if (tasks.size() > 1) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("Multiple saisie tasks found");
            return errorResponse;
        }

        Task saisieTask = tasks.get(0);

        // Get user roles from JwtAuthenticationToken
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        // Get candidate groups for the task
        List<String> candidateGroups = taskService.getIdentityLinksForTask(saisieTask.getId()).stream()
                .filter(link -> "candidate".equals(link.getType()))
                .map(IdentityLink::getGroupId)
                .collect(Collectors.toList());

        // Check if user has the required role (REDACTEUR_AC)
        boolean hasMatchingRole = jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(auth -> candidateGroups.contains(auth.replace("ROLE_", "")));

        if (!hasMatchingRole) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("User does not have the required role (REDACTEUR_AC) to complete saisie task");
            return errorResponse;
        }

        // Complete the saisieTask
        taskService.complete(saisieTask.getId());

        // Prepare response
        ProcessStartResponseDTO response = new ProcessStartResponseDTO();
        response.setProcessInstanceId(processInstance.getId());
        response.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        response.setTaskId(saisieTask.getId());
        response.setStatus("Process started and saisie task completed successfully");

        return response;
    }


    public TaskListResponseDTO getCurrentTasks() {
        // Fetch active tasks for the tenant ID
        List<Task> tasks = taskService.createTaskQuery()
                .taskTenantId(TENANT_ID)
                .active()
                .orderByTaskCreateTime().asc()
                .list();

        // Prepare task list
        List<TaskDTO> taskList = tasks.stream().map(task -> {
            TaskDTO taskInfo = new TaskDTO();
            taskInfo.setTaskId(task.getId());
            taskInfo.setTaskName(task.getName());
            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

            // Retrieve id and version from process instance variables
            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
            taskInfo.setId((Integer) processVariables.getOrDefault("id", null));
            taskInfo.setVersion((Integer) processVariables.getOrDefault("version", null));

            // Add traceability variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("decision", processVariables.getOrDefault("decision", ""));
            variables.put("comment", processVariables.getOrDefault("comment", ""));
            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
            variables.put("treater", processVariables.getOrDefault("treater", ""));
            taskInfo.setTraceability(variables);

            return taskInfo;
        }).collect(Collectors.toList());

        // Prepare response
        TaskListResponseDTO response = new TaskListResponseDTO();
        response.setTenantId(TENANT_ID);
        response.setTasks(taskList);
        response.setStatus("Tasks retrieved successfully");

        return response;
    }

    public TaskDetailsDTO getTaskDetails(String taskId) {
        // Get user roles from JwtAuthenticationToken
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        // Check user roles
        boolean hasRedacteurRole = jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_REDACTEUR_AC"));
        boolean hasValidateurRole = jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_VALIDATEUR_AC"));
        if (!hasRedacteurRole && !hasValidateurRole ) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("User does not have the required role (REDACTEUR_AC or VALIDATEUR_AC) to view task details");
            return errorResponse;
        }

        // Fetch the task by taskId
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskTenantId(TENANT_ID)
                .active()
                .singleResult();

        if (task == null) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("Task not found or not active");
            return errorResponse;
        }

        // Determine the task's candidate group using TaskService
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
        String candidateGroup = null;
        for (IdentityLink link : identityLinks) {
            if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
                candidateGroup = link.getGroupId();
                break;
            }
        }

        if (candidateGroup == null) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("Task does not have a candidate group");
            return errorResponse;
        }

        // Verify role matches the candidate group
        if (candidateGroup.equals("REDACTEUR_AC") && !hasRedacteurRole) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("User does not have ROLE_REDACTEUR_AC to view this task");
            return errorResponse;
        }
        if (candidateGroup.equals("VALIDATEUR_AC") && !hasValidateurRole) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("User does not have ROLE_VALIDATEUR_AC to view this task");
            return errorResponse;
        }

        if (!candidateGroup.equals("REDACTEUR_AC") && !candidateGroup.equals("VALIDATEUR_AC") ) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("Task does not belong to REDACTEUR_AC or VALIDATEUR_AC groups");
            return errorResponse;
        }

        // Prepare task details
        TaskDetailsDTO response = new TaskDetailsDTO();
        response.setTaskId(task.getId());

        response.setTaskName(task.getName());
        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
        response.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
        response.setProcessInstanceId(task.getProcessInstanceId());
        response.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        response.setCandidateGroup(candidateGroup);

        // Retrieve id from process instance variables
        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        Integer id = (Integer) processVariables.getOrDefault("id", null);
        Integer version = (Integer) processVariables.getOrDefault("version", null);

        // Fetch all agpm entities related to this task (by id) and map to agpmResponse
        List<AgpmResponseDto> agpmResponses = List.of();
        if (id != null && version != null) {
            Optional<Agpm> relatedagpms = agpmService.findById(id, version);
            agpmResponses = relatedagpms.stream()
                    .map(AgpmMapper.INSTANCE::mapToDto)
                    .collect(Collectors.toList());
        }
        response.setAgpms(agpmResponses);
        log.info("---------------: {}", agpmResponses.stream().map(AgpmResponseDto::toString).collect(Collectors.joining("\n")));

        // Add traceability variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("decision", processVariables.getOrDefault("decision", ""));
        variables.put("comment", processVariables.getOrDefault("comment", ""));
        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
        variables.put("treater", processVariables.getOrDefault("treater", ""));
        response.setTraceability(variables);

        response.setStatus("Task details retrieved successfully");

        return response;
    }


    @Transactional
    public TaskDTO validateTask(String taskId, ValidationRequest request) throws AgpmNotFound{
        // Get user roles from JwtAuthenticationToken
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        // Fetch the task by taskId
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskTenantId(TENANT_ID)
                .active()
                .singleResult();

        if (task == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("Task not found or not active");
            return errorResponse;
        }

        // Determine the task's candidate group
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
        String candidateGroup = null;
        for (IdentityLink link : identityLinks) {
            if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
                candidateGroup = link.getGroupId();
                break;
            }
        }

        if (candidateGroup == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("Task does not have a candidate group");
            return errorResponse;
        }

        // Check user roles and validate against candidate group
        boolean hasValidRole = false;
        String requiredRole = null;
        String userRole = candidateGroup;

        if ("VALIDATEUR_AC".equals(userRole)) {
            hasValidRole = jwtAuth.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_VALIDATEUR_AC"));
            requiredRole = "ROLE_VALIDATEUR_AC";
        } else if ("REDACTEUR_AC".equals(userRole)) {
            hasValidRole = jwtAuth.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_REDACTEUR_AC"));
            requiredRole = "ROLE_REDACTEUR_AC";
        } else if ("PUB_AC".equals(userRole)) {
            hasValidRole = jwtAuth.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_PUB_AC"));
            requiredRole = "ROLE_PUB_AC";
        } else {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("Invalid role");
            return errorResponse;
        }

        if (!hasValidRole) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("User does not have the required role (" + requiredRole + ") to validate this task");
            return errorResponse;
        }

        // Get agpm details from process variables
        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        Integer id = (Integer) processVariables.getOrDefault("id", null);
        Integer version = (Integer) processVariables.getOrDefault("version", null);

        if (id == null || version == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("No associated agpm ID found in process variables");
            return errorResponse;
        }

        // Get the latest agpm entity
        Optional<Agpm> agpmOptional = agpmService.findById(id, version);
        if (agpmOptional.isEmpty()) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("No AGPM entities found for id " + id);
            return errorResponse;
        }

        Agpm latestAgpm = agpmOptional.get();

        // Handle validation based on candidate group
        boolean validate = request.getValidate();
        Integer newStatus = null;

        switch (candidateGroup) {
            case "VALIDATEUR_AC":
                newStatus = 5; // A_rectifiee
                break;
            case "REDACTEUR_AC":
                newStatus = 2; // A_VALIDERR
                break;
            case "PUB_AC":
                newStatus = 3; // Publiée
                break;
        }

        // Update agpm status
        Agpm updatedAgpm = agpmService.updateStatus(id, latestAgpm.getVersion(), newStatus);
        if (updatedAgpm == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("Failed to update agpm status");
            return errorResponse;
        }


        // Add traceability variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("valide", request.getValidate());
        variables.put("comment", request.getComment());
        variables.put("dateComment", LocalDateTime.now());
        variables.put("treater", jwtAuth.getName());
        runtimeService.setVariables(task.getProcessInstanceId(), variables);

        // Complete the task
        taskService.complete(taskId);

        // Prepare response
        TaskDTO response = new TaskDTO();
        response.setTaskId(task.getId());
        response.setTaskName(task.getName());
        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
        response.setProcessInstanceId(task.getProcessInstanceId());
        response.setId(id);
        response.setVersion(version);
        response.setNewStatus(newStatus);
        response.setValidate(validate);
        response.setTraceability(variables);
        response.setStatus("Task validated successfully");

        return response;
    }

    public TaskListResponseDTO getUserTasks() {
        // Get user roles from JwtAuthenticationToken
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            TaskListResponseDTO errorResponse = new TaskListResponseDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        // Get user roles
        List<String> userRoles = jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());

        // Map roles to candidate groups
        Map<String, String> roleToGroup = new HashMap<>();
        roleToGroup.put("ROLE_REDACTEUR_AC", "REDACTEUR_AC");
        roleToGroup.put("ROLE_VALIDATEUR_AC", "VALIDATEUR_AC");
        roleToGroup.put("ROLE_PUB_AC", "PUB_AC");

        // Find the matching role and candidate group
        String candidateGroup = null;
        for (String role : userRoles) {
            if (roleToGroup.containsKey(role)) {
                candidateGroup = roleToGroup.get(role);
                break;
            }
        }

        if (candidateGroup == null) {
            TaskListResponseDTO errorResponse = new TaskListResponseDTO();
            errorResponse.setStatus("User does not have any of the required roles (REDACTEUR_AC, VALIDATEUR_AC, PUB_AC)");
            return errorResponse;
        }

        // Make candidateGroup effectively final
        final String finalCandidateGroup = candidateGroup;

        // Fetch active tasks for tenant ID with the matching candidate group
        List<Task> tasks = taskService.createTaskQuery()
                .taskTenantId(TENANT_ID)
                .taskCandidateGroup(finalCandidateGroup)
                .active()
                .orderByTaskCreateTime().asc()
                .list();

        // Prepare task list
        List<TaskDTO> taskList = tasks.stream().map(task -> {
            TaskDTO taskInfo = new TaskDTO();
            taskInfo.setTaskId(task.getId());
            taskInfo.setTaskName(task.getName());
            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

            // Retrieve id and version from process instance variables
            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
            taskInfo.setId((Integer) processVariables.getOrDefault("id", null));
            taskInfo.setVersion((Integer) processVariables.getOrDefault("version", null));
            taskInfo.setCandidateGroup(finalCandidateGroup);

            // Add traceability variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("decision", processVariables.getOrDefault("decision", ""));
            variables.put("comment", processVariables.getOrDefault("comment", ""));
            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
            variables.put("treater", processVariables.getOrDefault("treater", ""));
            taskInfo.setTraceability(variables);

            return taskInfo;
        }).collect(Collectors.toList());

        // Prepare response
        TaskListResponseDTO response = new TaskListResponseDTO();
        response.setTenantId(TENANT_ID);
        response.setTasks(taskList);
        response.setCandidateGroup(candidateGroup);
        response.setStatus("Tasks retrieved successfully");

        return response;
    }
}