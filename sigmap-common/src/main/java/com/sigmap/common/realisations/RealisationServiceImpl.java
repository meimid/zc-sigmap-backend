package com.sigmap.common.realisations;


import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.lots.exception.LotNotFound;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaRepository;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ref_params.RefParamRepository;

import com.sigmap.common.ppms.exception.PpmNotFound;

import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.exceptions.RealisationsMinimumRequirement;
import com.sigmap.common.realisations.exceptions.UpdateDeletedRealisation;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.request.RealisationRequest;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RealisationServiceImpl implements RealisationService {
    private final RealisationRepository repository;
    private final PaaRepository paaRepository;
    private final PpmRepository ppmRepository;
    private final LotRepository lotRepository;
    private final RefParamRepository refParamRepository;
    private final MessageSource messageSource;

    public RealisationServiceImpl(RealisationRepository repository, PaaRepository paaRepository, PpmRepository ppmRepository, LotRepository lotRepository, RefParamRepository refParamRepository, MessageSource messageSource) {
        this.repository = repository;
        this.paaRepository = paaRepository;
        this.ppmRepository = ppmRepository;
        this.lotRepository = lotRepository;
        this.refParamRepository = refParamRepository;
        this.messageSource = messageSource;
    }

    private Locale getLocal (){
        return LocaleContextHolder.getLocale();
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
    public Realisation findById(Integer realisationId, Integer realisationVer) throws RealisationNotFoundException {
        return repository.findById(new CompositeId(realisationId, realisationVer)).orElseThrow(() -> new RealisationNotFoundException("", realisationId, realisationVer));
    }

    @Override
    public Realisation save(Realisation realisation) {
        return repository.save(realisation);
    }

    @Transactional
    @Override
    public Realisation create(RealisationRequest request) throws RealisationAlreadyExistsException, RealisationsMinimumRequirement, PaaNotFound, PpmNotFound, LotNotFound, ReferenceNotFound {
        Integer id = request.getId();
        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        // Generate ID if not provided
        if (id == null) {
            id = repository.findMaxId() + 1;
        }
        CompositeId compositeId = new CompositeId(id, version);
        // Check if realisation already exists
        if (repository.existsById(compositeId)) {
                throw new RealisationAlreadyExistsException("",List.of(id), List.of(version));
        }

        // Validate that at least one of Paa and one of Ppm is provided
        if ((request.getPaaIds() == null || request.getPaaIds().isEmpty()) ||  (request.getPpmIds() == null || request.getPpmIds().isEmpty())) {
            throw new RealisationsMinimumRequirement("");
        }

        // Map request to entity
        Realisation realisation = RealisationMapper.INSTANCE.toEntity(request);
        realisation.setId(compositeId.getId());
        realisation.setVersion(version);

        List<String> notFoundRefs = new ArrayList<>();

        if (request.getService() != null) {
            realisation.setService(refParamRepository.findById(request.getService().getId()).get());
        } else {
            notFoundRefs.add(messageSource.getMessage("realisation.service", null, getLocal()));
        }

        if (request.getDirection() != null) {
            realisation.setDirection(refParamRepository.findById(request.getDirection().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.direction", null, getLocal()));
        }

        if (request.getSourceFinancement() != null) {
            realisation.setSourceFinancement( refParamRepository.findById(request.getSourceFinancement().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.source.financement", null, getLocal()));
        }

        if (request.getMonais() != null) {
            realisation.setMonais(refParamRepository.findById(request.getMonais().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.type.monais", null, getLocal()));
        }

        if (request.getTypeMarcher() != null) {
           realisation.setTypeMarcher(refParamRepository.findById(request.getTypeMarcher().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.type.marche", null, getLocal()));
        }

        if (request.getModeSelection() != null) {
            realisation.setModeSelection(refParamRepository.findById(request.getModeSelection().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.mode.selection", null, getLocal()));
        }

        if (!notFoundRefs.isEmpty()) {
            throw new ReferenceNotFound(" " , List.of(String.join(", ", notFoundRefs)));
        }

        // Set empty associations initially
        realisation.setPaas(new ArrayList<>());
        realisation.setPpms(new ArrayList<>());
        realisation.setLots(new HashSet<>());
        realisation.setDocs(new ArrayList<>());


        // Save entity first (without associations)
        Realisation savedRealisation = repository.save(realisation);

        // Fetch associated PAAs if needed
        if (request.getPaaIds() != null && !request.getPaaIds().isEmpty()) {
            List<Paa> paas = new ArrayList<>();
            for (CompositeId paaId : request.getPaaIds()) {
                Optional<Paa> paaOptional = paaRepository.findById(new CompositeId(paaId.getId(), paaId.getVersion()));
                if (paaOptional.isPresent()) {
                    paas.add(paaOptional.get());
                } else {
                    throw new PaaNotFound(" ",List.of(paaId.getId()),List.of(paaId.getVersion()) );
                }
            }
            savedRealisation.setPaas(paas);
        }

        // Fetch and set PPMs
        if (request.getPpmIds() != null && !request.getPpmIds().isEmpty()) {
            List<Ppm> ppms = new ArrayList<>();
            for (CompositeId ppmId : request.getPpmIds()) {
                Optional<Ppm> ppmOptional = ppmRepository.findById(new CompositeId(ppmId.getId(), ppmId.getVersion()));
                if (ppmOptional.isPresent()) {
                    ppms.add(ppmOptional.get());
                } else {
                    throw new PpmNotFound(" ",List.of(ppmId.getId()),List.of(ppmId.getVersion()));
                }
            }
            savedRealisation.setPpms(ppms);
        }

        // Fetch and set Lots
        if (request.getLotIds() != null && !request.getLotIds().isEmpty()) {
            Set<Lot> lots = new HashSet<>();
            for (CompositeId lotId : request.getLotIds()) {
                Optional<Lot> lotOptional = lotRepository.findById(new CompositeId(lotId.getId(), lotId.getVersion()));
                if (lotOptional.isPresent()) {
                    lots.add(lotOptional.get());
                } else {
                    throw new LotNotFound(" ",List.of(lotId.getId()),List.of(lotId.getVersion()));
                }
            }
            savedRealisation.setLots(lots);
        }

        ///TODO: add DOCs

        // Save again with associations
        return repository.save(savedRealisation);
    }
    @Override
    public Realisation update(Integer realisationId, Integer realisationVer, RealisationRequest request) throws ReferenceNotFound, PaaNotFound, PpmNotFound,LotNotFound, RealisationsMinimumRequirement, RealisationNotFoundException,UpdateDeletedRealisation {
        CompositeId compositeId = new CompositeId(realisationId, realisationVer);
        Optional<Realisation> existingRealisationOpt = repository.findById(compositeId);

        // Check if realisation exists
        if (existingRealisationOpt.isEmpty()) {
            throw new RealisationNotFoundException("",realisationId, realisationVer);
        }
        Realisation existingRealisation = existingRealisationOpt.get();

        // Check if realisation is deleted
        if (Boolean.TRUE.equals(existingRealisation.getIsDeleted())) {
            throw new UpdateDeletedRealisation("",realisationId, realisationVer);
        }

        List<String> notFoundRefs = new ArrayList<>();

        if (request.getService() != null) {
            existingRealisation.setService(refParamRepository.findById(request.getService().getId()).get());
        } else {
            notFoundRefs.add(messageSource.getMessage("realisation.service", null, getLocal()));
        }

        if (request.getDirection() != null) {
            existingRealisation.setDirection(refParamRepository.findById(request.getDirection().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.direction", null, getLocal()));
        }

        if (request.getSourceFinancement() != null) {
            existingRealisation.setSourceFinancement( refParamRepository.findById(request.getSourceFinancement().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.source.financement", null, getLocal()));
        }

        if (request.getMonais() != null) {
            existingRealisation.setMonais(refParamRepository.findById(request.getMonais().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.monais", null, getLocal()));
        }

        if (request.getTypeMarcher() != null) {
            existingRealisation.setTypeMarcher(refParamRepository.findById(request.getTypeMarcher().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.type.marche", null, getLocal()));
        }

        if (request.getModeSelection() != null) {
            existingRealisation.setModeSelection(refParamRepository.findById(request.getModeSelection().getId()).get());
        }else{
            notFoundRefs.add(messageSource.getMessage("realisation.mode.selection", null, getLocal()));
        }

        if (!notFoundRefs.isEmpty()) {
            throw new ReferenceNotFound(" " , List.of(String.join(", ", notFoundRefs)));
        }

        // Validate that at least one of Paa or Ppm is provided
        if ((request.getPaaIds() == null || request.getPaaIds().isEmpty()) &&
                (request.getPpmIds() == null || request.getPpmIds().isEmpty())) {
            throw new RealisationsMinimumRequirement("");
        }

        // Update realisation entity from request
        Realisation realisation = RealisationMapper.INSTANCE.updateRealisationFromRequest(request,existingRealisationOpt.get());

        // Clear existing associations and reset
        realisation.setPaas(new ArrayList<>());
        realisation.setPpms(new ArrayList<>());
        realisation.setLots(new HashSet<>());
        realisation.setDocs(new ArrayList<>());

        // Fetch and set associated PAAs
        if (request.getPaaIds() != null && !request.getPaaIds().isEmpty()) {
            List<Paa> paas = new ArrayList<>();
            for (CompositeId paaId : request.getPaaIds()) {
                Optional<Paa> paaOptional = paaRepository.findById(new CompositeId(paaId.getId(), paaId.getVersion()));
                if (paaOptional.isPresent()) {
                    paas.add(paaOptional.get());
                } else {
                    throw new PaaNotFound(" ", List.of(paaId.getId()), List.of(paaId.getVersion()));
                }
            }
            realisation.setPaas(paas);
        }
        // Fetch and set PPMs
        if (request.getPpmIds() != null && !request.getPpmIds().isEmpty()) {
            List<Ppm> ppms = new ArrayList<>();
            for (CompositeId ppmId : request.getPpmIds()) {
                Optional<Ppm> ppmOptional = ppmRepository.findById(new CompositeId(ppmId.getId(), ppmId.getVersion()));
                if (ppmOptional.isPresent()) {
                    ppms.add(ppmOptional.get());
                } else {
                    throw new PpmNotFound(" ", List.of(ppmId.getId()), List.of(ppmId.getVersion()));
                }
            }
            realisation.setPpms(ppms);
        }

        // Fetch and set Lots
        if (request.getLotIds() != null && !request.getLotIds().isEmpty()) {
            Set<Lot> lots = new HashSet<>();
            for (CompositeId lotId : request.getLotIds()) {
                Optional<Lot> lotOptional = lotRepository.findById(new CompositeId(lotId.getId(), lotId.getVersion()));
                if (lotOptional.isPresent()) {
                    lots.add(lotOptional.get());
                } else {
                    throw new LotNotFound(" ", List.of(lotId.getId()), List.of(lotId.getVersion()));
                }
            }
            realisation.setLots(lots);
        }

        ///TODO: add DOCs

        return repository.save(realisation);
    }

    @Override
    public void delete(Integer realisationId, Integer realisationVer) {
        repository.softDelete(realisationId, realisationVer);
    }
}