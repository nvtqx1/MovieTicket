package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueJoinResponse {
    private Long showtimeId;
    private Long userId;
    private Long queuePosition;
    private String status;
    private String message;
}
