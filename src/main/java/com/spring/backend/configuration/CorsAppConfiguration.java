package com.spring.backend.configuration;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsAppConfiguration {

  private static final List<String> ALLOW_EXPORT_HEADERS =
      Arrays.asList("Authorization", "Content-Disposition");

  @Primary
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*")); // Specify allowed origins
    configuration.setAllowedHeaders(List.of("*")); // Allowed HTTP methods
    configuration.setAllowedMethods(
        List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // Allowed request headers
    configuration.setAllowCredentials(false);
    configuration.setExposedHeaders(ALLOW_EXPORT_HEADERS); // Headers to expose to the client

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // Apply this configuration to all paths

    return source;
  }
}
