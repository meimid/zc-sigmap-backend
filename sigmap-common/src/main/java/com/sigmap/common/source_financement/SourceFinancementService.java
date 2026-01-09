package com.sigmap.common.source_financement;

import com.sigmap.common.source_financement.exceptions.SourceFinancementNotFound;
import com.sigmap.common.source_financement.request.SourceFinancementRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link SourceFinancement}.
 * The service provides methods to retrieve and manage SourceFinancements.
 *
 * <p>
 * The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link SourceFinancementSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see SourceFinancementSpecifications
 * @see Specification
 */
public interface SourceFinancementService {
    List<SourceFinancement> findAll();

    /**
     * Return a page of SourceFinancements based on the given specification and
     * pageable.
     *
     * @param spec     the specification to use
     * @param pageable the pageable to use
     * @return a page of SourceFinancements
     */
    Page<SourceFinancement> findAll(Specification<SourceFinancement> spec, Pageable pageable);

    /**
     * Return a page of deleted SourceFinancements based on the given specification
     * and pageable.
     *
     * @param spec     the specification to use
     * @param pageable the pageable to use
     * @return a page of SourceFinancements
     */
    Page<SourceFinancement> findAllDeleted(Specification<SourceFinancement> spec, Pageable pageable);

    /**
     * Return a page of deleted SourceFinancements based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of SourceFinancements
     */
    Page<SourceFinancement> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted SourceFinancement with the given id.
     *
     * @param id the id of the SourceFinancement to return
     * @return the SourceFinancement with the given id
     */
    Optional<SourceFinancement> findDeletedById(Integer id);

    /**
     * Return the SourceFinancement with the given specification.
     *
     * @param specification the specification to use
     * @return the SourceFinancement with the given specification
     */
    Optional<SourceFinancement> findOne(Specification<SourceFinancement> specification);

    /**
     * Return the SourceFinancement with the given id.
     *
     * @param id the id of the SourceFinancement to return
     * @return the SourceFinancement with the given id
     */
    SourceFinancement findById(Integer id) throws SourceFinancementNotFound;

    /**
     * Save a SourceFinancement.
     *
     * @param sourceFinancement the SourceFinancement to save
     * @return the saved SourceFinancement
     */
    SourceFinancement save(SourceFinancement sourceFinancement);

    /**
     * Create a SourceFinancement.
     *
     * @param request the request to create the SourceFinancement from
     * @return the created SourceFinancement
     */
    SourceFinancement create(SourceFinancementRequest request);

    /**
     * Update SourceFinancement.
     *
     * @param request the request to update the SourceFinancement from
     * @return the updated SourceFinancement
     */
    SourceFinancement update(Integer id, SourceFinancementRequest request) throws SourceFinancementNotFound;

    /**
     * Delete a SourceFinancement.
     *
     * @param id the id of the SourceFinancement to delete
     */
    void delete(Integer id) throws SourceFinancementNotFound;
}
