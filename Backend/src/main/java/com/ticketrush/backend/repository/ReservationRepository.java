package com.ticketrush.backend.repository;

import com.ticketrush.backend.dto.MovieRevenueDTO;
import com.ticketrush.backend.dto.TheaterRevenueDTO;
import com.ticketrush.backend.dto.DailyRevenueDTO;
import com.ticketrush.backend.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ticketrush.backend.entity.enums.ReservationStatus;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Safety net: Tìm các reservation đang LOCKED nhưng đã hết hạn (expiresAt < now)
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime expiresAt);

    // Lấy lịch sử đặt vé của một người dùng (Sắp xếp mới nhất lên đầu)
    List<Reservation> findByUserIdOrderByReservationTimeDesc(Long userId);

    // ========== NGÀY 19-21: DASHBOARD QUERIES (JPQL NÂNG CAO) ==========

    /**
     * Thống kê doanh thu theo phim (JOIN Movie -> Showtime -> Reservation).
     * Tính tổng tất cả reservations -> tổng total_price GROUP BY movie_id
     *
     * JPQL Query: SELECT new DTO(movieId, movieTitle, SUM(totalPrice), COUNT(showtime), COUNT(reservation))
     *             FROM Movie m
     *             JOIN m.showtimes s
     *             JOIN s.reservations r
     *             WHERE r.status = PAID
     *             GROUP BY m.id
     *
     * @return Danh sách MovieRevenueDTO chứa doanh thu theo phim
     */
    @Query("SELECT new com.ticketrush.backend.dto.MovieRevenueDTO(" +
           "  m.id, " +
           "  m.title, " +
           "  SUM(COALESCE(r.totalPrice, 0)), " +
           "  COUNT(DISTINCT s.id), " +
           "  COUNT(DISTINCT r.id) " +
           ") " +
           "FROM Movie m " +
           "LEFT JOIN Showtime s ON s.movie.id = m.id " +
           "LEFT JOIN Reservation r ON r.showtime.id = s.id AND r.paid = true " +
           "GROUP BY m.id, m.title " +
           "ORDER BY SUM(COALESCE(r.totalPrice, 0)) DESC")
    List<MovieRevenueDTO> getMovieRevenueStatistics();

    /**
     * Thống kê doanh thu theo rạp (JOIN Theater -> Showtime -> Reservation).
     * Tính tổng doanh thu của mỗi rạp
     *
     * @return Danh sách TheaterRevenueDTO chứa doanh thu theo rạp
     */
     @Query("SELECT new com.ticketrush.backend.dto.TheaterRevenueDTO(" +
            "  t.id, " +
            "  t.name, " +
            "  t.location, " +
            "  SUM(COALESCE(r.totalPrice, 0)), " +
            "  COUNT(DISTINCT s.id), " +
            "  COUNT(DISTINCT r.id) " +
            ") " +
            "FROM Theater t " +
            "LEFT JOIN Showtime s ON s.room.theater.id = t.id " +
            "LEFT JOIN Reservation r ON r.showtime.id = s.id AND r.paid = true " +
            "GROUP BY t.id, t.name, t.location " +
            "ORDER BY SUM(COALESCE(r.totalPrice, 0)) DESC")
     List<TheaterRevenueDTO> getTheaterRevenueStatistics();

    /**
     * Thống kê doanh thu theo ngày.
     * Lấy doanh thu của từng ngày từ ngày startDate đến endDate
     *
     * @param startDate Ngày bắt đầu
     * @param endDate   Ngày kết thúc
     * @return Danh sách DailyRevenueDTO chứa doanh thu theo ngày
     */
    @Query("""
    SELECT 
        FUNCTION('DATE', r.reservationTime),
        SUM(r.totalPrice),
        COUNT(r.id),
        COUNT(r.id)
    FROM Reservation r
    WHERE r.reservationTime >= :startDate
      AND r.reservationTime < :endDate
      AND r.paid = true
    GROUP BY FUNCTION('DATE', r.reservationTime)
    ORDER BY FUNCTION('DATE', r.reservationTime) DESC
""")
    List<Object[]> getDailyRevenueRaw(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Lấy tổng doanh thu toàn hệ thống (chỉ tính reservations đã PAID)
     *
     * @return Tổng doanh thu
     */
    @Query("SELECT SUM(r.totalPrice) FROM Reservation r WHERE r.paid = true")
    BigDecimal getTotalRevenue();

    /**
     * Lấy tổng doanh thu trong ngày chỉ định
     *
     * @param date Ngày cần thống kê
     * @return Tổng doanh thu trong ngày
     */
    @Query("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r " +
           "WHERE CAST(r.reservationTime AS date) = :date " +
           "AND r.paid = true")
    BigDecimal getRevenueByDate(@Param("date") LocalDate date);

    /**
     * Lấy tổng số reservations đã PAID
     *
     * @return Tổng số reservations
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.paid = true")
    Long getTotalPaidReservations();

    /**
     * Lấy tổng số tickets đã bán (join với seats để đếm)
     *
     * @return Tổng số tickets
     */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.isReserved = true AND s.reservation.paid = true")
    Long getTotalTicketsSold();
}
