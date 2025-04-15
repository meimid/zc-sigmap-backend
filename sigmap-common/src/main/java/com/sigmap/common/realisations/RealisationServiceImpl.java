package com.sigmap.common.realisations;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaRepository;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.request.RealisationRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RealisationServiceImpl implements RealisationService {
    private final RealisationRepository repository;
    private final PaaRepository paaRepository;
    private final PpmRepository ppmRepository;
    private final LotRepository lotRepository;



    public RealisationServiceImpl(RealisationRepository repository, PaaRepository paaRepository, PpmRepository ppmRepository, LotRepository lotRepository) {
        this.repository = repository;
        this.paaRepository = paaRepository;
        this.ppmRepository = ppmRepository;
        this.lotRepository = lotRepository;
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


    @Transactional
    @Override
    public Realisation create(RealisationRequest request) throws RealisationAlreadyExistsException {
        Integer id = request.getId();
        Integer version = request.getVersion();

        // Generate ID if not provided
        if (id == null) {
            id = repository.findMaxId() + 1;
            System.out.println("Generated Realisation ID: " + id);
        }

        CompositeId compositeId = new CompositeId(id, version);

        // Check if realisation already exists
        if (repository.existsById(compositeId)) {
            throw new RealisationAlreadyExistsException(id, version,
                    "Realisation with ID " + id + " and version " + version + " already exists in the database.");
        }

        // Map request to entity
        Realisation realisation = RealisationMapper.INSTANCE.toEntity(request);
        realisation.setId(compositeId.getId());

        // Set empty associations initially
        realisation.setPaas(new ArrayList<>());
        realisation.setPpms(new ArrayList<>());
        realisation.setLots(new HashSet<>());
        realisation.setDocs(new ArrayList<>());
        // Save entity first (without associations)
        Realisation savedRealisation = repository.save(realisation);

        // Fetch associated PAAs if needed
        if (request.getPaaIds() != null && !request.getPaaIds().isEmpty()) {
            List<Paa> paas = request.getPaaIds().stream()
                    .peek(paaId -> System.out.println("Associating PAA ID: " + paaId.getId() + ", Version: " + paaId.getVersion()))
                    .map(paaId -> paaRepository.findById(new CompositeId(paaId.getId(), paaId.getVersion())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            savedRealisation.setPaas(paas);
        }

        // Fetch and set PPMs
        if (request.getPpmIds() != null) {
            List<Ppm> ppms = request.getPpmIds().stream()
                    .map(ppmId -> ppmRepository.findById(new CompositeId(ppmId.getId(), ppmId.getVersion())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            savedRealisation.setPpms(ppms);
        }

        // Fetch and set Lots
        if (request.getLotIds() != null) {
            Set<Lot> lots = request.getLotIds().stream()
                    .map(lotId -> lotRepository.findById(new CompositeId(lotId.getId(), lotId.getVersion())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            savedRealisation.setLots(lots);
        }

        ///TODO:add DOCs

        // Save again with associations
        return repository.save(savedRealisation);
    }

    @Override
    public Realisation update(Integer realisationId, Integer realisationVer, RealisationRequest request) throws RealisationNotFoundException {
        CompositeId compositeId = new CompositeId(realisationId, realisationVer);
        Optional<Realisation> existingRealisationOpt = repository.findById(compositeId);

        if (existingRealisationOpt.isEmpty()) {
            throw new RealisationNotFoundException(realisationId, realisationVer,
                    "Realisation with ID " + realisationId + " and version " + realisationVer + " was not found in the database.");
        }
        Realisation realisation = RealisationMapper.INSTANCE.updateRealisationFromRequest(request,existingRealisationOpt.get());

        return repository.save(realisation);
    }

    @Override
    public void delete(Integer realisationId, Integer realisationVer) {
        repository.softDelete(realisationId, realisationVer);
    }
}