package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Seat;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // PHỤC VỤ NGÀY 5-6: Trả về toàn bộ ghế của 1 suất chiếu để FE vẽ sơ đồ (150 ghế)
    List<Seat> findByShowtimeId(Long showtimeId);

    // PHỤC VỤ TUẦN 2 (CHỐNG TRANH CHẤP - ROW LOCKING):
    // Tìm các ghế cụ thể mà user đang bấm chọn (VD: ["A1", "A2"]).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    List<Seat> findByShowtimeIdAndSeatNumberIn(Long showtimeId, List<String> seatNumbers);

    //Phục vụ TUẦN 2 (CHỐNG TRANH CHẤP - ROW LOCKING):
    // Tìm 1 ghế cụ thể để cập nhật trạng thái (VD: "A1").
    // Dùng khi user bấm chọn 1 ghế trên FE, hệ thống sẽ gọi API
    // này để lấy thông tin chi tiết của ghế đó (VD: giá tiền, loại ghế)
    // và đồng thời khóa dòng dữ liệu của ghế đó lại để tránh trường hợp 2 người cùng bấm chọn 1 ghế.
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

    // Tìm các ghế đang bị khóa bởi 1 đơn hàng cụ thể (Dùng khi user hủy đơn, muốn nhả ghế ra)
    List<Seat> findByReservationId(Long reservationId);

    long countByShowtimeId(Long showtimeid);
}
