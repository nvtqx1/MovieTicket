package com.ticketrush.backend.controller;

import com.ticketrush.backend.entity.User;
import com.ticketrush.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> response = users.stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "userName", u.getUserName(),
                "email", u.getEmail(),
                "phoneNumber", u.getPhoneNumber(),
                "dateOfBirth", u.getDateOfBirth() != null ? u.getDateOfBirth().toString() : "",
                "gender", u.getGender() != null ? u.getGender() : "",
                "role", u.getRole() != null ? u.getRole().getName() : ""
        )).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
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
