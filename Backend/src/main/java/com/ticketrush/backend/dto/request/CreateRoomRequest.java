package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request dùng để tạo hoặc cập nhật phòng chiếu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {
    
    private String name;
    
    private Integer capacity;
}

