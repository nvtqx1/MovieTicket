package com.ticketrush.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO response chứa thông tin đánh giá phim.
 */
@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long movieId;
    private Integer rating;
    private String comment;
    private Integer upvotes;
    private Integer downvotes;
    private String status;
    private LocalDateTime createdAt;
}
