package com.ticketrush.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response trả về sau khi sinh ma trận ghế.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSeatResponse {
    
    private Long showtimeId;
    
    private Integer totalSeatsGenerated;
    
    private Integer rows;
    
    private Integer cols;
    
    private String message;
}

