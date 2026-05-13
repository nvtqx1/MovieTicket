package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO request dùng để tạo hoặc cập nhật voucher.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVoucherRequest {
    
    private String code;
    
    private String description;
    
    private BigDecimal discountPercentage;
    
    private BigDecimal maxDiscountAmount;
    
    private Integer maxUsage;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
}

