package com.sigmap.common.monnaie;

import com.sigmap.common.monnaie.exceptions.MonnaieCodeAlreadyExist;
import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import com.sigmap.common.monnaie.mappers.MonnaieMapper;
import com.sigmap.common.monnaie.request.MonnaieRequest;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Service implementation of {@link MonnaieService}
 */
@Service
public class MonnaieServiceImpl implements MonnaieService {
    private final MonnaieRepository repository;

    /**
     * Constructor.
     *
     * @param repository     monnaie repository
     */

    public MonnaieServiceImpl(MonnaieRepository repository) {
        this.repository = repository;
    }


    @Override
    public List<Monnaie> findAll() {
        Specification<Monnaie> spec = MonnaieSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Monnaie> findAll(Specification<Monnaie> spec, Pageable pageable) {
        Specification<Monnaie> base = MonnaieSpecifications.deleted(false);
        Specification<Monnaie> combined = (spec != null) ? base.and(spec) : base;
        return repository.findAll(combined, pageable);

    }

    @Override
    public Optional<Monnaie> findOne(Specification<Monnaie> specification) {

        return repository.findOne(specification);

    }

    @Override
    public Page<Monnaie> findAllDeleted(Pageable pageable) {
        Specification<Monnaie> spec = MonnaieSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Monnaie> findAllDeleted(Specification<Monnaie> spec, Pageable pageable) {
        Specification<Monnaie> base = MonnaieSpecifications.deleted(true);
        Specification<Monnaie> combined = (spec != null) ? base.and(spec) : base;
        return repository.findAll(combined, pageable);
    }

    @Override
    public Optional<Monnaie> findDeletedById(Integer id) {
        Specification<Monnaie> specification = MonnaieSpecifications.id(id).and(MonnaieSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Monnaie findById(Integer id) throws MonnaieNotFound {
        Specification<Monnaie> specification = MonnaieSpecifications.id(id)
                .and(MonnaieSpecifications.deleted(false));
        return findOne(specification)
                .orElseThrow(() -> new MonnaieNotFound("", List.of(id)));
    }

    @Override
    public Monnaie save(Monnaie monnaie) {
        return repository.save(monnaie);
    }

    @Override
    public Monnaie create(MonnaieRequest request)  {
        Optional <Monnaie> monnaieCode=findOne(MonnaieSpecifications.code(request.getCode()).and(MonnaieSpecifications.deleted(false)));
        if(monnaieCode.isPresent()) {
            throw new MonnaieCodeAlreadyExist("",request.getCode());
        }
        Monnaie monnaie = MonnaieMapper.INSTANCE.map(request);
        return save(monnaie);
    }

    @Override
    public Monnaie update(Integer id, MonnaieRequest request) throws MonnaieNotFound {
        Monnaie monnaie = findById(id);
        Optional <Monnaie> monnaieCode=findOne(MonnaieSpecifications.code(request.getCode()).and(MonnaieSpecifications.deleted(false)));
        if(monnaieCode.isPresent()&&!monnaieCode.get().getId().equals(id)) {
            throw new MonnaieCodeAlreadyExist("",request.getCode());
        }
        MonnaieMapper.INSTANCE.updateFromRequest(request, monnaie);
        return save(monnaie);
    }

    @Override
    public void delete(Integer id) throws MonnaieNotFound {
        findById(id);
        repository.softDelete(id);
    }


}
