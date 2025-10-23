package com.sigmap.sigmap_params.dac_prix;

import com.sigmap.common.dac_prix.DacPrix;
import com.sigmap.common.dac_prix.DacPrixService;
import com.sigmap.common.dac_prix.mapper.DacPrixMapper;
import com.sigmap.common.dac_prix.request.DacPrixRequest;
import com.sigmap.common.dac_prix.response.DacPrixQuery;
import com.sigmap.common.dac_prix.response.DacPrixResponce;
import com.sigmap.common.menu_item.mappers.MenuItemMapper;
import com.sigmap.common.menu_item.response.MenuItemResponse;
import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Contrôleur REST pour la gestion des prix DAC
 */
@Slf4j
@RestController
@RequestMapping("/api/dac-prix")
@RequiredArgsConstructor
@Tag(name = "DAC Prix", description = "API pour la gestion des prix des DAC")
public class DacPrixController {

    private final DacPrixService dacPrixService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Créer un nouveau prix DAC")
    public ResponseEntity<DacPrixResponce> createDacPrix(@RequestBody DacPrixRequest request) throws MonnaieNotFound {
        
        log.debug("Création d'un nouveau prix DAC: {}", request);
        DacPrix createdDacPrix = dacPrixService.save(request);
        DacPrixResponce responce = DacPrixMapper.INSTANCE.map(createdDacPrix);
        return ResponseEntity.ok(responce);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Mettre à jour un prix DAC existant")
    public ResponseEntity<DacPrixResponce> updateDacPrix(@RequestBody DacPrixRequest request) {
        
        log.debug("Mise à jour du prix DAC: {}", request);
        DacPrix updatedDacPrix = dacPrixService.update(request);
        DacPrixResponce responce = DacPrixMapper.INSTANCE.map(updatedDacPrix);
        return ResponseEntity.ok(responce);
    }


    @GetMapping("/{dacId}/{dacVer}/{monnaieId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Récupérer un prix DAC par son ID")
    public ResponseEntity<DacPrixResponce> getDacPrixById(
            @PathVariable Integer dacId,
            @PathVariable Integer dacVer,
            @PathVariable Integer monnaieId)  {

        log.info("Récupération du prix DAC pour dacId={}, dacVer={}, monnaieId={}", dacId, dacVer, monnaieId);
        DacPrix dacPrix = dacPrixService.findById(dacId, dacVer, monnaieId);
        DacPrixResponce responce = DacPrixMapper.INSTANCE.map(dacPrix);
        return ResponseEntity.ok(responce);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Récupérer tous les prix DAC avec pagination et filtres")
    public ResponseEntity<Page<DacPrixResponce>> getAllDacPrix(DacPrixQuery query, Pageable pageable) {
        log.info("Récupération des prix DAC avec filtres: {}", query);
        Page<DacPrix> dacPrixPage = dacPrixService.getAllDacPrix(query.toSpecification(), pageable);
        Page<DacPrixResponce> responces = dacPrixPage.map(DacPrixMapper.INSTANCE::map);
        return ResponseEntity.ok(responces);
    }

}
