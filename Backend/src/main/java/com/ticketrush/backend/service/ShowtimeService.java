package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.response.ShowtimeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ShowtimeService {

    /**
     * Tìm kiếm suất chiếu (bản cũ - không phân trang)
     * ⚠️ Deprecated: Sử dụng searchShowtimes(Pageable) thay vì hàm này
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
    
    ShowtimeResponse getShowtimeById(Long id);
}


