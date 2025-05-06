package com.sigmap.common.lots;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.exception.LotNotFound;
import com.sigmap.common.lots.exception.UpdateDeletedLot;
import com.sigmap.common.lots.request.LotRequest;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
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
        Specification<Lot> deletedSpec = LotSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Lot> findAllDeleted(Pageable pageable) {
        Specification<Lot> spec = LotSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Lot> findDeletedById(Integer lotsId, Integer lotsVer) {
        Specification<Lot> spec = LotSpecifications.id(new CompositeId(lotsId, lotsVer))
                .and(LotSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Lot> findOne(Specification<Lot> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Lot findById(Integer lotsId, Integer lotsVer) throws LotNotFound {
        return repository.findById(new CompositeId(lotsId, lotsVer)).orElseThrow(() -> new LotNotFound("", List.of(lotsId), List.of(lotsVer)));
    }

    @Override
    public Lot save(Lot lot) {
        return repository.save(lot);
    }


    @Override
    public Lot create(LotRequest request) throws RealisationNotFoundException {
        Lot lot = new Lot();

        Integer id = request.getId();
        if (id == null) {
            Integer maxId = repository.findMaxId();
            id = (maxId != null) ? maxId + 1 : 1;
            System.out.println("Generated Lot ID: " + id);
        }

        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        lot.setId(id);
        lot.setVersion(version);
        lot.setDescription(request.getDescription());

        Lot savedLot = repository.save(lot);

        if (request.getRealisationIds() != null && !request.getRealisationIds().isEmpty()) {
            Set<Realisation> realisations = new HashSet<>();
            for (CompositeId realisationId : request.getRealisationIds()) {
                Realisation realisation = realisationRepository.findById(realisationId)
                        .orElseThrow(() -> new RealisationNotFoundException("",realisationId.getId(),realisationId.getVersion()));

                if (realisation.getLots() == null) {
                    realisation.setLots(new HashSet<>());
                }

                realisation.getLots().add(savedLot);
                realisations.add(realisation);
            }
            savedLot.setRealisations(realisations);

            savedLot = repository.save(savedLot);
        }

        return savedLot;
    }

    @Override
    public Lot update(Integer lotId, Integer lotVer, LotRequest request) throws LotNotFound, UpdateDeletedLot{
        CompositeId compositeId = new CompositeId(lotId, lotVer);
        Lot existingLot = repository.findById(compositeId)
                .orElseThrow(() -> new LotNotFound("" , List.of(lotId) , List.of(lotVer) ));
        if (Boolean.TRUE.equals(existingLot.getIsDeleted())) {
            throw new UpdateDeletedLot("" , List.of(lotId) , List.of(lotVer));
        }
        existingLot.setDescription(request.getDescription());

        if (request.getRealisationIds() != null && !request.getRealisationIds().isEmpty()) {
            Set<Realisation> realisations = new HashSet<>();
            for (CompositeId realisationId : request.getRealisationIds()) {
                Realisation realisation = realisationRepository.findById(realisationId)
                        .orElseThrow(() -> new RuntimeException("Realisation not found: " + realisationId));
                realisations.add(realisation);
            }
            existingLot.setRealisations(realisations);
        } else {
            existingLot.setRealisations(new HashSet<>()); // Clear associations if none provided
        }

        return repository.save(existingLot);
    }

    @Override
    public void delete(Integer lotsId, Integer lotsVer) {
       repository.softDelete(lotsId, lotsVer);
    }


}