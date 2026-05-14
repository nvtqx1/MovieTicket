package com.ticketrush.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request dùng để tham gia hàng chờ ảo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueJoinRequest {
    @NotNull(message = "showtimeId is required")
    private Long showtimeId;
}
