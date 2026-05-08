package com.ticketrush.backend.controller;

import com.ticketrush.backend.entity.Room;
import com.ticketrush.backend.entity.RoomSeat;
import com.ticketrush.backend.entity.SeatType;
import com.ticketrush.backend.repository.RoomRepository;
import com.ticketrush.backend.repository.RoomSeatRepository;
import com.ticketrush.backend.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller quản lý ghế cố định theo Phòng chiếu.
 *
 * Endpoints:
 * - POST /v1/admin/rooms/{roomId}/seats: Cấu hình & lưu sơ đồ ghế cho phòng
 * - GET  /v1/admin/rooms/{roomId}/seats: Lấy sơ đồ ghế hiện tại của phòng
 *
 * Logic phân loại ghế:
 * - Hàng cuối cùng: COUPLE
 * - Nửa sau (trừ hàng cuối): VIP  
 * - Nửa đầu: NORMAL
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {

    private final RoomRepository roomRepository;
    private final RoomSeatRepository roomSeatRepository;
    private final SeatTypeRepository seatTypeRepository;

    /**
     * POST /v1/admin/rooms/{roomId}/seats
     * Tạo & lưu sơ đồ ghế cố định cho phòng chiếu.
     * Nếu phòng đã có ghế trước đó, sẽ XOÁ HẾT rồi tạo lại.
     *
     * Request Body: { "rows": 10, "cols": 15 }
     * Response: { roomId, rows, cols, totalSeats, seats: [...] }
     */
    @PostMapping("/{roomId}/seats")
    @Transactional
    public ResponseEntity<?> configureRoomSeats(
            @PathVariable Long roomId,
            @RequestBody Map<String, Integer> payload) {

        log.info("🪑 Cấu hình ghế cho phòng {}", roomId);

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));

            Integer rows = payload.get("rows");
            Integer cols = payload.get("cols");

            if (rows == null || cols == null || rows <= 0 || cols <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Số hàng và số cột phải > 0"));
            }

            // Cập nhật ma trận phòng
            room.setMatrixRows(rows);
            room.setMatrixCols(cols);
            room.setCapacity(rows * cols);
            roomRepository.save(room);

            // Xoá ghế cũ nếu có
            roomSeatRepository.deleteAllByRoomId(roomId);

            // Lấy SeatTypes
            List<SeatType> seatTypes = seatTypeRepository.findAll();
            SeatType normalType = seatTypes.stream()
                    .filter(t -> "NORMAL".equalsIgnoreCase(t.getName())).findFirst()
                    .orElseThrow(() -> new RuntimeException("SeatType NORMAL not found"));
            SeatType vipType = seatTypes.stream()
                    .filter(t -> "VIP".equalsIgnoreCase(t.getName())).findFirst()
                    .orElse(normalType);
            SeatType coupleType = seatTypes.stream()
                    .filter(t -> "COUPLE".equalsIgnoreCase(t.getName())).findFirst()
                    .orElse(normalType);

            // Tạo ghế mới
            List<RoomSeat> seats = new ArrayList<>();
            for (int row = 0; row < rows; row++) {
                char rowChar = (char) ('A' + row);
                boolean isLastRow = (row == rows - 1);
                boolean isVipRow = (row >= rows / 2) && !isLastRow;

                for (int col = 0; col < cols; col++) {
                    SeatType type;
                    if (isLastRow) {
                        type = coupleType;
                    } else if (isVipRow) {
                        type = vipType;
                    } else {
                        type = normalType;
                    }

                    RoomSeat seat = RoomSeat.builder()
                            .room(room)
                            .seatNumber(String.valueOf(rowChar) + (col + 1))
                            .seatType(type)
                            .rowIndex(row)
                            .colIndex(col)
                            .build();
                    seats.add(seat);
                }
            }

            roomSeatRepository.saveAll(seats);
            log.info("✅ Đã tạo {} ghế cố định cho phòng {} ({}x{})", seats.size(), roomId, rows, cols);

            // Build response
            List<Map<String, Object>> seatList = seats.stream().map(s -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", s.getId());
                m.put("seatNumber", s.getSeatNumber());
                m.put("seatType", s.getSeatType().getName());
                m.put("rowIndex", s.getRowIndex());
                m.put("colIndex", s.getColIndex());
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("roomId", roomId);
            response.put("roomName", room.getName());
            response.put("rows", rows);
            response.put("cols", cols);
            response.put("totalSeats", seats.size());
            response.put("seats", seatList);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Lỗi cấu hình ghế phòng {}: {}", roomId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * GET /v1/admin/rooms/{roomId}/seats
     * Lấy sơ đồ ghế hiện tại của phòng để render trên frontend.
     */
    @GetMapping("/{roomId}/seats")
    public ResponseEntity<?> getRoomSeats(@PathVariable Long roomId) {
        log.info("📋 Lấy sơ đồ ghế phòng {}", roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu"));

        List<RoomSeat> seats = roomSeatRepository.findByRoomIdOrderByRowIndexAscColIndexAsc(roomId);

        List<Map<String, Object>> seatList = seats.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("seatNumber", s.getSeatNumber());
            m.put("seatType", s.getSeatType().getName());
            m.put("rowIndex", s.getRowIndex());
            m.put("colIndex", s.getColIndex());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("roomId", roomId);
        response.put("roomName", room.getName());
        response.put("rows", room.getMatrixRows());
        response.put("cols", room.getMatrixCols());
        response.put("totalSeats", seats.size());
        response.put("seats", seatList);

        return ResponseEntity.ok(response);
    }
}
