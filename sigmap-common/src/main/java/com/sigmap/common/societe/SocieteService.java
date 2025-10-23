package com.sigmap.common.societe;


import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.exception.SocieteHasAssociatedCompteBancaire;
import com.sigmap.common.societe.exception.SocieteHasAssociatedSocieteAddress;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.request.SocieteGlobalRequest;
import com.sigmap.common.societe.request.SocieteRequest;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface SocieteService {
    List<Societe> findAll();

    /**
     * Return a page of Societe based on the given specification and pageable.
     *
     * @param societe the specification to use
     * @param pageable the pageable to use
     * @return a page of Societe
     */
    Page<Societe> findAll(Specification<Societe> societe, Pageable pageable);

    /**
     * Return a page of deleted societe based on the given specification and pageable.
     *
     * @param societe the specification to use
     * @param pageable the pageable to use
     * @return a page of Societe
     */
    Page<Societe> findAllDeleted(Specification<Societe> societe, Pageable pageable);

    /**
     * Return a page of deleted Societe based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Societe
     */
    Page<Societe> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Societe with the given id.
     *
     * @param id the id of the Societe to return
     * @return the Societe with the given id
     */
    Optional<Societe> findDeletedById(Integer id);

    /**
     * Return the Societe with the given specification.
     *
     * @param specification the specification to use
     * @return the Societe with the given specification
     */
    Optional<Societe> findOne(Specification<Societe> specification);

    /**
     * Return the Societe with the given id.
     *
     * @param id the id of the Societe to return
     * @return the Societe with the given id
     */
    Societe findById(Integer id)throws SocieteNotFound;

    /**
     * Save a Societe.
     *
     * @param societe the Societe to save
     * @return the saved Societe
     */
    Societe save(Societe societe);

    /**
     * Create a Societe.
     *
     * @param request the request to create the Societe from
     * @return the created Societe
     */
    Societe create(SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound;
    /**
     * update Com.
     *
     * @param request the request to update the com from
     * @return the updated com
     */
    Societe update(Integer id,SocieteRequest request) throws RefParamNotFound, SocieteNotFound;
    /**
     * Delete a Societe.
     *
     * @param id the id of the Societe to delete
     */
    void delete(Integer id) throws SocieteNotFound;

    Page<Societe> findAllWithStatus(Specification<Societe> societe, SocieteStatus status, Pageable pageable);

    Societe updateStatus(Integer societeId, SocieteStatus status) throws SocieteNotFound;

}
