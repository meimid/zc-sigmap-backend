package com.sigmap.common.ref_params;

import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RefParamServiceImpl implements RefParamService {
    private final RefParamRepository repository;
    private final RefTypeService refTypeService;

    public RefParamServiceImpl(RefParamRepository repository, RefTypeService refTypeService) {
        this.repository = repository;
        this.refTypeService = refTypeService;
    }


    @Override
    public List<RefParam> findAll() {
        return repository.findAll();
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
        RefType refType = refTypeService
                .findById(request.getRefTypeId())
                .orElseThrow();

        RefParam refParam = new RefParam();

        refParam.setCode(request.getCode());
        refParam.setDescription(request.getDescription());
        refParam.setRefType(refType);

        return save(refParam);
    }
}
