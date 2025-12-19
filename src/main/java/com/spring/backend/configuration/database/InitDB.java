package com.spring.backend.configuration.database;

import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitDB implements CommandLineRunner {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  @Override
  public void run(String... args) {
    Optional<UserEntity> adminEntity = userRepository.findByUsername("admin");

    if (adminEntity.isEmpty()) {
      userRepository.save(
          UserEntity.builder()
              .username("admin")
              .password(passwordEncoder.encode("admin"))
              .name("admin")
              .email("admin@gmail.com")
              .phone("123456789")
              .cardId("044444444444444")
              .address("admin123")
              .gender("female")
              .isActive(true)
              .role(UserRole.ADMIN)
              .build());
    }

    Optional<UserEntity> userEntity = userRepository.findByUsername("user");

    if (userEntity.isEmpty()) {
      userRepository.save(
          UserEntity.builder()
              .username("user")
              .password(passwordEncoder.encode("user"))
              .name("user")
              .email("user@gmail.com")
              .phone("123456789")
              .cardId("044444444444444")
              .address("admin123")
              .gender("female")
              .isActive(true)
              .role(UserRole.CUSTOMER)
              .createdAt(Instant.now())
              .build());
    }
  }
}
