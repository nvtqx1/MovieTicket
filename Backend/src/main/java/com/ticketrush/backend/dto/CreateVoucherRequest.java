package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO để Admin tạo mã giảm giá (Voucher)
 * 
 * API: POST /v1/admin/vouchers
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVoucherRequest {
    
    /**
     * Mã voucher (VD: "TICKETRUSH", "FLASH50", "SUMMER2026")
     */
    private String code;
    
    /**
     * Mô tả voucher
     * VD: "Giảm 50% cho tất cả vé"
     */
    private String description;
    
    /**
     * Phần trăm giảm giá (0-100)
     * VD: 50 = 50%
     */
    private BigDecimal discountPercentage;
    
    /**
     * Số tiền giảm tối đa (VND)
     * VD: 200000 = tối đa giảm 200k
     */
    private BigDecimal maxDiscountAmount;
    
    /**
     * Số lần tối đa có thể sử dụng
     * VD: 100 = dùng được 100 lần
     */
    private Integer maxUsage;
    
    /**
     * Thời gian bắt đầu áp dụng
     * VD: "2026-05-01T00:00:00"
     */
    private LocalDateTime startTime;
    
    /**
     * Thời gian kết thúc áp dụng
     * VD: "2026-05-31T23:59:59"
     */
    private LocalDateTime endTime;
}

