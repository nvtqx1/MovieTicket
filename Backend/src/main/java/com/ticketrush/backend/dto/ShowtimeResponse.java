package com.ticketrush.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO (Data Transfer Object) để trả về thông tin suất chiếu cho Frontend
 * 
 * Được sử dụng bởi các API:
 * - GET /api/v1/showtimes?movieId=1&showDate=2026-04-18 (Suất chiếu của phim trên ngày)
 * - GET /api/v1/showtimes?theaterId=2&showDate=2026-04-18 (Suất chiếu tại rạp trên ngày)
 * - GET /api/v1/showtimes?movieId=1 (Tất cả suất chiếu của phim)
 * - GET /api/v1/showtimes/{id} (Chi tiết suất chiếu)
 * 
 * Đặc điểm:
 * - KHÔNG trả về raw Entity (Showtime), luôn dùng DTO này
 * - Bao gồm thông tin "enriched" (phim & rạp có sẵn, không cần call riêng)
 * - Lựa chọn các field cần thiết để hiển thị trên Frontend
 * 
 * @author Backend Team
 * @version 1.0
 * @since NGÀY 5-6 (2026-04-17)
 */
public record ShowtimeResponse(
        /**
         * ID của suất chiếu
         */
        Long id,
        
        /**
         * Ngày chiếu
         * Format: YYYY-MM-DD
         * Ví dụ: 2026-04-18
         */
        LocalDate showDate,
        
        /**
         * Giờ chiếu
         * Format: HH:mm:ss
         * Ví dụ: 10:00:00, 13:30:00, 19:00:00
         */
        LocalTime showTime,
        
        /**
         * Giá vé (VND - Đồng Việt Nam)
         * Ví dụ: 120000 (120 nghìn đồng)
         * Precision: 10 digits, 2 decimal places
         */
        BigDecimal price,
        
        /**
         * Tổng số ghế mở bán cho suất chiếu
         * Ví dụ: 150 ghế
         */
        Integer totalSeats,
        
        /**
         * Số ghế còn trống hiện tại
         * Ví dụ: 45 ghế (100 đã bán, 50 còn trống)
         */
        Integer availableSeats,
        
        /**
         * Cờ đánh dấu suất chiếu có phải Flash Sale không
         * true: Là flash sale (giá giảm)
         * false: Giá bình thường
         */
        Boolean isFlashSale,
        
        /**
         * Thông tin tóm tắt của phim
         * Bao gồm: id, title, posterImageUrl, genre
         * Lợi: Không cần call API riêng để lấy info phim
         */
        MovieSummary movie,
        
        /**
         * Thông tin tóm tắt của rạp
         * Bao gồm: id, name, location
         * Lợi: Không cần call API riêng để lấy info rạp
         */
        TheaterSummary theater
) {
    /**
     * Record con: Thông tin tóm tắt phim
     * Sử dụng để embed trong ShowtimeResponse
     */
    public record MovieSummary(
            /**
             * ID phim
             */
            Long id,
            
            /**
             * Tên phim
             * Ví dụ: "Avatar"
             */
            String title,
            
            /**
             * URL ảnh poster
             * Dùng để hiển thị hình ảnh trên Frontend
             */
            String posterImageUrl,
            
            /**
             * Thể loại phim
             * Ví dụ: "Khoa học viễn tưởng"
             */
            String genre
    ) {
    }

    /**
     * Record con: Thông tin tóm tắt rạp
     * Sử dụng để embed trong ShowtimeResponse
     */
    public record TheaterSummary(
            /**
             * ID rạp
             */
            Long id,
            
            /**
             * Tên rạp
             * Ví dụ: "CGV Landmark 81"
             */
            String name,
            
            /**
             * Địa chỉ rạp
             * Ví dụ: "106 Nguyen Hue, District 1, HCMC"
             */
            String location
    ) {
    }
}
