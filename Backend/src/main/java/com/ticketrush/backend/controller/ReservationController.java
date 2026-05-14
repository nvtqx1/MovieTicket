package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.ConfirmReservationRequest;
import com.ticketrush.backend.dto.request.CreateReservationRequest;
import com.ticketrush.backend.dto.response.CreateReservationResponse;
import com.ticketrush.backend.dto.response.ReservationResponse;
import com.ticketrush.backend.dto.response.TicketResponse;
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
 * Controller quản lý đơn đặt vé và vé của người dùng.
 *
 * Các method trích xuất user từ Spring Security để đảm bảo người dùng chỉ thao
 * tác trên dữ liệu của chính mình.
 */
@Slf4j
@RestController
@RequestMapping("/v1/reservations")
@AllArgsConstructor
@Tag(name = "Reservation Management", description = "API quản lý đơn đặt vé")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Chốt đơn đặt vé sau khi thanh toán.
     *
     * Service xử lý giao dịch toàn vẹn: cập nhật đơn, ghế, QR và tín hiệu realtime;
     * nếu lỗi, tầng service chịu trách nhiệm rollback transaction.
     *
     * @param authentication thông tin xác thực của người dùng.
     * @param request dữ liệu chốt đơn gồm reservationId, mã giao dịch và ghế.
     * @return thông tin đơn đã chốt hoặc lỗi xử lý.
     */
    @PostMapping("/confirm")
    @Operation(
            summary = "Chốt đơn đặt vé",
            description = "Xác nhận thanh toán và tạo vé điện tử",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chốt đơn thành công",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền chốt đơn"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ReservationResponse> confirmReservation(
            Authentication authentication,
            @RequestBody
            @Parameter(description = "Dữ liệu chốt đơn", required = true)
            ConfirmReservationRequest request) {

        try {
            Long userId = extractUserId(authentication);
            log.info("Chốt đơn cho user: {} với đơn ID: {}", userId, request.getReservationId());

            if (request.getReservationId() == null || request.getReservationId() <= 0) {
                log.warn("ID đơn không hợp lệ: {}", request.getReservationId());
                return ResponseEntity.badRequest().body(
                        ReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("ID đơn đặt vé phải > 0")
                                .build()
                );
            }

            if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
                log.warn("Danh sách ghế trống");
                return ResponseEntity.badRequest().body(
                        ReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("Danh sách ghế không được để trống")
                                .build()
                );
            }

            if (request.getTransactionCode() == null || request.getTransactionCode().trim().isEmpty()) {
                log.warn("Mã giao dịch trống");
                return ResponseEntity.badRequest().body(
                        ReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("Mã giao dịch không được để trống")
                                .build()
                );
            }

            ReservationResponse response = reservationService.confirmReservation(userId, request);

            if ("SUCCESS".equals(response.getApiStatus())) {
                log.info("Chốt đơn thành công: {}", request.getReservationId());
                return ResponseEntity.ok(response);
            } else {
                log.error("Chốt đơn thất bại: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("Lỗi chốt đơn không xác định: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message("Lỗi server: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Lấy chi tiết một đơn đặt vé.
     *
     * @param reservationId ID đơn đặt vé.
     * @param authentication thông tin xác thực của người dùng.
     * @return chi tiết đơn đặt vé nếu người dùng có quyền xem.
     */
    @GetMapping("/{reservationId}")
    @Operation(
            summary = "Lấy thông tin đơn đặt vé",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem đơn"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ReservationResponse> getReservation(
            @Parameter(description = "ID đơn đặt vé", example = "123", required = true)
            @PathVariable Long reservationId,
            Authentication authentication) {

        try {
            Long userId = extractUserId(authentication);
            log.info("Lấy thông tin đơn đặt vé ID: {} cho user: {}", reservationId, userId);

            ReservationResponse response = reservationService.getReservation(reservationId, userId);

            if ("SUCCESS".equals(response.getApiStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("Lỗi lấy thông tin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message("Lỗi server")
                            .build()
            );
        }
    }

    /**
     * Kiểm tra trạng thái dịch vụ đơn đặt vé.
     *
     * @return thông báo dịch vụ đang hoạt động.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Kiểm tra dịch vụ đơn đặt vé")
    @ApiResponse(responseCode = "200", description = "Dịch vụ hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Dịch vụ Chốt Đơn hoạt động bình thường");
    }

    /**
     * Tạo đơn đặt vé mới ở trạng thái chờ thanh toán.
     *
     * Service kiểm tra thời gian bán vé, tính tiền theo loại ghế và áp voucher
     * nếu request có mã giảm giá.
     *
     * @param authentication thông tin xác thực của người dùng.
     * @param request dữ liệu tạo đơn gồm suất chiếu, ghế và voucher nếu có.
     * @return thông tin đơn vừa tạo.
     */
    @PostMapping("/init")
    @Operation(
            summary = "Tạo đơn đặt vé",
            description = "Tạo đơn đặt vé mới với trạng thái PENDING",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đơn thành công",
                    content = @Content(schema = @Schema(implementation = CreateReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "409", description = "Quá thời gian mở bán"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<CreateReservationResponse> createReservation(
            Authentication authentication,
            @RequestBody
            @Parameter(description = "Dữ liệu tạo đơn", required = true)
            CreateReservationRequest request) {
        try {
            Long userId = extractUserId(authentication);
            log.info("Tạo đơn đặt vé cho user: {} với suất chiếu: {}", userId, request.getShowtimeId());

            if (request.getShowtimeId() == null || request.getShowtimeId() <= 0) {
                log.warn("ID suất chiếu không hợp lệ: {}", request.getShowtimeId());
                return ResponseEntity.badRequest().body(
                        CreateReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("ID suất chiếu phải > 0")
                                .build()
                );
            }

            if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
                log.warn("Danh sách ghế trống");
                return ResponseEntity.badRequest().body(
                        CreateReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message("Danh sách ghế không được để trống")
                                .build()
                );
            }

            CreateReservationResponse response = reservationService.createReservation(userId, request);

            if ("SUCCESS".equals(response.getApiStatus())) {
                log.info("Tạo đơn thành công: {}", response.getReservationId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.error("Tạo đơn thất bại: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation: {}", e.getMessage());
            if (e.getMessage().contains("Đã đóng quầy bán vé")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        CreateReservationResponse.builder()
                                .apiStatus("FAILED")
                                .message(e.getMessage())
                                .build()
                );
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    CreateReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("Lỗi tạo đơn không xác định: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CreateReservationResponse.builder()
                            .apiStatus("FAILED")
                            .message("Lỗi server: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Lấy danh sách vé của người dùng hiện tại.
     *
     * @param authentication thông tin xác thực của người dùng.
     * @return danh sách vé của người dùng đang đăng nhập.
     */
    @GetMapping("/my-tickets")
    @Operation(
            summary = "Vé của tôi",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách vé thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<?> getMyTickets(Authentication authentication) {
        try {
            log.info("Lấy danh sách vé của user hiện tại");

            String userEmail = extractUserEmail(authentication);
            log.info("Email: {}", userEmail);

            List<TicketResponse> tickets = reservationService.getUserReservations(userEmail);

            log.info("Tìm được {} vé", tickets.size());
            return ResponseEntity.ok(tickets);

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage(), "apiStatus", "FAILED")
            );
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách vé: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Lỗi server: " + e.getMessage(), "apiStatus", "FAILED")
            );
        }
    }

    /**
     * Lấy chi tiết vé để hiển thị hoặc in vé.
     *
     * @param reservationId ID đơn đặt vé cần lấy chi tiết vé.
     * @param authentication thông tin xác thực của người dùng.
     * @return chi tiết vé hoặc thông báo lỗi.
     */
    @GetMapping("/{reservationId}/ticket-detail")
    @Operation(
            summary = "Chi tiết vé",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<?> getTicketDetail(
            @PathVariable Long reservationId,
            Authentication authentication) {
        try {
            Long userId = extractUserId(authentication);
            com.ticketrush.backend.dto.response.TicketDetailResponse detail =
                    reservationService.getTicketDetail(reservationId, userId);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "apiStatus", "FAILED",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Lỗi lấy chi tiết vé: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "Lỗi hệ thống"
            ));
        }
    }

    /**
     * Hủy đơn đặt vé.
     *
     * @param reservationId ID đơn cần hủy.
     * @param authentication thông tin xác thực của người dùng.
     * @return thông báo hủy thành công hoặc lỗi nghiệp vụ.
     */
    @PostMapping("/{reservationId}/cancel")
    @Operation(
            summary = "Hủy đơn đặt vé",
            description = "Chỉ người tạo đơn hoặc Admin mới được hủy",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<?> cancelReservation(
            @PathVariable Long reservationId,
            Authentication authentication) {
        try {
            Long userId = extractUserId(authentication);
            log.info("User {} yêu cầu hủy đơn {}", userId, reservationId);

            reservationService.cancelReservation(reservationId, userId);

            return ResponseEntity.ok(Map.of(
                    "apiStatus", "SUCCESS",
                    "message", "Hủy đơn thành công"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "apiStatus", "FAILED",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi hủy đơn: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "Lỗi hệ thống"
            ));
        }
    }

    /**
     * Trích xuất ID người dùng từ Authentication.
     *
     * @param authentication thông tin xác thực hiện tại.
     * @return ID người dùng.
     * @throws IllegalArgumentException khi người dùng chưa đăng nhập hoặc principal không hợp lệ.
     */
    private Long extractUserId(Authentication authentication) throws IllegalArgumentException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Chưa đăng nhập");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new IllegalArgumentException("Không thể trích xuất user ID");
    }

    /**
     * Trích xuất email người dùng từ Authentication.
     *
     * @param authentication thông tin xác thực hiện tại.
     * @return email hoặc username của người dùng.
     * @throws IllegalArgumentException khi không thể lấy email.
     */
    private String extractUserEmail(Authentication authentication) throws IllegalArgumentException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Chưa đăng nhập");
        }

        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                return (String) principal;
            }
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            }
            throw new IllegalArgumentException("Không thể trích xuất email");
        } catch (Exception e) {
            log.error("Lỗi parse email: {}", e.getMessage());
            throw new IllegalArgumentException("Email không hợp lệ");
        }
    }
}
