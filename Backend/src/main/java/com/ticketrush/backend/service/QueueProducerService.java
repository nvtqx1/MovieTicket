package com.ticketrush.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Dịch vụ đưa người dùng vào hàng chờ bằng Redis và Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ticketrush.queue.topic:ticket_requests}")
    private String queueTopic;

    private static final String QUEUE_COUNTER_PREFIX = "queue:counter:showtime:";

    /**
     * Tham gia hàng chờ cho 1 suất chiếu
     */
    /**
     * Tăng vị trí hàng chờ trong Redis và gửi sự kiện vào Kafka.
     *
     * @param userId ID người dùng tham gia hàng chờ.
     * @param showtimeId ID suất chiếu cần xếp hàng.
     * @return vị trí hiện tại của người dùng trong hàng chờ.
     * @throws RuntimeException nếu không serialize được payload Kafka.
     */
    public Long joinQueue(Long userId, Long showtimeId) {
        // 1. Tăng counter trên Redis để lấy số thứ tự (queue position)
        String counterKey = QUEUE_COUNTER_PREFIX + showtimeId;
        Long queuePosition = redisTemplate.opsForValue().increment(counterKey);

        // 2. Tạo payload để đẩy vào Kafka
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("showtimeId", showtimeId);
        payload.put("queuePosition", queuePosition);
        payload.put("timestamp", System.currentTimeMillis());

        try {
            String message = objectMapper.writeValueAsString(payload);
            // 3. Đẩy message vào Kafka
            kafkaTemplate.send(queueTopic, String.valueOf(showtimeId), message);
            log.info("User {} joined queue for showtime {}, position {}", userId, showtimeId, queuePosition);
        } catch (JsonProcessingException e) {
            log.error("Error serializing queue payload for user {} showtime {}", userId, showtimeId, e);
            throw new RuntimeException("Error joining queue", e);
        }

        return queuePosition;
    }
}
