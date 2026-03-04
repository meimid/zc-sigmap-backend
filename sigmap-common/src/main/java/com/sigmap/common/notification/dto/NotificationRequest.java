package com.sigmap.common.notification.dto;

import com.sigmap.common.notification.NotificationPriority;
import com.sigmap.common.notification.NotificationSource;
import com.sigmap.common.notification.NotificationType;
import lombok.*;

import java.util.Map;

/**
 * Request DTO used by any service to send a notification.
 *
 * <p>
 * Usage example:
 * 
 * <pre>{@code
 * notificationService.send(NotificationRequest.builder()
 *         .userId(keycloakUserId)
 *         .title("DAC approved")
 *         .message("Your DAC #123 has been approved.")
 *         .source(NotificationSource.DAC)
 *         .type(NotificationType.STATUS_CHANGED)
 *         .priority(NotificationPriority.HIGH)
 *         .referenceId("123")
 *         .referenceType("Dac")
 *         .actionUrl("/dac/123")
 *         .build());
 * }</pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    /** Keycloak user ID of the recipient. */
    private String userId;

    private String title;

    private String message;

    private NotificationSource source;

    private NotificationType type;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    /** Optional: ID of the entity that caused this notification. */
    private String referenceId;

    /** Optional: Simple class name or type of the referenced entity. */
    private String referenceType;

    /** Optional: Frontend deep-link for the notification. */
    private String actionUrl;

    /** Optional: Arbitrary extra data serialised as JSON. */
    private Map<String, Object> metadata;
}
