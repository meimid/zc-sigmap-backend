package com.sigmap.common.reunion;

import com.sigmap.common.AlfrescoConf.DocumentCommuneService;
import com.sigmap.common.Participant.Participant;
import com.sigmap.common.Participant.ParticipantService;

import com.sigmap.common.Participant.exception.MultiplePresidentsNotAllowedException;
import com.sigmap.common.Participant.exception.PresidentFonctionRequiredException;
import com.sigmap.common.Participant.request.ParticipantRequest;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComService;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.config.ParticipantType;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.docs.Response.DocsResponseDTO;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.ParticipantFonction;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunion;
import com.sigmap.common.process_verbal_reunion.ProcessVerbalReunionService;
import com.sigmap.common.process_verbal_reunion.request.ProcessVerbalReunionRequest;
import com.sigmap.common.process_verbal_reunion_doc.ProcessVerbalReunionDoc;
import com.sigmap.common.process_verbal_reunion_doc.response.ProcessVerbalReunionDocResponse;
import com.sigmap.common.reunion.exception.*;
import com.sigmap.common.reunion.exception.DateBefore;
import com.sigmap.common.reunion.mapper.ReunionMapper;
import com.sigmap.common.reunion.request.ReunionRequest;
import com.sigmap.common.reunion.response.ReunionWithParticipantsAndPvDto;
import com.sigmap.common.reunionDac.ReunionDac;
import com.sigmap.common.reunionDac.ReunionDacId;
import com.sigmap.common.reunionDac.ReunionDacRepository;
import com.sigmap.common.reunionDac.request.ReunionDacRequest;
import com.sigmap.common.reunionPpm.ReunionPpm;
import com.sigmap.common.reunionPpm.ReunionPpmId;
import com.sigmap.common.reunionPpm.ReunionPpmRepository;
import com.sigmap.common.reunionPpm.request.ReunionPpmRequest;
import com.sigmap.common.reunion_dac_doc.ReunionDacDoc;
import com.sigmap.common.reunion_ppm_doc.ReunionPpmDoc;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.UserComRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReunionServiceImpl implements ReunionService{
    private final ReunionRepository repository;
    private final ComService comService;
    private final UserComRepository userComRepository;
    private final ParticipantService participantService;
    private final KeycloakAdminService keycloakAdminService;
    private final PpmService ppmService;
    private final DacService dacService;
    private final ReunionPpmRepository reunionPpmRepository;
    private final ReunionDacRepository reunionDacRepository;
    private final MailService mailService;
    private final CurrentUserService currentUserService;
    private final DocumentCommuneService documentCommuneService;
    private final ProcessVerbalReunionService processVerbalReunionService;


    public ReunionServiceImpl(ReunionRepository repository, ComService comService, UserComRepository userComRepository,
                              ParticipantService participantService, KeycloakAdminService keycloakAdminService,
                              PpmService ppmService, DacService dacService, ReunionPpmRepository reunionPpmRepository,
                              ReunionDacRepository reunionDacRepository, MailService mailService, CurrentUserService currentUserService, DocumentCommuneService documentCommuneService, ProcessVerbalReunionService processVerbalReunionService) {
        this.repository = repository;
        this.comService = comService;
        this.userComRepository = userComRepository;
        this.participantService = participantService;
        this.keycloakAdminService = keycloakAdminService;
        this.ppmService = ppmService;
        this.dacService = dacService;
        this.reunionPpmRepository = reunionPpmRepository;
        this.reunionDacRepository = reunionDacRepository;
        this.mailService = mailService;
        this.currentUserService = currentUserService;
        this.documentCommuneService = documentCommuneService;
        this.processVerbalReunionService = processVerbalReunionService;
    }
    @Override
    public Page<Reunion> findAll(Specification<Reunion> spec, Pageable pageable) {

              return repository.findAll(reunionSpecificationRoleAndComIds(spec), pageable);
    }

    @Override
    public Page<Reunion> findAllDeleted(Specification<Reunion> spec, Pageable pageable) {
        Specification<Reunion> finalSpec = Specification.where(spec).and(ReunionSpecifications.deleted(true));
        return repository.findAll(finalSpec, pageable);
    }

    @Override
    public Page<Reunion> findAllDeleted(Pageable pageable) {
        return findAllDeleted(null, pageable);
    }

    @Override
    public Optional<Reunion> findDeletedById(Integer id) {
        Specification<Reunion> spec = ReunionSpecifications.deleted(true).and((root, query, cb) -> cb.equal(root.get("id"), id));
        return repository.findOne(spec);
    }

    @Override
    public Optional<Reunion> findOne(Specification<Reunion> spec) {
        return repository.findOne(reunionSpecificationRoleAndComIds(spec));
    }

    @Override
    public Reunion findById(Integer id) throws ReunionNotFound {
        Specification<Reunion> specification =
                ReunionSpecifications.id(id).and(ReunionSpecifications.deleted(false));
        Reunion reunion= findOne(specification)
                .orElseThrow(() -> new ReunionNotFound("Reunion not found with id: " , id));
        reunion.setParticipants(  reunion.getParticipants().stream().filter(participant -> !participant.getIsDeleted()).collect(Collectors.toList()));
       return reunion;
    }

    @Override
    public Reunion save(Reunion reunion) {
        return repository.save(reunion);
    }
    @Override
    @Transactional
    public Reunion create(ReunionRequest request) throws PpmNotFound, AcNotFound, ComNotFound, IOException, DocNotFound {
        Com com = comService.findById(request.getComId());
        if(request.getReunionDate() != null) {
        Calendar nowCal = Calendar.getInstance();
        nowCal.set(Calendar.HOUR_OF_DAY, 0);
        nowCal.set(Calendar.MINUTE, 0);
        nowCal.set(Calendar.SECOND, 0);
        nowCal.set(Calendar.MILLISECOND, 0);
        Calendar reqCal = Calendar.getInstance();
        reqCal.setTime(request.getReunionDate().getTime());
        reqCal.set(Calendar.HOUR_OF_DAY, 0);
        reqCal.set(Calendar.MINUTE, 0);
        reqCal.set(Calendar.SECOND, 0);
        reqCal.set(Calendar.MILLISECOND, 0);
        if(reqCal.before(nowCal)){
            throw new DateBefore("Reunion date cannot be before now");
        }
        }
      //  Reunion reunion = ReunionMapper.INSTANCE.map(request);
        Reunion reunion = new Reunion();
        reunion.setCom(com);
        if(request.getReunionDate() != null) {
            reunion.setReunionDate(request.getReunionDate());
        }
        if(request.getEmplacement() != null) {
            reunion.setEmplacement(request.getEmplacement());
        }
        if(request.getObject()!=null) {
            reunion.setObject(request.getObject());
        }
        reunion.setStatus(ReunionStatus.BROUILLON);

        Reunion savedReunion = repository.save(reunion);
        createAndSetParticipantsFromUsers(savedReunion, request);
        synchronizePpms(request, savedReunion);
        synchronizeDacs(request, savedReunion);
        return reunion;
    }

    @Override
    @Transactional
    public Reunion updateBrillon(Integer id, ReunionRequest request,ReunionStatus status) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound {
        Reunion reunionToUpdate=findById(id);

        if(reunionToUpdate.getStatus() == ReunionStatus.BROUILLON) {
            if(request.getReunionDate() != null) {
                Calendar nowCal = Calendar.getInstance();
                nowCal.set(Calendar.HOUR_OF_DAY, 0);
                nowCal.set(Calendar.MINUTE, 0);
                nowCal.set(Calendar.SECOND, 0);
                nowCal.set(Calendar.MILLISECOND, 0);
                Calendar reqCal = Calendar.getInstance();
                reqCal.setTime(request.getReunionDate().getTime());
                reqCal.set(Calendar.HOUR_OF_DAY, 0);
                reqCal.set(Calendar.MINUTE, 0);
                reqCal.set(Calendar.SECOND, 0);
                reqCal.set(Calendar.MILLISECOND, 0);
                if(reqCal.before(nowCal)){
                    throw new DateBefore("Reunion date cannot be before now");
                }
            }

            ReunionMapper.INSTANCE.updateFromRequest(request,reunionToUpdate);
            reunionToUpdate.setStatus(status);
            synchronizePpms(request, reunionToUpdate);
            synchronizeDacs(request, reunionToUpdate);
            synchronizeParticipants(request, reunionToUpdate);

            return save(reunionToUpdate);
        }
        else {
            throw new ReunionStatusException("", reunionToUpdate.getStatus());
        }
    }
    @Override
    @Transactional
    public Reunion updatePlanifierOrNot(Integer id, ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound, MessagingException, IOException, DocNotFound {
        Reunion reunionToUpdate=findById(id);
    /*    reunionToUpdate.setParticipants(new ArrayList<>(reunionToUpdate.getParticipants()));
        reunionToUpdate.setReunionPpms(new ArrayList<>(reunionToUpdate.getReunionPpms()));
        reunionToUpdate.setReunionDacs(new ArrayList<>(reunionToUpdate.getReunionDacs()));*/
        Calendar nowCal = Calendar.getInstance();
        nowCal.set(Calendar.HOUR_OF_DAY, 0);
        nowCal.set(Calendar.MINUTE, 0);
        nowCal.set(Calendar.SECOND, 0);
        nowCal.set(Calendar.MILLISECOND, 0);
        if(reunionToUpdate.getStatus().equals(ReunionStatus.BROUILLON)){

        ReunionMapper.INSTANCE.updateFromRequest(request,reunionToUpdate);
            synchronizePpms(request, reunionToUpdate);
            synchronizeDacs(request, reunionToUpdate);
            synchronizeParticipants(request, reunionToUpdate);



        if(request.getReunionDate()!=null){
            Calendar reqCal = Calendar.getInstance();
            reqCal.setTime(request.getReunionDate().getTime());
            reqCal.set(Calendar.HOUR_OF_DAY, 0);
            reqCal.set(Calendar.MINUTE, 0);
            reqCal.set(Calendar.SECOND, 0);
            reqCal.set(Calendar.MILLISECOND, 0);
            if(reqCal.before(nowCal)){
                throw new DateBefore("Reunion date cannot be before now");
            }
            reunionToUpdate.setStatus(ReunionStatus.PLANIFIER);
            sendReunionInvitations(id);

        }else{
            reunionToUpdate.setReunionDate(null);
            reunionToUpdate.setStatus(ReunionStatus.NON_PLANIFIER);
        }


        return save(reunionToUpdate);
    }else if(reunionToUpdate.getStatus().equals(ReunionStatus.PLANIFIER)||reunionToUpdate.getStatus().equals(ReunionStatus.NON_PLANIFIER)||reunionToUpdate.getStatus().equals(ReunionStatus.EN_RETARD)){
            if(request.getReunionDate()!=null){
                Calendar reqCal = Calendar.getInstance();
                reqCal.setTime(request.getReunionDate().getTime());

                reqCal.set(Calendar.HOUR_OF_DAY, 0);
                reqCal.set(Calendar.MINUTE, 0);
                reqCal.set(Calendar.SECOND, 0);
                reqCal.set(Calendar.MILLISECOND, 0);

                if(reqCal.before(nowCal)){
                    throw new DateBefore("Reunion date cannot be before now");
                }

                reunionToUpdate.setStatus(ReunionStatus.PLANIFIER);
                if(!request.getReunionDate().equals(reunionToUpdate.getReunionDate())
                    ||!request.getEmplacement().equals(reunionToUpdate.getEmplacement())
                    ||!request.getObject().equals(reunionToUpdate.getObject())){

                    reunionToUpdate.setReunionDate(request.getReunionDate());
                    reunionToUpdate.setEmplacement(request.getEmplacement());
                    reunionToUpdate.setObject(request.getObject());
                    sendReunionInvitations(id);

                }

            }else{
                reunionToUpdate.setReunionDate(null);
                reunionToUpdate.setEmplacement(request.getEmplacement());
                reunionToUpdate.setObject(request.getObject());
                reunionToUpdate.setStatus(ReunionStatus.NON_PLANIFIER);
            }


            return save(reunionToUpdate);
        }
        else {
             throw new ReunionStatusException("", reunionToUpdate.getStatus());
        }
    }

    @Override
    @Transactional
    public Reunion updateAnnule(Integer id) throws ReunionNotFound, PpmNotFound, AcNotFound, MessagingException, IOException {
        Reunion reunionToUpdate = repository.findByIdWithParticipantsFetched(id).orElseThrow(()-> new ReunionNotFound("Reunion not found with id: " , id));
       // Reunion reunionToUpdate=findById(id);
        /*reunionToUpdate.setParticipants(new ArrayList<>(reunionToUpdate.getParticipants()));
        reunionToUpdate.setReunionPpms(new ArrayList<>(reunionToUpdate.getReunionPpms()));
        reunionToUpdate.setReunionDacs(new ArrayList<>(reunionToUpdate.getReunionDacs()));*/

        if(reunionToUpdate.getStatus() != ReunionStatus.BROUILLON &&
                reunionToUpdate.getStatus() != ReunionStatus.NON_PLANIFIER &&
                reunionToUpdate.getStatus() != ReunionStatus.PLANIFIER &&
                reunionToUpdate.getStatus()!=ReunionStatus.EN_RETARD){
            throw new ReunionStatusException("",reunionToUpdate.getStatus());
        }
       if(reunionToUpdate.getStatus() == ReunionStatus.PLANIFIER){
           sendReunionAnnulerInvitations(id);
       }
        reunionToUpdate.setStatus(ReunionStatus.ANNULER);
        return save(reunionToUpdate);
    }

    private void createAndSetParticipantsFromUsers(Reunion reunion, ReunionRequest request) {
        List<UserCom> commissionMembers = userComRepository.findAllByComIdAndIsDeletedFalse(request.getComId());
        Set<String> commissionMemberUserIds = commissionMembers.stream().map(UserCom::getUserId).collect(Collectors.toSet());

        Map<String, ParticipantRequest> requestParticipantsMap = request.getParticipants() != null ?
                request.getParticipants().stream()
                        .filter(p -> p.getUserId() != null)
                        .collect(Collectors.toMap(ParticipantRequest::getUserId, Function.identity(), (existing, replacement) -> existing))
                : Collections.emptyMap();

        for (UserCom userCom : commissionMembers) {

            UserDTO userDetails = keycloakAdminService.getUserById(userCom.getUserId());
            ParticipantRequest participantRequest = new ParticipantRequest();
            participantRequest.setReunionId(reunion.getId());
            participantRequest.setNom(userDetails.getFirstName());
            participantRequest.setPrenom(userDetails.getLastName());
            participantRequest.setMail(userDetails.getEmail());
            participantRequest.setUserId(userCom.getUserId());
            ParticipantType type=Objects.equals(reunion.getCom().getResponsableId(), userCom.getUserId()) ? ParticipantType.RESPONSABLE : ParticipantType.MEMBRE;
            participantRequest.setType(type);
            ParticipantFonction fonctionReunion=Objects.equals(reunion.getCom().getResponsableId(), userCom.getUserId()) ? ParticipantFonction.PRESIDENT : ParticipantFonction.MEMBRE;
            String fonction=String.valueOf(fonctionReunion);
            participantRequest.setOrganisme(reunion.getCom().getNom());
            participantRequest.setFonctionReunion(fonctionReunion);
            participantRequest.setFonction(fonction);
            participantRequest.setPresent(false);
            Participant participant= participantService.create(participantRequest);
            reunion.getParticipants().add(participant);

        }

        if (request.getParticipants() != null) {
            for (ParticipantRequest invitedRequest : request.getParticipants()) {
                if (invitedRequest.getUserId() == null || !commissionMemberUserIds.contains(invitedRequest.getUserId())) {
                    invitedRequest.setReunionId(reunion.getId());
                    invitedRequest.setType(ParticipantType.INVITE);
                    invitedRequest.setFonctionReunion(ParticipantFonction.MEMBRE);
                    Participant participant=  participantService.create(invitedRequest);
                    reunion.getParticipants().add(participant);

                }
            }
        }
    }

    @Transactional
    public void synchronizeParticipants(ReunionRequest request, Reunion reunionReq) {
        long requestHasPresident = request.getParticipants().stream()
                .filter(p -> p.getFonctionReunion() == ParticipantFonction.PRESIDENT).count();

        if ( requestHasPresident==0) {
            throw new PresidentFonctionRequiredException("");
        }
        if(requestHasPresident>1){
            throw new MultiplePresidentsNotAllowedException("More than one president");
        }
        Reunion reunion = repository.findByIdWithParticipants(reunionReq.getId())
                .orElseThrow(() -> new ReunionNotFound("Reunion not found with id: " , reunionReq.getId()));
        Map<Integer, Participant> currentParticipantsMap = reunion.getParticipants().stream()
                .collect(Collectors.toMap(Participant::getId, Function.identity()));

        Set<Integer> participantIdsToKeep = new HashSet<>();
        List<ParticipantRequest> participantsInRequest = request.getParticipants() != null ? request.getParticipants() : Collections.emptyList();
        List<Participant> participantsToSave = new ArrayList<>();
    if (reunion.getParticipants() == null) {
        reunion.setParticipants(new ArrayList<>());
    } else if (!(reunion.getParticipants() instanceof ArrayList)) {
        reunion.setParticipants(new ArrayList<>(reunion.getParticipants()));
    }
        for (ParticipantRequest participantRequest : participantsInRequest) {
            if (participantRequest.getId() != null) {
                Participant participantToUpdate = currentParticipantsMap.get(participantRequest.getId());

                if (participantToUpdate != null) {
                    if(reunion.getStatus().equals(ReunionStatus.EN_COURS)){
                    participantToUpdate.setPresent(participantRequest.isPresent());

                    }else{
                        participantToUpdate.setPresent(false);
                    }
                    if(reunion.getStatus().equals(ReunionStatus.BROUILLON)){
                    participantsToSave.add(participantToUpdate);
                    participantIdsToKeep.add(participantToUpdate.getId());
                    if(participantToUpdate.getType().equals(ParticipantType.INVITE)){
                        participantToUpdate.setOrganisme(participantRequest.getOrganisme());
                        participantToUpdate.setFonction(participantRequest.getFonction());
                    }
                    }
                }
            } else {
                if(reunion.getStatus().equals(ReunionStatus.BROUILLON)) {
                    participantRequest.setReunionId(reunion.getId());
                    participantRequest.setType(ParticipantType.INVITE);
                    participantRequest.setFonctionReunion(ParticipantFonction.MEMBRE);
                    Participant newInvitedParticipant = participantService.create(participantRequest);
                    participantsToSave.add(newInvitedParticipant);
                    reunion.getParticipants().add(newInvitedParticipant);
                }
            }
        }

        List<Participant> participantsToDelete = new ArrayList<>();
        for (Participant currentParticipant : currentParticipantsMap.values()) {
            if (!participantIdsToKeep.contains(currentParticipant.getId())) {
                participantsToDelete.add(currentParticipant);
            }
        }

        if (!participantsToDelete.isEmpty()&&reunion.getStatus()==ReunionStatus.BROUILLON) {
            participantsToDelete.forEach(p->p.setPresent(false));
            participantService.softDeleteAll(participantsToDelete);
        }

        if (!participantsToSave.isEmpty()) {
            participantService.saveAll(participantsToSave);
        }
    }

    @Transactional
    public void synchronizePpms(ReunionRequest request, Reunion reunionReq) throws PpmNotFound, AcNotFound, IOException, DocNotFound {
        Reunion reunion = repository.findByIdWithPpms(reunionReq.getId())
                .orElseThrow(() -> new ReunionNotFound("Reunion not found with id: " , reunionReq.getId()));

        // Log fetched Reunion details
        log.info("Fetched Reunion ID: {}", reunion.getId());
        log.info("Number of existing ReunionPpm entries: {}", reunion.getReunionPpms().size());
        log.info("Existing ReunionPpm details: {}", reunion.getReunionPpms().stream()
                .map(link -> String.format("reunionId=%s, ppmId=%s, ppmVer=%s",
                        link.getReunionId(), link.getPpmId(), link.getPpmVer()))
                .collect(Collectors.joining("; ")));

        // Map current links, logging and ignoring invalid entries
        Map<ReunionPpmId, ReunionPpm> currentLinks = new HashMap<>();
        for (ReunionPpm link : reunion.getReunionPpms()) {
            ReunionPpmId id = new ReunionPpmId(link.getReunionId(), link.getPpmId(), link.getPpmVer());
            if (currentLinks.containsKey(id)) {
                log.warn("Ignoring duplicate ReunionPpmId in database: reunionId={}, ppmId={}, ppmVer={}",
                        id.getReunionId(), id.getPpmId(), id.getPpmVer());
                throw new DuplicationData("Duplicate ReunionPpmId");
            }
            currentLinks.put(id, link);
        }

        // Map cached document IDs, logging and ignoring invalid entries
        Map<ReunionPpmId, List<Integer>> cachedDocIds = new HashMap<>();
        for (ReunionPpm link : reunion.getReunionPpms()) {
            ReunionPpmId id = new ReunionPpmId(link.getReunionId(), link.getPpmId(), link.getPpmVer());
            if (cachedDocIds.containsKey(id)) {
                throw new DuplicationData("Duplicate ReunionPpmId found in database: reunionId=" + id.getReunionId() +
                        ", ppmId=" + id.getPpmId() + ", ppmVer=" + id.getPpmVer());
            }
            cachedDocIds.put(id, link.getReunionPpmDocs() != null
                    ? link.getReunionPpmDocs().stream().map(ReunionPpmDoc::getDocId).toList()
                    : new ArrayList<>());
        }

        // Validate request for duplicates
        if (request.getReunionPpms() != null) {
            Map<ReunionPpmId, ReunionPpmRequest> requestPpmMap = new HashMap<>();
            for (ReunionPpmRequest ppmReq : request.getReunionPpms()) {
                if (ppmReq.getId() == null || ppmReq.getVersion() == null) {
                    throw new PpmIdNotFound("",null);
                }
                ReunionPpmId id = new ReunionPpmId(reunion.getId(), ppmReq.getId(), ppmReq.getVersion());
                if (requestPpmMap.containsKey(id)) {
                    throw new DuplicationData("Duplicate PPM in request: reunionId=" + reunion.getId() +
                            ", ppmId=" + ppmReq.getId() + ", ppmVer=" + ppmReq.getVersion());
                }
                requestPpmMap.put(id, ppmReq);
            }

            List<Integer> documentsIdsToDelete = new ArrayList<>();
            Set<ReunionPpmId> requestLinkIds = new HashSet<>();

            for (ReunionPpmRequest ppmReq : request.getReunionPpms()) {
                ReunionPpmId id = new ReunionPpmId(reunion.getId(), ppmReq.getId(), ppmReq.getVersion());
                requestLinkIds.add(id);

                ReunionPpm link = currentLinks.getOrDefault(id, new ReunionPpm());
                boolean isNewLink = link.getReunionId() == null;
                if (isNewLink) {
                    link.setReunionId(reunion.getId());
                    link.setPpmId(ppmReq.getId());
                    link.setPpmVer(ppmReq.getVersion());
                    link.setReunion(reunion);
                    link.setPpm(ppmService.findByIdPpm(ppmReq.getId(), ppmReq.getVersion())
                            .orElseThrow(() -> new PpmNotFound("", List.of(ppmReq.getId()), List.of(ppmReq.getVersion()))));
                }

                // Update fields only if provided
                if (ppmReq.getProcessInstanceId() != null) {
                    link.setProcessInstanceId(ppmReq.getProcessInstanceId());
                }
                if (ppmReq.getTaskId() != null) {
                    link.setTaskId(ppmReq.getTaskId());
                }
                if (reunion.getStatus().equals(ReunionStatus.EN_COURS)) {
                    if (ppmReq.getDecision() != null) {
                        link.setDecision(ppmReq.getDecision());
                    }
                    if (ppmReq.getObservation() != null) {
                        link.setObservation(ppmReq.getObservation());
                    }

                    List<Integer> requestedDocIds = ppmReq.getDocsPpm() != null
                            ? ppmReq.getDocsPpm().stream().filter(el -> !el.getIsDeleted()).map(Docs::getId).toList()
                            : new ArrayList<>();
                    log.info("requestedDocIds: {}", requestedDocIds.size());
                    List<Integer> existingDocIds = cachedDocIds.getOrDefault(id, new ArrayList<>());
                    documentsIdsToDelete.addAll(
                            existingDocIds.stream()
                                    .filter(docId -> !requestedDocIds.contains(docId))
                                    .toList()
                    );
                }


                ReunionPpm savedLink = reunionPpmRepository.save(link);
                if (reunion.getStatus().equals(ReunionStatus.EN_COURS)) {
                    if (ppmReq.getFiles() != null && !ppmReq.getFiles().isEmpty()) {
                        documentCommuneService.processAndSaveDocumentsForReunionPpm(
                                ppmReq.getFiles(),
                                savedLink,
                                Constants.PATH_ENTREPOT_GED_REUNION_PMM
                        );
                    }
                }
                if (reunion.getStatus().equals(ReunionStatus.BROUILLON) && isNewLink) {
                    reunion.getReunionPpms().add(savedLink);
                }
            }
            if (!documentsIdsToDelete.isEmpty()) {
                documentCommuneService.deleteDocuments(documentsIdsToDelete);
            }
        }
    }
   /* public void synchronizePpms(ReunionRequest request, Reunion reunionReq) throws PpmNotFound, AcNotFound, IOException, DocNotFound {


        Reunion reunion = repository.findByIdWithPpms(reunionReq.getId())
                .orElseThrow(() -> new ReunionNotFound("Reunion not found with id: " , reunionReq.getId()));

        // Map current links
        Map<ReunionPpmId, ReunionPpm> currentLinks =
                reunion.getReunionPpms().stream()
                        .collect(Collectors.toMap(
                                link -> new ReunionPpmId(link.getReunionId(),
                                        link.getPpmId(),
                                        link.getPpmVer()),
                                Function.identity()
                        ));
      *//* Map<ReunionPpmId, ReunionPpm> currentLinks = new HashMap<>();
        for (ReunionPpm link : reunion.getReunionPpms()) {
            ReunionPpmId id = new ReunionPpmId(link.getReunionId(), link.getPpmId(), link.getPpmVer());
            currentLinks.put(id, link);
        }*//*

        Map<ReunionPpmId, List<Integer>> cachedDocIds = reunion.getReunionPpms().stream()
                .collect(Collectors.toMap(
                        link -> new ReunionPpmId(link.getReunionId(), link.getPpmId(), link.getPpmVer()),
                        link -> link.getReunionPpmDocs() != null
                                ? link.getReunionPpmDocs().stream().map(ReunionPpmDoc::getDocId).toList()
                                : new ArrayList<>()
                ));

      *//*  Map<ReunionPpmId, List<Integer>> cachedDocIds = reunion.getReunionPpms().stream()
                .collect(Collectors.toMap(
                        link -> new ReunionPpmId(link.getReunionId(), link.getPpmId(), link.getPpmVer()),
                        link -> link.getReunionPpmDocs() != null
                                ? link.getReunionPpmDocs().stream().map(ReunionPpmDoc::getDocId).toList()
                                : new ArrayList<>(),
                        (existing, replacement) -> {
                            List<Integer> merged = new ArrayList<>(existing);
                            merged.addAll(replacement);
                            return merged;
                        }
                ));*//*
        List<Integer> documentsIdsToDelete = new ArrayList<>();
        Set<ReunionPpmId> requestLinkIds = new HashSet<>();
        log.info("Fetched Reunion ID: {}", reunion.getId());
        log.info("Number of existing ReunionPpm entries: {}", reunion.getReunionPpms().size());
        log.info("Existing ReunionPpm details: {}", reunion.getReunionPpms().stream()
                .map(link -> String.format("reunionId=%d, ppmId=%d, ppmVer=%d",
                        link.getReunionId(), link.getPpmId(), link.getPpmVer()))
                .collect(Collectors.joining("; ")));
        if (request.getReunionPpms() != null) {
            for (ReunionPpmRequest ppmReq : request.getReunionPpms()) {
                ReunionPpmId id = new ReunionPpmId(reunion.getId(), ppmReq.getId(), ppmReq.getVersion());
                requestLinkIds.add(id);

                ReunionPpm link = currentLinks.getOrDefault(id, new ReunionPpm());
                link.setReunionId(reunion.getId());
                link.setPpmId(ppmReq.getId());
                link.setPpmVer(ppmReq.getVersion());
                link.setReunion(reunion);
                link.setPpm(ppmService.findByIdPpm(ppmReq.getId(), ppmReq.getVersion()).orElseThrow(()->new PpmNotFound("", List.of(ppmReq.getId()), List.of(ppmReq.getVersion()))));
                link.setProcessInstanceId(ppmReq.getProcessInstanceId());
                link.setTaskId(ppmReq.getTaskId());
                if(reunion.getStatus().equals(ReunionStatus.EN_COURS)){

                        if(ppmReq.getDecision() != null){
                            link.setDecision(ppmReq.getDecision());
                        }
                        if(ppmReq.getObservation()!= null){

                            link.setObservation(ppmReq.getObservation());

                        }
                        //delete the document
                    List<Integer> requestedDocIds = ppmReq.getDocsPpm() != null
                            ? ppmReq.getDocsPpm().stream().filter(el->!el.getIsDeleted()).map(Docs::getId).toList()
                            : new ArrayList<>();
                        log.info("requestedDocIds: {}", requestedDocIds.size());
                    List<Integer> existingDocIds = cachedDocIds.getOrDefault(id, new ArrayList<>());
                    documentsIdsToDelete.addAll(
                            existingDocIds.stream()
                                    .filter(docId -> !requestedDocIds.contains(docId))
                                    .toList()
                    );
                }
                if (!documentsIdsToDelete.isEmpty()) {
                    documentCommuneService.deleteDocuments(documentsIdsToDelete);
                }

               ReunionPpm reunionPpm= reunionPpmRepository.save(link);
                if(reunion.getStatus().equals(ReunionStatus.EN_COURS)) {
                    if (ppmReq.getFiles() != null && !ppmReq.getFiles().isEmpty()) {
                        documentCommuneService.processAndSaveDocumentsForReunionPpm(
                                ppmReq.getFiles(),
                                reunionPpm,
                                Constants.PATH_ENTREPOT_GED_REUNION_PMM
                        );
                    }
                }
                if(reunion.getStatus().equals(ReunionStatus.BROUILLON)) {
                reunion.getReunionPpms().add(link);
                }
            }

        }
    }
*/
    @Transactional

    public void synchronizeDacs(ReunionRequest request, Reunion reunionReq) throws DacNotFound, IOException, DocNotFound {
        Reunion reunion = repository.findByIdWithDacs(reunionReq.getId())
                .orElseThrow(() -> new ReunionNotFound("Reunion not found with id: " , reunionReq.getId()));

        // Log fetched Reunion details
        log.info("Fetched Reunion ID: {}", reunion.getId());
        log.info("Number of existing ReunionDac entries: {}", reunion.getReunionDacs().size());
        log.info("Existing ReunionDac details: {}", reunion.getReunionDacs().stream()
                .map(link -> String.format("reunionId=%s, dacId=%s, dacVer=%s",
                        link.getReunionId(), link.getDacId(), link.getDacVer()))
                .collect(Collectors.joining("; ")));

        // Map current links, logging and ignoring invalid entries
        Map<ReunionDacId, ReunionDac> currentLinks = new HashMap<>();
        for (ReunionDac link : reunion.getReunionDacs()) {
            ReunionDacId id = new ReunionDacId(link.getReunionId(), link.getDacId(), link.getDacVer());
            if (currentLinks.containsKey(id)) {
                throw new DuplicationData("Duplicate ReunionDacId found in database: reunionId=" + id.getReunionId() +
                        ", dacId=" + id.getDacId() + ", dacVer=" + id.getDacVer());
            }
            currentLinks.put(id, link);
        }

        // Map cached document IDs, logging and ignoring invalid entries
        Map<ReunionDacId, List<Integer>> cachedDocIds = new HashMap<>();
        for (ReunionDac link : reunion.getReunionDacs()) {
            ReunionDacId id = new ReunionDacId(link.getReunionId(), link.getDacId(), link.getDacVer());
            if (cachedDocIds.containsKey(id)) {
                throw new DuplicationData("Duplicate ReunionDacId found in database: reunionId=" + id.getReunionId() +
                        ", dacId=" + id.getDacId() + ", dacVer=" + id.getDacVer());
            }
            cachedDocIds.put(id, link.getReunionDacDocs() != null
                    ? link.getReunionDacDocs().stream().map(ReunionDacDoc::getDocId).toList()
                    : new ArrayList<>());
        }

        // Process ReunionDac requests
        if (request.getReunionDacs() != null) {
            // Validate request for duplicates and null fields
            Map<ReunionDacId, ReunionDacRequest> requestDacMap = new HashMap<>();
            for (ReunionDacRequest dacReq : request.getReunionDacs()) {
                if (dacReq.getDacId() == null || dacReq.getDacVer() == null) {
                    throw new DacNotFound("DAC ID or version is null in request:" , null ,
                             null);
                }
                ReunionDacId id = new ReunionDacId(reunion.getId(), dacReq.getDacId(), dacReq.getDacVer());
                if (requestDacMap.containsKey(id)) {
                    throw new DuplicationData("Duplicate DAC in request: reunionId=" + reunion.getId() +
                            ", dacId=" + dacReq.getDacId() + ", dacVer=" + dacReq.getDacVer());
                }
                requestDacMap.put(id, dacReq);
            }

            List<Integer> documentsIdsToDelete = new ArrayList<>();
            Set<ReunionDacId> requestDacIds = new HashSet<>();

            for (ReunionDacRequest dacReq : request.getReunionDacs()) {
                ReunionDacId id = new ReunionDacId(reunion.getId(), dacReq.getDacId(), dacReq.getDacVer());
                requestDacIds.add(id);

                ReunionDac link = currentLinks.getOrDefault(id, new ReunionDac());
                boolean isNewLink = link.getReunionId() == null;
                if (isNewLink) {
                    link.setReunionId(reunion.getId());
                    link.setDacId(dacReq.getDacId());
                    link.setDacVer(dacReq.getDacVer());
                    link.setReunion(reunion);
                    link.setDac(dacService.findById(dacReq.getDacId(), dacReq.getDacVer())
                            .orElseThrow(() -> new DacNotFound("DAC not found with id: " ,dacReq.getDacId() ,
                                     dacReq.getDacVer())));
                }

                // Update fields only if provided
                if (dacReq.getProcessInstanceId() != null) {
                    link.setProcessInstanceId(dacReq.getProcessInstanceId());
                }
                if (dacReq.getTaskId() != null) {
                    link.setTaskId(dacReq.getTaskId());
                }
                if (reunion.getStatus().equals(ReunionStatus.EN_COURS)) {
                    if (dacReq.getDecision() != null) {
                        link.setDecision(dacReq.getDecision());
                    }
                    if (dacReq.getObservation() != null) {
                        link.setObservation(dacReq.getObservation());
                    }

                    List<Integer> requestedDocIds = dacReq.getDocs() != null
                            ? dacReq.getDocs().stream()
                            .filter(doc -> !doc.getIsDeleted())
                            .map(Docs::getId)
                            .toList()
                            : new ArrayList<>();
                    log.info("requestedDocIds: {}", requestedDocIds.size());
                    List<Integer> existingDocIds = cachedDocIds.getOrDefault(id, new ArrayList<>());
                    documentsIdsToDelete.addAll(
                            existingDocIds.stream()
                                    .filter(docId -> !requestedDocIds.contains(docId))
                                    .toList()
                    );
                }

                ReunionDac savedLink = reunionDacRepository.save(link);
                if (reunion.getStatus().equals(ReunionStatus.EN_COURS)) {
                    if (dacReq.getFiles() != null && !dacReq.getFiles().isEmpty()) {
                        documentCommuneService.processAndSaveDocumentsForReunionDac(
                                dacReq.getFiles(),
                                savedLink,
                                Constants.PATH_ENTREPOT_GED_REUNION_DAC
                        );
                    }
                }
                if (reunion.getStatus().equals(ReunionStatus.BROUILLON) && isNewLink) {
                    reunion.getReunionDacs().add(savedLink);
                }
            }

            // Delete documents after processing all DACs
            if (!documentsIdsToDelete.isEmpty()) {
                documentCommuneService.deleteDocuments(documentsIdsToDelete);
            }
        }
    }
    /*public void synchronizeDacs(ReunionRequest request, Reunion reunionReq) throws IOException, DocNotFound {


        Reunion reunion = repository.findByIdWithDacs(reunionReq.getId())
                .orElseThrow(() -> new ReunionNotFound("Reunion not found with id: " , reunionReq.getId()));
        // Map current links
     *//*   Map<ReunionDacId, ReunionDac> currentLinks =
                reunion.getReunionDacs().stream()
                        .collect(Collectors.toMap(
                                link -> new ReunionDacId(link.getReunionId(),
                                        link.getDacId(),
                                        link.getDacVer()),
                                Function.identity()
                        ));*//*

        Set<ReunionDacId> requestDacIds = new HashSet<>();

      *//*  Map<ReunionDacId, List<Integer>> cachedDocIds = reunion.getReunionDacs().stream()
                .collect(Collectors.toMap(
                        link -> new ReunionDacId(link.getReunionId(), link.getDacId(), link.getDacVer()),
                        link -> link.getReunionDacDocs() != null
                                ? link.getReunionDacDocs().stream().map(ReunionDacDoc::getDocId).toList()
                                : new ArrayList<>()
                ));*//*

        Map<ReunionDacId, ReunionDac> currentLinks = new HashMap<>();
        for (ReunionDac link : reunion.getReunionDacs()) {
            ReunionDacId id = new ReunionDacId(link.getReunionId(), link.getDacId(), link.getDacVer());
            currentLinks.put(id, link);
        }


        Map<ReunionDacId, List<Integer>> cachedDocIds = reunion.getReunionDacs().stream()
                .collect(Collectors.toMap(
                        link -> new ReunionDacId(link.getReunionId(), link.getDacId(), link.getDacVer()),
                        link -> link.getReunionDacDocs() != null
                                ? link.getReunionDacDocs().stream().map(ReunionDacDoc::getDocId).toList()
                                : new ArrayList<>(),
                        (existing, replacement) -> {
                            List<Integer> merged = new ArrayList<>(existing);
                            merged.addAll(replacement);
                            return merged;
                        }
                ));

        List<Integer> documentsIdsToDelete = new ArrayList<>();
        if (request.getReunionDacs() != null) {
            for (ReunionDacRequest dacReq : request.getReunionDacs()) {

                ReunionDacId id = new ReunionDacId(reunion.getId(), dacReq.getDacId(), dacReq.getDacVer());
                requestDacIds.add(id);

                ReunionDac link = currentLinks.getOrDefault(id, new ReunionDac());
                link.setReunionId(reunion.getId());
                link.setDacId(dacReq.getDacId());
                link.setDacVer(dacReq.getDacVer());
                link.setReunion(reunion);

                link.setDac(dacService.findById(dacReq.getDacId(), dacReq.getDacVer()).orElseThrow(()->new RuntimeException("DAC not found")));
                link.setProcessInstanceId(dacReq.getProcessInstanceId());
                link.setTaskId(dacReq.getTaskId());
                if(reunion.getStatus().equals(ReunionStatus.EN_COURS)) {
                    if(dacReq.getDecision() != null){
                        link.setDecision(dacReq.getDecision());
                    }
                    if(dacReq.getObservation() != null){
                        link.setObservation(dacReq.getObservation());
                    }
                    //delete the document
                    List<Integer> requestedDocIds = dacReq.getDocs() != null
                            ? dacReq.getDocs().stream().map(Docs::getId).toList()
                            : new ArrayList<>();
                    List<Integer> existingDocIds = cachedDocIds.getOrDefault(id, new ArrayList<>());
                    documentsIdsToDelete.addAll(
                            existingDocIds.stream()
                                    .filter(docId -> !requestedDocIds.contains(docId))
                                    .toList()
                    );

                    if (!documentsIdsToDelete.isEmpty()) {
                        documentCommuneService.deleteDocuments(documentsIdsToDelete);
                    }
                }

               ReunionDac reunionDac= reunionDacRepository.save(link);
                if(reunion.getStatus().equals(ReunionStatus.EN_COURS)) {

                    if (dacReq.getFiles() != null && !dacReq.getFiles().isEmpty()) {
                        documentCommuneService.processAndSaveDocumentsForReunionDac(
                                dacReq.getFiles(),
                                reunionDac,
                                Constants.PATH_ENTREPOT_GED_REUNION_DAC
                        );
                    }
                }
                if(reunion.getStatus().equals(ReunionStatus.BROUILLON)) {

                    reunion.getReunionDacs().add(link);
                    }
            }

        }

    }*/


    @Override
    public List<Reunion> findAll() {
        return repository.findAll(ReunionSpecifications.deleted(false));
    }

    @Override
    public void delete(Integer id) throws ReunionNotFound {
        Reunion reunion= findById(id);
         if(reunion.getStatus()!=ReunionStatus.BROUILLON){
             throw new ReunionStatusException("",reunion.getStatus());
         }
        repository.softDelete(id);
    }


    public void sendReunionInvitations(Integer id) throws MessagingException, IOException {
        // Get the current Admin ID
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        String connectedUserId = currentUserId.orElse("System");

        // Fetch the Reunion entity by ID
        Reunion reunion = repository.findByIdWithParticipantsFetched(id).orElseThrow(()-> new ReunionNotFound("Reunion not found with id: " , id));

        // Iterate over participants and send emails
        for (Participant participant : reunion.getParticipants()) {

           if(participant.getMail()!=null){
               mailService.sendReunionInvitationEmail(reunion, participant, connectedUserId);
           }
        }
    }

   @Scheduled(cron = "* * 1 * * ?")
   @Transactional
    public void updateReunionStatusToEnRetard() {
        Calendar now = Calendar.getInstance();
        List<Reunion> reunions = repository.findAllByReunionDateBeforeAndStatus(now, ReunionStatus.PLANIFIER);
        reunions.forEach(reunion -> {
            log.info("Updating reunion {} to status EN_RETARD", reunion.getId());
            reunion.setStatus(ReunionStatus.EN_RETARD);
            repository.save(reunion);

        });
    }

    @Override
    public Reunion startReunion(Integer id) throws ReunionNotFound {
        Reunion reunion = repository.findByIdWithParticipantsFetched(id).orElseThrow(()-> new ReunionNotFound("Reunion not found with id: " , id));
        reunion.setParticipants(new ArrayList<>(reunion.getParticipants()));
        reunion.setReunionPpms(new ArrayList<>(reunion.getReunionPpms()));
        reunion.setReunionDacs(new ArrayList<>(reunion.getReunionDacs()));
        if(reunion.getStatus().equals(ReunionStatus.EN_RETARD)||reunion.getStatus().equals(ReunionStatus.PLANIFIER)){
            reunion.setStatus(ReunionStatus.EN_COURS);
            repository.save(reunion);
            return reunion;
        }
        else {
            throw new ReunionStatusException("",reunion.getStatus());
        }
    }

    @Override
    public Reunion updateReunionAfterStart(Integer id, ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound {
        boolean check=checkParticipantPresence(request.getParticipants());
        if(!check){
            throw new ParticipantPresenceConflict("");
        }
        Reunion reunionToUpdate=findById(id);

        if(reunionToUpdate.getStatus() == ReunionStatus.EN_COURS) {
          //  ReunionMapper.INSTANCE.updateFromRequest(request,reunionToUpdate);
            synchronizeParticipants(request, reunionToUpdate);

            synchronizePpms(request, reunionToUpdate);
            synchronizeDacs(request, reunionToUpdate);
            if(reunionToUpdate.getProcessVerbal()!=null&&request.getProcessVerbal()!=null){
            processVerbalReunionService.update(reunionToUpdate.getProcessVerbal().getId(),request.getProcessVerbal());
            }
            else {
                ProcessVerbalReunionRequest processVerbalReunionRequest = new ProcessVerbalReunionRequest();
                ProcessVerbalReunion processVerbalReunion =processVerbalReunionService.create(processVerbalReunionRequest);
                reunionToUpdate.setProcessVerbal(processVerbalReunion);
            }
            updateParticipantsFonction(request, reunionToUpdate);
            return save(reunionToUpdate);
        }
        else {
            throw new ReunionStatusException("", reunionToUpdate.getStatus());
        }
    }

    @Override
    public Reunion validateReunion(Integer id, ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound, IOException, DocNotFound {
        boolean check=checkParticipantPresence(request.getParticipants());
        if(!check){
            throw new ParticipantPresenceConflict("");
        }
        Reunion reunionToUpdate=findById(id);

        if(reunionToUpdate.getStatus() == ReunionStatus.EN_COURS) {
            synchronizeParticipants(request, reunionToUpdate);
            synchronizePpms(request, reunionToUpdate);
            synchronizeDacs(request, reunionToUpdate);
            reunionToUpdate.setStatus(ReunionStatus.VALIDER);
            if(reunionToUpdate.getProcessVerbal()!=null&&request.getProcessVerbal()!=null){
                processVerbalReunionService.update(reunionToUpdate.getProcessVerbal().getId(),request.getProcessVerbal());
            }
            else {
                ProcessVerbalReunionRequest processVerbalReunionRequest = new ProcessVerbalReunionRequest();
                ProcessVerbalReunion processVerbalReunion =processVerbalReunionService.create(processVerbalReunionRequest);
                reunionToUpdate.setProcessVerbal(processVerbalReunion);
            }
            if(reunionToUpdate.getStatus().equals(ReunionStatus.EN_COURS)){
                updateParticipantsFonction(request, reunionToUpdate);
            }
            return save(reunionToUpdate);
        }
        else {
            throw new ReunionStatusException("", reunionToUpdate.getStatus());
        }
    }

    @Override
    public ProcessVerbalReunion injectPv(Integer reunionId, MultipartFile file) throws ReunionNotFound {
        Reunion reunionToUpdate=findById(reunionId);
        /*reunionToUpdate.setParticipants(new ArrayList<>(reunionToUpdate.getParticipants()));
        reunionToUpdate.setReunionPpms(new ArrayList<>(reunionToUpdate.getReunionPpms()));
        reunionToUpdate.setReunionDacs(new ArrayList<>(reunionToUpdate.getReunionDacs()));*/
        if(reunionToUpdate.getStatus() == ReunionStatus.VALIDER &&file!=null) {
         ProcessVerbalReunion pv= processVerbalReunionService.injectPvForReunion(reunionToUpdate.getProcessVerbal().getId(),file);
        reunionToUpdate.setStatus(ReunionStatus.CLOTURER);
        save(reunionToUpdate);
            return pv;
        }
        else {
            throw new ReunionStatusException("", reunionToUpdate.getStatus());
        }
    }

    @Override
    public boolean checkParticipantPresenceWithReunionId(Integer id) throws ReunionNotFound {
        Optional <Reunion> reunionToUpdate=repository.findByIdWithParticipantsFetched(id);
        if(reunionToUpdate.isPresent()){
            List<Participant> participants=reunionToUpdate.get().getParticipants().stream().filter(Participant::isPresent).collect(Collectors.toList());
            return !participants.isEmpty();
        }
        else {
            throw new ReunionNotFound("",id);
        }
    }

    @Override
    public List<Reunion> findAllByPpmIdListForAdminCentralAndSuperAdmin(Integer ppmId) throws PpmNotFound, AcNotFound {

        Specification<Reunion> spec= ReunionSpecifications.hasPpmId(ppmId).and(ReunionSpecifications.deleted(false)).and(ReunionSpecifications.status(ReunionStatus.CLOTURER));
        return repository.findAll(spec);
    }

    @Override
    public List<Reunion> findAllByPpmIdListForUserCom(Integer ppmId, String userId) throws PpmNotFound, AcNotFound {

        List<Integer> comIds=userComRepository.findAllByUserIdAndIsDeletedFalse(userId).stream().map(UserCom::getComId).collect(Collectors.toList());

        Specification<Reunion> spec= ReunionSpecifications.hasPpmId(ppmId).and(ReunionSpecifications.deleted(false)).and(ReunionSpecifications.status(ReunionStatus.CLOTURER));
        return repository.findAll(spec)
                .stream()
                .filter(reunion ->
                        comIds.contains(reunion.getCom().getId()))
                .collect(Collectors.toList());
    }

    /*this methode return dto because i will build some data from different services*/
    @Override
    public ReunionWithParticipantsAndPvDto particpantHistoryByReunionIdAndPPmIdForAdminCentralAndSuperAdmin(Integer reunionId, Integer ppmId) throws PpmNotFound, AcNotFound {

        Specification<Reunion> spec= ReunionSpecifications.hasPpmId(ppmId).and(ReunionSpecifications.deleted(false)).and(ReunionSpecifications.id(reunionId)).and(ReunionSpecifications.status(ReunionStatus.CLOTURER));
       Reunion reunion= repository.findOne(spec).orElseThrow(()->new ReunionNotFound("",reunionId));
        ReunionWithParticipantsAndPvDto dto=  ReunionMapper.INSTANCE.mapToReunionWithParticipantsAndPvDto(reunion);
        if (reunion.getProcessVerbal() != null && dto.getProcessVerbal() != null) {
            DocsResponseDTO pvDocDto = mapPvmDocs(reunion.getProcessVerbal().getDocuments());

            dto.getProcessVerbal().setDocument(
                    pvDocDto != null
                            ? new ProcessVerbalReunionDocResponse(
                            dto.getProcessVerbal().getId(),
                            pvDocDto.getId(),
                            pvDocDto,
                            true
                    )
                            : null
            );
        }
        UserDTO createdUser=keycloakAdminService.getUserById(reunion.getCreatedBy());
        UserDTO updatedUser=keycloakAdminService.getUserById(reunion.getUpdatedBy());
        dto.setCreatedBy(createdUser.getFirstName()+" "+createdUser.getLastName());
        dto.setUpdatedBy(updatedUser.getFirstName()+" "+updatedUser.getLastName());
        return dto;
    }

    @Override
    public ReunionWithParticipantsAndPvDto particpantHistoryByReunionIdAndPPmIdForUserCom(Integer reunionId, Integer ppmId,String userId) throws PpmNotFound, AcNotFound {
        List<Integer> comIds=userComRepository.findAllByUserIdAndIsDeletedFalse(userId).stream().map(UserCom::getComId).collect(Collectors.toList());

        Specification<Reunion> spec= ReunionSpecifications.hasPpmId(ppmId).and(ReunionSpecifications.deleted(false)).and(ReunionSpecifications.id(reunionId)).and(ReunionSpecifications.status(ReunionStatus.CLOTURER));
        Reunion reunion= repository.findOne(spec).orElseThrow(()->new ReunionNotFound("",reunionId));
        if(!comIds.contains(reunion.getCom().getId())) {
            throw new AuthorizationDeniedException("");
        }
        ReunionWithParticipantsAndPvDto dto=  ReunionMapper.INSTANCE.mapToReunionWithParticipantsAndPvDto(reunion);
        if (reunion.getProcessVerbal() != null && dto.getProcessVerbal() != null) {
            DocsResponseDTO pvDocDto = mapPvmDocs(reunion.getProcessVerbal().getDocuments());

            dto.getProcessVerbal().setDocument(
                    pvDocDto != null
                            ? new ProcessVerbalReunionDocResponse(
                            dto.getProcessVerbal().getId(),
                            pvDocDto.getId(),
                            pvDocDto,
                            true
                    )
                            : null
            );
        }
        UserDTO createdUser=keycloakAdminService.getUserById(reunion.getCreatedBy());
        UserDTO updatedUser=keycloakAdminService.getUserById(reunion.getUpdatedBy());
        dto.setCreatedBy(createdUser.getFirstName()+" "+createdUser.getLastName());
        dto.setUpdatedBy(updatedUser.getFirstName()+" "+updatedUser.getLastName());
        return dto;
    }

    private Specification<Reunion> reunionSpecificationRoleAndComIds (Specification<Reunion> spec){
        boolean superAdmin= Authentication.hasRole("SUPER_ADMIN");
        if(!superAdmin){
            List<Integer> comIds= currentUserService.getCurrentUserComIds();
            return  Specification.where(spec).and(ReunionSpecifications.deleted(false)).and(ReunionSpecifications.comIdSpec(comIds));
        }
        else{
            return  Specification.where(spec).and(ReunionSpecifications.deleted(false));
        }
    }
    public void sendReunionAnnulerInvitations(Integer id) throws MessagingException, IOException {
        // Get the current Admin ID
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        String connectedUserId = currentUserId.orElse("System");

        // Fetch the Reunion entity by ID
        Reunion reunion = repository.findByIdWithParticipantsFetched(id).orElseThrow(()-> new ReunionNotFound("Reunion not found with id: " , id));

        // Iterate over participants and send emails
        for (Participant participant : reunion.getParticipants()) {

            if(participant.getMail()!=null){
                mailService.sendReunionAnnulerEmail(reunion, participant, connectedUserId);
            }
        }
    }
    private boolean checkParticipantPresence(List<ParticipantRequest> participantRequests) {
        List<ParticipantRequest> Participants= participantRequests.stream().filter(ParticipantRequest::isPresent).collect(Collectors.toList());
        return !Participants.isEmpty();

    }
    private void updateParticipantsFonction(ReunionRequest request, Reunion reunion) {
        for (Participant participant : reunion.getParticipants()) {
            for (ParticipantRequest participantRequest : request.getParticipants()) {
                if((participant.getType().equals(ParticipantType.INVITE)||
                        participant.getType().equals(ParticipantType.MEMBRE))&&
                        participantRequest.getFonctionReunion().equals(ParticipantFonction.PRESIDENT)&&
                        !participantRequest.isPresent()) {
                    throw new ParticipantPresidentPresenceRequired("");
                }
                if (participant.getId().equals(participantRequest.getId())) {
                    if(participant.getType().equals(ParticipantType.RESPONSABLE)){
                        if(participantRequest.isPresent()){
                        participant.setFonctionReunion(ParticipantFonction.PRESIDENT);
                        }
                        else{
                            participant.setFonctionReunion(ParticipantFonction.MEMBRE);

                        }
                    }
                    else {
                        participant.setFonctionReunion(participantRequest.getFonctionReunion());
                    }

                    participantService.save(participant);
                    break;
                }
            }
        }
    }

    private DocsResponseDTO mapPvmDocs(List<ProcessVerbalReunionDoc> pvDocs) {
        if (pvDocs == null) return null;

        return pvDocs.stream()
                .filter(pvDoc -> pvDoc.isPv() && pvDoc.getDoc() != null && !pvDoc.getDoc().getIsDeleted())
                .map(ProcessVerbalReunionDoc::getDoc)
                .map(DocMapper.INSTANCE::mapToDto)
                .findFirst()
                .orElse(null);
    }
}
