package com.sigmap.common.realisations;


import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.request.RealisationRequest; // Assuming a DTO exists
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Realisation}.
 * The service provides methods to retrieve and manage Realisation entities.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link RealisationSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see RealisationSpecifications
 * @see Specification
 */
public interface RealisationService {
    List<Realisation> findAll();

    /**
     * Return a page of Realisation entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Realisation entities
     */
    Page<Realisation> findAll(Specification<Realisation> spec, Pageable pageable);
    /**
     * Return a page of deleted Realisation entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Realisation entities
     */
    /**
     * Return a page of deleted Realisation entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Realisation entities
     */
    Page<Realisation> findAllDeleted(Specification<Realisation> spec, Pageable pageable);

    /**
     * Return a page of deleted Realisation entities based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Realisation entities
     */
    Page<Realisation> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Realisation entity with the given composite ID components.
     *
     * @param realisationId the first part of the composite ID (realisation_id)
     * @param realisationVer the second part of the composite ID (realisation_ver)
     * @return the Realisation entity with the given ID components
     */
    Optional<Realisation> findDeletedById(Integer realisationId, Integer realisationVer);
    /**
     * Return the Realisation entity with the given specification.
     *
     * @param specification the specification to use
     * @return the Realisation entity with the given specification
     */
    /**
     * Return the Realisation entity with the given specification.
     *
     * @param specification the specification to use
     * @return the Realisation entity with the given specification
     */
    Optional<Realisation> findOne(Specification<Realisation> specification);

    /**
     * Return the Realisation entity with the given composite ID components.
     *
     * @param realisationId the first part of the composite ID (realisation_id)
     * @param realisationVer the second part of the composite ID (realisation_ver)
     * @return the Realisation entity with the given ID components
     */
    Optional<Realisation> findById(Integer realisationId, Integer realisationVer);

    /**
     * Save a Realisation entity.
     *
     * @param realisation the Realisation entity to save
     * @return the saved Realisation entity
     */
    Realisation save(Realisation realisation);

    /**
     * Create a Realisation entity.
     *
     * @param request the request to create the Realisation entity from
     * @return the created Realisation entity
     */
    Realisation create(RealisationRequest request) throws RealisationAlreadyExistsException;

    /**
     * Update an existing Realisation entity identified by its composite ID components.
     *
     * @param realisationId the first part of the composite ID (realisation_id)
     * @param realisationVer the second part of the composite ID (realisation_ver)
     * @param request the request object containing the updated Realisation details
     * @return the updated Realisation entity
     * @throws RealisationNotFoundException if the Realisation entity with the given ID components is not found
     */
    Realisation update(Integer realisationId, Integer realisationVer, RealisationRequest request) throws RealisationNotFoundException;

    /**
     * Delete a Realisation entity by its composite ID components.
     *
     * @param realisationId the first part of the composite ID (realisation_id)
     * @param realisationVer the second part of the composite ID (realisation_ver)
     */
    void delete(Integer realisationId, Integer realisationVer);
}