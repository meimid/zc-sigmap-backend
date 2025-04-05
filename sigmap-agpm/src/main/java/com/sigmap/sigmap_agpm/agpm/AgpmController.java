package com.sigmap.sigmap_agpm.agpm;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmService;
import com.sigmap.common.agpm.exceptions.AgpmAlreadyExistsException;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.mappers.AgpmMapper;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.request.AgpmRequest;
import com.sigmap.common.agpm.response.AgpmResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/agpms")
public class AgpmController {
    private final Logger logger = LoggerFactory.getLogger(AgpmController.class);
    private final AgpmService agpmService;

    public AgpmController(AgpmService agpmService) {
        this.agpmService = agpmService;
    }

    @GetMapping
    public ResponseEntity<Page<AgpmResponse>> findAll(
            AgpmQuery query, Pageable pageable) {
        final Jwt user = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        logger.info("User {}", user.getSubject());

        Page<Agpm> agpms = agpmService.findAll(query.toSpecification(), pageable);
        Page<AgpmResponse> responses = agpms.map(AgpmMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<AgpmResponse>> findAllDeleted(
            AgpmQuery query, Pageable pageable) {
        Page<Agpm> agpms = agpmService.findAllDeleted(query.toSpecification(), pageable);
        System.out.println("the agpms are " + agpms.getContent());
        Page<AgpmResponse> responses = agpms.map(AgpmMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<AgpmResponse> findById(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer) throws AgpmNotFound {
        Agpm agpm = agpmService.findById(agpmId, agpmVer)
                .orElseThrow(() -> new AgpmNotFound(agpmId, agpmVer, "Agpm with ID " + agpmId + " and version " + agpmVer + " was not found in the database."));
        AgpmResponse response = AgpmMapper.INSTANCE.map(agpm);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AgpmResponse> create(@Valid @RequestBody AgpmRequest request) throws AgpmAlreadyExistsException {
        Agpm agpm = agpmService.create(request);
        AgpmResponse response = AgpmMapper.INSTANCE.map(agpm);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<AgpmResponse> update(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer,
            @Valid @RequestBody AgpmRequest request) throws AgpmNotFound {
        Agpm updatedAgpm = agpmService.update(agpmId, agpmVer, request);
        AgpmResponse response = AgpmMapper.INSTANCE.map(updatedAgpm);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<AgpmResponse> deleteById(
            @PathVariable Integer agpmId,
            @PathVariable Integer agpmVer) {
        agpmService.delete(agpmId, agpmVer);
        Agpm deleted = agpmService.findDeletedById(agpmId, agpmVer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deleted Agpm not found"));
        AgpmResponse response = AgpmMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}