package com.sigmap.common.AlfrescoConf;

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
public class AlfrescoTokenHolder {

    // Ticket d'authentification et expiration en mémoire
    private static String ticket;
    private static long expirationTime;

    // URL d'Alfresco et informations d'authentification provenant du fichier de configuration
    @Value("${alfresco.url}")
    private String alfrescoUrl;

    @Value("${alfresco.username}")
    private String username;

    @Value("${alfresco.password}")
    private String password;

    // RestTemplate pour effectuer des requêtes HTTP vers Alfresco
    private final RestTemplate restTemplate;

    /**
     * Constructeur qui injecte RestTemplate pour effectuer des appels HTTP.
     *
     * @param restTemplate Le RestTemplate utilisé pour effectuer des requêtes HTTP
     */
    public AlfrescoTokenHolder(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Méthode pour récupérer le ticket d'authentification.
     * Si le ticket est expiré ou non encore généré, il sera automatiquement rafraîchi en appelant la méthode `authenticate()`.
     *
     * @return Le ticket d'authentification valide
     */
    public synchronized String getTicket() {
        // Vérifier si le ticket est nul ou expiré
        if (ticket == null || System.currentTimeMillis() > expirationTime) {
            authenticate(); // Rafraîchir le ticket si nécessaire
        }
        return ticket;
    }

    /**
     * Méthode privée pour obtenir un nouveau ticket d'authentification depuis Alfresco.
     * Cette méthode envoie une requête POST à l'API d'authentification d'Alfresco pour récupérer un ticket.
     */
    private void authenticate() {
        // URL d'authentification Alfresco pour récupérer le ticket
        String url = alfrescoUrl + "/api/-default-/public/authentication/versions/1/tickets";

        // Créer le corps de la requête avec les informations d'identification
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("userId", username);
        credentials.put("password", password);

        // Définir les en-têtes HTTP de la requête (ici JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Créer l'entité HTTP avec le corps et les en-têtes
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(credentials, headers);

        // Effectuer la requête POST pour obtenir le ticket d'authentification
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        Map<String, Object> body = response.getBody();

        // Vérifier si la réponse est valide
        if (body == null || body.get("entry") == null) {
            // Si la réponse est invalide ou le ticket est absent, lever une exception
            throw new RuntimeException("Failed to authenticate with Alfresco");
        }

        // Extraire le ticket d'authentification de la réponse
        Map<String, Object> entry = (Map<String, Object>) body.get("entry");
        ticket = (String) entry.get("id");

        // Définir l'heure d'expiration du ticket (30 minutes après la création)
        expirationTime = System.currentTimeMillis() + (1000 * 60 * 30); // 30 minutes
    }
}
