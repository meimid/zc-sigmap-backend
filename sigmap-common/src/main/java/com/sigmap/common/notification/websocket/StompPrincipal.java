package com.sigmap.common.notification.websocket;

import java.security.Principal;

/**
 * Lightweight {@link Principal} implementation for STOMP WebSocket sessions.
 * The {@link #getName()} value is the Keycloak user ID (JWT subject),
 * which is used by Spring's user-destination mechanism to route messages
 * to the correct connected user.
 */
public class StompPrincipal implements Principal {

    private final String userId;

    public StompPrincipal(String userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId;
    }
}
