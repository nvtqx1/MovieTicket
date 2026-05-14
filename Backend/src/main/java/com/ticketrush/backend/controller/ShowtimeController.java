package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.response.SeatResponse;
import com.ticketrush.backend.dto.response.ShowtimeResponse;
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
 * Controller tra cứu suất chiếu và ghế theo suất chiếu.
 *
 * Endpoint tìm kiếm dùng phân trang để giới hạn dữ liệu trả về.
 */
@RestController
@RequestMapping("/v1/showtimes")
@RequiredArgsConstructor
@Tag(name = "Showtime Management", description = "API quản lý suất chiếu")
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final SeatService seatService;

    /**
     * Tìm kiếm suất chiếu theo phim, rạp và ngày.
     *
     * Tham số {@code date} được ưu tiên hơn {@code showDate} để tương thích với
     * frontend đang gửi tên query khác nhau.
     *
     * @param movieId ID phim cần lọc, có thể null.
     * @param theaterId ID rạp cần lọc, có thể null.
     * @param showDate ngày chiếu cần lọc, có thể null.
     * @param date ngày chiếu alias, có thể null.
     * @param page số trang, bắt đầu từ 0.
     * @param size số bản ghi mỗi trang.
     * @return trang danh sách suất chiếu phù hợp.
     */
    @GetMapping
    @Operation(summary = "Tìm kiếm suất chiếu phân trang")
    @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công")
    public ResponseEntity<Page<ShowtimeResponse>> searchShowtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate,
            @RequestParam(required = false, name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDate filterDate = date != null ? date : showDate;
        return ResponseEntity.ok(showtimeService.searchShowtimes(movieId, theaterId, filterDate, pageable));
    }

    /**
     * Lấy chi tiết một suất chiếu.
     *
     * @param id ID suất chiếu cần lấy.
     * @return thông tin chi tiết suất chiếu.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết suất chiếu")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "404", description = "Suất chiếu không tồn tại")
    })
    public ResponseEntity<ShowtimeResponse> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    /**
     * Lấy danh sách ghế của một suất chiếu.
     *
     * @param id ID suất chiếu cần lấy ghế.
     * @return danh sách ghế kèm loại, trạng thái và giá.
     */
    @GetMapping("/{id}/seats")
    @Operation(summary = "Lấy danh sách ghế của suất chiếu")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy thành công",
            content = @Content(schema = @Schema(implementation = SeatResponse.class))),
        @ApiResponse(responseCode = "404", description = "Suất chiếu không tồn tại")
    })
    public ResponseEntity<List<SeatResponse>> getSeatsByShowtime(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatsByShowtime(id));
    }
}
