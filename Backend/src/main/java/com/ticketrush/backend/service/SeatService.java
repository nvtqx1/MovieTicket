package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.request.GenerateSeatRequest;
import com.ticketrush.backend.dto.response.GenerateSeatResponse;
import com.ticketrush.backend.dto.response.SeatResponse;

import java.util.List;

/**
 * Service interface để tự động sinh sơ đồ ghế cho suất chiếu
 * 
 * Chức năng chính:
 * - Tạo ma trận ghế dựa trên số hàng và số cột
 * - Sử dụng nested loop (O(rows × cols))
 * - Batch insert vào database (1 SQL query thay vì N queries)
 * 
 * Ví dụ sử dụng:
 * - Input: showtimeId=1, rows=10, cols=15
 * - Output: 150 ghế được tạo (A1 to J15)
 * - Performance: ~150ms (vs 2-3s manual entry)
 * 
 * Thiết kế Pattern:
 * - Interface-based design (dễ testing, mock)
 * - Implementation nằm trong service/impl/SeatServiceImpl.java
 * 
 * @author Backend Team
 * @version 1.0
 * @since NGÀY 7 (2026-04-17)
 */
/**
 * Dịch vụ quản lý sơ đồ ghế của suất chiếu.
 */
public interface SeatService {
    
    /**
     * Tự động sinh sơ đồ ghế hoàn chỉnh cho một suất chiếu
     * 
     * Quy trình:
     * 1. Kiểm tra suất chiếu tồn tại
     * 2. Kiểm tra chưa có ghế nào cho suất chiếu này
     * 3. Sử dụng nested loop tạo ma trận ghế
     *    - Hàng: A-Z (ký tự dựa trên char arithmetic)
     *    - Cột: 1-N (số nguyên)
     * 4. Batch save tất cả ghế với saveAll() (tối ưu performance)
     * 5. Trả về response chứa số ghế đã tạo
     * 
     * Ví dụ chi tiết:
     * Request:
     * {
     *   "showtimeId": 1,
     *   "rows": 10,
     *   "cols": 15
     * }
     * 
     * Response:
     * {
     *   "showtimeId": 1,
     *   "totalSeatsGenerated": 150,
     *   "rows": 10,
     *   "cols": 15,
     *   "message": "Đã tạo thành công 150 ghế cho suất chiếu (Hàng: 10, Cột: 15)"
     * }
     * 
     * Database Result:
     * Tạo 150 bản ghi trong bảng seats:
     * - A1, A2, A3, ..., A15 (hàng A)
     * - B1, B2, B3, ..., B15 (hàng B)
     * - ...
     * - J1, J2, J3, ..., J15 (hàng J)
     * Mỗi ghế có is_reserved=false (chưa được đặt)
     *
     * @param request GenerateSeatRequest chứa:
     *               - showtimeId: ID suất chiếu (bắt buộc)
     *               - rows: Số hàng (bắt buộc, >= 1)
     *               - cols: Số cột (bắt buộc, >= 1)
     * @return GenerateSeatResponse chứa:
     *         - showtimeId: ID suất chiếu
     *         - totalSeatsGenerated: Tổng số ghế tạo (rows × cols)
     *         - rows: Số hàng
     *         - cols: Số cột
     *         - message: Thông báo success bằng tiếng Việt
     * @throws RuntimeException nếu suất chiếu không tồn tại hoặc ghế đã tồn tại
     */
    /**
     * Sinh hoặc cập nhật ma trận ghế cho một suất chiếu.
     *
     * @param request thông tin suất chiếu, số hàng và số cột.
     * @return kết quả sinh ghế và tổng số ghế sau khi xử lý.
     * @throws RuntimeException nếu suất chiếu hoặc loại ghế bắt buộc không tồn tại.
     */
    GenerateSeatResponse generateSeatMatrix(GenerateSeatRequest request);

    /**
     * API: Lấy danh sách tất cả ghế của suất chiếu
     * 
     * GET /v1/showtimes/{id}/seats
     * 
     * Phục vụ cho Frontend vẽ sơ đồ ghế
     * 
     * @param showtimeId ID của suất chiếu
     * @return Danh sách ghế với trạng thái is_reserved
     * @throws IllegalArgumentException nếu suất chiếu không tồn tại
     */
    /**
     * Lấy danh sách ghế của một suất chiếu.
     *
     * @param showtimeId ID suất chiếu.
     * @return danh sách ghế kèm trạng thái và giá.
     * @throws IllegalArgumentException nếu suất chiếu không tồn tại.
     */
    List<SeatResponse> getSeatsByShowtime(Long showtimeId);

    /**
     * API: Xóa toàn bộ ghế của một suất chiếu
     * 
     * Phục vụ cho Admin khi cần tạo lại sơ đồ ghế
     * 
     * @param showtimeId ID của suất chiếu
     * @throws RuntimeException nếu suất chiếu đang có vé đã đặt
     */
    /**
     * Xóa toàn bộ ghế của một suất chiếu nếu chưa có ghế được đặt.
     *
     * @param showtimeId ID suất chiếu cần xóa ghế.
     * @throws RuntimeException nếu suất chiếu đang có vé đã đặt.
     */
    void deleteSeatsByShowtime(Long showtimeId);
}

