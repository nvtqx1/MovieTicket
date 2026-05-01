package com.ticketrush.backend.repository;

import com.ticketrush.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Tìm tất cả phòng của một rạp
     */
    List<Room> findByTheaterId(Long theaterId);
}

