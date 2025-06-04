/**
 * REST Controller for managing Agpm entities.
 * Provides endpoints for creating, retrieving, updating, and deleting Agpm entities,
 * with support for pagination, filtering, and file uploads. Integrates with AgpmService
 * for business logic and handles exceptions for robust API responses.
 */
package com.sigmap.sigmap_agpm.controller;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmMinimalResponseDto;
import com.sigmap.common.agpm.response.AgpmResponse;
import com.sigmap.common.agpm.response.AgpmResponseDto;
import com.sigmap.common.agpm.response.AgpmResponseDtoSoft;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.docs.file_check.FileCheckServices;
import com.sigmap.sigmap_agpm.service.AgpmFlowableService;
import com.sigmap.sigmap_agpm.service.dto.response.TaskDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST Controller for managing Agpm entities.
 * Provides endpoints for CRUD operations on Agpm entities.
 */
@RestController
@RequestMapping("/api/agpms")
@Slf4j
public class AgpmController {

    private final AgpmService agpmService;
    private final FileCheckServices fileCheckServices;

    @Autowired
    private AgpmFlowableService flowableService;

    @Value("${max-file-size}")
    private long maxFileUploadSize;

    /**
     * Constructor with dependency injection.
     *
     * @param agpmService       The AgpmService for business logic
     * @param fileCheckServices The service for validating file uploads
     */
    public AgpmController(AgpmService agpmService, FileCheckServices fileCheckServices) {
        this.agpmService = agpmService;
        this.fileCheckServices = fileCheckServices;
    }

    /**
     * Retrieves a paginated list of non-deleted Agpm entities, returning only the latest version for each ID
     * based on the given query.
     *
     * @param query    The query to filter Agpm entities (e.g., ref, nom, statusId)
     * @param pageable The pagination and sorting information
     * @return ResponseEntity with a Page of AgpmResponse DTOs and HTTP status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<Page<AgpmResponse>> getAllLatestAgpms(AgpmQuery query, Pageable pageable) throws AgpmNotFound, AcNotFound {
        Specification<Agpm> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<Agpm> agpmPage = agpmService.findAll(spec, pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        return new ResponseEntity<>(agpmResponsePage, HttpStatus.OK);
    }

    /**
     * Retrieves a paginated list of deleted Agpm entities.
     *
     * @param pageable The pagination and sorting information
     * @return ResponseEntity with a Page of AgpmResponse DTOs and HTTP status 200 (OK)
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<AgpmResponse>> getAllDeletedAgpms(Pageable pageable) {
        Page<Agpm> agpmPage = agpmService.findAllDeleted(pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        return new ResponseEntity<>(agpmResponsePage, HttpStatus.OK);
    }

    /**
     * Retrieves the latest version of an Agpm entity by its ID.
     *
     * @param agpmId The ID of the Agpm entity
     * @return ResponseEntity with the AgpmResponse DTO and HTTP status 200 (OK)
     * @throws AgpmNotFound if the Agpm with the specified ID is not found
     */
    @GetMapping("/{agpmId}")
    public ResponseEntity<AgpmResponse> getAgpmById(@PathVariable Integer agpmId) throws AgpmNotFound, AcNotFound {
        Agpm agpm = agpmService.findById(agpmId, null);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(agpm);
        return ResponseEntity.ok(agpmResponse);
    }

    /**
     * Creates a new Agpm entity.
     *
     * @param request The AgpmRequest DTO containing the data to create the Agpm
     * @return ResponseEntity with the created AgpmResponse DTO and HTTP status 200 (OK)
     * @throws AgpmAlreadyExistsException if an Agpm with the same ID and version exists
     * @throws AgpmNotNull                if required fields are null
     * @throws AcNotFound                 if the associated Ac entity is not found
     * @throws DocNotFound                if a requested document is not found
     * @throws AgpmDefaultStatus          if the default status cannot be set
     * @throws IOException                if an error occurs during file processing
     * @throws MaxUploadSizeFile          if the uploaded file exceeds the maximum size limit
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<AgpmResponse> createAgpm(@ModelAttribute("request") AgpmRequest request)
            throws AgpmAlreadyExistsException, AgpmNotNull, AcNotFound, DocNotFound, AgpmDefaultStatus, IOException, MaxUploadSizeFile {
        fileCheckServices.chechFileUpload(request.getFiles(), maxFileUploadSize);
        Agpm createdAgpm = agpmService.create(request);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(createdAgpm);
        return ResponseEntity.ok(agpmResponse);
    }

    /**
     * Updates the latest version of an Agpm entity by its ID without versioning.
     *
     * @param agpmId  The ID of the Agpm entity
     * @param request The AgpmRequest DTO containing the updated data
     * @return ResponseEntity with the updated AgpmResponse DTO and HTTP status 200 (OK)
     * @throws AgpmNotFound               if the Agpm with the specified ID is not found
     * @throws DocNotFound                if a requested document is not found
     * @throws IOException                if an error occurs during file processing
     * @throws InvalidIdAndVersionAgpm    if the ID is invalid
     * @throws MaxUploadSizeFile          if the uploaded file exceeds the maximum size limit
     * @throws AgpmInvalidStateException  if the Agpm is not in a valid state for updating
     */
    @PutMapping(consumes = {"multipart/form-data"}, value = "/{agpmId}")
    public ResponseEntity<AgpmResponse> updateAgpm(@PathVariable Integer agpmId,
                                                   @ModelAttribute("request") AgpmRequest request)
            throws AgpmNotFound, DocNotFound, IOException, InvalidIdAndVersionAgpm, MaxUploadSizeFile, AgpmInvalidStateException {
        fileCheckServices.chechFileUpload(request.getFiles(), maxFileUploadSize);
        Agpm updatedAgpm = agpmService.updateWithoutVersioning(agpmId, null, request);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(updatedAgpm);
        return new ResponseEntity<>(agpmResponse, HttpStatus.OK);
    }

    /**
     * Soft deletes an Agpm entity by its ID and version.
     *
     * @param agpmId  The ID of the Agpm entity
     * @param agpmVer The version of the Agpm entity
     * @return ResponseEntity with HTTP status 204 (No Content)
     * @throws AgpmNotFound               if the Agpm with the specified ID and version is not found
     * @throws AgpmSoftDelete             if the soft delete operation fails
     * @throws AgpmInvalidStatusException if the Agpm status is not valid for deletion
     */
    @DeleteMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<Void> deleteAgpm(@PathVariable Integer agpmId, @PathVariable Integer agpmVer)
            throws AgpmNotFound, AgpmSoftDelete, AgpmInvalidStatusException {
        agpmService.delete(agpmId, agpmVer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Retrieves all versions of an Agpm entity by its ID with full details in AgpmResponseDto format.
     *
     * @param agpmId   ID of the Agpm entity
     * @param pageable Pagination and sorting parameters
     * @return ResponseEntity containing a page of AgpmResponseDto
     * @throws AgpmNotFound if no Agpm entities are found for the ID
     */
    @GetMapping("/history/full/{agpmId}")
    public ResponseEntity<Page<AgpmResponseDto>> getAgpmHistoryFull(@PathVariable Integer agpmId, Pageable pageable) throws AgpmNotFound {
        log.info("Received request to fetch full history for Agpm ID {}, page {}, size {}, sort {}",
                agpmId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<AgpmResponseDto> agpmPage = agpmService.findAllVersionsByIdFull(agpmId, pageable);
        log.info("Returning {} versions for Agpm ID {} (page {} of {})",
                agpmPage.getNumberOfElements(), agpmId, agpmPage.getNumber(), agpmPage.getTotalPages());
        return ResponseEntity.ok(agpmPage);
    }

    /**
     * Retrieves all versions of an Agpm entity by its ID with minimal details in AgpmMinimalResponseDto format.
     *
     * @param agpmId   ID of the Agpm entity
     * @param pageable Pagination and sorting parameters
     * @return ResponseEntity containing a page of AgpmMinimalResponseDto
     * @throws AgpmNotFound if no Agpm entities are found for the ID
     */
    @GetMapping("/history/minimal/{agpmId}")
    public ResponseEntity<Page<AgpmMinimalResponseDto>> getAgpmHistoryMinimal(@PathVariable Integer agpmId, Pageable pageable) throws AgpmNotFound {
        log.info("Received request to fetch minimal history for Agpm ID {}, page {}, size {}, sort {}",
                agpmId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<AgpmMinimalResponseDto> agpmPage = agpmService.findAllVersionsByIdMinimal(agpmId, pageable);
        log.info("Returning {} minimal versions for Agpm ID {} (page {} of {})",
                agpmPage.getNumberOfElements(), agpmId, agpmPage.getNumber(), agpmPage.getTotalPages());
        return ResponseEntity.ok(agpmPage);
    }
    /**
     * find page of Agpms with Ac Id*/
    @GetMapping("/ac/{acId}")
    public ResponseEntity<Page<AgpmResponseDtoSoft>> findAgpmsByAcId(@PathVariable Integer acId, Pageable pageable) throws AcNotFound {
        Page<Agpm> agpms = agpmService.findAgpmsByAcId(acId,pageable);
        Page<AgpmResponseDtoSoft> responses = agpms.map(AgpmMapper.INSTANCE::toDto);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/details/{agpmId}")
    public ResponseEntity<TaskDetailsDTO> getAgpmWithTasks(@PathVariable Integer agpmId) {
        try {
            TaskDetailsDTO response = flowableService.getAgpmWithTasks(agpmId);
            return ResponseEntity.ok(response);
        } catch (AgpmNotFound | AcNotFound e) {
            TaskDetailsDTO errorResponse = new TaskDetailsDTO();
            errorResponse.setStatus(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}