package com.ticketrush.backend.dto;

/**
 * DTO (Data Transfer Object) để trả về thông tin rạp chiếu phim cho Frontend
 * 
 * Được sử dụng bởi các API:
 * - GET /api/v1/theaters (Lấy danh sách tất cả rạp)
 * - GET /api/v1/theaters/{id} (Lấy chi tiết một rạp)
 * - GET /api/v1/theaters/search?name=... (Tìm kiếm rạp theo tên)
 * 
 * Lưu ý: KHÔNG trả về raw Entity (Theater), luôn dùng DTO này để:
 * - Lựa chọn những field cần thiết
 * - Ẩn đi những field không cần (internal IDs, metadata, etc.)
 * - Kiểm soát dữ liệu được gửi đến client
 * 
 * @author Backend Team
 * @version 1.0
 * @since NGÀY 5-6 (2026-04-17)
 */
public record TheaterResponse(
        /**
         * ID của rạp (khóa chính)
         */
        Long id,
        
        /**
         * Tên rạp chiếu
         * Ví dụ: "CGV Landmark 81", "BHD Star Cineplex", "Lotte Cinema"
         */
        String name,
        
        /**
         * Địa chỉ rạp
         * Ví dụ: "106 Nguyen Hue, District 1, HCMC"
         */
        String location,
        
        /**
         * Sức chứa tối đa (tổng số ghế)
         * Ví dụ: 500 (tổng số ghế toàn rạp)
         */
        Integer capacity
) {
}
