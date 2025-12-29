package com.sigmap.common.appel_offre;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.appel_offre.request.AppelOffreEditRequest;
import com.sigmap.common.appel_offre.request.CreateAppelOffreRequest;
import com.sigmap.common.appel_offre.response.AppelOffreResponseById;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface AppelOffreService {

    public AppelOffre save(AppelOffre appelOffre);
    List<AppelOffre> findAll();

    Page<AppelOffre> findAll(Specification<AppelOffre> spec, Pageable pageable);

    AppelOffreResponseById findById(Integer id) throws PpmNotFound, AcNotFound;

    AppelOffre create(CreateAppelOffreRequest request) throws RealisationNotFoundException;

    AppelOffre update(Integer id, AppelOffreEditRequest request);
    public List<Ppm> getAllPpmsWithAcsforConnectedUser();
    public List<Realisation> getRealisationsNotRelatedToAppelOffreWithStatusCreeOrCloturer(Integer ppmId, Integer ppmVer);

}
