package com.sigmap.common.notification.dto;

import lombok.*;

/**
 * WebSocket event payload. Wraps a {@link NotificationResponse} with an event
 * type
 * so the frontend can distinguish between different kinds of WebSocket
 * messages.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    /**
     * Type of WebSocket event.
     * <ul>
     * <li>{@code NEW} — a new notification arrived</li>
     * <li>{@code READ} — a notification was marked as read</li>
     * <li>{@code READ_ALL} — all notifications were marked as read</li>
     * <li>{@code DELETED} — a notification was deleted</li>
     * <li>{@code COUNT_UPDATE} — only the unread count changed</li>
     * </ul>
     */
    private String eventType;

    /** The notification data (may be null for COUNT_UPDATE events). */
    private NotificationResponse notification;

    /** Current unread count after this event. */
    private Long unreadCount;

    /* ---- Convenience factory methods ---- */

    public static NotificationEvent newNotification(NotificationResponse response, long unreadCount) {
        return NotificationEvent.builder()
                .eventType("NEW")
                .notification(response)
                .unreadCount(unreadCount)
                .build();
    }

    public static NotificationEvent readNotification(NotificationResponse response, long unreadCount) {
        return NotificationEvent.builder()
                .eventType("READ")
                .notification(response)
                .unreadCount(unreadCount)
                .build();
    }

    public static NotificationEvent readAll(long unreadCount) {
        return NotificationEvent.builder()
                .eventType("READ_ALL")
                .unreadCount(unreadCount)
                .build();
    }

    public static NotificationEvent deleted(Long notificationId, long unreadCount) {
        return NotificationEvent.builder()
                .eventType("DELETED")
                .notification(NotificationResponse.builder().id(notificationId).build())
                .unreadCount(unreadCount)
                .build();
    }
}
