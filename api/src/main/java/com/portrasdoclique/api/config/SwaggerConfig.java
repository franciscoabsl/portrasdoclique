package com.portrasdoclique.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Por Trás do Clique API")
                        .description("API que expõe em tempo real o que acontece por trás de uma requisição HTTP")
                        .version("1.0.0")
                );
    }
}