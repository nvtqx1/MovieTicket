package com.ticketrush.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Vui lòng nhập địa chỉ email")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email không đúng định dạng (ví dụ: user@example.com)")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password;
}
