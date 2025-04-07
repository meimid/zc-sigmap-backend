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
import com.sigmap.common.agpm.response.AgpmResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<AgpmResponse> findAll() {
        Specification<Agpm> spec = AgpmSpecifications.deleted(false);
        List<Agpm> agpms = repository.findAll(spec);
        return agpmMapper.mapToResponseList(agpms);
    }

    @Override
    public Page<AgpmResponse> findAll(Specification<Agpm> spec, Pageable pageable) {
        Page<Agpm> agpmPage = repository.findAll(spec, pageable);
        return agpmMapper.mapToResponse(agpmPage);
    }

    @Override
    public Page<AgpmResponse> findAllDeleted(Specification<Agpm> spec, Pageable pageable) {
        Specification<Agpm> deletedSpec = AgpmSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        Page<Agpm> agpmPage = repository.findAll(deletedSpec, pageable);
        return agpmMapper.mapToResponse(agpmPage);
    }

    @Override
    public Page<AgpmResponse> findAllDeleted(Pageable pageable) {
        Specification<Agpm> spec = AgpmSpecifications.deleted(true);
        Page<Agpm> agpmPage = repository.findAll(spec, pageable);
        return agpmMapper.mapToResponse(agpmPage);
    }

    @Override
    public Optional<AgpmResponse> findDeletedById(Integer agpmId, Integer agpmVer) {
        Specification<Agpm> spec = AgpmSpecifications.id(new CompositeId(agpmId, agpmVer))
                .and(AgpmSpecifications.deleted(true));
        return repository.findOne(spec).map(agpmMapper::map);
    }

    @Override
    public Optional<AgpmResponse> findOne(Specification<Agpm> specification) {
        return repository.findOne(specification).map(agpmMapper::map);
    }

    @Override
    public Optional<AgpmResponse> findById(Integer agpmId, Integer agpmVer) {
        return repository.findById(new CompositeId(agpmId, agpmVer)).map(agpmMapper::map);
    }

    @Override

    public Agpm save(Agpm agpm) {
        return repository.save(agpm);
    }

    @Override
    @Transactional(readOnly = false)
    public AgpmResponse create(AgpmRequest request) throws AgpmAlreadyExistsException {

        Integer id = request.getId();
        Integer version = request.getVersion();
        Optional<Agpm> existingAgpm = repository.findById(new CompositeId(id, version));

        if (existingAgpm.isPresent()) {
            throw new AgpmAlreadyExistsException(id, version, "Agpm with ID " + id + " and version " + version + " already exists.");
        }

        Agpm agpm = agpmMapper.toEntity(request);

        Integer acId = request.getAcId();
        if (acId != null) {
            Ac ac = acRepository.findById(acId)
                    .orElseThrow(() -> new IllegalArgumentException("Ac with ID " + acId + " not found"));
            agpm.setAc(ac);
        }

        List<Integer> docIds = request.getDocIds();
        if (docIds != null && !docIds.isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(docIds);
            if (docs.size() != docIds.size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = docIds.stream()
                        .filter(docId -> !foundDocIds.contains(docId))
                        .toList();
                throw new IllegalArgumentException("Docs with IDs " + missingDocIds + " not found");
            }
            agpm.setDocs(docs);
        } else if (docIds != null) {
            agpm.setDocs(List.of());
        }

        agpm.setIsDeleted(false);
        Agpm savedAgpm = repository.save(agpm);

        return agpmMapper.map(savedAgpm);
    }

    @Override

    public void delete(Integer agpmId, Integer agpmVer) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Optional<Agpm> existingAgpm = repository.findById(compositeId);
        if (existingAgpm.isEmpty()) {
            throw new AgpmNotFound(agpmId, agpmVer,
                    "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database.");
        }

        Integer rowsAffected = repository.softDelete(agpmId, agpmVer);
        if (rowsAffected != 1) {
            throw new IllegalStateException("Soft delete failed: Expected 1 row to be affected, but " + rowsAffected + " rows were affected for Agpm with ID " + agpmId + " and version " + agpmVer);
        }
    }

    @Override
    public AgpmResponse update(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound(agpmId, agpmVer,
                        "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database."));

        agpmMapper.updateAgpmFromRequest(request, agpm);

        Integer acId = request.getAcId();
        if (acId != null) {
            Ac ac = acRepository.findById(acId)
                    .orElseThrow(() -> new IllegalArgumentException("Ac with ID " + acId + " not found"));
            agpm.setAc(ac);
        } else {
            agpm.setAc(null);
        }

        List<Integer> docIds = request.getDocIds();
        if (docIds != null && !docIds.isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(docIds);
            if (docs.size() != docIds.size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = docIds.stream()
                        .filter(docId -> !foundDocIds.contains(docId))
                        .toList();
                throw new IllegalArgumentException("Docs with IDs " + missingDocIds + " not found");
            }
            agpm.setDocs(docs);
        } else if (docIds != null) {
            agpm.setDocs(List.of());
        }

        Agpm updatedAgpm = repository.save(agpm);
        return agpmMapper.map(updatedAgpm);
    }
}