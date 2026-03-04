package com.sigmap.common.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigmap.common.notification.dto.NotificationEvent;
import com.sigmap.common.notification.dto.NotificationRequest;
import com.sigmap.common.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String WS_DESTINATION = "/queue/notifications";

    // ───────────────────────────── Send ──────────────────────────────

    @Override
    @Transactional
    public NotificationResponse send(NotificationRequest request) {
        Notification notification = buildEntity(request);
        Notification saved = notificationRepository.save(notification);
        log.info("Notification saved [id={}, user={}, source={}, type={}]",
                saved.getId(), saved.getUserId(), saved.getSource(), saved.getType());

        NotificationResponse response = toResponse(saved);
        broadcastToUser(saved.getUserId(), NotificationEvent.newNotification(
                response, getUnreadCount(saved.getUserId())));
        return response;
    }

    @Override
    @Transactional
    public List<NotificationResponse> sendToMultipleUsers(List<String> userIds, NotificationRequest request) {
        return userIds.stream()
                .map(userId -> {
                    request.setUserId(userId);
                    return send(request);
                })
                .collect(Collectors.toList());
    }

    // ──────────────────────────── Query ──────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(String userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    // ──────────────────────────── Update ─────────────────────────────

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);

        NotificationResponse response = toResponse(saved);
        broadcastToUser(saved.getUserId(), NotificationEvent.readNotification(
                response, getUnreadCount(saved.getUserId())));
        return response;
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        int updated = notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read for user {}", updated, userId);
        broadcastToUser(userId, NotificationEvent.readAll(0L));
    }

    // ──────────────────────────── Delete ─────────────────────────────

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setIsDeleted(true);
        notification.setDeletedAt(Calendar.getInstance());
        notificationRepository.save(notification);
        log.info("Soft-deleted notification [id={}]", notificationId);

        broadcastToUser(notification.getUserId(), NotificationEvent.deleted(
                notificationId, getUnreadCount(notification.getUserId())));
    }

    // ─────────────────────── Private helpers ─────────────────────────

    private Notification buildEntity(NotificationRequest request) {
        return Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .source(request.getSource())
                .type(request.getType())
                .priority(request.getPriority() != null ? request.getPriority() : NotificationPriority.NORMAL)
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .actionUrl(request.getActionUrl())
                .metadata(serializeMetadata(request.getMetadata()))
                .build();
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .title(n.getTitle())
                .message(n.getMessage())
                .source(n.getSource())
                .type(n.getType())
                .priority(n.getPriority())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .actionUrl(n.getActionUrl())
                .metadata(n.getMetadata())
                .createdAt(n.getCreatedAt())
                .createdBy(n.getCreatedBy())
                .build();
    }

    private void broadcastToUser(String userId, NotificationEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(userId, WS_DESTINATION, event);
            log.debug("WebSocket event [type={}, user={}]", event.getEventType(), userId);
        } catch (Exception e) {
            // WebSocket delivery is best-effort; the notification is already persisted.
            log.warn("Failed to send WebSocket event to user {}: {}", userId, e.getMessage());
        }
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize notification metadata: {}", e.getMessage());
            return null;
        }
    }
}
