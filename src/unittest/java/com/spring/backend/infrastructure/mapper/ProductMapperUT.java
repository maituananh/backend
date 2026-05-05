package com.spring.backend.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.domain.enums.ProductStatus;
import com.spring.backend.domain.product.Product;
import com.spring.backend.domain.product.ProductQuantity;
import com.spring.backend.infrastructure.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductMapperUT {

  private ProductMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ProductMapperImpl();
  }

  @Test
  void toDomain_constructsProductQuantityFromStockAndReservedQty() {
    ProductEntity entity = new ProductEntity();
    entity.setStockQty(10);
    entity.setReservedQty(3);
    entity.setName("Test");
    entity.setPrice(100.0);

    Product product = mapper.toDomain(entity);

    assertThat(product.getQuantity().getStockQty()).isEqualTo(10);
    assertThat(product.getQuantity().getReservedQty()).isEqualTo(3);
    assertThat(product.getQuantity().availableQty()).isEqualTo(7);
  }

  @Test
  void toDomain_convertsProductStatusEnum() {
    ProductEntity entity = new ProductEntity();
    entity.setStatus(ProductStatus.NEW);
    entity.setStockQty(1);
    entity.setReservedQty(0);

    Product product = mapper.toDomain(entity);

    assertThat(product.getStatus()).isEqualTo(ProductStatus.NEW);
  }

  @Test
  void toEntity_setsAvailableQtyFromQuantityComputed() {
    ProductQuantity quantity = new ProductQuantity(8, 2);
    Product product =
        Product.reconstitute(1L, "Test", 99.0, quantity, ProductStatus.NEW, true, null, null);

    ProductEntity entity = mapper.toEntity(product);

    assertThat(entity.getAvailableQty()).isEqualTo(6);
    assertThat(entity.getStockQty()).isEqualTo(8);
    assertThat(entity.getReservedQty()).isEqualTo(2);
  }
}
