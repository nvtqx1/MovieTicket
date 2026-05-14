    package com.ticketrush.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO response chứa thông tin suất chiếu.
 * @param id giá trị trường id.
 * @param showDate giá trị trường showDate.
 * @param showTime giá trị trường showTime.
 * @param price giá trị trường price.
 * @param totalSeats giá trị trường totalSeats.
 * @param availableSeats giá trị trường availableSeats.
 * @param isFlashSale giá trị trường isFlashSale.
 * @param roomName giá trị trường roomName.
 * @param movie giá trị trường movie.
 * @param theater giá trị trường theater.
 */
public record ShowtimeResponse(
        Long id,
        
        LocalDate showDate,
        
        LocalTime showTime,
        
        BigDecimal price,
        
        Integer totalSeats,
        
        Integer availableSeats,
        
        Boolean isFlashSale,

        String roomName,

        MovieSummary movie,
        
        TheaterSummary theater
) {
    /**
     * DTO con chứa thông tin tóm tắt phim.
     * @param id giá trị trường id.
     * @param title giá trị trường title.
     * @param posterImageUrl giá trị trường posterImageUrl.
     * @param genre giá trị trường genre.
     */
    public record MovieSummary(
            Long id,
            
            String title,
            
            String posterImageUrl,
            
            String genre
    ) {
    }

    /**
     * DTO con chứa thông tin tóm tắt rạp.
     * @param id giá trị trường id.
     * @param name giá trị trường name.
     * @param location giá trị trường location.
     */
    public record TheaterSummary(
            Long id,
            
            String name,
            
            String location
    ) {
    }
}
