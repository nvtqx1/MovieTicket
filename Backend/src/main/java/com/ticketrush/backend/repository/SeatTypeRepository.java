package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác dữ liệu loại ghế.
 */
@Repository
public interface SeatTypeRepository extends JpaRepository<SeatType, Integer> {

    /**
     * Tìm loại ghế theo tên.
     *
     * @param name tên loại ghế.
     * @return loại ghế nếu tồn tại.
     */
    Optional<SeatType> findByName(String name);

    /**
     * Kiểm tra tên loại ghế đã tồn tại hay chưa.
     *
     * @param name tên loại ghế cần kiểm tra.
     * @return true nếu tên loại ghế đã tồn tại.
     */
    Boolean existsByName(String name);
}
