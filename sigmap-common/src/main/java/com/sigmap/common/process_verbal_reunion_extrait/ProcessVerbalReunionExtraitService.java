package com.sigmap.common.process_verbal_reunion_extrait;

import com.sigmap.common.process_verbal_reunion_extrait.request.ProcessVerbalReunionExtraitRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ProcessVerbalReunionExtraitService {
    Page<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId, Pageable pageable);
    List<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId);
    ProcessVerbalReunionExtrait createParAc(Integer pvId, Integer acId) ;
    ProcessVerbalReunionExtrait createParDossierWithPpm(Integer pvId, Integer acId, Integer ppmId, Integer ppmVer);ProcessVerbalReunionExtrait createParDossierWithDac(Integer pvId, Integer acId, Integer dacId, Integer dacVer);
    List<Integer> findAcIdsWithExtraitByPvId(Integer pvId);
    ProcessVerbalReunionExtrait findByExtraitId(Integer extraitId);
    ProcessVerbalReunionExtrait update( ProcessVerbalReunionExtraitRequest extraitRequest) throws IOException;

    List<ProcessVerbalReunionExtrait> findExtraitsByPpmAndAcs(

            List<Integer> acIds,
            Integer ppmId,
            Integer ppmVer
    );
    List<ProcessVerbalReunionExtrait> findExtraitsByDacAndAcs(
            List<Integer> acIds,
            Integer dacId,
            Integer dacVer
    );
    Optional<ProcessVerbalReunionExtrait> findByExtraitIdPvIdAcId(Integer extraitId, Integer pvId, Integer acId);
    Optional<ProcessVerbalReunionExtrait> findOne(Specification<ProcessVerbalReunionExtrait> spec);
}
