package com.sigmap.common.keycloakAdmin.service;

import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakAdminService {

    @Autowired
    private KeycloakTokenService tokenService;

    @Value("${keycloak-admin.server-url}")
    private String keycloakBaseUrl;

    @Value("${keycloak-admin.realm}")
    private String realm;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<UserDTO> getAllUsers() {
        String accessToken = tokenService.getAccessToken();
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, List.class);

        List<Map<String, Object>> userList = response.getBody();

        List<UserDTO> dtoList = new ArrayList<>();
        for (Map<String, Object> userMap : userList) {
            UserDTO dto = new UserDTO();
            dto.setId((String) userMap.get("id"));
            dto.setUsername((String) userMap.get("username"));
            dto.setFirstName((String) userMap.get("firstName"));
            dto.setLastName((String) userMap.get("lastName"));
            dto.setEmail((String) userMap.get("email"));
            dto.setEmailVerified((Boolean) userMap.getOrDefault("emailVerified", false));
            dto.setCreatedTimestamp(userMap.get("createdTimestamp") != null ? Long.parseLong(userMap.get("createdTimestamp").toString()) : null);
            dto.setEnabled((Boolean) userMap.getOrDefault("enabled", false));

            // Gérer les attributs
            Map<String, List<String>> attributes = (Map<String, List<String>>) userMap.get("attributes");
            if (attributes != null) {
                List<String> comValues = attributes.get("com");
                List<String> acValues = attributes.get("ac");
                if (comValues != null && !comValues.isEmpty()) {
                    dto.setCom(Integer.parseInt(comValues.get(0)));
                }
                if (acValues != null && !acValues.isEmpty()) {
                    dto.setAc(Integer.parseInt(acValues.get(0)));
                }
            }

            dtoList.add(dto);
        }

        return dtoList;
    }
    public List<UserDTO> getUsersByCom(Integer com) {
        return getAllUsers().stream()
                .filter(user -> user.getCom() != null && user.getCom().equals(com))
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByAc(Integer ac) {
        return getAllUsers().stream()
                .filter(user -> user.getAc() != null && user.getAc().equals(ac))
                .collect(Collectors.toList());
    }
    public List<UserDTO> getUsersWithoutCom() {
        return getAllUsers().stream()
                .filter(user -> user.getCom() == null)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersWithoutAc() {
        return getAllUsers().stream()
                .filter(user -> user.getAc() == null)
                .collect(Collectors.toList());
    }

    public void updateUserAttribute(String userId, String attributeName, String attributeValue) {
        String token = tokenService.getAccessToken();
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakBaseUrl, realm, userId);

        // Get current user
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> user = response.getBody();

        if (user == null) {
            throw new RuntimeException("Impossible de récupérer l'utilisateur avec l'ID : " + userId);
        }

        // Update attributes
        Map<String, List<String>> attributes = (Map<String, List<String>>) user.get("attributes");
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        if (attributeValue == null) {
            attributes.remove(attributeName); // Supprime l'attribut
        } else {
            attributes.put(attributeName, List.of(attributeValue)); // Met à jour ou ajoute l'attribut
        }

        user.put("attributes", attributes);

        // Send PUT update
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(user, headers);
        restTemplate.exchange(url, HttpMethod.PUT, updateEntity, Void.class);
    }

}