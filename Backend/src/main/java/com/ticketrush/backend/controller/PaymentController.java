package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.PaymentCallbackRequest;
import com.ticketrush.backend.dto.response.ReservationResponse;
import com.ticketrush.backend.service.ReservationService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý callback thanh toán từ cổng thanh toán.
 *
 * Annotation {@link Hidden} được dùng cho callback để ẩn endpoint khỏi Swagger
 * vì đây là API dành cho hệ thống thanh toán, không dành cho frontend gọi trực tiếp.
 */
@Slf4j
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management (Hidden)", description = "API xử lý callback thanh toán")
public class PaymentController {

    private final ReservationService reservationService;

    /**
     * Xử lý callback xác nhận thanh toán từ payment gateway.
     *
     * Method phân biệt lỗi conflict khi đơn đã được chốt và lỗi validate thông
     * thường để trả HTTP status phù hợp.
     *
     * @param request dữ liệu callback từ cổng thanh toán.
     * @return thông tin reservation sau khi cập nhật thanh toán.
     */
    @PostMapping("/callback")
    @Hidden
    @Operation(
        summary = "Payment Callback",
        description = "API ẩn, chỉ dành cho Payment Gateway gọi khi thanh toán hoàn tất"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Callback thành công",
            content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "409", description = "Đơn đã được chốt"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<?> handlePaymentCallback(
            @RequestBody PaymentCallbackRequest request
    ) {
        try {
            log.info("Nhận payment callback từ {}: Transaction {}",
                    request.getProvider(), request.getTransactionCode());

            ReservationResponse response = reservationService.handlePaymentCallback(request);

            log.info("Xử lý callback thành công cho reservation: {}", response.getReservationId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validate callback: {}", e.getMessage());

            if (e.getMessage().contains("đã được chốt")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", e.getMessage(), "apiStatus", "CONFLICT")
                );
            }

            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage(), "apiStatus", "FAILED")
            );

        } catch (Exception e) {
            log.error("Lỗi xử lý callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("message", "Lỗi server: " + e.getMessage(), "apiStatus", "ERROR")
            );
        }
    }

    /**
     * Kiểm tra trạng thái dịch vụ callback thanh toán.
     *
     * @return thông báo dịch vụ đang hoạt động.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Kiểm tra dịch vụ thanh toán")
    @ApiResponse(responseCode = "200", description = "Server hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Callback Service hoạt động bình thường");
    }
}
