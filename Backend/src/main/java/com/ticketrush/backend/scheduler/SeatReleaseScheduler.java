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

/**
 * Scheduler tự động nhả ghế của các đơn giữ ghế đã hết hạn.
 *
 * Annotation {@link Component} đăng ký job vào Spring; {@link Slf4j} cung cấp
 * logger cho quá trình chạy nền.
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
     * Quét đơn LOCKED đã hết hạn và nhả ghế về trạng thái AVAILABLE.
     *
     * Annotation {@link Scheduled} chạy job mỗi 60 giây. Annotation
     * {@link Transactional} đảm bảo cập nhật đơn, ghế và suất chiếu trong cùng
     * một transaction; nếu lỗi chưa được bắt ở vòng lặp, transaction sẽ rollback.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> expiredReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.LOCKED)
                .filter(r -> r.getExpiresAt() != null && r.getExpiresAt().isBefore(now))
                .toList();

        if (expiredReservations.isEmpty()) {
            return;
        }

        log.info("[CRONJOB] Tìm thấy {} đơn quá hạn, bắt đầu nhả ghế...", expiredReservations.size());

        int totalReleasedSeats = 0;

        for (Reservation reservation : expiredReservations) {
            try {
                List<Seat> seats = seatRepository.findByReservationId(reservation.getId());

                if (!seats.isEmpty()) {
                    List<String> seatNumbers = seats.stream()
                            .map(Seat::getSeatNumber)
                            .toList();

                    for (Seat seat : seats) {
                        seat.setIsReserved(false);
                        seat.setReservation(null);
                    }
                    seatRepository.saveAll(seats);

                    Showtime showtime = reservation.getShowtime();
                    if (showtime != null) {
                        showtime.setAvailableSeats(showtime.getAvailableSeats() + seats.size());
                        showtimeRepository.save(showtime);

                        seatRealtimeService.broadcastSeatStatus(
                                showtime.getId(),
                                seatNumbers,
                                "AVAILABLE",
                                "Ghế đã được nhả do hết hạn giữ"
                        );
                    }

                    totalReleasedSeats += seats.size();

                    log.info("Nhả {} ghế: {} từ đơn #{} (hết hạn: {})",
                            seats.size(), seatNumbers, reservation.getId(), reservation.getExpiresAt());
                }

                reservation.setStatus(ReservationStatus.CANCELED);
                reservationRepository.save(reservation);

            } catch (Exception e) {
                log.error("Lỗi nhả ghế cho đơn #{}: {}", reservation.getId(), e.getMessage());
            }
        }

        log.info("[CRONJOB] Hoàn tất: {} đơn hủy, {} ghế được nhả",
                expiredReservations.size(), totalReleasedSeats);
    }
}
