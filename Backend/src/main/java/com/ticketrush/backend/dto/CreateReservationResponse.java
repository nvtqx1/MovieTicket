package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho API tạo đơn đặt vé (Create Reservation).
 * Chứa thông tin đơn vừa tạo (trạng thái PENDING).
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationResponse {

    /**
     * ID của đơn đặt vé vừa tạo
     */
    private Long reservationId;

    /**
     * ID của suất chiếu
     */
    private Long showtimeId;

    /**
     * Tên phim
     */
    private String movieName;

    /**
     * Tên rạp
     */
    private String theaterName;

    /**
     * Tên phòng chiếu
     */
    private String roomName;

    /**
     * Danh sách mã ghế được đặt
     */
    private List<String> seatNumbers;

    /**
     * Thời gian suất chiếu
     */
    private LocalDateTime showtimeStartTime;

    /**
     * Tổng giá tiền ban đầu (trước khi áp voucher)
     */
    private BigDecimal totalPrice;

    /**
     * Khoảng giảm nếu có voucher
     */
    private BigDecimal discountAmount;

    /**
     * Giá tiền cuối cùng (sau khi áp voucher)
     */
    private BigDecimal finalPrice;

    /**
     * Tên voucher (nếu có)
     */
    private String voucherCode;

    /**
     * Trạng thái đơn: PENDING, PAID, CANCELLED
     */
    private String status;

    /**
     * Thời gian hết hạn của đơn (sau 15 phút nếu không thanh toán)
     */
    private LocalDateTime expiresAt;

    /**
     * Thông báo kết quả
     */
    private String message;

    /**
     * Trạng thái API: SUCCESS, FAILED
     */
    private String apiStatus;
}

