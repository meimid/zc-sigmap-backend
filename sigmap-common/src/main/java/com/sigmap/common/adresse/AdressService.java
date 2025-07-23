package com.sigmap.common.adresse;

import com.sigmap.common.adresse.exceptions.AdresseNotFound;
import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface AdressService {
    Optional<Adresse> findOne(Specification<Adresse> spec);
    Adresse findById(Integer id) throws AdresseNotFound;
    Adresse save(Adresse adresse);
    Adresse create(AdresseRequest request) throws RefParamNotFound;
    Adresse update( AdresseRequest adresse) throws RefParamNotFound , AdresseNotFound;

}
