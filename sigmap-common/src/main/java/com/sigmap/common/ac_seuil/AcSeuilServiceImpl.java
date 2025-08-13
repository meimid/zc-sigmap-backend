package com.sigmap.common.ac_seuil;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionRepository;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcRepository;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac_seuil.exeptions.AcSeuilNotFound;
import com.sigmap.common.ac_seuil.request.AcSeuilRequest;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class AcSeuilServiceImpl implements AcSeuilService {

    private final AcSeuilRepository acSeuilRepository;
    private final AcRepository acRepository;
    private final RefParamRepository typeMarcherRepository;
    private final ModeDePassationSelectionRepository modePassationRepository;

    @Override
    public AcSeuil save(AcSeuilRequest request) throws AcNotFound, RefParamNotFound, ModeDePassationSelectionNotFound {

        AcSeuil acSeuil = new AcSeuil();
        // all Requests must have acId, typeMarcherId, modePassationId, and seuil
        if (request.getAcId() == null || request.getTypeMarcherId() == null || request.getModePassationId() == null || request.getSeuil() == null) {
            throw new RuntimeException("AcSeuilRequest must have acId, typeMarcherId, modePassationId, and seuil");
        }
        // Initialize the embedded ID
        AcSeuilId id = new AcSeuilId();
        acSeuil.setId(id);

        // Fetch AC by AC ID
        Ac ac = acRepository.findById(request.getAcId())
                .orElseThrow(() -> new AcNotFound("", List.of(request.getAcId())));
        acSeuil.getId().setAc(ac);

        // Fetch typeMarcher by TypeMarcherId
        RefParam typeMarcher = typeMarcherRepository.findById(request.getTypeMarcherId())
                .orElseThrow(() -> new RefParamNotFound("", List.of(request.getTypeMarcherId())));
        acSeuil.getId().setTypeMarcher(typeMarcher);

        // Fetch modePassation by modePassation ID
        ModeDePassationSelection modePassation = modePassationRepository.findById(request.getModePassationId())
                .orElseThrow(() -> new ModeDePassationSelectionNotFound("", request.getModePassationId()));
        acSeuil.getId().setModePassation(modePassation);

        // Set seuil
        acSeuil.setSeuil(request.getSeuil());

        return acSeuilRepository.save(acSeuil);
    }


    @Override
    public AcSeuil update(AcSeuilRequest request) throws AcNotFound, RefParamNotFound, ModeDePassationSelectionNotFound, AcSeuilNotFound {
        // Validate request
        if (request.getAcId() == null || request.getTypeMarcherId() == null || request.getModePassationId() == null || request.getSeuil() == null) {
            throw new RuntimeException("AcSeuilRequest must have acId, typeMarcherId, modePassationId, and seuil");
        }

        // Create composite key for lookup
        AcSeuilId acSeuilId = new AcSeuilId();

        // Fetch AC by AC ID
        Ac ac = acRepository.findById(request.getAcId())
                .orElseThrow(() -> new AcNotFound("", List.of(request.getAcId())));
        acSeuilId.setAc(ac);

        // Fetch typeMarcher by TypeMarcherId
        RefParam typeMarcher = typeMarcherRepository.findById(request.getTypeMarcherId())
                .orElseThrow(() -> new RefParamNotFound("", List.of(request.getTypeMarcherId())));
        acSeuilId.setTypeMarcher(typeMarcher);

        // Fetch modePassation by modePassation ID
        ModeDePassationSelection modePassation = modePassationRepository.findById(request.getModePassationId())
                .orElseThrow(() -> new ModeDePassationSelectionNotFound("", request.getModePassationId()));
        acSeuilId.setModePassation(modePassation);

        // Find existing AcSeuil by composite ID
        AcSeuil acSeuil = acSeuilRepository.findById(acSeuilId)
                .orElseThrow(() -> new AcSeuilNotFound("AcSeuil not found for provided composite key", request.getAcId(), request.getTypeMarcherId(), request.getModePassationId()));

        // Update seuil value
        acSeuil.setSeuil(request.getSeuil());

        // Save updated entity
        return acSeuilRepository.save(acSeuil);
    }

    @Override
    public Optional<AcSeuil> findOne(Specification<AcSeuil> specification) {
        log.info("--------------- Finding acSeuil by specification {}", specification);
        return acSeuilRepository.findOne(specification);
    }

    @Override
    public Page<AcSeuil> getAllAcSeuils(Specification<AcSeuil> spec, Pageable pageable) {
        log.debug("Fetching all AcSeuil entities with pagination: {}", pageable);
        return acSeuilRepository.findAll(spec, pageable);
    }

    @Override
    public Page<AcSeuil> findById (Integer acId, Pageable pageable) throws AcSeuilNotFound {
        log.debug("Fetching AcSeuil with acId: {} ", acId);
        // Validate request
        if (acId == null ) {
            throw new IllegalArgumentException("you must have acId");
        }
        // Find existing AcSeuil by specification with pagination
        Specification<AcSeuil> spec = AcSeuilSpecifications.acId(acId);
        return acSeuilRepository.findAll(spec, pageable);
    }
}
