package com.spring.backend.configuration.database;

import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.UserRepository;
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
  public void run(String... args) throws Exception {
    Optional<UserEntity> userEntity = userRepository.findByUsername("admin");

    if (userEntity.isEmpty()) {
      userRepository.save(
          UserEntity.builder()
              .username("admin")
              .password(passwordEncoder.encode("admin"))
              .name("admin")
              .email("admin@gmail.com")
              .build());
    }
  }
}
