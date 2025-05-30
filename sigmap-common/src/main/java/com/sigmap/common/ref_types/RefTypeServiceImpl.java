package com.sigmap.common.ref_types;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsSpecifications;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.ref_types.exceptions.*;
import com.sigmap.common.ref_types.mapper.RefTypeMapper;
import com.sigmap.common.ref_types.request.RefTypeRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RefTypeServiceImpl implements RefTypeService {
  private final RefTypeRepository repository;

  public RefTypeServiceImpl(RefTypeRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<RefType> findAll() {

    Specification<RefType> spec = RefTypeSpecifications.deleted(false);
    return repository.findAll(spec).stream().filter(el->!el.getSystemType()).collect(Collectors.toList());
  }

  @Override
  public Page<RefType> findAll(Specification<RefType> spec, Pageable pageable) {
    Page<RefType> page = repository.findAll(spec, pageable);
    List<RefType> filteredContent = page.getContent().stream()
            .filter(el ->
                    !el.getSystemType())
            .collect(Collectors.toList());
    return new PageImpl<>(filteredContent, pageable, page.getTotalElements());
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
            .orElseThrow(() -> new RefTypeNotFound("",id));
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
    boolean hasAssociatedRefParams = repository.hasAssociatedRefParams(id);
    if (hasAssociatedRefParams) {
      throw new HasAssociatedRefParams("",id);
    }
    repository.softDelete(id);
  }

}
