package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho API "Vé của tôi" (My Tickets).
 * Hiển thị thông tin vé chi tiết kèm tên phòng chiếu.
 * V3: Bắt buộc phải có roomName để khách biết đường đi.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    /**
     * ID của đơn đặt vé
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
     * Ví dụ: "Beta Cinemas"
     */
    private String theaterName;

    /**
     * Tên phòng chiếu
     * Ví dụ: "IMAX 01"
     */
    private String roomName;

    /**
     * Thông tin đầy đủ: "Rạp: Beta Cinemas - Phòng: IMAX 01"
     * Sử dụng để hiển thị chuyên nghiệp trên FE
     */
    private String location;

    /**
     * Danh sách mã ghế
     */
    private List<String> seatNumbers;

    /**
     * Thời gian suất chiếu
     */
    private LocalDateTime showtimeStartTime;

    /**
     * Tổng giá tiền
     */
    private BigDecimal totalPrice;

    /**
     * Trạng thái: PENDING, PAID, CANCELLED
     */
    private String status;

    /**
     * Ngày đặt vé
     */
    private LocalDateTime reservationTime;

    /**
     * Thoi diem het han giu ghe cua ve dang LOCKED/PENDING.
     */
    private LocalDateTime expiresAt;

    /**
     * Mã QR Hash (nếu đã thanh toán)
     */
    private String qrCodeHash;
}

