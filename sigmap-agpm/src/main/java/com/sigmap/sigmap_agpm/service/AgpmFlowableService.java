/**
 * Service class for managing Flowable processes related to Agpm entities.
 * This class handles process initiation, task management, validation, and retrieval of tasks
 * within the Flowable workflow engine, ensuring proper authorization and integration with Agpm entities.
 */
package com.sigmap.sigmap_agpm.service;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.response.AgpmResponseDto;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.sigmap_agpm.service.dto.request.ValidationRequest;
import com.sigmap.sigmap_agpm.service.dto.response.NextStepDTO;
import com.sigmap.sigmap_agpm.service.dto.response.ProcessStartResponseDTO;
import com.sigmap.sigmap_agpm.service.dto.response.TaskDTO;
import com.sigmap.sigmap_agpm.service.dto.response.TaskDetailsDTO;
import com.sigmap.sigmap_agpm.service.dto.response.TaskListResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for managing Flowable processes related to Agpm entities.
 * Handles process initiation, task management, and validation.
 */
@Slf4j
@Service
public class AgpmFlowableService {
    @Value("${tenant.id}")
    private String TENANT_ID;

    @Value("${flowable.process-definition-key}")
    private String processKey;

    @Value("${flowable.task-definition-key}")
    private String initialTaskKey;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AgpmService agpmService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    /**
     * Checks if the user has the specified role based on their JWT authentication token.
     *
     * @param jwtAuth The JWT authentication token
     * @param role    The role to check
     * @return True if the user has the role, false otherwise
     */
    private boolean hasRole(JwtAuthenticationToken jwtAuth, String role) {
        return jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_" + role));
    }

    /**
     * Checks if the task is assigned to the user based on their JWT authentication token.
     *
     * @param task    The Flowable task
     * @param jwtAuth The JWT authentication token
     * @return True if the task is assigned to the user, false otherwise
     */
    private boolean hasAssign(Task task, JwtAuthenticationToken jwtAuth) {
        return task.getAssignee() != null && task.getAssignee().equals(jwtAuth.getName());
    }

    /**
     * Checks if the user belongs to any candidate group of the task.
     *
     * @param task    The Flowable task
     * @param jwtAuth The JWT authentication token
     * @return True if the user is in a candidate group, false otherwise
     */
    private boolean hasCandidateGroup(Task task, JwtAuthenticationToken jwtAuth) {
        List<String> candidateGroups = taskService.getIdentityLinksForTask(task.getId()).stream()
                .filter(link -> "candidate".equals(link.getType()))
                .map(IdentityLink::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (candidateGroups.isEmpty()) {
            log.warn("No candidate groups found for task: {}", task.getId());
            return false;
        }

        return candidateGroups.stream().anyMatch(group -> hasRole(jwtAuth, group));
    }

    /**
     * Retrieves the candidate group for a task.
     *
     * @param task The Flowable task
     * @return The candidate group, or null if none exists
     */
    private String getCandidateGroup(Task task) {
        return taskService.getIdentityLinksForTask(task.getId()).stream()
                .filter(link -> "candidate".equals(link.getType()) && link.getGroupId() != null)
                .map(IdentityLink::getGroupId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if the user belongs to any candidate group defined in the task model.
     *
     * @param userTask The user task from the BPMN model
     * @param jwtAuth  The JWT authentication token
     * @return True if the user is in a candidate group, false otherwise
     */
    private boolean hasCandidateGroupFromModel(UserTask userTask, JwtAuthenticationToken jwtAuth) {
        List<String> candidateGroups = userTask.getCandidateGroups();
        if (candidateGroups == null || candidateGroups.isEmpty()) {
            log.warn("No candidate groups defined for task: {}", userTask.getId());
            return false;
        }

        return candidateGroups.stream().anyMatch(group -> hasRole(jwtAuth, group));
    }

    /**
     * Retrieves task properties (e.g., isUpdatable, isPublishable) from the BPMN model.
     *
     * @param task The Flowable task
     * @return A map containing task properties
     */
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

    /**
     * Starts a Flowable process instance for an Agpm entity using its ID, retrieving the latest version.
     *
     * @param id             The Agpm ID
     * @param processKey     The process definition key (optional)
     * @param initialTaskKey The initial task key (optional)
     * @return ProcessStartResponseDTO with the process details
     * @throws AgpmNotFound if the Agpm entity is not found
     */
    @Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id, String processKey, String initialTaskKey) throws AgpmNotFound {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        if (id == null) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("ID must not be null");
            return errorResponse;
        }

        // Retrieve the Agpm entity with the latest version
        Agpm agpm = agpmService.findById(id, null); // null for version as findById fetches the latest
        Integer version = agpm.getVersion();

        String effectiveProcessKey = (processKey != null && !processKey.isEmpty()) ? processKey : this.processKey;
        String effectiveTaskKey = (initialTaskKey != null && !initialTaskKey.isEmpty()) ? initialTaskKey : this.initialTaskKey;

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

        Agpm updated = agpmService.updateStatus(id, version, Constants.TRAITMENT_STATUS);
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", id);
        variables.put("version", version);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId(effectiveProcessKey, variables, TENANT_ID);
        String processInstanceId = processInstance.getProcessInstanceId();

        Optional<Agpm> agpmOptional = Optional.ofNullable(agpmService.findById(id, version));
        if (agpmOptional.isPresent()) {
            Agpm updatedAgpm = agpmOptional.get();
            updatedAgpm.setProcessInstanceId(processInstanceId);
            agpmService.save(updatedAgpm);
        }

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

    /**
     * Overloaded method to start a Flowable process instance for an Agpm entity using default process and task keys.
     *
     * @param id The Agpm ID
     * @return ProcessStartResponseDTO with the process details
     * @throws AgpmNotFound if the Agpm entity is not found
     */
    @Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id) throws AgpmNotFound {
        return startProcessInstance(id, null, null);
    }

    /**
     * Retrieves all active tasks for the tenant, including Agpm details where applicable.
     *
     * @return TaskListResponseDTO containing the list of tasks
     */
    public TaskListResponseDTO getCurrentTasks() {
        List<Task> tasks = taskService.createTaskQuery()
                .taskTenantId(TENANT_ID)
                .active()
                .orderByTaskCreateTime().asc()
                .list();

        List<TaskDTO> taskList = new ArrayList<>();
        for (Task task1 : tasks) {
            TaskDTO taskInfo = new TaskDTO();
            taskInfo.setTaskId(task1.getId());
            taskInfo.setTaskName(task1.getName());
            taskInfo.setTaskDefinitionKey(task1.getTaskDefinitionKey());
            taskInfo.setAssignee(task1.getAssignee() != null ? task1.getAssignee() : "");
            taskInfo.setProcessInstanceId(task1.getProcessInstanceId());
            taskInfo.setCreated(task1.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

            Map<String, Object> processVariables = runtimeService.getVariables(task1.getProcessInstanceId());
            Integer id = (Integer) processVariables.getOrDefault("id", null);
            Integer version = (Integer) processVariables.getOrDefault("version", null);

            if (id != null && version != null) {
                try {
                    Optional<Agpm> agpmOptional = Optional.ofNullable(agpmService.findById(id, version));
                    agpmOptional.ifPresent(agpm -> {
                        taskInfo.setId(id);
                        taskInfo.setVersion(version);
                        taskInfo.setNom(agpm.getNom());
                        taskInfo.setRef(agpm.getRef());
                    });
                } catch (AgpmNotFound e) {
                    log.warn("Agpm not found for id: {}, version: {} in task: {}", id, version, task1.getId(), e);
                }
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("decision", processVariables.getOrDefault("decision", ""));
            variables.put("comment", processVariables.getOrDefault("comment", ""));
            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
            variables.put("treater", processVariables.getOrDefault("treater", ""));
            taskInfo.setTraceability(variables);

            Map<String, Boolean> taskProperties = getTaskProperties(task1);
            taskInfo.setIsUpdatable(taskProperties.get("isUpdatable"));
            taskInfo.setIsPublishable(taskProperties.get("isPublishable"));

            TaskDTO apply = taskInfo;
            taskList.add(apply);
        }

        TaskListResponseDTO response = new TaskListResponseDTO();
        response.setTenantId(TENANT_ID);
        response.setTasks(taskList);
        response.setStatus("Tasks retrieved successfully");

        return response;
    }

    /**
     * Retrieves details of a specific task, including associated Agpm entities with non-deleted documents.
     *
     * @param taskId The ID of the task
     * @return TaskDetailsDTO with the task details
     * @throws AgpmNotFound if the associated Agpm entity is not found
     */
    public TaskDetailsDTO getTaskDetails(String taskId) throws AgpmNotFound {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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

        AgpmResponseDto agpmResponse = null;
        if (id != null && version != null) {
            Optional<Agpm> relatedAgpmOptional = Optional.ofNullable(agpmService.findById(id, version));
            if (relatedAgpmOptional.isPresent()) {
                Agpm agpm = relatedAgpmOptional.get();
                // Filter out deleted documents
                if (agpm.getDocs() != null) {
                    List<Docs> nonDeletedDocs = agpm.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .collect(Collectors.toList());
                    agpm.setDocs(nonDeletedDocs);
                }
                agpmResponse = AgpmMapper.INSTANCE.mapToDto(agpm);
            }
        }
        response.setAgpm(agpmResponse); // Update to accept a single AgpmResponseDto

        Map<String, Object> variables = new HashMap<>();
        variables.put("decision", processVariables.getOrDefault("decision", ""));
        variables.put("comment", processVariables.getOrDefault("comment", ""));
        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
        variables.put("treater", processVariables.getOrDefault("treater", ""));
        response.setTraceability(variables);

        Map<String, Boolean> taskProperties = getTaskProperties(task);
        response.setIsUpdatable(taskProperties.get("isUpdatable"));
        response.setIsPublishable(taskProperties.get("isPublishable"));

        List<NextStepDTO> nextSteps = getPossibleNextSteps(task);
        response.setNextSteps(nextSteps);

        response.setStatus("Task details retrieved successfully");

        return response;
    }

    /**
     * Determines the possible next steps for a task based on the BPMN model.
     *
     * @param task The Flowable task
     * @return List of possible next steps
     */
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
                                        if (nextElement instanceof org.flowable.bpmn.model.EndEvent) {
                                            nextStep.setActionLabel("Publier");
                                            log.info("Next element is an EndEvent, setting actionLabel to 'Publier' for task [{}]", task.getId());
                                        } else {
                                            nextStep.setActionLabel(nextStep.getValidationValue() ? "Validate" : "Reject");
                                        }
                                        nextSteps.add(nextStep);
                                        log.info("Added next step for task [{}]: variable={}, value={}, nextTask={}, action={}",
                                                task.getId(), variableName, conditionValue, nextStepName, nextStep.getActionLabel());
                                    }
                                }
                            }
                        } else {
                            String nextStepName = targetElement != null ? targetElement.getName() : "Complete";
                            if (nextStepName == null || nextStepName.isEmpty()) {
                                nextStepName = targetElement != null ? targetElement.getId() : "Complete";
                            }
                            NextStepDTO nextStep = new NextStepDTO();
                            nextStep.setValidationVariable(null);
                            nextStep.setValidationValue(null);
                            nextStep.setNextTaskName(nextStepName);
                            if (targetElement instanceof org.flowable.bpmn.model.EndEvent) {
                                nextStep.setActionLabel("Publier");
                                log.info("Target element is an EndEvent, setting actionLabel to 'Publier' for task [{}]", task.getId());
                            } else {
                                nextStep.setActionLabel("Passer pour validation");
                            }
                            nextSteps.add(nextStep);
                            log.info("Added single next step for task [{}]: nextTask={}, action={}", task.getId(), nextStepName, nextStep.getActionLabel());
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
            NextStepDTO nextStep = new NextStepDTO();
            nextStep.setValidationVariable(null);
            nextStep.setValidationValue(null);
            nextStep.setNextTaskName("Unknown");
            nextStep.setActionLabel("Complete");
            nextSteps.add(nextStep);
        }
        return nextSteps;
    }

    /**
     * Validates a task and updates the associated Agpm entity based on the validation request.
     *
     * @param taskId  The ID of the task to validate
     * @param request The validation request containing validation details
     * @return TaskDTO with the validation result
     * @throws AgpmNotFound if the Agpm entity is not found
     * @throws DocNotFound if a document is not found
     * @throws AcNotFound if the associated Ac entity is not found
     * @throws IOException if an error occurs during file processing
     */
    @Transactional
    public TaskDTO validateTask(String taskId, ValidationRequest request) throws AgpmNotFound, DocNotFound, AcNotFound, IOException {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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

        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        log.info("Process variables for task [{}]: {}", taskId, processVariables);
        Integer id = (Integer) processVariables.getOrDefault("id", null);
        Integer version = (Integer) processVariables.getOrDefault("version", null);

        if (id == null || version == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus(String.format("Missing required process variables: id=%s, version=%s", id, version));
            return errorResponse;
        }

        Optional<Agpm> agpmOptional = Optional.ofNullable(agpmService.findById(id, version));
        if (agpmOptional.isEmpty()) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("No AGPM entities found for id " + id + " and version " + version);
            return errorResponse;
        }

        Agpm latestAgpm = agpmOptional.get();

        Map<String, Boolean> taskProperties = getTaskProperties(task);
        Boolean isUpdatable = taskProperties.get("isUpdatable");
        Boolean isPublishable = taskProperties.get("isPublishable");

        if (isUpdatable) {
            if (request.getAgpm() == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("AGPM object is required in the request when isUpdatable is true");
                return errorResponse;
            }
            Agpm updatedAgpm = agpmService.updateWithVersioning(id, version, request.getAgpm());
            if (updatedAgpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update AGPM object");
                return errorResponse;
            }
            latestAgpm = updatedAgpm;
            version = latestAgpm.getVersion();
            processVariables.put("version", version);
            runtimeService.setVariables(task.getProcessInstanceId(), processVariables);
        }

        Integer newStatus = null;
        boolean validate = request.getValidate() != null ? request.getValidate() : false;
        if (validate) {
            newStatus = Constants.VALIDE_STATUS;
            Agpm updatedAgpm = agpmService.updateStatus(id, latestAgpm.getVersion(), newStatus);
            if (updatedAgpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update AGPM status to VALIDE");
                return errorResponse;
            }
            latestAgpm = updatedAgpm;
        } else if (!validate) {
            newStatus = Constants.TRAITMENT_STATUS;
            Agpm updatedAgpm = agpmService.updateStatus(id, latestAgpm.getVersion(), newStatus);
            if (updatedAgpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update AGPM status to EN_COURS_DE_TRAITEMENT");
                return errorResponse;
            }
            latestAgpm = updatedAgpm;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("valide", validate);
        variables.put("comment", request.getComment() != null ? request.getComment() : "");
        variables.put("dateComment", LocalDateTime.now());
        variables.put("treater", jwtAuth.getName());

        log.info("Setting process variables for task [{}]: valide={}, comment={}, dateComment={}, treater={}",
                taskId, validate, request.getComment(), LocalDateTime.now(), jwtAuth.getName());

        runtimeService.setVariables(task.getProcessInstanceId(), variables);
        taskService.complete(taskId);

        if (historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).finished().count() > 0) {
            newStatus = Constants.PUBLIE_STATUS;
            Agpm updatedAgpm = agpmService.updateStatus(id, latestAgpm.getVersion(), newStatus);
            if (updatedAgpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update AGPM status to PUBLIEE");
                return errorResponse;
            }
            latestAgpm = updatedAgpm;
        }

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

    /**
     * Retrieves tasks assigned to the current user based on their candidate groups.
     *
     * @return TaskListResponseDTO with the user's tasks
     */
    public TaskListResponseDTO getUserTasks() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            TaskListResponseDTO errorResponse = new TaskListResponseDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        List<String> userCandidateGroups = jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        if (userCandidateGroups.isEmpty()) {
            TaskListResponseDTO errorResponse = new TaskListResponseDTO();
            errorResponse.setStatus("User does not have any recognized roles");
            return errorResponse;
        }

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
            Integer id = (Integer) processVariables.getOrDefault("id", null);
            Integer version = (Integer) processVariables.getOrDefault("version", null);

            if (id != null && version != null) {
                try {
                    Optional<Agpm> agpmOptional = Optional.ofNullable(agpmService.findById(id, version));
                    agpmOptional.ifPresent(agpm -> {
                        taskInfo.setId(id);
                        taskInfo.setVersion(version);
                        taskInfo.setNom(agpm.getNom());
                        taskInfo.setRef(agpm.getRef());
                    });
                } catch (AgpmNotFound e) {
                    log.warn("Agpm not found for id: {}, version: {} in task: {}", id, version, task.getId(), e);
                }
            }

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

    /**
     * Extracts the condition value from a Flowable condition expression.
     *
     * @param conditionExpression The condition expression
     * @return The extracted condition value, or null if not found
     */
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

    /**
     * Extracts the variable name from a Flowable condition expression.
     *
     * @param conditionExpression The condition expression
     * @return The extracted variable name, or null if not found
     */
    private String extractVariableName(String conditionExpression) {
        if (conditionExpression == null) return null;
        String cleanedExpression = conditionExpression.trim().replace("#{", "${");
        Pattern pattern = Pattern.compile("\\$\\{([^\\s}]+?)\\s*(==|!=)\\s*(true|false|['\"][^'\"]+['\"])?\\}");
        Matcher matcher = pattern.matcher(cleanedExpression);
        if (matcher.find()) {
            String variableName = matcher.group(1).trim();
            log.info("Matched variable name: {}", variableName);
            return variableName;
        }
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