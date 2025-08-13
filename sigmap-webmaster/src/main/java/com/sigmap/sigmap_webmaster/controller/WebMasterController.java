package com.sigmap.sigmap_webmaster.controller;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.response.AcWebsiteResponse;
import com.sigmap.common.ac.response.LogoResponse;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.response.AgpmWebsiteResponse;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.request.PaaQuery;
import com.sigmap.common.paas.response.PaaDetailsWebsite;
import com.sigmap.common.paas.response.PaaWebsiteResponse;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.request.PpmQuery;
import com.sigmap.common.ppms.response.PpmDetailsWebsite;
import com.sigmap.common.ppms.response.PpmWebsiteResponse;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.response.RealisationPaaWebSite;
import com.sigmap.common.realisations.response.RealisationPpmWebsite;
import com.sigmap.sigmap_webmaster.services.WebMasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Slf4j
public class WebMasterController {
    private final PpmService ppmService;
    private final PaaService paaService;
    private final AgpmService agpmService;
    private final AcService acService;
    private final WebMasterService webMasterService;
    @Value("${file.upload.ac-logo-path}")
    private String acLogoPath;
    public WebMasterController(PpmService ppmService, PaaService paaService, AgpmService agpmService, AcService acService, WebMasterService webMasterService) {
        this.ppmService = ppmService;
        this.paaService = paaService;
        this.agpmService = agpmService;
        this.acService = acService;
        this.webMasterService = webMasterService;
    }


        @GetMapping("/ppm")
    public ResponseEntity<Page<PpmWebsiteResponse>> findAllForWebsite(PpmQuery query, Pageable pageable)  {
        Specification<Ppm> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<Ppm> ppmPage = ppmService.findAllForWebsite(spec,pageable);
        // Page<PpmWebsiteResponse> ppmResponsePage = ppmPage.map(PpmMapper.INSTANCE::toWebsiteResponse);
        Page<PpmWebsiteResponse> paaResponsePage = ppmPage.map(PpmWebsiteResponse::new);
        return new ResponseEntity<>(paaResponsePage, HttpStatus.OK);
    }

    @GetMapping("/ppm/{ppmId}")
    public ResponseEntity<PpmDetailsWebsite> findPpmForWebsiteByIdAndVersion(@PathVariable Integer ppmId
                                                                                       , Pageable pageable)
            throws PpmNotFound, AcNotFound {
        PpmDetailsWebsite response= webMasterService.getPpmById(ppmId, pageable);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/paa")
    public ResponseEntity<Page<PaaWebsiteResponse>> findAllForWebsite(PaaQuery query, Pageable pageable)  {
        Specification<Paa> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<Paa> paaPage = paaService.findAllForWebsite(spec,pageable);
        // Page<PaaWebsiteResponse> paaResponsePage = paaPage.map(PaaMapper.INSTANCE::toWebsiteResponse);
        Page<PaaWebsiteResponse> paaResponsePage = paaPage.map(PaaWebsiteResponse::new);

        return new ResponseEntity<>(paaResponsePage, HttpStatus.OK);
    }
    @GetMapping("/paa/{paaId}")
    public ResponseEntity<PaaDetailsWebsite> findPaaForWebsiteByIdAndVersion(@PathVariable Integer paaId,
                                                                             Pageable pageable) throws PaaNotFound {
        PaaDetailsWebsite response = webMasterService.getPaaById(paaId,pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/agpm")
    public ResponseEntity<Page<AgpmWebsiteResponse>> findAllForWebsite(AgpmQuery query, Pageable pageable)  {
        Specification<Agpm> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<Agpm> agpmPage = agpmService.findAllForWebsite(spec,pageable);
        //Page<AgpmWebsiteResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::toWebsiteResponse);
        Page<AgpmWebsiteResponse> agpmResponsePage = agpmPage.map(AgpmWebsiteResponse::new);

        return new ResponseEntity<>(agpmResponsePage, HttpStatus.OK);
    }

    @GetMapping("/agpm/{agpmId}")
    public ResponseEntity<AgpmWebsiteResponse> findAgpmForWebsiteByIdAndVersion(@PathVariable Integer agpmId) throws AgpmNotFound, AcNotFound {
        Agpm agpm = webMasterService.getAgpmById(agpmId);
        AgpmWebsiteResponse response = new AgpmWebsiteResponse(agpm);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/ac")
    public ResponseEntity <List<AcWebsiteResponse>> findAllAcsForWebsite()  {
        List<Ac> acs = acService.findAll();
        List<AcWebsiteResponse> responses= AcMapper.INSTANCE.mapToWebsiteResponse(acs);
        return ResponseEntity.ok(responses);
    }

  /*  @GetMapping("/{acId}/photo/display")
    public ResponseEntity<LogoResponse> displayAcPhoto(@PathVariable Integer acId) throws AcNotFound {
        LogoResponse response = acService.getAcPhoto(acId, acLogoPath);
        return ResponseEntity.ok(response);
    }*/
}
