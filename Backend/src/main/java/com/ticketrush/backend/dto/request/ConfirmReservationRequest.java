package com.ticketrush.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO request dùng để chốt đơn đặt vé.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmReservationRequest {

    private Long reservationId;

    private Long paymentMethodId;

    private String transactionCode;

    private String provider;

    private String voucherCode;

    private List<String> seatNumbers;

    private String notes;
}

