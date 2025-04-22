package com.sigmap.common.agpm;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.agpm.exceptions.AgpmAlreadyExistsException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;


/**
 * Service implementation for managing Agpm entities.
 * Handles CRUD operations and associations with Ac and Docs.
 */
@Service
@Slf4j
public class AgpmServiceImpl implements AgpmService {
    private final AgpmRepository repository;
    private final AcRepository acRepository;
    private final DocsRepository docsRepository;
    private final AgpmMapper agpmMapper;

    public AgpmServiceImpl(AgpmRepository repository, AcRepository acRepository, DocsRepository docsRepository, AgpmMapper agpmMapper) {
        this.repository = repository;
        this.acRepository = acRepository;
        this.docsRepository = docsRepository;
        this.agpmMapper = agpmMapper;
    }

    @Override
    public List<Agpm> findAll() {
        Specification<Agpm> spec = AgpmSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Agpm> findAll(Specification<Agpm> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }


    @Override
    public Page<Agpm> findAllDeleted(Pageable pageable) {
        Specification<Agpm> spec = AgpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }


    @Override
    public Optional<Agpm> findOne(Specification<Agpm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Agpm> findById(Integer agpmId, Integer agpmVer) {
        return repository.findById(new CompositeId(agpmId, agpmVer));
    }

    @Override
    public Agpm save(Agpm agpm) {
        return repository.save(agpm);
    }

    @Override
    public Agpm create(AgpmRequest request) throws AgpmAlreadyExistsException {
        Optional<Agpm> existingAgpm = repository.findById(new CompositeId(request.getId(), request.getVersion()));
        if (existingAgpm.isPresent()) {
            throw new AgpmAlreadyExistsException(request.getId(), request.getVersion(), "Agpm with ID " + request.getId() + " and version " + request.getVersion() + " already exists.");
        }

        Agpm agpm = agpmMapper.toEntity(request);
        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new IllegalArgumentException("Ac with ID " + request.getAcId() + " not found"));
            agpm.setAc(ac);
        }

        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(request.getDocIds());
            if (docs.size() != request.getDocIds().size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = request.getDocIds().stream().filter(docId -> !foundDocIds.contains(docId)).toList();
                throw new IllegalArgumentException("Docs with IDs " + missingDocIds + " not found");
            }
            agpm.setDocs(docs);
        } else if (request.getDocIds() != null) {
            agpm.setDocs(List.of());
        }
        //To be changed after decision
        agpm.setIsDeleted(false);
        return repository.save(agpm);
    }

    @Override
    public void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Optional<Agpm> existingAgpm = repository.findById(compositeId);
        if (existingAgpm.isEmpty()) {
            throw new AgpmNotFound(agpmId, agpmVer,
                    "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database.");
        }

        Integer rowsAffected = repository.softDelete(agpmId, agpmVer, Calendar.getInstance());
        if (rowsAffected != 1) {
            throw new IllegalStateException("Soft delete failed: Expected 1 row to be affected, but " + rowsAffected + " rows were affected for Agpm with ID " + agpmId + " and version " + agpmVer);
        }
    }

    @Override
    @Transactional
    public Agpm update(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound(agpmId, agpmVer,
                        "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database."));
        //To be changed after decision (Boolean.TRUE.equals(agpm.getIsDeleted())--> agpm.getIsDeleted() )
        if (Boolean.TRUE.equals(agpm.getIsDeleted())) {
            throw new IllegalArgumentException("Cannot update Agpm with ID " + agpmId + " and version " + agpmVer + " because it is marked as deleted.");
        }

        agpmMapper.updateAgpmFromRequest(request, agpm);

        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new IllegalArgumentException("Ac with ID " + request.getAcId() + " not found"));
            agpm.setAc(ac);
        } else {
            agpm.setAc(null);
        }

        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(request.getDocIds());
            if (docs.size() != request.getDocIds().size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = request.getDocIds().stream()
                        .filter(docId -> !foundDocIds.contains(docId))
                        .toList();
                throw new IllegalArgumentException("Docs with IDs " + missingDocIds + " not found");
            }
            agpm.setDocs(docs);
        } else if (request.getDocIds() != null) {
            agpm.setDocs(List.of());
        }

        return repository.save(agpm);
    }
}