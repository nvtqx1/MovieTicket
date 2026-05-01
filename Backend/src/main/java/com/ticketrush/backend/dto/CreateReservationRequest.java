package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO để tạo đơn đặt vé mới (Init Reservation).
 * Được gọi trước khi thanh toán, chỉ tạo đơn PENDING.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {

    /**
     * ID của suất chiếu
     * Ví dụ: 5
     */
    private Long showtimeId;

    /**
     * Danh sách mã ghế được đặt
     * Ví dụ: ["A1", "A2", "A3"]
     */
    private List<String> seatNumbers;

    /**
     * Optional voucher code
     * Ví dụ: "SUMMER20"
     */
    private String voucherCode;
}

