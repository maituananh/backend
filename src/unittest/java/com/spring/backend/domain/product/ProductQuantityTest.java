package com.spring.backend.domain.product;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductQuantityTest {

  @Test
  @DisplayName("reserve should reduce available qty and increase reserved qty")
  void reserve_reducesAvailableQty() {
    ProductQuantity qty = new ProductQuantity(10, 2);
    ProductQuantity reserved = qty.reserve(5);

    assertThat(reserved.availableQty()).isEqualTo(3);
    assertThat(reserved.getReservedQty()).isEqualTo(7);
    // original is immutable
    assertThat(qty.getReservedQty()).isEqualTo(2);
  }

  @Test
  @DisplayName("reserve should throw when requested qty exceeds available stock")
  void reserve_throwsWhenInsufficientStock() {
    ProductQuantity qty = new ProductQuantity(5, 0);

    assertThatThrownBy(() -> qty.reserve(10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("available");
  }

  @Test
  @DisplayName("release should clamp reserved qty to zero when releasing more than reserved")
  void release_clampsToZero() {
    ProductQuantity qty = new ProductQuantity(5, 2);
    ProductQuantity released = qty.release(5);

    assertThat(released.getReservedQty()).isEqualTo(0);
  }

  @Test
  @DisplayName("deduct should reduce both stock and reserved qty")
  void deduct_reducesStockAndReserved() {
    ProductQuantity qty = new ProductQuantity(10, 3);
    ProductQuantity deducted = qty.deduct(3);

    assertThat(deducted.getStockQty()).isEqualTo(7);
    assertThat(deducted.getReservedQty()).isEqualTo(0);
  }

  @Test
  @DisplayName("constructor should reject negative stock qty")
  void constructor_rejectsNegativeStock() {
    assertThatThrownBy(() -> new ProductQuantity(-1, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("constructor should reject reserved qty exceeding stock qty")
  void constructor_rejectsReservedExceedingStock() {
    assertThatThrownBy(() -> new ProductQuantity(5, 6))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
