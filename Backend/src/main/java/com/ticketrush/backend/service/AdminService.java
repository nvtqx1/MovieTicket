package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.request.CreateShowtimeRequest;
import com.ticketrush.backend.dto.response.ShowtimeResponse;

/**
 * Admin Service Interface - Quản lý Phim & Suất Chiếu
 * VỀ LỖ HỔNG 3: Admin API thêm suất chiếu với roomId
 *
 * @author TicketRush Team
 * @version 1.0
 */
public interface AdminService {

    /**
     * Tạo suất chiếu mới
     * VỀ LỖ HỔNG 3: Thay vì theaterId, giờ nhận roomId
     *
     * @param request CreateShowtimeRequest chứa movieId, roomId, showDate, showTime, price, isFlashSale
     * @return ShowtimeResponse thông tin suất chiếu vừa tạo
     * @throws Exception nếu phim hoặc phòng không tồn tại
     */
    ShowtimeResponse createShowtime(CreateShowtimeRequest request) throws Exception;

    // Task 1.2: Lấy danh sách showtime có filter
    java.util.List<ShowtimeResponse> getFilteredShowtimes(Long theaterId, Long movieId, java.time.LocalDate date);

    // Task 1.2: Xóa showtime
    void deleteShowtime(Long showtimeId);
}

