package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {
    Optional<QueueToken> findByToken(String token);

    // Câu lệnh này dùng để viết 1 con Bot chạy ngầm: Tự động xóa các Token đã hết hạn
    void deleteByExpiresAtBefore(LocalDateTime now);
}