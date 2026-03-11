package com.sigmap.common.notification.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;

/**
 * Intercepts STOMP {@code CONNECT} frames to authenticate WebSocket sessions
 * using the Keycloak JWT provided in the {@code Authorization} header.
 *
 * <p>
 * The decoded JWT subject (Keycloak user ID) is set as the session's
 * {@link java.security.Principal}, enabling user-targeted messaging via
 * {@link org.springframework.messaging.simp.SimpMessagingTemplate#convertAndSendToUser}.
 * </p>
 */
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            if (token != null) {
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    String userId = jwt.getSubject();
                    accessor.setUser(new StompPrincipal(userId));
                    log.info("WebSocket CONNECT authenticated for user: {}", userId);
                } catch (Exception e) {
                    log.error("WebSocket CONNECT authentication failed: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid or expired token", e);
                }
            } else {
                log.warn("WebSocket CONNECT received without Authorization token");
            }
        }

        return message;
    }

    /**
     * Extracts a Bearer token from either the STOMP {@code Authorization} header
     * or the {@code token} native header (useful when passing via query param
     * fallback).
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // Try standard Authorization header
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return authHeader;
        }

        // Fallback: token header (for clients that pass JWT as a custom header)
        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }

        return null;
    }
}
