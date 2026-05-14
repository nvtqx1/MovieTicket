package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository thao tác token hàng chờ ảo.
 */
@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    /**
     * Tìm token hàng chờ theo chuỗi token.
     *
     * @param token chuỗi token cần tìm.
     * @return token hàng chờ nếu tồn tại.
     */
    Optional<QueueToken> findByToken(String token);

    /**
     * Xóa các token đã hết hạn trước thời điểm chỉ định.
     *
     * @param now thời điểm dùng làm mốc hết hạn.
     */
    void deleteByExpiresAtBefore(LocalDateTime now);
}
