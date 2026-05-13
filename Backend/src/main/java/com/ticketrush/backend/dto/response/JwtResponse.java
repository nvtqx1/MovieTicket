package com.ticketrush.backend.dto.response;

import lombok.Data;

import java.util.List;

/**
 * DTO response chứa JWT và thông tin phân quyền.
 */
@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private List<String> roles;
    /**
     * Tạo đối tượng JwtResponse với dữ liệu truyền vào.
     * @param accessToken giá trị trường accessToken.
     * @param email giá trị trường email.
     * @param roles giá trị trường roles.
     */
    public JwtResponse(String accessToken, String email, List<String> roles) {
        this.token = accessToken;
        this.email = email;
        this.roles = roles;
    }
}