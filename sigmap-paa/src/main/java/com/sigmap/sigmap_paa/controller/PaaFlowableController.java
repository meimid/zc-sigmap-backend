package com.sigmap.sigmap_paa.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.request.PaaRequestGlobal;
import com.sigmap.common.realisations.request.RealisationRequestForPaa;
import com.sigmap.sigmap_paa.service.dto.request.ValidationRequest;
import com.sigmap.sigmap_paa.service.dto.response.*;
import com.sigmap.sigmap_paa.service.PaaFlowableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for managing Flowable process instances and tasks related to Paa entities.
 */
@RestController
@RequestMapping("/api/paas/flowable")
public class PaaFlowableController {

    @Autowired
    private PaaFlowableService paaFlowableService;

    private final Logger log = LoggerFactory.getLogger(PaaFlowableController.class);

    /**
     * Starts a Flowable process instance for a Paa entity using its ID.
     * Automatically retrieves the latest version of the Paa entity.
     *
     * @param id The Paa ID
     * @return ResponseEntity with ProcessStartResponseDTO
     * @throws PaaNotFound if the Paa entity is not found
     */
    @PostMapping("/start/{id}")
    public ResponseEntity<ProcessStartResponseDTO> startProcessInstance(@PathVariable Integer id) throws PaaNotFound, AcNotFound {
        log.info("Starting process instance for Paa ID: {}", id);
        ProcessStartResponseDTO response = paaFlowableService.startProcessInstance(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all active tasks for the tenant.
     *
     * @return ResponseEntity with TaskListResponseDTO
     */
    @GetMapping("/tasks")
    public ResponseEntity<TaskListResponseDTO> getCurrentTasks() {
        log.info("Retrieving all current tasks");
        TaskListResponseDTO response = paaFlowableService.getCurrentTasks();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details of a specific task.
     *
     * @param taskId The ID of the task
     * @return ResponseEntity with TaskDetailsDTO
     * @throws PaaNotFound if the associated Paa entity is not found
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDTO> getTaskDetails(@PathVariable String taskId) throws PaaNotFound, AcNotFound {
        log.info("Retrieving details for task ID: {}", taskId);
        TaskDetailsDTO response = paaFlowableService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a task and updates the associated Paa entity.
     *
     * @param taskId  The ID of the task to validate
     * @param request The validation request
     * @return ResponseEntity with TaskDTO
     * @throws PaaNotFound if the Paa entity is not found
     * @throws DocNotFound if a document is not found
     * @throws AcNotFound if the associated Ac entity is not found
     * @throws IOException if an error occurs during file processing
     */
    @PostMapping(consumes = {"multipart/form-data"}, value = "/tasks/{taskId}/validate")
    public ResponseEntity<TaskDTO> validateTask(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request,
    @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson) throws PaaNotFound, DocNotFound, AcNotFound, IOException {
        log.info("Validating task ID: {} with request: {}", taskId, request);
        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsFromJson(request.getPaa(), realisationsLotsJson);
                log.info("Successfully processed lots from JSON string");
            } catch (Exception e) {
                log.error("Error processing lots JSON: {}", e.getMessage());
            }
        }
        TaskDTO response = paaFlowableService.validateTask(taskId, request);
        return ResponseEntity.ok(response);
    }
//    private void processRealisationLotsFromJson(PaaRequestGlobal request, String lotsJson) {
//        System.out.println("the lots from here is");
//
//        System.out.println(lotsJson);
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            // The JSON should be in format: { "0": [{"id": 1, "description": "lot 1"}, ...], "1": [...] }
//            // Where the keys are realisation indices
//            Map<String, List<Map<String, Object>>> lotsMap = objectMapper.readValue(lotsJson,
//                    new TypeReference<Map<String, List<Map<String, Object>>>>() {});
//
//            // Process each realisation's lots
//            for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
//                int realisationIndex = Integer.parseInt(entry.getKey());
//
//                // Skip if index is out of bounds
//                if (realisationIndex >= request.getRealisations().size()) {
//                    continue;
//                }
//
//                // Get the target realisation
//                RealisationRequestForPaa realisation = request.getRealisations().get(realisationIndex);
//                Set<LotResponse> lots = new HashSet<>();
//
//                // Process lots for this realisation
//                for (Map<String, Object> lotData : entry.getValue()) {
//                    LotResponse lot = new LotResponse();
//
//                    if (lotData.containsKey("id")) {
//                        lot.setId(((Number) lotData.get("id")).intValue());
//                    }
//
//                    if (lotData.containsKey("version")) {
//                        lot.setVersion(((Number) lotData.get("version")).intValue());
//                    }
//
//                    if (lotData.containsKey("description")) {
//                        lot.setDescription((String) lotData.get("description"));
//                    }
//
//                    lots.add(lot);
//                }
//
//                // Set the processed lots on the realisation
//                realisation.setLots(lots);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to parse lots JSON: " + e.getMessage(), e);
//        }
//    }
private void processRealisationLotsFromJson(PaaRequestGlobal request, String lotsJson) {
    System.out.println("Processing lots JSON: " + lotsJson);
    System.out.println("Number of realisations in request: " + (request.getRealisations() != null ? request.getRealisations().size() : 0));

    if (request.getRealisations() == null || request.getRealisations().isEmpty()) {
        System.out.println("No realisations found in request - skipping lot processing");
        return;
    }

    try {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<Map<String, Object>>> lotsMap = objectMapper.readValue(lotsJson,
                new TypeReference<Map<String, List<Map<String, Object>>>>() {});

        for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
            int realisationIndex = Integer.parseInt(entry.getKey());
            System.out.println("Processing realisation at index: " + realisationIndex);

            if (realisationIndex >= request.getRealisations().size()) {
                System.out.println("Realisation index " + realisationIndex + " is out of bounds - skipping");
                continue;
            }

            RealisationRequestForPaa realisation = request.getRealisations().get(realisationIndex);
            System.out.println("Found realisation: " + realisation);

            Set<LotResponse> lots = new HashSet<>();
            for (Map<String, Object> lotData : entry.getValue()) {
                LotResponse lot = new LotResponse();
                if (lotData.containsKey("id")) {
                    lot.setId(((Number) lotData.get("id")).intValue());
                }
                if (lotData.containsKey("version")) {
                    lot.setVersion(((Number) lotData.get("version")).intValue());
                }
                if (lotData.containsKey("description")) {
                    lot.setDescription((String) lotData.get("description"));
                }
                lots.add(lot);
                System.out.println("Added lot: " + lot);
            }

            realisation.setLots(lots);
            System.out.println("Set lots on realisation at index " + realisationIndex + ": " + lots);
        }
    } catch (Exception e) {
        throw new RuntimeException("Failed to parse lots JSON: " + e.getMessage(), e);
    }
}

    /**
     * Retrieves tasks assigned to the current user based on their candidate groups.
     *
     * @return ResponseEntity with TaskListResponseDTO
     */
    @GetMapping("/tasks/user")
    public ResponseEntity<TaskListResponseDTO> getUserTasks() {
        log.info("Retrieving tasks for current user");
        TaskListResponseDTO response = paaFlowableService.getUserTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/task-history/{procInstId}")
    public List<HistoricTaskDto> loadHistoric(@PathVariable("procInstId") String procInstId) {
        return paaFlowableService.loadHistoricTasks(procInstId);
    }
}