package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.response.ReservationResponse;
import com.ticketrush.backend.security.UserDetailsImpl;
import com.ticketrush.backend.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller truy vấn và hủy vé theo reservation.
 *
 * Hỗ trợ cả prefix cũ {@code /tickets} và prefix versioned {@code /v1/tickets}.
 */
@RestController
@RequestMapping({"/tickets", "/v1/tickets"})
@RequiredArgsConstructor
public class TicketController {

    private final ReservationService reservationService;

    /**
     * Lấy thông tin vé của người dùng hiện tại.
     *
     * @param id ID reservation tương ứng với vé.
     * @param authentication thông tin xác thực của người dùng.
     * @return thông tin reservation/vé.
     * @throws Exception khi service lấy reservation phát sinh lỗi.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getTicket(@PathVariable Long id, Authentication authentication) throws Exception {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(reservationService.getReservation(id, userId));
    }

    /**
     * Hủy vé và nhả các ghế liên quan.
     *
     * @param id ID reservation cần hủy.
     * @param authentication thông tin xác thực của người dùng.
     * @return thông báo hủy vé thành công.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelTicket(@PathVariable Long id, Authentication authentication) {
        Long userId = extractUserId(authentication);
        reservationService.cancelReservation(id, userId);
        return ResponseEntity.ok(Map.of(
                "apiStatus", "SUCCESS",
                "message", "Ticket cancelled and seats released"
        ));
    }

    /**
     * Trích xuất ID người dùng từ Authentication.
     *
     * @param authentication thông tin xác thực hiện tại.
     * @return ID người dùng.
     * @throws IllegalArgumentException khi chưa đăng nhập hoặc principal không hợp lệ.
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Chua dang nhap");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new IllegalArgumentException("Khong the xac dinh user hien tai");
    }
}
