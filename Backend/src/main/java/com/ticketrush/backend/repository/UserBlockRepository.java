package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu khóa tài khoản người dùng.
 */
@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    /**
     * Kiểm tra người dùng có đang bị khóa hay không.
     *
     * @param blockedUserId ID người dùng cần kiểm tra.
     * @return true nếu người dùng đang bị khóa.
     */
    Boolean existsByBlockedUser_Id(Long blockedUserId);
}
