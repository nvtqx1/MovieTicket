package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO request dùng để giữ ghế tạm thời.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatRequest {
    private Long showtimeId;
    private List<String> seatNumbers;
}
