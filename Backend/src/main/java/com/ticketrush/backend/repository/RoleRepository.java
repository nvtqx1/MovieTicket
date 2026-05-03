package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> { // Chú ý: Khóa chính của Role là Integer
    Optional<Role> findByName(String name);

    Boolean existsByName(String name);
}