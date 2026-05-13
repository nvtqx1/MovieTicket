package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO response trả về sau khi tạo đơn đặt vé.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationResponse {

    private Long reservationId;

    private Long showtimeId;

    private String movieName;

    private String theaterName;

    private String roomName;

    private List<String> seatNumbers;

    private LocalDateTime showtimeStartTime;

    private BigDecimal totalPrice;

    private BigDecimal discountAmount;

    private BigDecimal finalPrice;

    private String voucherCode;

    private String status;

    private LocalDateTime expiresAt;

    private String message;

    private String apiStatus;
}

