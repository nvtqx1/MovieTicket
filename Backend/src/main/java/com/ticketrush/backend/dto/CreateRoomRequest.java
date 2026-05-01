package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để Admin tạo phòng chiếu mới cho rạp
 * 
 * API: POST /v1/admin/theaters/{theaterId}/rooms
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {
    
    /**
     * Tên phòng
     * Ví dụ: "IMAX 01", "2D Room 1", "3D Screen A"
     */
    private String name;
    
    /**
     * Sức chứa (số ghế) của phòng
     * Ví dụ: 150
     */
    private Integer capacity;
}

