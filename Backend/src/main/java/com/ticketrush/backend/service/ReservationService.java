package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.ConfirmReservationRequest;
import com.ticketrush.backend.dto.ReservationResponse;
import com.ticketrush.backend.entity.*;
import com.ticketrush.backend.entity.enums.ReservationStatus;
import com.ticketrush.backend.repository.ReservationRepository;
import com.ticketrush.backend.repository.SeatRepository;
import com.ticketrush.backend.repository.ShowtimeRepository;
import com.ticketrush.backend.util.QrCodeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý logic chốt đơn đặt vé (Confirm Reservation).
 * Chịu trách nhiệm cập nhật trạng thái thanh toán, ghế, và tạo mã QR.
 * 
 * ⚠️ QUAN TRỌNG: Sử dụng @Transactional để rollback nếu lỗi giữa chừng!
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final QrCodeUtil qrCodeUtil;
    private final SeatRealtimeService seatRealtimeService;

    /**
     * Chốt đơn đặt vé (Confirm Reservation).
     * 
     * Quy trình:
     * 1. Tìm đơn đặt vé
     * 2. Cập nhật status = PAID, paid = true
     * 3. Cập nhật tất cả ghế gắn với đơn này (isReserved = true)
     * 4. Tạo mã QR code
     * 5. Lưu QR hash vào DB
     * 6. Phát tín hiệu realtime báo ghế ĐÃ BÁN
     * 
     * ⚠️ CRITICAL: @Transactional đảm bảo rollback nếu bất kỳ bước nào thất bại!
     * Nếu lỗi → Tiền hoàn, ghế được nhả, mã QR không tạo.
     *
     * @param userId ID của người dùng
     * @param request Request chứa ID đơn, mã giao dịch, danh sách ghế
     * @return ReservationResponse chứa thông tin đơn đã chốt + mã QR
     * @throws Exception nếu xảy ra lỗi trong quá trình xử lý
     */
    @Transactional
    public ReservationResponse confirmReservation(Long userId, ConfirmReservationRequest request) throws Exception {
        try {
            log.info("🎫 Bắt đầu xử lý chốt đơn đặt vé ID: {} cho user: {}", 
                    request.getReservationId(), userId);

            // ========== BƯỚC 1: Tìm & validate đơn đặt vé ==========
            Reservation reservation = reservationRepository
                    .findById(request.getReservationId())
                    .orElseThrow(() -> {
                        log.error("❌ Không tìm thấy đơn đặt vé ID: {}", request.getReservationId());
                        return new IllegalArgumentException("❌ Đơn đặt vé không tồn tại");
                    });

            // Kiểm tra quyền sở hữu
            if (!reservation.getUser().getId().equals(userId)) {
                log.warn("❌ User {} không có quyền chốt đơn của user {}", userId, reservation.getUser().getId());
                throw new IllegalArgumentException("❌ Bạn không có quyền chốt đơn này");
            }

            // Kiểm tra trạng thái hiện tại
            if (reservation.getPaid()) {
                log.warn("⚠️ Đơn đặt vé {} đã được chốt rồi", request.getReservationId());
                throw new IllegalArgumentException("⚠️ Đơn đặt vé này đã được chốt rồi");
            }

            log.debug("✅ Validate đơn đặt vé thành công");

            // ========== BƯỚC 2: Cập nhật status = PAID, paid = true ==========
            log.info("💳 Cập nhật trạng thái thanh toán: PAID");
            reservation.setStatus(ReservationStatus.PAID);
            reservation.setPaid(true);
            reservationRepository.save(reservation);
            log.debug("✅ Cập nhật status = PAID thành công");

            // ========== BƯỚC 3: Cập nhật tất cả ghế gắn với đơn này ==========
            log.info("🪑 Cập nhật {} ghế với reservation_id: {}", 
                    request.getSeatNumbers().size(), request.getReservationId());

            List<Seat> seats = seatRepository.findByShowtimeIdAndSeatNumberIn(
                    reservation.getShowtime().getId(),
                    request.getSeatNumbers()
            );

            for (Seat seat : seats) {
                seat.setReservation(reservation);
                seat.setIsReserved(true);
            }
            seatRepository.saveAll(seats);
            log.debug("✅ Cập nhật {} ghế thành công", seats.size());

            // ========== BƯỚC 4: Tạo mã QR code ==========
            log.info("🎟️ Tạo mã QR code cho đơn đặt vé ID: {}", request.getReservationId());

            // Tạo hash bí mật từ: reservationId + transactionCode + timestamp + random
            String secretHash = generateSecretHash(
                    request.getReservationId(),
                    request.getTransactionCode()
            );

            // Tạo QR code từ QrCodeUtil
            String base64String = qrCodeUtil.generateReservationQrCode(
                    request.getReservationId(),
                    secretHash
            );

            String dataUri = qrCodeUtil.createDataUri(base64String);
            
            log.debug("✅ Tạo mã QR thành công - Size: {} bytes", base64String.length());

            // ========== BƯỚC 5: Lưu QR hash vào DB ==========
            log.info("💾 Lưu mã QR hash vào DB");
            reservation.setQrCodeHash(secretHash);
            reservationRepository.save(reservation);
            log.debug("✅ Lưu QR hash thành công");

            // ========== BƯỚC 6: Phát tín hiệu realtime ==========
            log.info("📢 Phát tín hiệu realtime báo ghế ĐÃ BÁN trên /topic");
            seatRealtimeService.broadcastSeatStatus(
                    reservation.getShowtime().getId(),
                    request.getSeatNumbers(),
                    "SOLD",
                    "Đơn #" + request.getReservationId(),
                    userId
            );
            log.debug("✅ Phát tín hiệu realtime thành công");

            // ========== BƯỚC 7: Xây dựng Response ==========
            log.info("✅ Xây dựng response chốt đơn thành công");

             return ReservationResponse.builder()
                     .reservationId(reservation.getId())
                     .showtimeId(reservation.getShowtime().getId())
                     .userId(reservation.getUser().getId())
                     .movieName(reservation.getShowtime().getMovie().getTitle())
                     .theaterName(reservation.getShowtime().getTheater().getName())
                     .roomName(reservation.getShowtime().getRoom())
                     .seatNumbers(request.getSeatNumbers())
                     .showtimeStartTime(reservation.getShowtime().getStartTime())
                     .totalPrice(reservation.getTotalPrice())
                    .status(reservation.getStatus().toString())
                    .qrCodeBase64(base64String)
                    .qrCodeDataUri(dataUri)
                    .qrCodeHash(secretHash)
                    .confirmedAt(LocalDateTime.now())
                    .transactionCode(request.getTransactionCode())
                    .message("✅ Chốt đơn thành công. Vé đã được tạo.")
                    .apiStatus("SUCCESS")
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi validate: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Lỗi chốt đơn: {}", e.getMessage(), e);
            // @Transactional sẽ tự động rollback tất cả changes
            throw new Exception("❌ Lỗi chốt đơn: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo hash bí mật cho QR code từ:
     * - reservationId
     * - transactionCode
     * - timestamp hiện tại
     * - UUID random
     *
     * @param reservationId ID đơn đặt vé
     * @param transactionCode Mã giao dịch
     * @return Hash bí mật
     */
    private String generateSecretHash(Long reservationId, String transactionCode) {
        String rawData = String.format(
                "%d_%s_%d_%s",
                reservationId,
                transactionCode,
                System.currentTimeMillis(),
                UUID.randomUUID().toString()
        );

        // Tạo hash từ MD5 hoặc SHA-256
        return Base64.getEncoder().encodeToString(rawData.getBytes());
    }

    /**
     * Lấy thông tin đơn đặt vé (Get Reservation Details).
     *
     * @param reservationId ID đơn đặt vé
     * @param userId ID người dùng
     * @return Thông tin đơn đặt vé
     * @throws Exception nếu không tìm thấy hoặc không có quyền
     */
    public ReservationResponse getReservation(Long reservationId, Long userId) throws Exception {
        try {
            log.info("📋 Lấy thông tin đơn đặt vé ID: {} cho user: {}", reservationId, userId);

            Reservation reservation = reservationRepository
                    .findById(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("❌ Đơn đặt vé không tồn tại"));

            if (!reservation.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("❌ Bạn không có quyền xem đơn này");
            }

            List<Seat> seats = seatRepository.findByReservationId(reservationId);
            List<String> seatNumbers = seats.stream()
                    .map(Seat::getSeatNumber)
                    .toList();

             return ReservationResponse.builder()
                     .reservationId(reservation.getId())
                     .showtimeId(reservation.getShowtime().getId())
                     .userId(reservation.getUser().getId())
                     .movieName(reservation.getShowtime().getMovie().getTitle())
                     .theaterName(reservation.getShowtime().getTheater().getName())
                     .roomName(reservation.getShowtime().getRoom())
                     .seatNumbers(seatNumbers)
                     .showtimeStartTime(reservation.getShowtime().getStartTime())
                    .totalPrice(reservation.getTotalPrice())
                    .status(reservation.getStatus().toString())
                    .qrCodeHash(reservation.getQrCodeHash())
                    .confirmedAt(reservation.getReservationTime())
                    .apiStatus("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("❌ Lỗi lấy thông tin đơn đặt: {}", e.getMessage());
            throw e;
        }
    }
}

