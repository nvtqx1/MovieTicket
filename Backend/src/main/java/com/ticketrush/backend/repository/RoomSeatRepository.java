package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.RoomSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomSeatRepository extends JpaRepository<RoomSeat, Long> {

    /**
     * Lấy tất cả ghế cố định của 1 phòng, sắp xếp theo hàng rồi cột
     */
    List<RoomSeat> findByRoomIdOrderByRowIndexAscColIndexAsc(Long roomId);

    /**
     * Xoá tất cả ghế cũ của phòng (để tái cấu hình)
     */
    void deleteAllByRoomId(Long roomId);

    /**
     * Đếm số ghế của phòng
     */
    long countByRoomId(Long roomId);
}
