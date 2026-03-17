package com.sigmap.common.com;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.com.exception.CNCMPPNotFound;
import com.sigmap.common.com.exception.ComHasAssociatedAc;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.dac.response.ParticipantResponse;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.response.UserComSoftResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ComService {
    List<Com> findAll();
    List<Com> findAll(Specification<Com> specification);

    /**
     * Return a page of Com based on the given specification and pageable.
     *
     * @param com the specification to use
     * @param pageable the pageable to use
     * @return a page of Com
     */
    Page<Com> findAll(Specification<Com> com, Pageable pageable);

    /**
     * Return a page of deleted Com based on the given specification and pageable.
     *
     * @param com the specification to use
     * @param pageable the pageable to use
     * @return a page of Com
     */
    Page<Com> findAllDeleted(Specification<Com> com, Pageable pageable);

    /**
     * Return a page of deleted Com based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Com
     */
    Page<Com> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Com with the given id.
     *
     * @param id the id of the Com to return
     * @return the Com with the given id
     */
    Optional<Com> findDeletedById(Integer id);

    /**
     * Return the Com with the given specification.
     *
     * @param specification the specification to use
     * @return the Com with the given specification
     */
    Optional<Com> findOne(Specification<Com> specification);

    /**
     * Return the Com with the given id.
     *
     * @param id the id of the Com to return
     * @return the Com with the given id
     */
    Com findById(Integer id) throws ComNotFound;

    /**
     * Save a Com.
     *
     * @param com the Com to save
     * @return the saved Com
     */
    Com save(Com com);

    /**
     * Create a Com.
     *
     * @param request the request to create the Com from
     * @return the created Com
     */
    Com create(ComRequest request) throws AcNotFound, RefParamNotFound, ComNotFound, IOException;
    /**
     * update Com.
     *
     * @param request the request to update the com from
     * @return the updated com
     */
    Com update(Integer id,ComRequest request) throws ComNotFound, RefParamNotFound, AcNotFound, AgpmDefaultStatus, DocNotFound, IOException;
    /**
     * Delete a Com.
     *
     * @param id the id of the Com to delete
     */
    void delete(Integer id) throws ComHasAssociatedAc, ComNotFound;

    public UserCom makeUserResponsable (String userId,Integer ComId);

    List<Com> getComsWithTypeCPMP() throws RefParamNotFound;

    List<Com> getComsWithTypeCNCMP() throws RefParamNotFound;

    Com getCommissionDeTutelle(Integer acId) throws ComNotFound,AcNotFound;

    Com getCommissionPrivee(Integer acId) throws ComNotFound,AcNotFound;
    List<Com>getAllCommissionsDeTutelles(Integer acId);
    List<Com> getAllSpecialCommissions();
    Com findCNCMPP() throws CNCMPPNotFound;

    /**
     * Get list of CPMP commission members for a given AC.
     *
     * @param acId The AC (Autorité Contractante) ID
     * @return List of CPMP commission members/participants
     * @throws AcNotFound if AC doesn't exist
     */
    List<UserComSoftResponse> getCpmpMembersByAc(Integer acId)
            throws AcNotFound;

    List<UserComSoftResponse> findAllComMemebersByComId(Integer comId);

}
