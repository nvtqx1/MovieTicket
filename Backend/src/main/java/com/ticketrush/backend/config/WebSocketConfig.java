package com.ticketrush.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket cho hệ thống phát tín hiệu realtime về tình trạng ghế
 * 
 * Kích hoạt STOMP messaging qua WebSocket với SockJS fallback
 * - Endpoint: /ws
 * - Topic prefix: /topic
 * - Application prefix: /app
 * 
 * Spring's autoconfiguration sẽ tự động tạo SimpMessagingTemplate bean
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Đăng ký các STOMP endpoints với SockJS fallback
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Cấu hình message broker để xử lý các đích /topic
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt simple in-memory message broker cho các đích /topic và /queue
        config.enableSimpleBroker("/topic", "/queue");
        
        // Các tuyến đến @MessageMapping methods được tiền tố /app
        config.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix cho tin nhắn user-specific
        config.setUserDestinationPrefix("/user");
    }
}
