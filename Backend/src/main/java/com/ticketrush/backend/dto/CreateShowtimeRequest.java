package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO để Admin thêm suất chiếu (Create Showtime).
 * V3: Thay vì theaterId, giờ request phải nhận roomId.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShowtimeRequest {

    /**
     * ID của phim
     * Ví dụ: 1
     */
    private Long movieId;

    /**
     * ID của phòng chiếu (THAY ĐỔI V3: Trước là theaterId)
     * Ví dụ: 5
     */
    private Long roomId;

    /**
     * Ngày chiếu
     * Ví dụ: 2026-05-15
     */
    private LocalDate showDate;

    /**
     * Thời gian chiếu
     * Ví dụ: 18:00
     */
    private LocalTime showTime;

    /**
     * Giá vé cơ bản
     * Ví dụ: 100000 VND
     */
    private BigDecimal price;

    /**
     * Có phải flash sale không
     */
    private Boolean isFlashSale = false;
}

