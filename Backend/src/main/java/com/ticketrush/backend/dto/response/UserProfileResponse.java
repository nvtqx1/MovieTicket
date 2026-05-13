package com.ticketrush.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO response chứa hồ sơ người dùng.
 */
@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String userName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String role;
}
