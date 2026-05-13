package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.response.QrCodeResponse;
import com.ticketrush.backend.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API Controller cho tạo và quản lý mã QR code.
 * Cung cấp các endpoint để tạo QR code cho đơn đặt vé và vé điện tử.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/qr-codes")
@AllArgsConstructor
@Tag(name = "🎫 QR Code Management", description = "API quản lý mã QR code cho vé và đơn đặt")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    /**
     * Tạo mã QR cho đơn đặt vé.
     *
     * API Endpoint: POST /api/qr-codes/reservation
     *
     * @param reservationId ID đơn đặt vé (VD: 123)
     * @param secretHash Hash bí mật (VD: "abc123xyz")
     * @return QrCodeResponse chứa Base64 và Data URI
     *
     * HTTP Status:
     * - 200 OK: Tạo thành công
     * - 400 Bad Request: Dữ liệu không hợp lệ
     * - 500 Internal Server Error: Lỗi server
     *
     * Ví dụ Response:
     * {
     *   "base64String": "iVBORw0KGgoAAAANSUhEUgAAASwAAASwCAYA...",
     *   "dataUri": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAASwCAYA...",
     *   "encodedData": "RESERVATION_123#abc123xyz",
     *   "imageSize": "300x300",
     *   "imageFormat": "PNG",
     *   "status": "SUCCESS",
     *   "message": "✅ Tạo mã QR thành công",
     *   "fileSizeBytes": 1523,
     *   "createdAt": "2026-04-30T14:30:45.123456",
     *   "entityId": 123,
     *   "entityType": "RESERVATION"
     * }
     */
    @PostMapping("/reservation")
    @Operation(
            summary = "🎫 Tạo mã QR cho đơn đặt vé",
            description = "Nhận ID đơn đặt vé và hash bí mật, trả về mã QR dạng Base64"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ Tạo mã QR thành công",
                    content = @Content(schema = @Schema(implementation = QrCodeResponse.class))),
            @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    public ResponseEntity<QrCodeResponse> generateReservationQrCode(
            @Parameter(description = "ID đơn đặt vé", example = "123", required = true)
            @RequestParam Long reservationId,

            @Parameter(description = "Hash bí mật (tối thiểu 6 ký tự)", example = "abc123xyz", required = true)
            @RequestParam String secretHash) {

        log.info("📱 Yêu cầu tạo mã QR đơn đặt vé ID: {}", reservationId);

        if (reservationId == null || reservationId <= 0) {
            log.warn("❌ ID đơn đặt vé không hợp lệ: {}", reservationId);
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("❌ ID đơn đặt vé phải > 0")
                            .build()
            );
        }

        if (secretHash == null || secretHash.trim().isEmpty() || secretHash.length() < 6) {
            log.warn("❌ Hash bí mật không hợp lệ");
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("❌ Hash bí mật phải có ít nhất 6 ký tự")
                            .build()
            );
        }

        QrCodeResponse response = qrCodeService.generateReservationQrCode(reservationId, secretHash);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Tạo mã QR cho vé điện tử.
     *
     * API Endpoint: POST /api/qr-codes/ticket
     *
     * @param ticketId ID vé (VD: 456)
     * @param verificationHash Hash xác minh (VD: "def456abc")
     * @return QrCodeResponse chứa Base64 và Data URI
     *
     * HTTP Status:
     * - 200 OK: Tạo thành công
     * - 400 Bad Request: Dữ liệu không hợp lệ
     * - 500 Internal Server Error: Lỗi server
     */
    @PostMapping("/ticket")
    @Operation(
            summary = "🎫 Tạo mã QR cho vé điện tử",
            description = "Nhận ID vé và hash xác minh, trả về mã QR dạng Base64"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ Tạo mã QR thành công"),
            @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    public ResponseEntity<QrCodeResponse> generateTicketQrCode(
            @Parameter(description = "ID vé", example = "456", required = true)
            @RequestParam Long ticketId,

            @Parameter(description = "Hash xác minh (tối thiểu 6 ký tự)", example = "def456abc", required = true)
            @RequestParam String verificationHash) {

        log.info("📱 Yêu cầu tạo mã QR vé ID: {}", ticketId);

        if (ticketId == null || ticketId <= 0) {
            log.warn("❌ ID vé không hợp lệ: {}", ticketId);
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("❌ ID vé phải > 0")
                            .build()
            );
        }

        if (verificationHash == null || verificationHash.trim().isEmpty() || verificationHash.length() < 6) {
            log.warn("❌ Hash xác minh không hợp lệ");
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("❌ Hash xác minh phải có ít nhất 6 ký tự")
                            .build()
            );
        }

        QrCodeResponse response = qrCodeService.generateTicketQrCode(ticketId, verificationHash);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Tạo mã QR từ dữ liệu tùy chỉnh.
     *
     * API Endpoint: POST /api/qr-codes/custom
     *
     * @param data Dữ liệu tùy chỉnh (VD: "CUSTOM_DATA_12345")
     * @return QrCodeResponse chứa Base64 và Data URI
     *
     * HTTP Status:
     * - 200 OK: Tạo thành công
     * - 400 Bad Request: Dữ liệu không hợp lệ
     * - 500 Internal Server Error: Lỗi server
     */
    @PostMapping("/custom")
    @Operation(
            summary = "🎫 Tạo mã QR tùy chỉnh",
            description = "Nhận dữ liệu tùy chỉnh, trả về mã QR dạng Base64"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ Tạo mã QR thành công"),
            @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    public ResponseEntity<QrCodeResponse> generateCustomQrCode(
            @Parameter(description = "Dữ liệu tùy chỉnh", example = "CUSTOM_DATA_12345", required = true)
            @RequestParam String data) {

        log.info("📱 Yêu cầu tạo mã QR tùy chỉnh");

        if (data == null || data.trim().isEmpty()) {
            log.warn("❌ Dữ liệu tùy chỉnh không được để trống");
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("❌ Dữ liệu không được để trống")
                            .build()
            );
        }

        QrCodeResponse response = qrCodeService.generateCustomQrCode(data);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin cấu hình QR code.
     *
     * API Endpoint: GET /api/qr-codes/info
     *
     * @return Chuỗi mô tả cấu hình QR code
     */
    @GetMapping("/info")
    @Operation(
            summary = "ℹ️ Lấy thông tin cấu hình QR code",
            description = "Trả về thông tin chi tiết về cấu hình QR code hiện tại"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thông tin thành công")
    public ResponseEntity<String> getQrCodeInfo() {
        log.info("📱 Yêu cầu lấy thông tin QR code");
        String info = qrCodeService.getQrCodeInfo();
        return ResponseEntity.ok(info);
    }

    /**
     * Lấy thông tin dung lượng tối đa QR code.
     *
     * API Endpoint: GET /api/qr-codes/capacity
     *
     * @return Chuỗi mô tả dung lượng tối đa
     */
    @GetMapping("/capacity")
    @Operation(
            summary = "📊 Lấy dung lượng tối đa QR code",
            description = "Trả về thông tin về dung lượng tối đa mà QR code có thể chứa"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thông tin thành công")
    public ResponseEntity<String> getQrCodeCapacity() {
        log.info("📱 Yêu cầu lấy dung lượng QR code");
        String capacity = qrCodeService.getQrCodeCapacity();
        return ResponseEntity.ok(capacity);
    }

    /**
     * Health check endpoint.
     *
     * API Endpoint: GET /api/qr-codes/health
     *
     * @return Thông báo trạng thái
     */
    @GetMapping("/health")
    @Operation(
            summary = "❤️ Health check",
            description = "Kiểm tra xem dịch vụ QR code có hoạt động bình thường không"
    )
    @ApiResponse(responseCode = "200", description = "✅ Dịch vụ QR code hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ 🎫 Dịch vụ QR Code Generator hoạt động bình thường");
    }
}

