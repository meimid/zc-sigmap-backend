package com.sigmap.common.notification;

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Persistent notification entity.
 *
 * <p>
 * Stores every notification sent to a user from any module.
 * Inherits audit fields (createdAt, createdBy, …) from {@link BaseEntity}.
 * </p>
 */
@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_source", columnList = "source"),
        @Index(name = "idx_notification_is_read", columnList = "is_read"),
        @Index(name = "idx_notification_created_at", columnList = "created_at"),
        @Index(name = "idx_notification_user_unread", columnList = "user_id, is_read, is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Keycloak subject (user ID) of the notification recipient. */
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    @Lob
    private String message;

    /** Which module originated this notification. */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private NotificationSource source;

    /** The kind of notification. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Optional reference back to the domain entity that triggered the notification.
     */
    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "reference_type", length = 100)
    private String referenceType;

    /** Deep-link URL the front-end can navigate to. */
    @Column(name = "action_url", length = 1000)
    private String actionUrl;

    /** Free-form JSON metadata. */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
}
