package com.ticketrush.backend.dto.response;

/**
 * DTO response chứa thông tin phim.
 * @param id giá trị trường id.
 * @param title giá trị trường title.
 * @param description giá trị trường description.
 * @param releaseYear giá trị trường releaseYear.
 * @param genre giá trị trường genre.
 * @param posterImageUrl giá trị trường posterImageUrl.
 */
public record MovieResponse(
        Long id,
        
        String title,
        
        String description,
        
        Integer releaseYear,
        
        String genre,
        
        String posterImageUrl
) {
}
