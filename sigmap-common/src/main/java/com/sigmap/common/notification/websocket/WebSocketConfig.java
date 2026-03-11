package com.sigmap.common.notification.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * Global WebSocket configuration using STOMP over SockJS.
 *
 * <p>
 * This configuration is placed in {@code sigmap-common} and is automatically
 * picked up
 * by every microservice module via {@code @ComponentScan("com.sigmap.common")}.
 * </p>
 *
 * <h3>Client connection example (JavaScript / SockJS + STOMP):</h3>
 * 
 * <pre>{@code
 * const socket = new SockJS('/dac/ws');
 * const stompClient = Stomp.over(socket);
 * stompClient.connect(
 *     { Authorization: 'Bearer ' + jwtToken },
 *     (frame) => {
 *         stompClient.subscribe('/user/queue/notifications', (msg) => {
 *             const event = JSON.parse(msg.body);
 *             console.log('Notification:', event);
 *         });
 *     }
 * );
 * }</pre>
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;
    private final List<String> allowedOrigins;

    public WebSocketConfig(
            JwtDecoder jwtDecoder,
            @Value("${cors.origins}") List<String> allowedOrigins) {
        this.jwtDecoder = jwtDecoder;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker for /topic (broadcast) and /queue (user-specific)
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for @MessageMapping-annotated methods
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix for user-targeted messages (convertAndSendToUser)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Authenticate STOMP CONNECT frames using Keycloak JWT
        registration.interceptors(new WebSocketAuthInterceptor(jwtDecoder));
    }
}
