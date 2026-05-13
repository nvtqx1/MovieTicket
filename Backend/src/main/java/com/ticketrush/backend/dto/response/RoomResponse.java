package com.ticketrush.backend.dto.response;

/**
 * DTO response chứa thông tin phòng chiếu.
 * @param id giá trị trường id.
 * @param name giá trị trường name.
 * @param capacity giá trị trường capacity.
 * @param theaterId giá trị trường theaterId.
 * @param theaterName giá trị trường theaterName.
 */
public record RoomResponse(
    Long id,
    
    String name,
    
    Integer capacity,
    
    Long theaterId,
    
    String theaterName
) {
}

