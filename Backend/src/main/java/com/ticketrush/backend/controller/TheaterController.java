package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.CreateRoomRequest;
import com.ticketrush.backend.dto.CreateTheaterRequest;
import com.ticketrush.backend.dto.RoomResponse;
import com.ticketrush.backend.dto.TheaterResponse;
import com.ticketrush.backend.service.TheaterService;
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

import java.util.List;

/**
 * REST Controller quản lý Rạp chiếu và Phòng chiếu
 * 
 * Endpoints:
 * - GET /v1/theaters: Lấy danh sách rạp
 * - POST /v1/admin/theaters: Admin tạo rạp mới (VỀ LỖ HỔNG 1)
 * - POST /v1/admin/theaters/{theaterId}/rooms: Admin tạo phòng (VỀ LỖ HỔNG 1)
 * - GET /v1/theaters/{theaterId}/rooms: Lấy danh sách phòng của rạp
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@RestController
@RequestMapping("/v1/theaters")
@RequiredArgsConstructor
@Tag(name = "🎬 Theater Management", description = "API quản lý rạp chiếu")
public class TheaterController {

    private final TheaterService theaterService;

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
     * VỀ LỖ HỔNG 1: Admin API - Tạo rạp chiếu mới
     * 
     * POST /v1/admin/theaters
     * 
     * Mục tiêu: Cho phép Admin nhập liệu rạp mà không phải chọc vào Database
     * 
     * @param request CreateTheaterRequest chứa name, location, capacity
     * @return TheaterResponse thông tin rạp vừa tạo
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
     * 
     * @param theaterId ID của rạp
     * @return Danh sách phòng thuộc rạp
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
     * VỀ LỖ HỔNG 1: Admin API - Tạo phòng chiếu cho rạp
     * 
     * POST /v1/admin/theaters/{theaterId}/rooms
     * 
     * Mục tiêu: Cho phép Admin tạo phòng chiếu mà không phải chọc vào Database
     * 
     * @param theaterId ID của rạp sở hữu phòng này
     * @param request CreateRoomRequest chứa name, capacity
     * @return RoomResponse thông tin phòng vừa tạo
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
}
