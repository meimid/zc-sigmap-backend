package com.sigmap.common.lots;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import com.sigmap.common.lots.request.LotRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

/**
 * Service interface for managing {@link Lot}.
 * The service provides methods to retrieve and manage Lot entities.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link LotSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see LotSpecifications
 * @see Specification
 */
public interface LotService {
    List<Lot> findAll();

    /**
     * Return a page of Lot entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Lot entities
     */
    Page<Lot> findAll(Specification<Lot> spec, Pageable pageable);

    /**
     * Return a page of deleted Lot entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Lot entities
     */
    Page<Lot> findAllDeleted(Specification<Lot> spec, Pageable pageable);

    /**
     * Return a page of deleted Lot entities based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Lot entities
     */
    Page<Lot> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Lot entity with the given composite ID.
     *
     * @param id the composite ID (LotsId) of the Lot entity to return
     * @return the Lot entity with the given ID
     */
    Optional<Lot> findDeletedById(LotsId id);

    /**
     * Return the Lot entity with the given specification.
     *
     * @param specification the specification to use
     * @return the Lot entity with the given specification
     */
    Optional<Lot> findOne(Specification<Lot> specification);

    /**
     * Return the Lot entity with the given composite ID.
     *
     * @param id the composite ID (LotsId) of the Lot entity to return
     * @return the Lot entity with the given ID
     */
    Optional<Lot> findById(LotsId id);

    /**
     * Save a Lot entity.
     *
     * @param lot the Lot entity to save
     * @return the saved Lot entity
     */
    Lot save(Lot lot);

    /**
     * Create a Lot entity.
     *
     * @param request the request to create the Lot entity from
     * @return the created Lot entity
     */
    Lot create(LotRequest request);

    /**
     * Delete a Lot entity.
     *
     * @param id the composite ID (LotsId) of the Lot entity to delete
     */
    void delete(LotsId id);
}