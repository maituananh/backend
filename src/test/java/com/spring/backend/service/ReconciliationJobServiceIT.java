package com.spring.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.spring.backend.adapter.stripe.StripeAdapter;
import com.spring.backend.entity.OrderEntity;
import com.spring.backend.entity.OrderItemEntity;
import com.spring.backend.entity.PaymentEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.PaymentMethod;
import com.spring.backend.enums.PaymentStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.job.ReconciliationJobService;
import com.spring.backend.repository.OrderItemRepository;
import com.spring.backend.repository.OrderRepository;
import com.spring.backend.repository.PaymentRepository;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class ReconciliationJobServiceIT {

  @Autowired private ReconciliationJobService reconciliationJobService;

  @Autowired private OrderRepository orderRepository;

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ProductRepository productRepository;

  @Autowired private OrderItemRepository orderItemRepository;

  @MockitoBean private StripeAdapter stripeAdapter;

  private UserEntity user;
  private ProductEntity product;

  @BeforeEach
  void setUp() {
    user =
        userRepository.save(
            UserEntity.builder()
                .email("test@example.com")
                .password("hash")
                .role(UserRole.CUSTOMER)
                .build());

    product =
        productRepository.save(
            ProductEntity.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .build());
  }

  @AfterEach
  void tearDown() {
    orderItemRepository.deleteAll();
    paymentRepository.deleteAll();
    orderRepository.deleteAll();
    productRepository.deleteAll();
    userRepository.deleteAll();
  }

  private OrderEntity createStuckOrder(String transactionId, int minutesAgo) {
    OrderEntity order =
        orderRepository.save(
            OrderEntity.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100))
                .createdAt(Instant.now().minus(minutesAgo, ChronoUnit.MINUTES))
                .build());

    orderItemRepository.save(
        OrderItemEntity.builder()
            .order(order)
            .product(product)
            .quantity(1)
            .unitPrice(BigDecimal.valueOf(100))
            .subtotal(BigDecimal.valueOf(100))
            .build());

    paymentRepository.save(
        PaymentEntity.builder()
            .order(order)
            .amount(BigDecimal.valueOf(100))
            .paymentMethod(PaymentMethod.CARD)
            .status(PaymentStatus.PENDING)
            .transactionId(transactionId)
            .build());

    return order;
  }

  @Test
  void reconcileStuckOrders_updatesCompleteAndExpiredSessions() throws Exception {
    OrderEntity orderComplete = createStuckOrder("cs_test_complete", 10);
    OrderEntity orderExpired = createStuckOrder("cs_test_expired", 10);
    OrderEntity orderTooNew = createStuckOrder("cs_test_new", 2); // Should be ignored (under 5m)
    OrderEntity orderTooOld =
        createStuckOrder("cs_test_old", 60 * 25); // Should be ignored (over 24h)

    Session sessionComplete = new Session();
    sessionComplete.setStatus("complete");
    when(stripeAdapter.retrieveSession("cs_test_complete")).thenReturn(sessionComplete);

    Session sessionExpired = new Session();
    sessionExpired.setStatus("expired");
    when(stripeAdapter.retrieveSession("cs_test_expired")).thenReturn(sessionExpired);

    reconciliationJobService.reconcileStuckOrders();

    OrderEntity fetchedComplete = orderRepository.findById(orderComplete.getId()).orElseThrow();
    PaymentEntity paymentComplete =
        paymentRepository.findByOrderId(orderComplete.getId()).orElseThrow();
    assertThat(fetchedComplete.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    assertThat(paymentComplete.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    assertThat(paymentComplete.getPaidAt()).isNotNull();

    OrderEntity fetchedExpired = orderRepository.findById(orderExpired.getId()).orElseThrow();
    PaymentEntity paymentExpired =
        paymentRepository.findByOrderId(orderExpired.getId()).orElseThrow();
    assertThat(fetchedExpired.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    assertThat(paymentExpired.getStatus()).isEqualTo(PaymentStatus.FAILED);

    OrderEntity fetchedNew = orderRepository.findById(orderTooNew.getId()).orElseThrow();
    assertThat(fetchedNew.getStatus()).isEqualTo(OrderStatus.PENDING);

    OrderEntity fetchedOld = orderRepository.findById(orderTooOld.getId()).orElseThrow();
    assertThat(fetchedOld.getStatus()).isEqualTo(OrderStatus.PENDING);
  }

  @Test
  void reconcileStuckOrders_transactionIsolation_oneFailureDoesNotRollbackOthers()
      throws Exception {
    OrderEntity orderFail = createStuckOrder("cs_test_fail", 10);
    OrderEntity orderSuccess = createStuckOrder("cs_test_success", 10);

    when(stripeAdapter.retrieveSession("cs_test_fail"))
        .thenThrow(new RuntimeException("Stripe API Timeout"));

    Session sessionComplete = new Session();
    sessionComplete.setStatus("complete");
    when(stripeAdapter.retrieveSession("cs_test_success")).thenReturn(sessionComplete);

    reconciliationJobService.reconcileStuckOrders();

    OrderEntity fetchedFail = orderRepository.findById(orderFail.getId()).orElseThrow();
    assertThat(fetchedFail.getStatus()).isEqualTo(OrderStatus.PENDING);

    OrderEntity fetchedSuccess = orderRepository.findById(orderSuccess.getId()).orElseThrow();
    assertThat(fetchedSuccess.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
  }
}
