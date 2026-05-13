package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.response.JwtResponse;
import com.ticketrush.backend.dto.request.LoginRequest;
import com.ticketrush.backend.dto.request.SignupRequest;
import com.ticketrush.backend.entity.Role;
import com.ticketrush.backend.entity.User;
import com.ticketrush.backend.repository.RoleRepository;
import com.ticketrush.backend.repository.UserRepository;
import com.ticketrush.backend.security.JwtUtils;
import com.ticketrush.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Dịch vụ xử lý đăng nhập, đăng ký và phát hành JWT cho người dùng.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder encoder;

    /**
     * Xác thực email, mật khẩu và tạo JWT kèm danh sách quyền.
     *
     * @param loginRequest thông tin đăng nhập của người dùng.
     * @return phản hồi chứa JWT, email và danh sách quyền.
     * @throws org.springframework.security.core.AuthenticationException nếu thông tin đăng nhập không hợp lệ.
     */
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // 1. Xác thực tài khoản
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Tạo Token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 3. Lấy thông tin User và Danh sách Role
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();

        // 4. Trả về đối tượng phản hồi chứa Token và Roles
        return new JwtResponse(jwt, userDetails.getUsername(), roles);
    }

    /**
     * Đăng ký tài khoản mới với quyền mặc định ROLE_USER.
     * {@code @Transactional} đảm bảo rollback nếu kiểm tra trùng hoặc lưu user thất bại.
     *
     * @param signUpRequest thông tin đăng ký của người dùng.
     * @throws RuntimeException nếu email, username đã tồn tại hoặc không tìm thấy quyền mặc định.
     */
    @Transactional
    public void registerUser(SignupRequest signUpRequest) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng trong hệ thống!");
        }

        // Kiểm tra userName đã tồn tại
        if (userRepository.existsByUserName(signUpRequest.getUserName())) {
            throw new RuntimeException("Tên người dùng đã được sử dụng trong hệ thống!");
        }

        // Tạo user mới
        User user = new User();
        user.setUserName(signUpRequest.getUserName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setDateOfBirth(signUpRequest.getDateOfBirth());
        user.setGender(signUpRequest.getGender());

        // Mặc định là ROLE_USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy quyền mặc định (ROLE_USER)."));
        user.setRole(userRole);

        userRepository.save(user);
    }
}
