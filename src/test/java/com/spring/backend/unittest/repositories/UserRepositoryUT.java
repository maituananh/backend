package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryUT {

  @Autowired private UserRepository userRepository;
  @Autowired private TestEntityManager entityManager;

  private UserEntity user;

  @BeforeEach
  void setUp() {
    user =
        UserEntity.builder()
            .username("testuser")
            .email("test@gmail.com")
            .password("password")
            .phone("0123456789")
            .cardId("123456789")
            .name("Test User")
            .isActive(true)
            .role(UserRole.CUSTOMER)
            .build();
    entityManager.persistAndFlush(user);
  }

  @Test
  @DisplayName("findByUsername should return user")
  void findByUsername_ReturnsUser() {
    Optional<UserEntity> found = userRepository.findByUsername("testuser");
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("test@gmail.com");
  }

  @Test
  @DisplayName("exists methods should work correctly")
  void existsMethods_Work() {
    assertThat(userRepository.existsByUsername("testuser")).isTrue();
    assertThat(userRepository.existsByEmail("test@gmail.com")).isTrue();
    assertThat(userRepository.existsByPhone("0123456789")).isTrue();
    assertThat(userRepository.existsByCardId("123456789")).isTrue();
  }

  @Test
  @DisplayName("countByIsActiveIsTrue should count active users")
  void countActive_Works() {
    assertThat(userRepository.countByIsActiveIsTrue()).isEqualTo(1);
  }

  @Test
  @DisplayName("search specification should filter by various fields")
  void search_Works() {
    Specification<UserEntity> spec =
        UserRepository.search("Test", "test@gmail.com", null, null, "test");
    List<UserEntity> results = userRepository.findAll(spec);
    assertThat(results).hasSize(1);

    spec = UserRepository.search("Nonexistent", null, null, null, null);
    results = userRepository.findAll(spec);
    assertThat(results).isEmpty();
  }
}
