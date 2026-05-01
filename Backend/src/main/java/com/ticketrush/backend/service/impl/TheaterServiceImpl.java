package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.CreateRoomRequest;
import com.ticketrush.backend.dto.CreateTheaterRequest;
import com.ticketrush.backend.dto.RoomResponse;
import com.ticketrush.backend.dto.TheaterResponse;
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

            // Tạo Room entity
            Room room = new Room();
            room.setTheater(theater);
            room.setName(request.getName());
            room.setCapacity(request.getCapacity());

            // Lưu vào database
            Room savedRoom = roomRepository.save(room);
            log.info("✅ Tạo phòng thành công: ID = {}", savedRoom.getId());

            return toRoomResponse(savedRoom);

        } catch (Exception e) {
            log.error("❌ Lỗi tạo phòng: {}", e.getMessage(), e);
            throw new IllegalArgumentException("❌ Lỗi tạo phòng: " + e.getMessage());
        }
    }

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

    private TheaterResponse toResponse(Theater theater) {
        return new TheaterResponse(
                theater.getId(),
                theater.getName(),
                theater.getLocation(),
                theater.getCapacity()
        );
    }

    private RoomResponse toRoomResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getTheater().getId(),
                room.getTheater().getName()
        );
    }
}
