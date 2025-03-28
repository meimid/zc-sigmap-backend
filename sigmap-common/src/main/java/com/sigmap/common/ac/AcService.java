package com.sigmap.common.ac;

import com.sigmap.common.ac.request.AcRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface AcService {
    List<Ac> findAll();

    /**
     * Return a page of Ac based on the given specification and pageable.
     *
     * @param ac the specification to use
     * @param pageable the pageable to use
     * @return a page of Ac
     */
    Page<Ac> findAll(Specification<Ac> ac, Pageable pageable);

    /**
     * Return a page of deleted Ac based on the given specification and pageable.
     *
     * @param ac the specification to use
     * @param pageable the pageable to use
     * @return a page of Ac
     */
    Page<Ac> findAllDeleted(Specification<Ac> ac, Pageable pageable);

    /**
     * Return a page of deleted Ac based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of Ac
     */
    Page<Ac> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted Ac with the given id.
     *
     * @param id the id of the Ac to return
     * @return the Ac with the given id
     */
    Optional<Ac> findDeletedById(Integer id);

    /**
     * Return the Ac with the given specification.
     *
     * @param specification the specification to use
     * @return the Ac with the given specification
     */
    Optional<Ac> findOne(Specification<Ac> specification);

    /**
     * Return the Ac with the given id.
     *
     * @param id the id of the Ac to return
     * @return the Ac with the given id
     */
    Optional<Ac> findById(Integer id);

    /**
     * Save a Ac.
     *
     * @param Ac the Ac to save
     * @return the saved Ac
     */
    Ac save(Ac Ac);

    /**
     * Create a Ac.
     *
     * @param request the request to create the Ac from
     * @return the created Ac
     */
    Ac create(AcRequest request);

    /**
     * update Ac.
     *
     * @param ac the request to update the Ac from
     * @return the updated Ac
     */
    Ac update(Ac ac);

    /**
     * Delete a Ac.
     *
     * @param id the id of the Ac to delete
     */
    void delete(Integer id);
}
