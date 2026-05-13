package com.ticketrush.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SignupRequest {
    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 6, max = 30, message = "Tên người dùng phải từ 6-30 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tên người dùng chỉ chứa chữ cái, số và dấu gạch dưới")
    private String userName;

    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email không đúng định dạng (ví dụ: user@example.com)")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải chứa ít nhất 8 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Mật khẩu phải chứa chữ hoa, chữ thường và số")
    private String password;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[1-9]\\d{8,9}$",
            message = "Số điện thoại không hợp lệ (ví dụ: 0912345678 hoặc +84912345678)")
    private String phoneNumber;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    private String gender;
}
