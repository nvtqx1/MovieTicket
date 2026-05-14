package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu phòng chiếu.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Tìm tất cả phòng của một rạp.
     *
     * @param theaterId ID rạp.
     * @return danh sách phòng thuộc rạp.
     */
    List<Room> findByTheaterId(Long theaterId);

    /**
     * Tìm ID phòng đã bị xóa mềm theo rạp và tên phòng.
     *
     * Query native được dùng để bỏ qua {@code @SQLRestriction} trên entity Room.
     *
     * @param theaterId ID rạp.
     * @param name tên phòng.
     * @return ID phòng đã xóa mềm nếu tồn tại.
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT id FROM rooms WHERE theater_id = :theaterId AND name = :name AND is_deleted = 1 LIMIT 1", nativeQuery = true)
    java.util.Optional<Long> findDeletedRoomId(Long theaterId, String name);

    /**
     * Khôi phục phòng đã bị xóa mềm.
     *
     * Annotation {@code @Modifying} báo cho Spring Data đây là query cập nhật dữ
     * liệu, không phải query đọc.
     *
     * @param id ID phòng cần khôi phục.
     * @param capacity sức chứa mới của phòng.
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "UPDATE rooms SET is_deleted = 0, capacity = :capacity, matrix_rows = NULL, matrix_cols = NULL WHERE id = :id", nativeQuery = true)
    void restoreDeletedRoom(Long id, Integer capacity);

    /**
     * Tìm phòng theo rạp và tên phòng.
     *
     * @param theaterId ID rạp.
     * @param name tên phòng.
     * @return phòng nếu tồn tại.
     */
    Optional<Room> findByTheaterIdAndName(Long theaterId, String name);
}
