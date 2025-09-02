package com.sigmap.common.process_verbal_reunion;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.process_verbal_reunion.request.ProcessVerbalReunionRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ProcessVerbalReunionService {
    Optional<ProcessVerbalReunion> findOne(Specification<ProcessVerbalReunion> spec);
    ProcessVerbalReunion findById(Integer id);
    public ProcessVerbalReunion save(ProcessVerbalReunion processVerbalReunion);

    ProcessVerbalReunion create(ProcessVerbalReunionRequest request);
    ProcessVerbalReunion update(Integer id,ProcessVerbalReunionRequest request);
    ProcessVerbalReunion injectPvForReunion(Integer pvId, MultipartFile file);
}
