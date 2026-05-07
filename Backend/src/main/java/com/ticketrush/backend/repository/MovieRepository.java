package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Trả về danh sách phim, xếp theo năm phát hành mới nhất lên đầu (bỏ qua phim đã xóa)
    List<Movie> findAllByIsDeletedFalseOrderByReleaseYearDesc();

    // Lấy danh sách phim phân trang (bỏ qua phim đã xóa)
    org.springframework.data.domain.Page<Movie> findByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);

    // Tính năng thanh Tìm kiếm (Search bar) trên Frontend: Tìm phim theo tên, không phân biệt hoa thường
    List<Movie> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String keyword);

    // Tính năng Lọc: Tìm phim theo thể loại (VD: "Hành động")
    List<Movie> findByGenreContainingIgnoreCaseAndIsDeletedFalse(String genre);

    // Lấy phim theo ID không bị xóa
    java.util.Optional<Movie> findByIdAndIsDeletedFalse(Long id);
}