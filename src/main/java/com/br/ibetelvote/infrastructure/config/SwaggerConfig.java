package com.br.ibetelvote.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenApi() {

        OpenAPI openAPI = new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("API Service - ASS")
                        .description("API Spring Boot para controles de sistema de eleições eclesiásticas.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Alex Soares")
                                .email("alexsaosilva@gmail.com")
                                .url("https://github.com/Led-ds/ibetelvote")));

        return openAPI;
    }
}
