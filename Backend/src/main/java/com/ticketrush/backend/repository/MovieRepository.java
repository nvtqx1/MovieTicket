package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Trả về danh sách phim, xếp theo năm phát hành mới nhất lên đầu
    List<Movie> findAllByOrderByReleaseYearDesc();

    // Tính năng thanh Tìm kiếm (Search bar) trên Frontend: Tìm phim theo tên, không phân biệt hoa thường
    List<Movie> findByTitleContainingIgnoreCase(String keyword);

    // Tính năng Lọc: Tìm phim theo thể loại (VD: "Hành động")
    List<Movie> findByGenreContainingIgnoreCase(String genre);

    Optional<Movie> findByTitle(String title);
}