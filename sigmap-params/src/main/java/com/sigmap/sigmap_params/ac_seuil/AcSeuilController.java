package com.sigmap.sigmap_params.ac_seuil;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ac_seuil.AcSeuil;
import com.sigmap.common.ac_seuil.AcSeuilService;
import com.sigmap.common.ac_seuil.exeptions.AcSeuilNotFound;
import com.sigmap.common.ac_seuil.request.AcSeuilRequest;
import com.sigmap.common.ac_seuil.response.AcSeuilQuery;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/ac-seuil")
@AllArgsConstructor
@Slf4j
@Tag(name = "Ac_Seuil", description = "Ac Seuil API")
public class AcSeuilController {

    private final AcSeuilService acSeuilService;

    @PostMapping
    public ResponseEntity<AcSeuil> createAcSeuil(@RequestBody AcSeuilRequest request) throws
            AcNotFound, RefParamNotFound, ModeDePassationSelectionNotFound {
        AcSeuil savedAcSeuil = acSeuilService.save(request);
        return ResponseEntity.ok(savedAcSeuil);
    }

    @PutMapping
    public ResponseEntity<AcSeuil> updateAcSeuil(@RequestBody AcSeuilRequest request)
            throws AcNotFound, RefParamNotFound, ModeDePassationSelectionNotFound, AcSeuilNotFound {
        AcSeuil updatedAcSeuil = acSeuilService.update(request);
        return ResponseEntity.ok(updatedAcSeuil);
    }

    @GetMapping
    public ResponseEntity<Page<AcSeuil>> getAllAcSeuil(AcSeuilQuery query, Pageable pageable) {
        Page<AcSeuil> acSeuilPage = acSeuilService.getAllAcSeuils(query.toSpecification(),pageable);
        return ResponseEntity.ok(acSeuilPage);
    }

    @GetMapping("/{acId}/{typeMarcherId}/{modePassationId}")
    public ResponseEntity<AcSeuil> findAcSeuilById(
            @PathVariable("acId") Integer acId,
            @PathVariable("typeMarcherId") Integer typeMarcherId,
            @PathVariable("modePassationId") Integer modePassationId)
            throws AcSeuilNotFound, AcNotFound, RefParamNotFound, ModeDePassationSelectionNotFound {
        log.debug("Fetching AcSeuil by composite ID: acId={}, typeMarcherId={}, modePassationId={}",
                acId, typeMarcherId, modePassationId);
        Optional<AcSeuil> acSeuilOptional = acSeuilService.findById(acId, typeMarcherId, modePassationId);
        return acSeuilOptional.map(ResponseEntity::ok)
                .orElseThrow(() -> new AcSeuilNotFound("AcSeuil not found with acId{], typeMarcherId={}, modePassationId={}", acId, typeMarcherId, modePassationId));
    }
}
