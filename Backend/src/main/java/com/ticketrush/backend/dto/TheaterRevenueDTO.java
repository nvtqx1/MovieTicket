package com.ticketrush.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho thống kê doanh thu theo rạp (Theater Revenue Statistics).
 * Dùng để so sánh hiệu suất giữa các rạp chiếu phim.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheaterRevenueDTO {

    /**
     * ID của rạp
     * Ví dụ: 1
     */
    private Long theaterId;

    /**
     * Tên rạp
     * Ví dụ: "CGV Hồ Tây"
     */
    private String theaterName;

    /**
     * Địa chỉ rạp
     * Ví dụ: "Tầng 5, Tòa nhà CGV, Hồ Tây, Hà Nội"
     */
    @JsonProperty("theaterAddress")
    private String theaterLocation;

    /**
     * Tổng doanh thu từ rạp này (VND)
     * Ví dụ: 1200000000
     */
    private BigDecimal totalRevenue;

    /**
     * Số lượng suất chiếu
     * Ví dụ: 45
     */
    private Long showtimeCount;

    /**
     * Số lượng vé bán được
     * Ví dụ: 890
     */
    private Long ticketsSold;

    // Constructor for JPQL queries
    public TheaterRevenueDTO(Long theaterId, String theaterName,String theaterLocation, Number totalRevenue, Number showtimeCount, Number ticketsSold) {
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.theaterLocation = theaterLocation;
        this.totalRevenue = totalRevenue instanceof BigDecimal ? (BigDecimal) totalRevenue : new BigDecimal(totalRevenue.toString());;
        this.showtimeCount = showtimeCount instanceof Long ? (Long) showtimeCount : ((Number) showtimeCount).longValue();
        this.ticketsSold = ticketsSold instanceof Long ? (Long) ticketsSold : ((Number) ticketsSold).longValue();
    }
}

