package com.sigmap.common.process_verbal;

import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal.exception.ProcessVerbalNotFound;
import com.sigmap.common.process_verbal.request.ProcessVerbalRequest;
import com.sigmap.common.pv_membre_present.PvMembrePresent;
import com.sigmap.common.pv_membre_present.PvMembrePresentRepository;
import com.sigmap.common.pv_membre_present.request.PvMembrePresentRequest;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.UserComRepository;
import com.sigmap.common.utils.SequenceGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ProcessVerbalServiceImpl implements ProcessVerbalService {
    private final ProcessVerbalRepository repository;
    private final PpmService ppmService;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final RefParamService refParamService;
    private final PvMembrePresentRepository pvMembrePresentRepository;
    private final UserComRepository userComRepository;
    private final DocumentCommuneService documentCommuneService;
    private final KeycloakAdminService keycloakAdminService;

    public ProcessVerbalServiceImpl(ProcessVerbalRepository repository, PpmService ppmService, SequenceGeneratorService sequenceGeneratorService, RefParamService refParamService, PvMembrePresentRepository pvMembrePresentRepository, UserComRepository userComRepository, DocumentCommuneService documentCommuneService, KeycloakAdminService keycloakAdminService) {
        this.repository = repository;
        this.ppmService = ppmService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.refParamService = refParamService;
        this.pvMembrePresentRepository = pvMembrePresentRepository;
        this.userComRepository = userComRepository;
        this.documentCommuneService = documentCommuneService;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Override
    public ProcessVerbal save(ProcessVerbal processVerbal) {
        return repository.save(processVerbal);
    }

    @Override
    public ProcessVerbal create(ProcessVerbalRequest request) throws PpmNotFound, AcNotFound, ComNotFound, RefParamNotFound, IOException {
        RefParam typeId=refParamService.findById(request.getTypeId());
        Ppm ppm=ppmService.findByIdPpm(request.getPpmId(),request.getPpmVersion()).orElseThrow(()->new PpmIdNotFound("",request.getPpmId()));
        UUID uuid = UUID.randomUUID();
        if(request.getMembers()==null || request.getMembers().isEmpty()) {
            request.setMembers(new ArrayList<>());
        }
        ProcessVerbal processVerbal=new ProcessVerbal();
        processVerbal.setMembres(new HashSet<>());
        log.info("memberr"+processVerbal.getMembres().size());
        log.info("memberr"+processVerbal.getMembres());
        String processVerbalId= "ppm"+"/"+ppm.getId()+"/"+ppm.getVersion()+"/"+uuid;
        List<UserCom> usersCom= userComRepository.findAllByComIdAndIsDeletedFalse(request.getComId());
        processVerbal.setProcessVerbalId(processVerbalId);
        processVerbal.setPpmId(ppm.getId());
        processVerbal.setPpmVersion(ppm.getVersion());
        if(request.getDocuments()!=null){
            documentCommuneService.processAndSaveDocumentsForPv(request.getDocuments(),ppm,Constants.PATH_ENTREPOT_GED_PPM,ppm.getId(),ppm.getVersion(),processVerbalId);
        }

        processVerbal.setObservation(request.getObservation());
        processVerbal.setDecision(request.isDecision());
        processVerbal.setTypeId(typeId);
        if(request.getStatus()!=null) {
            processVerbal.setStatus(request.getStatus());

        }
        if(request.getDateDebutReunion()!=null){
            processVerbal.setDateDebutReunion(request.getDateDebutReunion());
        }
        else {
            processVerbal.setDateDebutReunion(LocalDateTime.now());
        }
        processVerbal.setPpm(ppm);
        processVerbal= repository.save(processVerbal);
        createAndSetMembersFromUsers(request,processVerbalId,usersCom);
       Set<PvMembrePresent> pvMembrePresent=processVerbal.getMembres();

       // add the invited user to member set
       if(pvMembrePresent.size()!=request.getMembers().size()) {
           Set<PvMembrePresentRequest> invitedMemebers=request.getMembers().stream().filter(el->!usersCom.contains(el)).collect(Collectors.toSet());
           for(PvMembrePresentRequest membrePresent:invitedMemebers) {
               PvMembrePresent newMember = new PvMembrePresent();
               newMember.setPpmId(ppm.getId());
               newMember.setPpmVersion(ppm.getVersion());
               newMember.setProcessVerbalId(processVerbalId);
               newMember.setPvMembrePresentId(sequenceGeneratorService.getNextId(Constants.PV_MEMEBER_PRESENT));
               newMember.setNom(membrePresent.getNom());
               newMember.setPrenom(membrePresent.getPrenom());
               newMember.setPresent(membrePresent.isPresent());
               RefParam fonction = refParamService.findById(request.getFuncationId());
               newMember.setFonctionId(fonction);
               processVerbal.getMembres().add(newMember);
               if(membrePresent.getUserId()!=null) {
                   newMember.setUserId(membrePresent.getUserId());
               }
           }
       }
        repository.save(processVerbal);
        return processVerbal;
    }

    public void createAndSetMembersFromUsers(ProcessVerbalRequest request,String processVerbalId, List<UserCom> usersToSave) throws RefParamNotFound {
        ProcessVerbal processVerbal=repository.findByPpmIdAndPpmVersionAndProcessVerbalIdAndIsDeletedFalse(request.getPpmId(),request.getPpmVersion(),processVerbalId).orElseThrow(()-> new ProcessVerbalNotFound("",request.getPpmId(),request.getPpmVersion(),processVerbalId));
        Map<String, PvMembrePresentRequest> requestMemberStatusMap = request.getMembers().stream()
                .filter(member -> member.getUserId() != null)
                .collect(Collectors.toMap(PvMembrePresentRequest::getUserId, Function.identity()));

        for (UserCom user : usersToSave) {
            UserDTO userDTO = keycloakAdminService.getUserById(user.getUserId());
            PvMembrePresent newMember = new PvMembrePresent();
            newMember.setPpmId(request.getPpmId());
            newMember.setPpmVersion(request.getPpmVersion());
            newMember.setProcessVerbalId(processVerbalId);
            newMember.setPvMembrePresentId(sequenceGeneratorService.getNextId(Constants.PV_MEMEBER_PRESENT));
            newMember.setNom(userDTO.getFirstName());
            newMember.setPrenom(userDTO.getLastName());
            if(user.getUserId()!=null){
                newMember.setUserId(user.getUserId());
            }
            newMember.setPresent(processVerbal.getMembres().contains(newMember));
            RefParam fonction = refParamService.findById(request.getFuncationId());
            newMember.setFonctionId(fonction);
            PvMembrePresentRequest attendanceData = requestMemberStatusMap.get(user.getUserId());
            boolean wasPresent = (attendanceData != null && attendanceData.isPresent());
            newMember.setPresent(wasPresent);
            processVerbal.getMembres().add(newMember);
        }
        repository.save(processVerbal);
    }


}
