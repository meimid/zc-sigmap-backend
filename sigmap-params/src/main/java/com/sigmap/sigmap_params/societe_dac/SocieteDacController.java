package com.sigmap.sigmap_params.societe_dac;

import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_dac.SocieteDac;
import com.sigmap.common.societe_dac.SocieteDacService;
import com.sigmap.common.societe_dac.exceptions.SocieteDacNotFoundException;
import com.sigmap.common.societe_dac.mappers.SocieteDacMapper;
import com.sigmap.common.societe_dac.request.SocieteDacRequest;
import com.sigmap.common.societe_dac.response.SocieteDacQuerry;
import com.sigmap.common.societe_dac.response.SocieteDacResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Contrôleur REST pour la gestion des sociétés DAC
 */
@Slf4j
@RestController
@RequestMapping("/api/societe-dac")
@RequiredArgsConstructor
@Tag(name = "Société DAC", description = "API pour la gestion des sociétés DAC")
public class SocieteDacController {

    private final SocieteDacService societeDacService;

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Créer une nouvelle société DAC")
    public ResponseEntity<SocieteDacResponse> createSocieteDac(@Valid @ModelAttribute SocieteDacRequest request) throws SocieteNotFound, IOException {
        log.info("Création d'une nouvelle société DAC: {}", request);
        SocieteDac createdSocieteDac = societeDacService.create(request);
        SocieteDacResponse response = SocieteDacMapper.INSTANCE.map(createdSocieteDac);
        return ResponseEntity.ok(response);
    }

    /*@PutMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Mettre à jour une société DAC existante")
    public ResponseEntity<SocieteDacResponse> updateSocieteDac(@Valid @RequestBody SocieteDacRequest request) {
        log.info("Mise à jour de la société DAC: {}", request);
        SocieteDac updatedSocieteDac = societeDacService.update(request);
        SocieteDacResponse response = SocieteDacMapper.INSTANCE.map(updatedSocieteDac);
        return ResponseEntity.ok(response);
    }*/

    @GetMapping("/{dacId}/{dacVersion}/{societeId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Récupérer une société DAC par son ID")
    public ResponseEntity<SocieteDacResponse> getSocieteDacById(
            @PathVariable Integer dacId,
            @PathVariable Integer dacVersion,
            @PathVariable Integer societeId) {
        
        log.info("Récupération de la société DAC pour dacId={}, dacVersion={}, societeId={}", 
                dacId, dacVersion, societeId);
        
        SocieteDac societeDac = societeDacService.findById(dacId, dacVersion, societeId);
        SocieteDacResponse response = SocieteDacMapper.INSTANCE.map(societeDac);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Récupérer toutes les sociétés DAC avec pagination")
    public ResponseEntity<Page<SocieteDacResponse>> getAllSocieteDac(SocieteDacQuerry querry, Pageable pageable) {
        
        log.info("Récupération des sociétés DAC");
        
        Page<SocieteDac> societeDacPage =  societeDacService.findAll(querry.toSpecification(), pageable);
                
        Page<SocieteDacResponse> responses = societeDacPage.map(SocieteDacMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    /*@DeleteMapping("/{dacId}/{dacVersion}/{societeId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Supprimer une société DAC")
    public ResponseEntity<Void> deleteSocieteDac(
            @PathVariable Integer dacId,
            @PathVariable Integer dacVersion,
            @PathVariable Integer societeId) {
        
        log.info("Suppression de la société DAC avec dacId={}, dacVersion={}, societeId={}", 
                dacId, dacVersion, societeId);
        
        societeDacService.delete(dacId, dacVersion, societeId);
        return ResponseEntity.noContent().build();
    }*/

    @GetMapping("/notDeleted")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    @Operation(summary = "Récupérer toutes les sociétés DAC avec pagination")
    public ResponseEntity<Page<SocieteDacResponse>> getAllSocieteDacNotDeleted(SocieteDacQuerry querry, Pageable pageable) {

        log.info("Récupération des sociétés DAC not deleted");

        Page<SocieteDac> societeDacPage =  societeDacService.findAllNotDeleted(querry.toSpecification(), pageable);

        Page<SocieteDacResponse> responses = societeDacPage.map(SocieteDacMapper.INSTANCE::map);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/acceptPaiement/{dacId}/{dacVersion}/{societeId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteDacResponse> ApprouverSocieteDacPaiement(
            @PathVariable Integer dacId,
            @PathVariable Integer dacVersion,
            @PathVariable Integer societeId) {
        log.info("Mise à jour le statut de paiement de la société DAC with approuve: ");
        SocieteDac updatedSocieteDac = societeDacService.approuverPaiement(dacId, dacVersion, societeId);
        SocieteDacResponse response = SocieteDacMapper.INSTANCE.map(updatedSocieteDac);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rejectPaiement/{dacId}/{dacVersion}/{societeId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<SocieteDacResponse> rejectSocieteDacPaiement(
            @PathVariable Integer dacId,
            @PathVariable Integer dacVersion,
            @PathVariable Integer societeId) {
        log.info("Mise à jour le statut de paiement de la société DAC with reject: ");
        SocieteDac updatedSocieteDac = societeDacService.rejectePaiement(dacId, dacVersion, societeId);
        SocieteDacResponse response = SocieteDacMapper.INSTANCE.map(updatedSocieteDac);
        return ResponseEntity.ok(response);
    }
}
