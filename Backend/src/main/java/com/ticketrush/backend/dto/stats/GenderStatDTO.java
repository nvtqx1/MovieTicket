package com.ticketrush.backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO thống kê người dùng theo giới tính.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenderStatDTO {

    private String gender;

    private Long count;
    /**
     * Tạo đối tượng GenderStatDTO với dữ liệu truyền vào.
     * @param gender giá trị trường gender.
     * @param count giá trị trường count.
     */
    public GenderStatDTO(String gender, Number count) {
        this.gender = gender;
        this.count = count instanceof Long ? (Long) count : ((Number) count).longValue();
    }
}

