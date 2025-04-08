package com.sigmap.sigmap_agpm.agpm;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmAlreadyExistsException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
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
    public ResponseEntity<AgpmResponse> getAgpmById(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer) {
        Optional<Agpm> agpm = agpmService.findById(agpmId, agpmVer);
        return agpm.map(a -> new ResponseEntity<>(AgpmMapper.INSTANCE.map(a), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
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
    public ResponseEntity<AgpmResponse> createAgpm(@Valid @RequestBody AgpmRequest request) throws AgpmAlreadyExistsException {
        AgpmResponse createdAgpm = agpmService.create(request);
        return new ResponseEntity<>(createdAgpm, HttpStatus.CREATED);
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
            @Valid @RequestBody AgpmRequest request) throws AgpmNotFound {
        AgpmResponse updatedAgpm = agpmService.update(agpmId, agpmVer, request);
        return new ResponseEntity<>(updatedAgpm, HttpStatus.OK);
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
            @PathVariable Integer agpmVer) throws AgpmNotFound {
        agpmService.delete(agpmId, agpmVer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}