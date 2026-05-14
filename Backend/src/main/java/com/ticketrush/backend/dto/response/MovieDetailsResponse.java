package com.ticketrush.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * DTO response chứa chi tiết phim kèm lịch chiếu theo rạp.
 */
@Data
@Builder
public class MovieDetailsResponse {
    private MovieResponse movie;
    private List<TheaterShowtimes> theaters;

    /**
     * DTO con chứa rạp và danh sách suất chiếu của phim.
     */
    @Data
    @Builder
    public static class TheaterShowtimes {
        private Long theaterId;
        private String theaterName;
        private String location;
        private List<ShowtimeResponse> showtimes;
    }
}
