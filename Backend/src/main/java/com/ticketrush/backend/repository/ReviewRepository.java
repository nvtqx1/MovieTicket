package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Review;
import com.ticketrush.backend.entity.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository thao tác dữ liệu đánh giá phim.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Lấy toàn bộ đánh giá của một phim.
     *
     * @param movieId ID phim cần lấy đánh giá.
     * @return danh sách đánh giá của phim.
     */
    List<Review> findByMovieId(Long movieId);

    /**
     * Lấy đánh giá của một phim theo trạng thái duyệt.
     *
     * @param movieId ID phim cần lấy đánh giá.
     * @param status trạng thái duyệt của đánh giá.
     * @return danh sách đánh giá phù hợp.
     */
    List<Review> findByMovieIdAndStatus(Long movieId, ReviewStatus status);
}
