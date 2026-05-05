package com.spring.backend.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.config.IntegrationTest;
import com.spring.backend.domain.card.CardRepository;
import com.spring.backend.domain.cart.CartRepository;
import com.spring.backend.domain.category.CategoryRepository;
import com.spring.backend.domain.order.OrderRepository;
import com.spring.backend.domain.payment.PaymentRepository;
import com.spring.backend.domain.product.ProductRepository;
import com.spring.backend.domain.user.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Smoke test: Spring context loads with all 7 domain repository adapters wired. Covers INFRA-01
 * integration requirement.
 *
 * <p>Class-level @Disabled removed in Plan 09-04 — all 7 adapters now exist. Method-level @Disabled
 * remains until a full bootRun with database is verified in CI (database required
 * for @SpringBootTest to start the full application context).
 */
@IntegrationTest
class InfrastructureContextIT {

  @Autowired ProductRepository productRepository;

  @Autowired CartRepository cartRepository;

  @Autowired OrderRepository orderRepository;

  @Autowired UserRepository userRepository;

  @Autowired PaymentRepository paymentRepository;

  @Autowired CategoryRepository categoryRepository;

  @Autowired CardRepository cardRepository;

  @Test
  @Disabled("Requires running database — enable after bootRun verified in CI (Plan 09-04)")
  void contextLoads_allDomainRepositoryAdaptersAreWired() {
    assertThat(productRepository).isNotNull();
    assertThat(cartRepository).isNotNull();
    assertThat(orderRepository).isNotNull();
    assertThat(userRepository).isNotNull();
    assertThat(paymentRepository).isNotNull();
    assertThat(categoryRepository).isNotNull();
    assertThat(cardRepository).isNotNull();
  }
}
