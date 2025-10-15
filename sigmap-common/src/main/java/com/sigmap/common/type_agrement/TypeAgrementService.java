package com.sigmap.common.type_agrement;


import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import com.sigmap.common.type_agrement.exception.TypeAgrementNotFound;
import com.sigmap.common.type_agrement.request.TypeAgrementRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface TypeAgrementService {
    List<TypeAgrement> findAll();

    /**
     * Return a page of TypeAgrement based on the given specification and pageable.
     *
     * @param TypeAgrement the specification to use
     * @param pageable the pageable to use
     * @return a page of TypeAgrement
     */
    Page<TypeAgrement> findAll(Specification<TypeAgrement> typeAgrement, Pageable pageable);

    /**
     * Return a page of deleted TypeAgrement based on the given specification and pageable.
     *
     * @param TypeAgrement the specification to use
     * @param pageable the pageable to use
     * @return a page of TypeAgrement
     */
    Page<TypeAgrement> findAllDeleted(Specification<TypeAgrement> typeAgrement, Pageable pageable);

    /**
     * Return a page of deleted TypeAgrement based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of TypeAgrement
     */
    Page<TypeAgrement> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted TypeAgrement with the given id.
     *
     * @param id the id of the TypeAgrement to return
     * @return the TypeAgrement with the given id
     */
    Optional<TypeAgrement> findDeletedById(Integer id);

    /**
     * Return the TypeAgrement with the given specification.
     *
     * @param specification the specification to use
     * @return the TypeAgrement with the given specification
     */
    Optional<TypeAgrement> findOne(Specification<TypeAgrement> specification);

    /**
     * Return the TypeAgrement with the given id.
     *
     * @param id the id of the TypeAgrement to return
     * @return the TypeAgrement with the given id
     */
    TypeAgrement findById(Integer id) throws TypeAgrementNotFound;

    /**
     * Save a TypeAgrement.
     *
     * @param TypeAgrement the TypeAgrement to save
     * @return the saved TypeAgrement
     */
    TypeAgrement save(TypeAgrement typeAgrement);

    /**
     * Create a TypeAgrement.
     *
     * @param request the request to create the TypeAgrement from
     * @return the created TypeAgrement
     */
    TypeAgrement create(TypeAgrementRequest request) throws RefTypeNotFound, RefParamNotFound;

    /**
     * update TypeAgrement.
     *
     * @param request the request to create the TypeAgrement from
     * @return the updated TypeAgrement
     */
    TypeAgrement update(Integer id,TypeAgrementRequest request) throws TypeAgrementNotFound, RefTypeNotFound, RefParamNotFound;

    /**
     * Delete a TypeAgrement.
     *
     * @param id the id of the TypeAgrement to delete
     */
    void delete(Integer id) throws TypeAgrementNotFound;
}
