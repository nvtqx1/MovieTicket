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

/**
 * Cấu hình bảo mật chính của ứng dụng.
 *
 * Annotation {@link EnableWebSecurity} bật Spring Security cho web API;
 * {@link EnableMethodSecurity} cho phép dùng các annotation như
 * {@code @PreAuthorize} ở method controller/service.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthTokenFilter authTokenFilter;
    private final DaoAuthenticationProvider authenticationProvider;
    private final QueueTokenFilter queueTokenFilter;

    /**
     * Cấu hình security filter chain cho HTTP API.
     *
     * CSRF bị tắt vì API dùng JWT stateless; JWT filter chạy trước
     * {@link UsernamePasswordAuthenticationFilter}, còn queue filter chạy sau
     * filter JWT để đã có thông tin người dùng trong SecurityContext.
     *
     * @param http đối tượng cấu hình HTTP security.
     * @return filter chain bảo mật của ứng dụng.
     * @throws Exception khi cấu hình filter chain thất bại.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
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
                                "/v1/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/v1/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/v1/theaters/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/v1/showtimes/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider);
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(queueTokenFilter, AuthTokenFilter.class);

        return http.build();
    }

    /**
     * Tạo cấu hình CORS cho frontend local.
     *
     * @return source chứa cấu hình CORS áp dụng cho toàn bộ endpoint.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
