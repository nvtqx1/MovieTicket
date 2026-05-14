package com.ticketrush.backend.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Tiện ích tạo và mã hóa mã QR code.
 * Nhận vào một chuỗi dữ liệu và chuyển đổi thành:
 * - BitMatrix (ma trận điểm ảnh)
 * - Ảnh PNG
 * - Chuỗi Base64 (để hiển thị trên Frontend)
 *
 * @author TicketRush Team
 * @version 1.0
 */
/**
 * Tiện ích tạo QR code dạng ảnh PNG Base64 và Data URI.
 */
@Slf4j
@Component
public class QrCodeUtil {

    // Các hằng số cấu hình QR Code
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    private static final String IMAGE_FORMAT = "PNG";
    private static final BarcodeFormat BARCODE_FORMAT = BarcodeFormat.QR_CODE;

    /**
     * Tạo mã QR code từ chuỗi dữ liệu.
     * Mã QR được chuyển đổi thành chuỗi Base64 để sử dụng trực tiếp trên Frontend
     * qua thẻ: &lt;img src="data:image/png;base64,..."&gt;
     *
     * @param data Chuỗi dữ liệu để mã hóa (VD: "ORDER_123#abc123xyz")
     * @return Chuỗi Base64 biểu diễn ảnh PNG của mã QR
     * @throws Exception nếu quá trình mã hóa thất bại
     */
    /**
     * Tạo ảnh QR code từ chuỗi dữ liệu và trả về dạng Base64.
     *
     * @param data dữ liệu cần mã hóa vào QR code.
     * @return chuỗi Base64 của ảnh QR định dạng PNG.
     * @throws IllegalArgumentException nếu dữ liệu rỗng hoặc null.
     * @throws Exception nếu ZXing không mã hóa được dữ liệu hoặc lỗi tạo ảnh.
     */
    public String generateQrCodeBase64(String data) throws Exception {
        try {
            if (data == null || data.trim().isEmpty()) {
                throw new IllegalArgumentException("❌ Dữ liệu QR code không được để trống");
            }

            log.info("📝 Bắt đầu tạo mã QR cho dữ liệu: {}", data);

            // Bước 1: Tạo BitMatrix từ dữ liệu
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(data, BARCODE_FORMAT, QR_CODE_WIDTH, QR_CODE_HEIGHT);

            log.debug("✅ Tạo BitMatrix thành công - Kích thước: {}x{}", QR_CODE_WIDTH, QR_CODE_HEIGHT);

            // Bước 2: Chuyển BitMatrix thành ảnh PNG
            byte[] imageBytes = convertBitMatrixToImage(bitMatrix);

            log.debug("✅ Chuyển đổi BitMatrix thành ảnh thành công - Size: {} bytes", imageBytes.length);

            // Bước 3: Mã hóa ảnh thành Base64
            String base64String = Base64.getEncoder().encodeToString(imageBytes);

            log.info("✅ Tạo mã QR Base64 thành công - Chuỗi dài: {} ký tự", base64String.length());

            return base64String;

        } catch (WriterException e) {
            log.error("❌ Lỗi mã hóa ZXing: {}", e.getMessage(), e);
            throw new Exception("❌ Lỗi tạo mã QR: Không thể mã hóa dữ liệu", e);
        } catch (IllegalArgumentException e) {
            log.warn("❌ Dữ liệu không hợp lệ: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Lỗi không xác định khi tạo mã QR: {}", e.getMessage(), e);
            throw new Exception("❌ Lỗi không xác định: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo mã QR code từ ID đơn đặt và hash bí mật.
     * Định dạng dữ liệu: "RESERVATION_{id}#{hash}"
     *
     * @param reservationId ID đơn đặt vé (VD: 123)
     * @param secretHash Hash bí mật được tạo từ salt (VD: "abc123xyz")
     * @return Chuỗi Base64 biểu diễn ảnh PNG của mã QR
     * @throws Exception nếu quá trình mã hóa thất bại
     */
    /**
     * Tạo QR code cho đơn đặt vé theo định dạng {@code RESERVATION_{id}#{hash}}.
     *
     * @param reservationId ID đơn đặt vé.
     * @param secretHash hash bí mật dùng để xác thực đơn.
     * @return chuỗi Base64 của ảnh QR định dạng PNG.
     * @throws IllegalArgumentException nếu reservationId hoặc secretHash không hợp lệ.
     * @throws Exception nếu tạo QR thất bại.
     */
    public String generateReservationQrCode(Long reservationId, String secretHash) throws Exception {
        if (reservationId == null || reservationId <= 0) {
            throw new IllegalArgumentException("❌ ID đơn đặt vé không hợp lệ");
        }
        if (secretHash == null || secretHash.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Hash bí mật không được để trống");
        }

        String data = String.format("RESERVATION_%d#%s", reservationId, secretHash);
        return generateQrCodeBase64(data);
    }

    /**
     * Tạo mã QR code từ ID vé và hash xác minh.
     * Định dạng dữ liệu: "TICKET_{id}#{hash}"
     *
     * @param ticketId ID vé (VD: 456)
     * @param verificationHash Hash xác minh vé
     * @return Chuỗi Base64 biểu diễn ảnh PNG của mã QR
     * @throws Exception nếu quá trình mã hóa thất bại
     */
    /**
     * Tạo QR code cho vé theo định dạng {@code TICKET_{id}#{hash}}.
     *
     * @param ticketId ID vé.
     * @param verificationHash hash xác minh vé.
     * @return chuỗi Base64 của ảnh QR định dạng PNG.
     * @throws IllegalArgumentException nếu ticketId hoặc verificationHash không hợp lệ.
     * @throws Exception nếu tạo QR thất bại.
     */
    public String generateTicketQrCode(Long ticketId, String verificationHash) throws Exception {
        if (ticketId == null || ticketId <= 0) {
            throw new IllegalArgumentException("❌ ID vé không hợp lệ");
        }
        if (verificationHash == null || verificationHash.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Hash xác minh không được để trống");
        }

        String data = String.format("TICKET_%d#%s", ticketId, verificationHash);
        return generateQrCodeBase64(data);
    }

    /**
     * Chuyển đổi BitMatrix thành mảng byte ảnh PNG.
     * Sử dụng BufferedImage và ImageIO để ghi ảnh vào ByteArrayOutputStream.
     *
     * @param bitMatrix Ma trận điểm ảnh từ ZXing
     * @return Mảng byte chứa dữ liệu ảnh PNG
     * @throws IOException nếu ghi ảnh thất bại
     */
    /**
     * Chuyển ma trận QR của ZXing thành mảng byte ảnh PNG.
     *
     * @param bitMatrix ma trận điểm ảnh QR.
     * @return mảng byte của ảnh PNG.
     * @throws IOException nếu ghi ảnh vào stream thất bại.
     */
    private byte[] convertBitMatrixToImage(BitMatrix bitMatrix) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("❌ Lỗi chuyển đổi BitMatrix thành ảnh: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Tạo chuỗi Data URI để sử dụng trực tiếp trong thẻ HTML img.
     * Chuỗi có dạng: data:image/png;base64,{base64String}
     *
     * @param base64String Chuỗi Base64 của ảnh QR code
     * @return Data URI string
     */
    /**
     * Tạo Data URI để hiển thị QR trực tiếp trong thẻ HTML {@code img}.
     *
     * @param base64String chuỗi Base64 của ảnh QR.
     * @return Data URI dạng {@code data:image/png;base64,...}.
     */
    public String createDataUri(String base64String) {
        return String.format("data:image/%s;base64,%s", IMAGE_FORMAT.toLowerCase(), base64String);
    }

    /**
     * Kiểm tra độ dài tối đa mà mã QR có thể chứa.
     * Kích thước QR phụ thuộc vào mức sửa lỗi (Error Correction Level).
     *
     * @return Mô tả kích thước tối đa
     */
    /**
     * Lấy mô tả dung lượng dữ liệu tối đa của QR code.
     *
     * @return chuỗi mô tả dung lượng QR code.
     */
    public String getQrCodeCapacity() {
        return String.format("📊 Mã QR %dx%d có thể chứa tối đa ~4296 ký tự (maximum alphanumeric data)", 
                QR_CODE_WIDTH, QR_CODE_HEIGHT);
    }

    /**
     * Lấy thông tin cấu hình QR code hiện tại.
     *
     * @return Chuỗi mô tả cấu hình
     */
    /**
     * Lấy mô tả cấu hình QR code hiện tại.
     *
     * @return chuỗi mô tả định dạng, kích thước và kiểu mã hóa QR.
     */
    public String getQrCodeInfo() {
        return String.format("🎫 Cấu hình QR Code:%n" +
                "- Định dạng: %s%n" +
                "- Kích thước: %dx%d pixels%n" +
                "- Loại hình ảnh: %s%n" +
                "- Mã hóa: Base64",
                BARCODE_FORMAT, QR_CODE_WIDTH, QR_CODE_HEIGHT, IMAGE_FORMAT);
    }
}

