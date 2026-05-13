package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.request.ReviewRequest;
import com.ticketrush.backend.dto.response.ReviewResponse;
import java.util.List;

/**
 * Dịch vụ quản lý đánh giá phim của người dùng.
 */
public interface ReviewService {
    /**
     * Tạo đánh giá mới cho một bộ phim.
     *
     * @param userId ID người dùng tạo đánh giá.
     * @param request nội dung đánh giá.
     * @return đánh giá vừa được tạo.
     * @throws IllegalArgumentException nếu user hoặc phim không tồn tại.
     */
    ReviewResponse createReview(Long userId, ReviewRequest request);

    /**
     * Lấy danh sách đánh giá đã duyệt của một phim.
     *
     * @param movieId ID phim cần lấy đánh giá.
     * @return danh sách đánh giá của phim.
     */
    List<ReviewResponse> getReviewsByMovie(Long movieId);

    /**
     * Lấy chi tiết một đánh giá theo ID.
     *
     * @param id ID đánh giá.
     * @return thông tin đánh giá.
     * @throws IllegalArgumentException nếu đánh giá không tồn tại.
     */
    ReviewResponse getReviewById(Long id);

    /**
     * Xóa đánh giá của chính người dùng đã tạo.
     *
     * @param id ID đánh giá cần xóa.
     * @param userId ID người dùng yêu cầu xóa.
     * @throws IllegalArgumentException nếu đánh giá không tồn tại hoặc user không có quyền.
     */
    void deleteReview(Long id, Long userId);
}
