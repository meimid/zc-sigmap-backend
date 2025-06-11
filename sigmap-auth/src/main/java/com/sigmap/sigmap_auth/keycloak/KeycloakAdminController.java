package com.sigmap.sigmap_auth.keycloak;

import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.keycloakAdmin.dto.PasswordRequestDTO;
import com.sigmap.common.keycloakAdmin.dto.RoleDTO;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.dto.UserResquestDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.security.AuthoritiesConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Contrôleur REST pour la gestion des utilisateurs et rôles via Keycloak.
 */
@RestController
@RequestMapping("/api/keycloak")
@RequiredArgsConstructor
public class KeycloakAdminController {

    // Service pour interagir avec l’API Admin de Keycloak
    @Autowired
    private KeycloakAdminService keycloakAdminService;

    /**
     * Créer un nouvel utilisateur dans Keycloak.
     *
     * @param userDTO les données de l’utilisateur à créer
     * @return les informations de l’utilisateur créé
     */
    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserResquestDTO userDTO) {
        UserDTO createdUser = keycloakAdminService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Récupérer la liste de tous les utilisateurs.
     *
     * @return liste des utilisateurs
     */
    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return keycloakAdminService.getAllUsers();
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
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id,
                                              @RequestBody UserResquestDTO userDTO) throws AgpmDefaultStatus {
        UserDTO updatedUser = keycloakAdminService.updateUser(id, userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }

    /**
     * Réinitialiser le mot de passe d’un utilisateur.
     *
     * @param id           l’ID de l’utilisateur
     * @param passwordDTO  l’objet contenant le nouveau mot de passe et sa confirmation
     * @return 200 OK si succès, 400 si validation échoue
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> updatePassword(@PathVariable String id,
                                               @RequestBody PasswordRequestDTO passwordDTO) {
        // Validation de la correspondance des mots de passe
        if (passwordDTO.getNewPassword() == null ||
                passwordDTO.getRepeatNewPassword() == null ||
                !passwordDTO.getNewPassword().equals(passwordDTO.getRepeatNewPassword())) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }

        keycloakAdminService.updatePassword(id, passwordDTO.getNewPassword());
        return ResponseEntity.ok().build(); // 200 OK
    }

    /**
     * Récupérer la liste de tous les rôles disponibles dans le realm.
     *
     * @return liste des noms de rôles
     */
    @GetMapping("/roles")
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
    public ResponseEntity<Void> enableUser(@PathVariable String userId, @RequestParam boolean enabled) {
        try {
            keycloakAdminService.setUserEnabled(userId, enabled);
            return ResponseEntity.ok().build(); // 200 OK
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        }
    }

}
