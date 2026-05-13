package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu phim.
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Lấy danh sách phim chưa xóa, sắp xếp theo năm phát hành mới nhất.
     *
     * @return danh sách phim chưa bị xóa mềm.
     */
    List<Movie> findAllByIsDeletedFalseOrderByReleaseYearDesc();

    /**
     * Lấy danh sách phim chưa xóa theo phân trang.
     *
     * @param pageable thông tin phân trang.
     * @return trang dữ liệu phim chưa bị xóa mềm.
     */
    Page<Movie> findByIsDeletedFalse(Pageable pageable);

    /**
     * Tìm phim chưa xóa theo tiêu đề, không phân biệt hoa thường.
     *
     * @param keyword từ khóa tiêu đề phim.
     * @return danh sách phim khớp từ khóa.
     */
    List<Movie> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String keyword);

    /**
     * Tìm phim chưa xóa theo thể loại, không phân biệt hoa thường.
     *
     * @param genre từ khóa thể loại phim.
     * @return danh sách phim thuộc thể loại phù hợp.
     */
    List<Movie> findByGenreContainingIgnoreCaseAndIsDeletedFalse(String genre);

    /**
     * Tìm phim chưa xóa theo ID.
     *
     * @param id ID phim.
     * @return phim nếu tồn tại và chưa bị xóa mềm.
     */
    Optional<Movie> findByIdAndIsDeletedFalse(Long id);

    /**
     * Tìm phim theo tiêu đề chính xác.
     *
     * @param title tiêu đề phim.
     * @return phim nếu tồn tại.
     */
    Optional<Movie> findByTitle(String title);
}
