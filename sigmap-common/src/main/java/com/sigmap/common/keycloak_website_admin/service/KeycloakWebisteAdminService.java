package com.sigmap.common.keycloak_website_admin.service;

import com.sigmap.common.keycloakAdmin.service.KeycloakTokenService;
import com.sigmap.common.keycloak_website_admin.dto.UserResquest;
import com.sigmap.common.keycloak_website_admin.dto.UserWebsiteDTO;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
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
 * Gestion des groupes Pending/Active, création de société associée et mot de passe temporaire.
 */
@Service
public class KeycloakWebisteAdminService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakWebisteAdminService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final KeycloakTokenService tokenService;

    public KeycloakWebisteAdminService(KeycloakTokenService tokenService) {
        this.tokenService = tokenService;

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
     * Crée un utilisateur Website (Pending) et la société associée.
     *
     * @param request données complètes d’inscription (société + user)
     * @return ID Keycloak
     * @throws RefParamNotFound si paramètre de référence manquant
     * @throws SocieteNotFound si création société impossible
     */
    @Transactional
    public String createPendingWebsiteUserWithSociete(SocieteRequestInscription request)
            throws RefParamNotFound, SocieteNotFound {

        HttpHeaders headers = createAuthHeaders();

        // 1️⃣ Création utilisateur Keycloak
        String userId = createUserInKeycloak(request.getUserResquest(), headers);

        // 2️⃣ Ajout au groupe Pending
        addUserToGroup(userId, gpWebsitePending, headers);
        logger.info("Utilisateur {} ajouté au groupe Pending", request.getUserResquest().getUsername());

        // 3️⃣ Récupération infos Keycloak réelles
        UserWebsiteDTO userKeycloak = getUserDetails(userId, headers);
        if (userKeycloak == null)
            throw new RuntimeException("Impossible de récupérer les informations de l'utilisateur Keycloak");

        // 4️⃣ Injection des infos Keycloak dans le DTO
        mapUserKeycloakToRequest(userKeycloak, request.getUserResquest());

        // 5️⃣ Création de la société associée
     //   societeService.create(request);
        logger.info("Société créée et liée à l'utilisateur Keycloak {}", userId);

        return userId;
    }

    /**
     * Crée l'utilisateur dans Keycloak (statut inactive).
     */


    private String createUserInKeycloak(UserResquest dto, HttpHeaders headers) {
        // Ensure JSON content type
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "username", dto.getUsername(),
                "firstName", dto.getFirstName(),
                "lastName", dto.getLastName(),
                "email", dto.getEmail(),
                "enabled", false,
                "emailVerified", false,
                "attributes", Map.of(
                        "address", List.of(Optional.ofNullable(dto.getAddress()).orElse("")),
                        "mobile", List.of(Optional.ofNullable(dto.getMobile()).orElse("")),
                        "fixe", List.of(Optional.ofNullable(dto.getFixe()).orElse("")),
                        "cin", List.of(Optional.ofNullable(dto.getCin()).orElse("")),
                        "dateCIN", List.of(dto.getDateCIN() != null ? dto.getDateCIN().toString() : "")
                )
        );

        String url = String.format("%s/admin/realms/%s/users", keycloakBaseUrl, realm);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Erreur création utilisateur Keycloak");
        }

        String location = response.getHeaders().getFirst("Location");
        if (location == null) {
            throw new RuntimeException("Impossible de récupérer l'ID Keycloak");
        }

        return location.substring(location.lastIndexOf("/") + 1);
    }

    /**
     * Active un utilisateur Website et le déplace du groupe Pending vers Active.
     *
     * @param userId ID Keycloak
     * @return DTO utilisateur avec mot de passe temporaire
     */
    public UserWebsiteDTO validateWebsiteUser(String userId) {
        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String pendingId = getGroupIdByName(gpWebsitePending, headers);
        String activeId = getGroupIdByName(gpWebsiteActive, headers);
        if (pendingId == null || activeId == null)
            throw new RuntimeException("Groupes Keycloak introuvables");

        removeUserFromGroup(userId, pendingId, headers);
        addUserToGroup(userId, gpWebsiteActive, headers);
        enableUser(userId, true, headers);

        String tempPassword = updatePasswordForAdmin(userId);

        UserWebsiteDTO dto = getUserDetails(userId, headers);
        dto.setGenericPassword(tempPassword);
        return dto;
    }

    public List<UserWebsiteDTO> getPendingUsers() {
        return getUsersByGroup(gpWebsitePending);
    }

    public List<UserWebsiteDTO> getActiveUsers() {
        return getUsersByGroup(gpWebsiteActive);
    }

    /*---------------- Utilitaires ----------------*/

    private List<UserWebsiteDTO> getUsersByGroup(String groupName) {

        HttpHeaders headers = createAuthHeaders();
        String groupId = getGroupIdByName(groupName, headers);
        if (groupId == null) return Collections.emptyList();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = String.format("%s/admin/realms/%s/groups/%s/members", keycloakBaseUrl, realm, groupId);
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        List<Map<String, Object>> rawUsers = response.getBody();
        if (rawUsers == null) return Collections.emptyList();

        return rawUsers.stream().map(this::mapToUserWebsiteDTO).toList();
    }

    private void enableUser(String userId, boolean enabled, HttpHeaders headers) {
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakBaseUrl, realm, userId);
        Map<String, Object> payload = Map.of("enabled", enabled);
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
                .findFirst().orElse(null);
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenService.getAccessToken());
        return headers;
    }

    public String updatePasswordForAdmin(String userId) {
        HttpHeaders headers = createAuthHeaders();
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
        if (map.get("createdTimestamp") instanceof Number ts) dto.setCreatedTimestamp(ts.longValue());

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
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
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
