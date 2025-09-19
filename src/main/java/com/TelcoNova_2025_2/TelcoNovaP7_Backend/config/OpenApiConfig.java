package com.TelcoNova_2025_2.TelcoNovaP7_Backend.config;

import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_KEY = "bearer-jwt";

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
        .info(new Info()
        .title("TelcoNova Feature 1 APIs")
        .description("Describo")
        .version("v1.2"))
        .components(new Components()
            .addSecuritySchemes(BEARER_KEY, 
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_KEY));
    }
}

