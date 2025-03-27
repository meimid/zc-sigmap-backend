package com.sigmap.common.ac;

import com.sigmap.common.ac.request.AcRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_types.RefType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AcServiceImpl implements AcService{
    private final AcRepository repository;
    @Override
    public List<Ac> findAll() {
        Specification<Ac> ac = AcSpecifications.deleted(false);
        return repository.findAll(ac);
    }

    @Override
    public Page<Ac> findAll(Specification<Ac> ac, Pageable pageable) {
        return repository.findAll(ac, pageable);
    }

    @Override
    public Page<Ac> findAllDeleted(Specification<Ac> ac, Pageable pageable) {
        ac = AcSpecifications.deleted(true);
        return repository.findAll(ac, pageable);
    }

    @Override
    public Page<Ac> findAllDeleted(Pageable pageable) {
        Specification<Ac> ac = AcSpecifications.deleted(true);
        return repository.findAll(ac, pageable);
    }

    @Override
    public Optional<Ac> findDeletedById(Integer id) {
        Specification<Ac> specification = AcSpecifications.id(id).and(AcSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Ac> findOne(Specification<Ac> specification) {

        return repository.findOne(specification);
    }

    @Override
    public Optional<Ac> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Ac save(Ac ac) {
        return repository.save(ac);
    }

    @Override
    public Ac create(AcRequest request) {
        Ac ac = new Ac();
        ac.setNom(request.getNom());
        ac.setAncrage(request.getAncrage());
        ac.setContact(request.getContact());
        System.out.println(ac.getNom());
        System.out.println("---------------------"+request.getNom());
        return save(ac);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
