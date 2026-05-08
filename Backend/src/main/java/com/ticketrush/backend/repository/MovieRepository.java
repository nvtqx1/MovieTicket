package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Trả về danh sách phim, xếp theo năm phát hành mới nhất lên đầu (bỏ qua phim đã xóa)
    List<Movie> findAllByIsDeletedFalseOrderByReleaseYearDesc();

    // Lấy danh sách phim phân trang (bỏ qua phim đã xóa)
    Page<Movie> findByIsDeletedFalse(Pageable pageable);

    // Tính năng thanh Tìm kiếm (Search bar) trên Frontend: Tìm phim theo tên, không phân biệt hoa thường
    List<Movie> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String keyword);

    // Tính năng Lọc: Tìm phim theo thể loại (VD: "Hành động")
    List<Movie> findByGenreContainingIgnoreCaseAndIsDeletedFalse(String genre);

    // Lấy phim theo ID không bị xóa
    Optional<Movie> findByIdAndIsDeletedFalse(Long id);

    // Tìm theo đúng title
    Optional<Movie> findByTitle(String title);
}