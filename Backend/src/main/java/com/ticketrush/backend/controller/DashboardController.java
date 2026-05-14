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
import java.util.List;

/**
 * Controller cung cấp thống kê và báo cáo cho dashboard Admin.
 *
 * Annotation {@link PreAuthorize} giới hạn toàn bộ endpoint cho ADMIN;
 * {@link DateTimeFormat} giúp parse query ngày theo chuẩn ISO.
 */
@Slf4j
@RestController
@RequestMapping("/v1/dashboard")
@AllArgsConstructor
@Tag(name = "Admin Dashboard", description = "API thống kê và báo cáo doanh thu")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Lấy thống kê tổng quan của dashboard.
     *
     * @return các KPI chính của hệ thống.
     */
    @GetMapping("/summary")
    @Operation(summary = "Lấy tổng quan thống kê chung")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công",
                    content = @Content(schema = @Schema(implementation = GeneralStatsDTO.class))),
            @ApiResponse(responseCode = "403", description = "Không phải Admin"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<GeneralStatsDTO> getGeneralStatistics() {
        try {
            log.info("Admin yêu cầu tổng quan thống kê chung");
            GeneralStatsDTO stats = dashboardService.getGeneralStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Lỗi lấy tổng quan thống kê: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy thống kê người dùng theo giới tính.
     *
     * @return danh sách thống kê giới tính.
     */
    @GetMapping("/gender-stats")
    @Operation(summary = "Thống kê giới tính người dùng")
    @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    public ResponseEntity<List<GenderStatDTO>> getGenderStatistics() {
        try {
            log.info("Admin yêu cầu thống kê giới tính");
            List<GenderStatDTO> stats = dashboardService.getGenderStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Lỗi lấy thống kê giới tính: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy thống kê người dùng theo nhóm tuổi.
     *
     * @return danh sách thống kê nhóm tuổi.
     */
    @GetMapping("/age-group-stats")
    @Operation(summary = "Thống kê nhóm tuổi người dùng")
    @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    public ResponseEntity<List<AgeGroupStatProjection>> getAgeGroupStatistics() {
        try {
            log.info("Admin yêu cầu thống kê nhóm tuổi");
            List<AgeGroupStatProjection> stats = dashboardService.getAgeGroupStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Lỗi lấy thống kê nhóm tuổi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy thống kê doanh thu theo phim.
     *
     * @return danh sách doanh thu từng phim.
     */
    @GetMapping("/movie-revenue")
    @Operation(summary = "Thống kê doanh thu theo phim")
    @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    public ResponseEntity<List<MovieRevenueDTO>> getMovieRevenueStatistics() {
        try {
            log.info("Admin yêu cầu thống kê doanh thu phim");
            List<MovieRevenueDTO> stats = dashboardService.getMovieRevenueStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Lỗi lấy thống kê doanh thu phim: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy thống kê doanh thu theo rạp.
     *
     * @return danh sách doanh thu từng rạp.
     */
    @GetMapping("/theater-revenue")
    @Operation(summary = "Thống kê doanh thu theo rạp")
    @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    public ResponseEntity<List<TheaterRevenueDTO>> getTheaterRevenueStatistics() {
        try {
            log.info("Admin yêu cầu thống kê doanh thu rạp");
            List<TheaterRevenueDTO> stats = dashboardService.getTheaterRevenueStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Lỗi lấy thống kê doanh thu rạp: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy doanh thu từng ngày trong khoảng thời gian.
     *
     * @param startDate ngày bắt đầu theo định dạng ISO yyyy-MM-dd.
     * @param endDate ngày kết thúc theo định dạng ISO yyyy-MM-dd.
     * @return danh sách doanh thu theo ngày.
     */
    @GetMapping("/daily-revenue")
    @Operation(summary = "Thống kê doanh thu theo ngày")
    @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
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
            log.info("Admin yêu cầu thống kê doanh thu theo ngày từ {} đến {}", startDate, endDate);

            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }

            List<DailyRevenueDTO> stats = dashboardService.getDailyRevenueStatistics(startDate, endDate);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Lỗi lấy thống kê doanh thu theo ngày: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy doanh thu 7 ngày gần nhất.
     *
     * @return danh sách doanh thu 7 ngày gần nhất.
     */
    @GetMapping("/daily-revenue/last-7-days")
    @Operation(summary = "Doanh thu 7 ngày gần nhất")
    @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    public ResponseEntity<List<DailyRevenueDTO>> getLast7DaysRevenue() {
        try {
            log.info("Admin yêu cầu doanh thu 7 ngày gần nhất");
            List<DailyRevenueDTO> stats = dashboardService.getLast7DaysRevenue();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Lỗi lấy doanh thu 7 ngày: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy doanh thu 30 ngày gần nhất.
     *
     * @return danh sách doanh thu 30 ngày gần nhất.
     */
    @GetMapping("/daily-revenue/last-30-days")
    @Operation(summary = "Doanh thu 30 ngày gần nhất")
    @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    public ResponseEntity<List<DailyRevenueDTO>> getLast30DaysRevenue() {
        try {
            log.info("Admin yêu cầu doanh thu 30 ngày gần nhất");
            List<DailyRevenueDTO> stats = dashboardService.getLast30DaysRevenue();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Lỗi lấy doanh thu 30 ngày: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lấy doanh thu của tháng hiện tại.
     *
     * @return danh sách doanh thu từng ngày trong tháng hiện tại.
     */
    @GetMapping("/daily-revenue/current-month")
    @Operation(summary = "Doanh thu tháng hiện tại")
    @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    public ResponseEntity<List<DailyRevenueDTO>> getCurrentMonthRevenue() {
        try {
            log.info("Admin yêu cầu doanh thu tháng hiện tại");
            List<DailyRevenueDTO> stats = dashboardService.getCurrentMonthDailyRevenue();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Lỗi lấy doanh thu tháng hiện tại: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Kiểm tra trạng thái dịch vụ dashboard.
     *
     * @return thông báo dịch vụ đang hoạt động.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Kiểm tra dịch vụ Dashboard")
    @ApiResponse(responseCode = "200", description = "Dịch vụ hoạt động bình thường")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Dịch vụ Admin Dashboard hoạt động bình thường");
    }
}
