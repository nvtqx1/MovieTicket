package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.response.ShowtimeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Dịch vụ tra cứu thông tin suất chiếu.
 */
public interface ShowtimeService {

    /**
     * Tìm kiếm suất chiếu (bản cũ - không phân trang)
     * ⚠️ Deprecated: Sử dụng searchShowtimes(Pageable) thay vì hàm này
     */
    /**
     * Tìm kiếm suất chiếu không phân trang.
     *
     * @param movieId ID phim cần lọc, có thể null.
     * @param theaterId ID rạp cần lọc, có thể null.
     * @param showDate ngày chiếu cần lọc, có thể null.
     * @return danh sách suất chiếu phù hợp.
     */
    @Deprecated
    List<ShowtimeResponse> searchShowtimes(Long movieId, Long theaterId, LocalDate showDate);
    
    /**
     * Tìm kiếm suất chiếu với phân trang
     * 
     * @param movieId ID phim (tuỳ chọn)
     * @param theaterId ID rạp (tuỳ chọn)
     * @param showDate Ngày chiếu (tuỳ chọn)
     * @param pageable Pageable object (page, size)
     * @return Page<ShowtimeResponse> kết quả tìm kiếm
     */
    Page<ShowtimeResponse> searchShowtimes(Long movieId, Long theaterId, LocalDate showDate, Pageable pageable);
    
    /**
     * Lấy chi tiết một suất chiếu theo ID.
     *
     * @param id ID suất chiếu.
     * @return thông tin suất chiếu.
     * @throws com.ticketrush.backend.exception.ResourceNotFoundException nếu suất chiếu không tồn tại.
     */
    ShowtimeResponse getShowtimeById(Long id);
}


