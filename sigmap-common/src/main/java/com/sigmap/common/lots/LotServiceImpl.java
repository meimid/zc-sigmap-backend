package com.sigmap.common.lots;

import com.sigmap.common.lots.request.LotRequest;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LotServiceImpl implements LotService {
    private final LotRepository repository;
    private final RealisationRepository realisationRepository;

    public LotServiceImpl(LotRepository lotRepository, RealisationRepository realisationRepository) {
        this.repository = lotRepository;
        this.realisationRepository = realisationRepository;
    }

    @Override
    public List<Lot> findAll() {
        Specification<Lot> spec = LotSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Lot> findAll(Specification<Lot> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Lot> findAllDeleted(Specification<Lot> spec, Pageable pageable) {
        spec = LotSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Lot> findAllDeleted(Pageable pageable) {
        Specification<Lot> spec = LotSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Lot> findDeletedById(LotsId id) {
        Specification<Lot> spec = LotSpecifications.id(id).and(LotSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Lot> findOne(Specification<Lot> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<Lot> findById(LotsId id) {
        return repository.findById(id);
    }

    @Override
    public Lot save(Lot lot) {
        return repository.save(lot);
    }

    @Override
    public Lot create(LotRequest request) {
        Lot lot = new Lot();
        lot.setLotsId(request.getLotsId()); // Assuming lots_id can be null for auto-increment
        lot.setLotsVer(request.getLotsVer()); // Must be provided
        lot.setDescription(request.getDescription());

        if (request.getRealisationIds() != null && !request.getRealisationIds().isEmpty()) {
            Set<Realisation> realisations = new HashSet<>();
            for (Integer id : request.getRealisationIds()) { // Assuming Realisation still uses Integer ID
                Realisation realisation = realisationRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Realisation not found: " + id));
                realisations.add(realisation);
            }
            lot.setRealisations(realisations);
        }

        return repository.save(lot);
    }

    @Override
    public void delete(LotsId id) {
        repository.softDelete(id); // Assuming SoftDeleteRepository provides this method
    }
}