package com.spring.backend.infrastructure.mapper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for OrderMapper — covers INFRA-01 mapping correctness. Stubs filled in after Plan
 * 09-03 creates the mapper.
 */
class OrderMapperUT {

  @Test
  @Disabled("Stub — implemented after OrderMapper is created in Plan 09-03")
  void toDomain_constructsShippingAddressFromFlatEntityFields() {
    // TODO: entity with shippingName="Alice", shippingPhone="0900", shippingAddress="123 St"
    // when: mapper.toDomain(entity)
    // then: domain Order has shippingAddress.name=="Alice",
    //       shippingAddress.phone=="0900", shippingAddress.address=="123 St"
  }

  @Test
  @Disabled("Stub — implemented after OrderMapper is created in Plan 09-03")
  void toDomain_mapsOrderItemsFromEntityCollection() {
    // TODO: entity with 2 OrderItemEntity items
    // then: domain Order.items() has 2 OrderItem objects
  }

  @Test
  @Disabled("Stub — implemented after OrderMapper is created in Plan 09-03")
  void toDomain_convertsOrderStatusEnum() {
    // TODO: entity OrderStatus.PENDING → domain OrderStatus.PENDING
  }
}
