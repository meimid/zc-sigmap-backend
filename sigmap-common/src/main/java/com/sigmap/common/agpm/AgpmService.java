package com.sigmap.common.agpm;

import com.sigmap.common.agpm.exceptions.AgpmAlreadyExistsException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Agpm}.
 * The service provides methods to retrieve and manage AGPM entities.
 *
 * <p>The service accepts {@link Specification} objects, which are used to
 * filter the results of the retrieval methods. The
 * {@link Specification} object is a criteria object that is used to
 * specify the conditions of the query. The
 * {@link Specification} object can be created using the
 * {@link  AgpmSpecifications} class, or it can be created manually
 * using the {@link Specification} interface.
 *
// * @see AgpmSpecications
 * @see Specification
 */
public interface AgpmService {
    List<Agpm> findAll();

    /**
     * Return a page of AGPM entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of AGPM responses
     */
    Page<Agpm> findAll(Specification<Agpm> spec, Pageable pageable);

    /**
     * Return a page of deleted AGPM entities based on the given specification and pageable.
     *
     * @param spec the specification to use
     * @param pageable the pageable to use
     * @return a page of AGPM responses
     */
    Page<Agpm> findAllDeleted(Specification<Agpm> spec, Pageable pageable);

    /**
     * Return a page of deleted AGPM entities based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of AGPM responses
     */
    Page<Agpm> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted AGPM with the given id.
     *
     * @param agpmId,agpmVer the ids of the AGPM to return
     * @return the AGPM response
     */
    Optional<Agpm> findDeletedById(Integer agpmId, Integer agpmVer);

    /**
     * Return the AGPM with the given specification.
     *
     * @param specification the specification to use
     * @return the AGPM response
     */
    Optional<Agpm> findOne(Specification<Agpm> specification);

    /**
     * Return the AGPM with the given id.
     *
     * @param agpmId,agpmVer the ids of the AGPM to return
     * @return the AGPM response
     */
    Optional<Agpm> findById(Integer agpmId, Integer agpmVer);

    /**
     * Save an AGPM entity.
     *
     * @param agpm the AGPM to save
     * @return the saved AGPM
     */
    Agpm save(Agpm agpm);

    /**
     * Create a new AGPM entity.
     *
     * @param request the request to create the AGPM from
     * @return the created AGPM response
     */
    Agpm create(AgpmRequest request) throws AgpmAlreadyExistsException;

    /**
     * Update an existing AGPM entity.
     *
     * @param agpmId,agpmVer the ids of the AGPM to update
     * @param request the request containing the updated data
     * @return the updated AGPM response
     */
    Agpm update(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound;

    /**
     * Delete an AGPM entity (soft deletion).
     *
     * @param agpmId,agpmVer the ids of the AGPM to delete
     */
    void delete(Integer agpmId, Integer agpmVer);}