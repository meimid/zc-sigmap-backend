package com.sigmap.sigmap_params.seuil_general;

import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.seuil_general.SeuilGeneral;
import com.sigmap.common.seuil_general.SeuilGeneralService;
import com.sigmap.common.seuil_general.exception.SeuilGeneralNotFound;
import com.sigmap.common.seuil_general.mapper.SeuilGeneralMapper;
import com.sigmap.common.seuil_general.request.SeuilGeneralRequest;
import com.sigmap.common.seuil_general.response.SeuilGeneralQuery;
import com.sigmap.common.seuil_general.response.SeuilGeneralResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seuil-general")
@AllArgsConstructor
@Slf4j
@Tag(name = "Seuil_General", description = "Seuil General API")
public class SeuilGeneralController {

    private final SeuilGeneralService seuilGeneralService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<SeuilGeneralResponse> createSeuilGeneral(@RequestBody SeuilGeneralRequest request)
            throws RefParamNotFound {
        SeuilGeneral savedSeuilGeneral = seuilGeneralService.save(request);
        SeuilGeneralResponse response = SeuilGeneralMapper.INSTANCE.map(savedSeuilGeneral);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<SeuilGeneralResponse> updateSeuilGeneral(@PathVariable("id") Integer id,@RequestBody SeuilGeneralRequest request)
            throws RefParamNotFound, SeuilGeneralNotFound {
        SeuilGeneral updatedSeuilGeneral = seuilGeneralService.update(id,request);
        SeuilGeneralResponse response = SeuilGeneralMapper.INSTANCE.map(updatedSeuilGeneral);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Page<SeuilGeneralResponse>> getAllSeuilGeneral(SeuilGeneralQuery query, Pageable pageable) {
        Page<SeuilGeneral> seuilGeneralPage = seuilGeneralService.getAllSeuilGenerals(query.toSpecification(),
                pageable);
        Page<SeuilGeneralResponse> responses = seuilGeneralPage.map(SeuilGeneralMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<SeuilGeneralResponse> findSeuilGeneralById(@PathVariable("id") Integer id)
            throws SeuilGeneralNotFound {
        SeuilGeneral seuilGeneral = seuilGeneralService.findById(id);
        SeuilGeneralResponse response = SeuilGeneralMapper.INSTANCE.map(seuilGeneral);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSeuilGeneral(@PathVariable("id") Integer id) throws SeuilGeneralNotFound {
        seuilGeneralService.delete(id);
        return ResponseEntity.ok().build();
    }
}
