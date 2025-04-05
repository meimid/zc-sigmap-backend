package com.sigmap.common.ppms;

import com.sigmap.common.ppms.request.PpmRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Ppm}.
 * The service provides methods to retrieve and manage Ppms.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link PpmSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
 * @see PpmSpecifications
 * @see Specification
 */
public interface PpmService {
    List<Ppm> findAll();

    /**
     * Return a page of Ppms based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Ppms
     */
    Page<Ppm> findAll(Specification<Ppm> spec, Pageable pageable);

    /**
     * Return a page of deleted Ppms based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of Ppms
     */
    Page<Ppm> findAllDeleted(Specification<Ppm> spec, Pageable pageable);

    /**
     * Return a page of deleted Ppms based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Ppms
     */
    Page<Ppm> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Ppm with the given composite ID components.
     *
     * @param ppmId the first part of the composite ID (ppm_id)
     * @param ppmVer the second part of the composite ID (ppm_ver)
     * @return the Ppm with the given ID components
     */
    Optional<Ppm> findDeletedById(Integer ppmId, Integer ppmVer);

    /**
     * Return the Ppm with the given specification.
     *
     * @param specification the specification to use
     * @return the Ppm with the given specification
     */
    Optional<Ppm> findOne(Specification<Ppm> specification);

    /**
     * Return the Ppm with the given composite ID components.
     *
     * @param ppmId the first part of the composite ID (ppm_id)
     * @param ppmVer the second part of the composite ID (ppm_ver)
     * @return the Ppm with the given ID components
     */
    Optional<Ppm> findById(Integer ppmId, Integer ppmVer);

    /**
     * Save a Ppm.
     *
     * @param ppm the Ppm to save
     * @return the saved Ppm
     */
    Ppm save(Ppm ppm);

    /**
     * Create a Ppm.
     *
     * @param request the request to create the Ppm from
     * @return the created Ppm
     */
    Ppm create(PpmRequest request);

    /**
     * Delete a Ppm by its composite ID components.
     *
     * @param ppmId the first part of the composite ID (ppm_id)
     * @param ppmVer the second part of the composite ID (ppm_ver)
     */
    void delete(Integer ppmId, Integer ppmVer);
}