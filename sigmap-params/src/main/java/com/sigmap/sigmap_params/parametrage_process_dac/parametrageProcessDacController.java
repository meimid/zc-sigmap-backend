package com.sigmap.sigmap_params.parametrage_process_dac;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.parametrage_process_dac.ParametrageProcessDac;
import com.sigmap.common.parametrage_process_dac.ParametrageProcessDacService;
import com.sigmap.common.parametrage_process_dac.exeptions.ParametrageProcessDacNotFound;
import com.sigmap.common.parametrage_process_dac.mapper.ParametrageProcessDacMapper;
import com.sigmap.common.parametrage_process_dac.request.ParametrageProcessDacRequest;
import com.sigmap.common.parametrage_process_dac.response.ParametrageProcessDacQuery;
import com.sigmap.common.parametrage_process_dac.response.ParametrageProcessDacResonse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parametrage-process-dac")
@AllArgsConstructor
@Slf4j
@Tag(name = "Parametrage_Process_Dac", description = "Parametrage Process Dac")
public class parametrageProcessDacController {
    private final ParametrageProcessDacService parametrageProcessDacService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<ParametrageProcessDacResonse> create(@RequestBody ParametrageProcessDacRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound {
        ParametrageProcessDac saved = parametrageProcessDacService.save(request);
        ParametrageProcessDacResonse response = ParametrageProcessDacMapper.INSTANCE.map(saved);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<ParametrageProcessDacResonse> update(@RequestBody ParametrageProcessDacRequest request) throws RefParamNotFound, ModeDePassationSelectionNotFound, ParametrageProcessDacNotFound {
        ParametrageProcessDac updated = parametrageProcessDacService.update(request);
        ParametrageProcessDacResonse response = ParametrageProcessDacMapper.INSTANCE.map(updated);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Page<ParametrageProcessDacResonse>> findAll(ParametrageProcessDacQuery query, Pageable pageable) {
        Page<ParametrageProcessDac> page = parametrageProcessDacService.findAll(query.toSpecification(), pageable);
        Page<ParametrageProcessDacResonse> responses = page.map(ParametrageProcessDacMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{typeMarcherId}/{modePassationId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<ParametrageProcessDacResonse> findOneByIds(@PathVariable ("typeMarcherId") Integer typeMarcherId,
                                                                     @PathVariable ("modePassationId") Integer modePassationId) {
        return parametrageProcessDacService.findOne(typeMarcherId, modePassationId)
                .map(ParametrageProcessDacMapper.INSTANCE::map)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
