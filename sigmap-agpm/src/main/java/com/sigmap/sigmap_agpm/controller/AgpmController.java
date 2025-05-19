package com.sigmap.sigmap_agpm.controller;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.docs.file_check.FileCheckServices;
import lombok.extern.slf4j.Slf4j;
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


    @Value("${max-file-size}")
    private long maxFileUploadSize;

    /**
     * Constructor with dependency injection.
     *
     * @param agpmService The AgpmService for business logic
     */
    public AgpmController(AgpmService agpmService, FileCheckServices fileCheckServices) {
        this.agpmService = agpmService;
        this.fileCheckServices = fileCheckServices;
    }

    /**
     * GET /api/agpms/latest
     * Retrieves a paginated list of non-deleted Agpm entities, returning only the latest version for each ID
     * based on the given query.
     *
     * @param query    The query to filter Agpm entities (e.g., ref, nom, statusId)
     * @param pageable The pagination and sorting information
     * @return ResponseEntity with a Page of AgpmResponse DTOs and status 200
     */
    @GetMapping
    public ResponseEntity<Page<AgpmResponse>> getAllLatestAgpms(AgpmQuery query, Pageable pageable) {
        Specification<Agpm> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<Agpm> agpmPage = agpmService.findAll(spec, pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        return new ResponseEntity<>(agpmResponsePage, HttpStatus.OK);
    }
    /**
     * GET /api/agpms/deleted
     * Retrieves a paginated list of deleted Agpm entities.
     *
     * @param pageable The pagination and sorting information
     * @return ResponseEntity with a Page of AgpmResponse DTOs and status 200
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<AgpmResponse>> getAllDeletedAgpms(Pageable pageable) {
        Page<Agpm> agpmPage = agpmService.findAllDeleted(pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        return new ResponseEntity<>(agpmResponsePage, HttpStatus.OK);
    }

    /**
     * GET /api/agpms/{agpmId}
     * Retrieves the latest version of an Agpm entity by its ID.
     *
     * @param agpmId The Agpm ID
     * @return ResponseEntity with the AgpmResponse DTO and status 200
     * @throws AgpmNotFound if the Agpm with the specified ID is not found
     */
    @GetMapping("/{agpmId}")
    public ResponseEntity<AgpmResponse> getAgpmById(@PathVariable Integer agpmId) throws AgpmNotFound {
        Agpm agpm = agpmService.findById(agpmId, null);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(agpm);
        return ResponseEntity.ok(agpmResponse);
    }

    /**
     * POST /api/agpms
     * Creates a new Agpm entity.
     *
     * @param request The AgpmRequest DTO containing the data to create the Agpm
     * @return ResponseEntity with the created AgpmResponse DTO and status 200
     * @throws AgpmAlreadyExistsException if an Agpm with the same ID and version exists
     * @throws AgpmNotNull                if required fields are null
     * @throws AcNotFound                 if the associated Ac entity is not found
     * @throws DocNotFound                if a requested document is not found
     * @throws AgpmDefaultStatus          if the default status cannot be set
     * @throws IOException                if an error occurs during file processing
     * @note Consider adding @Valid for request validation if needed
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
     * PUT /api/agpms/{agpmId}
     * Updates the latest version of an Agpm entity by its ID without versioning.
     *
     * @param agpmId  The Agpm ID
     * @param request The AgpmRequest DTO containing the updated data
     * @return ResponseEntity with the updated AgpmResponse DTO and status 200
     * @throws AgpmNotFound            if the Agpm with the specified ID is not found
     * @throws DocNotFound             if a requested document is not found
     * @throws IOException             if an error occurs during file processing
     * @throws InvalidIdAndVersionAgpm if the ID is invalid
     * @note Consider adding @Valid for request validation if needed
     */
   @PutMapping(consumes = {"multipart/form-data"}, value = "/{agpmId}")
    public ResponseEntity<AgpmResponse> updateAgpm(@PathVariable Integer agpmId,
                                                  @ModelAttribute("request")
                                                   AgpmRequest request)
            throws AgpmNotFound, DocNotFound, IOException, InvalidIdAndVersionAgpm, MaxUploadSizeFile {
       fileCheckServices.chechFileUpload(request.getFiles(), maxFileUploadSize);
        Agpm updatedAgpm = agpmService.updateWithoutVersioning(agpmId, null, request);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(updatedAgpm);
        return new ResponseEntity<>(agpmResponse, HttpStatus.OK);
    }

    /**
     * DELETE /api/agpms/{agpmId}/{agpmVer}
     * Soft deletes an Agpm entity by its ID and version.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @return ResponseEntity with status 204 (No Content)
     * @throws AgpmNotFound   if the Agpm with the specified ID and version is not found
     * @throws AgpmSoftDelete if the soft delete operation fails
     */
    @DeleteMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<Void> deleteAgpm(@PathVariable Integer agpmId, @PathVariable Integer agpmVer)
            throws AgpmNotFound, AgpmSoftDelete {
        agpmService.delete(agpmId, agpmVer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}