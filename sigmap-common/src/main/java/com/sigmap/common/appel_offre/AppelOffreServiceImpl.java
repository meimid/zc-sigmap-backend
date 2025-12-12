package com.sigmap.common.appel_offre;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.appel_offre.exception.AppelOffreNotFound;
import com.sigmap.common.appel_offre.mapper.AppelOffreMapper;
import com.sigmap.common.appel_offre.request.AppelOffreEditRequest;
import com.sigmap.common.appel_offre.request.CreateAppelOffreRequest;
import com.sigmap.common.appel_offre.response.AppelOffreResponse;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AppelOffreServiceImpl implements AppelOffreService {

    private final AppelOffreRepository appelOffreRepository;
    private final PpmRepository ppmRepository;
    private final RealisationRepository realisationRepository;
    private final AppelOffreMapper appelOffreMapper;

    private static int lastYear = java.time.Year.now().getValue();
    private static int counter = 0;


    @Override
    public AppelOffre save(AppelOffre appelOffre) {
        return appelOffreRepository.save(appelOffre);
    }

    @Override
    public List<AppelOffre> findAll() {
        return appelOffreRepository.getAllNotDeleted();
    }

    @Override
    public Page<AppelOffre> findAll(Specification<AppelOffre> spec, Pageable pageable) {
        log.debug("Fetching All AppelOffre ");
        spec = spec.and(AppelOffreSpecifications.deleted(false));
        return appelOffreRepository.findAll(spec, pageable);
    }

    @Override
    public AppelOffre findById(Integer id) {
        log.debug("Fetching AppelOffre with Id: {} ", id);
        if (id == null){
            throw new IllegalArgumentException("you must have Id");
        }

        AppelOffre appelOffre = appelOffreRepository.findById(id)
                .orElseThrow(() -> new AppelOffreNotFound(id));

        return appelOffre;
    }

    @Override
    @Transactional
    public AppelOffre create(CreateAppelOffreRequest request) throws RealisationNotFoundException {
        //AppelOffre appelOffre = appelOffreMapper.createRequestToEntity(request);

        if (request.getPpmId() == null || request.getPpmVer() == null ||
            request.getRealisationId() == null || request.getRealisationVer() == null ||
            request.getModePassation() == null){
            throw new RuntimeException("AppelOffre must have ppmId, ppmVer, realisationId, realisationVer and modePassation");
        }

        AppelOffre appelOffre = new AppelOffre();

        // fetch PPM by ID
        CompositeId ppmId = new CompositeId(request.getPpmId(), request.getPpmVer());
        Ppm ppm = ppmRepository.findById(ppmId)
                .orElseThrow(() -> new PpmIdNotFound(request.getPpmId(), request.getPpmVer()));

        appelOffre.setPpm(ppm);

        // fetch realisation by ID
        CompositeId realId = new CompositeId(request.getRealisationId(), request.getRealisationVer());
        Realisation realisation = realisationRepository.findById(realId)
                .orElseThrow(() -> new RealisationNotFoundException(request.getRealisationId(), request.getRealisationVer()));

        appelOffre.setRealisation(realisation);

        //set the other fields

        //fro the ref it take ppm.nom/annéé/number
        //String reference = "";
        appelOffre.setReference(ppm.getNom().concat(generateReference()));

        appelOffre.setNom(request.getNom());
        appelOffre.setDescription(request.getDescription());
        appelOffre.setEtape(request.getEtape());
        appelOffre.setModePassation(request.getModePassation());
        appelOffre.setLotSepares(request.getLotSepares());
        appelOffre.setStatusAppelOffre(request.getStatusAppelOffre());

        save(appelOffre);

        return appelOffre;
    }

    @Override
    @Transactional
    public AppelOffre update(Integer id, AppelOffreEditRequest request){
        AppelOffre existingAppelOffre = appelOffreRepository.findById(id)
                .orElseThrow(() -> new AppelOffreNotFound( id));

        existingAppelOffre.setNom(request.getNom());
        existingAppelOffre.setDescription(request.getDescription());

        save(existingAppelOffre);
        return existingAppelOffre;
    }


    public static String generateReference() {
        int currentYear = java.time.Year.now().getValue();

        if (currentYear != lastYear) {
            lastYear = currentYear;
            counter = 1;
        } else {
            counter++;
        }

        return currentYear + "/" + counter;
    }
}
