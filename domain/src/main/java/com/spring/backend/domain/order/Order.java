package com.spring.backend.domain.order;

import com.spring.backend.domain.enums.OrderStatus;
import com.spring.backend.domain.shared.AggregateRoot;
import com.spring.backend.domain.shared.Money;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order extends AggregateRoot {

    private Long userId;
    private OrderStatus status;
    private Money totalAmount;
    private String note;
    private ShippingAddress shippingAddress;
    private List<OrderItem> items;

    private Order() {
        this.items = new ArrayList<>();
    }

    public static Order reconstitute(Long id, Long userId, OrderStatus status,
                                     Money totalAmount, String note,
                                     ShippingAddress shippingAddress,
                                     List<OrderItem> items) {
        Order o = new Order();
        o.id = id;
        o.userId = userId;
        o.status = status;
        o.totalAmount = totalAmount;
        o.note = note;
        o.shippingAddress = shippingAddress;
        o.items = new ArrayList<>(items);
        return o;
    }

    public Long getUserId()                      { return userId; }
    public OrderStatus getStatus()               { return status; }
    public Money getTotalAmount()                { return totalAmount; }
    public String getNote()                      { return note; }
    public ShippingAddress getShippingAddress()  { return shippingAddress; }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
