package com.example.campus_navigation_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusNavigationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Campus Navigation API")
                        .description("Campus navigation, building lookup, and location sample APIs.")
                        .version("v1"));
    }
}
