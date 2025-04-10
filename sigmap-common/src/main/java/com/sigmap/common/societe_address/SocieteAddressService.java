package com.sigmap.common.societe_address;

import com.sigmap.common.societe_address.request.SocieteAddressRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Optional;

public interface SocieteAddressService {
    List<SocieteAddress> findAll();

    /**
     * Return a page of SocieteAddress based on the given specification and pageable.
     *
     * @param societeAddress the specification to use
     * @param pageable the pageable to use
     * @return a page of SocieteAddress
     */
    Page<SocieteAddress> findAll(Specification<SocieteAddress> societeAddress, Pageable pageable);

    /**
     * Return a page of deleted SocieteAddress based on the given specification and pageable.
     *
     * @param societeAddress the specification to use
     * @param pageable the pageable to use
     * @return a page of SocieteAddress
     */
    Page<SocieteAddress> findAllDeleted(Specification<SocieteAddress> societeAddress, Pageable pageable);

    /**
     * Return a page of deleted SocieteAddress based on the given pageable.
     *
     * @param pageable the pageable to use
     * @return a page of SocieteAddress
     */
    Page<SocieteAddress> findAllDeleted(Pageable pageable);

    /**
     * Return the deleted SocieteAddress with the given id.
     *
     * @param id the id of the SocieteAddress to return
     * @return the SocieteAddress with the given id
     */
    Optional<SocieteAddress> findDeletedById(Integer id);

    /**
     * Return the SocieteAddress with the given specification.
     *
     * @param specification the specification to use
     * @return the SocieteAddress with the given specification
     */
    Optional<SocieteAddress> findOne(Specification<SocieteAddress> specification);

    /**
     * Return the SocieteAddress with the given id.
     *
     * @param id the id of the SocieteAddress to return
     * @return the SocieteAddress with the given id
     */
    Optional<SocieteAddress> findById(Integer id);

    /**
     * Save a SocieteAddress.
     *
     * @param societeAddress the SocieteAddress to save
     * @return the saved SocieteAddress
     */
    SocieteAddress save(SocieteAddress societeAddress);

    /**
     * Create a SocieteAddress.
     *
     * @param request the request to create the SocieteAddress from
     * @return the created SocieteAddress
     */
    SocieteAddress create(SocieteAddressRequest request);
    /**
     * update Com.
     *
     * @param request the request to update the com from
     * @return the updated SocieteAddress
     */
    SocieteAddress update(SocieteAddressRequest request);
    /**
     * Delete a SocieteAddress.
     *
     * @param id the id of the SocieteAddress to delete
     */
    void delete(Integer id);
}
