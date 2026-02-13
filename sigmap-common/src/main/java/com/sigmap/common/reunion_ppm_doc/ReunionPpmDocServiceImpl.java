package com.sigmap.common.reunion_ppm_doc;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReunionPpmDocServiceImpl implements ReunionPpmDocService {
    private final ReunionPpmDocRepository repository;

    public ReunionPpmDocServiceImpl(ReunionPpmDocRepository repository) {
        this.repository = repository;
    }

    public Optional<ReunionPpmDoc> findOne(Specification<ReunionPpmDoc> spec) {
        return repository.findOne(spec);
    }
}
