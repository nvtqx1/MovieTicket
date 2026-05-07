package com.ticketrush.backend.scheduler;

import com.ticketrush.backend.entity.Reservation;
import com.ticketrush.backend.entity.Seat;
import com.ticketrush.backend.entity.Showtime;
import com.ticketrush.backend.entity.enums.ReservationStatus;
import com.ticketrush.backend.repository.ReservationRepository;
import com.ticketrush.backend.repository.SeatRepository;
import com.ticketrush.backend.repository.ShowtimeRepository;
import com.ticketrush.backend.service.SeatRealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ════════════════════════════════════════════════════════
 * TASK 3.2: CRONJOB — TỰ ĐỘNG NHẢ GHẾ QUÁ HẠN
 * ════════════════════════════════════════════════════════
 * 
 * Vòng đời vé:
 *   AVAILABLE → [hold-seat] → LOCKED (10 phút)
 *   LOCKED → [checkout] → PAID ✅
 *   LOCKED → [hết 10 phút] → CANCELED → Ghế = AVAILABLE ♻️
 * 
 * Job này chạy mỗi 60 giây, quét DB tìm reservation
 * có expires_at < NOW() và status = LOCKED → tự động nhả.
 * 
 * Sau khi nhả, broadcast qua WebSocket để FE cập nhật real-time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatReleaseScheduler {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRealtimeService seatRealtimeService;

    /**
     * Chạy mỗi 60 giây
     * 
     * Quy trình:
     * 1. Quét tất cả reservation có status=LOCKED và expires_at < NOW()
     * 2. Với mỗi reservation quá hạn:
     *    a. Tìm tất cả ghế liên kết
     *    b. Set is_reserved = false, reservation = null
     *    c. Set reservation.status = CANCELED
     *    d. Cập nhật available_seats trong showtime
     *    e. Broadcast AVAILABLE qua WebSocket
     */
    @Scheduled(fixedRate = 60000) // 60 giây
    @Transactional
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Tìm tất cả reservation LOCKED đã quá hạn
        List<Reservation> expiredReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.LOCKED)
                .filter(r -> r.getExpiresAt() != null && r.getExpiresAt().isBefore(now))
                .toList();

        if (expiredReservations.isEmpty()) {
            return; // Không log để tránh spam
        }

        log.info("⏰ [CRONJOB] Tìm thấy {} đơn quá hạn, bắt đầu nhả ghế...", expiredReservations.size());

        int totalReleasedSeats = 0;

        for (Reservation reservation : expiredReservations) {
            try {
                // 2. Tìm ghế liên kết với reservation này
                List<Seat> seats = seatRepository.findByReservationId(reservation.getId());

                if (!seats.isEmpty()) {
                    List<String> seatNumbers = seats.stream()
                            .map(Seat::getSeatNumber)
                            .toList();

                    // 3. Nhả ghế: is_reserved = false, reservation = null
                    for (Seat seat : seats) {
                        seat.setIsReserved(false);
                        seat.setReservation(null);
                    }
                    seatRepository.saveAll(seats);

                    // 4. Cập nhật available_seats
                    Showtime showtime = reservation.getShowtime();
                    if (showtime != null) {
                        showtime.setAvailableSeats(showtime.getAvailableSeats() + seats.size());
                        showtimeRepository.save(showtime);

                        // 5. Broadcast AVAILABLE qua WebSocket
                        seatRealtimeService.broadcastSeatStatus(
                                showtime.getId(),
                                seatNumbers,
                                "AVAILABLE",
                                "Ghế đã được nhả (hết hạn giữ)"
                        );
                    }

                    totalReleasedSeats += seats.size();

                    log.info("♻️ Nhả {} ghế: {} từ đơn #{} (hết hạn: {})",
                            seats.size(), seatNumbers, reservation.getId(), reservation.getExpiresAt());
                }

                // 6. Cập nhật reservation status = CANCELED
                reservation.setStatus(ReservationStatus.CANCELED);
                reservationRepository.save(reservation);

            } catch (Exception e) {
                log.error("❌ Lỗi nhả ghế cho đơn #{}: {}", reservation.getId(), e.getMessage());
            }
        }

        log.info("✅ [CRONJOB] Hoàn tất: {} đơn hủy, {} ghế được nhả",
                expiredReservations.size(), totalReleasedSeats);
    }
}
