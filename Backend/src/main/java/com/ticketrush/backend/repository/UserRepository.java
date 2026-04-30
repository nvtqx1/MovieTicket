package com.ticketrush.backend.repository;

import com.ticketrush.backend.dto.GenderStatDTO;
import com.ticketrush.backend.dto.AgeGroupStatDTO;
import com.ticketrush.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Phục vụ ĐĂNG NHẬP (Login):
    // Tìm User dựa trên email. Trả về Optional để tránh lỗi NullPointerException nếu gõ sai email.
    Optional<User> findByEmail(String email);

    // 2. Phục vụ ĐĂNG KÝ (Register):
    // Kiểm tra xem Email này đã có ai dùng trong hệ thống chưa? (Trả về true/false cực nhanh)
    Boolean existsByEmail(String email);

    // 3. Phục vụ ĐĂNG KÝ (Register):
    // Kiểm tra xem Tên đăng nhập (Username) này đã bị ai xí chỗ chưa?
    Boolean existsByUserName(String userName);

    // (Tùy chọn) Tìm user theo username nếu hệ thống của bạn cho phép đăng nhập bằng cả username hoặc email
    Optional<User> findByUserName(String userName);

    // ========== NGÀY 19-21: DASHBOARD QUERIES (JPQL NÂNG CAO) ==========

    /**
     * Thống kê giới tính: GROUP BY gender
     * JPQL Query: SELECT new DTO(gender, COUNT(user)) FROM User GROUP BY gender
     *
     * @return Danh sách GenderStatDTO chứa giới tính và số lượng
     */
    @Query("SELECT new com.ticketrush.backend.dto.GenderStatDTO(u.gender, COUNT(u)) " +
           "FROM User u " +
           "WHERE u.gender IS NOT NULL " +
           "GROUP BY u.gender " +
           "ORDER BY COUNT(u) DESC")
    List<GenderStatDTO> getGenderStatistics();

    /**
     * Thống kê người dùng theo nhóm tuổi
     * Tính tuổi từ dateOfBirth như: YEAR(CURRENT_DATE()) - YEAR(dateOfBirth)
     *
     * @return Danh sách AgeGroupStatDTO chứa nhóm tuổi và số lượng
     */
     @Query("SELECT new com.ticketrush.backend.dto.AgeGroupStatDTO(" +
            "  CASE " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 18 THEN '< 18' " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 25 THEN '18-24' " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 35 THEN '25-34' " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 50 THEN '35-49' " +
            "    ELSE '50+' " +
            "  END, " +
            "  COUNT(u), " +
            "  CAST(CAST(COUNT(u) AS double) * 100 / (SELECT COUNT(u2) FROM User u2) AS double) " +
            ") " +
            "FROM User u " +
            "WHERE u.dateOfBirth IS NOT NULL " +
            "GROUP BY " +
            "  CASE " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 18 THEN '< 18' " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 25 THEN '18-24' " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 35 THEN '25-34' " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 50 THEN '35-49' " +
            "    ELSE '50+' " +
            "  END " +
            "ORDER BY " +
            "  CASE " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 18 THEN 1 " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 25 THEN 2 " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 35 THEN 3 " +
            "    WHEN CAST(YEAR(CURRENT_DATE()) AS int) - CAST(YEAR(u.dateOfBirth) AS int) < 50 THEN 4 " +
            "    ELSE 5 " +
            "  END")
    List<AgeGroupStatDTO> getAgeGroupStatistics();

    /**
     * Lấy tổng số người dùng
     *
     * @return Tổng số users
     */
    @Query("SELECT COUNT(u) FROM User u")
    Long getTotalUserCount();

    /**
     * Lấy tổng số người dùng đã tạo trong ngày chỉ định
     *
     * @param date Ngày cần thống kê
     * @return Tổng số users tạo trong ngày
     */
    @Query("SELECT COUNT(u) FROM User u " +
           "WHERE CAST(u.id AS date) = :date")
    Long getUserCountByDate(@Param("date") LocalDate date);
}