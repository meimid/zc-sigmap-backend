package com.sigmap.sigmap_params.notification;

import com.sigmap.common.notification.NotificationService;
import com.sigmap.common.notification.dto.NotificationResponse;
import com.sigmap.common.security.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoints for managing the current user's notifications.
 *
 * <p>
 * All endpoints use the authenticated Keycloak user ID from the JWT. No admin
 * override is provided — a user can only access their own notifications.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get the current user's notifications (paginated, newest first).
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.getNotifications(userId, pageable));
    }

    /**
     * Get the number of unread notifications.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        String userId = getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark a single notification as read.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /**
     * Mark all notifications as read for the current user.
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        String userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Soft-delete a notification.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    // ──────────────────────── Private helper ─────────────────────────

    private String getCurrentUserId() {
        return Authentication.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
    }
}
