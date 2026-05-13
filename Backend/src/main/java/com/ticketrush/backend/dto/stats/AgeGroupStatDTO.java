package com.ticketrush.backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thống kê người dùng theo nhóm tuổi (Age Group Statistics).
 * Dùng để analysis khách hàng nhắm tới trên Admin Dashboard.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgeGroupStatDTO {

    /**
     * Nhóm tuổi
     * Ví dụ: "13-18", "19-25", "26-35", "36-50", "50+"
     */
    private String ageGroup;

    /**
     * Số lượng người dùng trong nhóm
     * Ví dụ: 120
     */
    private Long count;

    /**
     * Tỷ lệ phần trăm so với tổng
     * Ví dụ: 25.5
     */
    private Double percentage;

    // Constructor for JPQL queries with Number types
    public AgeGroupStatDTO(String ageGroup, Number count, Number percentage) {
        this.ageGroup = ageGroup;
        this.count = count instanceof Long ? (Long) count : ((Number) count).longValue();
        this.percentage = percentage instanceof Double ? (Double) percentage : ((Number) percentage).doubleValue();
    }
}

