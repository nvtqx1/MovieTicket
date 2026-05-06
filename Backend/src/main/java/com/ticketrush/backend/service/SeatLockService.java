package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.SeatLockResponse;
import com.ticketrush.backend.entity.Reservation;
import com.ticketrush.backend.entity.Seat;
import com.ticketrush.backend.entity.User;
import com.ticketrush.backend.entity.enums.ReservationStatus;
import com.ticketrush.backend.repository.ReservationRepository;
import com.ticketrush.backend.repository.SeatRepository;
import com.ticketrush.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockService {

    private static final String SEAT_LOCK_KEY_PREFIX = "seat:";
    private static final String SEAT_LOCK_VALUE_PREFIX = "userId:";

    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // Dịch vụ realtime để cập nhật trạng thái ghế trên giao diện người dùng khi có thay đổi
    private final SeatRealtimeService seatRealtimeService;

    @Value("${ticketrush.seat-lock.ttl-seconds:600}")
    private long lockTtlSeconds;

    @Transactional
    public SeatLockResponse lockSeat(Long seatId, Long userId) {
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found" + seatId));

        LocalDateTime now = LocalDateTime.now();
        releaseIfExpired(seat, now, false);

        if (Boolean.TRUE.equals(seat.getIsReserved())) {
            throw new IllegalStateException("Seat is already sold");
        }

        if (seat.getReservation() != null) {
            Reservation reservation = seat.getReservation();
            if (Boolean.TRUE.equals(reservation.getPaid()) || ReservationStatus.PAID.equals(reservation.getStatus())) {
                throw new IllegalStateException("Seat is already sold");
            }
            throw new IllegalStateException("Seat is already locked");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found" + userId));

        LocalDateTime lockedUntil = now.plusSeconds(lockTtlSeconds);
        Reservation reservation = createReservation(seat, user, lockedUntil);
        seat.setReservation(reservation);
        seatRepository.save(seat);

        String redisKey = buildSeatLockKey(seatId);
        String redisValue = SEAT_LOCK_VALUE_PREFIX + userId;
        Boolean stored = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, redisValue, lockTtlSeconds, TimeUnit.SECONDS);

        if (!Boolean.TRUE.equals(stored)) {
            throw new IllegalStateException("Seat is already locked in Redis");
        }

        seatRealtimeService.broadcastSeatStatus(
                seat.getShowtime().getId(),
                List.of(seat.getSeatNumber()),
                "LOCKED",
                "Reservation #" + reservation.getId(),
                userId
        );

        return SeatLockResponse.builder()
                .seatId(seat.getId())
                .showtimeId(seat.getShowtime().getId())
                .seatNumber(seat.getSeatNumber())
                .userId(userId)
                .reservationId(reservation.getId())
                .ttlSeconds(lockTtlSeconds)
                .lockedUntil(lockedUntil)
                .redisKey(redisKey)
                .status("LOCKED")
                .message("Seat locked for 10 minutes")
                .build();
    }

    @Transactional
    public void releaseExpiredSeatLock(Long seatId) {
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found" + seatId));

        boolean released = releaseIfExpired(seat, LocalDateTime.now(), true);
        if (!released) {
            log.debug("Seat {} did not have an expired lock to release", seatId);
        }
    }

    public String buildSeatLockKey(Long seatId) {
        return SEAT_LOCK_KEY_PREFIX + seatId;
    }

    private Reservation createReservation(Seat seat, User user, LocalDateTime lockedUntil) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setShowtime(seat.getShowtime());
        reservation.setStatus(ReservationStatus.LOCKED);
        reservation.setPaid(false);
        reservation.setExpiresAt(lockedUntil);

        BigDecimal seatPrice = seat.getShowtime().getPrice().multiply(seat.getSeatType().getPriceMultiplier());
        reservation.setTotalPrice(seatPrice);

        return reservationRepository.save(reservation);
    }

    private Boolean releaseIfExpired(Seat seat, LocalDateTime now, boolean broadcast) {
        Reservation reservation = seat.getReservation();
        if (reservation == null) {
            return false;
        }

        if (Boolean.TRUE.equals(seat.getIsReserved()) || Boolean.TRUE.equals(reservation.getPaid())
                || ReservationStatus.PAID.equals(reservation.getStatus())) {
            return false;
        }

        LocalDateTime expiresAt = reservation.getExpiresAt();
        if (expiresAt != null && expiresAt.isAfter(now)) {
            return false;
        }

        Long userId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        reservation.setStatus(ReservationStatus.CANCELED);
        seat.setReservation(null);

        reservationRepository.save(reservation);
        seatRepository.saveAndFlush(seat);
        redisTemplate.delete(buildSeatLockKey(seat.getId()));

        if (broadcast) {
            seatRealtimeService.broadcastSeatStatus(
                    seat.getShowtime().getId(),
                    List.of(seat.getSeatNumber()),
                    "AVAILABLE",
                    "Seat lock expired",
                    userId
            );
        }

        log.info("Released expired lock for seat {} reservation {}", seat.getId(), reservation.getId());
        return true;
    }

}
