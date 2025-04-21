package com.sigmap.common.ref_types;

import com.sigmap.common.ref_types.mapper.RefTypeMapper;
import com.sigmap.common.ref_types.request.RefTypeRequest;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefTypeServiceImpl implements RefTypeService {
  private final RefTypeRepository repository;

  public RefTypeServiceImpl(RefTypeRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<RefType> findAll() {
    Specification<RefType> spec = RefTypeSpecifications.deleted(false);
    return repository.findAll(spec);
  }

  @Override
  public Page<RefType> findAll(Specification<RefType> spec, Pageable pageable) {
    return repository.findAll(spec, pageable);
  }

  @Override
  public Optional<RefType> findOne(Specification<RefType> specification) {
    return repository.findOne(specification);
  }

  @Override
  public Page<RefType> findAllDeleted(Specification<RefType> spec, Pageable pageable) {
    spec = RefTypeSpecifications.deleted(true);
    return repository.findAll(spec, pageable);  }

  @Override
  public Page<RefType> findAllDeleted(Pageable pageable) {
    Specification<RefType> spec = RefTypeSpecifications.deleted(true);
    return repository.findAll(spec, pageable);
  }

  @Override
  public Optional<RefType> findDeletedById(Integer id) {
    Specification<RefType> specification = RefTypeSpecifications.id(id).and(RefTypeSpecifications.deleted(true));
    return findOne(specification);  }

  @Override
  public Optional<RefType> findById(Integer id) {
    return repository.findById(id);
  }

  @Override
  public RefType save(RefType refType) {
    return repository.save(refType);
  }

  @Override
  public RefType create(RefTypeRequest request) {
    RefType refType = new RefType();
    refType= RefTypeMapper.INSTANCE.map(request);
    return save(refType);
  }

  @Override
  public RefType update(RefTypeRequest request) {
    RefType refType=repository.findById(request.getId()).orElseThrow(() -> new RuntimeException("RefParam not found: " + request.getId()));
    RefTypeMapper.INSTANCE.updateFromRequest(request, refType);
    return save(refType);
  }

  @Override
  public void delete(Integer id) {
    repository.softDelete(id);
  }
}
