package com.spring.backend.domain.product;

import static org.assertj.core.api.Assertions.*;

import com.spring.backend.domain.enums.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

  @Test
  @DisplayName("reserveQty should delegate to ProductQuantity and increase reserved qty")
  void reserveQty_delegatesToProductQuantity() {
    Product product =
        Product.reconstitute(
            1L, "watch", 100.0, new ProductQuantity(10, 2), ProductStatus.NEW, true, 1L, 1L);

    product.reserveQty(5);

    assertThat(product.getQuantity().getReservedQty()).isEqualTo(7);
  }

  @Test
  @DisplayName("deductQty should set SOLD_OUT status when stock reaches zero")
  void deductQty_setsSoldOutWhenStockReachesZero() {
    Product product =
        Product.reconstitute(
            1L, "watch", 100.0, new ProductQuantity(3, 0), ProductStatus.NEW, true, 1L, 1L);

    product.deductQty(3);

    assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
  }

  @Test
  @DisplayName("deductQty should not set SOLD_OUT when stock remains after deduction")
  void deductQty_doesNotSetSoldOutWhenStockRemains() {
    Product product =
        Product.reconstitute(
            1L, "watch", 100.0, new ProductQuantity(10, 0), ProductStatus.NEW, true, 1L, 1L);

    product.deductQty(3);

    assertThat(product.getStatus()).isNotEqualTo(ProductStatus.SOLD_OUT);
  }
}
