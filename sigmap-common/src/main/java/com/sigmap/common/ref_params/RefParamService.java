package com.sigmap.common.ref_params;

import com.sigmap.common.ref_params.request.RefParamRequest;

import java.util.List;
import java.util.Optional;

public interface RefParamService {
    List<RefParam> findAll();

    Optional<RefParam> findById(Integer id);

    RefParam save(RefParam refParam);

    RefParam create(RefParamRequest request);
}
