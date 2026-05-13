package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO response chứa thông tin vé của người dùng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private Long reservationId;

    private Long showtimeId;

    private String movieName;

    private String theaterName;

    private String roomName;

    private String location;

    private List<String> seatNumbers;

    private LocalDateTime showtimeStartTime;

    private BigDecimal totalPrice;

    private String status;

    private LocalDateTime reservationTime;

    private LocalDateTime expiresAt;

    private String qrCodeHash;
}

