package com.ticketrush.backend.controller;

import com.ticketrush.backend.entity.Room;
import com.ticketrush.backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {

    private final RoomRepository roomRepository;

    @PostMapping("/{roomId}/seats")
    public ResponseEntity<?> configureRoomSeats(
            @PathVariable Long roomId,
            @RequestBody Map<String, Integer> payload) {
        log.info("Cấu hình ma trận ghế cho phòng {}", roomId);
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));

            Integer rows = payload.get("rows");
            Integer cols = payload.get("cols");

            if (rows == null || cols == null || rows <= 0 || cols <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Số hàng và số cột phải > 0"));
            }

            room.setMatrixRows(rows);
            room.setMatrixCols(cols);
            room.setCapacity(rows * cols);
            roomRepository.save(room);

            return ResponseEntity.ok(Map.of(
                    "message", "Cấu hình ghế thành công",
                    "roomId", roomId,
                    "rows", rows,
                    "cols", cols,
                    "capacity", room.getCapacity()
            ));
        } catch (Exception e) {
            log.error("Lỗi cấu hình ghế phòng {}: {}", roomId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
}
