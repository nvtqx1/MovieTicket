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
 * Controller quản lý rạp chiếu và phòng chiếu.
 *
 * Lịch chiếu theo rạp được nhóm bằng {@link TreeMap} để giữ thứ tự ngày tăng dần.
 */
@RestController
@RequestMapping("/v1/theaters")
@RequiredArgsConstructor
@Tag(name = "Theater Management", description = "API quản lý rạp chiếu")
public class TheaterController {

    private final TheaterService theaterService;
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeServiceImpl showtimeServiceImpl;
    private final TheaterRepository theaterRepository;

    /**
     * Lấy danh sách tất cả rạp chiếu đang hoạt động.
     *
     * @return danh sách rạp chiếu.
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách rạp")
    @ApiResponse(responseCode = "200", description = "Lấy thành công")
    public ResponseEntity<List<TheaterResponse>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    /**
     * Lấy lịch chiếu của rạp trong 6 ngày tính từ hôm nay.
     *
     * @param id ID rạp cần lấy lịch.
     * @return thông tin rạp và lịch chiếu nhóm theo ngày.
     */
    @GetMapping("/{id}/schedule")
    @Operation(summary = "Lấy lịch chiếu của rạp")
    public ResponseEntity<?> getTheaterSchedule(@PathVariable Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rạp không tồn tại"));

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(5);

        var showtimes = showtimeRepository.findByTheaterAndDateRange(id, today, endDate);

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
     * Tạo rạp chiếu mới.
     *
     * @param request dữ liệu rạp cần tạo.
     * @return rạp vừa được tạo.
     */
    @PostMapping
    @Operation(
        summary = "Admin tạo rạp chiếu mới",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo rạp thành công",
            content = @Content(schema = @Schema(implementation = TheaterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "409", description = "Rạp đã tồn tại")
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
     * Lấy danh sách phòng chiếu của một rạp.
     *
     * @param theaterId ID rạp cần lấy phòng.
     * @return danh sách phòng chiếu của rạp.
     */
    @GetMapping("/{theaterId}/rooms")
    @Operation(summary = "Lấy danh sách phòng của rạp")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "404", description = "Rạp không tồn tại")
    })
    public ResponseEntity<List<RoomResponse>> getRoomsByTheater(@PathVariable Long theaterId) {
        return ResponseEntity.ok(theaterService.getRoomsByTheater(theaterId));
    }

    /**
     * Tạo phòng chiếu mới cho một rạp.
     *
     * @param theaterId ID rạp chứa phòng mới.
     * @param request dữ liệu phòng cần tạo.
     * @return phòng chiếu vừa tạo.
     */
    @PostMapping("/{theaterId}/rooms")
    @Operation(
        summary = "Admin tạo phòng chiếu mới",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo phòng thành công",
            content = @Content(schema = @Schema(implementation = RoomResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "404", description = "Rạp không tồn tại")
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

    /**
     * Cập nhật thông tin rạp chiếu.
     *
     * @param id ID rạp cần cập nhật.
     * @param request dữ liệu cập nhật.
     * @return thông tin rạp sau cập nhật.
     */
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

    /**
     * Xóa rạp chiếu.
     *
     * @param id ID rạp cần xóa.
     * @return phản hồi rỗng khi xóa thành công.
     */
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

    /**
     * Cập nhật phòng chiếu thuộc một rạp.
     *
     * @param theaterId ID rạp chứa phòng.
     * @param roomId ID phòng cần cập nhật.
     * @param request dữ liệu cập nhật phòng.
     * @return thông tin phòng sau cập nhật hoặc thông báo lỗi.
     */
    @PutMapping("/{theaterId}/rooms/{roomId}")
    @Operation(
        summary = "Admin sửa phòng chiếu",
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

    /**
     * Xóa phòng chiếu khỏi một rạp.
     *
     * @param theaterId ID rạp chứa phòng.
     * @param roomId ID phòng cần xóa.
     * @return thông báo xóa thành công hoặc lỗi nghiệp vụ.
     */
    @DeleteMapping("/{theaterId}/rooms/{roomId}")
    @Operation(
        summary = "Admin xóa phòng chiếu",
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
