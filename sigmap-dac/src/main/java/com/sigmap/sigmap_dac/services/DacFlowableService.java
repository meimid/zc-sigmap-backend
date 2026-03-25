package com.sigmap.sigmap_dac.services;

import com.sigmap.common.ac.AcServiceImpl;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComServiceImpl;
import com.sigmap.common.com.exception.CNCMPPNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.CncmpDecision;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacId;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.DacDocsService;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.request.DacWithDacDocsRequest;
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
import com.sigmap.common.dyna_form_fields.response.DynaFormFieldResponse;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import com.sigmap.common.ppms.exception.MissingAcIdException;
import com.sigmap.common.ppms.exception.MissingComIdException;
import com.sigmap.common.ppms.exception.UserNotAssociatedWithAcException;
import com.sigmap.common.ppms.exception.UserNotAssociatedWithComException;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtraitService;
import com.sigmap.common.reunion.response.ExtraitResponse;
import com.sigmap.common.reunion.response.PvResponse;
import com.sigmap.common.reunion.response.ReunionArgumentDocuments;
import com.sigmap.common.reunionDac.ReunionDacRepository;
import com.sigmap.common.reunionDac.ReunionDacSpecifications;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.dac_dyna_form.DacDynaForm;
import com.sigmap.common.dac_dyna_form.DacDynaFormRepository;
import com.sigmap.common.dac_dyna_form.mappers.DacDynaFormMapper;
import com.sigmap.common.dyna_form.DynaFormRepository;
import com.sigmap.common.dyna_form.mappers.DynaFormMapper;
import com.sigmap.common.dyna_form.response.DynaFormResponse;
import com.sigmap.common.dac_doc_soumission.DacDocSoumission;
import com.sigmap.common.dac_doc_soumission.DacDocSoumissionRepository;
import com.sigmap.common.dac_doc_soumission.mapper.DacDocSoumissionMapper;
import com.sigmap.common.dac_doc_soumission.response.DacDocSoumissionResponse;
import com.sigmap.common.dyna_form_fields.DynaFormFieldRepository;
import com.sigmap.sigmap_dac.services.dto.request.GroupValidationRequest;
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
    @Autowired private DacRepository dacRepository;
    @Autowired private DacService dacService;
    @Autowired private DacDynaFormRepository dacDynaFormRepository;
    @Autowired private DynaFormRepository dynaFormRepository;
    @Autowired private DacDynaFormMapper dacDynaFormMapper;
    @Autowired private DynaFormMapper dynaFormMapper;
    @Autowired private DacDocsService dacDocsService;
    @Autowired private CurrentUserService currentUserService;
    @Autowired private KeycloakAdminService keycloakAdminService;
    @Autowired private DacPrixRepository dacPrixRepository;
    @Autowired private ComServiceImpl comServiceImpl;
    @Autowired private AcServiceImpl acServiceImpl;
    @Autowired private EntityManager entityManager;
    @Autowired private DacDocSoumissionRepository dacDocSoumissionRepository;
    @Autowired private DacDocSoumissionMapper dacDocSoumissionMapper;
    @Autowired private DynaFormFieldRepository dynaFormFieldRepository;
    @Autowired
    private ReunionDacRepository reunionDacRepository;
    @Autowired
    ProcessVerbalReunionExtraitService processVerbalReunionExtraitService;

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

        Integer acId = (dac.getAppelOffre().getPpm() != null && dac.getAppelOffre().getPpm().getAc() != null)
                ? dac.getAppelOffre().getPpm().getAc().getId()
                : null;
        if (acId != null) vars.put("acId", acId);

        vars.put("isRestraint",
                dac.getAppelOffre().getRealisation().getModePassation() != null
                        && dac.getAppelOffre().getRealisation().getModePassation().isRestraint()
        );

        Integer etapeDac = 1;
        String etapeDacStr = dac.getEtapeDac();
        if (etapeDacStr != null && !etapeDacStr.isBlank()) {
            try {
                Integer parsed = Integer.valueOf(etapeDacStr.trim());
                if (parsed == 1 || parsed == 2) {
                    etapeDac = parsed;
                }
            } catch (NumberFormatException ignored) {
                // keep default = 1
            }
        }
        vars.put("etapeDac", etapeDac);

        // vars.put("shortListing", dac.isShortListing());
        // vars.put("parentDacId", dac.getParentDacId());

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

        String processInstanceId = pi.getProcessInstanceId();
        Integer dacId = (Integer) runtimeService.getVariable(processInstanceId, "id");
        Integer dacVer = (Integer) runtimeService.getVariable(processInstanceId, "version");

        taskService.setAssignee(t.getId(), Authentication.getCurrentUserId().orElse(((JwtAuthenticationToken) auth).getName()));
        taskService.complete(t.getId());
        updateDacEtapeProcess(processInstanceId, dacId, dacVer);

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
        resp.setCommissionRequired(props.get("commissionRequired"));
        resp.setMeetingRequired(props.get("meetingRequired"));
        Integer comIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId"); // ADDED
        if (comIdVar != null)
            resp.setComId(comIdVar);
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

        List<NextStepDTO> result = new ArrayList<>();

        try {
            Map<String, Object> vars = runtimeService.getVariables(task.getExecutionId());
            log.info("process Variables: {}", vars);

            BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowElement te = model.getFlowElement(task.getTaskDefinitionKey());

            if (!(te instanceof UserTask ut)) return result;

            for (SequenceFlow of : ut.getOutgoingFlows()) {

                FlowElement target = model.getFlowElement(of.getTargetRef());

            /* =============================================================
               CASE 1 : OUTGOING → GATEWAY
               ============================================================= */
                if (target instanceof ExclusiveGateway gw) {

                    for (SequenceFlow gf : gw.getOutgoingFlows()) {

                        String cond = gf.getConditionExpression();
                        String varsStr = extractVariableName(cond);

                        log.info("------ Checking SequenceFlow {} ------", gf.getId());
                        log.info("ConditionExpression = {}", cond);

                        boolean matches = true;

                    /* =============================================================
                       UNIVERSAL CONDITION EVALUATION (EXCEPT valide / cpmp_valide / cncmpDecision)
                       ============================================================= */
                        if (cond != null) {

                            String inner = cond.replace("${", "")
                                    .replace("}", "")
                                    .trim();

                            String[] parts = inner.split("&&");

                            for (String p : parts) {
                                p = p.trim();
                                log.info(" >> Evaluating condition part: '{}'", p);

                                /* ALWAYS DISPLAY THESE OPTIONS — DO NOT FILTER */
                                if (p.startsWith("valide")
                                        || p.startsWith("cpmp_valide")
                                        || p.startsWith("cncmpDecision")
                                        || p.startsWith("reserve_valide")) {

                                    log.info("    SKIP decision variable check for '{}'", p);
                                    continue;
                                }

                                /* ============================ BOOLEAN TRUE ============================== */
                                if (p.contains("== true")) {
                                    String var = p.replace("== true", "").trim();
                                    Object val = vars.get(var);
                                    log.info("    Expect {} == true, actual={}", var, val);

                                    if (!(val instanceof Boolean) || !((Boolean) val)) {
                                        log.warn("    ✗ Flow hidden: variable {} is not true", var);
                                        matches = false;
                                    }
                                }

                                /* ============================ BOOLEAN FALSE ============================= */
                                else if (p.contains("== false")) {
                                    String var = p.replace("== false", "").trim();
                                    Object val = vars.get(var);
                                    log.info("    Expect {} == false, actual={}", var, val);

                                    if (!(val instanceof Boolean) || ((Boolean) val)) {
                                        log.warn("    ✗ Flow hidden: variable {} is not false", var);
                                        matches = false;
                                    }
                                }

                                /* ============================ NULL COMPARISON =========================== */
                                else if (p.contains("== null")) {
                                    String var = p.replace("== null", "").trim();
                                    Object val = vars.get(var);
                                    log.info("    Expect {} == null, actual={}", var, val);

                                    if (val != null) {
                                        log.warn("    ✗ Flow hidden: {} is not null", var);
                                        matches = false;
                                    }
                                }

                                else if (p.contains("!= null")) {
                                    String var = p.replace("!= null", "").trim();
                                    Object val = vars.get(var);
                                    log.info("    Expect {} != null, actual={}", var, val);

                                    if (val == null) {
                                        log.warn("    ✗ Flow hidden: {} is null", var);
                                        matches = false;
                                    }
                                }

                                /* ============================ STRING EQUALITY =========================== */
                                else if (p.contains("== '")) {
                                    String[] seg = p.split("==");
                                    String var = seg[0].trim();
                                    String expected = seg[1].trim().replace("'", "");
                                    Object val = vars.get(var);
                                    log.info("    Expect {} == '{}', actual={}", var, expected, val);

                                    if (val == null || !val.toString().equals(expected)) {
                                        log.warn("    ✗ Flow hidden: {} value not equal to expected '{}'", var, expected);
                                        matches = false;
                                    }
                                }

                                else if (p.contains("!= '")) {
                                    String[] seg = p.split("!=");
                                    String var = seg[0].trim();
                                    String expected = seg[1].trim().replace("'", "");
                                    Object val = vars.get(var);
                                    log.info("    Expect {} != '{}', actual={}", var, expected, val);

                                    if (val != null && val.toString().equals(expected)) {
                                        log.warn("    ✗ Flow hidden: {} equals forbidden '{}'", var, expected);
                                        matches = false;
                                    }
                                }

                                if (!matches) {
                                    log.warn(" → RESULT: Flow '{}' is HIDDEN", gf.getId());
                                    break;
                                }
                            }
                        }

                        if (!matches) continue;

                        if (!applyCpmpUiFilter(gw, gf, vars)) {
                            log.info(" → RESULT: Flow '{}' hidden by CPMP UI rules", gf.getId());
                            continue;
                        }

                        log.info(" → RESULT: Flow '{}' is VISIBLE ✓", gf.getId());

                    /* =============================================================
                       BUILD NEXT STEP DTO
                       ============================================================= */
                        FlowElement next = model.getFlowElement(gf.getTargetRef());
                        String nextName = next.getName();

                        NextStepDTO dto = new NextStepDTO();
                        dto.setValidationVariable(varsStr);

                        dto.setValidationValue(extractConditionValue(cond));

                        dto.setNextTaskName(nextName);

                        String action = getActionLabelFromXml(gf);
                        if (action == null) action = "Passer pour validation";
                        dto.setActionLabel(action);

                        result.add(dto);
                    }
                }

            /* =============================================================
               CASE 2 : DIRECT SEQUENCE FLOW
               ============================================================= */
                else {

                    FlowElement next = target;

                    log.info("Direct sequence to: {}", next.getName());

                    NextStepDTO dto = new NextStepDTO();
                    dto.setValidationVariable(null);
                    dto.setValidationValue(null);
                    dto.setNextTaskName(next.getName());

                    String action = getActionLabelFromXml(of);
                    if (action == null) action = "Passer pour validation";
                    dto.setActionLabel(action);

                    result.add(dto);
                }
            }

        } catch (Exception e) {
            log.error("Error in getPossibleNextSteps", e);
        }

        return result;
    }

    private boolean applyCpmpUiFilter(
            ExclusiveGateway gw,
            SequenceFlow flow,
            Map<String, Object> vars
    ) {

        //  Ne concerne QUE le gateway CPMP
        if (!"ExclusiveGateway_1qgo4yf".equals(gw.getId())) {
            return true;
        }

        String action = getActionLabelFromXml(flow);
        Boolean isRestraint = (Boolean) vars.get("isRestraint");
        Integer etapeDac = (Integer) vars.get("etapeDac");

        // Sécurité
        if (action == null) return false;

        //  Reject → toujours visible
        if ("Reject".equalsIgnoreCase(action)) {
            return true;
        }

        //  Validate → restreint
        if ("Validate".equalsIgnoreCase(action) && Boolean.TRUE.equals(isRestraint)) {
            return true;
        }

        //  Validate → publication
        if ("Validate".equalsIgnoreCase(action)
                && Boolean.FALSE.equals(isRestraint)
                && Integer.valueOf(1).equals(etapeDac)) {
            return true;
        }

        //  Transmettre → DAO
        if ("Transmettre".equalsIgnoreCase(action)
                && Boolean.FALSE.equals(isRestraint)
                && Integer.valueOf(2).equals(etapeDac)) {
            return true;
        }

        //  Tout le reste est masqué
        return false;
    }


    private String getActionLabelFromXml(SequenceFlow flow) {
        if (flow.getExtensionElements() == null) return null;

        List<ExtensionElement> props = flow.getExtensionElements().get("property");
        if (props == null) return null;

        for (ExtensionElement p : props) {
            String name = p.getAttributeValue(null, "name");
            if ("actionLabel".equals(name)) {
                return p.getAttributeValue(null, "value");
            }
        }
        return null;
    }




    /* ======================= tiny parsing helpers ======================= */

    private String extractVariableName(String conditionExpression) {
        if (conditionExpression == null || conditionExpression.isBlank()) return null;

        String cleaned = conditionExpression.trim().replace("#{", "${");

        // Supprime les ${ et }
        if (cleaned.startsWith("${") && cleaned.endsWith("}")) {
            cleaned = cleaned.substring(2, cleaned.length() - 1).trim();
        }

        // On cherche TOUTES les variables, mais on retourne UNIQUEMENT celle qui contient "valide"
        Pattern varPattern = Pattern.compile("([a-zA-Z0-9_]+)\\s*(==|!=|<>|<=|>=|<|>)");
        Matcher matcher = varPattern.matcher(cleaned);

        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (candidate.toLowerCase().contains("valide")) {
                log.info("[GATEWAY] Variable 'valide' détectée : {}", candidate);
                return candidate; // ← ON RETOURNE LA PREMIÈRE QUI CONTIENT "valide"
            }
        }

        // Fallback : on prend la première variable trouvée si aucune ne contient "valide"
        matcher.reset();
        if (matcher.find()) {
            String first = matcher.group(1).trim();
            log.warn("[GATEWAY] Aucune variable '_valide' trouvée, fallback sur première variable : {}", first);
            return first;
        }

        log.info("[GATEWAY] Aucune variable trouvée dans l'expression : {}", conditionExpression);
        return null;
    }
    private String extractConditionValue(String conditionExpression) {
        if (conditionExpression == null || conditionExpression.isBlank())
            return null;

        String cleaned = conditionExpression.trim().replace("#{", "${");

        // strip ${ ... }
        if (cleaned.startsWith("${") && cleaned.endsWith("}")) {
            cleaned = cleaned.substring(2, cleaned.length() - 1).trim();
        }

        // BOOLEAN PATTERN → returns only "true" or "false"
        Matcher boolMatcher = Pattern.compile("==\\s*(true|false)", Pattern.CASE_INSENSITIVE)
                .matcher(cleaned);
        if (boolMatcher.find()) {
            return boolMatcher.group(1).toLowerCase();  // ← FIX: return "true" or "false"
        }

        // STRING PATTERN → returns the literal value (ex: APPROVE)
        Matcher strMatcher = Pattern.compile("==\\s*['\"]([^'\"]+)['\"]").matcher(cleaned);
        if (strMatcher.find()) {
            return strMatcher.group(1); // APPROVE / REJECT / CI
        }

        return null;
    }
    /* -------------------------------- validate / complete task -------------------------------- */
    @Transactional
    public List<TaskDTO> validateTaskGroup(List<GroupValidationRequest> requests)
            throws DacNotFound, DocNotFound, AcNotFound,
            MissingComIdException, MissingAcIdException,
            UserNotAssociatedWithAcException, UserNotAssociatedWithComException,
            ComNotFound, IOException, MonnaieNotFound {

        List<TaskDTO> responses = new ArrayList<>();

        for (GroupValidationRequest request : requests) {
            ValidationRequest validationRequest = new ValidationRequest();
            validationRequest.setValidate(request.getValidate());
            validationRequest.setComment(request.getComment());
            validationRequest.setComId(request.getComId());
            validationRequest.setFromMeeting(true);
            validationRequest.setCncmpDecision(request.getCncmpDecision());


            TaskDTO response = validateTask(request.getTaskId(), validationRequest);
            responses.add(response);
        }

        return responses;
    }

    @Transactional
    public TaskDTO validateTask(String taskId, ValidationRequest request)
            throws DacNotFound, DocNotFound, AcNotFound,
            MissingAcIdException, MissingComIdException,
            UserNotAssociatedWithAcException,
            UserNotAssociatedWithComException,
            ComNotFound, MonnaieNotFound {

        log.info("[FLOW] ==== Enter validateTask() for taskId={} ====", taskId);
        log.debug("[FLOW] Incoming ValidationRequest: {}", request);

        // --- Auth & task lookup ---
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            log.warn("[SECURITY] Missing or invalid JWT authentication");
            TaskDTO error = new TaskDTO();
            error.setStatus("Invalid or missing JWT authentication");
            return error;
        }
        log.info("[SECURITY] Authenticated user: {}", jwtAuth.getName());

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskTenantId(TENANT_ID)
                .active()
                .singleResult();

        if (task == null) {
            log.error("[TASK] Task not found or not active for id={}", taskId);
            TaskDTO error = new TaskDTO();
            error.setStatus("Task not found or not active");
            return error;
        }
        log.info("[TASK] Loaded active Flowable task '{}' ({})", task.getName(), task.getId());

        // Candidate group
        String candidateGroup = getCandidateGroup(task);
        log.debug("[TASK] Candidate group resolved: {}", candidateGroup);
        if (candidateGroup == null) {
            log.error("[TASK] Candidate group missing for task={}", taskId);
            TaskDTO error = new TaskDTO();
            error.setStatus("Task does not have a candidate group");
            return error;
        }

        // Permissions
        if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
            log.warn("[SECURITY] User {} not authorized for task {} in group {}", jwtAuth.getName(), taskId, candidateGroup);
            TaskDTO error = new TaskDTO();
            error.setStatus("User does not have the required role or assignment to validate this task");
            return error;
        }
        log.info("[SECURITY] Permission check passed for user {}", jwtAuth.getName());

        // --- Process vars & DAC ---
        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
        log.info("[FLOW] Process variables for DAC task [{}]: {}", taskId, processVariables);
        Integer id = (Integer) processVariables.getOrDefault("id", null);
        Integer version = (Integer) processVariables.getOrDefault("version", null);
        if (id == null || version == null) {
            log.error("[FLOW] Missing process variables: id={}, version={}", id, version);
            TaskDTO error = new TaskDTO();
            error.setStatus(String.format("Missing required process variables: id=%s, version=%s", id, version));
            return error;
        }

        Optional<Dac> dacOpt = dacService.findById(id, version);
        if (dacOpt.isEmpty()) {
            log.error("[DAC] DAC not found for id={}, version={}", id, version);
            TaskDTO error = new TaskDTO();
            error.setStatus("DAC not found for id " + id + " and version " + version);
            return error;
        }
        Dac latestDac = dacOpt.get();
        log.info("[DAC] Loaded DAC id={}, version={}, status={}", id, latestDac.getDacVer(), latestDac.getStatus());

        // --- Task properties  ---
        Map<String, Boolean> taskProps = getTaskProperties(task);
        Boolean isUpdatable        = taskProps.get("isUpdatable");
        Boolean isPublishable      = taskProps.get("isPublishable");
        Boolean commissionRequired = taskProps.get("commissionRequired");
        Boolean meetingRequired    = taskProps.get("meetingRequired");

        boolean fromMeeting = Boolean.TRUE.equals(request.getFromMeeting());
        log.info("[TASK] Properties: isUpdatable={}, isPublishable={}, commissionRequired={}, meetingRequired={}, fromMeeting={}",
                isUpdatable, isPublishable, commissionRequired, meetingRequired, fromMeeting);

        // --- meetingRequired enforcement  ---
        if (Boolean.TRUE.equals(meetingRequired)) {
            log.debug("[FLOW] Task requires meeting; verifying source");
            if (!fromMeeting) {
                log.warn("[FLOW] Meeting required but not triggered from meeting endpoint");
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
                            log.warn("[SECURITY] User {} lacks closing role {} for meeting-required task {}", jwtAuth.getName(), closerGroup, taskId);
                            TaskDTO error = new TaskDTO();
                            error.setStatus("Une réunion est requise. Seul le groupe de clôture peut terminer cette tâche.");
                            return error;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("[FLOW] meetingRequired enforcement failed for task [{}]: {}", taskId, e.getMessage());
            }
        }

        // --- If editable, update DAC (DacWithDacDocsRequest → new single version) ---
        if (Boolean.TRUE.equals(isUpdatable)) {
            log.info("[DAC] Task marked as updatable; processing DacWithDacDocs batch");

            DacWithDacDocsRequest dacWithDocs = request.getDacWithDacDocsRequest();
            log.info("[DAC] DacWithDacDocsRequest: {}", dacWithDocs);
            List<DacDocsRequest> updates = (dacWithDocs != null) ? dacWithDocs.getDacDocsRequests() : null;
log.info("[DAC] DacDocsRequest: {}", updates);
            if (dacWithDocs == null || updates == null || updates.isEmpty()) {

                log.error("[DAC] No DacWithDacDocsRequest / dacDocsRequests provided for updatable task {}", taskId);
                TaskDTO error = new TaskDTO();
                error.setStatus("DacWithDacDocsRequest with at least one dacDocsRequests is required when isUpdatable=true");
                return error;
            }

            for (DacDocsRequest u : updates) u.setDacId(id);

            log.debug("[DAC] Creating Dac + batch of {} DAC docs for DAC {}", updates.size(), id);
            dacDocsService.createDacWithDacDocsBatch(dacWithDocs,null);

            // refresh DAC to latest version
            latestDac = dacRepository.findDacWithLatestVersionByDacId(id);
            log.info("[DAC] Refreshed DAC after docs batch → version={}", latestDac.getDacVer());
            runtimeService.setVariable(task.getProcessInstanceId(), "version", latestDac.getDacVer());


            // refresh gateway-driving vars from DAC
            runtimeService.setVariable(task.getProcessInstanceId(), "isRestraint",
                    latestDac.getAppelOffre().getRealisation().getModePassation() != null
                            && latestDac.getAppelOffre().getRealisation().getModePassation().isRestraint());

            Integer etapeDac = 1;
            String etapeDacStr = latestDac.getEtapeDac();
            if (etapeDacStr != null && !etapeDacStr.isBlank()) {
                try {
                    Integer parsed = Integer.valueOf(etapeDacStr.trim());
                    if (parsed == 1 || parsed == 2) etapeDac = parsed;
                } catch (NumberFormatException ignored) {
                    // keep default
                }
            }
            runtimeService.setVariable(task.getProcessInstanceId(), "etapeDac", etapeDac);

            log.info("[DAC] Updated process vars after versioning: version={}, isRestraint={}, etapeDac={}",
                    latestDac.getDacVer(),
                    runtimeService.getVariable(task.getProcessInstanceId(), "isRestraint"),
                    runtimeService.getVariable(task.getProcessInstanceId(), "etapeDac"));
        }

        Object etapeObj = runtimeService.getVariable(task.getProcessInstanceId(), "etapeDac");
        if (etapeObj == null) {
            log.warn("[DAC] etapeDac is null for DAC {} — proceeding without blocking", id);
        }

        if (request.getCncmpDecision() != null) {
            runtimeService.setVariable(task.getProcessInstanceId(), "cncmpDecision", request.getCncmpDecision().name());
            log.info("[FLOW] Runtime variable 'cncmpDecision' set = {}", request.getCncmpDecision().name());
        }

        // --- Resolve next user task + its meeting/commission flags (best-effort) ---
        log.info("[FLOW] Resolving next user task and its flags for DAC [{}]", id);
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
                log.info("[FLOW] Next user task resolved: {} (meetingRequired={}, commissionRequired={})",
                        (nextUserTask != null ? nextUserTask.getName() : "none"),
                        nextMeetingRequired, nextCommissionRequired);
            } catch (Exception e) {
                log.error("[FLOW] Failed to resolve next user task for [{}]: {}", taskId, e.getMessage());
            }
        }

        boolean skipComAssociationCheck = false;
        Integer deferComIdAfterCompletion = null;
        log.debug("[COM] skipComAssociationCheck={}, deferComIdAfterCompletion={}", skipComAssociationCheck, deferComIdAfterCompletion);

        // --- Dynamic COM wiring (approve path), AC inside DAC via DAC→PPM→AC ---
        if (Boolean.TRUE.equals(request.getValidate())) {
            log.info("[COM] Evaluating dynamic commission wiring for validation path");
            Integer acIdFromDac = (latestDac.getAppelOffre().getPpm() != null && latestDac.getAppelOffre().getPpm().getAc() != null)
                    ? latestDac.getAppelOffre().getPpm().getAc().getId() : null;

            // Case: current = AC validation (no meeting) and next requires meeting → set CPMP for the AC
            if (!Boolean.TRUE.equals(meetingRequired) && Boolean.TRUE.equals(nextMeetingRequired)) {
                log.info("[COM] Case: AC→CPMP transition detected");
                if (acIdFromDac == null) throw new MissingAcIdException("acId is required to route commission from AC validation (DAC→PPM→AC)");
                Integer cpmpComId = acServiceImpl.getComIdByAcId(acIdFromDac);
                if (cpmpComId == null) throw new ComNotFound("your ac dont have a cpmp", List.of(acIdFromDac));
                Com cpmp = comServiceImpl.findById(cpmpComId);
                if (cpmp == null) throw new ComNotFound("CPMP commission not found", List.of(cpmpComId));

                attachComToDac(latestDac, cpmpComId);
                runtimeService.setVariable(task.getProcessInstanceId(), "comId", cpmpComId);
                log.info("[COM] Associated CPMP comId={} to DAC {} (process={})", cpmpComId, id, task.getProcessInstanceId());
                skipComAssociationCheck = true;
            }

            // Case: CPMP meeting and next needs commission → prepare CNCMP PER (defer switch)
            if (Boolean.TRUE.equals(meetingRequired) && Boolean.TRUE.equals(nextCommissionRequired)) {
                log.info("[COM] Case: CPMP→CNCMP(PER) transition detected");
                Com cncmpPer = comServiceImpl.findCNCMPP();
                if (cncmpPer == null) throw new ComNotFound("Aucune commission CNCMP Permanente n'est configurée.", List.of());

                attachComToDac(latestDac, cncmpPer.getId());
                deferComIdAfterCompletion = cncmpPer.getId();
                runtimeService.setVariable(task.getProcessInstanceId(), "cncmpPerComId", cncmpPer.getId());
                log.info("[COM] Deferred PER comId={} for process {}", cncmpPer.getId(), task.getProcessInstanceId());
            }
        }

        /* -------- CNCMP Decision (APPROVE / REJECT / CI) -------- */
        Boolean validateFlag = request.getValidate();
        boolean isCncmpDecisionTask = getModelUserTask(task)
                .map(ut -> userTaskHasFormProp(ut, "cncmpDecision"))
                .orElse(false);
        Integer deferComIdAfterDecision = null;
        log.debug("[FLOW] CNCMP decision task={} validateFlag={}", isCncmpDecisionTask, validateFlag);

        if (isCncmpDecisionTask) {
            var dec = request.getCncmpDecision();
            if (dec != null) {
                log.info("[COM] Processing CNCMP Decision: {}", dec);
                String pid = task.getProcessInstanceId();
                Integer perId = (Integer) runtimeService.getVariable(pid, "cncmpPerComId");

                // make sure comId switches to PER before sequence flows evaluate
                if (perId != null) {
                    runtimeService.setVariable(pid, "comId", perId);
                    try {
                        attachComToDac(latestDac, perId);
                        log.info("[COM] Attached PER comId={} to DAC {} (decision pre-apply)", perId, id);
                    } catch (Exception ignore) {}
                }
                runtimeService.setVariable(pid, "cncmpDecision", dec.name());

                switch (dec) {
                    case APPROVE -> {
                        deferComIdAfterDecision = perId;
                        validateFlag = null; // let gateway route by decision, not by cpmp_valide
                        log.info("[COM] CNCMP Decision APPROVE → Keep PER comId={}", perId);
                    }
                    case REJECT -> {
                        Integer acIdVar = (Integer) runtimeService.getVariable(pid, "acId");
                        if (acIdVar == null) throw new MissingAcIdException("acId required to route REJECT to CPMP");
                        Integer cpmpComId = acServiceImpl.getComIdByAcId(acIdVar);
                        if (cpmpComId == null) throw new ComNotFound("your ac dont have a cpmp", List.of(acIdVar));
                        deferComIdAfterDecision = cpmpComId;
                        validateFlag = null;
                        log.info("[COM] CNCMP Decision REJECT → Switch to CPMP comId={}", cpmpComId);
                    }
                    case CI -> {
                        Integer speComId = (Integer) runtimeService.getVariable(pid, "cncmpSpeComId");
                        deferComIdAfterDecision = speComId;
                        validateFlag = null;
                        log.info("[COM] CNCMP Decision CI → Switch to SCS comId={}", speComId);
                    }
                }
            }
        }

        if (isCncmpDecisionTask && request.getCncmpDecision() == CncmpDecision.APPROVE) {
            log.info("[STATUS] CNCMP APPROVE -> set status VALIDEE before Publication");
            try {
                Dac updated = dacService.updateStatus(id, latestDac.getDacVer(), ProcessStatus.VALIDE);
                if (updated != null) {
                    latestDac = updated;
                    log.info("[STATUS] DAC {} updated to VALIDEE (ver={})", id, latestDac.getDacVer());
                }
            } catch (Exception e) {
                log.warn("[STATUS] Failed to update DAC {} to VALIDEE: {}", id, e.getMessage());
            }
        }

        // --- Affectation: commissionRequired=true & not meeting ⇒ request.comId required (choose SCS) ---
        if (Boolean.TRUE.equals(commissionRequired) && !Boolean.TRUE.equals(meetingRequired)) {
            log.info("[COM] Affectation step detected (commissionRequired=true, meetingRequired=false)");
            Integer reqComId = request.getComId();
            if (reqComId == null) throw new MissingComIdException("comId is required at Affectation when validate is true");
            Com scs = comServiceImpl.findById(reqComId);
            if (scs == null) throw new ComNotFound("Commission not found", List.of(reqComId));

            attachComToDac(latestDac, reqComId);
            runtimeService.setVariable(task.getProcessInstanceId(), "comId", reqComId);
            runtimeService.setVariable(task.getProcessInstanceId(), "cncmpSpeComId", reqComId);
            log.info("[COM] DAC Affectation: Overwrote comId={} and set cncmpSpeComId", reqComId);
            skipComAssociationCheck = true;

            try {
                String pid = task.getProcessInstanceId();
                Integer perId  = (Integer) runtimeService.getVariable(pid, "cncmpPerComId");
                Integer acIdV  = (Integer) runtimeService.getVariable(pid, "acId");
                Integer cpmpId = (acIdV != null) ? acServiceImpl.getComIdByAcId(acIdV) : null;
                attachComToDac(latestDac, perId);
                attachComToDac(latestDac, cpmpId);
                log.info("[COM] DAC Affectation: ensured SCS/PER/CPMP linked");
            } catch (Exception e) {
                log.warn("[COM] DAC Affectation: failed to persist linked commissions: {}", e.getMessage());
            }
        }

        // --- Association checks with CURRENT step's comId ---
        Integer currComId = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
        log.debug("[CHECK] Current comId from runtime vars: {}", currComId);

        if ((!skipComAssociationCheck && currComId != null) && !Boolean.TRUE.equals(isPublishable)) {
            List<Integer> userComIds = currentUserService.getCurrentUserComIds();
            log.debug("[CHECK] userComIds={}", userComIds);
            if (userComIds == null || !userComIds.contains(currComId)) {
                log.error("[SECURITY] User not associated with comId {}", currComId);
                throw new UserNotAssociatedWithComException(
                        "User is not associated with the commission ID: " + currComId);
            }
            log.info("[SECURITY] User validated for commission ID: {}", currComId);
        } else if (currComId == null) {
            Integer acIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "acId");
            log.debug("[CHECK] No comId; fallback to AC association check acId={}", acIdVar);
            if (acIdVar == null) throw new MissingAcIdException("acId is required in process variables for tasks without comId");
            List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
            if (userAcIds == null || !userAcIds.contains(acIdVar)) {
                log.error("[SECURITY] User not associated with AC ID {}", acIdVar);
                throw new UserNotAssociatedWithAcException(
                        "User is not associated with the AC ID: " + acIdVar);
            }
            log.info("[SECURITY] User validated for AC ID: {}", acIdVar);
        } else {
            log.info("[CHECK] Skipping com association check at system hand-off (comId={})", currComId);
        }

        // Persist CURRENT comId into DAC.coms BEFORE status update
        try {
            Integer snap = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
            if (snap != null) {
                attachComToDac(latestDac, snap);
                log.info("[COM] Persisted current comId={} into DAC {}", snap, latestDac.getDacId());
            }
        } catch (Exception e) {
            log.warn("[COM] Failed to persist current comId before status update: {}", e.getMessage());
        }

        // --- Status updates ---
        log.info("[FLOW] Starting DAC status update logic for DAC {} (validate={}, publishable={})",
                id, request.getValidate(), isPublishable);

        ProcessStatus newStatus = null;
        boolean actedByDecision = isCncmpDecisionTask && request.getCncmpDecision() != null;

        if (Boolean.TRUE.equals(isPublishable)) {
            newStatus = ProcessStatus.PUBLIE;
            Dac updated = dacService.updateStatus(id, latestDac.getDacVer(), newStatus);
            if (updated == null) {
                log.error("[DAC] Failed to update status to Publiée");
                TaskDTO error = new TaskDTO();
                error.setStatus("Failed to update DAC status to Publiée");
                return error;
            }
            latestDac = updated;
            log.info("[DAC] Status updated to Publiée (DAC id={}, ver={})", id, latestDac.getDacVer());

        } else if ((request.getValidate() != null) || actedByDecision) {
            newStatus = ProcessStatus.EN_TRAITEMENT;
            Dac updated = dacService.updateStatus(id, latestDac.getDacVer(), newStatus);
            if (updated == null) {
                log.error("[DAC] Failed to update status to En cours de traitement");
                TaskDTO error = new TaskDTO();
                error.setStatus("Failed to update DAC status to En cours de traitement");
                return error;
            }
            latestDac = updated;
            log.info("[DAC] Updated status to En cours de traitement (DAC id={}, ver={})", id, latestDac.getDacVer());

        } else if (request.getValidate() != null && !request.getValidate()) {
            newStatus = ProcessStatus.EN_TRAITEMENT;
            Dac updated = dacService.updateStatus(id, latestDac.getDacVer(), newStatus);
            if (updated == null) {
                log.error("[DAC] Failed to keep status En cours de traitement");
                TaskDTO error = new TaskDTO();
                error.setStatus("Failed to keep status En cours de traitement");
                return error;
            }
            latestDac = updated;
            log.info("[DAC] Status remains En cours de traitement (DAC id={}, ver={})", id, latestDac.getDacVer());
        }

        log.info("[FLOW] ===== End of Part 2: Status update complete for DAC {} =====", id);

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

            log.info("[COM] SAFETY#1 ensured CPMP={}, PER={}, SCS={} linked to DAC {}", cpmpId, perId, scsId, id);
        } catch (Exception e) {
            log.warn("[COM] SAFETY#1 failed to re-ensure associations: {}", e.getMessage());
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
                        if (target instanceof ExclusiveGateway) {
                            for (SequenceFlow gf : ((ExclusiveGateway) target).getOutgoingFlows()) {
                                String conditionExpression = gf.getConditionExpression();
                                if (conditionExpression != null && conditionExpression.toLowerCase().contains("valide")) {
                                    String variableName = extractVariableName(conditionExpression);
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
            log.info("[FLOW] Gateway variable resolved: {}, nextTaskIsLast={}", processVariable, nextTaskIsLast);
        } catch (Exception e) {
            log.warn("[FLOW] Failed to extract gateway variable for task [{}]: {}", taskId, e.getMessage());
        }

        // Sanitize gateway variable name if a full expression slipped through (e.g., "cpmp_valide == true")
        if (processVariable != null && processVariable.contains("==")) {
            processVariable = processVariable.substring(0, processVariable.indexOf("==")).trim();
        }
        if (processVariable == null || processVariable.isBlank()) {
            processVariable = candidateGroup.toLowerCase() + "_valide";
        }

        // --- Write vars, traceability, early VALIDEE if entering last user task (when not publishable) ---
        Map<String, Object> varsToSet = new HashMap<>();
        if (request.getValidate() != null) {
            varsToSet.put(processVariable, request.getValidate());
        }
        varsToSet.put("comment", request.getComment() != null ? request.getComment() : "");
        varsToSet.put("dateComment", LocalDateTime.now());
        varsToSet.put("treater", jwtAuth.getName());
        if (Boolean.TRUE.equals(isPublishable)) varsToSet.put("isPublishable", true);

        // if decision provided, keep it in runtime as trace
        if (request.getCncmpDecision() != null) {
            varsToSet.put("cncmpDecision", request.getCncmpDecision().name());
        }

        runtimeService.setVariables(task.getProcessInstanceId(), varsToSet);
        log.info("[FLOW] Runtime variables updated for traceability: {}", varsToSet);

        if (nextTaskIsLast && !Boolean.TRUE.equals(isPublishable)) {
            Dac updated = dacService.updateStatus(id, latestDac.getDacVer(), ProcessStatus.VALIDE);
            if (updated != null) {
                latestDac = updated;
                log.info("[DAC] Status auto-set to VALIDÉE before entering last task, DAC ver={}", latestDac.getDacVer());
            }
        }

        dacService.save(latestDac);
        log.info("[DAC] Persisted DAC {} after updates", latestDac.getDacId());

        taskService.setAssignee(taskId, Authentication.getCurrentUserId().orElse(jwtAuth.getName()));
        log.info("[TASK] Assignee set to {}", jwtAuth.getName());

        if (request.getComment() != null) {
            taskService.addComment(taskId, task.getProcessInstanceId(), request.getComment());
            log.debug("[TASK] Comment added: {}", request.getComment());
        }

        // Snapshot current comId
        Integer comIdSnapshot = null;
        try {
            comIdSnapshot = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
            log.info("[CHECK] Snapshot current comId before completion: {}", comIdSnapshot);
        } catch (Exception ex) {
            log.debug("[CHECK] Could not snapshot comId [{}]: {}", taskId, ex.getMessage());
        }

        if (comIdSnapshot != null) {
            try {
                attachComToDac(latestDac, comIdSnapshot);
                log.info("[COM] Associated current comId={} to DAC {}", comIdSnapshot, id);
            } catch (Exception e) {
                log.warn("[COM] Failed to attach comId {} to DAC {}: {}", comIdSnapshot, id, e.getMessage());
            }
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
                log.info("[COM] SCS→PER: defer switch back to Permanent comId={} after completion", perId);
            }
        } catch (Exception e) {
            log.warn("[COM] SCS→PER planning failed: {}", e.getMessage());
        }

        // Apply deferred comId changes
        Integer finalSwitch = (deferComIdAfterDecision != null) ? deferComIdAfterDecision : deferComIdAfterCompletion;
        if (finalSwitch != null) {
            runtimeService.setVariable(task.getProcessInstanceId(), "comId", finalSwitch);
            try {
                attachComToDac(latestDac, finalSwitch);
                log.info("[COM] Deferred comId {} successfully applied to DAC {}", finalSwitch, id);
            } catch (Exception e) {
                log.warn("[COM] Failed to apply deferred comId {}: {}", finalSwitch, e.getMessage());
            }
        }

        // === SAFETY CASE #2 === keep CPMP associated right before completing
        try {
            String pid = task.getProcessInstanceId();
            Integer acIdForCpmp = (Integer) runtimeService.getVariable(pid, "acId");
            Integer cpmpId = (acIdForCpmp != null) ? acServiceImpl.getComIdByAcId(acIdForCpmp) : null;
            attachComToDac(latestDac, cpmpId);
            log.info("[COM] SAFETY#2 ensured CPMP comId={} linked before completion", cpmpId);
        } catch (Exception e) {
            log.warn("[COM] SAFETY#2 failed before completion: {}", e.getMessage());
        }

        if (request.getCncmpDecision() != null) {
            String pid = task.getProcessInstanceId();
            runtimeService.setVariable(pid, "cncmpDecision", request.getCncmpDecision().name());
            log.info("[FLOW] ✅ Persisted cncmpDecision={} into process instance {}", request.getCncmpDecision().name(), pid);
        }

        // --- FINAL COM SYNC before completing task ---
        try {
            String pid = task.getProcessInstanceId();
            Integer nextComId = (Integer) runtimeService.getVariable(pid, "comId");
            if (nextComId != null) {
                attachComToDac(latestDac, nextComId);
                log.info("[COM] Final sync: ensured comId={} attached before completing task {}", nextComId, taskId);
            } else {
                log.warn("[COM] No comId variable found before completion for task {}", taskId);
            }
        } catch (Exception e) {
            log.warn("[COM] Failed final com sync before completion: {}", e.getMessage());
        }

        String processInstanceId = task.getProcessInstanceId();
        Integer dacId = (Integer) runtimeService.getVariable(processInstanceId, "id");
        Integer dacVer = (Integer) runtimeService.getVariable(processInstanceId, "version");

        // If process ended, force PUBLIE
        boolean finished = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .finished().count() > 0;

        if (finished) {
            latestDac = dacService.updateStatus(id, latestDac.getDacVer(), ProcessStatus.PUBLIE);
            log.info("[FLOW] Process ended; DAC set to PUBLIE (DAC id={}, ver={})", id, latestDac.getDacVer());
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

        Integer finalComId = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId");
        if (finalComId != null) {
            resp.setComId(finalComId);
            log.info("[RESP] Returning TaskDTO with comId={}", finalComId);
        } else {
            log.warn("[RESP] No comId variable found for processInstanceId={}", task.getProcessInstanceId());
        }

        resp.setStatus("Task validated successfully");
        log.info("[FLOW] ==== validateTask() completed for taskId={} ====", taskId);

        // Complete task
        taskService.complete(taskId);
        log.info("[TASK] Task [{}] completed successfully", taskId);
        updateDacEtapeProcess(processInstanceId, dacId, dacVer);
        return resp;
    }





    private void attachComToDac(Dac dac, Integer comId) throws ComNotFound {
        if (comId == null) {
            log.warn("❌ attachComToDac called with null comId for DAC(id={}, ver={})", dac.getDacId(), dac.getDacVer());
            return;
        }

        log.info("🔸 [attachComToDac] Start linking DAC(id={}, ver={}) to COM(id={})", dac.getDacId(), dac.getDacVer(), comId);

        // Ensure DAC is managed by EntityManager
        if (!entityManager.contains(dac)) {
            log.debug("🔹 DAC entity is detached, merging...");
            dac = entityManager.merge(dac);
        }

        // Load commission entity
        Com com = comServiceImpl.findById(comId);
        if (com == null) {
            log.error("❌ [attachComToDac] Commission with ID {} not found", comId);
            throw new ComNotFound("Commission not found with ID: " + comId, List.of(comId));
        }

        // Initialize the coms list if necessary
        if (dac.getComs() == null) {
            dac.setComs(new ArrayList<>());
            log.debug("🟡 Initialized empty DAC.coms list for DAC(id={}, ver={})", dac.getDacId(), dac.getDacVer());
        }

        // Check if already linked
        boolean alreadyLinked = dac.getComs().stream()
                .anyMatch(c -> Objects.equals(c.getId(), comId));

        if (!alreadyLinked) {
            dac.getComs().add(com);
            log.info("✅ [attachComToDac] Added COM(id={}) to DAC(id={}, ver={}) list", comId, dac.getDacId(), dac.getDacVer());
            try {
                dacService.save(dac);
                entityManager.flush();
                log.info("💾 [attachComToDac] Persisted join DAC_COM (DAC={}, COM={})", dac.getDacId(), comId);
            } catch (Exception e) {
                log.error("❌ [attachComToDac] Failed to persist DAC_COM link: {}", e.getMessage(), e);
            }
        } else {
            log.info("ℹ️ [attachComToDac] DAC(id={}, ver={}) already linked to COM(id={})", dac.getDacId(), dac.getDacVer(), comId);
        }
    }

    /* -------------------------------- list my tasks -------------------------------- */

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
                        instances.stream().map(ProcessInstance::getId).collect(Collectors.toList())
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
                        instances.stream().map(ProcessInstance::getId).collect(Collectors.toList())
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

        List<Task> filtered = new ArrayList<>();
        int chunk = 1000;
        for (int i = 0; i < matchingProcessInstanceIds.size(); i += chunk) {
            List<String> part = matchingProcessInstanceIds.subList(i, Math.min(i + chunk, matchingProcessInstanceIds.size()));
            if (part.isEmpty()) continue;
            filtered.addAll(
                    taskService.createTaskQuery()
                            .taskTenantId(TENANT_ID)
                            .taskCandidateGroupIn(userCandidateGroups)
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
            dto.setMeetingRequired(props.get("meetingRequired"));
            Integer comIdVar = (Integer) runtimeService.getVariable(t.getProcessInstanceId(), "comId"); // ADDED
            if (comIdVar != null) dto.setComId(comIdVar);
            return dto;
        }).collect(Collectors.toList());

        TaskListResponseDTO r = new TaskListResponseDTO();
        r.setTenantId(TENANT_ID);
        r.setTasks(dtos);
        r.setCandidateGroup(String.join(", ", userCandidateGroups));
        r.setStatus("Tasks retrieved successfully");
        return r;
    }

    /* -------------------------------- DAC + visible task -------------------------------- */

    @Transactional
    public TaskDetailsDTO getDacWithTasks(Integer dacId) throws DacNotFound, AcNotFound {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        List<Integer> comIds=currentUserService.getCurrentUserComIds();
        List<Integer> acIds=currentUserService.getCurrentUserAcIds();
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
        PvResponse listPvComByDac=dacService.listPvComByDac(comIds,dacId,dac.getDacVer());
        ReunionArgumentDocuments listDocDac=dacService.listDocDac(dac.getComs().stream().map(Com::getId).collect(Collectors.toList()),dacId,dac.getDacVer());
        ExtraitResponse listExtrais= dacService.findAllExtraits (acIds, dacId, dac.getDacVer());
        resp.getDacDocs().setPvCPMP(listPvComByDac.getPvCPMP().stream().distinct().collect(Collectors.toList()));
        resp.getDacDocs().setPvCNCMPPermanente(listPvComByDac.getPvCNCMPPermanente().stream().distinct().collect(Collectors.toList()));
        resp.getDacDocs().setPvCNCMPSpecialise(listPvComByDac.getPvCNCMPSpecialise().stream().distinct().collect(Collectors.toList()));
        resp.getDacDocs().setDocumentCPMP(listDocDac.getDocumentCPMP());
        resp.getDacDocs().setDocumentCNCMPSpecialise(listDocDac.getDocumentCNCMPSpecialise());
        resp.getDacDocs().setDocumentCNCMPPermanente(listDocDac.getDocumentCNCMPPermanente());
        resp.getDacDocs().setExtraitCPMP(listExtrais.getExtraitCPMP().stream().distinct().collect(Collectors.toList()));
        resp.getDacDocs().setExtraitCNCMPP(listExtrais.getExtraitCNCMPP().stream().distinct().collect(Collectors.toList()));
        if (dacDocs != null) {
            dacDocs.setAoId(dac.getAppelOffre().getId());
            dacDocs.setAoReference(dac.getAppelOffre().getReference());
            List<DacDynaForm> dacDynaForms = dacDynaFormRepository.findByDacIdAndDacVer(dac.getDacId(), dac.getDacVer())
                    .stream()
                    .filter(df -> !Boolean.TRUE.equals(df.getIsDeleted()))
                    .collect(Collectors.toList());
            if (dacDynaForms != null && !dacDynaForms.isEmpty()) {
                Map<com.sigmap.common.entity.CompositeId, List<DacDynaForm>> grouped = dacDynaForms.stream()
                        .collect(Collectors.groupingBy(df -> new com.sigmap.common.entity.CompositeId(df.getDynaFormId(), df.getDynaFormVer())));

                List<DynaFormResponse> dynaFormsList = new ArrayList<>();
                for (Map.Entry<com.sigmap.common.entity.CompositeId, List<DacDynaForm>> entry : grouped.entrySet()) {
                    dynaFormRepository.findById(entry.getKey()).ifPresent(f -> {
                        DynaFormResponse fResp = dynaFormMapper.toResponse(f);
                        Map<Integer, DacDynaForm> fieldMap = entry.getValue().stream()
                                .collect(Collectors.toMap(DacDynaForm::getDynaFormFieldsId, df -> df, (a, b) -> a));

                        // Populate display attribute for each field
                        for (DynaFormFieldResponse fieldResp : fResp.getFields()) {
                            DacDynaForm ddf = fieldMap.get(fieldResp.getDynaFormFieldsId());
                            if (ddf != null && Objects.equals(ddf.getDisplay(), true)) {
                                fieldResp.setDisplay(true);
                            } else {
                                fieldResp.setDisplay(false);
                            }
                        }

                        dynaFormsList.add(fResp);
                    });
                }
                dacDocs.setDynaForms(dynaFormsList);
            } else {
                dacDocs.setDynaForms(new ArrayList<>());
            }

            // Populating submissionPieces in DacResponseDTO
            List<DacDocSoumission> dacDocSoumissions = dacDocSoumissionRepository.findByDacIdAndDacVer(dac.getDacId(), dac.getDacVer());
            List<DacDocSoumissionResponse> submissionPieces = dacDocSoumissions.stream()
                    .map(dacDocSoumissionMapper::map)
                    .collect(Collectors.toList());
            dacDocs.setSubmissionPieces(submissionPieces);
        }


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
            resp.setProcessInstanceId(dac.getProcessInstanceId());
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
        Integer comIdVar = (Integer) runtimeService.getVariable(task.getProcessInstanceId(), "comId"); // ADDED
        if (comIdVar != null)
            resp.setComId(comIdVar);
        Map<String, Boolean> props = getTaskProperties(task);
        resp.setIsUpdatable(props.get("isUpdatable"));
        resp.setIsPublishable(props.get("isPublishable"));
        resp.setMeetingRequired(props.get("meetingRequired"));
        resp.setCommissionRequired(props.get("commissionRequired"));
        resp.setNextSteps(getPossibleNextSteps(task));
        resp.setStatus("DAC and task details retrieved successfully");

        return resp;
    }

    /* -------------------------------- history -------------------------------- */

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

    @Transactional
    public void updateDacEtapeProcess(String processInstanceId,Integer dacId,Integer dacVer) {

        // get the next active task
        var nextTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();

        String stepName = (nextTask != null ? nextTask.getName() : "Completed");


       Dac dac = dacRepository.findDacWithLatestVersionByDacId(dacId);
              dac.setEtapeProcess(stepName);
                dacRepository.save(dac);
            };

    @Transactional(readOnly = true)
    public TaskListResponseDTO getDacTasksByComId(Integer comId) {

        TaskListResponseDTO response = new TaskListResponseDTO();

        // Auth check
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
            response.setStatus("No active DAC process instances found for comId " + comId);
            return response;
        }

        List<String> processInstanceIds = instances.stream()
                .map(ProcessInstance::getId)
                .collect(Collectors.toList());

        // User candidate groups
        List<String> userCandidateGroups = jwtAuth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        List<TaskDTO> taskList = new ArrayList<>();
        int chunk = 1000;

        for (int i = 0; i < processInstanceIds.size(); i += chunk) {

            List<String> part = processInstanceIds.subList(i, Math.min(i + chunk, processInstanceIds.size()));

            List<Task> tasks = taskService.createTaskQuery()
                    .taskTenantId(TENANT_ID)
                    .processInstanceIdIn(part)
                    .active()
                    .orderByTaskCreateTime().desc()
                    .list();

            for (Task task : tasks) {

                if (!hasCandidateGroup(task, jwtAuth) && !hasAssign(task, jwtAuth)) {
                    continue;
                }

                TaskDTO dto = new TaskDTO();
                dto.setTaskId(task.getId());
                dto.setTaskName(task.getName());
                dto.setTaskDefinitionKey(task.getTaskDefinitionKey());
                dto.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
                dto.setProcessInstanceId(task.getProcessInstanceId());
                dto.setCreated(task.getCreateTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
                dto.setCandidateGroup(getCandidateGroup(task));

                // Load process vars
                Map<String, Object> vars = runtimeService.getVariables(task.getProcessInstanceId());
                Integer dacId = (Integer) vars.get("id");
                Integer dacVer = (Integer) vars.get("version");

                // Load correct DAC information
                if (dacId != null && dacVer != null) {
                    try {
                        Optional<Dac> dacOpt = dacService.findById(dacId, dacVer);
                        if (dacOpt.isPresent()) {
                            Dac dac = dacOpt.get();
                            dto.setId(dacId);
                            dto.setVersion(dacVer);
                            dto.setRef(dac.getReference());
                            dto.setNom(dac.getAppelOffre().getPpm().getNom());
                            dto.setStatus(String.valueOf(dac.getStatus()));


                            if (dac.getAppelOffre().getPpm().getAc() != null) {
                                dto.setNomAc(dac.getAppelOffre().getPpm().getAc().getNom());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to load DAC id {} ver {}: {}", dacId, dacVer, e.getMessage());
                    }
                }

                // Traceability
                Map<String, Object> trace = new HashMap<>();
                trace.put("decision", vars.getOrDefault("decision", ""));
                trace.put("comment", vars.getOrDefault("comment", ""));
                trace.put("dateComment", vars.getOrDefault("dateComment", ""));
                trace.put("treater", vars.getOrDefault("treater", ""));
                dto.setTraceability(trace);

                // Task properties from BPMN
                Map<String, Boolean> props = getTaskProperties(task);
                dto.setIsUpdatable(props.get("isUpdatable"));
                dto.setIsPublishable(props.get("isPublishable"));
                dto.setCommissionRequired(props.get("commissionRequired"));
                dto.setMeetingRequired(props.get("meetingRequired"));

                Boolean meetingRequired = props.get("meetingRequired");
                boolean exists = reunionDacRepository.exists(
                        ReunionDacSpecifications.hasDacId(dto.getId())
                                .and(ReunionDacSpecifications.hasDacVer(dto.getVersion()))
                                .and(
                                        ReunionDacSpecifications.reunionStatus(ReunionStatus.EN_COURS)
                                                .or(ReunionDacSpecifications.reunionStatus(ReunionStatus.VALIDER))
                                                .or(ReunionDacSpecifications.reunionStatus(ReunionStatus.PLANIFIER))
                                )
                );


                if (meetingRequired != null && meetingRequired && !exists) {
                    dto.setMeetingRequired(true);
                    taskList.add(dto);
                }
            }
        }

        response.setTenantId(TENANT_ID);
        response.setTasks(taskList);
        response.setCandidateGroup(
                userCandidateGroups.isEmpty() ? null : String.join(", ", userCandidateGroups)
        );
        response.setStatus("DAC tasks retrieved successfully for comId " + comId);

        return response;
    }



}
