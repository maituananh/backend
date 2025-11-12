package com.spring.backend.configuration;

import com.spring.backend.helper.UserHelper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@RequiredArgsConstructor
public class JpaAuditingConfig {

  private final UserHelper userHelper;

  @Bean
  public AuditorAware<Long> auditorProvider() {
    // Trả về user hiện tại — có thể tích hợp với SecurityContextHolder
    return () -> Optional.ofNullable(userHelper.getCurrentUserId());
  }
}
