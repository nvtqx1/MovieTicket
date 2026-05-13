package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.request.CreateRoomRequest;
import com.ticketrush.backend.dto.request.CreateTheaterRequest;
import com.ticketrush.backend.dto.response.RoomResponse;
import com.ticketrush.backend.dto.response.TheaterResponse;
import com.ticketrush.backend.entity.Room;
import com.ticketrush.backend.entity.Theater;
import com.ticketrush.backend.repository.RoomRepository;
import com.ticketrush.backend.repository.TheaterRepository;
import com.ticketrush.backend.service.TheaterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Triển khai của TheaterService interface
 * 
 * Chức năng:
 * - Lấy danh sách tất cả rạp
 * - Admin tạo rạp mới (VỀ LỖ HỔNG 1)
 * - Admin tạo phòng cho rạp (VỀ LỖ HỔNG 1)
 * - Lấy danh sách phòng của rạp
 * 
 * @author TicketRush Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TheaterServiceImpl implements TheaterService {

    private final TheaterRepository theaterRepository;
    private final RoomRepository roomRepository;
    private final com.ticketrush.backend.repository.ShowtimeRepository showtimeRepository;

    /**
     * Lấy toàn bộ rạp hiện có.
     *
     * @return danh sách rạp.
     */
    @Override
    public List<TheaterResponse> getAllTheaters() {
        return theaterRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * VỀ LỖ HỔNG 1: Admin API - Tạo rạp chiếu mới
     * 
     * @param request CreateTheaterRequest chứa name, location, capacity
     * @return TheaterResponse thông tin rạp vừa tạo
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc tên trùng lặp
     */
    @Override
    @Transactional  // Override readOnly = true để có thể lưu database
    public TheaterResponse createTheater(CreateTheaterRequest request) {
        try {
            log.info("🏢 Admin tạo rạp mới: {}", request.getName());

            // Validate
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("❌ Tên rạp không được để trống");
            }
            if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
                throw new IllegalArgumentException("❌ Địa chỉ rạp không được để trống");
            }
            if (request.getCapacity() == null || request.getCapacity() <= 0) {
                throw new IllegalArgumentException("❌ Sức chứa phải > 0");
            }

            // Tạo Theater entity
            Theater theater = new Theater();
            theater.setName(request.getName());
            theater.setLocation(request.getLocation());
            theater.setCapacity(request.getCapacity());

            // Lưu vào database
            Theater savedTheater = theaterRepository.save(theater);
            log.info("✅ Tạo rạp thành công: ID = {}", savedTheater.getId());

            return toResponse(savedTheater);

        } catch (Exception e) {
            log.error("❌ Lỗi tạo rạp: {}", e.getMessage(), e);
            throw new IllegalArgumentException("❌ Lỗi tạo rạp: " + e.getMessage());
        }
    }

    /**
     * VỀ LỖ HỔNG 1: Admin API - Tạo phòng chiếu cho rạp
     * 
     * @param theaterId ID của rạp sở hữu phòng này
     * @param request CreateRoomRequest chứa name, capacity
     * @return RoomResponse thông tin phòng vừa tạo
     * @throws IllegalArgumentException nếu theaterId không tồn tại hoặc dữ liệu không hợp lệ
     */
    @Override
    @Transactional  // Override readOnly = true để có thể lưu database
    public RoomResponse createRoom(Long theaterId, CreateRoomRequest request) {
        try {
            log.info("🎬 Admin tạo phòng mới cho rạp {}: {}", theaterId, request.getName());

            // Validate request
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("❌ Tên phòng không được để trống");
            }
            if (request.getCapacity() == null || request.getCapacity() <= 0) {
                throw new IllegalArgumentException("❌ Sức chứa phòng phải > 0");
            }

            // Tìm rạp
            Theater theater = theaterRepository.findById(theaterId)
                    .orElseThrow(() -> new IllegalArgumentException("❌ Rạp không tồn tại"));

            // 1. Kiểm tra xem phòng đang active có trùng tên không
            //    (findByTheaterId tự động lọc is_deleted=false nhờ @SQLRestriction)
            boolean activeRoomExists = roomRepository.findByTheaterId(theaterId).stream()
                    .anyMatch(r -> r.getName().equalsIgnoreCase(request.getName().trim()));
            if (activeRoomExists) {
                 throw new IllegalArgumentException("❌ Phòng '" + request.getName() + "' đã tồn tại trong rạp này");
            }

            // 2. Kiểm tra xem có phòng nào bị xoá mềm trùng tên không để khôi phục
            java.util.Optional<Long> deletedRoomIdOpt = roomRepository.findDeletedRoomId(theaterId, request.getName().trim());
            Room savedRoom;
            
            if (deletedRoomIdOpt.isPresent()) {
                // Khôi phục phòng đã xóa mềm bằng lệnh native update
                Long id = deletedRoomIdOpt.get();
                roomRepository.restoreDeletedRoom(id, request.getCapacity());
                
                // Lấy lại entity sau khi đã khôi phục (is_deleted = 0 nên findById sẽ thấy)
                savedRoom = roomRepository.findById(id).orElseThrow();
                log.info("✅ Khôi phục phòng đã xóa thành công: ID = {}", savedRoom.getId());
            } else {
                // 3. Tạo Room entity mới hoàn toàn
                Room room = new Room();
                room.setTheater(theater);
                room.setName(request.getName().trim());
                room.setCapacity(request.getCapacity());
                savedRoom = roomRepository.save(room);
                log.info("✅ Tạo phòng mới thành công: ID = {}", savedRoom.getId());
            }

            return toRoomResponse(savedRoom);

        } catch (Exception e) {
            log.error("❌ Lỗi tạo phòng: {}", e.getMessage(), e);
            throw new IllegalArgumentException("❌ Lỗi tạo phòng: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách phòng của một rạp.
     *
     * @param theaterId ID rạp cần lấy phòng.
     * @return danh sách phòng thuộc rạp.
     * @throws IllegalArgumentException nếu rạp không tồn tại.
     */
    @Override
    public List<RoomResponse> getRoomsByTheater(Long theaterId) {
        log.info("🎬 Lấy danh sách phòng của rạp: {}", theaterId);
        
        // Verify theater exists
        theaterRepository.findById(theaterId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Rạp không tồn tại"));

        return roomRepository.findByTheaterId(theaterId)
                .stream()
                .map(this::toRoomResponse)
                .toList();
    }

    /**
     * Chuyển entity Theater sang DTO phản hồi.
     *
     * @param theater entity rạp cần chuyển đổi.
     * @return DTO rạp.
     */
    private TheaterResponse toResponse(Theater theater) {
        return new TheaterResponse(
                theater.getId(),
                theater.getName(),
                theater.getLocation(),
                theater.getCapacity()
        );
    }

    /**
     * Chuyển entity Room sang DTO phản hồi.
     *
     * @param room entity phòng cần chuyển đổi.
     * @return DTO phòng.
     */
    private RoomResponse toRoomResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getTheater().getId(),
                room.getTheater().getName()
        );
    }

    /**
     * Cập nhật thông tin rạp.
     * {@code @Transactional} ghi đè read-only của class để cho phép lưu database.
     *
     * @param id ID rạp cần cập nhật.
     * @param request dữ liệu cập nhật rạp.
     * @return rạp sau khi cập nhật.
     * @throws IllegalArgumentException nếu rạp không tồn tại.
     */
    @Override
    @Transactional
    public TheaterResponse updateTheater(Long id, CreateTheaterRequest request) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("❌ Rạp không tồn tại"));
        
        theater.setName(request.getName());
        theater.setLocation(request.getLocation());
        theater.setCapacity(request.getCapacity());
        
        Theater updated = theaterRepository.save(theater);
        return toResponse(updated);
    }

    /**
     * Xóa rạp theo ID.
     * {@code @Transactional} đảm bảo thao tác xóa chạy trong một giao dịch.
     *
     * @param id ID rạp cần xóa.
     * @throws IllegalArgumentException nếu rạp không tồn tại hoặc không thể xóa do ràng buộc dữ liệu.
     */
    @Override
    @Transactional
    public void deleteTheater(Long id) {
        if (!theaterRepository.existsById(id)) {
            throw new IllegalArgumentException("❌ Rạp không tồn tại");
        }
        try {
            theaterRepository.deleteById(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("❌ Không thể xóa rạp (có thể đang chứa phòng chiếu hoặc đơn hàng)");
        }
    }

    // ═══════════════════════════════════════
    // TASK 1.1: Room Update/Delete
    // ═══════════════════════════════════════

    /**
     * Cập nhật thông tin phòng chiếu thuộc một rạp.
     * {@code @Transactional} đảm bảo thay đổi phòng được lưu nguyên khối.
     *
     * @param theaterId ID rạp sở hữu phòng.
     * @param roomId ID phòng cần cập nhật.
     * @param request dữ liệu cập nhật phòng.
     * @return phòng sau khi cập nhật.
     * @throws IllegalArgumentException nếu phòng không tồn tại hoặc không thuộc rạp.
     */
    @Override
    @Transactional
    public RoomResponse updateRoom(Long theaterId, Long roomId, CreateRoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Phòng không tồn tại"));

        if (!room.getTheater().getId().equals(theaterId)) {
            throw new IllegalArgumentException("❌ Phòng không thuộc rạp này");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            room.setName(request.getName());
        }
        if (request.getCapacity() != null && request.getCapacity() > 0) {
            room.setCapacity(request.getCapacity());
        }

        Room updated = roomRepository.save(room);
        log.info("✅ Cập nhật phòng {} thành công", roomId);
        return toRoomResponse(updated);
    }

    /**
     * Soft Delete phòng chiếu.
     *
     * Cơ chế hoạt động:
     * 1. Nhờ @SQLDelete trên Room entity, khi gọi roomRepository.deleteById(id),
     *    Hibernate sẽ KHÔNG chạy "DELETE FROM rooms WHERE id=?".
     *    Thay vào đó nó chạy "UPDATE rooms SET is_deleted = true WHERE id=?".
     *    → Không vi phạm FK constraint vì row vẫn tồn tại trong DB.
     *
     * 2. Nhờ @SQLRestriction("is_deleted = false"), mọi câu query SELECT sau đó
     *    sẽ tự động bỏ qua room này → Admin UI không còn thấy phòng đã xóa.
     *
     * Business rule:
     * - Nếu phòng CÒN lịch chiếu từ hôm nay trở đi → CHẶN (đã bán vé, không được xóa).
     * - Nếu phòng chỉ có lịch chiếu trong quá khứ → CHO PHÉP soft delete.
     * - Nếu phòng không có lịch chiếu nào → CHO PHÉP soft delete.
     */
    /**
     * Xóa mềm phòng chiếu nếu không còn lịch chiếu trong tương lai.
     * Dựa vào {@code @SQLDelete} và {@code @SQLRestriction} trên entity Room để giữ dữ liệu nhưng ẩn khỏi truy vấn.
     *
     * @param theaterId ID rạp sở hữu phòng.
     * @param roomId ID phòng cần xóa.
     * @throws IllegalArgumentException nếu phòng không tồn tại, không thuộc rạp hoặc còn lịch chiếu tương lai.
     */
    @Override
    @Transactional
    public void deleteRoom(Long theaterId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));

        if (!room.getTheater().getId().equals(theaterId)) {
            throw new IllegalArgumentException("Phòng không thuộc rạp này");
        }

        // Business rule: Chặn xóa nếu CÒN lịch chiếu trong tương lai (có thể đã bán vé)
        boolean hasFutureShowtimes = showtimeRepository
                .existsByRoomIdAndShowDateGreaterThanEqual(roomId, java.time.LocalDate.now());

        if (hasFutureShowtimes) {
            throw new IllegalArgumentException(
                    "Không thể xóa phòng vì đang có lịch chiếu trong tương lai. " +
                    "Hãy hủy hoặc chuyển các lịch chiếu sang phòng khác trước."
            );
        }

        // Soft delete: @SQLDelete sẽ tự chuyển thành UPDATE rooms SET is_deleted=true
        roomRepository.deleteById(roomId);
        log.info("✅ Soft-delete phòng {} (rạp {}) thành công", roomId, theaterId);
    }
}
