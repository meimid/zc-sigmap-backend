package com.sigmap.common.seuil_general;

import com.sigmap.common.ac_seuil.AcSeuil;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.seuil_general.exception.SeuilGeneralNotFound;
import com.sigmap.common.seuil_general.exception.SeuilWithTypeMarcheExiste;
import com.sigmap.common.seuil_general.request.SeuilGeneralRequest;
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
public class SeuilGeneralServiceImpl implements SeuilGeneralService {

    private final SeuilGeneralRepository seuilGeneralRepository;
    private final RefParamRepository refParamRepository;

    @Override
    public SeuilGeneral save(SeuilGeneralRequest request) throws RefParamNotFound {
        if (request.getTypeMarcherId() == null || request.getMontant() == null) {
            throw new IllegalArgumentException("SeuilGeneralRequest must have typeMarcherId and montant");
        }

        SeuilGeneral seuil= seuilGeneralRepository.findByTypeMarcherId(request.getTypeMarcherId());
        if (seuil != null) {
            throw new SeuilWithTypeMarcheExiste(request.getTypeMarcherId());
        }
        SeuilGeneral seuilGeneral = new SeuilGeneral();

        RefParam typeMarcher = refParamRepository.findById(request.getTypeMarcherId())
                .orElseThrow(() -> new RefParamNotFound("", List.of(request.getTypeMarcherId())));
        seuilGeneral.setTypeMarcher(typeMarcher);
        seuilGeneral.setMontant(request.getMontant());

        return seuilGeneralRepository.save(seuilGeneral);
    }

    @Override
    public SeuilGeneral update(Integer id,SeuilGeneralRequest request) throws RefParamNotFound, SeuilGeneralNotFound {
        if (id == null) {
            throw new IllegalArgumentException("SeuilGeneralRequest must have id for update");
        }

        SeuilGeneral seuilGeneral = seuilGeneralRepository.findById(id)
                .orElseThrow(() -> new SeuilGeneralNotFound(id));

        if (request.getTypeMarcherId() != null) {
            RefParam typeMarcher = refParamRepository.findById(request.getTypeMarcherId())
                    .orElseThrow(() -> new RefParamNotFound("", List.of(request.getTypeMarcherId())));
            seuilGeneral.setTypeMarcher(typeMarcher);
        }

        if (request.getMontant() != null) {
            seuilGeneral.setMontant(request.getMontant());
        }

        return seuilGeneralRepository.save(seuilGeneral);
    }

    @Override
    public Page<SeuilGeneral> getAllSeuilGenerals(Specification<SeuilGeneral> spec, Pageable pageable) {
        log.debug("Fetching all SeuilGeneral entities with pagination: {}", pageable);
        spec= spec.and(SeuilGeneralSpecifications.deleted(false));
        return seuilGeneralRepository.findAll(spec, pageable);
    }

    @Override
    public SeuilGeneral findById(Integer id) throws SeuilGeneralNotFound {
        return seuilGeneralRepository.findById(id)
                .orElseThrow(() -> new SeuilGeneralNotFound(id));
    }

    @Override
    public void delete(Integer id) throws SeuilGeneralNotFound {
        if (!seuilGeneralRepository.existsById(id)) {
            throw new SeuilGeneralNotFound(id);
        }
        seuilGeneralRepository.softDelete(id);
    }

    @Override
    public Optional<SeuilGeneral> findOne(Specification<SeuilGeneral> specification) {
        log.info("--------------- Finding seuil general by specification {}", specification);
        return seuilGeneralRepository.findOne(specification);
    }
}
