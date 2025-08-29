package com.sigmap.common.Participant;

import com.sigmap.common.Participant.exception.ParticipantNotFound;
import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.Participant.request.ParticipantInvitedRequest;
import com.sigmap.common.Participant.request.ParticipantRequest;
import com.sigmap.common.config.ParticipantType;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionRepository;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunion.exception.ReunionStatusException;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.UserComRepository;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final UserComRepository userComRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final ReunionRepository reunionRepository;
    private final MailService mailService;


    public ParticipantServiceImpl(ParticipantRepository repository, SequenceGeneratorService sequenceGeneratorService, UserComRepository userComRepository, KeycloakAdminService keycloakAdminService, ReunionRepository reunionRepository, MailService mailService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.userComRepository = userComRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.reunionRepository = reunionRepository;
        this.mailService = mailService;
    }

    @Override
    public List<Participant> findAll() {
        return repository.findAll(ParticipantSpecifications.deleted(false));
    }

    @Override
    public Page<Participant> findAll(Specification<Participant> spec, Pageable pageable) {
        Specification<Participant> finalSpec = Specification.where(spec).and(ParticipantSpecifications.deleted(false));
        return repository.findAll(finalSpec, pageable);
    }

    @Override
    public List<Participant> findAllList(Specification<Participant> spec) {
        Specification<Participant> finalSpec = Specification.where(spec).and(ParticipantSpecifications.deleted(false));
        return repository.findAll(finalSpec);    }

    @Override
    public Optional<Participant> findOne(Specification<Participant> spec) {
        return repository.findOne(spec);
    }

    @Override
    public Optional<Participant> findOneByUserIdAndReunionId(String userId, Integer reunionId) {
        return repository.findParticipantByUserIdAndReunionId(userId,reunionId);
    }

    @Override
    public void softDeleteAll(List<Participant> members) {
        repository.softDeleteAll(members);
    }

    @Override
    public List<Participant> saveAll(List<Participant> members) {
        return repository.saveAll(members);
    }

    @Override
    public List<Participant> findByReunionId(Integer reunionId) {
       return repository.findByReunionIdAndIsDeletedFalse(reunionId);
    }

    @Override
    public Participant findById(Integer id, Integer reunionId) throws ParticipantNotFound {
        ParticipantId compositeId = new ParticipantId();
        compositeId.setId(id);
        compositeId.setReunionId(reunionId);
        return repository.findById(compositeId)
                .orElseThrow(() -> new ParticipantNotFound("Participant not found with id: " , id));
    }

    @Override
    public Participant save(Participant participant) {
        return repository.save(participant);
    }

    @Override
    @Transactional
    public Participant create(ParticipantRequest request) {
        Participant participant = ParticipantMapper.INSTANCE.map(request);
        participant.setId(sequenceGeneratorService.getNextId("participant_id_seq"));
        return repository.save(participant);
    }

    @Override
    @Transactional
    public Participant update(Integer id, Integer reunionId, ParticipantRequest request) throws ParticipantNotFound {
        Participant participantToUpdate = findById(id, reunionId);
        ParticipantMapper.INSTANCE.updateFromRequest(request, participantToUpdate);
        return repository.save(participantToUpdate);
    }

    @Override
    @Transactional
    public void delete(Integer id, Integer reunionId) throws ParticipantNotFound {
       findById(id, reunionId);
        repository.softDelete(id, reunionId); 
    }

    @Override
    @Transactional
    public List<Participant> synchronizeCommissionParticipants(Integer reunionId) throws MessagingException, IOException {
        Optional<Reunion> reunionOptional = reunionRepository.findById(reunionId);
        if (reunionOptional.isEmpty()) {
            throw new ReunionNotFound("",reunionId);
        }

        Reunion reunion = reunionOptional.get();
        if(!reunion.getStatus().equals(ReunionStatus.BROUILLON)){
            throw new ReunionStatusException("",reunion.getStatus());
        }

        // Get current commission members
        List<UserCom> commissionMembers = userComRepository.findAllByComIdAndIsDeletedFalse(reunion.getCom().getId());

        // Get current commission-related participants (MEMBRE or RESPONSABLE)
        Map<String, Participant> comParticipantsMap = reunion.getParticipants().stream()
                .filter(p -> p.getType().equals(ParticipantType.MEMBRE) || p.getType().equals(ParticipantType.RESPONSABLE))
                .collect(Collectors.toMap(Participant::getUserId, Function.identity(), (existing, replacement) -> existing));

        List<Participant> participantsToSave = new ArrayList<>();
        List<Participant> participantsToDelete = new ArrayList<>();
        List<Participant> updatedParticipants = new ArrayList<>(reunion.getParticipants());

        // Process current commission members: add new or update existing
        for (UserCom userCom : commissionMembers) {
            String userId = userCom.getUserId();
            Participant participant = comParticipantsMap.get(userId);

            UserDTO userDetails = keycloakAdminService.getUserById(userId);
            ParticipantType type = Objects.equals(reunion.getCom().getResponsableId(), userId) ? ParticipantType.RESPONSABLE : ParticipantType.MEMBRE;
            String fonction = String.valueOf(type);

            if (participant == null) {
                // Create new participant for new commission member
                ParticipantRequest participantRequest = new ParticipantRequest();
                participantRequest.setReunionId(reunion.getId());
                participantRequest.setNom(userDetails.getFirstName());
                participantRequest.setPrenom(userDetails.getLastName());
                participantRequest.setMail(userDetails.getEmail());
                participantRequest.setUserId(userId);
                participantRequest.setType(type);
                participantRequest.setOrganisme(reunion.getCom().getNom());
                participantRequest.setFonction(fonction);
                participantRequest.setPresent(false); // Default for new participants

                Participant newParticipant = create(participantRequest);
                updatedParticipants.add(newParticipant);
                participantsToSave.add(newParticipant);
            } else {
                // Update existing participant's details, preserve presence
                participant.setNom(userDetails.getFirstName());
                participant.setPrenom(userDetails.getLastName());
                participant.setMail(userDetails.getEmail());
                participant.setType(type); // Update role if RESPONSABLE changed
                participant.setOrganisme(reunion.getCom().getNom());
                participant.setFonction(fonction);
                participantsToSave.add(participant);
            }

            // Remove processed userId to identify participants to delete
            comParticipantsMap.remove(userId);
        }

        // Remaining participants in comParticipantsMap are no longer in commission, soft delete them
        participantsToDelete.addAll(comParticipantsMap.values());
        if (!participantsToDelete.isEmpty()) {
            participantsToDelete.forEach(p -> p.setPresent(false));
            repository.softDeleteAll(participantsToDelete);
            updatedParticipants.removeAll(participantsToDelete); // Remove from final list
        }

        // Save updated and new participants
        if (!participantsToSave.isEmpty()) {
            Set<String> existingUserIds = reunion.getParticipants().stream()
                    .map(Participant::getUserId)
                    .collect(Collectors.toSet());
            List<Participant> savedParticpents=  repository.saveAll(participantsToSave);
            sendReunionInvitations(reunionId,savedParticpents.stream().filter(p->!existingUserIds.contains(p.getUserId())).collect(Collectors.toList()));
        }

        // Update reunion's participants list
        reunion.setParticipants(updatedParticipants);

        // Return the updated list of all participants
        return new ArrayList<>(updatedParticipants);
    }

    @Override
    public Participant createInvitedParticipant(Integer reunionId, ParticipantInvitedRequest request) {
        Optional<Reunion> reunionOptional = reunionRepository.findById(reunionId);
        if (reunionOptional.isEmpty()) {
            throw new ReunionNotFound("",reunionId);
        }
        if(!reunionOptional.get().getStatus().equals(ReunionStatus.EN_COURS)){
            throw new ReunionStatusException("",reunionOptional.get().getStatus());
        }
        Participant participant = ParticipantMapper.INSTANCE.mapFromInvitedRequest(request);
        participant.setId(sequenceGeneratorService.getNextId("participant_id_seq"));
        participant.setReunionId(reunionId);
        participant.setType(ParticipantType.INVITE);
        return repository.save(participant);
    }
    public void sendReunionInvitations(Integer id,List<Participant> participants) throws MessagingException, IOException {
        // Get the current Admin ID
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        String connectedUserId = currentUserId.orElse("System");

        // Fetch the Reunion entity by ID
        Reunion reunion = reunionRepository.findByIdWithParticipantsFetched(id).orElseThrow(()-> new ReunionNotFound("Reunion not found with id: " , id));

        // Iterate over participants and send emails
        for (Participant participant : participants) {

            if(participant.getMail()!=null){
                mailService.sendReunionInvitationEmail(reunion, participant, connectedUserId);
            }
        }
    }
}
