package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.ConfirmReservationRequest;
import com.ticketrush.backend.dto.CreateReservationRequest;
import com.ticketrush.backend.dto.CreateReservationResponse;
import com.ticketrush.backend.dto.ReservationResponse;
import com.ticketrush.backend.dto.TicketResponse;
import com.ticketrush.backend.entity.*;
import com.ticketrush.backend.entity.enums.PaymentStatus;
import com.ticketrush.backend.entity.enums.ReservationStatus;
import com.ticketrush.backend.repository.PaymentRepository;
import com.ticketrush.backend.repository.ReservationRepository;
import com.ticketrush.backend.repository.SeatRepository;
import com.ticketrush.backend.repository.ShowtimeRepository;
import com.ticketrush.backend.repository.UserRepository;
import com.ticketrush.backend.repository.VoucherRepository;
import com.ticketrush.backend.util.QrCodeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final PaymentRepository paymentRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
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

            if (seats.size() != request.getSeatNumbers().size()) {
                throw new IllegalArgumentException("Mot hoac nhieu ghe khong ton tai trong suat chieu nay");
            }

            if (seats.stream().anyMatch(seat -> 
                    seat.getReservation() == null || !seat.getReservation().getId().equals(reservation.getId()))) {
                throw new IllegalArgumentException("Mot hoac nhieu ghe da bi nguoi khac dat hoac khong thuoc don hang nay");
            }

            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal basePrice = reservation.getShowtime().getPrice() != null ? reservation.getShowtime().getPrice() : BigDecimal.ZERO;
            for (Seat seat : seats) {
                totalPrice = totalPrice.add(basePrice.multiply(seat.getSeatType().getPriceMultiplier()));
            }

            Voucher voucher = resolveVoucher(request.getVoucherCode(), totalPrice);
            if (voucher != null) {
                BigDecimal discountAmount = totalPrice
                        .multiply(voucher.getDiscountPercentage())
                        .divide(BigDecimal.valueOf(100));
                if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                    discountAmount = voucher.getMaxDiscountAmount();
                }
                totalPrice = totalPrice.subtract(discountAmount).max(BigDecimal.ZERO);
                voucher.setCurrentUsage((voucher.getCurrentUsage() != null ? voucher.getCurrentUsage() : 0) + 1);
                voucherRepository.save(voucher);
                reservation.setVoucher(voucher);
            }

            reservation.setTotalPrice(totalPrice);

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
            Payment payment = paymentRepository.findByReservationId(reservation.getId());
            if (payment == null) {
                payment = new Payment();
                payment.setReservation(reservation);
            }
            payment.setTransactionReference(request.getTransactionCode());
            payment.setProvider(resolveProvider(request.getProvider()));
            payment.setAmount(reservation.getTotalPrice());
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

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
                     .roomName(reservation.getShowtime().getRoom().getName())
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

    private String resolveProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            return "VNPAY";
        }
        return provider.trim().toUpperCase();
    }

    private Voucher resolveVoucher(String voucherCode, BigDecimal totalPrice) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            return null;
        }

        Voucher voucher = voucherRepository.findByCodeIgnoreCase(voucherCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Voucher khong ton tai"));

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartTime().isAfter(now) || voucher.getEndTime().isBefore(now)) {
            throw new IllegalArgumentException("Voucher khong nam trong thoi gian su dung");
        }

        int currentUsage = voucher.getCurrentUsage() != null ? voucher.getCurrentUsage() : 0;
        if (currentUsage >= voucher.getMaxUsage()) {
            throw new IllegalArgumentException("Voucher da het luot su dung");
        }

        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tong tien khong hop le de ap voucher");
        }

        return voucher;
    }

    /**
     * API Tạo Đơn Hàng (Init Reservation) - VỀ LỖ HỔNG 1
     * 
     * Logic:
     * 1. Validate thời gian chiếu (chặn 15 phút trước suất chiếu)
     * 2. Tính tiền kèm loại ghế (dùng priceMultiplier)
     * 3. Xử lý voucher nếu có
     * 4. Lưu database với status_id = 1 (PENDING)
     * 5. Trả về thông tin đơn vừa tạo
     *
     * @param userId ID người dùng
     * @param request Request chứa showtimeId, seatNumbers, voucherCode
     * @return CreateReservationResponse chứa thông tin đơn vừa tạo
     * @throws Exception nếu xảy ra lỗi
     */
    @Transactional
    public CreateReservationResponse createReservation(Long userId, CreateReservationRequest request) throws Exception {
        try {
            log.info("🎫 Bắt đầu tạo đơn đặt vé cho user: {} với suất chiếu: {}", userId, request.getShowtimeId());

            // ========== BƯỚC 1: Validate input ==========
            if (request.getShowtimeId() == null || request.getShowtimeId() <= 0) {
                throw new IllegalArgumentException("❌ ID suất chiếu không hợp lệ");
            }
            if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
                throw new IllegalArgumentException("❌ Danh sách ghế không được để trống");
            }

            // ========== BƯỚC 2: Lấy showtime và validate thời gian ==========
            log.info("🎬 Kiểm tra suất chiếu ID: {}", request.getShowtimeId());
            Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                    .orElseThrow(() -> new IllegalArgumentException("❌ Suất chiếu không tồn tại"));

            // VỀ LỖ HỔNG 1: Chặn thời gian chiếu
            LocalDateTime startTime = showtime.getStartTime();
            LocalDateTime deadlineTime = startTime.minusMinutes(15);
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(deadlineTime)) {
                log.warn("⏰ Đã quá thời gian mở bán: {} (deadline: {})", now, deadlineTime);
                throw new IllegalArgumentException("❌ Đã đóng quầy bán vé. Suất chiếu bắt đầu lúc: " + startTime);
            }
            log.info("✅ Thời gian hợp lệ. Deadline: {}", deadlineTime);

            // ========== BƯỚC 3: Validate ghế và tính tiền kèm Loại Ghế ==========
            log.info("🪑 Validate {} ghế", request.getSeatNumbers().size());
            List<Seat> seats = seatRepository.findByShowtimeIdAndSeatNumberIn(
                    showtime.getId(),
                    request.getSeatNumbers()
            );

            if (seats.size() != request.getSeatNumbers().size()) {
                throw new IllegalArgumentException("❌ Một hoặc nhiều ghế không tồn tại trong suất chiếu này");
            }

            // Kiểm tra ghế đã có người giữ (isReserved = true hoặc có reservation_id)
            if (seats.stream().anyMatch(seat -> Boolean.TRUE.equals(seat.getIsReserved()) || seat.getReservation() != null)) {
                throw new IllegalArgumentException("❌ Một hoặc nhiều ghế đã được đặt hoặc có người đang giữ chỗ");
            }

            // VỀ LỖ HỔNG 1: Tính tiền kèm Loại Ghế (dùng priceMultiplier)
            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal basePrice = showtime.getPrice() != null ? showtime.getPrice() : BigDecimal.ZERO;
            for (Seat seat : seats) {
                BigDecimal seatPrice = basePrice.multiply(seat.getSeatType().getPriceMultiplier());
                totalPrice = totalPrice.add(seatPrice);
                log.debug("  - Ghế {}: {} x {} = {}", 
                        seat.getSeatNumber(), 
                        basePrice, 
                        seat.getSeatType().getPriceMultiplier(), 
                        seatPrice);
            }
            log.info("💰 Tổng tiền = {}", totalPrice);

            // ========== BƯỚC 4: Xử lý Voucher (Mới thêm ở V3) ==========
            BigDecimal discountAmount = BigDecimal.ZERO;
            Voucher appliedVoucher = null;

            if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
                log.info("🎁 Validate voucher: {}", request.getVoucherCode());
                appliedVoucher = resolveVoucher(request.getVoucherCode(), totalPrice);
                if (appliedVoucher != null) {
                    // Tính tiền được giảm
                    discountAmount = totalPrice
                            .multiply(appliedVoucher.getDiscountPercentage())
                            .divide(BigDecimal.valueOf(100));
                    
                    // Kiểm tra nếu discount > max_discount_amount thì chỉ trừ bằng giá trị max
                    if (appliedVoucher.getMaxDiscountAmount() != null && 
                        discountAmount.compareTo(appliedVoucher.getMaxDiscountAmount()) > 0) {
                        discountAmount = appliedVoucher.getMaxDiscountAmount();
                        log.info("  Giảm tối đa: {}", discountAmount);
                    } else {
                        log.info("  Giảm: {} ({} %)", discountAmount, appliedVoucher.getDiscountPercentage());
                    }
                }
            }

            BigDecimal finalPrice = totalPrice.subtract(discountAmount).max(BigDecimal.ZERO);
            log.info("💳 Giá cuối: {} - {} = {}", totalPrice, discountAmount, finalPrice);

            // ========== BƯỚC 5: Lưu Database ==========
            log.info("💾 Lưu đơn đặt vé vào database");

            // Lấy user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("❌ Người dùng không tồn tại"));

            // Tạo reservation mới với status = PENDING (LOCKED in enum = 1)
            Reservation reservation = new Reservation();
            reservation.setUser(user);
            reservation.setShowtime(showtime);
            reservation.setStatus(ReservationStatus.LOCKED); // status_id = 1 (PENDING state)
            reservation.setTotalPrice(finalPrice);
            reservation.setVoucher(appliedVoucher);
            reservation.setPaid(false);
            // Đặt thời gian hết hạn = now + 15 phút
            reservation.setExpiresAt(now.plusMinutes(15));

            Reservation savedReservation = reservationRepository.save(reservation);
            log.info("✅ Đơn đặt vé đã tạo: ID = {}", savedReservation.getId());

            // Cập nhật reservation_id vào các ghế đã chọn và đánh dấu là đang được giữ (isReserved = true)
            for (Seat seat : seats) {
                seat.setReservation(savedReservation);
                seat.setIsReserved(true);
            }
            seatRepository.saveAll(seats);
            log.info("✅ Cập nhật {} ghế thành công", seats.size());

            // ========== BƯỚC 6: Xây dựng Response ==========
            return CreateReservationResponse.builder()
                    .reservationId(savedReservation.getId())
                    .showtimeId(showtime.getId())
                    .movieName(showtime.getMovie().getTitle())
                    .theaterName(showtime.getTheater().getName())
                    .roomName(showtime.getRoom().getName())
                    .seatNumbers(request.getSeatNumbers())
                    .showtimeStartTime(startTime)
                    .totalPrice(totalPrice)
                    .discountAmount(discountAmount)
                    .finalPrice(finalPrice)
                    .voucherCode(appliedVoucher != null ? appliedVoucher.getCode() : null)
                    .status(savedReservation.getStatus().toString()) // LOCKED
                    .expiresAt(savedReservation.getExpiresAt())
                    .message("✅ Tạo đơn đặt vé thành công. Vui lòng thanh toán trong 15 phút.")
                    .apiStatus("SUCCESS")
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi validate: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Lỗi tạo đơn đặt vé: {}", e.getMessage(), e);
            throw new Exception("❌ Lỗi tạo đơn đặt vé: " + e.getMessage(), e);
        }
    }

    /**
     * API Vé Của Tôi (My Tickets) - VỀ LỖ HỔNG 2
     * 
     * Logic:
     * 1. Lấy email từ userEmail (gọi từ controller)
     * 2. Tìm user từ email
     * 3. Lấy danh sách Reservation của user
     * 4. Map sang DTO TicketResponse
     * 5. Bắt buộc phải có roomName - "Rạp: Beta Cinemas - Phòng: IMAX 01"
     *
     * @param userEmail Email của người dùng hiện tại (lấy từ Security Context)
     * @return List<TicketResponse> danh sách vé của người dùng
     * @throws Exception nếu không tìm thấy user
     */
    public List<TicketResponse> getUserReservations(String userEmail) throws Exception {
        try {
            log.info("🎫 Lấy danh sách vé của user: {}", userEmail);

            // Tìm user từ email
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("❌ Người dùng không tồn tại"));

            // Lấy danh sách reservation của user
            List<Reservation> reservations = reservationRepository.findByUserIdOrderByReservationTimeDesc(user.getId());
            log.info("📋 Tìm được {} đơn đặt vé", reservations.size());

            // Map sang DTO TicketResponse
            return reservations.stream()
                    .map(this::mapToTicketResponse)
                    .toList();

        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách vé: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Map Reservation entity sang TicketResponse DTO
     * VỀ LỖ HỔNG 2: Bắt buộc phải có roomName để khách biết đường đi
     * Format: "Rạp: Beta Cinemas - Phòng: IMAX 01"
     */
    private TicketResponse mapToTicketResponse(Reservation reservation) {
        String theaterName = reservation.getShowtime().getTheater().getName();
        String roomName = reservation.getShowtime().getRoom().getName();
        String location = String.format("Rạp: %s - Phòng: %s", theaterName, roomName);

        // Lấy danh sách ghế
        List<Seat> seats = seatRepository.findByReservationId(reservation.getId());
        List<String> seatNumbers = seats.stream()
                .map(Seat::getSeatNumber)
                .toList();

        return TicketResponse.builder()
                .reservationId(reservation.getId())
                .showtimeId(reservation.getShowtime().getId())
                .movieName(reservation.getShowtime().getMovie().getTitle())
                .theaterName(theaterName)
                .roomName(roomName)
                .location(location)
                .seatNumbers(seatNumbers)
                .showtimeStartTime(reservation.getShowtime().getStartTime())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus().toString())
                .reservationTime(reservation.getReservationTime())
                .qrCodeHash(reservation.getQrCodeHash())
                .build();
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
                     .roomName(reservation.getShowtime().getRoom().getName())
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

    /**
     * VỀ LỖ HỔNG 3: Payment Callback từ Payment Gateway
     * 
     * API: POST /v1/payments/callback (HIDDEN - chỉ payment gateway gọi)
     * 
     * Luồng xử lý:
     * 1. Validate callback từ payment gateway
     * 2. Verify transactionCode + amount khớp
     * 3. Cập nhật Reservation status = PAID
     * 4. Cập nhật Seat status = RESERVED
     * 5. Tạo QR code và lưu vào DB
     * 6. Phát tín hiệu realtime báo ghế ĐÃ BÁN
     * 
     * ⚠️ CRITICAL: Chỉ payment gateway được gọi API này (check IP/Secret)
     *
     * @param request PaymentCallbackRequest từ payment gateway
     * @return Response xác nhận đã xử lý
     * @throws Exception nếu validate fail hoặc reservation không tồn tại
     */
    @Transactional
    public ReservationResponse handlePaymentCallback(com.ticketrush.backend.dto.PaymentCallbackRequest request) throws Exception {
        try {
            log.info("💳 Payment Callback từ {}: Transaction {} cho Reservation {}", 
                    request.getProvider(), request.getTransactionCode(), request.getReservationId());

            // ========== BƯỚC 1: Validate request ==========
            if (request.getReservationId() == null || request.getReservationId() <= 0) {
                throw new IllegalArgumentException("❌ ID đơn đặt vé không hợp lệ");
            }
            if (request.getTransactionCode() == null || request.getTransactionCode().trim().isEmpty()) {
                throw new IllegalArgumentException("❌ Mã giao dịch không hợp lệ");
            }
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("❌ Số tiền không hợp lệ");
            }

            // ========== BƯỚC 2: Tìm Reservation và verify amount ==========
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> {
                        log.error("❌ Không tìm thấy đơn đặt vé ID: {}", request.getReservationId());
                        return new IllegalArgumentException("❌ Đơn đặt vé không tồn tại");
                    });

            // Kiểm tra amount khớp
            if (reservation.getTotalPrice().compareTo(request.getAmount()) != 0) {
                log.error("❌ Số tiền không khớp. Expected: {}, Received: {}", 
                        reservation.getTotalPrice(), request.getAmount());
                throw new IllegalArgumentException("❌ Số tiền thanh toán không khớp");
            }

            // Kiểm tra xem đã được chốt rồi (idempotency)
            if (reservation.getPaid()) {
                log.warn("⚠️ Đơn đặt vé {} đã được chốt rồi", request.getReservationId());
                throw new IllegalArgumentException("⚠️ Đơn đặt vé này đã được chốt rồi");
            }

            log.debug("✅ Validate callback thành công");

            // ========== BƯỚC 3: Kiểm tra payment status ==========
            if (!"SUCCESS".equalsIgnoreCase(request.getPaymentStatus())) {
                log.warn("❌ Thanh toán thất bại: {}", request.getPaymentStatus());
                reservation.setStatus(ReservationStatus.CANCELED);
                reservationRepository.save(reservation);
                throw new IllegalArgumentException("❌ Thanh toán thất bại: " + request.getPaymentStatus());
            }

            // ========== BƯỚC 4: Cập nhật status = PAID, paid = true ==========
            log.info("✅ Payment SUCCESS từ {}. Cập nhật reservation status = PAID", request.getProvider());
            reservation.setStatus(ReservationStatus.PAID);
            reservation.setPaid(true);
            reservationRepository.save(reservation);
            log.debug("✅ Cập nhật status = PAID thành công");

            // ========== BƯỚC 5: Cập nhật tất cả ghế gắn với đơn này ==========
            log.info("🪑 Cập nhật ghế với reservation_id: {}", request.getReservationId());
            List<Seat> seats = seatRepository.findByReservationId(reservation.getId());

            for (Seat seat : seats) {
                seat.setIsReserved(true);
            }
            seatRepository.saveAll(seats);
            log.debug("✅ Cập nhật {} ghế thành công", seats.size());

            // ========== BƯỚC 6: Tạo mã QR code ==========
            log.info("🎟️ Tạo mã QR code cho đơn đặt vé ID: {}", request.getReservationId());

            String secretHash = generateSecretHash(
                    request.getReservationId(),
                    request.getTransactionCode()
            );

            String base64String = qrCodeUtil.generateReservationQrCode(
                    request.getReservationId(),
                    secretHash
            );

            String dataUri = qrCodeUtil.createDataUri(base64String);
            log.debug("✅ Tạo mã QR thành công - Size: {} bytes", base64String.length());

            // ========== BƯỚC 7: Lưu QR hash vào DB ==========
            log.info("💾 Lưu mã QR hash và payment info vào DB");
            reservation.setQrCodeHash(secretHash);
            reservationRepository.save(reservation);

            // Lưu payment info
            Payment payment = paymentRepository.findByReservationId(reservation.getId());
            if (payment == null) {
                payment = new Payment();
                payment.setReservation(reservation);
            }
            payment.setTransactionReference(request.getTransactionCode());
            payment.setProvider(request.getProvider() != null ? request.getProvider().toUpperCase() : "UNKNOWN");
            payment.setAmount(request.getAmount());
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            log.debug("✅ Lưu payment info thành công");

            // ========== BƯỚC 8: Phát tín hiệu realtime ==========
            log.info("📢 Phát tín hiệu realtime báo ghế ĐÃ BÁN trên /topic");
            List<String> seatNumbers = seats.stream()
                    .map(Seat::getSeatNumber)
                    .toList();

            seatRealtimeService.broadcastSeatStatus(
                    reservation.getShowtime().getId(),
                    seatNumbers,
                    "SOLD",
                    "Đơn #" + request.getReservationId(),
                    reservation.getUser().getId()
            );
            log.debug("✅ Phát tín hiệu realtime thành công");

            // ========== BƯỚC 9: Xây dựng Response ==========
            log.info("✅ Xử lý callback thành công");

            return ReservationResponse.builder()
                    .reservationId(reservation.getId())
                    .showtimeId(reservation.getShowtime().getId())
                    .userId(reservation.getUser().getId())
                    .movieName(reservation.getShowtime().getMovie().getTitle())
                    .theaterName(reservation.getShowtime().getTheater().getName())
                    .roomName(reservation.getShowtime().getRoom().getName())
                    .seatNumbers(seatNumbers)
                    .showtimeStartTime(reservation.getShowtime().getStartTime())
                    .totalPrice(reservation.getTotalPrice())
                    .status(reservation.getStatus().toString())
                    .qrCodeBase64(base64String)
                    .qrCodeDataUri(dataUri)
                    .qrCodeHash(secretHash)
                    .confirmedAt(LocalDateTime.now())
                    .transactionCode(request.getTransactionCode())
                    .message("✅ Callback thành công. Vé đã được tạo.")
                    .apiStatus("SUCCESS")
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi validate callback: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Lỗi xử lý callback: {}", e.getMessage(), e);
            throw new Exception("❌ Lỗi xử lý callback: " + e.getMessage(), e);
        }
    }

    /**
     * Hủy đơn đặt vé do người dùng yêu cầu
     */
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn đặt vé không tồn tại"));

        // Chỉ user tạo đơn (hoặc Admin - tuỳ logic) mới được hủy
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Không có quyền hủy đơn này");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalArgumentException("Đơn đã bị hủy từ trước");
        }
        
        // Cập nhật trạng thái
        reservation.setStatus(ReservationStatus.CANCELED);
        
        // Nhả ghế
        List<Seat> seats = seatRepository.findByReservationId(reservationId);
        for (Seat seat : seats) {
            seat.setIsReserved(false);
            seat.setReservation(null);
        }
        seatRepository.saveAll(seats);

        // Broadcast realtime
        if (!seats.isEmpty()) {
            List<String> seatNumbers = seats.stream().map(Seat::getSeatNumber).toList();
            seatRealtimeService.broadcastSeatStatus(
                    reservation.getShowtime().getId(),
                    seatNumbers,
                    "AVAILABLE",
                    "Đơn bị hủy bởi người dùng",
                    null
            );
        }

        reservationRepository.save(reservation);
        log.info("✅ Đã hủy đơn {} và giải phóng {} ghế", reservationId, seats.size());
    }

    /**
     * Tự động dọn dẹp các đơn đặt vé đã quá hạn giữ ghế (chạy mỗi phút).
     * Giải phóng ghế cho người khác mua.
     */
    @Transactional
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findExpiredLockedReservations(now);

        if (expiredReservations.isEmpty()) {
            return;
        }

        log.info("🧹 Đang dọn dẹp {} đơn đặt vé quá hạn giữ ghế", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            log.info("  - Hủy đơn: {}, hết hạn lúc: {}", reservation.getId(), reservation.getExpiresAt());
            reservation.setStatus(ReservationStatus.CANCELED);
            
            // Lấy danh sách ghế đang bị đơn này giữ
            List<Seat> seats = seatRepository.findByReservationId(reservation.getId());
            for (Seat seat : seats) {
                seat.setIsReserved(false);
                seat.setReservation(null);
            }
            seatRepository.saveAll(seats);

            // Gửi realtime báo ghế trống lại
            if (!seats.isEmpty()) {
                List<String> seatNumbers = seats.stream().map(Seat::getSeatNumber).toList();
                seatRealtimeService.broadcastSeatStatus(
                        reservation.getShowtime().getId(),
                        seatNumbers,
                        "AVAILABLE",
                        "Đơn #" + reservation.getId() + " quá hạn",
                        null
                );
            }
        }

        reservationRepository.saveAll(expiredReservations);
        log.info("✅ Dọn dẹp hoàn tất");
    }
}

