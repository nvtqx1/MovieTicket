package com.ticketrush.backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO thống kê tổng quan người dùng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOverviewDTO {

    private Long userId;

    private String userName;

    private String email;

    private String phoneNumber;

    private String gender;

    private LocalDate dateOfBirth;

    private Long reservationCount;

    private Long totalSpent;

    private String role;
}

