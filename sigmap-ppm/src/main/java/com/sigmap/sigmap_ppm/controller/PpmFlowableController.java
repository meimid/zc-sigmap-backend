package com.sigmap.sigmap_ppm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
import com.sigmap.sigmap_ppm.services.dto.request.ValidationRequest;
import com.sigmap.sigmap_ppm.services.dto.response.ProcessStartResponseDTO;
import com.sigmap.sigmap_ppm.services.dto.response.TaskDTO;
import com.sigmap.sigmap_ppm.services.dto.response.TaskDetailsDTO;
import com.sigmap.sigmap_ppm.services.dto.response.TaskListResponseDTO;
import com.sigmap.sigmap_ppm.services.PpmFlowableService;
import jakarta.transaction.Transactional;
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
 * REST Controller for managing Flowable process instances and tasks related to Ppm entities.
 */
@RestController
@RequestMapping("/api/ppms/flowable")
public class PpmFlowableController {

    @Autowired
    private PpmFlowableService ppmFlowableService;

    private final Logger log = LoggerFactory.getLogger(PpmFlowableController.class);

    /**
     * Starts a Flowable process instance for a Ppm entity using its ID.
     * Automatically retrieves the latest version of the Ppm entity.
     *
     * @param id The Ppm ID
     * @return ResponseEntity with ProcessStartResponseDTO
     * @throws PpmNotFound if the Ppm entity is not found
     */
    @PostMapping("/start/{id}")
    public ResponseEntity<ProcessStartResponseDTO> startProcessInstance(@PathVariable Integer id) throws PpmNotFound, AcNotFound {
        log.info("Starting process instance for Ppm ID: {}", id);
        ProcessStartResponseDTO response = ppmFlowableService.startProcessInstance(id);
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
        TaskListResponseDTO response = ppmFlowableService.getCurrentTasks();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details of a specific task.
     *
     * @param taskId The ID of the task
     * @return ResponseEntity with TaskDetailsDTO
     * @throws PpmNotFound if the associated Ppm entity is not found
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDTO> getTaskDetails(@PathVariable String taskId) throws PpmNotFound, AcNotFound {
        log.info("Retrieving details for task ID: {}", taskId);
        TaskDetailsDTO response = ppmFlowableService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a task and updates the associated Ppm entity.
     *
     * @param taskId  The ID of the task to validate
     * @param request The validation request
     * @param realisationsLotsJson JSON string containing lots for realisations
     * @return ResponseEntity with TaskDTO
     * @throws PpmNotFound if the Ppm entity is not found
     * @throws DocNotFound if a document is not found
     * @throws AcNotFound if the associated Ac entity is not found
     * @throws IOException if an error occurs during file processing
     */
    @PostMapping(consumes = {"multipart/form-data"}, value = "/tasks/{taskId}/validate")
    public ResponseEntity<TaskDTO> validateTask(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson) throws PpmNotFound, DocNotFound, AcNotFound, IOException {
        log.info("Validating task ID: {} with request: {}", taskId, request);

        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsFromJson(request.getPpm(), realisationsLotsJson);
                log.info("Successfully processed lots from JSON string");
            } catch (Exception e) {
                log.error("Error processing lots JSON: {}", e.getMessage());
            }
        }
        TaskDTO response = ppmFlowableService.validateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    private void processRealisationLotsFromJson(PpmWithLotsRequestDto request, String lotsJson) {
        log.info("Processing lots JSON: {}", lotsJson);
        log.info("Number of realisations in request: {}", (request.getRealisations() != null ? request.getRealisations().size() : 0));

        if (request.getRealisations() == null || request.getRealisations().isEmpty()) {
            log.info("No realisations found in request - skipping lot processing");
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<Map<String, Object>>> lotsMap = objectMapper.readValue(lotsJson,
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {});

            for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
                int realisationIndex = Integer.parseInt(entry.getKey());
                log.info("Processing realisation at index: {}", realisationIndex);

                if (realisationIndex >= request.getRealisations().size()) {
                    log.info("Realisation index {} is out of bounds - skipping", realisationIndex);
                    continue;
                }

                RealisationBaseRequestDto realisation = request.getRealisations().get(realisationIndex);
                log.info("Found realisation: {}", realisation);

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
                    log.info("Added lot: {}", lot);
                }

                realisation.setLots(lots);
                log.info("Set lots on realisation at index {}: {}", realisationIndex, lots);
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
        TaskListResponseDTO response = ppmFlowableService.getUserTasks();
        return ResponseEntity.ok(response);
    }
}