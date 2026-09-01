package com.sigmap.sigmap_ppm.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WhatsAppNotificationService {

    private final boolean enabled;
    private final String graphVersion;
    private final String phoneNumberId;
    private final String accessToken;
    private final String decisionTemplateName;
    private final String arrivalTemplateName;
    private final String templateLanguage;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WhatsAppNotificationService(
            @Value("${whatsapp.enabled:false}") boolean enabled,
            @Value("${whatsapp.graph-version:v23.0}") String graphVersion,
            @Value("${whatsapp.phone-number-id:}") String phoneNumberId,
            @Value("${whatsapp.access-token:}") String accessToken,
            @Value("${whatsapp.decision-template-name:notification_decision_ppm}") String decisionTemplateName,
            @Value("${whatsapp.arrival-template-name:notification_arrivee_ppm}") String arrivalTemplateName,
            @Value("${whatsapp.template-language:fr}") String templateLanguage,
            ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.graphVersion = graphVersion;
        this.phoneNumberId = phoneNumberId;
        this.accessToken = accessToken;
        this.decisionTemplateName = decisionTemplateName;
        this.arrivalTemplateName = arrivalTemplateName;
        this.templateLanguage = templateLanguage;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Best-effort notification: a missing phone/configuration or any Meta error is
     * logged and never propagated to the business workflow.
     */
    public void sendDecisionNotification(String phone, String commission, String reference, String decision) {
        sendTemplate(phone, decisionTemplateName, reference, List.of(
                textParameter("commission", valueOrDefault(commission, "Commission")),
                textParameter("ppm_reference", valueOrDefault(reference, "N/A")),
                textParameter("decision", valueOrDefault(decision, "Avis"))
        ));
    }

    public void sendArrivalNotification(String phone, String sender, String reference) {
        sendTemplate(phone, arrivalTemplateName, reference, List.of(
                textParameter("ppm_reference", valueOrDefault(reference, "N/A")),
                textParameter("emetteur", valueOrDefault(sender, "Autorité contractante"))
        ));
    }

    private void sendTemplate(String phone, String templateName, String reference,
                              List<Map<String, String>> parameters) {
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            log.info("[WHATSAPP] Numéro absent, notification ignorée pour le dossier {}", reference);
            return;
        }
        if (!enabled) {
            log.debug("[WHATSAPP] Envoi désactivé, notification ignorée pour le dossier {}", reference);
            return;
        }
        if (isBlank(phoneNumberId) || isBlank(accessToken) || isBlank(templateName)) {
            log.warn("[WHATSAPP] Configuration Meta incomplète, notification ignorée pour le dossier {}", reference);
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "messaging_product", "whatsapp",
                    "to", normalizedPhone,
                    "type", "template",
                    "template", Map.of(
                            "name", templateName,
                            "language", Map.of("code", templateLanguage),
                            "components", List.of(Map.of(
                                    "type", "body",
                                    "parameters", parameters
                            ))
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://graph.facebook.com/" + graphVersion + "/" + phoneNumberId + "/messages"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[WHATSAPP] Notification envoyée au {} pour le dossier {}", masked(normalizedPhone), reference);
            } else {
                log.warn("[WHATSAPP] Meta a refusé la notification du dossier {} (HTTP {}): {}",
                        reference, response.statusCode(), response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[WHATSAPP] Envoi interrompu pour le dossier {}", reference);
        } catch (Exception e) {
            log.warn("[WHATSAPP] Échec non bloquant pour le dossier {}: {}", reference, e.getMessage());
        }
    }

    private Map<String, String> textParameter(String parameterName, String value) {
        return Map.of("type", "text", "parameter_name", parameterName, "text", value);
    }

    private String normalizePhone(String phone) {
        if (isBlank(phone)) return null;
        String normalized = phone.trim().replaceAll("[^0-9]", "");
        if (normalized.isBlank()) return null;
        if (normalized.length() == 8) return "222" + normalized;
        if (normalized.startsWith("00")) return normalized.substring(2);
        return normalized;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String masked(String phone) {
        return phone.length() <= 4 ? "****" : "***" + phone.substring(phone.length() - 4);
    }
}
