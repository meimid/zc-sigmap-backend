package com.sigmap.sigmap_params.reunion;


import com.sigmap.common.Participant.Participant;
import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.Participant.response.ParticipantResponseDto;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionService;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunion.mapper.ReunionMapper;
import com.sigmap.common.reunion.request.ReunionQuery;
import com.sigmap.common.reunion.request.ReunionRequest;
import com.sigmap.common.reunion.response.ReunionResponse;
import com.sigmap.common.reunionDac.mapper.ReunionDacMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reunions")
@Slf4j
public class ReunionController {

    private final ReunionService reunionService;

    public ReunionController(ReunionService reunionService) {
        this.reunionService = reunionService;
    }

    @GetMapping
    public ResponseEntity<Page<ReunionResponse>> findAll(ReunionQuery query, Pageable pageable) {
        Page<Reunion> page = reunionService.findAll(query.toSpecification(), pageable);
        return ResponseEntity.ok(page.map(ReunionMapper.INSTANCE::map));
    }

   /* @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ReunionResponse>> findAllDeleted(ReunionQuery query, Pageable pageable) {
        Page<Reunion> page = reunionService.findAllDeleted(query.toSpecification(), pageable);
        return ResponseEntity.ok(page.map(ReunionMapper.INSTANCE::map));
    }*/

    @GetMapping("/{id}")
    public ResponseEntity<ReunionResponse> findById(@PathVariable Integer id) throws ReunionNotFound {
        Reunion reunion = reunionService.findById(id);
        ReunionResponse response=ReunionMapper.INSTANCE.map(reunion);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ReunionResponse> create(@RequestBody ReunionRequest request) throws PpmNotFound, ComNotFound, AcNotFound {
        Reunion createdReunion = reunionService.create(request);
        ReunionResponse response=ReunionMapper.INSTANCE.map(createdReunion);
        response.setReunionDacs(response.getReunionDacs().stream().filter(el->el.getDac()!=null).toList());
        response.setParticipants(response.getParticipants().stream().filter(el->el.getId()!=null).toList());
        response.setReunionPpms(response.getReunionPpms().stream().filter(el->el.getPpm()!=null).toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/brouillon")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ReunionResponse> updateBrouillon(@PathVariable Integer id, @RequestBody ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound {
        Reunion updatedReunion = reunionService.update(id, request,ReunionStatus.BROUILLON);
        List<Participant> participantList=updatedReunion.getParticipants().stream().filter(el->!el.getIsDeleted()).toList();
        List<ParticipantResponseDto> responseDtos=ParticipantMapper.INSTANCE.toDtos(participantList);
        ReunionResponse response=ReunionMapper.INSTANCE.map(updatedReunion);
        response.setParticipants(responseDtos);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}/planifier_or_not")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ReunionResponse> updatePlanifierOrNot(@PathVariable Integer id, @RequestBody ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound {
        Reunion updatedReunion = reunionService.updatePlanifierOrNot(id, request);
        List<Participant> participantList=updatedReunion.getParticipants().stream().filter(el->!el.getIsDeleted()).toList();
        List<ParticipantResponseDto> responseDtos=ParticipantMapper.INSTANCE.toDtos(participantList);
        ReunionResponse response=ReunionMapper.INSTANCE.map(updatedReunion);
        response.setParticipants(responseDtos);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ReunionResponse> updateAnnuler(@PathVariable Integer id, @RequestBody ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound {
        Reunion updatedReunion = reunionService.update(id, request,ReunionStatus.ANNULER);
        List<Participant> participantList=updatedReunion.getParticipants().stream().filter(el->!el.getIsDeleted()).toList();
        List<ParticipantResponseDto> responseDtos=ParticipantMapper.INSTANCE.toDtos(participantList);
        ReunionResponse response=ReunionMapper.INSTANCE.map(updatedReunion);
        response.setParticipants(responseDtos);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ReunionResponse> deleteById(@PathVariable Integer id) throws ReunionNotFound {
        reunionService.delete(id);
        Reunion deletedReunion = reunionService.findDeletedById(id)
                .orElseThrow(() -> new ReunionNotFound("Could not find reunion after deletion: " , id));
        return ResponseEntity.ok(ReunionMapper.INSTANCE.map(deletedReunion));
    }
}
