package com.sigmap.common.paas;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.paas.exceptions.*;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.paas.request.PaaRequestGlobal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Paa}.
 * The service provides methods to retrieve and manage Paa entities.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link PaaSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see PaaSpecifications
 * @see Specification
 */
public interface PaaService {

    public Paa getPaaOfCurrentYear();

    List<Paa> findAll();

    /**
     * Return a page of Paa entities based on the given specification and pageable.
     *
     * @param spec     the specification to use
     * @param pageable the pageable to use
     * @return a page of Paa entities
     */
    Page<Paa> findAll(Specification<Paa> spec, Pageable pageable) throws AcNotFound, PaaNotFound;

    /**
     * Return a page of deleted Paa entities based on the given specification and pageable.
     *
     * @param spec     the specification to use
     * @param pageable the pageable to use
     * @return a page of Paa entities
     */
    Page<Paa> findAllDeleted(Specification<Paa> spec, Pageable pageable);

    /**
     * Return a page of deleted Paa entities based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Paa entities
     */
    Page<Paa> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Paa entity with the given composite ID components.
     *
     * @param paaId  the first part of the composite ID (paa_id)
     * @param paaVer the second part of the composite ID (paa_ver)
     * @return the Paa entity with the given ID components
     */
    Optional<Paa> findDeletedById(Integer paaId, Integer paaVer);

    /**
     * Return the Paa entity with the given specification.
     *
     * @param specification the specification to use
     * @return the Paa entity with the given specification
     */
    Optional<Paa> findOne(Specification<Paa> specification);

    /**
     * Return the Paa entity with the given composite ID components.
     *
     * @param paaId  the first part of the composite ID (paa_id)
     * @param paaVer the second part of the composite ID (paa_ver)
     * @return the Paa entity with the given ID components
     */
    Optional<Paa> findById(Integer paaId, Integer paaVer) throws PaaNotFound, AcNotFound;

    /**
     * Find the latest version of a Paa entity by its ID.
     *
     * @param paaId The ID of the Paa to find.
     * @return An Optional containing the latest version of the Paa, or empty if not found.
     */
    Optional<Paa> findLatestById(Integer paaId);

    /**
     * Save a Paa entity.
     *
     * @param paa the Paa entity to save
     * @return the saved Paa entity
     */
    Paa save(Paa paa);

    /**
     * Create a Paa entity.
     *
     * @param request the request to create the Paa entity from
     * @return the created Paa entity
     */
    Paa create(PaaRequest request) throws PaaAlreadyExistsException;

    /**
     * Create a PAA with its Realisations and Lots in a single operation.
     * This method allows for creating a complete PAA hierarchy including:
     * - PAA entity with basic properties
     * - Associated Realisations
     * - Lots within each Realisation
     *
     * @param request the global request containing PAA, Realisations, and Lots data
     * @return the created PAA entity with all relationships established
     * @throws PaaAlreadyExistsException if a PAA with the generated ID and version already exists
     */
    Paa createWithRelationships(PaaRequestGlobal request) throws PaaAlreadyExistsException, AcNotFound, PaaCreationLimitExceededException;

    /**
     * Delete a Paa entity by its composite ID components.
     *
     * @param paaId  the first part of the composite ID (paa_id)
     * @param paaVer the second part of the composite ID (paa_ver)
     */
    void delete(Integer paaId, Integer paaVer) throws PaaNotFound, PaaSoftDelete, PaaInvalidStatusException;

    Paa update(Integer paaId, Integer paaVer, PaaRequestGlobal request) throws PaaNotFound, PaaInvalidStateException;

    /**
     * Updates a PAA by creating a new version with the updated data.
     * The old version is preserved in the database.
     * <p>
     * This method handles versioning:
     * - If any PAA field changes, a new PAA version is created
     * - If any Realisation is added, modified, or removed, a new PAA version is created
     * <p>
     *
     * @param paaId   the ID of the PAA to update
     * @param paaVer  the current version of the PAA
     * @param request the request containing the updated data
     * @return the newly created version of the PAA
     * @throws PaaNotFound if the PAA is not found or is deleted
     */
    Paa updateWithVersioning(Integer paaId, Integer paaVer, PaaRequestGlobal request) throws PaaNotFound;

    /**
     * Updates a PAA with versioning by automatically finding and using the latest version.
     * <p>
     * This method finds the latest version of the PAA with the given ID and then
     * calls the version-specific updateWithVersioning method.
     *
     * @param paaId   the ID of the PAA to update
     * @param request the request containing the updated data
     * @return the newly created version of the PAA if changes detected, otherwise the existing PAA
     * @throws PaaNotFound if no versions of the PAA exist or if all versions are deleted
     */
    Paa updateWithVersioning(Integer paaId, PaaRequestGlobal request) throws PaaNotFound;

    Paa updateStatus(Integer paaId, Integer paaVer, ProcessStatus status) throws PaaNotFound;

    Paa findByIdPaa(Integer paaId, Integer paaVer) throws PaaNotFound;

    void deleteTest(Integer paaId, Integer paaVer) throws PaaNotFound, PaaSoftDelete;

    /**
     * find the list of paas with Ac id
     * */
    Page<Paa> findPaasByAcId(Integer acId, Pageable pageable) throws AcNotFound;

    /**
     * Retrieves all versions of an Paa entity by its ID, including only non-deleted documents,
     * in Paa format with pagination.
     *
     * @param paaId   ID of the Paa entity
     * @param pageable Pagination and sorting configuration
     * @return Page of Paa representing all versions for the given ID
     * @throws PaaIdNotFound if no Paa entities are found for the ID
     */
    Page<Paa> findAllVersionsById(Integer paaId, Pageable pageable) throws PaaIdNotFound;

    List<Paa> findCurrentYearPaasByUser() throws AcNotFound, PaaNotFound;

    List<Ac> getAcsWithoutPaaForCurrentYear() throws AcNotFound;



}