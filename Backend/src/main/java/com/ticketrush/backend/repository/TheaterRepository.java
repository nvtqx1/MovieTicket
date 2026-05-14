package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu rạp chiếu.
 */
@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    /**
     * Tìm rạp theo một phần tên, không phân biệt hoa thường.
     *
     * @param name từ khóa tên rạp.
     * @return danh sách rạp khớp từ khóa.
     */
    List<Theater> findByNameContainingIgnoreCase(String name);

    /**
     * Tìm rạp theo tên chính xác.
     *
     * @param name tên rạp.
     * @return rạp nếu tồn tại.
     */
    Optional<Theater> findByName(String name);
}
