package com.ticketrush.backend.dto.projection;

/**
 * Projection nhận kết quả thống kê người dùng theo nhóm tuổi.
 */
public interface AgeGroupStatProjection {
    /**
     * Lấy giá trị getAgeGroup.
     * @return giá trị getAgeGroup.
     */
    String getAgeGroup();
    /**
     * Lấy giá trị getCount.
     * @return giá trị getCount.
     */
    Long getCount();
    /**
     * Lấy giá trị getPercentage.
     * @return giá trị getPercentage.
     */
    Double getPercentage();
}
