package com.ticketrush.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketrush.backend.dto.SeatLockRequest;
import com.ticketrush.backend.service.QueueTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueTokenFilter extends OncePerRequestFilter {

    private final QueueTokenService queueTokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Chỉ chặn API lock ghế
        if (request.getRequestURI().equals("/api/seats/lock") && request.getMethod().equalsIgnoreCase("POST")) {
            
            String queueToken = request.getHeader("X-Queue-Token");
            
            if (queueToken == null || queueToken.isEmpty()) {
                sendErrorResponse(response, "Missing X-Queue-Token header. You must join the queue first.");
                return;
            }

            try {
                // Đọc body để lấy userId (từ SecurityContext) và showtimeId (từ DB)
                // Tuy nhiên, SeatLockRequest chỉ có seatIds, không có showtimeId trực tiếp.
                // Ở tầng filter, việc query DB lấy showtimeId từ seatId khá tốn kém.
                // Nhưng vì token có chứa showtimeId, ta có thể lấy thông tin trực tiếp từ user hiện tại.
                
                UserDetailsImpl userDetails = (UserDetailsImpl) org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal();
                
                Long userId = userDetails.getId();
                
                // TẠM THỜI: Đọc showtimeId từ tham số (Nếu FE có gửi kèm trên query params)
                // Hoặc tối ưu hơn: Bắt FE truyền showtimeId trong SeatLockRequest.
                // Vì API /seats/lock hiện tại KHÔNG yêu cầu truyền showtimeId, ta có 2 cách:
                // C1: FE truyền showtimeId qua Header (VD: X-Showtime-Id)
                // C2: Lấy từ Header cho nhanh.
                
                String showtimeIdStr = request.getHeader("X-Showtime-Id");
                if (showtimeIdStr == null || showtimeIdStr.isEmpty()) {
                    sendErrorResponse(response, "Missing X-Showtime-Id header required for queue validation.");
                    return;
                }
                
                Long showtimeId = Long.parseLong(showtimeIdStr);

                // Validate
                boolean isValid = queueTokenService.validateQueueToken(queueToken, userId, showtimeId);
                
                if (!isValid) {
                    sendErrorResponse(response, "Invalid or expired queue token. Please join the queue again.");
                    return;
                }
                
                // Nếu OK, cho đi tiếp
                filterChain.doFilter(request, response);
                return;

            } catch (Exception e) {
                log.error("Error validating queue token in filter", e);
                sendErrorResponse(response, "Error validating queue token: " + e.getMessage());
                return;
            }
        }

        // Các request khác cho đi qua bình thường
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"FORBIDDEN\", \"message\":\"" + message + "\"}");
    }
}
