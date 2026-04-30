package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.MovieResponse;
import com.ticketrush.backend.entity.Movie;
import com.ticketrush.backend.exception.ResourceNotFoundException;
import com.ticketrush.backend.repository.MovieRepository;
import com.ticketrush.backend.service.MovieService;
import lombok.RequiredArgsConstructor;
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
     * Lấy danh sách tất cả phim đang chiếu, sắp xếp theo năm phát hành mới nhất
     * 
     * Quy trình:
     * 1. Gọi repository.findAllByOrderByReleaseYearDesc() lấy tất cả Movie entity
     * 2. Convert stream của Movie entities
     * 3. Map mỗi Movie sang MovieResponse bằng toResponse()
     * 4. Collect vào List
     * 
     * @return List<MovieResponse> danh sách phim sắp xếp theo năm giảm dần
     */
    @Override
    public List<MovieResponse> getNowShowingMovies() {
        return movieRepository.findAllByOrderByReleaseYearDesc()
                .stream()
                .map(this::toResponse)
                .toList();
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
