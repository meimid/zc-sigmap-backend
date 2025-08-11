package com.sigmap.common.reunion;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.Participant.ParticipantId;
import com.sigmap.common.Participant.ParticipantService;
import com.sigmap.common.Participant.exception.ParticipantNotFound;
import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.Participant.request.ParticipantRequest;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComService;
import com.sigmap.common.com.ComSpecifications;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.ParticipantType;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dac.exeption.DacWithRealisation;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunion.mapper.ReunionMapper;
import com.sigmap.common.reunion.request.ReunionRequest;
import com.sigmap.common.reunionDac.ReunionDac;
import com.sigmap.common.reunionDac.ReunionDacId;
import com.sigmap.common.reunionDac.ReunionDacRepository;
import com.sigmap.common.reunionDac.request.ReunionDacRequest;
import com.sigmap.common.reunionPpm.ReunionPpm;
import com.sigmap.common.reunionPpm.ReunionPpmId;
import com.sigmap.common.reunionPpm.ReunionPpmRepository;
import com.sigmap.common.reunionPpm.request.ReunionPpmRequest;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.UserComRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
    @Autowired
    EntityManager entityManager;
    public ReunionServiceImpl(ReunionRepository repository, ComService comService, UserComRepository userComRepository, ParticipantService participantService, KeycloakAdminService keycloakAdminService, PpmService ppmService, DacService dacService, ReunionPpmRepository reunionPpmRepository, ReunionDacRepository reunionDacRepository) {
        this.repository = repository;
        this.comService = comService;
        this.userComRepository = userComRepository;
        this.participantService = participantService;
        this.keycloakAdminService = keycloakAdminService;
        this.ppmService = ppmService;
        this.dacService = dacService;
        this.reunionPpmRepository = reunionPpmRepository;
        this.reunionDacRepository = reunionDacRepository;
    }
    @Override
    public Page<Reunion> findAll(Specification<Reunion> spec, Pageable pageable) {
        Specification<Reunion> finalSpec = Specification.where(spec).and(ReunionSpecifications.deleted(false));
        return repository.findAll(finalSpec, pageable);
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
        return repository.findOne(spec);
    }

    @Override
    public Reunion findById(Integer id) throws ReunionNotFound {
        Specification<Reunion> specification =
                ReunionSpecifications.id(id).and(ReunionSpecifications.deleted(false));
        Reunion reunion= findOne(specification)
                .orElseThrow(() -> new ReunionNotFound("Reunion not found with id: " , id));
        reunion.setParticipants(  reunion.getParticipants().stream().filter(participant -> !participant.getIsDeleted()).toList());
       return reunion;
    }

    @Override
    public Reunion save(Reunion reunion) {
        return repository.save(reunion);
    }
    @Override
    @Transactional
    public Reunion create(ReunionRequest request) throws PpmNotFound, AcNotFound, ComNotFound {
        Com com = comService.findById(request.getComId());
        Reunion reunion = ReunionMapper.INSTANCE.map(request);
        reunion.setCom(com);
        reunion.setStatus(ReunionStatus.BROUILLON);
        Reunion savedReunion = repository.save(reunion);
        createAndSetParticipantsFromUsers(savedReunion, request);
        synchronizePpms(request, savedReunion);
        synchronizeDacs(request, savedReunion);
        log.info("-------------- Reunion created -------------- {} ",savedReunion);
        return reunion;
    }

    @Override
    @Transactional
    public Reunion update(Integer id, ReunionRequest request,ReunionStatus status) throws ReunionNotFound, PpmNotFound, AcNotFound {
        Reunion reunionToUpdate = repository.findByIdWithParticipantsFetched(id).orElseThrow(()-> new ReunionNotFound("Reunion not found with id: " , id));
        reunionToUpdate.getReunionDacs().size();
        reunionToUpdate.getReunionPpms().size();
        ReunionMapper.INSTANCE.updateFromRequest(request,reunionToUpdate);
        reunionToUpdate.setStatus(status);
        synchronizeParticipants(request, reunionToUpdate);
        synchronizePpms(request, reunionToUpdate);
        synchronizeDacs(request, reunionToUpdate);
        return save(reunionToUpdate);
    }
    @Override
    @Transactional
    public Reunion updatePlanifierOrNot(Integer id, ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound {
        Reunion reunionToUpdate = repository.findByIdWithParticipantsFetched(id).orElseThrow(()-> new ReunionNotFound("Reunion not found with id: " , id));
        reunionToUpdate.getReunionDacs().size();
        reunionToUpdate.getReunionPpms().size();
        ReunionMapper.INSTANCE.updateFromRequest(request,reunionToUpdate);
        if(request.getReunionDate()!=null){
            reunionToUpdate.setStatus(ReunionStatus.PLANIFIER);
        }else{
            reunionToUpdate.setStatus(ReunionStatus.NON_PLANIFIER);
        }

        synchronizeParticipants(request, reunionToUpdate);
        synchronizePpms(request, reunionToUpdate);
        synchronizeDacs(request, reunionToUpdate);
        return save(reunionToUpdate);
    }

    @Override
    @Transactional
    public Reunion updateAnnule(Integer id) throws ReunionNotFound, PpmNotFound, AcNotFound {
        Reunion reunionToUpdate = repository.findByIdWithParticipantsFetched(id).orElseThrow(()-> new ReunionNotFound("Reunion not found with id: " , id));
        reunionToUpdate.getReunionDacs().size();
        reunionToUpdate.getReunionPpms().size();
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
            participantRequest.setType(Objects.equals(reunion.getCom().getResponsableId(), userCom.getUserId()) ? ParticipantType.RESPONSABLE : ParticipantType.MEMBRE);
          ParticipantRequest requestData = requestParticipantsMap.get(userCom.getUserId());
            if (requestData != null) {
                participantRequest.setPresent(requestData.isPresent());
                participantRequest.setOrganisme(requestData.getOrganisme());
                participantRequest.setFonction(requestData.getFonction());
            } else {
                participantRequest.setPresent(false);
            }
            Participant participant= participantService.create(participantRequest);
            reunion.getParticipants().add(participant);

        }

        if (request.getParticipants() != null) {
            for (ParticipantRequest invitedRequest : request.getParticipants()) {
                if (invitedRequest.getUserId() == null || !commissionMemberUserIds.contains(invitedRequest.getUserId())) {
                    invitedRequest.setReunionId(reunion.getId());
                    invitedRequest.setType(ParticipantType.INVITE);
                    Participant participant=  participantService.create(invitedRequest);
                    reunion.getParticipants().add(participant);

                }
            }
        }
    }

    private void synchronizeParticipants(ReunionRequest request, Reunion reunion) {
        Map<Integer, Participant> currentParticipantsMap = reunion.getParticipants().stream()
                .collect(Collectors.toMap(Participant::getId, Function.identity()));

        Set<Integer> participantIdsToKeep = new HashSet<>();
        List<ParticipantRequest> participantsInRequest = request.getParticipants() != null ? request.getParticipants() : Collections.emptyList();
        List<Participant> participantsToSave = new ArrayList<>();

        for (ParticipantRequest participantRequest : participantsInRequest) {
            if (participantRequest.getId() != null) {
                Participant participantToUpdate = currentParticipantsMap.get(participantRequest.getId());
                if (participantToUpdate != null) {
                    participantToUpdate.setPresent(participantRequest.isPresent());
                    participantToUpdate.setOrganisme(participantRequest.getOrganisme());
                    participantToUpdate.setFonction(participantRequest.getFonction());
                    participantsToSave.add(participantToUpdate);
                    participantIdsToKeep.add(participantToUpdate.getId());
                }
            } else {
                participantRequest.setReunionId(reunion.getId());
                participantRequest.setType(ParticipantType.INVITE);
                Participant newInvitedParticipant = participantService.create(participantRequest);
                participantsToSave.add(newInvitedParticipant);
                reunion.getParticipants().add(newInvitedParticipant);
            }
        }

        List<Participant> participantsToDelete = new ArrayList<>();
        for (Participant currentParticipant : currentParticipantsMap.values()) {
            if (!participantIdsToKeep.contains(currentParticipant.getId())) {
                participantsToDelete.add(currentParticipant);
            }
        }

        if (!participantsToDelete.isEmpty()) {
            participantsToDelete.forEach(p->p.setPresent(false));
            participantService.softDeleteAll(participantsToDelete);
        }

        if (!participantsToSave.isEmpty()) {
            participantService.saveAll(participantsToSave);
        }
    }

    private void synchronizePpms(ReunionRequest request, Reunion reunion) throws PpmNotFound, AcNotFound {
        Map<ReunionPpmId, ReunionPpm> currentLinks = reunion.getReunionPpms().stream()
                .collect(Collectors.toMap(link -> new ReunionPpmId(link.getReunionId(), link.getPpmId(), link.getPpmVer()), Function.identity()));

        Set<ReunionPpmId> requestLinkIds = new HashSet<>();

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
                reunionPpmRepository.save(link);
                reunion.getReunionPpms().add(link);
            }

        }

   /*    for (ReunionPpm currentLink : currentLinks.values()) {
            ReunionPpmId currentId = new ReunionPpmId(currentLink.getReunionId(), currentLink.getPpmId(), currentLink.getPpmVer());
            if (!requestLinkIds.contains(currentId)) {
                reunionPpmRepository.delete(currentLink);
            }
        }*/
    }

    private void synchronizeDacs(ReunionRequest request, Reunion reunion) {
        Map<ReunionDacId, ReunionDac> currentLinks = reunion.getReunionDacs().stream()
                .collect(Collectors.toMap(link -> new ReunionDacId(link.getReunionId(), link.getDacId(), link.getDacVer()), Function.identity()));

        Set<ReunionDacId> requestDacIds = new HashSet<>();

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
                reunionDacRepository.save(link);
                reunion.getReunionDacs().add(link);
            }

        }

      /*  for (ReunionDac currentLink : currentLinks.values()) {
            ReunionDacId currentId = new ReunionDacId(currentLink.getReunionId(), currentLink.getDacId(), currentLink.getDacVer());
            if (!requestDacIds.contains(currentId)) {
                reunionDacRepository.delete(currentLink);
            }
        }*/
    }
    @Override
    public List<Reunion> findAll() {
        return repository.findAll(ReunionSpecifications.deleted(false));
    }

    @Override
    public void delete(Integer id) throws ReunionNotFound {
         findById(id);
        repository.softDelete(id);
    }
  /*  private void synchronizePpms(ReunionRequest request, Reunion reunion) throws PpmNotFound, AcNotFound {
        // Step 1: Fetch the current children directly from the database. This is safe.
        List<ReunionPpm> currentLinksList = reunionPpmRepository.findByReunionId(reunion.getId());
        Map<ReunionPpmId, ReunionPpm> currentLinksMap = currentLinksList.stream()
                .collect(Collectors.toMap(link -> new ReunionPpmId(link.getReunionId(), link.getPpmId(), link.getPpmVer()), Function.identity()));

        Set<ReunionPpmId> requestLinkIds = new HashSet<>();
        List<ReunionPpm> linksToSave = new ArrayList<>();

        // Step 2: Loop through the request to find new or updated links.
        if (request.getReunionPpms() != null) {
            for (ReunionPpmRequest ppmReq : request.getReunionPpms()) {
                ReunionPpmId id = new ReunionPpmId(reunion.getId(), ppmReq.getId(), ppmReq.getVersion());
                requestLinkIds.add(id);

                // Get the existing link from our map, or create a new one if it doesn't exist.
                ReunionPpm link = currentLinksMap.getOrDefault(id, new ReunionPpm());

                // Update all fields
                link.setReunion(reunion);
                link.setPpmId(ppmReq.getId());
                link.setPpmVer(ppmReq.getVersion());
                link.setPpm(ppmService.findByIdPpm(ppmReq.getId(), ppmReq.getVersion()).orElseThrow(() -> new PpmNotFound("", List.of(ppmReq.getId()), List.of(ppmReq.getVersion()))));
                link.setProcessInstanceId(ppmReq.getProcessInstanceId());
                link.setTaskId(ppmReq.getTaskId());

                linksToSave.add(link);
            }
        }

        // Step 3: Save all new and updated links.
        reunionPpmRepository.saveAll(linksToSave);

        // Step 4: Find and delete any links that were not in the request.
        List<ReunionPpm> linksToDelete = new ArrayList<>();
        for (ReunionPpm currentLink : currentLinksList) {
            ReunionPpmId currentId = new ReunionPpmId(currentLink.getReunionId(), currentLink.getPpmId(), currentLink.getPpmVer());
            if (!requestLinkIds.contains(currentId)) {
                linksToDelete.add(currentLink);
            }
        }

        if (!linksToDelete.isEmpty()) {
            reunionPpmRepository.deleteAll(linksToDelete);
        }
    }
    private void synchronizeDacs(ReunionRequest request, Reunion reunion) {
        // Step 1: Fetch the current children directly from the database.
        List<ReunionDac> currentLinksList = reunionDacRepository.findByReunionId(reunion.getId());
        Map<ReunionDacId, ReunionDac> currentLinksMap = currentLinksList.stream()
                .collect(Collectors.toMap(link -> new ReunionDacId(link.getReunionId(), link.getDacId(), link.getDacVer()), Function.identity()));

        Set<ReunionDacId> requestDacIds = new HashSet<>();
        List<ReunionDac> linksToSave = new ArrayList<>();

        // Step 2: Loop through the request to find new or updated links.
        if (request.getReunionDacs() != null) {
            for (ReunionDacRequest dacReq : request.getReunionDacs()) {
                ReunionDacId id = new ReunionDacId(reunion.getId(), dacReq.getDacId(), dacReq.getDacVer());
                requestDacIds.add(id);

                ReunionDac link = currentLinksMap.getOrDefault(id, new ReunionDac());

                // Update all fields
                link.setReunion(reunion);
                link.setDacId(dacReq.getDacId());
                link.setDacVer(dacReq.getDacVer());
                link.setDac(dacService.findById(dacReq.getDacId(), dacReq.getDacVer()).orElseThrow(() -> new RuntimeException("DAC not found")));
                link.setProcessInstanceId(dacReq.getProcessInstanceId());
                link.setTaskId(dacReq.getTaskId());

                linksToSave.add(link);
            }
        }

        // Step 3: Save all new and updated links.
        reunionDacRepository.saveAll(linksToSave);

        // Step 4: Find and delete any links that were not in the request.
        List<ReunionDac> linksToDelete = new ArrayList<>();
        for (ReunionDac currentLink : currentLinksList) {
            ReunionDacId currentId = new ReunionDacId(currentLink.getReunionId(), currentLink.getDacId(), currentLink.getDacVer());
            if (!requestDacIds.contains(currentId)) {
                linksToDelete.add(currentLink);
            }
        }

        if (!linksToDelete.isEmpty()) {
            reunionDacRepository.deleteAll(linksToDelete);
        }
    }
    private void synchronizeParticipants(ReunionRequest request, Reunion reunion) {
        // Step 1: Fetch current participants directly from the database.
        List<Participant> currentParticipants = participantService.findByReunionId(reunion.getId());
        Map<Integer, Participant> currentParticipantsMap = currentParticipants.stream()
                .collect(Collectors.toMap(Participant::getId, Function.identity()));

        Set<Integer> participantIdsToKeep = new HashSet<>();
        List<Participant> participantsToSave = new ArrayList<>();

        // The rest of your logic is mostly correct, we just need to use our fetched list.
        List<ParticipantRequest> participantsInRequest = request.getParticipants() != null ? request.getParticipants() : Collections.emptyList();

        for (ParticipantRequest participantRequest : participantsInRequest) {
            if (participantRequest.getId() != null) {
                Participant participantToUpdate = currentParticipantsMap.get(participantRequest.getId());
                if (participantToUpdate != null) {
                    // Update existing participant
                    participantToUpdate.setPresent(participantRequest.isPresent());
                    participantToUpdate.setOrganisme(participantRequest.getOrganisme());
                    participantToUpdate.setFonction(participantRequest.getFonction());
                    participantsToSave.add(participantToUpdate);
                    participantIdsToKeep.add(participantToUpdate.getId());
                }
            } else {
                // Create new participant
                participantRequest.setReunionId(reunion.getId());
                participantRequest.setType(ParticipantType.INVITE);
                participantsToSave.add(participantService.create(participantRequest));
            }
        }

        // Determine which participants to delete
        List<Participant> participantsToDelete = new ArrayList<>();
        for (Participant currentParticipant : currentParticipants) {
            if (!participantIdsToKeep.contains(currentParticipant.getId())) {
                participantsToDelete.add(currentParticipant);
            }
        }

        if (!participantsToDelete.isEmpty()) {
            participantService.softDeleteAll(participantsToDelete);
        }

        if (!participantsToSave.isEmpty()) {
            participantService.saveAll(participantsToSave);
        }
    }*/
}
