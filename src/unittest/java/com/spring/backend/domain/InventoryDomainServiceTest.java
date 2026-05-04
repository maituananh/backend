package com.spring.backend.domain;

import static org.assertj.core.api.Assertions.*;

import com.spring.backend.domain.enums.ProductStatus;
import com.spring.backend.domain.product.Product;
import com.spring.backend.domain.product.ProductQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryDomainServiceTest {

  private final InventoryDomainService service = new InventoryDomainService();

  @Test
  @DisplayName("reserveStock should delegate to product.reserveQty and increase reserved qty")
  void reserveStock_delegatesToProductReserveQty() {
    Product product =
        Product.reconstitute(
            1L, "watch", 100.0, new ProductQuantity(10, 0), ProductStatus.NEW, true, 1L, 1L);

    service.reserveStock(product, 3);

    assertThat(product.getQuantity().getReservedQty()).isEqualTo(3);
  }

  @Test
  @DisplayName("reserveStock should throw IllegalArgumentException for null product")
  void reserveStock_throwsForNullProduct() {
    assertThatThrownBy(() -> service.reserveStock(null, 1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName(
      "deductStock should delegate to product.deductQty and set SOLD_OUT when stock reaches zero")
  void deductStock_delegatesToProductDeductQty() {
    Product product =
        Product.reconstitute(
            1L, "watch", 100.0, new ProductQuantity(5, 3), ProductStatus.NEW, true, 1L, 1L);

    service.deductStock(product, 5);

    assertThat(product.getQuantity().getStockQty()).isEqualTo(0);
    assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
  }

  @Test
  @DisplayName("releaseStock should delegate to product.releaseQty and reduce reserved qty")
  void releaseStock_delegatesToProductReleaseQty() {
    Product product =
        Product.reconstitute(
            1L, "watch", 100.0, new ProductQuantity(10, 5), ProductStatus.NEW, true, 1L, 1L);

    service.releaseStock(product, 3);

    assertThat(product.getQuantity().getReservedQty()).isEqualTo(2);
  }
}
