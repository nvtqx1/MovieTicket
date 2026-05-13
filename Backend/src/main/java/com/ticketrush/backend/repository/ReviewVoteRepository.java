package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu vote của review.
 */
@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
}
