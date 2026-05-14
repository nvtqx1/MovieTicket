package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu suất chiếu.
 */
@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    /**
     * Kiểm tra phòng đã có suất chiếu nào hay chưa.
     *
     * @param roomId ID phòng chiếu.
     * @return true nếu phòng đã có ít nhất một suất chiếu.
     */
    boolean existsByRoomId(Long roomId);

    /**
     * Kiểm tra phòng còn lịch chiếu từ ngày chỉ định trở đi hay không.
     *
     * @param roomId ID phòng chiếu.
     * @param date ngày bắt đầu kiểm tra.
     * @return true nếu còn suất chiếu từ ngày chỉ định trở đi.
     */
    boolean existsByRoomIdAndShowDateGreaterThanEqual(Long roomId, LocalDate date);

    /**
     * Lấy suất chiếu của một phim trong một ngày, sắp xếp theo giờ.
     *
     * @param movieId ID phim.
     * @param showDate ngày chiếu.
     * @return danh sách suất chiếu phù hợp.
     */
    List<Showtime> findByMovieIdAndShowDateOrderByShowTimeAsc(Long movieId, LocalDate showDate);

    /**
     * Lấy suất chiếu của một rạp trong một ngày, sắp xếp theo giờ.
     *
     * @param theaterId ID rạp.
     * @param showDate ngày chiếu.
     * @return danh sách suất chiếu phù hợp.
     */
    List<Showtime> findByRoomTheaterIdAndShowDateOrderByShowTimeAsc(Long theaterId, LocalDate showDate);

    /**
     * Tìm suất chiếu theo phim, rạp và ngày.
     *
     * Annotation {@link Query} dùng JPQL fetch join để lấy sẵn phim, phòng và
     * rạp, tránh truy vấn lười lặp lại khi dựng response.
     *
     * @param movieId ID phim cần lọc, có thể null.
     * @param theaterId ID rạp cần lọc, có thể null.
     * @param showDate ngày chiếu cụ thể, có thể null.
     * @param fromDate ngày bắt đầu khi không truyền showDate.
     * @return danh sách suất chiếu phù hợp.
     */
    @Query("""
            SELECT s
            FROM Showtime s
            JOIN FETCH s.movie m
            JOIN FETCH s.room r
            JOIN FETCH r.theater t
            WHERE (:movieId IS NULL OR m.id = :movieId)
              AND (:theaterId IS NULL OR t.id = :theaterId)
              AND (
                    (:showDate IS NOT NULL AND s.showDate = :showDate)
                    OR (:showDate IS NULL AND s.showDate >= :fromDate)
                  )
            ORDER BY s.showDate ASC, s.showTime ASC
            """)
    List<Showtime> searchShowtimes(
            @Param("movieId") Long movieId,
            @Param("theaterId") Long theaterId,
            @Param("showDate") LocalDate showDate,
            @Param("fromDate") LocalDate fromDate
    );

    /**
     * Lấy lịch chiếu sắp tới của một phim, có thể lọc theo rạp.
     *
     * @param movieId ID phim.
     * @param fromDate ngày bắt đầu lấy lịch.
     * @param theaterId ID rạp cần lọc, có thể null.
     * @return danh sách suất chiếu sắp tới.
     */
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

    /**
     * Lấy lịch chiếu của rạp trong một khoảng ngày.
     *
     * @param theaterId ID rạp.
     * @param startDate ngày bắt đầu.
     * @param endDate ngày kết thúc.
     * @return danh sách suất chiếu của rạp trong khoảng ngày.
     */
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

    /**
     * Tìm suất chiếu đầu tiên theo phòng, ngày và giờ.
     *
     * @param roomId ID phòng chiếu.
     * @param showDate ngày chiếu.
     * @param showTime giờ chiếu.
     * @return suất chiếu nếu tồn tại.
     */
    Optional<Showtime> findFirstByRoomIdAndShowDateAndShowTime(Long roomId, LocalDate showDate, LocalTime showTime);
}
