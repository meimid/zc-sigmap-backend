package com.sigmap.common.process_verbal_reunion_extrait_ppm;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface ProcessVerbalReunionExtraitPpmService {

    ProcessVerbalReunionExtraitPpm create(ProcessVerbalReunionExtraitPpm entity);

    ProcessVerbalReunionExtraitPpm getById(
            Integer extraitId,
            Integer pvId,
            Integer acId,
            Integer ppmId,
            Integer ppmVer
    );

    List<ProcessVerbalReunionExtraitPpm> getByExtrait(Integer pvId, Integer acId);
    Optional<ProcessVerbalReunionExtraitPpm> findExisting(
            Integer pvId,
            Integer acId,
            Integer ppmId,
            Integer ppmVer
    );

    List<ProcessVerbalReunionExtraitPpm> findExistingCompletedExtraits(
            Integer acId,
            Integer ppmId,
            Integer ppmVer
    );

    Optional<ProcessVerbalReunionExtraitPpm> findOne(Specification<ProcessVerbalReunionExtraitPpm> spec);
}