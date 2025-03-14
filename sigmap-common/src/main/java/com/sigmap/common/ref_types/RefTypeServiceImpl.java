package com.sigmap.common.ref_types;

import com.sigmap.common.ref_types.request.RefTypeRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RefTypeServiceImpl implements RefTypeService {
    private final RefTypeRepository repository;

    public RefTypeServiceImpl(RefTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RefType> findAll() {
        return repository.findAll();
    }

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
        refType.setDescription(request.getDescription());

        System.out.println("REF TYPE" + refType);
        return save(refType);
    }
}
