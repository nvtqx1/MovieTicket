package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Kéo toàn bộ bình luận của 1 bộ phim ra
    List<Review> findByMovieId(Long movieId);
}