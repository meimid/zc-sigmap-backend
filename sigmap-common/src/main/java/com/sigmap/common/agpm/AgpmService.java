/**
 * Service interface for managing Agpm entities.
 * Defines methods for CRUD operations, filtering, pagination, and status updates of Agpm entities.
 * Provides functionality to interact with Agpm entities, including creation, retrieval, updating,
 * and soft deletion, with support for versioning and exception handling.
 */
package com.sigmap.common.agpm;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmMinimalResponseDto;
import com.sigmap.common.agpm.response.AgpmResponseDto;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.util.Optional;

/**
 * Service interface for managing Agpm entities.
 * Defines methods for CRUD operations, filtering, and pagination of Agpm entities.
 */
public interface AgpmService {

    /**
     * Retrieves a paginated list of Agpm entities based on the provided specification.
     *
     * @param spec     The JPA specification to filter Agpm entities (e.g., by ref, nom, statusId)
     * @param pageable The pagination and sorting information
     * @return A Page of Agpm entities matching the specification
     */
    Page<Agpm> findAll(Specification<Agpm> spec, Pageable pageable);

    /**
     * Retrieves a paginated list of deleted Agpm entities.
     *
     * @param pageable The pagination and sorting information
     * @return A Page of deleted Agpm entities
     */
    Page<Agpm> findAllDeleted(Pageable pageable);

    /**
     * Retrieves a single Agpm entity based on the provided specification.
     *
     * @param specification The JPA specification to filter the Agpm entity
     * @return An Optional containing the Agpm entity if found, or empty if not found
     */
    Optional<Agpm> findOne(Specification<Agpm> specification);

    /**
     * Retrieves an Agpm entity by its ID and version.
     *
     * @param agpmId  The ID of the Agpm entity
     * @param agpmVer The version of the Agpm entity (nullable to fetch the latest version)
     * @return The Agpm entity if found
     * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
     */
    Agpm findById(Integer agpmId, Integer agpmVer) throws AgpmNotFound;

    /**
     * Saves an Agpm entity to the database.
     *
     * @param agpm The Agpm entity to save
     * @return The saved Agpm entity
     */
    Agpm save(Agpm agpm);

    /**
     * Creates a new Agpm entity based on the provided request data.
     *
     * @param request The AgpmRequest DTO containing the data to create the Agpm entity
     * @return The created Agpm entity
     * @throws AgpmAlreadyExistsException if an Agpm with the same ID and version already exists
     * @throws AgpmNotNull if required fields in the request are null
     * @throws AcNotFound if the associated Ac entity is not found
     * @throws DocNotFound if a document referenced in the request is not found
     * @throws AgpmDefaultStatus if the default status is invalid
     * @throws IOException if an error occurs during file processing
     */
    Agpm create(AgpmRequest request) throws AgpmAlreadyExistsException, AgpmNotNull, AcNotFound, DocNotFound, AgpmDefaultStatus, IOException;

    /**
     * Soft deletes an Agpm entity by its ID and version.
     *
     * @param agpmId  The ID of the Agpm entity to delete
     * @param agpmVer The version of the Agpm entity to delete
     * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
     * @throws AgpmSoftDelete if soft deletion is not allowed
     * @throws AgpmInvalidStatusException if the Agpm entity has an invalid status for deletion
     */
    void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound, AgpmSoftDelete, AgpmInvalidStatusException;

    /**
     * Updates an existing Agpm entity with versioning, using the provided request data.
     *
     * @param agpmId  The ID of the Agpm entity to update
     * @param agpmVer The version of the Agpm entity to update
     * @param request The AgpmRequest DTO containing the updated data
     * @return The updated Agpm entity
     * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
     * @throws AcNotFound if the associated Ac entity is not found
     * @throws DocNotFound if a document referenced in the request is not found
     * @throws ReferenceNotFound if a referenced parameter is not found
     * @throws IOException if an error occurs during file processing
     */
    Agpm updateWithVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, AcNotFound, DocNotFound, ReferenceNotFound, IOException;

    /**
     * Updates an existing Agpm entity without versioning, using the provided request data.
     *
     * @param agpmId  The ID of the Agpm entity to update
     * @param agpmVer The version of the Agpm entity to update
     * @param request The AgpmRequest DTO containing the updated data
     * @return The updated Agpm entity
     * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
     * @throws DocNotFound if a document referenced in the request is not found
     * @throws IOException if an error occurs during file processing
     * @throws InvalidIdAndVersionAgpm if the ID and version combination is invalid
     * @throws AgpmInvalidStateException if the Agpm entity is in an invalid state for updating
     */
    Agpm updateWithoutVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, DocNotFound, IOException, InvalidIdAndVersionAgpm, AgpmInvalidStateException;

    /**
     * Updates the status of an Agpm entity.
     *
     * @param agpmId   The ID of the Agpm entity to update
     * @param agpmVer  The version of the Agpm entity to update
     * @param statusId The new status ID to set
     * @return The updated Agpm entity
     * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
     */
    Agpm updateStatus(Integer agpmId, Integer agpmVer, Integer statusId) throws AgpmNotFound;

    /**
     * Retrieves all versions of an Agpm entity by its ID, including only non-deleted documents,
     * in AgpmResponseDto format with pagination.
     *
     * @param agpmId   ID of the Agpm entity
     * @param pageable Pagination and sorting configuration
     * @return Page of AgpmResponseDto representing all versions for the given ID
     * @throws AgpmNotFound if no Agpm entities are found for the ID
     */
    Page<AgpmResponseDto> findAllVersionsByIdFull(Integer agpmId, Pageable pageable) throws AgpmNotFound;

    /**
     * Retrieves all versions of an Agpm entity by its ID, including only id, version, ref, nom, and metadata,
     * in AgpmMinimalResponseDto format with pagination.
     *
     * @param agpmId   ID of the Agpm entity
     * @param pageable Pagination and sorting configuration
     * @return Page of AgpmMinimalResponseDto representing all versions for the given ID
     * @throws AgpmNotFound if no Agpm entities are found for the ID
     */
    Page<AgpmMinimalResponseDto> findAllVersionsByIdMinimal(Integer agpmId, Pageable pageable) throws AgpmNotFound;
}