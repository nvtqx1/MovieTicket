package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.request.ConfirmReservationRequest;
import com.ticketrush.backend.dto.request.CreateReservationRequest;
import com.ticketrush.backend.dto.response.CreateReservationResponse;
import com.ticketrush.backend.dto.response.ReservationResponse;
import com.ticketrush.backend.dto.response.TicketResponse;
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
 * Service xÃ¡Â»Â­ lÃƒÂ½ logic chÃ¡Â»â€˜t Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© (Confirm Reservation).
 * ChÃ¡Â»â€¹u trÃƒÂ¡ch nhiÃ¡Â»â€¡m cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t trÃ¡ÂºÂ¡ng thÃƒÂ¡i thanh toÃƒÂ¡n, ghÃ¡ÂºÂ¿, vÃƒÂ  tÃ¡ÂºÂ¡o mÃƒÂ£ QR.
 *
 * Ã¢Å¡Â Ã¯Â¸Â QUAN TRÃ¡Â»Å’NG: SÃ¡Â»Â­ dÃ¡Â»Â¥ng @Transactional Ã„â€˜Ã¡Â»Æ’ rollback nÃ¡ÂºÂ¿u lÃ¡Â»â€”i giÃ¡Â»Â¯a chÃ¡Â»Â«ng!
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
     * ChÃ¡Â»â€˜t Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© (Confirm Reservation).
     *
     * Quy trÃƒÂ¬nh:
     * 1. TÃƒÂ¬m Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©
     * 2. CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t status = PAID, paid = true
     * 3. CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ ghÃ¡ÂºÂ¿ gÃ¡ÂºÂ¯n vÃ¡Â»â€ºi Ã„â€˜Ã†Â¡n nÃƒÂ y (isReserved = true)
     * 4. TÃ¡ÂºÂ¡o mÃƒÂ£ QR code
     * 5. LÃ†Â°u QR hash vÃƒÂ o DB
     * 6. PhÃƒÂ¡t tÃƒÂ­n hiÃ¡Â»â€¡u realtime bÃƒÂ¡o ghÃ¡ÂºÂ¿ Ã„ÂÃƒÆ’ BÃƒÂN
     *
     * Ã¢Å¡Â Ã¯Â¸Â CRITICAL: @Transactional Ã„â€˜Ã¡ÂºÂ£m bÃ¡ÂºÂ£o rollback nÃ¡ÂºÂ¿u bÃ¡ÂºÂ¥t kÃ¡Â»Â³ bÃ†Â°Ã¡Â»â€ºc nÃƒÂ o thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i!
     * NÃ¡ÂºÂ¿u lÃ¡Â»â€”i Ã¢â€ â€™ TiÃ¡Â»Ân hoÃƒÂ n, ghÃ¡ÂºÂ¿ Ã„â€˜Ã†Â°Ã¡Â»Â£c nhÃ¡ÂºÂ£, mÃƒÂ£ QR khÃƒÂ´ng tÃ¡ÂºÂ¡o.
     *
     * @param userId ID cÃ¡Â»Â§a ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng
     * @param request Request chÃ¡Â»Â©a ID Ã„â€˜Ã†Â¡n, mÃƒÂ£ giao dÃ¡Â»â€¹ch, danh sÃƒÂ¡ch ghÃ¡ÂºÂ¿
     * @return ReservationResponse chÃ¡Â»Â©a thÃƒÂ´ng tin Ã„â€˜Ã†Â¡n Ã„â€˜ÃƒÂ£ chÃ¡Â»â€˜t + mÃƒÂ£ QR
     * @throws Exception nÃ¡ÂºÂ¿u xÃ¡ÂºÂ£y ra lÃ¡Â»â€”i trong quÃƒÂ¡ trÃƒÂ¬nh xÃ¡Â»Â­ lÃƒÂ½
     */
    @Transactional
    public ReservationResponse confirmReservation(Long userId, ConfirmReservationRequest request) throws Exception {
        try {
            log.info("Ã°Å¸Å½Â« BÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u xÃ¡Â»Â­ lÃƒÂ½ chÃ¡Â»â€˜t Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© ID: {} cho user: {}",
                    request.getReservationId(), userId);

            // ========== BÃ†Â¯Ã¡Â»Å¡C 1: TÃƒÂ¬m & validate Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© ==========
            Reservation reservation = reservationRepository
                    .findById(request.getReservationId())
                    .orElseThrow(() -> {
                        log.error("Ã¢ÂÅ’ KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© ID: {}", request.getReservationId());
                        return new IllegalArgumentException("Ã¢ÂÅ’ Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i");
                    });

            // KiÃ¡Â»Æ’m tra quyÃ¡Â»Ân sÃ¡Â»Å¸ hÃ¡Â»Â¯u
            if (!reservation.getUser().getId().equals(userId)) {
                log.warn("Ã¢ÂÅ’ User {} khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân chÃ¡Â»â€˜t Ã„â€˜Ã†Â¡n cÃ¡Â»Â§a user {}", userId, reservation.getUser().getId());
                throw new IllegalArgumentException("Ã¢ÂÅ’ BÃ¡ÂºÂ¡n khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân chÃ¡Â»â€˜t Ã„â€˜Ã†Â¡n nÃƒÂ y");
            }

            // KiÃ¡Â»Æ’m tra trÃ¡ÂºÂ¡ng thÃƒÂ¡i hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i
            if (reservation.getPaid()) {
                log.warn("Ã¢Å¡Â Ã¯Â¸Â Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© {} Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c chÃ¡Â»â€˜t rÃ¡Â»â€œi", request.getReservationId());
                throw new IllegalArgumentException("Ã¢Å¡Â Ã¯Â¸Â Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© nÃƒÂ y Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c chÃ¡Â»â€˜t rÃ¡Â»â€œi");
            }

            log.debug("Ã¢Å“â€¦ Validate Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© thÃƒÂ nh cÃƒÂ´ng");

            // ========== BÃ†Â¯Ã¡Â»Å¡C 2: CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t status = PAID, paid = true ==========
            log.info("Ã°Å¸â€™Â³ CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t trÃ¡ÂºÂ¡ng thÃƒÂ¡i thanh toÃƒÂ¡n: PAID");
            reservation.setStatus(ReservationStatus.PAID);
            reservation.setPaid(true);
            reservationRepository.save(reservation);
            log.debug("Ã¢Å“â€¦ CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t status = PAID thÃƒÂ nh cÃƒÂ´ng");

            // ========== BÃ†Â¯Ã¡Â»Å¡C 3: CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ ghÃ¡ÂºÂ¿ gÃ¡ÂºÂ¯n vÃ¡Â»â€ºi Ã„â€˜Ã†Â¡n nÃƒÂ y ==========
            log.info("Ã°Å¸Âªâ€˜ CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t {} ghÃ¡ÂºÂ¿ vÃ¡Â»â€ºi reservation_id: {}",
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
            log.debug("Ã¢Å“â€¦ CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t {} ghÃ¡ÂºÂ¿ thÃƒÂ nh cÃƒÂ´ng", seats.size());

            // ========== BÃ†Â¯Ã¡Â»Å¡C 4: TÃ¡ÂºÂ¡o mÃƒÂ£ QR code ==========
            log.info("Ã°Å¸Å½Å¸Ã¯Â¸Â TÃ¡ÂºÂ¡o mÃƒÂ£ QR code cho Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© ID: {}", request.getReservationId());

            // TÃ¡ÂºÂ¡o hash bÃƒÂ­ mÃ¡ÂºÂ­t tÃ¡Â»Â«: reservationId + transactionCode + timestamp + random
            String secretHash = generateSecretHash(
                    request.getReservationId(),
                    request.getTransactionCode()
            );

            // TÃ¡ÂºÂ¡o QR code tÃ¡Â»Â« QrCodeUtil
            String base64String = qrCodeUtil.generateReservationQrCode(
                    request.getReservationId(),
                    secretHash
            );

            String dataUri = qrCodeUtil.createDataUri(base64String);

            log.debug("Ã¢Å“â€¦ TÃ¡ÂºÂ¡o mÃƒÂ£ QR thÃƒÂ nh cÃƒÂ´ng - Size: {} bytes", base64String.length());

            // ========== BÃ†Â¯Ã¡Â»Å¡C 5: LÃ†Â°u QR hash vÃƒÂ o DB ==========
            log.info("Ã°Å¸â€™Â¾ LÃ†Â°u mÃƒÂ£ QR hash vÃƒÂ o DB");
            reservation.setQrCodeHash(secretHash);
            reservationRepository.save(reservation);
            log.debug("Ã¢Å“â€¦ LÃ†Â°u QR hash thÃƒÂ nh cÃƒÂ´ng");

            // ========== BÃ†Â¯Ã¡Â»Å¡C 6: PhÃƒÂ¡t tÃƒÂ­n hiÃ¡Â»â€¡u realtime ==========
            log.info("Ã°Å¸â€œÂ¢ PhÃƒÂ¡t tÃƒÂ­n hiÃ¡Â»â€¡u realtime bÃƒÂ¡o ghÃ¡ÂºÂ¿ Ã„ÂÃƒÆ’ BÃƒÂN trÃƒÂªn /topic");
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
                    "Ã„ÂÃ†Â¡n #" + request.getReservationId(),
                    userId
            );
            log.debug("Ã¢Å“â€¦ PhÃƒÂ¡t tÃƒÂ­n hiÃ¡Â»â€¡u realtime thÃƒÂ nh cÃƒÂ´ng");

            // ========== BÃ†Â¯Ã¡Â»Å¡C 7: XÃƒÂ¢y dÃ¡Â»Â±ng Response ==========
            log.info("Ã¢Å“â€¦ XÃƒÂ¢y dÃ¡Â»Â±ng response chÃ¡Â»â€˜t Ã„â€˜Ã†Â¡n thÃƒÂ nh cÃƒÂ´ng");

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
                    .message("Ã¢Å“â€¦ ChÃ¡Â»â€˜t Ã„â€˜Ã†Â¡n thÃƒÂ nh cÃƒÂ´ng. VÃƒÂ© Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃ¡ÂºÂ¡o.")
                    .apiStatus("SUCCESS")
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("Ã¢Å¡Â Ã¯Â¸Â LÃ¡Â»â€”i validate: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ LÃ¡Â»â€”i chÃ¡Â»â€˜t Ã„â€˜Ã†Â¡n: {}", e.getMessage(), e);
            // @Transactional sÃ¡ÂºÂ½ tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng rollback tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ changes
            throw new Exception("Ã¢ÂÅ’ LÃ¡Â»â€”i chÃ¡Â»â€˜t Ã„â€˜Ã†Â¡n: " + e.getMessage(), e);
        }
    }

    /**
     * TÃ¡ÂºÂ¡o hash bÃƒÂ­ mÃ¡ÂºÂ­t cho QR code tÃ¡Â»Â«:
     * - reservationId
     * - transactionCode
     * - timestamp hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i
     * - UUID random
     *
     * @param reservationId ID Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©
     * @param transactionCode MÃƒÂ£ giao dÃ¡Â»â€¹ch
     * @return Hash bÃƒÂ­ mÃ¡ÂºÂ­t
     */
    private String generateSecretHash(Long reservationId, String transactionCode) {
        String rawData = String.format(
                "%d_%s_%d_%s",
                reservationId,
                transactionCode,
                System.currentTimeMillis(),
                UUID.randomUUID().toString()
        );

        // TÃ¡ÂºÂ¡o hash tÃ¡Â»Â« MD5 hoÃ¡ÂºÂ·c SHA-256
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
     * API TÃ¡ÂºÂ¡o Ã„ÂÃ†Â¡n HÃƒÂ ng (Init Reservation) - VÃ¡Â»â‚¬ LÃ¡Â»â€“ HÃ¡Â»â€NG 1
     *
     * Logic:
     * 1. Validate thÃ¡Â»Âi gian chiÃ¡ÂºÂ¿u (chÃ¡ÂºÂ·n 15 phÃƒÂºt trÃ†Â°Ã¡Â»â€ºc suÃ¡ÂºÂ¥t chiÃ¡ÂºÂ¿u)
     * 2. TÃƒÂ­nh tiÃ¡Â»Ân kÃƒÂ¨m loÃ¡ÂºÂ¡i ghÃ¡ÂºÂ¿ (dÃƒÂ¹ng priceMultiplier)
     * 3. XÃ¡Â»Â­ lÃƒÂ½ voucher nÃ¡ÂºÂ¿u cÃƒÂ³
     * 4. LÃ†Â°u database vÃ¡Â»â€ºi status_id = 1 (PENDING)
     * 5. TrÃ¡ÂºÂ£ vÃ¡Â»Â thÃƒÂ´ng tin Ã„â€˜Ã†Â¡n vÃ¡Â»Â«a tÃ¡ÂºÂ¡o
     *
     * @param userId ID ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng
     * @param request Request chÃ¡Â»Â©a showtimeId, seatNumbers, voucherCode
     * @return CreateReservationResponse chÃ¡Â»Â©a thÃƒÂ´ng tin Ã„â€˜Ã†Â¡n vÃ¡Â»Â«a tÃ¡ÂºÂ¡o
     * @throws Exception nÃ¡ÂºÂ¿u xÃ¡ÂºÂ£y ra lÃ¡Â»â€”i
     */
    @Transactional
    public CreateReservationResponse createReservation(Long userId, CreateReservationRequest request) throws Exception {
        try {
            log.info("Ã°Å¸Å½Â« BÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u tÃ¡ÂºÂ¡o Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© cho user: {} vÃ¡Â»â€ºi suÃ¡ÂºÂ¥t chiÃ¡ÂºÂ¿u: {}", userId, request.getShowtimeId());

            // ========== BÃ†Â¯Ã¡Â»Å¡C 1: Validate input ==========
            if (request.getShowtimeId() == null || request.getShowtimeId() <= 0) {
                throw new IllegalArgumentException("Ã¢ÂÅ’ ID suÃ¡ÂºÂ¥t chiÃ¡ÂºÂ¿u khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡");
            }
            if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
                throw new IllegalArgumentException("Ã¢ÂÅ’ Danh sÃƒÂ¡ch ghÃ¡ÂºÂ¿ khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng");
            }

            // ========== BÃ†Â¯Ã¡Â»Å¡C 2: LÃ¡ÂºÂ¥y showtime vÃƒÂ  validate thÃ¡Â»Âi gian ==========
            log.info("Ã°Å¸Å½Â¬ KiÃ¡Â»Æ’m tra suÃ¡ÂºÂ¥t chiÃ¡ÂºÂ¿u ID: {}", request.getShowtimeId());
            Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                    .orElseThrow(() -> new IllegalArgumentException("Ã¢ÂÅ’ SuÃ¡ÂºÂ¥t chiÃ¡ÂºÂ¿u khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i"));

            // VÃ¡Â»â‚¬ LÃ¡Â»â€“ HÃ¡Â»â€NG 1: ChÃ¡ÂºÂ·n thÃ¡Â»Âi gian chiÃ¡ÂºÂ¿u
            LocalDateTime startTime = showtime.getStartTime();
            LocalDateTime deadlineTime = startTime.minusMinutes(15);
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(deadlineTime)) {
                log.warn("Ã¢ÂÂ° Ã„ÂÃƒÂ£ quÃƒÂ¡ thÃ¡Â»Âi gian mÃ¡Â»Å¸ bÃƒÂ¡n: {} (deadline: {})", now, deadlineTime);
                throw new IllegalArgumentException("Ã¢ÂÅ’ Ã„ÂÃƒÂ£ Ã„â€˜ÃƒÂ³ng quÃ¡ÂºÂ§y bÃƒÂ¡n vÃƒÂ©. SuÃ¡ÂºÂ¥t chiÃ¡ÂºÂ¿u bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u lÃƒÂºc: " + startTime);
            }
            log.info("Ã¢Å“â€¦ ThÃ¡Â»Âi gian hÃ¡Â»Â£p lÃ¡Â»â€¡. Deadline: {}", deadlineTime);

            // ========== BÃ†Â¯Ã¡Â»Å¡C 3: Validate ghÃ¡ÂºÂ¿ vÃƒÂ  tÃƒÂ­nh tiÃ¡Â»Ân kÃƒÂ¨m LoÃ¡ÂºÂ¡i GhÃ¡ÂºÂ¿ ==========
            log.info("Ã°Å¸Âªâ€˜ Validate {} ghÃ¡ÂºÂ¿", request.getSeatNumbers().size());
            List<Seat> seats = seatRepository.findByShowtimeIdAndSeatNumberIn(
                    showtime.getId(),
                    request.getSeatNumbers()
            );

            if (seats.size() != request.getSeatNumbers().size()) {
                throw new IllegalArgumentException("Ã¢ÂÅ’ MÃ¡Â»â„¢t hoÃ¡ÂºÂ·c nhiÃ¡Â»Âu ghÃ¡ÂºÂ¿ khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i trong suÃ¡ÂºÂ¥t chiÃ¡ÂºÂ¿u nÃƒÂ y");
            }

            // KiÃ¡Â»Æ’m tra ghÃ¡ÂºÂ¿ Ã„â€˜ÃƒÂ£ cÃƒÂ³ ngÃ†Â°Ã¡Â»Âi giÃ¡Â»Â¯ (isReserved = true hoÃ¡ÂºÂ·c cÃƒÂ³ reservation_id)
            if (seats.stream().anyMatch(seat -> Boolean.TRUE.equals(seat.getIsReserved()) || seat.getReservation() != null)) {
                throw new IllegalArgumentException("Ã¢ÂÅ’ MÃ¡Â»â„¢t hoÃ¡ÂºÂ·c nhiÃ¡Â»Âu ghÃ¡ÂºÂ¿ Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡ÂºÂ·t hoÃ¡ÂºÂ·c cÃƒÂ³ ngÃ†Â°Ã¡Â»Âi Ã„â€˜ang giÃ¡Â»Â¯ chÃ¡Â»â€”");
            }

            // VÃ¡Â»â‚¬ LÃ¡Â»â€“ HÃ¡Â»â€NG 1: TÃƒÂ­nh tiÃ¡Â»Ân kÃƒÂ¨m LoÃ¡ÂºÂ¡i GhÃ¡ÂºÂ¿ (dÃƒÂ¹ng priceMultiplier)
            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal basePrice = showtime.getPrice() != null ? showtime.getPrice() : BigDecimal.ZERO;
            for (Seat seat : seats) {
                BigDecimal seatPrice = basePrice.multiply(seat.getSeatType().getPriceMultiplier());
                totalPrice = totalPrice.add(seatPrice);
                log.debug("  - GhÃ¡ÂºÂ¿ {}: {} x {} = {}",
                        seat.getSeatNumber(),
                        basePrice,
                        seat.getSeatType().getPriceMultiplier(),
                        seatPrice);
            }
            log.info("Ã°Å¸â€™Â° TÃ¡Â»â€¢ng tiÃ¡Â»Ân = {}", totalPrice);

            // ========== BÃ†Â¯Ã¡Â»Å¡C 4: XÃ¡Â»Â­ lÃƒÂ½ Voucher (MÃ¡Â»â€ºi thÃƒÂªm Ã¡Â»Å¸ V3) ==========
            BigDecimal discountAmount = BigDecimal.ZERO;
            Voucher appliedVoucher = null;

            if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
                log.info("Ã°Å¸Å½Â Validate voucher: {}", request.getVoucherCode());
                appliedVoucher = resolveVoucher(request.getVoucherCode(), totalPrice);
                if (appliedVoucher != null) {
                    // TÃƒÂ­nh tiÃ¡Â»Ân Ã„â€˜Ã†Â°Ã¡Â»Â£c giÃ¡ÂºÂ£m
                    discountAmount = totalPrice
                            .multiply(appliedVoucher.getDiscountPercentage())
                            .divide(BigDecimal.valueOf(100));

                    // KiÃ¡Â»Æ’m tra nÃ¡ÂºÂ¿u discount > max_discount_amount thÃƒÂ¬ chÃ¡Â»â€° trÃ¡Â»Â« bÃ¡ÂºÂ±ng giÃƒÂ¡ trÃ¡Â»â€¹ max
                    if (appliedVoucher.getMaxDiscountAmount() != null &&
                        discountAmount.compareTo(appliedVoucher.getMaxDiscountAmount()) > 0) {
                        discountAmount = appliedVoucher.getMaxDiscountAmount();
                        log.info("  GiÃ¡ÂºÂ£m tÃ¡Â»â€˜i Ã„â€˜a: {}", discountAmount);
                    } else {
                        log.info("  GiÃ¡ÂºÂ£m: {} ({} %)", discountAmount, appliedVoucher.getDiscountPercentage());
                    }
                }
            }

            BigDecimal finalPrice = totalPrice.subtract(discountAmount).max(BigDecimal.ZERO);
            log.info("Ã°Å¸â€™Â³ GiÃƒÂ¡ cuÃ¡Â»â€˜i: {} - {} = {}", totalPrice, discountAmount, finalPrice);

            // ========== BÃ†Â¯Ã¡Â»Å¡C 5: LÃ†Â°u Database ==========
            log.info("Ã°Å¸â€™Â¾ LÃ†Â°u Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© vÃƒÂ o database");

            // LÃ¡ÂºÂ¥y user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Ã¢ÂÅ’ NgÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i"));

            // TÃ¡ÂºÂ¡o reservation mÃ¡Â»â€ºi vÃ¡Â»â€ºi status = PENDING (LOCKED in enum = 1)
            Reservation reservation = new Reservation();
            reservation.setUser(user);
            reservation.setShowtime(showtime);
            reservation.setStatus(ReservationStatus.LOCKED); // status_id = 1 (PENDING state)
            reservation.setTotalPrice(finalPrice);
            reservation.setVoucher(appliedVoucher);
            reservation.setPaid(false);
            // Ã„ÂÃ¡ÂºÂ·t thÃ¡Â»Âi gian hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n = now + 15 phÃƒÂºt
            reservation.setExpiresAt(now.plusMinutes(15));

            Reservation savedReservation = reservationRepository.save(reservation);
            log.info("Ã¢Å“â€¦ Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© Ã„â€˜ÃƒÂ£ tÃ¡ÂºÂ¡o: ID = {}", savedReservation.getId());

            // CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t reservation_id vÃƒÂ o cÃƒÂ¡c ghÃ¡ÂºÂ¿ Ã„â€˜ÃƒÂ£ chÃ¡Â»Ân vÃƒÂ  Ã„â€˜ÃƒÂ¡nh dÃ¡ÂºÂ¥u lÃƒÂ  Ã„â€˜ang Ã„â€˜Ã†Â°Ã¡Â»Â£c giÃ¡Â»Â¯ (isReserved = true)
            for (Seat seat : seats) {
                seat.setReservation(savedReservation);
                seat.setIsReserved(true);
            }
            seatRepository.saveAll(seats);
            log.info("Ã¢Å“â€¦ CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t {} ghÃ¡ÂºÂ¿ thÃƒÂ nh cÃƒÂ´ng", seats.size());

            // ========== BÃ†Â¯Ã¡Â»Å¡C 6: XÃƒÂ¢y dÃ¡Â»Â±ng Response ==========
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
                    .message("Ã¢Å“â€¦ TÃ¡ÂºÂ¡o Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© thÃƒÂ nh cÃƒÂ´ng. Vui lÃƒÂ²ng thanh toÃƒÂ¡n trong 15 phÃƒÂºt.")
                    .apiStatus("SUCCESS")
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("Ã¢Å¡Â Ã¯Â¸Â LÃ¡Â»â€”i validate: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ LÃ¡Â»â€”i tÃ¡ÂºÂ¡o Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©: {}", e.getMessage(), e);
            throw new Exception("Ã¢ÂÅ’ LÃ¡Â»â€”i tÃ¡ÂºÂ¡o Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©: " + e.getMessage(), e);
        }
    }

    /**
     * API VÃƒÂ© CÃ¡Â»Â§a TÃƒÂ´i (My Tickets) - VÃ¡Â»â‚¬ LÃ¡Â»â€“ HÃ¡Â»â€NG 2
     *
     * Logic:
     * 1. LÃ¡ÂºÂ¥y email tÃ¡Â»Â« userEmail (gÃ¡Â»Âi tÃ¡Â»Â« controller)
     * 2. TÃƒÂ¬m user tÃ¡Â»Â« email
     * 3. LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch Reservation cÃ¡Â»Â§a user
     * 4. Map sang DTO TicketResponse
     * 5. BÃ¡ÂºÂ¯t buÃ¡Â»â„¢c phÃ¡ÂºÂ£i cÃƒÂ³ roomName - "RÃ¡ÂºÂ¡p: Beta Cinemas - PhÃƒÂ²ng: IMAX 01"
     *
     * @param userEmail Email cÃ¡Â»Â§a ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i (lÃ¡ÂºÂ¥y tÃ¡Â»Â« Security Context)
     * @return List<TicketResponse> danh sÃƒÂ¡ch vÃƒÂ© cÃ¡Â»Â§a ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng
     * @throws Exception nÃ¡ÂºÂ¿u khÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y user
     */
    public List<TicketResponse> getUserReservations(String userEmail) throws Exception {
        try {
            log.info("Ã°Å¸Å½Â« LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch vÃƒÂ© cÃ¡Â»Â§a user: {}", userEmail);

            // TÃƒÂ¬m user tÃ¡Â»Â« email
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Ã¢ÂÅ’ NgÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i"));

            // LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch reservation cÃ¡Â»Â§a user
            List<Reservation> reservations = reservationRepository.findByUserIdOrderByReservationTimeDesc(user.getId());
            log.info("Ã°Å¸â€œâ€¹ TÃƒÂ¬m Ã„â€˜Ã†Â°Ã¡Â»Â£c {} Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©", reservations.size());

            // Map sang DTO TicketResponse
            return reservations.stream()
                    .map(this::mapToTicketResponse)
                    .toList();

        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ LÃ¡Â»â€”i lÃ¡ÂºÂ¥y danh sÃƒÂ¡ch vÃƒÂ©: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Map Reservation entity sang TicketResponse DTO
     * VÃ¡Â»â‚¬ LÃ¡Â»â€“ HÃ¡Â»â€NG 2: BÃ¡ÂºÂ¯t buÃ¡Â»â„¢c phÃ¡ÂºÂ£i cÃƒÂ³ roomName Ã„â€˜Ã¡Â»Æ’ khÃƒÂ¡ch biÃ¡ÂºÂ¿t Ã„â€˜Ã†Â°Ã¡Â»Âng Ã„â€˜i
     * Format: "RÃ¡ÂºÂ¡p: Beta Cinemas - PhÃƒÂ²ng: IMAX 01"
     */
    private TicketResponse mapToTicketResponse(Reservation reservation) {
        String theaterName = reservation.getShowtime().getTheater().getName();
        String roomName = reservation.getShowtime().getRoom().getName();
        String location = String.format("RÃ¡ÂºÂ¡p: %s - PhÃƒÂ²ng: %s", theaterName, roomName);

        // LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch ghÃ¡ÂºÂ¿
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
                .expiresAt(reservation.getExpiresAt())
                .qrCodeHash(reservation.getQrCodeHash())
                .build();
    }

    /**
     * LÃ¡ÂºÂ¥y thÃƒÂ´ng tin Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© (Get Reservation Details).
     *
     * @param reservationId ID Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©
     * @param userId ID ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng
     * @return ThÃƒÂ´ng tin Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©
     * @throws Exception nÃ¡ÂºÂ¿u khÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y hoÃ¡ÂºÂ·c khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân
     */
    public ReservationResponse getReservation(Long reservationId, Long userId) throws Exception {
        try {
            log.info("Ã°Å¸â€œâ€¹ LÃ¡ÂºÂ¥y thÃƒÂ´ng tin Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© ID: {} cho user: {}", reservationId, userId);

            Reservation reservation = reservationRepository
                    .findById(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("Ã¢ÂÅ’ Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i"));

            if (!reservation.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Ã¢ÂÅ’ BÃ¡ÂºÂ¡n khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân xem Ã„â€˜Ã†Â¡n nÃƒÂ y");
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
                    .expiresAt(reservation.getExpiresAt())
                    .apiStatus("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ LÃ¡Â»â€”i lÃ¡ÂºÂ¥y thÃƒÂ´ng tin Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * VÃ¡Â»â‚¬ LÃ¡Â»â€“ HÃ¡Â»â€NG 3: Payment Callback tÃ¡Â»Â« Payment Gateway
     *
     * API: POST /v1/payments/callback (HIDDEN - chÃ¡Â»â€° payment gateway gÃ¡Â»Âi)
     *
     * LuÃ¡Â»â€œng xÃ¡Â»Â­ lÃƒÂ½:
     * 1. Validate callback tÃ¡Â»Â« payment gateway
     * 2. Verify transactionCode + amount khÃ¡Â»â€ºp
     * 3. CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t Reservation status = PAID
     * 4. CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t Seat status = RESERVED
     * 5. TÃ¡ÂºÂ¡o QR code vÃƒÂ  lÃ†Â°u vÃƒÂ o DB
     * 6. PhÃƒÂ¡t tÃƒÂ­n hiÃ¡Â»â€¡u realtime bÃƒÂ¡o ghÃ¡ÂºÂ¿ Ã„ÂÃƒÆ’ BÃƒÂN
     *
     * Ã¢Å¡Â Ã¯Â¸Â CRITICAL: ChÃ¡Â»â€° payment gateway Ã„â€˜Ã†Â°Ã¡Â»Â£c gÃ¡Â»Âi API nÃƒÂ y (check IP/Secret)
     *
     * @param request PaymentCallbackRequest tÃ¡Â»Â« payment gateway
     * @return Response xÃƒÂ¡c nhÃ¡ÂºÂ­n Ã„â€˜ÃƒÂ£ xÃ¡Â»Â­ lÃƒÂ½
     * @throws Exception nÃ¡ÂºÂ¿u validate fail hoÃ¡ÂºÂ·c reservation khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i
     */
    @Transactional
    public ReservationResponse handlePaymentCallback(com.ticketrush.backend.dto.request.PaymentCallbackRequest request) throws Exception {
        try {
            log.info("Ã°Å¸â€™Â³ Payment Callback tÃ¡Â»Â« {}: Transaction {} cho Reservation {}",
                    request.getProvider(), request.getTransactionCode(), request.getReservationId());

            // ========== BÃ†Â¯Ã¡Â»Å¡C 1: Validate request ==========
            if (request.getReservationId() == null || request.getReservationId() <= 0) {
                throw new IllegalArgumentException("Ã¢ÂÅ’ ID Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡");
            }
            if (request.getTransactionCode() == null || request.getTransactionCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Ã¢ÂÅ’ MÃƒÂ£ giao dÃ¡Â»â€¹ch khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡");
            }
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Ã¢ÂÅ’ SÃ¡Â»â€˜ tiÃ¡Â»Ân khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡");
            }

            // ========== BÃ†Â¯Ã¡Â»Å¡C 2: TÃƒÂ¬m Reservation vÃƒÂ  verify amount ==========
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> {
                        log.error("Ã¢ÂÅ’ KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© ID: {}", request.getReservationId());
                        return new IllegalArgumentException("Ã¢ÂÅ’ Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i");
                    });

            // KiÃ¡Â»Æ’m tra amount khÃ¡Â»â€ºp
            if (reservation.getTotalPrice().compareTo(request.getAmount()) != 0) {
                log.error("Ã¢ÂÅ’ SÃ¡Â»â€˜ tiÃ¡Â»Ân khÃƒÂ´ng khÃ¡Â»â€ºp. Expected: {}, Received: {}",
                        reservation.getTotalPrice(), request.getAmount());
                throw new IllegalArgumentException("Ã¢ÂÅ’ SÃ¡Â»â€˜ tiÃ¡Â»Ân thanh toÃƒÂ¡n khÃƒÂ´ng khÃ¡Â»â€ºp");
            }

            // KiÃ¡Â»Æ’m tra xem Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c chÃ¡Â»â€˜t rÃ¡Â»â€œi (idempotency)
            if (reservation.getPaid()) {
                log.warn("Ã¢Å¡Â Ã¯Â¸Â Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© {} Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c chÃ¡Â»â€˜t rÃ¡Â»â€œi", request.getReservationId());
                throw new IllegalArgumentException("Ã¢Å¡Â Ã¯Â¸Â Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© nÃƒÂ y Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c chÃ¡Â»â€˜t rÃ¡Â»â€œi");
            }

            log.debug("Ã¢Å“â€¦ Validate callback thÃƒÂ nh cÃƒÂ´ng");

            // ========== BÃ†Â¯Ã¡Â»Å¡C 3: KiÃ¡Â»Æ’m tra payment status ==========
            if (!"SUCCESS".equalsIgnoreCase(request.getPaymentStatus())) {
                log.warn("Ã¢ÂÅ’ Thanh toÃƒÂ¡n thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i: {}", request.getPaymentStatus());
                reservation.setStatus(ReservationStatus.CANCELED);
                reservationRepository.save(reservation);
                throw new IllegalArgumentException("Ã¢ÂÅ’ Thanh toÃƒÂ¡n thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i: " + request.getPaymentStatus());
            }

            // ========== BÃ†Â¯Ã¡Â»Å¡C 4: CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t status = PAID, paid = true ==========
            log.info("Ã¢Å“â€¦ Payment SUCCESS tÃ¡Â»Â« {}. CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t reservation status = PAID", request.getProvider());
            reservation.setStatus(ReservationStatus.PAID);
            reservation.setPaid(true);
            reservationRepository.save(reservation);
            log.debug("Ã¢Å“â€¦ CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t status = PAID thÃƒÂ nh cÃƒÂ´ng");

            // ========== BÃ†Â¯Ã¡Â»Å¡C 5: CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ ghÃ¡ÂºÂ¿ gÃ¡ÂºÂ¯n vÃ¡Â»â€ºi Ã„â€˜Ã†Â¡n nÃƒÂ y ==========
            log.info("Ã°Å¸Âªâ€˜ CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t ghÃ¡ÂºÂ¿ vÃ¡Â»â€ºi reservation_id: {}", request.getReservationId());
            List<Seat> seats = seatRepository.findByReservationId(reservation.getId());

            for (Seat seat : seats) {
                seat.setIsReserved(true);
            }
            seatRepository.saveAll(seats);
            log.debug("Ã¢Å“â€¦ CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t {} ghÃ¡ÂºÂ¿ thÃƒÂ nh cÃƒÂ´ng", seats.size());

            // ========== BÃ†Â¯Ã¡Â»Å¡C 6: TÃ¡ÂºÂ¡o mÃƒÂ£ QR code ==========
            log.info("Ã°Å¸Å½Å¸Ã¯Â¸Â TÃ¡ÂºÂ¡o mÃƒÂ£ QR code cho Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© ID: {}", request.getReservationId());

            String secretHash = generateSecretHash(
                    request.getReservationId(),
                    request.getTransactionCode()
            );

            String base64String = qrCodeUtil.generateReservationQrCode(
                    request.getReservationId(),
                    secretHash
            );

            String dataUri = qrCodeUtil.createDataUri(base64String);
            log.debug("Ã¢Å“â€¦ TÃ¡ÂºÂ¡o mÃƒÂ£ QR thÃƒÂ nh cÃƒÂ´ng - Size: {} bytes", base64String.length());

            // ========== BÃ†Â¯Ã¡Â»Å¡C 7: LÃ†Â°u QR hash vÃƒÂ o DB ==========
            log.info("Ã°Å¸â€™Â¾ LÃ†Â°u mÃƒÂ£ QR hash vÃƒÂ  payment info vÃƒÂ o DB");
            reservation.setQrCodeHash(secretHash);
            reservationRepository.save(reservation);

            // LÃ†Â°u payment info
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

            log.debug("Ã¢Å“â€¦ LÃ†Â°u payment info thÃƒÂ nh cÃƒÂ´ng");

            // ========== BÃ†Â¯Ã¡Â»Å¡C 8: PhÃƒÂ¡t tÃƒÂ­n hiÃ¡Â»â€¡u realtime ==========
            log.info("Ã°Å¸â€œÂ¢ PhÃƒÂ¡t tÃƒÂ­n hiÃ¡Â»â€¡u realtime bÃƒÂ¡o ghÃ¡ÂºÂ¿ Ã„ÂÃƒÆ’ BÃƒÂN trÃƒÂªn /topic");
            List<String> seatNumbers = seats.stream()
                    .map(Seat::getSeatNumber)
                    .toList();

            seatRealtimeService.broadcastSeatStatus(
                    reservation.getShowtime().getId(),
                    seatNumbers,
                    "SOLD",
                    "Ã„ÂÃ†Â¡n #" + request.getReservationId(),
                    reservation.getUser().getId()
            );
            log.debug("Ã¢Å“â€¦ PhÃƒÂ¡t tÃƒÂ­n hiÃ¡Â»â€¡u realtime thÃƒÂ nh cÃƒÂ´ng");

            // ========== BÃ†Â¯Ã¡Â»Å¡C 9: XÃƒÂ¢y dÃ¡Â»Â±ng Response ==========
            log.info("Ã¢Å“â€¦ XÃ¡Â»Â­ lÃƒÂ½ callback thÃƒÂ nh cÃƒÂ´ng");

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
                    .message("Ã¢Å“â€¦ Callback thÃƒÂ nh cÃƒÂ´ng. VÃƒÂ© Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c tÃ¡ÂºÂ¡o.")
                    .apiStatus("SUCCESS")
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("Ã¢Å¡Â Ã¯Â¸Â LÃ¡Â»â€”i validate callback: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ LÃ¡Â»â€”i xÃ¡Â»Â­ lÃƒÂ½ callback: {}", e.getMessage(), e);
            throw new Exception("Ã¢ÂÅ’ LÃ¡Â»â€”i xÃ¡Â»Â­ lÃƒÂ½ callback: " + e.getMessage(), e);
        }
    }

    /**
     * HÃ¡Â»Â§y Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© do ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng yÃƒÂªu cÃ¡ÂºÂ§u
     */
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i"));

        // ChÃ¡Â»â€° user tÃ¡ÂºÂ¡o Ã„â€˜Ã†Â¡n (hoÃ¡ÂºÂ·c Admin - tuÃ¡Â»Â³ logic) mÃ¡Â»â€ºi Ã„â€˜Ã†Â°Ã¡Â»Â£c hÃ¡Â»Â§y
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("KhÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân hÃ¡Â»Â§y Ã„â€˜Ã†Â¡n nÃƒÂ y");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalArgumentException("Ã„ÂÃ†Â¡n Ã„â€˜ÃƒÂ£ bÃ¡Â»â€¹ hÃ¡Â»Â§y tÃ¡Â»Â« trÃ†Â°Ã¡Â»â€ºc");
        }

        List<Seat> seats = releaseReservationSeats(reservation, "Don bi huy boi nguoi dung", userId);
        reservationRepository.save(reservation);
        log.info("Ã¢Å“â€¦ Ã„ÂÃƒÂ£ hÃ¡Â»Â§y Ã„â€˜Ã†Â¡n {} vÃƒÂ  giÃ¡ÂºÂ£i phÃƒÂ³ng {} ghÃ¡ÂºÂ¿", reservationId, seats.size());
    }

    private List<Seat> releaseReservationSeats(Reservation reservation, String realtimeMessage, Long actorUserId) {
        reservation.setStatus(ReservationStatus.CANCELED);
        reservation.setPaid(false);
        reservation.setExpiresAt(null);

        List<Seat> seats = seatRepository.findByReservationId(reservation.getId());
        for (Seat seat : seats) {
            seat.setIsReserved(false);
            seat.setReservation(null);
        }
        seatRepository.saveAll(seats);

        Showtime showtime = reservation.getShowtime();
        if (showtime != null && !seats.isEmpty()) {
            int currentAvailable = showtime.getAvailableSeats() != null ? showtime.getAvailableSeats() : 0;
            int totalSeats = showtime.getTotalSeats() != null ? showtime.getTotalSeats() : currentAvailable + seats.size();
            showtime.setAvailableSeats(Math.min(totalSeats, currentAvailable + seats.size()));
            showtimeRepository.save(showtime);

            List<String> seatNumbers = seats.stream().map(Seat::getSeatNumber).toList();
            seatRealtimeService.broadcastSeatStatus(
                    showtime.getId(),
                    seatNumbers,
                    "AVAILABLE",
                    realtimeMessage,
                    actorUserId
            );
        }

        return seats;
    }

    /**
     * TÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng dÃ¡Â»Ân dÃ¡ÂºÂ¹p cÃƒÂ¡c Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© Ã„â€˜ÃƒÂ£ quÃƒÂ¡ hÃ¡ÂºÂ¡n giÃ¡Â»Â¯ ghÃ¡ÂºÂ¿ (chÃ¡ÂºÂ¡y mÃ¡Â»â€”i phÃƒÂºt).
     * GiÃ¡ÂºÂ£i phÃƒÂ³ng ghÃ¡ÂºÂ¿ cho ngÃ†Â°Ã¡Â»Âi khÃƒÂ¡c mua.
     */
    @Transactional
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findExpiredLockedReservations(now);

        if (expiredReservations.isEmpty()) {
            return;
        }

        log.info("Ã°Å¸Â§Â¹ Ã„Âang dÃ¡Â»Ân dÃ¡ÂºÂ¹p {} Ã„â€˜Ã†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© quÃƒÂ¡ hÃ¡ÂºÂ¡n giÃ¡Â»Â¯ ghÃ¡ÂºÂ¿", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            log.info("  - HÃ¡Â»Â§y Ã„â€˜Ã†Â¡n: {}, hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n lÃƒÂºc: {}", reservation.getId(), reservation.getExpiresAt());
            releaseReservationSeats(reservation, "Don #" + reservation.getId() + " qua han", null);
        }

        reservationRepository.saveAll(expiredReservations);
        log.info("Ã¢Å“â€¦ DÃ¡Â»Ân dÃ¡ÂºÂ¹p hoÃƒÂ n tÃ¡ÂºÂ¥t");
    }

    /**
     * Task 2.3: LÃ¡ÂºÂ¥y chi tiÃ¡ÂºÂ¿t vÃƒÂ© Ã„â€˜Ã¡ÂºÂ§y Ã„â€˜Ã¡Â»Â§
     * TrÃ¡ÂºÂ£ vÃ¡Â»Â: TÃƒÂªn phim, RÃ¡ÂºÂ¡p, PhÃƒÂ²ng (Hall), DÃƒÂ£y (Row), SÃ¡Â»â€˜ ghÃ¡ÂºÂ¿ (Seat), GiÃ¡Â»Â chiÃ¡ÂºÂ¿u
     * DÃƒÂ¹ng Ã„â€˜Ã¡Â»Æ’ render UI vÃƒÂ© giÃ¡ÂºÂ¥y truyÃ¡Â»Ân thÃ¡Â»â€˜ng vÃƒÂ  mÃƒÂ£ hÃƒÂ³a QR Code
     */
    @Transactional(readOnly = true)
    public com.ticketrush.backend.dto.response.TicketDetailResponse getTicketDetail(Long reservationId, Long userId) {
        log.info("Ã°Å¸Å½Â« LÃ¡ÂºÂ¥y chi tiÃ¡ÂºÂ¿t vÃƒÂ© ID: {} cho user: {}", reservationId, userId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i"));

        // KiÃ¡Â»Æ’m tra quyÃ¡Â»Ân
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("BÃ¡ÂºÂ¡n khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân xem Ã„â€˜Ã†Â¡n nÃƒÂ y");
        }

        Showtime showtime = reservation.getShowtime();
        Movie movie = showtime.getMovie();
        Room room = showtime.getRoom();
        Theater theater = room.getTheater();

        // LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch ghÃ¡ÂºÂ¿
        List<Seat> seats = seatRepository.findByReservationId(reservationId);

        List<com.ticketrush.backend.dto.response.TicketDetailResponse.SeatDetail> seatDetails = seats.stream()
                .map(seat -> {
                    String seatNumber = seat.getSeatNumber();
                    // TÃƒÂ¡ch dÃƒÂ£y (Row) vÃƒÂ  sÃ¡Â»â€˜ ghÃ¡ÂºÂ¿ (Col) tÃ¡Â»Â« seatNumber VD "A12" Ã¢â€ â€™ row="A", col="12"
                    String row = seatNumber.replaceAll("[0-9]", "");
                    String col = seatNumber.replaceAll("[^0-9]", "");

                    return com.ticketrush.backend.dto.response.TicketDetailResponse.SeatDetail.builder()
                            .seatNumber(seatNumber)
                            .row(row)
                            .col(col)
                            .seatType(seat.getSeatType().getName())
                            .build();
                })
                .toList();

        // TÃ¡ÂºÂ¡o QR Code Data URI nÃ¡ÂºÂ¿u Ã„â€˜ÃƒÂ£ thanh toÃƒÂ¡n
        String qrDataUri = null;
        if (reservation.getQrCodeHash() != null) {
            try {
                String base64 = qrCodeUtil.generateQrCodeBase64(reservation.getQrCodeHash());
                qrDataUri = qrCodeUtil.createDataUri(base64);
            } catch (Exception e) {
                log.warn("Ã¢Å¡Â Ã¯Â¸Â KhÃƒÂ´ng thÃ¡Â»Æ’ tÃ¡ÂºÂ¡o QR Code: {}", e.getMessage());
            }
        }

        return com.ticketrush.backend.dto.response.TicketDetailResponse.builder()
                .reservationId(reservation.getId())
                .movieTitle(movie.getTitle())
                .moviePosterUrl(movie.getPosterImageUrl())
                .movieGenre(movie.getGenre())
                .theaterName(theater.getName())
                .theaterLocation(theater.getLocation())
                .roomName(room.getName())
                .seats(seatDetails)
                .showDate(showtime.getShowDate())
                .showTime(showtime.getShowTime())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus().toString())
                .qrCodeHash(reservation.getQrCodeHash())
                .qrCodeDataUri(qrDataUri)
                .build();
    }
}

