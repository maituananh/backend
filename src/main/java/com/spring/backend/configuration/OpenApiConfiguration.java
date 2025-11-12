package com.spring.backend.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default Server URL")})
public class OpenApiConfiguration {

  @Bean
  public OpenAPI customOpenAPI() {
    final String securitySchemeName = "bearerAuth";

    return new OpenAPI()
        .info(
            new Info()
                .title("Pawn-service API")
                .version("1.0")
                .description("API documentation with global Authorization header"))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                        .name(securitySchemeName)
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        .description("Enter JWT token as: Bearer <token>")));
  }
}
