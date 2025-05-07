package com.sigmap.common.paas;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.paas.exceptions.PaaAlreadyExistsException;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.mappers.PaaMapper;
import com.sigmap.common.paas.request.PaaRequest;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.exceptions.RealisationsMinimumRequirement;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Service implementation of {@link PaaService}
 */
@Service
public class PaaServiceImpl implements PaaService {
    private final PaaRepository repository;
    private final RealisationRepository realisationRepository;
    private final RefParamRepository refParamRepository;
    private final MessageSource messageSource;

    public PaaServiceImpl(PaaRepository repository, RealisationRepository realisationRepository, RefParamRepository refParamRepository, MessageSource messageSource) {
        this.repository = repository;
        this.realisationRepository = realisationRepository;
        this.refParamRepository = refParamRepository;
        this.messageSource = messageSource;
    }

    private Locale getLocal (){
        return LocaleContextHolder.getLocale();
    }


    @Override
    public List<Paa> findAll() {
        Specification<Paa> spec = PaaSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public Page<Paa> findAll(Specification<Paa> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<Paa> findAllDeleted(Specification<Paa> spec, Pageable pageable) {
        Specification<Paa> deletedSpec = PaaSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<Paa> findAllDeleted(Pageable pageable) {
        Specification<Paa> spec = PaaSpecifications.deleted(true);
        return repository.findAll(spec, pageable);
    }

    @Override
    public Optional<Paa> findDeletedById(Integer paaId, Integer paaVer) {
        Specification<Paa> spec = PaaSpecifications.id(new CompositeId(paaId, paaVer))
                .and(PaaSpecifications.deleted(true));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Paa> findOne(Specification<Paa> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Paa findById(Integer paaId, Integer paaVer) throws PaaNotFound {
        if (paaId == null || paaVer == null) {
            throw new PaaNotFound("Invalid ID or version provided",
                    List.of(paaId),
                    List.of(paaVer));
        }
        return repository.findById(new CompositeId(paaId, paaVer))
                .orElseThrow(() -> new PaaNotFound("", List.of(paaId), List.of(paaVer)));
    }

    @Override
    public Paa save(Paa paa) {
        return repository.save(paa);
    }

    @Override
    public Paa create(PaaRequest request) throws PaaAlreadyExistsException, ReferenceNotFound, RealisationsMinimumRequirement {
        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        Integer id = request.getId();
        if (id == null) {
            id = repository.findMaxId();
            if (id == null) {
                id = 1;
            } else {
                id++;
            }
        }

        Optional<Paa> existingPaa = repository.findById(new CompositeId(id, version));
        if (existingPaa.isPresent()) {
            throw new PaaAlreadyExistsException("", id, version);
        }

        Paa paa = PaaMapper.INSTANCE.toEntity(request);
        paa.setRealisations(new ArrayList<>());

        List<String> notFoundRefs = new ArrayList<>();

        // Set status from RefParam
        if (request.getStatus() != null) {
            paa.setStatus(refParamRepository.findById(request.getStatus().getId()).get());
        } else {
            notFoundRefs.add(messageSource.getMessage("ppm.status", new Integer[]{request.getStatus().getId()}, getLocal()));
        }

        if (!notFoundRefs.isEmpty()) {
            throw new ReferenceNotFound(" " , List.of(String.join(", ", notFoundRefs)));
        }

        paa.setId(id);
        paa.setVersion(version);

        Paa savedPaa = repository.save(paa);

        if (request.getRealisationIds() != null && !request.getRealisationIds().isEmpty()) {
            List<Realisation> realisations = realisationRepository.findAllById(request.getRealisationIds());
            if (realisations.size() != request.getRealisationIds().size()) {
                throw new RealisationsMinimumRequirement("Some realisations were not found");
            }

            // Update both sides of the relationship in memory
            for (Realisation realisation : realisations) {
                if (realisation.getPaas() == null) {
                    realisation.setPaas(new ArrayList<>());
                }
                if (!realisation.getPaas().contains(savedPaa)) {
                    realisation.getPaas().add(savedPaa);
                }
            }

            // Save all realisations in a single batch operation
            realisationRepository.saveAll(realisations);

            // Update and save PAA with the realisations
            savedPaa.setRealisations(realisations);
            savedPaa = repository.save(savedPaa);
        }

        return savedPaa;
    }

    @Override
    public void delete(Integer paaId, Integer paaVer) {
        repository.softDelete(paaId, paaVer);
    }

    @Override
    public Paa update(Integer paaId, Integer paaVer, PaaRequest request) throws PaaNotFound {
        CompositeId compositeId = new CompositeId(paaId, paaVer);
        Optional<Paa> existingPaaOpt = repository.findById(compositeId);

        if (existingPaaOpt.isEmpty()) {
            throw new PaaNotFound("", List.of(paaId), List.of(paaVer));
        }

        Paa existingPaa = existingPaaOpt.get();
        if (Boolean.TRUE.equals(existingPaa.getIsDeleted())) {
            throw new PaaNotFound("", List.of(paaId), List.of(paaVer));
        }

        Paa paa = PaaMapper.INSTANCE.updatePaaFromRequest(request, existingPaaOpt.get());
        return repository.save(paa);
    }
}