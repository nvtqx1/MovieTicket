package com.ticketrush.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO khi Admin tạo voucher thành công
 * 
 * API: POST /v1/admin/vouchers
 * 
 * @author TicketRush Team
 * @version 1.0
 */
public record VoucherResponse(
    /**
     * ID của voucher
     */
    Long id,
    
    /**
     * Mã voucher
     */
    String code,
    
    /**
     * Mô tả
     */
    String description,
    
    /**
     * Phần trăm giảm
     */
    BigDecimal discountPercentage,
    
    /**
     * Số tiền giảm tối đa
     */
    BigDecimal maxDiscountAmount,
    
    /**
     * Số lần tối đa dùng
     */
    Integer maxUsage,
    
    /**
     * Số lần đã dùng
     */
    Integer currentUsage,
    
    /**
     * Thời gian bắt đầu
     */
    LocalDateTime startTime,
    
    /**
     * Thời gian kết thúc
     */
    LocalDateTime endTime,
    
    /**
     * Trạng thái (active/expired)
     */
    String status
) {
}

