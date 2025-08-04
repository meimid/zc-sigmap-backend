package com.sigmap.sigmap_params.reunion;


import com.sigmap.common.Participant.mapper.ParticipantMapper;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
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

    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ReunionResponse>> findAllDeleted(ReunionQuery query, Pageable pageable) {
        Page<Reunion> page = reunionService.findAllDeleted(query.toSpecification(), pageable);
        return ResponseEntity.ok(page.map(ReunionMapper.INSTANCE::map));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReunionResponse> findById(@PathVariable Integer id) throws ReunionNotFound {
        Reunion reunion = reunionService.findById(id);
        return ResponseEntity.ok(ReunionMapper.INSTANCE.map(reunion));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ReunionResponse> create(@RequestBody ReunionRequest request) throws PpmNotFound, ComNotFound, AcNotFound {
        Reunion createdReunion = reunionService.create(request);
        ReunionResponse response=ReunionMapper.INSTANCE.map(createdReunion);
        log.info("...................... {}",response.toString());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<ReunionResponse> update(@PathVariable Integer id, @RequestBody ReunionRequest request) throws ReunionNotFound, PpmNotFound, AcNotFound {
        Reunion updatedReunion = reunionService.update(id, request);
        return ResponseEntity.ok(ReunionMapper.INSTANCE.map(updatedReunion));
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
