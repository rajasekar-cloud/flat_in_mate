package com.flatmate.app.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration("chatWebSocketConfig")
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Clients subscribe to /topic/match/{matchId}, /topic/match/{matchId}/read, etc.
        config.enableSimpleBroker("/topic");
        // Client sends messages to /app/chat, /app/chat/typing
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Native WebSocket endpoint — used by mobile apps / Postman
        registry.addEndpoint("/ws").setAllowedOrigins("*");

        // SockJS fallback endpoint — used by web frontend
        registry.addEndpoint("/chat-socket").setAllowedOrigins("*").withSockJS();
    }

    /**
     * Attach the JWT interceptor so every STOMP CONNECT frame is authenticated
     * before the session is established.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
