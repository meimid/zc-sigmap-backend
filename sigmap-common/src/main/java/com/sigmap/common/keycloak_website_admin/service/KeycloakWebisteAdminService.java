package com.sigmap.common.keycloak_website_admin.service;

import com.sigmap.common.keycloakAdmin.service.KeycloakTokenService;
import com.sigmap.common.keycloak_website_admin.dto.UserResquest;
import com.sigmap.common.keycloak_website_admin.dto.UserWebsiteDTO;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.request.SocieteRequestInscription;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

/**
 * Service d'administration Keycloak pour la gestion des utilisateurs "Website".
 * Permet la création, l'activation, la récupération et le déplacement des utilisateurs
 * entre les groupes Pending et Active, avec intégration à la logique métier "Société".
 */
@Service
public class KeycloakWebisteAdminService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakWebisteAdminService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private final KeycloakTokenService tokenService;
    private final SocieteService societeService;

    public KeycloakWebisteAdminService(KeycloakTokenService tokenService, SocieteService societeService) {
        this.tokenService = tokenService;
        this.societeService = societeService;
    }

    @Value("${keycloak-admin.server-url}")
    private String keycloakBaseUrl;

    @Value("${keycloak-admin.realm}")
    private String realm;

    @Value("${keycloak-admin.groupes.website-active}")
    private String gpWebsiteActive;

    @Value("${keycloak-admin.groupes.website-pending}")
    private String gpWebsitePending;


    /**
     * Crée un utilisateur Website (Pending) dans Keycloak
     * puis crée la société associée dans le système local.
     *
     * @param request données complètes d’inscription (société + user)
     * @return ID de l’utilisateur Keycloak créé
     * @throws RefParamNotFound si paramètre de référence manquant
     * @throws SocieteNotFound si la société n’a pas pu être créée
     */
    @Transactional
    public String createPendingWebsiteUserWithSociete(SocieteRequestInscription request)
            throws RefParamNotFound, SocieteNotFound {

        String token = tokenService.getAccessToken();
        HttpHeaders headers = createHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1️⃣ Création utilisateur dans Keycloak
        String userId = createUserInKeycloak(request.getUserResquest(), headers);

        // 2️⃣ Ajout au groupe "Pending"
        addUserToGroup(userId, gpWebsitePending, headers);
        logger.info("✅ Utilisateur {} ajouté au groupe PENDING.", request.getUserResquest().getUsername());

        // 3️⃣ Récupération infos Keycloak réelles
        UserWebsiteDTO userKeycloak = getUserDetails(userId, headers);
        if (userKeycloak == null)
            throw new RuntimeException("Erreur lors de la récupération des données utilisateur depuis Keycloak.");

        // 4️⃣ Injection des infos Keycloak dans la requête Société
        mapUserKeycloakToRequest(userKeycloak, request.getUserResquest());

        // 5️⃣ Création de la société associée
        societeService.create(request);

        logger.info("✅ Société créée et liée à l’utilisateur Keycloak {}", userId);
        return userId;
    }

    /**
     * Crée un utilisateur dans Keycloak avec statut inactif.
     *
     * @param dto informations du user à créer
     * @param headers headers HTTP avec token d’admin
     * @return l’ID Keycloak du nouvel utilisateur
     */
    private String createUserInKeycloak(UserResquest dto, HttpHeaders headers) {
        Map<String, Object> payload = Map.of(
                "username", dto.getUsername(),
                "firstName", dto.getFirstName(),
                "lastName", dto.getLastName(),
                "email", dto.getEmail(),
                "enabled", false,
                "emailVerified", false,
                "attributes", Map.of(
                        "address", List.of(dto.getAddress()),
                        "mobile", List.of(dto.getMobile()),
                        "fixe", List.of(dto.getFixe()),
                        "cin", List.of(dto.getCin()),
                        "dateCIN", List.of(dto.getDateCIN() != null ? dto.getDateCIN().toString() : "")
                )
        );

        String url = String.format("%s/admin/realms/%s/users", keycloakBaseUrl, realm);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(payload, headers), Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("❌ Échec création utilisateur Keycloak");
        }

        String location = response.getHeaders().getFirst("Location");
        if (location == null) throw new RuntimeException("❌ Impossible de récupérer l’ID Keycloak");
        return location.substring(location.lastIndexOf("/") + 1);
    }

    /**
     * Active un utilisateur Website, le déplace du groupe Pending vers Active,
     * génère un mot de passe temporaire, et retourne les infos complètes.
     *
     * @param userId ID Keycloak
     * @return informations utilisateur avec mot de passe temporaire
     */
    public UserWebsiteDTO validateWebsiteUser(String userId) {
        String token = tokenService.getAccessToken();
        HttpHeaders headers = createHeaders(token);

        String pendingId = getGroupIdByName(gpWebsitePending, headers);
        String activeId = getGroupIdByName(gpWebsiteActive, headers);
        if (pendingId == null || activeId == null)
            throw new RuntimeException("Groupes Website introuvables dans Keycloak");

        // Déplacement du groupe pending → active
        removeUserFromGroup(userId, pendingId, headers);
        addUserToGroup(userId, gpWebsiteActive, headers);

        // Activation du compte
        enableUser(userId, true, headers);

        // Génération d’un mot de passe temporaire
        String tempPassword = updatePasswordForAdmin(userId);

        UserWebsiteDTO dto = getUserDetails(userId, headers);
        dto.setGenericPassword(tempPassword);
        return dto;
    }


    /**
     * Récupère la liste des utilisateurs en attente d’activation.
     */
    public List<UserWebsiteDTO> getPendingUsers() {
        return getUsersByGroup(gpWebsitePending);
    }

    /**
     * Récupère la liste des utilisateurs actifs.
     */
    public List<UserWebsiteDTO> getActiveUsers() {
        return getUsersByGroup(gpWebsiteActive);
    }

    private List<UserWebsiteDTO> getUsersByGroup(String groupName) {
        String token = tokenService.getAccessToken();
        HttpHeaders headers = createHeaders(token);

        String groupId = getGroupIdByName(groupName, headers);
        if (groupId == null) return Collections.emptyList();

        String url = String.format("%s/admin/realms/%s/groups/%s/members", keycloakBaseUrl, realm, groupId);
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        List<Map<String, Object>> rawUsers = response.getBody();
        if (rawUsers == null) return Collections.emptyList();

        return rawUsers.stream().map(this::mapToUserWebsiteDTO).toList();
    }


    private void enableUser(String userId, boolean enabled, HttpHeaders headers) {
        Map<String, Object> payload = Map.of("enabled", enabled);
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakBaseUrl, realm, userId);
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(payload, headers), Void.class);
    }

    private void addUserToGroup(String userId, String groupName, HttpHeaders headers) {
        String groupId = getGroupIdByName(groupName, headers);
        if (groupId == null) return;
        String url = String.format("%s/admin/realms/%s/users/%s/groups/%s", keycloakBaseUrl, realm, userId, groupId);
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(headers), Void.class);
    }

    private void removeUserFromGroup(String userId, String groupId, HttpHeaders headers) {
        String url = String.format("%s/admin/realms/%s/users/%s/groups/%s", keycloakBaseUrl, realm, userId, groupId);
        restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }

    private String getGroupIdByName(String groupName, HttpHeaders headers) {
        String url = String.format("%s/admin/realms/%s/groups", keycloakBaseUrl, realm);
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        List<Map<String, Object>> groups = response.getBody();
        if (groups == null) return null;
        return groups.stream()
                .filter(g -> groupName.equals(g.get("name")))
                .map(g -> (String) g.get("id"))
                .findFirst()
                .orElse(null);
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    public String updatePasswordForAdmin(String userId) {
        String token = tokenService.getAccessToken();
        HttpHeaders headers = createHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String tempPassword = generateTemporaryPassword();
        Map<String, Object> payload = Map.of(
                "type", "password",
                "temporary", true,
                "value", tempPassword
        );

        String url = String.format("%s/admin/realms/%s/users/%s/reset-password", keycloakBaseUrl, realm, userId);
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(payload, headers), Void.class);
        return tempPassword;
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private UserWebsiteDTO getUserDetails(String userId, HttpHeaders headers) {
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakBaseUrl, realm, userId);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> map = response.getBody();
        return map != null ? mapToUserWebsiteDTO(map) : null;
    }

    private UserWebsiteDTO mapToUserWebsiteDTO(Map<String, Object> map) {
        UserWebsiteDTO dto = new UserWebsiteDTO();
        dto.setId((String) map.get("id"));
        dto.setUsername((String) map.get("username"));
        dto.setEmail((String) map.get("email"));
        dto.setFirstName((String) map.get("firstName"));
        dto.setLastName((String) map.get("lastName"));
        dto.setEnabled(Boolean.TRUE.equals(map.get("enabled")));
        dto.setEmailVerified(Boolean.TRUE.equals(map.get("emailVerified")));
        if (map.get("createdTimestamp") instanceof Number ts) {
            dto.setCreatedTimestamp(ts.longValue());
        }

        Map<String, List<String>> attrs = (Map<String, List<String>>) map.get("attributes");
        if (attrs != null) {
            dto.setAddress(getAttr(attrs, "address"));
            dto.setMobile(getAttr(attrs, "mobile"));
            dto.setFixe(getAttr(attrs, "fixe"));
            dto.setCin(getAttr(attrs, "cin"));
            String dateCIN = getAttr(attrs, "dateCIN");
            if (dateCIN != null && !dateCIN.isBlank()) dto.setDateCIN(LocalDate.parse(dateCIN));
        }
        return dto;
    }

    private String getAttr(Map<String, List<String>> attrs, String key) {
        List<String> values = attrs.get(key);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    private void mapUserKeycloakToRequest(UserWebsiteDTO src, UserResquest target) {
        target.setId(src.getId());
        target.setUsername(src.getUsername());
        target.setFirstName(src.getFirstName());
        target.setLastName(src.getLastName());
        target.setEmail(src.getEmail());
        target.setAddress(src.getAddress());
        target.setMobile(src.getMobile());
        target.setFixe(src.getFixe());
        target.setCin(src.getCin());
        target.setDateCIN(src.getDateCIN());
    }
}
