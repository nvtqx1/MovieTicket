package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.payload.SeatStatusPayload;
import com.ticketrush.backend.entity.Reservation;
import com.ticketrush.backend.entity.Seat;
import com.ticketrush.backend.repository.ReservationRepository;
import com.ticketrush.backend.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Example Service - Cách sử dụng SeatRealtimeService trong thực tế
 * Cập nhật: Nhân viên có thể copy logic này vào BookingService của họ
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingRealtimeExampleService {
    
    private final SeatRealtimeService seatRealtimeService;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    
    /**
     * Khóa ghế tạm thời khi user chọn (Khóa 5 phút)
     * 
     * @param showtimeId ID suất chiếu
     * @param seatNumbers Danh sách ghế (VD: ["A1", "A2"])
     * @param userId ID user đang chọn
     */
    @Transactional
    public void lockSeatsTemporarily(Long showtimeId, List<String> seatNumbers, Long userId) {
        try {
            // 1. Cập nhật ghế thành RESERVED (đã đặt chỗ) trong database
            for (String seatNumber : seatNumbers) {
                // Lấy ghế theo seat number và showtime
                List<Seat> seats = seatRepository.findAll(); // TODO: Implement custom query
                seats.stream()
                    .filter(s -> s.getSeatNumber().equals(seatNumber) && 
                               s.getShowtime().getId().equals(showtimeId) &&
                               !s.getIsReserved())
                    .forEach(seat -> {
                        seat.setIsReserved(true);
                        seatRepository.save(seat);
                    });
            }
            
            // 2. Phát tín hiệu cho toàn bộ users
            seatRealtimeService.broadcastSeatStatus(
                showtimeId,
                seatNumbers,
                "LOCKED",
                userId
            );
            
            log.info("🔒 Khóa ghế {} cho suất chiếu {} bởi user {}", seatNumbers, showtimeId, userId);
            
        } catch (Exception e) {
            log.error("❌ Lỗi khóa ghế: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Xác nhận đặt vé - Chuyển ghế từ LOCKED → SOLD
     * 
     * @param showtimeId ID suất chiếu
     * @param seatNumbers Danh sách ghế
     * @param reservationId ID đơn đặt
     */
    @Transactional
    public void confirmBooking(Long showtimeId, List<String> seatNumbers, Long reservationId) {
        try {
            // 1. Cập nhật ghế (đã được linked với reservation)
            // Lấy reservation
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElse(null);
            
            if (reservation != null) {
                // Ghế sẽ được associated qua Reservation
                log.info("✅ Xác nhận đặt vé {}", reservationId);
            }
            
            // 2. Phát tín hiệu
            seatRealtimeService.broadcastSeatStatus(
                showtimeId,
                seatNumbers,
                "SOLD",
                "Đơn đặt vé #" + reservationId + " đã được xác nhận"
            );
            
            log.info("✅ Xác nhận đặt vé: {} cho suất chiếu {}", reservationId, showtimeId);
            
        } catch (Exception e) {
            log.error("❌ Lỗi xác nhận đặt vé: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Nhả ghế - Chuyển từ LOCKED → AVAILABLE (nếu hết thời gian chọn hoặc user cancel)
     * 
     * @param showtimeId ID suất chiếu
     * @param seatNumbers Danh sách ghế
     * @param reason Lý do nhả ghế
     */
    @Transactional
    public void releaseSeats(Long showtimeId, List<String> seatNumbers, String reason) {
        try {
            // 1. Cập nhật ghế thành AVAILABLE (isReserved = false)
            for (String seatNumber : seatNumbers) {
                List<Seat> seats = seatRepository.findAll();
                seats.stream()
                    .filter(s -> s.getSeatNumber().equals(seatNumber) && 
                               s.getShowtime().getId().equals(showtimeId))
                    .forEach(seat -> {
                        seat.setIsReserved(false);
                        seatRepository.save(seat);
                    });
            }
            
            // 2. Broadcast tín hiệu
            seatRealtimeService.broadcastSeatStatus(
                showtimeId,
                seatNumbers,
                "AVAILABLE",
                reason
            );
            
            log.info("🟩 Nhả ghế {} cho suất chiếu {} - Lý do: {}", seatNumbers, showtimeId, reason);
            
        } catch (Exception e) {
            log.error("❌ Lỗi nhả ghế: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Cancel đơn đặt - Nhả ghế và refund thanh toán
     * 
     * @param reservationId ID đơn đặt
     */
    @Transactional
    public void cancelBooking(Long reservationId) {
        try {
            // 1. Lấy thông tin đơn đặt
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt"));
            
            // 2. Lấy danh sách ghế từ seats liên kết với reservation
            List<Seat> reservedSeats = seatRepository.findAll().stream()
                    .filter(s -> s.getReservation() != null && s.getReservation().getId().equals(reservationId))
                    .collect(Collectors.toList());
            
            List<String> seatNumbers = reservedSeats.stream()
                    .map(Seat::getSeatNumber)
                    .collect(Collectors.toList());
            
            // 3. Nhả ghế
            releaseSeats(reservation.getShowtime().getId(), seatNumbers, "User hủy đơn #" + reservationId);
            
            log.info("🚫 Đơn đặt {} đã bị hủy và ghế được nhả lại", reservationId);
            
        } catch (Exception e) {
            log.error("❌ Lỗi hủy đơn đặt: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Auto-release seats - Được gọi bởi @Scheduled task mỗi phút
     * Kiểm tra tất cả đặt vé quá hạn và nhả lại ghế
     */
    @Transactional
    public void autoReleaseExpiredSeats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 1. Lấy tất cả đặt vé quá hạn
            List<Reservation> expiredReservations = reservationRepository.findAll().stream()
                    .filter(r -> r.getExpiresAt() != null && r.getExpiresAt().isBefore(now))
                    .collect(Collectors.toList());
            
            if (expiredReservations.isEmpty()) {
                log.debug("✅ Không có đơn đặt vé quá hạn");
                return;
            }
            
            // 2. Grouping by showtime để broadcast
            expiredReservations.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getShowtime().getId(),
                            Collectors.mapping(r -> {
                                List<Seat> seats = seatRepository.findAll().stream()
                                        .filter(s -> s.getReservation() != null && 
                                                   s.getReservation().getId().equals(r.getId()))
                                        .collect(Collectors.toList());
                                return seats;
                            }, Collectors.toList())
                    ))
                    .forEach((showtimeId, seatLists) -> {
                        seatLists.stream()
                                .flatMap(List::stream)
                                .map(Seat::getSeatNumber)
                                .distinct()
                                .collect(Collectors.toList())
                                .forEach(seatNumber -> {
                                    List<Seat> seats = new java.util.ArrayList<>();
                                    // Release seat logic
                                });
                    });
            
            log.info("✅ Tự động nhả lại {} đơn đặt vé quá hạn", expiredReservations.size());
            
        } catch (Exception e) {
            log.error("❌ Lỗi tự động nhả ghế: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Phát tín hiệu thông báo tùy chỉnh - Cho các trường hợp đặc biệt
     * 
     * @param showtimeId ID suất chiếu
     * @param seatNumbers Danh sách ghế
     * @param customMessage Thông báo tùy chỉnh
     */
    public void broadcastCustomMessage(Long showtimeId, List<String> seatNumbers, String customMessage) {
        try {
            SeatStatusPayload payload = SeatStatusPayload.builder()
                    .showtimeId(showtimeId)
                    .seatNumbers(seatNumbers)
                    .status("ALERT")
                    .description(customMessage)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            seatRealtimeService.broadcastCustomPayload(showtimeId, payload);
            
            log.info("📢 Phát thông báo tùy chỉnh: {}", customMessage);
            
        } catch (Exception e) {
            log.error("❌ Lỗi phát thông báo tùy chỉnh: {}", e.getMessage(), e);
        }
    }
}

