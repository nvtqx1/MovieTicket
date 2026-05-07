package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.HoldSeatRequest;
import com.ticketrush.backend.dto.HoldSeatResponse;
import com.ticketrush.backend.entity.*;
import com.ticketrush.backend.entity.enums.ReservationStatus;
import com.ticketrush.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ========================================================
 * BOOKING SERVICE — CORE CONCURRENCY LOGIC
 * ========================================================
 * 
 * Task 3.1: Pessimistic Locking cho Hold-Seat
 * Task 3.3: Mock Payment Checkout
 * 
 * NGUYÊN LÝ PESSIMISTIC LOCK:
 * ────────────────────────────
 * Khi 2 request đồng thời muốn giữ cùng 1 ghế:
 * 
 *   Thread 1: BEGIN → SELECT * FROM seats WHERE ... FOR UPDATE → Khóa row!
 *   Thread 2: BEGIN → SELECT * FROM seats WHERE ... FOR UPDATE → ĐANG CHỜ...
 *   Thread 1: UPDATE is_reserved = true → COMMIT → Nhả lock
 *   Thread 2: Bây giờ đọc được → Thấy is_reserved = true → REJECT!
 * 
 * Lock timeout: 3 giây (nếu Thread 1 xử lý quá lâu → Thread 2 timeout)
 * Hold timeout: 10 phút (nếu user không thanh toán → CronJob nhả ghế)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SeatRealtimeService seatRealtimeService;

    /** Thời gian giữ ghế: 10 phút */
    private static final int HOLD_DURATION_MINUTES = 10;

    /**
     * ═══════════════════════════════════════════════
     * TASK 3.1: HOLD SEAT — Với PESSIMISTIC LOCKING
     * ═══════════════════════════════════════════════
     * 
     * Đảm bảo: Nếu 2 request đến cùng mili-giây cho cùng 1 ghế,
     * chỉ 1 request thành công.
     * 
     * @param userId ID người dùng
     * @param request {showtimeId, seatNumbers}
     * @return HoldSeatResponse
     */
    @Transactional
    public HoldSeatResponse holdSeats(Long userId, HoldSeatRequest request) {
        log.info("🔒 [HOLD-SEAT] User {} yêu cầu giữ ghế {} cho showtime {}",
                userId, request.getSeatNumbers(), request.getShowtimeId());

        // 1. Validate showtime tồn tại
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại"));

        // 2. Validate user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // ═══════════════════════════════════════════════════
        // 3. PESSIMISTIC LOCK — Đây là điểm mấu chốt!
        // ═══════════════════════════════════════════════════
        // SeatRepository.findByShowtimeIdAndSeatNumberIn() 
        // có annotation @Lock(PESSIMISTIC_WRITE)
        // → MySQL sẽ chạy: SELECT ... FOR UPDATE
        // → Thread khác phải ĐỢI nếu cùng query các row này
        List<Seat> seats = seatRepository.findByShowtimeIdAndSeatNumberIn(
                request.getShowtimeId(), request.getSeatNumbers());

        // 4. Validate tất cả ghế tồn tại
        if (seats.size() != request.getSeatNumbers().size()) {
            log.warn("⚠️ Một số ghế không tồn tại trong suất chiếu {}", request.getShowtimeId());
            return HoldSeatResponse.builder()
                    .apiStatus("FAILED")
                    .message("Một hoặc nhiều ghế không tồn tại")
                    .build();
        }

        // 5. CHECK: Có ghế nào đã bị giữ chưa?
        // Vì đang ở trong Pessimistic Lock, ta đọc được giá trị MỚI NHẤT
        List<Seat> alreadyReserved = seats.stream()
                .filter(Seat::getIsReserved)
                .toList();

        if (!alreadyReserved.isEmpty()) {
            List<String> takenSeats = alreadyReserved.stream()
                    .map(Seat::getSeatNumber)
                    .toList();
            log.warn("❌ Ghế đã bị giữ: {}", takenSeats);

            return HoldSeatResponse.builder()
                    .apiStatus("FAILED")
                    .message("Ghế " + String.join(", ", takenSeats) + " đã được người khác giữ")
                    .build();
        }

        // 6. Tạo Reservation (status = LOCKED)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_DURATION_MINUTES);

        // Tính tổng giá
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Seat seat : seats) {
            BigDecimal basePrice = showtime.getPrice() != null ? showtime.getPrice() : BigDecimal.ZERO;
            BigDecimal multiplier = seat.getSeatType() != null && seat.getSeatType().getPriceMultiplier() != null
                    ? seat.getSeatType().getPriceMultiplier()
                    : BigDecimal.ONE;
            totalPrice = totalPrice.add(basePrice.multiply(multiplier));
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setShowtime(showtime);
        reservation.setStatus(ReservationStatus.LOCKED);
        reservation.setTotalPrice(totalPrice);
        reservation.setPaid(false);
        reservation.setExpiresAt(expiresAt);
        reservation = reservationRepository.save(reservation);

        // 7. MARK ghế là RESERVED + gắn reservation
        for (Seat seat : seats) {
            seat.setIsReserved(true);
            seat.setReservation(reservation);
        }
        seatRepository.saveAll(seats);

        // 8. Cập nhật available seats
        showtime.setAvailableSeats(showtime.getAvailableSeats() - seats.size());
        showtimeRepository.save(showtime);

        // 9. BROADCAST qua WebSocket → Tất cả user khác thấy ghế chuyển xám
        seatRealtimeService.broadcastSeatStatus(
                request.getShowtimeId(),
                request.getSeatNumbers(),
                "LOCKED",
                "Ghế đã được giữ",
                userId
        );

        log.info("✅ [HOLD-SEAT] Giữ thành công {} ghế cho user {}, hết hạn lúc {}",
                seats.size(), userId, expiresAt);

        return HoldSeatResponse.builder()
                .apiStatus("SUCCESS")
                .message("Giữ ghế thành công! Bạn có " + HOLD_DURATION_MINUTES + " phút để thanh toán.")
                .reservationId(reservation.getId())
                .heldSeats(request.getSeatNumbers())
                .expiresAt(expiresAt)
                .holdDurationSeconds(HOLD_DURATION_MINUTES * 60L)
                .build();
    }

    /**
     * ═══════════════════════════════════════════════
     * TASK 3.3: MOCK CHECKOUT — Thanh toán mô phỏng
     * ═══════════════════════════════════════════════
     * 
     * POST /v1/booking/checkout/{reservationId}
     * Chuyển status: LOCKED → PAID (không gọi cổng thanh toán thật)
     * 
     * @param userId ID người dùng
     * @param reservationId ID đơn đặt vé
     * @param paymentMethod Phương thức thanh toán (VD: "MOMO", "VNPAY", "CASH")
     */
    @Transactional
    public Map<String, Object> mockCheckout(Long userId, Long reservationId, String paymentMethod) {
        log.info("💳 [CHECKOUT] User {} thanh toán đơn {} bằng {}",
                userId, reservationId, paymentMethod);

        // 1. Tìm reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));

        // 2. Validate quyền
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền thanh toán đơn này");
        }

        // 3. Check status
        if (reservation.getStatus() != ReservationStatus.LOCKED) {
            throw new IllegalArgumentException("Đơn này không ở trạng thái chờ thanh toán (Status: " + reservation.getStatus() + ")");
        }

        // 4. Check hết hạn
        if (reservation.getExpiresAt() != null && reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Đơn đặt vé đã hết hạn. Vui lòng đặt lại.");
        }

        // 5. MOCK PAYMENT — Luôn thành công
        reservation.setStatus(ReservationStatus.PAID);
        reservation.setPaid(true);
        reservation.setExpiresAt(null); // Không hết hạn nữa

        // 6. Generate QR Code
        String qrHash = "TICKET_" + reservationId + "_" + UUID.randomUUID().toString().substring(0, 8);
        reservation.setQrCodeHash(qrHash);
        reservationRepository.save(reservation);

        // 7. Broadcast SOLD qua WebSocket
        List<Seat> seats = seatRepository.findByReservationId(reservationId);
        List<String> seatNumbers = seats.stream().map(Seat::getSeatNumber).toList();

        seatRealtimeService.broadcastSeatStatus(
                reservation.getShowtime().getId(),
                seatNumbers,
                "SOLD",
                "Đơn #" + reservationId + " đã thanh toán",
                userId
        );

        log.info("✅ [CHECKOUT] Thanh toán thành công đơn {} - QR: {}", reservationId, qrHash);

        // 8. QR hash is stored - FE can use ticket-detail API to get the QR image
        String qrImageDataUri = "";

        return Map.of(
                "apiStatus", "SUCCESS",
                "message", "Thanh toán thành công!",
                "reservationId", reservationId,
                "paymentMethod", paymentMethod != null ? paymentMethod : "CASH",
                "qrCodeHash", qrHash,
                "qrCodeDataUri", qrImageDataUri != null ? qrImageDataUri : "",
                "seatNumbers", seatNumbers,
                "totalPrice", reservation.getTotalPrice()
        );
    }
}
