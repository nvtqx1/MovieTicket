package com.ticketrush.backend.dto.response;

import java.math.BigDecimal;

/**
 * Response DTO khi Frontend kiểm tra mã giảm giá
 * 
 * API: GET /v1/vouchers/check?code=TICKETRUSH
 * 
 * Dùng để Frontend hiển thị tiền giảm ngay khi user gõ mã
 * 
 * @author TicketRush Team
 * @version 1.0
 */
public record VoucherCheckResponse(
    /**
     * Mã voucher được check
     */
    String code,
    
    /**
     * Có hợp lệ không (trong thời gian sử dụng)
     */
    Boolean isValid,
    
    /**
     * Mô tả voucher
     */
    String description,
    
    /**
     * Phần trăm giảm
     * VD: 50 = 50%
     */
    BigDecimal discountPercentage,
    
    /**
     * Số tiền giảm tối đa
     */
    BigDecimal maxDiscountAmount,
    
    /**
     * Số lần còn lại có thể dùng
     */
    Integer remainingUsage,
    
    /**
     * Thông báo cho Frontend hiển thị
     * VD: "Giảm 50% tối đa 200.000đ"
     */
    String message
) {
}

