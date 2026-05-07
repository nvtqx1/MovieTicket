package com.ticketrush.backend.worker;

import com.ticketrush.backend.entity.Reservation;
import com.ticketrush.backend.entity.Seat;
import com.ticketrush.backend.entity.enums.ReservationStatus;
import com.ticketrush.backend.repository.ReservationRepository;
import com.ticketrush.backend.repository.SeatRepository;
import com.ticketrush.backend.service.SeatRealtimeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Safety net worker: Quét DB mỗi 60 giây để tìm các reservation LOCKED đã hết hạn
 * mà Redis KeyExpiredEvent có thể đã bỏ sót (do app offline, network issue, v.v.)
 *
 * Pattern "Belt and Suspenders":
 * - Redis listener = fast path (xử lý ngay khi key expire)
 * - Scheduled sweep = safety net (dọn dẹp những gì bị sót)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatLockSweepWorker {

    private static final String SEAT_LOCK_KEY_PREFIX = "seat:";

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SeatRealtimeService seatRealtimeService;

    /**
     * Chạy mỗi 60 giây, quét tìm reservation LOCKED có expiresAt < now
     * và giải phóng ghế + xóa key Redis (nếu còn tồn tại)
     */
    @Scheduled(fixedRate = 60_000, initialDelay = 30_000)
    @Transactional
    public void sweepExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.LOCKED, now);

        if (expiredReservations.isEmpty()) {
            return;
        }

        log.info("Sweep worker found {} expired LOCKED reservations to clean up", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            try {
                releaseReservation(reservation);
            } catch (Exception e) {
                log.error("Sweep worker failed to release reservation {}: {}",
                        reservation.getId(), e.getMessage(), e);
            }
        }
    }

    private void releaseReservation(Reservation reservation) {
        // Tìm tất cả ghế thuộc reservation này
        List<Seat> seats = seatRepository.findByReservationId(reservation.getId());

        Long userId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        Long showtimeId = reservation.getShowtime() != null ? reservation.getShowtime().getId() : null;

        // Giải phóng từng ghế
        for (Seat seat : seats) {
            seat.setReservation(null);
            seatRepository.save(seat);

            // Xóa key Redis nếu còn tồn tại (có thể đã bị xóa bởi TTL)
            String redisKey = SEAT_LOCK_KEY_PREFIX + seat.getId();
            redisTemplate.delete(redisKey);
        }

        // Cập nhật trạng thái reservation
        reservation.setStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);

        // Broadcast realtime
        if (showtimeId != null && !seats.isEmpty()) {
            List<String> seatNumbers = seats.stream()
                    .map(Seat::getSeatNumber)
                    .toList();

            seatRealtimeService.broadcastSeatStatus(
                    showtimeId,
                    seatNumbers,
                    "AVAILABLE",
                    "Seat lock expired (sweep)",
                    userId
            );
        }

        log.info("Sweep worker released reservation {} with {} seats",
                reservation.getId(), seats.size());
    }
}
