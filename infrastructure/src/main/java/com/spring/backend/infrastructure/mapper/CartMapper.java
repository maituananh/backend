package com.spring.backend.infrastructure.mapper;

import com.spring.backend.domain.cart.Cart;
import com.spring.backend.domain.cart.CartItem;
import com.spring.backend.domain.shared.Money;
import com.spring.backend.infrastructure.entity.CartEntity;
import com.spring.backend.infrastructure.entity.CartItemEntity;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @BeanMapping(ignoreByDefault = true)
    Cart toDomain(CartEntity entity);

    @ObjectFactory
    default Cart createCart(CartEntity entity) {
        List<CartItem> items = entity.getItems() == null
            ? List.of()
            : entity.getItems().stream()
                .map(this::toCartItem)
                .toList();
        return Cart.reconstitute(
            entity.getId(),
            entity.getCustomer() != null ? entity.getCustomer().getId() : null,
            items
        );
    }

    @BeanMapping(ignoreByDefault = true)
    CartItem toCartItem(CartItemEntity entity);

    @ObjectFactory
    default CartItem createCartItem(CartItemEntity e) {
        return CartItem.reconstitute(
            e.getId(),
            e.getProduct() != null ? e.getProduct().getId() : null,
            e.getProduct() != null ? e.getProduct().getName() : null,
            new Money(e.getPrice()),
            e.getQuantity(),
            e.getStatus()
        );
    }
}
