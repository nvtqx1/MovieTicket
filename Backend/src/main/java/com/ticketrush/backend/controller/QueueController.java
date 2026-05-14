package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.QueueJoinRequest;
import com.ticketrush.backend.dto.response.QueueJoinResponse;
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
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller xử lý hàng chờ ảo khi lưu lượng tăng cao.
 *
 * Request hợp lệ được đưa vào Kafka thông qua {@link QueueProducerService}.
 */
@RestController
@RequestMapping("/v1/queue")
@RequiredArgsConstructor
@Tag(name = "Virtual Queue", description = "Hàng chờ ảo khi traffic đột biến")
public class QueueController {

    private final QueueProducerService queueProducerService;

    /**
     * Đưa người dùng hiện tại vào hàng chờ của suất chiếu.
     *
     * Annotation {@link Valid} validate request trước khi gửi vào hàng chờ.
     *
     * @param authentication thông tin xác thực của người dùng.
     * @param request dữ liệu gồm ID suất chiếu.
     * @return vị trí hiện tại của người dùng trong hàng chờ.
     */
    @PostMapping("/join")
    public ResponseEntity<QueueJoinResponse> joinQueue(
            Authentication authentication,
            @Valid @RequestBody QueueJoinRequest request
    ) {
        Long userId = extractUserId(authentication);
        Long showtimeId = request.getShowtimeId();

        Long queuePosition = queueProducerService.joinQueue(userId, showtimeId);

        return ResponseEntity.ok(QueueJoinResponse.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .queuePosition(queuePosition)
                .status("WAITING")
                .message("Successfully joined the queue. Please wait for your turn.")
                .build());
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
}
