package com.sigmap.sigmap_webmaster.controller;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.AcService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.ac.response.AcWebsiteResponse;
import com.sigmap.common.ac.response.LogoResponse;
import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.ActiviteService;
import com.sigmap.common.activite.mapper.ActiviteMapper;
import com.sigmap.common.activite.response.ActiviteResponseDto;
import com.sigmap.common.activite.response.ActiviteResponseDtoSoft;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.response.AgpmWebsiteResponse;
import com.sigmap.common.config.RefTypes;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.docs.Request.AlfrescoIdRequest;
import com.sigmap.common.docs.exeptions.DocNotFound;
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
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.ref_params.response.RefParamResponseDtoSoft;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.request.SocieteGlobalRequest;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import com.sigmap.common.societe.response.SocieteResponse;
import com.sigmap.sigmap_webmaster.dto.TypeMarcheStatProjection;
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

    private final AcService acService;
    private final WebMasterService webMasterService;
    private final DocsService docsService;
    private final RefParamService refParamService;
    private final ActiviteService  activiteService;
    private final SocieteService societeService;
    @Value("${file.upload.ac-logo-path}")
    private String acLogoPath;
    public WebMasterController( AcService acService, WebMasterService webMasterService, DocsService docsService, RefParamService refParamService, ActiviteService activiteService, SocieteService societeService) {

        this.acService = acService;
        this.webMasterService = webMasterService;
        this.docsService = docsService;
        this.refParamService = refParamService;
        this.activiteService = activiteService;
        this.societeService = societeService;
    }


        @GetMapping("/ppm")
    public ResponseEntity<Page<PpmWebsiteResponse>> findPpmAllForWebsite(PpmQuery query, Pageable pageable)  {
        Specification<Ppm> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<PpmWebsiteResponse> responses = webMasterService.findAllPpmForWebsite(spec,pageable);
            return ResponseEntity.ok(responses);
        }

    @GetMapping("/ppm/{ppmId}")
    public ResponseEntity<PpmDetailsWebsite> findPpmForWebsiteByIdAndVersion(@PathVariable Integer ppmId
                                                                                       , Pageable pageable)
            throws PpmNotFound, AcNotFound {
        PpmDetailsWebsite response= webMasterService.getPpmById(ppmId, pageable);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/paa")
    public ResponseEntity<Page<PaaWebsiteResponse>> findPaaAllForWebsite(PaaQuery query, Pageable pageable)  {
        Specification<Paa> spec = (query != null) ? query.toSpecification() : Specification.where(null);
        Page<PaaWebsiteResponse> responses = webMasterService.findPaaAllForWebsite(spec,pageable);
        return ResponseEntity.ok(responses);
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
        Page<AgpmWebsiteResponse> responses = webMasterService.findAgpmAllForWebsite(spec,pageable);
        return ResponseEntity.ok(responses);
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

    @PostMapping("/download/agpm")
    public ResponseEntity<Resource> downloadDocumentAgpm(@RequestBody AlfrescoIdRequest alfrescoId) throws IOException,  DocNotFound {
        Docs docInfo = docsService.findByAlfescoId(alfrescoId.getId())
                .orElseThrow(() -> new DocsNotFoundException(alfrescoId.getId()));
        byte[] fileContent = webMasterService.downloadDocumentAgpm(alfrescoId.getId());
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
    @PostMapping("/download/ppm")
    public ResponseEntity<Resource> downloadDocumentPpm(@RequestBody AlfrescoIdRequest alfrescoId) throws IOException,  DocNotFound {
        Docs docInfo = docsService.findByAlfescoId(alfrescoId.getId())
                .orElseThrow(() -> new DocsNotFoundException(alfrescoId.getId()));
        byte[] fileContent = webMasterService.downloadDocumentPpm(alfrescoId.getId());
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

    @PostMapping("/download/paa")
    public ResponseEntity<Resource> downloadDocumentPaa(@RequestBody AlfrescoIdRequest alfrescoId) throws IOException,  DocNotFound {
        Docs docInfo = docsService.findByAlfescoId(alfrescoId.getId())
                .orElseThrow(() -> new DocsNotFoundException(alfrescoId.getId()));
        byte[] fileContent = webMasterService.downloadDocumentPaa(alfrescoId.getId());
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

    @GetMapping("/type-societe")
    public ResponseEntity<List<RefParamResponseDtoSoft>> findAllTypeSociete() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.TYPE_SOCIETE);
        List<RefParamResponseDtoSoft> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDtoSoft)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/categorie-activite")
    public ResponseEntity<List<RefParamResponseDtoSoft>> findAllCategorieActivite() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.CATEGORIE_ACTIVITE);
        List<RefParamResponseDtoSoft> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDtoSoft)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/pays")
    public ResponseEntity<List<RefParamResponseDtoSoft>> findAllPays() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.PAYS);
        List<RefParamResponseDtoSoft> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDtoSoft)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/categories-activite/{categoriesActiviteId}")
    public ResponseEntity<List<ActiviteResponseDtoSoft>> findAllBycategoriesActiviteId(
            @PathVariable Integer categoriesActiviteId) throws RefParamNotFound {
        List<Activite> params=activiteService.findByCategoriesActiviteId(categoriesActiviteId);
        List<ActiviteResponseDtoSoft> response = params.stream()
                .map(ActiviteMapper.INSTANCE::mapToResponseDtoSoft)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new Societe.
     *
     * @param request the request to create the Societe from
     * @return the created Societe
     */
    @PermitAll
    @PostMapping("/societe-create")
    public ResponseEntity<SocieteResponse> create(@RequestBody SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound {
        Societe societe = societeService.create(request);
        SocieteResponse response = SocieteMapper.INSTANCE.map(societe);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stats/type-marche")
    public ResponseEntity<List<TypeMarcheStatProjection>> getTypeMarcheStats() {
        return ResponseEntity.ok(webMasterService.getTypeMarcheStats());
    }

}
