package com.spring.backend.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.domain.enums.OrderStatus;
import com.spring.backend.domain.order.Order;
import com.spring.backend.infrastructure.entity.OrderEntity;
import com.spring.backend.infrastructure.entity.OrderItemEntity;
import com.spring.backend.infrastructure.entity.ProductEntity;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderMapperUT {

  private OrderMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new OrderMapperImpl();
  }

  @Test
  void toDomain_constructsShippingAddressFromFlatEntityFields() {
    OrderEntity entity = new OrderEntity();
    entity.setShippingName("Alice");
    entity.setShippingPhone("0900000000");
    entity.setShippingAddress("123 St");
    entity.setTotalAmount(BigDecimal.valueOf(100));
    entity.setStatus(OrderStatus.PENDING);

    Order order = mapper.toDomain(entity);

    assertThat(order.getShippingAddress().getName()).isEqualTo("Alice");
    assertThat(order.getShippingAddress().getPhone()).isEqualTo("0900000000");
    assertThat(order.getShippingAddress().getAddress()).isEqualTo("123 St");
  }

  @Test
  void toDomain_mapsOrderItemsFromEntityCollection() {
    ProductEntity product = new ProductEntity();
    product.setId(1L);

    OrderItemEntity item1 = new OrderItemEntity();
    item1.setProduct(product);
    item1.setProductName("Item A");
    item1.setUnitPrice(BigDecimal.valueOf(50));
    item1.setQuantity(1);
    item1.setSubtotal(BigDecimal.valueOf(50));

    OrderItemEntity item2 = new OrderItemEntity();
    item2.setProduct(product);
    item2.setProductName("Item B");
    item2.setUnitPrice(BigDecimal.valueOf(30));
    item2.setQuantity(2);
    item2.setSubtotal(BigDecimal.valueOf(60));

    OrderEntity entity = new OrderEntity();
    entity.setShippingName("Bob");
    entity.setShippingPhone("0911111111");
    entity.setShippingAddress("456 Ave");
    entity.setTotalAmount(BigDecimal.valueOf(110));
    entity.setStatus(OrderStatus.PENDING);
    entity.setItems(List.of(item1, item2));

    Order order = mapper.toDomain(entity);

    assertThat(order.getItems()).hasSize(2);
  }

  @Test
  void toDomain_convertsOrderStatusEnum() {
    OrderEntity entity = new OrderEntity();
    entity.setShippingName("Carol");
    entity.setShippingPhone("0922222222");
    entity.setShippingAddress("789 Blvd");
    entity.setTotalAmount(BigDecimal.valueOf(200));
    entity.setStatus(OrderStatus.PENDING);

    Order order = mapper.toDomain(entity);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
  }
}
