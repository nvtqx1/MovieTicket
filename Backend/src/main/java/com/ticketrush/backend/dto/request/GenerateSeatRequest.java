package com.ticketrush.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) cho request tạo sơ đồ ghế tự động
 * 
 * Được sử dụng bởi API: POST /api/v1/admin/seats/matrix/generate
 * 
 * Chứa thông tin cần thiết để sinh ma trận ghế cho một suất chiếu:
 * - showtimeId: ID của suất chiếu cần tạo ghế
 * - rows: Số hàng ghế (A-Z, ví dụ: 10 hàng = A đến J)
 * - cols: Số cột ghế (1-N, ví dụ: 15 cột = 1 đến 15)
 * 
 * Ví dụ: 10 hàng × 15 cột = 150 ghế (A1, A2, ..., J15)
 * 
 * Validation:
 * - showtimeId: Bắt buộc, phải tồn tại trong database
 * - rows: Bắt buộc, phải >= 1, max 26 (A-Z)
 * - cols: Bắt buộc, phải >= 1
 * 
 * @author Backend Team
 * @version 1.0
 * @since NGÀY 7 (2026-04-17)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSeatRequest {

    /**
     * ID của suất chiếu cần tạo ghế
     * 
     * Bắt buộc: Không được null
     * Ràng buộc: Phải tồn tại trong bảng showtimes
     */
    @NotNull(message = "Vui lòng cung cấp ID suất chiếu")
    private Long showtimeId;

    /**
     * Số hàng ghế trong sơ đồ
     * 
     * Ví dụ:
     * - rows = 5: Tạo 5 hàng (A, B, C, D, E)
     * - rows = 10: Tạo 10 hàng (A, B, C, D, E, F, G, H, I, J)
     * - rows = 20: Tạo 20 hàng (A-T)
     * 
     * Bắt buộc: Không được null
     * Ràng buộc: >= 1 (ít nhất 1 hàng)
     */
    @NotNull(message = "Vui lòng cung cấp số hàng ghế")
    @Min(value = 1, message = "Số hàng phải lớn hơn 0")
    private Integer rows;

    /**
     * Số cột ghế trong sơ đồ
     * 
     * Ví dụ:
     * - cols = 8: Tạo 8 cột ghế (1-8)
     * - cols = 15: Tạo 15 cột ghế (1-15)
     * - cols = 25: Tạo 25 cột ghế (1-25)
     * 
     * Bắt buộc: Không được null
     * Ràng buộc: >= 1 (ít nhất 1 cột)
     */
    @NotNull(message = "Vui lòng cung cấp số cột ghế")
    @Min(value = 1, message = "Số cột phải lớn hơn 0")
    private Integer cols;
}

