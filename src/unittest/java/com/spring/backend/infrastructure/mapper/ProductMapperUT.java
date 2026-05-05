package com.spring.backend.infrastructure.mapper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ProductMapper — covers INFRA-01 mapping correctness. Stubs filled in after Plan
 * 09-03 creates the mapper.
 */
class ProductMapperUT {

  @Test
  @Disabled("Stub — implemented after ProductMapper is created in Plan 09-03")
  void toDomain_constructsProductQuantityFromStockAndReservedQty() {
    // TODO: build a ProductEntity with stockQty=10, reservedQty=3
    // when: mapper.toDomain(entity)
    // then: domain Product has quantity.stockQty==10, quantity.reservedQty==3,
    //       quantity.availableQty()==7
  }

  @Test
  @Disabled("Stub — implemented after ProductMapper is created in Plan 09-03")
  void toDomain_convertsProductStatusEnum() {
    // TODO: entity with ProductStatus.AVAILABLE → domain ProductStatus.AVAILABLE
  }

  @Test
  @Disabled("Stub — implemented after ProductMapper is created in Plan 09-03")
  void toEntity_setsAvailableQtyFromQuantityComputed() {
    // TODO: domain product with quantity(8, 2) → entity.availableQty == 6
  }
}
