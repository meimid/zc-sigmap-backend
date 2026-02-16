package com.sigmap.common.process_verbal_reunion_extrait_dac;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface ProcessVerbalReunionExtraitDacService {

    ProcessVerbalReunionExtraitDac create(ProcessVerbalReunionExtraitDac entity);

    ProcessVerbalReunionExtraitDac getById(
            Integer extraitId,
            Integer pvId,
            Integer acId,
            Integer dacId,
            Integer dacVer
    );

    List<ProcessVerbalReunionExtraitDac> getByPvIdAndAcId(Integer pvId, Integer acId);
    Optional<ProcessVerbalReunionExtraitDac> findExisting(
            Integer pvId,
            Integer acId,
            Integer dacId,
            Integer dacVer
    );

    List<ProcessVerbalReunionExtraitDac> findAll(Specification<ProcessVerbalReunionExtraitDac> spec);

    Optional<ProcessVerbalReunionExtraitDac> findOne(Specification<ProcessVerbalReunionExtraitDac> spec);
}