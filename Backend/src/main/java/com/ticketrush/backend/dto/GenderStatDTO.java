package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thống kê giới tính (Gender Statistics).
 * Dùng cho Chart.js vẽ biểu đồ phân bố giới tính người dùng.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenderStatDTO {

    /**
     * Giới tính
     * Ví dụ: "Male", "Female", "Other"
     */
    private String gender;

    /**
     * Số lượng người dùng
     * Ví dụ: 150
     */
    private Long count;

    // Constructor for JPQL queries with Number type
    public GenderStatDTO(String gender, Number count) {
        this.gender = gender;
        this.count = count instanceof Long ? (Long) count : ((Number) count).longValue();
    }
}

