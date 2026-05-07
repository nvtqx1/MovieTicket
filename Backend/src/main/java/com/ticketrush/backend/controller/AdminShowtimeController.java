package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.CreateShowtimeRequest;
import com.ticketrush.backend.dto.ShowtimeResponse;
import com.ticketrush.backend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Admin API Controller - Quản lý Phim & Suất Chiếu
 * VỀ LỖ HỔNG 3: Admin giờ đây phải thao tác với Phòng chiếu chứ không chỉ Rạp chung chung.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin/showtimes")
@AllArgsConstructor
@Tag(name = "🎬 Admin Management", description = "API quản lý phim & suất chiếu (chỉ Admin)")
@PreAuthorize("hasRole('ADMIN')") // Chỉ Admin mới được truy cập
public class AdminShowtimeController {

    private final AdminService adminService;

    // ═══════════════════════════════════════════════
    // TASK 1.2: GET Showtime List với Filter
    // ═══════════════════════════════════════════════

    @GetMapping
    @Operation(
            summary = "📋 Danh sách lịch chiếu (có filter)",
            description = "Lấy danh sách lịch chiếu, có thể lọc theo rạp và ngày",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<List<ShowtimeResponse>> listShowtimes(
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String date) {
        try {
            LocalDate filterDate = null;
            if (date != null && !date.trim().isEmpty()) {
                try {
                    filterDate = LocalDate.parse(date);
                } catch (Exception ex) {
                    try {
                        filterDate = LocalDate.parse(date, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception innerEx) {
                        log.warn("❌ Không thể parse ngày: {}. Bỏ qua filter ngày.", date);
                    }
                }
            }
            List<ShowtimeResponse> showtimes = adminService.getFilteredShowtimes(theaterId, movieId, filterDate);
            return ResponseEntity.ok(showtimes);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy danh sách showtime: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "🗑️ Xóa lịch chiếu",
            description = "Admin xóa lịch chiếu (chỉ khi chưa có vé nào được đặt)",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<?> deleteShowtime(@PathVariable Long id) {
        try {
            adminService.deleteShowtime(id);
            return ResponseEntity.ok(Map.of("message", "Xóa lịch chiếu thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * API Thêm Suất Chiếu (Create Showtime) - VỀ LỖ HỔNG 3
     * 
     * API Endpoint: POST /api/v1/admin/showtimes
     * 
     * Thay đổi ở DTO:
     * - Thay vì nhận theaterId từ Frontend, bây giờ Request phải nhận roomId
     * - Admin khi chọn xếp lịch phải xác định rõ phim này chiếu ở Phòng 1 hay Phòng 2
     * - Lưu thẳng room_id này xuống bảng showtimes
     *
     * @param request CreateShowtimeRequest chứa:
     *                - movieId: ID phim
     *                - roomId: ID phòng chiếu (THỐ LỖ HỔNG 3: Không còn theaterId)
     *                - showDate: Ngày chiếu
     *                - showTime: Giờ chiếu
     *                - price: Giá vé
     *                - isFlashSale: Có phải flash sale
     * @return ShowtimeResponse với thông tin suất chiếu vừa tạo
     */
    @PostMapping
    @Operation(
            summary = "🎬 Thêm Suất Chiếu (Create Showtime)",
            description = "Admin tạo suất chiếu mới với roomId (không phải theaterId). " +
                    "Admin phải chọn cụ thể Phòng chiếu (IMAX 01, IMAX 02, ...) cho phim. " +
                    "Lưu room_id thẳng xuống bảng showtimes.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "✅ Thêm suất chiếu thành công",
                    content = @Content(schema = @Schema(implementation = ShowtimeResponse.class))),
            @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "❌ Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "❌ Không phải Admin"),
            @ApiResponse(responseCode = "404", description = "❌ Phim hoặc Phòng chiếu không tồn tại"),
            @ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    public ResponseEntity<?> createShowtime(
            @RequestBody
            @Parameter(description = "Dữ liệu tạo suất chiếu", required = true)
            CreateShowtimeRequest request) {
        try {
            log.info("🎬 Admin tạo suất chiếu mới: phim {}, phòng {}, ngày {}", 
                    request.getMovieId(), request.getRoomId(), request.getShowDate());

            // Validate input
            if (request.getMovieId() == null || request.getMovieId() <= 0) {
                log.warn("❌ ID phim không hợp lệ: {}", request.getMovieId());
                return ResponseEntity.badRequest().body(
                        Map.of("message", "❌ ID phim phải > 0", "apiStatus", "FAILED")
                );
            }

            if (request.getRoomId() == null || request.getRoomId() <= 0) {
                log.warn("❌ ID phòng chiếu không hợp lệ: {}", request.getRoomId());
                return ResponseEntity.badRequest().body(
                        Map.of("message", "❌ ID phòng chiếu phải > 0 (không phải theaterId)", "apiStatus", "FAILED")
                );
            }

            if (request.getShowDate() == null || request.getShowTime() == null) {
                log.warn("❌ Ngày giờ chiếu không hợp lệ");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "❌ Ngày giờ chiếu không được để trống", "apiStatus", "FAILED")
                );
            }

            if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                log.warn("❌ Giá vé không hợp lệ: {}", request.getPrice());
                return ResponseEntity.badRequest().body(
                        Map.of("message", "❌ Giá vé phải > 0", "apiStatus", "FAILED")
                );
            }

            // Gọi service để tạo suất chiếu
            ShowtimeResponse response = adminService.createShowtime(request);

            log.info("✅ Tạo suất chiếu thành công: ID = {}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Lỗi validation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("message", "❌ " + e.getMessage(), "apiStatus", "FAILED")
            );
        } catch (Exception e) {
            log.error("❌ Lỗi tạo suất chiếu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "❌ Lỗi server: " + e.getMessage(), "apiStatus", "FAILED")
            );
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    @Operation(summary = "❤️ Health check", description = "Kiểm tra dịch vụ admin")
    @ApiResponse(responseCode = "200", description = "✅ Dịch vụ hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ 🎬 Dịch vụ Admin (Manage Movies & Showtimes) hoạt động bình thường");
    }
}


