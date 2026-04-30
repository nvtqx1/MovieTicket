package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) cho response sau khi tạo sơ đồ ghế
 * 
 * Được trả về bởi API: POST /api/v1/admin/seats/matrix/generate
 * 
 * Chứa thông tin kết quả tạo ghế:
 * - showtimeId: ID suất chiếu đã tạo ghế
 * - totalSeatsGenerated: Tổng số ghế được tạo (rows × cols)
 * - rows: Số hàng ghế đã tạo
 * - cols: Số cột ghế đã tạo
 * - message: Thông báo thành công bằng tiếng Việt
 * 
 * Ví dụ response:
 * {
 *   "showtimeId": 1,
 *   "totalSeatsGenerated": 150,
 *   "rows": 10,
 *   "cols": 15,
 *   "message": "Đã tạo thành công 150 ghế cho suất chiếu (Hàng: 10, Cột: 15)"
 * }
 * 
 * @author Backend Team
 * @version 1.0
 * @since NGÀY 7 (2026-04-17)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSeatResponse {
    
    /**
     * ID của suất chiếu đã tạo ghế
     */
    private Long showtimeId;
    
    /**
     * Tổng số ghế được tạo = rows × cols
     * 
     * Ví dụ:
     * - 10 hàng × 15 cột = 150 ghế
     * - 20 hàng × 25 cột = 500 ghế
     * - 5 hàng × 8 cột = 40 ghế
     */
    private Integer totalSeatsGenerated;
    
    /**
     * Số hàng ghế đã tạo
     */
    private Integer rows;
    
    /**
     * Số cột ghế đã tạo
     */
    private Integer cols;
    
    /**
     * Thông báo thành công bằng tiếng Việt
     * 
     * Format: "Đã tạo thành công {N} ghế cho suất chiếu (Hàng: {rows}, Cột: {cols})"
     */
    private String message;
}

