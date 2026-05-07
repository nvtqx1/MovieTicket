package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.HoldSeatRequest;
import com.ticketrush.backend.dto.HoldSeatResponse;
import com.ticketrush.backend.security.UserDetailsImpl;
import com.ticketrush.backend.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ════════════════════════════════════════
 * BOOKING CONTROLLER
 * ════════════════════════════════════════
 * 
 * Task 3.1: POST /hold-seat — Giữ ghế với Pessimistic Lock
 * Task 3.3: POST /checkout/{id} — Thanh toán Mock
 */
@RestController
@RequestMapping("/v1/booking")
@RequiredArgsConstructor
@Tag(name = "🎫 Booking", description = "API đặt vé & thanh toán")
public class BookingController {

    private final BookingService bookingService;

    /**
     * TASK 3.1: HOLD SEAT
     * 
     * Sử dụng Pessimistic Lock để chặn race condition.
     * Nếu 2 user giữ cùng ghế cùng lúc → chỉ 1 thành công.
     * Ghế sẽ bị khóa 10 phút, sau đó CronJob tự nhả.
     */
    @PostMapping("/hold-seat")
    @Operation(
            summary = "🔒 Giữ ghế (Pessimistic Lock)",
            description = "Giữ ghế cho user trong 10 phút. Sử dụng SELECT ... FOR UPDATE để chống race condition.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<HoldSeatResponse> holdSeat(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody HoldSeatRequest request) {
        try {
            HoldSeatResponse response = bookingService.holdSeats(currentUser.getId(), request);
            if ("FAILED".equals(response.getApiStatus())) {
                return ResponseEntity.status(409).body(response); // 409 Conflict
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    HoldSeatResponse.builder()
                            .apiStatus("FAILED")
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * TASK 3.3: MOCK CHECKOUT
     * 
     * Mô phỏng thanh toán thành công.
     * Chuyển reservation: LOCKED → PAID
     * Tạo QR Code hash và broadcast SOLD qua WebSocket.
     */
    @PostMapping("/checkout/{reservationId}")
    @Operation(
            summary = "💳 Thanh toán Mock",
            description = "Mô phỏng thanh toán, chuyển vé từ LOCKED → PAID",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<?> checkout(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long reservationId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String paymentMethod = body != null ? body.getOrDefault("paymentMethod", "CASH") : "CASH";
            Map<String, Object> result = bookingService.mockCheckout(currentUser.getId(), reservationId, paymentMethod);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "apiStatus", "FAILED",
                    "message", e.getMessage()
            ));
        }
    }
}
