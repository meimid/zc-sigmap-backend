package com.sigmap.common.ref_types;

import com.sigmap.common.ref_types.request.RefTypeRequest;

import java.util.List;
import java.util.Optional;

public interface RefTypeService {
    List<RefType> findAll();

    Optional<RefType> findById(Integer id);

    RefType save(RefType refType);

    RefType create(RefTypeRequest request);
}
