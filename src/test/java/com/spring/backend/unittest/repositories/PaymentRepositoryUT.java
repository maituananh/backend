package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.OrderEntity;
import com.spring.backend.entity.PaymentEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.PaymentMethod;
import com.spring.backend.enums.PaymentStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.PaymentRepository;
import java.math.BigDecimal;
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
public class PaymentRepositoryUT {

  @Autowired private PaymentRepository paymentRepository;
  @Autowired private TestEntityManager entityManager;

  private OrderEntity order;
  private PaymentEntity payment;

  @BeforeEach
  void setUp() {
    UserEntity user =
        UserEntity.builder()
            .username("u")
            .password("p")
            .email("e@e.com")
            .phone("1")
            .cardId("1")
            .role(UserRole.CUSTOMER)
            .build();
    order =
        OrderEntity.builder()
            .user(user)
            .totalAmount(BigDecimal.valueOf(100))
            .shippingName("Test Name")
            .shippingPhone("0123456789")
            .shippingAddress("Test Address")
            .build();

    entityManager.persist(user);
    entityManager.persist(order);

    payment =
        PaymentEntity.builder()
            .order(order)
            .paymentMethod(PaymentMethod.STRIPE)
            .status(PaymentStatus.PENDING)
            .transactionId("tx_123")
            .amount(BigDecimal.valueOf(100))
            .build();
    entityManager.persist(payment);
    entityManager.flush();
  }

  @Test
  @DisplayName("findByOrderId should return payment")
  void findByOrderId_Works() {
    Optional<PaymentEntity> found = paymentRepository.findByOrderId(order.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getTransactionId()).isEqualTo("tx_123");
  }

  @Test
  @DisplayName("findByTransactionId should return payment")
  void findByTransactionId_Works() {
    Optional<PaymentEntity> found = paymentRepository.findByTransactionId("tx_123");
    assertThat(found).isPresent();
  }
}
