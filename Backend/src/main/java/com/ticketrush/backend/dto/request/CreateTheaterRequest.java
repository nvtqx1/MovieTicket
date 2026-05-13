package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để Admin tạo rạp chiếu mới
 * 
 * API: POST /v1/admin/theaters
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTheaterRequest {
    
    /**
     * Tên rạp
     * Ví dụ: "CGV Hồ Tây"
     */
    private String name;
    
    /**
     * Địa chỉ rạp
     * Ví dụ: "123 Đường Tây Hồ, Quận Tây Hồ, Hà Nội"
     */
    private String location;
    
    /**
     * Sức chứa tối đa (tổng số chỗ ngồi)
     * Ví dụ: 500
     */
    private Integer capacity;
}

