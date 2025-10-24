package com.sigmap.sigmap_website_auth.societe;

import com.sigmap.common.keycloak_website_admin.service.InscriptionOrchestrationService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/soumissionnaire")
@RequiredArgsConstructor
@Slf4j
public class SoumissionnaireInscriptionController {
    private final InscriptionOrchestrationService inscriptionOrchestrationService;

    /***  Inscription d’un utilisateur website (Pending) ***/
    @PostMapping("/users/register-with-societe")
    public ResponseEntity<String> registerWebsiteUserWithSociete(@RequestBody SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound {
        String userId = inscriptionOrchestrationService.createUserAndSociete(request);
        log.info("Nouvelle demande d'inscription Website avec société, userId: {}", userId);
        return ResponseEntity.ok(userId);
    }
}
