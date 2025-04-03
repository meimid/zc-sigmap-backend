package com.sigmap.common.paas;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.paas.exceptions.PaaAlreadyExistsException;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.mappers.PaaMapper;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation of {@link PaaService}
 */
@Service
public class PaaServiceImpl implements PaaService {
    private final PaaRepository repository;
    public PaaServiceImpl(PaaRepository repository) {
        this.repository = repository;

    }

    @Override
    public List<Paa> findAll() {
        Specification<Paa> spec = PaaSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Paa> findAll(Specification<Paa> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Paa> findAllDeleted(Specification<Paa> spec, Pageable pageable) {
        Specification<Paa> deletedSpec = PaaSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Paa> findAllDeleted(Pageable pageable) {
        Specification<Paa> spec = PaaSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }



    @Override
    public Optional<Paa> findDeletedById(Integer paaId, Integer paaVer) {
        Specification<Paa> spec = PaaSpecifications.id(new CompositeId(paaId, paaVer))
                .and(PaaSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Paa> findOne(Specification<Paa> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Paa> findById(Integer paaId, Integer paaVer) {
        return repository.findById(new CompositeId(paaId, paaVer));
    }

    @Override
    public Paa save(Paa paa) {
        return repository.save(paa);
    }

    @Override
    public Paa create(PaaRequest request) throws PaaAlreadyExistsException {
        Integer id = request.getId();
        Integer version = request.getVersion();
        Optional<Paa> existingPaa = repository.findById(new CompositeId(id, version));

        if (existingPaa.isPresent()) {
            throw new PaaAlreadyExistsException(id, version, "Paa with ID " + id + " and version " + version + " already exists.");
        }

        Paa paa = new Paa();
        paa.setId(id);
        paa.setVersion(version);
        paa.setRef(request.getRef());
        paa.setNom(request.getNom());
        paa.setAnnee(request.getAnnee());
        // TODO: verify this later
        // paa.setAc(request.getAc());
        paa.setBudgetEstime(request.getBudgetEstime());
        paa.setStatusId(request.getStatusId());
        return repository.save(paa);
    }

    @Override
    public void delete(Integer paaId, Integer paaVer) {
        repository.softDelete(paaId, paaVer);
    }

    @Override
    public Paa update(Integer paaId, Integer paaVer, PaaRequest request) throws PaaNotFound {
        CompositeId compositeId = new CompositeId(paaId, paaVer);
        Optional<Paa> existingPaaOpt = repository.findById(compositeId);

        if (existingPaaOpt.isEmpty()) {
            throw new PaaNotFound(paaId, paaVer,
                    "Paa with ID " + paaId + " and version " + paaVer + " was not found in the database.");
        }

        Paa paa = existingPaaOpt.get();


        paa.setRef(request.getRef());
        paa.setNom(request.getNom());
        paa.setAnnee(request.getAnnee());
        // paa.setAc(request.getAc()); // TODO: verify this later
        paa.setBudgetEstime(request.getBudgetEstime());
        paa.setStatusId(request.getStatusId());

        // Save and return the updated entity
        return repository.save(paa);
    }
}