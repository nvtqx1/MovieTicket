package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.CreateShowtimeRequest;
import com.ticketrush.backend.dto.response.ShowtimeResponse;
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
 * Controller quản trị suất chiếu.
 *
 * Annotation {@link PreAuthorize} giới hạn toàn bộ endpoint cho người dùng có
 * role ADMIN; {@link Slf4j} cung cấp logger cho xử lý lỗi và audit.
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin/showtimes")
@AllArgsConstructor
@Tag(name = "Admin Management", description = "API quản lý suất chiếu cho Admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShowtimeController {

    private final AdminService adminService;

    /**
     * Lấy danh sách suất chiếu có lọc theo rạp, phim và ngày.
     *
     * Ngày được parse theo ISO trước, sau đó thử định dạng {@code dd/MM/yyyy};
     * nếu parse lỗi thì bỏ qua filter ngày để API vẫn trả dữ liệu.
     *
     * @param theaterId ID rạp cần lọc, có thể null.
     * @param movieId ID phim cần lọc, có thể null.
     * @param date ngày cần lọc ở dạng chuỗi, có thể null.
     * @return danh sách suất chiếu phù hợp điều kiện lọc.
     */
    @GetMapping
    @Operation(
            summary = "Danh sách lịch chiếu có lọc",
            description = "Lấy danh sách lịch chiếu, có thể lọc theo rạp, phim và ngày",
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
                        log.warn("Không thể parse ngày: {}. Bỏ qua filter ngày.", date);
                    }
                }
            }
            List<ShowtimeResponse> showtimes = adminService.getFilteredShowtimes(theaterId, movieId, filterDate);
            return ResponseEntity.ok(showtimes);
        } catch (Exception e) {
            log.error("Lỗi lấy danh sách showtime: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Xóa một suất chiếu.
     *
     * @param id ID suất chiếu cần xóa.
     * @return thông báo xóa thành công hoặc lỗi nghiệp vụ.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xóa lịch chiếu",
            description = "Admin xóa lịch chiếu khi chưa có vé được đặt",
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
     * Tạo suất chiếu mới cho một phòng chiếu.
     *
     * Method tự kiểm tra các trường bắt buộc trước khi gọi service để trả lỗi
     * rõ ràng cho frontend.
     *
     * @param request dữ liệu tạo suất chiếu gồm phim, phòng, ngày, giờ và giá vé.
     * @return suất chiếu vừa tạo hoặc thông báo lỗi validate.
     */
    @PostMapping
    @Operation(
            summary = "Thêm suất chiếu",
            description = "Admin tạo suất chiếu mới với roomId",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Thêm suất chiếu thành công",
                    content = @Content(schema = @Schema(implementation = ShowtimeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không phải Admin"),
            @ApiResponse(responseCode = "404", description = "Phim hoặc phòng chiếu không tồn tại"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<?> createShowtime(
            @RequestBody
            @Parameter(description = "Dữ liệu tạo suất chiếu", required = true)
            CreateShowtimeRequest request) {
        try {
            log.info("Admin tạo suất chiếu mới: phim {}, phòng {}, ngày {}",
                    request.getMovieId(), request.getRoomId(), request.getShowDate());

            if (request.getMovieId() == null || request.getMovieId() <= 0) {
                log.warn("ID phim không hợp lệ: {}", request.getMovieId());
                return ResponseEntity.badRequest().body(
                        Map.of("message", "ID phim phải > 0", "apiStatus", "FAILED")
                );
            }

            if (request.getRoomId() == null || request.getRoomId() <= 0) {
                log.warn("ID phòng chiếu không hợp lệ: {}", request.getRoomId());
                return ResponseEntity.badRequest().body(
                        Map.of("message", "ID phòng chiếu phải > 0", "apiStatus", "FAILED")
                );
            }

            if (request.getShowDate() == null || request.getShowTime() == null) {
                log.warn("Ngày giờ chiếu không hợp lệ");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Ngày giờ chiếu không được để trống", "apiStatus", "FAILED")
                );
            }

            if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                log.warn("Giá vé không hợp lệ: {}", request.getPrice());
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Giá vé phải > 0", "apiStatus", "FAILED")
                );
            }

            ShowtimeResponse response = adminService.createShowtime(request);

            log.info("Tạo suất chiếu thành công: ID = {}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage(), "apiStatus", "FAILED")
            );
        } catch (Exception e) {
            log.error("Lỗi tạo suất chiếu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Lỗi server: " + e.getMessage(), "apiStatus", "FAILED")
            );
        }
    }

    /**
     * Kiểm tra trạng thái dịch vụ quản trị suất chiếu.
     *
     * @return thông báo dịch vụ đang hoạt động.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Kiểm tra dịch vụ admin")
    @ApiResponse(responseCode = "200", description = "Dịch vụ hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Dịch vụ Admin (Manage Movies & Showtimes) hoạt động bình thường");
    }
}
