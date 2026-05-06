package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockResponse {

    private Long seatId;
    private Long showtimeId;
    private String seatNumber;
    private Long userId;
    private Long reservationId;
    private Long ttlSeconds;
    private LocalDateTime lockedUntil;
    private String redisKey;
    private String status;
    private String message;
}
