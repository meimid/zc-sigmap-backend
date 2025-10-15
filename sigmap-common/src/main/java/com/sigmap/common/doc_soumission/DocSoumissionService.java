package com.sigmap.common.doc_soumission;


import com.sigmap.common.doc_soumission.exception.DocSoumissionNotFound;
import com.sigmap.common.doc_soumission.request.DocSoumissionRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocSoumissionService {
    List<DocSoumission> findAllList(Specification<DocSoumission> docSoumission);

    List<DocSoumission> findAll();

    /**
     * Return a page of DocSoumission based on the given specification and pageable.
     *
     * @param docSoumission the specification to use
     * @param pageable the pageable to use
     * @return a page of DocSoumission
     */
    Page<DocSoumission> findAll(Specification<DocSoumission> docSoumission, Pageable pageable);


    /**
     * Return the DocSoumission with the given specification.
     *
     * @param specification the specification to use
     * @return the DocSoumission with the given specification
     */
    Optional<DocSoumission> findOne(Specification<DocSoumission> specification);

    /**
     * Return the DocSoumission with the given id.
     *
     * @param id the id of the DocSoumission to return
     * @return the DocSoumission with the given id
     */
    DocSoumission findById(Integer id) throws DocSoumissionNotFound;

    /**
     * Save a DocSoumission.
     *
     * @param docSoumission the DocSoumission to save
     * @return the saved DocSoumission
     */
    DocSoumission save(DocSoumission docSoumission);

    /**
     * Create a DocSoumission.
     *
     * @param request the request to create the DocSoumission from
     * @return the created DocSoumission
     */
    DocSoumission create(DocSoumissionRequest request) ;

    /**
     * update DocSoumission.
     *
     * @param request the request to create the DocSoumission from
     * @return the updated DocSoumission
     */
    DocSoumission update(Integer id,DocSoumissionRequest request) throws DocSoumissionNotFound;

    /**
     * Delete a DocSoumission.
     *
     * @param id the id of the DocSoumission to delete
     */
    void delete(Integer id) throws DocSoumissionNotFound;

    List<DocSoumission> findNotExistingDocSoumissionOfDacDocSoumissionByDacId(Integer dacId);
    List<DocSoumission> findExistingDocSoumissionOfDacDocSoumissionByDacId(Integer dacId);

}
