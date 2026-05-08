package com.ticketrush.backend.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketrush.backend.dto.SeatStatusPayload;
import com.ticketrush.backend.service.QueueTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueConsumerWorker {

    private final ObjectMapper objectMapper;
    private final QueueTokenService queueTokenService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Lắng nghe từ Topic ticket_requests
     * containerFactory = "kafkaListenerContainerFactory" đã được cấu hình trong KafkaConfig
     * để lấy tối đa 50 messages/lần (MAX_POLL_RECORDS)
     */
    @KafkaListener(
            topics = "${ticketrush.queue.topic:ticket_requests}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTicketRequest(String message) {
        try {
            // 1. Đọc message
            JsonNode payload = objectMapper.readTree(message);
            Long userId = payload.get("userId").asLong();
            Long showtimeId = payload.get("showtimeId").asLong();

            log.info("Processing queue request for user {} showtime {}", userId, showtimeId);

            // 2. Sinh Token và lưu DB + Redis
            String queueToken = queueTokenService.generateAndSaveQueueToken(userId, showtimeId);

            // 3. Broadcast qua WebSocket báo cho FE biết User này đã tới lượt
            // FE sẽ lắng nghe ở /topic/queue/{userId}
            String destination = "/topic/queue/" + userId;
            
            // Tận dụng SeatStatusPayload (hoặc tạo DTO mới) để gửi thông báo
            SeatStatusPayload response = SeatStatusPayload.builder()
                    .showtimeId(showtimeId)
                    .status("YOUR_TURN")
                    .description(queueToken) // Gửi token về cho FE
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend(destination, response);
            log.info("Granted queue access to user {} for showtime {}", userId, showtimeId);

            // 4. Giả lập xử lý chậm (Tùy chọn: giúp điều tiết tốc độ nhả token)
            // Thread.sleep(20);

        } catch (Exception e) {
            log.error("Error processing queue message: {}", message, e);
        }
    }
}
