package com.spring.backend.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.domain.enums.OrderStatus;
import com.spring.backend.domain.enums.PaymentMethod;
import com.spring.backend.domain.enums.UserRole;
import com.spring.backend.infrastructure.entity.OrderEntity;
import com.spring.backend.infrastructure.entity.PaymentEntity;
import com.spring.backend.infrastructure.entity.UserEntity;
import com.spring.backend.infrastructure.repository.OrderJpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

  @Autowired private OrderJpaRepository orderRepository;
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

  @org.junit.jupiter.api.Nested
  @DisplayName("findStuckPendingOrders")
  class FindStuckPendingOrdersTests {

    private Instant lookbackCutoff;
    private Instant minAgeCutoff;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
      lookbackCutoff = Instant.now().minus(24, ChronoUnit.HOURS);
      minAgeCutoff = Instant.now().minus(5, ChronoUnit.MINUTES);
    }

    private OrderEntity persistOrderWithPayment(
        OrderStatus status, String transactionId, long minutesAgo) {
      OrderEntity o =
          OrderEntity.builder()
              .user(user)
              .totalAmount(BigDecimal.valueOf(100))
              .status(status)
              .shippingName("N")
              .shippingPhone("0")
              .shippingAddress("A")
              .build();
      entityManager.persist(o);
      entityManager.flush();

      // Force createdAt — @CreatedDate sets Instant.now() on persist, so override via native SQL
      Instant targetCreatedAt = Instant.now().minus(minutesAgo, ChronoUnit.MINUTES);
      entityManager
          .getEntityManager()
          .createNativeQuery("UPDATE orders SET created_at = ? WHERE id = ?")
          .setParameter(1, java.sql.Timestamp.from(targetCreatedAt))
          .setParameter(2, o.getId())
          .executeUpdate();
      entityManager.clear();

      PaymentEntity p =
          PaymentEntity.builder()
              .order(entityManager.find(OrderEntity.class, o.getId()))
              .amount(BigDecimal.valueOf(100))
              .paymentMethod(PaymentMethod.STRIPE)
              .transactionId(transactionId)
              .build();
      entityManager.persist(p);
      entityManager.flush();
      return entityManager.find(OrderEntity.class, o.getId());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("order in 5min-to-24h window with transactionId — is returned")
    void orderInWindow_isReturned() {
      OrderEntity stuck = persistOrderWithPayment(OrderStatus.PENDING, "sess_stuck", 10);

      List<OrderEntity> result =
          orderRepository.findStuckPendingOrders(lookbackCutoff, minAgeCutoff);

      assertThat(result).extracting(OrderEntity::getId).contains(stuck.getId());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("order under 5 minutes old — excluded (below minAgeCutoff)")
    void orderTooNew_excluded() {
      persistOrderWithPayment(OrderStatus.PENDING, "sess_new", 2);

      List<OrderEntity> result =
          orderRepository.findStuckPendingOrders(lookbackCutoff, minAgeCutoff);

      assertThat(result).isEmpty();
    }

    @org.junit.jupiter.api.Test
    @DisplayName("order over 24 hours old — excluded (exceeds lookback window)")
    void orderTooOld_excluded() {
      persistOrderWithPayment(OrderStatus.PENDING, "sess_old", 60 * 25); // 25 hours

      List<OrderEntity> result =
          orderRepository.findStuckPendingOrders(lookbackCutoff, minAgeCutoff);

      assertThat(result).isEmpty();
    }

    @org.junit.jupiter.api.Test
    @DisplayName("non-PENDING order — excluded regardless of age")
    void nonPendingOrder_excluded() {
      persistOrderWithPayment(OrderStatus.CONFIRMED, "sess_conf", 10);

      List<OrderEntity> result =
          orderRepository.findStuckPendingOrders(lookbackCutoff, minAgeCutoff);

      assertThat(result).isEmpty();
    }

    @org.junit.jupiter.api.Test
    @DisplayName("null transactionId — excluded (no Stripe session to check)")
    void nullTransactionId_excluded() {
      persistOrderWithPayment(OrderStatus.PENDING, null, 10);

      List<OrderEntity> result =
          orderRepository.findStuckPendingOrders(lookbackCutoff, minAgeCutoff);

      assertThat(result).isEmpty();
    }

    @org.junit.jupiter.api.Test
    @DisplayName("multiple stuck orders — all returned")
    void multipleStuckOrders_allReturned() {
      OrderEntity a = persistOrderWithPayment(OrderStatus.PENDING, "sess_a", 10);
      OrderEntity b = persistOrderWithPayment(OrderStatus.PENDING, "sess_b", 15);

      List<OrderEntity> result =
          orderRepository.findStuckPendingOrders(lookbackCutoff, minAgeCutoff);

      assertThat(result)
          .extracting(OrderEntity::getId)
          .containsExactlyInAnyOrder(a.getId(), b.getId());
    }
  }
}
