package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.response.ShowtimeResponse;
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

/**
 * Triển khai tra cứu suất chiếu và chuyển đổi dữ liệu sang DTO.
 * {@code @Transactional(readOnly = true)} tối ưu các truy vấn chỉ đọc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    /**
     * Tìm kiếm suất chiếu không phân trang.
     *
     * @param movieId ID phim cần lọc, có thể null.
     * @param theaterId ID rạp cần lọc, có thể null.
     * @param showDate ngày chiếu cần lọc, có thể null.
     * @return danh sách suất chiếu phù hợp.
     */
    @Override
    public List<ShowtimeResponse> searchShowtimes(Long movieId, Long theaterId, LocalDate showDate) {
        // Nếu không truyền showDate, mặc định lấy từ hôm nay trở đi
        LocalDate fromDate = showDate != null ? showDate : LocalDate.now();

        return showtimeRepository.searchShowtimes(movieId, theaterId, showDate, fromDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Tìm kiếm suất chiếu và phân trang thủ công từ kết quả truy vấn.
     *
     * @param movieId ID phim cần lọc, có thể null.
     * @param theaterId ID rạp cần lọc, có thể null.
     * @param showDate ngày chiếu cần lọc, có thể null.
     * @param pageable thông tin phân trang.
     * @return trang dữ liệu suất chiếu.
     */
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

    /**
     * Lấy chi tiết suất chiếu theo ID.
     *
     * @param id ID suất chiếu.
     * @return thông tin suất chiếu.
     * @throws ResourceNotFoundException nếu suất chiếu không tồn tại.
     */
    @Override
    public ShowtimeResponse getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
        return mapToResponse(showtime);
    }

    /**
     * Public method để AdminServiceImpl và ShowtimeServiceImpl có thể gọi được
     */
    /**
     * Chuyển entity Showtime sang DTO dùng chung cho các service khác.
     *
     * @param showtime entity suất chiếu cần chuyển đổi.
     * @return DTO suất chiếu.
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

    /**
     * Chuyển entity Showtime sang DTO phản hồi.
     *
     * @param showtime entity suất chiếu cần chuyển đổi.
     * @return DTO suất chiếu.
     */
    private ShowtimeResponse mapToResponse(Showtime showtime) {
        return toResponse(showtime);
    }
}
