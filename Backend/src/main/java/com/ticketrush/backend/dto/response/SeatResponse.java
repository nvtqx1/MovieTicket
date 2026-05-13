package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response chứa thông tin ghế theo suất chiếu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponse {
    
    private Long id;
    
    private String seatNumber;
    
    private String seatType;
    
    private Boolean isReserved;
    
    private java.math.BigDecimal basePrice;
    
    private java.math.BigDecimal finalPrice;
}

