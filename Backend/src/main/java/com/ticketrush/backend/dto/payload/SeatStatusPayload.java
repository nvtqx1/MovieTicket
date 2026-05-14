package com.ticketrush.backend.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payload gửi trạng thái ghế realtime qua WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatStatusPayload implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long showtimeId;
    
    private List<String> seatNumbers;
    
    private String status;
    
    private String description;
    
    private LocalDateTime timestamp;
    
    private Long userId;
}

