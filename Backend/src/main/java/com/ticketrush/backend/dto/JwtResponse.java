package com.ticketrush.backend.dto;

import lombok.Data;

import java.util.List;

@Data // Tự động sinh ra Getter/Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private List<String> roles; // Thêm danh sách quyền

    // Cập nhật lại Constructor
    public JwtResponse(String accessToken, String email, List<String> roles) {
        this.token = accessToken;
        this.email = email;
        this.roles = roles;
    }
}