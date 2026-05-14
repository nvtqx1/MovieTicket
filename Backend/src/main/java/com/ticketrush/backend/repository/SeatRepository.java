package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Seat;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu ghế theo suất chiếu.
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * Lấy toàn bộ ghế của một suất chiếu.
     *
     * @param showtimeId ID suất chiếu.
     * @return danh sách ghế của suất chiếu.
     */
    List<Seat> findByShowtimeId(Long showtimeId);

    /**
     * Xóa toàn bộ ghế của một suất chiếu.
     *
     * @param showtimeId ID suất chiếu cần xóa ghế.
     */
    void deleteByShowtimeId(Long showtimeId);

    /**
     * Tìm và khóa nhiều ghế theo mã ghế trong một suất chiếu.
     *
     * Annotation {@link Lock} dùng khóa ghi bi quan để chống tranh chấp giữ ghế;
     * {@link QueryHints} giới hạn thời gian chờ lock ở mức 3000ms.
     *
     * @param showtimeId ID suất chiếu.
     * @param seatNumbers danh sách mã ghế cần khóa.
     * @return danh sách ghế được tìm thấy.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    List<Seat> findByShowtimeIdAndSeatNumberIn(Long showtimeId, List<String> seatNumbers);

    /**
     * Tìm một ghế theo ID và khóa dòng dữ liệu để cập nhật an toàn.
     *
     * Query fetch trước các quan hệ cần dùng để tránh lazy loading khi xử lý giữ
     * ghế; khóa bi quan giúp tránh hai người chọn cùng một ghế.
     *
     * @param seatId ID ghế cần khóa.
     * @return ghế nếu tồn tại.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("""
            SELECT s
            FROM Seat s
            JOIN FETCH s.showtime st
            JOIN FETCH s.seatType
            LEFT JOIN FETCH st.movie
            LEFT JOIN FETCH st.room r
            LEFT JOIN FETCH r.theater
            LEFT JOIN FETCH s.reservation res
            LEFT JOIN FETCH res.user
            WHERE s.id = :seatId
            """)
    Optional<Seat> findByIdForUpdate(@Param("seatId") Long seatId);

    /**
     * Tìm nhiều ghế theo ID và khóa dòng dữ liệu theo thứ tự tăng dần.
     *
     * Sắp xếp theo ID giúp giảm nguy cơ deadlock khi nhiều request khóa nhiều
     * ghế cùng lúc.
     *
     * @param seatIds danh sách ID ghế cần khóa.
     * @return danh sách ghế được khóa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("""
            SELECT s
            FROM Seat s
            JOIN FETCH s.showtime st
            JOIN FETCH s.seatType
            LEFT JOIN FETCH st.movie
            LEFT JOIN FETCH st.room r
            LEFT JOIN FETCH r.theater
            LEFT JOIN FETCH s.reservation res
            LEFT JOIN FETCH res.user
            WHERE s.id IN :seatIds
            ORDER BY s.id ASC
            """)
    List<Seat> findByIdsForUpdate(@Param("seatIds") List<Long> seatIds);

    /**
     * Tìm các ghế thuộc một đơn đặt vé.
     *
     * @param reservationId ID đơn đặt vé.
     * @return danh sách ghế của đơn.
     */
    List<Seat> findByReservationId(Long reservationId);

    /**
     * Đếm số ghế của một suất chiếu.
     *
     * @param showtimeid ID suất chiếu.
     * @return số ghế của suất chiếu.
     */
    long countByShowtimeId(Long showtimeid);
}
