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
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final DocsRepository docsRepository;
    private final AgpmMapper agpmMapper;
    private final RefParamRepository refParamRepository;

    public AgpmServiceImpl(AgpmRepository repository, AcRepository acRepository, DocsRepository docsRepository, AgpmMapper agpmMapper, RefParamRepository refParamRepository) {
        this.repository = repository;
        this.docsRepository = docsRepository;
        this.agpmMapper = agpmMapper;
        this.refParamRepository = refParamRepository;
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
        Integer version = request.getVersion();
        if (version == null) {
            throw new IllegalArgumentException("AGPM version must not be null");
        }
        Integer id = request.getId();
        if (id == null) {
            id = repository.findMaxId();
            if (id == null) {
                id = 1;
            }
            else {
                id++;
            }
        }

        Optional<Agpm> existingAgpm = repository.findById(new CompositeId(id, version));
        if (existingAgpm.isPresent()) {
            throw new AgpmAlreadyExistsException(id, version,
                    "Agpm with ID " + id + " and version " + version + " already exists.");
        }

        Agpm agpm = agpmMapper.toEntity(request);
        agpm.setId(id);
        agpm.setVersion(version);

        // Manually set Ac with ID 1 (will be retrived from keycloack)
        Ac ac = new Ac();
        ac.setId(1);
        agpm.setAc(ac);


        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(request.getDocIds());
            if (docs.size() != request.getDocIds().size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = request.getDocIds().stream()
                        .filter(docId -> !foundDocIds.contains(docId)).toList();
                throw new IllegalArgumentException("Docs with IDs " + missingDocIds + " not found");
            }
            agpm.setDocs(docs);
        } else if (request.getDocIds() != null) {
            agpm.setDocs(List.of());
        }

        agpm.setIsDeleted(false);

        RefParam defaultStatus = refParamRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Default status with ID 1 not found"));
        agpm.setStatus(defaultStatus);

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
    public Agpm updateWithVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm existingAgpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound(agpmId, agpmVer,
                        "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database."));

        if (Boolean.TRUE.equals(existingAgpm.getIsDeleted())) {
            throw new IllegalArgumentException("Cannot update Agpm with ID " + agpmId + " and version " + agpmVer + " because it is marked as deleted.");
        }

        Integer nextVersion = repository.findMaxVersionById(agpmId);

        nextVersion += 1;

        Agpm newAgpm = new Agpm();
        newAgpm.setId(agpmId);
        newAgpm.setVersion(nextVersion);
        newAgpm.setAc(existingAgpm.getAc());
        newAgpm.setDocs(existingAgpm.getDocs() != null ? new ArrayList<>(existingAgpm.getDocs()) : null);
        newAgpm.setStatus(existingAgpm.getStatus());
        newAgpm.setIsDeleted(existingAgpm.getIsDeleted());

        agpmMapper.updateAgpmFromRequest(request, newAgpm);

        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
            List<Docs> docs = docsRepository.findByIdIn(request.getDocIds());
            if (docs.size() != request.getDocIds().size()) {
                List<Integer> foundDocIds = docs.stream().map(Docs::getId).toList();
                List<Integer> missingDocIds = request.getDocIds().stream()
                        .filter(docId -> !foundDocIds.contains(docId))
                        .toList();
                throw new IllegalArgumentException("Docs with IDs " + missingDocIds + " not found");
            }
            newAgpm.setDocs(docs);
        } else if (request.getDocIds() != null) {
            newAgpm.setDocs(List.of());
        }

        return repository.save(newAgpm);
    }

    @Transactional
    public Agpm updateWithoutVersioning(Integer agpmId, Integer agpmVer, AgpmRequest request) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound(agpmId, agpmVer,
                        "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database."));

        if (Boolean.TRUE.equals(agpm.getIsDeleted())) {
            throw new IllegalArgumentException("Cannot update Agpm with ID " + agpmId + " and version " + agpmVer + " because it is marked as deleted.");
        }

        agpmMapper.updateAgpmFromRequest(request, agpm);

        // Update Docs association if provided
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

        // Set audit fields
        agpm.setUpdatedAt(Calendar.getInstance());
        agpm.setUpdatedBy("system");

        // Save the updated Agpm entity
        return repository.save(agpm);
    }

    @Override
    @Transactional
    public Agpm updateStatus(Integer agpmId, Integer agpmVer, Integer statusId) throws AgpmNotFound {
        CompositeId compositeId = new CompositeId(agpmId, agpmVer);
        Agpm agpm = repository.findById(compositeId)
                .orElseThrow(() -> new AgpmNotFound(agpmId, agpmVer,
                        "Agpm with ID " + agpmId + " and version " + agpmVer + " not found"));

        RefParam newStatus = refParamRepository.findById(statusId)
                .orElseThrow(() -> new IllegalStateException("Status with ID " + statusId + " not found in ref_param"));

        agpm.setStatus(newStatus);
        agpm.setUpdatedAt(Calendar.getInstance());
        agpm.setUpdatedBy("system");

        return repository.save(agpm);
    }

}