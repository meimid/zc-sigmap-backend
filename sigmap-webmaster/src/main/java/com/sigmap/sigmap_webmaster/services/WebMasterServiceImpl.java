package com.sigmap.sigmap_webmaster.services;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmRepository;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.response.AgpmWebsiteResponse;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.model_agpm.ModelAgpm;
import com.sigmap.common.model_agpm.ModelAgpmRepository;
import com.sigmap.common.model_agpm.ModelAgpmService;
import com.sigmap.common.model_paa.ModelPaa;
import com.sigmap.common.model_paa.ModelPaaRepository;
import com.sigmap.common.model_paa.ModelPaaService;
import com.sigmap.common.model_ppm.ModelPpm;
import com.sigmap.common.model_ppm.ModelPpmRepository;
import com.sigmap.common.model_ppm.ModelPpmService;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebMasterServiceImpl implements WebMasterService {

    private final PpmRepository ppmRepository;
    private final PaaRepository paaRepository;
    private final AgpmRepository agpmRepository;
    private final ModelPpmService modelPpmService;
    private final ModelPaaService modelPaaService;
    private final ModelAgpmService modelAgpmService;
    private final AlfrescoService alfrescoService;
    private final ModelPaaRepository modelPaaRepository;
    private final ModelAgpmRepository modelAgpmRepository;
    private final ModelPpmRepository modelPpmRepository;


    public WebMasterServiceImpl(PpmRepository ppmRepository, PaaRepository paaRepository, AgpmRepository agpmRepository, ModelPpmService modelPpmService, ModelPaaService modelPaaService, ModelAgpmService modelAgpmService, AlfrescoService alfrescoService, ModelPaaRepository modelPaaRepository, ModelAgpmRepository modelAgpmRepository, ModelPpmRepository modelPpmRepository) {

        this.ppmRepository = ppmRepository;
        this.paaRepository = paaRepository;
        this.agpmRepository = agpmRepository;
        this.modelPpmService = modelPpmService;
        this.modelPaaService = modelPaaService;
        this.modelAgpmService = modelAgpmService;
        this.alfrescoService = alfrescoService;
        this.modelPaaRepository = modelPaaRepository;
        this.modelAgpmRepository = modelAgpmRepository;
        this.modelPpmRepository = modelPpmRepository;
    }

    @Override
    public PpmDetailsWebsite getPpmById(Integer ppmId, Pageable pageable) throws PpmNotFound {
        Integer ppmVer = ppmRepository.findMaxVersionById(ppmId);
        Specification<Ppm> spec = PpmSpecifications.id(new CompositeId(ppmId, ppmVer)).and(PpmSpecifications.deleted(false)).and(PpmSpecifications.isPublished());
        Ppm ppm = ppmRepository.findOne(spec).orElseThrow(() -> new PpmIdNotFound("", ppmId));
        Optional<ModelPpm> modelPpm = modelPpmService.getModel(ppmId, ppmVer);

        PpmWebsiteResponse ppmResponse = new PpmWebsiteResponse(ppm);
        List<RealisationPpmWebsite> allRealisationsDto = ppm.getRealisations().stream()
                .map(RealisationPpmWebsite::new).skip(pageable.getOffset())
                .limit(pageable.getPageSize()).collect(Collectors.toList());
        Page<RealisationPpmWebsite> realisationPage = new PageImpl<>(allRealisationsDto, pageable, allRealisationsDto.size());
        PpmDetailsWebsite ppmDetailsWebsite = new PpmDetailsWebsite();
        ppmDetailsWebsite.setPpm(ppmResponse);
        ppmDetailsWebsite.setRealisations(realisationPage);
        if (modelPpm.isPresent()) {
            if (modelPpm.get().getDoc() != null && !modelPpm.get().getDoc().getIsDeleted()) {
                DocsResponseDTO docResponseDTO = new DocsResponseDTO();
                docResponseDTO.setId(modelPpm.get().getDoc().getId());
                docResponseDTO.setName(modelPpm.get().getDoc().getName());
                docResponseDTO.setAlfresco_id(modelPpm.get().getDoc().getAlfresco_id());
                docResponseDTO.setContentType(modelPpm.get().getDoc().getContentType());
                docResponseDTO.setFileExtension(modelPpm.get().getDoc().getFileExtension());
                ppmResponse.setDoc(docResponseDTO);

            }
        }
        return ppmDetailsWebsite;
    }


    @Override
    public PaaDetailsWebsite getPaaById(Integer paaId, Pageable pageable) throws PaaNotFound {
        Integer paaVer = paaRepository.findMaxVersionById(paaId);
        Specification<Paa> spec = PaaSpecifications.id(new CompositeId(paaId, paaVer)).and(PaaSpecifications.deleted(false)).and(PaaSpecifications.isPublished());
        Paa paa = paaRepository.findOne(spec).orElseThrow(() -> new PaaIdNotFound("", paaId));
        Optional<ModelPaa> modelPaa = modelPaaService.getModel(paaId, paaVer);
        PaaWebsiteResponse paaResponse = new PaaWebsiteResponse(paa);
        List<RealisationPaaWebSite> allRealisationsDto = paa.getRealisations().stream()
                .map(RealisationPaaWebSite::new).skip(pageable.getOffset())
                .limit(pageable.getPageSize()).collect(Collectors.toList());
        Page<RealisationPaaWebSite> realisationPage = new PageImpl<>(allRealisationsDto, pageable, allRealisationsDto.size());
        PaaDetailsWebsite paaDetailsWebsite = new PaaDetailsWebsite();
        paaDetailsWebsite.setPaa(paaResponse);
        paaDetailsWebsite.setRealisations(realisationPage);
        if (modelPaa.isPresent()) {
            if (modelPaa.get().getDoc() != null && !modelPaa.get().getDoc().getIsDeleted()) {
                DocsResponseDTO docResponseDTO = new DocsResponseDTO();
                docResponseDTO.setId(modelPaa.get().getDoc().getId());
                docResponseDTO.setName(modelPaa.get().getDoc().getName());
                docResponseDTO.setAlfresco_id(modelPaa.get().getDoc().getAlfresco_id());
                docResponseDTO.setContentType(modelPaa.get().getDoc().getContentType());
                docResponseDTO.setFileExtension(modelPaa.get().getDoc().getFileExtension());
                paaResponse.setDoc(docResponseDTO);
            }
        }
        return paaDetailsWebsite;

    }


    @Override
    public AgpmWebsiteResponse getAgpmById(Integer agpmId) throws AgpmNotFound {
        Integer agpmVer = agpmRepository.findMaxVersionById(agpmId);
        Specification<Agpm> spec = AgpmSpecifications.id(new CompositeId(agpmId, agpmVer)).and(AgpmSpecifications.deleted(false)).and(AgpmSpecifications.isPublished());
        Agpm agpmOpt = agpmRepository.findOne(spec).orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), List.of(agpmVer)));
        Optional<ModelAgpm> modelAgpm = modelAgpmService.getModel(agpmId, agpmVer);
        AgpmWebsiteResponse agpmResponse = new AgpmWebsiteResponse(agpmOpt);
        if (modelAgpm.isPresent()) {
            if (modelAgpm.get().getDoc() != null && !modelAgpm.get().getDoc().getIsDeleted()) {
                DocsResponseDTO docResponseDTO = new DocsResponseDTO();
                docResponseDTO.setId(modelAgpm.get().getDoc().getId());
                docResponseDTO.setName(modelAgpm.get().getDoc().getName());
                docResponseDTO.setAlfresco_id(modelAgpm.get().getDoc().getAlfresco_id());
                docResponseDTO.setContentType(modelAgpm.get().getDoc().getContentType());
                docResponseDTO.setFileExtension(modelAgpm.get().getDoc().getFileExtension());
                agpmResponse.setDoc(docResponseDTO);
            }
        }

        return agpmResponse;
    }
    @Override
    public Optional <DocumentResponseDTO> getDocumentInformation(String alfrescoId) {
        return Optional.ofNullable(alfrescoService.getDocumentById(alfrescoId));
    }
    @Override
    public byte[] downloadDocument(String alfrescoId, Integer id, Integer ver) throws IOException {

        DocumentResponseDTO dummyContent = getDocumentInformation(alfrescoId).orElseThrow(() -> new DocsNotFoundException(""));
        if (dummyContent.getFile()!=null) {
            return dummyContent.getFile();
        }
        else {
            throw new IOException("Error while downloading document.");
        }

    }
}
