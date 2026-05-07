package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.UserProfileResponse;
import com.ticketrush.backend.entity.User;
import com.ticketrush.backend.repository.UserRepository;
import com.ticketrush.backend.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profile")
@RequiredArgsConstructor
@Tag(name = "👤 User Profile", description = "API quản lý hồ sơ người dùng")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "📋 Lấy thông tin cá nhân", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            UserProfileResponse response = UserProfileResponse.builder()
                    .id(user.getId())
                    .userName(user.getUserName())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .dateOfBirth(user.getDateOfBirth())
                    .gender(user.getGender())
                    .role(user.getRole().getName())
                    .build();

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).build();
    }
}
