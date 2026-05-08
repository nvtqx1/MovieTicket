package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Showtime;
import org.flywaydb.core.internal.sqlscript.ShouldExecuteEvaluator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    boolean existsByRoomId(Long roomId);

    /**
     * Kiểm tra phòng có lịch chiếu từ ngày chỉ định trở đi không.
     * Dùng cho logic Soft Delete: chỉ chặn xóa nếu CÒN lịch chiếu tương lai.
     */
    boolean existsByRoomIdAndShowDateGreaterThanEqual(Long roomId, LocalDate date);

    // PHỤC VỤ NGÀY 5-6 (API GET /showtimes):
    // Lấy các suất chiếu của 1 Phim, trong 1 Ngày cụ thể, Sắp xếp giờ từ sớm đến muộn
    List<Showtime> findByMovieIdAndShowDateOrderByShowTimeAsc(Long movieId, LocalDate showDate);

    // Tùy chọn thêm: Tìm suất chiếu theo Rạp và Ngày (dành cho màn hình "Chọn Rạp trước, chọn Phim sau")
    List<Showtime> findByRoomTheaterIdAndShowDateOrderByShowTimeAsc(Long theaterId, LocalDate showDate);

    @Query("""
            SELECT s
            FROM Showtime s
            JOIN FETCH s.movie m
            JOIN FETCH s.room r
            JOIN FETCH r.theater t
            WHERE (:movieId IS NULL OR m.id = :movieId)
              AND (:theaterId IS NULL OR t.id = :theaterId)
              AND (:fromDate IS NULL OR s.showDate = :fromDate)
            ORDER BY s.showDate ASC, s.showTime ASC
            """)
    List<Showtime> searchShowtimes(
            @Param("movieId") Long movieId,
            @Param("theaterId") Long theaterId,
            @Param("showDate") LocalDate showDate,
            @Param("fromDate") LocalDate fromDate
    );

    // Task 2.2: Lấy lịch chiếu của phim từ hôm nay trở đi, có thể filter theo rạp
    @Query("""
            SELECT s
            FROM Showtime s
            JOIN FETCH s.movie m
            JOIN FETCH s.room r
            JOIN FETCH r.theater t
            WHERE m.id = :movieId
              AND s.showDate >= :fromDate
              AND (:theaterId IS NULL OR t.id = :theaterId)
            ORDER BY s.showDate ASC, s.showTime ASC
            """)
    List<Showtime> findUpcomingByMovie(
            @Param("movieId") Long movieId,
            @Param("fromDate") LocalDate fromDate,
            @Param("theaterId") Long theaterId
    );

    // Task 2.2: Lấy lịch chiếu của rạp trong khoảng ngày
    @Query("""
            SELECT s
            FROM Showtime s
            JOIN FETCH s.movie m
            JOIN FETCH s.room r
            JOIN FETCH r.theater t
            WHERE t.id = :theaterId
              AND s.showDate BETWEEN :startDate AND :endDate
            ORDER BY s.showDate ASC, s.showTime ASC
            """)
    List<Showtime> findByTheaterAndDateRange(
            @Param("theaterId") Long theaterId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    Optional<Showtime> findFirstByRoomIdAndShowDateAndShowTime(Long roomId, LocalDate showDate, LocalTime showTime);
}
