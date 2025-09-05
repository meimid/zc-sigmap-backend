package com.sigmap.sigmap_params.participant;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.Participant.ParticipantService;
import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.Participant.request.ParticipantInvitedRequest;
import com.sigmap.common.Participant.request.ParticipantRequest;
import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionRepository;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/participant")
public class ParticipantController {
    private final ParticipantService service;

    public ParticipantController(ParticipantService service) {
        this.service = service;
    }

    @PutMapping("/{reunionId}/synchronize-participants")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<List<ParticipantResponseDto>> synchronizeCommissionParticipants(@PathVariable Integer reunionId) throws ComNotFound, MessagingException, IOException {

        List<com.sigmap.common.Participant.Participant> updatedParticipants = service.synchronizeCommissionParticipants(reunionId);
        List<ParticipantResponseDto> response = ParticipantMapper.INSTANCE.toDtos(updatedParticipants);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/{reunionId}/add_new_invited_participant")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','CNCMP-PER-PLANIFICATEUR','CNCMP-SPE-PLANIFICATEUR','CPMP-PLANIFICATEUR')")
    public ResponseEntity<List<ParticipantResponseDto>> createNewParticipants(@PathVariable Integer reunionId, @RequestBody ParticipantInvitedRequest request
    ) throws  ReunionNotFound {
       List <Participant> participants = service.createInvitedParticipant(reunionId,request);
        List <ParticipantResponseDto> response = participants.stream().map(ParticipantMapper.INSTANCE::toDto).collect(Collectors.toList());
        return ResponseEntity.ok().body(response);
    }

}
