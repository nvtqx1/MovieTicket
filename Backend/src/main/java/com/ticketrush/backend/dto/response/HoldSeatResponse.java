package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho API giữ ghế
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldSeatResponse {
    private String apiStatus; // SUCCESS / FAILED
    private String message;
    private Long reservationId;
    private List<String> heldSeats;
    private LocalDateTime expiresAt;
    private long holdDurationSeconds;
}
