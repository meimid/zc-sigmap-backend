package com.sigmap.common.parametrage_process_dac;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionRepository;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.parametrage_process_dac.exeptions.ParametrageProcessDacNotFound;
import com.sigmap.common.parametrage_process_dac.request.ParametrageProcessDacRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ParametrageProcessDacServiceImpl implements ParametrageProcessDacService {

    private final ParametrageProcessDacRepository parametrageProcessDacRepository;
    private final RefParamRepository typeMarcherRepository;
    private final ModeDePassationSelectionRepository modePassationRepository;

    @Override
    public ParametrageProcessDac save(ParametrageProcessDacRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound {
        if ( request.getTypeMarcherId() == null || request.getModePassationId() == null || request.getProcess() == null) {
            throw new RuntimeException("AcProcessRequest must have typeMarcherId, modePassationId, and process");
        }

        ParametrageProcessDac parametrageProcessDac = new ParametrageProcessDac();

        ParametrageProcessDacId id = new ParametrageProcessDacId();
        parametrageProcessDac.setId(id);

        //fetch typeMarcher and modePassation
        RefParam typeMarcher = typeMarcherRepository.findById(request.getTypeMarcherId())
                .orElseThrow(() -> new RefParamNotFound("", List.of(request.getTypeMarcherId())));
        parametrageProcessDac.getId().setTypeMarcher(typeMarcher);

        ModeDePassationSelection modePassation = modePassationRepository.findById(request.getModePassationId())
                .orElseThrow(() -> new ModeDePassationSelectionNotFound("", request.getModePassationId()));
        parametrageProcessDac.getId().setModePassation(modePassation);

        //set process
        parametrageProcessDac.setProcess(request.getProcess());

        return parametrageProcessDacRepository.save(parametrageProcessDac);
    }

    @Override
    public ParametrageProcessDac update(ParametrageProcessDacRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound, ParametrageProcessDacNotFound {
        if (request.getTypeMarcherId() == null || request.getModePassationId() == null || request.getProcess() == null) {
            throw new RuntimeException("AcProcessRequest must have typeMarcherId, modePassationId, and process");
        }

        ParametrageProcessDacId id = new ParametrageProcessDacId();

        RefParam typeMarcher = typeMarcherRepository.findById(request.getTypeMarcherId())
                .orElseThrow(() -> new RefParamNotFound("", List.of(request.getTypeMarcherId())));
        id.setTypeMarcher(typeMarcher);

        ModeDePassationSelection modePassation = modePassationRepository.findById(request.getModePassationId())
                .orElseThrow(() -> new ModeDePassationSelectionNotFound("", request.getModePassationId()));
        id.setModePassation(modePassation);

        ParametrageProcessDac parametrageProcessDac = parametrageProcessDacRepository.findById(id)
                .orElseThrow(() -> new ParametrageProcessDacNotFound("AcProcess not found for provided composite key", request.getTypeMarcherId(), request.getModePassationId()));

        parametrageProcessDac.setProcess(request.getProcess());

        return parametrageProcessDacRepository.save(parametrageProcessDac);
    }

    @Override
    public Page<ParametrageProcessDac> findAll(Specification<ParametrageProcessDac> spec, Pageable pageable) {
        log.debug("Fetching all AcProcess entities with pagination: {}", pageable);
        return parametrageProcessDacRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<ParametrageProcessDac> findOne(Specification<ParametrageProcessDac> specification) {
        log.info("--------------- Finding acProcess by specification {}", specification);
        return parametrageProcessDacRepository.findOne(specification);
    }

    @Override
    public Optional<ParametrageProcessDac> findOne(Integer typeMarcherId, Integer modePassationId) {
        Specification<ParametrageProcessDac> spec = Specification.where(ParametrageProcessDacSpecification.typeMarcherId(typeMarcherId))
                .and(ParametrageProcessDacSpecification.modePassationId(modePassationId));
        return parametrageProcessDacRepository.findOne(spec);
    }
}
