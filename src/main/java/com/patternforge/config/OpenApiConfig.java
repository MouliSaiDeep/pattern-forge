package com.patternforge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI patternForgeOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PatternForge API")
                .version("1.0")
                .description("Interactive structural design patterns playground"));
    }
}
