package com.sigmap.common.agpm;

import com.sigmap.common.agpm.exceptions.AgpmAlreadyExistsException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing Agpm entities.
 * Defines methods for CRUD operations and querying Agpm entities.
 */
public interface AgpmService {

    /**
     * Retrieves all non-deleted Agpm entities.
     *
     * @return List of AgpmResponse DTOs
     */
    List<AgpmResponse> findAll();

    /**
     * Retrieves a paginated list of Agpm entities based on the given specification.
     *
     * @param spec     The specification for filtering
     * @param pageable The pagination information
     * @return Page of AgpmResponse DTOs
     */
    Page<AgpmResponse> findAll(Specification<Agpm> spec, Pageable pageable);

    /**
     * Retrieves a paginated list of deleted Agpm entities based on the given specification.
     *
     * @param spec     The specification for filtering
     * @param pageable The pagination information
     * @return Page of AgpmResponse DTOs
     */
    Page<AgpmResponse> findAllDeleted(Specification<Agpm> spec, Pageable pageable);

    /**
     * Retrieves a paginated list of deleted Agpm entities.
     *
     * @param pageable The pagination information
     * @return Page of AgpmResponse DTOs
     */
    Page<AgpmResponse> findAllDeleted(Pageable pageable);

    /**
     * Retrieves a deleted Agpm entity by its ID and version.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @return Optional containing the AgpmResponse DTO if found
     */
    Optional<AgpmResponse> findDeletedById(Integer agpmId, Integer agpmVer);

    /**
     * Retrieves an Agpm entity based on the given specification.
     *
     * @param specification The specification for filtering
     * @return Optional containing the AgpmResponse DTO if found
     */
    Optional<AgpmResponse> findOne(Specification<Agpm> specification);

    /**
     * Retrieves an Agpm entity by its ID and version.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @return Optional containing the AgpmResponse DTO if found
     */
    Optional<AgpmResponse> findById(Integer agpmId, Integer agpmVer);

    /**
     * Saves an Agpm entity.
     *
     * @param agpm The Agpm entity to save
     * @return The saved Agpm entity
     */
    Agpm save(Agpm agpm);

    /**
     * Creates a new Agpm entity.
     *
     * @param request The AgpmRequest DTO containing the data to create the Agpm
     * @return The created Agpm entity as an AgpmResponse DTO
     * @throws AgpmAlreadyExistsException if an Agpm with the same ID and version exists
     */
    AgpmResponse create(AgpmRequest request) throws AgpmAlreadyExistsException;

    /**
     * Soft deletes an Agpm entity by its ID and version.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @throws AgpmNotFound if the Agpm entity is not found
     */
    void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound;

    /**
     * Updates an existing Agpm entity.
     *
     * @param agpmId  The Agpm ID
     * @param agpmVer The Agpm version
     * @param request The AgpmRequest DTO containing the updated data
     * @return The updated Agpm entity as an AgpmResponse DTO
     * @throws AgpmNotFound if the Agpm entity is not found
     */
    AgpmResponse update(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound;
}