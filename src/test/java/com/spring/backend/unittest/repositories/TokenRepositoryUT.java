package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.TokenEntity;
import com.spring.backend.repository.TokenRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class TokenRepositoryUT {

  @Autowired private TokenRepository tokenRepository;
  @Autowired private TestEntityManager entityManager;

  private TokenEntity token;

  @BeforeEach
  void setUp() {
    token = TokenEntity.builder().accessToken("at").refreshToken("rt").build();
    entityManager.persistAndFlush(token);
  }

  @Test
  @DisplayName("findByAccessToken should return token")
  void findByAccessToken_Works() {
    Optional<TokenEntity> found = tokenRepository.findByAccessToken("at");
    assertThat(found).isPresent();
    assertThat(found.get().getRefreshToken()).isEqualTo("rt");
  }

  @Test
  @DisplayName("findByRefreshToken should return token")
  void findByRefreshToken_Works() {
    Optional<TokenEntity> found = tokenRepository.findByRefreshToken("rt");
    assertThat(found).isPresent();
  }
}
