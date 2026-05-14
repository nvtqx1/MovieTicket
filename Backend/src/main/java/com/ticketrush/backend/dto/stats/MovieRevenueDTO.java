package com.ticketrush.backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO thống kê doanh thu theo phim.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRevenueDTO {

    private Long movieId;

    private String movieTitle;

    private BigDecimal totalRevenue;

    private Long showtimeCount;

    private Long ticketsSold;
    /**
     * Tạo đối tượng MovieRevenueDTO với dữ liệu truyền vào.
     * @param movieId giá trị trường movieId.
     * @param movieTitle giá trị trường movieTitle.
     * @param totalRevenue giá trị trường totalRevenue.
     * @param showtimeCount giá trị trường showtimeCount.
     * @param ticketsSold giá trị trường ticketsSold.
     */
    public MovieRevenueDTO(Long movieId, String movieTitle, Number totalRevenue, Number showtimeCount, Number ticketsSold) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.totalRevenue = totalRevenue instanceof BigDecimal ? (BigDecimal) totalRevenue : BigDecimal.valueOf(((Number) totalRevenue).doubleValue());
        this.showtimeCount = showtimeCount instanceof Long ? (Long) showtimeCount : ((Number) showtimeCount).longValue();
        this.ticketsSold = ticketsSold instanceof Long ? (Long) ticketsSold : ((Number) ticketsSold).longValue();
    }
}

