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
 * VỀ LỖ HỔNG 3: Payment Callback Controller
 * 
 * API HIDDEN - Chỉ dành cho Payment Gateway server (VNPay, MoMo, Stripe)
 * Được gọi khi hệ thống thanh toán xác nhận tiền đã trừ khỏi tài khoản khách
 * 
 * QUAN TRỌNG:
 * - Chỉ Payment Gateway được phép gọi API này
 * - KHÔNG phải Frontend tự gọi
 * - Yêu cầu xác thực IP/Secret từ gateway
 * 
 * Luồng an toàn:
 * 1. Frontend tạo đơn (POST /v1/reservations/init)
 * 2. Frontend gọi payment gateway → nhập tiền
 * 3. Payment Gateway confirm tiền → gọi callback API này
 * 4. Callback API cập nhật Reservation status = PAID
 * 5. Frontend nhận tín hiệu realtime → hiển thị thành công
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "💳 Payment Management (Hidden)", description = "API xử lý callback thanh toán - CHỈ cho Payment Gateway")
public class PaymentController {

    private final ReservationService reservationService;

    /**
     * VỀ LỖ HỔNG 3: Payment Callback từ Payment Gateway
     * 
     * POST /v1/payments/callback (HIDDEN - chỉ payment gateway gọi)
     * 
     * KHÔNG hiển thị trên SwaggerUI (mục đích bảo mật)
     * 
     * Quy trình:
     * 1. Validate callback từ payment gateway
     * 2. Verify transactionCode + amount khớp
     * 3. Cập nhật Reservation status = PAID
     * 4. Cập nhật Seat status = RESERVED
     * 5. Tạo QR code và lưu vào DB
     * 6. Phát tín hiệu realtime báo ghế ĐÃ BÁN
     * 
     * ⚠️ CRITICAL: Chỉ payment gateway được gọi API này (check IP/Secret)
     * 
     * Request từ VNPay/MoMo:
     * {
     *   "transactionCode": "VNP_20260430_ABC123",
     *   "reservationId": 123,
     *   "paymentStatus": "SUCCESS",
     *   "amount": 450000,
     *   "provider": "VNPAY",
     *   "additionalInfo": "..." (optional)
     * }
     * 
     * Response:
     * {
     *   "reservationId": 123,
     *   "status": "PAID",
     *   "qrCodeHash": "...",
     *   "message": "✅ Callback thành công. Vé đã được tạo.",
     *   "apiStatus": "SUCCESS"
     * }
     * 
     * @param request PaymentCallbackRequest từ payment gateway
     * @return Response xác nhận đã xử lý thanh toán thành công
     */
    @PostMapping("/callback")
    @Hidden  // HIDDEN từ SwaggerUI vì đây là hidden API
    @Operation(
        summary = "💳 Payment Callback (HIDDEN)",
        description = "HIDDEN API - Chỉ Payment Gateway gọi. " +
            "Cập nhật trạng thái thanh toán khi tiền đã trừ khỏi tài khoản khách. " +
            "Frontend KHÔNG được gọi API này."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "✅ Callback thành công",
            content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
        @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "409", description = "❌ Conflict - Đơn đã được chốt rồi"),
        @ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    public ResponseEntity<?> handlePaymentCallback(
            @RequestBody PaymentCallbackRequest request
    ) {
        try {
            log.info("💳 Nhận payment callback từ {}: Transaction {}", 
                    request.getProvider(), request.getTransactionCode());

            // Gọi service để xử lý callback
            ReservationResponse response = reservationService.handlePaymentCallback(request);

            log.info("✅ Xử lý callback thành công cho reservation: {}", response.getReservationId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi validate callback: {}", e.getMessage());
            
            // Nếu là conflict (đã được chốt), trả về 409
            if (e.getMessage().contains("đã được chốt")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", "❌ " + e.getMessage(), "apiStatus", "CONFLICT")
                );
            }
            
            return ResponseEntity.badRequest().body(
                Map.of("message", "❌ " + e.getMessage(), "apiStatus", "FAILED")
            );

        } catch (Exception e) {
            log.error("❌ Lỗi xử lý callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("message", "❌ Lỗi server: " + e.getMessage(), "apiStatus", "ERROR")
            );
        }
    }

    /**
     * Health check endpoint cho Payment Gateway
     * 
     * GET /v1/payments/health
     * 
     * Dùng để payment gateway kiểm tra xem callback server có online không
     * 
     * @return Trạng thái server
     */
    @GetMapping("/health")
    @Operation(summary = "❤️ Health check", description = "Kiểm tra dịch vụ thanh toán")
    @ApiResponse(responseCode = "200", description = "✅ Server hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ 💳 Payment Callback Service hoạt động bình thường");
    }
}

