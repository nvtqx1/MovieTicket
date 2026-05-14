package com.ticketrush.backend.dto.response;

/**
 * DTO response chứa thông tin rạp chiếu.
 * @param id giá trị trường id.
 * @param name giá trị trường name.
 * @param location giá trị trường location.
 * @param capacity giá trị trường capacity.
 */
public record TheaterResponse(
        Long id,
        
        String name,
        
        String location,
        
        Integer capacity
) {
}
