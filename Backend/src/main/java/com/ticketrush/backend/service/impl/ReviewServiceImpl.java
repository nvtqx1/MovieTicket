package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.request.ReviewRequest;
import com.ticketrush.backend.dto.response.ReviewResponse;
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

/**
 * Triển khai nghiệp vụ tạo, đọc và xóa đánh giá phim.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    /**
     * Tạo đánh giá mới và tự động duyệt.
     * {@code @Transactional} đảm bảo lưu đánh giá rollback nếu validate user hoặc phim thất bại.
     *
     * @param userId ID người dùng tạo đánh giá.
     * @param request nội dung đánh giá.
     * @return đánh giá vừa được tạo.
     * @throws IllegalArgumentException nếu user hoặc phim không tồn tại.
     */
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

    /**
     * Lấy danh sách đánh giá đã duyệt theo phim.
     *
     * @param movieId ID phim cần lấy đánh giá.
     * @return danh sách đánh giá đã duyệt.
     */
    @Override
    public List<ReviewResponse> getReviewsByMovie(Long movieId) {
        List<Review> reviews = reviewRepository.findByMovieIdAndStatus(movieId, ReviewStatus.APPROVED);
        return reviews.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một đánh giá.
     *
     * @param id ID đánh giá.
     * @return thông tin đánh giá.
     * @throws IllegalArgumentException nếu đánh giá không tồn tại.
     */
    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại"));
        return mapToResponse(review);
    }

    /**
     * Xóa đánh giá nếu người yêu cầu là chủ đánh giá.
     * {@code @Transactional} đảm bảo thao tác xóa được commit hoặc rollback nguyên khối.
     *
     * @param id ID đánh giá cần xóa.
     * @param userId ID người dùng yêu cầu xóa.
     * @throws IllegalArgumentException nếu đánh giá không tồn tại hoặc user không có quyền.
     */
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

    /**
     * Chuyển entity Review sang DTO phản hồi.
     *
     * @param review entity đánh giá cần chuyển đổi.
     * @return DTO đánh giá.
     */
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
