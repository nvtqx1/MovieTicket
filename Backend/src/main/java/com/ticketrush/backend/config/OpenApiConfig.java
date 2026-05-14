package com.ticketrush.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình tài liệu OpenAPI và xác thực JWT cho Swagger.
 *
 * Annotation {@link Configuration} giúp Spring đăng ký bean OpenAPI dùng chung
 * cho trang tài liệu API.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Tạo cấu hình OpenAPI cho TicketRush.
     *
     * Security scheme {@code bearerAuth} khai báo JWT Bearer để Swagger gửi token
     * trong header Authorization cho các endpoint cần xác thực.
     *
     * @return đối tượng cấu hình OpenAPI của ứng dụng.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("TicketRush API")
                        .version("1.0.0")
                        .description("Tài liệu API cho dự án Bán vé xem phim TicketRush")
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
