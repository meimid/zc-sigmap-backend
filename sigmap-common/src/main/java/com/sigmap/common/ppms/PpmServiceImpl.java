package com.sigmap.common.ppms;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.exception.UpdateDeletedPpm;
import com.sigmap.common.ppms.request.PpmRequest;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
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

@Service
public class PpmServiceImpl implements PpmService {
    private final PpmRepository repository;
    private final AcRepository acRepository;
    private final RefParamRepository refParamRepository;
    private final MessageSource messageSource;

    /**
     * Constructor.
     *
     * @param repository ppm repository
     *                    //TODO
//     * @param acService ac service
     */
    public PpmServiceImpl(PpmRepository repository, AcRepository acRepository, RefParamRepository refParamRepository, MessageSource messageSource) {
        this.repository = repository;
        this.acRepository = acRepository;
        this.refParamRepository = refParamRepository;
        this.messageSource = messageSource;
    }

    private Locale getLocal (){
        return LocaleContextHolder.getLocale();
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
    public Ppm findById(Integer ppmId, Integer ppmVer) throws PpmNotFound {
        return repository.findById(new CompositeId(ppmId, ppmVer)).orElseThrow(() -> new PpmNotFound("", List.of(ppmId), List.of(ppmVer)));
    }

    @Override
    public Ppm save(Ppm ppm) {
        return repository.save(ppm);
    }

    @Override
    public Ppm create(PpmRequest request) throws AcNotFound , ReferenceNotFound{
        Integer id = request.getId();
        if (id == null) {
            Integer maxId = repository.findMaxId();
            id = (maxId != null) ? maxId + 1 : 1;
            request.setId(id);
        }

        Integer version = request.getVersion() != null ? request.getVersion() : 1;
        request.setVersion(version);

        Ppm ppm = new Ppm();
        ppm.setId(id);
        ppm.setVersion(version);
        ppm.setRef(request.getRef());
        ppm.setNom(request.getNom());
        ppm.setAnnee(request.getAnnee());
        ppm.setBudgetEstime(request.getBudgetEstime());

        List<String> notFoundRefs = new ArrayList<>();

        // Set status if provided
        if (request.getStatus() != null) {
            ppm.setStatus(refParamRepository.findById(request.getStatus().getId()).get());
        }else {
            notFoundRefs.add(messageSource.getMessage("ppm.status", new Integer[]{request.getStatus().getId()}, getLocal()));
        }

        if (!notFoundRefs.isEmpty()) {
            throw new ReferenceNotFound(" " , List.of(String.join(", ", notFoundRefs)));
        }

        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new AcNotFound(" " , List.of(request.getAcId())));
            ppm.setAc(ac);
        }
        return repository.save(ppm);
    }

    @Override
    public Ppm update(Integer ppmId, Integer ppmVer, PpmRequest request) throws PpmNotFound, AcNotFound, ReferenceNotFound, UpdateDeletedPpm {
        CompositeId compositeId = new CompositeId(ppmId, ppmVer);
        Optional<Ppm> existingPpmOpt = repository.findById(compositeId);

        if (existingPpmOpt.isEmpty()) {
            throw new PpmNotFound("", List.of(ppmId), List.of(ppmVer));
        }

        Ppm existingPpm = existingPpmOpt.get();
        if (Boolean.TRUE.equals(existingPpm.getIsDeleted())) {
            throw new UpdateDeletedPpm("", List.of(ppmId), List.of(ppmVer));
        }

        // Update fields
        existingPpm.setRef(request.getRef());
        existingPpm.setNom(request.getNom());
        existingPpm.setAnnee(request.getAnnee());
        existingPpm.setBudgetEstime(request.getBudgetEstime());

        List<String> notFoundRefs = new ArrayList<>();

        // Update status if provided
        if (request.getStatus() != null) {
            existingPpm.setStatus(refParamRepository.findById(request.getStatus().getId()).get());
        } else {
            existingPpm.setStatus(null);
            notFoundRefs.add(messageSource.getMessage("ppm.status", new Integer[]{request.getStatus().getId()}, getLocal()));
        }
        if (!notFoundRefs.isEmpty()) {
            throw new ReferenceNotFound(" " , List.of(String.join(", ", notFoundRefs)));
        }

        if (request.getAcId() != null) {
            Ac ac = acRepository.findById(request.getAcId())
                    .orElseThrow(() -> new AcNotFound("", List.of(request.getAcId())));
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
