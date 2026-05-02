package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.CreateShowtimeRequest;
import com.ticketrush.backend.dto.ShowtimeResponse;

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
}

