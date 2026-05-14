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
 * DTO thống kê doanh thu theo ngày.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRevenueDTO {

    private LocalDate date;

    private BigDecimal totalRevenue;

    private Long orderCount;

    private Long ticketsSold;
    /**
     * Tạo đối tượng DailyRevenueDTO với dữ liệu truyền vào.
     * @param date giá trị trường date.
     * @param totalRevenue giá trị trường totalRevenue.
     * @param orderCount giá trị trường orderCount.
     * @param ticketsSold giá trị trường ticketsSold.
     */
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

    /**
     * Xử lý nội bộ cho toLocalDate.
     * @param date giá trị tham số date.
     * @return kết quả xử lý của toLocalDate.
     */
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

    /**
     * Xử lý nội bộ cho toBigDecimal.
     * @param value giá trị tham số value.
     * @return kết quả xử lý của toBigDecimal.
     */
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

