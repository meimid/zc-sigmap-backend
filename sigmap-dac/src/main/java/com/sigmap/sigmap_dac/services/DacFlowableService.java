package com.sigmap.sigmap_dac.services;

import com.sigmap.common.ac.AcServiceImpl;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComServiceImpl;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac_docs.DacDocsService;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DynaDocsResponseDTO;
import com.sigmap.common.dac_prix.DacPrix;
import com.sigmap.common.dac_prix.DacPrixRepository;
import com.sigmap.common.dac_prix.mapper.DacPrixMapper;
import com.sigmap.common.dac_prix.response.DacPrixResponce;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import com.sigmap.common.dyna_fields.response.DynaFieldsResponse;
import com.sigmap.common.dyna_fields_details.response.DynaFieldsDetailsDTO;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ppms.exception.MissingAcIdException;
import com.sigmap.common.ppms.exception.MissingComIdException;
import com.sigmap.common.ppms.exception.UserNotAssociatedWithAcException;
import com.sigmap.common.ppms.exception.UserNotAssociatedWithComException;
import com.sigmap.common.security.Authentication;
import com.sigmap.sigmap_dac.services.dto.request.ValidationRequest;
import com.sigmap.sigmap_dac.services.dto.response.HistoricTaskDto;
import com.sigmap.sigmap_dac.services.dto.response.NextStepDTO;
import com.sigmap.sigmap_dac.services.dto.response.ProcessStartResponseDTO;
import com.sigmap.sigmap_dac.services.dto.response.TaskDTO;
import com.sigmap.sigmap_dac.services.dto.response.TaskDetailsDTO;
import com.sigmap.sigmap_dac.services.dto.response.TaskListResponseDTO;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Dynamic (non-hardcoded) Flowable service for DAC process.
 * - Discovers gateway decision vars & next steps from BPMN at runtime
 * - Uses DacDocsService.findByDacIdAndVer(...) for rich DAC+details snapshots
 * - Rectification: one-shot batch via createDacDocsBatch(...) => single DAC version for all changes
 */
@Slf4j
@Service
public class DacFlowableService {

    @Value("${tenant.id}")
    private String TENANT_ID;

    @Value("${flowable.dac.process-definition-key:dacProcess}")
    private String processKey;

    @Value("${flowable.dac.initial-task-key:saisieTask}")
    private String initialTaskKey;

    @Autowired private RuntimeService runtimeService;
    @Autowired private TaskService taskService;
    @Autowired private RepositoryService repositoryService;
    @Autowired private HistoryService historyService;

    @Autowired private DacService dacService;
    @Autowired private DacDocsService dacDocsService;
    @Autowired private CurrentUserService currentUserService;
    @Autowired private KeycloakAdminService keycloakAdminService;
    @Autowired private DacPrixRepository dacPrixRepository;
    @Autowired private ComServiceImpl comServiceImpl;
    @Autowired private AcServiceImpl acServiceImpl;
    @Autowired private EntityManager entityManager;

    /* -------------------------------- helpers: auth / roles -------------------------------- */

    private boolean hasRole(JwtAuthenticationToken jwtAuth, String role) {
        return jwtAuth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(auth -> auth.equals("ROLE_" + role));
    }

    private boolean hasAssign(Task task, JwtAuthenticationToken jwtAuth) {
        return task.getAssignee() != null && task.getAssignee().equals(jwtAuth.getName());
    }

    private boolean hasCandidateGroup(Task task, JwtAuthenticationToken jwtAuth) {
        List<String> groups = taskService.getIdentityLinksForTask(task.getId()).stream()
                .filter(l -> "candidate".equals(l.getType()))
                .map(IdentityLink::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return !groups.isEmpty() && groups.stream().anyMatch(g -> hasRole(jwtAuth, g));
    }

    private String getCandidateGroup(Task task) {
        return taskService.getIdentityLinksForTask(task.getId()).stream()
                .filter(l -> "candidate".equals(l.getType()) && l.getGroupId() != null)
                .map(IdentityLink::getGroupId)
                .findFirst().orElse(null);
    }

    private boolean hasCandidateGroupFromModel(UserTask ut, JwtAuthenticationToken jwtAuth) {
        List<String> cgs = ut.getCandidateGroups();
        return cgs != null && !cgs.isEmpty() && cgs.stream().anyMatch(g -> hasRole(jwtAuth, g));
    }

    /** Reads boolean flags from BPMN form properties: isUpdatable, isPublishable, meetingRequired, commissionRequired. */
    private Map<String, Boolean> getTaskProperties(Task task) {
        Map<String, Boolean> props = new HashMap<>();
        props.put("isUpdatable", false);
        props.put("isPublishable", false);
        props.put("meetingRequired", false);
        props.put("commissionRequired", false);

        try {
            BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowElement fe = model.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
            if (fe instanceof UserTask ut && ut.getFormProperties() != null) {
                for (FormProperty fp : ut.getFormProperties()) {
                    switch (fp.getId()) {
                        case "isUpdatable"        -> props.put("isUpdatable", Boolean.parseBoolean(fp.getDefaultExpression()));
                        case "isPublishable"      -> props.put("isPublishable", Boolean.parseBoolean(fp.getDefaultExpression()));
                        case "meetingRequired"    -> props.put("meetingRequired", Boolean.parseBoolean(fp.getDefaultExpression()));
                        case "commissionRequired" -> props.put("commissionRequired", Boolean.parseBoolean(fp.getDefaultExpression()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to read form properties for task {}", task.getId(), e);
        }
        return props;
    }

    private Optional<UserTask> getModelUserTask(Task task) {
        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowElement fe = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
            if (fe instanceof UserTask ut) return Optional.of(ut);
        } catch (Exception ignore) {}
        return Optional.empty();
    }

    private boolean userTaskHasFormProp(UserTask ut, String formPropId) {
        if (ut.getFormProperties() == null) return false;
        return ut.getFormProperties().stream().anyMatch(fp -> formPropId.equals(fp.getId()));
    }

    /* -------------------------------- process start -------------------------------- */

    @Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id) throws DacNotFound, AcNotFound {
        return startProcessInstance(id, null, null);
    }

    @Transactional
    public ProcessStartResponseDTO startProcessInstance(Integer id, String processKeyOverride, String initialTaskKeyOverride)
            throws DacNotFound, AcNotFound {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            var r = new ProcessStartResponseDTO();
            r.setStatus("Invalid or missing JWT authentication");
            return r;
        }

        if (id == null) {
            var r = new ProcessStartResponseDTO();
            r.setStatus("ID must not be null");
            return r;
        }
        Integer latestVersion = dacService.findLatestVersionById(id);
        Optional<Dac> dacOpt = dacService.findById(id, latestVersion);
        if (dacOpt.isEmpty()) {
            var r = new ProcessStartResponseDTO();
            r.setStatus("DAC not found for id " + id);
            return r;
        }
        Dac dac = dacOpt.get();
        Integer version = dac.getDacVer();

        String effectiveProcessKey = (processKeyOverride != null && !processKeyOverride.isEmpty())
                ? processKeyOverride : this.processKey;
        String effectiveTaskKey = (initialTaskKeyOverride != null && !initialTaskKeyOverride.isEmpty())
                ? initialTaskKeyOverride : this.initialTaskKey;

        BpmnModel bpmnModel = repositoryService.getBpmnModel(
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(effectiveProcessKey)
                        .latestVersion()
                        .singleResult()
                        .getId()
        );
        FlowElement fe = bpmnModel.getMainProcess().getFlowElement(effectiveTaskKey);
        if (!(fe instanceof UserTask initTaskModel)) {
            var r = new ProcessStartResponseDTO();
            r.setStatus("Initial task " + effectiveTaskKey + " not found or not a UserTask");
            return r;
        }
        if (!hasCandidateGroupFromModel(initTaskModel, (JwtAuthenticationToken) auth)) {
            var r = new ProcessStartResponseDTO();
            r.setStatus("User does not have the required role to start the process (candidate groups: "
                    + String.join(",", initTaskModel.getCandidateGroups()) + ")");
            return r;
        }

        dacService.updateStatus(id, version, ProcessStatus.EN_TRAITEMENT);

        Map<String, Object> vars = new HashMap<>();
        vars.put("id", id);
        vars.put("version", version);
        Integer acId = (dac.getPpm() != null && dac.getPpm().getAc() != null) ? dac.getPpm().getAc().getId() : null;
        if (acId != null) vars.put("acId", acId);
        vars.put("isRestraint", dac.getModePassation() != null && dac.getModePassation().isRestraint());
        vars.put("shortListing", dac.isShortListing());
        vars.put("parentDacId", dac.getParentDacId());

        ProcessInstance pi = runtimeService.startProcessInstanceByKeyAndTenantId(effectiveProcessKey, vars, TENANT_ID);

        dac.setProcessInstanceId(pi.getProcessInstanceId());
        dacService.save(dac);

        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(pi.getId())
                .taskTenantId(TENANT_ID)
                .taskDefinitionKey(effectiveTaskKey)
                .active()
                .list();

        if (tasks.size() != 1) {
            var r = new ProcessStartResponseDTO();
            r.setStatus(tasks.isEmpty()
                    ? "Initial task not found for the process"
                    : "Multiple initial tasks found with key " + effectiveTaskKey);
            return r;
        }

        Task t = tasks.get(0);
        if (!hasCandidateGroup(t, (JwtAuthenticationToken) auth)) {
            var r = new ProcessStartResponseDTO();
            r.setStatus("User cannot complete initial task. Candidate groups: " + getCandidateGroup(t));
            return r;
        }
        taskService.setAssignee(t.getId(), Authentication.getCurrentUserId().orElse(((JwtAuthenticationToken) auth).getName()));
        taskService.complete(t.getId());

        var resp = new ProcessStartResponseDTO();
        resp.setProcessInstanceId(pi.getId());
        resp.setProcessDefinitionId(pi.getProcessDefinitionId());
        resp.setTaskId(t.getId());
        resp.setAcId(acId);
        resp.setStatus("Process " + effectiveProcessKey + " started and initial task completed");
        return resp;
    }

    /* -------------------------------- read task details -------------------------------- */

    public TaskDetailsDTO getTaskDetails(String taskId) throws DacNotFound, AcNotFound {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            var r = new TaskDetailsDTO();
            r.setStatus("Invalid or missing JWT authentication");
            return r;
        }

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskTenantId(TENANT_ID)
                .active()
                .singleResult();
        if (task == null) {
            var r = new TaskDetailsDTO();
            r.setStatus("Task not found or not active");
            return r;
        }

        if (!(hasCandidateGroup(task, jwtAuth) || hasAssign(task, jwtAuth))) {
            var r = new TaskDetailsDTO();
            r.setStatus("User not allowed to view this task");
            return r;
        }

        Map<String, Object> pvars = runtimeService.getVariables(task.getProcessInstanceId());
        Integer dacId = (Integer) pvars.get("id");
        Integer dacVer = (Integer) pvars.get("version");

        DacResponseDTO dacDocs = null;
        if (dacId != null && dacVer != null) {
            dacDocs = dacService.findByDacIdAndVer(dacId, dacVer);
        }

        Map<String, Boolean> props = getTaskProperties(task);

        TaskDetailsDTO resp = new TaskDetailsDTO();
        resp.setTaskId(task.getId());
        resp.setTaskName(task.getName());
        resp.setTaskDefinitionKey(task.getTaskDefinitionKey());
        resp.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
        resp.setProcessInstanceId(task.getProcessInstanceId());
        resp.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resp.setCandidateGroup(getCandidateGroup(task));
        resp.setDacDocs(dacDocs);
        resp.setTraceability(extractTraceability(pvars));
        resp.setIsUpdatable(props.get("isUpdatable"));
        resp.setIsPublishable(props.get("isPublishable"));
        resp.setNextSteps(getPossibleNextSteps(task));
        resp.setStatus("Task details retrieved successfully");
        return resp;
    }

    private Map<String, Object> extractTraceability(Map<String, Object> pvars) {
        Map<String, Object> m = new HashMap<>();
        m.put("decision", pvars.getOrDefault("decision", ""));
        m.put("comment", pvars.getOrDefault("comment", ""));
        m.put("dateComment", pvars.getOrDefault("dateComment", ""));
        m.put("treater", pvars.getOrDefault("treater", ""));
        return m;
    }

    /* -------------------------------- next steps (dynamic) -------------------------------- */

    private List<NextStepDTO> getPossibleNextSteps(Task task) {
        List<NextStepDTO> next = new ArrayList<>();
        try {
            BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowElement te = model.getFlowElement(task.getTaskDefinitionKey());
            if (te instanceof UserTask ut) {
                for (SequenceFlow of : ut.getOutgoingFlows()) {
                    FlowElement target = model.getFlowElement(of.getTargetRef());
                    if (target instanceof ExclusiveGateway gw) {
                        for (SequenceFlow gf : gw.getOutgoingFlows()) {
                            String cond = gf.getConditionExpression();
                            String var = extractVariableName(cond);
                            String rhs = extractConditionValue(cond);
                            FlowElement then = model.getFlowElement(gf.getTargetRef());
                            String name = (then != null && then.getName() != null && !then.getName().isEmpty())
                                    ? then.getName() : (then != null ? then.getId() : "Unknown");

                            var n = new NextStepDTO();
                            n.setValidationVariable(var);
                            n.setValidationValue(rhs);
                            n.setNextTaskName(name);
                            if (then instanceof EndEvent) {
                                n.setActionLabel("Publier");
                            } else if ("true".equalsIgnoreCase(rhs)) {
                                n.setActionLabel("Validate");
                            } else if ("false".equalsIgnoreCase(rhs)) {
                                n.setActionLabel("Reject");
                            } else {
                                n.setActionLabel(var != null ? (var + " = " + rhs) : "Passer pour validation");
                            }
                            next.add(n);
                        }
                    } else {
                        String name = (target != null && target.getName() != null && !target.getName().isEmpty())
                                ? target.getName() : (target != null ? target.getId() : "Complete");
                        var n = new NextStepDTO();
                        n.setValidationVariable(null);
                        n.setValidationValue(null);
                        n.setNextTaskName(name);
                        n.setActionLabel(target instanceof EndEvent ? "Publier" : "Passer pour validation");
                        next.add(n);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("getPossibleNextSteps failed for task {}", task.getId(), e);
            var n = new NextStepDTO();
            n.setNextTaskName("Unknown");
            n.setActionLabel("Complete");
            next.add(n);
        }
        return next;
    }

    private String extractVariableName(String expr) {
        if (expr == null) return null;
        String cleaned = expr.trim().replace("#{", "${");
        Pattern p = Pattern.compile("\\$\\{\\s*([A-Za-z0-9_\\.]+)\\s*(==|!=)\\s*(true|false|'[^']+'|\"[^\"]+\")\\s*}");
        Matcher m = p.matcher(cleaned);
        if (m.find()) return m.group(1).trim();
        if (cleaned.startsWith("${") && cleaned.endsWith("}")) {
            return cleaned.substring(2, cleaned.length() - 1).trim();
        }
        return null;
    }

    private String extractConditionValue(String expr) {
        if (expr == null) return null;
        String c = expr.trim().replace("#{", "${");

        Matcher m = Pattern.compile("\\$\\{[^}]*?(==|!=)\\s*(true|false)\\s*}").matcher(c);
        if (m.find()) return m.group(2).toLowerCase();

        m = Pattern.compile("\\$\\{[^}]*?(==|!=)\\s*'([^']+)'\\s*}").matcher(c);
        if (m.find()) return m.group(2);

        m = Pattern.compile("\\$\\{[^}]*?(==|!=)\\s*\"([^\"]+)\"\\s*}").matcher(c);
        if (m.find()) return m.group(2);

        return null;
    }

    /** Resolve the decision variable for the branch matching the user's validate flag; also tell if that branch hits EndEvent next. */
    private static class DecisionResolution {
        String variable;
        boolean nextIsEnd;
    }

    private DecisionResolution resolveDecision(Task task, Boolean validateFlag) {
        DecisionResolution res = new DecisionResolution();
        res.variable = null;
        res.nextIsEnd = false;
        try {
            BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowElement te = model.getFlowElement(task.getTaskDefinitionKey());
            if (!(te instanceof UserTask ut)) return res;

            for (SequenceFlow of : ut.getOutgoingFlows()) {
                FlowElement target = model.getFlowElement(of.getTargetRef());
                if (target instanceof ExclusiveGateway gw) {
                    for (SequenceFlow gf : gw.getOutgoingFlows()) {
                        String cond = gf.getConditionExpression();
                        String var = extractVariableName(cond);
                        String rhs = extractConditionValue(cond); // "true" / "false" / enum
                        if (var != null && rhs != null) {
                            // If validateFlag is boolean, only pick matching boolean branch
                            if (validateFlag == null || "true".equalsIgnoreCase(rhs) == validateFlag) {
                                FlowElement then = model.getFlowElement(gf.getTargetRef());
                                res.variable = var;
                                res.nextIsEnd = (then instanceof EndEvent);
                                return res;
                            }
                        }
                    }
                } else {
                    // direct sequence flow
                    res.variable = null;
                    res.nextIsEnd = (target instanceof EndEvent);
                    return res;
                }
            }
        } catch (Exception e) {
            log.debug("resolveDecision failed for task {}: {}", task.getId(), e.getMessage());
        }
        return res;
    }

    /* -------------------------------- validate / complete task -------------------------------- */

    @Transactional
    public TaskDTO validateTask(String taskId, ValidationRequest request)
            throws DacNotFound, DocNotFound, AcNotFound,
            MissingAcIdException, MissingComIdException,
            UserNotAssociatedWithAcException,
            UserNotAssociatedWithComException,
            ComNotFound {

        // --- Auth & task lookup ---
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            TaskDTO error = new TaskDTO();
            error.setStatus("Invalid or missing JWT authentication");
            return error;
        }

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskTenantId(TENANT_ID)
                .active()
                .singleResult();
        if (task == null) {
            TaskDTO error = new TaskDTO();
            error.setStatus("Task not found or not active");
            return error;
        }

        // Candidate group
        String candidateGroup = getCandidateGroup(task);
        if (candidateGroup == null) {
            TaskDTO error = new TaskDTO();
            error.setStatus("Task does not have a candidate group");
            return error;
        }

        // Permissions
        if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
            TaskDTO error = new TaskDTO();
            error.setStatus("User does not have the required role or assignment to validate this task");
            return error;
        }

        // --- Process vars & DAC ---
        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        log.info("Process variables for DAC task [{}]: {}", taskId, processVariables);
        Integer id = (Integer) processVariables.getOrDefault("id", null);
        Integer version = (Integer) processVariables.getOrDefault("version", null);
        if (id == null || version == null) {
            TaskDTO error = new TaskDTO();
            error.setStatus(String.format("Missing required process variables: id=%s, version=%s", id, version));
            return error;
        }

        Optional<Dac> dacOpt = dacService.findById(id, version);
        if (dacOpt.isEmpty()) {
            TaskDTO error = new TaskDTO();
            error.setStatus("DAC not found for id " + id + " and version " + version);
            return error;
        }
        Dac latestDac = dacOpt.get();

        // --- Task properties  ---
        Map<String, Boolean> taskProps = getTaskProperties(task);
        Boolean isUpdatable        = taskProps.get("isUpdatable");
        Boolean isPublishable      = taskProps.get("isPublishable");
        Boolean commissionRequired = taskProps.get("commissionRequired");
        Boolean meetingRequired    = taskProps.get("meetingRequired");

        boolean fromMeeting = Boolean.TRUE.equals(request.getFromMeeting());
        log.info("DAC approve-path: [{}:{}] isUpdatable={}, meetingRequired={}, commissionRequired={}, publishable={}, fromMeeting={}",
                task.getTaskDefinitionKey(), task.getName(),
                isUpdatable, meetingRequired, commissionRequired, isPublishable, fromMeeting);

        // --- meetingRequired enforcement  ---
        if (Boolean.TRUE.equals(meetingRequired)) {
            if (!fromMeeting) {
                TaskDTO error = new TaskDTO();
                error.setStatus("Une réunion est requise pour clôturer cette tâche. Utilisez le service (endpoint) dédié à la réunion.");
                return error;
            }
            try {
                BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                FlowElement fe = model.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
                if (fe instanceof UserTask ut) {
                    List<String> modelGroups = ut.getCandidateGroups();
                    if (modelGroups != null && modelGroups.size() >= 2) {
                        String closerGroup = modelGroups.get(modelGroups.size() - 1);
                        if (!hasRole(jwtAuth, closerGroup)) {
                            TaskDTO error = new TaskDTO();
                            error.setStatus("Une réunion est requise. Seul le groupe de clôture peut terminer cette tâche.");
                            return error;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("meetingRequired enforcement: could not resolve candidate groups for task [{}]: {}", taskId, e.getMessage());
            }
        }

        // --- If editable, update DAC (batch docs → new single version),---
        if (Boolean.TRUE.equals(isUpdatable)) {
            List<DacDocsRequest> updates = request.getDacDocsRequest();
            if (updates == null || updates.isEmpty()) {
                TaskDTO error = new TaskDTO();
                error.setStatus("At least one DacDocsRequest is required when isUpdatable=true");
                return error;
            }
            for (DacDocsRequest u : updates) u.setDacId(id);
            dacDocsService.createDacDocsBatch(updates);

            // refresh DAC to latest version
            latestDac = dacService.findById(id, null).orElse(latestDac);
            runtimeService.setVariable(task.getProcessInstanceId(), "version", latestDac.getDacVer());

            // refresh gateway-driving vars from DAC
            runtimeService.setVariable(task.getProcessInstanceId(), "isRestraint",
                    latestDac.getModePassation() != null && latestDac.getModePassation().isRestraint());
            runtimeService.setVariable(task.getProcessInstanceId(), "shortListing", latestDac.isShortListing());
            runtimeService.setVariable(task.getProcessInstanceId(), "parentDacId", latestDac.getParentDacId());
            log.info("Updated DAC after versioning; DAC ver={}, isRestraint={}, shortListing={}, parentDacId={}",
                    latestDac.getDacVer(),
                    runtimeService.getVariable(task.getProcessInstanceId(), "isRestraint"),
                    runtimeService.getVariable(task.getProcessInstanceId(), "shortListing"),
                    runtimeService.getVariable(task.getProcessInstanceId(), "parentDacId"));
        }

        // --- Resolve next user task + its meeting/commission flags (best-effort) ---
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

                    if (nextUserTask != null && nextUserTask.getFormProperties() != null) {
                        for (FormProperty fp : nextUserTask.getFormProperties()) {
                            if ("meetingRequired".equals(fp.getId()))
                                nextMeetingRequired = Boolean.parseBoolean(fp.getDefaultExpression());
                            if ("commissionRequired".equals(fp.getId()))
                                nextCommissionRequired = Boolean.parseBoolean(fp.getDefaultExpression());
                        }
                    }
                }
            } catch (Exception e) {
                log.info("Next-step resolution failed for DAC task [{}]: {}", taskId, e.getMessage());
            }
        }

        log.info("DAC approve-path: nextTask [{}] meetingRequired={}, commissionRequired={}",
                nextUserTask != null ? nextUserTask.getId() : "null", nextMeetingRequired, nextCommissionRequired);

        boolean skipComAssociationCheck = false;
        Integer deferComIdAfterCompletion = null;  // for CPMP→PER or SCS→PER

        // --- Dynamic COM wiring (approve path), AC inside DAC via DAC→PPM→AC ---
        if (Boolean.TRUE.equals(request.getValidate())) {
            // Resolve AC id through DAC->PPM->AC
            Integer acIdFromDac = (latestDac.getPpm() != null && latestDac.getPpm().getAc() != null)
                    ? latestDac.getPpm().getAc().getId() : null;

            // Case: current = AC validation (no meeting) and next requires meeting → set CPMP for the AC
            if (!Boolean.TRUE.equals(meetingRequired) && Boolean.TRUE.equals(nextMeetingRequired)) {
                if (acIdFromDac == null) throw new MissingAcIdException("acId is required to route commission from AC validation (DAC→PPM→AC)");
                Integer cpmpComId = acServiceImpl.getComIdByAcId(acIdFromDac);
                if (cpmpComId == null) throw new ComNotFound("your ac dont have a cpmp", List.of(acIdFromDac));
                Com cpmp = comServiceImpl.findById(cpmpComId);
                if (cpmp == null) throw new ComNotFound("CPMP commission not found", List.of(cpmpComId));

                // associate CPMP to DAC (idempotent)
                attachComToDac(latestDac, cpmpComId);

                runtimeService.setVariable(task.getProcessInstanceId(), "comId", cpmpComId);
                log.info("DAC AC→CPMP: Set comId {} for process {}", cpmpComId, task.getProcessInstanceId());
                skipComAssociationCheck = true;
            }

            // Case: CPMP meeting and next needs commission → prepare CNCMP PER (defer switch after completion)
            if (Boolean.TRUE.equals(meetingRequired) && Boolean.TRUE.equals(nextCommissionRequired)) {
                Com cncmpPer = comServiceImpl.findCNCMPP();
                if (cncmpPer == null) throw new ComNotFound("Aucune commission CNCMP Permanente n'est configurée.", List.of());

                attachComToDac(latestDac, cncmpPer.getId()); // keep PER associated with DAC
                deferComIdAfterCompletion = cncmpPer.getId();
                runtimeService.setVariable(task.getProcessInstanceId(), "cncmpPerComId", cncmpPer.getId());
                log.info("DAC CPMP→CNCMPP: defer switch to PER comId {} after completion", cncmpPer.getId());
            }
        }

        /* -------- CNCMP Decision (APPROVE / REJECT / CI) -------- */
        Boolean validateFlag = request.getValidate();
        boolean isCncmpDecisionTask = getModelUserTask(task)
                .map(ut -> userTaskHasFormProp(ut, "cncmpDecision"))
                .orElse(false);

        Integer deferComIdAfterDecision = null;

        if (isCncmpDecisionTask) {
            var dec = request.getCncmpDecision();
            if (dec != null) {
                String pid = task.getProcessInstanceId();

                Integer perId = (Integer) runtimeService.getVariable(pid, "cncmpPerComId");
                if (perId != null) {
                    runtimeService.setVariable(pid, "comId", perId);
                    try { attachComToDac(latestDac, perId); } catch (Exception ignore) {}
                }
                runtimeService.setVariable(pid, "cncmpDecision", dec.name());

                switch (dec) {
                    case APPROVE -> {
                        deferComIdAfterDecision = perId;
                        validateFlag = null;
                        log.info("DAC CNCMP Decision: APPROVE");
                    }
                    case REJECT -> {
                        Integer acIdVar = (Integer) runtimeService.getVariable(pid, "acId");
                        if (acIdVar == null) throw new MissingAcIdException("acId required to route REJECT to CPMP");
                        Integer cpmpComId = acServiceImpl.getComIdByAcId(acIdVar);
                        if (cpmpComId == null) throw new ComNotFound("your ac dont have a cpmp", List.of(acIdVar));
                        deferComIdAfterDecision = cpmpComId;
                        validateFlag = null;
                        log.info("DAC CNCMP Decision: REJECT → defer comId to CPMP={}", cpmpComId);
                    }
                    case CI -> {
                        Integer speComId = (Integer) runtimeService.getVariable(pid, "cncmpSpeComId");
                        deferComIdAfterDecision = speComId;
                        validateFlag = null;
                        log.info("DAC CNCMP Decision: CI → defer comId to SCS={}", speComId);
                    }
                }
            }
        }

        // --- Affectation: commissionRequired=true & not meeting ⇒ request.comId required (choose SCS) ---
        if (Boolean.TRUE.equals(commissionRequired) && !Boolean.TRUE.equals(meetingRequired)) {
            Integer reqComId = request.getComId();
            if (reqComId == null) {
                throw new MissingComIdException("comId is required at Affectation (commissionRequired=true) when validate is true");
            }
            Com scs = comServiceImpl.findById(reqComId);
            if (scs == null) throw new ComNotFound("Commission not found", List.of(reqComId));

            attachComToDac(latestDac, reqComId); // SCS
            runtimeService.setVariable(task.getProcessInstanceId(), "comId", reqComId);
            runtimeService.setVariable(task.getProcessInstanceId(), "cncmpSpeComId", reqComId);
            log.info("DAC Affectation: Overwrote comId to {} and set cncmpSpeComId", reqComId);
            skipComAssociationCheck = true;

            // Ensure PER + CPMP remain associated alongside SCS (idempotent)
            try {
                String pid = task.getProcessInstanceId();
                Integer perId  = (Integer) runtimeService.getVariable(pid, "cncmpPerComId");
                Integer acIdV  = (Integer) runtimeService.getVariable(pid, "acId");
                Integer cpmpId = (acIdV != null) ? acServiceImpl.getComIdByAcId(acIdV) : null;

                attachComToDac(latestDac, perId);
                attachComToDac(latestDac, cpmpId);
            } catch (Exception e) {
                log.warn("DAC Affectation: failed to persist SCS/PER/CPMP associations: {}", e.getMessage());
            }
        }

        // --- Association checks with CURRENT step's comId  ---
        Integer currComId = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
        if ((!skipComAssociationCheck && currComId != null) && !Boolean.TRUE.equals(isPublishable)) {
            List<Integer> userComIds = currentUserService.getCurrentUserComIds();
            if (userComIds == null || !userComIds.contains(currComId)) {
                throw new com.sigmap.common.ppms.exception.UserNotAssociatedWithComException(
                        "User is not associated with the commission ID: " + currComId);
            }
            log.info("DAC User validated for commission ID: {}", currComId);
        } else if (currComId == null) {
            Integer acIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "acId");
            if (acIdVar == null) throw new MissingAcIdException("acId is required in process variables for tasks without comId");
            List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
            if (userAcIds == null || !userAcIds.contains(acIdVar)) {
                throw new com.sigmap.common.ppms.exception.UserNotAssociatedWithAcException(
                        "User is not associated with the AC ID: " + acIdVar);
            }
            log.info("DAC User validated for AC ID: {}", acIdVar);
        } else {
            log.info("DAC: Skipping commission association check at system hand-off (comId={})", currComId);
        }

        // Persist CURRENT comId into DAC.coms BEFORE status update (idempotent)
        try {
            Integer snap = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
            if (snap != null) attachComToDac(latestDac, snap);
        } catch (Exception e) {
            log.warn("DAC Association: failed to add CURRENT comId before status update: {}", e.getMessage());
        }

        // --- Status updates  ---
        ProcessStatus newStatus = null;
        boolean actedByDecision = isCncmpDecisionTask && request.getCncmpDecision() != null;

        if (Boolean.TRUE.equals(isPublishable)) {
            newStatus = ProcessStatus.PUBLIE;
            Dac updated = dacService.updateStatus(id, latestDac.getDacVer(), newStatus);
            if (updated == null) {
                TaskDTO error = new TaskDTO();
                error.setStatus("Failed to update DAC status to Publiée");
                return error;
            }
            latestDac = updated;
            log.info("Updated DAC status to Publiée for task [{}], new status: {}", taskId, latestDac.getStatus());

        } else if ((request.getValidate() != null) || actedByDecision) {
            newStatus = ProcessStatus.EN_TRAITEMENT;
            Dac updated = dacService.updateStatus(id, latestDac.getDacVer(), newStatus);
            if (updated == null) {
                TaskDTO error = new TaskDTO();
                error.setStatus("Failed to update DAC status to En cours de traitement");
                return error;
            }
            latestDac = updated;

        } else if (request.getValidate() != null && !request.getValidate()) {
            newStatus = ProcessStatus.EN_TRAITEMENT;
            Dac updated = dacService.updateStatus(id, latestDac.getDacVer(), newStatus);
            if (updated == null) {
                TaskDTO error = new TaskDTO();
                error.setStatus("Failed to keep status En cours de traitement");
                return error;
            }
            latestDac = updated;
        }

        // === SAFETY CASE #1 === re-ensure CPMP/PER/SCS are associated after status update ===
        try {
            String pid = task.getProcessInstanceId();
            Integer acIdForCpmp = (Integer) runtimeService.getVariable(pid, "acId");
            Integer cpmpId = (acIdForCpmp != null) ? acServiceImpl.getComIdByAcId(acIdForCpmp) : null;
            Integer perId  = (Integer) runtimeService.getVariable(pid, "cncmpPerComId");
            Integer scsId  = (Integer) runtimeService.getVariable(pid, "cncmpSpeComId");

            attachComToDac(latestDac, cpmpId);
            attachComToDac(latestDac, perId);
            attachComToDac(latestDac, scsId);
        } catch (Exception e) {
            log.warn("DAC SAFETY#1: failed to re-ensure associations after status update: {}", e.getMessage());
        }

        // --- Determine gateway variable + whether next is EndEvent  ---
        String processVariable = candidateGroup.toLowerCase() + "_valide";
        boolean nextTaskIsLast = false;
        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            if (bpmnModel != null) {
                FlowElement te = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
                if (te instanceof UserTask ut) {
                    for (SequenceFlow of : ut.getOutgoingFlows()) {
                        FlowElement target = bpmnModel.getFlowElement(of.getTargetRef());
                        if (target instanceof ExclusiveGateway gw) {
                            for (SequenceFlow gf : gw.getOutgoingFlows()) {
                                String cond = gf.getConditionExpression();
                                if (cond != null && cond.toLowerCase().contains("valide")) {
                                    String variableName = extractVariableName(cond);
                                    if (variableName != null && variableName.toLowerCase().contains("valide")) {
                                        processVariable = variableName;
                                    }
                                }
                                FlowElement next = bpmnModel.getFlowElement(gf.getTargetRef());
                                if (next instanceof EndEvent) { nextTaskIsLast = true; break; }
                            }
                        } else if (target instanceof EndEvent) {
                            nextTaskIsLast = true; break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("DAC: Failed to extract gateway condition or determine next task for [{}]: {}", taskId, e.getMessage());
        }

        // --- Write vars, traceability, early VALIDEE if entering last user task (when not publishable) ---
        Map<String, Object> varsToSet = new HashMap<>();
        if (validateFlag != null) varsToSet.put(processVariable, validateFlag);
        varsToSet.put("comment", request.getComment() != null ? request.getComment() : "");
        varsToSet.put("dateComment", LocalDateTime.now());
        varsToSet.put("treater", jwtAuth.getName());
        if (Boolean.TRUE.equals(isPublishable)) varsToSet.put("isPublishable", true);
        runtimeService.setVariables(task.getProcessInstanceId(), varsToSet);

        if (nextTaskIsLast && !Boolean.TRUE.equals(isPublishable)) {
            Dac updated = dacService.updateStatus(id, latestDac.getDacVer(), ProcessStatus.VALIDE);
            if (updated != null) {
                latestDac = updated;
                log.info("DAC status set to Validée before entering last task for [{}], status: {}", taskId, latestDac.getStatus());
            }
        }

        dacService.save(latestDac);
        taskService.setAssignee(taskId, Authentication.getCurrentUserId().orElse(jwtAuth.getName()));
        if (request.getComment() != null) taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());

        // Snapshot current comId (e.g., SCS after Affectation)
        Integer comIdSnapshot = null;
        try { comIdSnapshot = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId"); }
        catch (Exception ex) { log.debug("DAC: Could not snapshot comId before completion for [{}]: {}", taskId, ex.getMessage()); }

        // Ensure CURRENT comId is associated (idempotent)
        if (comIdSnapshot != null) {
            try { attachComToDac(latestDac, comIdSnapshot); }
            catch (Exception e) { log.warn("DAC Association: failed to add comId {} to DAC: {}", comIdSnapshot, e.getMessage()); }
        }

        // ---------- SCS→PER (defer switch back) ----------
        try {
            String pid = task.getProcessInstanceId();
            Integer perId     = (Integer) runtimeService.getVariable(pid, "cncmpPerComId");
            Integer speId     = (Integer) runtimeService.getVariable(pid, "cncmpSpeComId");
            Integer currentId = (Integer) runtimeService.getVariable(pid, "comId");

            boolean onScsTask = !Boolean.TRUE.equals(commissionRequired)
                    && perId != null && speId != null
                    && Objects.equals(currentId, speId);

            if (deferComIdAfterDecision == null && deferComIdAfterCompletion == null && onScsTask) {
                deferComIdAfterCompletion = perId;
                log.info("DAC SCS→PER: defer switch back to Permanent comId {} after completing SCS task", perId);
            }
        } catch (Exception e) {
            log.warn("DAC SCS→PER: failed to plan switch back: {}", e.getMessage());
        }

        // Apply deferred comId changes just before completion (and persist)
        Integer finalSwitch = (deferComIdAfterDecision != null) ? deferComIdAfterDecision : deferComIdAfterCompletion;
        if (finalSwitch != null) {
            runtimeService.setVariable(task.getProcessInstanceId(), "comId", finalSwitch);
            try { attachComToDac(latestDac, finalSwitch); }
            catch (Exception e) { log.warn("DAC Switch & Association: failed to ensure deferred comId {}: {}", finalSwitch, e.getMessage()); }
        }

        // === SAFETY CASE #2 === keep CPMP associated right before completing
        try {
            String pid = task.getProcessInstanceId();
            Integer acIdForCpmp = (Integer) runtimeService.getVariable(pid, "acId");
            Integer cpmpId = (acIdForCpmp != null) ? acServiceImpl.getComIdByAcId(acIdForCpmp) : null;
            attachComToDac(latestDac, cpmpId);
        } catch (Exception e) {
            log.warn("DAC SAFETY#2: failed to ensure CPMP before completing: {}", e.getMessage());
        }

        // Complete task
        taskService.complete(taskId);

        // If process ended, force PUBLIE
        boolean finished = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .finished().count() > 0;
        if (finished) {
            latestDac = dacService.updateStatus(id, latestDac.getDacVer(), ProcessStatus.PUBLIE);
        }

        // --- Response ---
        TaskDTO resp = new TaskDTO();
        resp.setTaskId(task.getId());
        resp.setTaskName(task.getName());
        resp.setTaskDefinitionKey(task.getTaskDefinitionKey());
        resp.setProcessInstanceId(task.getProcessInstanceId());
        resp.setId(id);
        resp.setVersion(latestDac.getDacVer());
        resp.setNewStatus(newStatus != null ? newStatus : latestDac.getStatus());
        resp.setValidate(validateFlag != null ? validateFlag : request.getValidate());
        resp.setTraceability(varsToSet);
        resp.setIsUpdatable(isUpdatable);
        resp.setIsPublishable(isPublishable);
        if (comIdSnapshot != null) resp.setComId(comIdSnapshot);
        resp.setStatus("Task validated successfully");
        return resp;
    }

    private void attachComToDac(Dac dac, Integer comId) throws ComNotFound {
        if (comId == null) return;
        if (!entityManager.contains(dac)) dac = entityManager.merge(dac);
        Dac finalDac = dac;
        List<Com> list = Optional.ofNullable(dac.getComs())
                .orElseGet(() -> { List<Com> l = new ArrayList<>(); finalDac.setComs(l); return l; });
        boolean exists = list.stream().anyMatch(c -> c.getId().equals(comId));
        if (!exists) {
            Com com = comServiceImpl.findById(comId);
            if (com != null) {
                list.add(com);
                dacService.save(dac);
                entityManager.flush();
                log.info("Added comId {} to DAC(id={}, ver={})", comId, dac.getDacId(), dac.getDacVer());
            }
        }
    }

    /* -------------------------------- list my tasks -------------------------------- */

    public TaskListResponseDTO getUserTasks() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;

        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds == null || userAcIds.isEmpty()) {
            return new TaskListResponseDTO();
        }

        List<String> userGroups = jwtAuth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
        if (userGroups.isEmpty()) {
            return new TaskListResponseDTO();
        }

        // Collect process instances limited by user's ACs
        List<String> instanceIds = new ArrayList<>();
        for (Integer acId : userAcIds) {
            var instances = runtimeService.createProcessInstanceQuery()
                    .variableValueEquals("acId", acId)
                    .list();
            instanceIds.addAll(instances.stream().map(ProcessInstance::getId).toList());
        }

        List<Task> filtered = new ArrayList<>();
        int chunk = 1000;
        for (int i = 0; i < instanceIds.size(); i += chunk) {
            List<String> part = instanceIds.subList(i, Math.min(i + chunk, instanceIds.size()));
            if (part.isEmpty()) continue;
            filtered.addAll(
                    taskService.createTaskQuery()
                            .taskTenantId(TENANT_ID)
                            .taskCandidateGroupIn(userGroups)
                            .processInstanceIdIn(List.of(part.toArray(new String[0])))
                            .active()
                            .orderByTaskCreateTime().desc()
                            .list()
            );
        }

        List<TaskDTO> dtos = filtered.stream().map(t -> {
            TaskDTO dto = new TaskDTO();
            dto.setTaskId(t.getId());
            dto.setTaskName(t.getName());
            dto.setTaskDefinitionKey(t.getTaskDefinitionKey());
            dto.setAssignee(t.getAssignee() != null ? t.getAssignee() : "");
            dto.setProcessInstanceId(t.getProcessInstanceId());
            dto.setCreated(t.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            dto.setCandidateGroup(getCandidateGroup(t));

            Map<String, Object> pvars = runtimeService.getVariables(t.getProcessInstanceId());
            Integer id = (Integer) pvars.get("id");
            Integer ver = (Integer) pvars.get("version");
            if (id != null && ver != null) {
                try {
                    Dac d = dacService.findById(id, ver).orElse(null);
                    if (d != null) {
                        dto.setId(id);
                        dto.setVersion(ver);
                        dto.setRef(d.getReference());
                    }
                } catch (Exception ignored) { }
            }
            dto.setTraceability(extractTraceability(pvars));

            Map<String, Boolean> props = getTaskProperties(t);
            dto.setIsUpdatable(props.get("isUpdatable"));
            dto.setIsPublishable(props.get("isPublishable"));
            return dto;
        }).collect(Collectors.toList());

        TaskListResponseDTO r = new TaskListResponseDTO();
        r.setTenantId(TENANT_ID);
        r.setTasks(dtos);
        r.setCandidateGroup(String.join(", ", userGroups));
        r.setStatus("Tasks retrieved successfully");
        return r;
    }

    /* -------------------------------- DAC + visible task -------------------------------- */

    @Transactional
    public TaskDetailsDTO getDacWithTasks(Integer dacId) throws DacNotFound, AcNotFound {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            var r = new TaskDetailsDTO();
            r.setStatus("Invalid or missing JWT authentication");
            return r;
        }

        Integer latestVersion = dacService.findLatestVersionById(dacId);
        Dac dac = dacService.findById(dacId, latestVersion).orElse(null);
        if (dac == null) {
            var r = new TaskDetailsDTO();
            r.setStatus("DAC not found for id " + dacId);
            return r;
        }

        TaskDetailsDTO resp = new TaskDetailsDTO();

        // Attach full DAC docs/details snapshot for latest version
        DacResponseDTO dacDocs = dacService.findByDacIdAndVer(dac.getDacId(), dac.getDacVer());

        // sort dacDocs -> dynaDoc -> dynaFields -> dynaFieldsDetails by columnPos ascending
        if (dacDocs != null && dacDocs.getDynaDocs() != null) {
            for (DynaDocsResponseDTO dd : dacDocs.getDynaDocs()) {
                if (dd.getDynaFields() != null) {
                    // sort details inside each field by columnPos
                    for (DynaFieldsDTO f : dd.getDynaFields()) {
                        if (f.getDynaFieldsDetails() != null && !f.getDynaFieldsDetails().isEmpty()) {
                            List<DynaFieldsDetailsDTO> sorted = new ArrayList<>(f.getDynaFieldsDetails());
                            sorted.sort(java.util.Comparator.comparing(DynaFieldsDetailsDTO::getColumnPos, Comparator.nullsLast(Integer::compareTo)));
                            f.setDynaFieldsDetails(new LinkedHashSet<>(sorted));
                        }
                    }
                    // sort fields by dynaFieldsOrder if present
                    dd.getDynaFields().sort(Comparator.comparing(DynaFieldsDTO::getDynaFieldsOrder, Comparator.nullsLast(Integer::compareTo)));
                }
            }
            // sort dynaDocs by dynaDocOrder if present
            dacDocs.getDynaDocs().sort(java.util.Comparator.comparing(DynaDocsResponseDTO::getDynaDocOrder, Comparator.nullsLast(Integer::compareTo)
            ));
        }

        resp.setDacDocs(dacDocs);

        // Attached DacPrix
        List<DacPrix> prixDac = dacPrixRepository.findByDac(dac.getDacId(), dac.getDacVer());
        List<DacPrixResponce> responces = prixDac.stream().map(DacPrixMapper.INSTANCE::map).toList();
        resp.setDacPrix(responces);

        String piId = dac.getProcessInstanceId();
        if (piId == null) {
            resp.setHasTask(false);
            resp.setStatus("DAC loaded successfully, no active process");
            return resp;
        }

        Task task = taskService.createTaskQuery()
                .processInstanceId(piId)
                .taskTenantId(TENANT_ID)
                .active()
                .singleResult();

        if (task == null) {
            resp.setHasTask(false);
            resp.setStatus("DAC loaded successfully, no active task");
            return resp;
        }

        if (!(hasCandidateGroup(task, jwtAuth) || hasAssign(task, jwtAuth))) {
            resp.setHasTask(false);
            resp.setStatus("DAC loaded, a task exists but user cannot view it");
            return resp;
        }

        resp.setHasTask(true);
        resp.setTaskId(task.getId());
        resp.setTaskName(task.getName());
        resp.setTaskDefinitionKey(task.getTaskDefinitionKey());
        resp.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
        resp.setProcessInstanceId(task.getProcessInstanceId());
        resp.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resp.setCandidateGroup(getCandidateGroup(task));
        resp.setTraceability(extractTraceability(runtimeService.getVariables(task.getProcessInstanceId())));

        Map<String, Boolean> props = getTaskProperties(task);
        resp.setIsUpdatable(props.get("isUpdatable"));
        resp.setIsPublishable(props.get("isPublishable"));
        resp.setNextSteps(getPossibleNextSteps(task));
        resp.setStatus("DAC and task details retrieved successfully");
        return resp;
    }

    /* -------------------------------- history -------------------------------- */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<HistoricTaskDto> loadHistoricTasks(String processInstId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstId)
                .finished()
                .list()
                .stream()
                .map(ht -> {
                    List<Comment> comments = taskService.getTaskComments(ht.getId());
                    return new HistoricTaskDto(ht, comments);
                })
                .map(h -> {
                    if (h.getAssignee() != null) {
                        UserDTO u = keycloakAdminService.getUserById(h.getAssignee());
                        if (u != null) h.setConcern(u.getFirstName() + " " + u.getLastName());
                    }
                    return h;
                })
                .collect(Collectors.toList());
    }
}
