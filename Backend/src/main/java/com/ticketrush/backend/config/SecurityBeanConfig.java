package com.ticketrush.backend.config;

import com.ticketrush.backend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cấu hình các bean nền tảng cho xác thực Spring Security.
 *
 * Annotation {@link RequiredArgsConstructor} tạo constructor cho dependency
 * {@link UserDetailsServiceImpl}; {@link Configuration} đăng ký các bean bảo mật.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityBeanConfig {

    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Tạo bộ mã hóa mật khẩu bằng thuật toán BCrypt.
     *
     * @return password encoder dùng cho lưu và kiểm tra mật khẩu.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Lấy {@link AuthenticationManager} từ cấu hình xác thực của Spring Security.
     *
     * @param authConfig cấu hình xác thực hiện tại của Spring Security.
     * @return authentication manager dùng cho quá trình đăng nhập.
     * @throws Exception khi Spring Security không tạo được authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Tạo provider xác thực dựa trên {@link UserDetailsServiceImpl} và BCrypt.
     *
     * @return provider xác thực tài khoản người dùng.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
