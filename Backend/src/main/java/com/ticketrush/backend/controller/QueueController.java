package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.QueueJoinRequest;
import com.ticketrush.backend.dto.QueueJoinResponse;
import com.ticketrush.backend.security.UserDetailsImpl;
import com.ticketrush.backend.service.QueueProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueProducerService queueProducerService;

    @PostMapping("/join")
    public ResponseEntity<QueueJoinResponse> joinQueue(
            Authentication authentication,
            @Valid @RequestBody QueueJoinRequest request
    ) {
        Long userId = extractUserId(authentication);
        Long showtimeId = request.getShowtimeId();

        // Đẩy yêu cầu vào hàng chờ Kafka
        Long queuePosition = queueProducerService.joinQueue(userId, showtimeId);

        return ResponseEntity.ok(QueueJoinResponse.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .queuePosition(queuePosition)
                .status("WAITING")
                .message("Successfully joined the queue. Please wait for your turn.")
                .build());
    }

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
}
