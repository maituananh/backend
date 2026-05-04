package com.spring.backend.domain.order;

import com.spring.backend.domain.shared.AggregateRoot;
import com.spring.backend.domain.shared.Money;

public class OrderItem extends AggregateRoot {

    private Long productId;
    private String productName;
    private String productImage;
    private Money unitPrice;
    private Integer quantity;
    private Money subtotal;

    private OrderItem() {}

    public static OrderItem reconstitute(Long id, Long productId, String productName,
                                         String productImage, Money unitPrice,
                                         Integer quantity, Money subtotal) {
        OrderItem o = new OrderItem();
        o.id = id;
        o.productId = productId;
        o.productName = productName;
        o.productImage = productImage;
        o.unitPrice = unitPrice;
        o.quantity = quantity;
        o.subtotal = subtotal;
        return o;
    }

    public Long getProductId()      { return productId; }
    public String getProductName()  { return productName; }
    public String getProductImage() { return productImage; }
    public Money getUnitPrice()     { return unitPrice; }
    public Integer getQuantity()    { return quantity; }
    public Money getSubtotal()      { return subtotal; }
}
