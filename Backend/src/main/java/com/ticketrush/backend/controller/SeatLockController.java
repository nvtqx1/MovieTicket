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

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatLockController {

    private final SeatLockService seatLockService;

    /**
     * Lock nhiều ghế cùng lúc (all-or-nothing).
     *
     * Request body:
     * {
     *   "seatIds": [1, 2, 3]
     * }
     *
     * - Nếu tất cả ghế OK → trả 200 với thông tin reservation
     * - Nếu bất kỳ ghế nào fail → rollback tất cả, trả 409 CONFLICT
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

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Unauthenticated request");
        }

        // principal là đối tượng UserDetailsImpl mà chúng ta đã tạo, chứa thông tin user đã đăng nhập
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        if (principal instanceof String value) {
            return Long.parseLong(value);
        }

        throw new IllegalArgumentException("Cannot extract user ID from authentication principal");
    }

    private SeatLockResponse failedResponse(List<Long> seatIds, String message) {
        // Trả về thông tin ghế trong error response
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
