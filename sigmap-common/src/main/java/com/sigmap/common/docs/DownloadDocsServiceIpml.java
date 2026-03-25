package com.sigmap.common.docs;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.AgpmSpecifications;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComRepository;
import com.sigmap.common.com.ComService;
import com.sigmap.common.com.ComSpecifications;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.DacSpecifications;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.DacDocsRepository;
import com.sigmap.common.dac_docs.DacDocsSpecifications;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
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
import com.sigmap.common.ppm_avis_note_rapporteur.PpmAvisNoteRapporteur;
import com.sigmap.common.ppm_avis_note_rapporteur.PpmAvisNoteRapporteurService;
import com.sigmap.common.ppm_avis_note_rapporteur.PpmAvisNoteRapporteurSpecifications;
import com.sigmap.common.ppm_avis_note_rapporteur.exception.PpmAvisNotNotFound;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunionService;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunionSpecifications;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtraitService;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtraitSpecifications;
import com.sigmap.common.process_verbal_reunion_extrait.excepton.ExtraitNotFound;
import com.sigmap.common.process_verbal_reunion_extrait_dac.ProcessVerbalReunionExtraitDacService;
import com.sigmap.common.process_verbal_reunion_extrait_ppm.ProcessVerbalReunionExtraitPpmService;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionRepository;
import com.sigmap.common.reunion.ReunionService;
import com.sigmap.common.reunion.ReunionSpecifications;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunionPpm.ReunionPpmDocSpecifications;
import com.sigmap.common.reunion_dac_doc.ReunionDacDoc;
import com.sigmap.common.reunion_dac_doc.ReunionDacDocService;
import com.sigmap.common.reunion_dac_doc.ReunionDacDocSpecifications;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        private final ReunionPpmDocService reunionPpmDocService;
        private final DacService dacService;
        private final ReunionDacDocService reunionDacDocService;
        private final ModelDacService modelDacService;
        private final ModelPpmService modelPpmService;
        private final ModelPaaService modelPaaService;
        private final ModelAgpmService modelAgpmService;
        private final ComRepository comRepository;
        private final DacDocsRepository dacDocsRepository;
        private final ComService comService;
        private final PpmAvisNoteRapporteurService ppmAvisNoteRapporteurService;
    private final PpmRepository ppmRepository;


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

        Reunion reunion = reunionService.findAll(
                Specification.where(ReunionSpecifications.deleted(false))
                        .and(ReunionSpecifications.status(ReunionStatus.CLOTURER)
                                .or(ReunionSpecifications.status(ReunionStatus.VALIDER)))
                        .and(ReunionSpecifications.pvId(pv.getId()))
        ).stream().findAny().orElseThrow(() -> new DocsNotFoundException("Reunion not found/closed for pvId " + pv.getId()));

        // 1) user com ids
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
        if (userComIds == null || userComIds.isEmpty()) {
            throw new AccessDeniedException("Access denied");
        }

        // 2) allowed com ids from PPM side
        List<Integer> ppmComIds =
                reunion.getReunionPpms() == null ? List.of() :
                        reunion.getReunionPpms().stream()
                                .map(rp -> rp.getPpm())
                                .filter(Objects::nonNull)
                                .flatMap(ppm -> ppm.getComs() == null ? Stream.empty() : ppm.getComs().stream())
                                .map(Com::getId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList();

        // 3) allowed com ids from DAC side
        List<Integer> dacComIds =
                reunion.getReunionDacs() == null ? List.of() :
                        reunion.getReunionDacs().stream()
                                .map(rd -> rd.getDac())
                                .filter(Objects::nonNull)
                                .flatMap(dac -> dac.getComs() == null ? Stream.empty() : dac.getComs().stream())
                                .map(Com::getId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList();

        // 4) decision
        boolean allowed = intersects(userComIds, ppmComIds) || intersects(userComIds, dacComIds);
        if (allowed) {
            return docsService.downloadDocument(alfrescoId);

        }
        throw new AccessDeniedException("Access denied");

    }



    @Override
    public byte[] downloadExtrait(String alfrescoId)
            throws IOException, DocsNotFoundException, ReunionNotFound, ExtraitNotFound {

        // 1) Doc exists
        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        // 2) Extrait exists for this doc
        ProcessVerbalReunionExtrait extrait = processVerbalReunionExtraitService.findOne(
                ProcessVerbalReunionExtraitSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new ExtraitNotFound("Extrait not found for doc " + doc.getId()));

        // 3) Reunion must exist + must be closed/validated + not deleted (keep SAME SPEC style)
        Reunion reunion = reunionService.findAll(
                Specification.where(ReunionSpecifications.deleted(false))
                        .and(ReunionSpecifications.status(ReunionStatus.CLOTURER)
                                .or(ReunionSpecifications.status(ReunionStatus.VALIDER)))
                        .and(ReunionSpecifications.pvId(extrait.getPvId()))
        ).stream().findAny().orElseThrow(() -> new AccessDeniedException("Access denied"));

        // 4) Current user scopes
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();

        // 5) Allowed COMs from PPM and DAC attached to this reunion
        List<Integer> allowedPpmComs =
                reunion.getReunionPpms() == null ? List.of() :
                        reunion.getReunionPpms().stream()
                                .flatMap(rp ->
                                        rp.getPpm() == null || rp.getPpm().getComs() == null
                                                ? java.util.stream.Stream.empty()
                                                : rp.getPpm().getComs().stream()
                                )
                                .map(Com::getId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList();

        List<Integer> allowedDacComs =
                reunion.getReunionDacs() == null ? List.of() :
                        reunion.getReunionDacs().stream()
                                .flatMap(rd ->
                                        rd.getDac() == null || rd.getDac().getComs() == null
                                                ? java.util.stream.Stream.empty()
                                                : rd.getDac().getComs().stream()
                                )
                                .map(Com::getId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList();

        // COM rule: user must intersect with PPM or DAC COM lists
        boolean comAllowed =
                (userComIds != null && !userComIds.isEmpty()) && (
                        allowedPpmComs.stream().anyMatch(userComIds::contains)
                                || allowedDacComs.stream().anyMatch(userComIds::contains)
                );


            boolean acAllowedForParAc =
                    extrait.getAcId() != null
                            && userAcIds != null
                            && userAcIds.contains(extrait.getAcId());

            if (acAllowedForParAc || comAllowed) {
                return docsService.downloadDocument(alfrescoId);
            }

        throw new AccessDeniedException("Access denied");
    }



    @Override
    public byte[] downloadPpmArgumentDocumentForPpm(String alfrescoId)
            throws IOException, DocsNotFoundException, PpmNotFound {

        // 1) Doc exists
        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException(
                        "Doc not found for alfrescoId " + alfrescoId));

        // 2) Link doc -> PPM
        ReunionPpmDoc link = reunionPpmDocService.findOne(
                ReunionPpmDocSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new DocsNotFoundException(
                "No PPM link found for docId " + doc.getId()));

        Integer ppmId = link.getPpmId();
        Integer ppmVer = link.getPpmVer();

        if (ppmId == null || ppmVer == null) {
            throw new DocsNotFoundException(
                    "PPM id/version missing for docId " + doc.getId());
        }

        // 3) MUST belong to closed/validated reunion
        reunionService.findAll(
                Specification.where(ReunionSpecifications.deleted(false))
                        .and(ReunionSpecifications.status(ReunionStatus.CLOTURER)
                                .or(ReunionSpecifications.status(ReunionStatus.VALIDER))
                                .or(ReunionSpecifications.status(ReunionStatus.EN_COURS)))
                        .and(ReunionSpecifications.hasPpmId(ppmId))
                        .and(ReunionSpecifications.hasPpmVer(ppmVer))
        ).stream().findAny().orElseThrow(() -> new AccessDeniedException("Access denied"));

        // 4) Current user scopes
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
        List<Integer> userAcIds  = currentUserService.getCurrentUserAcIds();

        // 5) Allowed COMs from this PPM
        List<Integer> allowedPpmComs =
                (link.getPpm() == null || link.getPpm().getComs() == null)
                        ? List.of()
                        : link.getPpm().getComs().stream()
                        .map(Com::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        boolean comAllowed =
                (userComIds != null && !userComIds.isEmpty())
                        && allowedPpmComs.stream().anyMatch(userComIds::contains);

        // 6) AC rule (simple and strict)
        boolean acAllowed =
                link.getPpm() != null
                        && link.getPpm().getAc() != null
                        && userAcIds != null
                        && userAcIds.contains(link.getPpm().getAc().getId());

        if (acAllowed || comAllowed) {
            return docsService.downloadDocument(alfrescoId);
        }

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

        // 4) Authorize by user's ACs
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        List<Integer> userComIds=currentUserService.getCurrentUserComIds();
        boolean comAllowed =
                (userComIds != null && !userComIds.isEmpty())
                        && dac.getComs().stream().map(Com::getId).anyMatch(userComIds::contains);

        log.info("dac coms {}",dac.getComs().stream().map(Com::getId).collect(Collectors.toList()));
        log.info("user coms {}",userComIds);


        // 6) AC rule (simple and strict)
        boolean acAllowed =
                dac.getAppelOffre() != null
                       && ppm.getAc() != null
                        && userAcIds != null
                        && userAcIds.contains(ppm.getAc().getId());

        if (acAllowed || comAllowed) {
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

        Agpm agpm = agpmService.findAll(AgpmSpecifications.docId(doc.getId()).and(AgpmSpecifications.isNotDeleted())
                ).stream().findFirst()
        .orElseThrow(() -> new AgpmNotFound("AGPM not found for doc " + doc.getId(), null, null));

        Integer agpmAcId = (agpm.getAc() != null ? agpm.getAc().getId() : null);
        if (agpmAcId != null && acIds.contains(agpmAcId)) {
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
                        PaaSpecifications.docId(doc.getId()).and(PaaSpecifications.isNotDeleted()))
                .stream()
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

    @Override
    public byte[] downloadPpmArgumentDocumentForReunion(String alfrescoId) throws IOException, DocsNotFoundException, PpmNotFound, AcNotFound {
        // 1) Doc exists
        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException(
                        "Doc not found for alfrescoId " + alfrescoId));

        // 2) Link doc -> PPM
        ReunionPpmDoc link = reunionPpmDocService.findOne(
                ReunionPpmDocSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new DocsNotFoundException(
                "No PPM link found for docId " + doc.getId()));

        Integer ppmId = link.getPpmId();
        Integer ppmVer = link.getPpmVer();

        if (ppmId == null || ppmVer == null) {
            throw new DocsNotFoundException(
                    "PPM id/version missing for docId " + doc.getId());
        }

        // 3) MUST belong to closed/validated reunion
        reunionService.findAll(
                Specification.where(ReunionSpecifications.deleted(false))
                        .and(ReunionSpecifications.status(ReunionStatus.CLOTURER)
                                .or(ReunionSpecifications.status(ReunionStatus.VALIDER))
                                .or(ReunionSpecifications.status(ReunionStatus.EN_COURS)))
                        .and(ReunionSpecifications.hasPpmId(ppmId))
                        .and(ReunionSpecifications.hasPpmVer(ppmVer))
        ).stream().findAny().orElseThrow(() -> new AccessDeniedException("Access denied"));

        // 4) Current user scopes
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
        List<Integer> userAcIds  = currentUserService.getCurrentUserAcIds();

        // 5) Allowed COMs from this PPM
        List<Integer> allowedPpmComs =
                (link.getPpm() == null || link.getPpm().getComs() == null)
                        ? List.of()
                        : link.getPpm().getComs().stream()
                        .map(Com::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        boolean comAllowed =
                (userComIds != null && !userComIds.isEmpty())
                        && allowedPpmComs.stream().anyMatch(userComIds::contains);

        // 6) AC rule (simple and strict)
        boolean acAllowed =
                link.getPpm() != null
                        && link.getPpm().getAc() != null
                        && userAcIds != null
                        && userAcIds.contains(link.getPpm().getAc().getId());

        if (acAllowed || comAllowed) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }


    @Override
    public byte[] downloadDacArgumentDocumentForReunion(String alfrescoId)
            throws IOException, DocsNotFoundException {

        // 1) Doc exists
        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        // 2) Link doc -> DAC
        ReunionDacDoc link = reunionDacDocService.findOne(
                ReunionDacDocSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new DocsNotFoundException("No DAC link found for docId " + doc.getId()));

        Integer dacId = link.getDacId();
        Integer dacVer = link.getDacVer();

        if (dacId == null || dacVer == null) {
            throw new DocsNotFoundException("DAC id/version missing for docId " + doc.getId());
        }

        // 3) MUST belong to closed/validated/in-progress reunion
        reunionService.findAll(
                Specification.where(ReunionSpecifications.deleted(false))
                        .and(ReunionSpecifications.status(ReunionStatus.CLOTURER)
                                .or(ReunionSpecifications.status(ReunionStatus.VALIDER))
                                .or(ReunionSpecifications.status(ReunionStatus.EN_COURS)))
                        .and(ReunionSpecifications.hasDacId(dacId))
                        .and(ReunionSpecifications.hasDacVer(dacVer))
        ).stream().findAny().orElseThrow(() -> new AccessDeniedException("Access denied"));

        // 4) Current user scopes
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
        List<Integer> userAcIds  = currentUserService.getCurrentUserAcIds();

        // 5) Allowed COMs from this DAC (null-safe)
        List<Integer> allowedDacComs =
                (link.getDac() == null || link.getDac().getComs() == null)
                        ? List.of()
                        : link.getDac().getComs().stream()
                        .map(Com::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        boolean comAllowed =
                (userComIds != null && !userComIds.isEmpty())
                        && allowedDacComs.stream().anyMatch(userComIds::contains);

        // 6) AC rule (DAC -> AppelOffre -> PPM -> AC)
        Integer dacAcId =
                (link.getDac() != null
                        && link.getDac().getAppelOffre() != null
                        && link.getDac().getAppelOffre().getPpm() != null
                        && link.getDac().getAppelOffre().getPpm().getAc() != null)
                        ? link.getDac().getAppelOffre().getPpm().getAc().getId()
                        : null;

        boolean acAllowed =
                dacAcId != null
                        && userAcIds != null
                        && userAcIds.contains(dacAcId);

        if (acAllowed || comAllowed) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }



    private static boolean intersects(List<Integer> a, List<Integer> b) {
        if (a == null || a.isEmpty() || b == null || b.isEmpty()) return false;
        Set<Integer> set = new HashSet<>(a);
        return b.stream().anyMatch(set::contains);
    }



    @Override
    public byte[] downloadPpmDoc(String alfrescoId)
            throws IOException, DocsNotFoundException, PpmNotFound, AcNotFound {

        List<Integer> acIds = currentUserService.getCurrentUserAcIds();
        List<Integer> comIds=currentUserService.getCurrentUserComIds();

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        Ppm ppm = ppmService.findAll(
                        PpmSpecifications.docId(doc.getId()).and(PpmSpecifications.isNotDeleted())).stream().findFirst()
                .orElseThrow(() -> new PpmNotFound("PPM not found for doc " + doc.getId(), null, null));

        Integer ppmAcId = (ppm.getAc() != null ? ppm.getAc().getId() : null);
        List<Integer> ppmComIds=ppm.getComs().stream().map(Com::getId).collect(Collectors.toList());
        boolean acMatch = ppmAcId != null && acIds.contains(ppmAcId);

        boolean comMatch = ppmComIds.stream()
                .anyMatch(comIds::contains);

        if (acMatch || comMatch) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }
    @Override
    public byte[] downloadModelPpm(String alfrescoId) throws IOException, DocsNotFoundException, PpmNotFound {
        List<Integer> acIds = currentUserService.getCurrentUserAcIds();
        List<Integer> comIds = currentUserService.getCurrentUserComIds();
        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        ModelPpm model = modelPpmService.findOne(
                ModelPpmSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new PpmNotFound("Model PPM not found for doc " + doc.getId(), null, null));
        Ppm ppm = model.getPpm();
        Integer ppmAcId=ppm.getAc().getId();
        List<Integer> ppmComIds = ppm.getComs()
                .stream()
                .map(Com::getId)
                .toList();

        boolean acMatch = ppmAcId != null && acIds.contains(ppmAcId);

        boolean comMatch = ppmComIds.stream()
                .anyMatch(comIds::contains);

        if (acMatch || comMatch) {
            return docsService.downloadDocument(alfrescoId);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public byte[] downloadDacArgumentDocumentForDac(String alfrescoId)
            throws IOException, DocsNotFoundException {

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        // 2) Link doc -> DAC
        ReunionDacDoc link = reunionDacDocService.findOne(
                ReunionDacDocSpecifications.docId(doc.getId())
        ).orElseThrow(() -> new DocsNotFoundException("No DAC link found for docId " + doc.getId()));

        Integer dacId = link.getDacId();
        Integer dacVer = link.getDacVer();

        if (dacId == null || dacVer == null) {
            throw new DocsNotFoundException("DAC id/version missing for docId " + doc.getId());
        }

        // 3) MUST belong to closed/validated/in-progress reunion
        reunionService.findAll(
                Specification.where(ReunionSpecifications.deleted(false))
                        .and(ReunionSpecifications.status(ReunionStatus.CLOTURER)
                                .or(ReunionSpecifications.status(ReunionStatus.VALIDER))
                                .or(ReunionSpecifications.status(ReunionStatus.EN_COURS)))
                        .and(ReunionSpecifications.hasDacId(dacId))
                        .and(ReunionSpecifications.hasDacVer(dacVer))
        ).stream().findAny().orElseThrow(() -> new AccessDeniedException("Access denied"));

        // 4) Current user scopes
        List<Integer> userComIds = currentUserService.getCurrentUserComIds();
        List<Integer> userAcIds  = currentUserService.getCurrentUserAcIds();

        // 5) Allowed COMs from this DAC (null-safe)
        List<Integer> allowedDacComs =
                (link.getDac() == null || link.getDac().getComs() == null)
                        ? List.of()
                        : link.getDac().getComs().stream()
                        .map(Com::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        boolean comAllowed =
                (userComIds != null && !userComIds.isEmpty())
                        && allowedDacComs.stream().anyMatch(userComIds::contains);

        // 6) AC rule (DAC -> AppelOffre -> PPM -> AC)
        Integer dacAcId =
                (link.getDac() != null
                        && link.getDac().getAppelOffre() != null
                        && link.getDac().getAppelOffre().getPpm() != null
                        && link.getDac().getAppelOffre().getPpm().getAc() != null)
                        ? link.getDac().getAppelOffre().getPpm().getAc().getId()
                        : null;

        boolean acAllowed =
                dacAcId != null
                        && userAcIds != null
                        && userAcIds.contains(dacAcId);

        if (acAllowed || comAllowed) {
            return docsService.downloadDocument(alfrescoId);
        }

        throw new AccessDeniedException("Access denied");
    }


    @Override
    public byte[] downloadComDocument(String alfrescoId)
            throws IOException, DocsNotFoundException {

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() ->
                        new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        boolean linked = comRepository.exists(
                Specification.where(ComSpecifications.deleted(false))
                        .and(ComSpecifications.hasDocId(doc.getId()))
        );

        if (!linked) {
            throw new DocsNotFoundException("No COM link found for docId " + doc.getId());
        }

        return docsService.downloadDocument(alfrescoId);
    }

    @Override
    public byte[] downloadDacDocsDocument(String alfrescoId) throws IOException, DocsNotFoundException {

        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        List<Integer> acIds = currentUserService.getCurrentUserAcIds();
        List<Integer> comIds = currentUserService.getCurrentUserComIds();
        if (acIds == null || acIds.isEmpty()) {
            throw new AccessDeniedException("Access denied");
        }

        boolean allowed = dacDocsRepository.exists(
                Specification.where(DacDocsSpecifications.hasDocId(doc.getId()))
                        .and(DacDocsSpecifications.ppmAcIn(acIds))
        );
        List<Integer> dacComIds=new ArrayList<>();
        Optional<DacDocs> dacDocs = dacDocsRepository.findAll(DacDocsSpecifications.hasDocId(doc.getId())).stream().findFirst();
        if(dacDocs.isPresent()) {
            dacComIds=dacDocs.get().getDac().getComs().stream().map(Com::getId).collect(Collectors.toList());
        }

        boolean allowedAc=acIds.contains(dacDocs.get().getDac().getAppelOffre().getPpm().getAc().getId());
        if (allowed&&(allowedAc||dacComIds.stream().anyMatch(comIds::contains))) {
            return docsService.downloadDocument(alfrescoId);

        }
        throw new AccessDeniedException("Access denied");

    }

    @Override
    public byte[] downloadAvisDocsDocument(String alfrescoId) throws IOException, DocsNotFoundException, PpmNotFound, AcNotFound {
        Docs doc = docsService.findByAlfescoId(alfrescoId)
                .orElseThrow(() -> new DocsNotFoundException("Doc not found for alfrescoId " + alfrescoId));

        PpmAvisNoteRapporteur avis =
                ppmAvisNoteRapporteurService
                        .findOne(Specification.where(PpmAvisNoteRapporteurSpecifications.docIdNote(doc.getId())))
                        .orElseThrow(() -> new PpmAvisNotNotFound(""));
        Ppm ppm= ppmService.findAll(Specification.where(PpmSpecifications.ppmId(avis.getPpmId()).and(PpmSpecifications.version(avis.getPpmVer())
                .and(PpmSpecifications.deleted(false))))).stream().findFirst().orElseThrow(()->new PpmIdNotFound("",avis.getPpmId()));
        List<Com> coms=ppm.getComs();

        List<Integer> comIds = currentUserService.getCurrentUserComIds();
        Set<String> userComTypes = comService.findAll(
                        Specification.where(ComSpecifications.deleted(false))
                                .and(ComSpecifications.idIn(comIds))).stream()
                .map(c -> c.getComType() != null ? c.getComType().getCode() : null)
                .collect(Collectors.toSet());


        List<Integer> ppmComIds=coms.stream().filter(c->!c.getComType().getIsDeleted()
                &&c.getComType().getCode().equals("CNCMP-S")).map(Com::getId).collect(Collectors.toList());
        log.info("ppmComIds: {}",ppmComIds);
        log.info("userComTypes: {}",userComTypes);
        log.info("user comIds:{}",comIds);

        if (ppmComIds.stream().anyMatch(comIds::contains)
                &&userComTypes.stream().anyMatch("CNCMP-S"::equals)) {
            return docsService.downloadDocument(alfrescoId);

        }
        throw new AccessDeniedException("Access denied");    }

}
