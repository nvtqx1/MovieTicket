package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.ShowtimeResponse;
import com.ticketrush.backend.entity.Showtime;
import com.ticketrush.backend.exception.ResourceNotFoundException;
import com.ticketrush.backend.repository.ShowtimeRepository;
import com.ticketrush.backend.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    @Override
    public List<ShowtimeResponse> searchShowtimes(Long movieId, Long theaterId, LocalDate showDate) {
        // Nếu không truyền showDate, mặc định lấy từ hôm nay trở đi
        LocalDate fromDate = showDate != null ? showDate : LocalDate.now();

        return showtimeRepository.searchShowtimes(movieId, theaterId, showDate, fromDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ShowtimeResponse getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
        return toResponse(showtime);
    }

    private ShowtimeResponse toResponse(Showtime showtime) {
        return new ShowtimeResponse(
                showtime.getId(),
                showtime.getShowDate(),
                showtime.getShowTime(),
                showtime.getPrice(),
                showtime.getTotalSeats(),
                showtime.getAvailableSeats(),
                showtime.getIsFlashSale(),
                new ShowtimeResponse.MovieSummary(
                        showtime.getMovie().getId(),
                        showtime.getMovie().getTitle(),
                        showtime.getMovie().getPosterImageUrl(),
                        showtime.getMovie().getGenre()
                ),
                new ShowtimeResponse.TheaterSummary(
                        showtime.getTheater().getId(),
                        showtime.getTheater().getName(),
                        showtime.getTheater().getLocation()
                )
        );
    }
}
