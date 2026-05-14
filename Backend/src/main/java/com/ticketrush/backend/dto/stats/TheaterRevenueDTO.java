package com.ticketrush.backend.dto.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO thống kê doanh thu theo rạp.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheaterRevenueDTO {

    private Long theaterId;

    private String theaterName;

    @JsonProperty("theaterAddress")
    private String theaterLocation;

    private BigDecimal totalRevenue;

    private Long showtimeCount;

    private Long ticketsSold;
    /**
     * Tạo đối tượng TheaterRevenueDTO với dữ liệu truyền vào.
     * @param theaterId giá trị trường theaterId.
     * @param theaterName giá trị trường theaterName.
     * @param theaterLocation giá trị trường theaterLocation.
     * @param totalRevenue giá trị trường totalRevenue.
     * @param showtimeCount giá trị trường showtimeCount.
     * @param ticketsSold giá trị trường ticketsSold.
     */
    public TheaterRevenueDTO(Long theaterId, String theaterName,String theaterLocation, Number totalRevenue, Number showtimeCount, Number ticketsSold) {
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.theaterLocation = theaterLocation;
        this.totalRevenue = totalRevenue instanceof BigDecimal ? (BigDecimal) totalRevenue : new BigDecimal(totalRevenue.toString());;
        this.showtimeCount = showtimeCount instanceof Long ? (Long) showtimeCount : ((Number) showtimeCount).longValue();
        this.ticketsSold = ticketsSold instanceof Long ? (Long) ticketsSold : ((Number) ticketsSold).longValue();
    }
}

