package com.sigmap.common.docs;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
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
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

    @Override
    public byte[] downloadModelPaa(String alfrescoId) throws IOException, DocsNotFoundException, PaaNotFound {
        List<Integer> acIds = currentUserService.getCurrentUserAcIds();
        Docs doc=docsService.findByAlfescoId( alfrescoId).orElseThrow(()->new DocsNotFoundException(""));
        Paa paa=paaService.findOne(PaaSpecifications.docId(doc.getId()).and(PaaSpecifications.isNotDeleted())).orElseThrow(()->new PaaNotFound("",null,null));
        if(acIds.contains(paa.getId())){
            return   docsService.downloadDocument(alfrescoId);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public byte[] downloadModelAgpm(String alfrescoId) throws IOException, DocsNotFoundException, AgpmNotFound {
        List<Integer> acIds = currentUserService.getCurrentUserAcIds();
        Docs doc=docsService.findByAlfescoId( alfrescoId).orElseThrow(()->new DocsNotFoundException(""));
        Agpm agpm=agpmService.findOne(AgpmSpecifications.docId(doc.getId()).and(AgpmSpecifications.isNotDeleted())).orElseThrow(()->new AgpmNotFound("",null,null));
        if(acIds.contains(agpm.getId())){
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

        ProcessVerbalReunionExtrait extrait = processVerbalReunionExtraitService.findOne(
                ProcessVerbalReunionExtraitSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new ExtraitNotFound("Extrait not found for doc " + doc.getId()));

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

        Ppm ppm = ppmService.findOne(
                PpmSpecifications.docId(doc.getId()).and(PpmSpecifications.isNotDeleted())
        ).orElseThrow(() -> new PpmNotFound("PPM not found for doc " + doc.getId(), null, null));

        if (ppm.getAc() != null && ppm.getAc().getId() != null && acIds.contains(ppm.getAc().getId())) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }


}
