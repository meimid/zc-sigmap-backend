package com.sigmap.common.process_verbal_reunion_extrait;


import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.config.Constants;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.ExtraitType;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.process_verbal_reunion_extrait.excepton.ExtraitNotFound;
import com.sigmap.common.process_verbal_reunion_extrait.request.ProcessVerbalReunionExtraitRequest;
import com.sigmap.common.process_verbal_reunion_extrait_dac.ProcessVerbalReunionExtraitDac;
import com.sigmap.common.process_verbal_reunion_extrait_dac.ProcessVerbalReunionExtraitDacService;
import com.sigmap.common.process_verbal_reunion_extrait_ppm.ProcessVerbalReunionExtraitPpm;
import com.sigmap.common.process_verbal_reunion_extrait_ppm.ProcessVerbalReunionExtraitPpmService;
import com.sigmap.common.utils.SequenceGeneratorService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ProcessVerbalReunionExtraitServiceImpl implements ProcessVerbalReunionExtraitService {

    private final ProcessVerbalReunionExtraitRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final DocumentCommuneService documentCommuneService;
    private final ProcessVerbalReunionExtraitPpmService extraitPpmService;
    private final ProcessVerbalReunionExtraitDacService extraitDacService;

    public ProcessVerbalReunionExtraitServiceImpl(ProcessVerbalReunionExtraitRepository repository, SequenceGeneratorService sequenceGeneratorService, DocumentCommuneService documentCommuneService, ProcessVerbalReunionExtraitPpmService extraitPpmService, ProcessVerbalReunionExtraitDacService extraitDacService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.documentCommuneService = documentCommuneService;
        this.extraitPpmService = extraitPpmService;
        this.extraitDacService = extraitDacService;
    }

    @Override
    public Page<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId, Pageable pageable) {
        return repository.findAllByPvIdAndIsDeletedFalse(pvId, pageable);

    }

    @Override
    public List<ProcessVerbalReunionExtrait> findAllByPvId(Integer pvId) {
        return repository.findAllByPvIdAndIsDeletedFalse(pvId).stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProcessVerbalReunionExtrait createParAc(Integer pvId, Integer acId) {
        Optional<ProcessVerbalReunionExtrait> existing =
                repository.findParAcByPvIdAndAcId(pvId, acId);

        if (existing.isPresent()) {
            throw new IllegalStateException(
                    "PAR_AC extrait already exists for pv=" + pvId + " ac=" + acId
            );
        }
        ProcessVerbalReunionExtrait extrait = new ProcessVerbalReunionExtrait();
        extrait.setExtraitId(sequenceGeneratorService.getNextId("process_verbal_reunion_extrait_id_seq"));
        extrait.setPvId(pvId);
        extrait.setAcId(acId);
        extrait.setExtraitType(ExtraitType.PAR_AC);
        return repository.save(extrait);
    }
    @Override
    @Transactional
    public ProcessVerbalReunionExtrait createParDossierWithPpm(Integer pvId, Integer acId, Integer ppmId, Integer ppmVer) {
        Optional<ProcessVerbalReunionExtraitPpm> optionalProcessVerbalReunionExtraitPpm=
                extraitPpmService.findExisting(pvId, acId, ppmId, ppmVer);
        if (optionalProcessVerbalReunionExtraitPpm.isPresent()) {
            throw new IllegalStateException("Extrait already exists for this dossier");
        }

        // 1) create main extrait row
        ProcessVerbalReunionExtrait extrait = new ProcessVerbalReunionExtrait();
        extrait.setExtraitId(sequenceGeneratorService.getNextId("process_verbal_reunion_extrait_id_seq"));
        extrait.setPvId(pvId);
        extrait.setAcId(acId);
        extrait.setExtraitType(ExtraitType.PAR_DOSSIER);
        extrait = repository.save(extrait);

        ProcessVerbalReunionExtraitPpm processVerbalReunionExtraitPpm = new ProcessVerbalReunionExtraitPpm();
        processVerbalReunionExtraitPpm.setPpmId(ppmId);
        processVerbalReunionExtraitPpm.setPvId(pvId);
        processVerbalReunionExtraitPpm.setPpmVer(ppmVer);
        processVerbalReunionExtraitPpm.setAcId(acId);
        processVerbalReunionExtraitPpm.setExtraitId(extrait.getExtraitId());
        processVerbalReunionExtraitPpm.setPpm(new Ppm());
        processVerbalReunionExtraitPpm.getPpm().setId(ppmId);
        processVerbalReunionExtraitPpm.getPpm().setVersion(ppmVer);
        extraitPpmService.create(processVerbalReunionExtraitPpm);

        return extrait;
    }

    @Override
    @Transactional
    public ProcessVerbalReunionExtrait createParDossierWithDac(
            Integer pvId,
            Integer acId,
            Integer dacId,
            Integer dacVer
    ) {
        Optional<ProcessVerbalReunionExtraitDac> optionalProcessVerbalReunionExtraitDac=
                extraitDacService.findExisting(pvId, acId, dacId, dacVer);
        if (optionalProcessVerbalReunionExtraitDac.isPresent()) {
            throw new IllegalStateException("Extrait already exists for this dossier");
        }

        // 1) create main extrait row
        ProcessVerbalReunionExtrait extrait = new ProcessVerbalReunionExtrait();
        extrait.setExtraitId(
                sequenceGeneratorService.getNextId("process_verbal_reunion_extrait_id_seq")
        );
        extrait.setPvId(pvId);
        extrait.setAcId(acId);
        extrait.setExtraitType(ExtraitType.PAR_DOSSIER);

        extrait = repository.save(extrait);

        // 2) create join DAC row (same pattern as PPM)

        ProcessVerbalReunionExtraitDac processVerbalReunionExtraitDac =
                new ProcessVerbalReunionExtraitDac();

        processVerbalReunionExtraitDac.setExtraitId(extrait.getExtraitId());
        processVerbalReunionExtraitDac.setPvId(pvId);
        processVerbalReunionExtraitDac.setAcId(acId);
        processVerbalReunionExtraitDac.setDacId(dacId);
        processVerbalReunionExtraitDac.setDacVer(dacVer);
        processVerbalReunionExtraitDac.setDac(new Dac());
        processVerbalReunionExtraitDac.getDac().setDacId(dacId);
        processVerbalReunionExtraitDac.getDac().setDacVer(dacVer);

        extraitDacService.create(processVerbalReunionExtraitDac);

        return extrait;
    }
    @Override
    public ProcessVerbalReunionExtrait findByExtraitId(Integer extraitId) {
        return repository.findByExtraitIdAndIsDeletedFalse(extraitId)
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .orElseThrow(() ->
                        new ExtraitNotFound("Extrait not found with id " + extraitId)
                );

    }
    @Override
    public ProcessVerbalReunionExtrait update( ProcessVerbalReunionExtraitRequest extraitRequest) throws IOException {

        ProcessVerbalReunionExtrait extrait = findByExtraitId(extraitRequest.getExtraitId());
        if (extraitRequest.getFile() != null && !extraitRequest.getFile().isEmpty()) {
            Docs doc = documentCommuneService.processAndSaveDocumentsForPvExtrait(
                    extraitRequest.getFile(),
                    extrait.getAcId(),
                    extrait.getPvId(),
                    Constants.PATH_ENTREPOT_GED_PV_EXTRAIT
            );
            extrait.setDoc(doc);
        }

        return repository.save(extrait);
    }

    @Override
    public List<Integer> findAcIdsWithExtraitByPvId(Integer pvId) {
        return repository.findAllByPvIdAndIsDeletedFalse(pvId).stream()
                .map(ProcessVerbalReunionExtrait::getAcId)
                .distinct()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessVerbalReunionExtrait> findExtraitsByPpmAndAcs(

            List<Integer> acIds,
            Integer ppmId,
            Integer ppmVer
    ) {

        if (ppmId == null || ppmVer == null || acIds == null) {
            throw new IllegalArgumentException("ppmId, ppmVer and acId must not be null");
        }


        List<ProcessVerbalReunionExtrait> extraits = repository.findExtraitsByPpmAndAcs(acIds, ppmId, ppmVer);
        log.info("extraits size = {}", extraits.size());
        extraits.forEach(e -> log.info("extraitId={} type={} doc?={}", e.getExtraitId(), e.getExtraitType(), e.getDoc()!=null));
        return extraits;
    }

    @Override
    public List<ProcessVerbalReunionExtrait> findExtraitsByDacAndAcs(
            List<Integer> acIds,
            Integer dacId,
            Integer dacVer
    ) {
        if (dacId == null || dacVer == null || acIds == null) {
            throw new IllegalArgumentException("dacId, dacVer and acIds must not be null");
        }

        List<ProcessVerbalReunionExtrait> extraits = repository.findExtraitsByDacAndAcs(acIds, dacId, dacVer);
        log.info("extraits size = {}", extraits.size());
        extraits.forEach(e -> log.info("extraitId={} type={} doc?={}", e.getExtraitId(), e.getExtraitType(), e.getDoc()!=null));
        return extraits;
    }


    @Override
    public Optional<ProcessVerbalReunionExtrait> findByExtraitIdPvIdAcId(Integer extraitId, Integer pvId, Integer acId) {
        return repository.findByExtraitIdPvIdAcId(extraitId, pvId, acId);
    }

    @Override
    public Optional<ProcessVerbalReunionExtrait> findOne(Specification<ProcessVerbalReunionExtrait> spec) {
        return repository.findOne(spec);
    }

}