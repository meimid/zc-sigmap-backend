package com.sigmap.sigmap_website_auth.societe;

import com.sigmap.common.keycloak_website_admin.dto.UserResquest;
import com.sigmap.common.keycloak_website_admin.service.InscriptionOrchestrationService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.mappers.SocieteMapper;
import com.sigmap.common.societe.request.SocieteStatusRequest;
import com.sigmap.common.societe.response.SocieteResponse;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteService;
import com.sigmap.common.user_societe.exception.TokenRequired;
import com.sigmap.common.user_societe.mapper.UserSocieteMapper;
import com.sigmap.common.user_societe.response.UserSocieteResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/societe")
public class SocieteController {
    private final SocieteService societeService;
    private final InscriptionOrchestrationService inscriptionOrchestrationService;
    private final UserSocieteService userSocieteService;
    public SocieteController(SocieteService societeService, InscriptionOrchestrationService inscriptionOrchestrationService, UserSocieteService userSocieteService) {
        this.societeService = societeService;
        this.inscriptionOrchestrationService = inscriptionOrchestrationService;
        this.userSocieteService = userSocieteService;
    }
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL')")

    @PutMapping("/validate/{id}")
    public ResponseEntity<SocieteResponse> updateStatus(@PathVariable Integer id, @RequestBody SocieteStatusRequest request) throws RefParamNotFound, SocieteNotFound {
        Societe Societe = societeService.updateStatus(id,request.isApproved());
        SocieteResponse response = SocieteMapper.INSTANCE.map(Societe);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
