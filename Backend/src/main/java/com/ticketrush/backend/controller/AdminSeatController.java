package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.GenerateSeatRequest;
import com.ticketrush.backend.dto.response.GenerateSeatResponse;
import com.ticketrush.backend.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản trị ghế của suất chiếu.
 *
 * Annotation {@link RestController} khai báo API REST, {@link RequestMapping}
 * đặt prefix endpoint và {@link RequiredArgsConstructor} inject service qua constructor.
 */
@RestController
@RequestMapping("/v1/admin/seats")
@RequiredArgsConstructor
public class AdminSeatController {

    private final SeatService seatService;

    /**
     * Sinh ma trận ghế cho một suất chiếu.
     *
     * Annotation {@link Valid} yêu cầu Spring validate dữ liệu request trước khi
     * gọi service.
     *
     * @param request dữ liệu gồm suất chiếu, số hàng và số cột.
     * @return phản hồi chứa số ghế đã tạo và thông tin ma trận.
     */
    @PostMapping("/matrix/generate")
    public ResponseEntity<GenerateSeatResponse> generateSeatMatrix(
            @Valid @RequestBody GenerateSeatRequest request) {
        GenerateSeatResponse response = seatService.generateSeatMatrix(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa toàn bộ ghế thuộc một suất chiếu.
     *
     * @param showtimeId ID suất chiếu cần xóa ghế.
     * @return phản hồi rỗng khi thành công hoặc thông báo lỗi khi thất bại.
     */
    @DeleteMapping("/showtime/{showtimeId}")
    public ResponseEntity<?> deleteSeatsByShowtime(@PathVariable Long showtimeId) {
        try {
            seatService.deleteSeatsByShowtime(showtimeId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
