package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho API chốt đơn (Confirm Reservation).
 * Chứa thông tin đơn đặt sau khi xác nhận thanh toán.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    /**
     * ID đơn đặt vé
     * Ví dụ: 123
     */
    private Long reservationId;

    /**
     * ID suất chiếu
     * Ví dụ: 1
     */
    private Long showtimeId;

    /**
     * ID người dùng
     * Ví dụ: 456
     */
    private Long userId;

    /**
     * Tên phim
     * Ví dụ: "Avengers: Endgame"
     */
    private String movieName;

    /**
     * Rạp chiếu
     * Ví dụ: "CGV Hồ Tây"
     */
    private String theaterName;

    /**
     * Phòng chiếu
     * Ví dụ: "P1"
     */
    private String roomName;

    /**
     * Danh sách mã ghế
     * Ví dụ: ["A1", "A2", "A3"]
     */
    private List<String> seatNumbers;

    /**
     * Thời gian suất chiếu
     * Ví dụ: "2026-05-15 18:00"
     */
    private LocalDateTime showtimeStartTime;

    /**
     * Tổng giá tiền (VND)
     * Ví dụ: 450000
     */
    private BigDecimal totalPrice;

    /**
     * Trạng thái thanh toán
     * Ví dụ: "PAID"
     */
    private String status;

    /**
     * Mã QR code (Base64 String)
     * Dùng để hiển thị vé
     * Ví dụ: "iVBORw0KGgoAAAANSUhEUgAAASwAAASwCAYAAABkW7XS..."
     */
    private String qrCodeBase64;

    /**
     * Data URI của QR code (sẵn sàng dùng trong <img>)
     * Ví dụ: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAASwCAYAAABkW7XS..."
     */
    private String qrCodeDataUri;

    /**
     * Hash của QR code (lưu vào DB)
     * Dùng để xác minh khi quét
     * Ví dụ: "QR_HASH_ABC123XYZ"
     */
    private String qrCodeHash;

    /**
     * Thời gian đơn được chốt
     * ISO 8601 format: "2026-04-30T14:30:45.123456"
     */
    private LocalDateTime confirmedAt;

    /**
     * Mã giao dịch từ hệ thống thanh toán
     * Ví dụ: "TXN_20260430_ABC123XYZ"
     */
    private String transactionCode;

    /**
     * Thông báo chi tiết
     * Ví dụ: "✅ Chốt đơn thành công"
     */
    private String message;

    /**
     * Trạng thái của API call
     * "SUCCESS" hoặc "FAILED"
     */
    private String apiStatus;
}

