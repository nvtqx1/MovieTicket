package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.MovieResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface để quản lý các thao tác liên quan đến Phim
 * 
 * Đây là layer trung gian giữa Controller (HTTP requests) và Repository (database)
 * 
 * Các chức năng:
 * - Lấy danh sách phim đang chiếu
 * - Lấy chi tiết phim theo ID
 * 
 * Thiết kế Pattern:
 * - Interface-based design (dễ testing, dễ mock)
 * - Implementation nằm trong service/impl/MovieServiceImpl.java
 * - Chuyển đổi Entity thành Response DTO trước khi trả về
 * 
 * @author Backend Team
 * @version 1.0
 * @since NGÀY 5-6 (2026-04-17)
 */
public interface MovieService {

    /**
     * Lấy danh sách phim với phân trang
     * 
     * ⚠️ Bắt buộc phân trang để tránh OutOfMemory khi có hàng ngàn bộ phim
     * 
     * @param pageable Pageable object (page, size, sort)
     * @return Page<MovieResponse> danh sách phim với thông tin phân trang
     */
    Page<MovieResponse> getNowShowingMovies(Pageable pageable);

    /**
     * Lấy chi tiết một phim theo ID
     * 
     * @param id ID của phim cần lấy
     * @return MovieResponse chứa thông tin phim
     * @throws RuntimeException nếu không tìm thấy phim
     */
    MovieResponse getMovieById(Long id);

    /**
     * Lấy dữ liệu chi tiết của một bộ phim và các thông tin liên quan đến rạp (theater)
     */
    com.ticketrush.backend.dto.MovieDetailsResponse getMovieDetailsWithTheaters(Long id);

    /**
     * Admin API: Tạo phim mới
     * 
     * POST /v1/admin/movies
     * 
     * @param request CreateMovieRequest chứa title, description, releaseYear, genre, posterImageUrl
     * @return MovieResponse thông tin phim vừa tạo
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    MovieResponse createMovie(com.ticketrush.backend.dto.CreateMovieRequest request);

    /**
     * Admin API: Cập nhật phim
     */
    MovieResponse updateMovie(Long id, com.ticketrush.backend.dto.CreateMovieRequest request);

    /**
     * Admin API: Xóa phim (Soft Delete)
     */
    void deleteMovie(Long id);

    /**
     * Task 2.1: Tìm kiếm phim theo tên
     */
    List<MovieResponse> searchMovies(String keyword);
}
