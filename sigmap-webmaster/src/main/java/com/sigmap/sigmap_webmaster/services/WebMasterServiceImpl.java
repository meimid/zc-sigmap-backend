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
import com.sigmap.common.paas.response.PaaDetailsWebsite;
import com.sigmap.common.paas.response.PaaWebsiteResponse;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.response.PpmDetailsWebsite;
import com.sigmap.common.ppms.response.PpmWebsiteResponse;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.response.RealisationPaaWebSite;
import com.sigmap.common.realisations.response.RealisationPpmWebsite;
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
    public PpmDetailsWebsite getPpmById(Integer ppmId, Pageable pageable) throws PpmNotFound {
        Integer ppmVer=ppmRepository.findMaxVersionById(ppmId);
        Specification<Ppm> spec = PpmSpecifications.id(new CompositeId(ppmId, ppmVer)).and(PpmSpecifications.deleted(false)).and(PpmSpecifications.isPublished());
        Ppm ppm = ppmRepository.findOne(spec).orElseThrow(()-> new PpmIdNotFound("",ppmId));

        PpmWebsiteResponse ppmResponse = new PpmWebsiteResponse(ppm);

        List<RealisationPpmWebsite> allRealisationsDto = ppm.getRealisations().stream()
                .map(RealisationPpmWebsite::new).collect(Collectors.toList());


        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRealisationsDto.size());
        List<RealisationPpmWebsite> pageContent = allRealisationsDto.subList(start, end);

        Page<RealisationPpmWebsite> realisationPage = new PageImpl<>(pageContent, pageable, allRealisationsDto.size());

        PpmDetailsWebsite ppmDetailsWebsite = new PpmDetailsWebsite();
        ppmDetailsWebsite.setPpm(ppmResponse);

        ppmDetailsWebsite.setRealisations(realisationPage);

        return ppmDetailsWebsite;
    }




    @Override
    public PaaDetailsWebsite getPaaById(Integer paaId, Pageable pageable) throws PaaNotFound {
        Integer paaVer=paaRepository.findMaxVersionById(paaId);
        Specification<Paa> spec = PaaSpecifications.id(new CompositeId(paaId, paaVer)).and(PaaSpecifications.deleted(false)).and(PaaSpecifications.isPublished());
        Paa paa = paaRepository.findOne(spec).orElseThrow(()-> new PaaIdNotFound("",paaId));
        PaaWebsiteResponse paaResponse = new PaaWebsiteResponse(paa);
        List<RealisationPaaWebSite> allRealisationsDto = paa.getRealisations().stream()
                .map(RealisationPaaWebSite::new).collect(Collectors.toList());


        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRealisationsDto.size());
        List<RealisationPaaWebSite> pageContent = allRealisationsDto.subList(start, end);
        Page<RealisationPaaWebSite> realisationPage = new PageImpl<>(pageContent, pageable, allRealisationsDto.size());
        PaaDetailsWebsite paaDetailsWebsite = new PaaDetailsWebsite();
        paaDetailsWebsite.setPaa(paaResponse);
        paaDetailsWebsite.setRealisations(realisationPage);
        return paaDetailsWebsite;

    }


    @Override
    public Agpm getAgpmById (Integer agpmId) throws AgpmNotFound {
        Integer agpmVer=agpmRepository.findMaxVersionById(agpmId);
        Specification<Agpm> spec = AgpmSpecifications.id(new CompositeId(agpmId, agpmVer)).and(AgpmSpecifications.deleted(false)).and(AgpmSpecifications.isPublished());
        Agpm agpmOpt = agpmRepository.findOne(spec).orElseThrow(()->new AgpmNotFound("",List.of(agpmId),List.of(agpmVer)));
            return agpmOpt;
    }


}
