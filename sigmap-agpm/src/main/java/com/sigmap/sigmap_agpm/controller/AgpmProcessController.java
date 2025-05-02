//package com.sigmap.sigmap_agpm.controller;
//
//import org.flowable.engine.HistoryService;
//import org.flowable.engine.RepositoryService;
//import org.flowable.engine.RuntimeService;
//import org.flowable.engine.TaskService;
//import org.flowable.engine.runtime.ProcessInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/process")
//public class AgpmProcessController {
//
//    @Value("${tenant.id}")
//    private String TENANT_ID;
//
//
//    @Autowired
//    private RepositoryService repositoryService;
//
//    @Autowired
//    private RuntimeService runtimeService;
//
//    @Autowired
//    private TaskService taskService;
//
//    @Autowired
//    private HistoryService historyService;
//
//    @PostMapping("/start/")
//    public ResponseEntity<Map<String, String>> startProcessInstance() {
//
//        try {
//
////            ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("congee_diagram",variables,TENANT_ID);
//
////            Map<String, Object> variables = new HashMap<>();
////            variables.put("requester_user_id", demandeConge.getEmploye());
////            variables.put("startDate", demandeConge.getDateDebut());
////            variables.put("endDate", demandeConge.getDateFin()) ;
////            variables.put("congeeId", demandeConge.getId());
//
//            Map<String, String> response = new HashMap<>();
//            response.put("processInstanceId", processInstance.getId());
//            response.put("processDefinitionId", processInstance.getProcessDefinitionId());
//            response.put("status", "Process started successfully");
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Failed to start process: " + e.getMessage()));
//        }
//    }
//}
