package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.CreateMovieRequest;
import com.ticketrush.backend.dto.MovieResponse;
import com.ticketrush.backend.entity.Movie;
import com.ticketrush.backend.exception.ResourceNotFoundException;
import com.ticketrush.backend.repository.MovieRepository;
import com.ticketrush.backend.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Triển khai của MovieService interface
 * 
 * Chức năng:
 * - Lấy danh sách phim đang chiếu
 * - Lấy chi tiết phim theo ID
 * - Chuyển đổi Movie Entity thành MovieResponse DTO
 * - Admin tạo phim mới (VỀ LỖ HỔNG 1)
 * 
 * Đặc điểm:
 * - @Service: Spring bean, được quản lý bởi Spring container
 * - @RequiredArgsConstructor: Tự động tạo constructor với các final fields (dependency injection)
 * - @Transactional(readOnly = true): Tất cả method đều là read-only (không modify database)
 * - Gọi Repository để lấy dữ liệu từ database
 * - Map từ Entity sang DTO trước khi trả về (KHÔNG bao giờ return raw Entity)
 * 
 * @author Backend Team
 * @version 1.0
 * @since NGÀY 5-6 (2026-04-17)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieServiceImpl implements MovieService {

    /**
     * Repository dùng để thao tác với bảng movies trong database
     * Được inject tự động bởi Spring thông qua constructor
     */
    private final MovieRepository movieRepository;

    /**
     * Lấy danh sách phim với phân trang
     * 
     * ⚠️ Bắt buộc phân trang để tránh OutOfMemory khi có hàng ngàn bộ phim
     */
    @Override
    public Page<MovieResponse> getNowShowingMovies(Pageable pageable) {
        log.info("📋 Lấy danh sách phim - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        // Lấy page các Movie entities, sau đó map sang DTO
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        
        // Map Page<Movie> sang Page<MovieResponse>
        List<MovieResponse> responses = moviePage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();
        
        return new PageImpl<>(responses, pageable, moviePage.getTotalElements());
    }

    /**
     * Lấy chi tiết một phim theo ID
     * 
     * Quy trình:
     * 1. Gọi repository.findById(id) lấy Optional<Movie>
     * 2. Nếu tồn tại: map sang MovieResponse bằng toResponse()
     * 3. Nếu không tồn tại: throw ResourceNotFoundException
     * 
     * @param id ID của phim cần lấy
     * @return MovieResponse chứa thông tin phim
     * @throws ResourceNotFoundException nếu không tìm thấy phim
     */
    @Override
    public MovieResponse getMovieById(Long id) {
        return movieRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    /**
     * VỀ LỖ HỔNG 1: Admin API - Tạo phim mới
     * 
     * Quy trình:
     * 1. Validate request (title không được trống)
     * 2. Tạo Movie entity từ request
     * 3. Lưu vào database
     * 4. Return MovieResponse
     * 
     * @param request CreateMovieRequest chứa title, description, releaseYear, genre, posterImageUrl
     * @return MovieResponse thông tin phim vừa tạo
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    @Override
    @Transactional  // Override readOnly = true để có thể lưu database
    public MovieResponse createMovie(CreateMovieRequest request) {
        try {
            log.info("🎬 Admin tạo phim mới: {}", request.getTitle());

            // Validate
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("❌ Tên phim không được để trống");
            }

            // Tạo Movie entity
            Movie movie = new Movie();
            movie.setTitle(request.getTitle());
            movie.setDescription(request.getDescription());
            movie.setReleaseYear(request.getReleaseYear());
            movie.setGenre(request.getGenre());
            movie.setPosterImageUrl(request.getPosterImageUrl());

            // Lưu vào database
            Movie savedMovie = movieRepository.save(movie);
            log.info("✅ Tạo phim thành công: ID = {}", savedMovie.getId());

            // Return DTO
            return toResponse(savedMovie);

        } catch (Exception e) {
            log.error("❌ Lỗi tạo phim: {}", e.getMessage(), e);
            throw new IllegalArgumentException("❌ Lỗi tạo phim: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi Movie Entity sang MovieResponse DTO
     * 
     * Mục đích:
     * - Lựa chọn những field cần thiết
     * - Ẩn đi những field không cần (internal IDs, metadata, etc.)
     * - Kiểm soát dữ liệu được gửi đến client
     * 
     * @param movie Movie entity từ database
     * @return MovieResponse DTO để gửi đến client
     */
    private MovieResponse toResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getReleaseYear(),
                movie.getGenre(),
                movie.getPosterImageUrl()
        );
    }
}
