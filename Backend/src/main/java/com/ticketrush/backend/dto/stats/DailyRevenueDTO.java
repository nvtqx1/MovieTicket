package com.ticketrush.backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho thống kê doanh thu theo ngày (Daily Revenue Statistics).
 * Dùng để vẽ biểu đồ doanh thu theo thời gian trên Admin Dashboard.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRevenueDTO {

    /**
     * Ngày thống kê
     * Ví dụ: "2026-04-30"
     */
    private LocalDate date;

    /**
     * Tổng doanh thu trong ngày (VND)
     * Ví dụ: 45000000
     */
    private BigDecimal totalRevenue;

    /**
     * Số lượng đơn đặt vé trong ngày
     * Ví dụ: 23
     */
    private Long orderCount;

    /**
     * Số lượng vé bán được trong ngày
     * Ví dụ: 87
     */
    private Long ticketsSold;

    // Constructor for JPQL queries
    public DailyRevenueDTO(
            Object date,
            Number totalRevenue,
            Number orderCount,
            Number ticketsSold
    ) {
        this.date = toLocalDate(date);
        this.totalRevenue = toBigDecimal(totalRevenue);
        this.orderCount = orderCount != null ? orderCount.longValue() : 0L;
        this.ticketsSold = ticketsSold != null ? ticketsSold.longValue() : 0L;
    }

    private LocalDate toLocalDate(Object date) {
        if (date instanceof LocalDate localDate) {
            return localDate;
        }
        if (date instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (date instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        throw new IllegalArgumentException("Unsupported date projection type: " +
                (date != null ? date.getClass().getName() : "null"));
    }

    private BigDecimal toBigDecimal(Number value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(value.doubleValue());
    }
}

