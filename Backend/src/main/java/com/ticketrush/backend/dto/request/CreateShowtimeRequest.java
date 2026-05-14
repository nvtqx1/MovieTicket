package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO request dùng để tạo suất chiếu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShowtimeRequest {

    private Long movieId;

    private Long roomId;

    private LocalDate showDate;

    private LocalTime showTime;

    private BigDecimal price;

    private Boolean isFlashSale = false;
}

