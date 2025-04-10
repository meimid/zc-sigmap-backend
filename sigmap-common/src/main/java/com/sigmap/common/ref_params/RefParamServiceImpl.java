package com.sigmap.common.ref_params;

import com.sigmap.common.contact.Mapper.ContactMapper;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;

import java.util.List;
import java.util.Optional;

import com.sigmap.common.ref_types.mapper.RefTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Service implementation of {@link RefParamService}
 */
@Slf4j
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
    public Optional<RefParam> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public RefParam save(RefParam refParam) {
        return repository.save(refParam);
    }

    @Override
    public RefParam create(RefParamRequest request) {
        RefParam refParam = new RefParam();
        RefType refType = refTypeService.findById(request.getRefTypeId()).orElseThrow();
        log.info("Creating RefParam with refTypeId: " + refType);
        refParam = RefParamMapper.INSTANCE.map(request);
        refParam.setRefType(refType);

        return save(refParam);
    }

    @Override
    public RefParam update(RefParamRequest request) {
        RefParam refParam = repository.findById(request.getId()).orElseThrow(() -> new RuntimeException("RefParam not found: " + request.getId()));
        RefType refType = refTypeService.findById(request.getRefTypeId()).orElseThrow(() -> new RuntimeException("refType not found: " + request.getId()));
        RefParamMapper.INSTANCE.updateFromRequest(request, refParam);
        refParam.setRefType(refType);
        return save(refParam);
    }

    @Override
    public void delete(Integer id) {
        repository.softDelete(id);
    }
}
