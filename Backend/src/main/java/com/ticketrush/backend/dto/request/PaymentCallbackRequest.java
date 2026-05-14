package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request nhận callback thanh toán từ cổng thanh toán.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackRequest {
    
    private String transactionCode;
    
    private Long reservationId;
    
    private String paymentStatus;
    
    private java.math.BigDecimal amount;
    
    private String provider;
    
    private String additionalInfo;
}

