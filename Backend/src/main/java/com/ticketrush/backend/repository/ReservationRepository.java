package com.ticketrush.backend.repository;

import com.ticketrush.backend.dto.stats.MovieRevenueDTO;
import com.ticketrush.backend.dto.stats.TheaterRevenueDTO;
import com.ticketrush.backend.entity.Reservation;
import com.ticketrush.backend.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository thao tác dữ liệu đơn đặt vé và thống kê doanh thu.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Tìm các đơn theo trạng thái và thời điểm hết hạn.
     *
     * @param status trạng thái đơn cần lọc.
     * @param expiresAt thời điểm hết hạn trước mốc này.
     * @return danh sách đơn phù hợp.
     */
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime expiresAt);

    /**
     * Lấy lịch sử đặt vé của người dùng, mới nhất trước.
     *
     * @param userId ID người dùng.
     * @return danh sách đơn đặt vé của người dùng.
     */
    List<Reservation> findByUserIdOrderByReservationTimeDesc(Long userId);

    /**
     * Tìm các đơn đang khóa ghế nhưng đã quá hạn.
     *
     * Annotation {@link Query} dùng JPQL để cố định điều kiện trạng thái LOCKED
     * và so sánh thời điểm hết hạn.
     *
     * @param now thời điểm hiện tại dùng để so sánh hết hạn.
     * @return danh sách đơn khóa ghế đã hết hạn.
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = com.ticketrush.backend.entity.enums.ReservationStatus.LOCKED AND r.expiresAt < :now")
    List<Reservation> findExpiredLockedReservations(@Param("now") LocalDateTime now);

    /**
     * Thống kê doanh thu theo phim.
     *
     * Query gom doanh thu, số suất chiếu và số đơn đã thanh toán theo từng phim.
     *
     * @return danh sách doanh thu theo phim.
     */
    @Query("SELECT new com.ticketrush.backend.dto.stats.MovieRevenueDTO(" +
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
     * Thống kê doanh thu theo rạp.
     *
     * Query gom doanh thu, số suất chiếu và số đơn đã thanh toán theo từng rạp.
     *
     * @return danh sách doanh thu theo rạp.
     */
    @Query("SELECT new com.ticketrush.backend.dto.stats.TheaterRevenueDTO(" +
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
     * Lấy dữ liệu doanh thu thô theo ngày trong khoảng thời gian.
     *
     * Method trả {@code Object[]} để service tự map sang DTO theo database đang
     * dùng.
     *
     * @param startDate thời điểm bắt đầu.
     * @param endDate thời điểm kết thúc, không bao gồm mốc này.
     * @return danh sách dòng thống kê thô theo ngày.
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
     * Lấy tổng doanh thu toàn hệ thống từ các đơn đã thanh toán.
     *
     * @return tổng doanh thu.
     */
    @Query("SELECT SUM(r.totalPrice) FROM Reservation r WHERE r.paid = true")
    BigDecimal getTotalRevenue();

    /**
     * Lấy tổng doanh thu trong một ngày.
     *
     * @param date ngày cần thống kê.
     * @return tổng doanh thu trong ngày.
     */
    @Query("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r " +
           "WHERE CAST(r.reservationTime AS date) = :date " +
           "AND r.paid = true")
    BigDecimal getRevenueByDate(@Param("date") LocalDate date);

    /**
     * Đếm tổng số đơn đã thanh toán.
     *
     * @return số đơn đã thanh toán.
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.paid = true")
    Long getTotalPaidReservations();

    /**
     * Đếm tổng số vé đã bán.
     *
     * @return số ghế đã bán thuộc các đơn đã thanh toán.
     */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.isReserved = true AND s.reservation.paid = true")
    Long getTotalTicketsSold();
}
