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
 * Controller quản trị sơ đồ ghế cố định theo phòng chiếu.
 *
 * Annotation {@link PreAuthorize} giới hạn cho ADMIN; {@link Transactional}
 * đảm bảo thao tác xóa ghế cũ và tạo ghế mới được commit hoặc rollback cùng nhau.
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
     * Cấu hình lại sơ đồ ghế cố định của phòng chiếu.
     *
     * Method xóa toàn bộ ghế cũ rồi tạo lại theo ma trận mới; hàng cuối là
     * COUPLE, nửa sau là VIP, nửa đầu là NORMAL.
     *
     * @param roomId ID phòng chiếu cần cấu hình.
     * @param payload dữ liệu gồm rows và cols.
     * @return thông tin sơ đồ ghế sau khi tạo.
     */
    @PostMapping("/{roomId}/seats")
    @Transactional
    public ResponseEntity<?> configureRoomSeats(
            @PathVariable Long roomId,
            @RequestBody Map<String, Integer> payload) {

        log.info("Cấu hình ghế cho phòng {}", roomId);

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

            roomSeatRepository.deleteAllByRoomId(roomId);

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
            log.info("Đã tạo {} ghế cố định cho phòng {} ({}x{})", seats.size(), roomId, rows, cols);

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
            log.error("Lỗi cấu hình ghế phòng {}: {}", roomId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Lấy sơ đồ ghế hiện tại của phòng chiếu.
     *
     * @param roomId ID phòng chiếu cần xem ghế.
     * @return thông tin phòng và danh sách ghế đã sắp xếp theo hàng, cột.
     */
    @GetMapping("/{roomId}/seats")
    public ResponseEntity<?> getRoomSeats(@PathVariable Long roomId) {
        log.info("Lấy sơ đồ ghế phòng {}", roomId);

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
