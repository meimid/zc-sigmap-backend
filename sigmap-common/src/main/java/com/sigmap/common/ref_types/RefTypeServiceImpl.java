package com.sigmap.common.ref_types;

import com.sigmap.common.config.RefTypes;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_types.exceptions.*;
import com.sigmap.common.ref_types.mapper.RefTypeMapper;
import com.sigmap.common.ref_types.request.RefTypeRequest;

import java.sql.Ref;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class RefTypeServiceImpl implements RefTypeService {
  private final RefTypeRepository repository;
  private final RefParamRepository refParamRepository;

  public RefTypeServiceImpl(RefTypeRepository repository,RefParamRepository refParamRepository) {
    this.repository = repository;
    this.refParamRepository = refParamRepository;
  }

  @Override
  public List<RefType> findAll() {
    Specification<RefType> spec = RefTypeSpecifications.systemType();
    return repository.findAll(spec);
  }

  @Override
  public List<RefType> findAllWithSystemType(Specification<RefType> spec) {
    return repository.findAll(spec);
  }

  @Override
  public Page<RefType> findAll(Specification<RefType> spec, Pageable pageable) {
    Specification<RefType> systemTypeSpec = RefTypeSpecifications.systemType();
    Specification<RefType> combinedSpec;
    if (spec != null) {
      combinedSpec = spec.and(systemTypeSpec);
    } else {
      combinedSpec = systemTypeSpec;
    }
    return repository.findAll(combinedSpec, pageable);
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
  public RefType findById(Integer id) throws RefTypeNotFound {
    Specification<RefType> specification = RefTypeSpecifications.id(id)
            .and(RefTypeSpecifications.deleted(false));
    return findOne(specification)
            .orElseThrow(() -> new RefTypeIdNotFound("",id));
  }
  @Override
  public RefType findByCode(RefTypes code) throws RefTypeNotFound {
    Specification<RefType> specification = RefTypeSpecifications.code(code)
            .and(RefTypeSpecifications.deleted(false));
    return findOne(specification)
            .orElseThrow(() -> new RefTypeNotFound("",code));
  }
  @Override
  public RefType save(RefType refType) {
    return repository.save(refType);
  }

  private boolean check_id(Integer refTypeId){
    return refTypeId != null && refTypeId > 0;
  }

  @Override
  public RefType create(RefTypeRequest request) throws RefTypePositiveIdAndNotEmpty {
    Optional<RefType> checkId = repository.findById(request.getId());
    if (checkId.isPresent()) {
      throw new RefTypeAlreadyExist("" , request.getId());
    }
    else if (!this.check_id(request.getId())) {
      throw new RefTypePositiveIdAndNotEmpty("");
    }
    else {
      RefType refType = RefTypeMapper.INSTANCE.map(request);
      if(refType.getSystemType()==null) {
        refType.setSystemType(false);
      }
      return save(refType);
    }
  }



  @Override
  public RefType update(RefTypeRequest request) throws RefTypeNotFound {
    RefType refType = findById(request.getId());
    RefTypeMapper.INSTANCE.updateFromRequest(request, refType);
    return save(refType);
  }

  @Override
  public void delete(Integer id) throws HasAssociatedRefParams {
    findById(id);

    // Create specification to find non-deleted RefParams for this RefType
    Specification<RefParam> nonDeletedForRefType = Specification.where(
            RefParamSpecifications.deleted(false)
                    .and(RefParamSpecifications.hasRefTypeId(id))
    );

    long count = refParamRepository.count(nonDeletedForRefType);
    boolean hasAssociatedRefParams = count > 0;

    if (hasAssociatedRefParams) {
      throw new HasAssociatedRefParams("", id);
    }
    repository.softDelete(id);
  }

}
