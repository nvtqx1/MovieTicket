package com.ticketrush.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho tổng quan người dùng (User Overview).
 * Dùng để hiển thị thông tin chi tiết từng user trên Admin Dashboard.
 *
 * @author TicketRush Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOverviewDTO {

    /**
     * ID người dùng
     * Ví dụ: 123
     */
    private Long userId;

    /**
     * Tên người dùng
     * Ví dụ: "john_doe"
     */
    private String userName;

    /**
     * Email
     * Ví dụ: "john@example.com"
     */
    private String email;

    /**
     * Số điện thoại
     * Ví dụ: "0912345678"
     */
    private String phoneNumber;

    /**
     * Giới tính
     * Ví dụ: "Male"
     */
    private String gender;

    /**
     * Ngày sinh
     * Ví dụ: "1990-01-15"
     */
    private LocalDate dateOfBirth;

    /**
     * Số lượng đơn đặt vé
     * Ví dụ: 12
     */
    private Long reservationCount;

    /**
     * Tổng tiền đã chi tiêu (VND)
     * Ví dụ: 2500000
     */
    private Long totalSpent;

    /**
     * Vai trò (Role)
     * Ví dụ: "USER", "ADMIN"
     */
    private String role;
}

