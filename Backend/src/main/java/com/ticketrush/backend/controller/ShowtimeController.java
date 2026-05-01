package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.SeatResponse;
import com.ticketrush.backend.dto.ShowtimeResponse;
import com.ticketrush.backend.service.SeatService;
import com.ticketrush.backend.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller quản lý Suất chiếu và Ghế
 * 
 * Endpoints:
 * - GET /v1/showtimes: Tìm kiếm suất chiếu
 * - GET /v1/showtimes/{id}: Lấy chi tiết suất chiếu
 * - GET /v1/showtimes/{id}/seats: Lấy danh sách ghế (VỀ LỖ HỔNG 2)
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@RestController
@RequestMapping("/v1/showtimes")
@RequiredArgsConstructor
@Tag(name = "🎬 Showtime Management", description = "API quản lý suất chiếu")
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final SeatService seatService;

    /**
     * GET /v1/showtimes?movieId=1&theaterId=2&showDate=2026-05-01&page=0&size=10
     * Tìm kiếm suất chiếu theo phim, rạp, hoặc ngày với phân trang
     * 
     * ⚠️ QUAN TRỌNG: Sử dụng pagination để tránh OutOfMemory
     * khi có hàng ngàn suất chiếu
     */
    @GetMapping
    @Operation(
        summary = "🔍 Tìm kiếm suất chiếu (Phân trang)",
        description = "Tìm kiếm suất chiếu theo phim, rạp, ngày với phân trang. " +
            "⚠️ Bắt buộc phân trang để tránh OutOfMemory"
    )
    @ApiResponse(responseCode = "200", description = "✅ Tìm kiếm thành công")
    public ResponseEntity<Page<ShowtimeResponse>> searchShowtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(showtimeService.searchShowtimes(movieId, theaterId, showDate, pageable));
    }

    /**
     * GET /v1/showtimes/{id}
     * Lấy chi tiết suất chiếu
     */
    @GetMapping("/{id}")
    @Operation(summary = "🎬 Lấy chi tiết suất chiếu", description = "Lấy thông tin chi tiết của một suất chiếu")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "✅ Lấy thành công"),
        @ApiResponse(responseCode = "404", description = "❌ Suất chiếu không tồn tại")
    })
    public ResponseEntity<ShowtimeResponse> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    /**
     * VỀ LỖ HỔNG 2: API Lấy danh sách ghế của suất chiếu
     * 
     * GET /v1/showtimes/{id}/seats
     * 
     * Mục tiêu: Frontend có thể vẽ bản đồ 150 ghế nhanh chóng
     * Tách riêng danh sách ghế ra khỏi API GET /v1/showtimes/{id}
     * giúp giảm tải dung lượng trả về, Frontend chạy nhẹ mượt
     * 
     * Response: Mảng danh sách ghế gồm:
     * - ID ghế
     * - Số ghế (A1, A2, B1, ...)
     * - Loại ghế (NORMAL, VIP, COUPLE)
     * - Trạng thái is_reserved (true = đã đặt, false = còn trống)
     * - Giá bán
     * 
     * @param id ID của suất chiếu
     * @return Danh sách ghế của suất chiếu đó
     */
    @GetMapping("/{id}/seats")
    @Operation(
        summary = "🪑 Lấy danh sách ghế của suất chiếu",
        description = "Lấy tất cả ghế của một suất chiếu để Frontend vẽ sơ đồ ghế. " +
            "Bao gồm ID ghế, số ghế, loại ghế, trạng thái đặt, và giá bán."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "✅ Lấy thành công",
            content = @Content(schema = @Schema(implementation = SeatResponse.class))),
        @ApiResponse(responseCode = "404", description = "❌ Suất chiếu không tồn tại")
    })
    public ResponseEntity<List<SeatResponse>> getSeatsByShowtime(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatsByShowtime(id));
    }
}

