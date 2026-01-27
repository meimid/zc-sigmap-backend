package com.sigmap.common.process_verbal_reunion_extrait;

import com.sigmap.common.entity.ExtraitType;
import com.sigmap.common.process_verbal_reunion_extrait.request.ProcessVerbalReunionExtraitRequest;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProcessVerbalReunionExtraitService {
    Page<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId, Pageable pageable);
    List<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId);
    ProcessVerbalReunionExtrait createParAc(Integer pvId, Integer acId) ;
    ProcessVerbalReunionExtrait createParDossierWithPpm(Integer pvId, Integer acId, Integer ppmId, Integer ppmVer);ProcessVerbalReunionExtrait createParDossierWithDac(Integer pvId, Integer acId, Integer dacId, Integer dacVer);
    List<Integer> findAcIdsWithExtraitByPvId(Integer pvId);
    ProcessVerbalReunionExtrait findByExtraitId(Integer extraitId);
    ProcessVerbalReunionExtrait update( ProcessVerbalReunionExtraitRequest extraitRequest) throws IOException;

    List<ProcessVerbalReunionExtrait> findParDossierExtraitsByPpmAndAc(
            Integer ppmId,
            Integer ppmVer,
            Integer acId
    );

}
