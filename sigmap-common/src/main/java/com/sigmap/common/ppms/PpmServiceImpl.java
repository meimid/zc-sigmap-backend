package com.sigmap.common.ppms;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ppms.request.PpmRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PpmServiceImpl implements PpmService {
    private final PpmRepository repository;
    private final AcRepository acRepository;
    //private final AcService acService;

    /**
     * Constructor.
     *
     * @param repository ppm repository
     *                    //TODO
//     * @param acService ac service
     */
    public PpmServiceImpl(PpmRepository repository, AcRepository acRepository) {
        this.repository = repository;
        this.acRepository = acRepository;
    }

    @Override
    public List<Ppm> findAll() {
        Specification<Ppm> spec = PpmSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Ppm> findAll(Specification<Ppm> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Ppm> findAllDeleted(Specification<Ppm> spec, Pageable pageable) {
        Specification<Ppm> deletedSpec = PpmSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Ppm> findAllDeleted(Pageable pageable) {
        Specification<Ppm> spec = PpmSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Ppm> findDeletedById(Integer ppmId, Integer ppmVer) {
        Specification<Ppm> specification = PpmSpecifications.id(new CompositeId(ppmId, ppmVer))
                .and(PpmSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<Ppm> findOne(Specification<Ppm> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Ppm> findById(Integer ppmId, Integer ppmVer) {
        return repository.findById(new CompositeId(ppmId, ppmVer));
    }

    @Override
    public Ppm save(Ppm ppm) {
        return repository.save(ppm);
    }

    @Override
    public Ppm create(PpmRequest request) {
        Integer id = request.getId();
        if (id == null) {
            Integer maxId = repository.findMaxId();
            id = (maxId != null) ? maxId + 1 : 1;
            request.setId(id);
        }

        Integer version = request.getVersion() != null ? request.getVersion() : 1;

        Ppm ppm = new Ppm();
        ppm.setId(id);
        ppm.setVersion(version);
        ppm.setRef(request.getRef());
        ppm.setNom(request.getNom());
        ppm.setAnnee(request.getAnnee());
        ppm.setBudgetEstime(request.getBudgetEstime());
        ppm.setStatusId(request.getStatusId());

        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
            ppm.setAc(ac);
        }

        return repository.save(ppm);
    }

    @Override
    public Ppm update(Integer ppmId, Integer ppmVer, PpmRequest request) {
        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        Optional<Ppm> existingPpmOpt = repository.findById(compositeId);

        if (existingPpmOpt.isEmpty()) {
            throw new RuntimeException("Ppm with ID " + ppmId + " and version " + ppmVer + " was not found in the database.");
        }

        Ppm existingPpm = existingPpmOpt.get();

        // Update fields
        existingPpm.setRef(request.getRef());
        existingPpm.setNom(request.getNom());
        existingPpm.setAnnee(request.getAnnee());
        existingPpm.setBudgetEstime(request.getBudgetEstime());
        existingPpm.setStatusId(request.getStatusId());

        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new RuntimeException("Ac with ID " + request.getAcId() + " not found"));
            existingPpm.setAc(ac);
        } else {
            existingPpm.setAc(null);
        }

        return repository.save(existingPpm);
    }

    @Override
    public void delete(Integer ppmId, Integer ppmVer) {
        repository.softDelete(ppmId, ppmVer);
    }
}
