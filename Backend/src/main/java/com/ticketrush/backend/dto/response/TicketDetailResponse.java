package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO response chứa chi tiết vé để hiển thị hoặc in vé.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailResponse {

    private Long reservationId;
    private String movieTitle;
    private String moviePosterUrl;
    private String movieGenre;
    private String theaterName;
    private String theaterLocation;
    private String roomName;
    private List<SeatDetail> seats;
    private LocalDate showDate;
    private LocalTime showTime;
    private BigDecimal totalPrice;
    private String status;
    private String qrCodeHash;
    private String qrCodeDataUri;

    /**
     * DTO class dùng để truyền dữ liệu trong hệ thống.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatDetail {
        private String seatNumber;
        private String row;
        private String col;
        private String seatType;
    }
}
