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
 * Controller tạo và tra cứu thông tin QR code.
 *
 * Các endpoint kiểm tra dữ liệu đầu vào trước khi gọi service để trả lỗi rõ ràng
 * cho client.
 */
@Slf4j
@RestController
@RequestMapping("/api/qr-codes")
@AllArgsConstructor
@Tag(name = "QR Code Management", description = "API quản lý mã QR cho vé và đơn đặt")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    /**
     * Tạo mã QR cho đơn đặt vé.
     *
     * @param reservationId ID đơn đặt vé.
     * @param secretHash hash bí mật dùng để mã hóa nội dung QR.
     * @return mã QR dạng Base64 và Data URI.
     */
    @PostMapping("/reservation")
    @Operation(summary = "Tạo mã QR cho đơn đặt vé")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo mã QR thành công",
                    content = @Content(schema = @Schema(implementation = QrCodeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<QrCodeResponse> generateReservationQrCode(
            @Parameter(description = "ID đơn đặt vé", example = "123", required = true)
            @RequestParam Long reservationId,

            @Parameter(description = "Hash bí mật", example = "abc123xyz", required = true)
            @RequestParam String secretHash) {

        log.info("Yêu cầu tạo mã QR đơn đặt vé ID: {}", reservationId);

        if (reservationId == null || reservationId <= 0) {
            log.warn("ID đơn đặt vé không hợp lệ: {}", reservationId);
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("ID đơn đặt vé phải > 0")
                            .build()
            );
        }

        if (secretHash == null || secretHash.trim().isEmpty() || secretHash.length() < 6) {
            log.warn("Hash bí mật không hợp lệ");
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("Hash bí mật phải có ít nhất 6 ký tự")
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
     * @param ticketId ID vé.
     * @param verificationHash hash xác minh vé.
     * @return mã QR dạng Base64 và Data URI.
     */
    @PostMapping("/ticket")
    @Operation(summary = "Tạo mã QR cho vé điện tử")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo mã QR thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<QrCodeResponse> generateTicketQrCode(
            @Parameter(description = "ID vé", example = "456", required = true)
            @RequestParam Long ticketId,

            @Parameter(description = "Hash xác minh", example = "def456abc", required = true)
            @RequestParam String verificationHash) {

        log.info("Yêu cầu tạo mã QR vé ID: {}", ticketId);

        if (ticketId == null || ticketId <= 0) {
            log.warn("ID vé không hợp lệ: {}", ticketId);
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("ID vé phải > 0")
                            .build()
            );
        }

        if (verificationHash == null || verificationHash.trim().isEmpty() || verificationHash.length() < 6) {
            log.warn("Hash xác minh không hợp lệ");
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("Hash xác minh phải có ít nhất 6 ký tự")
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
     * @param data dữ liệu cần mã hóa thành QR.
     * @return mã QR dạng Base64 và Data URI.
     */
    @PostMapping("/custom")
    @Operation(summary = "Tạo mã QR tùy chỉnh")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo mã QR thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<QrCodeResponse> generateCustomQrCode(
            @Parameter(description = "Dữ liệu tùy chỉnh", example = "CUSTOM_DATA_12345", required = true)
            @RequestParam String data) {

        log.info("Yêu cầu tạo mã QR tùy chỉnh");

        if (data == null || data.trim().isEmpty()) {
            log.warn("Dữ liệu tùy chỉnh không được để trống");
            return ResponseEntity.badRequest().body(
                    QrCodeResponse.builder()
                            .status("FAILED")
                            .message("Dữ liệu không được để trống")
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
     * Lấy thông tin cấu hình QR code hiện tại.
     *
     * @return chuỗi mô tả cấu hình QR code.
     */
    @GetMapping("/info")
    @Operation(summary = "Lấy thông tin cấu hình QR code")
    @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công")
    public ResponseEntity<String> getQrCodeInfo() {
        log.info("Yêu cầu lấy thông tin QR code");
        String info = qrCodeService.getQrCodeInfo();
        return ResponseEntity.ok(info);
    }

    /**
     * Lấy dung lượng tối đa mà QR code có thể chứa.
     *
     * @return chuỗi mô tả dung lượng tối đa.
     */
    @GetMapping("/capacity")
    @Operation(summary = "Lấy dung lượng tối đa QR code")
    @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công")
    public ResponseEntity<String> getQrCodeCapacity() {
        log.info("Yêu cầu lấy dung lượng QR code");
        String capacity = qrCodeService.getQrCodeCapacity();
        return ResponseEntity.ok(capacity);
    }

    /**
     * Kiểm tra trạng thái dịch vụ QR code.
     *
     * @return thông báo dịch vụ đang hoạt động.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check")
    @ApiResponse(responseCode = "200", description = "Dịch vụ QR code hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Dịch vụ QR Code Generator hoạt động bình thường");
    }
}
