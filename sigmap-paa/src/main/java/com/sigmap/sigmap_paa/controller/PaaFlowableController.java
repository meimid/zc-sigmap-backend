package com.sigmap.sigmap_paa.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.model_paa.ModelPaaService;
import com.sigmap.common.model_paa.exceptions.PaaModelShouldExiste;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.request.PaaRequestGlobal;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.request.RealisationRequestForPaa;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.sigmap_paa.service.dto.request.ModelePaaRequest;
import com.sigmap.sigmap_paa.service.dto.request.ValidationRequest;
import com.sigmap.sigmap_paa.service.dto.response.*;
import com.sigmap.sigmap_paa.service.PaaFlowableService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

/**
 * REST Controller for managing Flowable process instances and tasks related to Paa entities.
 */
@RestController
@RequestMapping("/api/paas/flowable")
public class PaaFlowableController {
    private final ObjectMapper objectMapper;
    @Autowired
    private PaaFlowableService paaFlowableService;
    private final DocumentCommuneService documentCommuneService;
    private final ModelPaaService modelPaaService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;

    public PaaFlowableController(ObjectMapper objectMapper, PaaFlowableService paaFlowableService,
                                 DocumentCommuneService documentCommuneService,
                                 ModelPaaService modelPaaService,
                                 TaskService taskService,
                                 RuntimeService runtimeService) {
        this.objectMapper = objectMapper;
        this.paaFlowableService = paaFlowableService;
        this.documentCommuneService = documentCommuneService;
        this.modelPaaService = modelPaaService;
        this.taskService = taskService;
        this.runtimeService = runtimeService;

    }

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
    @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
        @RequestParam(value = "realisationsFinancementsJson", required = false) String realisationsFinancementsJson)throws PaaNotFound, DocNotFound, AcNotFound, IOException, RealisationNotFoundException, RealisationAlreadyExistsException {
        log.info("Validating task ID: {} with request: {}", taskId, request);
        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsFromJson(request.getPaa(), realisationsLotsJson);
                log.info("Successfully processed lots from JSON string");
            } catch (Exception e) {
                log.error("Error processing lots JSON: {}", e.getMessage());
            }
        }

        // ADDED THIS: Process financements from JSON string if provided
        if (realisationsFinancementsJson != null && !realisationsFinancementsJson.isEmpty()) {
            try {
                processRealisationFinancementsFromJson(request.getPaa(), realisationsFinancementsJson);
                log.info("Successfully processed financements from JSON string");
            } catch (Exception e) {
                log.error("Error processing financements JSON: {}", e.getMessage());
            }
        }
        TaskDTO response = paaFlowableService.validateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    private void processRealisationFinancementsFromJson(PaaRequestGlobal request, String realisationsFinancementsJson) {
        if (realisationsFinancementsJson == null || realisationsFinancementsJson.isEmpty()) {
            return;
        }

        try {
            log.info("Processing financements JSON: {}", realisationsFinancementsJson);
            Map<String, List<Map<String, Object>>> financementMap = objectMapper.readValue(
                    realisationsFinancementsJson,
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {}
            );

            log.info("Parsed {} realisation indices from financements JSON", financementMap.size());

            List<RealisationRequestForPaa> realisations = request.getRealisations();
            if (realisations == null) {
                realisations = new ArrayList<>();
                request.setRealisations(realisations);
            }

            for (Map.Entry<String, List<Map<String, Object>>> entry : financementMap.entrySet()) {
                int index;
                try {
                    index = Integer.parseInt(entry.getKey());
                } catch (NumberFormatException e) {
                    log.warn("Invalid realisation index: {}. Skipping.", entry.getKey());
                    continue;
                }

                if (index >= realisations.size()) {
                    log.warn("Skipping financements for realisation index {} as it's out of bounds (size: {})", index, realisations.size());
                    continue;
                }

                List<FinancementRequest> financementRequests = new ArrayList<>();
                for (Map<String, Object> financementData : entry.getValue()) {
                    FinancementRequest financementRequest = new FinancementRequest();
                    // Explicitly map fields
                    if (financementData.containsKey("id")) {
                        Object id = financementData.get("id");
                        if (id instanceof Number) {
                            financementRequest.setFinancementId(((Number) id).intValue());
                        }
                    }
                    if (financementData.containsKey("monnaieId")) {
                        Object monnaieId = financementData.get("monnaieId");
                        if (monnaieId instanceof Number) {
                            financementRequest.setMonnaieId(((Number) monnaieId).intValue());
                        }
                    }
                    if (financementData.containsKey("budgetEstimer")) {
                        Object budgetEstimer = financementData.get("budgetEstimer");
                        if (budgetEstimer instanceof Number) {
                            financementRequest.setBudgetEstimer(BigDecimal.valueOf(((Number) budgetEstimer).doubleValue()));
                        }
                    }
                    if (financementData.containsKey("sourceFinancement")) {
                        Object sourceFinancement = financementData.get("sourceFinancement");
                        if (sourceFinancement instanceof Map) {
                            Map<String, Object> sourceFinancementMap = (Map<String, Object>) sourceFinancement;
                            if (sourceFinancementMap.containsKey("id")) {
                                Object sourceId = sourceFinancementMap.get("id");
                                if (sourceId instanceof Number) {
                                    RefParam refParam = new RefParam();
                                    refParam.setId(((Number) sourceId).intValue());
                                    financementRequest.setSourceFinancement(refParam);
                                }
                            }
                        }
                    }
                    financementRequests.add(financementRequest);
                }

                log.info("Added {} financements to realisation at index {}", financementRequests.size(), index);
                log.info("{}", financementRequests);
                realisations.get(index).setFinancements(financementRequests);
            }

            log.info("Successfully processed financements from JSON string");
        } catch (IOException e) {
            log.error("Error parsing financements JSON: {}", realisationsFinancementsJson, e);
            throw new IllegalArgumentException("Failed to parse financements JSON: " + e.getMessage());
        }
    }


    private void processRealisationLotsFromJson(PaaRequestGlobal request, String lotsJson) {
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
            RealisationRequestForPaa realisation = request.getRealisations().get(realisationIndex);
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
        TaskListResponseDTO response = paaFlowableService.getUserTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/task-history/{procInstId}")
    public List<HistoricTaskDto> loadHistoric(@PathVariable("procInstId") String procInstId) {
        return paaFlowableService.loadHistoricTasks(procInstId);
    }
    @PostMapping("/modele-paa")
    public ResponseEntity<byte[]> generateModelePpm(@RequestBody ModelePaaRequest modele )  throws Exception {

        byte[] pdfBytes = paaFlowableService.generateModelePaa(modele.getPaaId(),modele.getPaaVer(),modele.getFonction()+" - "+ modele.getSignataire());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "Modele_PAA" + modele.getPaaId() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }


    @PreAuthorize("hasRole('VALIDATEUR_AC')")
    @Transactional(rollbackFor = {
            Exception.class
    })
    @PostMapping(consumes = {"multipart/form-data"}, value = "/tasks/{taskId}/validate_with_model")
    public ResponseEntity<TaskDTO> validateTaskWithModel(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request,
            @RequestPart(value = "modelPaaFile", required = false) MultipartFile modelPaaFile,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
            @RequestParam(value = "realisationsFinancementsJson", required = false) String realisationsFinancementsJson
    ) throws IOException, PaaNotFound, AcNotFound, ComNotFound, DocNotFound, PpmNotFound, RealisationNotFoundException, RealisationAlreadyExistsException {

        if (modelPaaFile == null || modelPaaFile.isEmpty()) {
            throw new PaaModelShouldExiste("Le fichier Model PAA est requis.");
        }

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        Map<String, Object> vars = runtimeService.getVariables(task.getProcessInstanceId());
        Integer paaId = (Integer) vars.get("id");
        Integer paaVer = (Integer) vars.get("version");
        if (paaId == null || paaVer == null) {
            throw new IllegalStateException("Process variables 'id' and 'version' are required on this task.");
        }

        String rootPath = Constants.PATH_ENTREPOT_GED_PAA;
        Integer docId = documentCommuneService.processAndSaveDocumentsForModelPaa(
                modelPaaFile, paaId,paaVer, rootPath
        );

        modelPaaService.setModel(paaId, paaVer, docId);

        if (realisationsLotsJson != null && !realisationsLotsJson.isBlank()) {
            try {
                processRealisationLotsFromJson(request.getPaa(), realisationsLotsJson);
            } catch (Exception e) {
                log.warn("Failed to parse lots JSON before validate: {}", e.getMessage());
            }
        }
        if (realisationsFinancementsJson != null && !realisationsFinancementsJson.isBlank()) {
            try {
                processRealisationFinancementsFromJson(request.getPaa(), realisationsFinancementsJson);
            } catch (Exception e) {
                log.warn("Failed to parse financements JSON before validate: {}", e.getMessage());
            }
        }


        TaskDTO response = paaFlowableService.validateTask(taskId, request);

        return ResponseEntity.ok(response);
    }

}