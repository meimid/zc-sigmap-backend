package com.sigmap.common.com;
import com.sigmap.common.ac.exception.AcDeleted;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.com.exception.ComDeleted;
import com.sigmap.common.com.exception.ComHasAssociatedAc;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.com.request.ComRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.user_com.UserCom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface ComService {
    List<Com> findAll();

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
    Com create(ComRequest request) throws AcNotFound, RefParamNotFound;
    /**
     * update Com.
     *
     * @param request the request to update the com from
     * @return the updated com
     */
    Com update(Integer id,ComRequest request) throws ComNotFound, RefParamNotFound, AcNotFound, AgpmDefaultStatus;
    /**
     * Delete a Com.
     *
     * @param id the id of the Com to delete
     */
    void delete(Integer id) throws ComHasAssociatedAc;

    public UserCom makeUserResponsable (String userId,Integer ComId);
}
