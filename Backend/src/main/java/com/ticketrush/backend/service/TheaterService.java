package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.request.CreateRoomRequest;
import com.ticketrush.backend.dto.request.CreateTheaterRequest;
import com.ticketrush.backend.dto.response.RoomResponse;
import com.ticketrush.backend.dto.response.TheaterResponse;

import java.util.List;

/**
 * Dịch vụ quản lý rạp và phòng chiếu.
 */
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

    /**
     * Cập nhật thông tin rạp.
     *
     * @param id ID rạp cần cập nhật.
     * @param request dữ liệu cập nhật rạp.
     * @return thông tin rạp sau khi cập nhật.
     * @throws IllegalArgumentException nếu rạp không tồn tại hoặc dữ liệu không hợp lệ.
     */
    TheaterResponse updateTheater(Long id, CreateTheaterRequest request);

    /**
     * Xóa rạp theo ID.
     *
     * @param id ID rạp cần xóa.
     * @throws IllegalArgumentException nếu rạp không tồn tại hoặc không thể xóa do ràng buộc dữ liệu.
     */
    void deleteTheater(Long id);

    /**
     * Cập nhật phòng chiếu thuộc một rạp.
     *
     * @param theaterId ID rạp sở hữu phòng.
     * @param roomId ID phòng cần cập nhật.
     * @param request dữ liệu cập nhật phòng.
     * @return thông tin phòng sau khi cập nhật.
     * @throws IllegalArgumentException nếu phòng không tồn tại hoặc không thuộc rạp.
     */
    RoomResponse updateRoom(Long theaterId, Long roomId, CreateRoomRequest request);

    /**
     * Xóa mềm phòng chiếu thuộc một rạp.
     *
     * @param theaterId ID rạp sở hữu phòng.
     * @param roomId ID phòng cần xóa.
     * @throws IllegalArgumentException nếu phòng không tồn tại, không thuộc rạp hoặc còn lịch chiếu tương lai.
     */
    void deleteRoom(Long theaterId, Long roomId);
}
