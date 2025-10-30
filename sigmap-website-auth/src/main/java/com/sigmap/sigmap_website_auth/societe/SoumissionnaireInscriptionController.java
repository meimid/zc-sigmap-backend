package com.sigmap.sigmap_website_auth.societe;


import com.sigmap.common.keycloak_website_admin.dto.UserResquest;
import com.sigmap.common.keycloak_website_admin.service.InscriptionOrchestrationService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteService;
import com.sigmap.common.user_societe.exception.TokenRequired;
import com.sigmap.common.user_societe.exception.UserSocieteNotFound;
import com.sigmap.common.user_societe.mapper.UserSocieteMapper;
import com.sigmap.common.user_societe.response.UserSocieteResponse;

import com.sigmap.common.user_societe.response.UserSocieteResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/soumissionnaire")
@RequiredArgsConstructor
@Slf4j
public class SoumissionnaireInscriptionController {
    private final InscriptionOrchestrationService inscriptionOrchestrationService;
    private final UserSocieteService userSocieteService;

     /***  Inscription d’un utilisateur website (Pending) ***/
    @PostMapping("/users/register-with-societe")
    public ResponseEntity<String> registerWebsiteUserWithSociete(@RequestBody SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound {
        String userId = inscriptionOrchestrationService.createUserAndSociete(request);
        log.info("Nouvelle demande d'inscription Website avec société, userId: {}", userId);
        return ResponseEntity.ok(userId);
    }
    /**
     * Create a new UserSociete.
     *
     * @param request the UserSocieteRequest to create
     * @return the created UserSocieteResponse
     */
    @PostMapping("/new-user")
    public ResponseEntity<UserSocieteResponse> create(
            @RequestBody UserResquest request,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest servletRequest) throws SocieteNotFound {
        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new TokenRequired();
        }
         String responsableId = jwt.getSubject();
        String userId = inscriptionOrchestrationService.createUserNotResponsable(request,responsableId);

        UserSociete userSociete=userSocieteService.findByUserId(userId);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(userSociete);
        return ResponseEntity.ok(response);
    }
    /***  Inscription d’un utilisateur website (Pending) ***/
    @PutMapping
    public ResponseEntity<UserSocieteResponse> updateWebsiteUserWithSociete(@RequestBody SocieteRequestInscription request,@AuthenticationPrincipal Jwt jwt) throws RefParamNotFound, SocieteNotFound {
        String responsableId = jwt.getSubject();
        UserSociete param =inscriptionOrchestrationService.updateUserWithSociete(request,responsableId);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Find a UserSociete by userId only.
     *
     * @return the UserSocieteResponse
     */
    @GetMapping
    public ResponseEntity<UserSocieteResponse> findByUserId(@AuthenticationPrincipal Jwt jwt)
            throws UserSocieteNotFound {
        String userId = jwt.getSubject();

        UserSociete param = userSocieteService.findByUserId(userId);
        UserSocieteResponse response = UserSocieteMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSocieteResponseDto>> findUsersListByUserId(@AuthenticationPrincipal Jwt jwt)
            throws UserSocieteNotFound {
        String userId = jwt.getSubject();

        UserSociete param = userSocieteService.findByUserId(userId);
        List<UserSociete> list = userSocieteService.findBySocieteId(param.getSocieteId());
        List<UserSocieteResponseDto> responses = list.stream().filter(u->!u.getResponsable())
                .map(UserSocieteMapper.INSTANCE::toDto)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
