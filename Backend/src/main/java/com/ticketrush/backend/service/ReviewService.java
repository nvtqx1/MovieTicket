package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.ReviewRequest;
import com.ticketrush.backend.dto.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(Long userId, ReviewRequest request);
    List<ReviewResponse> getReviewsByMovie(Long movieId);
    ReviewResponse getReviewById(Long id);
    void deleteReview(Long id, Long userId);
}
