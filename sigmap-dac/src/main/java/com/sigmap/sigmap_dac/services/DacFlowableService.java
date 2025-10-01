package com.sigmap.sigmap_dac.services;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac_docs.DacDocsService;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.security.Authentication;
import com.sigmap.sigmap_dac.services.dto.request.ValidationRequest;
import com.sigmap.sigmap_dac.services.dto.response.HistoricTaskDto;
import com.sigmap.sigmap_dac.services.dto.response.NextStepDTO;
import com.sigmap.sigmap_dac.services.dto.response.ProcessStartResponseDTO;
import com.sigmap.sigmap_dac.services.dto.response.TaskDTO;
import com.sigmap.sigmap_dac.services.dto.response.TaskDetailsDTO;
import com.sigmap.sigmap_dac.services.dto.response.TaskListResponseDTO;
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

    /** Reads boolean flags from BPMN form properties: isUpdatable, isPublishable, meetingRequired. */
    private Map<String, Boolean> getTaskProperties(Task task) {
        Map<String, Boolean> props = new HashMap<>();
        props.put("isUpdatable", false);
        props.put("isPublishable", false);
        props.put("meetingRequired", false);

        try {
            BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowElement fe = model.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
            if (fe instanceof UserTask ut && ut.getFormProperties() != null) {
                for (FormProperty fp : ut.getFormProperties()) {
                    switch (fp.getId()) {
                        case "isUpdatable" -> props.put("isUpdatable", Boolean.parseBoolean(fp.getDefaultExpression()));
                        case "isPublishable" -> props.put("isPublishable", Boolean.parseBoolean(fp.getDefaultExpression()));
                        case "meetingRequired" -> props.put("meetingRequired", Boolean.parseBoolean(fp.getDefaultExpression()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to read form properties for task {}", task.getId(), e);
        }
        return props;
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

        Optional<Dac> dacOpt = dacService.findById(id, 1); // latest
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

        List<DacResponseDTO> dacDocs = Collections.emptyList();
        if (dacId != null && dacVer != null) {
            dacDocs = Collections.singletonList(dacService.findByDacIdAndVer(dacId, dacVer));
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
            throws DacNotFound, DocNotFound, AcNotFound, IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            var r = new TaskDTO();
            r.setStatus("Invalid or missing JWT authentication");
            return r;
        }

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskTenantId(TENANT_ID)
                .active()
                .singleResult();
        if (task == null) {
            var r = new TaskDTO();
            r.setStatus("Task not found or not active");
            return r;
        }

        if (!(hasCandidateGroup(task, jwtAuth) || hasAssign(task, jwtAuth))) {
            var r = new TaskDTO();
            r.setStatus("User not allowed to validate this task");
            return r;
        }

        Map<String, Object> pvars = runtimeService.getVariables(task.getProcessInstanceId());
        Integer id = (Integer) pvars.get("id");
        Integer version = (Integer) pvars.get("version");
        if (id == null || version == null) {
            var r = new TaskDTO();
            r.setStatus("Missing process variables: id/version");
            return r;
        }

        Optional<Dac> currentOpt = dacService.findById(id, version);
        if (currentOpt.isEmpty()) {
            var r = new TaskDTO();
            r.setStatus("DAC not found for id " + id + " and version " + version);
            return r;
        }
        Dac current = currentOpt.get();

        Map<String, Boolean> props = getTaskProperties(task);
        boolean isUpdatable = Boolean.TRUE.equals(props.get("isUpdatable"));
        boolean isPublishable = Boolean.TRUE.equals(props.get("isPublishable"));

        if (isUpdatable) {
            List<DacDocsRequest> updates = request.getDacDocsRequest();
            if (updates == null || updates.isEmpty()) {
                var r = new TaskDTO();
                r.setStatus("At least one DacDocsRequest is required when isUpdatable=true");
                return r;
            }
            // force DAC id from process vars to avoid tampering; batch ensures one new version
            for (DacDocsRequest u : updates) u.setDacId(id);
            dacDocsService.createDacDocsBatch(updates);

            // refresh to latest version (batch creates a single new version)
            current = dacService.findById(id, null).orElse(current);
            version = current.getDacVer();
            pvars.put("version", version);
            runtimeService.setVariables(task.getProcessInstanceId(), pvars);
        }

        // Dynamically resolve decision variable & whether chosen branch goes to EndEvent
        Boolean validate = request.getValidate();
        DecisionResolution decision = resolveDecision(task, validate);

        ProcessStatus newStatus = null;
        if (isPublishable) {
            newStatus = ProcessStatus.PUBLIE;
        } else if (Boolean.TRUE.equals(validate) && decision.nextIsEnd) {
            newStatus = ProcessStatus.VALIDE;
        } else {
            newStatus = ProcessStatus.EN_TRAITEMENT;
        }
        if (newStatus != null) {
            current = dacService.updateStatus(id, current.getDacVer(), newStatus);
        }

        Map<String, Object> toSet = new HashMap<>();
        if (decision.variable != null && validate != null) {
            toSet.put(decision.variable, validate);
        }
        toSet.put("comment", request.getComment() != null ? request.getComment() : "");
        toSet.put("dateComment", LocalDateTime.now());
        toSet.put("treater", jwtAuth.getName());
        if (isPublishable) {
            toSet.put("isPublishable", true);
        }
        runtimeService.setVariables(task.getProcessInstanceId(), toSet);

        taskService.setAssignee(taskId, Authentication.getCurrentUserId().orElse(jwtAuth.getName()));
        if (request.getComment() != null) {
            taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
        }
        taskService.complete(taskId);

        // If process ended after completion -> ensure status PUBLIE
        boolean finished = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .finished().count() > 0;
        if (finished) {
            current = dacService.updateStatus(id, current.getDacVer(), ProcessStatus.PUBLIE);
        }

        TaskDTO r = new TaskDTO();
        r.setTaskId(task.getId());
        r.setTaskName(task.getName());
        r.setTaskDefinitionKey(task.getTaskDefinitionKey());
        r.setProcessInstanceId(task.getProcessInstanceId());
        r.setId(id);
        r.setVersion(current.getDacVer());
        r.setNewStatus(current.getStatus());
        r.setValidate(validate);
        r.setTraceability(toSet);
        r.setIsUpdatable(isUpdatable);
        r.setIsPublishable(isPublishable);
        r.setStatus("Task validated successfully");
        return r;
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
        resp.setDacDocs((List<DacResponseDTO>) dacDocs);

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
