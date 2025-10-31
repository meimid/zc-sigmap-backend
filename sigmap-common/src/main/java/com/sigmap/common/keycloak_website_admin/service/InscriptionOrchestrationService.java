package com.sigmap.common.keycloak_website_admin.service;


import com.sigmap.common.adresse.AdressService;
import com.sigmap.common.com.exception.ResponsableRequired;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloak_website_admin.dto.UserResquest;
import com.sigmap.common.keycloak_website_admin.dto.UserWebsiteDTO;
import com.sigmap.common.keycloak_website_admin.exception.CreateUserFailed;
import com.sigmap.common.keycloak_website_admin.exception.EmailOrUserNameAlreadyUsedForAnotherUser;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.request.SocieteRequest;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteService;
import com.sigmap.common.user_societe.request.UserSocieteRequest;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Optional;

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
    private final AdressService adressService;
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
    public UserWebsiteDTO validateUser(String userId) throws SocieteNotFound, MessagingException, IOException {
        logger.info("Validation utilisateur Keycloak {}", userId);

        // Appel du service Keycloak pour activer le compte et déplacer le groupe
        UserWebsiteDTO dto = keycloakService.validateWebsiteUser(userId);
        UserSociete userSociete =userSocieteService.findByUserId(userId);
        societeService.updateStatus(userSociete.getSocieteId(), true);


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


    public String createUserNotResponsable(UserResquest request,String responsableId) throws  SocieteNotFound {
        logger.info("Début création utilisateur Keycloak et société pour {}", request.getUsername());
        HttpHeaders headers = keycloakService.createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Optional<String> currentUserId = Authentication.getCurrentUserId();
            //  Créer utilisateur Keycloak en Pending
       String userId = keycloakService.createWebsiteUserUnderResponsable(request);
        log.info("userId id .............{}", userId);
        if (userId != null) {
            request.setId(userId);
            try {
                userSocieteService.createNonResponsableUser(request, responsableId);
                String tempPassword = keycloakService.updatePasswordForAdmin(userId);
                UserWebsiteDTO dto = keycloakService.getUserDetails(userId, headers);
                dto.setGenericPassword(tempPassword);
                UserDTO userDTO=keycloakService.mapToUserDTO(dto,userId);
                mailService.sendActivationEmail1(userDTO, tempPassword, currentUserId.get());
            }
            catch (RuntimeException | MessagingException | IOException e) {
                keycloakService.deleteUser(userId);
                log.info("error when updating password {}", e.getMessage());
                throw new RuntimeException(e);
            }
            logger.info("Utilisateur {}  créés avec succès", request.getUsername());
        } else {
            throw new RuntimeException("Erreur lors de la création de l'utilisateur Keycloak");
        }
        return userId;

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


    /**
     * Crée un utilisateur Website Keycloak (Pending) et la société associée
     *
     * @param request DTO contenant les informations utilisateur + société
     * @return ID Keycloak de l'utilisateur créé
     * @throws RefParamNotFound si des paramètres de référence sont manquants
     * @throws SocieteNotFound si la société ne peut pas être créée
     */
    @Transactional
    public String updateUserAndSociete(Integer societeId,SocieteRequestInscription request) throws RefParamNotFound, SocieteNotFound {societeService.findById(societeId);
      Societe societe= societeService.findById(societeId);
       UserSociete userSociete=userSocieteService.findById(request.getUserResquest().getId(),societeId);
       if(!userSociete.getResponsable()||userSociete.getStatus().equals(ActiveStatus.NON_ACTIF)){
          throw new ResponsableRequired("");
       }
        SocieteRequest societeRequest = mapSocieteRequestForUpdate(request, societe);
         societeService.update(societeId, societeRequest);
        UserSocieteRequest userSocieteRequest = mapUserSocieteRequestForUpdate(societeId, request, userSociete);
        userSociete= userSocieteService.update(request.getUserResquest().getId(),userSocieteRequest,request.getUserResquest().getId());
        return userSociete.getUserId();

    }

    public SocieteRequest mapSocieteRequestForUpdate(SocieteRequestInscription request, Societe societe) {
        SocieteRequest societeRequest = new SocieteRequest();

        if (request != null) {
            if (request.getTypeId() != null)
                societeRequest.setTypeId(request.getTypeId());

            if (request.getNom() != null)
                societeRequest.setNom(request.getNom());

            if (request.getActiviteIds() != null)
                societeRequest.setActiviteIds(request.getActiviteIds());

            if (request.getCategorieSoumissionnaireId() != null)
                societeRequest.setCategorieSoumissionnaireId(request.getCategorieSoumissionnaireId());

            if (request.getAdresseRequest() != null)
                societeRequest.setAdresseRequest(request.getAdresseRequest());
        }

        if (societe != null) {
            if (societe.getNif() != null)
                societeRequest.setNif(societe.getNif());

            if (societe.getNumCnss() != null)
                societeRequest.setNumCnss(societe.getNumCnss());

            if (societe.getDateNif() != null)
                societeRequest.setDateNif(societe.getDateNif());
        }

        return societeRequest;
    }

    public UserSocieteRequest mapUserSocieteRequestForUpdate(Integer societeId,
                                                             SocieteRequestInscription request,
                                                             UserSociete userSociete) {
        UserSocieteRequest userSocieteRequest = new UserSocieteRequest();

        userSocieteRequest.setSocieteId(societeId);

        if (request != null && request.getUserResquest() != null) {
            var userReq = request.getUserResquest();

            if (userReq.getId() != null)
                userSocieteRequest.setUserId(userReq.getId());

            if (userReq.getLastName() != null)
                userSocieteRequest.setNom(userReq.getLastName());

            if (userReq.getFirstName() != null)
                userSocieteRequest.setPrenom(userReq.getFirstName());

            if (userSociete != null && userSociete.getEmail() != null)
                userSocieteRequest.setEmail(userSociete.getEmail());

            if (userReq.getDateCIN() != null)
                userSocieteRequest.setDateDelivranceCin(userReq.getDateCIN());

            if (userReq.getMobile() != null)
                userSocieteRequest.setTel1(userReq.getMobile());

            if (userReq.getFixe() != null)
                userSocieteRequest.setTel2(userReq.getFixe());

            if (userReq.getCin() != null)
                userSocieteRequest.setNumCin(userReq.getCin());
        }

        return userSocieteRequest;
    }

    public UserSociete updateUserWithSociete(SocieteRequestInscription request, String responsableId) throws RefParamNotFound {

        request.getUserResquest().setId(responsableId);
        Integer societeId=userSocieteService.findByUserId(responsableId).getSocieteId();
        String userId = updateUserAndSociete(societeId,request);
        UserSociete param = userSocieteService.findByUserId(userId);
        // get societe by id from param.getSocieteId() because type fetch lazy (chargement à la demande)
        Societe societe=societeService.findById(param.getSocieteId());
        param.setSociete(societe);
        return param;

    }


}
