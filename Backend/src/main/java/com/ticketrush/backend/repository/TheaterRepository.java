package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    // Nếu Frontend có ô tìm kiếm rạp theo tên (VD: gõ "CGV")
    // IgnoreCase giúp tìm kiếm không phân biệt hoa thường
    List<Theater> findByNameContainingIgnoreCase(String name);

    Optional<Theater> findByName(String name);
}