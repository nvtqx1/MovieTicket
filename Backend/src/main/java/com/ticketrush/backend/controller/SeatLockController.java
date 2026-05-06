package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.SeatLockRequest;
import com.ticketrush.backend.dto.SeatLockResponse;
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

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatLockController {

    private final SeatLockService seatLockService;

    @PostMapping("/lock")
    public ResponseEntity<SeatLockResponse> lockSeat(
            Authentication authentication,
            @Valid @RequestBody SeatLockRequest request
            ) {
        Long userId = extractUserId(authentication);

        try {
            return ResponseEntity.ok(seatLockService.lockSeat(request.getSeatId(), userId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(failedResponse(request.getSeatId(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(failedResponse(request.getSeatId(), e.getMessage()));
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

    private SeatLockResponse failedResponse(Long seatId, String message) {
        return SeatLockResponse.builder()
                .seatId(seatId)
                .status("FAILED")
                .message(message)
                .build();
    }
}
