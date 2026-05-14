package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request dùng để tạo hoặc cập nhật rạp chiếu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTheaterRequest {
    
    private String name;
    
    private String location;
    
    private Integer capacity;
}

