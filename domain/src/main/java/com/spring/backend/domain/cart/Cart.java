package com.spring.backend.domain.cart;

import com.spring.backend.domain.shared.AggregateRoot;
import com.spring.backend.domain.shared.Money;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Cart extends AggregateRoot {

    private Long customerId;
    private List<CartItem> items;

    private Cart() {
        this.items = new ArrayList<>();
    }

    public static Cart reconstitute(Long id, Long customerId, List<CartItem> items) {
        Cart c = new Cart();
        c.id = id;
        c.customerId = customerId;
        c.items = new ArrayList<>(items);
        return c;
    }

    // Migrated from CartEntity.getTotalAmount — uses Money arithmetic instead of BigDecimal
    public Money getTotalAmount() {
        return items.stream()
            .map(CartItem::getTotalItem)
            .reduce(Money.ZERO, Money::add);
    }

    // Migrated from CartEntity.getCartName — uses CartItem.getProductName() (denormalized field)
    public String getCartName() {
        return items.stream()
            .map(CartItem::getProductName)
            .collect(Collectors.joining(", "));
    }

    public Long getCustomerId() { return customerId; }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
