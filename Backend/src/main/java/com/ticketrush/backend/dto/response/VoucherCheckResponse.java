package com.ticketrush.backend.dto.response;

import java.math.BigDecimal;

/**
 * DTO response chứa kết quả kiểm tra voucher.
 * @param code giá trị trường code.
 * @param isValid giá trị trường isValid.
 * @param description giá trị trường description.
 * @param discountPercentage giá trị trường discountPercentage.
 * @param maxDiscountAmount giá trị trường maxDiscountAmount.
 * @param remainingUsage giá trị trường remainingUsage.
 * @param message giá trị trường message.
 */
public record VoucherCheckResponse(
    String code,
    
    Boolean isValid,
    
    String description,
    
    BigDecimal discountPercentage,
    
    BigDecimal maxDiscountAmount,
    
    Integer remainingUsage,
    
    String message
) {
}

