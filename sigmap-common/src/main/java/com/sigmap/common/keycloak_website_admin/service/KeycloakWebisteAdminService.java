package com.sigmap.common.keycloak_website_admin.service;

import com.sigmap.common.keycloakAdmin.service.KeycloakTokenService;
import com.sigmap.common.keycloak_website_admin.dto.UserResquestWebsiteDTO;
import com.sigmap.common.keycloak_website_admin.dto.UserWebsiteDTO;
import com.sigmap.common.keycloak_website_admin.dto.UserWebsiteGlobalRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe.request.SocieteGlobalRequest;
import com.sigmap.common.user_societe.request.UserSocieteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
     * Crée un utilisateur Website dans Keycloak (Pending) et crée la société associée.
     */
    public String createPendingWebsiteUserWithSociete(UserWebsiteGlobalRequest request) throws RefParamNotFound, SocieteNotFound {
        //  Création de l'utilisateur dans Keycloak
        String userId = createPendingWebsiteUser(request.getUserRequest());

        //  Création de la société et alimentation table UserSociete
        SocieteGlobalRequest societeGlobal = request.getSocieteRequest();

        // Remplir userSocieteRequest avec l'userId généré
        if (societeGlobal.getUserSocieteRequest() == null) {
            societeGlobal.setUserSocieteRequest(new UserSocieteRequest());
        }
        societeGlobal.getUserSocieteRequest().setUserId(userId);
        societeGlobal.getUserSocieteRequest().setNom(request.getUserRequest().getLastName());
        societeGlobal.getUserSocieteRequest().setPrenom(request.getUserRequest().getFirstName());
        societeGlobal.getUserSocieteRequest().setEmail(request.getUserRequest().getEmail());
        societeGlobal.getUserSocieteRequest().setTel1(request.getUserRequest().getMobile());
        societeGlobal.getUserSocieteRequest().setTel2(request.getUserRequest().getFixe());
        societeGlobal.getUserSocieteRequest().setNumCin(request.getUserRequest().getCin());
        societeGlobal.getUserSocieteRequest().setDateDelivranceCin(
                request.getUserRequest().getDateCIN() != null ? java.sql.Date.valueOf(request.getUserRequest().getDateCIN()) : null
        );

        // Appel du service societe
        societeService.create(societeGlobal);

        return userId;
    }

    /*** Création utilisateur website (Pending) ***/
    public String createPendingWebsiteUser(UserResquestWebsiteDTO dto) {
        String token = tokenService.getAccessToken();
        HttpHeaders headers = createHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> userPayload = Map.of(
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
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(userPayload, headers), Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Erreur lors de la création de l'utilisateur");
        }

        String location = response.getHeaders().getFirst("Location");
        if (location == null) throw new RuntimeException("Impossible de récupérer l'ID de l'utilisateur");
        String userId = location.substring(location.lastIndexOf("/") + 1);

        // Ajouter au groupe Pending
        addUserToGroup(userId, gpWebsitePending, headers);
        logger.info("Utilisateur {} créé en attente de validation", dto.getUsername());

        return userId;
    }

    /*** Validation par l'admin (Activation + déplacement groupe) ***/
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

        // Activer le compte
        enableUser(userId, true, headers);

        // Générer mot de passe temporaire
        String tempPassword = updatePasswordForAdmin(userId);

        UserWebsiteDTO dto = getUserDetails(userId, headers);
        dto.setGenericPassword(tempPassword);
        return dto;
    }

    /*** Récupération utilisateurs par groupe ***/
    public List<UserWebsiteDTO> getPendingUsers() {
        return getUsersByGroup(gpWebsitePending);
    }

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

        return rawUsers.stream()
                .map(map -> mapToUserWebsiteDTO(map))
                .toList();
    }

    /*** Utilitaires généraux ***/
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

    /*** Mot de passe temporaire + détails utilisateur ***/
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

    public String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        StringBuilder sb = new StringBuilder(12);
        SecureRandom random = new SecureRandom();
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
        dto.setCreatedTimestamp(((Number) map.get("createdTimestamp")).longValue());

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
}
