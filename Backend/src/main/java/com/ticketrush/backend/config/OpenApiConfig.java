package com.ticketrush.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Tên của Security Scheme, bạn có thể đặt bất kỳ tên nào
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // 1. Cấu hình thông tin chung cho API
                .info(new Info()
                        .title("TicketRush API")
                        .version("1.0.0")
                        .description("Tài liệu API cho dự án Bán vé xem phim TicketRush")
                )
                // 2. Thêm yêu cầu bảo mật vào tất cả các endpoint
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 3. Định nghĩa Security Scheme (cơ chế bảo mật)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP) // Kiểu là HTTP
                                        .scheme("bearer") // Scheme là "bearer" cho JWT
                                        .bearerFormat("JWT") // Định dạng là JWT
                        )
                );
    }
}
