package com.sigmap.sigmap_auth.keycloak;

import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.keycloakAdmin.dto.PasswordRequestDTO;
import com.sigmap.common.keycloakAdmin.dto.RoleDTO;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.dto.UserResquestDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.mail.MailService;
import com.sigmap.common.security.Authentication;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Contrôleur REST pour la gestion des utilisateurs et rôles via Keycloak.
 */
@RestController
@RequestMapping("/api/keycloak")
@RequiredArgsConstructor

public class KeycloakAdminController {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminController.class);

    // Service pour interagir avec l’API Admin de Keycloak
    @Autowired
    private KeycloakAdminService keycloakAdminService;
    @Autowired
    private MailService mailService;


    private boolean sendWithRetry(Supplier<Boolean> emailSender, String recipientEmail, int maxRetries) {
        int retryCount = 0;
        boolean emailSent = false;
        while (!emailSent && retryCount < maxRetries) {
            try {
                emailSent = emailSender.get();
                if (emailSent) {
                    log.info("Email sent successfully to {} (attempt {})", recipientEmail, retryCount + 1);
                    return true;
                } else {
                    retryCount++;
                    log.warn("Failed to send email to {} (attempt {}/{}): emailSender returned false", recipientEmail, retryCount, maxRetries);
                }
            } catch (Exception e) {
                retryCount++;
                log.warn("Failed to send email to {} (attempt {}/{}): {}", recipientEmail, retryCount, maxRetries, e.getMessage());
            }
            if (retryCount >= maxRetries) {
                log.error("Failed to send email to {} after {} attempts", recipientEmail, maxRetries);
                return false;
            }
        }
        return emailSent;
    }
    /**
     * Créer un nouvel utilisateur dans Keycloak.
     *
     * @param userDTO les données de l'utilisateur à créer
     * @return les informations de l'utilisateur créé
     */
    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserResquestDTO userDTO) throws MessagingException {

        // Get the current authenticated user ID
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        String connectedUserId = currentUserId.orElse("System");
        UserDTO createdUser = keycloakAdminService.createUser(userDTO);

        try {
            mailService.sendActivationEmail(userDTO, createdUser.getGenericPassword(), connectedUserId);
            //emailSent = true;
            log.info("Activation email sent successfully to {} on first attempt", userDTO.getEmail());
        } catch (MessagingException | IOException e) {
            log.error("Failed to send activation email to {} on first attempt: {}", userDTO.getEmail(), e.getMessage());
        }


        createdUser.setGenericPassword(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Récupérer la liste de tous les utilisateurs.
     *
     * @return liste des utilisateurs
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public List<UserDTO> getUsers() {

        String currentUserId = Authentication.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur courant introuvable"));

        boolean isAdminAcOnly = Authentication.hasRole("ADMIN_AC")
                && !Authentication.hasRole("SUPER_ADMIN")
                && !Authentication.hasRole("ADMIN_CENTRAL");

        if (isAdminAcOnly) {
            return keycloakAdminService.getUsersManagedByCurrentAdmin(currentUserId);
        } else {
            return keycloakAdminService.getAllUsers();
        }
    }


    /**
     * Récupérer un utilisateur par son ID.
     *
     * @param userId l’ID de l’utilisateur
     * @return les informations de l’utilisateur ou 404 si non trouvé
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        try {
            UserDTO userDTO = keycloakAdminService.getUserById(userId);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            // Retourner 404 si l’utilisateur n’est pas trouvé
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Mettre à jour les informations d’un utilisateur.
     *
     * @param id      l’ID de l’utilisateur
     * @param userDTO les nouvelles données utilisateur
     * @return les informations mises à jour
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id,
                                              @RequestBody UserResquestDTO userDTO) throws AgpmDefaultStatus {
        UserDTO updatedUser = keycloakAdminService.updateUser(id, userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }

    /**
     * Réinitialiser le mot de passe d'un utilisateur.
     *
     * @param id           l'ID de l'utilisateur
     * @return 200 OK si succès, 400 si validation échoue
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Void> updatePassword(@PathVariable String id) throws MessagingException {
        // Get the current authenticated user ID
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        String connectedUserId = currentUserId.orElse("System");
        String tmpPassword = generateTemporaryPassword();
        UserDTO user = keycloakAdminService.updatePassword(id, tmpPassword);

        try {
            mailService.sendEditPasswordForAdminEmail(user, tmpPassword, connectedUserId);
            log.info("Password reset email sent successfully to {} on first attempt", user.getEmail());
        } catch (MessagingException | IOException e) {
            log.warn("Failed to send password reset email to {} on first attempt: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok().build(); // 200 OK
    }
    public String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    /**
     * Récupérer la liste de tous les rôles disponibles dans le realm.
     *
     * @return liste des noms de rôles
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public List<RoleDTO> getAllRealmRoles() {
        return keycloakAdminService.getAllRealmRolesWithDescriptions();
    }

    /**
     * Supprimer un utilisateur par son ID.
     *
     * @param userId l’ID de l’utilisateur à supprimer
     * @return 204 No Content si succès, 404 si non trouvé
     */
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        keycloakAdminService.deleteUser(userId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Activer ou désactiver un utilisateur par son ID.
     *
     * @param userId  l’ID de l’utilisateur
     * @param enabled true pour activer, false pour désactiver
     * @return 200 OK si succès, 404 si utilisateur introuvable
     */
    @PutMapping("/users/{userId}/enable")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public ResponseEntity<Void> enableUser(@PathVariable String userId, @RequestParam boolean enabled) {
        try {
            keycloakAdminService.setUserEnabled(userId, enabled);
            return ResponseEntity.ok().build(); // 200 OK
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        }
    }

    @GetMapping("/users/com")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'ADMIN_AC', 'SUPER_ADMIN')")
    public List<UserDTO> getAllUsersExcptetTheUsersCom(@RequestParam Integer comId) throws ComNotFound {

        String currentUserId = Authentication.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur courant introuvable"));

        boolean isAdminAcOnly = Authentication.hasRole("ADMIN_AC")
                && !Authentication.hasRole("SUPER_ADMIN")
                && !Authentication.hasRole("ADMIN_CENTRAL");

        if (isAdminAcOnly) {
            return keycloakAdminService.getUsersManagedByCurrentAdmin(currentUserId);
        } else {
            return keycloakAdminService.getAllUsersExcptetTheUsersCom(comId);
        }
    }


}
