package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.OrderEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class OrderRepositoryUT {

  @Autowired private OrderRepository orderRepository;
  @Autowired private TestEntityManager entityManager;

  private UserEntity user;
  private OrderEntity order;

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

    order =
        OrderEntity.builder()
            .user(user)
            .totalAmount(BigDecimal.valueOf(100))
            .status(OrderStatus.PENDING)
            .shippingName("Test Name")
            .shippingPhone("0123456789")
            .shippingAddress("Test Address")
            .build();
    entityManager.persist(order);
    entityManager.flush();
  }

  @Test
  @DisplayName("findByIdAndUserId should return user order")
  void findByIdAndUserId_Works() {
    Optional<OrderEntity> found = orderRepository.findByIdAndUserId(order.getId(), user.getId());
    assertThat(found).isPresent();

    found = orderRepository.findByIdAndUserId(order.getId(), 999L);
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findByUserIdOrderByCreatedAtDesc should return paginated orders")
  void findByUserId_Paginated_Works() {
    Page<OrderEntity> result =
        orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 5));
    assertThat(result.getContent()).hasSize(1);
  }
}
