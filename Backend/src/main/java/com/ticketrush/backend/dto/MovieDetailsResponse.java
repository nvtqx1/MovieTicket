package com.ticketrush.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MovieDetailsResponse {
    private MovieResponse movie;
    private List<TheaterShowtimes> theaters;

    @Data
    @Builder
    public static class TheaterShowtimes {
        private Long theaterId;
        private String theaterName;
        private String location;
        private List<ShowtimeResponse> showtimes;
    }
}
