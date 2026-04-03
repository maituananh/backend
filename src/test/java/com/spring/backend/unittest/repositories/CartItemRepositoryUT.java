package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.*;
import com.spring.backend.enums.CartItemStatus;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.CartItemRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
public class CartItemRepositoryUT {

  @Autowired private CartItemRepository cartItemRepository;
  @Autowired private TestEntityManager entityManager;

  private CartEntity cart;
  private ProductEntity product;
  private UserEntity user;
  private CategoryEntity category;

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
    category = CategoryEntity.builder().name("C").isActive(true).build();
    product =
        ProductEntity.builder()
            .name("P")
            .price(100.0)
            .status(ProductStatus.NEW)
            .isActived(true)
            .customer(user)
            .category(category)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(1))
            .build();
    cart = CartEntity.builder().customer(user).build();

    entityManager.persist(user);
    entityManager.persist(category);
    entityManager.persist(product);
    entityManager.persist(cart);
    entityManager.flush();
  }

  @Test
  @DisplayName("findByCartIdAndProductId should return item")
  void findByCartIdAndProductId_Works() {
    CartItemEntity item =
        CartItemEntity.builder()
            .cart(cart)
            .product(product)
            .quantity(1)
            .price(BigDecimal.valueOf(100))
            .status(CartItemStatus.PENDING)
            .build();
    entityManager.persist(item);
    entityManager.flush();

    Optional<CartItemEntity> found =
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
    assertThat(found).isPresent();
  }

  @Test
  @DisplayName("updatePriceByProductId should update prices")
  void updatePrice_Works() {
    CartItemEntity item =
        CartItemEntity.builder()
            .cart(cart)
            .product(product)
            .quantity(1)
            .price(BigDecimal.valueOf(100))
            .status(CartItemStatus.PENDING)
            .build();
    entityManager.persist(item);
    entityManager.flush();

    cartItemRepository.updatePriceByProductId(product.getId(), BigDecimal.valueOf(150));
    entityManager.clear(); // Clear cache to get fresh from DB

    CartItemEntity updated = cartItemRepository.findById(item.getId()).orElseThrow();
    assertThat(updated.getPrice().doubleValue()).isEqualTo(150.0);
  }

  @Test
  @DisplayName("deleteByCartCustomerIdAndProductIdIn should remove items")
  void batchDelete_Works() {
    CartItemEntity item =
        CartItemEntity.builder()
            .cart(cart)
            .product(product)
            .quantity(1)
            .price(BigDecimal.valueOf(100))
            .status(CartItemStatus.PENDING)
            .build();
    entityManager.persist(item);
    entityManager.flush();

    cartItemRepository.deleteByCartCustomerIdAndProductIdIn(user.getId(), List.of(product.getId()));
    entityManager.flush();
    entityManager.clear();

    assertThat(cartItemRepository.findById(item.getId())).isEmpty();
  }
}
