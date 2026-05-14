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

/**
 * Controller quản trị người dùng.
 *
 * Annotation {@link Tag} nhóm endpoint trên Swagger; {@link RequiredArgsConstructor}
 * inject repository qua constructor.
 */
@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "API quản lý người dùng cho Admin")
public class AdminUserController {

    private final UserRepository userRepository;

    /**
     * Lấy danh sách toàn bộ người dùng.
     *
     * Dùng {@link HashMap} để chấp nhận giá trị null khi dựng response.
     *
     * @return danh sách người dùng ở dạng map dữ liệu đơn giản.
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách người dùng", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> response = users.stream().map(u -> {
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
     * Đảo trạng thái khóa tài khoản của người dùng.
     *
     * @param id ID người dùng cần khóa hoặc mở khóa.
     * @return thông tin trạng thái khóa mới hoặc 404 nếu không tìm thấy.
     */
    @PutMapping("/{id}/ban")
    @Operation(summary = "Khóa hoặc mở khóa người dùng", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<?> toggleBanUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

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

    /**
     * Xóa cứng người dùng khỏi hệ thống.
     *
     * Chặn xóa khi người dùng đã phát sinh dữ liệu liên quan để tránh lỗi toàn
     * vẹn dữ liệu.
     *
     * @param id ID người dùng cần xóa.
     * @return phản hồi rỗng khi thành công hoặc thông báo lỗi khi không thể xóa.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không thể xóa người dùng này vì họ đã phát sinh dữ liệu."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi hệ thống khi xóa người dùng: " + e.getMessage()));
        }
    }
}
