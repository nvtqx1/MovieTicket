package com.ticketrush.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO response chứa thông tin voucher.
 * @param id giá trị trường id.
 * @param code giá trị trường code.
 * @param description giá trị trường description.
 * @param discountPercentage giá trị trường discountPercentage.
 * @param maxDiscountAmount giá trị trường maxDiscountAmount.
 * @param maxUsage giá trị trường maxUsage.
 * @param currentUsage giá trị trường currentUsage.
 * @param startTime giá trị trường startTime.
 * @param endTime giá trị trường endTime.
 * @param status giá trị trường status.
 */
public record VoucherResponse(
    Long id,
    
    String code,
    
    String description,
    
    BigDecimal discountPercentage,
    
    BigDecimal maxDiscountAmount,
    
    Integer maxUsage,
    
    Integer currentUsage,
    
    LocalDateTime startTime,
    
    LocalDateTime endTime,
    
    String status
) {
}

