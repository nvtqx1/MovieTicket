package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.request.CreateRoomRequest;
import com.ticketrush.backend.dto.request.CreateTheaterRequest;
import com.ticketrush.backend.dto.response.RoomResponse;
import com.ticketrush.backend.dto.response.TheaterResponse;

import java.util.List;

public interface TheaterService {

    /**
     * Lấy danh sách tất cả rạp
     * @return Danh sách rạp
     */
    List<TheaterResponse> getAllTheaters();

    /**
     * Admin API: Tạo rạp chiếu mới
     * 
     * POST /v1/admin/theaters
     * 
     * @param request CreateTheaterRequest chứa name, location, capacity
     * @return TheaterResponse thông tin rạp vừa tạo
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc tên trùng lặp
     */
    TheaterResponse createTheater(CreateTheaterRequest request);

    /**
     * Admin API: Tạo phòng chiếu cho rạp
     * 
     * POST /v1/admin/theaters/{theaterId}/rooms
     * 
     * @param theaterId ID của rạp sở hữu phòng này
     * @param request CreateRoomRequest chứa name, capacity
     * @return RoomResponse thông tin phòng vừa tạo
     * @throws IllegalArgumentException nếu theaterId không tồn tại hoặc dữ liệu không hợp lệ
     */
    RoomResponse createRoom(Long theaterId, CreateRoomRequest request);

    /**
     * Lấy danh sách phòng của một rạp
     * 
     * @param theaterId ID của rạp
     * @return Danh sách phòng thuộc rạp
     */
    List<RoomResponse> getRoomsByTheater(Long theaterId);

    TheaterResponse updateTheater(Long id, CreateTheaterRequest request);

    void deleteTheater(Long id);

    // Task 1.1: Room CRUD
    RoomResponse updateRoom(Long theaterId, Long roomId, CreateRoomRequest request);

    void deleteRoom(Long theaterId, Long roomId);
}
