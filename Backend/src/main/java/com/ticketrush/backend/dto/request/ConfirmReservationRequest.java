package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO để chốt đơn đặt vé (Confirm Reservation).
 * Chứa thông tin cần thiết để xác nhận thanh toán và lưu vé.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmReservationRequest {

    /**
     * ID của đơn đặt vé cần chốt
     * Ví dụ: 123
     */
    private Long reservationId;

    /**
     * ID của phương thức thanh toán
     * Ví dụ: 1 (Credit Card), 2 (E-wallet), etc.
     */
    private Long paymentMethodId;

    /**
     * Mã giao dịch từ hệ thống thanh toán
     * Ví dụ: "TXN_20260430_ABC123XYZ"
     */
    private String transactionCode;

    /**
     * Payment provider, for example: VNPAY, MOMO, STRIPE.
     */
    private String provider;

    /**
     * Optional voucher code applied by the customer.
     */
    private String voucherCode;

    /**
     * Danh sách mã ghế được đặt
     * Ví dụ: ["A1", "A2", "A3"]
     */
    private List<String> seatNumbers;

    /**
     * Ghi chú thêm (tuỳ chọn)
     * Ví dụ: "Đã thanh toán qua thẻ Visa"
     */
    private String notes;
}

