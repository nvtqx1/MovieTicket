package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true, length = 30)
    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 6, max = 30, message = "Tên người dùng phải từ 6-30 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tên người dùng chỉ chứa chữ cái, số và dấu gạch dưới")
    private String userName;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email không đúng định dạng (ví dụ: user@example.com)")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải chứa ít nhất 8 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Mật khẩu phải chứa chữ hoa, chữ thường và số")
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[1-9]\\d{8,9}$", 
            message = "Số điện thoại không hợp lệ (ví dụ: 0912345678 hoặc +84912345678)")
    private String phoneNumber;

    @Column(name = "date_of_birth", nullable = false)
    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;

}