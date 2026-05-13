package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.CreateRoomRequest;
import com.ticketrush.backend.dto.request.CreateTheaterRequest;
import com.ticketrush.backend.dto.response.RoomResponse;
import com.ticketrush.backend.dto.response.ShowtimeResponse;
import com.ticketrush.backend.dto.response.TheaterResponse;
import com.ticketrush.backend.entity.Theater;
import com.ticketrush.backend.repository.ShowtimeRepository;
import com.ticketrush.backend.repository.TheaterRepository;
import com.ticketrush.backend.service.TheaterService;
import com.ticketrush.backend.service.impl.ShowtimeServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller quản lý Rạp chiếu và Phòng chiếu
 * 
 * Endpoints:
 * - GET /v1/theaters: Lấy danh sách rạp
 * - GET /v1/theaters/{id}/schedule: Lịch chiếu 6 ngày tới (Task 2.2)
 * - POST /v1/admin/theaters: Admin tạo rạp mới
 * - POST /v1/admin/theaters/{theaterId}/rooms: Admin tạo phòng
 * - GET /v1/theaters/{theaterId}/rooms: Lấy danh sách phòng của rạp
 * 
 * @author TicketRush Team
 * @version 2.0
 */
@RestController
@RequestMapping("/v1/theaters")
@RequiredArgsConstructor
@Tag(name = "🏛️ Theater Management", description = "API quản lý rạp chiếu")
public class TheaterController {

    private final TheaterService theaterService;
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeServiceImpl showtimeServiceImpl;
    private final TheaterRepository theaterRepository;

    /**
     * GET /v1/theaters
     * Lấy danh sách tất cả rạp chiếu
     */
    @GetMapping
    @Operation(summary = "📋 Lấy danh sách rạp", description = "Lấy tất cả rạp chiếu đang hoạt động")
    @ApiResponse(responseCode = "200", description = "✅ Lấy thành công")
    public ResponseEntity<List<TheaterResponse>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    /**
     * Task 2.2: GET /v1/theaters/{id}/schedule
     * Lấy lịch chiếu của rạp trong 6 ngày tới, nhóm theo ngày
     * 
     * Response format:
     * {
     *   "theaterId": 1,
     *   "theaterName": "CGV Hồ Tây",
     *   "schedule": {
     *     "2026-05-07": [ ShowtimeResponse, ... ],
     *     "2026-05-08": [ ShowtimeResponse, ... ]
     *   }
     * }
     */
    @GetMapping("/{id}/schedule")
    @Operation(summary = "📅 Lấy lịch chiếu của rạp (6 ngày tới)")
    public ResponseEntity<?> getTheaterSchedule(@PathVariable Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rạp không tồn tại"));

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(5); // 6 ngày (hôm nay + 5)

        var showtimes = showtimeRepository.findByTheaterAndDateRange(id, today, endDate);

        // Nhóm theo ngày, sắp xếp theo thứ tự ngày
        Map<LocalDate, List<ShowtimeResponse>> schedule = showtimes.stream()
                .map(showtimeServiceImpl::toResponse)
                .collect(Collectors.groupingBy(
                        ShowtimeResponse::showDate,
                        TreeMap::new,
                        Collectors.toList()
                ));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("theaterId", theater.getId());
        result.put("theaterName", theater.getName());
        result.put("location", theater.getLocation());
        result.put("schedule", schedule);

        return ResponseEntity.ok(result);
    }

    /**
     * POST /v1/theaters - Admin tạo rạp mới
     */
    @PostMapping
    @Operation(
        summary = "➕ Admin: Tạo rạp chiếu mới",
        description = "Admin API để tạo rạp chiếu mới",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "✅ Tạo rạp thành công",
            content = @Content(schema = @Schema(implementation = TheaterResponse.class))),
        @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "❌ Chưa đăng nhập"),
        @ApiResponse(responseCode = "409", description = "❌ Rạp đã tồn tại")
    })
    public ResponseEntity<TheaterResponse> createTheater(@RequestBody CreateTheaterRequest request) {
        try {
            TheaterResponse response = theaterService.createTheater(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /v1/theaters/{theaterId}/rooms
     * Lấy danh sách phòng chiếu của một rạp
     */
    @GetMapping("/{theaterId}/rooms")
    @Operation(summary = "🎬 Lấy danh sách phòng của rạp", description = "Lấy tất cả phòng chiếu trong một rạp")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "✅ Lấy thành công"),
        @ApiResponse(responseCode = "404", description = "❌ Rạp không tồn tại")
    })
    public ResponseEntity<List<RoomResponse>> getRoomsByTheater(@PathVariable Long theaterId) {
        return ResponseEntity.ok(theaterService.getRoomsByTheater(theaterId));
    }

    /**
     * POST /v1/theaters/{theaterId}/rooms - Admin tạo phòng
     */
    @PostMapping("/{theaterId}/rooms")
    @Operation(
        summary = "➕ Admin: Tạo phòng chiếu mới",
        description = "Admin API để tạo phòng chiếu cho một rạp",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "✅ Tạo phòng thành công",
            content = @Content(schema = @Schema(implementation = RoomResponse.class))),
        @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "❌ Chưa đăng nhập"),
        @ApiResponse(responseCode = "404", description = "❌ Rạp không tồn tại")
    })
    public ResponseEntity<RoomResponse> createRoom(
        @PathVariable Long theaterId,
        @RequestBody CreateRoomRequest request
    ) {
        try {
            RoomResponse response = theaterService.createRoom(theaterId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sửa rạp chiếu")
    public ResponseEntity<TheaterResponse> updateTheater(
        @PathVariable Long id,
        @RequestBody CreateTheaterRequest request
    ) {
        try {
            return ResponseEntity.ok(theaterService.updateTheater(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa rạp chiếu")
    public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
        try {
            theaterService.deleteTheater(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ═══════════════════════════════════════════════
    // TASK 1.1: Room CRUD — Sửa/Xóa Phòng chiếu
    // ═══════════════════════════════════════════════

    @PutMapping("/{theaterId}/rooms/{roomId}")
    @Operation(
        summary = "✏️ Admin: Sửa phòng chiếu",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<?> updateRoom(
        @PathVariable Long theaterId,
        @PathVariable Long roomId,
        @RequestBody CreateRoomRequest request
    ) {
        try {
            RoomResponse response = theaterService.updateRoom(theaterId, roomId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{theaterId}/rooms/{roomId}")
    @Operation(
        summary = "🗑️ Admin: Xóa phòng chiếu",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<?> deleteRoom(
        @PathVariable Long theaterId,
        @PathVariable Long roomId
    ) {
        try {
            theaterService.deleteRoom(theaterId, roomId);
            return ResponseEntity.ok(Map.of("message", "Xóa phòng chiếu thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
