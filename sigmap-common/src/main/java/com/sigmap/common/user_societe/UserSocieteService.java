package com.sigmap.common.user_societe;

import com.sigmap.common.keycloak_website_admin.dto.UserResquest;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.user_societe.exception.UserSocieteNotFound;
import com.sigmap.common.user_societe.request.CreateNewNonResponsableRequest;
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
     * @param userSociete the specification to use
     * @param pageable the pageable to use
     * @return a page of UserSociete
     */
    Page<UserSociete> findAll(Specification<UserSociete> userSociete, Pageable pageable);

    public List<UserSociete> findAllList(Specification<UserSociete> spec);
    /**
     * Return a page of deleted UserSociete based on the given specification and pageable.
     *
     * @param userSociete the specification to use
     * @param pageable the pageable to use
     * @return a page of UserSociete
     */


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
     * @param userId and societeId are  the ids of the UserSociete to return
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
    UserSociete createNonResponsableUser(UserResquest request, String responsableId) throws SocieteNotFound;
    /**
     * update Com.
     *
     * @param request the request to update the com from
     * @return the updated com
     */
    UserSociete update(String userId,UserSocieteRequest request,String responsableId) throws SocieteNotFound ,UserSocieteNotFound;
    /**
     * Delete a UserSociete.
     *
     *     * @param userId AND societeId  ARE the ids of the UserSociete  to delete
     */
    void delete(String userId,Integer societeId) ;

    List<UserSociete> findBySocieteId(Integer societeId) throws SocieteNotFound;

    UserSociete findByUserId(String userId) throws UserSocieteNotFound;

    UserSociete makeUserSocieteResponsable(String userId, Integer societeId) throws SocieteNotFound;
    UserSociete toggleUserSocieteStatus(String userId, Integer societeId) throws UserSocieteNotFound;

}