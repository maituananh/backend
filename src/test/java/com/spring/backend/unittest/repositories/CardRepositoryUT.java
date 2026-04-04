package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.CardEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.CardRepository;
import java.util.List;
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
public class CardRepositoryUT {

  @Autowired private CardRepository cardRepository;
  @Autowired private TestEntityManager entityManager;

  private UserEntity user;
  private CardEntity card;

  @BeforeEach
  void setUp() {
    user =
        UserEntity.builder()
            .username("u")
            .password("p")
            .email("e@e.com")
            .phone("1")
            .cardId("1")
            .role(UserRole.CUSTOMER)
            .build();
    entityManager.persist(user);

    card = CardEntity.builder().customer(user).numberOfCard("12345").build();
    entityManager.persist(card);
    entityManager.flush();
  }

  @Test
  @DisplayName("findByCustomerId should return cards for customer")
  void findByCustomerId_Works() {
    List<CardEntity> cards = cardRepository.findByCustomerId(user.getId());
    assertThat(cards).hasSize(1);
    assertThat(cards.get(0).getNumberOfCard()).isEqualTo("12345");
  }
}
