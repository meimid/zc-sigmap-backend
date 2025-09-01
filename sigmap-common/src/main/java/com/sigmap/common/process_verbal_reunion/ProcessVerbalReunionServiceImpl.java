package com.sigmap.common.process_verbal_reunion;


import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.config.Constants;
import com.sigmap.common.process_verbal_reunion.exception.ProcessVerbalReunionNotFound;
import com.sigmap.common.process_verbal_reunion.mapper.ProcessVerbalReunionMapper;
import com.sigmap.common.process_verbal_reunion.request.ProcessVerbalReunionRequest;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class ProcessVerbalReunionServiceImpl  implements ProcessVerbalReunionService {
    private final ProcessVerbalReunionRespository respository;
    private final DocumentCommuneService documentCommuneService;


    public ProcessVerbalReunionServiceImpl(ProcessVerbalReunionRespository respository, DocumentCommuneService documentCommuneService) {
        this.respository = respository;
        this.documentCommuneService = documentCommuneService;
    }



    @Override
    public Optional<ProcessVerbalReunion> findOne(Specification<ProcessVerbalReunion> spec) {
        return respository.findOne(spec);
    }

    @Override
    public ProcessVerbalReunion findById(Integer id) {
        Specification<ProcessVerbalReunion> specification = ProcessVerbalReunionSpecifications.id(id)
                .and(ProcessVerbalReunionSpecifications.deleted(false));
        return findOne(specification)
                .orElseThrow(() -> new ProcessVerbalReunionNotFound("",id));
    }

    @Override
    public ProcessVerbalReunion save(ProcessVerbalReunion processVerbalReunion) {
       return respository.save(processVerbalReunion);
    }
    @Override
    @Transactional
    public ProcessVerbalReunion create(ProcessVerbalReunionRequest request) {
        ProcessVerbalReunion processVerbalReunion = ProcessVerbalReunionMapper.INSTANCE.map(request);
        return respository.save(processVerbalReunion);
    }
    @Override
    @Transactional
    public ProcessVerbalReunion update(Integer id, ProcessVerbalReunionRequest request) {
        // Retrieve the existing ProcessVerbalReunion by ID
        ProcessVerbalReunion processVerbalReunion = findById(id);

        // Update fields from the request using the mapper
        ProcessVerbalReunionMapper.INSTANCE.updateFromRequest(request, processVerbalReunion);


        return respository.save(processVerbalReunion);
    }

    @Override
    public ProcessVerbalReunion injectPvForReunion(Integer pvId, MultipartFile file) {
        ProcessVerbalReunion processVerbalReunion = findById(pvId);

        if (file != null && !file.isEmpty()) {
            try {
                documentCommuneService.processAndSaveDocumentsForPV(file, processVerbalReunion, Constants.PATH_ENTREPOT_GED_REUNION_PV);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process uploaded file: " + e.getMessage(), e);
            }
        }
        return respository.save(processVerbalReunion);

    }
}
