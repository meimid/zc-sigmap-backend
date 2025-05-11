package com.sigmap.sigmap_agpm.agpm;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Optional;

/**
 * REST Controller for managing Agpm entities.
 * Provides endpoints for CRUD operations on Agpm.
 */
@RestController
@RequestMapping("/api/agpms")
public class AgpmController {

    private final AgpmService agpmService;

    /**
     * Constructor with dependency injection.
     *
     * @param agpmService The AgpmService for business logic
     */
    public AgpmController(AgpmService agpmService) {
        this.agpmService = agpmService;
    }

    /**
     * GET /api/agpms
     * Retrieves a paginated list of non-deleted Agpm entities based on the given query.
     *
     * @param query    The query to use for filtering (e.g., ref, nom, statusId)
     * @param pageable The pagination and sorting information
     * @return ResponseEntity containing a Page of AgpmResponse DTOs
     */
    @GetMapping
    public ResponseEntity<Page<AgpmResponse>> getAllAgpms(AgpmQuery query, Pageable pageable) {
        Page<Agpm> agpmPage = agpmService.findAll(query.toSpecification(), pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        return new ResponseEntity<>(agpmResponsePage, HttpStatus.OK);
    }

    /**
     * GET /api/agpms/deleted
     * Retrieves a paginated list of deleted Agpm entities.
     *
     * @param pageable The pagination and sorting information
     * @return ResponseEntity containing a Page of AgpmResponse DTOs
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<AgpmResponse>> getAllDeletedAgpms(Pageable pageable) {
        Page<Agpm> agpmPage = agpmService.findAllDeleted(pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        return new ResponseEntity<>(agpmResponsePage, HttpStatus.OK);
    }


    /**
     * GET /api/agpms/{agpmId}/{agpmVer}
     * Retrieves an Agpm entity by its ID and version.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @return ResponseEntity containing the AgpmResponse DTO, or 404 if not found
     */
    @GetMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<AgpmResponse> getAgpmById(@PathVariable Integer agpmId,@PathVariable Integer agpmVer) throws AgpmNotFound{
        Agpm agpm = agpmService.findById(agpmId, agpmVer);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(agpm);
        return ResponseEntity.ok(agpmResponse);
    }



    /**
     * POST /api/agpms
     * Creates a new Agpm entity.
     *
     * @param request The AgpmRequest DTO containing the data to create the Agpm
     * @return ResponseEntity containing the created AgpmResponse DTO with status 201
     * @throws AgpmAlreadyExistsException if an Agpm with the same ID and version already exists
     */
    @PostMapping
    public ResponseEntity<AgpmResponse> createAgpm(@Valid @RequestBody AgpmRequest request) throws AgpmAlreadyExistsException, AgpmNotNull, AcNotFound, DocNotFound, AgpmDefaultStatus {
        Agpm createdAgpm = agpmService.create(request);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(createdAgpm);
        return new ResponseEntity<>(agpmResponse, HttpStatus.CREATED);
    }

    /**
     * PUT /api/agpms/{agpmId}/{agpmVer}
     * Updates an existing Agpm entity by its ID and version.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @param request The AgpmRequest DTO containing the updated data
     * @return ResponseEntity containing the updated AgpmResponse DTO with status 200
     * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
     */
    @PutMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<AgpmResponse> updateAgpm(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer,
            @Valid @RequestBody AgpmRequest request) throws AgpmNotFound, AcNotFound, DocNotFound, ReferenceNotFound {
        Agpm updatedAgpm = agpmService.updateWithoutVersioning(agpmId, agpmVer, request);
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
     * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
     */
    @DeleteMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<Void> deleteAgpm(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer) throws AgpmNotFound, AgpmSoftDelete {
        agpmService.delete(agpmId, agpmVer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/status/{agpmId}/{agpmVer}/{statusId}")
    public ResponseEntity<AgpmResponse> updateAgpmStatus(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer,
            @PathVariable Integer statusId) throws AgpmNotFound {

        Agpm updated = agpmService.updateStatus(agpmId, agpmVer, statusId);
        AgpmResponse response =  AgpmMapper.INSTANCE.map(updated);
        return ResponseEntity.ok(response);
    }


}