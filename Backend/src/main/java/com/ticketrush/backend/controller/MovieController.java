package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.CreateMovieRequest;
import com.ticketrush.backend.dto.MovieResponse;
import com.ticketrush.backend.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller để xử lý HTTP requests liên quan đến Phim
 * 
 * Endpoint base: /api/v1/movies
 * 
 * Chức năng:
 * - GET /api/v1/movies: Lấy danh sách phim đang chiếu
 * - GET /api/v1/movies/{id}: Lấy chi tiết phim theo ID
 * - POST /api/v1/admin/movies: Admin tạo phim mới (VỀ LỖ HỔNG 1)
 * 
 * Design Pattern:
 * - @RestController: Annotation chỉ ra đây là REST controller
 * - @RequestMapping("/v1/movies"): Base path cho tất cả endpoint
 * - @RequiredArgsConstructor: Tự động tạo constructor với dependency injection
 * - ResponseEntity<T>: Bao bọc response data + HTTP status code
 * 
 * Tầng HTTP:
 * - Nhận HTTP requests từ client
 * - Gọi service layer để xử lý
 * - Trả về HTTP response (JSON + status code)
 * 
 * @author Backend Team
 * @version 1.0
 * @since NGÀY 5-6 (2026-04-17)
 */
@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
@Tag(name = "🎬 Movie Management", description = "API quản lý phim")
public class MovieController {

    /**
     * Service dùng để xử lý business logic liên quan đến phim
     * Được inject tự động bởi Spring thông qua constructor
     */
    private final MovieService movieService;

    /**
     * GET /api/v1/movies?page=0&size=10
     * Lấy danh sách phim với phân trang (Pagination)
     * 
     * ⚠️ QUAN TRỌNG: Sử dụng pagination để tránh OutOfMemory
     * khi có hàng ngàn bộ phim trong database
     * 
     * Query Parameters:
     * - page: Trang (bắt đầu từ 0), default = 0
     * - size: Số bộ phim per page, default = 10
     * 
     * Response:
     * HTTP 200 OK
     * {
     *   "content": [
     *     {
     *       "id": 1,
     *       "title": "Avatar",
     *       "description": "...",
     *       "releaseYear": 2009,
     *       "genre": "Science Fiction",
     *       "posterImageUrl": "https://..."
     *     },
     *     ...
     *   ],
     *   "pageable": {
     *     "pageNumber": 0,
     *     "pageSize": 10
     *   },
     *   "totalElements": 5000,
     *   "totalPages": 500,
     *   "first": true,
     *   "last": false
     * }
     * 
     * @param page Số trang (bắt đầu từ 0), default = 0
     * @param size Số bộ phim per page, default = 10
     * @return ResponseEntity chứa Page<MovieResponse> với phân trang
     */
    @GetMapping
    @Operation(summary = "📋 Lấy danh sách phim (Phân trang)", 
        description = "Lấy phim với phân trang. " +
            "⚠️ Bắt buộc phân trang để tránh OutOfMemory khi có hàng ngàn bộ phim")
    @ApiResponse(responseCode = "200", description = "✅ Lấy thành công")
    public ResponseEntity<Page<MovieResponse>> getNowShowingMovies(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(movieService.getNowShowingMovies(pageable));
    }

    /**
     * Task 2.1: API Search phim theo tên
     * GET /api/v1/movies/search?keyword=avatar
     */
    @GetMapping("/search")
    @Operation(summary = "🔍 Tìm kiếm phim theo tên")
    public ResponseEntity<List<MovieResponse>> searchMovies(@RequestParam String keyword) {
        return ResponseEntity.ok(movieService.searchMovies(keyword));
    }

    /**
     * GET /api/v1/movies/{id}
     * Lấy chi tiết một phim theo ID
     * 
     * Path Parameter:
     * - id: ID của phim cần lấy (ví dụ: 1, 2, 3)
     * 
     * Response (200 OK):
     * {
     *   "id": 1,
     *   "title": "Avatar",
     *   "description": "A paraplegic Marine dispatched to the moon Pandora...",
     *   "releaseYear": 2009,
     *   "genre": "Science Fiction",
     *   "posterImageUrl": "https://..."
     * }
     * 
     * Error Response (404 Not Found):
     * Nếu ID không tồn tại, trả về 404 từ service layer
     * 
     * @param id ID của phim cần lấy
     * @return ResponseEntity chứa MovieResponse
     */
    @GetMapping("/{id}")
    @Operation(summary = "🎬 Lấy chi tiết phim", description = "Lấy thông tin chi tiết của một phim theo ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "✅ Lấy thành công"),
        @ApiResponse(responseCode = "404", description = "❌ Không tìm thấy phim")
    })
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "🎬 Lấy chi tiết phim và lịch chiếu", description = "Lấy dữ liệu chi tiết của phim và các rạp đang chiếu")
    public ResponseEntity<com.ticketrush.backend.dto.MovieDetailsResponse> getMovieDetailsWithTheaters(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieDetailsWithTheaters(id));
    }

    /**
     * VỀ LỖ HỔNG 1: Admin API - Tạo phim mới
     * 
     * POST /api/v1/admin/movies
     * 
     * Mục tiêu: Cho phép Admin nhập liệu hệ thống cơ bản mà không phải chọc vào Database
     * 
     * @param request CreateMovieRequest chứa title, description, releaseYear, genre, posterImageUrl
     * @return MovieResponse thông tin phim vừa tạo
     */
    @PostMapping
    @Operation(
        summary = "➕ Admin: Tạo phim mới",
        description = "Admin API để tạo phim mới. Lưu ý: yêu cầu quyền admin",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "✅ Tạo phim thành công",
            content = @Content(schema = @Schema(implementation = MovieResponse.class))),
        @ApiResponse(responseCode = "400", description = "❌ Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "❌ Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "❌ Không có quyền admin")
    })
    public ResponseEntity<MovieResponse> createMovie(@RequestBody CreateMovieRequest request) {
        try {
            MovieResponse response = movieService.createMovie(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "✏️ Admin: Cập nhật phim", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @RequestBody CreateMovieRequest request) {
        try {
            MovieResponse response = movieService.updateMovie(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "🗑️ Admin: Xóa phim (Soft Delete)", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
