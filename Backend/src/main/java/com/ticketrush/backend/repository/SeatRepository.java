package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // PHỤC VỤ NGÀY 5-6: Trả về toàn bộ ghế của 1 suất chiếu để FE vẽ sơ đồ (150 ghế)
    List<Seat> findByShowtimeId(Long showtimeId);

    // PHỤC VỤ TUẦN 2 (CHỐNG TRANH CHẤP - ROW LOCKING):
    // Tìm các ghế cụ thể mà user đang bấm chọn (VD: ["A1", "A2"]).
    // Lưu ý: Lát nữa sang tuần 2, bạn sẽ phải thêm @Lock(LockModeType.PESSIMISTIC_WRITE) lên trên hàm này!
    List<Seat> findByShowtimeIdAndSeatNumberIn(Long showtimeId, List<String> seatNumbers);

    // Tìm các ghế đang bị khóa bởi 1 đơn hàng cụ thể (Dùng khi user hủy đơn, muốn nhả ghế ra)
    List<Seat> findByReservationId(Long reservationId);
}