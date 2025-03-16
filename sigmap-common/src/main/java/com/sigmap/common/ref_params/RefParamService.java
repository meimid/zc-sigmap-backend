package com.sigmap.common.ref_params;

import com.sigmap.common.ref_params.request.RefParamRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Ref;
import java.util.List;
import java.util.Optional;

public interface RefParamService {
  List<RefParam> findAll();

  Page<RefParam> findAll(Specification<RefParam> spec, Pageable pageable);

  Page<RefParam> findAllDeleted(Specification<RefParam> spec, Pageable pageable);

  Page<RefParam> findAllDeleted(Pageable pageable);

  Optional<RefParam> findDeletedById(Integer id);

  Optional<RefParam> findOne(Specification<RefParam> specification);

  Optional<RefParam> findById(Integer id);

  RefParam save(RefParam refParam);

  RefParam create(RefParamRequest request);

  void delete(Integer id);
}
