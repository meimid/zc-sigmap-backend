package com.sigmap.sigmap_website_auth.societe;

import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.keycloak_website_admin.service.InscriptionOrchestrationService;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Optional;

@RestController
@RequestMapping("/api/soumissionnaire")
@RequiredArgsConstructor
@Slf4j
public class SoumissionnaireInscriptionController {
    private final InscriptionOrchestrationService inscriptionOrchestrationService;
    private final KeycloakAdminService keycloakAdminService;

    private final MailService mailService;

    /***  Inscription d’un utilisateur website (Pending) ***/
    @PostMapping("/users/register-with-societe")
    public ResponseEntity<String> registerWebsiteUserWithSociete(@RequestBody SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound {
        String userId = inscriptionOrchestrationService.createUserAndSociete(request);
        log.info("Nouvelle demande d'inscription Website avec société, userId: {}", userId);


        String tmpPassword = inscriptionOrchestrationService.generateTemporaryPasswordForInscription();
        try {
            UserDTO user = keycloakAdminService.updatePassword(userId, tmpPassword);

            try {
                mailService.sendEditPasswordForAdminEmail(user, tmpPassword, userId);
                log.info("Password reset email sent successfully to {} on first attempt", user.getEmail());
            } catch (MessagingException | IOException e) {
                log.warn("Failed to send password reset email to {} on first attempt: {}", user.getEmail(), e.getMessage());
                throw new RuntimeException(e);
            }
        }
        catch (Exception e) {
            log.info("error when updating password {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(userId);
    }


}
