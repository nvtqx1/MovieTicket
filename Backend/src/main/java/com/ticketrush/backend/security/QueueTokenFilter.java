package com.ticketrush.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketrush.backend.dto.request.SeatLockRequest;
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

/**
 * Filter kiểm tra queue token trước khi cho phép khóa ghế.
 *
 * Filter chỉ áp dụng cho API lock ghế và dùng header X-Queue-Token,
 * X-Showtime-Id để xác minh người dùng đã qua hàng chờ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueTokenFilter extends OncePerRequestFilter {

    private final QueueTokenService queueTokenService;
    private final ObjectMapper objectMapper;

    /**
     * Kiểm tra queue token cho request khóa ghế, các request khác được đi tiếp.
     *
     * @param request HTTP request hiện tại.
     * @param response HTTP response hiện tại.
     * @param filterChain chuỗi filter tiếp theo.
     * @throws ServletException khi filter servlet phát sinh lỗi.
     * @throws IOException khi đọc hoặc ghi request/response lỗi.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().equals("/api/seats/lock") && request.getMethod().equalsIgnoreCase("POST")) {
            String queueToken = request.getHeader("X-Queue-Token");

            if (queueToken == null || queueToken.isEmpty()) {
                sendErrorResponse(response, "Missing X-Queue-Token header. You must join the queue first.");
                return;
            }

            try {
                UserDetailsImpl userDetails = (UserDetailsImpl) org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal();

                Long userId = userDetails.getId();

                String showtimeIdStr = request.getHeader("X-Showtime-Id");
                if (showtimeIdStr == null || showtimeIdStr.isEmpty()) {
                    sendErrorResponse(response, "Missing X-Showtime-Id header required for queue validation.");
                    return;
                }

                Long showtimeId = Long.parseLong(showtimeIdStr);

                boolean isValid = queueTokenService.validateQueueToken(queueToken, userId, showtimeId);

                if (!isValid) {
                    sendErrorResponse(response, "Invalid or expired queue token. Please join the queue again.");
                    return;
                }

                filterChain.doFilter(request, response);
                return;

            } catch (Exception e) {
                log.error("Error validating queue token in filter", e);
                sendErrorResponse(response, "Error validating queue token: " + e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Ghi response lỗi 403 dạng JSON khi queue token không hợp lệ.
     *
     * @param response HTTP response hiện tại.
     * @param message thông báo lỗi trả về client.
     * @throws IOException khi ghi response thất bại.
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"FORBIDDEN\", \"message\":\"" + message + "\"}");
    }
}
