package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.response.SeatLockResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Dịch vụ khóa ghế tạm thời bằng Redis và reservation trong database.
 */
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

    /**
     * Lock nhiều ghế cùng lúc theo pattern All-or-Nothing:
     * - Nếu 1 ghế fail → rollback tất cả → không ghế nào bị lock
     * - Tất cả ghế phải thuộc cùng 1 suất chiếu (showtime)
     *
     * Flow: Validate ALL → SET Redis ALL → INSERT MySQL → Broadcast
     *       Nếu Redis fail giữa chừng → xóa các key đã SET
     *       Nếu MySQL fail → xóa toàn bộ key Redis
     */
    /**
     * Khóa nhiều ghế theo cơ chế all-or-nothing bằng Redis và database.
     * {@code @Transactional} kết hợp khóa ghi từ repository để tránh hai người giữ cùng một ghế.
     *
     * @param seatIds danh sách ID ghế cần khóa.
     * @param userId ID người dùng thực hiện khóa.
     * @return thông tin các ghế đã khóa và thời điểm hết hạn.
     * @throws IllegalArgumentException nếu danh sách ghế rỗng, ghế/user không tồn tại hoặc ghế khác suất chiếu.
     * @throws IllegalStateException nếu ghế đã bán hoặc đang bị khóa.
     */
    @Transactional
    public SeatLockResponse lockSeats(List<Long> seatIds, Long userId) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("seatIds cannot be empty");
        }

        // === Bước 0: Fetch tất cả ghế với PESSIMISTIC_WRITE lock (1 query) ===
        List<Seat> seats = seatRepository.findByIdsForUpdate(seatIds);

        if (seats.size() != seatIds.size()) {
            List<Long> foundIds = seats.stream().map(Seat::getId).toList();
            List<Long> missingIds = seatIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new IllegalArgumentException("Seats not found: " + missingIds);
        }

        // === Bước 1: Validate tất cả ghế trước khi lock bất kỳ ghế nào ===
        LocalDateTime now = LocalDateTime.now();
        Long showtimeId = null;

        for (Seat seat : seats) {
            // Auto-release nếu reservation cũ đã hết hạn
            releaseIfExpired(seat, now, false);

            // Kiểm tra ghế đã bán chưa
            if (Boolean.TRUE.equals(seat.getIsReserved())) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is already sold");
            }

            // Kiểm tra ghế đã bị lock bởi người khác chưa
            if (seat.getReservation() != null) {
                Reservation existingRes = seat.getReservation();
                if (Boolean.TRUE.equals(existingRes.getPaid()) || ReservationStatus.PAID.equals(existingRes.getStatus())) {
                    throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is already sold");
                }
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is already locked");
            }

            // Đảm bảo tất cả ghế thuộc cùng 1 suất chiếu
            if (showtimeId == null) {
                showtimeId = seat.getShowtime().getId();
            } else if (!showtimeId.equals(seat.getShowtime().getId())) {
                throw new IllegalArgumentException("All seats must belong to the same showtime");
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // === Bước 2: SET Redis cho tất cả ghế (all-or-nothing) ===
        String redisValue = SEAT_LOCK_VALUE_PREFIX + userId;
        List<String> lockedRedisKeys = new ArrayList<>();

        try {
            for (Seat seat : seats) {
                String redisKey = buildSeatLockKey(seat.getId());
                Boolean stored = redisTemplate.opsForValue()
                        .setIfAbsent(redisKey, redisValue, lockTtlSeconds, TimeUnit.SECONDS);

                if (!Boolean.TRUE.equals(stored)) {
                    throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is already locked in Redis");
                }
                lockedRedisKeys.add(redisKey);
            }
        } catch (Exception e) {
            // Rollback: xóa tất cả key Redis đã SET thành công
            rollbackRedisKeys(lockedRedisKeys);
            throw e;
        }

        // === Bước 3: INSERT MySQL — 1 Reservation chung cho tất cả ghế ===
        LocalDateTime lockedUntil = now.plusSeconds(lockTtlSeconds);
        Reservation reservation;
        try {
            reservation = createReservation(seats, user, lockedUntil);
            for (Seat seat : seats) {
                seat.setReservation(reservation);
            }
            seatRepository.saveAll(seats);
        } catch (Exception e) {
            // Compensate: xóa toàn bộ key Redis
            rollbackRedisKeys(lockedRedisKeys);
            log.error("MySQL failed after Redis SET for seats {}, compensated by deleting Redis keys", seatIds, e);
            throw e;
        }

        // === Bước 4: Broadcast realtime ===
        List<String> seatNumbers = seats.stream()
                .map(Seat::getSeatNumber)
                .toList();

        seatRealtimeService.broadcastSeatStatus(
                showtimeId,
                seatNumbers,
                "LOCKED",
                "Reservation #" + reservation.getId(),
                userId
        );

        // === Bước 5: Build response ===
        List<SeatLockResponse.LockedSeatInfo> lockedSeatInfos = seats.stream()
                .map(s -> SeatLockResponse.LockedSeatInfo.builder()
                        .seatId(s.getId())
                        .seatNumber(s.getSeatNumber())
                        .build())
                .toList();

        return SeatLockResponse.builder()
                .showtimeId(showtimeId)
                .userId(userId)
                .reservationId(reservation.getId())
                .lockedSeats(lockedSeatInfos)
                .ttlSeconds(lockTtlSeconds)
                .lockedUntil(lockedUntil)
                .status("LOCKED")
                .message("Locked " + seats.size() + " seat(s) for 10 minutes")
                .build();
    }

    /**
     * Giải phóng khóa ghế nếu khóa đã hết hạn.
     * {@code @Transactional} đảm bảo cập nhật ghế, reservation và Redis nhất quán.
     *
     * @param seatId ID ghế cần kiểm tra và giải phóng.
     * @throws IllegalArgumentException nếu ghế không tồn tại.
     */
    @Transactional
    public void releaseExpiredSeatLock(Long seatId) {
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));

        boolean released = releaseIfExpired(seat, LocalDateTime.now(), true);
        if (!released) {
            log.debug("Seat {} did not have an expired lock to release", seatId);
        }
    }

    /**
     * Tạo key Redis dùng để lưu trạng thái khóa ghế.
     *
     * @param seatId ID ghế.
     * @return Redis key của ghế.
     */
    public String buildSeatLockKey(Long seatId) {
        return SEAT_LOCK_KEY_PREFIX + seatId;
    }

    /**
     * Tạo 1 Reservation chung cho nhiều ghế, tổng giá = sum(giá từng ghế)
     */
    /**
     * Tạo reservation LOCKED cho danh sách ghế đã khóa.
     *
     * @param seats danh sách ghế được khóa.
     * @param user người dùng giữ ghế.
     * @param lockedUntil thời điểm hết hạn giữ ghế.
     * @return reservation đã được lưu.
     */
    private Reservation createReservation(List<Seat> seats, User user, LocalDateTime lockedUntil) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setShowtime(seats.get(0).getShowtime());
        reservation.setStatus(ReservationStatus.LOCKED);
        reservation.setPaid(false);
        reservation.setExpiresAt(lockedUntil);

        // Tổng giá = sum(giá suất chiếu × hệ số loại ghế) cho từng ghế
        BigDecimal totalPrice = seats.stream()
                .map(seat -> seat.getShowtime().getPrice().multiply(seat.getSeatType().getPriceMultiplier()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        reservation.setTotalPrice(totalPrice);

        return reservationRepository.save(reservation);
    }

    /**
     * Rollback: xóa tất cả Redis key đã SET thành công
     */
    /**
     * Xóa các key Redis đã tạo khi một bước khóa ghế bị lỗi.
     *
     * @param keys danh sách key Redis cần xóa.
     */
    private void rollbackRedisKeys(List<String> keys) {
        if (keys.isEmpty()) return;
        try {
            redisTemplate.delete(keys);
            log.info("Rolled back {} Redis keys", keys.size());
        } catch (Exception ex) {
            log.error("Failed to rollback Redis keys {}: {}", keys, ex.getMessage(), ex);
        }
    }

    /**
     * Giải phóng reservation hết hạn của một ghế nếu ghế chưa được thanh toán.
     *
     * @param seat ghế cần kiểm tra.
     * @param now thời điểm hiện tại dùng để so sánh hạn khóa.
     * @param broadcast có gửi thông báo realtime sau khi giải phóng hay không.
     * @return {@code true} nếu đã giải phóng, ngược lại {@code false}.
     */
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
