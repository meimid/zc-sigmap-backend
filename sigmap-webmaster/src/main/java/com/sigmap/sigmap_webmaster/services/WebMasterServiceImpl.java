package com.sigmap.sigmap_webmaster.services;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmRepository;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaRepository;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.PaaSpecifications;
import com.sigmap.common.paas.exceptions.PaaIdNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.realisations.Realisation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebMasterServiceImpl implements WebMasterService {

    private final PpmRepository ppmRepository;
    private final PaaRepository paaRepository;
    private final AgpmRepository agpmRepository;

    public WebMasterServiceImpl( PpmRepository ppmRepository, PaaRepository paaRepository, AgpmRepository agpmRepository) {

        this.ppmRepository = ppmRepository;
        this.paaRepository = paaRepository;
        this.agpmRepository = agpmRepository;
    }

    @Override
    public Page<Realisation> getPpmById(Integer ppmId, Pageable pageable) throws PpmNotFound {
        Integer ppmVer=ppmRepository.findMaxVersionById(ppmId);
        Specification<Ppm> spec = PpmSpecifications.id(new CompositeId(ppmId, ppmVer)).and(PpmSpecifications.deleted(false)).and(PpmSpecifications.isPublished());
        Ppm ppm = ppmRepository.findOne(spec).orElseThrow(()-> new PpmIdNotFound("",ppmId));

            List<Realisation> allRealisations = ppm.getRealisations();
            List<Realisation> pageContent = allRealisations.stream()
                    .skip(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .collect(Collectors.toList());

            return new PageImpl<>(pageContent, pageable, allRealisations.size());

    }


    @Override
    public Page<Realisation> getPaaById(Integer paaId,Pageable pageable) throws PaaNotFound {
        Integer paaVer=paaRepository.findMaxVersionById(paaId);
        Specification<Paa> spec = PaaSpecifications.id(new CompositeId(paaId, paaVer)).and(PaaSpecifications.deleted(false)).and(PaaSpecifications.isPublished());
        Paa paa = paaRepository.findOne(spec).orElseThrow(()-> new PaaIdNotFound("",paaId));
            List<Realisation> allRealisations = paa.getRealisations();
            List<Realisation> pageContent = allRealisations.stream()
                    .skip(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .collect(Collectors.toList());
            return new PageImpl<>(pageContent, pageable, allRealisations.size());

    }


    @Override
    public Agpm getAgpmById (Integer agpmId) throws AgpmNotFound {
        Integer agpmVer=agpmRepository.findMaxVersionById(agpmId);
        Specification<Agpm> spec = AgpmSpecifications.id(new CompositeId(agpmId, agpmVer)).and(AgpmSpecifications.deleted(false)).and(AgpmSpecifications.isPublished());
        Agpm agpmOpt = agpmRepository.findOne(spec).orElseThrow(()->new AgpmNotFound("",List.of(agpmId),List.of(agpmVer)));
            return agpmOpt;
    }


}
