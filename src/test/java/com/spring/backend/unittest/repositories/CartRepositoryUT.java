package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.CartEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.CartRepository;
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
public class CartRepositoryUT {

  @Autowired private CartRepository cartRepository;
  @Autowired private TestEntityManager entityManager;

  private UserEntity user;
  private CartEntity cart;

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

    cart = CartEntity.builder().customer(user).build();
    entityManager.persist(cart);
    entityManager.flush();
  }

  @Test
  @DisplayName("findByCustomerId should return user cart")
  void findByCustomerId_ReturnsCart() {
    Optional<CartEntity> found = cartRepository.findByCustomerId(user.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getCustomer().getId()).isEqualTo(user.getId());
  }
}
