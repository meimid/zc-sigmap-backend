package com.sigmap.sigmap_params.participant;

import com.sigmap.common.Participant.ParticipantService;
import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionRepository;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/participant")
public class ParticipantController {
    private final ParticipantService service;
    private final ReunionRepository reunionRepository;

    public ParticipantController(ParticipantService service, ReunionRepository reunionRepository) {
        this.service = service;
        this.reunionRepository = reunionRepository;
    }

    @PutMapping("/{reunionId}/synchronize-participants")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<List<ParticipantResponseDto>> synchronizeCommissionParticipants(@PathVariable Integer reunionId) throws  ComNotFound {
        Optional<Reunion> reunionOptional = reunionRepository.findById(reunionId);
        if (reunionOptional.isEmpty()) {
            throw new ReunionNotFound("",reunionId);
        }

        Reunion reunion = reunionOptional.get();
        List<com.sigmap.common.Participant.Participant> updatedParticipants = service.synchronizeCommissionParticipants(reunion);
        List<ParticipantResponseDto> response = ParticipantMapper.INSTANCE.toDtos(updatedParticipants);
        return ResponseEntity.ok().body(response);
    }
}
