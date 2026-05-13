package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để Admin tạo phim mới
 * 
 * API: POST /v1/admin/movies
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMovieRequest {
    
    /**
     * Tên phim
     * Ví dụ: "Avatar"
     */
    private String title;
    
    /**
     * Mô tả phim
     */
    private String description;
    
    /**
     * Năm phát hành
     * Ví dụ: 2009
     */
    private Integer releaseYear;
    
    /**
     * Thể loại phim
     * Ví dụ: "Science Fiction"
     */
    private String genre;
    
    /**
     * URL ảnh poster
     * Ví dụ: "https://example.com/avatar.jpg"
     */
    private String posterImageUrl;
}

