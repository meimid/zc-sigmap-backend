package com.sigmap.common.docs;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.DacSpecifications;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.entity.ExtraitType;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.model_agpm.ModelAgpm;
import com.sigmap.common.model_agpm.ModelAgpmService;
import com.sigmap.common.model_agpm.ModelAgpmSpecifications;
import com.sigmap.common.model_dac.ModelDac;
import com.sigmap.common.model_dac.ModelDacService;
import com.sigmap.common.model_dac.ModelDacSpecifications;
import com.sigmap.common.model_paa.ModelPaa;
import com.sigmap.common.model_paa.ModelPaaService;
import com.sigmap.common.model_paa.ModelPaaSpecifications;
import com.sigmap.common.model_ppm.ModelPpm;
import com.sigmap.common.model_ppm.ModelPpmService;
import com.sigmap.common.model_ppm.ModelPpmSpecifications;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaService;
import com.sigmap.common.paas.PaaSpecifications;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunionService;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunionSpecifications;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtraitService;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtraitSpecifications;
import com.sigmap.common.process_verbal_reunion_extrait.excepton.ExtraitNotFound;
import com.sigmap.common.process_verbal_reunion_extrait_ppm.ProcessVerbalReunionExtraitPpm;
import com.sigmap.common.process_verbal_reunion_extrait_ppm.ProcessVerbalReunionExtraitPpmService;
import com.sigmap.common.process_verbal_reunion_extrait_ppm.ProcessVerbalReunionExtraitPpmSpecifications;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionService;
import com.sigmap.common.reunion.ReunionSpecifications;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunion.response.PvResponse;
import com.sigmap.common.reunion.response.ReunionArgumentDocuments;
import com.sigmap.common.reunionPpm.ReunionPpmDocSpecifications;
import com.sigmap.common.reunion_dac_doc.ReunionDacDoc;
import com.sigmap.common.reunion_dac_doc.ReunionDacDocService;
import com.sigmap.common.reunion_dac_doc.ReunionDacDocSpecifications;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadDocsServiceIpml implements DownloadDocsService {

        private final DocsService docsService;
        private final PaaService paaService;
        private final CurrentUserService currentUserService;
        private final AgpmService agpmService;
        private final ProcessVerbalReunionService processVerbalReunionService;
        private final PpmService ppmService;
        private final ReunionService reunionService;
        private final ProcessVerbalReunionExtraitService processVerbalReunionExtraitService;
        private final ProcessVerbalReunionExtraitPpmService processVerbalReunionExtraitPpmService;
        private final ReunionPpmDocService reunionPpmDocService;
        private final DacService dacService;
        private final ReunionDacDocService reunionDacDocService;
        private final ModelDacService modelDacService;
        private final ModelPpmService modelPpmService;
        private final ModelPaaService modelPaaService;
        private final ModelAgpmService modelAgpmService;
    @Override
    public byte[] downloadModelPaa(String alfrescoId) throws IOException, DocsNotFoundException, PaaNotFound {
        List<Integer> acIds = currentUserService.getCurrentUserAcIds();
        Docs doc=docsService.findByAlfescoId( alfrescoId).orElseThrow(()->new DocsNotFoundException(""));
        ModelPaa model = modelPaaService.findOne(
                ModelPaaSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new PaaNotFound("Model Paa not found for doc " + doc.getId(), null, null));
        if(acIds.contains(model.getPaa().getAc().getId())) {
            return   docsService.downloadDocument(alfrescoId);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public byte[] downloadModelAgpm(String alfrescoId) throws IOException, DocsNotFoundException, AgpmNotFound {
        List<Integer> acIds = currentUserService.getCurrentUserAcIds();
        Docs doc=docsService.findByAlfescoId( alfrescoId).orElseThrow(()->new DocsNotFoundException(""));
        ModelAgpm model = modelAgpmService.findOne(
                ModelAgpmSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new AgpmNotFound("Model Agpm not found for doc " + doc.getId(), null, null));
        if(acIds.contains(model.getAgpm().getAc().getId())) {
            return   docsService.downloadDocument(alfrescoId);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public byte[] downloadPv(String alfrescoId) throws IOException, DocsNotFoundException {
        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        ProcessVerbalReunion pv = processVerbalReunionService.findOne(
                ProcessVerbalReunionSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new DocsNotFoundException("PV not found for doc " + doc.getId()));

        Reunion reunion = reunionService.findOne(
                Specification.where(ReunionSpecifications.deleted(false))
                        .and(ReunionSpecifications.status(ReunionStatus.CLOTURER))
                        .and(ReunionSpecifications.pvId(pv.getId()))
        ).orElseThrow(() -> new DocsNotFoundException("Reunion not found/closed for pvId " + pv.getId()));

        List<Integer> comIds = currentUserService.getCurrentUserComIds();

        boolean allowed = reunion.getReunionPpms() != null
                && reunion.getReunionPpms().stream()
                .filter(rp -> rp.getPpmId() != null && rp.getPpmVer() != null)
                .anyMatch(rp -> {
                    PvResponse dto = ppmService.listPvComByPpm(comIds, rp.getPpmId(), rp.getPpmVer());
                    return dto.getPvCPMP().contains(alfrescoId)
                            || dto.getPvCNCMPPermanente().contains(alfrescoId)
                            || dto.getPvCNCMPSpecialise().contains(alfrescoId);
                });

        if (allowed) {
            return docsService.downloadDocument(alfrescoId);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public byte[] downloadExtrait(String alfrescoId) throws IOException, DocsNotFoundException, ReunionNotFound, ExtraitNotFound {
        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        log.info("doc id: " + doc.getId());
        //  CASE 1: PAR_AC => no PPM link expected

        ProcessVerbalReunionExtrait extrait = processVerbalReunionExtraitService.findOne(
                ProcessVerbalReunionExtraitSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new ExtraitNotFound("Extrait not found for doc " + doc.getId()));

        if (ExtraitType.PAR_AC.equals(extrait.getExtraitType())) {
            List<Integer> acIds = currentUserService.getCurrentUserAcIds();

            if (extrait.getAcId() != null && acIds.contains(extrait.getAcId())) {
                return docsService.downloadDocument(alfrescoId);
            }

            throw new AccessDeniedException("Access denied");
        }

        //  CASE 2: needs PPM link
        ProcessVerbalReunionExtraitPpm link = processVerbalReunionExtraitPpmService.findOne(
                ProcessVerbalReunionExtraitPpmSpecifications
                        .byExtraitKey(extrait.getExtraitId(), extrait.getPvId(), extrait.getAcId())
                        .and(ProcessVerbalReunionExtraitPpmSpecifications.ppmNotDeleted())
        ).orElseThrow(() -> new ExtraitNotFound(
                "No PPM linked to extrait " + extrait.getExtraitId()
                        + " pvId=" + extrait.getPvId()
                        + " acId=" + extrait.getAcId()
        ));

        Integer ppmId = link.getPpmId();
        Integer ppmVersion = link.getPpmVer();
        List<Integer> acIds=currentUserService.getCurrentUserAcIds();
        List<String> extraits = processVerbalReunionExtraitService
                .findExtraitsByPpmAndAcs(acIds, ppmId, ppmVersion)
                .stream()
                .filter(e ->
                        e.getDoc() != null
                                && !Boolean.TRUE.equals(e.getIsDeleted())
                                && !Boolean.TRUE.equals(e.getDoc().getIsDeleted())
                )
                .map(e -> e.getDoc().getAlfresco_id())
                .collect(Collectors.toList());

        if(extraits.contains(alfrescoId)){
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public byte[] downloadPpmArgumentDocument(String alfrescoId)
            throws IOException, DocsNotFoundException {

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        ReunionPpmDoc link = reunionPpmDocService.findOne(
                ReunionPpmDocSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new DocsNotFoundException("No PPM link found for docId " + doc.getId()));


        if (link.getPpmId() == null || link.getPpmVer() == null) {
            throw new DocsNotFoundException("PPM id/version missing for docId " + doc.getId());
        }

        Integer ppmId = link.getPpmId();
        Integer ppmVersion = link.getPpmVer();
        List<Integer> comIds = currentUserService.getCurrentUserComIds();
        ReunionArgumentDocuments dto = ppmService.listDocPpm(comIds, ppmId, ppmVersion);

        boolean allowed =
                dto.getDocumentCPMP().contains(alfrescoId)
                        || dto.getDocumentCNCMPPermanente().contains(alfrescoId)
                        || dto.getDocumentCNCMPSpecialise().contains(alfrescoId);

        if (allowed) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public byte[] downloadModelPpm(String alfrescoId) throws IOException, DocsNotFoundException, PpmNotFound {
        List<Integer> acIds = currentUserService.getCurrentUserAcIds();

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        ModelPpm model = modelPpmService.findOne(
                ModelPpmSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new PpmNotFound("Model PPM not found for doc " + doc.getId(), null, null));

        if (model.getPpm().getAc() != null && model.getPpm().getAc().getId() != null && acIds.contains(model.getPpm().getAc().getId())) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public byte[] downloadDacArgumentDocument(String alfrescoId)
            throws IOException, DocsNotFoundException {

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        ReunionDacDoc link = reunionDacDocService.findOne(
                ReunionDacDocSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new DocsNotFoundException("No DAC link found for docId " + doc.getId()));

        if (link.getDacId() == null || link.getDacVer() == null) {
            throw new DocsNotFoundException("DAC id/version missing for docId " + doc.getId());
        }

        Integer dacId = link.getDacId();
        Integer dacVersion = link.getDacVer();

        List<Integer> comIds = currentUserService.getCurrentUserComIds();

 /*       ReunionArgumentDocuments dto = dacService.listDocDac(comIds, dacId, dacVersion);

        boolean allowed =
                dto.getDocumentCPMP().contains(alfrescoId)
                        || dto.getDocumentCNCMPPermanente().contains(alfrescoId)
                        || dto.getDocumentCNCMPSpecialise().contains(alfrescoId);

        if (allowed) {
            return docsService.downloadDocument(alfrescoId);
        }*/

        throw new AccessDeniedException("Access denied");
    }

    @Override
    public byte[] downloadDacModel(String alfrescoId) throws IOException, DocsNotFoundException {
        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        // 1) Find model_dac link by docId (not deleted doc/model)
        ModelDac model = modelDacService.findOne(
                ModelDacSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new DocsNotFoundException("Model DAC not found for docId " + doc.getId()));

        if (model.getDacId() == null || model.getDacVer() == null) {
            throw new DocsNotFoundException("DAC id/version missing for model_dac docId " + doc.getId());
        }

        Integer dacId = model.getDacId();
        Integer dacVer = model.getDacVer();

        // 2) Load DAC (not deleted)
        Dac dac = dacService.findOne(
                Specification.where(DacSpecifications.id(dacId, dacVer))
                        .and(DacSpecifications.searchByDeleted(false))
        ).orElseThrow(() -> new DocsNotFoundException("DAC not found for id=" + dacId + " ver=" + dacVer));

        // 3) Go DAC -> AppelOffre -> PPM -> AC
        if (dac.getAppelOffre() == null || dac.getAppelOffre().getPpm() == null) {
            throw new DocsNotFoundException("PPM not found through DAC -> AppelOffre for dacId=" + dacId + " ver=" + dacVer);
        }

        Ppm ppm = dac.getAppelOffre().getPpm();

        if (ppm.getAc() == null || ppm.getAc().getId() == null) {
            throw new DocsNotFoundException("AC not found for PPM id=" + ppm.getId() + " ver=" + ppm.getVersion());
        }

        Integer ppmAcId = ppm.getAc().getId();

        // 4) Authorize by user's ACs
        List<Integer> acIds = currentUserService.getCurrentUserAcIds();
        if (acIds.contains(ppmAcId)) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }
    @Override
    public byte[] downloadAgpmDoc(String alfrescoId)
            throws IOException, DocsNotFoundException, AgpmNotFound, AcNotFound {

        List<Integer> acIds = currentUserService.getCurrentUserAcIds();

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        Agpm agpm = agpmService.findAll(AgpmSpecifications.docId(doc.getId()),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "version"))
        ).stream().findFirst()
        .orElseThrow(() -> new AgpmNotFound("AGPM not found for doc " + doc.getId(), null, null));

        Integer agpmAcId = (agpm.getAc() != null ? agpm.getAc().getId() : null);
        if (agpmAcId != null && acIds.contains(agpmAcId)) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }
    @Override
    public byte[] downloadPpmDoc(String alfrescoId)
            throws IOException, DocsNotFoundException, PpmNotFound, AcNotFound {

        List<Integer> acIds = currentUserService.getCurrentUserAcIds();

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        Ppm ppm = ppmService.findAll(
                        PpmSpecifications.docId(doc.getId()),
                        PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "version"))
                ).stream().findFirst()
                .orElseThrow(() -> new PpmNotFound("PPM not found for doc " + doc.getId(), null, null));

        Integer ppmAcId = (ppm.getAc() != null ? ppm.getAc().getId() : null);
        if (ppmAcId != null && acIds.contains(ppmAcId)) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }
    @Override
    public byte[] downloadPaaDoc(String alfrescoId)
            throws IOException, DocsNotFoundException, PaaNotFound, AcNotFound {

        List<Integer> acIds = currentUserService.getCurrentUserAcIds();

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        Paa paa = paaService.findAll(
                        PaaSpecifications.docId(doc.getId()),
                        PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "version"))
                ).stream()
                .findFirst()
                .orElseThrow(() ->
                        new PaaNotFound("PAA not found for doc " + doc.getId(), null, null)
                );

        Integer paaAcId = (paa.getAc() != null ? paa.getAc().getId() : null);
        if (paaAcId != null && acIds.contains(paaAcId)) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }


}
