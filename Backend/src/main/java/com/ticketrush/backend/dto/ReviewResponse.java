package com.ticketrush.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

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
