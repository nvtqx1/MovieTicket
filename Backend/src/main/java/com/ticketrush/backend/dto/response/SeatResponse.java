package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO để Frontend vẽ sơ đồ ghế
 * 
 * API: GET /v1/showtimes/{id}/seats
 * 
 * Phục vụ cho việc hiển thị danh sách 150 ghế trên UI
 * Mỗi ghế gồm: ID, số ghế, loại ghế, trạng thái đặt
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponse {
    
    /**
     * ID của ghế trong database
     */
    private Long id;
    
    /**
     * Mã ghế: "A1", "A2", "B1", ...
     */
    private String seatNumber;
    
    /**
     * Loại ghế: "NORMAL", "VIP", "COUPLE"
     */
    private String seatType;
    
    /**
     * Trạng thái: true = đã đặt, false = còn trống
     */
    private Boolean isReserved;
    
    /**
     * Giá bán cơ bản của suất chiếu (sau đó nhân với priceMultiplier của seatType)
     */
    private java.math.BigDecimal basePrice;
    
    /**
     * Giá cuối cùng = basePrice * seatType.priceMultiplier
     */
    private java.math.BigDecimal finalPrice;
}

