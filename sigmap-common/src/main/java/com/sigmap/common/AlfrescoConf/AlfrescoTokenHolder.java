package com.sigmap.common.AlfrescoConf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsable de la gestion du ticket d'authentification pour Alfresco.
 * Cette classe est utilisée pour obtenir un ticket d'authentification valide pour effectuer des appels API sécurisés vers Alfresco.
 * Le ticket est rafraîchi automatiquement si nécessaire (par exemple, lorsqu'il a expiré).
 */
@Component
@Slf4j
public class AlfrescoTokenHolder {

    private String ticket;
    private long expirationTime;

    @Value("${alfresco.url}")
    private String alfrescoUrl;

    @Value("${alfresco.username}")
    private String username;

    @Value("${alfresco.password}")
    private String password;

    private final RestTemplate restTemplate;

    public AlfrescoTokenHolder(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public synchronized String getTicket() {
        if (ticket == null || System.currentTimeMillis() > expirationTime) {
            log.warn("Alfresco ticket expired or null. Re-authenticating...");
            authenticate();
        } else {
            log.debug("Using existing Alfresco ticket");
        }
        return ticket;
    }

    public synchronized void authenticate() {
        try {
            String url = alfrescoUrl + "/api/-default-/public/authentication/versions/1/tickets";

            Map<String, Object> credentials = new HashMap<>();
            credentials.put("userId", username);
            credentials.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(credentials, headers);

            log.info("Authenticating to Alfresco...");

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            Map<String, Object> body = response.getBody();

            if (body == null || body.get("entry") == null) {
                log.error("Invalid authentication response from Alfresco");
                throw new RuntimeException("Failed to authenticate with Alfresco");
            }

            Map<String, Object> entry = (Map<String, Object>) body.get("entry");
            ticket = (String) entry.get("id");

            expirationTime = System.currentTimeMillis() + (1000 * 60 * 25); // 25 min (safe)

            log.info("New Alfresco ticket generated successfully");

        } catch (Exception e) {
            log.error("Error while authenticating to Alfresco", e);
            throw e;
        }
    }
}