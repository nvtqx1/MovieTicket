package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO request dùng để tạo đơn đặt vé mới.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {

    private Long showtimeId;

    private List<String> seatNumbers;

    private String voucherCode;
}

