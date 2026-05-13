package com.ticketrush.backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho thống kê doanh thu theo phim (Movie Revenue Statistics).
 * Dùng để hiển thị doanh thu của từng phim trên Admin Dashboard.
 * Tính tổng tất cả reservations → tổng total_price GROUP BY movie_id
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRevenueDTO {

    /**
     * ID của phim
     * Ví dụ: 1
     */
    private Long movieId;

    /**
     * Tên phim
     * Ví dụ: "Avengers: Endgame"
     */
    private String movieTitle;

    /**
     * Tổng doanh thu từ phim này (VND)
     * Ví dụ: 450000000
     */
    private BigDecimal totalRevenue;

    /**
     * Số lượng suất chiếu
     * Ví dụ: 12
     */
    private Long showtimeCount;

    /**
     * Số lượng vé bán được
     * Ví dụ: 234
     */
    private Long ticketsSold;

    // Constructor for JPQL queries
    public MovieRevenueDTO(Long movieId, String movieTitle, Number totalRevenue, Number showtimeCount, Number ticketsSold) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.totalRevenue = totalRevenue instanceof BigDecimal ? (BigDecimal) totalRevenue : BigDecimal.valueOf(((Number) totalRevenue).doubleValue());
        this.showtimeCount = showtimeCount instanceof Long ? (Long) showtimeCount : ((Number) showtimeCount).longValue();
        this.ticketsSold = ticketsSold instanceof Long ? (Long) ticketsSold : ((Number) ticketsSold).longValue();
    }
}

