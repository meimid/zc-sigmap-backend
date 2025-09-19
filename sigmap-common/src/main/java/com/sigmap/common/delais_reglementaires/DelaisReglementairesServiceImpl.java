package com.sigmap.common.delais_reglementaires;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionRepository;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.delais_reglementaires.exceptions.DelaisReglementairesNotFound;
import com.sigmap.common.delais_reglementaires.exceptions.DelaisReglementairesObligationFields;
import com.sigmap.common.delais_reglementaires.request.DelaisReglementairesRequest;
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
public class DelaisReglementairesServiceImpl implements DelaisReglementairesService {

    private final DelaisReglementairesRepository delaisReglementairesRepository;
    private final RefParamRepository typeMarcherRepository;
    private final ModeDePassationSelectionRepository modePassationRepository;

    @Override
    public DelaisReglementaires save(DelaisReglementairesRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound {

        DelaisReglementaires delaisReglementaires = new DelaisReglementaires();
        // all Requests must have typeMarcherId, modePassationId, and all delais fields
        if (request.getTypeMarcherId() == null || request.getModePassationId() == null || 
            request.getDelaiPublicationLancement() == null || request.getDelaiLancementAttribution() == null || 
            request.getDelaiAttributionDemarrage() == null) {
            throw new DelaisReglementairesObligationFields("DelaisReglementairesRequest must have typeMarcherId, modePassationId, and all delais fields");
        }
        
        // Initialize the embedded ID
        DelaisReglementairesId id = new DelaisReglementairesId();
        delaisReglementaires.setId(id);

        // Fetch typeMarcher by TypeMarcherId
        RefParam typeMarcher = typeMarcherRepository.findById(request.getTypeMarcherId())
                .orElseThrow(() -> new RefParamNotFound("", List.of(request.getTypeMarcherId())));
        delaisReglementaires.getId().setTypeMarcher(typeMarcher);

        // Fetch modePassation by modePassation ID
        ModeDePassationSelection modePassation = modePassationRepository.findById(request.getModePassationId())
                .orElseThrow(() -> new ModeDePassationSelectionNotFound("", request.getModePassationId()));
        delaisReglementaires.getId().setModePassation(modePassation);

        // Set delais values
        delaisReglementaires.setDelaiPublicationLancement(request.getDelaiPublicationLancement());
        delaisReglementaires.setDelaiLancementAttribution(request.getDelaiLancementAttribution());
        delaisReglementaires.setDelaiAttributionDemarrage(request.getDelaiAttributionDemarrage());

        return delaisReglementairesRepository.save(delaisReglementaires);
    }

    @Override
    public DelaisReglementaires update(DelaisReglementairesRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound, DelaisReglementairesNotFound {
        // Validate request
        if (request.getTypeMarcherId() == null || request.getModePassationId() == null || 
            request.getDelaiPublicationLancement() == null || request.getDelaiLancementAttribution() == null || 
            request.getDelaiAttributionDemarrage() == null) {
            throw new RuntimeException("DelaisReglementairesRequest must have typeMarcherId, modePassationId, and all delais fields");
        }

        // Create composite key for lookup
        DelaisReglementairesId delaisReglementairesId = new DelaisReglementairesId();

        // Fetch typeMarcher by TypeMarcherId
        RefParam typeMarcher = typeMarcherRepository.findById(request.getTypeMarcherId())
                .orElseThrow(() -> new RefParamNotFound("", List.of(request.getTypeMarcherId())));
        delaisReglementairesId.setTypeMarcher(typeMarcher);

        // Fetch modePassation by modePassation ID
        ModeDePassationSelection modePassation = modePassationRepository.findById(request.getModePassationId())
                .orElseThrow(() -> new ModeDePassationSelectionNotFound("", request.getModePassationId()));
        delaisReglementairesId.setModePassation(modePassation);

        // Find existing DelaisReglementaires by composite ID
        DelaisReglementaires delaisReglementaires = delaisReglementairesRepository.findById(delaisReglementairesId)
                .orElseThrow(() -> new DelaisReglementairesNotFound("DelaisReglementaires not found for provided composite key", request.getTypeMarcherId(), request.getModePassationId()));

        // Update delais values
        delaisReglementaires.setDelaiPublicationLancement(request.getDelaiPublicationLancement());
        delaisReglementaires.setDelaiLancementAttribution(request.getDelaiLancementAttribution());
        delaisReglementaires.setDelaiAttributionDemarrage(request.getDelaiAttributionDemarrage());

        // Save updated entity
        return delaisReglementairesRepository.save(delaisReglementaires);
    }

    @Override
    public Optional<DelaisReglementaires> findOne(Specification<DelaisReglementaires> specification) {
        return delaisReglementairesRepository.findOne(specification);
    }

    @Override
    public Page<DelaisReglementaires> getAllDelaisReglementaires(Specification<DelaisReglementaires> spec, Pageable pageable) {
        //spec= spec.and(DelaisReglementairesSpecifications.deleted(false));
        log.debug("Fetching all DelaisReglementaires entities with pagination: {}", pageable);
        return delaisReglementairesRepository.findAll(spec, pageable);
    }

}
