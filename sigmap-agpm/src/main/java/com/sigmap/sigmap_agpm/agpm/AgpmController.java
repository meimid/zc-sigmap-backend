package com.sigmap.sigmap_agpm.agpm;

import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmAlreadyExistsException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
     * Retrieves a paginated list of non-deleted Agpm entities.
     *
     * @param page The page number (default: 0)
     * @param size The page size (default: 10)
     * @return ResponseEntity containing a Page of AgpmResponse DTOs
     */
    @GetMapping
    public ResponseEntity<Page<AgpmResponse>> getAllAgpms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AgpmResponse> agpmPage = agpmService.findAll(null, pageable);
        return new ResponseEntity<>(agpmPage, HttpStatus.OK);
    }

    /**
     * GET /api/agpms/deleted
     * Retrieves a paginated list of deleted Agpm entities.
     *
     * @param page The page number (default: 0)
     * @param size The page size (default: 10)
     * @return ResponseEntity containing a Page of AgpmResponse DTOs
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<AgpmResponse>> getAllDeletedAgpms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AgpmResponse> agpmPage = agpmService.findAllDeleted(pageable);
        return new ResponseEntity<>(agpmPage, HttpStatus.OK);
    }

    /**
     * GET /api/agpms/{agpmId}/{agpmVer}
     * Retrieves an Agpm entity by its ID and version.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @return ResponseEntity containing the AgpmResponse DTO
     */
    @GetMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<AgpmResponse> getAgpmById(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer) {
        Optional<AgpmResponse> agpmResponse = agpmService.findById(agpmId, agpmVer);
        return agpmResponse.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET /api/agpms/deleted/{agpmId}/{agpmVer}
     * Retrieves a deleted Agpm entity by its ID and version.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @return ResponseEntity containing the AgpmResponse DTO
     */
    @GetMapping("/deleted/{agpmId}/{agpmVer}")
    public ResponseEntity<AgpmResponse> getDeletedAgpmById(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer) {
        Optional<AgpmResponse> agpmResponse = agpmService.findDeletedById(agpmId, agpmVer);
        return agpmResponse.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /api/agpms
     * Creates a new Agpm entity.
     *
     * @param request The AgpmRequest DTO containing the data to create the Agpm
     * @return ResponseEntity containing the created AgpmResponse DTO
     * @throws AgpmAlreadyExistsException if an Agpm with the same ID and version exists
     */
    @PostMapping
    public ResponseEntity<AgpmResponse> createAgpm(@Valid @RequestBody AgpmRequest request) throws AgpmAlreadyExistsException {
        AgpmResponse createdAgpm = agpmService.create(request);
        return new ResponseEntity<>(createdAgpm, HttpStatus.CREATED);
    }

    /**
     * PUT /api/agpms/{agpmId}/{agpmVer}
     * Updates an existing Agpm entity.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @param request The AgpmRequest DTO containing the updated data
     * @return ResponseEntity containing the updated AgpmResponse DTO
     * @throws AgpmNotFound if the Agpm is not found
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
     * @return ResponseEntity with no content
     * @throws AgpmNotFound if the Agpm is not found
     */
    @DeleteMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<Void> deleteAgpm(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer) throws AgpmNotFound {
        agpmService.delete(agpmId, agpmVer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}