package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.ConfirmReservationRequest;
import com.ticketrush.backend.dto.CreateReservationRequest;
import com.ticketrush.backend.dto.CreateReservationResponse;
import com.ticketrush.backend.dto.ReservationResponse;
import com.ticketrush.backend.dto.TicketResponse;
import com.ticketrush.backend.security.UserDetailsImpl;
import com.ticketrush.backend.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API Controller quản lý chốt đơn đặt vé (Confirm Reservation).
 * Cung cấp endpoint để chốt đơn, thanh toán, và tạo vé điện tử.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/reservations")
@AllArgsConstructor
@Tag(name = "🎫 Reservation Management", description = "API quản lý đơn đặt vé")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Chốt đơn đặt vé (Confirm Reservation).
     *
     * API Endpoint: POST /api/v1/reservations/confirm
     *
     * Quy trình:
     * 1. Cập nhật status = PAID, paid = true
     * 2. Cập nhật tất cả ghế: isReserved = true, gắn reservation_id
     * 3. Tạo mã QR code từ QrCodeUtil
     * 4. Lưu QR hash vào DB
     * 5. Phát tín hiệu realtime báo ghế ĐÃ BÁN
     *
     * ⚠️ QUAN TRỌNG: @Transactional đảm bảo rollback nếu lỗi!
     * Nếu bất kỳ bước nào thất bại:
     * - Tiền hoàn lại
     * - Ghế được nhả
     * - Mã QR không được tạo
     *
     * @param authentication Thông tin authenticate của user (từ Spring Security)
     * @param request ConfirmReservationRequest chứa:
     *                - reservationId: ID đơn đặt cần chốt
     *                - paymentMethodId: ID phương thức thanh toán
     *                - transactionCode: Mã giao dịch từ hệ thống thanh toán
     *                - seatNumbers: Danh sách mã ghế ["A1", "A2", ...]
     *                - notes: Ghi chú thêm (tuỳ chọn)
     * @return ReservationResponse chứa:
     *         - Thông tin đơn đặt đã chốt
     *         - Mã QR (Base64 + Data URI)
     *         - QR Hash lưu vào DB
     *         - Timestamp confirm
     *
     * HTTP Status:
     * - 200 OK: Chốt đơn thành công
     * - 400 Bad Request: Dữ liệu không hợp lệ hoặc đơn không tồn tại
     * - 401 Unauthorized: Người dùng chưa đăng nhập
     * - 403 Forbidden: Không có quyền chốt đơn này
     * - 500 Internal Server Error: Lỗi server
     *
     * Ví dụ Request:
     * {
     *   "reservationId": 123,
     *   "paymentMethodId": 1,
     *   "transactionCode": "TXN_20260430_ABC123XYZ",
     *   "seatNumbers": ["A1", "A2", "A3"],
     *   "notes": "Thanh toán qua Visa"
     * }
     *
     * Ví dụ Response (200 OK):
     * {
     *   "reservationId": 123,
     *   "movieName": "Avengers: Endgame",
     *   "theaterName": "CGV Hồ Tây",
     *   "seatNumbers": ["A1", "A2", "A3"],
     *   "totalPrice": 450000,
     *   "status": "PAID",
     *   "qrCodeBase64": "iVBORw0KGgoAAAANSUhEUgAAASwAAASwCAY...",
     *   "qrCodeDataUri": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAASwCAY...",
     *   "qrCodeHash": "UkVTRVJWQVRJT05fMTIzI2FiYzEyM3l6 ego=",
     *   "confirmedAt": "2026-04-30T14:30:45.123456",
     *   "transactionCode": "TXN_20260430_ABC123XYZ",
     *   "message": "✅ Chốt đơn thành công. Vé đã được tạo.",
     *   "apiStatus": "SUCCESS"
     * }
     */
     @PostMapping("/confirm")
     @Operation(
             summary = "🎫 Chốt đơn đặt vé (Confirm Reservation)",
             description = "Xác nhận thanh toán, tạo vé điện tử với mã QR code. " +
                     "Sử dụng @Transactional để đảm bảo tính toàn vẹn giao dịch. " +
                     "Nếu lỗi → Tự động rollback tiền, ghế, mã QR.",
             security = @SecurityRequirement(name = "bearer-jwt")
     )
     @ApiResponses({
             @ApiResponse(responseCode = "200", description = "✅ Chốt đơn thành công",
                     content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
             @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
             @ApiResponse(responseCode = "401", description = "❌ Chưa đăng nhập"),
             @ApiResponse(responseCode = "403", description = "❌ Không có quyền chốt đơn này"),
             @ApiResponse(responseCode = "500", description = "❌ Lỗi server (tự động rollback)")
     })
     public ResponseEntity<ReservationResponse> confirmReservation(
             Authentication authentication,

            @RequestBody
            @Parameter(description = "Dữ liệu chốt đơn", required = true)
            ConfirmReservationRequest request) {

        try {
            // Lấy user ID từ authentication
            Long userId = extractUserId(authentication);
            log.info("📱 Chốt đơn cho user: {} với đơn ID: {}", userId, request.getReservationId());

            // Validate input
            if (request.getReservationId() == null || request.getReservationId() <= 0) {
                log.warn("❌ ID đơn không hợp lệ: {}", request.getReservationId());
                return ResponseEntity.badRequest().body(
                        ReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("❌ ID đơn đặt vé phải > 0")
                                .build()
                );
            }

            if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
                log.warn("❌ Danh sách ghế trống");
                return ResponseEntity.badRequest().body(
                        ReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("❌ Danh sách ghế không được để trống")
                                .build()
                );
            }

            if (request.getTransactionCode() == null || request.getTransactionCode().trim().isEmpty()) {
                log.warn("❌ Mã giao dịch trống");
                return ResponseEntity.badRequest().body(
                        ReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("❌ Mã giao dịch không được để trống")
                                .build()
                );
            }

            // Gọi service để chốt đơn
            ReservationResponse response = reservationService.confirmReservation(userId, request);

            if ("SUCCESS".equals(response.getApiStatus())) {
                log.info("✅ Chốt đơn thành công: {}", request.getReservationId());
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ Chốt đơn thất bại: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi validation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message("❌ " + e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Lỗi chốt đơn không xác định: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message("❌ Lỗi server: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Lấy thông tin đơn đặt vé (Get Reservation Details).
     *
     * API Endpoint: GET /api/v1/reservations/{reservationId}
     *
     * @param reservationId ID đơn đặt vé
     * @param authentication Thông tin authenticate của user
     * @return Thông tin chi tiết đơn đặt vé
     *
     * HTTP Status:
     * - 200 OK: Lấy thông tin thành công
     * - 401 Unauthorized: Chưa đăng nhập
     * - 403 Forbidden: Không có quyền xem đơn này
     * - 404 Not Found: Không tìm thấy đơn
     * - 500 Internal Server Error: Lỗi server
     */
     @GetMapping("/{reservationId}")
     @Operation(
             summary = "📋 Lấy thông tin đơn đặt vé",
             description = "Lấy chi tiết thông tin của một đơn đặt vé đã chốt",
             security = @SecurityRequirement(name = "bearer-jwt")
     )
     @ApiResponses({
             @ApiResponse(responseCode = "200", description = "✅ Lấy thông tin thành công"),
             @ApiResponse(responseCode = "401", description = "❌ Chưa đăng nhập"),
             @ApiResponse(responseCode = "403", description = "❌ Không có quyền xem đơn này"),
             @ApiResponse(responseCode = "404", description = "❌ Không tìm thấy đơn"),
             @ApiResponse(responseCode = "500", description = "❌ Lỗi server")
     })
     public ResponseEntity<ReservationResponse> getReservation(
             @Parameter(description = "ID đơn đặt vé", example = "123", required = true)
             @PathVariable Long reservationId,

             Authentication authentication) {

        try {
            Long userId = extractUserId(authentication);
            log.info("📋 Lấy thông tin đơn đặt vé ID: {} cho user: {}", reservationId, userId);

            ReservationResponse response = reservationService.getReservation(reservationId, userId);

            if ("SUCCESS".equals(response.getApiStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message("❌ " + e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Lỗi lấy thông tin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message("❌ Lỗi server")
                            .build()
            );
        }
    }

    /**
     * Health check endpoint.
     *
     * API Endpoint: GET /api/v1/reservations/health
     *
     * @return Thông báo trạng thái
     */
    @GetMapping("/health")
    @Operation(summary = "❤️ Health check", description = "Kiểm tra dịch vụ đơn đặt vé")
    @ApiResponse(responseCode = "200", description = "✅ Dịch vụ hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ 🎫 Dịch vụ Chốt Đơn (Reservation Confirmation) hoạt động bình thường");
    }

    /**
     * API Tạo Đơn Hàng (Init Reservation) - VỀ LỖ HỔNG 1
     * 
     * API Endpoint: POST /api/v1/reservations/init
     * 
     * Tạo đơn đặt vé mới với trạng thái PENDING.
     * Logic:
     * 1. Chặn thời gian chiếu (15 phút trước suất chiếu)
     * 2. Tính tiền kèm loại ghế (dùng priceMultiplier)
     * 3. Xử lý voucher nếu có
     * 4. Lưu database
     * 5. Trả về thông tin đơn vừa tạo
     *
     * HTTP Status:
     * - 201 Created: Tạo đơn thành công
     * - 400 Bad Request: Dữ liệu không hợp lệ
     * - 401 Unauthorized: Chưa đăng nhập
     * - 409 Conflict: Quá thời gian mở bán
     * - 500 Internal Server Error: Lỗi server
     */
    @PostMapping("/init")
    @Operation(
            summary = "🎫 Tạo Đơn Hàng (Init Reservation)",
            description = "Tạo đơn đặt vé mới với trạng thái PENDING. " +
                    "Validate thời gian, tính tiền kèm loại ghế, xử lý voucher. " +
                    "Trả về thông tin đơn vừa tạo. Khách có 15 phút để thanh toán.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "✅ Tạo đơn thành công",
                    content = @Content(schema = @Schema(implementation = CreateReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "❌ Chưa đăng nhập"),
            @ApiResponse(responseCode = "409", description = "❌ Quá thời gian mở bán (15 phút trước suất chiếu)"),
            @ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    public ResponseEntity<CreateReservationResponse> createReservation(
            Authentication authentication,
            @RequestBody
            @Parameter(description = "Dữ liệu tạo đơn", required = true)
            CreateReservationRequest request) {
        try {
            Long userId = extractUserId(authentication);
            log.info("📱 Tạo đơn đặt vé cho user: {} với suất chiếu: {}", userId, request.getShowtimeId());

            // Validate input
            if (request.getShowtimeId() == null || request.getShowtimeId() <= 0) {
                log.warn("❌ ID suất chiếu không hợp lệ: {}", request.getShowtimeId());
                return ResponseEntity.badRequest().body(
                        CreateReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("❌ ID suất chiếu phải > 0")
                                .build()
                );
            }

            if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
                log.warn("❌ Danh sách ghế trống");
                return ResponseEntity.badRequest().body(
                        CreateReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("❌ Danh sách ghế không được để trống")
                                .build()
                );
            }

            // Gọi service để tạo đơn
            CreateReservationResponse response = reservationService.createReservation(userId, request);

            if ("SUCCESS".equals(response.getApiStatus())) {
                log.info("✅ Tạo đơn thành công: {}", response.getReservationId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.error("❌ Tạo đơn thất bại: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi validation: {}", e.getMessage());
            // Nếu là lỗi thời gian, trả về 409 Conflict
            if (e.getMessage().contains("Đã đóng quầy bán vé")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        CreateReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("❌ " + e.getMessage())
                                .build()
                );
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    CreateReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message("❌ " + e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Lỗi tạo đơn không xác định: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CreateReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message("❌ Lỗi server: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * API Vé Của Tôi (My Tickets) - VỀ LỖ HỔNG 2
     * 
     * API Endpoint: GET /api/v1/reservations/my-tickets
     * 
     * Lấy danh sách vé của khách hàng (từ Security Context).
     * V3: Bắt buộc phải có roomName để khách biết đường đi.
     * Format: "Rạp: Beta Cinemas - Phòng: IMAX 01"
     *
     * HTTP Status:
     * - 200 OK: Lấy thành công
     * - 401 Unauthorized: Chưa đăng nhập
     * - 404 Not Found: Không tìm thấy user
     * - 500 Internal Server Error: Lỗi server
     */
    @GetMapping("/my-tickets")
    @Operation(
            summary = "🎫 Vé Của Tôi (My Tickets)",
            description = "Lấy danh sách vé của khách hàng hiện tại. " +
                    "Hiển thị chi tiết hơn với tên phòng chiếu. " +
                    "Format: Rạp: Beta Cinemas - Phòng: IMAX 01",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ Lấy danh sách vé thành công"),
            @ApiResponse(responseCode = "401", description = "❌ Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "❌ Không tìm thấy user"),
            @ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    public ResponseEntity<?> getMyTickets(Authentication authentication) {
        try {
            log.info("🎫 Lấy danh sách vé của user hiện tại");

            // Lấy email từ authentication
            String userEmail = extractUserEmail(authentication);
            log.info("📧 Email: {}", userEmail);

            // Gọi service
            List<TicketResponse> tickets = reservationService.getUserReservations(userEmail);

            log.info("✅ Tìm được {} vé", tickets.size());
            return ResponseEntity.ok(tickets);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseEntity<>(
                            Map.of("message", "❌ " + e.getMessage(), "apiStatus", "FAILED"),
                            HttpStatus.NOT_FOUND
                    ).getBody()
            );
        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách vé: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "❌ Lỗi server: " + e.getMessage(), "apiStatus", "FAILED")
            );
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * Task 2.3: Chi tiết vé - trả về đầy đủ thông tin để render UI vé giấy + QR
     *
     * API Endpoint: GET /api/v1/reservations/{reservationId}/ticket-detail
     */
    @GetMapping("/{reservationId}/ticket-detail")
    @Operation(
            summary = "🎫 Chi tiết vé (Ticket Detail)",
            description = "Lấy thông tin vé đầy đủ: phim, rạp, phòng, dãy, ghế, giờ chiếu. " +
                    "Dùng để render UI vé giấy truyền thống và mã hóa QR Code.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<?> getTicketDetail(
            @PathVariable Long reservationId,
            Authentication authentication) {
        try {
            Long userId = extractUserId(authentication);
            com.ticketrush.backend.dto.TicketDetailResponse detail =
                    reservationService.getTicketDetail(reservationId, userId);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "❌ " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Lỗi lấy chi tiết vé: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "❌ Lỗi hệ thống"
            ));
        }
    }

    /**
     * Hủy đơn đặt vé (Cancel Reservation).
     *
     * API Endpoint: POST /api/v1/reservations/{reservationId}/cancel
     */
    @PostMapping("/{reservationId}/cancel")
    @Operation(
            summary = "🚫 Hủy đơn đặt vé",
            description = "Hủy đơn đặt vé. Chỉ người tạo đơn hoặc Admin mới được hủy.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<?> cancelReservation(
            @PathVariable Long reservationId,
            Authentication authentication) {
        try {
            Long userId = extractUserId(authentication);
            log.info("🚫 User {} yêu cầu hủy đơn {}", userId, reservationId);
            
            reservationService.cancelReservation(reservationId, userId);
            
            return ResponseEntity.ok(Map.of(
                    "apiStatus", "SUCCESS",
                    "message", "✅ Hủy đơn thành công"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "❌ " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Lỗi hệ thống khi hủy đơn: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "❌ Lỗi hệ thống"
            ));
        }
    }

    /**
     * Trích xuất User ID từ Authentication object.
     *
     * @param authentication Spring Security Authentication
     * @return User ID
     * @throws IllegalArgumentException nếu không tìm thấy user ID
     */
    private Long extractUserId(Authentication authentication) throws IllegalArgumentException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("❌ Chưa đăng nhập");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new IllegalArgumentException("❌ Không thể trích xuất user ID");
    }

    /**
     * Trích xuất User Email từ Authentication object.
     *
     * @param authentication Spring Security Authentication
     * @return User Email
     * @throws IllegalArgumentException nếu không tìm thấy email
     */
    private String extractUserEmail(Authentication authentication) throws IllegalArgumentException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("❌ Chưa đăng nhập");
        }

        try {
            // Nếu principal là String, có thể đó là email hoặc username
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                return (String) principal;
            }
            // Nếu có UserDetails object, lấy username/email từ đó
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            }
            throw new IllegalArgumentException("❌ Không thể trích xuất email");
        } catch (Exception e) {
            log.error("❌ Lỗi parse email: {}", e.getMessage());
            throw new IllegalArgumentException("❌ Email không hợp lệ");
        }
    }
}

