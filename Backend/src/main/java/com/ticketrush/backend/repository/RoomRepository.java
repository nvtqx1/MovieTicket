package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Tìm tất cả phòng của một rạp
     */
    List<Room> findByTheaterId(Long theaterId);

    /**
     * Tìm ID phòng bị xoá dựa trên theaterId và tên phòng để khôi phục.
     * Trả về Long thay vì Room Entity để Hibernate KHÔNG tự động gắn thêm @SQLRestriction("is_deleted = false")
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT id FROM rooms WHERE theater_id = :theaterId AND name = :name AND is_deleted = 1 LIMIT 1", nativeQuery = true)
    java.util.Optional<Long> findDeletedRoomId(Long theaterId, String name);

    /**
     * Khôi phục phòng bằng lệnh UPDATE native
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "UPDATE rooms SET is_deleted = 0, capacity = :capacity, matrix_rows = NULL, matrix_cols = NULL WHERE id = :id", nativeQuery = true)
    void restoreDeletedRoom(Long id, Integer capacity);

    Optional<Room> findByTheaterIdAndName(Long theaterId, String name);
}

