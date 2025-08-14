package com.sigmap.common.financement;

import com.sigmap.common.financement.exceptions.FinancementAlreadyExistsException;
import com.sigmap.common.financement.exceptions.FinancementNotFoundException;
import com.sigmap.common.financement.request.FinancementRequest;
import com.sigmap.common.financement.request.FinancementBaseRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Financement}.
 * The service provides methods to retrieve and manage Financement entities.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link FinancementSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see FinancementSpecifications
 * @see Specification
 */
public interface FinancementService {

    /**
     * Retrieves all Financement entities.
     *
     * @return a list of all Financement entities
     */
    List<Financement> findAll();

    /**
     * Retrieves a paginated list of Financement entities based on the given specification.
     *
     * @param spec the specification to filter the results
     * @param pageable the pagination information
     * @return a paginated list of Financement entities
     */
    Page<Financement> findAll(Specification<Financement> spec, Pageable pageable);

    /**
     * Retrieves all Financement entities based on the given specification.
     *
     * @param spec the specification to filter the results
     * @return a list of Financement entities matching the specification
     */
    List<Financement> findAll(Specification<Financement> spec);

    /**
     * Retrieves a Financement entity by its composite ID.
     *
     * @param id the composite ID of the Financement entity
     * @return an Optional containing the Financement entity if found, empty otherwise
     */
    Optional<Financement> findById(FinancementId id);

    /**
     * Saves a Financement entity.
     *
     * @param financement the Financement entity to save
     * @return the saved Financement entity
     */
    Financement save(Financement financement);

    /**
     * Creates a new Financement entity based on the given request.
     *
     * @param request the request containing the data for the new Financement entity
     * @return the created Financement entity
     * @throws FinancementAlreadyExistsException if a Financement with the same ID already exists
     */
    Financement create(FinancementRequest request) throws FinancementAlreadyExistsException;

    /**
     * Updates an existing Financement entity.
     *
     * @param id the composite ID of the Financement entity to update
     * @param request the request containing the updated data
     * @return the updated Financement entity
     * @throws FinancementNotFoundException if the Financement entity is not found
     */
    Financement update(FinancementId id, FinancementBaseRequestDto request) throws FinancementNotFoundException;

    /**
     * Deletes a Financement entity by its composite ID.
     *
     * @param id the composite ID of the Financement entity to delete
     * @throws FinancementNotFoundException if the Financement entity is not found
     */
    void deleteById(FinancementId id) throws FinancementNotFoundException;

    /**
     * Checks if a Financement entity exists with the given composite ID.
     *
     * @param id the composite ID to check
     * @return true if the entity exists, false otherwise
     */
    boolean existsById(FinancementId id);

    /**
     * Returns the total count of Financement entities.
     *
     * @return the total count of entities
     */
    long count();

    /**
     * Returns the count of Financement entities matching the given specification.
     *
     * @param spec the specification to filter the results
     * @return the count of entities matching the specification
     */
    long count(Specification<Financement> spec);
}
