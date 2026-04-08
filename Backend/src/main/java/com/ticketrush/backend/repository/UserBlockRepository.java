package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    // Kiểm tra xem User A có đang bị chặn không
    Boolean existsByBlockedUser_Id(Long blockedUserId);
}