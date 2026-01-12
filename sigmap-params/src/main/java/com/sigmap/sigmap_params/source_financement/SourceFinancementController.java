package com.sigmap.sigmap_params.source_financement;

import com.sigmap.common.source_financement.SourceFinancement;
import com.sigmap.common.source_financement.SourceFinancementService;
import com.sigmap.common.source_financement.exceptions.SourceFinancementCodeAlreadyExist;
import com.sigmap.common.source_financement.exceptions.SourceFinancementNotFound;
import com.sigmap.common.source_financement.mappers.SourceFinancementMapper;
import com.sigmap.common.source_financement.request.SourceFinancementQuery;
import com.sigmap.common.source_financement.request.SourceFinancementRequest;
import com.sigmap.common.source_financement.response.SourceFinancementResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/source-financement")
@RequiredArgsConstructor
@Tag(name = "Source Financement", description = "Source Financement Management API")
public class SourceFinancementController {

    private final SourceFinancementService sourceFinancementService;

    @GetMapping
    public ResponseEntity<List<SourceFinancementResponse>> findAll() {

        List<SourceFinancementResponse> response = sourceFinancementService.findAll().stream()
                .map(SourceFinancementMapper.INSTANCE::map)
                .collect(Collectors.toList());


        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceFinancementResponse> findById(@PathVariable Integer id)
            throws SourceFinancementNotFound {
        SourceFinancement entity = sourceFinancementService.findById(id);
        return ResponseEntity.ok(SourceFinancementMapper.INSTANCE.map(entity));
    }

    @PostMapping
    public ResponseEntity<SourceFinancementResponse> create(@Valid @RequestBody SourceFinancementRequest request)
            throws SourceFinancementCodeAlreadyExist {
        SourceFinancement entity = sourceFinancementService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SourceFinancementMapper.INSTANCE.map(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SourceFinancementResponse> update(@PathVariable Integer id,
            @Valid @RequestBody SourceFinancementRequest request)
            throws SourceFinancementNotFound, SourceFinancementCodeAlreadyExist {
        SourceFinancement entity = sourceFinancementService.update(id, request);
        return ResponseEntity.ok(SourceFinancementMapper.INSTANCE.map(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) throws SourceFinancementNotFound {
        sourceFinancementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
