package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.MovieResponse;

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
     * Lấy danh sách tất cả phim đang chiếu
     * 
     * Sắp xếp: Theo năm phát hành từ mới nhất đến cũ nhất
     * 
     * @return List<MovieResponse> danh sách phim
     */
    List<MovieResponse> getNowShowingMovies();

    /**
     * Lấy chi tiết một phim theo ID
     * 
     * @param id ID của phim cần lấy
     * @return MovieResponse chứa thông tin phim
     * @throws RuntimeException nếu không tìm thấy phim
     */
    MovieResponse getMovieById(Long id);
}
