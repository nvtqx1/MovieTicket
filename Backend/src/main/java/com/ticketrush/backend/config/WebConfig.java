package com.ticketrush.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình MVC cho ứng dụng web.
 *
 * Annotation {@link Configuration} đăng ký cấu hình CORS vào Spring MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Đăng ký CORS cho toàn bộ API.
     *
     * Chỉ cho phép các origin frontend nội bộ, các HTTP method cần dùng và header
     * xác thực phổ biến; bật credentials để hỗ trợ cookie hoặc header xác thực.
     *
     * @param registry registry dùng để khai báo mapping CORS.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:5174",
                        "http://127.0.0.1:5173",
                        "http://127.0.0.1:5174"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                .allowCredentials(true);
    }
}
