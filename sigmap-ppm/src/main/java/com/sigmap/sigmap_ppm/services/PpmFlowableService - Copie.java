package com.sigmap.sigmap_ppm.services;//package com.sigmap.sigmap_ppm.ppms.services;
//
//import com.sigmap.common.ppms.Ppm;
//import com.sigmap.common.ppms.PpmService;
//import com.sigmap.common.ppms.mappers.PpmMapper;
//import com.sigmap.common.ppms.response.PpmResponse;
//import com.sigmap.common.request.ValidationRequest;
//import com.sigmap.common.response.ProcessStartResponseDTO;
//import com.sigmap.common.response.TaskDTO;
//import com.sigmap.common.response.TaskListResponseDTO;
//import com.sigmap.sigmap_ppm.ppms.dto.TaskDetailsDTO;
//import org.flowable.bpmn.model.BpmnModel;
//import org.flowable.bpmn.model.ExclusiveGateway;
//import org.flowable.bpmn.model.FlowElement;
//import org.flowable.bpmn.model.SequenceFlow;
//import org.flowable.engine.RepositoryService;
//import org.flowable.engine.RuntimeService;
//import org.flowable.engine.TaskService;
//import org.flowable.task.api.Task;
//import org.flowable.engine.runtime.ProcessInstance;
//import org.flowable.identitylink.api.IdentityLink;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class PpmFlowableService {
//
//    @Value("${tenant.id}")
//    private String TENANT_ID;
//
//    @Autowired
//    private RuntimeService runtimeService;
//
//    @Autowired
//    private TaskService taskService;
//
//    @Autowired
//    private PpmService ppmService;
//
//    @Autowired
//    private RepositoryService repositoryService;
//
//    @Transactional
//    public ProcessStartResponseDTO startProcessInstance(Integer id, Integer version) {
//        // Update PPM status
//        Ppm updated = ppmService.updateStatus(id, version, 2);
//
//        // Start the process
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("id", id);
//        variables.put("version", version);
//
//        ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("ppmProcess", variables, TENANT_ID);
//
//        // Find the saisieTask
//        List<Task> tasks = taskService.createTaskQuery()
//                .processInstanceId(processInstance.getId())
//                .taskTenantId(TENANT_ID)
//                .taskDefinitionKey("saisieTask")
//                .active()
//                .list();
//
//        if (tasks.isEmpty()) {
//            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
//            errorResponse.setStatus("Saisie task not found for the process");
//            return errorResponse;
//        }
//
//        if (tasks.size() > 1) {
//            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
//            errorResponse.setStatus("Multiple saisie tasks found");
//            return errorResponse;
//        }
//
//        Task saisieTask = tasks.get(0);
//
//        // Get user roles from JwtAuthenticationToken
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (!(authentication instanceof JwtAuthenticationToken)) {
//            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
//            errorResponse.setStatus("Invalid or missing JWT authentication");
//            return errorResponse;
//        }
//
//        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//        // Get candidate groups for the task
//        List<String> candidateGroups = taskService.getIdentityLinksForTask(saisieTask.getId()).stream()
//                .filter(link -> "candidate".equals(link.getType()))
//                .map(IdentityLink::getGroupId)
//                .collect(Collectors.toList());
//
//        // Check if user has a matching role
//        boolean hasMatchingRole = jwtAuth.getAuthorities().stream()
//                .map(authority -> authority.getAuthority())
//                .anyMatch(auth -> candidateGroups.contains(auth.replace("ROLE_", "")));
//
//        if (!hasMatchingRole) {
//            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
//            errorResponse.setStatus("User does not have a role matching the candidate group(s): " + String.join(", ", candidateGroups));
//            return errorResponse;
//        }
//
//        // Complete the saisieTask
//        taskService.complete(saisieTask.getId());
//
//        // Prepare response
//        ProcessStartResponseDTO response = new ProcessStartResponseDTO();
//        response.setProcessInstanceId(processInstance.getId());
//        response.setProcessDefinitionId(processInstance.getProcessDefinitionId());
//        response.setTaskId(saisieTask.getId());
//        response.setStatus("Process started and saisie task completed successfully");
//
//        return response;
//    }
//
//    public TaskListResponseDTO getCurrentTasks() {
//        // Fetch active tasks for the tenant ID
//        List<Task> tasks = taskService.createTaskQuery()
//                .taskTenantId(TENANT_ID)
//                .active()
//                .orderByTaskCreateTime().asc()
//                .list();
//
//        // Prepare task list
//        List<TaskDTO> taskList = tasks.stream().map(task -> {
//            TaskDTO taskInfo = new TaskDTO();
//            taskInfo.setTaskId(task.getId());
//            taskInfo.setTaskName(task.getName());
//            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
//            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
//            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
//            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
//
//            // Retrieve id and version from process instance variables
//            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//            taskInfo.setId((Integer) processVariables.getOrDefault("id", null));
//            taskInfo.setVersion((Integer) processVariables.getOrDefault("version", null));
//
//            // Add traceability variables
//            Map<String, Object> variables = new HashMap<>();
//            variables.put("decision", processVariables.getOrDefault("decision", ""));
//            variables.put("comment", processVariables.getOrDefault("comment", ""));
//            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
//            variables.put("treater", processVariables.getOrDefault("treater", ""));
//            taskInfo.setTraceability(variables);
//
//            return taskInfo;
//        }).collect(Collectors.toList());
//
//        // Prepare response
//        TaskListResponseDTO response = new TaskListResponseDTO();
//        response.setTenantId(TENANT_ID);
//        response.setTasks(taskList);
//        response.setStatus("Tasks retrieved successfully");
//
//        return response;
//    }
//
//    public TaskDetailsDTO getTaskDetails(String taskId) {
//        // Get user roles from JwtAuthenticationToken
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (!(authentication instanceof JwtAuthenticationToken)) {
//            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
//            errorResponse.setStatus("Invalid or missing JWT authentication");
//            return errorResponse;
//        }
//
//        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//
//        // Fetch the task by taskId
//        Task task = taskService.createTaskQuery()
//                .taskId(taskId)
//                .taskTenantId(TENANT_ID)
//                .active()
//                .singleResult();
//
//        if (task == null) {
//            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
//            errorResponse.setStatus("Task not found or not active");
//            return errorResponse;
//        }
//
//        // Determine the task's candidate group
//        String candidateGroup = null;
//        for (IdentityLink link : taskService.getIdentityLinksForTask(task.getId())) {
//            if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
//                candidateGroup = link.getGroupId();
//                break;
//            }
//        }
//
//        if (candidateGroup == null) {
//            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
//            errorResponse.setStatus("Task does not have a candidate group");
//            return errorResponse;
//        }
//
//        // Assign to final variable for lambda
//        final String finalCandidateGroup = candidateGroup;
//
//        // Check if user has a matching role
//        boolean hasMatchingRole = jwtAuth.getAuthorities().stream()
//                .map(authority -> authority.getAuthority())
//                .anyMatch(auth -> finalCandidateGroup.equals(auth.replace("ROLE_", "")));
//
//        if (!hasMatchingRole) {
//            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
//            errorResponse.setStatus("User does not have a role matching the candidate group: " + finalCandidateGroup);
//            return errorResponse;
//        }
//
//        // Prepare task details
//        TaskDetailsDTO response = new TaskDetailsDTO();
//        response.setTaskId(task.getId());
//        response.setTaskName(task.getName());
//        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
//        response.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
//        response.setProcessInstanceId(task.getProcessInstanceId());
//        response.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
//        response.setCandidateGroup(finalCandidateGroup);
//
//        // Retrieve id from process instance variables
//        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//        Integer id = (Integer) processVariables.getOrDefault("id", null);
//        Integer version = (Integer) processVariables.getOrDefault("version", null);
//
//        // Fetch all Ppm entities related to this task (by id) and map to PpmResponse
//        List<PpmResponse> ppmResponses = List.of();
//        if (id != null && version != null) {
//            Optional<Ppm> relatedPpms = ppmService.findById(id, version);
//            ppmResponses = relatedPpms.stream()
//                    .map(PpmMapper.INSTANCE::map)
//                    .collect(Collectors.toList());
//        }
//        response.setPpms(ppmResponses);
//
//        // Add traceability variables
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("decision", processVariables.getOrDefault("decision", ""));
//        variables.put("comment", processVariables.getOrDefault("comment", ""));
//        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
//        variables.put("treater", processVariables.getOrDefault("treater", ""));
//        response.setTraceability(variables);
//
//        response.setStatus("Task details retrieved successfully");
//
//        return response;
//    }
//
////    @Transactional
////    public TaskDTO validateTask(String taskId, ValidationRequest request) {
////        // Get user roles from JwtAuthenticationToken
////        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////        if (!(authentication instanceof JwtAuthenticationToken)) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("Invalid or missing JWT authentication");
////            return errorResponse;
////        }
////
////        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
////
////        // Fetch the task by taskId
////        Task task = taskService.createTaskQuery()
////                .taskId(taskId)
////                .taskTenantId(TENANT_ID)
////                .active()
////                .singleResult();
////
////        if (task == null) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("Task not found or not active");
////            return errorResponse;
////        }
////
////        // Determine the task's candidate group
////        String candidateGroup = null;
////        for (IdentityLink link : taskService.getIdentityLinksForTask(task.getId())) {
////            if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
////                candidateGroup = link.getGroupId();
////                break;
////            }
////        }
////
////        if (candidateGroup == null) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("Task does not have a candidate group");
////            return errorResponse;
////        }
////
////        // Assign to final variable for lambda
////        final String finalCandidateGroup = candidateGroup;
////
////        // Check if user has a matching role
////        boolean hasMatchingRole = jwtAuth.getAuthorities().stream()
////                .map(authority -> authority.getAuthority())
////                .anyMatch(auth -> finalCandidateGroup.equals(auth.replace("ROLE_", "")));
////
////        if (!hasMatchingRole) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("User does not have a role matching the candidate group: " + finalCandidateGroup);
////            return errorResponse;
////        }
////
////        // Get PPM details from process variables
////        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
////        Integer id = (Integer) processVariables.getOrDefault("id", null);
////        Integer version = (Integer) processVariables.getOrDefault("version", null);
////
////        if (id == null || version == null) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("No associated PPM ID found in process variables");
////            return errorResponse;
////        }
////
////        // Get the latest PPM entity
////        Optional<Ppm> ppmOptional = ppmService.findById(id, version);
////        if (ppmOptional.isEmpty()) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("No PPM entities found for id " + id);
////            return errorResponse;
////        }
////
////        Ppm latestPpm = ppmOptional.get();
////
////        // Handle validation dynamically
////        boolean isValid = request.getIsValid();
////        Integer newStatus = isValid ? 4 : 5; // Default: A_publie or A_rectifiee
////        String processVariable = finalCandidateGroup.toLowerCase() + "_valide"; // e.g., validateur_ac_valide
////
////        // Special case for Reserve_CPMP task
////        if (task.getTaskDefinitionKey() != null && task.getTaskDefinitionKey().equals("Reserve_CPMP")) {
////            newStatus = isValid ? 8 : 5; // CNMP_A_VALIDER or A_rectifiee
////            processVariable = "reserve_valide";
////        }
////
////        // Update PPM status
////        Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
////        if (updatedPpm == null) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("Failed to update PPM status");
////            return errorResponse;
////        }
////
////        // Set process variable if needed
////        if (processVariable != null) {
////            runtimeService.setVariable(task.getProcessInstanceId(), processVariable, isValid);
////        }
////
////        // Add traceability variables
////        Map<String, Object> variables = new HashMap<>();
////        variables.put("decision", request.getIsValid());
////        variables.put("comment", request.getComment());
////        variables.put("dateComment", LocalDateTime.now());
////        variables.put("treater", jwtAuth.getName());
////        runtimeService.setVariables(task.getProcessInstanceId(), variables);
////
////        // Complete the task
////        taskService.complete(taskId);
////
////        // Prepare response
////        TaskDTO response = new TaskDTO();
////        response.setTaskId(task.getId());
////        response.setTaskName(task.getName());
////        response.setProcessInstanceId(task.getProcessInstanceId());
////        response.setId(id);
////        response.setVersion(version);
////        response.setNewStatus(newStatus);
////        response.setIsValid(isValid);
////        response.setTraceability(variables);
////        response.setStatus("Task validated successfully");
////
////        return response;
////    }
//
//    @Transactional
//    public TaskDTO validateTask(String taskId, ValidationRequest request) {
//        // Get user roles from JwtAuthenticationToken
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (!(authentication instanceof JwtAuthenticationToken)) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Invalid or missing JWT authentication");
//            return errorResponse;
//        }
//
//        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//
//        // Fetch the task by taskId
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
//        // Determine the task's candidate group
//        String candidateGroup = null;
//        for (IdentityLink link : taskService.getIdentityLinksForTask(task.getId())) {
//            if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
//                candidateGroup = link.getGroupId();
//                break;
//            }
//        }
//
//        if (candidateGroup == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Task does not have a candidate group");
//            return errorResponse;
//        }
//
//        // Assign to final variable for lambda
//        final String finalCandidateGroup = candidateGroup;
//
//        // Check if user has a matching role
//        boolean hasMatchingRole = jwtAuth.getAuthorities().stream()
//                .map(authority -> authority.getAuthority())
//                .anyMatch(auth -> finalCandidateGroup.equals(auth.replace("ROLE_", "")));
//
//        if (!hasMatchingRole) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("User does not have a role matching the candidate group: " + finalCandidateGroup);
//            return errorResponse;
//        }
//
//        // Get PPM details from process variables
//        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//        Integer id = (Integer) processVariables.getOrDefault("id", null);
//        Integer version = (Integer) processVariables.getOrDefault("version", null);
//
//        if (id == null || version == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("No associated PPM ID found in process variables");
//            return errorResponse;
//        }
//
//        // Get the latest PPM entity
//        Optional<Ppm> ppmOptional = ppmService.findById(id, version);
//        if (ppmOptional.isEmpty()) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("No PPM entities found for id " + id);
//            return errorResponse;
//        }
//
//        Ppm latestPpm = ppmOptional.get();
//
//        // Handle validation dynamically
//        boolean isValid = request.getIsValid();
//        Integer newStatus = isValid ? 4 : 5; // Default: A_publie or A_rectifiee
//
//        // Special case for Reserve_CPMP task
//        if (task.getTaskDefinitionKey() != null && task.getTaskDefinitionKey().equals("Reserve_CPMP")) {
//            newStatus = isValid ? 8 : 5; // CNMP_A_VALIDER or A_rectifiee
//        }
//
//        // Update PPM status
//        Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
//        if (updatedPpm == null) {
//            TaskDTO errorResponse = new TaskDTO();
//            errorResponse.setStatus("Failed to update PPM status");
//            return errorResponse;
//        }
//
//        // Extract condition variable from gateway
//        String processVariable = finalCandidateGroup.toLowerCase() + "_valide"; // Fallback
//        try {
//            // Get BPMN model
//            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
//            if (bpmnModel != null) {
//                FlowElement taskElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
//                if (taskElement instanceof org.flowable.bpmn.model.UserTask) {
//                    // Find outgoing sequence flows
//                    List<SequenceFlow> outgoingFlows = ((org.flowable.bpmn.model.UserTask) taskElement).getOutgoingFlows();
//                    for (SequenceFlow flow : outgoingFlows) {
//                        // Check if flow leads to an exclusive gateway
//                        FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());
//                        if (targetElement instanceof ExclusiveGateway) {
//                            // Get gateway's outgoing flows
//                            List<SequenceFlow> gatewayFlows = ((ExclusiveGateway) targetElement).getOutgoingFlows();
//                            for (SequenceFlow gatewayFlow : gatewayFlows) {
//                                String conditionExpression = gatewayFlow.getConditionExpression();
//                                if (conditionExpression != null && conditionExpression.toLowerCase().contains("valide")) {
//                                    // Extract variable name (e.g., "valide" from "#{valide==false}")
//                                    String variableName = extractVariableName(conditionExpression);
//                                    if (variableName != null && variableName.toLowerCase().contains("valide")) {
//                                        processVariable = variableName;
//                                        break;
//                                    }
//                                }
//                            }
//                            if (processVariable != null) break; // Exit if we found a valid variable
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            // Log error but proceed with fallback
//            System.err.println("Failed to extract gateway condition: " + e.getMessage());
//        }
//
//        // Set process variable
//        runtimeService.setVariable(task.getProcessInstanceId(), processVariable, isValid);
//
//        // Add traceability variables
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("decision", request.getIsValid());
//        variables.put("comment", request.getComment());
//        variables.put("dateComment", LocalDateTime.now());
//        variables.put("treater", jwtAuth.getName());
//        runtimeService.setVariables(task.getProcessInstanceId(), variables);
//
//        // Complete the task
//        taskService.complete(taskId);
//
//        // Prepare response
//        TaskDTO response = new TaskDTO();
//        response.setTaskId(task.getId());
//        response.setTaskName(task.getName());
//        response.setProcessInstanceId(task.getProcessInstanceId());
//        response.setId(id);
//        response.setVersion(version);
//        response.setNewStatus(newStatus);
//        response.setIsValid(isValid);
//        response.setTraceability(variables);
//        response.setStatus("Task validated successfully");
//
//        return response;
//    }
//
//    // Helper method to extract variable name from condition expression
//    private String extractVariableName(String conditionExpression) {
//        if (conditionExpression == null) return null;
//        // Expect expression like "#{variable==value}"
//        String trimmed = conditionExpression.trim();
//        if (trimmed.startsWith("#{")) {
//            int endIndex = trimmed.indexOf("==");
//            if (endIndex == -1) endIndex = trimmed.indexOf("}");
//            if (endIndex != -1) {
//                return trimmed.substring(2, endIndex).trim();
//            }
//        }
//        return null;
//    }
//
//    public TaskListResponseDTO getUserTasks() {
//        // Get user roles from JwtAuthenticationToken
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (!(authentication instanceof JwtAuthenticationToken)) {
//            TaskListResponseDTO errorResponse = new TaskListResponseDTO();
//            errorResponse.setStatus("Invalid or missing JWT authentication");
//            return errorResponse;
//        }
//
//        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//
//        // Fetch all active tasks for the tenant ID
//        List<Task> allTasks = taskService.createTaskQuery()
//                .taskTenantId(TENANT_ID)
//                .active()
//                .orderByTaskCreateTime().asc()
//                .list();
//
//        // Filter tasks where the user has a matching role
//        List<Task> userTasks = allTasks.stream()
//                .filter(task -> {
//                    List<String> candidateGroups = taskService.getIdentityLinksForTask(task.getId()).stream()
//                            .filter(link -> "candidate".equals(link.getType()))
//                            .map(IdentityLink::getGroupId)
//                            .collect(Collectors.toList());
//                    return jwtAuth.getAuthorities().stream()
//                            .map(authority -> authority.getAuthority())
//                            .anyMatch(auth -> candidateGroups.contains(auth.replace("ROLE_", "")));
//                })
//                .collect(Collectors.toList());
//
//        // Prepare task list
//        List<TaskDTO> taskList = userTasks.stream().map(task -> {
//            TaskDTO taskInfo = new TaskDTO();
//            taskInfo.setTaskId(task.getId());
//            taskInfo.setTaskName(task.getName());
//            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
//            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
//            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
//            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
//
//            // Retrieve id and version from process instance variables
//            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//            taskInfo.setId((Integer) processVariables.getOrDefault("id", null));
//            taskInfo.setVersion((Integer) processVariables.getOrDefault("version", null));
//
//            // Set candidate group (first matching group)
//            List<String> candidateGroups = taskService.getIdentityLinksForTask(task.getId()).stream()
//                    .filter(link -> "candidate".equals(link.getType()))
//                    .map(IdentityLink::getGroupId)
//                    .collect(Collectors.toList());
//            taskInfo.setCandidateGroup(candidateGroups.isEmpty() ? "" : candidateGroups.get(0));
//
//            // Add traceability variables
//            Map<String, Object> variables = new HashMap<>();
//            variables.put("decision", processVariables.getOrDefault("decision", ""));
//            variables.put("comment", processVariables.getOrDefault("comment", ""));
//            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
//            variables.put("treater", processVariables.getOrDefault("treater", ""));
//            taskInfo.setTraceability(variables);
//
//            return taskInfo;
//        }).collect(Collectors.toList());
//
//        // Prepare response
//        TaskListResponseDTO response = new TaskListResponseDTO();
//        response.setTenantId(TENANT_ID);
//        response.setTasks(taskList);
//        response.setStatus(userTasks.isEmpty() ? "No tasks found for user's roles" : "Tasks retrieved successfully");
//
//        return response;
//    }
//}
//
//
////package com.sigmap.sigmap_ppm.ppms.services;
////
////import com.sigmap.common.ppms.Ppm;
////import com.sigmap.common.ppms.PpmService;
////import com.sigmap.common.ppms.mappers.PpmMapper;
////import com.sigmap.common.ppms.response.PpmResponse;
////import com.sigmap.common.request.ValidationRequest;
////import com.sigmap.common.response.ProcessStartResponseDTO;
////import com.sigmap.common.response.TaskDTO;
////import com.sigmap.common.response.TaskListResponseDTO;
////import com.sigmap.sigmap_ppm.ppms.dto.TaskDetailsDTO;
////import org.flowable.engine.RuntimeService;
////import org.flowable.engine.TaskService;
////import org.flowable.task.api.Task;
////import org.flowable.engine.runtime.ProcessInstance;
////import org.flowable.identitylink.api.IdentityLink;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.security.core.Authentication;
////import org.springframework.security.core.context.SecurityContextHolder;
////import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////import java.time.LocalDateTime;
////import java.time.ZoneId;
////import java.util.*;
////import java.util.stream.Collectors;
////
////@Service
////public class PpmFlowableService {
////
////    @Value("${tenant.id}")
////    private String TENANT_ID;
////
////    @Autowired
////    private RuntimeService runtimeService;
////
////    @Autowired
////    private TaskService taskService;
////
////    @Autowired
////    private PpmService ppmService;
////
////    @Transactional
////    public ProcessStartResponseDTO startProcessInstance(Integer id, Integer version) {
////        // Update PPM status
////        Ppm updated = ppmService.updateStatus(id, version, 2);
////
////        // Start the process
////        Map<String, Object> variables = new HashMap<>();
////        variables.put("id", id);
////        variables.put("version", version);
////
////        ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("ppmProcess", variables, TENANT_ID);
////
////        // Find the saisieTask
////        List<Task> tasks = taskService.createTaskQuery()
////                .processInstanceId(processInstance.getId())
////                .taskTenantId(TENANT_ID)
////                .taskDefinitionKey("saisieTask")
////                .active()
////                .list();
////
////        if (tasks.isEmpty()) {
////            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
////            errorResponse.setStatus("Saisie task not found for the process");
////            return errorResponse;
////        }
////
////        if (tasks.size() > 1) {
////            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
////            errorResponse.setStatus("Multiple saisie tasks found");
////            return errorResponse;
////        }
////
////        Task saisieTask = tasks.get(0);
////
////        // Get user roles from JwtAuthenticationToken
////        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////        if (!(authentication instanceof JwtAuthenticationToken)) {
////            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
////            errorResponse.setStatus("Invalid or missing JWT authentication");
////            return errorResponse;
////        }
////
////        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
////        // Get candidate groups for the task
////        List<String> candidateGroups = taskService.getIdentityLinksForTask(saisieTask.getId()).stream()
////                .filter(link -> "candidate".equals(link.getType()))
////                .map(IdentityLink::getGroupId)
////                .collect(Collectors.toList());
////
////        // Check if user has the required role (REDACTEUR_AC)
////        boolean hasMatchingRole = jwtAuth.getAuthorities().stream()
////                .map(authority -> authority.getAuthority())
////                .anyMatch(auth -> candidateGroups.contains(auth.replace("ROLE_", "")));
////
////        if (!hasMatchingRole) {
////            ProcessStartResponseDTO errorResponse = new ProcessStartResponseDTO();
////            errorResponse.setStatus("User does not have the required role (REDACTEUR_AC) to complete saisie task");
////            return errorResponse;
////        }
////
////        // Complete the saisieTask
////        taskService.complete(saisieTask.getId());
////
////        // Prepare response
////        ProcessStartResponseDTO response = new ProcessStartResponseDTO();
////        response.setProcessInstanceId(processInstance.getId());
////        response.setProcessDefinitionId(processInstance.getProcessDefinitionId());
////        response.setTaskId(saisieTask.getId());
////        response.setStatus("Process started and saisie task completed successfully");
////
////        return response;
////    }
////
////    public TaskListResponseDTO getCurrentTasks() {
////        // Fetch active tasks for the tenant ID
////        List<Task> tasks = taskService.createTaskQuery()
////                .taskTenantId(TENANT_ID)
////                .active()
////                .orderByTaskCreateTime().asc()
////                .list();
////
////        // Prepare task list
////        List<TaskDTO> taskList = tasks.stream().map(task -> {
////            TaskDTO taskInfo = new TaskDTO();
////            taskInfo.setTaskId(task.getId());
////            taskInfo.setTaskName(task.getName());
////            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
////            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
////            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
////            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
////
////            // Retrieve id and version from process instance variables
////            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
////            taskInfo.setId((Integer) processVariables.getOrDefault("id", null));
////            taskInfo.setVersion((Integer) processVariables.getOrDefault("version", null));
////
////            // Add traceability variables
////            Map<String, Object> variables = new HashMap<>();
////            variables.put("decision", processVariables.getOrDefault("decision", ""));
////            variables.put("comment", processVariables.getOrDefault("comment", ""));
////            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
////            variables.put("treater", processVariables.getOrDefault("treater", ""));
////            taskInfo.setTraceability(variables);
////
////            return taskInfo;
////        }).collect(Collectors.toList());
////
////        // Prepare response
////        TaskListResponseDTO response = new TaskListResponseDTO();
////        response.setTenantId(TENANT_ID);
////        response.setTasks(taskList);
////        response.setStatus("Tasks retrieved successfully");
////
////        return response;
////    }
////
////    public TaskDetailsDTO getTaskDetails(String taskId) {
////        // Get user roles from JwtAuthenticationToken
////        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////        if (!(authentication instanceof JwtAuthenticationToken)) {
////            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
////            errorResponse.setStatus("Invalid or missing JWT authentication");
////            return errorResponse;
////        }
////
////        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
////
////        // Check user roles
////        boolean hasRedacteurRole = jwtAuth.getAuthorities().stream()
////                .map(authority -> authority.getAuthority())
////                .anyMatch(auth -> auth.equals("ROLE_REDACTEUR_AC"));
////        boolean hasValidateurRole = jwtAuth.getAuthorities().stream()
////                .map(authority -> authority.getAuthority())
////                .anyMatch(auth -> auth.equals("ROLE_VALIDATEUR_AC"));
////        boolean hasCpmpRole = jwtAuth.getAuthorities().stream()
////                .map(authority -> authority.getAuthority())
////                .anyMatch(auth -> auth.equals("ROLE_CPMP"));
////        boolean hasCncmpRole = jwtAuth.getAuthorities().stream()
////                .map(authority -> authority.getAuthority())
////                .anyMatch(auth -> auth.equals("ROLE_CNCMP"));
////
////        if (!hasRedacteurRole && !hasValidateurRole && !hasCpmpRole && !hasCncmpRole) {
////            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
////            errorResponse.setStatus("User does not have the required role (REDACTEUR_AC, VALIDATEUR_AC, CPMP, or CNCMP) to view task details");
////            return errorResponse;
////        }
////
////        // Fetch the task by taskId
////        Task task = taskService.createTaskQuery()
////                .taskId(taskId)
////                .taskTenantId(TENANT_ID)
////                .active()
////                .singleResult();
////
////        if (task == null) {
////            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
////            errorResponse.setStatus("Task not found or not active");
////            return errorResponse;
////        }
////
////        // Determine the task's candidate group using TaskService
////        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
////        String candidateGroup = null;
////        for (IdentityLink link : identityLinks) {
////            if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
////                candidateGroup = link.getGroupId();
////                break;
////            }
////        }
////
////        if (candidateGroup == null) {
////            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
////            errorResponse.setStatus("Task does not have a candidate group");
////            return errorResponse;
////        }
////
////        // Verify role matches the candidate group
////        if (candidateGroup.equals("REDACTEUR_AC") && !hasRedacteurRole) {
////            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
////            errorResponse.setStatus("User does not have ROLE_REDACTEUR_AC to view this task");
////            return errorResponse;
////        }
////        if (candidateGroup.equals("VALIDATEUR_AC") && !hasValidateurRole) {
////            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
////            errorResponse.setStatus("User does not have ROLE_VALIDATEUR_AC to view this task");
////            return errorResponse;
////        }
////        if (candidateGroup.equals("CPMP") && !hasCpmpRole) {
////            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
////            errorResponse.setStatus("User does not have ROLE_CPMP to view this task");
////            return errorResponse;
////        }
////        if (candidateGroup.equals("CNCMP") && !hasCncmpRole) {
////            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
////            errorResponse.setStatus("User does not have ROLE_CNCMP to view this task");
////            return errorResponse;
////        }
////        if (!candidateGroup.equals("REDACTEUR_AC") && !candidateGroup.equals("VALIDATEUR_AC") &&
////                !candidateGroup.equals("CPMP") && !candidateGroup.equals("CNCMP")) {
////            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
////            errorResponse.setStatus("Task does not belong to REDACTEUR_AC, VALIDATEUR_AC, CPMP, or CNCMP groups");
////            return errorResponse;
////        }
////
////        // Prepare task details
////        TaskDetailsDTO response = new TaskDetailsDTO();
////        response.setTaskId(task.getId());
////        response.setTaskName(task.getName());
////        response.setTaskDefinitionKey(task.getTaskDefinitionKey());
////        response.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
////        response.setProcessInstanceId(task.getProcessInstanceId());
////        response.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
////        response.setCandidateGroup(candidateGroup);
////
////        // Retrieve id from process instance variables
////        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
////        Integer id = (Integer) processVariables.getOrDefault("id", null);
////        Integer version = (Integer) processVariables.getOrDefault("version", null);
////
////        // Fetch all Ppm entities related to this task (by id) and map to PpmResponse
////        List<PpmResponse> ppmResponses = List.of();
////        if (id != null && version != null) {
////            Optional<Ppm> relatedPpms = ppmService.findById(id, version);
////            ppmResponses = relatedPpms.stream()
////                    .map(PpmMapper.INSTANCE::map)
////                    .collect(Collectors.toList());
////        }
////        response.setPpms(ppmResponses);
////
////        // Add traceability variables
////        Map<String, Object> variables = new HashMap<>();
////        variables.put("decision", processVariables.getOrDefault("decision", ""));
////        variables.put("comment", processVariables.getOrDefault("comment", ""));
////        variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
////        variables.put("treater", processVariables.getOrDefault("treater", ""));
////        response.setTraceability(variables);
////
////        response.setStatus("Task details retrieved successfully");
////
////        return response;
////    }
////
////    @Transactional
////    public TaskDTO validateTask(String taskId, ValidationRequest request) {
////        // Get user roles from JwtAuthenticationToken
////        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////        if (!(authentication instanceof JwtAuthenticationToken)) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("Invalid or missing JWT authentication");
////            return errorResponse;
////        }
////
////        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
////
////        // Fetch the task by taskId
////        Task task = taskService.createTaskQuery()
////                .taskId(taskId)
////                .taskTenantId(TENANT_ID)
////                .active()
////                .singleResult();
////
////        if (task == null) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("Task not found or not active");
////            return errorResponse;
////        }
////
////        // Determine the task's candidate group
////        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
////        String candidateGroup = null;
////        for (IdentityLink link : identityLinks) {
////            if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
////                candidateGroup = link.getGroupId();
////                break;
////            }
////        }
////
////        if (candidateGroup == null) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("Task does not have a candidate group");
////            return errorResponse;
////        }
////
////        // Check user roles and validate against candidate group
////        boolean hasValidRole = false;
////        String requiredRole = null;
////        String userRole = candidateGroup;
////
////        if ("VALIDATEUR_AC".equals(userRole)) {
////            hasValidRole = jwtAuth.getAuthorities().stream()
////                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_VALIDATEUR_AC"));
////            requiredRole = "ROLE_VALIDATEUR_AC";
////        } else if ("REDACTEUR_AC".equals(userRole)) {
////            hasValidRole = jwtAuth.getAuthorities().stream()
////                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_REDACTEUR_AC"));
////            requiredRole = "ROLE_REDACTEUR_AC";
////        } else if ("CPMP".equals(userRole)) {
////            hasValidRole = jwtAuth.getAuthorities().stream()
////                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_CPMP"));
////            requiredRole = "ROLE_CPMP";
////        } else if ("CNCMP".equals(userRole)) {
////            hasValidRole = jwtAuth.getAuthorities().stream()
////                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_CNCMP"));
////            requiredRole = "ROLE_CNCMP";
////        } else if ("PUB_AC".equals(userRole)) {
////            hasValidRole = jwtAuth.getAuthorities().stream()
////                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_PUB_AC"));
////            requiredRole = "ROLE_PUB_AC";
////        } else {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("Invalid role");
////            return errorResponse;
////        }
////
////        if (!hasValidRole) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("User does not have the required role (" + requiredRole + ") to validate this task");
////            return errorResponse;
////        }
////
////        // Get PPM details from process variables
////        Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
////        Integer id = (Integer) processVariables.getOrDefault("id", null);
////        Integer version = (Integer) processVariables.getOrDefault("version", null);
////
////        if (id == null || version == null) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("No associated PPM ID found in process variables");
////            return errorResponse;
////        }
////
////        // Get the latest PPM entity
////        Optional<Ppm> ppmOptional = ppmService.findById(id, version);
////        if (ppmOptional.isEmpty()) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("No PPM entities found for id " + id);
////            return errorResponse;
////        }
////
////        Ppm latestPpm = ppmOptional.get();
////
////        // Handle validation based on candidate group
////        boolean isValid = request.getIsValid();
////        Integer newStatus = null;
////        String processVariable = null;
////
////        switch (candidateGroup) {
////            case "VALIDATEUR_AC":
////                newStatus = isValid ? 6 : 5; // CPMP_A_VALider or A_rectifiee
////                processVariable = "valide";
////                break;
////            case "REDACTEUR_AC":
////                newStatus = 2; // A_VALIDERR
////                processVariable = "redacteur_valide";
////                break;
////            case "CPMP":
////                // Check if this is a reserve task or validation task based on task ID
////                if ("Reserve_CPMP".equals(task.getTaskDefinitionKey())) {
////                    newStatus = isValid ? 8 : 5; // CNMP_A_VALIDER or A_rectifiee
////                    processVariable = "reserve_valide";
////                } else {
////                    newStatus = isValid ? 8 : 5; // CNMP_A_VALIDER or A_rectifiee
////                    processVariable = "cpmp_valide";
////                }
////                break;
////            case "CNCMP":
////                newStatus = isValid ? 4 : 7; // A_publie or RESERVE_CPMP
////                processVariable = "cncmp_valide";
////                break;
////            case "PUB_AC":
////                newStatus = 3; // Publiée
////                break;
////        }
////
////        // Update PPM status
////        Ppm updatedPpm = ppmService.updateStatus(id, latestPpm.getVersion(), newStatus);
////        if (updatedPpm == null) {
////            TaskDTO errorResponse = new TaskDTO();
////            errorResponse.setStatus("Failed to update PPM status");
////            return errorResponse;
////        }
////
////        // Set process variable if needed
////        if (processVariable != null) {
////            // For CPMP tasks, we need to ensure the variable is set in the correct format
////            if ("CPMP".equals(candidateGroup)) {
////                if ("Reserve_CPMP".equals(task.getTaskDefinitionKey())) {
////                    runtimeService.setVariable(task.getProcessInstanceId(), "reserve_valide", isValid);
////                } else {
////                    runtimeService.setVariable(task.getProcessInstanceId(), "cpmp_valide", isValid);
////                }
////            } else {
////                runtimeService.setVariable(task.getProcessInstanceId(), processVariable, isValid);
////            }
////        }
////
////        // Add traceability variables
////        Map<String, Object> variables = new HashMap<>();
////        variables.put("decision", request.getIsValid());
////        variables.put("comment", request.getComment());
////        variables.put("dateComment", LocalDateTime.now());
////        variables.put("treater", jwtAuth.getName());
////        runtimeService.setVariables(task.getProcessInstanceId(), variables);
////
////        // Complete the task
////        taskService.complete(taskId);
////
////        // Prepare response
////        TaskDTO response = new TaskDTO();
////        response.setTaskId(task.getId());
////        response.setTaskName(task.getName());
////        response.setProcessInstanceId(task.getProcessInstanceId());
////        response.setId(id);
////        response.setVersion(version);
////        response.setNewStatus(newStatus);
////        response.setIsValid(isValid);
////        response.setTraceability(variables);
////        response.setStatus("Task validated successfully");
////
////        return response;
////    }
////
////    public TaskListResponseDTO getUserTasks() {
////        // Get user roles from JwtAuthenticationToken
////        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////        if (!(authentication instanceof JwtAuthenticationToken)) {
////            TaskListResponseDTO errorResponse = new TaskListResponseDTO();
////            errorResponse.setStatus("Invalid or missing JWT authentication");
////            return errorResponse;
////        }
////
////        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
////
////        // Get user roles
////        List<String> userRoles = jwtAuth.getAuthorities().stream()
////                .map(authority -> authority.getAuthority())
////                .collect(Collectors.toList());
////
////        // Map roles to candidate groups
////        Map<String, String> roleToGroup = new HashMap<>();
////        roleToGroup.put("ROLE_REDACTEUR_AC", "REDACTEUR_AC");
////        roleToGroup.put("ROLE_VALIDATEUR_AC", "VALIDATEUR_AC");
////        roleToGroup.put("ROLE_PUB_AC", "PUB_AC");
////        roleToGroup.put("ROLE_CPMP", "CPMP");
////        roleToGroup.put("ROLE_CNCMP", "CNCMP");
////
////        // Find the matching role and candidate group
////        String candidateGroup = null;
////        for (String role : userRoles) {
////            if (roleToGroup.containsKey(role)) {
////                candidateGroup = roleToGroup.get(role);
////                break;
////            }
////        }
////
////        if (candidateGroup == null) {
////            TaskListResponseDTO errorResponse = new TaskListResponseDTO();
////            errorResponse.setStatus("User does not have any of the required roles (REDACTEUR_AC, VALIDATEUR_AC, PUB_AC, CPMP, CNCMP)");
////            return errorResponse;
////        }
////
////        // Make candidateGroup effectively final
////        final String finalCandidateGroup = candidateGroup;
////
////        // Fetch active tasks for tenant ID with the matching candidate group
////        List<Task> tasks = taskService.createTaskQuery()
////                .taskTenantId(TENANT_ID)
////                .taskCandidateGroup(finalCandidateGroup)
////                .active()
////                .orderByTaskCreateTime().asc()
////                .list();
////
////        // Prepare task list
////        List<TaskDTO> taskList = tasks.stream().map(task -> {
////            TaskDTO taskInfo = new TaskDTO();
////            taskInfo.setTaskId(task.getId());
////            taskInfo.setTaskName(task.getName());
////            taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
////            taskInfo.setAssignee(task.getAssignee() != null ? task.getAssignee() : "");
////            taskInfo.setProcessInstanceId(task.getProcessInstanceId());
////            taskInfo.setCreated(task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
////
////            // Retrieve id and version from process instance variables
////            Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
////            taskInfo.setId((Integer) processVariables.getOrDefault("id", null));
////            taskInfo.setVersion((Integer) processVariables.getOrDefault("version", null));
////            taskInfo.setCandidateGroup(finalCandidateGroup);
////
////            // Add traceability variables
////            Map<String, Object> variables = new HashMap<>();
////            variables.put("decision", processVariables.getOrDefault("decision", ""));
////            variables.put("comment", processVariables.getOrDefault("comment", ""));
////            variables.put("dateComment", processVariables.getOrDefault("dateComment", ""));
////            variables.put("treater", processVariables.getOrDefault("treater", ""));
////            taskInfo.setTraceability(variables);
////
////            return taskInfo;
////        }).collect(Collectors.toList());
////
////        // Prepare response
////        TaskListResponseDTO response = new TaskListResponseDTO();
////        response.setTenantId(TENANT_ID);
////        response.setTasks(taskList);
////        response.setCandidateGroup(candidateGroup);
////        response.setStatus("Tasks retrieved successfully");
////
////        return response;
////    }
////}