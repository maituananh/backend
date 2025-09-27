package com.spring.backend.configuration;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsAppConfiguration {

  private static final List<String> ALLOW_EXPORT_HEADERS =
      Arrays.asList("Authorization", "Content-Disposition");

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("*"); // Specify allowed origins
    configuration.addAllowedHeader("*"); // Allowed HTTP methods
    configuration.addAllowedMethod("*"); // Allowed request headers
    configuration.setAllowCredentials(
        true); // Allow credentials (e.g., cookies, authorization headers)
    configuration.setExposedHeaders(ALLOW_EXPORT_HEADERS); // Headers to expose to the client

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // Apply this configuration to all paths

    return source;
  }
}
