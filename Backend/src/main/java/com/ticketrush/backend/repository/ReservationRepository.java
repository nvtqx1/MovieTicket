package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // Lấy lịch sử đặt vé của một người dùng (Sắp xếp mới nhất lên đầu)
    List<Reservation> findByUserIdOrderByReservationTimeDesc(Long userId);
}