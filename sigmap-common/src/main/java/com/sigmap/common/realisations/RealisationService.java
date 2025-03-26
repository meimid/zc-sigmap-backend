package com.sigmap.common.realisations;


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
    Page<Realisation> findAllDeleted(Specification<Realisation> spec, Pageable pageable);

    /**
     * Return a page of deleted Realisation entities based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Realisation entities
     */
    Page<Realisation> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Realisation entity with the given composite ID.
     *
     * @param id the composite ID (RealisationId) of the Realisation entity to return
     * @return the Realisation entity with the given ID
     */
    Optional<Realisation> findDeletedById(RealisationId id);

    /**
     * Return the Realisation entity with the given specification.
     *
     * @param specification the specification to use
     * @return the Realisation entity with the given specification
     */
    Optional<Realisation> findOne(Specification<Realisation> specification);

    /**
     * Return the Realisation entity with the given composite ID.
     *
     * @param id the composite ID (RealisationId) of the Realisation entity to return
     * @return the Realisation entity with the given ID
     */
    Optional<Realisation> findById(RealisationId id);

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
    Realisation create(RealisationRequest request);

    /**
     * Delete a Realisation entity.
     *
     * @param id the composite ID (RealisationId) of the Realisation entity to delete
     */
    void delete(RealisationId id);
}