package com.sigmap.common.ref_params;


import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;

import java.util.List;
import java.util.Optional;

import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Service implementation of {@link RefParamService}
 */
@Service
public class RefParamServiceImpl implements RefParamService {
    private final RefParamRepository repository;
    private final RefTypeService refTypeService;

    /**
     * Constructor.
     *
     * @param repository     refParam repository
     * @param refTypeService refType service
     */
    public RefParamServiceImpl(RefParamRepository repository, RefTypeService refTypeService) {
        this.repository = repository;
        this.refTypeService = refTypeService;
    }

    @Override
    public List<RefParam> findAll() {
        Specification<RefParam> spec = RefParamSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<RefParam> findAll(Specification<RefParam> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<RefParam> findOne(Specification<RefParam> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Page<RefParam> findAllDeleted(Pageable pageable) {
        Specification<RefParam> spec = RefParamSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<RefParam> findAllDeleted(Specification<RefParam> spec, Pageable pageable) {
        spec = RefParamSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<RefParam> findDeletedById(Integer id) {
        Specification<RefParam> specification = RefParamSpecifications.id(id).and(RefParamSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public RefParam findById(Integer id) throws RefParamNotFound {
        return repository.findById(id)
                .orElseThrow(() -> new RefParamNotFound("",List.of(id)));
    }

    @Override
    public RefParam save(RefParam refParam) {
        return repository.save(refParam);
    }

    @Override
    public RefParam create(RefParamRequest request) throws RefTypeNotFound {
        RefType refType = refTypeService.findById(request.getRefTypeId());
        RefParam refParam = RefParamMapper.INSTANCE.map(request);
        refParam.setRefType(refType);
        return save(refParam);
    }

    @Override
    public RefParam update(Integer id,RefParamRequest request) throws RefParamNotFound, RefTypeNotFound{
        RefParam refParam = repository.findById(id)
                .orElseThrow(() -> new RefParamNotFound("",List.of(id)));
        RefType refType = refTypeService.findById(request.getRefTypeId());
        RefParamMapper.INSTANCE.updateFromRequest(request, refParam);
        refParam.setRefType(refType);
        return save(refParam);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
