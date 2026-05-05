package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.ReviewRequest;
import com.ticketrush.backend.dto.ReviewResponse;
import com.ticketrush.backend.entity.Movie;
import com.ticketrush.backend.entity.Review;
import com.ticketrush.backend.entity.User;
import com.ticketrush.backend.entity.enums.ReviewStatus;
import com.ticketrush.backend.repository.MovieRepository;
import com.ticketrush.backend.repository.ReviewRepository;
import com.ticketrush.backend.repository.UserRepository;
import com.ticketrush.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Phim không tồn tại"));

        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setStatus(ReviewStatus.APPROVED); // Default auto-approve
        
        Review savedReview = reviewRepository.save(review);
        log.info("✅ User {} đã đánh giá phim {} với rating {}", userId, movie.getId(), request.getRating());

        return mapToResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getReviewsByMovie(Long movieId) {
        List<Review> reviews = reviewRepository.findByMovieIdAndStatus(movieId, ReviewStatus.APPROVED);
        return reviews.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại"));
        return mapToResponse(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại"));

        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.delete(review);
        log.info("🗑️ Đã xóa đánh giá ID {} của user {}", id, userId);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getUserName())
                .movieId(review.getMovie().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .upvotes(review.getUpvotes())
                .downvotes(review.getDownvotes())
                .status(review.getStatus().name())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
