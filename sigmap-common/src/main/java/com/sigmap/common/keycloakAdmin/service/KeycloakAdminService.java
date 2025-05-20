package com.sigmap.common.keycloakAdmin.service;

import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.dto.UserResquestDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
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
        String baseUrl = keycloakBaseUrl + "/admin/realms/" + realm;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/users", HttpMethod.GET, entity, List.class);

        List<Map<String, Object>> userList = response.getBody();
        List<UserDTO> dtoList = new ArrayList<>();

        for (Map<String, Object> userMap : userList) {
            UserDTO dto = new UserDTO();
            String userId = (String) userMap.get("id");
            dto.setId(userId);
            dto.setUsername((String) userMap.get("username"));
            dto.setFirstName((String) userMap.get("firstName"));
            dto.setLastName((String) userMap.get("lastName"));
            dto.setEmail((String) userMap.get("email"));
            dto.setEmailVerified((Boolean) userMap.getOrDefault("emailVerified", false));
            dto.setCreatedTimestamp(userMap.get("createdTimestamp") != null ? Long.parseLong(userMap.get("createdTimestamp").toString()) : null);
            dto.setEnabled((Boolean) userMap.getOrDefault("enabled", false));

            // Attributs com et ac
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

            // Appel pour récupérer les roles de l'utilisateur
            String rolesUrl = baseUrl + "/users/" + userId + "/role-mappings/realm";
            ResponseEntity<List> rolesResponse = restTemplate.exchange(rolesUrl, HttpMethod.GET, entity, List.class);
            List<Map<String, Object>> roles = rolesResponse.getBody();

            List<String> roleNames = new ArrayList<>();
            if (roles != null) {
                for (Map<String, Object> role : roles) {
                    roleNames.add((String) role.get("name"));
                }
            }

            dto.setRoles(roleNames);

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
    public List<String> getAllRealmRoleNames() {
        String token = tokenService.getAccessToken();
        String url = String.format("%s/admin/realms/%s/roles", keycloakBaseUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
        List<Map<String, Object>> roles = response.getBody();

        return roles.stream()
                .map(role -> (String) role.get("name"))
                .collect(Collectors.toList());
    }
    public void createUser(UserResquestDTO userDTO) {
        String token = tokenService.getAccessToken();
        String url = String.format("%s/admin/realms/%s/users", keycloakBaseUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> user = new HashMap<>();
        user.put("username", userDTO.getUsername());
        user.put("firstName", userDTO.getFirstName());
        user.put("lastName", userDTO.getLastName());
        user.put("email", userDTO.getEmail());
        user.put("enabled", true);

        // Gérer les attributs "com" et "ac"
        Map<String, List<String>> attributes = new HashMap<>();
        if (userDTO.getCom() != null) {
            attributes.put("com", List.of(userDTO.getCom().toString()));
        }
        if (userDTO.getAc() != null) {
            attributes.put("ac", List.of(userDTO.getAc().toString()));
        }
        if (!attributes.isEmpty()) {
            user.put("attributes", attributes);
        }

        // Set password
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", userDTO.getPassword());
        credentials.put("temporary", true);

        user.put("credentials", List.of(credentials));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(user, headers);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);

        // Récupération de l’ID depuis l’en-tête Location
        URI location = response.getHeaders().getLocation();
        if (location != null) {
            String userId = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);

            // Affectation des rôles si présents
            if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
                assignRolesToUser(userId, userDTO.getRoles());
            }
        }
    }
    public void updateUser(String userId, UserResquestDTO userDTO) {
        String token = tokenService.getAccessToken();
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakBaseUrl, realm, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("username", userDTO.getUsername());
        user.put("firstName", userDTO.getFirstName());
        user.put("lastName", userDTO.getLastName());
        user.put("email", userDTO.getEmail());
        user.put("enabled", true);

        Map<String, List<String>> attributes = new HashMap<>();
        if (userDTO.getCom() != null) attributes.put("com", List.of(userDTO.getCom().toString()));
        if (userDTO.getAc() != null) attributes.put("ac", List.of(userDTO.getAc().toString()));
        if (!attributes.isEmpty()) user.put("attributes", attributes);

        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(user, headers), Void.class);

        // Mise à jour des rôles de manière fine
        if (userDTO.getRoles() != null) {
            updateUserRolesSmart(userId, userDTO.getRoles());
        }
    }
    public void updatePassword(String userId, String newPassword) {
        String token = tokenService.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> passwordPayload = new HashMap<>();
        passwordPayload.put("type", "password");
        passwordPayload.put("temporary", true);
        passwordPayload.put("value", newPassword);

        String url = String.format("%s/admin/realms/%s/users/%s/reset-password", keycloakBaseUrl, realm, userId);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(passwordPayload, headers);

        restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
    }

    private void updateUserRolesSmart(String userId, List<String> targetRoleNames) {
        String token = tokenService.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Récupération des rôles actuels
        String getRolesUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", keycloakBaseUrl, realm, userId);
        ResponseEntity<List> rolesResp = restTemplate.exchange(getRolesUrl, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        List<Map<String, Object>> existingRoles = rolesResp.getBody();

        Set<String> existingRoleNames = new HashSet<>();
        Map<String, Map<String, Object>> roleMapByName = new HashMap<>();
        if (existingRoles != null) {
            for (Map<String, Object> role : existingRoles) {
                String roleName = (String) role.get("name");
                existingRoleNames.add(roleName);
                roleMapByName.put(roleName, role);
            }
        }

        Set<String> newRoles = new HashSet<>(targetRoleNames);

        // Calcul des rôles à ajouter et à supprimer
        Set<String> toAdd = new HashSet<>(newRoles);
        toAdd.removeAll(existingRoleNames);

        Set<String> toRemove = new HashSet<>(existingRoleNames);
        toRemove.removeAll(newRoles);

        // Supprimer les rôles obsolètes
        if (!toRemove.isEmpty()) {
            List<Map<String, Object>> rolesToRemove = toRemove.stream()
                    .map(roleMapByName::get)
                    .filter(Objects::nonNull)
                    .toList();

            restTemplate.exchange(
                    getRolesUrl,
                    HttpMethod.DELETE,
                    new HttpEntity<>(rolesToRemove, headers),
                    Void.class
            );
        }

        // Ajouter les nouveaux rôles
        if (!toAdd.isEmpty()) {
            List<Map<String, Object>> rolesToAdd = new ArrayList<>();
            for (String roleName : toAdd) {
                String roleUrl = String.format("%s/admin/realms/%s/roles/%s", keycloakBaseUrl, realm, roleName);
                ResponseEntity<Map> roleResp = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
                rolesToAdd.add(roleResp.getBody());
            }

            restTemplate.exchange(
                    getRolesUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(rolesToAdd, headers),
                    Void.class
            );
        }
    }



    private void assignRolesToUser(String userId, List<String> roleNames) {
        String token = tokenService.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> rolesToAssign = new ArrayList<>();
        for (String roleName : roleNames) {
            // Get full role info
            String roleUrl = String.format("%s/admin/realms/%s/roles/%s", keycloakBaseUrl, realm, roleName);
            ResponseEntity<Map> roleResp = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            rolesToAssign.add(roleResp.getBody());
        }

        String assignUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", keycloakBaseUrl, realm, userId);
        HttpEntity<List<Map<String, Object>>> assignEntity = new HttpEntity<>(rolesToAssign, headers);
        restTemplate.exchange(assignUrl, HttpMethod.POST, assignEntity, Void.class);
    }
    /*public void updateUserAttribute(String userId, String attributeName, String attributeValue) {
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
    }*/

}