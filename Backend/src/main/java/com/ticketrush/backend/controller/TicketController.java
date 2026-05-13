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

@RestController
@RequestMapping({"/tickets", "/v1/tickets"})
@RequiredArgsConstructor
public class TicketController {

    private final ReservationService reservationService;

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getTicket(@PathVariable Long id, Authentication authentication) throws Exception {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(reservationService.getReservation(id, userId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelTicket(@PathVariable Long id, Authentication authentication) {
        Long userId = extractUserId(authentication);
        reservationService.cancelReservation(id, userId);
        return ResponseEntity.ok(Map.of(
                "apiStatus", "SUCCESS",
                "message", "Ticket cancelled and seats released"
        ));
    }

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
