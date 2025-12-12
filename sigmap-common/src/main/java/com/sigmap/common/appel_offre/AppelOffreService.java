package com.sigmap.common.appel_offre;

import com.sigmap.common.appel_offre.exception.AppelOffreNotFound;
import com.sigmap.common.appel_offre.request.AppelOffreEditRequest;
import com.sigmap.common.appel_offre.request.CreateAppelOffreRequest;
import com.sigmap.common.appel_offre.response.AppelOffreResponse;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface AppelOffreService {

    public AppelOffre save(AppelOffre appelOffre);
    List<AppelOffre> findAll();

    Page<AppelOffre> findAll(Specification<AppelOffre> spec, Pageable pageable);

    AppelOffre findById(Integer id);

    AppelOffre create(CreateAppelOffreRequest request) throws RealisationNotFoundException;

    AppelOffre update(Integer id, AppelOffreEditRequest request);

}
