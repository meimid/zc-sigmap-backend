package com.sigmap.common.societe_address;

import com.sigmap.common.adresse.AdressService;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteRepository;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.SocieteSpecifications;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_address.exception.AddressIdRequiredException;
import com.sigmap.common.societe_address.exception.SocieteAddressAlreadyExist;
import com.sigmap.common.societe_address.exception.SocieteAdresseNotFound;
import com.sigmap.common.societe_address.exception.TypeIdRequiredException;
import com.sigmap.common.societe_address.mappers.SocieteAddressMapper;
import com.sigmap.common.societe_address.request.SocieteAddressRequest;
import com.sigmap.common.societe_address.request.SocieteAddressUpdateRequest;
import com.sigmap.common.societe_address.request.SocieteAdressUnderSocieteRequest;
import com.sigmap.common.user_societe.exception.SocieteIdRequiredException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SocieteAddressServiceImpl implements SocieteAddressService {
    private final SocieteAddressRepository repository;
    private final SocieteRepository societeRepository;
    private final RefParamService refParamService;
    private final AdressService addressService;

    public SocieteAddressServiceImpl(SocieteAddressRepository repository, SocieteRepository societeRepository, RefParamService refParamService, AdressService addressService) {
        this.repository = repository;
        this.societeRepository = societeRepository;
        this.refParamService = refParamService;
        this.addressService = addressService;
    }

    @Override
    public List<SocieteAddress> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<SocieteAddress> findAll(Specification<SocieteAddress> societeAddress, Pageable pageable) {
        return repository.findAll(societeAddress, pageable);    }




    @Override
    public Optional<SocieteAddress> findOne(Specification<SocieteAddress> specification) {
        return repository.findOne(specification);
    }

    @Override
    public SocieteAddress findById(Integer societeId, Integer adresseId) throws SocieteAdresseNotFound {
        Specification<SocieteAddress> specification =  Specification
                .where(SocieteAddressSpecifications.societeId(societeId))
                .and(SocieteAddressSpecifications.addressId(adresseId));
        return findOne(specification)
                .orElseThrow(() -> new SocieteAdresseNotFound("",societeId,adresseId));
    }

    @Override
    public SocieteAddress save(SocieteAddress societeAddress) {
        return repository.save(societeAddress);
    }

    @Override
    public SocieteAddress create(SocieteAddressRequest request) throws RefParamNotFound, SocieteNotFound {
        if (request.getSocieteId() == null) {
            throw new SocieteIdRequiredException();
        }

        if (request.getAddressId() == null) {
            throw new AddressIdRequiredException();
        }

        if (request.getTypeId() == null) {
            throw new TypeIdRequiredException();
        }

        Specification<SocieteAddress> specification =  Specification
                .where(SocieteAddressSpecifications.societeId(request.getSocieteId()))
                .and(SocieteAddressSpecifications.addressId(request.getAddressId()));
        Optional <SocieteAddress > existing=findOne(specification);

        if (existing.isPresent()) {
            throw new SocieteAddressAlreadyExist();
        }

        SocieteAddress societeAddress= SocieteAddressMapper.INSTANCE.map(request);

        RefParam type=refParamService.findById(request.getTypeId());
        Specification<Societe> specificationSociete = SocieteSpecifications.id(request.getSocieteId())
                .and(SocieteSpecifications.deleted(false));
        Societe societe= societeRepository.findOne(specificationSociete)
                .orElseThrow(() -> new SocieteNotFound("",List.of(request.getSocieteId())));
        Adresse address=addressService.findById(request.getAddressId());
        societeAddress.setType(type);
        societeAddress.setSociete(societe);
        societeAddress.setAddress(address);
        return save(societeAddress);
    }

    @Override
    public SocieteAddress update(Integer societeId, Integer adresseId, SocieteAddressUpdateRequest request)
    throws RefParamNotFound, SocieteNotFound , SocieteAdresseNotFound{
        if (societeId == null) {
            throw new SocieteIdRequiredException();
        }

        if (adresseId == null) {
            throw new AddressIdRequiredException();
        }

        if (request.getTypeId() == null) {
            throw new TypeIdRequiredException();
        }
        SocieteAddress societeAddress = findById(societeId,adresseId);
        SocieteAddressMapper.INSTANCE.updateFromRequest(request, societeAddress);
        RefParam type=refParamService.findById(request.getTypeId());
        societeAddress.setType(type);
        return save(societeAddress);
    }

    @Override
    public List<SocieteAddress> findSocieteAddressBySocieteId(Integer societeId) throws SocieteNotFound {
        Specification<Societe> specificationSociete = SocieteSpecifications.id(societeId)
                .and(SocieteSpecifications.deleted(false));
        societeRepository.findOne(specificationSociete)
                .orElseThrow(() -> new SocieteNotFound("",List.of(societeId)));
        Specification<SocieteAddress> spec=SocieteAddressSpecifications.societeId(societeId);
        return repository.findAll(spec);
    }
}
