package com.ticketrush.backend.repository;

import com.ticketrush.backend.dto.stats.GenderStatDTO;
import com.ticketrush.backend.dto.projection.AgeGroupStatProjection;
import com.ticketrush.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu người dùng và thống kê người dùng.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm người dùng theo email.
     *
     * @param email email đăng nhập.
     * @return người dùng nếu tồn tại.
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra email đã tồn tại hay chưa.
     *
     * @param email email cần kiểm tra.
     * @return true nếu email đã tồn tại.
     */
    Boolean existsByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại hay chưa.
     *
     * @param userName username cần kiểm tra.
     * @return true nếu username đã tồn tại.
     */
    Boolean existsByUserName(String userName);

    /**
     * Tìm người dùng theo username.
     *
     * @param userName username cần tìm.
     * @return người dùng nếu tồn tại.
     */
    Optional<User> findByUserName(String userName);

    /**
     * Đếm số username bắt đầu bằng prefix chỉ định.
     *
     * @param userNamePrefix tiền tố username.
     * @return số người dùng có username bắt đầu bằng prefix.
     */
    long countByUserNameStartingWith(String userNamePrefix);

    /**
     * Thống kê số người dùng theo giới tính.
     *
     * @return danh sách thống kê giới tính.
     */
    @Query("SELECT new com.ticketrush.backend.dto.stats.GenderStatDTO(u.gender, COUNT(u)) " +
           "FROM User u " +
           "WHERE u.gender IS NOT NULL AND u.isBanned = false " +
           "GROUP BY u.gender " +
           "ORDER BY COUNT(u) DESC")
    List<GenderStatDTO> getGenderStatistics();

    /**
     * Thống kê người dùng theo nhóm tuổi.
     *
     * Query native dùng hàm ngày của database để tính tuổi từ date_of_birth và
     * trả về projection theo alias ageGroup, count, percentage.
     *
     * @return danh sách thống kê nhóm tuổi.
     */
    @Query(value =
             "SELECT age_group AS ageGroup, cnt AS count, " +
             "  ROUND(cnt * 100.0 / (SELECT COUNT(*) FROM users), 2) AS percentage " +
             "FROM ( " +
             "  SELECT " +
             "    CASE " +
             "      WHEN YEAR(CURDATE()) - YEAR(date_of_birth) < 18 THEN '< 18' " +
             "      WHEN YEAR(CURDATE()) - YEAR(date_of_birth) < 25 THEN '18-24' " +
             "      WHEN YEAR(CURDATE()) - YEAR(date_of_birth) < 35 THEN '25-34' " +
             "      WHEN YEAR(CURDATE()) - YEAR(date_of_birth) < 50 THEN '35-49' " +
             "      ELSE '50+' " +
             "    END AS age_group, " +
             "    CASE " +
             "      WHEN YEAR(CURDATE()) - YEAR(date_of_birth) < 18 THEN 1 " +
             "      WHEN YEAR(CURDATE()) - YEAR(date_of_birth) < 25 THEN 2 " +
             "      WHEN YEAR(CURDATE()) - YEAR(date_of_birth) < 35 THEN 3 " +
             "      WHEN YEAR(CURDATE()) - YEAR(date_of_birth) < 50 THEN 4 " +
             "      ELSE 5 " +
             "    END AS sort_order, " +
             "    COUNT(*) AS cnt " +
             "  FROM users " +
             "  WHERE date_of_birth IS NOT NULL " +
             "  GROUP BY age_group, sort_order " +
             ") AS sub " +
             "ORDER BY sort_order",
             nativeQuery = true)
    List<AgeGroupStatProjection> getAgeGroupStatistics();

    /**
     * Đếm tổng số người dùng.
     *
     * @return tổng số người dùng.
     */
    @Query("SELECT COUNT(u) FROM User u")
    Long getTotalUserCount();

    /**
     * Đếm số người dùng đang hoạt động.
     *
     * @return số người dùng không bị khóa.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isBanned = false")
    Long getActiveUserCount();

    /**
     * Đếm số người dùng tạo trong một ngày.
     *
     * @param date ngày cần thống kê.
     * @return số người dùng tạo trong ngày.
     */
    @Query("SELECT COUNT(u) FROM User u " +
           "WHERE CAST(u.id AS date) = :date")
    Long getUserCountByDate(@Param("date") LocalDate date);
}
