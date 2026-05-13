package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO từ Payment Gateway (VNPay, MoMo, Stripe)
 * 
 * API: POST /v1/payments/callback (HIDDEN API - chỉ payment gateway gọi)
 * 
 * Đây là callback từ hệ thống thanh toán bên ngoài
 * khi xác nhận tiền đã trừ khỏi tài khoản khách
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackRequest {
    
    /**
     * Mã giao dịch từ payment gateway
     * VD: "VNP_20260430_ABC123" hoặc "MoMo_20260430_XYZ"
     */
    private String transactionCode;
    
    /**
     * ID của Reservation trong hệ thống TicketRush
     * VD: 123
     */
    private Long reservationId;
    
    /**
     * Trạng thái thanh toán từ gateway
     * "SUCCESS", "FAILED", "PENDING"
     */
    private String paymentStatus;
    
    /**
     * Số tiền đã thanh toán (phải khớp với reservation.totalPrice)
     * VD: 450000
     */
    private java.math.BigDecimal amount;
    
    /**
     * Mã provider thanh toán
     * VD: "VNPAY", "MOMO", "STRIPE"
     */
    private String provider;
    
    /**
     * Thông tin bổ sung từ gateway (JSON string hoặc free text)
     */
    private String additionalInfo;
}

