package com.spring.backend.infrastructure.mapper;

import com.spring.backend.domain.order.Order;
import com.spring.backend.domain.order.OrderItem;
import com.spring.backend.domain.order.ShippingAddress;
import com.spring.backend.domain.shared.Money;
import com.spring.backend.infrastructure.entity.OrderEntity;
import com.spring.backend.infrastructure.entity.OrderItemEntity;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @BeanMapping(ignoreByDefault = true)
    Order toDomain(OrderEntity entity);

    @ObjectFactory
    default Order createOrder(OrderEntity entity) {
        ShippingAddress address = new ShippingAddress(
            entity.getShippingName(),
            entity.getShippingPhone(),
            entity.getShippingAddress()
        );
        Money total = new Money(entity.getTotalAmount());
        List<OrderItem> items = entity.getItems() == null
            ? List.of()
            : entity.getItems().stream()
                .map(this::toOrderItem)
                .toList();
        return Order.reconstitute(
            entity.getId(),
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getStatus(),
            total,
            entity.getNote(),
            address,
            items
        );
    }

    @BeanMapping(ignoreByDefault = true)
    OrderItem toOrderItem(OrderItemEntity entity);

    @ObjectFactory
    default OrderItem createOrderItem(OrderItemEntity e) {
        return OrderItem.reconstitute(
            e.getId(),
            e.getProduct() != null ? e.getProduct().getId() : null,
            e.getProductName(),
            e.getProductImage(),
            new Money(e.getUnitPrice()),
            e.getQuantity(),
            new Money(e.getSubtotal())
        );
    }
}
