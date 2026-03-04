package com.sigmap.common.notification.dto;

import com.sigmap.common.notification.NotificationPriority;
import com.sigmap.common.notification.NotificationSource;
import com.sigmap.common.notification.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Calendar;

/**
 * Response DTO for notifications returned to the frontend via REST or
 * WebSocket.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private String userId;

    private String title;

    private String message;

    private NotificationSource source;

    private NotificationType type;

    private NotificationPriority priority;

    private Boolean isRead;

    private LocalDateTime readAt;

    private String referenceId;

    private String referenceType;

    private String actionUrl;

    private String metadata;

    private Calendar createdAt;

    private String createdBy;
}
