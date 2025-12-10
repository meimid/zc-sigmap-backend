package com.sigmap.common.compte_bancaire;
import com.sigmap.common.compte_bancaire.exception.CompteBancaireNotFound;
import com.sigmap.common.compte_bancaire.request.CompteBancaireRequest;
import com.sigmap.common.compte_bancaire.request.CompteBancaireRequestDTO;
import com.sigmap.common.compte_bancaire.request.CompteBancaireUnderSocieteRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.exception.SocieteNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Optional;

public interface CompteBancaireService {
    List<CompteBancaire> findAll();

    /**
     * Return a page of CompteBancaire based on the given specification and pageable.
     *
     * @param compteBancaire the specification to use
     * @param pageable the pageable to use
     * @return a page of CompteBancaire
     */
    Page<CompteBancaire> findAll(Specification<CompteBancaire> compteBancaire, Pageable pageable);

    /**
     * Return a page of deleted CompteBancaire based on the given specification and pageable.
     *
     * @param compteBancaire the specification to use
     * @param pageable the pageable to use
     * @return a page of CompteBancaire
     */
    Page<CompteBancaire> findAllDeleted(Specification<CompteBancaire> compteBancaire, Pageable pageable);

    /**
     * Return a page of deleted CompteBancaire based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of CompteBancaire
     */
    Page<CompteBancaire> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted CompteBancaire with the given id.
     *
     * @param id the id of the CompteBancaire to return
     * @return the CompteBancaire with the given id
     */
    Optional<CompteBancaire> findDeletedById(Integer id);

    /**
     * Return the CompteBancaire with the given specification.
     *
     * @param specification the specification to use
     * @return the CompteBancaire with the given specification
     */
    Optional<CompteBancaire> findOne(Specification<CompteBancaire> specification);

    /**
     * Return the CompteBancaire with the given id.
     *
     * @param id the id of the CompteBancaire to return
     * @return the CompteBancaire with the given id
     */
    CompteBancaire findById(Integer id) throws CompteBancaireNotFound;

    /**
     * Save a CompteBancaire.
     *
     * @param compteBancaire the CompteBancaire to save
     * @return the saved CompteBancaire
     */
    CompteBancaire save(CompteBancaire compteBancaire);

    /**
     * Create a CompteBancaire.
     *
     * @param request the request to create the CompteBancaire from
     * @return the created CompteBancaire
     */
    CompteBancaire create(CompteBancaireRequestDTO request)throws RefParamNotFound, SocieteNotFound;
    /**
     * update CompteBancaire.
     *
     * @param request the request to update the CompteBancaire from
     * @return the updated CompteBancaire
     */
    CompteBancaire update(Integer id,CompteBancaireRequestDTO request) throws CompteBancaireNotFound, RefParamNotFound, SocieteNotFound;
    /**
     * Delete a CompteBancaire.
     *
     * @param id the id of the CompteBancaire to delete
     */
    void delete(Integer id);

    /**
     * Create a CompteBancaire.
     *
     * @param request the request to create the CompteBancaireUnderSociete from
     * @return the created CompteBancaire
     */
    CompteBancaire createUnderSociete(Integer societeId,CompteBancaireUnderSocieteRequest request)throws RefParamNotFound, SocieteNotFound;

    Page <CompteBancaire> findCompteBancaireListBySocieteId(Integer societeId,Pageable pageable) throws SocieteNotFound;

}
