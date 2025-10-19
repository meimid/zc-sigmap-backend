package com.sigmap.common.societe_address;

import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_address.exception.SocieteAdresseNotFound;
import com.sigmap.common.societe_address.request.SocieteAddressRequest;
import com.sigmap.common.societe_address.request.SocieteAddressUpdateRequest;
import com.sigmap.common.societe_address.request.SocieteAdressUnderSocieteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Optional;

public interface SocieteAddressService {
    /**
     * Return all {@link SocieteAddress} records.
     *
     * @return a list of all {@link SocieteAddress} entities
     */
    List<SocieteAddress> findAll();

    /**
     * Return a page of {@link SocieteAddress} based on the given specification and pageable.
     *
     * @param societeAddress the specification to use
     * @param pageable the pageable to use
     * @return a page of {@link SocieteAddress}
     */
    Page<SocieteAddress> findAll(Specification<SocieteAddress> societeAddress, Pageable pageable);



    /**
     * Return a {@link SocieteAddress} matching the given specification.
     *
     * @param specification the specification to use
     * @return an {@link Optional} containing the matching {@link SocieteAddress}
     */
    Optional<SocieteAddress> findOne(Specification<SocieteAddress> specification);

    /**
     * Return the {@link SocieteAddress} with the given composite keys.
     *
     * @param societeId the ID of the related {@link com.sigmap.common.societe.Societe}
     * @param adresseId the ID of the related {@link com.sigmap.common.adresse.Adresse}
     * @return the {@link SocieteAddress} with the given keys
     * @throws SocieteAdresseNotFound if the {@link SocieteAddress} is not found
     */
    SocieteAddress findById(Integer societeId, Integer adresseId) throws SocieteAdresseNotFound;

    /**
     * Save a {@link SocieteAddress}.
     *
     * @param societeAddress the {@link SocieteAddress} to save
     * @return the saved {@link SocieteAddress}
     */
    SocieteAddress save(SocieteAddress societeAddress);

    /**
     * Create a new {@link SocieteAddress}.
     *
     * @param request the request to create the {@link SocieteAddress} from
     * @return the created {@link SocieteAddress}
     * @throws RefParamNotFound if a referenced parameter is not found
     * @throws SocieteNotFound if the referenced {@link com.sigmap.common.societe.Societe} is not found
     */
    SocieteAddress create(SocieteAddressRequest request)
            throws RefParamNotFound, SocieteNotFound;

    /**
     * Update an existing {@link SocieteAddress}.
     *
     * @param societeId the ID of the related {@link com.sigmap.common.societe.Societe}
     * @param adresseId the ID of the related {@link com.sigmap.common.adresse.Adresse}
     * @param request the request containing updated data
     * @return the updated {@link SocieteAddress}
     * @throws RefParamNotFound if a referenced parameter is not found
     * @throws SocieteNotFound if the referenced {@link com.sigmap.common.societe.Societe} is not found
     * @throws SocieteAdresseNotFound if the {@link SocieteAddress} is not found
     */
    SocieteAddress update(Integer societeId, Integer adresseId, SocieteAddressUpdateRequest request)
            throws RefParamNotFound, SocieteNotFound, SocieteAdresseNotFound;







    /**
     * Return a paginated list of {@link SocieteAddress} records associated with a specific {@link com.sigmap.common.societe.Societe}.
     *
     * @param societeId the ID of the {@link com.sigmap.common.societe.Societe}
     * @param pageable the pagination configuration
     * @return a page of {@link SocieteAddress} entities linked to the given societe
     * @throws SocieteNotFound if the {@link com.sigmap.common.societe.Societe} is not found
     */
    List<SocieteAddress> findSocieteAddressBySocieteId(Integer societeId)
            throws SocieteNotFound;
}