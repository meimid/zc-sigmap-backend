package com.sigmap.common.dac_doc_soumission;

import com.sigmap.common.dac_doc_soumission.exception.DacDocSoumissionNotFound;
import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface DacDocSoumissionService {
    List<DacDocSoumission> findAllList(Specification<DacDocSoumission> dacDocSoumission);

    List<DacDocSoumission> findAll();

    /**
     * Return a page of DacDocSoumission based on the given specification and pageable.
     *
     * @param dacDocSoumission the specification to use
     * @param pageable the pageable to use
     * @return a page of DacDocSoumission
     */
    Page<DacDocSoumission> findAll(Specification<DacDocSoumission> dacDocSoumission, Pageable pageable);


    /**
     * Return the DacDocSoumission with the given specification.
     *
     * @param specification the specification to use
     * @return the DacDocSoumission with the given specification
     */
    Optional<DacDocSoumission> findOne(Specification<DacDocSoumission> specification);

    /**
     * Return the DacDocSoumission with the given id.
     *
     * @param dacId and @param docSoumissionId of the DacDocSoumission to return
     * @return the DacDocSoumission with the given ids
     */
    DacDocSoumission findByDacIdAndDocSoumissionId(Integer dacId,Integer docSoumissionId) throws DacDocSoumissionNotFound;

    /**
     * Save a DacDocSoumission.
     *
     * @param dacDocSoumission the DacDocSoumission to save
     * @return the saved DacDocSoumission
     */
    DacDocSoumission save(DacDocSoumission dacDocSoumission);

    /**
     * Create a DacDocSoumission.
     *
     * @param request the request to create the DacDocSoumission from
     * @return the created DacDocSoumission
     */
    DacDocSoumission create(DacDocSoumissionRequest request) ;

    /**
     * update DacDocSoumission.
     *
     * @param request the request to create the DacDocSoumission from
     * @return the updated DacDocSoumission
     */
    DacDocSoumission update( DacDocSoumissionRequest request) throws DacDocSoumissionNotFound;

    /**
     * Delete a DacDocSoumission.
     *
     * @param dacId and docSoumissionId  the ids of the DacDocSoumission to delete
     */
    void delete(Integer dacId, Integer docSoumissionId) throws DacDocSoumissionNotFound;
}
