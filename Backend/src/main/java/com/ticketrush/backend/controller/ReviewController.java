package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.ReviewRequest;
import com.ticketrush.backend.dto.response.ReviewResponse;
import com.ticketrush.backend.security.UserDetailsImpl;
import com.ticketrush.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "⭐ Review Management", description = "API quản lý đánh giá phim")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "📝 Thêm đánh giá phim", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<?> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        try {
            Long userId = extractUserId(authentication);
            ReviewResponse response = reviewService.createReview(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "apiStatus", "SUCCESS",
                    "message", "✅ Thêm đánh giá thành công",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "❌ " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Lỗi hệ thống: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "❌ Lỗi hệ thống"
            ));
        }
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "📋 Lấy danh sách đánh giá của 1 phim")
    public ResponseEntity<?> getReviewsByMovie(@PathVariable Long movieId) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByMovie(movieId);
            return ResponseEntity.ok(Map.of(
                    "apiStatus", "SUCCESS",
                    "data", reviews
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "❌ Lỗi hệ thống"
            ));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "🗑️ Xóa đánh giá", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<?> deleteReview(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = extractUserId(authentication);
            reviewService.deleteReview(id, userId);
            return ResponseEntity.ok(Map.of(
                    "apiStatus", "SUCCESS",
                    "message", "✅ Đã xóa đánh giá"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "❌ " + e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "apiStatus", "FAILED",
                    "message", "❌ Lỗi hệ thống"
            ));
        }
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Chưa đăng nhập");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        }
        throw new IllegalArgumentException("Không thể trích xuất user ID");
    }
}
