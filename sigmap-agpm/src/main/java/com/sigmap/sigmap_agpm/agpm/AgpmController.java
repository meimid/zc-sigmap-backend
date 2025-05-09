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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/agpms")
public class AgpmController {

    private final AgpmService agpmService;

    public AgpmController(AgpmService agpmService) {
        this.agpmService = agpmService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAgpms(AgpmQuery query, Pageable pageable) {
        Page<Agpm> agpmPage = agpmService.findAll(query.toSpecification(), pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        Map<String, Object> result = new HashMap<>();
        result.put("agpms", agpmResponsePage);
        result.put("status", "AGPMs retrieved successfully");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Map<String, Object>> getAllDeletedAgpms(Pageable pageable) {
        Page<Agpm> agpmPage = agpmService.findAllDeleted(pageable);
        Page<AgpmResponse> agpmResponsePage = agpmPage.map(AgpmMapper.INSTANCE::map);
        Map<String, Object> result = new HashMap<>();
        result.put("agpms", agpmResponsePage);
        result.put("status", "Deleted AGPMs retrieved successfully");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<Map<String, Object>> getAgpmById(@PathVariable Integer agpmId, @PathVariable Integer agpmVer) {
        Optional<Agpm> agpm = agpmService.findById(agpmId, agpmVer);
        Map<String, Object> result = new HashMap<>();
        if (agpm.isPresent()) {
            result.put("agpm", AgpmMapper.INSTANCE.map(agpm.get()));
            result.put("status", "AGPM retrieved successfully");
            return ResponseEntity.ok(result);
        }
        result.put("status", "AGPM not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAgpm(@Valid @RequestBody AgpmRequest request) throws AgpmAlreadyExistsException {
        Agpm createdAgpm = agpmService.create(request);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(createdAgpm);
        Map<String, Object> result = new HashMap<>();
        result.put("agpm", agpmResponse);
        result.put("status", "AGPM created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<Map<String, Object>> updateAgpm(@PathVariable Integer agpmId, @PathVariable Integer agpmVer, @Valid @RequestBody AgpmRequest request) throws AgpmNotFound {
        Agpm updatedAgpm = agpmService.updateWithoutVersioning(agpmId, agpmVer, request);
        AgpmResponse agpmResponse = AgpmMapper.INSTANCE.map(updatedAgpm);
        Map<String, Object> result = new HashMap<>();
        result.put("agpm", agpmResponse);
        result.put("status", "AGPM updated successfully");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{agpmId}/{agpmVer}")
    public ResponseEntity<Map<String, Object>> deleteAgpm(@PathVariable Integer agpmId, @PathVariable Integer agpmVer) throws AgpmNotFound {
        agpmService.delete(agpmId, agpmVer);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "AGPM deleted successfully");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/status/{agpmId}/{agpmVer}/{statusId}")
    public ResponseEntity<Map<String, Object>> updateAgpmStatus(@PathVariable Integer agpmId, @PathVariable Integer agpmVer, @PathVariable Integer statusId) throws AgpmNotFound {
        Agpm updated = agpmService.updateStatus(agpmId, agpmVer, statusId);
        AgpmResponse response = AgpmMapper.INSTANCE.map(updated);
        Map<String, Object> result = new HashMap<>();
        result.put("agpm", response);
        result.put("status", "AGPM status updated successfully");
        return ResponseEntity.ok(result);
    }
}