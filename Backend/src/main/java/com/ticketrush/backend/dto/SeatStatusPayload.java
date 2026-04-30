package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa payload tín hiệu realtime của tình trạng ghế
 * Gửi qua WebSocket để báo cho toàn bộ client trong rạp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatStatusPayload implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** ID của suất chiếu */
    private Long showtimeId;
    
    /** Danh sách mã ghế (ví dụ: "A1", "A2", "B5") */
    private List<String> seatNumbers;
    
    /** Tình trạng ghế: "LOCKED" (đang khóa), "SOLD" (đã bán), "AVAILABLE" (có sẵn) */
    private String status;
    
    /** Mô tả thêm (tuỳ chọn) */
    private String description;
    
    /** Thời gian gửi tín hiệu */
    private LocalDateTime timestamp;
    
    /** ID của user thực hiện hành động (tuỳ chọn) */
    private Long userId;
}

