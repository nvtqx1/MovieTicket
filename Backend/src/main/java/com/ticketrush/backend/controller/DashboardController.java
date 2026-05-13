package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.projection.AgeGroupStatProjection;
import com.ticketrush.backend.dto.stats.DailyRevenueDTO;
import com.ticketrush.backend.dto.stats.GenderStatDTO;
import com.ticketrush.backend.dto.stats.GeneralStatsDTO;
import com.ticketrush.backend.dto.stats.MovieRevenueDTO;
import com.ticketrush.backend.dto.stats.TheaterRevenueDTO;
import com.ticketrush.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Controller quản lý Admin Dashboard - Thống kê & Báo cáo.
 * Cung cấp các endpoints để Admin xem báo cáo doanh thu, khách hàng.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/dashboard")
@AllArgsConstructor
@Tag(name = "📊 Admin Dashboard", description = "API thống kê & báo cáo doanh thu, khách hàng")
@PreAuthorize("hasRole('ADMIN')") // Chỉ Admin mới được truy cập
public class DashboardController {

    private final DashboardService dashboardService;

    // ========== GENERAL STATISTICS ==========

    /**
     * Lấy tổng quan thống kê chung (Overview Dashboard).
     *
     * API Endpoint: GET /api/v1/dashboard/summary
     *
     * Response chứa:
     * - Tổng số users
     * - Tổng số rạp
     * - Tổng số phim
     * - Tổng số suất chiếu
     * - Tổng doanh thu
     * - Tổng đơn đặt vé
     * - Tổng vé bán
     * - Doanh thu hôm nay
     * - Đơn đặt vé hôm nay
     *
     * @return GeneralStatsDTO chứa các số liệu chính
     *
     * HTTP Status:
     * - 200 OK: Lấy thống kê thành công
     * - 403 Forbidden: Không phải Admin
     * - 500 Internal Server Error: Lỗi server
     */
    @GetMapping("/summary")
    @Operation(
            summary = "📊 Lấy tổng quan thống kê chung",
            description = "Hiển thị overview dashboard với các KPI chính"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ Lấy thống kê thành công",
                    content = @Content(schema = @Schema(implementation = GeneralStatsDTO.class))),
            @ApiResponse(responseCode = "403", description = "❌ Không phải Admin"),
            @ApiResponse(responseCode = "500", description = "❌ Lỗi server")
    })
    public ResponseEntity<GeneralStatsDTO> getGeneralStatistics() {
        try {
            log.info("📊 Admin yêu cầu tổng quan thống kê chung");
            GeneralStatsDTO stats = dashboardService.getGeneralStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy tổng quan thống kê: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== GENDER STATISTICS ==========

    /**
     * Lấy thống kê giới tính của người dùng.
     *
     * API Endpoint: GET /api/v1/dashboard/gender-stats
     *
     * Response chứa danh sách với: giới tính, số lượng
     * Dùng để vẽ pie chart hoặc bar chart trên frontend
     *
     * @return Danh sách GenderStatDTO
     */
    @GetMapping("/gender-stats")
    @Operation(
            summary = "👥 Thống kê giới tính người dùng",
            description = "Hiển thị phân bố giới tính, dùng vẽ pie chart"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thống kê thành công")
    public ResponseEntity<List<GenderStatDTO>> getGenderStatistics() {
        try {
            log.info("📊 Admin yêu cầu thống kê giới tính");
            List<GenderStatDTO> stats = dashboardService.getGenderStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê giới tính: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== AGE GROUP STATISTICS ==========

    /**
     * Lấy thống kê người dùng theo nhóm tuổi.
     *
     * API Endpoint: GET /api/v1/dashboard/age-group-stats
     *
     * Response chứa danh sách với: nhóm tuổi, số lượng, phần trăm
     * Dùng để vẽ bar chart hoặc donut chart trên frontend
     *
     * @return Danh sách AgeGroupStatDTO
     */
    @GetMapping("/age-group-stats")
    @Operation(
            summary = "🎂 Thống kê nhóm tuổi người dùng",
            description = "Hiển thị phân bố người dùng theo nhóm tuổi"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thống kê thành công")
    public ResponseEntity<List<AgeGroupStatProjection>> getAgeGroupStatistics() {
        try {
            log.info("📊 Admin yêu cầu thống kê nhóm tuổi");
            List<AgeGroupStatProjection> stats = dashboardService.getAgeGroupStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê nhóm tuổi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== MOVIE REVENUE STATISTICS ==========

    /**
     * Lấy thống kê doanh thu theo phim.
     *
     * API Endpoint: GET /api/v1/dashboard/movie-revenue
     *
     * Response chứa danh sách với:
     * - ID phim
     * - Tên phim
     * - Tổng doanh thu
     * - Số suất chiếu
     * - Số vé bán
     *
     * Dùng để vẽ bar chart hoặc table trên frontend
     *
     * @return Danh sách MovieRevenueDTO
     */
    @GetMapping("/movie-revenue")
    @Operation(
            summary = "🎬 Thống kê doanh thu theo phim",
            description = "Hiển thị doanh thu từng phim (JOIN Movie -> Showtime -> Reservation)"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thống kê thành công")
    public ResponseEntity<List<MovieRevenueDTO>> getMovieRevenueStatistics() {
        try {
            log.info("📊 Admin yêu cầu thống kê doanh thu phim");
            List<MovieRevenueDTO> stats = dashboardService.getMovieRevenueStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê doanh thu phim: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== THEATER REVENUE STATISTICS ==========

    /**
     * Lấy thống kê doanh thu theo rạp.
     *
     * API Endpoint: GET /api/v1/dashboard/theater-revenue
     *
     * Response chứa danh sách với:
     * - ID rạp
     * - Tên rạp
     * - Địa chỉ
     * - Tổng doanh thu
     * - Số suất chiếu
     * - Số vé bán
     *
     * Dùng để so sánh hiệu suất giữa các rạp
     *
     * @return Danh sách TheaterRevenueDTO
     */
    @GetMapping("/theater-revenue")
    @Operation(
            summary = "🎭 Thống kê doanh thu theo rạp",
            description = "Hiển thị doanh thu từng rạp (JOIN Theater -> Showtime -> Reservation)"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thống kê thành công")
    public ResponseEntity<List<TheaterRevenueDTO>> getTheaterRevenueStatistics() {
        try {
            log.info("📊 Admin yêu cầu thống kê doanh thu rạp");
            List<TheaterRevenueDTO> stats = dashboardService.getTheaterRevenueStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê doanh thu rạp: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== DAILY REVENUE STATISTICS ==========

    /**
     * Lấy thống kê doanh thu theo ngày trong khoảng thời gian.
     *
     * API Endpoint: GET /api/v1/dashboard/daily-revenue?startDate=2026-04-01&endDate=2026-04-30
     *
     * @param startDate Ngày bắt đầu (format: yyyy-MM-dd)
     * @param endDate   Ngày kết thúc (format: yyyy-MM-dd)
     * @return Danh sách DailyRevenueDTO
     */
    @GetMapping("/daily-revenue")
    @Operation(
            summary = "📈 Thống kê doanh thu theo ngày",
            description = "Hiển thị doanh thu từng ngày trong khoảng thời gian, dùng vẽ line chart"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thống kê thành công")
    public ResponseEntity<List<DailyRevenueDTO>> getDailyRevenue(
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd)", example = "2026-04-01", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd)", example = "2026-04-30", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        try {
            log.info("📊 Admin yêu cầu thống kê doanh thu theo ngày từ {} đến {}", startDate, endDate);

            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }

            List<DailyRevenueDTO> stats = dashboardService.getDailyRevenueStatistics(startDate, endDate);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ Lỗi lấy thống kê doanh thu theo ngày: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy thống kê doanh thu 7 ngày gần nhất.
     *
     * API Endpoint: GET /api/v1/dashboard/daily-revenue/last-7-days
     *
     * @return Danh sách DailyRevenueDTO cho 7 ngày gần nhất
     */
    @GetMapping("/daily-revenue/last-7-days")
    @Operation(
            summary = "📊 Doanh thu 7 ngày gần nhất",
            description = "Lấy doanh thu của 7 ngày gần nhất để xem xu hướng tuần"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thống kê thành công")
    public ResponseEntity<List<DailyRevenueDTO>> getLast7DaysRevenue() {
        try {
            log.info("📊 Admin yêu cầu doanh thu 7 ngày gần nhất");
            List<DailyRevenueDTO> stats = dashboardService.getLast7DaysRevenue();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy doanh thu 7 ngày: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy thống kê doanh thu 30 ngày gần nhất.
     *
     * API Endpoint: GET /api/v1/dashboard/daily-revenue/last-30-days
     *
     * @return Danh sách DailyRevenueDTO cho 30 ngày gần nhất
     */
    @GetMapping("/daily-revenue/last-30-days")
    @Operation(
            summary = "📈 Doanh thu 30 ngày gần nhất",
            description = "Lấy doanh thu của 30 ngày gần nhất để xem xu hướng tháng"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thống kê thành công")
    public ResponseEntity<List<DailyRevenueDTO>> getLast30DaysRevenue() {
        try {
            log.info("📊 Admin yêu cầu doanh thu 30 ngày gần nhất");
            List<DailyRevenueDTO> stats = dashboardService.getLast30DaysRevenue();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy doanh thu 30 ngày: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy thống kê doanh thu tháng hiện tại.
     *
     * API Endpoint: GET /api/v1/dashboard/daily-revenue/current-month
     *
     * @return Danh sách DailyRevenueDTO cho tháng hiện tại
     */
    @GetMapping("/daily-revenue/current-month")
    @Operation(
            summary = "📅 Doanh thu tháng hiện tại",
            description = "Lấy doanh thu từ ngày đầu tháng đến hôm nay"
    )
    @ApiResponse(responseCode = "200", description = "✅ Lấy thống kê thành công")
    public ResponseEntity<List<DailyRevenueDTO>> getCurrentMonthRevenue() {
        try {
            log.info("📊 Admin yêu cầu doanh thu tháng hiện tại");
            List<DailyRevenueDTO> stats = dashboardService.getCurrentMonthDailyRevenue();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Lỗi lấy doanh thu tháng hiện tại: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== HEALTH CHECK ==========

    /**
     * Health check endpoint.
     *
     * API Endpoint: GET /api/v1/dashboard/health
     *
     * @return Thông báo trạng thái
     */
    @GetMapping("/health")
    @Operation(summary = "❤️ Health check", description = "Kiểm tra dịch vụ Dashboard")
    @ApiResponse(responseCode = "200", description = "✅ Dịch vụ hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ 📊 Dịch vụ Admin Dashboard hoạt động bình thường");
    }
}

