package com.sigmap.common.AlfrescoConf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
@Slf4j
@Component
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
        //  Configuration timeout
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5s
        factory.setReadTimeout(5000);    // 5s
        this.restTemplate.setRequestFactory(factory);
    }

    public synchronized String getTicket() {
        if (ticket == null || System.currentTimeMillis() > expirationTime) {
            log.warn("Alfresco ticket expired or null. Re-authenticating...");
            authenticateWithRetry();
        } else {
            log.debug("Using existing Alfresco ticket");
        }
        return ticket;
    }

    /**
     * Authentification avec retry réseau
     */
    private void authenticateWithRetry() {
        int maxAttempts = 3;
        long delay = 500; // ms

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                authenticate();
                return; // succès
            } catch (Exception e) {
                log.error("Authentication attempt {}/{} failed", attempt, maxAttempts, e);

                if (attempt == maxAttempts) {
                    log.error("All authentication attempts failed");
                    throw new RuntimeException("Unable to authenticate with Alfresco", e);
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * Authentification simple (1 tentative)
     */
    private void authenticate() {
        String url = alfrescoUrl + "/api/-default-/public/authentication/versions/1/tickets";

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("userId", username);
        credentials.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(credentials, headers);

        log.info("Authenticating to Alfresco...");

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Authentication failed with status: " + response.getStatusCode());
        }

        Map<String, Object> body = response.getBody();

        if (body == null || body.get("entry") == null) {
            throw new RuntimeException("Invalid authentication response from Alfresco");
        }

        Map<String, Object> entry = (Map<String, Object>) body.get("entry");
        ticket = (String) entry.get("id");

        //  marge de sécurité
        expirationTime = System.currentTimeMillis() + (1000 * 60 * 25);

        log.info("New Alfresco ticket generated successfully");
    }
}