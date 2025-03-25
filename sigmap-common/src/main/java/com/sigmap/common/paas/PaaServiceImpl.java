package com.sigmap.common.paas;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamSpecifications;
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

    /**
     * Constructor.
     *
     * @param repository paa repository
     * @param refTypeService refType service
     */
    public PaaServiceImpl(PaaRepository repository, RefTypeService refTypeService) {
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
    public Optional<Paa> findOne(Specification<Paa> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Page<Paa> findAllDeleted(Pageable pageable) {
        Specification<Paa> spec = PaaSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Paa> findAllDeleted(Specification<Paa> spec, Pageable pageable) {
        spec = PaaSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Paa> findDeletedById(Integer id) {
        Specification<Paa> specification = PaaSpecifications.id(id).and(PaaSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Paa> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Paa save(Paa paa) {
        return repository.save(paa);
    }

    @Override
    public Paa create(PaaRequest request) {
        //TODO:complete and add the exception
//        Ac ac = acService.findById(request.getAcId()).orElseThrow();

        Paa paa = new Paa();

        paa.setPaaId(request.getPaaId());
        paa.setPaaVer(request.getPaaVer());
        paa.setRef(request.getRef());
        paa.setNom(request.getNom());
        paa.setAnnee(request.getAnnee());
        //TODO: to complete later just uncomment
//        paa.setAc(ac);
        paa.setBudgetEstime(request.getBudgetEstime());
        paa.setStatusId(request.getStatusId());

        return save(paa);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
