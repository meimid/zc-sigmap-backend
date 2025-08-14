package com.sigmap.sigmap_params.ac_seuil;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac_seuil.AcSeuil;
import com.sigmap.common.ac_seuil.AcSeuilService;
import com.sigmap.common.ac_seuil.exeptions.AcSeuilNotFound;
import com.sigmap.common.ac_seuil.mapper.AcSeuilMapper;
import com.sigmap.common.ac_seuil.request.AcSeuilRequest;
import com.sigmap.common.ac_seuil.response.AcSeuilQuery;
import com.sigmap.common.ac_seuil.response.AcSeuilResponse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ac-seuil")
@AllArgsConstructor
@Slf4j
@Tag(name = "Ac_Seuil", description = "Ac Seuil API")
public class AcSeuilController {

    private final AcSeuilService acSeuilService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<AcSeuilResponse> createAcSeuil(@RequestBody AcSeuilRequest request) throws
            AcNotFound, RefParamNotFound, ModeDePassationSelectionNotFound {
        AcSeuil savedAcSeuil = acSeuilService.save(request);
        AcSeuilResponse acSeuilResponse = AcSeuilMapper.INSTANCE.map(savedAcSeuil);
        return ResponseEntity.ok(acSeuilResponse);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<AcSeuilResponse> updateAcSeuil(@RequestBody AcSeuilRequest request)
            throws AcNotFound, RefParamNotFound, ModeDePassationSelectionNotFound, AcSeuilNotFound {
        AcSeuil updatedAcSeuil = acSeuilService.update(request);
        AcSeuilResponse response = AcSeuilMapper.INSTANCE.map(updatedAcSeuil);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AcSeuilResponse>> getAllAcSeuil(AcSeuilQuery query, Pageable pageable) {
        Page<AcSeuil> acSeuilPage = acSeuilService.getAllAcSeuils(query.toSpecification(),pageable);
        Page<AcSeuilResponse> responses=acSeuilPage.map(AcSeuilMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{acId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AcSeuilResponse>> findAcSeuilById(@PathVariable("acId") Integer acId, Pageable pageable)
            throws AcSeuilNotFound{
        try {
            if (acId == null ) {
                throw new IllegalArgumentException("you must have acId");
            }
            log.debug("Fetching AcSeuil by ID: acId={} : ", acId);
            Page<AcSeuil> acSeuilList = acSeuilService.findById(acId,pageable);
            Page<AcSeuilResponse> responsePage = acSeuilList.map(AcSeuilMapper.INSTANCE::map);
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching AcSeuil by ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

    }
}
