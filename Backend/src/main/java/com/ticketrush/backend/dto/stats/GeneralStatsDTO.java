package com.ticketrush.backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho tổng quan thống kê chung (General Statistics).
 * Hiển thị các số liệu chính trên Admin Dashboard (overview cards).
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralStatsDTO {

    /**
     * Tổng số người dùng
     * Ví dụ: 1500
     */
    private Long totalUsers;

    /**
     * Tổng số rạp chiếu
     * Ví dụ: 8
     */
    private Long totalTheaters;

    /**
     * Tổng số phim
     * Ví dụ: 25
     */
    private Long totalMovies;

    /**
     * Tổng số suất chiếu
     * Ví dụ: 150
     */
    private Long totalShowtimes;

    /**
     * Tổng doanh thu (VND)
     * Ví dụ: 5000000000
     */
    private BigDecimal totalRevenue;

    /**
     * Tổng số đơn đặt vé
     * Ví dụ: 2345
     */
    private Long totalReservations;

    /**
     * Tổng số vé đã bán
     * Ví dụ: 8900
     */
    private Long totalTicketsSold;

    /**
     * Doanh thu hôm nay (VND)
     * Ví dụ: 45000000
     */
    private BigDecimal todayRevenue;

    /**
     * Số đơn đặt vé hôm nay
     * Ví dụ: 23
     */
    private Long todayReservations;
}

