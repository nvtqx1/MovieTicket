package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.MovieResponse;
import com.ticketrush.backend.service.MovieService;
import lombok.RequiredArgsConstructor;
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
public class MovieController {

    /**
     * Service dùng để xử lý business logic liên quan đến phim
     * Được inject tự động bởi Spring thông qua constructor
     */
    private final MovieService movieService;

    /**
     * GET /api/v1/movies
     * Lấy danh sách tất cả phim đang chiếu
     * 
     * Response:
     * HTTP 200 OK
     * [
     *   {
     *     "id": 1,
     *     "title": "Avatar",
     *     "description": "...",
     *     "releaseYear": 2009,
     *     "genre": "Science Fiction",
     *     "posterImageUrl": "https://..."
     *   },
     *   ...
     * ]
     * 
     * @return ResponseEntity chứa List<MovieResponse> danh sách phim
     */
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getNowShowingMovies() {
        return ResponseEntity.ok(movieService.getNowShowingMovies());
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
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }
}
