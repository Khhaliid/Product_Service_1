package se.product_service_1.config;

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
        return new OpenAPI()
//                .servers(List.of(
//                        new Server()
//                                .url("https://aireviews.drillbi.se")
//                                .description("Prod via NPM")
//                ))
                .info(new Info()
                        .title("API Documentation")
                        .version("1.0.0")
                        .description("API documentation for User_Service_1 API"))
                        .addSecurityItem(new SecurityRequirement()
                                .addList("bearerAuth"))
                        .components(new Components()
                                .addSecuritySchemes("bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT"))
                );
    }
}
