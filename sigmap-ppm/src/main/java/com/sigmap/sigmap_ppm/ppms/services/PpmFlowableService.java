package com.sigmap.sigmap_ppm.ppms.services;




import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.response.PpmResponse;
import com.sigmap.common.request.ValidationRequest;
import com.sigmap.common.response.NextStepDTO;
import com.sigmap.common.response.ProcessStartResponseDTO;
import com.sigmap.common.response.TaskDTO;
import com.sigmap.common.response.TaskListResponseDTO;
import com.sigmap.sigmap_ppm.ppms.dto.TaskDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;




import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.FormProperty;
import com.sigmap.common.realisations.mappers.RealisationResponseMapper;
import com.sigmap.common.realisations.response.RealisationResponseDTO;



@Service
@Slf4j
public class PpmFlowableService {

    @Value("${tenant.id}")
    private String TENANT_ID;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private PpmService ppmService;


    @Value("${flowable.process-definition-key:ppmProcess}")
    private String processKey;

    @Value("${flowable.task-definition-key:saisieTask}")
    private String initialTaskKey;




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
    public ProcessStartResponseDTO startProcessInstance(Integer id, Integer version) {
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

        Ppm updated = ppmService.updateStatus(id, version, 2); // BROUILLON
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

        List<PpmResponse> ppmResponses = List.of();
        if (id != null && version != null) {
            Optional<Ppm> relatedPpms = ppmService.findById(id, version);
            ppmResponses = relatedPpms.stream()
                    .map(ppm -> {
                        PpmResponse ppmResponse = PpmMapper.INSTANCE.map(ppm);
                        // Always include realisations with their lots
                        if (ppm.getRealisations() != null) {
                            List<RealisationResponseDTO> realisationDTOs = ppm.getRealisations().stream()
                                    .map(RealisationResponseMapper.INSTANCE::map)
                                    .collect(Collectors.toList());
                            ppmResponse.setRealisations(realisationDTOs);
                        }
                        return ppmResponse;
                    })
                    .collect(Collectors.toList());
        }
        response.setPpms(ppmResponses);
        log.info("PPM Responses for task [{}]: {}", taskId, ppmResponses);

        Map<String, Object> variables = new HashMap<>();
        variables.put("decision", processVariables.getOrDefault("decision", ""));
        variables.put("comment", processVariables.getOrDefault("comment", ""));
        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
        variables.put("treater", processVariables.getOrDefault("treater", ""));
        response.setTraceability(variables);

        Map<String, Boolean> taskProperties = getTaskProperties(task);
        response.setUpdatable(taskProperties.get("isUpdatable"));
        response.setPublishable(taskProperties.get("isPublishable"));
        response.setStatus("Task details retrieved successfully");

        // Determine next steps based on validation outcomes
        List<NextStepDTO> nextSteps = new ArrayList<>();
        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            if (bpmnModel != null) {
                FlowElement taskElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                log.info("Task element for [{}]: {}", taskId, taskElement != null ? taskElement.getId() : "null");
                if (taskElement instanceof org.flowable.bpmn.model.UserTask) {
                    List<SequenceFlow> outgoingFlows = ((org.flowable.bpmn.model.UserTask) taskElement).getOutgoingFlows();
                    log.info("Outgoing flows for task [{}]: {}", taskId, outgoingFlows.size());
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
                                                taskId, variableName, conditionValue, nextStepName, nextStep.getActionLabel());
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
                            log.info("Added single next step for task [{}]: nextTask={}, action={}", taskId, nextStepName, "Complete");
                        }
                    }
                } else {
                    log.info("Task [{}] is not a UserTask: {}", taskId, taskElement != null ? taskElement.getClass().getSimpleName() : "null");
                }
            } else {
                log.info("BPMN model not found for process definition [{}] for task [{}]", task.getProcessDefinitionId(), taskId);
            }
        } catch (Exception e) {
            log.info("Failed to determine next steps for task [" + taskId + "]: " + e.getMessage());
            // Fallback to a generic complete action
            NextStepDTO nextStep = new NextStepDTO();
            nextStep.setValidationVariable(null);
            nextStep.setValidationValue(null);
            nextStep.setNextTaskName("Unknown");
            nextStep.setActionLabel("Complete");
            nextSteps.add(nextStep);
        }

        log.info("Setting nextSteps for task [{}]: {}", taskId, nextSteps);
        System.out.println("the next steps are" + nextSteps);
        response.setNextSteps(nextSteps);

        return response;
    }

    @Transactional
    public TaskDTO validateTask(String taskId, ValidationRequest request) {
        // Authenticate user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        // Fetch task
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

        // Get candidate group
        String candidateGroup = getCandidateGroup(task);
        if (candidateGroup == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("Task does not have a candidate group");
            return errorResponse;
        }

        // Check user permissions
        if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("User does not have the required role or assignment to validate this task");
            return errorResponse;
        }

        // Retrieve process variables
        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        log.info("Process variables for task [{}]: {}", taskId, processVariables);
        Integer id = (Integer) processVariables.getOrDefault("id", null);
        Integer version = (Integer) processVariables.getOrDefault("version", null);

        // Validate process variables
        if (id == null || version == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus(String.format("Missing required process variables: id=%s, version=%s", id, version));
            return errorResponse;
        }

        // Fetch Ppm entity
        Optional<Ppm> ppmOptional = ppmService.findById(id, version);
        if (ppmOptional.isEmpty()) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("No PPM entities found for id " + id + " and version " + version);
            return errorResponse;
        }

        Ppm latestPpm = ppmOptional.get();

        // Get task properties
        Map<String, Boolean> taskProperties = getTaskProperties(task);
        Boolean isUpdatable = taskProperties.get("isUpdatable");
        Boolean isPublishable = taskProperties.get("isPublishable");

        // Handle isUpdatable
        if (isUpdatable) {
            if (request.getPpm() == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Ppm object is required in the request when isUpdatable is true");
                return errorResponse;
            }
            Ppm updatedPpm = ppmService.update(id, version, request.getPpm(), isUpdatable);
            if (updatedPpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update PPM object");
                return errorResponse;
            }
            latestPpm = updatedPpm;
            version = updatedPpm.getVersion(); // Update the version to the new one
            // Update process variables with the new version
            processVariables.put("version", version);
            runtimeService.setVariables(task.getProcessInstanceId(), processVariables);
        }

        // Handle status update
        Integer newStatus = null;
        if (isPublishable) {
            newStatus = 3; // Publiée
            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
            if (updatedPpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update PPM status to Publiée");
                return errorResponse;
            }
            latestPpm = updatedPpm;
        }

        // Commented-out status update logic preserved

        else {
            switch (candidateGroup) {
                case "VALIDATEUR_AC":
                    newStatus = 5; // A_rectifiee
                    break;
                case "REDACTEUR_AC":
                    newStatus = 2; // A_VALIDERR
                    break;
                case "PUB_AC":
                    newStatus = 3; // Should not reach here since isPublishable handles this
                    break;
                default:
                    newStatus = latestPpm.getStatus().getId(); // Keep existing status
            }
            if (!isUpdatable) {
                Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
                if (updatedPpm == null) {
                    TaskDTO errorResponse = new TaskDTO();
                    errorResponse.setStatus("Failed to update PPM status");
                    return errorResponse;
                }
                latestPpm = updatedPpm;
            }
        }


        // Determine process variable name dynamically
        String processVariable = candidateGroup.toLowerCase() + "_valide"; // Fallback
        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            if (bpmnModel != null) {
                FlowElement taskElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                if (taskElement instanceof org.flowable.bpmn.model.UserTask) {
                    List<SequenceFlow> outgoingFlows = ((org.flowable.bpmn.model.UserTask) taskElement).getOutgoingFlows();
                    for (SequenceFlow flow : outgoingFlows) {
                        FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());
                        if (targetElement instanceof ExclusiveGateway) {
                            List<SequenceFlow> gatewayFlows = ((ExclusiveGateway) targetElement).getOutgoingFlows();
                            for (SequenceFlow gatewayFlow : gatewayFlows) {
                                String conditionExpression = gatewayFlow.getConditionExpression();
                                if (conditionExpression != null && conditionExpression.toLowerCase().contains("valide")) {
                                    String variableName = extractVariableName(conditionExpression);
                                    if (variableName != null && variableName.toLowerCase().contains("valide")) {
                                        processVariable = variableName;
                                        break;
                                    }
                                }
                            }
                            if (processVariable != null) break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("Failed to extract gateway condition: " + e.getMessage());
        }

        // Set process variables
        boolean validate = request.getIsValid() != null ? request.getIsValid() : false;
        Map<String, Object> variables = new HashMap<>();
        variables.put(processVariable, validate);
        variables.put("comment", request.getComment() != null ? request.getComment() : "");
        variables.put("dateComment", LocalDateTime.now());
        variables.put("treater", jwtAuth.getName());

        log.info("Setting process variables for task [{}]: {}={}, comment={}, dateComment={}, treater={}",
                taskId, processVariable, validate, request.getComment(), LocalDateTime.now(), jwtAuth.getName());

        runtimeService.setVariables(task.getProcessInstanceId(), variables);

        // Complete task
        taskService.complete(taskId);

        // Prepare response
        TaskDTO response = new TaskDTO();
        response.setTaskId(task.getId());
        response.setTaskName(task.getName());
        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
        response.setProcessInstanceId(task.getProcessInstanceId());
        response.setId(id);
        response.setVersion(version);
        response.setNewStatus(newStatus != null ? newStatus : latestPpm.getStatus().getId());
        response.setIsValid(validate);
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

            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
            taskInfo.setId((Integer) processVariables.getOrDefault("id", null));
            taskInfo.setVersion((Integer) processVariables.getOrDefault("version", null));
            taskInfo.setCandidateGroup(getCandidateGroup(task));

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


    // Helper method to extract variable name from condition expression
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
