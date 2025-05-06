package com.sigmap.common.societe;


import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.request.SocieteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
@Service
public class SocieteServiceImpl implements SocieteService {
    private final SocieteRepository repository;
    private final RefParamService refParamService;

    public SocieteServiceImpl(SocieteRepository repository, RefParamService refParamService) {
        this.repository = repository;
        this.refParamService = refParamService;
    }

    @Override
    public List<Societe> findAll() {
        Specification<Societe> spec = SocieteSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Societe> findAll(Specification<Societe> societe, Pageable pageable) {
        return repository.findAll(societe, pageable);    }

    @Override
    public Page<Societe> findAllDeleted(Specification<Societe> societe, Pageable pageable) {
        societe = SocieteSpecifications.deleted(true);
        return repository.findAll(societe, pageable);
    }

    @Override
    public Page<Societe> findAllDeleted(Pageable pageable) {
        Specification<Societe> com = SocieteSpecifications.deleted(true);
        return repository.findAll(com, pageable);
    }

    @Override
    public Optional<Societe> findDeletedById(Integer id) {
        Specification<Societe> specification = SocieteSpecifications.id(id).and(SocieteSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Societe> findOne(Specification<Societe> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Societe findById(Integer id) throws SocieteNotFound {
        return repository.findById(id).orElseThrow(() -> new SocieteNotFound("",List.of(id)));    }

    @Override
    public Societe save(Societe societe) {
        return repository.save(societe);
    }

    @Override
    public Societe create(SocieteRequest request) throws RefParamNotFound {
        RefParam type=refParamService.findById(request.getTypeId());
        RefParam pays=refParamService.findById(request.getPaysId());
        Societe societe =  SocieteMapper.INSTANCE.map(request);
        societe.setPays(pays);
        societe.setType(type);
        return save(societe);
    }

    @Override
    public Societe update(Integer id,SocieteRequest request) throws RefParamNotFound {
        Societe societe=repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Societe not found with id "+ id));
        RefParam type=refParamService.findById(request.getTypeId());
        RefParam pays=refParamService.findById(request.getPaysId());
        SocieteMapper.INSTANCE.updateFromRequest(request, societe);
        societe.setPays(pays);
        societe.setType(type);
        return save(societe);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
