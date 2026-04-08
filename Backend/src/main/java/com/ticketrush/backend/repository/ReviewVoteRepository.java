package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
    // Không cần viết thêm hàm gì lúc này, các hàm CRUD mặc định là đủ dùng.
}