package com.sigmap.sigmap_ppm.services;

import com.sigmap.common.ac.AcServiceImpl;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComServiceImpl;
import com.sigmap.common.com.exception.CNCMPPNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.CncmpDecision;
import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.*;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationService;
import com.sigmap.sigmap_ppm.services.dto.request.GroupValidationRequest;
import com.sigmap.sigmap_ppm.services.dto.request.ValidationRequest;
import com.sigmap.sigmap_ppm.services.dto.response.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    @Autowired private RuntimeService runtimeService;
    @Autowired private TaskService taskService;
    @Autowired private KeycloakAdminService keycloakAdminService;
    @Autowired private HistoryService historyService;
    @Autowired private RepositoryService repositoryService;
    @Autowired private PpmService ppmService;
    @Autowired private CurrentUserService currentUserService;
    @Autowired private ComServiceImpl comServiceImpl;
    @Autowired private AcServiceImpl acServiceImpl;
    @Autowired private RealisationService realisationService;
    @Autowired private EntityManager entityManager;

    /* ======================= small helpers ======================= */

    private boolean hasRole(JwtAuthenticationToken jwtAuth, String role) {
        return jwtAuth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_" + role));
    }

    private boolean hasAssign(Task task, JwtAuthenticationToken jwtAuth) {
        return task.getAssignee() != null && task.getAssignee().equals(jwtAuth.getName());
    }

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

    private String getCandidateGroup(Task task) {
        return taskService.getIdentityLinksForTask(task.getId()).stream()
                .filter(link -> "candidate".equals(link.getType()) && link.getGroupId() != null)
                .map(IdentityLink::getGroupId)
                .findFirst()
                .orElse(null);
    }

    private boolean hasCandidateGroupFromModel(UserTask userTask, JwtAuthenticationToken jwtAuth) {
        List<String> candidateGroups = userTask.getCandidateGroups();
        if (candidateGroups == null || candidateGroups.isEmpty()) {
            log.warn("No candidate groups defined for task: {}", userTask.getId());
            return false;
        }
        return candidateGroups.stream().anyMatch(group -> hasRole(jwtAuth, group));
    }

    private Optional<UserTask> getModelUserTask(Task liveTask) {
        try {
            BpmnModel bpmn = repositoryService.getBpmnModel(liveTask.getProcessDefinitionId());
            FlowElement fe = bpmn.getMainProcess().getFlowElement(liveTask.getTaskDefinitionKey());
            if (fe instanceof UserTask ut) return Optional.of(ut);
        } catch (Exception e) {
            log.warn("getModelUserTask({}): {}", liveTask.getId(), e.getMessage());
        }
        return Optional.empty();
    }

    private boolean userTaskHasFormProp(UserTask ut, String propId) {
        if (ut.getFormProperties() == null) return false;
        return ut.getFormProperties().stream().anyMatch(fp -> propId.equals(fp.getId()));
    }

    private Map<String, Boolean> getTaskProperties(Task task) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("isUpdatable", false);
        properties.put("isPublishable", false);
        properties.put("commissionRequired", false);
        properties.put("meetingRequired", false);

        try {
            String processDefinitionId = task.getProcessDefinitionId();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

            if (flowElement instanceof UserTask userTask) {
                List<FormProperty> formProperties = userTask.getFormProperties();
                log.info("Form properties for task [{}]: {}", task.getId(), formProperties != null ? formProperties.size() : 0);
                if (formProperties != null) {
                    for (FormProperty formProperty : formProperties) {
                        if ("isUpdatable".equals(formProperty.getId())) {
                            properties.put("isUpdatable", Boolean.parseBoolean(formProperty.getDefaultExpression()));
                        }
                        if ("isPublishable".equals(formProperty.getId())) {
                            properties.put("isPublishable", Boolean.parseBoolean(formProperty.getDefaultExpression()));
                        }
                        if ("commissionRequired".equals(formProperty.getId())) {
                            properties.put("commissionRequired", Boolean.parseBoolean(formProperty.getDefaultExpression()));
                        }
                        if ("meetingRequired".equals(formProperty.getId())) {
                            properties.put("meetingRequired", Boolean.parseBoolean(formProperty.getDefaultExpression()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving properties for task [{}]: {}", task.getId(), e.getMessage(), e);
        }

        log.info("Returning task properties for task [{}]: {}", task.getId(), properties);
        return properties;
    }

    /* ======================= start process ======================= */

    @Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id, Integer version) throws PpmNotFound, AcNotFound {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("Invalid or missing JWT authentication");
            return errorResponse;
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        if (id == null || version == null) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus("ID and version must not be null");
            return errorResponse;
        }

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

        List<Realisation> realisations = ppm.getRealisations() != null ? ppm.getRealisations() : new ArrayList<>();
        for (Realisation realisation : realisations) {
            realisation.setStatut(RealisationStatus.NON_PUBLIE);
            realisationService.save(realisation);
        }

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

        ppmService.updateStatus(id, version, ProcessStatus.EN_TRAITEMENT);
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", id);
        variables.put("version", version);
        Integer acId = ppm.getAc() != null ? ppm.getAc().getId() : null;
        if (acId != null) {
            variables.put("acId", acId);
        }

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId(effectiveProcessKey, variables, TENANT_ID);
        String processInstanceId = processInstance.getProcessInstanceId();

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

    @Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id) throws PpmNotFound, AcNotFound {
        Integer latestVersion = ppmService.findLatestVersionById(id);
        return startProcessInstance(id, latestVersion);
    }

    /* ======================= read tasks ======================= */

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

            if (id != null && version != null) {
                Optional<Ppm> ppmOptional;
                try {
                    ppmOptional = ppmService.findById(id, version);
                } catch (PpmNotFound | AcNotFound e) {
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

        Map<String, Object> variables = new HashMap<>();
        variables.put("decision", processVariables.getOrDefault("decision", ""));
        variables.put("comment", processVariables.getOrDefault("comment", ""));
        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
        variables.put("treater", processVariables.getOrDefault("treater", ""));
        response.setTraceability(variables);

        Map<String, Boolean> taskProperties = getTaskProperties(task);
        response.setIsUpdatable(taskProperties.get("isUpdatable"));
        response.setIsPublishable(taskProperties.get("isPublishable"));
        response.setCommissionRequired(taskProperties.get("commissionRequired"));

        List<NextStepDTO> nextSteps = getPossibleNextSteps(task);
        response.setNextSteps(nextSteps);

        response.setStatus("Task details retrieved successfully");

        return response;
    }

    /* ======================= validate ======================= */

    @Transactional
    public List<TaskDTO> validateTaskGroup(List<GroupValidationRequest> requests)
            throws PpmNotFound, AcNotFound, MissingComIdException, MissingAcIdException,
            UserNotAssociatedWithAcException, ComNotFound {

        List<TaskDTO> responses = new ArrayList<>();

        for (GroupValidationRequest request : requests) {
            ValidationRequest validationRequest = new ValidationRequest();
            validationRequest.setValidate(request.getValidate());
            validationRequest.setComment(request.getComment());
            validationRequest.setPpm(request.getPpm());
            validationRequest.setComId(request.getComId());
            validationRequest.setCncmpDecision(request.getCncmpDecision());

            TaskDTO response = validateTask(request.getTaskId(), validationRequest);
            responses.add(response);
        }

        return responses;
    }

    @Transactional
    public TaskDTO validateTask(String taskId, ValidationRequest request)
            throws PpmNotFound, AcNotFound, MissingComIdException, MissingAcIdException,
            UserNotAssociatedWithAcException, ComNotFound {

        // --- Auth & task lookup ---
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

        // --- Process vars & PPM ---
        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        Integer id = (Integer) processVariables.getOrDefault("id", null);
        Integer version = (Integer) processVariables.getOrDefault("version", null);

        if (id == null || version == null) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus(String.format("Missing required process variables: id=%s, version=%s", id, version));
            return errorResponse;
        }

        Optional<Ppm> ppmOptional = ppmService.findById(id, version);
        if (ppmOptional.isEmpty()) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("No PPM entities found for id " + id + " and version " + version);
            return errorResponse;
        }
        Ppm latestPpm = ppmOptional.get();

        // --- Task properties ---
        Map<String, Boolean> taskProperties = getTaskProperties(task);
        Boolean isUpdatable        = taskProperties.get("isUpdatable");
        Boolean isPublishable      = taskProperties.get("isPublishable");
        Boolean commissionRequired = taskProperties.get("commissionRequired");
        Boolean meetingRequired    = taskProperties.get("meetingRequired");

        log.info("approve-path: currentTask [{}:{}] isUpdatable={}, meetingRequired={}, commissionRequired={}, publishable={}",
                task.getTaskDefinitionKey(), task.getName(), isUpdatable, meetingRequired, commissionRequired, isPublishable);

        // --- meetingRequired enforcement ---
        if (Boolean.TRUE.equals(meetingRequired)) {
            try {
                BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                FlowElement fe = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
                if (fe instanceof UserTask ut) {
                    List<String> modelGroups = ut.getCandidateGroups();
                    if (modelGroups != null && modelGroups.size() >= 2) {
                        String closerGroup = modelGroups.get(modelGroups.size() - 1);
                        if (!hasRole(jwtAuth, closerGroup)) {
                            TaskDTO errorResponse = new TaskDTO();
                            errorResponse.setStatus("Une réunion est requise. Seul le groupe de clôture peut terminer cette tâche.");
                            return errorResponse;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("meetingRequired enforcement: could not resolve candidate groups for task [{}]: {}", taskId, e.getMessage());
            }
        }

        // --- If editable, update PPM (versioning) ---
        if (Boolean.TRUE.equals(isUpdatable)) {
            if (request.getPpm() == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Ppm object is required in the request when isUpdatable is true");
                return errorResponse;
            }
            Ppm updatedPpm = ppmService.updateWithVersioning(id, request.getPpm());
            if (updatedPpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update PPM object");
                return errorResponse;
            }
            latestPpm = updatedPpm;
            runtimeService.setVariable(task.getProcessInstanceId(), "version", latestPpm.getVersion());
        }

        // --- Resolve next user task (best-effort for logging only) ---
        UserTask nextUserTask = null;
        Boolean nextMeetingRequired = null;
        Boolean nextCommissionRequired = null;
        if (Boolean.TRUE.equals(request.getValidate())) {
            try {
                BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                FlowElement taskElement = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

                if (taskElement instanceof UserTask ut) {
                    for (SequenceFlow flow : ut.getOutgoingFlows()) {
                        FlowElement target = bpmnModel.getMainProcess().getFlowElement(flow.getTargetRef());
                        if (target instanceof ExclusiveGateway gw) {
                            for (SequenceFlow gf : gw.getOutgoingFlows()) {
                                String val = extractConditionValue(gf.getConditionExpression());
                                if ("true".equalsIgnoreCase(val)) {
                                    FlowElement nxt = bpmnModel.getMainProcess().getFlowElement(gf.getTargetRef());
                                    if (nxt instanceof UserTask) nextUserTask = (UserTask) nxt;
                                    break;
                                }
                            }
                        } else if (target instanceof UserTask) {
                            nextUserTask = (UserTask) target;
                        }
                        if (nextUserTask != null) break;
                    }

                    if (nextUserTask == null) {
                        for (SequenceFlow flow : ut.getOutgoingFlows()) {
                            FlowElement target = bpmnModel.getMainProcess().getFlowElement(flow.getTargetRef());
                            if (target instanceof ExclusiveGateway gw) {
                                for (SequenceFlow gf : gw.getOutgoingFlows()) {
                                    FlowElement maybe = bpmnModel.getMainProcess().getFlowElement(gf.getTargetRef());
                                    if (maybe instanceof UserTask cand) {
                                        Boolean candMeeting = cand.getFormProperties().stream()
                                                .filter(fp -> "meetingRequired".equals(fp.getId()))
                                                .findFirst()
                                                .map(fp -> Boolean.parseBoolean(fp.getDefaultExpression()))
                                                .orElse(null);
                                        if (Boolean.TRUE.equals(candMeeting)) {
                                            nextUserTask = cand;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (nextUserTask != null) break;
                        }
                    }

                    if (nextUserTask != null) {
                        List<FormProperty> fps = nextUserTask.getFormProperties();
                        if (fps != null) {
                            for (FormProperty fp : fps) {
                                if ("meetingRequired".equals(fp.getId()))
                                    nextMeetingRequired = Boolean.parseBoolean(fp.getDefaultExpression());
                                if ("commissionRequired".equals(fp.getId()))
                                    nextCommissionRequired = Boolean.parseBoolean(fp.getDefaultExpression());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.info("Next-step resolution failed for task [{}]: {}", taskId, e.getMessage());
            }
        }

        log.info("approve-path: nextTask [{}] meetingRequired={}, commissionRequired={}",
                nextUserTask != null ? nextUserTask.getId() : "null", nextMeetingRequired, nextCommissionRequired);

        boolean skipComAssociationCheck = false;

        // --- Dynamic COM wiring (approve path) ---
        if (Boolean.TRUE.equals(request.getValidate())) {
            // AC validation (no meeting) → next meeting=true ⇒ set CPMP of AC
            if (!Boolean.TRUE.equals(meetingRequired) && Boolean.TRUE.equals(nextMeetingRequired)) {
                Integer acIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "acId");
                if (acIdVar == null) throw new MissingAcIdException("acId is required to route commission from AC validation");
                Integer cpmpComId = acServiceImpl.getComIdByAcId(acIdVar);
                if (cpmpComId == null) throw new ComNotFound("your ac dont have a cpmp", List.of(acIdVar));
                Com cpmpCom = comServiceImpl.findById(cpmpComId);
                if (cpmpCom == null) throw new ComNotFound("CPMP commission not found", List.of(cpmpComId));

                if (!entityManager.contains(latestPpm)) latestPpm = entityManager.merge(latestPpm);
                Ppm finalLatestPpm2 = latestPpm;
                List<Com> currentComs = Optional.ofNullable(latestPpm.getComs())
                        .orElseGet(() -> { List<Com> l = new ArrayList<>(); finalLatestPpm2.setComs(l); return l; });
                if (currentComs.stream().noneMatch(c -> c.getId().equals(cpmpComId))) currentComs.add(cpmpCom);

                runtimeService.setVariable(task.getProcessInstanceId(), "comId", cpmpComId);
                log.info("AC→CPMP: Set comId {} for process {}", cpmpComId, task.getProcessInstanceId());
            }

            // CPMP meeting → next needs commission ⇒ set CNCMPP (Permanent)
            if (Boolean.TRUE.equals(meetingRequired) && Boolean.TRUE.equals(nextCommissionRequired)) {
                Com cncmpPer;
                try {
                    cncmpPer = comServiceImpl.findCNCMPP();
                } catch (CNCMPPNotFound e) {
                    log.warn("CPMP→CNCMPP: permanent commission not found for process {}", task.getProcessInstanceId());
                    throw new ComNotFound("Aucune commission CNCMP Permanente n'est configurée. Impossible de passer à l’affectation.", List.of());
                }
                if (cncmpPer == null) {
                    log.warn("CPMP→CNCMPP: findCNCMPP() returned null for process {}", task.getProcessInstanceId());
                    throw new ComNotFound("Aucune commission CNCMP Permanente n'est configurée. Impossible de passer à l’affectation.", List.of());
                }

                if (!entityManager.contains(latestPpm)) latestPpm = entityManager.merge(latestPpm);
                Ppm finalLatestPpm1 = latestPpm;
                List<Com> currentComs = Optional.ofNullable(latestPpm.getComs())
                        .orElseGet(() -> { List<Com> l = new ArrayList<>(); finalLatestPpm1.setComs(l); return l; });
                if (currentComs.stream().noneMatch(c -> c.getId().equals(cncmpPer.getId()))) currentComs.add(cncmpPer);

                runtimeService.setVariable(task.getProcessInstanceId(), "comId", cncmpPer.getId());
                runtimeService.setVariable(task.getProcessInstanceId(), "cncmpPerComId", cncmpPer.getId()); // store permanent comId
                log.info("CPMP→CNCMPP: Set comId {} (Permanent) for process {}", cncmpPer.getId(), task.getProcessInstanceId());

                // skip association for this system hand-off
                skipComAssociationCheck = true;
            }
        }

        // --- Affectation: commissionRequired=true & not meeting ⇒ request.comId required ---
        if (Boolean.TRUE.equals(commissionRequired) && !Boolean.TRUE.equals(meetingRequired)) {
            if (Boolean.TRUE.equals(request.getValidate())) {
                Integer reqComId = request.getComId();
                if (reqComId == null) {
                    throw new MissingComIdException("comId is required at Affectation (commissionRequired=true) when validate is true");
                }
                Com com = comServiceImpl.findById(reqComId);
                if (com == null) throw new ComNotFound("Commission not found", List.of(reqComId));

                if (!entityManager.contains(latestPpm)) latestPpm = entityManager.merge(latestPpm);
                Ppm finalLatestPpm = latestPpm;
                List<Com> currentComs = Optional.ofNullable(latestPpm.getComs())
                        .orElseGet(() -> { List<Com> l = new ArrayList<>(); finalLatestPpm.setComs(l); return l; });
                if (currentComs.stream().noneMatch(c -> c.getId().equals(reqComId))) {
                    currentComs.add(com);
                    log.info("Affectation: Added comId {} to PPM", reqComId);
                }

                runtimeService.setVariable(task.getProcessInstanceId(), "comId", reqComId);
                runtimeService.setVariable(task.getProcessInstanceId(), "cncmpSpeComId", reqComId); // store specialized comId
                log.info("Affectation: Overwrote comId to {} for process {}", reqComId, task.getProcessInstanceId());
                skipComAssociationCheck = false;
            } else {
                log.info("Affectation rejected/neutral: comId not required.");
            }
        }

        /* -------- CNCMP Decision (APPROVE / REJECT / CI) — NO hardcoded task ids/names -------- */
        Boolean validateFlag = request.getValidate(); // may be nullified for decision routing
        boolean isCncmpDecisionTask = getModelUserTask(task)
                .map(ut -> userTaskHasFormProp(ut, "cncmpDecision"))
                .orElse(false);

        if (isCncmpDecisionTask) {
            CncmpDecision dec = request.getCncmpDecision();
            if (dec != null) {
                String pid = task.getProcessInstanceId();
                runtimeService.setVariable(pid, "cncmpDecision", dec.name()); // drive BPMN gateway

                switch (dec) {
                    case APPROVE:
                        // → BPMN will route to Publication via ${cncmpDecision=='APPROVE'}
                        validateFlag = null; // do not write *_valide
                        log.info("CNCMP Decision: APPROVE");
                        break;

                    case REJECT:
                        // → Route to Reserve (CPMP); ensure comId=CPMP by AC
                        Integer acIdVar = (Integer) runtimeService.getVariable(pid, "acId");
                        if (acIdVar == null) throw new MissingAcIdException("acId required to route REJECT to CPMP");
                        Integer cpmpComId = acServiceImpl.getComIdByAcId(acIdVar);
                        if (cpmpComId == null) throw new ComNotFound("your ac dont have a cpmp", List.of(acIdVar));
                        runtimeService.setVariable(pid, "comId", cpmpComId);
                        log.info("CNCMP Decision: REJECT → set comId to CPMP={}", cpmpComId);
                        validateFlag = null; // no *_valide
                        // association will be checked below (no skipping)
                        break;

                    case CI:
                        // → Route to Révision SCS (CI); restore specialized com if known
                        Integer speComId = (Integer) runtimeService.getVariable(pid, "cncmpSpeComId");
                        if (speComId != null) {
                            runtimeService.setVariable(pid, "comId", speComId);
                            log.info("CNCMP Decision: CI → set comId to specialized SCS={}", speComId);
                        } else {
                            log.warn("CNCMP Decision: CI but cncmpSpeComId not found; leaving comId unchanged");
                        }
                        validateFlag = null; // no *_valide
                        break;
                }
            }
        }

        // --- Association checks AFTER any comId overwrites ---
        Integer currComId = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
        if (!skipComAssociationCheck && currComId != null) {
            List<Integer> userComIds = currentUserService.getCurrentUserComIds();
            if (userComIds == null || !userComIds.contains(currComId)) {
                throw new UserNotAssociatedWithComException("User is not associated with the commission ID: " + currComId);
            }
            log.info("User validated for commission ID: {}", currComId);
        } else if (currComId == null) {
            Integer acIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "acId");
            if (acIdVar == null) {
                throw new MissingAcIdException("acId is required in process variables for tasks without comId");
            }
            List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
            if (userAcIds == null || !userAcIds.contains(acIdVar)) {
                throw new UserNotAssociatedWithAcException("User is not associated with the AC ID: " + acIdVar);
            }
            log.info("User validated for AC ID: {}", acIdVar);
        } else {
            log.info("Skipping commission association check at system hand-off (comId={})", currComId);
        }

        // --- Status updates ---
        ProcessStatus newStatus = null;
        boolean actedByDecision = isCncmpDecisionTask && request.getCncmpDecision() != null;

        if (Boolean.TRUE.equals(isPublishable)) {
            newStatus = ProcessStatus.PUBLIE;
            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
            if (updatedPpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update PPM status to Publiée");
                return errorResponse;
            }
            latestPpm = updatedPpm;

            List<Realisation> realisations = latestPpm.getRealisations() != null ? latestPpm.getRealisations() : new ArrayList<>();
            for (Realisation realisation : realisations) {
                realisation.setStatut(RealisationStatus.PUBLIE);
                realisationService.save(realisation);
            }
        } else if ((request.getValidate() != null) || actedByDecision) {
            // keep process "en traitement" after any validation/decision act
            newStatus = ProcessStatus.EN_TRAITEMENT;
            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
            if (updatedPpm == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Failed to update PPM status to En cours de traitement");
                return errorResponse;
            }
            latestPpm = updatedPpm;
        }

        // --- Determine gateway variable name & whether next is EndEvent ---
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
                                    break;
                                }
                            }
                        } else if (targetElement instanceof org.flowable.bpmn.model.EndEvent) {
                            nextTaskIsLast = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("Failed to extract gateway condition or determine next task for task [{}]: {}", taskId, e.getMessage());
        }

        // --- Write vars, finish task ---
        Map<String, Object> varsToSet = new HashMap<>();
        // IMPORTANT: only write *_valide when we actually have a boolean decision path
        if (validateFlag != null) {
            varsToSet.put(processVariable, validateFlag);
        }
        varsToSet.put("comment", request.getComment() != null ? request.getComment() : "");
        varsToSet.put("dateComment", LocalDateTime.now());
        varsToSet.put("treater", jwtAuth.getName());
        if (Boolean.TRUE.equals(isPublishable)) {
            varsToSet.put("isPublishable", true);
        }
        runtimeService.setVariables(task.getProcessInstanceId(), varsToSet);

        if (nextTaskIsLast && !Boolean.TRUE.equals(isPublishable)) {
            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), ProcessStatus.VALIDE);
            if (updatedPpm != null) {
                latestPpm = updatedPpm;
            }
        }

        ppmService.save(latestPpm);
        taskService.setAssignee(taskId, com.sigmap.common.security.Authentication.getCurrentUserId().get());
        if (request.getComment() != null) {
            taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
        }
        taskService.complete(taskId);

        // --- Response ---
        TaskDTO response = new TaskDTO();
        response.setTaskId(task.getId());
        response.setTaskName(task.getName());
        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
        response.setProcessInstanceId(task.getProcessInstanceId());
        response.setId(id);
        response.setVersion(latestPpm.getVersion());
        response.setNewStatus(newStatus != null ? newStatus : latestPpm.getStatus());
        response.setValidate(validateFlag != null ? validateFlag : request.getValidate());
        response.setTraceability(varsToSet);
        response.setIsUpdatable(isUpdatable);
        response.setIsPublishable(isPublishable);
        response.setStatus("Task validated successfully");
        return response;
    }

    /* ======================= next steps helper ======================= */

//    private List<NextStepDTO> getPossibleNextSteps(Task task) {
//        List<NextStepDTO> nextSteps = new ArrayList<>();
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
//                                if (conditionExpression != null) {
//                                    String variableName = extractVariableName(conditionExpression);
//                                    String conditionValue = extractConditionValue(conditionExpression);
//                                    if (variableName != null && conditionValue != null) {
//                                        FlowElement nextElement = bpmnModel.getFlowElement(gatewayFlow.getTargetRef());
//                                        String nextStepName = nextElement != null ? nextElement.getName() : "Unknown";
//                                        if (nextStepName == null || nextStepName.isEmpty()) {
//                                            nextStepName = nextElement != null ? nextElement.getId() : "Unknown";
//                                        }
//                                        NextStepDTO nextStep = new NextStepDTO();
//                                        nextStep.setValidationVariable(variableName);
//                                        nextStep.setValidationValue(String.valueOf(Boolean.parseBoolean(conditionValue)));
//                                        nextStep.setNextTaskName(nextStepName);
//                                        if (nextElement instanceof org.flowable.bpmn.model.EndEvent) {
//                                            nextStep.setActionLabel("Publier");
//                                        } else {
//                                            nextStep.setActionLabel(Boolean.parseBoolean(nextStep.getValidationValue()) ? "Validate" : "Reject");
//                                        }
//                                        nextSteps.add(nextStep);
//                                    }
//                                }
//                            }
//                        } else {
//                            String nextStepName = targetElement != null ? targetElement.getName() : "Complete";
//                            if (nextStepName == null || nextStepName.isEmpty()) {
//                                nextStepName = targetElement != null ? targetElement.getId() : "Complete";
//                            }
//                            NextStepDTO nextStep = new NextStepDTO();
//                            nextStep.setValidationVariable(null);
//                            nextStep.setValidationValue(null);
//                            nextStep.setNextTaskName(nextStepName);
//                            if (targetElement instanceof org.flowable.bpmn.model.EndEvent) {
//                                nextStep.setActionLabel("Publier");
//                            } else {
//                                nextStep.setActionLabel("Passer pour validation");
//                            }
//                            nextSteps.add(nextStep);
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.info("Failed to determine next steps for task [" + task.getId() + "]: " + e.getMessage());
//            NextStepDTO nextStep = new NextStepDTO();
//            nextStep.setValidationVariable(null);
//            nextStep.setValidationValue(null);
//            nextStep.setNextTaskName("Unknown");
//            nextStep.setActionLabel("Complete");
//            nextSteps.add(nextStep);
//        }
//        return nextSteps;
//    }
private List<NextStepDTO> getPossibleNextSteps(Task task) {
    List<NextStepDTO> nextSteps = new ArrayList<>();
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
                            if (conditionExpression != null) {
                                String variableName = extractVariableName(conditionExpression);
                                String rhs = extractConditionValue(conditionExpression); // boolean "true"/"false" or enum like "APPROVE"/"REJECT"/"CI"
                                if (variableName != null && rhs != null) {
                                    FlowElement nextElement = bpmnModel.getFlowElement(gatewayFlow.getTargetRef());
                                    String nextStepName = (nextElement != null && nextElement.getName() != null && !nextElement.getName().isEmpty())
                                            ? nextElement.getName()
                                            : (nextElement != null ? nextElement.getId() : "Unknown");

                                    NextStepDTO nextStep = new NextStepDTO();
                                    nextStep.setValidationVariable(variableName);
                                    nextStep.setValidationValue(rhs);
                                    nextStep.setNextTaskName(nextStepName);

                                    if (nextElement instanceof org.flowable.bpmn.model.EndEvent) {
                                        nextStep.setActionLabel("Publier");
                                    } else if ("true".equalsIgnoreCase(rhs)) {
                                        nextStep.setActionLabel("Validate");
                                    } else if ("false".equalsIgnoreCase(rhs)) {
                                        nextStep.setActionLabel("Reject");
                                    } else {
                                        nextStep.setActionLabel(variableName + " = " + rhs);
                                    }
                                    nextSteps.add(nextStep);
                                }
                            }
                        }
                    } else {
                        String nextStepName = (targetElement != null && targetElement.getName() != null && !targetElement.getName().isEmpty())
                                ? targetElement.getName()
                                : (targetElement != null ? targetElement.getId() : "Complete");

                        NextStepDTO nextStep = new NextStepDTO();
                        nextStep.setValidationVariable(null);
                        nextStep.setValidationValue(null);
                        nextStep.setNextTaskName(nextStepName);
                        if (targetElement instanceof org.flowable.bpmn.model.EndEvent) {
                            nextStep.setActionLabel("Publier");
                        } else {
                            nextStep.setActionLabel("Passer pour validation");
                        }
                        nextSteps.add(nextStep);
                    }
                }
            }
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


    /* ======================= list user tasks ======================= */

    public TaskListResponseDTO getUserTasks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
        if ((userAcIds == null || userAcIds.isEmpty()) && (userComIds == null || userComIds.isEmpty())) {
            log.warn("No AC IDs or comIds found for the current user");
            return new TaskListResponseDTO();
        }
        log.info("ac ids: {}, comIds: {}", userAcIds, userComIds);

        List<String> userCandidateGroups = jwtAuth.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
        log.info("User roles (candidate groups): {}", userCandidateGroups);

        if (userCandidateGroups.isEmpty()) {
            log.warn("User does not have any recognized roles");
            return new TaskListResponseDTO();
        }

        List<String> matchingProcessInstanceIds = new ArrayList<>();

        if (userAcIds != null && !userAcIds.isEmpty()) {
            for (Integer acId : userAcIds) {
                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                        .variableValueEquals("acId", acId)
                        .list();
                matchingProcessInstanceIds.addAll(
                        instances.stream().map(ProcessInstance::getId).collect(Collectors.toList())
                );
            }
        }
        if (userComIds != null && !userComIds.isEmpty()) {
            for (Integer comId : userComIds) {
                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                        .variableValueEquals("comId", comId)
                        .list();
                matchingProcessInstanceIds.addAll(
                        instances.stream().map(ProcessInstance::getId).collect(Collectors.toList())
                );
            }
        }

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

            if (id != null && version != null) {
                try {
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

    /* ======================= tiny parsing helpers ======================= */

    private String extractVariableName(String conditionExpression) {
        if (conditionExpression == null) return null;
        String cleanedExpression = conditionExpression.trim().replace("#{", "${");
        Pattern pattern = Pattern.compile("\\$\\{([^\\s}]+?)\\s*(==|!=)\\s*(true|false|['\"][^'\"]+['\"])?\\}");
        Matcher matcher = pattern.matcher(cleanedExpression);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        if (cleanedExpression.startsWith("${") && cleanedExpression.endsWith("}")) {
            String variableName = cleanedExpression.substring(2, cleanedExpression.length() - 1).trim();
            if (!variableName.isEmpty()) return variableName;
        }
        return null;
    }

    private String extractConditionValue(String conditionExpression) {
        if (conditionExpression == null) return null;
        String cleaned = conditionExpression.trim().replace("#{", "${");

        // boolean
        Matcher m = Pattern.compile("\\$\\{[^}]*?(==|!=)\\s*(true|false)\\s*\\}").matcher(cleaned);
        if (m.find()) return m.group(2).toLowerCase(); // "true" or "false"

        // single-quoted string
        m = Pattern.compile("\\$\\{[^}]*?(==|!=)\\s*'([^']+)'\\s*\\}").matcher(cleaned);
        if (m.find()) return m.group(2); // e.g. CI / APPROVE / REJECT

        // double-quoted string
        m = Pattern.compile("\\$\\{[^}]*?(==|!=)\\s*\"([^\"]+)\"\\s*\\}").matcher(cleaned);
        if (m.find()) return m.group(2);

        return null;
    }

    /* ======================= composite: get PPM + task ======================= */

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

        Integer latestVersion = ppmService.findLatestVersionById(ppmId);
        Optional<Ppm> ppmOptional = ppmService.findById(ppmId, latestVersion);
        if (ppmOptional.isEmpty()) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus("PPM non trouvé pour l'ID: " + ppmId);
            return errorResponse;
        }

        Ppm ppm = ppmOptional.get();

        TaskDetailsDTO response = new TaskDetailsDTO();

        if (ppm.getDocs() != null) {
            List<Docs> nonDeletedDocs = ppm.getDocs().stream()
                    .filter(doc -> !Boolean.TRUE.equals(doc.getIsDeleted()))
                    .collect(Collectors.toList());
            ppm.setDocs(nonDeletedDocs);
        }
        PpmResponseDto ppmResponse = PpmMapper.INSTANCE.mapToDto(ppm);
        response.setPpm(ppmResponse);

        String processInstanceId = ppm.getProcessInstanceId();
        if (processInstanceId == null) {
            response.setHasTask(false);
            response.setStatus("PPM récupéré avec succès, aucune tâche associée");
            return response;
        }

        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskTenantId(TENANT_ID)
                .active()
                .singleResult();

        if (task == null) {
            response.setHasTask(false);
            response.setStatus("PPM récupéré avec succès, aucune tâche active associée");
            return response;
        }

        boolean userCanViewTask = hasCandidateGroup(task, jwtAuth) || hasAssign(task, jwtAuth);
        if (!userCanViewTask) {
            log.warn("L'utilisateur {} n'a pas les droits pour voir la tâche {} du PPM {}", jwtAuth.getName(), task.getId(), ppmId);
            response.setHasTask(false);
            response.setStatus("PPM récupéré avec succès, une tâche existe mais l'utilisateur n'a pas les droits de la voir");
            return response;
        }

        response.setHasTask(true);
        response.setTaskId(task.getId());
        response.setTaskName(task.getName());
        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
        response.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
        response.setProcessInstanceId(task.getProcessInstanceId());
        response.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        response.setCandidateGroup(getCandidateGroup(task));

        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        Map<String, Object> variables = new HashMap<>();
        variables.put("decision", processVariables.getOrDefault("decision", ""));
        variables.put("comment", processVariables.getOrDefault("comment", ""));
        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
        variables.put("treater", processVariables.getOrDefault("treater", ""));
        response.setTraceability(variables);

        Map<String, Boolean> taskProperties = getTaskProperties(task);
        response.setIsUpdatable(taskProperties.get("isUpdatable"));
        response.setIsPublishable(taskProperties.get("isPublishable"));
        response.setCommissionRequired(taskProperties.get("commissionRequired"));

        Optional<Integer> ComId = Optional.ofNullable((Integer) runtimeService.getVariable(processInstanceId, "comId"));
        ComId.ifPresent(response::setComId);

        List<NextStepDTO> nextSteps = getPossibleNextSteps(task);
        response.setNextSteps(nextSteps);

        response.setStatus("PPM et détails de la tâche récupérés avec succès");
        return response;
    }

    /* ======================= history ======================= */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<HistoricTaskDto> loadHistoricTasks(String processInstId) {
        return historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstId)
                .finished()
                .list()
                .stream()
                .map(task -> {
                    List<Comment> comments = taskService.getTaskComments(task.getId());
                    return new HistoricTaskDto(task, comments);
                })
                .map(h -> {
                    if (h.getAssignee() != null) {
                        UserDTO user = keycloakAdminService.getUserById(h.getAssignee());
                        if (user != null) {
                            h.setConcern(user.getFirstName().concat(" ").concat(user.getLastName()));
                        }
                    }
                    return h;
                })
                .collect(Collectors.toList());
    }
}
