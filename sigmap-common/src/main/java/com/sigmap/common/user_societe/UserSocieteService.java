package com.sigmap.common.user_societe;

import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.user_societe.exception.UserSocieteNotFound;
import com.sigmap.common.user_societe.request.UserSocieteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface UserSocieteService {
    List<UserSociete> findAll();

    /**
     * Return a page of UserSociete based on the given specification and pageable.
     *
     * @param UserSociete the specification to use
     * @param pageable the pageable to use
     * @return a page of UserSociete
     */
    Page<UserSociete> findAll(Specification<UserSociete> UserSociete, Pageable pageable);

    /**
     * Return a page of deleted UserSociete based on the given specification and pageable.
     *
     * @param UserSociete the specification to use
     * @param pageable the pageable to use
     * @return a page of UserSociete
     */
    Page<UserSociete> findAllDeleted(Specification<UserSociete> UserSociete, Pageable pageable);

    /**
     * Return a page of deleted UserSociete based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of UserSociete
     */
    Page<UserSociete> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted UserSociete with the given id.
     *
     * @param userId AND societeId  ARE the ids of the UserSociete to return
     * @return the UserSociete with the given id
     */
    Optional<UserSociete> findDeletedById(String userId,Integer societeId);

    /**
     * Return the UserSociete with the given specification.
     *
     * @param specification the specification to use
     * @return the UserSociete with the given specification
     */
    Optional<UserSociete> findOne(Specification<UserSociete> specification);

    /**
     * Return the UserSociete with the given id.
     *
     * @param id the id of the UserSociete to return
     * @return the UserSociete with the given id
     */
    UserSociete findById(String userId,Integer societeId)throws UserSocieteNotFound;

    /**
     * Save a UserSociete.
     *
     * @param userSociete the UserSociete to save
     * @return the saved UserSociete
     */
    UserSociete save(UserSociete userSociete);

    /**
     * Create a UserSociete.
     *
     * @param request the request to create the UserSociete from
     * @return the created UserSociete
     */
    UserSociete create(UserSocieteRequest request) throws SocieteNotFound;
    /**
     * update Com.
     *
     * @param request the request to update the com from
     * @return the updated com
     */
    UserSociete update(Integer id,UserSocieteRequest request) throws SocieteNotFound ,UserSocieteNotFound;
    /**
     * Delete a UserSociete.
     *
     * @param id the id of the UserSociete to delete
     */
    void delete(Integer id) ;

    List<UserSociete> findBySocieteId(Integer societeId);

    UserSociete findByUserId(String userId);
}