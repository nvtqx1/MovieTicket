package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.SeatStatusPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service để gửi tín hiệu realtime về tình trạng ghế qua WebSocket
 * Báo cho toàn bộ user trong rạp biết ghế nào vừa bị đổi màu (đang khóa/đã bán)
 */
@Slf4j
@Service
public class SeatRealtimeService {
    
    /** Inject SimpMessagingTemplate để gửi thông điệp STOMP */
    /** Sử dụng @Autowired với setter injection thay chế constructor injection */
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Broadcast tín hiệu thay đổi tình trạng ghế cho toàn bộ client
     * 
     * @param showtimeId ID của suất chiếu
     * @param seatNumbers Danh sách mã ghế (ví dụ: ["A1", "A2", "B5"])
     * @param status Tình trạng ghế: "LOCKED" (đang khóa), "SOLD" (đã bán), "AVAILABLE" (có sẵn)
     */
    public void broadcastSeatStatus(Long showtimeId, List<String> seatNumbers, String status) {
        try {
            // Đóng gói dữ liệu thành DTO
            SeatStatusPayload payload = SeatStatusPayload.builder()
                    .showtimeId(showtimeId)
                    .seatNumbers(seatNumbers)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Gửi tín hiệu qua WebSocket đến tất cả user subscribe /topic/showtimes/{showtimeId}
            String destination = "/topic/showtimes/" + showtimeId;
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("✅ Phát tín hiệu tình trạng ghế: {} ghế với trạng thái {} cho suất chiếu {}", 
                    seatNumbers.size(), status, showtimeId);
            
        } catch (Exception e) {
            log.error("❌ Lỗi phát tín hiệu tình trạng ghế cho suất chiếu {}: {}", showtimeId, e.getMessage(), e);
        }
    }
    
    /**
     * Broadcast tín hiệu thay đổi tình trạng ghế với mô tả thêm
     * 
     * @param showtimeId ID của suất chiếu
     * @param seatNumbers Danh sách mã ghế
     * @param status Tình trạng ghế
     * @param description Mô tả thêm (tuỳ chọn)
     */
    public void broadcastSeatStatus(Long showtimeId, List<String> seatNumbers, String status, String description) {
        try {
            SeatStatusPayload payload = SeatStatusPayload.builder()
                    .showtimeId(showtimeId)
                    .seatNumbers(seatNumbers)
                    .status(status)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            String destination = "/topic/showtimes/" + showtimeId;
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("✅ Phát tín hiệu tình trạng ghế: {} ghế với trạng thái {} cho suất chiếu {} - {}", 
                    seatNumbers.size(), status, showtimeId, description);
            
        } catch (Exception e) {
            log.error("❌ Lỗi phát tín hiệu tình trạng ghế cho suất chiếu {}: {}", showtimeId, e.getMessage(), e);
        }
    }
    
    /**
     * Broadcast tín hiệu thay đổi tình trạng ghế với thông tin user
     * 
     * @param showtimeId ID của suất chiếu
     * @param seatNumbers Danh sách mã ghế
     * @param status Tình trạng ghế
     * @param userId ID của user thực hiện hành động
     */
    public void broadcastSeatStatus(Long showtimeId, List<String> seatNumbers, String status, Long userId) {
        try {
            SeatStatusPayload payload = SeatStatusPayload.builder()
                    .showtimeId(showtimeId)
                    .seatNumbers(seatNumbers)
                    .status(status)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            String destination = "/topic/showtimes/" + showtimeId;
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("✅ Phát tín hiệu tình trạng ghế: {} ghế với trạng thái {} cho suất chiếu {} bởi user {}", 
                    seatNumbers.size(), status, showtimeId, userId);
            
        } catch (Exception e) {
            log.error("❌ Lỗi phát tín hiệu tình trạng ghế cho suất chiếu {}: {}", showtimeId, e.getMessage(), e);
        }
    }
    
    /**
     * Gửi payload custom thẳng qua WebSocket
     * 
     * @param showtimeId ID của suất chiếu
     * @param payload Payload tùy chỉnh
     */
    public void broadcastCustomPayload(Long showtimeId, SeatStatusPayload payload) {
        try {
            payload.setTimestamp(LocalDateTime.now());
            payload.setShowtimeId(showtimeId);
            
            String destination = "/topic/showtimes/" + showtimeId;
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("✅ Phát payload tùy chỉnh cho suất chiếu {}", showtimeId);
            
        } catch (Exception e) {
            log.error("❌ Lỗi phát payload tùy chỉnh cho suất chiếu {}: {}", showtimeId, e.getMessage(), e);
        }
    }

    /**
     * Broadcast tín hiệu thay đổi tình trạng ghế với description và user ID
     * (Ngày 17-18: Chốt đơn - gọi khi confirm reservation)
     * 
     * @param showtimeId ID của suất chiếu
     * @param seatNumbers Danh sách mã ghế
     * @param status Tình trạng ghế (VD: "SOLD")
     * @param description Mô tả thêm (VD: "Đơn #123")
     * @param userId ID của user thực hiện hành động
     */
    public void broadcastSeatStatus(Long showtimeId, List<String> seatNumbers, String status, String description, Long userId) {
        try {
            SeatStatusPayload payload = SeatStatusPayload.builder()
                    .showtimeId(showtimeId)
                    .seatNumbers(seatNumbers)
                    .status(status)
                    .description(description)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            String destination = "/topic/showtimes/" + showtimeId;
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("✅ Phát tín hiệu tình trạng ghế: {} ghế với trạng thái {} cho suất chiếu {} - {} (user: {})", 
                    seatNumbers.size(), status, showtimeId, description, userId);
            
        } catch (Exception e) {
            log.error("❌ Lỗi phát tín hiệu tình trạng ghế cho suất chiếu {}: {}", showtimeId, e.getMessage(), e);
        }
    }
}
