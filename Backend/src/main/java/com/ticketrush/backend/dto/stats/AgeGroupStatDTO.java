package com.ticketrush.backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO thống kê người dùng theo nhóm tuổi.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgeGroupStatDTO {

    private String ageGroup;

    private Long count;

    private Double percentage;
    /**
     * Tạo đối tượng AgeGroupStatDTO với dữ liệu truyền vào.
     * @param ageGroup giá trị trường ageGroup.
     * @param count giá trị trường count.
     * @param percentage giá trị trường percentage.
     */
    public AgeGroupStatDTO(String ageGroup, Number count, Number percentage) {
        this.ageGroup = ageGroup;
        this.count = count instanceof Long ? (Long) count : ((Number) count).longValue();
        this.percentage = percentage instanceof Double ? (Double) percentage : ((Number) percentage).doubleValue();
    }
}

