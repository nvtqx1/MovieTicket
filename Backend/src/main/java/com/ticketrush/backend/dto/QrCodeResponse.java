package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin mã QR code được tạo.
 * Gửi chuỗi Base64 hoặc Data URI tới Frontend để hiển thị.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrCodeResponse {

    /**
     * Chuỗi Base64 biểu diễn ảnh QR code (không bao gồm prefix)
     * Ví dụ: "iVBORw0KGgoAAAANSUhEUgAAASwAAASwCAYAAABkW7XSAAAABHNCSVQICAgIf..."
     */
    private String base64String;

    /**
     * Data URI sẵn sàng dùng trong thẻ img
     * Ví dụ: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAASwCAYAAABkW7XSAAAABHNCSVQICAgIf..."
     * Có thể dùng trực tiếp: &lt;img src="{dataUri}"&gt;
     */
    private String dataUri;

    /**
     * Dữ liệu gốc được mã hóa thành QR code
     * Ví dụ: "RESERVATION_123#abc123xyz" hoặc "TICKET_456#def456abc"
     */
    private String encodedData;

    /**
     * Kích thước ảnh QR code (pixels)
     * Ví dụ: "300x300"
     */
    private String imageSize;

    /**
     * Định dạng ảnh
     * Ví dụ: "PNG"
     */
    private String imageFormat;

    /**
     * Trạng thái tạo QR code
     * ✅ SUCCESS - Tạo thành công
     * ❌ FAILED - Tạo thất bại
     */
    private String status;

    /**
     * Thông báo chi tiết
     * Ví dụ: "✅ Tạo mã QR thành công"
     */
    private String message;

    /**
     * Kích thước file ảnh (bytes)
     */
    private Long fileSizeBytes;

    /**
     * Timestamp tạo QR code
     * ISO 8601 format: "2026-04-30T14:30:45.123456"
     */
    private String createdAt;

    /**
     * ID đơn đặt vé hoặc vé (tùy chọn)
     * Dùng để kết nối với entity
     */
    private Long entityId;

    /**
     * Loại entity
     * RESERVATION - Đơn đặt vé
     * TICKET - Vé điện tử
     */
    private String entityType;
}

