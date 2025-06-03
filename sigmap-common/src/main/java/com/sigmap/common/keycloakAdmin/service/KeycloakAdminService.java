package com.sigmap.common.keycloakAdmin.service;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.agpm.exceptions.AgpmDefaultStatus;
import com.sigmap.common.com.Com;
import com.sigmap.common.config.Constants;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.dto.UserResquestDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakTokenService;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.user_ac.UserAcRepository;
import com.sigmap.common.user_com.UserCom;
import com.sigmap.common.user_com.UserComRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakAdminService {
    private final Logger logger = LoggerFactory.getLogger(KeycloakAdminService.class);

    @Autowired
    private KeycloakTokenService tokenService;

    @Value("${keycloak-admin.server-url}")
    private String keycloakBaseUrl;

    @Value("${keycloak-admin.realm}")
    private String realm;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserAcRepository userAcRepository;
    @Autowired
    private UserComRepository userComRepository;

    @Autowired
    private RefParamRepository refParamRepository;

    public List<UserDTO> getAllUsers() {
        String accessToken = tokenService.getAccessToken();
        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String baseUrl = keycloakBaseUrl + "/admin/realms/" + realm;

        List<UserDTO> users = fetchUsersFromKeycloak(baseUrl, entity);

        if (users.isEmpty()) {
            return Collections.emptyList();
        }


        return users;
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private List<UserDTO> fetchUsersFromKeycloak(String baseUrl, HttpEntity<Void> entity) {
        String url = baseUrl + "/users";
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        List<Map<String, Object>> rawUsers = response.getBody();
        if (rawUsers == null) return Collections.emptyList();

        return rawUsers.stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    private UserDTO mapToUserDTO(Map<String, Object> userMap) {
        UserDTO dto = new UserDTO();
        dto.setId((String) userMap.get("id"));
        dto.setUsername((String) userMap.get("username"));
        dto.setFirstName((String) userMap.get("firstName"));
        dto.setLastName((String) userMap.get("lastName"));
        dto.setEmail((String) userMap.get("email"));
        dto.setAddress((String) userMap.get("address"));
        dto.setEmailVerified((Boolean) userMap.getOrDefault("emailVerified", false));
        dto.setCreatedTimestamp(userMap.get("createdTimestamp") != null ?
                Long.parseLong(userMap.get("createdTimestamp").toString()) : null);
        dto.setEnabled((Boolean) userMap.getOrDefault("enabled", false));
        return dto;
    }

    public UserDTO getUserById(String userId) {
        HttpEntity<Void> entity = createHttpEntityWithToken();

        Map<String, Object> userMap = fetchUserData(userId, entity);
        if (userMap == null || userMap.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID : " + userId);
        }

        UserDTO userDTO = mapToUserDTO(userMap);

        userDTO.setRoles(fetchRolesForUser(userId, entity));
        userDTO.setAcs(fetchAcsForUser(userId));
        userDTO.setComs(fetchComsForUser(userId));

        return userDTO;
    }

    private HttpEntity<Void> createHttpEntityWithToken() {
        String accessToken = tokenService.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(headers);
    }

    private Map<String, Object> fetchUserData(String userId, HttpEntity<Void> entity) {
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/users/" + userId;
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }



    private List<String> fetchRolesForUser(String userId, HttpEntity<Void> entity) {
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            List<Map<String, Object>> roles = response.getBody();
            if (roles == null) return Collections.emptyList();

            return roles.stream()
                    .map(role -> (String) role.get("name"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.warn("Failed to fetch roles for user: " + userId, e);
            return Collections.emptyList();
        }
    }

    private List<Ac> fetchAcsForUser(String userId) {
        return userAcRepository.findAllByUserIdAndIsDeletedFalse(userId).stream()
                .map(UserAc::getAc)
                .collect(Collectors.toList());
    }

    private List<Com> fetchComsForUser(String userId) {
        return userComRepository.findAllByUserIdAndIsDeletedFalse(userId).stream()
                .map(UserCom::getCom)
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
    public UserDTO createUser(UserResquestDTO userDTO) {
        String token = tokenService.getAccessToken();
        String url = String.format("%s/admin/realms/%s/users", keycloakBaseUrl, realm);

        HttpHeaders headers = buildHeaders(token);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(buildUserPayload(userDTO), headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new RuntimeException("Erreur lors de la création de l'utilisateur : ID introuvable");
        }

        String userId = extractUserIdFromLocation(location);

        if (!CollectionUtils.isEmpty(userDTO.getRoles())) {
            assignRolesToUser(userId, userDTO.getRoles());
        }

        linkUserToEntities(userId, userDTO.getAcIds(),userDTO.getAcPiloteId(), true);
        linkUserToEntities(userId, userDTO.getComIds(), null,false);

        return getUserById(userId);

    }
    public UserDTO updateUser(String userId, UserResquestDTO userDTO) throws AgpmDefaultStatus {
        String token = tokenService.getAccessToken();
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakBaseUrl, realm, userId);

        HttpHeaders headers = buildHeaders(token);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(buildUserPayload(userDTO), headers);

        restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

        if (userDTO.getRoles() != null) {
            updateUserRolesSmart(userId, userDTO.getRoles());
        }

        updateLinkedEntities(userId, userDTO.getAcIds(),userDTO.getAcPiloteId(), true);
        updateLinkedEntities(userId, userDTO.getComIds(), null, false);

        return getUserById(userId);

    }
    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Map<String, Object> buildUserPayload(UserResquestDTO userDTO) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", userDTO.getUsername());
        user.put("firstName", userDTO.getFirstName());
        user.put("lastName", userDTO.getLastName());
        user.put("email", userDTO.getEmail());
        user.put("enabled", userDTO.isEnable());
        return user;
    }

    private String extractUserIdFromLocation(URI location) {
        return location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
    }
    private void linkUserToEntities(String userId, List<Integer> ids,Integer acPiloteId, boolean isAc) {
        if (ids == null || ids.isEmpty()) return;

        for (Integer id : ids) {
            try {
                if (isAc) {
                    UserAc userAc = new UserAc();
                    userAc.setUserId(userId);
                    userAc.setAcId(id);
                    userAc.setIsDeleted(false);
                    userAc.setStatus(getDefaultStatus());
                    if (id  == acPiloteId){
                        userAc.setAcPilote(true);
                    }
                    userAcRepository.save(userAc);
                } else {
                    UserCom userCom = new UserCom();
                    userCom.setUserId(userId);
                    userCom.setComId(id);
                    userCom.setIsDeleted(false);
                    userCom.setStatus(getDefaultStatus());
                    userComRepository.save(userCom);
                }
            } catch (Exception e) {
                logger.warn("Impossible d'associer l'utilisateur {} à l'entité {} : {}", userId, id, e.getMessage());
            }
        }
    }

    private void updateLinkedEntities(String userId, List<Integer> newIds,Integer acPiloteId, boolean isAc) throws AgpmDefaultStatus {
        if (newIds == null) return;

        Set<Integer> newIdSet = new HashSet<>(newIds);
        if (isAc) {
            List<UserAc> existing = userAcRepository.findByUserIdAndIsDeletedFalse(userId);
            for (UserAc e : existing) {
                if (!newIdSet.contains(e.getAcId())) {
                    userAcRepository.softDelete(e.getAcId(), userId);
                }
            }
            for (Integer id : newIdSet) {
                if (existing.stream().noneMatch(e -> e.getAcId().equals(id))) {
                    UserAc userAc = new UserAc();
                    userAc.setUserId(userId);
                    userAc.setAcId(id);
                    userAc.setIsDeleted(false);
                    if (id  == acPiloteId){
                        userAc.setAcPilote(true);
                    }
                    userAc.setStatus(getDefaultStatus());
                    userAcRepository.save(userAc);
                }
            }
        } else {
            List<UserCom> existing = userComRepository.findByUserIdAndIsDeletedFalse(userId);
            for (UserCom e : existing) {
                if (!newIdSet.contains(e.getComId())) {
                    userComRepository.softDelete(e.getComId(), userId);
                }
            }
            for (Integer id : newIdSet) {
                if (existing.stream().noneMatch(e -> e.getComId().equals(id))) {
                    UserCom userCom = new UserCom();
                    userCom.setUserId(userId);
                    userCom.setComId(id);
                    userCom.setIsDeleted(false);
                    userCom.setStatus(getDefaultStatus());
                    userComRepository.save(userCom);
                }
            }
        }
    }

    private RefParam getDefaultStatus() throws AgpmDefaultStatus {
        return refParamRepository.findById(Constants.ACTIF_STATUS)
                .orElseThrow(() -> new AgpmDefaultStatus("Default status not found"));
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