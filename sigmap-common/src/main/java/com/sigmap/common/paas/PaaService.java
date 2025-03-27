package com.sigmap.common.paas;

import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.ref_params.request.RefParamRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Paa}.
 * The service provides methods to retrieve and manage Paa entities.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link PaaSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see PaaSpecifications
 * @see Specification
 */
public interface PaaService {
    List<Paa> findAll();

    /**
     * Return a page of Paa entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Paa entities
     */
    Page<Paa> findAll(Specification<Paa> spec, Pageable pageable);

    /**
     * Return a page of deleted Paa entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Paa entities
     */
    Page<Paa> findAllDeleted(Specification<Paa> spec, Pageable pageable);

    /**
     * Return a page of deleted Paa entities based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Paa entities
     */
    Page<Paa> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Paa entity with the given composite ID components.
     *
     * @param paaId the first part of the composite ID (paa_id)
     * @param paaVer the second part of the composite ID (paa_ver)
     * @return the Paa entity with the given ID components
     */
    Optional<Paa> findDeletedById(Integer paaId, Integer paaVer);

    /**
     * Return the Paa entity with the given specification.
     *
     * @param specification the specification to use
     * @return the Paa entity with the given specification
     */
    Optional<Paa> findOne(Specification<Paa> specification);

    /**
     * Return the Paa entity with the given composite ID components.
     *
     * @param paaId the first part of the composite ID (paa_id)
     * @param paaVer the second part of the composite ID (paa_ver)
     * @return the Paa entity with the given ID components
     */
    Optional<Paa> findById(Integer paaId, Integer paaVer);

    /**
     * Save a Paa entity.
     *
     * @param paa the Paa entity to save
     * @return the saved Paa entity
     */
    Paa save(Paa paa);

    /**
     * Create a Paa entity.
     *
     * @param request the request to create the Paa entity from
     * @return the created Paa entity
     */
    Paa create(PaaRequest request);

    /**
     * Delete a Paa entity by its composite ID components.
     *
     * @param paaId the first part of the composite ID (paa_id)
     * @param paaVer the second part of the composite ID (paa_ver)
     */
    void delete(Integer paaId, Integer paaVer);
}