package com.ticketrush.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthTokenFilter authTokenFilter;
    private final DaoAuthenticationProvider authenticationProvider;
    private final QueueTokenFilter queueTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Bật CORS
                .csrf(csrf -> csrf.disable()) // Tắt CSRF vì hệ thống API dùng JWT không bị lỗi này
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // MỞ CỬA CHO SWAGGER UI
                        .requestMatchers(
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html",
                                "/v1/api-docs/**" // Thêm dòng này để khớp với cái lỗi của bạn
                        ).permitAll()
                        // 1. API Xác thực -> Mở cửa tự do
                        .requestMatchers("/v1/auth/**").permitAll()
                        // 2. Các API công khai khác
                        .requestMatchers(HttpMethod.GET,"/v1/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/v1/theaters/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/v1/showtimes/**").permitAll()
                        // 3. Đường ống WebSocket
                        .requestMatchers("/ws/**").permitAll()
                        // 4. TOÀN BỘ CÁC API KHÁC -> Bắt buộc phải có Token
                        .anyRequest().authenticated()
                );
        // Nạp bộ cung cấp dữ liệu và bộ lọc JWT (AuthTokenFilter) lên tuyến đầu
        http.authenticationProvider(authenticationProvider);
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(queueTokenFilter, AuthTokenFilter.class);

        return http.build();
    }

    // Cấu hình CORS để Frontend ở Port khác có thể gọi được API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                        "http://localhost:5173",
                        "http://localhost:5174",
                        "http://127.0.0.1:5173",
                        "http://127.0.0.1:5174",
                        "https://movie-ticket-admin-sigma.vercel.app",
                        "https://movie-ticket-mauve.vercel.app",
                        "https://movieticket.me",
                        "https://www.movieticket.me"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
