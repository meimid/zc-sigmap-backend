package com.sigmap.sigmap_website_auth.keycloak;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.com.Com;
import com.sigmap.common.keycloakAdmin.dto.*;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.keycloakAdmin.service.KeycloakTokenService;
import com.sigmap.common.user_ac.UserAcRepository;
import com.sigmap.common.user_com.UserComRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/keycloak/profile")
@RequiredArgsConstructor
public class KeycloakAccountController {

    private final Logger logger = LoggerFactory.getLogger(KeycloakAccountController.class);

    @Autowired
    private KeycloakAdminService keycloakAdminService;
    @Autowired
    private KeycloakTokenService tokenService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak-admin.server-url}")
    private String keycloakUrl;

    @Value("${keycloak-admin.realm}")
    private String realm;

    @GetMapping
    public UserDTO getCurrentUser(@AuthenticationPrincipal Jwt principal) {
        String userId = principal.getSubject();
        return keycloakAdminService.getUserById(userId);
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody UpdateProfileDTO profileData,
            @AuthenticationPrincipal Jwt principal) {

        String accessToken = tokenService.getAccessToken();
        String userId = principal.getSubject();

        logger.info("Updating profile of user {} with admin token", userId);

        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(buildUserPayload(profileData), headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(keycloakAdminService.getUserById(userId));
        } else {
            logger.error("Failed to update profile: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).build();
        }
    }
    private Map<String, Object> buildUserPayload(UpdateProfileDTO userDTO) {
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", userDTO.getFirstName());
        user.put("lastName", userDTO.getLastName());
        user.put("email", userDTO.getEmail()); // Ajout du champ requis

        Map<String, List<String>> attributes = new HashMap<>();
        if (userDTO.getAddress() != null) {
            attributes.put("address", List.of(userDTO.getAddress()));
        }
        if (userDTO.getMobile() != null) {
            attributes.put("mobile", List.of(userDTO.getMobile()));
        }
        if (userDTO.getFixe() != null) {
            attributes.put("fixe", List.of(userDTO.getFixe()));
        }

        if (!attributes.isEmpty()) {
            user.put("attributes", attributes);
        }

        return user;
    }


    @PutMapping("/password")
    public ResponseEntity<UserDTO> updatePassword(
            @RequestBody UpdatePasswordDTO passwordData,
            @AuthenticationPrincipal Jwt principal) {

        String accessToken = tokenService.getAccessToken();
        String userId = principal.getSubject();

        logger.info("Resetting password of user {} with admin token", userId);

        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";

        Map<String, Object> payload = Map.of(
                "type", "password",
                "value", passwordData.getNewPassword(),
                "temporary", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(keycloakAdminService.getUserById(userId));
        } else {
            logger.error("Failed to reset password: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).build();
        }
    }


}
