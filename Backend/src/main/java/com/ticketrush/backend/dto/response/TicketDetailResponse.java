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
 * Task 2.3: DTO chi tiết vé - dùng để render UI vé giấy và mã hóa QR Code
 * Chứa đầy đủ: Tên phim, Rạp, Phòng, Dãy, Số ghế, Giờ chiếu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailResponse {

    /** ID đơn đặt vé */
    private Long reservationId;

    // ===== THÔNG TIN PHIM =====
    private String movieTitle;
    private String moviePosterUrl;
    private String movieGenre;

    // ===== THÔNG TIN RẠP & PHÒNG =====
    private String theaterName;
    private String theaterLocation;
    private String roomName; // Phòng (Hall)

    // ===== THÔNG TIN GHẾ =====
    /** Danh sách ghế chi tiết: dãy (row) + số ghế (col) */
    private List<SeatDetail> seats;

    // ===== THÔNG TIN SUẤT CHIẾU =====
    private LocalDate showDate;
    private LocalTime showTime;

    // ===== THÔNG TIN THANH TOÁN =====
    private BigDecimal totalPrice;
    private String status;

    // ===== QR CODE =====
    private String qrCodeHash;
    private String qrCodeDataUri;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatDetail {
        /** Mã ghế đầy đủ: VD "A1" */
        private String seatNumber;
        /** Dãy (Row): VD "A" */
        private String row;
        /** Số ghế (Column): VD "1" */
        private String col;
        /** Loại ghế: NORMAL / VIP / COUPLE */
        private String seatType;
    }
}
