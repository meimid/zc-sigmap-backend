package com.sigmap.common.lots;

import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.request.PaaRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import com.sigmap.common.lots.request.LotRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
public interface LotService {
    List<Lot> findAll();

    /**
     * Return a page of Lot entities based on the given specification and pageable.
     *
     * @param spec     the specification to use
     * @param pageable the pageable to use
     * @return a page of Lot entities
     */
    Page<Lot> findAll(Specification<Lot> spec, Pageable pageable);

    /**
     * Return a page of deleted Lot entities based on the given specification and pageable.
     *
     * @param spec     the specification to use
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
     * Return the deleted Lot entity with the given composite ID components.
     *
     * @param lotsId  the first part of the composite ID (lots_id)
     * @param lotsVer the second part of the composite ID (lots_ver)
     * @return the Lot entity with the given ID components
     */
    Optional<Lot> findDeletedById(Integer lotsId, Integer lotsVer);

    /**
     * Return the Lot entity with the given specification.
     *
     * @param specification the specification to use
     * @return the Lot entity with the given specification
     */
    Optional<Lot> findOne(Specification<Lot> specification);

    /**
     * Return the Lot entity with the given composite ID components.
     *
     * @param lotsId  the first part of the composite ID (lots_id)
     * @param lotsVer the second part of the composite ID (lots_ver)
     * @return the Lot entity with the given ID components
     */
    Optional<Lot> findById(Integer lotsId, Integer lotsVer);

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
     * Delete a Lot entity by its composite ID components.
     *
     * @param lotsId  the first part of the composite ID (lots_id)
     * @param lotsVer the second part of the composite ID (lots_ver)
     */
    void delete(Integer lotsId, Integer lotsVer);

    Lot update(Integer lotId, Integer lotVer, LotRequest request);

}