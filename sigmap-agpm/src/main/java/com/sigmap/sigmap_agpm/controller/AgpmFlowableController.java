/**
 * REST Controller for managing Flowable process instances and tasks related to Agpm entities.
 * Provides endpoints for starting processes, retrieving tasks, validating tasks, and fetching user-specific tasks,
 * integrating with the AgpmFlowableService for workflow management.
 */
package com.sigmap.sigmap_agpm.controller;

import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.model_agpm.ModelAgpmService;
import com.sigmap.common.model_agpm.exceptions.AgpmModelShouldExiste;
import com.sigmap.common.model_paa.ModelPaaService;
import com.sigmap.sigmap_agpm.service.dto.request.ModeleAgpmRequest;
import com.sigmap.sigmap_agpm.service.dto.request.ValidationRequest;
import com.sigmap.sigmap_agpm.service.dto.response.*;
import com.sigmap.sigmap_agpm.service.AgpmFlowableService;
import org.springframework.transaction.annotation.Transactional;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing Flowable process instances and tasks related to Agpm entities.
 */
@RestController
@RequestMapping("/api/agpms/flowable")
public class AgpmFlowableController {

    @Autowired
    private AgpmFlowableService agpmFlowableService;
    private final DocumentCommuneService documentCommuneService;
    private final ModelAgpmService modelAgpmService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;

    private final Logger log = LoggerFactory.getLogger(AgpmFlowableController.class);

    public AgpmFlowableController(DocumentCommuneService documentCommuneService, ModelPaaService modelPaaService, ModelAgpmService modelAgpmService, TaskService taskService, RuntimeService runtimeService) {
        this.documentCommuneService = documentCommuneService;
        this.modelAgpmService = modelAgpmService;
        this.taskService = taskService;
        this.runtimeService = runtimeService;
    }

    /**
     * Starts a Flowable process instance for an Agpm entity using its ID.
     * Automatically retrieves the latest version of the Agpm entity.
     *
     * @param id The ID of the Agpm entity
     * @return ResponseEntity with ProcessStartResponseDTO and HTTP status 200 (OK)
     * @throws AgpmNotFound if the Agpm entity is not found
     */
    @PostMapping("/start/{id}")
    public ResponseEntity<ProcessStartResponseDTO> startProcessInstance(@PathVariable Integer id) throws AgpmNotFound, AcNotFound {
        log.info("Starting process instance for Agpm ID: {}", id);
        ProcessStartResponseDTO response = agpmFlowableService.startProcessInstance(id);
        return ResponseEntity.ok(response);
    }

//    /**
//     * Retrieves all active tasks for the tenant.
//     *
//     * @return ResponseEntity with TaskListResponseDTO and HTTP status 200 (OK)
//     */
//    @GetMapping("/tasks")
//    public ResponseEntity<TaskListResponseDTO> getCurrentTasks() {
//        log.info("Retrieving all current tasks");
//        TaskListResponseDTO response = agpmFlowableService.getCurrentTasks();
//        return ResponseEntity.ok(response);
//    }

    /**
     * Retrieves details of a specific task.
     *
     * @param taskId The ID of the task
     * @return ResponseEntity with TaskDetailsDTO and HTTP status 200 (OK)
     * @throws AgpmNotFound if the associated Agpm entity is not found
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailsDTO> getTaskDetails(@PathVariable String taskId) throws AgpmNotFound, AcNotFound {
        log.info("Retrieving details for task ID: {}", taskId);
        TaskDetailsDTO response = agpmFlowableService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a task and updates the associated Agpm entity.
     *
     * @param taskId  The ID of the task to validate
     * @param request The validation request containing validation details
     * @return ResponseEntity with TaskDTO and HTTP status 200 (OK)
     * @throws AgpmNotFound if the Agpm entity is not found
     * @throws DocNotFound if a document is not found
     * @throws AcNotFound if the associated Ac entity is not found
     * @throws IOException if an error occurs during file processing
     */
    @PostMapping(consumes = {"multipart/form-data"}, value = "/tasks/{taskId}/validate")
    public ResponseEntity<TaskDTO> validateTask(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request) throws AgpmNotFound, DocNotFound, AcNotFound, IOException {
        log.info("Validating task ID: {} with request: {}", taskId, request);
        TaskDTO response = agpmFlowableService.validateTask(taskId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves tasks assigned to the current user based on their candidate groups.
     *
     * @return ResponseEntity with TaskListResponseDTO and HTTP status 200 (OK)
     */
    @GetMapping("/tasks/user")
    public ResponseEntity<TaskListResponseDTO> getUserTasks() {
        log.info("Retrieving tasks for current user");
        TaskListResponseDTO response = agpmFlowableService.getUserTasks();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/task-history/{procInstId}")
    public List<HistoricTaskDto> loadHistoric(@PathVariable("procInstId") String procInstId) {
        return agpmFlowableService.loadHistoricTasks(procInstId);
    }


    @PreAuthorize("hasRole('VALIDATEUR_AC')")
    @Transactional(rollbackFor = {
            Exception.class
    })
    @PostMapping(consumes = {"multipart/form-data"}, value = "/tasks/{taskId}/validate_with_model")
    public ResponseEntity<TaskDTO> validateTaskWithModel(
            @PathVariable String taskId,
            @ModelAttribute("request") ValidationRequest request,
            @RequestPart(value = "modelPaaFile", required = false) MultipartFile modelAgpmFile
    ) throws IOException, AcNotFound, DocNotFound, AgpmNotFound {

        if (modelAgpmFile == null || modelAgpmFile.isEmpty()) {
            throw new AgpmModelShouldExiste("Le fichier Model AGPM est requis.");
        }

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        Map<String, Object> vars = runtimeService.getVariables(task.getProcessInstanceId());
        Integer agpmId = (Integer) vars.get("id");
        Integer agpmVer = (Integer) vars.get("version");
        if (agpmId == null || agpmVer == null) {
            throw new IllegalStateException("Process variables 'id' and 'version' are required on this task.");
        }

        String rootPath = Constants.PATH_ENTREPOT_GED_AGPM;
        Integer docId = documentCommuneService.processAndSaveDocumentsForModelAgpm(
                modelAgpmFile, agpmId,agpmVer, rootPath
        );

        modelAgpmService.setModel(agpmId, agpmVer, docId);


        TaskDTO response = agpmFlowableService.validateTask(taskId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/modele-agpm")
    public ResponseEntity<byte[]> generateReportPv(@RequestBody ModeleAgpmRequest  modele)  throws Exception {
        URL pvFolderUrl = getClass().getClassLoader().getResource("modele/");
        String reportsPath = pvFolderUrl.getPath();

        log.info("reportsPath {} ", reportsPath);

        byte[] pdfBytes = agpmFlowableService.generateModeleAgpm(modele.getAgpmId(),modele.getAgpmVer(),modele.getFonction()+" - "+ modele.getSignataire(), reportsPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "modele_agpm_" + modele.getAgpmId() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}