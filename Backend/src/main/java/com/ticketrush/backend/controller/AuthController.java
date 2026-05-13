package com.ticketrush.backend.controller;

import com.ticketrush.backend.dto.response.JwtResponse;
import com.ticketrush.backend.dto.request.LoginRequest;
import com.ticketrush.backend.dto.request.SignupRequest;
import com.ticketrush.backend.security.UserDetailsImpl;
import com.ticketrush.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý đăng nhập, đăng ký và thông tin người dùng hiện tại.
 *
 * Annotation {@link RestController} trả dữ liệu JSON, {@link RequestMapping}
 * đặt prefix API xác thực.
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Xác thực người dùng và cấp JWT.
     *
     * Annotation {@link Valid} yêu cầu validate request đăng nhập.
     *
     * @param loginRequest thông tin đăng nhập của người dùng.
     * @return JWT và thông tin đăng nhập thành công.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * Đăng ký tài khoản người dùng mới.
     *
     * @param signUpRequest thông tin tài khoản cần đăng ký.
     * @return thông báo đăng ký thành công.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest){
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok("User registered successfully!");
    }

    /**
     * Lấy thông tin người dùng đang đăng nhập từ SecurityContext.
     *
     * @return thông tin người dùng hiện tại hoặc lỗi 401 khi phiên không hợp lệ.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Phiên đăng nhập không hợp lệ hoặc đã hết hạn.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(userDetails);
    }
}
