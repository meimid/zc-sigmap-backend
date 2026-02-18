package com.sigmap.common.process_verbal_reunion_extrait_ppm;

import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.process_verbal_reunion_extrait.excepton.ExtraitNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcessVerbalReunionExtraitPpmServiceImpl
        implements ProcessVerbalReunionExtraitPpmService {

    private final ProcessVerbalReunionExtraitPpmRepository repository;

    @Override
    public ProcessVerbalReunionExtraitPpm create(ProcessVerbalReunionExtraitPpm entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessVerbalReunionExtraitPpm getById(
            Integer extraitId,
            Integer pvId,
            Integer acId,
            Integer ppmId,
            Integer ppmVer
    ) {
        return repository.findById(
                new ProcessVerbalReunionExtraitPpmId(
                        extraitId, pvId, acId, ppmId, ppmVer)).orElseThrow(() ->
                new ExtraitNotFound("Extrait PPM relation not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessVerbalReunionExtraitPpm> getByExtrait(
            Integer pvId,
            Integer acId
    ) {
        return repository.findByPvIdAndAcId(pvId, acId);
    }

    @Override
    public Optional<ProcessVerbalReunionExtraitPpm> findExisting(
            Integer pvId,
            Integer acId,
            Integer ppmId,
            Integer ppmVer
    ) {
        return repository.findExistingPpmLink(pvId, acId, ppmId, ppmVer);
    }

    @Override
    public List<ProcessVerbalReunionExtraitPpm> findExistingCompletedExtraits(
            Integer acId,
            Integer ppmId,
            Integer ppmVer
    ) {
        return repository.findExistingExtraitByAcIdAndPpmIdAndPpmVer(
                acId,
                ppmId,
                ppmVer
        );
    }
    @Override
    public Optional<ProcessVerbalReunionExtraitPpm> findOne(Specification<ProcessVerbalReunionExtraitPpm> spec) {
        return repository.findOne(spec);
    }

    @Override
    public List<ProcessVerbalReunionExtraitPpm> findAll(Specification<ProcessVerbalReunionExtraitPpm> spec) {
        return repository.findAll(spec);
    }
}