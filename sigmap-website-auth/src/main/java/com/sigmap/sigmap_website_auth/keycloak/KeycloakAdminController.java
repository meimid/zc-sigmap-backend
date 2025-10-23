package com.sigmap.sigmap_website_auth.keycloak;

import com.sigmap.common.keycloak_website_admin.dto.UserWebsiteDTO;
import com.sigmap.common.keycloak_website_admin.service.KeycloakWebisteAdminService;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KeycloakAdminController {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminController.class);

    private final KeycloakWebisteAdminService keycloakAdminService;
    private final MailService mailService;


    /***  Inscription d’un utilisateur website (Pending) ***/
    @PostMapping("/users/register-with-societe")
    public ResponseEntity<String> registerWebsiteUserWithSociete(@RequestBody SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound {
        String userId = keycloakAdminService.createPendingWebsiteUserWithSociete(request);
        log.info("Nouvelle demande d'inscription Website avec société, userId: {}", userId);
        return ResponseEntity.ok(userId);
    }


    /*** Validation par l’admin (Activation + mail) ***//*
    @PostMapping("/users/{userId}/validate")
    public ResponseEntity<UserWebsiteDTO> validateWebsiteUser(@PathVariable String userId) throws MessagingException, IOException {
        UserWebsiteDTO userDto = keycloakAdminService.validateWebsiteUser(userId);

        // Envoi d’un mail avec le mot de passe temporaire
        String subject = "Votre compte Website est activé";
        String body = String.format("Bonjour %s,\n\nVotre compte a été activé.\nVotre mot de passe temporaire est : %s\n\nMerci.",
                userDto.getFirstName(), userDto.getGenericPassword());
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        String connectedUserId = currentUserId.orElse("System");
       // mailService.sendActivationEmailWebsite(userDto, userDto.getGenericPassword(), connectedUserId);

        log.info("Utilisateur {} validé et mail envoyé", userDto.getUsername());
        return ResponseEntity.ok(userDto);
    }

    *//*** Récupération des utilisateurs Pending ***//*
    @GetMapping("/users/pending")
    public ResponseEntity<List<UserWebsiteDTO>> getPendingUsers() {
        List<UserWebsiteDTO> pendingUsers = keycloakAdminService.getPendingUsers();
        return ResponseEntity.ok(pendingUsers);
    }

    *//*** Récupération des utilisateurs Active ***//*
    @GetMapping("/users/active")
    public ResponseEntity<List<UserWebsiteDTO>> getActiveUsers() {
        List<UserWebsiteDTO> activeUsers = keycloakAdminService.getActiveUsers();
        return ResponseEntity.ok(activeUsers);
    }*/
}
