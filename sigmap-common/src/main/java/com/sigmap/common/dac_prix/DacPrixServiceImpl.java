package com.sigmap.common.dac_prix;


import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacId;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac_prix.exceptions.DacPrixAlreadyExist;
import com.sigmap.common.dac_prix.exceptions.DacPrixNotFound;
import com.sigmap.common.dac_prix.request.DacPrixRequest;
import com.sigmap.common.delais_reglementaires.DelaisReglementaires;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.MonnaieRepository;
import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class DacPrixServiceImpl implements DacPrixService {

    private final DacPrixRepository dacPrixRepository;
    private final DacRepository dacRepository;
    private final MonnaieRepository monnaieRepository;


    @Override
    @Transactional
    public DacPrix save(DacPrixRequest request) throws MonnaieNotFound {
        
        log.debug("Sauvegarde d'un nouveau prix DAC: {}", request);

        // verifier l'existence des field de request
        if(request.getDacId() == null || request.getDacVer() == null || request.getMonnaieId() == null || request.getPrix() == null) {
            throw new IllegalArgumentException("Les champs dacId, dacVer, monnaieId et prix sont obligatoires");
        }

        // verification de l'existace du DacPrix
        Optional<DacPrix> existingDacPrix = dacPrixRepository.findById(request.getDacId(), request.getDacVer(), request.getMonnaieId());
        if(existingDacPrix.isPresent()) {
            throw new DacPrixAlreadyExist(request.getDacId(), request.getDacVer(), request.getMonnaieId());
        }


        // vérification de l'existance du Dac
        DacId dacId = new DacId(request.getDacId(), request.getDacVer());
        Dac dac = dacRepository .findById(dacId)
                .orElseThrow(() -> new DacNotFound(" " , request.getDacId(), request.getDacVer()));

        // Vérification de l'existence de la monnaie
        Monnaie monnaie = monnaieRepository.findById(request.getMonnaieId())
                .orElseThrow(() -> new MonnaieNotFound("", List.of(request.getMonnaieId())));

        DacPrixId dacPrixId = new DacPrixId(dac, monnaie);

        DacPrix dacPrix = new DacPrix();
        dacPrix.setId(dacPrixId);
        dacPrix.setPrix(request.getPrix());

        dacPrix = dacPrixRepository.save(dacPrix);

        log.info("Sauvegarde du prix DAC: {}", dacPrix);
        return dacPrix;
    }



    @Override
    @Transactional
    public DacPrix update(DacPrixRequest request)  throws DacPrixNotFound {
        
        log.debug("Mise à jour du prix DAC: {}", request);

        // verifier l'existence des field de request
        if(request.getDacId() == null || request.getDacVer() == null || request.getMonnaieId() == null || request.getPrix() == null) {
            throw new IllegalArgumentException("Les champs dacId, dacVer, monnaieId et prix sont obligatoires");
        }
        
        // Vérification de l'existence de l'entité à mettre à jour
        Optional<DacPrix> existingDacPrix = dacPrixRepository.findById(request.getDacId(), request.getDacVer(), request.getMonnaieId());
        if(existingDacPrix.isEmpty()) {
            throw new DacPrixNotFound(request.getDacId(), request.getDacVer(), request.getMonnaieId());
        }
        // update prix
        existingDacPrix.get().setPrix(request.getPrix());

        DacPrix savedacPrix = dacPrixRepository.save(existingDacPrix.get());

        
        return savedacPrix;
    }

    @Override
    @Transactional(readOnly = true)
    public DacPrix findById(Integer dacId, Integer dacVer, Integer monnaieId) {
        log.debug("Recherche du prix DAC pour dacId={}, dacVer={}, monnaieId={}", dacId, dacVer, monnaieId);

        // verifier l'existence des field de request
        if(dacId == null || dacVer == null || monnaieId == null) {
            throw new IllegalArgumentException("Les champs dacId, dacVer, monnaieId sont obligatoires");
        }
                
        return dacPrixRepository.findById(dacId, dacVer, monnaieId).orElseThrow(() -> new DacPrixNotFound(dacId, dacVer, monnaieId));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<DacPrix> getAllDacPrix(Specification<DacPrix> spec, Pageable pageable) {
        log.debug("Récupération des prix DAC avec spécification et pagination");
        return dacPrixRepository.findAll(spec, pageable);
    }
}