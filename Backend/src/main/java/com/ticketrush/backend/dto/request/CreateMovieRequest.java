package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request dùng để tạo hoặc cập nhật phim.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMovieRequest {
    
    private String title;
    
    private String description;
    
    private Integer releaseYear;
    
    private String genre;
    
    private String posterImageUrl;
}

