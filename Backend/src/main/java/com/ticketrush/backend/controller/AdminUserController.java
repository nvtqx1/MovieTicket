package com.ticketrush.backend.controller;

import com.ticketrush.backend.entity.User;
import com.ticketrush.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "👥 Admin User Management", description = "API quản lý người dùng (Admin)")
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "📋 Lấy danh sách người dùng", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> response = users.stream().map(u -> {
            // Dùng HashMap thay vì Map.of vì Map.of không cho phép null
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("userName", u.getUserName());
            map.put("email", u.getEmail());
            map.put("phoneNumber", u.getPhoneNumber());
            map.put("dateOfBirth", u.getDateOfBirth() != null ? u.getDateOfBirth().toString() : "");
            map.put("gender", u.getGender() != null ? u.getGender() : "");
            map.put("role", u.getRole() != null ? u.getRole().getName() : "");
            map.put("isBanned", u.getIsBanned() != null && u.getIsBanned());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Task 1.3: Ban/Unban user (toggle)
     * PUT /v1/admin/users/{id}/ban
     */
    @PutMapping("/{id}/ban")
    @Operation(summary = "🚫 Ban/Unban người dùng", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<?> toggleBanUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Toggle ban status
        boolean newStatus = !(user.getIsBanned() != null && user.getIsBanned());
        user.setIsBanned(newStatus);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "userName", user.getUserName(),
                "isBanned", newStatus,
                "message", newStatus ? "Đã khóa tài khoản " + user.getUserName() : "Đã mở khóa tài khoản " + user.getUserName()
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "🗑️ Xóa người dùng (Hard delete)", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không thể xóa người dùng này vì họ đã phát sinh dữ liệu (vé đặt, đánh giá...). Để bảo toàn doanh thu, hệ thống chặn thao tác này."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi hệ thống khi xóa người dùng: " + e.getMessage()));
        }
    }
}
