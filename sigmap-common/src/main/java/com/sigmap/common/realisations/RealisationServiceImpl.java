package com.sigmap.common.realisations;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.paas.PaaRepository;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.request.RealisationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RealisationServiceImpl implements RealisationService {
    private final RealisationRepository repository;

    public RealisationServiceImpl(RealisationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Realisation> findAll() {
        Specification<Realisation> spec = RealisationSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Realisation> findAll(Specification<Realisation> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Realisation> findAllDeleted(Specification<Realisation> spec, Pageable pageable) {
        Specification<Realisation> deletedSpec = RealisationSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Realisation> findAllDeleted(Pageable pageable) {
        Specification<Realisation> spec = RealisationSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Realisation> findDeletedById(Integer realisationId, Integer realisationVer) {
        Specification<Realisation> spec = RealisationSpecifications.id(new CompositeId(realisationId, realisationVer))
                .and(RealisationSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Realisation> findOne(Specification<Realisation> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Realisation> findById(Integer realisationId, Integer realisationVer) {
        return repository.findById(new CompositeId(realisationId, realisationVer));
    }

    @Override
    public Realisation save(Realisation realisation) {
        return repository.save(realisation);
    }

    @Override
    public Realisation create(RealisationRequest request) throws RealisationAlreadyExistsException {
        Integer id = request.getId();
        Integer version = request.getVersion();
        Optional<Realisation> existingRealisation = repository.findById(new CompositeId(id, version));

        if (existingRealisation.isPresent()) {
            throw new RealisationAlreadyExistsException(id, version,
                    "Realisation with ID " + id + " and version " + version + " already exists in the database.");
        }

        Realisation realisation = new Realisation();
        realisation.setId(id);
        realisation.setVersion(version);
        realisation.setRealisationEnvisagees(request.getRealisationEnvisagees());

        realisation.setServiceId(request.getServiceId());
        realisation.setDirectionId(request.getDirectionId());
        realisation.setSourceFinancementId(request.getSourceFinancementId());
        realisation.setMonaisId(request.getMonaisId());
        realisation.setTypeMarcherId(request.getTypeMarcherId());
        realisation.setModeSelectionId(request.getModeSelectionId());
        realisation.setDatePrevLps(request.getDatePrevLps());
        realisation.setDatePrevAc(request.getDatePrevAc());
        realisation.setDatePrevDp(request.getDatePrevDp());
        realisation.setDatePrevAp(request.getDatePrevAp());
        realisation.setBudgetEstime(request.getBudgetEstime());
        realisation.setIsPublished(request.getIsPublished());
        realisation.setPublishedAt(request.getPublishedAt());
        realisation.setSort(request.getSort());
        return repository.save(realisation);
    }

    @Override
    public Realisation update(Integer realisationId, Integer realisationVer, RealisationRequest request) throws RealisationNotFoundException {
        CompositeId compositeId = new CompositeId(realisationId, realisationVer);
        Optional<Realisation> existingRealisationOpt = repository.findById(compositeId);

        if (existingRealisationOpt.isEmpty()) {
            throw new RealisationNotFoundException(realisationId, realisationVer,
                    "Realisation with ID " + realisationId + " and version " + realisationVer + " was not found in the database.");
        }

        Realisation realisation = existingRealisationOpt.get();

        // Update fields from request based on Realisation entity
        realisation.setRealisationEnvisagees(request.getRealisationEnvisagees());
        realisation.setServiceId(request.getServiceId());
        realisation.setDirectionId(request.getDirectionId());
        realisation.setSourceFinancementId(request.getSourceFinancementId());
        realisation.setMonaisId(request.getMonaisId());
        realisation.setTypeMarcherId(request.getTypeMarcherId());
        realisation.setModeSelectionId(request.getModeSelectionId());
        realisation.setDatePrevLps(request.getDatePrevLps());
        realisation.setDatePrevAc(request.getDatePrevAc());
        realisation.setDatePrevDp(request.getDatePrevDp());
        realisation.setDatePrevAp(request.getDatePrevAp());
        realisation.setBudgetEstime(request.getBudgetEstime());
        realisation.setIsPublished(request.getIsPublished());
        realisation.setPublishedAt(request.getPublishedAt());
        realisation.setSort(request.getSort());

        // Note: Not updating ppms, paas, lots, or docs relationships here
        // These might require separate handling if needed

        // Save and return the updated entity
        return repository.save(realisation);
    }

    @Override
    public void delete(Integer realisationId, Integer realisationVer) {
        repository.softDelete(realisationId, realisationVer);
    }
}