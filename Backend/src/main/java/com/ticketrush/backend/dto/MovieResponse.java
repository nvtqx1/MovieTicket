package com.ticketrush.backend.dto;

/**
 * DTO (Data Transfer Object) để trả về thông tin phim cho Frontend
 * 
 * Được sử dụng bởi các API:
 * - GET /api/v1/movies (Lấy danh sách phim)
 * - GET /api/v1/movies/{id} (Lấy chi tiết phim)
 * - GET /api/v1/movies/search?keyword=... (Tìm kiếm phim)
 * - GET /api/v1/movies/filter?genre=... (Lọc phim theo thể loại)
 * 
 * Lưu ý: KHÔNG trả về raw Entity (Movie), luôn dùng DTO này để:
 * - Lựa chọn những field cần thiết
 * - Ẩn đi những field không cần (created_at, updated_at, etc.)
 * - Kiểm soát dữ liệu được gửi đến client
 * 
 * @author Backend Team
 * @version 1.0
 * @since NGÀY 5-6 (2026-04-17)
 */
public record MovieResponse(
        /**
         * ID của phim (khóa chính)
         */
        Long id,
        
        /**
         * Tên phim
         * Ví dụ: "Avatar", "Avengers: Endgame"
         */
        String title,
        
        /**
         * Mô tả/nội dung tóm tắt về phim
         * Có thể chứa TEXT dài
         */
        String description,
        
        /**
         * Năm sản xuất/phát hành phim
         * Ví dụ: 2009, 2019
         */
        Integer releaseYear,
        
        /**
         * Thể loại/loại phim
         * Ví dụ: "Hành động", "Khoa học viễn tưởng", "Tình cảm"
         */
        String genre,
        
        /**
         * URL của ảnh poster
         * Dùng để hiển thị hình ảnh phim trên Frontend
         * Ví dụ: "https://example.com/poster.jpg"
         */
        String posterImageUrl
) {
}
