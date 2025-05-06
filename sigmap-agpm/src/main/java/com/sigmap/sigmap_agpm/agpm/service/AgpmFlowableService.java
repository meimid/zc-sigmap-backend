package com.sigmap.sigmap_agpm.agpm.service;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.response.AgpmResponseDto;
import com.sigmap.sigmap_agpm.agpm.request.ValidationRequest;
import com.sigmap.sigmap_agpm.agpm.response.NextStepDTO;
import com.sigmap.sigmap_agpm.agpm.response.ProcessStartResponseDTO;
import com.sigmap.sigmap_agpm.agpm.response.TaskDTO;
import com.sigmap.sigmap_agpm.agpm.response.TaskDetailsDTO;
import com.sigmap.sigmap_agpm.agpm.response.TaskListResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;

@Slf4j
@Service
public class AgpmFlowableService {
    @Value("${tenant.id:agpm}")
    private String TENANT_ID;

    @Value("${flowable.process.key:agpmProcess}")
    private String processKey;

    @Value("${flowable.initial.task.key:saisieTask}")
    private String initialTaskKey;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AgpmService agpmService;

    @Autowired
    private RepositoryService repositoryService;

    // Utility method to check if the user has a specific role
    private boolean hasRole(JwtAuthenticationToken jwtAuth, String role) {
        return jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_" + role));
    }

    // Utility method to check if the task is assigned to the current user
    private boolean hasAssign(Task task, JwtAuthenticationToken jwtAuth) {
        return task.getAssignee() != null && task.getAssignee().equals(jwtAuth.getName());
    }

    // Utility method to check if the task's candidate group matches any of the user's roles
    private boolean hasCandidateGroup(Task task, JwtAuthenticationToken jwtAuth) {
        List<String> candidateGroups = taskService.getIdentityLinksForTask(task.getId()).stream()
                .filter(link -> "candidate".equals(link.getType()))
                .map(IdentityLink::getGroupId)
                .filter(group -> group != null)
                .collect(Collectors.toList());

        if (candidateGroups.isEmpty()) {
            log.warn("No candidate groups found for task: {}", task.getId());
            return false;
        }

        return candidateGroups.stream().anyMatch(group -> hasRole(jwtAuth, group));
    }

    // Utility method to get the candidate group for a task
    private String getCandidateGroup(Task task) {
        return taskService.getIdentityLinksForTask(task.getId()).stream()
                .filter(link -> "candidate".equals(link.getType()) && link.getGroupId() != null)
                .map(IdentityLink::getGroupId)
                .findFirst()
                .orElse(null);
    }

    // Utility method to check if user's roles match the candidate groups defined in the BPMN model for a task
    private boolean hasCandidateGroupFromModel(UserTask userTask, JwtAuthenticationToken jwtAuth) {
        List<String> candidateGroups = userTask.getCandidateGroups();
        if (candidateGroups == null || candidateGroups.isEmpty()) {
            log.warn("No candidate groups defined for task: {}", userTask.getId());
            return false;
        }

        return candidateGroups.stream().anyMatch(group -> hasRole(jwtAuth, group));
    }

    // Updated method to retrieve both isUpdatable and isPublishable properties
    private Map<String, Boolean> getTaskProperties(Task task) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("isUpdatable", false);
        properties.put("isPublishable", false);

        try {
            String processDefinitionId = task.getProcessDefinitionId();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

            if (flowElement instanceof UserTask userTask) {
                List<FormProperty> formProperties = userTask.getFormProperties();
                log.info("Form properties for task [{}]: {}", task.getId(), formProperties.size());
                if (formProperties != null) {
                    for (FormProperty formProperty : formProperties) {
                        if ("isUpdatable".equals(formProperty.getId())) {
                            boolean isUpdatable = Boolean.parseBoolean(formProperty.getDefaultExpression());
                            properties.put("isUpdatable", isUpdatable);
                            log.info("Found form property isUpdatable = {}", isUpdatable);
                        }
                        if ("isPublishable".equals(formProperty.getId())) {
                            boolean isPublishable = Boolean.parseBoolean(formProperty.getDefaultExpression());
                            properties.put("isPublishable", isPublishable);
                            log.info("Found form property isPublishable = {}", isPublishable);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving properties for task [{}]: {}", task.getId(), e.getMessage(), e);
        }

        return properties;
    }

    @Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id, Integer version, String processKey, String initialTaskKey) throws AgpmNotFound {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        // Validate input parameters
        if (id == null || version == null) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("ID and version must not be null");
            return errorResponse;
        }

        // Use provided processKey or fallback to default
        String effectiveProcessKey = (processKey != null && !processKey.isEmpty()) ? processKey : this.processKey;
        String effectiveTaskKey = (initialTaskKey != null && !initialTaskKey.isEmpty()) ? initialTaskKey : this.initialTaskKey;

        // Pre-check the role by inspecting the BPMN model for the initial task's candidate groups
        BpmnModel bpmnModel = repositoryService.getBpmnModel(
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(effectiveProcessKey)
                        .latestVersion()
                        .singleResult()
                        .getId()
        );
        FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(effectiveTaskKey);
        if (!(flowElement instanceof UserTask)) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus(String.format("Task %s not found or not a user task in the process definition", effectiveTaskKey));
            return errorResponse;
        }

        UserTask initialTaskModel = (UserTask) flowElement;
        List<String> taskCandidateGroups = initialTaskModel.getCandidateGroups();
        if (taskCandidateGroups == null || taskCandidateGroups.isEmpty()) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus(String.format("No candidate groups defined for task %s in the process definition", effectiveTaskKey));
            return errorResponse;
        }

        // Get user's groups (roles without ROLE_ prefix)
        List<String> userGroups = jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        if (!hasCandidateGroupFromModel(initialTaskModel, jwtAuth)) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            String taskCandidateGroupsStr = String.join(", ", taskCandidateGroups);
            String userGroupsStr = userGroups.isEmpty() ? "none" : String.join(", ", userGroups);
            errorResponse.setStatus(String.format(
                    "User does not have the required role to start the process. " +
                            "Task candidate groups: [%s], Assigned user: none, User groups: [%s]",
                    taskCandidateGroupsStr, userGroupsStr));
            return errorResponse;
        }

        Agpm updated = agpmService.updateStatus(id, version, 2); // BROUILLON
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", id);
        variables.put("version", version);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId(effectiveProcessKey, variables, TENANT_ID);
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskTenantId(TENANT_ID)
                .taskDefinitionKey(effectiveTaskKey)
                .active()
                .list();
        if (tasks.isEmpty()) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus(String.format("Task %s not found for the process", effectiveTaskKey));
            return errorResponse;
        }
        if (tasks.size() > 1) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus(String.format("Multiple tasks with key %s found", effectiveTaskKey));
            return errorResponse;
        }

        Task initialTask = tasks.get(0);

        if (!hasCandidateGroup(initialTask, jwtAuth)) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            String taskCandidateGroupsStr = String.join(", ", taskCandidateGroups);
            String userGroupsStr = userGroups.isEmpty() ? "none" : String.join(", ", userGroups);
            String assignedUser = hasAssign(initialTask, jwtAuth) ? initialTask.getAssignee() : "none";
            errorResponse.setStatus(String.format(
                    "User does not have the required role to complete task %s. " +
                            "Task candidate groups: [%s], Assigned user: %s, User groups: [%s]",
                    effectiveTaskKey, taskCandidateGroupsStr, assignedUser, userGroupsStr));
            return errorResponse;
        }

        taskService.complete(initialTask.getId());

        ProcessStartResponseDTO response = new ProcessStartResponseDTO();
        response.setProcessInstanceId(processInstance.getId());
        response.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        response.setTaskId(initialTask.getId());
        response.setStatus(String.format("Process %s started and task %s completed successfully", effectiveProcessKey, effectiveTaskKey));

        return response;
    }

    // Overloaded method to use default processKey and initialTaskKey
    @Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id, Integer version) throws AgpmNotFound {
        return startProcessInstance(id, version, null, null);
    }

    public TaskListResponseDTO getCurrentTasks() {
        List<Task> tasks = taskService.createTaskQuery()
                .taskTenantId(TENANT_ID)
                .active()
                .orderByTaskCreateTime().asc()
                .list();

        List<TaskDTO> taskList = tasks.stream().map(task -> {
            TaskDTO taskInfo = new TaskDTO();
            taskInfo.setTaskId(task.getId());
            taskInfo.setTaskName(task.getName());
            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
            taskInfo.setId((Integer) processVariables.getOrDefault("id", null));
            taskInfo.setVersion((Integer) processVariables.getOrDefault("version", null));

            Map<String, Object> variables = new HashMap<>();
            variables.put("decision", processVariables.getOrDefault("decision", ""));
            variables.put("comment", processVariables.getOrDefault("comment", ""));
            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
            variables.put("treater", processVariables.getOrDefault("treater", ""));
            taskInfo.setTraceability(variables);

            Map<String, Boolean> taskProperties = getTaskProperties(task);
            taskInfo.setIsUpdatable(taskProperties.get("isUpdatable"));
            taskInfo.setIsPublishable(taskProperties.get("isPublishable"));



            return taskInfo;
        }).collect(Collectors.toList());

        TaskListResponseDTO response = new TaskListResponseDTO();
        response.setTenantId(TENANT_ID);
        response.setTasks(taskList);
        response.setStatus("Tasks retrieved successfully");

        return response;
    }

    public TaskDetailsDTO getTaskDetails(String taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

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

        String candidateGroup = getCandidateGroup(task);
        if (candidateGroup == null) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("Task does not have a candidate group");
            return errorResponse;
        }

        if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("User does not have the required role or assignment to view this task");
            return errorResponse;
        }

        TaskDetailsDTO response = new TaskDetailsDTO();
        response.setTaskId(task.getId());
        response.setTaskName(task.getName());
        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
        response.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
        response.setProcessInstanceId(task.getProcessInstanceId());
        response.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        response.setCandidateGroup(candidateGroup);

        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        Integer id = (Integer) processVariables.getOrDefault("id", null);
        Integer version = (Integer) processVariables.getOrDefault("version", null);

        List<AgpmResponseDto> agpmResponses = List.of();
        if (id != null && version != null) {
            Optional<Agpm> relatedagpms = agpmService.findById(id, version);
            agpmResponses = relatedagpms.stream()
                    .map(AgpmMapper.INSTANCE::mapToDto)
                    .collect(Collectors.toList());
        }
        response.setAgpms(agpmResponses);
        log.info("---------------: {}", agpmResponses.stream().map(AgpmResponseDto::toString).collect(Collectors.joining("\n")));

        Map<String, Object> variables = new HashMap<>();
        variables.put("decision", processVariables.getOrDefault("decision", ""));
        variables.put("comment", processVariables.getOrDefault("comment", ""));
        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
        variables.put("treater", processVariables.getOrDefault("treater", ""));
        response.setTraceability(variables);

        Map<String, Boolean> taskProperties = getTaskProperties(task);
        response.setIsUpdatable(taskProperties.get("isUpdatable"));
        response.setIsPublishable(taskProperties.get("isPublishable"));

        // Add possible next steps using the updated method
        List<NextStepDTO> nextSteps = getPossibleNextSteps(task);
        response.setNextSteps(nextSteps);

        response.setStatus("Task details retrieved successfully");

        return response;
    }

    // Updated method to determine possible next steps based on the BPMN model (from PpmFlowableService)
    private List<NextStepDTO> getPossibleNextSteps(Task task) {
        List<NextStepDTO> nextSteps = new ArrayList<>();
        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            if (bpmnModel != null) {
                FlowElement taskElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                log.info("Task element for [{}]: {}", task.getId(), taskElement != null ? taskElement.getId() : "null");
                if (taskElement instanceof org.flowable.bpmn.model.UserTask) {
                    List<SequenceFlow> outgoingFlows = ((org.flowable.bpmn.model.UserTask) taskElement).getOutgoingFlows();
                    log.info("Outgoing flows for task [{}]: {}", task.getId(), outgoingFlows.size());
                    for (SequenceFlow flow : outgoingFlows) {
                        FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());
                        log.info("Target element for flow [{}]: {}", flow.getId(), targetElement != null ? targetElement.getId() : "null");
                        if (targetElement instanceof ExclusiveGateway) {
                            List<SequenceFlow> gatewayFlows = ((ExclusiveGateway) targetElement).getOutgoingFlows();
                            log.info("Gateway flows for gateway [{}]: {}", targetElement.getId(), gatewayFlows.size());
                            for (SequenceFlow gatewayFlow : gatewayFlows) {
                                String conditionExpression = gatewayFlow.getConditionExpression();
                                log.info("Condition expression for gateway flow [{}]: {}", gatewayFlow.getId(), conditionExpression);
                                if (conditionExpression != null) {
                                    String variableName = extractVariableName(conditionExpression);
                                    String conditionValue = extractConditionValue(conditionExpression);
                                    log.info("Extracted variable: {}, value: {}", variableName, conditionValue);
                                    if (variableName != null && conditionValue != null) {
                                        FlowElement nextElement = bpmnModel.getFlowElement(gatewayFlow.getTargetRef());
                                        String nextStepName = nextElement != null ? nextElement.getName() : "Unknown";
                                        if (nextStepName == null || nextStepName.isEmpty()) {
                                            nextStepName = nextElement != null ? nextElement.getId() : "Unknown";
                                        }
                                        NextStepDTO nextStep = new NextStepDTO();
                                        nextStep.setValidationVariable(variableName);
                                        nextStep.setValidationValue(Boolean.parseBoolean(conditionValue));
                                        nextStep.setNextTaskName(nextStepName);
                                        nextStep.setActionLabel(nextStep.getValidationValue() ? "Validate" : "Reject");
                                        nextSteps.add(nextStep);
                                        log.info("Added next step for task [{}]: variable={}, value={}, nextTask={}, action={}",
                                                task.getId(), variableName, conditionValue, nextStepName, nextStep.getActionLabel());
                                    }
                                }
                            }
                        } else {
                            // Handle tasks with a single outgoing flow (no gateway)
                            String nextStepName = targetElement != null ? targetElement.getName() : "Complete";
                            if (nextStepName == null || nextStepName.isEmpty()) {
                                nextStepName = targetElement != null ? targetElement.getId() : "Complete";
                            }
                            NextStepDTO nextStep = new NextStepDTO();
                            nextStep.setValidationVariable(null);
                            nextStep.setValidationValue(null);
                            nextStep.setNextTaskName(nextStepName);
                            nextStep.setActionLabel("Complete");
                            nextSteps.add(nextStep);
                            log.info("Added single next step for task [{}]: nextTask={}, action={}", task.getId(), nextStepName, "Complete");
                        }
                    }
                } else {
                    log.info("Task [{}] is not a UserTask: {}", task.getId(), taskElement != null ? taskElement.getClass().getSimpleName() : "null");
                }
            } else {
                log.info("BPMN model not found for process definition [{}] for task [{}]", task.getProcessDefinitionId(), task.getId());
            }
        } catch (Exception e) {
            log.info("Failed to determine next steps for task [" + task.getId() + "]: " + e.getMessage());
            // Fallback to a generic complete action
            NextStepDTO nextStep = new NextStepDTO();
            nextStep.setValidationVariable(null);
            nextStep.setValidationValue(null);
            nextStep.setNextTaskName("Unknown");
            nextStep.setActionLabel("Complete");
            nextSteps.add(nextStep);
        }
        return nextSteps;
    }

    @Transactional
    public TaskDTO validateTask(String taskId, ValidationRequest request) throws AgpmNotFound {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

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

        String candidateGroup = getCandidateGroup(task);
        if (candidateGroup == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("Task does not have a candidate group");
            return errorResponse;
        }

        if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("User does not have the required role or assignment to validate this task");
            return errorResponse;
        }

        // Retrieve process variables and log them for debugging
        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        log.info("Process variables for task [{}]: {}", taskId, processVariables);
        Integer id = (Integer) processVariables.getOrDefault("id", null);
        Integer version = (Integer) processVariables.getOrDefault("version", null);

        // Strengthen validation for id and version
        if (id == null || version == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus(String.format("Missing required process variables: id=%s, version=%s", id, version));
            return errorResponse;
        }

        // Fetch the Agpm entity
        Optional<Agpm> agpmOptional = agpmService.findById(id, version);
        if (agpmOptional.isEmpty()) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("No AGPM entities found for id " + id + " and version " + version);
            return errorResponse;
        }

        Agpm latestAgpm = agpmOptional.get();

        // Get task properties (isUpdatable and isPublishable)
        Map<String, Boolean> taskProperties = getTaskProperties(task);
        Boolean isUpdatable = taskProperties.get("isUpdatable");
        Boolean isPublishable = taskProperties.get("isPublishable");

        // Handle isUpdatable: Update the Agpm object if true
        if (isUpdatable) {
            if (request.getAgpm() == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("AGPM object is required in the request when isUpdatable is true");
                return errorResponse;
            }
            Agpm updatedAgpm = agpmService.update(id, version, request.getAgpm());
            if (updatedAgpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update AGPM object");
                return errorResponse;
            }
            // Update the latestAgpm to reflect the updated state
            latestAgpm = updatedAgpm;
        }

        // Handle isPublishable: Update status to Publiée (status 3) if true
        Integer newStatus = null;
        if (isPublishable) {
            newStatus = 3; // Publiée
            Agpm updatedAgpm = agpmService.updateStatus(id, latestAgpm.getVersion(), newStatus);
            if (updatedAgpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update AGPM status to Publiée");
                return errorResponse;
            }
            latestAgpm = updatedAgpm;
        }

        boolean validate = request.getValidate() != null ? request.getValidate() : false;
        Map<String, Object> variables = new HashMap<>();
        variables.put("valide", validate);
        variables.put("comment", request.getComment() != null ? request.getComment() : "");
        variables.put("dateComment", LocalDateTime.now());
        variables.put("treater", jwtAuth.getName());

        // Log the variables for debugging
        log.info("Setting process variables for task [{}]: valide={}, comment={}, dateComment={}, treater={}",
                taskId, validate, request.getComment(), LocalDateTime.now(), jwtAuth.getName());

        runtimeService.setVariables(task.getProcessInstanceId(), variables);

        taskService.complete(taskId);

        TaskDTO response = new TaskDTO();
        response.setTaskId(task.getId());
        response.setTaskName(task.getName());
        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
        response.setProcessInstanceId(task.getProcessInstanceId());
        response.setId(id);
        response.setVersion(version);
        response.setNewStatus(newStatus != null ? newStatus : (latestAgpm.getStatus() != null ? latestAgpm.getStatus().getId() : null));
        response.setValidate(validate);
        response.setTraceability(variables);
        response.setIsUpdatable(isUpdatable);
        response.setIsPublishable(isPublishable);
        response.setStatus("Task validated successfully");

        return response;
    }

    public TaskListResponseDTO getUserTasks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            TaskListResponseDTO errorResponse = new TaskListResponseDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        // Get all candidate groups that match the user's roles
        List<String> userCandidateGroups = jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        if (userCandidateGroups.isEmpty()) {
            TaskListResponseDTO errorResponse = new TaskListResponseDTO();
            errorResponse.setStatus("User does not have any recognized roles");
            return errorResponse;
        }

        // Query tasks for all matching candidate groups
        List<Task> tasks = taskService.createTaskQuery()
                .taskTenantId(TENANT_ID)
                .taskCandidateGroupIn(userCandidateGroups)
                .active()
                .orderByTaskCreateTime().asc()
                .list();

        List<TaskDTO> taskList = tasks.stream().map(task -> {
            TaskDTO taskInfo = new TaskDTO();
            taskInfo.setTaskId(task.getId());
            taskInfo.setTaskName(task.getName());
            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            taskInfo.setCandidateGroup(getCandidateGroup(task));

            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
            taskInfo.setId((Integer) processVariables.getOrDefault("id", null));
            taskInfo.setVersion((Integer) processVariables.getOrDefault("version", null));

            Map<String, Object> variables = new HashMap<>();
            variables.put("decision", processVariables.getOrDefault("decision", ""));
            variables.put("comment", processVariables.getOrDefault("comment", ""));
            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
            variables.put("treater", processVariables.getOrDefault("treater", ""));
            taskInfo.setTraceability(variables);

            Map<String, Boolean> taskProperties = getTaskProperties(task);
            taskInfo.setIsUpdatable(taskProperties.get("isUpdatable"));
            taskInfo.setIsPublishable(taskProperties.get("isPublishable"));


            return taskInfo;
        }).collect(Collectors.toList());

        TaskListResponseDTO response = new TaskListResponseDTO();
        response.setTenantId(TENANT_ID);
        response.setTasks(taskList);
        response.setCandidateGroup(userCandidateGroups.isEmpty() ? null : String.join(", ", userCandidateGroups));
        response.setStatus("Tasks retrieved successfully");

        return response;
    }

    // Helper method to extract condition value from condition expression (from PpmFlowableService)
    private String extractConditionValue(String conditionExpression) {
        if (conditionExpression == null) return null;
        String cleanedExpression = conditionExpression.trim().replace("#{", "${");
        Pattern pattern = Pattern.compile("\\$\\{[^\\s}]+?\\s*(==|!=)\\s*(true|false)\\}");
        Matcher matcher = pattern.matcher(cleanedExpression);
        if (matcher.find()) {
            String value = matcher.group(2).trim();
            log.info("Matched condition value: {}", value);
            return value;
        }
        log.info("No condition value matched in expression: {}", conditionExpression);
        return null;
    }

    // Helper method to extract variable name from condition expression (from PpmFlowableService)
    private String extractVariableName(String conditionExpression) {
        if (conditionExpression == null) return null;
        // Handle expressions like ${variable==true}, ${variable == false}, #{variable==true}, etc.
        String cleanedExpression = conditionExpression.trim().replace("#{", "${");
        // Regex to match variable names in expressions like ${variable==value} or ${variable == value}
        Pattern pattern = Pattern.compile("\\$\\{([^\\s}]+?)\\s*(==|!=)\\s*(true|false|['\"][^'\"]+['\"])?\\}");
        Matcher matcher = pattern.matcher(cleanedExpression);
        if (matcher.find()) {
            String variableName = matcher.group(1).trim();
            log.info("Matched variable name: {}", variableName);
            return variableName;
        }
        // Fallback for simpler expressions like ${variable}
        if (cleanedExpression.startsWith("${") && cleanedExpression.endsWith("}")) {
            String variableName = cleanedExpression.substring(2, cleanedExpression.length() - 1).trim();
            if (!variableName.isEmpty()) {
                log.info("Matched simple variable name: {}", variableName);
                return variableName;
            }
        }
        log.info("No variable name matched in expression: {}", conditionExpression);
        return null;
    }
}