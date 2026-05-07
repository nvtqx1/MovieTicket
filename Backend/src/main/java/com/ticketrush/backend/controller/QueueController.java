package com.ticketrush.backend.controller;

import com.ticketrush.backend.security.UserDetailsImpl;
import com.ticketrush.backend.service.VirtualQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Task 4.1: Virtual Queue Controller
 */
@RestController
@RequestMapping("/v1/queue")
@RequiredArgsConstructor
@Tag(name = "🚦 Virtual Queue", description = "Hàng chờ ảo khi traffic đột biến")
public class QueueController {

    private final VirtualQueueService queueService;

    @PostMapping("/join")
    @Operation(summary = "📋 Tham gia hàng chờ", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<VirtualQueueService.QueueStatus> joinQueue(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody Map<String, Long> body) {
        Long showtimeId = body.get("showtimeId");
        if (showtimeId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(queueService.joinQueue(showtimeId, currentUser.getId()));
    }

    @GetMapping("/status")
    @Operation(summary = "📊 Kiểm tra vị trí", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<VirtualQueueService.QueueStatus> getStatus(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam Long showtimeId) {
        return ResponseEntity.ok(queueService.getStatus(showtimeId, currentUser.getId()));
    }

    @GetMapping("/validate")
    @Operation(summary = "✅ Validate token", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        boolean valid = queueService.validateToken(token);
        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "message", valid ? "Token hợp lệ" : "Token không hợp lệ hoặc đã hết hạn"
        ));
    }
}
