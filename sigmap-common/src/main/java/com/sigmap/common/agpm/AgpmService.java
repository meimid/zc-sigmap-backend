package com.sigmap.common.agpm;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.agpm.request.AgpmRequest;
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

//    /**
//     * Retrieves a list of all non-deleted Agpm entities.
//     *
//     * @return A list of AgpmResponse DTOs representing non-deleted Agpm entities
//     */
//    List<Agpm> findAll();

    /**
     * Retrieves a paginated list of Agpm entities based on the given specification.
     *
     * @param spec     The specification to filter Agpm entities (e.g., by ref, nom, statusId)
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
     * Retrieves a single Agpm entity based on the given specification.
     *
     * @param specification The specification to filter the Agpm entity
     * @return An Optional containing the AgpmResponse DTO if found, or empty if not found
     */
    Optional<Agpm> findOne(Specification<Agpm> specification);

    /**
     * Retrieves an Agpm entity by its ID and version.
     *
     * @param agpmId  The ID of the Agpm entity
     * @param agpmVer The version of the Agpm entity
     * @return An Optional containing the Agpm entity if found, or empty if not found
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
     * Creates a new Agpm entity based on the provided request.
     *
     * @param request The AgpmRequest DTO containing the data to create the Agpm entity
     * @return The created AgpmResponse DTO
     * @throws AgpmAlreadyExistsException if an Agpm with the same ID and version already exists
     */
    Agpm create(AgpmRequest request) throws AgpmAlreadyExistsException, AgpmNotNull, AcNotFound, DocNotFound, AgpmDefaultStatus, IOException;

    /**
     * Soft deletes an Agpm entity by its ID and version.
     *
     * @param agpmId  The ID of the Agpm entity to delete
     * @param agpmVer The version of the Agpm entity to delete
     * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
     */
    void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound, AgpmSoftDelete;

    /**
     * Updates an existing Agpm entity by its ID and version with the provided request data.
     *
     * @param agpmId  The ID of the Agpm entity to update
     * @param agpmVer The version of the Agpm entity to update
     * @param request The AgpmRequest DTO containing the updated data
     * @return The updated AgpmResponse DTO
     * @throws AgpmNotFound if the Agpm with the specified ID and version is not found
     */
    Agpm updateWithVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, AcNotFound, DocNotFound, ReferenceNotFound, IOException;

     Agpm updateWithoutVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound, DocNotFound, IOException, InvalidIdAndVersionAgpm;

        Agpm updateStatus(Integer agpmId, Integer agpmVer,Integer statusId) throws AgpmNotFound;
}