package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.*;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.OrderItemRepository;
import java.math.BigDecimal;
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
public class OrderItemRepositoryUT {

  @Autowired private OrderItemRepository orderItemRepository;
  @Autowired private TestEntityManager entityManager;

  private OrderEntity order;
  private ProductEntity product;
  private OrderItemEntity orderItem;

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
    entityManager.persist(user);

    category = CategoryEntity.builder().name("C").isActive(true).build();
    entityManager.persist(category);

    product = ProductEntity.builder().name("P").price(100.0).availableQty(10).build();
    entityManager.persist(product);

    order =
        OrderEntity.builder()
            .user(user)
            .totalAmount(BigDecimal.valueOf(100))
            .status(OrderStatus.PENDING)
            .shippingName("N")
            .shippingPhone("P")
            .shippingAddress("A")
            .build();
    entityManager.persist(order);

    orderItem =
        OrderItemEntity.builder()
            .order(order)
            .product(product)
            .productName("P")
            .unitPrice(BigDecimal.valueOf(100))
            .quantity(1)
            .subtotal(BigDecimal.valueOf(100))
            .build();
    entityManager.persist(orderItem);
    entityManager.flush();
  }

  private CategoryEntity category;

  @Test
  @DisplayName("findByOrderId should return items for order")
  void findByOrderId_Works() {
    List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
    assertThat(items).hasSize(1);
    assertThat(items.get(0).getProductName()).isEqualTo("P");
  }
}
