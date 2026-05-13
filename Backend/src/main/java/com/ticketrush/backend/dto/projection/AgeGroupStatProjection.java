package com.ticketrush.backend.dto.projection;

/**
 * Interface projection cho native query thống kê nhóm tuổi.
 * Spring Data JPA sẽ tự map các column alias (ageGroup, count, percentage)
 * sang getter tương ứng.
 */
public interface AgeGroupStatProjection {
    String getAgeGroup();
    Long getCount();
    Double getPercentage();
}
