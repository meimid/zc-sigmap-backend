package com.sigmap.sigmap_ppm.services;

import com.sigmap.common.ac.AcServiceImpl;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComServiceImpl;
import com.sigmap.common.com.exception.CNCMPPNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.CncmpDecision;
import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ppm_avis_note_rapporteur.PpmAvisNoteRapporteurService;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.*;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationService;
import com.sigmap.common.reunionPpm.ReunionPpmRepository;
import com.sigmap.common.reunionPpm.ReunionPpmSpecifications;
import com.sigmap.common.user_com.UserComRepository;
import com.sigmap.sigmap_ppm.services.dto.request.GroupValidationRequest;
import com.sigmap.sigmap_ppm.services.dto.request.ValidationRequest;
import com.sigmap.sigmap_ppm.services.dto.response.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.flowable.bpmn.model.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.identitylink.api.IdentityLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;

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
    private KeycloakAdminService keycloakAdminService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private PpmService ppmService;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private ComServiceImpl comServiceImpl;
    @Autowired
    private AcServiceImpl acServiceImpl;
    @Autowired
    private RealisationService realisationService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private PpmRepository ppmRepository;
    @Autowired
    private ReunionPpmRepository reunionPpmRepository;
    @Autowired
    private PpmAvisNoteRapporteurService ppmAvisNoteRapporteurService;
    @Autowired
    private UserComRepository userComRepository;


    /* ======================= small helpers ======================= */

    private boolean hasRole(JwtAuthenticationToken jwtAuth, String role) {
        return jwtAuth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_" + role));
    }

    // Utility method to check if the task is assigned to the current user
    private boolean hasAssign(Task task, JwtAuthenticationToken jwtAuth) {
        return task.getAssignee() != null && task.getAssignee().equals(jwtAuth.getName());
    }

    // Utility method to check if the task's candidate group matches any of the
    // user's roles
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
            if (fe instanceof UserTask ut)
                return Optional.of(ut);
        } catch (Exception e) {
            log.warn("getModelUserTask({}): {}", liveTask.getId(), e.getMessage());
        }
        return Optional.empty();
    }

    private boolean userTaskHasFormProp(UserTask ut, String propId) {
        if (ut.getFormProperties() == null)
            return false;
        return ut.getFormProperties().stream().anyMatch(fp -> propId.equals(fp.getId()));
    }

    private Map<String, Boolean> getTaskProperties(Task task) {
        Map<String, Boolean> properties = new HashMap<>();
        properties.put("isUpdatable", false);
        properties.put("isPublishable", false);
        properties.put("commissionRequired", false);
        properties.put("meetingRequired", false);
        properties.put("assignRapporteur", false);
        properties.put("switchBackToPerAfterCompletion", false);

        try {
            String processDefinitionId = task.getProcessDefinitionId();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

            if (flowElement instanceof UserTask userTask) {
                List<FormProperty> formProperties = userTask.getFormProperties();
                log.info("Form properties for task [{}]: {}", task.getId(),
                        formProperties != null ? formProperties.size() : 0);
                if (formProperties != null) {
                    for (FormProperty formProperty : formProperties) {
                        if ("isUpdatable".equals(formProperty.getId())) {
                            properties.put("isUpdatable", Boolean.parseBoolean(formProperty.getDefaultExpression()));
                        }
                        if ("isPublishable".equals(formProperty.getId())) {
                            properties.put("isPublishable", Boolean.parseBoolean(formProperty.getDefaultExpression()));
                        }
                        if ("commissionRequired".equals(formProperty.getId())) {
                            properties.put("commissionRequired",
                                    Boolean.parseBoolean(formProperty.getDefaultExpression()));
                        }
                        if ("meetingRequired".equals(formProperty.getId())) {
                            properties.put("meetingRequired",
                                    Boolean.parseBoolean(formProperty.getDefaultExpression()));
                        }
                        if ("assignRapporteur".equals(formProperty.getId())) {
                            properties.put("assignRapporteur",
                                    Boolean.parseBoolean(formProperty.getDefaultExpression()));
                        }

                        if ("switchBackToPerAfterCompletion".equals(formProperty.getId())) {
                            properties.put("switchBackToPerAfterCompletion",
                                    Boolean.parseBoolean(formProperty.getDefaultExpression()));
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
    private String resolveValidateProcessVariable(String candidateGroup) {
        if (candidateGroup == null) {
            return "valide";
        }

        return switch (candidateGroup) {
            case "CPMP-FILTRATION" -> "cpmpFiltrageValide";
            case "CNCMP-SPE-AFFECTATION" -> "specializedAffectationValide";
            case "CNCMP-FILTRATION" -> "cncmpFiltrageValide";
            default -> candidateGroup.toLowerCase() + "_valide";
        };
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

        // Set associated Realisation statuses to NON_PUBLIE
        List<Realisation> realisations = ppm.getRealisations() != null ? ppm.getRealisations() : new ArrayList<>();
        for (Realisation realisation : realisations) {
            realisation.setStatut(RealisationStatus.NON_PUBLIE);
            realisationService.save(realisation);
            log.info("Set Realisation [id={}, version={}] status to NON_PUBLIE", realisation.getId(),
                    realisation.getVersion());
        }

        // Use provided processKey or fallback to default
        String effectiveProcessKey = (processKey != null && !processKey.isEmpty()) ? processKey : this.processKey;
        String effectiveTaskKey = (initialTaskKey != null && !initialTaskKey.isEmpty()) ? initialTaskKey
                : this.initialTaskKey;

        // Pre-check the role by inspecting the BPMN model for the initial task's
        // candidate groups
        BpmnModel bpmnModel = repositoryService.getBpmnModel(
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(effectiveProcessKey)
                        .latestVersion()
                        .singleResult()
                        .getId());
        FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(effectiveTaskKey);
        if (!(flowElement instanceof UserTask)) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus(
                    String.format("Task %s not found or not a user task in the process definition", effectiveTaskKey));
            return errorResponse;
        }

        UserTask initialTaskModel = (UserTask) flowElement;
        List<String> taskCandidateGroups = initialTaskModel.getCandidateGroups();
        if (taskCandidateGroups == null || taskCandidateGroups.isEmpty()) {
            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
            errorResponse.setStatus(String.format("No candidate groups defined for task %s in the process definition",
                    effectiveTaskKey));
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
        variables.put("isCondApprove", false);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId(effectiveProcessKey,
                variables, TENANT_ID);
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

        Integer ppmId = (Integer) runtimeService.getVariable(processInstanceId, "id");
        Integer ppmVer = (Integer) runtimeService.getVariable(processInstanceId, "version");

        taskService.complete(initialTask.getId());

        updatePpmEtapeProcess(processInstanceId, ppmId, ppmVer);

        ProcessStartResponseDTO response = new ProcessStartResponseDTO();
        response.setProcessInstanceId(processInstance.getId());
        response.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        response.setTaskId(initialTask.getId());
        response.setAcId(acId);
        response.setStatus(String.format("Process %s started and task %s completed successfully", effectiveProcessKey,
                effectiveTaskKey));

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
            log.info("Process variables for {} : {}", id, version);

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
                    taskInfo.setModelAlfrescoId(ppm.getModelAlfrescoId());
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
            // ADDED: expose meetingRequired + comId on TaskDTO
            taskInfo.setMeetingRequired(taskProperties.get("meetingRequired")); // ADDED
            Integer comIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId"); // ADDED
            if (comIdVar != null)
                taskInfo.setComId(comIdVar); // ADDED

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
        response.setIsUpdatable(taskProperties.get("isUpdatable"));
        response.setIsPublishable(taskProperties.get("isPublishable"));
        response.setCommissionRequired(taskProperties.get("commissionRequired"));
        // ADDED: expose meetingRequired + comId on TaskDetailsDTO
        response.setMeetingRequired(taskProperties.get("meetingRequired")); // ADDED
        Integer comIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId"); // ADDED
        if (comIdVar != null)
            response.setComId(comIdVar); // ADDED

        List<NextStepDTO> nextSteps = getPossibleNextSteps(task);
        log.info("Setting nextSteps for task [{}]: {}", taskId, nextSteps);
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
            validationRequest.setFromMeeting(true);

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

        Map<String, Object> varsToSet = new HashMap<>();

        // Get candidate group
        String candidateGroup = getCandidateGroup(task);

        // Check user permissions
        if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
            TaskDTO errorResponse = new TaskDTO();
            errorResponse.setStatus("User does not have the required role or assignment to validate this task");
            return errorResponse;
        }

        // --- Process vars & PPM ---
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

        // --- Task properties ---
        Map<String, Boolean> taskProperties = getTaskProperties(task);
        Boolean isUpdatable = taskProperties.get("isUpdatable");
        Boolean isPublishable = taskProperties.get("isPublishable");
        Boolean commissionRequired = taskProperties.get("commissionRequired");
        Boolean meetingRequired = taskProperties.get("meetingRequired");
        Boolean assignRapporteur = taskProperties.get("assignRapporteur");
        Boolean switchBackToPerAfterCompletion = taskProperties.get("switchBackToPerAfterCompletion");
        Boolean affectationRequired = taskProperties.get("affectationRequired");

        boolean fromMeeting = Boolean.TRUE.equals(request.getFromMeeting());

        log.info(
                "approve-path: currentTask [{}:{}] isUpdatable={}, meetingRequired={}, commissionRequired={}, assignRapporteur={}, switchBackToPerAfterCompletion={}, publishable={}, fromMeeting={}",
                task.getTaskDefinitionKey(), task.getName(), isUpdatable, meetingRequired, commissionRequired,
                assignRapporteur, switchBackToPerAfterCompletion, isPublishable, fromMeeting);

        // --- meetingRequired enforcement ---
        if (Boolean.TRUE.equals(meetingRequired)) {
            if (!fromMeeting) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus(
                        "Une réunion est requise pour clôturer cette tâche. Utilisez le service dédié à la réunion.");
                return errorResponse;
            }
            try {
                BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                FlowElement fe = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
                if (fe instanceof UserTask ut) {
                    List<String> modelGroups = ut.getCandidateGroups();
                    if (modelGroups != null && modelGroups.size() >= 2) {
                        String closerGroup = modelGroups.get(modelGroups.size() - 1);
                        if (!hasRole(jwtAuth, closerGroup)) {
                            TaskDTO errorResponse = new TaskDTO();
                            errorResponse.setStatus(
                                    "Une réunion est requise. Seul le groupe de clôture peut terminer cette tâche.");
                            return errorResponse;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("meetingRequired enforcement: could not resolve candidate groups for task [{}]: {}", taskId,
                        e.getMessage());
            }
        }

        // ==========================================================
        // SAVE DRAFT (RECTIF) LOGIC
        // ==========================================================
        boolean isDraftSave = (request.getValidate() == null);

        if (Boolean.TRUE.equals(isUpdatable)) {
            if (request.getPpm() == null) {
                TaskDTO errorResponse = new TaskDTO();
                errorResponse.setStatus("Ppm object is required in the request when isUpdatable is true");
                return errorResponse;
            }

            if (isDraftSave) {
                String pid = task.getProcessInstanceId();

                String rectifDraftTaskId = null;
                try {
                    rectifDraftTaskId = (String) runtimeService.getVariable(pid, "rectifDraftTaskId");
                } catch (Exception ignore) {
                }
                if (rectifDraftTaskId == null || !rectifDraftTaskId.equals(task.getId())) {
                    try {
                        runtimeService.removeVariable(pid, "rectifDraftVersion");
                    } catch (Exception ignore) {
                    }
                    runtimeService.setVariable(pid, "rectifDraftTaskId", task.getId());
                }

                Integer rectifDraftVersion = null;
                try {
                    rectifDraftVersion = (Integer) runtimeService.getVariable(pid, "rectifDraftVersion");
                } catch (Exception ignore) {
                }

                Ppm updatedPpm;

                if (rectifDraftVersion == null) {
                    log.info("RECTIF DRAFT: first save draft => versioning from (id={},ver={})", id, version);
                    updatedPpm = ppmService.updateWithVersioning(id, version, request.getPpm());
                    if (updatedPpm == null) {
                        TaskDTO errorResponse = new TaskDTO();
                        errorResponse.setStatus("Failed to save draft (versioning)");
                        return errorResponse;
                    }

                    if (updatedPpm.getVersion() != null && !updatedPpm.getVersion().equals(version)) {
                        runtimeService.setVariable(pid, "rectifDraftVersion", updatedPpm.getVersion());
                        runtimeService.setVariable(pid, "version", updatedPpm.getVersion());
                        log.info("RECTIF DRAFT: created draft version={} for pid={}", updatedPpm.getVersion(), pid);
                    } else {
                        runtimeService.setVariable(pid, "version", version);
                        log.info(
                                "RECTIF DRAFT: no changes detected => no version bump, keep rectifDraftVersion NULL (pid={})",
                                pid);
                    }

                    latestPpm = updatedPpm;

                } else {
                    if (!(request.getPpm() instanceof PpmWithLotsRequestDto)) {
                        TaskDTO errorResponse = new TaskDTO();
                        errorResponse.setStatus("Draft update requires PpmWithLotsRequestDto payload");
                        return errorResponse;
                    }

                    log.info("RECTIF DRAFT: subsequent save => update in place (id={},ver={})", id, rectifDraftVersion);
                    updatedPpm = ppmService.updateWithRelationships(id, rectifDraftVersion,
                            (PpmWithLotsRequestDto) request.getPpm());

                    if (updatedPpm == null) {
                        TaskDTO errorResponse = new TaskDTO();
                        errorResponse.setStatus("Failed to save draft (update in place)");
                        return errorResponse;
                    }

                    runtimeService.setVariable(pid, "version", rectifDraftVersion);
                    latestPpm = updatedPpm;

                    log.info("RECTIF DRAFT: updated in place draft version={} for pid={}", rectifDraftVersion, pid);
                }

                varsToSet.put("comment", request.getComment() != null ? request.getComment() : "");
                varsToSet.put("dateComment", LocalDateTime.now());
                varsToSet.put("treater", jwtAuth.getName());
                if (Boolean.TRUE.equals(isPublishable)) {
                    varsToSet.put("isPublishable", true);
                }

                runtimeService.setVariables(pid, varsToSet);

                ppmService.save(latestPpm);

                try {
                    Integer ppmIdDraft = (Integer) runtimeService.getVariable(pid, "id");
                    Integer ppmVerDraft = (Integer) runtimeService.getVariable(pid, "version");
                    updatePpmEtapeProcess(pid, ppmIdDraft, ppmVerDraft);
                } catch (Exception e) {
                    log.warn("RECTIF DRAFT: updatePpmEtapeProcess failed for pid={}: {}", pid, e.getMessage());
                }

                TaskDTO response = new TaskDTO();
                response.setTaskId(task.getId());
                response.setTaskName(task.getName());
                response.setTaskDefinitionKey(task.getTaskDefinitionKey());
                response.setProcessInstanceId(pid);
                response.setId(id);
                response.setVersion((Integer) runtimeService.getVariable(pid, "version"));
                response.setNewStatus(latestPpm.getStatus());
                response.setValidate(null);
                response.setTraceability(varsToSet);
                response.setIsUpdatable(isUpdatable);
                response.setIsPublishable(isPublishable);
                response.setMeetingRequired(meetingRequired);

                try {
                    Integer comIdSnapshot = (Integer) runtimeService.getVariable(pid, "comId");
                    if (comIdSnapshot != null) {
                        response.setComId(comIdSnapshot);
                    }
                } catch (Exception ignore) {
                }

                response.setStatus("Draft saved successfully");
                return response;
            }

            // --- Normal path (validate != null): update PPM with versioning BEFORE completing ---
            log.info("Inspecting request.getPpm() for task [{}]: {}", taskId, request.getPpm());

            String pid = task.getProcessInstanceId();
            Integer rectifDraftVersionForValidate = null;
            try {
                rectifDraftVersionForValidate = (Integer) runtimeService.getVariable(pid, "rectifDraftVersion");
            } catch (Exception ignore) {
            }

            if (rectifDraftVersionForValidate != null) {
                if (!(request.getPpm() instanceof PpmWithLotsRequestDto)) {
                    TaskDTO errorResponse = new TaskDTO();
                    errorResponse.setStatus(
                            "Validation update requires PpmWithLotsRequestDto payload when rectifDraftVersion exists");
                    return errorResponse;
                }

                log.info("RECTIF VALIDATE: update in place (id={},ver={}) instead of bumping again", id,
                        rectifDraftVersionForValidate);
                Ppm updatedPpm = ppmService.updateWithRelationships(id, rectifDraftVersionForValidate,
                        (PpmWithLotsRequestDto) request.getPpm());

                if (updatedPpm == null) {
                    TaskDTO errorResponse = new TaskDTO();
                    errorResponse.setStatus("Failed to update PPM object (rectif validate update in place)");
                    return errorResponse;
                }

                latestPpm = updatedPpm;
                runtimeService.setVariable(pid, "version", rectifDraftVersionForValidate);
                log.info("Updated ppm after update-in-place, coms: {}", latestPpm.getComs());
            } else {
                Ppm updatedPpm = ppmService.updateWithVersioning(id, version, request.getPpm());
                if (updatedPpm == null) {
                    TaskDTO errorResponse = new TaskDTO();
                    errorResponse.setStatus("Failed to update PPM object");
                    return errorResponse;
                }
                latestPpm = updatedPpm;
                runtimeService.setVariable(task.getProcessInstanceId(), "version", latestPpm.getVersion());
                log.info("Updated ppm after versioning, coms: {}", latestPpm.getComs());
            }
        }
        // ==========================================================

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
                                    if (nxt instanceof UserTask) {
                                        nextUserTask = (UserTask) nxt;
                                    }
                                    break;
                                }
                            }
                        } else if (target instanceof UserTask) {
                            nextUserTask = (UserTask) target;
                        }
                        if (nextUserTask != null) {
                            break;
                        }
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
                                if (nextUserTask != null) {
                                    break;
                                }
                            }
                        }
                    }

                    if (nextUserTask != null) {
                        List<FormProperty> fps = nextUserTask.getFormProperties();
                        if (fps != null) {
                            for (FormProperty fp : fps) {
                                if ("meetingRequired".equals(fp.getId())) {
                                    nextMeetingRequired = Boolean.parseBoolean(fp.getDefaultExpression());
                                }
                                if ("commissionRequired".equals(fp.getId())) {
                                    nextCommissionRequired = Boolean.parseBoolean(fp.getDefaultExpression());
                                }
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
        Integer deferComIdAfterCompletion = null;

        // --- Dynamic COM wiring (approve path) ---
        if (Boolean.TRUE.equals(request.getValidate())) {
            log.info("Inspecting request for task [{}]: {}", taskId, request.getValidate());

            Integer currentComIdForRouting = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");

            // AC validation → next commission step ⇒ set CPMP of AC
            if (!Boolean.TRUE.equals(meetingRequired)
                    && (
                            // Nouveau process
                            Boolean.TRUE.equals(nextCommissionRequired)
                            ||
                            // Ancien process
                            Boolean.TRUE.equals(nextMeetingRequired))
                    && !Boolean.TRUE.equals(commissionRequired)
                    && !Boolean.TRUE.equals(assignRapporteur)
                    && (currentComIdForRouting == null || currentComIdForRouting == 0)) {

                Integer acIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "acId");
                if (acIdVar == null) {
                    throw new MissingAcIdException("acId is required to route commission from AC validation");
                }

                Integer cpmpComId = acServiceImpl.getComIdByAcId(acIdVar);
                if (cpmpComId == null) {
                    throw new ComNotFound("your ac dont have a cpmp", List.of(acIdVar));
                }

                Com cpmpCom = comServiceImpl.findById(cpmpComId);
                if (cpmpCom == null) {
                    throw new ComNotFound("CPMP commission not found", List.of(cpmpComId));
                }

                if (!entityManager.contains(latestPpm)) {
                    latestPpm = entityManager.merge(latestPpm);
                }

                Ppm finalLatestPpm2 = latestPpm;
                List<Com> currentComs = Optional.ofNullable(latestPpm.getComs())
                        .orElseGet(() -> {
                            List<Com> l = new ArrayList<>();
                            finalLatestPpm2.setComs(l);
                            return l;
                        });

                if (currentComs.stream().noneMatch(c -> c.getId().equals(cpmpComId))) {
                    currentComs.add(cpmpCom);
                }

                runtimeService.setVariable(task.getProcessInstanceId(), "comId", cpmpComId);

                log.info("AC→CPMP: Set comId {} for process {}", cpmpComId, task.getProcessInstanceId());

                skipComAssociationCheck = true;
            }

            // CPMP meeting → next needs commission ⇒ prepare CNCMPP (Permanent)
            if (Boolean.TRUE.equals(meetingRequired) && Boolean.TRUE.equals(nextCommissionRequired)) {
                Com cncmpPer;
                try {
                    cncmpPer = comServiceImpl.findCNCMPP();
                } catch (CNCMPPNotFound e) {
                    log.warn("CPMP→CNCMPP: permanent commission not found for process {}", task.getProcessInstanceId());
                    throw new ComNotFound(
                            "Aucune commission CNCMP Permanente n'est configurée. Impossible de passer à l’affectation.",
                            List.of());
                }

                if (cncmpPer == null) {
                    log.warn("CPMP→CNCMPP: findCNCMPP() returned null for process {}", task.getProcessInstanceId());
                    throw new ComNotFound(
                            "Aucune commission CNCMP Permanente n'est configurée. Impossible de passer à l’affectation.",
                            List.of());
                }

                if (!entityManager.contains(latestPpm)) {
                    latestPpm = entityManager.merge(latestPpm);
                }

                Ppm finalLatestPpm1 = latestPpm;
                List<Com> currentComs = Optional.ofNullable(latestPpm.getComs())
                        .orElseGet(() -> {
                            List<Com> l = new ArrayList<>();
                            finalLatestPpm1.setComs(l);
                            return l;
                        });

                if (currentComs.stream().noneMatch(c -> c.getId().equals(cncmpPer.getId()))) {
                    currentComs.add(cncmpPer);
                }

                deferComIdAfterCompletion = cncmpPer.getId();
                varsToSet.put("cncmpPerComId", cncmpPer.getId());

                log.info("CPMP→CNCMPP: defer switch to PER comId {} after completion", cncmpPer.getId());
            }
        }

        /* -------- CNCMP Decision (APPROVE / REJECT / CI ) -------- */
        Boolean validateFlag = request.getValidate();
        boolean isCncmpDecisionTask = getModelUserTask(task)
                .map(ut -> userTaskHasFormProp(ut, "cncmpDecision"))
                .orElse(false);

        Integer deferComIdAfterDecision = null;

        if (isCncmpDecisionTask) {
            CncmpDecision dec = request.getCncmpDecision();
            if (dec != null) {
                String pid = task.getProcessInstanceId();

                Integer perId = (Integer) processVariables.get("cncmpPerComId");
                if (perId != null) {
                    varsToSet.put("comId", perId);
                    try {
                        if (!entityManager.contains(latestPpm)) {
                            latestPpm = entityManager.merge(latestPpm);
                        }
                        Ppm finalLatestPpm4 = latestPpm;
                        List<Com> current = Optional.ofNullable(latestPpm.getComs())
                                .orElseGet(() -> {
                                    List<Com> l = new ArrayList<>();
                                    finalLatestPpm4.setComs(l);
                                    return l;
                                });
                        boolean exists = current.stream().anyMatch(c -> c.getId().equals(perId));
                        if (!exists) {
                            Com per = comServiceImpl.findById(perId);
                            if (per != null) {
                                current.add(per);
                                ppmService.save(latestPpm);
                            }
                        }
                        log.info("Association: added PER comId {} to PPM (id={}, ver={})", perId, latestPpm.getId(),
                                latestPpm.getVersion());
                    } catch (Exception ignore) {
                    }
                }
                varsToSet.put("cncmpDecision", dec.name());
                varsToSet.put("isCondApprove", dec == CncmpDecision.COND_APPROVE);

                switch (dec) {
                    case APPROVE:
                        deferComIdAfterDecision = null;
                        validateFlag = null;
                        log.info("CNCMP Decision: APPROVE");
                        break;

                    case REJECT_END:
                        validateFlag = null;
                        log.info("CNCMP Decision: REJECT");
                        break;

                    case REJECT:
                        Integer acIdVarRej = (Integer) runtimeService.getVariable(pid, "acId");
                        if (acIdVarRej == null) {
                            throw new MissingAcIdException("acId required to route REPORT to CPMP");
                        }
                        Integer cpmpComIdRej = acServiceImpl.getComIdByAcId(acIdVarRej);
                        if (cpmpComIdRej == null) {
                            throw new ComNotFound("your ac dont have a cpmp", List.of(acIdVarRej));
                        }
                        deferComIdAfterDecision = cpmpComIdRej;
                        validateFlag = null;
                        log.info("CNCMP Decision: REPORT → defer comId to CPMP={}", cpmpComIdRej);
                        break;

                    case REPORT:
                        Integer acIdVarR = (Integer) runtimeService.getVariable(pid, "acId");
                        if (acIdVarR == null) {
                            throw new MissingAcIdException("acId required to route REPORT to CPMP");
                        }
                        Integer cpmpComIdR = acServiceImpl.getComIdByAcId(acIdVarR);
                        if (cpmpComIdR == null) {
                            throw new ComNotFound("your ac dont have a cpmp", List.of(acIdVarR));
                        }
                        deferComIdAfterDecision = cpmpComIdR;
                        validateFlag = null;
                        log.info("CNCMP Decision: REPORT → defer comId to CPMP={}", cpmpComIdR);
                        break;

                    case COND_APPROVE:
                        Integer acIdVarC = (Integer) runtimeService.getVariable(pid, "acId");
                        if (acIdVarC == null) {
                            throw new MissingAcIdException("acId required to route COND_APPROVE to CPMP");
                        }
                        Integer cpmpComIdC = acServiceImpl.getComIdByAcId(acIdVarC);
                        if (cpmpComIdC == null) {
                            throw new ComNotFound("your ac dont have a cpmp", List.of(acIdVarC));
                        }
                        deferComIdAfterDecision = cpmpComIdC;
                        validateFlag = null;
                        varsToSet.put("isCondApprove", true);
                        log.info("CNCMP Decision: COND_APPROVE → defer comId to CPMP={} and set isCondApprove=true",
                                cpmpComIdC);
                        break;

                    case CI:
                        Integer speComId = (Integer) runtimeService.getVariable(pid, "cncmpSpeComId");
                        deferComIdAfterDecision = speComId;
                        validateFlag = null;
                        log.info("CNCMP Decision: CI → defer comId to SCS={}", speComId);
                        break;
                }
            }
        }

        // --- Specialized rapporteur assignment task ---
        if (Boolean.TRUE.equals(assignRapporteur)) {

            Integer currentComId = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");

            if (currentComId == null) {
                throw new MissingComIdException(
                        "comId is required in process variables for rapporteur assignment task");
            }

            List<Integer> userComIds = currentUserService.getCurrentUserComIds();

            if (userComIds == null || !userComIds.contains(currentComId)) {
                throw new UserNotAssociatedWithComException(
                        "User is not associated with the commission ID: " + currentComId);
            }

            varsToSet.put("comId", currentComId);

            if (Boolean.TRUE.equals(request.getValidate())) {

                if (request.getAssigneeUserId() == null || request.getAssigneeUserId().isBlank()) {
                    TaskDTO errorResponse = new TaskDTO();
                    errorResponse.setStatus(
                            "assigneeUserId is required when validating rapporteur assignment task");
                    return errorResponse;
                }

                boolean validRapporteur =
                        userComRepository.existsByUserIdAndComIdAndIsDeletedFalse(
                                request.getAssigneeUserId(),
                                currentComId
                        );

                if (!validRapporteur) {
                    TaskDTO errorResponse = new TaskDTO();
                    errorResponse.setStatus(
                            "Selected user must belong to the current commission");
                    return errorResponse;
                }
            }

            skipComAssociationCheck = true;
        }

        if (Boolean.FALSE.equals(request.getValidate())
                && Boolean.TRUE.equals(commissionRequired)
                && !Boolean.TRUE.equals(meetingRequired)
                && !Boolean.TRUE.equals(assignRapporteur)) {

            Integer acIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "acId");
            if (acIdVar == null) {
                throw new MissingAcIdException("acId is required to route back to CPMP");
            }

            Integer cpmpComId = acServiceImpl.getComIdByAcId(acIdVar);
            if (cpmpComId == null) {
                throw new ComNotFound("your ac dont have a cpmp", List.of(acIdVar));
            }

            deferComIdAfterCompletion = cpmpComId;
            log.info("Affectation false path: defer comId to CPMP={} after completion", cpmpComId);
        }

        // --- Affectation: commissionRequired=true & not meeting ⇒ request.comId required ---
        boolean isAffectationTask =
                Boolean.TRUE.equals(affectationRequired)           // nouveau process
                        || "Task_0x68o40".equals(task.getTaskDefinitionKey()); // ancien process (instances en cours)
        if (Boolean.TRUE.equals(request.getValidate())
                && Boolean.TRUE.equals(commissionRequired)
                && !Boolean.TRUE.equals(meetingRequired)
                && !Boolean.TRUE.equals(assignRapporteur)
                && isAffectationTask) {
            Integer reqComId = request.getComId();
            if (reqComId == null) {
                throw new MissingComIdException(
                        "comId is required at Affectation (commissionRequired=true) when validate is true");
            }
            Com com = comServiceImpl.findById(reqComId);
            if (com == null) {
                throw new ComNotFound("Commission not found", List.of(reqComId));
            }

            if (!entityManager.contains(latestPpm)) {
                latestPpm = entityManager.merge(latestPpm);
            }
            Ppm finalLatestPpm = latestPpm;
            List<Com> currentComs = Optional.ofNullable(latestPpm.getComs())
                    .orElseGet(() -> {
                        List<Com> l = new ArrayList<>();
                        finalLatestPpm.setComs(l);
                        return l;
                    });
            if (currentComs.stream().noneMatch(c -> c.getId().equals(reqComId))) {
                currentComs.add(com);
                log.info("Affectation: Added comId {} to PPM", reqComId);
            }

            runtimeService.setVariable(task.getProcessInstanceId(), "comId", reqComId);
            runtimeService.setVariable(task.getProcessInstanceId(), "cncmpSpeComId", reqComId);
            log.info("Affectation: Overwrote comId to {} for process {}", reqComId, task.getProcessInstanceId());
            skipComAssociationCheck = true;

            try {
                String pid = task.getProcessInstanceId();
                Integer perId = (Integer) runtimeService.getVariable(pid, "cncmpPerComId");
                Integer acIdV = (Integer) runtimeService.getVariable(pid, "acId");
                Integer cpmpId = (acIdV != null) ? acServiceImpl.getComIdByAcId(acIdV) : null;

                if (!entityManager.contains(latestPpm)) {
                    latestPpm = entityManager.merge(latestPpm);
                }
                Ppm finalLatestPpm5 = latestPpm;
                List<Com> list = Optional.ofNullable(latestPpm.getComs())
                        .orElseGet(() -> {
                            List<Com> l = new ArrayList<>();
                            finalLatestPpm5.setComs(l);
                            return l;
                        });

                java.util.function.Consumer<Integer> addIfMissing = cid -> {
                    if (cid != null && list.stream().noneMatch(x -> x.getId().equals(cid))) {
                        Com cc = null;
                        try {
                            cc = comServiceImpl.findById(cid);
                        } catch (ComNotFound e) {
                            throw new RuntimeException(e);
                        }
                        if (cc != null) {
                            list.add(cc);
                        }
                    }
                };
                addIfMissing.accept(reqComId);
                addIfMissing.accept(perId);
                addIfMissing.accept(cpmpId);

                ppmService.save(latestPpm);
                entityManager.flush();
                log.info("Affectation: ensured associations SCS={}, PER={}, CPMP={} are persisted for PPM(id={},ver={})",
                        reqComId, perId, cpmpId, latestPpm.getId(), latestPpm.getVersion());
            } catch (Exception e) {
                log.warn("Affectation: failed to persist SCS/PER/CPMP associations: {}", e.getMessage());
            }
        }

        // --- Association checks with CURRENT step's comId (SCS after Affectation) ---
        Integer currComId = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
        if ((!skipComAssociationCheck && currComId != null) && isPublishable == false && commissionRequired == true) {
            List<Integer> userComIds = currentUserService.getCurrentUserComIds();
            if (userComIds == null || !userComIds.contains(currComId)) {
                throw new UserNotAssociatedWithComException(
                        "User is not associated with the commission ID: " + currComId);
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

        // Persist CURRENT comId into PPM.coms before any status update
        try {
            Integer currComIdForPersist = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
            log.info("Current comId for persist:***************************** {}", currComIdForPersist);
            if (currComIdForPersist != null) {

                if (!entityManager.contains(latestPpm)) {
                    latestPpm = entityManager.merge(latestPpm);
                }
                Ppm finalLatestPpm3 = latestPpm;
                List<Com> currentComs = Optional.ofNullable(latestPpm.getComs())
                        .orElseGet(() -> {
                            List<Com> l = new ArrayList<>();
                            finalLatestPpm3.setComs(l);
                            return l;
                        });
                boolean exists = currentComs.stream().anyMatch(c -> c.getId().equals(currComIdForPersist));
                if (!exists) {
                    Com assocCom = comServiceImpl.findById(currComIdForPersist);
                    if (assocCom != null) {
                        currentComs.add(assocCom);
                        latestPpm.setComs(currentComs);
                        ppmService.save(latestPpm);
                        log.info(
                                "Association: Added CURRENT step comId {} to PPM (id={}, ver={}) BEFORE status update",
                                currComIdForPersist, latestPpm.getId(), latestPpm.getVersion());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Association: failed to add CURRENT comId before status update: {}", e.getMessage());
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
            log.info("Updated PPM status to Publiée for task [{}], new status: {}", taskId, latestPpm.getStatus());

            List<Realisation> realisations = latestPpm.getRealisations() != null ? latestPpm.getRealisations()
                    : new ArrayList<>();
            for (Realisation realisation : realisations) {
                realisation.setStatut(RealisationStatus.PUBLIE);
                realisationService.save(realisation);
                log.info("Set Realisation [id={}, version={}] status to PUBLIE", realisation.getId(),
                        realisation.getVersion());
            }
        } else if ((request.getValidate() != null) || actedByDecision) {
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

        // === SAFETY CASE #1 ===
        try {
            String pid = task.getProcessInstanceId();

            Integer acIdForCpmp = (Integer) runtimeService.getVariable(pid, "acId");
            Integer cpmpId = (acIdForCpmp != null) ? acServiceImpl.getComIdByAcId(acIdForCpmp) : null;

            Integer perId = (Integer) runtimeService.getVariable(pid, "cncmpPerComId");
            Integer scsId = (Integer) runtimeService.getVariable(pid, "cncmpSpeComId");

            if (!entityManager.contains(latestPpm)) {
                latestPpm = entityManager.merge(latestPpm);
            }
            Ppm keepPpm = latestPpm;
            List<Com> list = Optional.ofNullable(keepPpm.getComs())
                    .orElseGet(() -> {
                        List<Com> l = new ArrayList<>();
                        keepPpm.setComs(l);
                        return l;
                    });

            java.util.function.Consumer<Integer> addIfMissing = cid -> {
                if (cid != null && list.stream().noneMatch(x -> x.getId().equals(cid))) {
                    try {
                        Com cc = comServiceImpl.findById(cid);
                        if (cc != null) {
                            list.add(cc);
                        }
                    } catch (Exception ignore) {
                    }
                }
            };

            addIfMissing.accept(cpmpId);
            addIfMissing.accept(perId);
            addIfMissing.accept(scsId);

            ppmService.save(latestPpm);
            entityManager.flush();
            log.info("SAFETY#1: ensured associations after status update (CPMP={}, PER={}, SCS={})",
                    cpmpId, perId, scsId);
        } catch (Exception e) {
            log.warn("SAFETY#1: failed to re-ensure associations after status update: {}", e.getMessage());
        }

        // --- Determine gateway variable name & whether next is EndEvent ---
        String processVariable = resolveValidateProcessVariable(candidateGroup);
        boolean nextTaskIsLast = false;
        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            if (bpmnModel != null) {
                FlowElement taskElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                if (taskElement instanceof org.flowable.bpmn.model.UserTask) {
                    List<SequenceFlow> outgoingFlows = ((org.flowable.bpmn.model.UserTask) taskElement)
                            .getOutgoingFlows();
                    for (SequenceFlow flow : outgoingFlows) {
                        FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());
                        if (targetElement instanceof ExclusiveGateway) {
                            List<SequenceFlow> gatewayFlows = ((ExclusiveGateway) targetElement).getOutgoingFlows();
                            for (SequenceFlow gatewayFlow : gatewayFlows) {
                                String conditionExpression = gatewayFlow.getConditionExpression();
                                if (conditionExpression != null
                                        && conditionExpression.toLowerCase().contains("valide")) {
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
            log.info("Failed to extract gateway condition or determine next task for task [{}]: {}", taskId,
                    e.getMessage());
        }

        // --- Write vars, finish task ---
        if (validateFlag != null) {
            varsToSet.put(processVariable, validateFlag);
        }


        if (candidateGroup != null
                && candidateGroup.contains("AC")
                && validateFlag != null
                && !"valide".equals(processVariable)) {

            varsToSet.put("valide", validateFlag);
        }

        varsToSet.put("comment", request.getComment() != null ? request.getComment() : "");
        varsToSet.put("dateComment", LocalDateTime.now());
        varsToSet.put("treater", jwtAuth.getName());
        if (Boolean.TRUE.equals(isPublishable)) {
            varsToSet.put("isPublishable", true);
        }

        runtimeService.setVariables(task.getProcessInstanceId(), varsToSet);

        if (nextTaskIsLast && !Boolean.TRUE.equals(isPublishable)) {
            ProcessStatus targetStatus = ProcessStatus.VALIDE;

            if (isCncmpDecisionTask) {
                CncmpDecision dec = request.getCncmpDecision();
                if (dec == CncmpDecision.REJECT_END) {
                    targetStatus = ProcessStatus.REJETE;
                } else if (dec == CncmpDecision.REPORT || dec == CncmpDecision.COND_APPROVE
                        || dec == CncmpDecision.CI) {
                    targetStatus = ProcessStatus.EN_TRAITEMENT;
                }
            }

            Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), targetStatus);
            if (updatedPpm != null) {
                latestPpm = updatedPpm;
                log.info("Updated PPM status to {} for task [{}], new status: {}",
                        targetStatus, taskId, latestPpm.getStatus());
            }
        }

        ppmService.save(latestPpm);
        log.info("Saved PPM with coms: {} for ppmId: {}, version: {}", latestPpm.getComs(), latestPpm.getId(),
                latestPpm.getVersion());
        taskService.setAssignee(taskId, com.sigmap.common.security.Authentication.getCurrentUserId().get());
        if (request.getComment() != null) {
            taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
        }

        // Snapshot current comId
        Integer comIdSnapshot = null;
        try {
            comIdSnapshot = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
        } catch (Exception ex) {
            log.debug("Could not snapshot comId before completion for task [{}]: {}", taskId, ex.getMessage());
        }

        // Ensure CURRENT comId is associated (idempotent)
        if (comIdSnapshot != null) {
            try {
                if (!entityManager.contains(latestPpm)) {
                    latestPpm = entityManager.merge(latestPpm);
                }
                List<Com> currentComs = latestPpm.getComs();
                Integer finalComIdSnapshot = comIdSnapshot;
                boolean exists = currentComs.stream().anyMatch(c -> c.getId().equals(finalComIdSnapshot));
                if (!exists) {
                    Com assocCom = comServiceImpl.findById(comIdSnapshot);
                    if (assocCom != null) {
                        currentComs.add(assocCom);
                        log.info("Association: Added current step comId {} to PPM (id={}, ver={})", comIdSnapshot,
                                latestPpm.getId(), latestPpm.getVersion());
                        ppmService.save(latestPpm);
                    }
                }
            } catch (Exception e) {
                log.warn("Association: failed to add comId {} to PPM: {}", comIdSnapshot, e.getMessage());
            }
        }

        // --- Specialized filtration false path: stay / switch to specialized commission ---
        if (Boolean.FALSE.equals(request.getValidate())
                && Boolean.TRUE.equals(switchBackToPerAfterCompletion)) {

            Integer speComId = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "cncmpSpeComId");
            if (speComId == null) {
                throw new MissingComIdException(
                        "cncmpSpeComId is required to route back to specialized commission");
            }

            deferComIdAfterCompletion = speComId;
            log.info("Specialized filtration false path: defer comId to specialized commission={} after completion",
                    speComId);
        }

        // ---------- SCS→PER (defer switch back) ----------
        try {
            String pid = task.getProcessInstanceId();
            Integer perId = (Integer) runtimeService.getVariable(pid, "cncmpPerComId");
            Integer speId = (Integer) runtimeService.getVariable(pid, "cncmpSpeComId");
            Integer currentId = (Integer) runtimeService.getVariable(pid, "comId");

            boolean onScsTask = !Boolean.TRUE.equals(commissionRequired)
                    && Boolean.TRUE.equals(meetingRequired)
                    && perId != null && speId != null
                    && currentId != null && currentId.equals(speId);

            if (Boolean.TRUE.equals(request.getValidate())
                    && Boolean.TRUE.equals(switchBackToPerAfterCompletion)
                    && deferComIdAfterDecision == null
                    && deferComIdAfterCompletion == null
                    && perId != null) {
                deferComIdAfterCompletion = perId;
                log.info("SCS→PER: defer switch back to Permanent comId {} after completing task", perId);
            } else if (deferComIdAfterDecision == null && deferComIdAfterCompletion == null && onScsTask) {
                deferComIdAfterCompletion = perId;
                log.info("SCS→PER: OLD-PROCESS defer switch back to Permanent comId {} after completing SCS task", perId);
            }
        } catch (Exception e) {
            log.warn("SCS→PER: failed to plan switch back: {}", e.getMessage());
        }

        // Apply deferred comId changes just before completion (and persist)
        Integer finalSwitch = (deferComIdAfterDecision != null) ? deferComIdAfterDecision : deferComIdAfterCompletion;
        if (finalSwitch != null) {
            runtimeService.setVariable(task.getProcessInstanceId(), "comId", finalSwitch);
            try {
                if (!entityManager.contains(latestPpm)) {
                    latestPpm = entityManager.merge(latestPpm);
                }
                Ppm finalLatestPpm6 = latestPpm;
                List<Com> currentComs = Optional.ofNullable(latestPpm.getComs())
                        .orElseGet(() -> {
                            List<Com> l = new ArrayList<>();
                            finalLatestPpm6.setComs(l);
                            return l;
                        });
                boolean exists = currentComs.stream().anyMatch(c -> c.getId().equals(finalSwitch));
                if (!exists) {
                    Com assocCom = comServiceImpl.findById(finalSwitch);
                    if (assocCom != null) {
                        currentComs.add(assocCom);
                        ppmService.save(latestPpm);
                    }
                }
                log.info("Switch & Association: set comId to {} and ensured association for next step", finalSwitch);
                varsToSet.put("comId", finalSwitch);
            } catch (Exception e) {
                log.warn("Switch & Association: failed to ensure deferred comId {}: {}", finalSwitch, e.getMessage());
            }
        }

        // === SAFETY CASE #2 ===
        try {
            String pid = task.getProcessInstanceId();
            Integer acIdForCpmp = (Integer) runtimeService.getVariable(pid, "acId");
            Integer cpmpId = (acIdForCpmp != null) ? acServiceImpl.getComIdByAcId(acIdForCpmp) : null;

            if (cpmpId != null) {
                if (!entityManager.contains(latestPpm)) {
                    latestPpm = entityManager.merge(latestPpm);
                }
                Ppm keepPpm2 = latestPpm;
                List<Com> list2 = Optional.ofNullable(keepPpm2.getComs())
                        .orElseGet(() -> {
                            List<Com> l = new ArrayList<>();
                            keepPpm2.setComs(l);
                            return l;
                        });

                boolean hasCpmp = list2.stream().anyMatch(c -> c.getId().equals(cpmpId));
                if (!hasCpmp) {
                    try {
                        Com cpmp = comServiceImpl.findById(cpmpId);
                        if (cpmp != null) {
                            list2.add(cpmp);
                            ppmService.save(latestPpm);
                            entityManager.flush();
                            log.info("SAFETY#2: re-added missing CPMP={} to association before completing task",
                                    cpmpId);
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
        } catch (Exception e) {
            log.warn("SAFETY#2: failed to ensure CPMP before completing task: {}", e.getMessage());
        }

        String processInstanceId = task.getProcessInstanceId();
        Integer ppmId = (Integer) runtimeService.getVariable(processInstanceId, "id");
        Integer ppmVer = (Integer) runtimeService.getVariable(processInstanceId, "version");

        // ==========================================================
// SAVE AVIS + NOTE RAPPORTEUR (for specialized rapporteur task)
// ==========================================================
        try {
            if (request.getAvis() != null || request.getDocIdNote() != null) {

                ppmAvisNoteRapporteurService.saveOrUpdate(
                        processInstanceId,
                        taskId,
                        ppmId,
                        ppmVer,
                        jwtAuth.getName(),
                        request.getAvis(),
                        request.getDocIdNote()
                );

                log.info(
                        "Saved rapporteur avis/note: processInstanceId={}, taskId={}, ppmId={}, ppmVer={}, rapporteurUserId={}, noteDocId={}",
                        processInstanceId,
                        taskId,
                        ppmId,
                        ppmVer,
                        jwtAuth.getName(),
                        request.getDocIdNote()
                );
            }
        } catch (Exception e) {
            log.error("Failed to save rapporteur avis/note for task [{}]: {}", taskId, e.getMessage(), e);
        }
        // Complete task
        taskService.complete(taskId, varsToSet);

        if (Boolean.TRUE.equals(assignRapporteur) && Boolean.TRUE.equals(request.getValidate())) {
            assignNextTaskToUser(task.getProcessInstanceId(), request.getAssigneeUserId());
        }

        updatePpmEtapeProcess(processInstanceId, ppmId, ppmVer);

        try {
            runtimeService.removeVariable(processInstanceId, "rectifDraftVersion");
        } catch (Exception ignore) {
        }
        try {
            runtimeService.removeVariable(processInstanceId, "rectifDraftTaskId");
        } catch (Exception ignore) {
        }

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
        response.setMeetingRequired(meetingRequired);

        try {
            Integer responseComId = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
            if (responseComId != null) {
                response.setComId(responseComId);
            } else if (comIdSnapshot != null) {
                response.setComId(comIdSnapshot);
            }
        } catch (Exception ignore) {
            if (comIdSnapshot != null) {
                response.setComId(comIdSnapshot);
            }
        }

        response.setStatus("Task validated successfully");
        return response;
    }

    private void assignNextTaskToUser(String processInstanceId, String assigneeUserId) {
        if (assigneeUserId == null || assigneeUserId.isBlank()) {
            return;
        }

        List<Task> nextTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .list();

        if (nextTasks == null || nextTasks.isEmpty()) {
            log.warn("No next active task found for processInstanceId={}", processInstanceId);
            return;
        }

        if (nextTasks.size() > 1) {
            log.warn("Multiple active tasks found for processInstanceId={}, assigning first one", processInstanceId);
        }

        Task nextTask = nextTasks.get(0);
        taskService.setAssignee(nextTask.getId(), assigneeUserId);
        log.info("Assigned next task [{}:{}] to user {}",
                nextTask.getTaskDefinitionKey(), nextTask.getId(), assigneeUserId);
    }


    /* ======================= next steps helper ======================= */

    private List<NextStepDTO> getPossibleNextSteps(Task task) {
        List<NextStepDTO> nextSteps = new ArrayList<>();
        try {
            Map<String, Object> processVars = runtimeService.getVariables(task.getProcessInstanceId());
            Boolean isCondApprove = (Boolean) processVars.getOrDefault("isCondApprove", false);

            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            if (bpmnModel != null) {
                FlowElement taskElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                if (taskElement instanceof org.flowable.bpmn.model.UserTask) {
                    List<SequenceFlow> outgoingFlows = ((org.flowable.bpmn.model.UserTask) taskElement)
                            .getOutgoingFlows();

                    for (SequenceFlow flow : outgoingFlows) {
                        FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());

                        if (targetElement instanceof ExclusiveGateway) {
                            List<SequenceFlow> gatewayFlows =
                                    ((ExclusiveGateway) targetElement).getOutgoingFlows();

                            for (SequenceFlow gatewayFlow : gatewayFlows) {

                                FlowElement nextElement =
                                        bpmnModel.getFlowElement(gatewayFlow.getTargetRef());

                                String nextTargetName =
                                        (nextElement != null && nextElement.getName() != null)
                                                ? nextElement.getName()
                                                : "";

                                String currentTaskName = task.getName();

                                if (currentTaskName != null && currentTaskName.contains("Validation (AC)")) {
                                    if (Boolean.TRUE.equals(isCondApprove)) {
                                        if (nextTargetName.contains("(CPMP)"))
                                            continue;
                                    } else {
                                        if (nextTargetName.contains("Publication"))
                                            continue;
                                    }
                                }

                                String conditionExpression = gatewayFlow.getConditionExpression();

                                if (conditionExpression != null) {

                                    String variableName = extractVariableName(conditionExpression);
                                    String rhs = extractConditionValue(conditionExpression);

                                    nextElement = bpmnModel.getFlowElement(gatewayFlow.getTargetRef());

                                    String nextStepName =
                                            (nextElement != null && nextElement.getName() != null
                                                    && !nextElement.getName().isEmpty())
                                                    ? nextElement.getName()
                                                    : (nextElement != null ? nextElement.getId() : "Unknown");

                                    if (conditionExpression.contains("REPORT")
                                            && conditionExpression.contains("COND_APPROVE")) {

                                        NextStepDTO reportStep = new NextStepDTO();
                                        reportStep.setValidationVariable("cncmpDecision");
                                        reportStep.setValidationValue("REPORT");
                                        reportStep.setNextTaskName(nextStepName);
                                        reportStep.setActionLabel("Reporter");
                                        nextSteps.add(reportStep);

                                        NextStepDTO condApproveStep = new NextStepDTO();
                                        condApproveStep.setValidationVariable("cncmpDecision");
                                        condApproveStep.setValidationValue("COND_APPROVE");
                                        condApproveStep.setNextTaskName(nextStepName);
                                        condApproveStep.setActionLabel("Approuver sous réserve");
                                        nextSteps.add(condApproveStep);

                                    } else if (variableName != null && rhs != null) {

                                        NextStepDTO nextStep = new NextStepDTO();
                                        nextStep.setValidationVariable(variableName);
                                        nextStep.setValidationValue(rhs);
                                        nextStep.setNextTaskName(nextStepName);

                                        if ("APPROVE".equalsIgnoreCase(rhs)) {
                                            nextStep.setActionLabel("Approuver");

                                        } else if ("REJECT".equalsIgnoreCase(rhs)) {
                                            nextStep.setActionLabel("Rejeter");

                                        } else if ("REJECT_END".equalsIgnoreCase(rhs)) {
                                            nextStep.setActionLabel("Rejet");

                                        } else if ("COND_APPROVE".equalsIgnoreCase(rhs)) {
                                            nextStep.setActionLabel("Approuver sous réserve");

                                        } else if ("REPORT".equalsIgnoreCase(rhs)) {
                                            nextStep.setActionLabel("Reporter");

                                        } else if ("CI".equalsIgnoreCase(rhs)) {
                                            nextStep.setActionLabel("Révision SCS");

                                        } else if ("true".equalsIgnoreCase(rhs)) {

                                            if (currentTaskName != null
                                                    && currentTaskName.contains("Validation (AC)")
                                                    && nextTargetName.contains("Publication")) {

                                                nextStep.setActionLabel("Envoyer pour publication");

                                            } else {
                                                nextStep.setActionLabel("Valider");
                                            }

                                        } else if ("false".equalsIgnoreCase(rhs)) {

                                            if (variableName != null
                                                    && variableName.toLowerCase().contains("filtrage")) {

                                                nextStep.setActionLabel("Complément d'information");

                                            } else {

                                                String taskName = task.getName();

                                                if (taskName != null && taskName.contains("(CPMP)")) {
                                                    nextStep.setActionLabel("Reporter");
                                                } else {
                                                    nextStep.setActionLabel("Rejeter");
                                                }
                                            }

                                        } else if (nextElement instanceof org.flowable.bpmn.model.EndEvent) {

                                            nextStep.setActionLabel("Publier");

                                        } else {

                                            nextStep.setActionLabel(variableName + " = " + rhs);
                                        }

                                        nextSteps.add(nextStep);
                                    }
                                }

                            }

                        } else {

                            String nextStepName =
                                    (targetElement != null && targetElement.getName() != null
                                            && !targetElement.getName().isEmpty())
                                            ? targetElement.getName()
                                            : (targetElement != null ? targetElement.getId() : "Complete");

                            NextStepDTO nextStep = new NextStepDTO();
                            nextStep.setValidationVariable(null);
                            nextStep.setValidationValue(null);
                            nextStep.setNextTaskName(nextStepName);

                            if (targetElement instanceof org.flowable.bpmn.model.EndEvent) {

                                nextStep.setActionLabel("Publier");

                                log.info(
                                        "Target element is an EndEvent, setting actionLabel to 'Publier' for task [{}]",
                                        task.getId());

                            } else {

                                nextStep.setValidationValue("Passer pour validation");
                                nextStep.setActionLabel("Passer pour validation");
                            }

                            nextSteps.add(nextStep);
                            log.info("Added single next step for task [{}]: nextTask={}, action={}", task.getId(),
                                    nextStepName, nextStep.getActionLabel());
                        }
                    }

                } else {
                    log.info("Task [{}] is not a UserTask: {}", task.getId(),
                            taskElement != null ? taskElement.getClass().getSimpleName() : "null");
                }

            } else {
                log.info("BPMN model not found for process definition [{}] for task [{}]",
                        task.getProcessDefinitionId(), task.getId());
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
                log.info("ac id {} matched {} instances: {}", acId, instances.size(),
                        instances.stream().map(ProcessInstance::getId).collect(Collectors.toList()));
                matchingProcessInstanceIds.addAll(
                        instances.stream().map(ProcessInstance::getId).collect(Collectors.toList()));
            }
        }
        if (userComIds != null && !userComIds.isEmpty()) {
            for (Integer comId : userComIds) {
                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                        .variableValueEquals("comId", comId)
                        .list();
                log.info("comId {} matched {} instances: {}", comId, instances.size(),
                        instances.stream().map(ProcessInstance::getId).collect(Collectors.toList()));
                matchingProcessInstanceIds.addAll(
                        instances.stream().map(ProcessInstance::getId).collect(Collectors.toList()));
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

        String currentUserId = com.sigmap.common.security.Authentication.getCurrentUserId().orElse(null);

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

            // ADDED: fetch tasks directly assigned to current user
            if (currentUserId != null) {
                log.info("chek here");
                List<Task> assignedTasks = taskService.createTaskQuery()
                        .taskTenantId(TENANT_ID)
                        .taskAssignee(currentUserId)
                        .processInstanceIdIn(List.of(chunk.toArray(new String[0])))
                        .active()
                        .orderByTaskCreateTime().desc()
                        .list();

                for (Task assignedTask : assignedTasks) {
                    boolean alreadyExists = filteredTasks.stream()
                            .anyMatch(t -> t.getId().equals(assignedTask.getId()));
                    if (!alreadyExists) {
                        filteredTasks.add(assignedTask);
                    }

                    log.info("Assigned Task [{}] (processInstanceId: {}) assigned to current user {}",
                            assignedTask.getId(), assignedTask.getProcessInstanceId(), currentUserId);
                }
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
                        taskInfo.setModelAlfrescoId(ppm.getModelAlfrescoId());
                    });
                } catch (AcNotFound | PpmNotFound e) {
                    log.warn("PPM not found for id: {}, version: {} in task: {}, error: {}", id, version, task.getId(),
                            e.getMessage());
                    continue;
                } catch (PpmNotAssociatedWithUserAcException e) {
                    log.warn("User not associated with PPM id: {}, version: {} in task: {}, error: {}", id, version,
                            task.getId(), e.getMessage());
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
            taskInfo.setMeetingRequired(taskProperties.get("meetingRequired"));
            Integer comIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
            if (comIdVar != null)
                taskInfo.setComId(comIdVar);

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
        if (conditionExpression == null)
            return null;
        String cleanedExpression = conditionExpression.trim().replace("#{", "${");

        // Try to find the first variable name mentioned
        Pattern pattern = Pattern.compile("\\$\\{\\s*!?\\s*([^\\s=!|&}]+)");
        Matcher matcher = pattern.matcher(cleanedExpression);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Fallback for simple ${var}
        if (cleanedExpression.startsWith("${") && cleanedExpression.endsWith("}")) {
            String variableName = cleanedExpression.substring(2, cleanedExpression.length() - 1).trim();
            if (!variableName.isEmpty())
                return variableName;
        }
        return null;
    }

    private String extractConditionValue(String conditionExpression) {
        if (conditionExpression == null)
            return null;
        String cleaned = conditionExpression.trim().replace("#{", "${");

        // Check for common enum values mentioned in quotes
        Pattern enumPattern = Pattern.compile("['\"]([^'\"]+)['\"]");
        Matcher em = enumPattern.matcher(cleaned);
        if (em.find()) {
            return em.group(1);
        }

        // boolean
        Matcher m = Pattern.compile("(==|!=)\\s*(true|false)").matcher(cleaned);
        if (m.find())
            return m.group(2).toLowerCase();

        // Check for simple boolean ${var} or ${!var}
        if (cleaned.contains("${!"))
            return "false";
        if (cleaned.startsWith("${") && !cleaned.contains("==") && !cleaned.contains("!="))
            return "true";

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

        if (ppm.getRealisations() != null) {
            ppm.getRealisations().forEach(real -> {
                if (real.getFinancements() != null) {
                    real.setFinancements(
                            real.getFinancements().stream()
                                    .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                                    .collect(Collectors.toList()));
                }
            });
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

        // Récupérer la tâche active pour ce processInstanceId (une seule tâche est
        // attendue)
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
            log.warn("L'utilisateur {} n'a pas les droits pour voir la tâche {} du PPM {}", jwtAuth.getName(),
                    task.getId(), ppmId);
            response.setHasTask(false);
            response.setStatus(
                    "PPM récupéré avec succès, une tâche existe mais l'utilisateur n'a pas les droits de la voir");
            return response;
        }

        // Si l'utilisateur a les droits, inclure les détails de la tâche et définir
        // hasTask à true
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
        response.setIsUpdatable(taskProperties.get("isUpdatable"));
        response.setIsPublishable(taskProperties.get("isPublishable"));
        response.setCommissionRequired(taskProperties.get("commissionRequired"));
        // ADDED: expose meetingRequired on TaskDetailsDTO (composite)
        response.setMeetingRequired(taskProperties.get("meetingRequired")); // ADDED

        Optional<Integer> ComId = Optional.ofNullable((Integer) runtimeService.getVariable(processInstanceId, "comId"));
        ComId.ifPresent(response::setComId);

        // Ajouter les prochaines étapes possibles
        List<NextStepDTO> nextSteps = getPossibleNextSteps(task);
        response.setNextSteps(nextSteps);

        response.setStatus("PPM et détails de la tâche récupérés avec succès");

        return response;
    }

    /* ======================= history ======================= */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<HistoricTaskDto> loadHistoricTasks(String processInstId) {

        // 1. Tâches terminées
        List<HistoricTaskDto> historicTasks = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstId)
                .finished()
                .list()
                .stream()
                .map(task -> {
                    return new HistoricTaskDto(task);
                })
                .map(this::resolveConcern)
                .collect(Collectors.toList());

        // 2. Tâches actives (courantes)
        List<HistoricTaskDto> currentTasks = taskService
                .createTaskQuery()
                .processInstanceId(processInstId)
                .list()
                .stream()
                .map(task -> {
                    HistoricTaskDto dto = new HistoricTaskDto(task);

                    // Résolution du concern directement ici avec accès à `task`
                    if (task.getAssignee() != null) {
                        UserDTO user = keycloakAdminService.getUserById(task.getAssignee());
                        if (user != null) {
                            dto.setConcern(user.getFirstName() + " " + user.getLastName());
                        }
                    } else {
                        List<String> groups = taskService
                                .getIdentityLinksForTask(task.getId())
                                .stream()
                                .filter(link -> "candidate".equals(link.getType())
                                        && link.getGroupId() != null)
                                .map(IdentityLink::getGroupId)
                                .collect(Collectors.toList());

                        dto.setConcern(groups.isEmpty()
                                ? "Non affecté"
                                : "Groupe : " + String.join(", ", groups));
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // 3. Fusion et tri chronologique
        List<HistoricTaskDto> result = new ArrayList<>(historicTasks);
        result.addAll(currentTasks);
        result.sort(Comparator.comparing(
                HistoricTaskDto::getStartDate,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        return result;
    }

    // Utilisé uniquement pour les tâches historiques (assignee ou non affecté)
    private HistoricTaskDto resolveConcern(HistoricTaskDto dto) {
        if (dto.getAssignee() != null) {
            UserDTO user = keycloakAdminService.getUserById(dto.getAssignee());
            if (user != null) {
                dto.setConcern(user.getFirstName() + " " + user.getLastName());
            }
        } else {
            dto.setConcern("Non affecté");
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public TaskListResponseDTO getTasksByComId(Integer comId) {
        TaskListResponseDTO response = new TaskListResponseDTO();

        // Auth check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            response.setStatus("Invalid or missing JWT authentication");
            return response;
        }

        if (comId == null) {
            response.setStatus("comId must not be null");
            return response;
        }

        // Collect process instances having this comId
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .variableValueEquals("comId", comId)
                .list();

        if (instances == null || instances.isEmpty()) {
            response.setTenantId(TENANT_ID);
            response.setTasks(Collections.emptyList());
            response.setStatus("No active process instances found for comId " + comId);
            return response;
        }

        List<String> processInstanceIds = instances.stream()
                .map(ProcessInstance::getId)
                .collect(Collectors.toList());

        // User roles (candidate groups)
        List<String> userCandidateGroups = jwtAuth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        List<TaskDTO> taskList = new ArrayList<>();
        int chunkSize = 1000;

        for (int i = 0; i < processInstanceIds.size(); i += chunkSize) {
            List<String> chunk = processInstanceIds.subList(i, Math.min(i + chunkSize, processInstanceIds.size()));

            // Fetch active tasks for these instances
            List<Task> tasks = taskService.createTaskQuery()
                    .taskTenantId(TENANT_ID)
                    .processInstanceIdIn(chunk) // NOTE: pass the list directly
                    .active()
                    .orderByTaskCreateTime().desc()
                    .list();

            for (Task task : tasks) {
                // Enforce visibility: user must be candidate or assignee
                if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
                    continue;
                }

                TaskDTO taskInfo = new TaskDTO();
                taskInfo.setTaskId(task.getId());
                taskInfo.setTaskName(task.getName());
                taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
                taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
                taskInfo.setProcessInstanceId(task.getProcessInstanceId());
                taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                taskInfo.setCandidateGroup(getCandidateGroup(task));

                // Load process variables
                Map<String, Object> procVars = runtimeService.getVariables(task.getProcessInstanceId());
                Integer id = (Integer) procVars.getOrDefault("id", null);
                Integer version = (Integer) procVars.getOrDefault("version", null);

                // Attach PPM info if available
                if (id != null && version != null) {
                    try {
                        Optional<Ppm> ppmOpt = ppmService.findById(id, version);


                        ppmOpt.ifPresent(ppm -> {

                            taskInfo.setId(id);
                            taskInfo.setVersion(version);
                            taskInfo.setNom(ppm.getNom());
                            taskInfo.setRef(ppm.getRef());
                            taskInfo.setModelAlfrescoId(ppm.getModelAlfrescoId());

                            if (ppm.getAc() != null) {
                                taskInfo.setNomAc(ppm.getAc().getNom());
                            } else {
                                taskInfo.setNomAc(null);
                            }
                        });


                    } catch (Exception e) {
                        log.warn("Failed loading PPM for id {}, version {} in task {}: {}",
                                id, version, task.getId(), e.getMessage());
                    }
                }

                // Traceability
                Map<String, Object> trace = new HashMap<>();
                trace.put("decision", procVars.getOrDefault("decision", ""));
                trace.put("comment", procVars.getOrDefault("comment", ""));
                trace.put("dateComment", procVars.getOrDefault("dateComment", ""));
                trace.put("treater", procVars.getOrDefault("treater", ""));
                taskInfo.setTraceability(trace);

                // Task properties from BPMN form props
                Map<String, Boolean> props = getTaskProperties(task);
                taskInfo.setIsUpdatable(props.get("isUpdatable"));
                taskInfo.setIsPublishable(props.get("isPublishable"));
                taskInfo.setCommissionRequired(props.get("commissionRequired"));

                Boolean meetingRequired = props.get("meetingRequired");
                boolean exists = reunionPpmRepository.exists(
                        ReunionPpmSpecifications.hasPpmId(taskInfo.getId())
                                .and(ReunionPpmSpecifications.hasPpmVer(taskInfo.getVersion()))
                                .and(
                                        ReunionPpmSpecifications.reunionStatus(ReunionStatus.EN_COURS)
                                                .or(ReunionPpmSpecifications.reunionStatus(ReunionStatus.VALIDER))
                                                .or(ReunionPpmSpecifications.reunionStatus(ReunionStatus.PLANIFIER))
                                )
                );
                if (meetingRequired != null && meetingRequired && !exists) {
                        taskList.add(taskInfo);
                        taskInfo.setMeetingRequired(true);

                }

            }
        }

        response.setTenantId(TENANT_ID);
        response.setTasks(taskList);
        response.setCandidateGroup(userCandidateGroups.isEmpty() ? null : String.join(", ", userCandidateGroups));
        response.setStatus("Tasks retrieved successfully for comId " + comId);
        return response;
    }

    public byte[] generateModelePpm(Integer ppmId, Integer ppmVers, String signataire) throws Exception {
        try (InputStream mainReportStream = getClass()
                .getClassLoader()
                .getResourceAsStream("modele/ppm.jasper"); Connection conn = dataSource.getConnection()) {

            if (mainReportStream == null) {
                throw new FileNotFoundException("Fichier ppm.jasper introuvable !");
            }

            Map<String, Object> parametres = new HashMap<>();
            parametres.put("ppm_id", ppmId);
            parametres.put("ppm_ver", ppmVers);
            parametres.put("nom", signataire);
            parametres.put("REPORT_CONNECTION", conn);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    mainReportStream, parametres, conn);

            // Export PDF
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException e) {
            throw new RuntimeException("Erreur lors de la génération du rapport Jasper : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de la génération du modèle ppm : " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updatePpmEtapeProcess(String processInstanceId, Integer ppmId, Integer ppmVer) {

        var nextTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();

        String stepName = (nextTask != null ? nextTask.getName() : "Completed");

        if (ppmId != null && ppmVer != null) {
            CompositeId key = new CompositeId(ppmId, ppmVer);
            ppmRepository.findById(key).ifPresent(ppm -> {
                ppm.setEtapeProcess(stepName);
                ppmRepository.save(ppm);
            });
        }
    }


    public byte[] generateModelePpmXlsx(Integer ppmId, Integer ppmVers, String signataire) throws Exception {
        try (InputStream mainReportStream = getClass().getClassLoader().getResourceAsStream("modele/ppm.jasper");
             Connection conn = dataSource.getConnection();
             ByteArrayOutputStream xlsReport = new ByteArrayOutputStream()) {

            if (mainReportStream == null) {
                throw new FileNotFoundException("Fichier ppm.jasper introuvable !");
            }

            Map<String, Object> parametres = new HashMap<>();
            parametres.put("ppm_id", ppmId);
            parametres.put("ppm_ver", ppmVers);
            parametres.put("nom", signataire);
            parametres.put("REPORT_CONNECTION", conn);

            JasperPrint jasperPrint = JasperFillManager.fillReport(mainReportStream, parametres, conn);

            // Export XLSX
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(xlsReport));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();

            // Options importantes pour un Excel “propre”
            config.setOnePagePerSheet(false);           // évite une feuille par page
            config.setRemoveEmptySpaceBetweenRows(true);
            config.setRemoveEmptySpaceBetweenColumns(true);
            config.setDetectCellType(true);             // nombres => nombres, dates => dates (quand possible)
            config.setWhitePageBackground(false);
            config.setIgnoreGraphics(false);            // mets true si tu veux ignorer images/graphique
            config.setCollapseRowSpan(false);

            exporter.setConfiguration(config);
            exporter.exportReport();

            return xlsReport.toByteArray();

        } catch (JRException e) {
            throw new RuntimeException("Erreur lors de la génération XLSX : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de la génération XLSX : " + e.getMessage(), e);
        }
    }

}
