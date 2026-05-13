package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.RoomSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu ghế cố định của phòng chiếu.
 */
@Repository
public interface RoomSeatRepository extends JpaRepository<RoomSeat, Long> {

    /**
     * Lấy ghế cố định của phòng, sắp xếp theo hàng rồi cột.
     *
     * @param roomId ID phòng chiếu.
     * @return danh sách ghế cố định của phòng.
     */
    List<RoomSeat> findByRoomIdOrderByRowIndexAscColIndexAsc(Long roomId);

    /**
     * Xóa toàn bộ ghế cố định của một phòng.
     *
     * @param roomId ID phòng chiếu cần xóa ghế.
     */
    void deleteAllByRoomId(Long roomId);

    /**
     * Đếm số ghế cố định của một phòng.
     *
     * @param roomId ID phòng chiếu.
     * @return số ghế cố định của phòng.
     */
    long countByRoomId(Long roomId);
}
