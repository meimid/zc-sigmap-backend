package com.sigmap.common.keycloak_website_admin.service;


import com.sigmap.common.entity.SocieteStatus;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.keycloak_website_admin.dto.UserResquest;
import com.sigmap.common.keycloak_website_admin.dto.UserWebsiteDTO;
import com.sigmap.common.keycloak_website_admin.exception.CreateUserFailed;
import com.sigmap.common.keycloak_website_admin.exception.EmailOrUserNameAlreadyUsedForAnotherUser;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.security.SecureRandom;

/**
 * Service d'orchestration pour gérer l'inscription d'un utilisateur Website
 * et la création de la société associée.
 *
 * Rôle :
 * - Créer l'utilisateur Keycloak en mode Pending
 * - Créer la société dans la base
 * - Associer l'utilisateur à la société
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InscriptionOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(InscriptionOrchestrationService.class);

    private final KeycloakWebisteAdminService keycloakService;
    private final SocieteService societeService;
    private final UserSocieteService userSocieteService;
    private final KeycloakAdminService keycloakAdminService;
    @Autowired
    MailService mailService;
    /**
     * Crée un utilisateur Website Keycloak (Pending) et la société associée
     *
     * @param request DTO contenant les informations utilisateur + société
     * @return ID Keycloak de l'utilisateur créé
     * @throws RefParamNotFound si des paramètres de référence sont manquants
     * @throws SocieteNotFound si la société ne peut pas être créée
     */
    public String createUserAndSociete(SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound {
        logger.info("Début création utilisateur Keycloak et société pour {}", request.getUserResquest().getUsername());
        String userId;
        try {
            // 1️⃣ Créer utilisateur Keycloak en Pending
             userId = keycloakService.createPendingWebsiteUserWithSociete(request.getUserResquest());
             // get user by user id from keycloak

       ;

            if (userId != null) {
                request.getUserResquest().setId(userId);
                try {
                    societeService.create(request);
                }catch (RuntimeException e){
                        try {
                            keycloakService.deleteUser(userId);
                            logger.warn("Rollback : utilisateur Keycloak {} supprimé suite à un échec de création de société", userId);
                        } catch (Exception deleteEx) {
                            logger.error("Impossible de supprimer l'utilisateur Keycloak {} lors du rollback : {}", userId, deleteEx.getMessage());

                        }
                        throw e;
                }
                logger.info("Utilisateur {} et société créés avec succès", request.getUserResquest().getUsername());
            } else {
                throw new RuntimeException("Erreur lors de la création de l'utilisateur Keycloak");
            }

            return userId;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                throw new EmailOrUserNameAlreadyUsedForAnotherUser("");
            } else {

                throw new CreateUserFailed("");
            }
        }
    }

    /**
     * Valide l'utilisateur Website : activation + déplacement du groupe Pending → Active
     *
     * @param userId ID Keycloak de l'utilisateur
     * @return DTO utilisateur complet avec mot de passe temporaire
     */
    public UserWebsiteDTO validateUser(String userId) throws SocieteNotFound {
        logger.info("Validation utilisateur Keycloak {}", userId);

        // Appel du service Keycloak pour activer le compte et déplacer le groupe
        UserWebsiteDTO dto = keycloakService.validateWebsiteUser(userId);
        UserSociete userSociete =userSocieteService.findByUserId(userId);
        societeService.updateStatus(userSociete.getSocieteId(), SocieteStatus.VALIDEE);


        logger.info("Utilisateur {} activé et groupe Website Active mis à jour", userId);
        return dto;
    }

    /**
     * Récupère tous les utilisateurs en attente (Pending)
     *
     * @return liste des DTO utilisateurs Pending
     */
    public java.util.List<UserWebsiteDTO> getPendingUsers() {
        return keycloakService.getPendingUsers();
    }

    /**
     * Récupère tous les utilisateurs actifs (Active)
     *
     * @return liste des DTO utilisateurs Active
     */
    public java.util.List<UserWebsiteDTO> getActiveUsers() {
        return keycloakService.getActiveUsers();
    }


    public String createUserNotResponsableAndSociete(UserResquest request,String responsableId) throws  SocieteNotFound {
        logger.info("Début création utilisateur Keycloak et société pour {}", request.getUsername());
        String userId;
        try {
            // 1️⃣ Créer utilisateur Keycloak en Pending
            userId = keycloakService.createPendingWebsiteUserWithSociete(request);


            if (userId != null) {
                request.setId(userId);
                try {
                    userSocieteService.createNonResponsableUser(request,responsableId);
                    try {

                        String tmpPassword = generateTemporaryPasswordForInscription();

                        UserDTO user = keycloakAdminService.updatePassword(userId, tmpPassword);

                        try {
                            mailService.sendEditPasswordForAdminEmail(user, tmpPassword, userId);
                            log.info("Password reset email sent successfully to {} on first attempt", user.getEmail());
                        } catch (MessagingException | IOException e) {
                            keycloakService.deleteUser(userId);
                            log.warn("Failed to send password reset email to {} on first attempt: {}", user.getEmail(), e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                    catch (Exception e) {
                        keycloakAdminService.deleteUser(userId);
                        log.info("error when updating password {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                }catch (RuntimeException e){
                    try {
                        keycloakService.deleteUser(userId);
                        logger.warn("Rollback : utilisateur Keycloak {} supprimé suite à un échec de création de société", userId);
                    } catch (Exception deleteEx) {
                        logger.error("Impossible de supprimer l'utilisateur Keycloak {} lors du rollback : {}", userId, deleteEx.getMessage());

                    }
                    throw e;
                }
                logger.info("Utilisateur {}  créés avec succès", request.getUsername());
            } else {
                throw new RuntimeException("Erreur lors de la création de l'utilisateur Keycloak");
            }

            return userId;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                throw new EmailOrUserNameAlreadyUsedForAnotherUser("");
            } else {

                throw new CreateUserFailed("");
            }
        }
    }

    public String generateTemporaryPasswordForInscription() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
