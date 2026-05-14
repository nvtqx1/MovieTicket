package com.ticketrush.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket STOMP cho cập nhật realtime.
 *
 * Annotation {@link EnableWebSocketMessageBroker} bật message broker WebSocket;
 * Spring tự tạo {@code SimpMessagingTemplate} để gửi message realtime.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Đăng ký endpoint STOMP cho client kết nối WebSocket.
     *
     * Endpoint {@code /ws} cho phép mọi origin pattern và bật SockJS để fallback
     * khi trình duyệt hoặc môi trường không hỗ trợ WebSocket thuần.
     *
     * @param registry registry dùng để khai báo STOMP endpoint.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Cấu hình message broker và prefix định tuyến STOMP.
     *
     * Broker nội bộ xử lý các destination {@code /topic} và {@code /queue};
     * prefix {@code /app} chuyển message đến các method {@code @MessageMapping};
     * prefix {@code /user} dùng cho message theo từng người dùng.
     *
     * @param config registry dùng để cấu hình message broker.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt simple in-memory message broker cho các đích /topic và /queue.
        config.enableSimpleBroker("/topic", "/queue");

        // Các tuyến đến @MessageMapping methods được tiền tố /app.
        config.setApplicationDestinationPrefixes("/app");

        // User destination prefix cho tin nhắn user-specific.
        config.setUserDestinationPrefix("/user");
    }
}
