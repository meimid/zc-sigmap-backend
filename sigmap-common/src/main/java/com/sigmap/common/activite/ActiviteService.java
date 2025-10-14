package com.sigmap.common.activite;


import com.sigmap.common.activite.exception.ActiviteNotFound;
import com.sigmap.common.activite.request.ActiviteRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface ActiviteService {
    List<Activite> findByCategoriesActiviteId(Integer typeMarcherId) throws RefParamNotFound;
    List<Activite> findAll();

    /**
     * Return a page of Activite based on the given specification and pageable.
     *
     * @param Activite the specification to use
     * @param pageable the pageable to use
     * @return a page of Activite
     */
    Page<Activite> findAll(Specification<Activite> Activite, Pageable pageable);

    /**
     * Return a page of deleted Activite based on the given specification and pageable.
     *
     * @param Activite the specification to use
     * @param pageable the pageable to use
     * @return a page of Activite
     */
    Page<Activite> findAllDeleted(Specification<Activite> Activite, Pageable pageable);

    /**
     * Return a page of deleted Activite based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Activite
     */
    Page<Activite> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Activite with the given id.
     *
     * @param id the id of the Activite to return
     * @return the Activite with the given id
     */
    Optional<Activite> findDeletedById(Integer id);

    /**
     * Return the Activite with the given specification.
     *
     * @param specification the specification to use
     * @return the Activite with the given specification
     */
    Optional<Activite> findOne(Specification<Activite> specification);

    /**
     * Return the Activite with the given id.
     *
     * @param id the id of the Activite to return
     * @return the Activite with the given id
     */
    Activite findById(Integer id) throws ActiviteNotFound;

    /**
     * Save a Activite.
     *
     * @param Activite the Activite to save
     * @return the saved Activite
     */
    Activite save(Activite Activite);

    /**
     * Create a Activite.
     *
     * @param request the request to create the Activite from
     * @return the created Activite
     */
    Activite create(ActiviteRequest request) throws  RefParamNotFound;

    /**
     * update Activite.
     *
     * @param request the request to create the Activite from
     * @return the updated Activite
     */
    Activite update(Integer id,ActiviteRequest request) throws ActiviteNotFound, RefParamNotFound;

    /**
     * Delete a Activite.
     *
     * @param id the id of the Activite to delete
     */
    void delete(Integer id) throws ActiviteNotFound;

}
