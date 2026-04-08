package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Phục vụ ĐĂNG NHẬP (Login):
    // Tìm User dựa trên email. Trả về Optional để tránh lỗi NullPointerException nếu gõ sai email.
    Optional<User> findByEmail(String email);

    // 2. Phục vụ ĐĂNG KÝ (Register):
    // Kiểm tra xem Email này đã có ai dùng trong hệ thống chưa? (Trả về true/false cực nhanh)
    Boolean existsByEmail(String email);

    // 3. Phục vụ ĐĂNG KÝ (Register):
    // Kiểm tra xem Tên đăng nhập (Username) này đã bị ai xí chỗ chưa?
    Boolean existsByUserName(String userName);

    // (Tùy chọn) Tìm user theo username nếu hệ thống của bạn cho phép đăng nhập bằng cả username hoặc email
    Optional<User> findByUserName(String userName);
}