package com.spring.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.spring.backend.config.IntegrationTest;
import com.spring.backend.entity.CartEntity;
import com.spring.backend.entity.CartItemEntity;
import com.spring.backend.entity.OrderItemEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.CartItemStatus;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Inventory Service Integration Tests")
@IntegrationTest
class InventoryServiceIT {

  @Autowired private InventoryService inventoryService;
  @Autowired private ProductRepository productRepository;

  @BeforeEach
  void cleanUp() {
    productRepository.deleteAll();
  }

  @Test
  @DisplayName("validateStock allows orders when inventory is enough")
  void validateStock_allowsWhenInventorySufficient() {
    ProductEntity product = saveProduct(ProductStatus.NEW, 10, 0);
    CartItemEntity cartItem = createCartItem(product, 2);

    assertDoesNotThrow(() -> inventoryService.validateStock(List.of(cartItem)));

    ProductEntity refreshed = productRepository.findById(product.getId()).orElseThrow();
    assertEquals(10, refreshed.getAvailableQty());
  }

  @Test
  @DisplayName("validateStock fails when requested quantity exceeds availability")
  void validateStock_throwsWhenInventoryMissing() {
    ProductEntity product = saveProduct(ProductStatus.NEW, 1, 0);
    CartItemEntity cartItem = createCartItem(product, 2);

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> inventoryService.validateStock(List.of(cartItem)),
            "Should block checkout when stock is insufficient");

    assertEquals(
        "Insufficient stock for product 'Test product': available=1, requested=2",
        thrown.getMessage());
  }

  @Test
  @DisplayName("deductStock drains reserved qty and marks product sold out when stock reaches zero")
  void deductStock_reducesStockAndResetsReserved() {
    ProductEntity product = saveProduct(ProductStatus.NEW, 2, 2);
    OrderItemEntity orderItem = createOrderItem(product, 2);

    inventoryService.deductStock(List.of(orderItem));

    ProductEntity refreshed = productRepository.findById(product.getId()).orElseThrow();
    assertEquals(0, refreshed.getStockQty());
    assertEquals(0, refreshed.getReservedQty());
    assertEquals(ProductStatus.SOLD_OUT, refreshed.getStatus());
  }

  @Test
  @DisplayName("releaseStock releases reserved units without touching actual stock")
  void releaseStock_decreasesReservedOnly() {
    ProductEntity product = saveProduct(ProductStatus.NEW, 5, 3);
    OrderItemEntity orderItem = createOrderItem(product, 2);

    inventoryService.releaseStock(List.of(orderItem));

    ProductEntity refreshed = productRepository.findById(product.getId()).orElseThrow();
    assertEquals(1, refreshed.getReservedQty());
    assertEquals(4, refreshed.getAvailableQty());
    assertEquals(5, refreshed.getStockQty());
  }

  @Test
  @DisplayName("validateStock throws when cart item lacks a product")
  void validateStock_throwsWhenCartItemHasNoProduct() {
    CartItemEntity cartItem = createCartItem(null, 1);
    cartItem.setId(42L);

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> inventoryService.validateStock(List.of(cartItem)),
            "Should fail when product is missing");

    assertEquals("Product not found for cart item id=42", thrown.getMessage());
  }

  @Test
  @DisplayName("deductStock tolerates missing product entries and processes the rest")
  void deductStock_skipsMissingProductEntries() {
    ProductEntity product = saveProduct(ProductStatus.NEW, 5, 5);
    OrderItemEntity validItem = createOrderItem(product, 1);
    OrderItemEntity missingProductItem = createMissingProductOrderItem(99L, 1);

    assertDoesNotThrow(() -> inventoryService.deductStock(List.of(validItem, missingProductItem)));

    ProductEntity refreshed = productRepository.findById(product.getId()).orElseThrow();
    assertEquals(4, refreshed.getStockQty());
    assertEquals(4, refreshed.getReservedQty());
  }

  @Test
  @DisplayName("releaseStock tolerates missing product entries")
  void releaseStock_skipsMissingProductEntries() {
    ProductEntity product = saveProduct(ProductStatus.NEW, 5, 3);
    OrderItemEntity validItem = createOrderItem(product, 2);
    OrderItemEntity missingProductItem = createMissingProductOrderItem(100L, 1);

    assertDoesNotThrow(() -> inventoryService.releaseStock(List.of(validItem, missingProductItem)));

    ProductEntity refreshed = productRepository.findById(product.getId()).orElseThrow();
    assertEquals(1, refreshed.getReservedQty());
  }

  private CartItemEntity createCartItem(ProductEntity product, int quantity) {
    CartEntity cart = CartEntity.builder().customer(createUser()).build();
    CartItemEntity cartItem = new CartItemEntity();
    cartItem.setCart(cart);
    cartItem.setProduct(product);
    cartItem.setQuantity(quantity);
    cartItem.setPrice(BigDecimal.valueOf(100));
    cartItem.setStatus(CartItemStatus.PENDING);
    return cartItem;
  }

  private OrderItemEntity createOrderItem(ProductEntity product, int quantity) {
    OrderItemEntity orderItem = new OrderItemEntity();
    orderItem.setProduct(product);
    orderItem.setQuantity(quantity);
    orderItem.setUnitPrice(BigDecimal.valueOf(100));
    orderItem.setSubtotal(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(quantity)));
    return orderItem;
  }

  private OrderItemEntity createMissingProductOrderItem(Long id, int quantity) {
    OrderItemEntity orderItem = createOrderItem(null, quantity);
    orderItem.setId(id);
    return orderItem;
  }

  private ProductEntity saveProduct(ProductStatus status, int stockQty, int reservedQty) {
    ProductEntity product =
        ProductEntity.builder().name("Test product").price(100).status(status).build();
    product.setStockQty(stockQty);
    product.setReservedQty(reservedQty);
    return productRepository.save(product);
  }

  private UserEntity createUser() {
    return UserEntity.builder()
        .username("inventory-user")
        .password("password")
        .email("inventory@example.com")
        .cardId("CARD-123")
        .phone("0900000000")
        .role(UserRole.CUSTOMER)
        .build();
  }
}
