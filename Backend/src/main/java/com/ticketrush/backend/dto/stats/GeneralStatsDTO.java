package com.ticketrush.backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO thống kê tổng quan dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralStatsDTO {

    private Long totalUsers;

    private Long totalTheaters;

    private Long totalMovies;

    private Long totalShowtimes;

    private BigDecimal totalRevenue;

    private Long totalReservations;

    private Long totalTicketsSold;

    private BigDecimal todayRevenue;

    private Long todayReservations;
}

