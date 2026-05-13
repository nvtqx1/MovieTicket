package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.request.CreateMovieRequest;
import com.ticketrush.backend.dto.response.MovieResponse;
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
 * Controller xử lý API phim.
 *
 * Annotation {@link Tag} nhóm tài liệu Swagger; các endpoint danh sách dùng
 * phân trang để tránh trả dữ liệu quá lớn.
 */
@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Movie Management", description = "API quản lý phim")
public class MovieController {

    private final MovieService movieService;

    /**
     * Lấy danh sách phim đang chiếu theo phân trang.
     *
     * @param page số trang, bắt đầu từ 0.
     * @param size số phim trên mỗi trang.
     * @return trang dữ liệu phim đang chiếu.
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách phim phân trang")
    @ApiResponse(responseCode = "200", description = "Lấy thành công")
    public ResponseEntity<Page<MovieResponse>> getNowShowingMovies(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(movieService.getNowShowingMovies(pageable));
    }

    /**
     * Tìm phim theo từ khóa tên phim.
     *
     * @param keyword từ khóa tìm kiếm.
     * @return danh sách phim khớp từ khóa.
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm phim theo tên")
    public ResponseEntity<List<MovieResponse>> searchMovies(@RequestParam String keyword) {
        return ResponseEntity.ok(movieService.searchMovies(keyword));
    }

    /**
     * Lấy thông tin chi tiết của một phim.
     *
     * @param id ID phim cần lấy.
     * @return thông tin phim.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết phim")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy phim")
    })
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    /**
     * Lấy chi tiết phim kèm rạp và lịch chiếu.
     *
     * @param id ID phim cần lấy chi tiết.
     * @return chi tiết phim và các rạp đang chiếu.
     */
    @GetMapping("/{id}/details")
    @Operation(summary = "Lấy chi tiết phim và lịch chiếu")
    public ResponseEntity<com.ticketrush.backend.dto.response.MovieDetailsResponse> getMovieDetailsWithTheaters(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieDetailsWithTheaters(id));
    }

    /**
     * Tạo phim mới.
     *
     * @param request dữ liệu phim cần tạo.
     * @return phim vừa được tạo.
     */
    @PostMapping
    @Operation(
        summary = "Admin tạo phim mới",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo phim thành công",
            content = @Content(schema = @Schema(implementation = MovieResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền admin")
    })
    public ResponseEntity<MovieResponse> createMovie(@RequestBody CreateMovieRequest request) {
        try {
            MovieResponse response = movieService.createMovie(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cập nhật thông tin phim.
     *
     * @param id ID phim cần cập nhật.
     * @param request dữ liệu phim mới.
     * @return thông tin phim sau cập nhật.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Admin cập nhật phim", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @RequestBody CreateMovieRequest request) {
        try {
            MovieResponse response = movieService.updateMovie(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Xóa mềm một phim.
     *
     * @param id ID phim cần xóa.
     * @return phản hồi không có nội dung khi xóa thành công.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Admin xóa phim", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
