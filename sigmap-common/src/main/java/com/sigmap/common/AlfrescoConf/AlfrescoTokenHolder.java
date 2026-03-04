package com.sigmap.common.AlfrescoConf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 500;

    // Lock non-bloquant : un seul thread authentifie, les autres attendent sans bloquer
    private final ReentrantLock authLock = new ReentrantLock();

    private final RestTemplate restTemplate;

    public AlfrescoTokenHolder(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        //  Configuration timeout
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5s
        factory.setReadTimeout(5000);    // 5s
        this.restTemplate.setRequestFactory(factory);
    }

    public String getTicket() {
        // Double-checked locking : évite le goulot d'étranglement du synchronized
        if (isTicketValid()) {
            log.debug("Using existing Alfresco ticket");
            return ticket;
        }

        // Un seul thread à la fois peut renouveler le ticket
        if (authLock.tryLock()) {
            try {
                // Re-vérifier après acquisition du lock (un autre thread a peut-être déjà authentifié)
                if (!isTicketValid()) {
                    authenticateWithRetry();
                }
            } finally {
                authLock.unlock();
            }
        } else {
            // Un autre thread est en train d'authentifier → attendre qu'il finisse
            waitForValidTicket();
        }

        if (ticket == null) {
            throw new RuntimeException("Alfresco authentication failed after retries. Service unavailable.");
        }

        return ticket;
    }

    private boolean isTicketValid() {
        return ticket != null && System.currentTimeMillis() < expirationTime;
    }

    private void waitForValidTicket() {
        long deadline = System.currentTimeMillis() + 5000; // attendre max 5s
        while (!isTicketValid() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Alfresco ticket");
            }
        }
    }

    private void authenticateWithRetry() {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                log.info("Alfresco authentication attempt {}/{}", attempt, MAX_RETRY);
                doAuthenticate();
                log.info("Alfresco ticket obtained successfully on attempt {}", attempt);
                return; // succès
            } catch (ResourceAccessException e) {
                // Hôte inaccessible → retry avec backoff
                lastException = e;
                log.warn("Alfresco host unreachable (attempt {}/{}): {}", attempt, MAX_RETRY, e.getMessage());
            } catch (HttpServerErrorException e) {
                // Erreur 5xx côté Alfresco → retry
                lastException = e;
                log.warn("Alfresco server error {} (attempt {}/{})", e.getStatusCode(), attempt, MAX_RETRY);
            } catch (HttpClientErrorException e) {
                // Erreur 4xx (mauvais credentials, etc.) → inutile de retry
                log.error("Alfresco authentication failed with client error: {}", e.getStatusCode());
                throw new RuntimeException("Invalid Alfresco credentials or configuration", e);
            }

            if (attempt < MAX_RETRY) {
                long delay = RETRY_DELAY_MS * attempt; // backoff exponentiel simple
                log.info("Retrying in {}ms...", delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted");
                }
            }
        }

        // Tous les retries ont échoué → invalider le ticket
        log.error("All {} authentication attempts failed. Last error: {}", MAX_RETRY,
                lastException != null ? lastException.getMessage() : "unknown");
        ticket = null;
        expirationTime = 0;
    }

    private void doAuthenticate() {
        String url = alfrescoUrl + "/api/-default-/public/authentication/versions/1/tickets";

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("userId", username);
        credentials.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(credentials, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        Map<String, Object> body = response.getBody();
        if (body == null || body.get("entry") == null) {
            throw new RuntimeException("Invalid authentication response from Alfresco");
        }

        Map<String, Object> entry = (Map<String, Object>) body.get("entry");
        String newTicket = (String) entry.get("id");

        if (newTicket == null || newTicket.isBlank()) {
            throw new RuntimeException("Empty ticket received from Alfresco");
        }

        this.ticket = newTicket;
        this.expirationTime = System.currentTimeMillis() + (1000 * 60 * 23); // 23 min (marge de sécurité)
    }

    // Forcer le renouvellement si on reçoit un 401 ailleurs dans l'app
    public void invalidateTicket() {
        log.warn("Alfresco ticket manually invalidated (likely 401 received)");
        this.ticket = null;
        this.expirationTime = 0;
    }
}