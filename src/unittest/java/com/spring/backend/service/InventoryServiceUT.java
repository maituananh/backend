package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.domain.enums.ProductStatus;
import com.spring.backend.infrastructure.entity.CartItemEntity;
import com.spring.backend.infrastructure.entity.OrderItemEntity;
import com.spring.backend.infrastructure.entity.ProductEntity;
import com.spring.backend.infrastructure.repository.ProductJpaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceUT {

  @Mock private ProductJpaRepository productRepository;

  @InjectMocks private InventoryService inventoryService;

  @Test
  @DisplayName("reserveStock should work correctly")
  void reserveStock_Works() {
    ProductEntity product = new ProductEntity();
    product.setId(1L);
    product.setStockQty(10);
    product.setReservedQty(2); // available = 8

    CartItemEntity item = new CartItemEntity();
    item.setProduct(product);
    item.setQuantity(5);

    when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));

    inventoryService.reserveStock(List.of(item));

    assertThat(product.getReservedQty()).isEqualTo(7);
    verify(productRepository).save(product);
  }

  @Test
  @DisplayName("reserveStock should fail if product missing in cart item")
  void reserveStock_ProductNullInItem() {
    CartItemEntity item = new CartItemEntity();
    item.setId(100L);

    assertThatThrownBy(() -> inventoryService.reserveStock(List.of(item)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Product not found");
  }

  @Test
  @DisplayName("reserveStock should fail if product not in repo")
  void reserveStock_ProductNotFound() {
    ProductEntity product = new ProductEntity();
    product.setId(1L);
    CartItemEntity item = new CartItemEntity();
    item.setProduct(product);

    when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> inventoryService.reserveStock(List.of(item)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Product not found");
  }

  @Test
  @DisplayName("reserveStock should fail if insufficient stock")
  void reserveStock_InsufficientStock() {
    ProductEntity product = new ProductEntity();
    product.setId(1L);
    product.setStockQty(5);
    product.setReservedQty(0); // available = 5

    CartItemEntity item = new CartItemEntity();
    item.setProduct(product);
    item.setQuantity(10);

    when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));

    assertThatThrownBy(() -> inventoryService.reserveStock(List.of(item)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Insufficient stock");
  }

  @Test
  @DisplayName("deductStock should work correctly")
  void deductStock_Works() {
    ProductEntity product = new ProductEntity();
    product.setId(1L);
    product.setStockQty(10);
    product.setReservedQty(5);

    OrderItemEntity item = new OrderItemEntity();
    item.setProduct(product);
    item.setQuantity(5);

    when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));

    inventoryService.deductStock(List.of(item));

    assertThat(product.getStockQty()).isEqualTo(5);
    assertThat(product.getReservedQty()).isEqualTo(0);
    verify(productRepository).save(product);
  }

  @Test
  @DisplayName("deductStock should update status to SOLD_OUT")
  void deductStock_SoldOut() {
    ProductEntity product = new ProductEntity();
    product.setId(1L);
    product.setStockQty(5);
    product.setReservedQty(5);
    product.setStatus(ProductStatus.NEW);

    OrderItemEntity item = new OrderItemEntity();
    item.setProduct(product);
    item.setQuantity(5);

    when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));

    inventoryService.deductStock(List.of(item));

    assertThat(product.getStockQty()).isEqualTo(0);
    assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
  }

  @Test
  @DisplayName("deductStock should handle missing product gracefully")
  void deductStock_MissingProduct() {
    OrderItemEntity item = new OrderItemEntity();
    item.setProduct(null);

    inventoryService.deductStock(List.of(item)); // Should not throw

    OrderItemEntity item2 = new OrderItemEntity();
    ProductEntity p = new ProductEntity();
    p.setId(1L);
    item2.setProduct(p);
    when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

    inventoryService.deductStock(List.of(item2)); // Should not throw

    verify(productRepository, never()).save(any());
  }

  @Test
  @DisplayName("releaseStock should work correctly")
  void releaseStock_Works() {
    ProductEntity product = new ProductEntity();
    product.setId(1L);
    product.setReservedQty(5);

    OrderItemEntity item = new OrderItemEntity();
    item.setProduct(product);
    item.setQuantity(5);

    when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));

    inventoryService.releaseStock(List.of(item));

    assertThat(product.getReservedQty()).isEqualTo(0);
    verify(productRepository).save(product);
  }

  @Test
  @DisplayName("releaseStock should handle missing product gracefully")
  void releaseStock_MissingProduct() {
    OrderItemEntity item = new OrderItemEntity();
    item.setProduct(null);

    inventoryService.releaseStock(List.of(item)); // Should not throw

    verify(productRepository, never()).save(any());
  }
}
