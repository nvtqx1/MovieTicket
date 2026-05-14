package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response trả về khi tham gia hàng chờ.
 */
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
