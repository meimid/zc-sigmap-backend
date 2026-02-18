package com.sigmap.common.process_verbal_reunion_extrait_dac;

import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.process_verbal_reunion_extrait.excepton.ExtraitNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcessVerbalReunionExtraitDacServiceImpl implements ProcessVerbalReunionExtraitDacService {

    private final ProcessVerbalReunionExtraitDacRepository repository;

    @Override
    public ProcessVerbalReunionExtraitDac create(ProcessVerbalReunionExtraitDac entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessVerbalReunionExtraitDac getById(
            Integer extraitId,
            Integer pvId,
            Integer acId,
            Integer dacId,
            Integer dacVer) {
        return repository.findById(new ProcessVerbalReunionExtraitDacId(extraitId, pvId, acId, dacId, dacVer)).orElseThrow(() ->
                new ExtraitNotFound("Extrait DAC relation not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessVerbalReunionExtraitDac> getByPvIdAndAcId(Integer pvId, Integer acId) {
        return repository.findByPvIdAndAcId(pvId, acId);
    }
    @Override
    public Optional<ProcessVerbalReunionExtraitDac> findExisting(
            Integer pvId,
            Integer acId,
            Integer dacId,
            Integer dacVer
    ) {
        return repository.findExistingDacLink(pvId, acId, dacId, dacVer);
    }

    @Override
    public List<ProcessVerbalReunionExtraitDac> findAll(Specification<ProcessVerbalReunionExtraitDac> spec) {
        return repository.findAll(spec);
    }

    @Override
    public Optional<ProcessVerbalReunionExtraitDac> findOne(Specification<ProcessVerbalReunionExtraitDac> spec) {
        return repository.findOne(spec);
    }

}