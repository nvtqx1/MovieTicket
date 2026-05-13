package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu vai trò người dùng.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Tìm vai trò theo tên.
     *
     * @param name tên vai trò.
     * @return vai trò nếu tồn tại.
     */
    Optional<Role> findByName(String name);

    /**
     * Kiểm tra tên vai trò đã tồn tại hay chưa.
     *
     * @param name tên vai trò cần kiểm tra.
     * @return true nếu tên vai trò đã tồn tại.
     */
    Boolean existsByName(String name);
}
