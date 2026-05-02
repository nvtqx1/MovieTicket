package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.ShowtimeResponse;
import com.ticketrush.backend.entity.Showtime;
import com.ticketrush.backend.exception.ResourceNotFoundException;
import com.ticketrush.backend.repository.ShowtimeRepository;
import com.ticketrush.backend.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
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
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Page<ShowtimeResponse> searchShowtimes(Long movieId, Long theaterId, LocalDate showDate, Pageable pageable) {
        log.info("🎬 Tìm kiếm suất chiếu - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        // Nếu không truyền showDate, mặc định lấy từ hôm nay trở đi
        LocalDate fromDate = showDate != null ? showDate : LocalDate.now();

        List<Showtime> showtimes = showtimeRepository.searchShowtimes(movieId, theaterId, showDate, fromDate);
        
        // Khởi tạo phân trang từ manual list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), showtimes.size());
        
        List<ShowtimeResponse> responses = showtimes.subList(start, end)
                .stream()
                .map(this::mapToResponse)
                .toList();
        
        return new PageImpl<>(responses, pageable, showtimes.size());
    }

    @Override
    public ShowtimeResponse getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
        return mapToResponse(showtime);
    }

    /**
     * Public method để AdminServiceImpl và ShowtimeServiceImpl có thể gọi được
     */
    public ShowtimeResponse toResponse(Showtime showtime) {
        return new ShowtimeResponse(
                showtime.getId(),
                showtime.getShowDate(),
                showtime.getShowTime(),
                showtime.getPrice(),
                showtime.getTotalSeats(),
                showtime.getAvailableSeats(),
                showtime.getIsFlashSale(),
                showtime.getRoom().getName(),
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

    private ShowtimeResponse mapToResponse(Showtime showtime) {
        return toResponse(showtime);
    }
}
