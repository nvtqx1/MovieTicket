package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.SeatLockRequest;
import com.ticketrush.backend.dto.response.SeatLockResponse;
import com.ticketrush.backend.security.UserDetailsImpl;
import com.ticketrush.backend.service.SeatLockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller khóa ghế tạm thời cho người dùng.
 *
 * Endpoint khóa ghế theo nguyên tắc all-or-nothing: nếu một ghế lỗi thì toàn bộ
 * yêu cầu khóa bị từ chối.
 */
@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatLockController {

    private final SeatLockService seatLockService;

    /**
     * Khóa nhiều ghế cùng lúc cho người dùng hiện tại.
     *
     * Annotation {@link Valid} validate danh sách ghế trước khi xử lý.
     *
     * @param authentication thông tin xác thực của người dùng.
     * @param request danh sách ID ghế cần khóa.
     * @return kết quả khóa ghế hoặc lỗi 409 khi có xung đột.
     */
    @PostMapping("/lock")
    public ResponseEntity<SeatLockResponse> lockSeats(
            Authentication authentication,
            @Valid @RequestBody SeatLockRequest request
            ) {
        Long userId = extractUserId(authentication);

        try {
            return ResponseEntity.ok(seatLockService.lockSeats(request.getSeatIds(), userId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(failedResponse(request.getSeatIds(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(failedResponse(request.getSeatIds(), e.getMessage()));
        }
    }

    /**
     * Trích xuất ID người dùng từ Authentication.
     *
     * @param authentication thông tin xác thực hiện tại.
     * @return ID người dùng.
     * @throws IllegalArgumentException khi chưa đăng nhập hoặc principal không hợp lệ.
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Unauthenticated request");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        if (principal instanceof String value) {
            return Long.parseLong(value);
        }

        throw new IllegalArgumentException("Cannot extract user ID from authentication principal");
    }

    /**
     * Tạo response lỗi cho danh sách ghế khóa thất bại.
     *
     * @param seatIds danh sách ID ghế trong request.
     * @param message thông báo lỗi.
     * @return response lỗi dạng SeatLockResponse.
     */
    private SeatLockResponse failedResponse(List<Long> seatIds, String message) {
        List<SeatLockResponse.LockedSeatInfo> seatInfos = seatIds.stream()
                .map(id -> SeatLockResponse.LockedSeatInfo.builder().seatId(id).build())
                .toList();

        return SeatLockResponse.builder()
                .lockedSeats(seatInfos)
                .status("FAILED")
                .message(message)
                .build();
    }
}
