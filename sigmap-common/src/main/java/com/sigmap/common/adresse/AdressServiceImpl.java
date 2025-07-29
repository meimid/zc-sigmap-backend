package com.sigmap.common.adresse;


import com.sigmap.common.adresse.exceptions.AdresseNotFound;
import com.sigmap.common.adresse.mappers.AdresseMapper;
import com.sigmap.common.adresse.request.AdresseRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class AdressServiceImpl implements AdressService{
    private final AdresseRespository repository;
    private final RefParamService refParamService;

    public AdressServiceImpl(AdresseRespository repository, RefParamService refParamService) {
        this.repository = repository;
        this.refParamService = refParamService;
    }

    @Override
    public Optional<Adresse> findOne(Specification<Adresse> spec) {
        return repository.findOne(spec);
    }

    @Override
    public Adresse findById(Integer id) throws AdresseNotFound {
        Specification<Adresse> specification = AdressSpecifications.id(id)
                .and(AdressSpecifications.deleted(false));
        return findOne(specification)
                .orElseThrow(() -> new AdresseNotFound("", id));


    }

    @Override
    public Adresse save(Adresse adresse) {
        return repository.save(adresse);
    }

    @Override
    public Adresse create(AdresseRequest request) throws RefParamNotFound {

        Adresse adresse = AdresseMapper.INSTANCE.map(request);
        if(request.getPaysId()!=null){
            RefParam pays = refParamService.findById(request.getPaysId());
            adresse.setPays(pays);
        }
            return save(adresse);
    }

    @Override
    public Adresse update( AdresseRequest request) throws RefParamNotFound, AdresseNotFound {
        Adresse adresse = findById(request.getId());
        AdresseMapper.INSTANCE.updateFromRequest(request, adresse);
        if(request.getPaysId()!=null) {
            RefParam pays = refParamService.findById(request.getPaysId());
            adresse.setPays(pays);
        }
        adresse.setId(adresse.getId());
        return save(adresse);
    }
}
