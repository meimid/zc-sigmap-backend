package com.sigmap.sigmap_ppm.services;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComServiceImpl;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.*;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.realisations.request.RealisationWithLotsRequestDto;
import com.sigmap.sigmap_ppm.services.dto.request.ValidationRequest;
import com.sigmap.sigmap_ppm.services.dto.response.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
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

@Service
@Slf4j
public class PpmFlowableService {

    @Value("${tenant.id}")
    private String TENANT_ID;

    @Value("${flowable.process-definition-key:ppmProcess}")
    private String processKey;

    @Value("${flowable.task-definition-key:saisieTask}")
    private String initialTaskKey;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private PpmService ppmService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ComServiceImpl comServiceImpl;

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

    // Utility method to check if user's roles match the candidate groups defined in the BPMN model
    private boolean hasCandidateGroupFromModel(UserTask userTask, JwtAuthenticationToken jwtAuth) {
        List<String> candidateGroups = userTask.getCandidateGroups();
        if (candidateGroups == null || candidateGroups.isEmpty()) {
            log.warn("No candidate groups defined for task: {}", userTask.getId());
            return false;
        }

        return candidateGroups.stream().anyMatch(group -> hasRole(jwtAuth, group));
    }

    // Method to retrieve task properties (isUpdatable, isPublishable, commissionRequired)
    private Map<String, Boolean> getTaskProperties(Task task) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("isUpdatable", false);
        properties.put("isPublishable", false);
        properties.put("commissionRequired", false);

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
                        if ("commissionRequired".equals(formProperty.getId())) {
                            boolean commissionRequired = Boolean.parseBoolean(formProperty.getDefaultExpression());
                            properties.put("commissionRequired", commissionRequired);
                            log.info("Found form property commissionRequired = {}", commissionRequired);
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
    public ProcessStartResponseDTO startProcessInstance(Integer id, Integer version) throws PpmNotFound, AcNotFound {
        // Validate authentication
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

        // Fetch PPM entity to check status
        Optional<Ppm> ppmOptional = ppmService.findById(id, version);
        if (ppmOptional.isEmpty()) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("No PPM entity found for id " + id + " and version " + version);
            return errorResponse;
        }
        Ppm ppm = ppmOptional.get();
        if (ppm.getStatus() == null || ppm.getStatus() != ProcessStatus.BROUILLON) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("Process can only be started when PPM is in draft status. Current status ID: " +
                    (ppm.getStatus() != null ? ppm.getStatus() : "null"));
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

        // Update PPM status to TRAITMENT_STATUS
        Ppm updated = ppmService.updateStatus(id, version, ProcessStatus.EN_TRAITEMENT);
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", id);
        variables.put("version", version);
        // Add the AC ID from the Ppm entity
        Integer acId = ppm.getAc() != null ? ppm.getAc().getId() : null;
        if (acId != null) {
            variables.put("acId", acId);
        }

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId(effectiveProcessKey, variables, TENANT_ID);
        String processInstanceId = processInstance.getProcessInstanceId();

        // Set the processInstanceId on the PPM entity
        Optional<Ppm> ppmOptionalUpdated = ppmService.findById(id, version);
        if (ppmOptionalUpdated.isPresent()) {
            Ppm updatedPpm = ppmOptionalUpdated.get();
            updatedPpm.setProcessInstanceId(processInstanceId);
            ppmService.save(updatedPpm);
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
        response.setAcId(acId);
        response.setStatus(String.format("Process %s started and task %s completed successfully", effectiveProcessKey, effectiveTaskKey));

        return response;
    }

    /**
     * Overloaded method to start a Flowable process instance for a Ppm entity using default process and task keys.
     *
     * @param id The Ppm ID
     * @return ProcessStartResponseDTO with the process details
     * @throws PpmNotFound if the Ppm entity is not found
     */
    @jakarta.transaction.Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id) throws PpmNotFound, AcNotFound {
        Integer latestVersion = ppmService.findLatestVersionById(id);
        return startProcessInstance(id, latestVersion);
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
            Integer id = (Integer) processVariables.getOrDefault("id", null);
            Integer version = (Integer) processVariables.getOrDefault("version", null);
            log.info("Process variables for {} : {}", id, version);

            if (id != null && version != null) {
                Optional<Ppm> ppmOptional = null;
                try {
                    ppmOptional = ppmService.findById(id, version);
                } catch (PpmNotFound e) {
                    throw new RuntimeException(e);
                } catch (AcNotFound e) {
                    throw new RuntimeException(e);
                }
                ppmOptional.ifPresent(ppm -> {
                    taskInfo.setId(id);
                    taskInfo.setVersion(version);
                    taskInfo.setNom(ppm.getNom());
                    taskInfo.setRef(ppm.getRef());
                });
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
            taskInfo.setCommissionRequired(taskProperties.get("commissionRequired"));

            return taskInfo;
        }).collect(Collectors.toList());

        TaskListResponseDTO response = new TaskListResponseDTO();
        response.setTenantId(TENANT_ID);
        response.setTasks(taskList);
        response.setStatus("Tasks retrieved successfully");

        return response;
    }

    public TaskDetailsDTO getTaskDetails(String taskId) throws PpmNotFound, AcNotFound {
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

        PpmResponseDto ppmResponse = null;
        if (id != null && version != null) {
            Optional<Ppm> relatedPpmOptional = ppmService.findById(id, version);
            if (relatedPpmOptional.isPresent()) {
                Ppm ppm = relatedPpmOptional.get();
                if (ppm.getDocs() != null) {
                    List<Docs> nonDeletedDocs = ppm.getDocs().stream()
                            .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                            .collect(Collectors.toList());
                    ppm.setDocs(nonDeletedDocs);
                }
                ppmResponse = PpmMapper.INSTANCE.mapToDto(ppm);
            }
        }
        response.setPpm(ppmResponse);
        log.info("PPM Responses for task [{}]: {}", taskId, ppmResponse);

        Map<String, Object> variables = new HashMap<>();
        variables.put("decision", processVariables.getOrDefault("decision", ""));
        variables.put("comment", processVariables.getOrDefault("comment", ""));
        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
        variables.put("treater", processVariables.getOrDefault("treater", ""));
        response.setTraceability(variables);

        Map<String, Boolean> taskProperties = getTaskProperties(task);
        response.setUpdatable(taskProperties.get("isUpdatable"));
        response.setPublishable(taskProperties.get("isPublishable"));
        response.setCommissionRequired(taskProperties.get("commissionRequired"));

        List<NextStepDTO> nextSteps = getPossibleNextSteps(task);
        log.info("Setting nextSteps for task [{}]: {}", taskId, nextSteps);
        response.setNextSteps(nextSteps);

        response.setStatus("Task details retrieved successfully");

        return response;
    }

//    @Transactional
//    public TaskDTO validateTask(String taskId, ValidationRequest request) throws PpmNotFound, AcNotFound, MissingComIdException, MissingAcIdException, UserNotAssociatedWithAcException {
//        // Authenticate user
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (!(authentication instanceof JwtAuthenticationToken)) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Invalid or missing JWT authentication");
//            return errorResponse;
//        }
//        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//
//        // Fetch task
//        Task task = taskService.createTaskQuery()
//                .taskId(taskId)
//                .taskTenantId(TENANT_ID)
//                .active()
//                .singleResult();
//
//        if (task == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Task not found or not active");
//            return errorResponse;
//        }
//
//        // Get candidate group
//        String candidateGroup = getCandidateGroup(task);
//        if (candidateGroup == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Task does not have a candidate group");
//            return errorResponse;
//        }
//
//        // Check user permissions
//        if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("User does not have the required role or assignment to validate this task");
//            return errorResponse;
//        }
//
//        // Retrieve process variables
//        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//        log.info("Process variables for task [{}]: {}", taskId, processVariables);
//        Integer id = (Integer) processVariables.getOrDefault("id", null);
//        Integer version = (Integer) processVariables.getOrDefault("version", null);
//
//        // Validate process variables
//        if (id == null || version == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus(String.format("Missing required process variables: id=%s, version=%s", id, version));
//            return errorResponse;
//        }
//
//        // Fetch Ppm entity
//        Optional<Ppm> ppmOptional = ppmService.findById(id, version);
//        if (ppmOptional.isEmpty()) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("No PPM entities found for id " + id + " and version " + version);
//            return errorResponse;
//        }
//
//        Ppm latestPpm = ppmOptional.get();
//
//        // Get task properties
//        Map<String, Boolean> taskProperties = getTaskProperties(task);
//        Boolean isUpdatable = taskProperties.get("isUpdatable");
//        Boolean isPublishable = taskProperties.get("isPublishable");
//        Boolean commissionRequired = taskProperties.get("commissionRequired");
//
//        // Handle isUpdatable
//        if (isUpdatable) {
//            if (request.getPpm() == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Ppm object is required in the request when isUpdatable is true");
//                return errorResponse;
//            }
//            // Log the state of realisationFiles before calling updateWithVersioning
//            log.info("Inspecting request.getPpm() for task [{}]: {}", taskId, request.getPpm());
//            if (request.getPpm() != null && request.getPpm().getRealisationsWithLots() != null) {
//                for (RealisationWithLotsRequestDto realisation : request.getPpm().getRealisationsWithLots()) {
//                    log.info("Realisation ID: {}, Files: {}", realisation.getId(),
//                            (realisation.getRealisationFiles() != null ? realisation.getRealisationFiles().size() : "null"));
//                }
//            } else {
//                log.info("No realisations or PPM data in request for task [{}]", taskId);
//            }
//            // Create new Ppm with incremented version
//            Ppm updatedPpm = ppmService.updateWithVersioning(id, request.getPpm());
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to update PPM object");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//            // Update process variable to reflect new version
//            log.info("Updated PPM version for task [{}]: {} -> {}", taskId, version, latestPpm.getVersion());
//            runtimeService.setVariable(task.getProcessInstanceId(), "version", latestPpm.getVersion());
//        }
//
//        // Handle commissionRequired
//        handleCommissionRequired(commissionRequired, request, processVariables, task.getProcessInstanceId());
//        validateUserCommissionAssociation(processVariables, jwtAuth);
//
//        // Add AC check for tasks without comId
//        Integer comId = (Integer) processVariables.get("comId");
//        if (comId == null) {
//            Integer acId = (Integer) processVariables.get("acId");
//            if (acId == null) {
//                throw new MissingAcIdException("acId is required in process variables for tasks without comId");
//            }
//            List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
//            if (userAcIds == null || !userAcIds.contains(acId)) {
//                throw new UserNotAssociatedWithAcException("User is not associated with the AC ID: " + acId);
//            }
//            log.info("User validated for AC ID: {}", acId);
//        }
//
//        // Handle status update
//        Integer newStatus = null;
//        if (isPublishable) {
//            // For the final task, set status to Publiée
//            newStatus = Constants.PUBLIE_STATUS;
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to update PPM status to Publiée");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//            log.info("Updated PPM status to Publiée for task [{}], new status: {}", taskId, latestPpm.getStatus().getId()); // Debug log
//        } else if (request.getValidate() != null && request.getValidate()) {
//            newStatus = Constants.TRAITMENT_STATUS; // Set to TRAITMENT_STATUS (36) if validated, keeping "en cours de traitement"
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to update PPM status to En cours de traitement");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//        } else if (request.getValidate() != null && !request.getValidate()) {
//            newStatus = Constants.TRAITMENT_STATUS; // Set to TRAITMENT_STATUS (36) if rejected, keeping "en cours de traitement"
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to keeping en cours de traitement");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//        }
//
//        // Determine process variable name dynamically and check if the next task is the last one
//        String processVariable = candidateGroup.toLowerCase() + "_valide"; // Fallback
//        boolean nextTaskIsLast = false;
//        try {
//            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
//            if (bpmnModel != null) {
//                FlowElement taskElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
//                if (taskElement instanceof org.flowable.bpmn.model.UserTask) {
//                    List<SequenceFlow> outgoingFlows = ((org.flowable.bpmn.model.UserTask) taskElement).getOutgoingFlows();
//                    for (SequenceFlow flow : outgoingFlows) {
//                        FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());
//                        if (targetElement instanceof ExclusiveGateway) {
//                            List<SequenceFlow> gatewayFlows = ((ExclusiveGateway) targetElement).getOutgoingFlows();
//                            for (SequenceFlow gatewayFlow : gatewayFlows) {
//                                String conditionExpression = gatewayFlow.getConditionExpression();
//                                if (conditionExpression != null && conditionExpression.toLowerCase().contains("valide")) {
//                                    String variableName = extractVariableName(conditionExpression);
//                                    if (variableName != null && variableName.toLowerCase().contains("valide")) {
//                                        processVariable = variableName;
//                                    }
//                                }
//                                // Check if the target of this flow leads to the last task
//                                FlowElement nextElement = bpmnModel.getFlowElement(gatewayFlow.getTargetRef());
//                                if (nextElement instanceof org.flowable.bpmn.model.UserTask) {
//                                    List<SequenceFlow> nextOutgoingFlows = ((org.flowable.bpmn.model.UserTask) nextElement).getOutgoingFlows();
//                                    for (SequenceFlow nextFlow : nextOutgoingFlows) {
//                                        FlowElement finalElement = bpmnModel.getFlowElement(nextFlow.getTargetRef());
//                                        if (finalElement instanceof org.flowable.bpmn.model.EndEvent) {
//                                            nextTaskIsLast = true;
//                                            log.info("Next task after [{}] is the last task", taskId);
//                                            break;
//                                        }
//                                    }
//                                }
//                                if (nextTaskIsLast) break;
//                            }
//                            if (nextTaskIsLast || processVariable != null) break;
//                        } else if (targetElement instanceof org.flowable.bpmn.model.UserTask) {
//                            List<SequenceFlow> nextOutgoingFlows = ((org.flowable.bpmn.model.UserTask) targetElement).getOutgoingFlows();
//                            for (SequenceFlow nextFlow : nextOutgoingFlows) {
//                                FlowElement finalElement = bpmnModel.getFlowElement(nextFlow.getTargetRef());
//                                if (finalElement instanceof org.flowable.bpmn.model.EndEvent) {
//                                    nextTaskIsLast = true;
//                                    log.info("Next task after [{}] is the last task", taskId);
//                                    break;
//                                }
//                            }
//                            if (nextTaskIsLast) break;
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.info("Failed to extract gateway condition or determine next task for task [" + taskId + "]: " + e.getMessage());
//        }
//
//        // Set process variables
//        boolean validate = request.getValidate() != null ? request.getValidate() : false;
//        Map<String, Object> variables = new HashMap<>();
//        variables.put(processVariable, validate);
//        variables.put("comment", request.getComment() != null ? request.getComment() : "");
//        variables.put("dateComment", LocalDateTime.now());
//        variables.put("treater", jwtAuth.getName());
//        if (isPublishable) {
//            variables.put("isPublishable", true); // Track for final status update
//        }
//
//        log.info("Setting process variables for task [{}]: {}={}, comment={}, dateComment={}, treater={}",
//                taskId, processVariable, validate, request.getComment(), LocalDateTime.now(), jwtAuth.getName());
//
//        runtimeService.setVariables(task.getProcessInstanceId(), variables);
//
//        // If this task's completion leads to the last task, set status to Validée
//        if (nextTaskIsLast && !isPublishable) {
//            Integer intermediateStatus = Constants.VALIDE_STATUS; // Set to VALIDE_STATUS (34) before entering the last task
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), intermediateStatus);
//            if (updatedPpm == null) {
//                log.warn("Failed to update PPM status to Validée before entering last task for task [{}]", taskId);
//            } else {
//                latestPpm = updatedPpm;
//                log.info("Updated PPM status to Validée before entering last task for task [{}], new status: {}", taskId, latestPpm.getStatus().getId());
//            }
//        }
//
//        // Complete task
//        taskService.complete(taskId);
//
//        // Prepare response
//        TaskDTO response = new TaskDTO();
//        response.setTaskId(task.getId());
//        response.setTaskName(task.getName());
//        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
//        response.setProcessInstanceId(task.getProcessInstanceId());
//        response.setId(id);
//        response.setVersion(latestPpm.getVersion());
//        response.setNewStatus(newStatus != null ? newStatus : latestPpm.getStatus().getId());
//        response.setValidate(validate);
//        response.setTraceability(variables);
//        response.setIsUpdatable(isUpdatable);
//        response.setIsPublishable(isPublishable);
//        response.setStatus("Task validated successfully");
//
//        return response;
//    }

//    @Transactional
//    public TaskDTO validateTask(String taskId, ValidationRequest request) throws PpmNotFound, AcNotFound, MissingComIdException, MissingAcIdException, UserNotAssociatedWithAcException, ComNotFound {
//        // Authenticate user
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (!(authentication instanceof JwtAuthenticationToken)) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Invalid or missing JWT authentication");
//            return errorResponse;
//        }
//        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//
//        // Fetch task
//        Task task = taskService.createTaskQuery()
//                .taskId(taskId)
//                .taskTenantId(TENANT_ID)
//                .active()
//                .singleResult();
//
//        if (task == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Task not found or not active");
//            return errorResponse;
//        }
//
//        // Get candidate group
//        String candidateGroup = getCandidateGroup(task);
//        if (candidateGroup == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Task does not have a candidate group");
//            return errorResponse;
//        }
//
//        // Check user permissions
//        if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("User does not have the required role or assignment to validate this task");
//            return errorResponse;
//        }
//
//        // Retrieve process variables
//        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//        log.info("Process variables for task [{}]: {}", taskId, processVariables);
//        Integer id = (Integer) processVariables.getOrDefault("id", null);
//        Integer version = (Integer) processVariables.getOrDefault("version", null);
//
//        // Validate process variables
//        if (id == null || version == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus(String.format("Missing required process variables: id=%s, version=%s", id, version));
//            return errorResponse;
//        }
//
//        // Fetch Ppm entity in a managed context
//        Optional<Ppm> ppmOptional = ppmService.findById(id, version);
//        if (ppmOptional.isEmpty()) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("No PPM entities found for id " + id + " and version " + version);
//            return errorResponse;
//        }
//        Ppm latestPpm = entityManager.merge(ppmOptional.get()); // Ensure managed entity
//        log.info("Fetched and merged latestPpm for id: {}, version: {}, coms: {}", id, version, latestPpm.getComs());
//
//        // Get task properties
//        Map<String, Boolean> taskProperties = getTaskProperties(task);
//        Boolean isUpdatable = taskProperties.get("isUpdatable");
//        Boolean isPublishable = taskProperties.get("isPublishable");
//        Boolean commissionRequired = taskProperties.get("commissionRequired");
//
//        // Handle isUpdatable
//        if (isUpdatable) {
//            if (request.getPpm() == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Ppm object is required in the request when isUpdatable is true");
//                return errorResponse;
//            }
//            log.info("Inspecting request.getPpm() for task [{}]: {}", taskId, request.getPpm());
//            if (request.getPpm() != null && request.getPpm().getRealisationsWithLots() != null) {
//                for (RealisationWithLotsRequestDto realisation : request.getPpm().getRealisationsWithLots()) {
//                    log.info("Realisation ID: {}, Files: {}", realisation.getId(),
//                            (realisation.getRealisationFiles() != null ? realisation.getRealisationFiles().size() : "null"));
//                }
//            } else {
//                log.info("No realisations or PPM data in request for task [{}]", taskId);
//            }
//            Ppm updatedPpm = ppmService.updateWithVersioning(id, request.getPpm());
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to update PPM object");
//                return errorResponse;
//            }
//            latestPpm = entityManager.merge(updatedPpm); // Merge to keep managed
//            runtimeService.setVariable(task.getProcessInstanceId(), "version", latestPpm.getVersion());
//        }
//
//        handleCommissionRequired(commissionRequired, request, processVariables, task.getProcessInstanceId(), latestPpm);
//
//        validateUserCommissionAssociation(processVariables, jwtAuth);
//
//        // Add AC check for tasks without comId
//        Integer comId = (Integer) processVariables.get("comId");
//        if (comId == null) {
//            Integer acId = (Integer) processVariables.get("acId");
//            if (acId == null) {
//                throw new MissingAcIdException("acId is required in process variables for tasks without comId");
//            }
//            List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
//            if (userAcIds == null || !userAcIds.contains(acId)) {
//                throw new UserNotAssociatedWithAcException("User is not associated with the AC ID: " + acId);
//            }
//            log.info("User validated for AC ID: {}", acId);
//        }
//
//        // Handle status update
//        Integer newStatus = null;
//        if (isPublishable) {
//            newStatus = Constants.PUBLIE_STATUS;
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to update PPM status to Publiée");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//            log.info("Updated PPM status to Publiée for task [{}], new status: {}", taskId, latestPpm.getStatus().getId());
//        } else if (request.getValidate() != null && request.getValidate()) {
//            newStatus = Constants.TRAITMENT_STATUS;
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to update PPM status to En cours de traitement");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//        } else if (request.getValidate() != null && !request.getValidate()) {
//            newStatus = Constants.TRAITMENT_STATUS;
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to keeping en cours de traitement");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//        }
//
//        // Determine process variable name dynamically and check if the next task is the last one
//        String processVariable = candidateGroup.toLowerCase() + "_valide";
//        boolean nextTaskIsLast = false;
//        try {
//            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
//            if (bpmnModel != null) {
//                FlowElement taskElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
//                if (taskElement instanceof org.flowable.bpmn.model.UserTask) {
//                    List<SequenceFlow> outgoingFlows = ((org.flowable.bpmn.model.UserTask) taskElement).getOutgoingFlows();
//                    for (SequenceFlow flow : outgoingFlows) {
//                        FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());
//                        if (targetElement instanceof ExclusiveGateway) {
//                            List<SequenceFlow> gatewayFlows = ((ExclusiveGateway) targetElement).getOutgoingFlows();
//                            for (SequenceFlow gatewayFlow : gatewayFlows) {
//                                String conditionExpression = gatewayFlow.getConditionExpression();
//                                if (conditionExpression != null && conditionExpression.toLowerCase().contains("valide")) {
//                                    String variableName = extractVariableName(conditionExpression);
//                                    if (variableName != null && variableName.toLowerCase().contains("valide")) {
//                                        processVariable = variableName;
//                                    }
//                                }
//                                FlowElement nextElement = bpmnModel.getFlowElement(gatewayFlow.getTargetRef());
//                                if (nextElement instanceof org.flowable.bpmn.model.EndEvent) {
//                                    nextTaskIsLast = true;
//                                    log.info("Next task after [{}] is the last task", taskId);
//                                    break;
//                                }
//                            }
//                        } else if (targetElement instanceof org.flowable.bpmn.model.EndEvent) {
//                            nextTaskIsLast = true;
//                            log.info("Next task after [{}] is the last task", taskId);
//                            break;
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.info("Failed to extract gateway condition or determine next task for task [" + taskId + "]: " + e.getMessage());
//        }
//
//        // Set process variables
//        boolean validate = request.getValidate() != null ? request.getValidate() : false;
//        Map<String, Object> variables = new HashMap<>();
//        variables.put(processVariable, validate);
//        variables.put("comment", request.getComment() != null ? request.getComment() : "");
//        variables.put("dateComment", LocalDateTime.now());
//        variables.put("treater", jwtAuth.getName());
//        if (isPublishable) {
//            variables.put("isPublishable", true);
//        }
//
//        log.info("Setting process variables for task [{}]: {}={}, comment={}, dateComment={}, treater={}",
//                taskId, processVariable, validate, request.getComment(), LocalDateTime.now(), jwtAuth.getName());
//
//        runtimeService.setVariables(task.getProcessInstanceId(), variables);
//
//        // If this task's completion leads to the last task, set status to Validée
//        if (nextTaskIsLast && !isPublishable) {
//            Integer intermediateStatus = Constants.VALIDE_STATUS;
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), intermediateStatus);
//            if (updatedPpm == null) {
//                log.warn("Failed to update PPM status to Validée before entering last task for task [{}]", taskId);
//            } else {
//                latestPpm = updatedPpm;
//                log.info("Updated PPM status to Validée before entering last task for task [{}], new status: {}", taskId, latestPpm.getStatus().getId());
//            }
//        }
//
//        // Save and flush to ensure all coms are persisted
//        ppmService.save(latestPpm);
//        log.info("Saved PPM with coms before flush: {} for ppmId: {}, version: {}", latestPpm.getComs(), latestPpm.getId(), latestPpm.getVersion());
//        entityManager.flush(); // Force immediate persistence
//        log.info("Flushed PPM with coms after flush: {} for ppmId: {}, version: {}", latestPpm.getComs(), latestPpm.getId(), latestPpm.getVersion());
//
//        // Complete task
//        taskService.complete(taskId);
//
//        // Prepare response
//        TaskDTO response = new TaskDTO();
//        response.setTaskId(task.getId());
//        response.setTaskName(task.getName());
//        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
//        response.setProcessInstanceId(task.getProcessInstanceId());
//        response.setId(id);
//        response.setVersion(latestPpm.getVersion());
//        response.setNewStatus(newStatus != null ? newStatus : latestPpm.getStatus().getId());
//        response.setValidate(validate);
//        response.setTraceability(variables);
//        response.setIsUpdatable(isUpdatable);
//        response.setIsPublishable(isPublishable);
//        response.setStatus("Task validated successfully");
//
//        return response;
//    }



//    @Transactional
//    public TaskDTO validateTask(String taskId, ValidationRequest request) throws PpmNotFound, AcNotFound, MissingComIdException, MissingAcIdException, UserNotAssociatedWithAcException, ComNotFound {
//        // Authenticate user
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (!(authentication instanceof JwtAuthenticationToken)) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Invalid or missing JWT authentication");
//            return errorResponse;
//        }
//        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//
//        // Fetch task
//        Task task = taskService.createTaskQuery()
//                .taskId(taskId)
//                .taskTenantId(TENANT_ID)
//                .active()
//                .singleResult();
//
//        if (task == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Task not found or not active");
//            return errorResponse;
//        }
//
//        // Get candidate group
//        String candidateGroup = getCandidateGroup(task);
//        if (candidateGroup == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Task does not have a candidate group");
//            return errorResponse;
//        }
//
//        // Check user permissions
//        if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("User does not have the required role or assignment to validate this task");
//            return errorResponse;
//        }
//
//        // Retrieve process variables
//        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//        log.info("Process variables for task [{}]: {}", taskId, processVariables);
//        Integer id = (Integer) processVariables.getOrDefault("id", null);
//        Integer version = (Integer) processVariables.getOrDefault("version", null);
//
//        // Validate process variables
//        if (id == null || version == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus(String.format("Missing required process variables: id=%s, version=%s", id, version));
//            return errorResponse;
//        }
//
//        // Fetch Ppm entity
//        Optional<Ppm> ppmOptional = ppmService.findById(id, version);
//        if (ppmOptional.isEmpty()) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("No PPM entities found for id " + id + " and version " + version);
//            return errorResponse;
//        }
//
//        Ppm latestPpm = ppmOptional.get();
//
//        // Get task properties
//        Map<String, Boolean> taskProperties = getTaskProperties(task);
//        Boolean isUpdatable = taskProperties.get("isUpdatable");
//        Boolean isPublishable = taskProperties.get("isPublishable");
//        Boolean commissionRequired = taskProperties.get("commissionRequired");
//
//        // Handle isUpdatable
//        if (isUpdatable) {
//            if (request.getPpm() == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Ppm object is required in the request when isUpdatable is true");
//                return errorResponse;
//            }
//            log.info("Inspecting request.getPpm() for task [{}]: {}", taskId, request.getPpm());
//            if (request.getPpm() != null && request.getPpm().getRealisationsWithLots() != null) {
//                for (RealisationWithLotsRequestDto realisation : request.getPpm().getRealisationsWithLots()) {
//                    log.info("Realisation ID: {}, Files: {}", realisation.getId(),
//                            (realisation.getRealisationFiles() != null ? realisation.getRealisationFiles().size() : "null"));
//                }
//            } else {
//                log.info("No realisations or PPM data in request for task [{}]", taskId);
//            }
//            // Create new Ppm with incremented version
//            Ppm updatedPpm = ppmService.updateWithVersioning(id, request.getPpm());
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to update PPM object");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//            // Update process variable to reflect new version
//            log.info("Updated PPM version for task [{}]: {} -> {}", taskId, version, latestPpm.getVersion());
//            runtimeService.setVariable(task.getProcessInstanceId(), "version", latestPpm.getVersion());
//        }
//
//        handleCommissionRequired(commissionRequired, request, processVariables, task.getProcessInstanceId(), latestPpm);
//
//        validateUserCommissionAssociation(processVariables, jwtAuth);
//
//        // Add AC check for tasks without comId
//        Integer comId = (Integer) processVariables.get("comId");
//        if (comId == null) {
//            Integer acId = (Integer) processVariables.get("acId");
//            if (acId == null) {
//                throw new MissingAcIdException("acId is required in process variables for tasks without comId");
//            }
//            List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
//            if (userAcIds == null || !userAcIds.contains(acId)) {
//                throw new UserNotAssociatedWithAcException("User is not associated with the AC ID: " + acId);
//            }
//            log.info("User validated for AC ID: {}", acId);
//        }
//
//        // Handle status update
//        ProcessStatus newStatus = null;
//        if (isPublishable) {
//            // For the final task, set status to Publiée
//            newStatus = ProcessStatus.PUBLIE; // Set to PUBLIE_STATUS (e.g., 35)
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to update PPM status to Publiée");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//            log.info("Updated PPM status to Publiée for task [{}], new status: {}", taskId, latestPpm.getStatus()); // Debug log
//        } else if (request.getValidate() != null && request.getValidate()) {
//            newStatus = ProcessStatus.EN_TRAITEMENT; // Set to TRAITMENT_STATUS (36) if validated, keeping "en cours de traitement"
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to update PPM status to En cours de traitement");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//        } else if (request.getValidate() != null && !request.getValidate()) {
//            newStatus = ProcessStatus.EN_TRAITEMENT; // Set to TRAITMENT_STATUS (36) if rejected, keeping "en cours de traitement"
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//            if (updatedPpm == null) {
//                TaskDTO errorResponse = new TaskDTO();
//                errorResponse.setStatus("Failed to keeping en cours de traitement");
//                return errorResponse;
//            }
//            latestPpm = updatedPpm;
//        }
//
//        // Determine process variable name dynamically and check if the next task is the last one
//        String processVariable = candidateGroup.toLowerCase() + "_valide";
//        boolean nextTaskIsLast = false;
//        try {
//            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
//            if (bpmnModel != null) {
//                FlowElement taskElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
//                if (taskElement instanceof org.flowable.bpmn.model.UserTask) {
//                    List<SequenceFlow> outgoingFlows = ((org.flowable.bpmn.model.UserTask) taskElement).getOutgoingFlows();
//                    for (SequenceFlow flow : outgoingFlows) {
//                        FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());
//                        if (targetElement instanceof ExclusiveGateway) {
//                            List<SequenceFlow> gatewayFlows = ((ExclusiveGateway) targetElement).getOutgoingFlows();
//                            for (SequenceFlow gatewayFlow : gatewayFlows) {
//                                String conditionExpression = gatewayFlow.getConditionExpression();
//                                if (conditionExpression != null && conditionExpression.toLowerCase().contains("valide")) {
//                                    String variableName = extractVariableName(conditionExpression);
//                                    if (variableName != null && variableName.toLowerCase().contains("valide")) {
//                                        processVariable = variableName;
//                                    }
//                                }
//                                FlowElement nextElement = bpmnModel.getFlowElement(gatewayFlow.getTargetRef());
//                                if (nextElement instanceof org.flowable.bpmn.model.EndEvent) {
//                                    nextTaskIsLast = true;
//                                    log.info("Next task after [{}] is the last task", taskId);
//                                    break;
//                                }
//                            }
//                        } else if (targetElement instanceof org.flowable.bpmn.model.EndEvent) {
//                            nextTaskIsLast = true;
//                            log.info("Next task after [{}] is the last task", taskId);
//                            break;
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.info("Failed to extract gateway condition or determine next task for task [" + taskId + "]: " + e.getMessage());
//        }
//
//        // Set process variables
//        boolean validate = request.getValidate() != null ? request.getValidate() : false;
//        Map<String, Object> variables = new HashMap<>();
//        variables.put(processVariable, validate);
//        variables.put("comment", request.getComment() != null ? request.getComment() : "");
//        variables.put("dateComment", LocalDateTime.now());
//        variables.put("treater", jwtAuth.getName());
//        if (isPublishable) {
//            variables.put("isPublishable", true);
//        }
//
//        log.info("Setting process variables for task [{}]: {}={}, comment={}, dateComment={}, treater={}",
//                taskId, processVariable, validate, request.getComment(), LocalDateTime.now(), jwtAuth.getName());
//
//        runtimeService.setVariables(task.getProcessInstanceId(), variables);
//
//        // If this task's completion leads to the last task, set status to Validée
//        if (nextTaskIsLast && !isPublishable) {
//            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), ProcessStatus.VALIDE);
//            if (updatedPpm == null) {
//                log.warn("Failed to update PPM status to Validée before entering last task for task [{}]", taskId);
//            } else {
//                latestPpm = updatedPpm;
//                log.info("Updated PPM status to Validée before entering last task for task [{}], new status: {}", taskId, latestPpm.getStatus());
//            }
//        }
//
//        // Save the updated Ppm to persist the coms relationship
//        ppmService.save(latestPpm);
//        log.info("Saved PPM with coms: {} for ppmId: {}, version: {}", latestPpm.getComs(), latestPpm.getId(), latestPpm.getVersion());
//
//        // Complete task
//        taskService.complete(taskId);
//
//        // Prepare response
//        TaskDTO response = new TaskDTO();
//        response.setTaskId(task.getId());
//        response.setTaskName(task.getName());
//        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
//        response.setProcessInstanceId(task.getProcessInstanceId());
//        response.setId(id);
//        response.setVersion(latestPpm.getVersion());
//        response.setNewStatus(newStatus != null ? newStatus : latestPpm.getStatus());
//        response.setValidate(validate);
//        response.setTraceability(variables);
//        response.setIsUpdatable(isUpdatable);
//        response.setIsPublishable(isPublishable);
//        response.setStatus("Task validated successfully");
//
//        return response;
//    }

    @Transactional
    public TaskDTO validateTask(String taskId, ValidationRequest request) throws PpmNotFound, AcNotFound, MissingComIdException, MissingAcIdException, UserNotAssociatedWithAcException, ComNotFound {
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
        Boolean commissionRequired = taskProperties.get("commissionRequired");

        // Handle isUpdatable
        if (isUpdatable) {
            if (request.getPpm() == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Ppm object is required in the request when isUpdatable is true");
                return errorResponse;
            }
            log.info("Inspecting request.getPpm() for task [{}]: {}", taskId, request.getPpm());
            Ppm updatedPpm = ppmService.updateWithVersioning(id, request.getPpm());
            if (updatedPpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update PPM object");
                return errorResponse;
            }
            latestPpm = updatedPpm;
            runtimeService.setVariable(task.getProcessInstanceId(), "version", latestPpm.getVersion());
            log.info("Updated ppm after versioning, coms: {}", latestPpm.getComs());
        }

        handleCommissionRequired(commissionRequired, request, processVariables, task.getProcessInstanceId(), latestPpm);

        validateUserCommissionAssociation(processVariables, jwtAuth);

        // Add AC check for tasks without comId
        Integer comId = (Integer) processVariables.get("comId");
        if (comId == null) {
            Integer acId = (Integer) processVariables.get("acId");
            if (acId == null) {
                throw new MissingAcIdException("acId is required in process variables for tasks without comId");
            }
            List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
            if (userAcIds == null || !userAcIds.contains(acId)) {
                throw new UserNotAssociatedWithAcException("User is not associated with the AC ID: " + acId);
            }
            log.info("User validated for AC ID: {}", acId);
        }

        // Handle status update
        ProcessStatus newStatus = null;
        if (isPublishable) {
            newStatus = ProcessStatus.PUBLIE;
            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
            if (updatedPpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update PPM status to Publiée");
                return errorResponse;
            }
            latestPpm = updatedPpm;
            log.info("Updated PPM status to Publiée for task [{}], new status: {}", taskId, latestPpm.getStatus());
        } else if (request.getValidate() != null && request.getValidate()) {
            newStatus = ProcessStatus.EN_TRAITEMENT;
            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
            if (updatedPpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update PPM status to En cours de traitement");
                return errorResponse;
            }
            latestPpm = updatedPpm;
        } else if (request.getValidate() != null && !request.getValidate()) {
            newStatus = ProcessStatus.EN_TRAITEMENT;
            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
            if (updatedPpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to keeping en cours de traitement");
                return errorResponse;
            }
            latestPpm = updatedPpm;
        }

        // Determine process variable name dynamically and check if the next task is the last one
        String processVariable = candidateGroup.toLowerCase() + "_valide";
        boolean nextTaskIsLast = false;
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
                                    }
                                }
                                FlowElement nextElement = bpmnModel.getFlowElement(gatewayFlow.getTargetRef());
                                if (nextElement instanceof org.flowable.bpmn.model.EndEvent) {
                                    nextTaskIsLast = true;
                                    log.info("Next task after [{}] is the last task", taskId);
                                    break;
                                }
                            }
                        } else if (targetElement instanceof org.flowable.bpmn.model.EndEvent) {
                            nextTaskIsLast = true;
                            log.info("Next task after [{}] is the last task", taskId);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("Failed to extract gateway condition or determine next task for task [" + taskId + "]: " + e.getMessage());
        }

        // Set process variables
        boolean validate = request.getValidate() != null ? request.getValidate() : false;
        Map<String, Object> variables = new HashMap<>();
        variables.put(processVariable, validate);
        variables.put("comment", request.getComment() != null ? request.getComment() : "");
        variables.put("dateComment", LocalDateTime.now());
        variables.put("treater", jwtAuth.getName());
        if (isPublishable) {
            variables.put("isPublishable", true);
        }

        log.info("Setting process variables for task [{}]: {}={}, comment={}, dateComment={}, treater={}",
                taskId, processVariable, validate, request.getComment(), LocalDateTime.now(), jwtAuth.getName());

        runtimeService.setVariables(task.getProcessInstanceId(), variables);

        // If this task's completion leads to the last task, set status to Validée
        if (nextTaskIsLast && !isPublishable) {
            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), ProcessStatus.VALIDE);
            if (updatedPpm == null) {
                log.warn("Failed to update PPM status to Validée before entering last task for task [{}]", taskId);
            } else {
                latestPpm = updatedPpm;
                log.info("Updated PPM status to Validée before entering last task for task [{}], new status: {}", taskId, latestPpm.getStatus());
            }
        }

        // Save the updated Ppm to persist the coms relationship
        ppmService.save(latestPpm);
        log.info("Saved PPM with coms: {} for ppmId: {}, version: {}", latestPpm.getComs(), latestPpm.getId(), latestPpm.getVersion());

        // Complete task
        taskService.complete(taskId);

        // Prepare response
        TaskDTO response = new TaskDTO();
        response.setTaskId(task.getId());
        response.setTaskName(task.getName());
        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
        response.setProcessInstanceId(task.getProcessInstanceId());
        response.setId(id);
        response.setVersion(latestPpm.getVersion());
        response.setNewStatus(newStatus != null ? newStatus : latestPpm.getStatus());
        response.setValidate(validate);
        response.setTraceability(variables);
        response.setIsUpdatable(isUpdatable);
        response.setIsPublishable(isPublishable);
        response.setStatus("Task validated successfully");

        return response;
    }
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
     * Retrieves tasks assigned to the current user based on their candidate groups.
     *
     * @return TaskListResponseDTO with the user's tasks
     */
    public TaskListResponseDTO getUserTasks() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        // Get user's AC IDs and comIds
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
        if ((userAcIds == null || userAcIds.isEmpty()) && (userComIds == null || userComIds.isEmpty())) {
            log.warn("No AC IDs or comIds found for the current user");
            return new TaskListResponseDTO();
        }
        log.info("ac ids: {}, comIds: {}", userAcIds, userComIds);

        // Get user's candidate groups (roles)
        List<String> userCandidateGroups = jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
        log.info("User roles (candidate groups): {}", userCandidateGroups);

        if (userCandidateGroups.isEmpty()) {
            log.warn("User does not have any recognized roles");
            return new TaskListResponseDTO();
        }

        List<String> matchingProcessInstanceIds = new ArrayList<>();

        // Collect process instances where acId or comId matches user's IDs
        if (userAcIds != null && !userAcIds.isEmpty()) {
            for (Integer acId : userAcIds) {
                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                        .variableValueEquals("acId", acId)
                        .list();
                log.info("ac id {} matched {} instances: {}", acId, instances.size(), instances.stream().map(ProcessInstance::getId).collect(Collectors.toList()));
                matchingProcessInstanceIds.addAll(
                        instances.stream()
                                .map(ProcessInstance::getId)
                                .collect(Collectors.toList())
                );
            }
        }
        if (userComIds != null && !userComIds.isEmpty()) {
            for (Integer comId : userComIds) {
                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                        .variableValueEquals("comId", comId)
                        .list();
                log.info("comId {} matched {} instances: {}", comId, instances.size(), instances.stream().map(ProcessInstance::getId).collect(Collectors.toList()));
                matchingProcessInstanceIds.addAll(
                        instances.stream()
                                .map(ProcessInstance::getId)
                                .collect(Collectors.toList())
                );
            }
        }

        // Log all active tasks for matched process instances to inspect candidate groups
        for (String processInstanceId : matchingProcessInstanceIds) {
            List<Task> allTasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .active()
                    .list();
            for (Task task : allTasks) {
                String taskCandidateGroup = getCandidateGroup(task);
                log.info("Task [{}] (processInstanceId: {}) required candidate group: {}, user has roles: {}",
                        task.getId(), processInstanceId, taskCandidateGroup, userCandidateGroups);
            }
        }

        List<Task> filteredTasks = new ArrayList<>();
        int chunkSize = 1000;
        for (int i = 0; i < matchingProcessInstanceIds.size(); i += chunkSize) {
            List<String> chunk = matchingProcessInstanceIds.subList(i,
                    Math.min(i + chunkSize, matchingProcessInstanceIds.size()));
            List<Task> tasks = taskService.createTaskQuery()
                    .taskTenantId(TENANT_ID)
                    .taskCandidateGroupIn(userCandidateGroups)
                    .processInstanceIdIn(List.of(chunk.toArray(new String[0])))
                    .active()
                    .orderByTaskCreateTime().desc()
                    .list();
            filteredTasks.addAll(tasks);
            for (Task task : tasks) {
                String taskCandidateGroup = getCandidateGroup(task);
                log.info("Filtered Task [{}] (processInstanceId: {}) required candidate group: {}, user has roles: {}",
                        task.getId(), task.getProcessInstanceId(), taskCandidateGroup, userCandidateGroups);
            }
        }
        log.info("filteredTasks: {}", filteredTasks);

        List<TaskDTO> taskList = new ArrayList<>();
        for (Task task : filteredTasks) {
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
            log.info("taskId: {}, id {}, version: {}", task.getId(), id, version);

            if (id != null && version != null) {
                try {
                    log.info("id: {}, version: {}", id, version);
                    Optional<Ppm> ppmOptional = ppmService.findById(id, version);
                    ppmOptional.ifPresent(ppm -> {
                        taskInfo.setId(id);
                        taskInfo.setVersion(version);
                        taskInfo.setNom(ppm.getNom());
                        taskInfo.setRef(ppm.getRef());
                    });
                } catch (AcNotFound | PpmNotFound e) {
                    log.warn("PPM not found for id: {}, version: {} in task: {}, error: {}", id, version, task.getId(), e.getMessage());
                    continue;
                } catch (PpmNotAssociatedWithUserAcException e) {
                    log.warn("User not associated with PPM id: {}, version: {} in task: {}, error: {}", id, version, task.getId(), e.getMessage());
                    continue;
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
            taskInfo.setCommissionRequired(taskProperties.get("commissionRequired"));

            taskList.add(taskInfo);
        }

        TaskListResponseDTO response = new TaskListResponseDTO();
        response.setTenantId(TENANT_ID);
        response.setTasks(taskList);
        response.setCandidateGroup(userCandidateGroups.isEmpty() ? null : String.join(", ", userCandidateGroups));
        response.setStatus("Tasks retrieved successfully");

        return response;
    }
//    public TaskListResponseDTO getUserTasks() {
//        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//
//        // Get user's AC IDs and comIds
//        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
//        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
//        if ((userAcIds == null || userAcIds.isEmpty()) && (userComIds == null || userComIds.isEmpty())) {
//            log.warn("No AC IDs or comIds found for the current user");
//            return new TaskListResponseDTO(); // Return empty response if no AC or comIds
//        }
//        log.info("ac ids: {}, comIds: {}", userAcIds, userComIds);
//
//        // Get user's candidate groups
//        List<String> userCandidateGroups = jwtAuth.getAuthorities().stream()
//                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
//                .collect(Collectors.toList());
//
//        if (userCandidateGroups.isEmpty()) {
//            log.warn("User does not have any recognized roles");
//            return new TaskListResponseDTO(); // Return empty response if no candidate groups
//        }
//
//        List<String> matchingProcessInstanceIds = new ArrayList<>();
//
//        // Collect process instances where acId or comId matches user's IDs
//        if (userAcIds != null && !userAcIds.isEmpty()) {
//            for (Integer acId : userAcIds) {
//                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
//                        .variableValueEquals("acId", acId)
//                        .list();
//                log.info("ac id {} matched {} instances", acId, instances.size());
//                matchingProcessInstanceIds.addAll(
//                        instances.stream()
//                                .map(ProcessInstance::getId)
//                                .collect(Collectors.toList())
//                );
//            }
//        }
//        if (userComIds != null && !userComIds.isEmpty()) {
//            for (Integer comId : userComIds) {
//                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
//                        .variableValueEquals("comId", comId)
//                        .list();
//                log.info("comId {} matched {} instances", comId, instances.size());
//                matchingProcessInstanceIds.addAll(
//                        instances.stream()
//                                .map(ProcessInstance::getId)
//                                .collect(Collectors.toList())
//                );
//            }
//        }
//
//        List<Task> filteredTasks = new ArrayList<>();
//        int chunkSize = 1000;
//        for (int i = 0; i < matchingProcessInstanceIds.size(); i += chunkSize) {
//            List<String> chunk = matchingProcessInstanceIds.subList(i,
//                    Math.min(i + chunkSize, matchingProcessInstanceIds.size()));
//            filteredTasks.addAll(
//                    taskService.createTaskQuery()
//                            .taskTenantId(TENANT_ID)
//                            .taskCandidateGroupIn(userCandidateGroups)
//                            .processInstanceIdIn(List.of(chunk.toArray(new String[0])))
//                            .active()
//                            .orderByTaskCreateTime().desc()
//                            .list()
//            );
//        }
//        log.info("filteredTasks: {}", filteredTasks);
//
//        List<TaskDTO> taskList = new ArrayList<>();
//        for (Task task : filteredTasks) {
//            TaskDTO taskInfo = new TaskDTO();
//            taskInfo.setTaskId(task.getId());
//            taskInfo.setTaskName(task.getName());
//            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
//            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
//            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
//            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
//            taskInfo.setCandidateGroup(getCandidateGroup(task));
//
//            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//            Integer id = (Integer) processVariables.getOrDefault("id", null);
//            Integer version = (Integer) processVariables.getOrDefault("version", null);
//            log.info("taskId: {}, id {}, version: {}", task.getId(), id, version);
//
//            if (id != null && version != null) {
//                try {
//                    log.info("id: {}, version: {}", id, version);
//                    Optional<Ppm> ppmOptional = ppmService.findById(id, version);
//                    ppmOptional.ifPresent(ppm -> {
//                        taskInfo.setId(id);
//                        taskInfo.setVersion(version);
//                        taskInfo.setNom(ppm.getNom());
//                        taskInfo.setRef(ppm.getRef());
//                    });
//                } catch (AcNotFound | PpmNotFound e) {
//                    log.warn("PPM not found for id: {}, version: {} in task: {}, error: {}", id, version, task.getId(), e.getMessage());
//                    continue; // Skip this task and proceed to the next
//                } catch (PpmNotAssociatedWithUserAcException e) {
//                    log.warn("User not associated with PPM id: {}, version: {} in task: {}, error: {}", id, version, task.getId(), e.getMessage());
//                    continue; // Skip this task if no AC or comId match
//                }
//            }
//
//            Map<String, Object> variables = new HashMap<>();
//            variables.put("decision", processVariables.getOrDefault("decision", ""));
//            variables.put("comment", processVariables.getOrDefault("comment", ""));
//            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
//            variables.put("treater", processVariables.getOrDefault("treater", ""));
//            taskInfo.setTraceability(variables);
//
//            Map<String, Boolean> taskProperties = getTaskProperties(task);
//            taskInfo.setIsUpdatable(taskProperties.get("isUpdatable"));
//            taskInfo.setIsPublishable(taskProperties.get("isPublishable"));
//            taskInfo.setCommissionRequired(taskProperties.get("commissionRequired"));
//
//            taskList.add(taskInfo);
//        }
//
//        TaskListResponseDTO response = new TaskListResponseDTO();
//        response.setTenantId(TENANT_ID);
//        response.setTasks(taskList);
//        response.setCandidateGroup(userCandidateGroups.isEmpty() ? null : String.join(", ", userCandidateGroups));
//        response.setStatus("Tasks retrieved successfully");
//
//        return response;
//    }


    //lfou9aniya jawha behi
//    public TaskListResponseDTO getUserTasks() {
//        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//
//        // Get user's AC IDs and comIds
//        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
//        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
//        if ((userAcIds == null || userAcIds.isEmpty()) && (userComIds == null || userComIds.isEmpty())) {
//            log.warn("No AC IDs or comIds found for the current user");
//            return new TaskListResponseDTO();
//        }
//        log.info("ac ids: {}, comIds: {}", userAcIds, userComIds);
//
//        // Get user's candidate groups
//        List<String> userCandidateGroups = jwtAuth.getAuthorities().stream()
//                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
//                .collect(Collectors.toList());
//
//        if (userCandidateGroups.isEmpty()) {
//            log.warn("User does not have any recognized roles");
//            return new TaskListResponseDTO();
//        }
//
//        List<String> matchingProcessInstanceIds = new ArrayList<>();
//
//        // Collect process instances where acId or comId matches user's IDs
//        if (userAcIds != null && !userAcIds.isEmpty()) {
//            for (Integer acId : userAcIds) {
//                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
//                        .variableValueEquals("acId", acId)
//                        .list();
//                log.info("ac id {} matched {} instances", acId, instances.size());
//                matchingProcessInstanceIds.addAll(
//                        instances.stream()
//                                .map(ProcessInstance::getId)
//                                .collect(Collectors.toList())
//                );
//            }
//        }
//        if (userComIds != null && !userComIds.isEmpty()) {
//            for (Integer comId : userComIds) {
//                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
//                        .variableValueEquals("comId", comId)
//                        .list();
//                log.info("comId {} matched {} instances", comId, instances.size());
//                matchingProcessInstanceIds.addAll(
//                        instances.stream()
//                                .map(ProcessInstance::getId)
//                                .collect(Collectors.toList())
//                );
//            }
//        }
//
//        List<Task> filteredTasks = new ArrayList<>();
//        int chunkSize = 1000;
//        for (int i = 0; i < matchingProcessInstanceIds.size(); i += chunkSize) {
//            List<String> chunk = matchingProcessInstanceIds.subList(i,
//                    Math.min(i + chunkSize, matchingProcessInstanceIds.size()));
//            filteredTasks.addAll(
//                    taskService.createTaskQuery()
//                            .taskTenantId(TENANT_ID)
//                            .taskCandidateGroupIn(userCandidateGroups)
//                            .processInstanceIdIn(List.of(chunk.toArray(new String[0])))
//                            .active()
//                            .orderByTaskCreateTime().desc()
//                            .list()
//            );
//        }
//        log.info("filteredTasks: {}", filteredTasks);
//
//        List<TaskDTO> taskList = filteredTasks.stream().map(task -> {
//            TaskDTO taskInfo = new TaskDTO();
//            taskInfo.setTaskId(task.getId());
//            taskInfo.setTaskName(task.getName());
//            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
//            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
//            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
//            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
//            taskInfo.setCandidateGroup(getCandidateGroup(task));
//
//            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//            Integer id = (Integer) processVariables.getOrDefault("id", null);
//            Integer version = (Integer) processVariables.getOrDefault("version", null);
//            log.info("taskId: {},id {}, version: {}", task.getId(), id, version);
//
//            if (id != null && version != null) {
//                try {
//                    log.info("id: {}, version: {}", id, version);
//                    Optional<Ppm> ppmOptional = ppmService.findById(id, version);
//                    ppmOptional.ifPresent(ppm -> {
//                        taskInfo.setId(id);
//                        taskInfo.setVersion(version);
//                        taskInfo.setNom(ppm.getNom());
//                        taskInfo.setRef(ppm.getRef());
//                    });
//                } catch (AcNotFound | PpmNotFound e) {
//                    log.warn("Agpm not found for id: {}, version: {} in task: {}", id, version, task.getId(), e);
//                }
//            }
//
//            Map<String, Object> variables = new HashMap<>();
//            variables.put("decision", processVariables.getOrDefault("decision", ""));
//            variables.put("comment", processVariables.getOrDefault("comment", ""));
//            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
//            variables.put("treater", processVariables.getOrDefault("treater", ""));
//            taskInfo.setTraceability(variables);
//
//            Map<String, Boolean> taskProperties = getTaskProperties(task);
//            taskInfo.setIsUpdatable(taskProperties.get("isUpdatable"));
//            taskInfo.setIsPublishable(taskProperties.get("isPublishable"));
//            taskInfo.setCommissionRequired(taskProperties.get("commissionRequired"));
//
//            return taskInfo;
//        }).collect(Collectors.toList());
//
//        TaskListResponseDTO response = new TaskListResponseDTO();
//        response.setTenantId(TENANT_ID);
//        response.setTasks(taskList);
//        response.setCandidateGroup(userCandidateGroups.isEmpty() ? null : String.join(", ", userCandidateGroups));
//        response.setStatus("Tasks retrieved successfully");
//
//        return response;
//    }

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
     * Retrieves a Ppm entity by its ID along with its associated task, if any.
     * If a task exists and the user has permission to view it, includes the task details and sets hasTask to true.
     * Otherwise, sets hasTask to false and omits task details.
     *
     * @param ppmId The ID of the Ppm entity
     * @return TaskDetailsDTO containing the Ppm details and task information (if applicable)
     * @throws PpmNotFound if the Ppm entity is not found
     */
    @Transactional
    public TaskDetailsDTO getPpmWithTasks(Integer ppmId) throws PpmNotFound, AcNotFound {
        log.info("Récupération du PPM avec ID: {} et sa tâche associée", ppmId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        // Récupérer le PPM avec la dernière version
        Integer latestVersion = ppmService.findLatestVersionById(ppmId);
        Optional<Ppm> ppmOptional = ppmService.findById(ppmId, latestVersion);
        if (ppmOptional.isEmpty()) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("PPM non trouvé pour l'ID: " + ppmId);
            return errorResponse;
        }

        Ppm ppm = ppmOptional.get();

        // Préparer l'objet de réponse
        TaskDetailsDTO response = new TaskDetailsDTO();

        // Mapper le PPM vers PpmResponseDto
        if (ppm.getDocs() != null) {
            List<Docs> nonDeletedDocs = ppm.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList());
            ppm.setDocs(nonDeletedDocs);
        }
        PpmResponseDto ppmResponse = PpmMapper.INSTANCE.mapToDto(ppm);
        response.setPpm(ppmResponse);

        // Vérifier si le PPM a un processus actif
        String processInstanceId = ppm.getProcessInstanceId();
        if (processInstanceId == null) {
            response.setHasTask(false);
            response.setStatus("PPM récupéré avec succès, aucune tâche associée");
            return response;
        }

        // Récupérer la tâche active pour ce processInstanceId (une seule tâche est attendue)
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskTenantId(TENANT_ID)
                .active()
                .singleResult();

        // S'il n'y a pas de tâche active
        if (task == null) {
            response.setHasTask(false);
            response.setStatus("PPM récupéré avec succès, aucune tâche active associée");
            return response;
        }

        // Vérifier si l'utilisateur a les droits de voir la tâche
        boolean userCanViewTask = hasCandidateGroup(task, jwtAuth) || hasAssign(task, jwtAuth);

        if (!userCanViewTask) {
            log.warn("L'utilisateur {} n'a pas les droits pour voir la tâche {} du PPM {}",
                    jwtAuth.getName(), task.getId(), ppmId);
            response.setHasTask(false);
            response.setStatus("PPM récupéré avec succès, une tâche existe mais l'utilisateur n'a pas les droits de la voir");
            return response;
        }

        // Si l'utilisateur a les droits, inclure les détails de la tâche et définir hasTask à true
        response.setHasTask(true);
        response.setTaskId(task.getId());
        response.setTaskName(task.getName());
        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
        response.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
        response.setProcessInstanceId(task.getProcessInstanceId());
        response.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        response.setCandidateGroup(getCandidateGroup(task));

        // Ajouter les variables de traçabilité
        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        Map<String, Object> variables = new HashMap<>();
        variables.put("decision", processVariables.getOrDefault("decision", ""));
        variables.put("comment", processVariables.getOrDefault("comment", ""));
        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
        variables.put("treater", processVariables.getOrDefault("treater", ""));
        response.setTraceability(variables);

        // Ajouter les propriétés de la tâche
        Map<String, Boolean> taskProperties = getTaskProperties(task);
        response.setUpdatable(taskProperties.get("isUpdatable"));
        response.setPublishable(taskProperties.get("isPublishable"));
        response.setCommissionRequired(taskProperties.get("isCommissionRequired"));

        // Ajouter les prochaines étapes possibles
        List<NextStepDTO> nextSteps = getPossibleNextSteps(task);
        response.setNextSteps(nextSteps);

        response.setStatus("PPM et détails de la tâche récupérés avec succès");

        return response;
    }
    @Autowired
    private EntityManager entityManager;

    private void handleCommissionRequired(Boolean commissionRequired, ValidationRequest request, Map<String, Object> processVariables, String processInstanceId, Ppm ppm) throws ComNotFound {
        if (commissionRequired) {
            // Only require comId if validate is true
            if (request.getValidate() != null && request.getValidate()) {
                if (request.getComId() == null) {
                    throw new MissingComIdException("comId is required when commissionRequired is true and validate is true");
                }
                Integer comId = request.getComId();

                Com com = comServiceImpl.findById(comId);
                if (com == null) {
                    throw new ComNotFound("Commission not found", List.of(comId));
                }

                // Ensure ppm is managed and fetch the current coms
                if (!entityManager.contains(ppm)) {
                    ppm = entityManager.merge(ppm);
                    log.info("Merged ppm to ensure managed state, initial coms: {}", ppm.getComs());
                }

                List<Com> currentComs = ppm.getComs();
                if (currentComs == null) {
                    currentComs = new ArrayList<>();
                    ppm.setComs(currentComs);
                    log.info("Initialized empty coms list for ppm");
                }

                // Check if com already exists to avoid duplicates
                boolean exists = currentComs.stream()
                        .anyMatch(existingCom -> existingCom.getId().equals(comId));
                if (!exists) {
                    currentComs.add(com);
                    log.info("Added new comId {} to coms list, current coms: {}", comId, currentComs);
                } else {
                    log.info("comId {} already exists in coms list: {}", comId, currentComs);
                }

                // Set the process variable
                runtimeService.setVariable(processInstanceId, "comId", comId);
                log.info("Saved comId {} as process variable for process instance {}", comId, processInstanceId);
            } else {
                log.info("validate is false or null, skipping comId requirement and addition for task process instance {}", processInstanceId);
            }
        }
    }
    private void validateUserCommissionAssociation(Map<String, Object> processVariables, JwtAuthenticationToken jwtAuth) {
        Integer comId = (Integer) processVariables.get("comId");
        if (comId != null) {
            List<Integer> userComIds = currentUserService.getCurrentUserComIds();
            if (userComIds == null || !userComIds.contains(comId)) {
                throw new UserNotAssociatedWithComException("User is not associated with the commission ID: " + comId);
            }
            log.info("User validated for commission ID: {}", comId);
        }
    }
}
