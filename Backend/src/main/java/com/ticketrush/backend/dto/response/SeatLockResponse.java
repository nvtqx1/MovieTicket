package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockResponse {

    private Long showtimeId;
    private Long userId;
    private Long reservationId;
    private List<LockedSeatInfo> lockedSeats;
    private Long ttlSeconds;
    private LocalDateTime lockedUntil;
    private String status;
    private String message;

    /**
     * Thông tin chi tiết từng ghế đã lock
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LockedSeatInfo {
        private Long seatId;
        private String seatNumber;
    }
}
