package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    // PHỤC VỤ NGÀY 5-6 (API GET /showtimes):
    // Lấy các suất chiếu của 1 Phim, trong 1 Ngày cụ thể, Sắp xếp giờ từ sớm đến muộn
    List<Showtime> findByMovieIdAndShowDateOrderByShowTimeAsc(Long movieId, LocalDate showDate);

    // Tùy chọn thêm: Tìm suất chiếu theo Rạp và Ngày (dành cho màn hình "Chọn Rạp trước, chọn Phim sau")
    List<Showtime> findByTheaterIdAndShowDateOrderByShowTimeAsc(Long theaterId, LocalDate showDate);
}