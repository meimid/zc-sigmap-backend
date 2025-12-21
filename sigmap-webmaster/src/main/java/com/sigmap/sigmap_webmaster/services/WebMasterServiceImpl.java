package com.sigmap.sigmap_webmaster.services;

import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.response.AgpmWebsiteResponse;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.response.DacDetailsWebsiteResponse;
import com.sigmap.common.dac.response.DacWebsiteResponse;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.DacDocsRepository;
import com.sigmap.common.dac_prix.DacPrix;
import com.sigmap.common.dac_prix.DacPrixRepository;
import com.sigmap.common.docs.Response.DocsResponseWebsiteDTO;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.financement.Financement;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.model_agpm.ModelAgpm;
import com.sigmap.common.model_agpm.ModelAgpmService;
import com.sigmap.common.model_dac.ModelDac;
import com.sigmap.common.model_dac.ModelDacService;
import com.sigmap.common.model_paa.ModelPaa;
import com.sigmap.common.model_paa.ModelPaaService;
import com.sigmap.common.model_ppm.ModelPpm;
import com.sigmap.common.model_ppm.ModelPpmService;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.PaaSpecifications;
import com.sigmap.common.paas.exceptions.PaaIdNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.response.PaaDetailsWebsite;
import com.sigmap.common.paas.response.PaaWebsiteResponse;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.response.PpmDetailsWebsite;
import com.sigmap.common.ppms.response.PpmWebsiteResponse;
import com.sigmap.common.realisations.response.RealisationPaaWebSite;
import com.sigmap.common.realisations.response.RealisationPpmWebsite;
import com.sigmap.sigmap_webmaster.dto.TypeMarcheStatProjection;
import com.sigmap.sigmap_webmaster.repositories.WebMasterStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebMasterServiceImpl implements WebMasterService {

    private final ModelPpmService modelPpmService;
    private final ModelPaaService modelPaaService;
    private final ModelAgpmService modelAgpmService;
    private final AlfrescoService alfrescoService;
    private final PpmService ppmService;
    private final PaaService paaService;
    private final AgpmService agpmService;
    private final WebMasterStatsRepository webMasterStatsRepository;
    private final ModelDacService modelDacService;
    private final DacRepository dacRepository;
    private final DacDocsRepository dacDocsRepository;
    private final DacPrixRepository dacPrixRepository;
    private final KeycloakAdminService keycloakAdminService;

    public WebMasterServiceImpl(ModelPpmService modelPpmService, ModelPaaService modelPaaService, ModelAgpmService modelAgpmService, AlfrescoService alfrescoService, PpmService ppmService, PaaService paaService, AgpmService agpmService, WebMasterStatsRepository webMasterStatsRepository, ModelDacService modelDacService, DacRepository dacRepository, DacDocsRepository dacDocsRepository, DacPrixRepository dacPrixRepository, KeycloakAdminService keycloakAdminService) {

        this.modelPpmService = modelPpmService;
        this.modelPaaService = modelPaaService;
        this.modelAgpmService = modelAgpmService;
        this.alfrescoService = alfrescoService;
        this.ppmService = ppmService;
        this.paaService = paaService;
        this.agpmService = agpmService;
        this.webMasterStatsRepository = webMasterStatsRepository;
        this.modelDacService = modelDacService;
        this.dacRepository = dacRepository;
        this.dacDocsRepository = dacDocsRepository;
        this.dacPrixRepository = dacPrixRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Override
    public PpmDetailsWebsite getPpmById(Integer ppmId, Pageable pageable) throws PpmNotFound {
        Specification<Ppm> spec = PpmSpecifications.ppmId(ppmId).and(PpmSpecifications.isLatestPublishedVersion());
        Ppm ppm = ppmService.findOne(spec).orElseThrow(() -> new PpmIdNotFound("", ppmId));
        Optional<ModelPpm> modelPpm = modelPpmService.getModel(ppmId, ppm.getVersion());
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
                DocsResponseWebsiteDTO docResponseDTO = new DocsResponseWebsiteDTO();
                docResponseDTO.setName(modelPpm.get().getDoc().getName());
                docResponseDTO.setAlfresco_id(modelPpm.get().getDoc().getAlfresco_id());
                docResponseDTO.setFileExtension(modelPpm.get().getDoc().getFileExtension());
                ppmResponse.setDoc(docResponseDTO);
            }
        }
        return ppmDetailsWebsite;
    }


    @Override
    public PaaDetailsWebsite getPaaById(Integer paaId, Pageable pageable) throws PaaNotFound {
        Specification<Paa> spec = PaaSpecifications.paaId(paaId).and(PaaSpecifications.isLatestPublishedVersion());
        Paa paa = paaService.findOne(spec).orElseThrow(() -> new PaaIdNotFound("", paaId));
        Optional<ModelPaa> modelPaa = modelPaaService.getModel(paaId, paa.getVersion());
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
                DocsResponseWebsiteDTO docResponseDTO = new DocsResponseWebsiteDTO();
                docResponseDTO.setName(modelPaa.get().getDoc().getName());
                docResponseDTO.setAlfresco_id(modelPaa.get().getDoc().getAlfresco_id());
                docResponseDTO.setFileExtension(modelPaa.get().getDoc().getFileExtension());
                paaResponse.setDoc(docResponseDTO);
            }
        }
        return paaDetailsWebsite;

    }


    @Override
    public AgpmWebsiteResponse getAgpmById(Integer agpmId) throws AgpmNotFound {
        Specification<Agpm> spec = AgpmSpecifications.agpmId(agpmId).and(AgpmSpecifications.isLatestPublishedVersion());
        Agpm agpmOpt = agpmService.findOne(spec).orElseThrow(() -> new AgpmNotFound("", List.of(agpmId), null));
        Optional<ModelAgpm> modelAgpm = modelAgpmService.getModel(agpmId, agpmOpt.getVersion());
        AgpmWebsiteResponse agpmResponse = new AgpmWebsiteResponse(agpmOpt);
        if (modelAgpm.isPresent()) {
            if (modelAgpm.get().getDoc() != null && !modelAgpm.get().getDoc().getIsDeleted()) {
                DocsResponseWebsiteDTO docResponseDTO = new DocsResponseWebsiteDTO();
                docResponseDTO.setName(modelAgpm.get().getDoc().getName());
                docResponseDTO.setAlfresco_id(modelAgpm.get().getDoc().getAlfresco_id());
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
    public byte[] downloadDocumentAgpm(String alfrescoId) throws IOException, DocNotFound {

        boolean valid = false;


        //  Check AGPM
        if (!valid) {
            List<Agpm> agpms = agpmService.findAllForWebsite(null, Pageable.unpaged()).getContent();
            for (Agpm a : agpms) {
                if (matchesAgpm(alfrescoId, a)) {
                    valid = true;
                    break;
                }
            }
        }
        if (!valid) {
            throw new DocNotFound("Document not associated with latest published entity", null);
        }

        DocumentResponseDTO dummyContent = getDocumentInformation(alfrescoId).orElseThrow(() -> new DocsNotFoundException(""));
        if (dummyContent.getFile()!=null) {
            return dummyContent.getFile();
        }
        else {
            throw new IOException("Error while downloading document.");
        }

    }

    @Override
    public byte[] downloadDocumentPaa(String alfrescoId) throws IOException, DocNotFound {
        boolean valid = false;

        //  Check PAA
        List<Paa> paas = paaService.findAllForWebsite(null, Pageable.unpaged()).getContent();
        for (Paa p : paas) {
            if (matchesPaa(alfrescoId, p)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new DocNotFound("Document not associated with latest published entity", null);
        }
        DocumentResponseDTO dummyContent = getDocumentInformation(alfrescoId).orElseThrow(() -> new DocsNotFoundException(""));
        if (dummyContent.getFile()!=null) {
            return dummyContent.getFile();
        }
        else {
            throw new IOException("Error while downloading document.");
        }
    }

    @Override
    public byte[] downloadDocumentPpm(String alfrescoId) throws IOException, DocNotFound {
        boolean valid = false;

        //  Check PPM
        if (!valid) {
            List<Ppm> ppms = ppmService.findAllForWebsite(null, Pageable.unpaged()).getContent();
            for (Ppm p : ppms) {
                if (matchesPpm(alfrescoId, p)) {
                    valid = true;
                    break;
                }
            }
        }
        if (!valid) {
            throw new DocNotFound("Document not associated with latest published entity", null);
        }


        DocumentResponseDTO dummyContent = getDocumentInformation(alfrescoId).orElseThrow(() -> new DocsNotFoundException(""));
        if (dummyContent.getFile()!=null) {
            return dummyContent.getFile();
        }
        else {
            throw new IOException("Error while downloading document.");
        }
    }

    @Override
    public Page<PpmWebsiteResponse> findAllPpmForWebsite(Specification<Ppm> spec, Pageable pageable) {
        Page<Ppm> ppmPage = ppmService.findAllForWebsite(spec,pageable);
        List<PpmWebsiteResponse> responses = new ArrayList<>();
        for(Ppm ppm : ppmPage.getContent()) {
            Optional<ModelPpm> modelPpm = modelPpmService.getModel(ppm.getId(), ppm.getVersion());

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
                    DocsResponseWebsiteDTO docResponseDTO = new DocsResponseWebsiteDTO();
                    docResponseDTO.setName(modelPpm.get().getDoc().getName());
                    docResponseDTO.setAlfresco_id(modelPpm.get().getDoc().getAlfresco_id());
                    docResponseDTO.setFileExtension(modelPpm.get().getDoc().getFileExtension());
                    ppmResponse.setDoc(docResponseDTO);
                }
            }
            responses.add(ppmResponse);
        }
        return new PageImpl<>(responses, pageable, ppmPage.getTotalElements());
    }

    @Override
    public Page<PaaWebsiteResponse> findPaaAllForWebsite(Specification<Paa> spec, Pageable pageable) {
        Page<Paa> paaPage=paaService.findAllForWebsite(spec,pageable);
        List<PaaWebsiteResponse> responses = new ArrayList<>();
        for(Paa paa : paaPage.getContent()) {
            Optional<ModelPaa> modelPaa = modelPaaService.getModel(paa.getId(), paa.getVersion());
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
                    DocsResponseWebsiteDTO docResponseDTO = new DocsResponseWebsiteDTO();
                    docResponseDTO.setName(modelPaa.get().getDoc().getName());
                    docResponseDTO.setAlfresco_id(modelPaa.get().getDoc().getAlfresco_id());
                    docResponseDTO.setFileExtension(modelPaa.get().getDoc().getFileExtension());
                    paaResponse.setDoc(docResponseDTO);
                }
            }
            responses.add(paaResponse);
        }
        return new PageImpl<>(responses, pageable, paaPage.getTotalElements());
    }

    @Override
    public Page<AgpmWebsiteResponse> findAgpmAllForWebsite(Specification<Agpm> spec, Pageable pageable) {
        Page<Agpm> agpmPage=agpmService.findAllForWebsite(spec,pageable);
        List<AgpmWebsiteResponse> responses = new ArrayList<>();
        for(Agpm agpm : agpmPage.getContent()) {
            Optional<ModelAgpm> modelAgpm = modelAgpmService.getModel(agpm.getId(), agpm.getVersion());
            AgpmWebsiteResponse agpmResponse = new AgpmWebsiteResponse(agpm);
            if (modelAgpm.isPresent()) {
                if (modelAgpm.get().getDoc() != null && !modelAgpm.get().getDoc().getIsDeleted()) {
                    DocsResponseWebsiteDTO docResponseDTO = new DocsResponseWebsiteDTO();
                    docResponseDTO.setName(modelAgpm.get().getDoc().getName());
                    docResponseDTO.setAlfresco_id(modelAgpm.get().getDoc().getAlfresco_id());
                    docResponseDTO.setFileExtension(modelAgpm.get().getDoc().getFileExtension());
                    agpmResponse.setDoc(docResponseDTO);
                }
            }
            responses.add(agpmResponse);
        }
        return new PageImpl<>(responses, pageable, agpmPage.getTotalElements());

    }

    private boolean matchesPaa(String alfrescoId, Paa paa) {
        Optional<ModelPaa> model = modelPaaService.getModel(paa.getId(), paa.getVersion());

        if (model.isPresent()
                && model.get().getDoc() != null
                && !model.get().getDoc().getIsDeleted()) {

            return alfrescoId.equals(model.get().getDoc().getAlfresco_id());
        }
        return false;
    }

    private boolean matchesPpm(String alfrescoId, Ppm ppm) {
        Optional<ModelPpm> model = modelPpmService.getModel(ppm.getId(), ppm.getVersion());

        if (model.isPresent()
                && model.get().getDoc() != null
                && !model.get().getDoc().getIsDeleted()) {

            return alfrescoId.equals(model.get().getDoc().getAlfresco_id());
        }
        return false;
    }


    private boolean matchesAgpm(String alfrescoId, Agpm agpm) {
        Optional<ModelAgpm> model = modelAgpmService.getModel(agpm.getId(), agpm.getVersion());

        if (model.isPresent()
                && model.get().getDoc() != null
                && !model.get().getDoc().getIsDeleted()) {

            log.info("Matching agpm: {}" , model.get().getDoc().getAlfresco_id());

            return alfrescoId.equals(model.get().getDoc().getAlfresco_id());
        }
        return false;
    }

    @Override
    public List<TypeMarcheStatProjection> getTypeMarcheStats() {
        return webMasterStatsRepository.countRealisationsByTypeMarche();
    }

    @Override
    public boolean isDocumentRelatedToDac(String alfrescoId) {
        return modelDacService.isDocumentRelatedToDac(alfrescoId);
    }

    @Override
    public byte[] downloadDocumentDac(String alfrescoId) throws IOException, DocNotFound {
        if (!isDocumentRelatedToDac(alfrescoId)) {
            throw new DocNotFound("Document not associated with latest published entity", null);
        }

        DocumentResponseDTO dummyContent = getDocumentInformation(alfrescoId).orElseThrow(() -> new DocsNotFoundException(""));
        if (dummyContent.getFile() != null) {
            return dummyContent.getFile();
        } else {
            throw new IOException("Error while downloading document.");
        }
    }

    @Override
    public Page<DacWebsiteResponse> findPublishedDacsForWebsite(Pageable pageable) {
        Page<Dac> dacPage = dacRepository.findPublishedDacsForWebsite(pageable);
        return dacPage.map(this::mapToWebsiteResponse);
    }

    @Override
    public Page<DacWebsiteResponse> findPublishedDacsForUser(String userId, Pageable pageable) {
        Page<Dac> dacs = dacRepository.findPublishedDacsForUser(userId, pageable);
        return dacs.map(this::mapToWebsiteResponse);
    }

    private DacWebsiteResponse mapToWebsiteResponse(Dac dac) {
        DacWebsiteResponse response = new DacWebsiteResponse();
        if (dac.getDacId() != null) {
            response.setId(dac.getDacId());
        }
        // 1. Time remaining
        if (dac.getDateLimiteSoumission() != null) {
            ZonedDateTime deadline = dac.getDateLimiteSoumission().toInstant()
                    .atZone(ZoneId.systemDefault());
            ZonedDateTime now = ZonedDateTime.now();

            if (deadline.isAfter(now)) {
                java.time.Duration duration = java.time.Duration.between(now, deadline);
                long days = duration.toDays();
                long hoursOfLastDay = duration.minusDays(days).toHours();

                response.setNbrJoursRestants(days);
                response.setNbrHeuresRestants(hoursOfLastDay);
            } else {
                response.setNbrJoursRestants(0L);
                response.setNbrHeuresRestants(0L);
            }
        }

        // 2. ModePassation libelle
        if (dac.getAppelOffre() != null && dac.getAppelOffre().getRealisation() != null
                && dac.getAppelOffre().getRealisation().getModePassation() != null) {
            response.setModePassation(dac.getAppelOffre().getRealisation().getModePassation().getLibelle());
        }

        // 3. Nom of AppelOffre
        if (dac.getAppelOffre() != null) {
            response.setNomAppelOffre(dac.getAppelOffre().getNom());
        }

        // 4. TypeMarche description
        if (dac.getAppelOffre() != null && dac.getAppelOffre().getRealisation() != null
                && dac.getAppelOffre().getRealisation().getTypeMarcher() != null) {
            response.setDescriptionTypeMarche(dac.getAppelOffre().getRealisation().getTypeMarcher().getDescription());
        }

        // 5. DateLimiteSoumission
        response.setDateLimiteSoumission(dac.getDateLimiteSoumission());

        // 6. Alfresco ID from ModelDac
        modelDacService.getModel(dac.getDacId(), dac.getDacVer()).ifPresent(modelDac -> {
            if (!Boolean.TRUE.equals(modelDac.getIsDeleted()) && modelDac.getDoc() != null && !Boolean.TRUE.equals(modelDac.getDoc().getIsDeleted())) {
                response.setAlfrescoId(modelDac.getDoc().getAlfresco_id());
            }
        });
        // 7. NomAC
        if (dac.getAppelOffre() != null && dac.getAppelOffre().getPpm() != null && dac.getAppelOffre().getPpm().getAc() != null) {
            response.setNomAc(dac.getAppelOffre().getPpm().getAc().getNom());
        }
        return response;
    }
    @Override
    public DacDetailsWebsiteResponse getDacDetailsById(Integer dacId) throws DocNotFound {
        Dac dac = dacRepository.findPublishedDacByIdForWebsite(dacId)
                .orElseThrow(() -> new DocNotFound("Published DAC not found for ID: " + dacId, null));

        DacDetailsWebsiteResponse response = new DacDetailsWebsiteResponse();

        // 1. Time remaining
        if (dac.getDateLimiteSoumission() != null) {
            ZonedDateTime deadline = dac.getDateLimiteSoumission().toInstant()
                    .atZone(ZoneId.systemDefault());
            ZonedDateTime now = ZonedDateTime.now();

            if (deadline.isAfter(now)) {
                java.time.Duration duration = java.time.Duration.between(now, deadline);
                long days = duration.toDays();
                long hoursOfLastDay = duration.minusDays(days).toHours();

                response.setNbrJoursRestants(days);
                response.setNbrHeuresRestants(hoursOfLastDay);
            } else {
                response.setNbrJoursRestants(0L);
                response.setNbrHeuresRestants(0L);
            }
        }

        // 2. ModePassation
        if (dac.getAppelOffre() != null && dac.getAppelOffre().getRealisation() != null
                && dac.getAppelOffre().getRealisation().getModePassation() != null) {
            response.setModePassationLibelle(dac.getAppelOffre().getRealisation().getModePassation().getLibelle());
            if (dac.getAppelOffre().getRealisation().getModePassation().getType() != null) {
                response.setModePassationType(dac.getAppelOffre().getRealisation().getModePassation().getType().name());
            }
        }

        // 3. nomAc
        if (dac.getAppelOffre() != null && dac.getAppelOffre().getPpm() != null && dac.getAppelOffre().getPpm().getAc() != null) {
            response.setNomAc(dac.getAppelOffre().getPpm().getAc().getNom());
        }

        // 4. descriptionTypeMarche
        if (dac.getAppelOffre() != null && dac.getAppelOffre().getRealisation() != null
                && dac.getAppelOffre().getRealisation().getTypeMarcher() != null) {
            response.setDescriptionTypeMarche(dac.getAppelOffre().getRealisation().getTypeMarcher().getDescription());
        }

        // 5. dateLimiteSoumission
        response.setDateLimiteSoumission(dac.getDateLimiteSoumission());

        // 6. Alfresco ID from ModelDac
        modelDacService.getModel(dac.getDacId(), dac.getDacVer()).ifPresent(modelDac -> {
            if (!Boolean.TRUE.equals(modelDac.getIsDeleted()) && modelDac.getDoc() != null && !Boolean.TRUE.equals(modelDac.getDoc().getIsDeleted())) {
                response.setAlfrescoId(modelDac.getDoc().getAlfresco_id());
            }
        });

        // 7. AppelOffre details
        if (dac.getAppelOffre() != null) {
            response.setReferenceAo(dac.getAppelOffre().getReference());
            response.setNomAppelOffre(dac.getAppelOffre().getNom());
            response.setDescriptionAo(dac.getAppelOffre().getDescription());
            response.setLotSepares(dac.getAppelOffre().getLotSepares());

            // 8. Financements
            if (dac.getAppelOffre().getRealisation() != null && dac.getAppelOffre().getRealisation().getFinancements() != null) {
                String financementsStr = dac.getAppelOffre().getRealisation().getFinancements().stream()
                        .filter(f -> f.getSourceFinancement() != null)
                        .map(f -> f.getSourceFinancement().getDescription())
                        .collect(Collectors.joining(", "));
                response.setFinancements(financementsStr);
                
                // 9. Lots
                if (dac.getAppelOffre().getRealisation().getLots() != null) {
                    List<String> lotDescriptions = dac.getAppelOffre().getRealisation().getLots().stream()
                            .map(Lot::getDescription)
                            .collect(Collectors.toList());
                    response.setLots(lotDescriptions);
                }
            }
        }

        // 10. DAC details
        response.setReferenceDac(dac.getReference());
        response.setDatePublicationDac(dac.getUpdatedAt()); // Using updatedAt for release date as per request
        response.setPayant(dac.isPayant() ? "Oui" : "Non");
        response.setConditionsParticipation(dac.getConditionsParticipation());

        // 11. DacPrix
        if (dac.isPayant()) {
            List<DacPrix> dacPrixList = dacPrixRepository.findByDac(dac.getDacId(), dac.getDacVer());
            List<DacDetailsWebsiteResponse.DacPrixWebsiteResponse> dacPrixResponses = dacPrixList.stream()
                    .map(dp -> {
                        DacDetailsWebsiteResponse.DacPrixWebsiteResponse dpRes = new DacDetailsWebsiteResponse.DacPrixWebsiteResponse();
                        if (dp.getId() != null && dp.getId().getMonnaie() != null) {
                            dpRes.setMonnaieDescription(dp.getId().getMonnaie().getDescription());
                        }
                        if (dp.getPrix() != null) {
                            dpRes.setPrix(dp.getPrix().stripTrailingZeros().toPlainString());
                        }
                        return dpRes;
                    }).collect(Collectors.toList());
            response.setDacPrix(dacPrixResponses);
        }

        // 12. Responsable DAC from Keycloak
        if (dac.getResponsableDacId() != null) {
            try {
                UserDTO keycloakUser = keycloakAdminService.getBasicUserById(dac.getResponsableDacId());
                if (keycloakUser != null) {
                    DacDetailsWebsiteResponse.ResponsableDacResponse respRes = new DacDetailsWebsiteResponse.ResponsableDacResponse();
                    respRes.setFirstName(keycloakUser.getFirstName());
                    respRes.setLastName(keycloakUser.getLastName());
                    respRes.setEmail(keycloakUser.getEmail());
                    respRes.setTel(keycloakUser.getMobile() != null ? keycloakUser.getMobile() : keycloakUser.getFixe());
                    respRes.setAdresse(keycloakUser.getAddress());
                    response.setResponsableDac(respRes);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch responsable DAC user from Keycloak ID: {}. Error: {}", dac.getResponsableDacId(), e.getMessage());
            }
        }

        return response;
    }
}
