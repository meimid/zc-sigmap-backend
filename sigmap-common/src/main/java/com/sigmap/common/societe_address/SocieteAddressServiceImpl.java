package com.sigmap.common.societe_address;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_address.mappers.SocieteAddressMapper;
import com.sigmap.common.societe_address.request.SocieteAddressRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class SocieteAddressServiceImpl implements SocieteAddressService {
    private final SocieteAddressRepository repository;
    private final SocieteService societeService;
    private final RefParamService refParamService;

    public SocieteAddressServiceImpl(SocieteAddressRepository repository, SocieteService societeService, RefParamService refParamService) {
        this.repository = repository;
        this.societeService = societeService;
        this.refParamService = refParamService;
    }

    @Override
    public List<SocieteAddress> findAll() {
        Specification<SocieteAddress> spec = SocieteAddressSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<SocieteAddress> findAll(Specification<SocieteAddress> societeAddress, Pageable pageable) {
        return repository.findAll(societeAddress, pageable);    }

    @Override
    public Page<SocieteAddress> findAllDeleted(Specification<SocieteAddress> societeAddress, Pageable pageable) {
        societeAddress= SocieteAddressSpecifications.deleted(true);
        return repository.findAll(societeAddress, pageable);
    }

    @Override
    public Page<SocieteAddress> findAllDeleted(Pageable pageable) {
        Specification<SocieteAddress> societeAddress = SocieteAddressSpecifications.deleted(true);
        return repository.findAll(societeAddress, pageable);
    }

    @Override
    public Optional<SocieteAddress> findDeletedById(Integer id) {
        Specification<SocieteAddress> specification = SocieteAddressSpecifications.id(id).and(SocieteAddressSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<SocieteAddress> findOne(Specification<SocieteAddress> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<SocieteAddress> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public SocieteAddress save(SocieteAddress societeAddress) {
        return repository.save(societeAddress);
    }

    @Override
    public SocieteAddress create(SocieteAddressRequest request) throws RefParamNotFound, SocieteNotFound {
        SocieteAddress societeAddress= SocieteAddressMapper.INSTANCE.map(request);
        RefParam type=refParamService.findById(request.getTypeId());
        Societe societe=societeService.findById(request.getSocieteId());
        societeAddress.setType(type);
        societeAddress.setSociete(societe);
        return save(societeAddress);
    }

    @Override
    public SocieteAddress update(Integer id,SocieteAddressRequest request) throws RefParamNotFound, SocieteNotFound {
        SocieteAddress societeAddress = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"SocieteAddress not found: " + id));
        SocieteAddressMapper.INSTANCE.updateFromRequest(request, societeAddress);
        RefParam type=refParamService.findById(request.getTypeId());
        Societe societe=societeService.findById(request.getSocieteId());
        societeAddress.setType(type);
        societeAddress.setSociete(societe);
        return save(societeAddress);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);

    }
}
