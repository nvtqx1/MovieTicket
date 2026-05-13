package com.ticketrush.backend.dto.response;

/**
 * Response DTO để trả về thông tin phòng chiếu cho Frontend
 * 
 * Được sử dụng bởi các API:
 * - POST /v1/admin/theaters/{theaterId}/rooms (Tạo phòng mới)
 * - GET /v1/theaters/{theaterId}/rooms (Lấy danh sách phòng)
 * 
 * @author TicketRush Team
 * @version 1.0
 */
public record RoomResponse(
    /**
     * ID của phòng (khóa chính)
     */
    Long id,
    
    /**
     * Tên phòng
     * Ví dụ: "IMAX 01"
     */
    String name,
    
    /**
     * Sức chứa của phòng
     * Ví dụ: 150
     */
    Integer capacity,
    
    /**
     * ID của rạp sở hữu phòng này
     */
    Long theaterId,
    
    /**
     * Tên rạp
     * Ví dụ: "CGV Hồ Tây"
     */
    String theaterName
) {
}

