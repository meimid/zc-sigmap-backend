package com.sigmap.common.process_verbal_reunion_extrait;


import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.process_verbal_reunion_extrait.excepton.ExtraitNotFound;
import com.sigmap.common.process_verbal_reunion_extrait.request.ProcessVerbalReunionExtraitRequest;
import com.sigmap.common.process_verbal_reunion_extrait.response.ProcessVerbalReunionExtraitRespons;
import com.sigmap.common.utils.SequenceGeneratorService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.util.List;

@Service
public class ProcessVerbalReunionExtraitServiceImpl implements ProcessVerbalReunionExtraitService {

    private final ProcessVerbalReunionExtraitRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final DocumentCommuneService documentCommuneService;

    public ProcessVerbalReunionExtraitServiceImpl(ProcessVerbalReunionExtraitRepository repository, SequenceGeneratorService sequenceGeneratorService, DocumentCommuneService documentCommuneService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.documentCommuneService = documentCommuneService;
    }

    @Override
    public Page<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId, Pageable pageable) {
        return repository.findAllByPvId(pvId, pageable);

    }

    @Override
    public List<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId) {
        return repository.findAllByPvId(pvId);
    }

    @Override
    public ProcessVerbalReunionExtrait create(ProcessVerbalReunionExtraitRequest request) throws IOException {

        ProcessVerbalReunionExtrait extrait = new ProcessVerbalReunionExtrait();

        extrait.setExtraitId(sequenceGeneratorService
                .getNextId("process_verbal_reunion_extrait_id_seq"));

        extrait.setPvId(request.getPvId());
        extrait.setAcId(request.getAcId());
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            Docs doc = documentCommuneService.processAndSaveDocumentsForPvExtrait(
                    request.getFile(),
                    request.getAcId(),
                    request.getPvId(),
                    Constants.PATH_ENTREPOT_GED_PV_EXTRAIT
            );
            extrait.setDoc(doc);
        }

        return repository.save(extrait);
    }

    @Override
    public ProcessVerbalReunionExtrait findByIds(ProcessVerbalReunionExtraitRequest request) {
        return repository.findByIds(request.getExtraitId(), request.getPvId(), request.getAcId()).orElseThrow(()-> new ExtraitNotFound(""));
    }
    @Override
    public ProcessVerbalReunionExtrait update(ProcessVerbalReunionExtraitRequest request) throws IOException {
        ProcessVerbalReunionExtrait extrait = repository
                .findByIds(request.getExtraitId(), request.getPvId(), request.getAcId())
                .orElseThrow(() -> new ExtraitNotFound("Extrait not found"));

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            Docs doc = documentCommuneService.processAndSaveDocumentsForPvExtrait(
                    request.getFile(),
                    request.getAcId(),
                    request.getPvId(),
                    Constants.PATH_ENTREPOT_GED_PV_EXTRAIT
            );
            extrait.setDoc(doc);
        }


        return repository.save(extrait);
    }
    @Override
    public List<Integer> findAcIdsWithExtraitByPvId(Integer pvId) {
        return repository.findAllByPvId(pvId).stream()
                .map(ProcessVerbalReunionExtrait::getAcId)
                .distinct()
                .toList();
    }

}