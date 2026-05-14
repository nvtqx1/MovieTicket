package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.response.QrCodeResponse;
import com.ticketrush.backend.util.QrCodeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dịch vụ quản lý tạo và xử lý mã QR code.
 * Sử dụng QrCodeUtil để mã hóa dữ liệu thành QR code.
 * Trả về phản hồi dạng DTO để gửi tới Frontend.
 *
 * @author TicketRush Team
 * @version 1.0
 */
/**
 * Dịch vụ tạo QR code cho reservation, vé và dữ liệu tùy chỉnh.
 */
@Slf4j
@Service
@AllArgsConstructor
public class QrCodeService {

    private final QrCodeUtil qrCodeUtil;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Tạo mã QR cho đơn đặt vé.
     * Dữ liệu: RESERVATION_{id}#{secretHash}
     *
     * @param reservationId ID đơn đặt vé
     * @param secretHash Hash bí mật
     * @return QrCodeResponse chứa Base64 và Data URI
     */
    /**
     * Tạo QR code cho đơn đặt vé từ reservationId và secret hash.
     *
     * @param reservationId ID đơn đặt vé.
     * @param secretHash hash bí mật để xác thực QR.
     * @return phản hồi chứa QR dạng Base64, Data URI và metadata.
     */
    public QrCodeResponse generateReservationQrCode(Long reservationId, String secretHash) {
        try {
            log.info("🎫 Tạo mã QR cho đơn đặt vé ID: {}", reservationId);

            String base64String = qrCodeUtil.generateReservationQrCode(reservationId, secretHash);
            String dataUri = qrCodeUtil.createDataUri(base64String);
            String encodedData = String.format("RESERVATION_%d#%s", reservationId, secretHash);

            log.info("✅ Tạo mã QR đơn đặt vé thành công - Size: {} bytes", base64String.length());

            return buildSuccessResponse(base64String, dataUri, encodedData, reservationId, "RESERVATION");

        } catch (Exception e) {
            log.error("❌ Lỗi tạo mã QR đơn đặt vé: {}", e.getMessage(), e);
            return buildErrorResponse("❌ Lỗi tạo mã QR: " + e.getMessage());
        }
    }

    /**
     * Tạo mã QR cho vé điện tử.
     * Dữ liệu: TICKET_{id}#{verificationHash}
     *
     * @param ticketId ID vé
     * @param verificationHash Hash xác minh
     * @return QrCodeResponse chứa Base64 và Data URI
     */
    /**
     * Tạo QR code cho vé điện tử.
     *
     * @param ticketId ID vé.
     * @param verificationHash hash xác minh vé.
     * @return phản hồi chứa QR dạng Base64, Data URI và metadata.
     */
    public QrCodeResponse generateTicketQrCode(Long ticketId, String verificationHash) {
        try {
            log.info("🎫 Tạo mã QR cho vé ID: {}", ticketId);

            String base64String = qrCodeUtil.generateTicketQrCode(ticketId, verificationHash);
            String dataUri = qrCodeUtil.createDataUri(base64String);
            String encodedData = String.format("TICKET_%d#%s", ticketId, verificationHash);

            log.info("✅ Tạo mã QR vé thành công - Size: {} bytes", base64String.length());

            return buildSuccessResponse(base64String, dataUri, encodedData, ticketId, "TICKET");

        } catch (Exception e) {
            log.error("❌ Lỗi tạo mã QR vé: {}", e.getMessage(), e);
            return buildErrorResponse("❌ Lỗi tạo mã QR: " + e.getMessage());
        }
    }

    /**
     * Tạo mã QR từ dữ liệu tùy chỉnh.
     * Hữu ích cho các trường hợp không phải đơn đặt hoặc vé.
     *
     * @param customData Dữ liệu tùy chỉnh
     * @return QrCodeResponse chứa Base64 và Data URI
     */
    /**
     * Tạo QR code từ dữ liệu tùy chỉnh.
     *
     * @param customData dữ liệu cần mã hóa vào QR.
     * @return phản hồi chứa QR dạng Base64, Data URI và metadata.
     */
    public QrCodeResponse generateCustomQrCode(String customData) {
        try {
            log.info("🎫 Tạo mã QR tùy chỉnh từ dữ liệu: {}", customData);

            String base64String = qrCodeUtil.generateQrCodeBase64(customData);
            String dataUri = qrCodeUtil.createDataUri(base64String);

            log.info("✅ Tạo mã QR tùy chỉnh thành công - Size: {} bytes", base64String.length());

            return buildSuccessResponse(base64String, dataUri, customData, null, "CUSTOM");

        } catch (Exception e) {
            log.error("❌ Lỗi tạo mã QR tùy chỉnh: {}", e.getMessage(), e);
            return buildErrorResponse("❌ Lỗi tạo mã QR: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin cấu hình QR code.
     *
     * @return Chuỗi mô tả cấu hình
     */
    /**
     * Lấy thông tin cấu hình QR code đang sử dụng.
     *
     * @return chuỗi mô tả cấu hình QR code.
     */
    public String getQrCodeInfo() {
        return qrCodeUtil.getQrCodeInfo();
    }

    /**
     * Lấy thông tin dung lượng tối đa QR code.
     *
     * @return Chuỗi mô tả dung lượng
     */
    /**
     * Lấy thông tin dung lượng tối đa của QR code.
     *
     * @return chuỗi mô tả dung lượng QR code.
     */
    public String getQrCodeCapacity() {
        return qrCodeUtil.getQrCodeCapacity();
    }

    // ========== Private Helper Methods ==========

    /**
     * Xây dựng QrCodeResponse thành công.
     *
     * @param base64String Chuỗi Base64
     * @param dataUri Data URI
     * @param encodedData Dữ liệu được mã hóa
     * @param entityId ID entity (nếu có)
     * @param entityType Loại entity
     * @return QrCodeResponse hoàn chỉnh
     */
    /**
     * Tạo response thành công cho QR code.
     *
     * @param base64String ảnh QR dạng Base64.
     * @param dataUri ảnh QR dạng Data URI.
     * @param encodedData dữ liệu đã mã hóa trong QR.
     * @param entityId ID entity liên quan, có thể null.
     * @param entityType loại entity liên quan.
     * @return DTO phản hồi tạo QR thành công.
     */
    private QrCodeResponse buildSuccessResponse(String base64String, String dataUri, String encodedData,
                                               Long entityId, String entityType) {
        return QrCodeResponse.builder()
                .base64String(base64String)
                .dataUri(dataUri)
                .encodedData(encodedData)
                .imageSize("300x300")
                .imageFormat("PNG")
                .status("SUCCESS")
                .message("✅ Tạo mã QR thành công")
                .fileSizeBytes((long) base64String.length())
                .createdAt(LocalDateTime.now().format(DATETIME_FORMATTER))
                .entityId(entityId)
                .entityType(entityType)
                .build();
    }

    /**
     * Xây dựng QrCodeResponse lỗi.
     *
     * @param errorMessage Thông báo lỗi
     * @return QrCodeResponse với status lỗi
     */
    /**
     * Tạo response lỗi khi tạo QR thất bại.
     *
     * @param errorMessage thông báo lỗi.
     * @return DTO phản hồi lỗi.
     */
    private QrCodeResponse buildErrorResponse(String errorMessage) {
        return QrCodeResponse.builder()
                .status("FAILED")
                .message(errorMessage)
                .createdAt(LocalDateTime.now().format(DATETIME_FORMATTER))
                .build();
    }
}

