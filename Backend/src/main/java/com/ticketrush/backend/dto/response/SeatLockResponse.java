package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO response chứa kết quả khóa ghế.
 */
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
     * DTO con chứa thông tin ghế đã khóa.
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
