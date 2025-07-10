package com.sigmap.sigmap_webmaster.services;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmRepository;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaRepository;
import com.sigmap.common.paas.PaaSpecifications;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.exception.PpmNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WebMasterServiceImpl implements WebMasterService {
    private final AgpmRepository agpmRepository;
    private final PpmRepository ppmRepository;
    private final PaaRepository paaRepository;


    public WebMasterServiceImpl(AgpmRepository agpmRepository, PpmRepository ppmRepository, PaaRepository paaRepository) {
        this.agpmRepository = agpmRepository;
        this.ppmRepository = ppmRepository;
        this.paaRepository = paaRepository;
    }

    @Override
    public Ppm getPpmByIdAndVersion(Integer ppmId, Integer ppmVer) throws PpmNotFound {
        Specification<Ppm> spec = PpmSpecifications.id(new CompositeId(ppmId, ppmVer)).and(PpmSpecifications.deleted(false));
        Ppm ppmOpt = ppmRepository.findOne(spec).get();
        if (ppmOpt == null) {
            throw new PpmNotFound("Aucun PPM trouvé pour l'ID et la version", List.of(ppmId), List.of(ppmVer));
        }else{
            return ppmOpt;
        }
    }


    @Override
    public Paa getPaaByIdAndVersion(Integer paaId, Integer paaVer) throws PaaNotFound {
        Specification<Paa> spec = PaaSpecifications.id(new CompositeId(paaId, paaVer)).and(PaaSpecifications.deleted(false));
        Paa paaOpt = paaRepository.findOne(spec).get();
        if (paaOpt == null) {
            throw new PaaNotFound("Aucun PPM trouvé pour l'ID et la version", List.of(paaId), List.of(paaVer));
        }else{
            return paaOpt;
        }

    }


    @Override
    public Agpm getAgpmByIdAndVersion (Integer agpmId, Integer agpmVer) throws AgpmNotFound {

        Specification<Agpm> spec = AgpmSpecifications.id(new CompositeId(agpmId, agpmVer)).and(AgpmSpecifications.deleted(false));
        Agpm agpmOpt = agpmRepository.findOne(spec).get();
        if (agpmOpt == null) {
            throw new AgpmNotFound("Aucun AGPM trouvé pour l'ID et la version", List.of(agpmId), List.of(agpmVer));
        }else{
            return agpmOpt;
        }

    }

}
