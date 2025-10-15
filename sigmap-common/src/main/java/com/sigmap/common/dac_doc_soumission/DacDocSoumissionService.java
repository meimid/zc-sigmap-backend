package com.sigmap.common.dac_doc_soumission;

import com.sigmap.common.dac_doc_soumission.exception.DacDocSoumissionNotFound;
import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionCreateRequest;
import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionUpdateRequest;
import com.sigmap.common.doc_soumission.DocSoumission;
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
    DacDocSoumission create(DacDocSoumissionCreateRequest request) ;

    /**
     * update DacDocSoumission.
     *
     * @param request the request to create the DacDocSoumission from
     * @return the updated DacDocSoumission
     */
    DacDocSoumission update(Integer dacId, Integer docSoumissionId, DacDocSoumissionUpdateRequest request) throws DacDocSoumissionNotFound;

    /**
     * Delete a DacDocSoumission.
     *
     * @param dacId and docSoumissionId  the ids of the DacDocSoumission to delete
     */
    void delete(Integer dacId, Integer docSoumissionId) throws DacDocSoumissionNotFound;
    List<DocSoumission> findNotExistingDocSoumissionOfDacDocSoumissionByDacId(Integer dacId);
    List<DocSoumission> findExistingDocSoumissionOfDacDocSoumissionByDacId(Integer dacId);

}
