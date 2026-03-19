package com.sigmap.sigmap_ppm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.lots.response.LotResponse;
import com.sigmap.common.model_ppm.ModelPpmService;
import com.sigmap.common.model_ppm.exceptions.PpmModelShouldExiste;
import com.sigmap.common.paas.request.PaaRequestGlobal;
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
import com.sigmap.common.realisations.request.RealisationWithLotsRequestDto;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.reunion.ReunionService;
import com.sigmap.common.reunion.ReunionServiceImpl;
import com.sigmap.common.reunion.exception.ParticipantPresenceConflict;
import com.sigmap.sigmap_ppm.exeptions.GlobalExceptionHandler;
import com.sigmap.sigmap_ppm.services.dto.request.GroupValidationRequest;
import com.sigmap.sigmap_ppm.services.dto.request.ModeleRequest;
import com.sigmap.sigmap_ppm.services.dto.request.ValidationRequest;
import com.sigmap.sigmap_ppm.services.dto.response.*;
import com.sigmap.sigmap_ppm.services.PpmFlowableService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

/**
 * REST Controller for managing Flowable process instances and tasks related to
 * Ppm entities.
 */
@RestController
@RequestMapping("/api/ppms/flowable")
@Slf4j
public class PpmFlowableController {

    @Autowired
    private PpmFlowableService ppmFlowableService;
    private final ProcessVerbalService processVerbalService;
    private final ObjectMapper objectMapper;
    private final DocumentCommuneService documentCommuneService;
    private final ModelPpmService modelPpmService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final ReunionService reunionService;

    public PpmFlowableController(ProcessVerbalService processVerbalService, ObjectMapper objectMapper,
            DocumentCommuneService documentCommuneService,
            ModelPpmService modelPpmService,
            TaskService taskService,
            RuntimeService runtimeService, ReunionService reunionService) {
        this.processVerbalService = processVerbalService;
        this.objectMapper = objectMapper;
        this.documentCommuneService = documentCommuneService;
        this.modelPpmService = modelPpmService;
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.reunionService = reunionService;
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
    public ResponseEntity<ProcessStartResponseDTO> startProcessInstance(@PathVariable Integer id)
            throws PpmNotFound, AcNotFound {
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
     * @param taskId               The ID of the task to validate
     * @param request              The validation request
     * @param realisationsLotsJson JSON string containing lots for realisations
     * @return ResponseEntity with TaskDTO
     * @throws PpmNotFound if the Ppm entity is not found
     * @throws DocNotFound if a document is not found
     * @throws AcNotFound  if the associated Ac entity is not found
     * @throws IOException if an error occurs during file processing
     */
    @PostMapping(consumes = { "multipart/form-data" }, value = "/tasks/{taskId}/validate")
    public ResponseEntity<TaskDTO> validateTask(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
            @RequestParam(value = "realisationsFinancementsJson", required = false) String realisationsFinancementsJson)
            throws PpmNotFound, DocNotFound, AcNotFound, IOException, ComNotFound {
        log.info("Validating task ID: {} with request: {}", taskId, request);

        if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
            try {
                processRealisationLotsFromJson(request.getPpm(), realisationsLotsJson);
                log.info("Successfully processed lots from JSON string");
            } catch (Exception e) {
                log.error("Error processing lots JSON: {}", e.getMessage());
            }
        }
        // ADDED THIS: Process financements from JSON string if provided
        if (realisationsFinancementsJson != null && !realisationsFinancementsJson.isEmpty()) {
            try {
                processRealisationFinancementsFromJson(request.getPpm(), realisationsFinancementsJson);
                log.info("Successfully processed financements from JSON string");
            } catch (Exception e) {
                log.error("Error processing financements JSON: {}", e.getMessage());
            }
        }
        request.setFromMeeting(false);
        TaskDTO response = ppmFlowableService.validateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    private void processRealisationFinancementsFromJson(PpmWithLotsRequestDto request,
            String realisationsFinancementsJson) {
        if (realisationsFinancementsJson == null || realisationsFinancementsJson.isEmpty()) {
            return;
        }

        try {
            log.info("Processing financements JSON: {}", realisationsFinancementsJson);
            Map<String, List<Map<String, Object>>> financementMap = objectMapper.readValue(
                    realisationsFinancementsJson,
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {
                    });

            log.info("Parsed {} realisation indices from financements JSON", financementMap.size());

            List<RealisationBaseRequestDto> realisations = request.getRealisations();
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
                    log.warn("Skipping financements for realisation index {} as it's out of bounds (size: {})", index,
                            realisations.size());
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
                            financementRequest
                                    .setBudgetEstimer(BigDecimal.valueOf(((Number) budgetEstimer).doubleValue()));
                        }
                    }
                    if (financementData.containsKey("examenPrealable")) {
                        Object examenPrealable = financementData.get("examenPrealable");
                        if (examenPrealable instanceof Boolean) {
                            financementRequest.setExamenPrealable((Boolean) examenPrealable);
                        } else if (examenPrealable != null) {
                            financementRequest.setExamenPrealable(Boolean.valueOf(examenPrealable.toString()));
                        }
                    }
                    if (financementData.containsKey("sourceFinancement")) {
                        Object sourceFinancement = financementData.get("sourceFinancement");
                        if (sourceFinancement instanceof Map) {
                            Map<String, Object> sourceFinancementMap = (Map<String, Object>) sourceFinancement;
                            if (sourceFinancementMap.containsKey("id")) {
                                Object sourceId = sourceFinancementMap.get("id");
                                if (sourceId instanceof Number) {
                                    com.sigmap.common.source_financement.SourceFinancement sf = new com.sigmap.common.source_financement.SourceFinancement();
                                    sf.setId(((Number) sourceId).intValue());
                                    financementRequest.setSourceFinancement(sf);
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

    /**
     * Validates a group of tasks and updates the associated Ppm entities.
     *
     * @param requestsJson         The list of validation requests containing task
     *                             IDs
     * @param realisationsLotsJson JSON string containing lots for realisations
     * @return ResponseEntity with List<TaskDTO>
     * @throws PpmNotFound if a Ppm entity is not found
     * @throws DocNotFound if a document is not found
     * @throws AcNotFound  if an associated Ac entity is not found
     * @throws IOException if an error occurs during file processing
     * @throws ComNotFound if a commission is not found
     */
    @PostMapping(consumes = { "multipart/form-data" }, value = "/tasks/validate-group/{idReu}")
    public ResponseEntity<List<TaskDTO>> validateTaskGroup(
            @RequestParam("requestsJson") String requestsJson,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
            @PathVariable Integer idReu)
            throws PpmNotFound, DocNotFound, AcNotFound, IOException, ComNotFound {
        Boolean present = reunionService.checkParticipantPresenceWithReunionId(idReu);
        if (present) {
            log.info("Validating group of tasks with requests JSON");

            // Deserialize requestsJson into List<GroupValidationRequest>
            List<GroupValidationRequest> requests;
            try {
                requests = objectMapper.readValue(requestsJson, new TypeReference<List<GroupValidationRequest>>() {
                });
                log.info("Parsed {} requests from JSON", requests.size());
            } catch (IOException e) {
                log.error("Failed to parse requests JSON: {}", e.getMessage());
                throw new IOException("Invalid requests JSON format", e);
            }

            for (GroupValidationRequest request : requests) {
                if (realisationsLotsJson != null && !realisationsLotsJson.isEmpty()) {
                    try {
                        processRealisationLotsFromJson(request.getPpm(), realisationsLotsJson);
                        log.info("Successfully processed lots from JSON string for task ID: {}", request.getTaskId());
                    } catch (Exception e) {
                        log.error("Error processing lots JSON for task ID: {}: {}", request.getTaskId(),
                                e.getMessage());
                    }
                }
            }
            List<TaskDTO> response = ppmFlowableService.validateTaskGroup(requests);
            return ResponseEntity.ok(response);
        } else {
            throw new ParticipantPresenceConflict("");
        }
    }

    @PostMapping(consumes = { "multipart/form-data" }, value = "/tasks/{taskId}/validate_avis_note_rapporteur")
    public ResponseEntity<TaskDTO> validateAvisNoteRapporteur(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request,
            @RequestPart(value = "noteFile", required = false) MultipartFile noteFile,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
            @RequestParam(value = "realisationsFinancementsJson", required = false) String realisationsFinancementsJson)
            throws IOException, PpmNotFound, AcNotFound, ComNotFound, DocNotFound {

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        Map<String, Object> vars = runtimeService.getVariables(task.getProcessInstanceId());
        Integer ppmId = (Integer) vars.get("id");
        Integer ppmVer = (Integer) vars.get("version");
        if (ppmId == null || ppmVer == null) {
            throw new IllegalStateException("Process variables 'id' and 'version' are required on this task.");
        }

        if (noteFile != null && !noteFile.isEmpty()) {
            String rootPath = Constants.PATH_ENTREPOT_GED_PPM;
            Integer docIdNote = documentCommuneService.processAndSaveDocumentsForModelPpm(
                    noteFile, ppmId, ppmVer, rootPath);

            request.setDocIdNote(docIdNote);
        }

        if (realisationsLotsJson != null && !realisationsLotsJson.isBlank()) {
            try {
                processRealisationLotsFromJson(request.getPpm(), realisationsLotsJson);
            } catch (Exception e) {
                log.warn("Failed to parse lots JSON before validate: {}", e.getMessage());
            }
        }

        if (realisationsFinancementsJson != null && !realisationsFinancementsJson.isBlank()) {
            try {
                processRealisationFinancementsFromJson(request.getPpm(), realisationsFinancementsJson);
            } catch (Exception e) {
                log.warn("Failed to parse financements JSON before validate: {}", e.getMessage());
            }
        }

        request.setFromMeeting(false);
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
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {
                    });
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
                            log.debug("Set budget to {} for lot with description: {}", lot.getBudget(),
                                    lot.getDescription());
                        } else {
                            log.warn("Invalid budget format for lot with description: {}, setting to null",
                                    lot.getDescription());
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

    @PostMapping(consumes = { "multipart/form-data" }, value = "/pv")
    public ResponseEntity<ProcessVerbalResponse> createPv(
            @ModelAttribute("request")
            // @RequestBody
            ProcessVerbalRequest request) throws PpmNotFound, AcNotFound, IOException, ComNotFound, RefParamNotFound {
        ProcessVerbal processVerbal = processVerbalService.create(request);
        ProcessVerbalResponse response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pv/find-by-ids/{ppmId}/{ppmVer}")
    public ResponseEntity<ProcessVerbalResponse> findByIds(@PathVariable Integer ppmId, @PathVariable Integer ppmVer)
            throws PpmNotFound {
        ProcessVerbal processVerbal = processVerbalService.findByIds(ppmId, ppmVer);
        ProcessVerbalResponse response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
        return ResponseEntity.ok(response);

    }

    @PutMapping(consumes = { "multipart/form-data" }, value = "/pv")
    public ResponseEntity<ProcessVerbalResponse> updatePv(
            @ModelAttribute("request")
            // @RequestBody
            ProcessVerbalRequest request) throws IOException, RefParamNotFound, DocNotFound {
        ProcessVerbal processVerbal = processVerbalService.update(request);
        ProcessVerbalResponse response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pv/find_by-ids_task_process/{ppmId}/{ppmVer}/{taskId}/{processInstanceId}")
    public ResponseEntity<ProcessVerbalResponse> findByIdsAndTaskIdAndProcessInstanceId(@PathVariable Integer ppmId,
            @PathVariable Integer ppmVer,
            @PathVariable String taskId,
            @PathVariable String processInstanceId) {
        ProcessVerbal processVerbal = processVerbalService.findByIdsAndTaskIdAndProcessInstanceId(ppmId, ppmVer, taskId,
                processInstanceId);
        ProcessVerbalResponse response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
        return ResponseEntity.ok(response);
    }

    @PutMapping(consumes = { "multipart/form-data" }, value = "/pv/add_pv")
    public ResponseEntity<ProcessVerbalResponse> addNewPv(
            @ModelAttribute("request")
            // @RequestBody
            PvRequest request) throws IOException, RefParamNotFound, DocNotFound {
        ProcessVerbal processVerbal = processVerbalService.addNewPv(request);
        ProcessVerbalResponse response = ProcessVerbalMapper.INSTANCE.map(processVerbal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/task-history/{procInstId}")
    public List<HistoricTaskDto> loadHistoric(@PathVariable("procInstId") String procInstId) {
        return ppmFlowableService.loadHistoricTasks(procInstId);
    }

    @GetMapping("/tasks/by_com/{comId}")
    public ResponseEntity<TaskListResponseDTO> getTasksByCom(@PathVariable Integer comId) {
        return ResponseEntity.ok(ppmFlowableService.getTasksByComId(comId));
    }

    @PostMapping("/modele-ppm")
    public ResponseEntity<byte[]> generateModelePpm(@RequestBody ModeleRequest modele) throws Exception {

        byte[] pdfBytes = ppmFlowableService.generateModelePpm(modele.getPpmId(), modele.getPpmVer(),
                modele.getFonction() + " - " + modele.getSignataire());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "Modele_PPM" + modele.getPpmId() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PreAuthorize("hasRole('VALIDATEUR_AC')")
    @Transactional(rollbackFor = {
            Exception.class
    })
    @PostMapping(consumes = { "multipart/form-data" }, value = "/tasks/{taskId}/validate_with_model")
    public ResponseEntity<TaskDTO> validateTaskWithModel(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request,
            @RequestPart(value = "modelPpmFile", required = false) MultipartFile modelPpmFile,
            @RequestParam(value = "realisationsLotsJson", required = false) String realisationsLotsJson,
            @RequestParam(value = "realisationsFinancementsJson", required = false) String realisationsFinancementsJson)
            throws IOException, PpmNotFound, AcNotFound, ComNotFound, DocNotFound {

        if (modelPpmFile == null || modelPpmFile.isEmpty()) {
            throw new PpmModelShouldExiste("Le fichier Model PPM est requis.");
        }

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        Map<String, Object> vars = runtimeService.getVariables(task.getProcessInstanceId());
        Integer ppmId = (Integer) vars.get("id");
        Integer ppmVer = (Integer) vars.get("version");
        if (ppmId == null || ppmVer == null) {
            throw new IllegalStateException("Process variables 'id' and 'version' are required on this task.");
        }

        String rootPath = Constants.PATH_ENTREPOT_GED_PPM;
        Integer docId = documentCommuneService.processAndSaveDocumentsForModelPpm(
                modelPpmFile, ppmId, ppmVer, rootPath);

        modelPpmService.setModel(ppmId, ppmVer, docId);
        // modelPpmService.setModel(ppmId, ppmVer, docId);

        if (realisationsLotsJson != null && !realisationsLotsJson.isBlank()) {
            try {
                processRealisationLotsFromJson(request.getPpm(), realisationsLotsJson);
            } catch (Exception e) {
                log.warn("Failed to parse lots JSON before validate: {}", e.getMessage());
            }
        }
        if (realisationsFinancementsJson != null && !realisationsFinancementsJson.isBlank()) {
            try {
                processRealisationFinancementsFromJson(request.getPpm(), realisationsFinancementsJson);
            } catch (Exception e) {
                log.warn("Failed to parse financements JSON before validate: {}", e.getMessage());
            }
        }

        request.setFromMeeting(false);
        TaskDTO response = ppmFlowableService.validateTask(taskId, request);

        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @PostMapping("/instances/{processInstanceId}/variables")
    public ResponseEntity<?> addVariable(
            @PathVariable String processInstanceId,
            @RequestBody Map<String, Object> variable) {

        String name = (String) variable.get("name");
        Object value = variable.get("value");

        runtimeService.setVariable(processInstanceId, name, value);
        return ResponseEntity.ok("Variable ajoutée avec succès !");
    }
}