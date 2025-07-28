package com.sigmap.sigmap_ppm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.request.PpmBaseRequestDto;
import com.sigmap.common.ppms.request.PpmWithLotsRequestDto;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.process_verbal.ProcessVerbalService;
import com.sigmap.common.process_verbal.mappers.ProcessVerbalMapper;
import com.sigmap.common.process_verbal.request.ProcessVerbalRequest;
import com.sigmap.common.process_verbal.request.PvRequest;
import com.sigmap.common.process_verbal.response.ProcessVerbalResponse;

import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
import com.sigmap.common.realisations.request.RealisationRequestForPaa;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.sigmap_ppm.services.dto.request.ValidationRequest;
import com.sigmap.sigmap_ppm.services.dto.response.*;
import com.sigmap.sigmap_ppm.services.PpmFlowableService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for managing Flowable process instances and tasks related to Ppm entities.
 */
@RestController
@RequestMapping("/api/ppms/flowable")
@Slf4j
public class PpmFlowableController {


    @Autowired
    private PpmFlowableService ppmFlowableService;
    private final ProcessVerbalService processVerbalService;




    public PpmFlowableController(ProcessVerbalService processVerbalService) {
        this.processVerbalService = processVerbalService;
    }

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
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson) throws PpmNotFound, DocNotFound, AcNotFound, IOException, ComNotFound {
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

    private void processRealisationLotsFromJson(PpmBaseRequestDto request, String lotsJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            log.info("Processing lots JSON: {}", lotsJson);

            // Validate input
            if (lotsJson == null || lotsJson.trim().isEmpty()) {
                log.warn("Lots JSON is empty or null, skipping processing");
                return;
            }

            // Parse JSON into a map of realisation indices to lists of lot data
            Map<String, List<Map<String, Object>>> lotsMap = objectMapper.readValue(lotsJson,
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {});
            log.info("Parsed {} realisation indices from lots JSON", lotsMap.size());

            // Ensure realisations list exists
            if (request.getRealisations() == null) {
                request.setRealisations(new ArrayList<>());
                log.warn("Realisations list was null, initialized as empty");
            }

            // Process each realisation's lots
            for (Map.Entry<String, List<Map<String, Object>>> entry : lotsMap.entrySet()) {
                int realisationIndex = Integer.parseInt(entry.getKey());

                // Skip if index is out of bounds
                if (realisationIndex >= request.getRealisations().size()) {
                    log.warn("Skipping lots for realisation index {} as it's out of bounds (size: {})",
                            realisationIndex, request.getRealisations().size());
                    continue;
                }

                // Get the target realisation
                RealisationBaseRequestDto realisation = request.getRealisations().get(realisationIndex);
                if (realisation == null) {
                    log.warn("Realisation at index {} is null, skipping", realisationIndex);
                    continue;
                }

                List<LotResponse> lots = new ArrayList<>();

                // Process lots for this realisation
                for (Map<String, Object> lotData : entry.getValue()) {
                    LotResponse lot = new LotResponse();

                    if (lotData.containsKey("id")) {
                        lot.setId(((Number) lotData.get("id")).intValue());
                    } else {
                        log.debug("Lot data missing id, setting to null");
                    }

                    if (lotData.containsKey("version")) {
                        lot.setVersion(((Number) lotData.get("version")).intValue());
                    } else {
                        log.debug("Lot data missing version, setting to null");
                    }

                    if (lotData.containsKey("description")) {
                        lot.setDescription((String) lotData.get("description"));
                    } else {
                        log.warn("Lot data missing description, skipping lot");
                        continue;
                    }

                    // MODIFIED: Handle budget field
                    if (lotData.containsKey("budget")) {
                        Object budgetObj = lotData.get("budget");
                        if (budgetObj instanceof Number) {
                            lot.setBudget(new java.math.BigDecimal(budgetObj.toString()));
                            log.debug("Set budget to {} for lot with description: {}", lot.getBudget(), lot.getDescription());
                        } else {
                            log.warn("Invalid budget format for lot with description: {}, setting to null", lot.getDescription());
                        }
                    } else {
                        log.debug("Lot data missing budget, setting to null");
                    }

                    lots.add(lot);
                }

                // Set the processed lots on the realisation
                realisation.setLots(lots);
                log.info("Added {} lots to realisation at index {}", lots.size(), realisationIndex);
                log.info(lots.toString());
            }
        } catch (Exception e) {
            log.error("Failed to parse lots JSON: {}", e.getMessage(), e);
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

    @PostMapping(
            consumes = {"multipart/form-data"},
            value = "/pv")
    public ResponseEntity<ProcessVerbalResponse> createPv(
            @ModelAttribute("request")
           // @RequestBody
            ProcessVerbalRequest request
    ) throws PpmNotFound, AcNotFound, IOException, ComNotFound, RefParamNotFound {
        ProcessVerbal processVerbal= processVerbalService.create(request);
        ProcessVerbalResponse  response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
        return ResponseEntity.ok(response);
    }
@GetMapping("/pv/find-by-ids/{ppmId}/{ppmVer}")
public ResponseEntity<ProcessVerbalResponse> findByIds(@PathVariable Integer ppmId,@PathVariable Integer ppmVer) throws PpmNotFound {
    ProcessVerbal processVerbal=processVerbalService.findByIds(ppmId,ppmVer);
    ProcessVerbalResponse  response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
    return ResponseEntity.ok(response);

}
    @PutMapping(
            consumes = {"multipart/form-data"},
            value = "/pv")
    public ResponseEntity<ProcessVerbalResponse> updatePv(
            @ModelAttribute("request")
            // @RequestBody
            ProcessVerbalRequest request
    ) throws IOException, RefParamNotFound, DocNotFound{
        ProcessVerbal processVerbal= processVerbalService.update(request);
        ProcessVerbalResponse  response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pv/find_by-ids_task_process/{ppmId}/{ppmVer}/{taskId}/{processInstanceId}")
    public ResponseEntity<ProcessVerbalResponse> findByIdsAndTaskIdAndProcessInstanceId(@PathVariable Integer ppmId,
                                                           @PathVariable Integer ppmVer,
                                                           @PathVariable String taskId,
                                                           @PathVariable String processInstanceId)   {
        ProcessVerbal processVerbal=processVerbalService.findByIdsAndTaskIdAndProcessInstanceId(ppmId,ppmVer,taskId,processInstanceId);
        ProcessVerbalResponse  response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
        return ResponseEntity.ok(response);
    }

    @PutMapping(
            consumes = {"multipart/form-data"},
            value = "/pv/add_pv")
    public ResponseEntity<ProcessVerbalResponse> addNewPv(
            @ModelAttribute("request")
            // @RequestBody
            PvRequest request
    ) throws  IOException, RefParamNotFound, DocNotFound {
        ProcessVerbal processVerbal= processVerbalService.addNewPv(request);
        ProcessVerbalResponse  response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/task-history/{procInstId}")
    public List<HistoricTaskDto> loadHistoric(@PathVariable("procInstId") String procInstId) {
        return ppmFlowableService.loadHistoricTasks(procInstId);
    }
}