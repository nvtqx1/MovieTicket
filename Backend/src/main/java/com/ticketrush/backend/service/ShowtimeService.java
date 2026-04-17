package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.ShowtimeResponse;

import java.time.LocalDate;
import java.util.List;

public interface ShowtimeService {

    List<ShowtimeResponse> searchShowtimes(Long movieId, Long theaterId, LocalDate showDate);
    
    ShowtimeResponse getShowtimeById(Long id);
}


