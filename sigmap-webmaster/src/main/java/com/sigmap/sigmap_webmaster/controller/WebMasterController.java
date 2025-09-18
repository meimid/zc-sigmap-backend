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
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.docs.Request.AlfrescoIdRequest;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.model_agpm.ModelAgpmRepository;
import com.sigmap.common.model_paa.ModelPaaRepository;
import com.sigmap.common.model_ppm.ModelPpmRepository;
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
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
    private final DocsService docsService;
    private final ModelPaaRepository modelPaaRepository;
    private final ModelAgpmRepository modelAgpmRepository;
    private final ModelPpmRepository modelPpmRepository;
    @Value("${file.upload.ac-logo-path}")
    private String acLogoPath;
    public WebMasterController(PpmService ppmService, PaaService paaService, AgpmService agpmService, AcService acService, WebMasterService webMasterService, DocsService docsService, ModelPaaRepository modelPaaRepository, ModelAgpmRepository modelAgpmRepository, ModelPpmRepository modelPpmRepository) {
        this.ppmService = ppmService;
        this.paaService = paaService;
        this.agpmService = agpmService;
        this.acService = acService;
        this.webMasterService = webMasterService;
        this.docsService = docsService;
        this.modelPaaRepository = modelPaaRepository;
        this.modelAgpmRepository = modelAgpmRepository;
        this.modelPpmRepository = modelPpmRepository;
    }


        @GetMapping("/ppm")
    public ResponseEntity<Page<PpmWebsiteResponse>> findAllForWebsite(PpmQuery query, Pageable pageable)  {
        Specification<Ppm> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<Ppm> ppmPage = ppmService.findAllForWebsite(spec,pageable);
        // Page<PpmWebsiteResponse> ppmResponsePage = ppmPage.map(PpmMapper.INSTANCE::toWebsiteResponse);
        Page<PpmWebsiteResponse> paaResponsePage = ppmPage.map(PpmWebsiteResponse::new);
            return ResponseEntity.ok(paaResponsePage);
        }

    @GetMapping("/ppm/{ppmId}")
    public ResponseEntity<PpmDetailsWebsite> findPpmForWebsiteByIdAndVersion(@PathVariable Integer ppmId
                                                                                       , Pageable pageable)
            throws PpmNotFound, AcNotFound {
        PpmDetailsWebsite response= webMasterService.getPpmById(ppmId, pageable);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/paa")
    public ResponseEntity<Page<PaaWebsiteResponse>> findAllForWebsite(PaaQuery query, Pageable pageable)  {
        Specification<Paa> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<Paa> paaPage = paaService.findAllForWebsite(spec,pageable);
        Page<PaaWebsiteResponse> paaResponsePage = paaPage.map(PaaWebsiteResponse::new);

        return ResponseEntity.ok(paaResponsePage);
    }
    @GetMapping("/paa/{paaId}")
    public ResponseEntity<PaaDetailsWebsite> findPaaForWebsiteByIdAndVersion(@PathVariable Integer paaId,
                                                                             Pageable pageable) throws PaaNotFound {
        PaaDetailsWebsite response = webMasterService.getPaaById(paaId,pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/agpm")
    public ResponseEntity<Page<AgpmWebsiteResponse>> findAllForWebsite(AgpmQuery query, Pageable pageable)  {
        Specification<Agpm> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<Agpm> agpmPage = agpmService.findAllForWebsite(spec,pageable);
        Page<AgpmWebsiteResponse> agpmResponsePage = agpmPage.map(AgpmWebsiteResponse::new);
        return ResponseEntity.ok(agpmResponsePage);
    }

    @GetMapping("/agpm/{agpmId}")
    public ResponseEntity<AgpmWebsiteResponse> findAgpmForWebsiteByIdAndVersion(@PathVariable Integer agpmId) throws AgpmNotFound, AcNotFound {
        AgpmWebsiteResponse response = webMasterService.getAgpmById(agpmId);
        return  ResponseEntity.ok(response);
    }
    @GetMapping("/ac")
    public ResponseEntity <List<AcWebsiteResponse>> findAllAcsForWebsite()  {
        List<Ac> acs = acService.findAll();
        List<AcWebsiteResponse> response= AcMapper.INSTANCE.mapToWebsiteResponse(acs);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{id}/{ver}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer id,@PathVariable Integer ver) throws DocsNotFoundException, IOException {
        String alfrescoId;
        Optional<String> optionalAlfrescoIdPpm=modelPpmRepository.findModelAlfrescoId(id,ver);
        Optional<String> optionalAlfrescoIdPaa=modelPaaRepository.findModelAlfrescoId(id,ver);
        Optional<String> optionalAlfrescoIdAgpm=modelAgpmRepository.findModelAlfrescoId(id,ver);
        if (optionalAlfrescoIdPpm.isPresent()){
            alfrescoId = optionalAlfrescoIdPpm.get();
        }else if(optionalAlfrescoIdPaa.isPresent()){
            alfrescoId = optionalAlfrescoIdPaa.get();
        }else if(optionalAlfrescoIdAgpm.isPresent()){
            alfrescoId = optionalAlfrescoIdAgpm.get();
        }
        else {
            throw new DocsNotFoundException("document not found.");
        }
        Docs docInfo = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException(alfrescoId));
        byte[] fileContent = webMasterService.downloadDocument(alfrescoId,id,ver);
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docInfo.getName() + "\"");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers)
                .contentLength(fileContent.length)
                .contentType(MediaType.parseMediaType(docInfo.getContentType()))
                .body(resource);
    }
    @GetMapping("/{acId}/photo/display")
    public ResponseEntity<LogoResponse> displayAcPhoto(@PathVariable Integer acId) throws AcNotFound {
        LogoResponse response = acService.getAcPhoto(acId, acLogoPath);
        return ResponseEntity.ok(response);
    }

}
