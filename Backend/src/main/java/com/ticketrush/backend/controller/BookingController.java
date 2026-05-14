package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.HoldSeatRequest;
import com.ticketrush.backend.dto.response.HoldSeatResponse;
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
 * Controller xử lý giữ ghế và thanh toán mô phỏng.
 *
 * Annotation {@link AuthenticationPrincipal} lấy người dùng hiện tại từ Spring
 * Security để tránh truyền userId từ client.
 */
@RestController
@RequestMapping("/v1/booking")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "API đặt vé và thanh toán")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Giữ ghế cho người dùng hiện tại.
     *
     * Service phía dưới dùng cơ chế khóa bi quan để tránh hai người giữ cùng một
     * ghế trong cùng thời điểm.
     *
     * @param currentUser người dùng đang đăng nhập.
     * @param request danh sách ghế cần giữ.
     * @return kết quả giữ ghế hoặc lỗi xung đột khi ghế không còn khả dụng.
     */
    @PostMapping("/hold-seat")
    @Operation(
            summary = "Giữ ghế",
            description = "Giữ ghế cho user trong thời gian chờ thanh toán",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<HoldSeatResponse> holdSeat(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody HoldSeatRequest request) {
        try {
            HoldSeatResponse response = bookingService.holdSeats(currentUser.getId(), request);
            if ("FAILED".equals(response.getApiStatus())) {
                return ResponseEntity.status(409).body(response);
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
     * Thanh toán mô phỏng cho đơn đặt vé.
     *
     * Khi thành công, reservation được chuyển sang trạng thái đã thanh toán và
     * service xử lý các cập nhật ghế, QR hoặc realtime liên quan.
     *
     * @param currentUser người dùng đang đăng nhập.
     * @param reservationId ID đơn đặt vé cần thanh toán.
     * @param body dữ liệu tùy chọn, có thể chứa paymentMethod.
     * @return kết quả thanh toán mô phỏng.
     */
    @PostMapping("/checkout/{reservationId}")
    @Operation(
            summary = "Thanh toán mô phỏng",
            description = "Mô phỏng thanh toán, chuyển vé từ LOCKED sang PAID",
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
