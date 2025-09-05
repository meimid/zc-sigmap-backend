package com.sigmap.common.lots;

import com.sigmap.common.lots.exception.LotNotFound;
import com.sigmap.common.lots.exception.UpdateDeletedLot;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import com.sigmap.common.lots.request.LotRequest;
import com.sigmap.common.lots.request.LotBaseRequestDto;
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
    Lot findById(Integer lotsId, Integer lotsVer) throws LotNotFound;

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
    Lot create(LotRequest request) throws RealisationNotFoundException;

    /**
     * Delete a Lot entity by its composite ID components.
     *
     * @param lotsId  the first part of the composite ID (lots_id)
     * @param lotsVer the second part of the composite ID (lots_ver)
     */
    void delete(Integer lotsId, Integer lotsVer);

    Lot update(Integer lotId, Integer lotVer, LotRequest request) throws LotNotFound, UpdateDeletedLot;

    /**
     * Updates a lot by creating a new version with the updated data if the description changes.
     * The old version is preserved in the database. If only relationships change,
     * no new version is created.
     *
     * @param lotId the ID of the lot to update
     * @param lotVer the current version of the lot
     * @param request the request containing the updated data
     * @return the newly created version of the lot if description changes, otherwise the existing lot
     * @throws RuntimeException if the lot is not found or is deleted
     */
    Lot updateWithVersioning(Integer lotId, Integer lotVer, LotBaseRequestDto request);
    
    /**
     * Updates a lot by creating a new version with the updated data if the description changes.
     * Automatically finds and uses the latest version of the lot.
     * The old version is preserved in the database. If only relationships change,
     * no new version is created.
     *
     * @param lotId the ID of the lot to update
     * @param request the request containing the updated data
     * @return the newly created version of the lot if description changes, otherwise the existing lot
     * @throws RuntimeException if the lot is not found or if all versions are deleted
     */
    Lot updateWithVersioning(Integer lotId, LotBaseRequestDto request);
}