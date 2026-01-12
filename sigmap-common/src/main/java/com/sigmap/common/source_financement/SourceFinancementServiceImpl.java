package com.sigmap.common.source_financement;

import com.sigmap.common.source_financement.exceptions.SourceFinancementCodeAlreadyExist;
import com.sigmap.common.source_financement.exceptions.SourceFinancementNotFound;
import com.sigmap.common.source_financement.mappers.SourceFinancementMapper;
import com.sigmap.common.source_financement.request.SourceFinancementRequest;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Service implementation of {@link SourceFinancementService}
 */
@Service
@Slf4j
public class SourceFinancementServiceImpl implements SourceFinancementService {
    private final SourceFinancementRepository repository;

    /**
     * Constructor.
     *
     * @param repository source financement repository
     */
    public SourceFinancementServiceImpl(SourceFinancementRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SourceFinancement> findAll() {
        Specification<SourceFinancement> spec = SourceFinancementSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<SourceFinancement> findAllPage(Specification<SourceFinancement> spec, Pageable pageable) {
        Specification<SourceFinancement> base = SourceFinancementSpecifications.deleted(false);
        Specification<SourceFinancement> combined = (spec != null) ? base.and(spec) : base;
        return repository.findAll(combined, pageable);
    }

    @Override
    public List<SourceFinancement> findAll(Specification<SourceFinancement> spec) {
        Specification<SourceFinancement> base = SourceFinancementSpecifications.deleted(false);
        Specification<SourceFinancement> combined = (spec != null) ? base.and(spec) : base;
        return repository.findAll(combined);
    }

    @Override
    public Optional<SourceFinancement> findOne(Specification<SourceFinancement> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Page<SourceFinancement> findAllDeleted(Pageable pageable) {
        Specification<SourceFinancement> spec = SourceFinancementSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<SourceFinancement> findAllDeleted(Specification<SourceFinancement> spec, Pageable pageable) {
        Specification<SourceFinancement> base = SourceFinancementSpecifications.deleted(true);
        Specification<SourceFinancement> combined = (spec != null) ? base.and(spec) : base;
        return repository.findAll(combined, pageable);
    }

    @Override
    public Optional<SourceFinancement> findDeletedById(Integer id) {
        Specification<SourceFinancement> specification = SourceFinancementSpecifications.id(id)
                .and(SourceFinancementSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public SourceFinancement findById(Integer id) throws SourceFinancementNotFound {
        Specification<SourceFinancement> specification = SourceFinancementSpecifications.id(id)
                .and(SourceFinancementSpecifications.deleted(false));
        return findOne(specification)
                .orElseThrow(() -> new SourceFinancementNotFound("", List.of(id)));
    }

    @Override
    public SourceFinancement save(SourceFinancement sourceFinancement) {
        return repository.save(sourceFinancement);
    }

    @Override
    public SourceFinancement create(SourceFinancementRequest request) {
        Optional<SourceFinancement> existingCode = findOne(
                SourceFinancementSpecifications.code(request.getCode())
                        .and(SourceFinancementSpecifications.deleted(false)));
        if (existingCode.isPresent()) {
            throw new SourceFinancementCodeAlreadyExist("", request.getCode());
        }
        SourceFinancement sourceFinancement = SourceFinancementMapper.INSTANCE.map(request);
        return save(sourceFinancement);
    }

    @Override
    public SourceFinancement update(Integer id, SourceFinancementRequest request) throws SourceFinancementNotFound {
        SourceFinancement sourceFinancement = findById(id);
        Optional<SourceFinancement> existingCode = findOne(
                SourceFinancementSpecifications.code(request.getCode())
                        .and(SourceFinancementSpecifications.deleted(false)));
        if (existingCode.isPresent() && !existingCode.get().getId().equals(id)) {
            throw new SourceFinancementCodeAlreadyExist("", request.getCode());
        }
        SourceFinancementMapper.INSTANCE.updateFromRequest(request, sourceFinancement);
        return save(sourceFinancement);
    }

    @Override
    public void delete(Integer id) throws SourceFinancementNotFound {
        findById(id); // Verify it exists
        repository.softDelete(id);
    }
}
