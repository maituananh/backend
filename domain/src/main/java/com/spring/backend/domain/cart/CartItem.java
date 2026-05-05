package com.spring.backend.domain.cart;

import com.spring.backend.domain.enums.CartItemStatus;
import com.spring.backend.domain.shared.AggregateRoot;
import com.spring.backend.domain.shared.Money;

public class CartItem extends AggregateRoot {

    private Long productId;
    private String productName;
    private Money price;
    private Integer quantity;
    private CartItemStatus status;

    private CartItem() {}

    public static CartItem reconstitute(Long id, Long productId, String productName,
                                        Money price, Integer quantity, CartItemStatus status) {
        CartItem c = new CartItem();
        c.id = id;
        c.productId = productId;
        c.productName = productName;
        c.price = price;
        c.quantity = quantity;
        c.status = status;
        return c;
    }

    // Migrated from CartItemEntity.getTotalItem
    public Money getTotalItem() {
        return price.multiply(quantity);
    }

    public Long getProductId()       { return productId; }
    public String getProductName()   { return productName; }
    public Money getPrice()          { return price; }
    public Integer getQuantity()     { return quantity; }
    public CartItemStatus getStatus(){ return status; }
}
