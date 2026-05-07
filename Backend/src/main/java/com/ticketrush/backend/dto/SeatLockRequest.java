package com.ticketrush.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockRequest {

    @NotEmpty(message = "seatIds is required and cannot be empty")
    @Size(max = 10, message = "Cannot lock more than 10 seats at once")
    private List<Long> seatIds;
}
