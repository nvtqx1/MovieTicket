package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO response chứa thông tin đơn đặt vé.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private Long reservationId;

    private Long showtimeId;

    private Long userId;

    private String movieName;

    private String theaterName;

    private String roomName;

    private List<String> seatNumbers;

    private LocalDateTime showtimeStartTime;

    private BigDecimal totalPrice;

    private String status;

    private String qrCodeBase64;

    private String qrCodeDataUri;

    private String qrCodeHash;

    private LocalDateTime confirmedAt;

    private LocalDateTime expiresAt;

    private String transactionCode;

    private String message;

    private String apiStatus;
}

