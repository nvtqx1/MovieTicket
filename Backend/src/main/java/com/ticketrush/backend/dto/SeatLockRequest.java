package com.ticketrush.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockRequest {

    @NotNull(message = "seatId is required")
    @Positive(message = "seatId must be greater than 0")
    private Long seatId;
}
