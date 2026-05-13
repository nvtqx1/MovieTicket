package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO cho API giữ ghế
 * POST /v1/booking/hold-seat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatRequest {
    private Long showtimeId;
    private List<String> seatNumbers;
}
