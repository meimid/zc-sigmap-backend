package com.sigmap.common.process_verbal_reunion_extrait;

import com.sigmap.common.process_verbal_reunion_extrait.request.ProcessVerbalReunionExtraitRequest;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcessVerbalReunionExtraitService {
    Page<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId, Pageable pageable);
    List<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId);
    ProcessVerbalReunionExtrait create(ProcessVerbalReunionExtraitRequest extrait) throws IOException;
    ProcessVerbalReunionExtrait findByIds(ProcessVerbalReunionExtraitRequest request);
    ProcessVerbalReunionExtrait update( ProcessVerbalReunionExtraitRequest request) throws IOException;
    List<Integer> findAcIdsWithExtraitByPvId(Integer pvId);


}
