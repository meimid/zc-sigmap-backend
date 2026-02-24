package com.sigmap.common.notification;

import com.sigmap.common.notification.dto.NotificationRequest;
import com.sigmap.common.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Global notification service. Inject this into any service across any module
 * to send real-time, persisted notifications to users.
 */
public interface NotificationService {

    /**
     * Persist a notification and broadcast it to the recipient via WebSocket.
     *
     * @param request the notification payload
     * @return the persisted notification
     */
    NotificationResponse send(NotificationRequest request);

    /**
     * Send the same notification to multiple users.
     *
     * @param userIds recipient Keycloak IDs
     * @param request the notification payload (userId field is ignored; each ID
     *                from the list is used)
     * @return list of persisted notifications
     */
    List<NotificationResponse> sendToMultipleUsers(List<String> userIds, NotificationRequest request);

    /**
     * Paginated list of notifications for a user, newest first.
     */
    Page<NotificationResponse> getNotifications(String userId, Pageable pageable);

    /**
     * Number of unread notifications for the given user.
     */
    long getUnreadCount(String userId);

    /**
     * Mark a single notification as read.
     */
    NotificationResponse markAsRead(Long notificationId);

    /**
     * Mark all notifications as read for the given user.
     */
    void markAllAsRead(String userId);

    /**
     * Soft-delete a notification.
     */
    void deleteNotification(Long notificationId);
}
