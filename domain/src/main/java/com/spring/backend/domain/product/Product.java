package com.spring.backend.domain.product;

import com.spring.backend.domain.enums.ProductStatus;
import com.spring.backend.domain.shared.AggregateRoot;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Product extends AggregateRoot {

    private Boolean isActived;
    private String name;
    private double price;
    private LocalDate startDate;
    private LocalDate endDate;
    private String code;
    private String description;
    private ProductQuantity quantity;
    private ProductStatus status;
    private Double dailyProfit;
    private Long categoryId;
    private Long customerId;
    private List<Image> images;

    private Product() {
        this.images = new ArrayList<>();
    }

    public static Product reconstitute(Long id, String name, double price,
                                       ProductQuantity quantity, ProductStatus status,
                                       Boolean isActived, Long categoryId, Long customerId) {
        Product p = new Product();
        p.id = id;
        p.name = name;
        p.price = price;
        p.quantity = quantity;
        p.status = status;
        p.isActived = isActived;
        p.categoryId = categoryId;
        p.customerId = customerId;
        return p;
    }

    // Business methods migrated from ProductEntity.setStockQty/setReservedQty

    public ProductQuantity reserveQty(int qty) {
        this.quantity = this.quantity.reserve(qty);
        return this.quantity;
    }

    public ProductQuantity releaseQty(int qty) {
        this.quantity = this.quantity.release(qty);
        return this.quantity;
    }

    public ProductQuantity deductQty(int qty) {
        this.quantity = this.quantity.deduct(qty);
        if (this.quantity.getStockQty() <= 0) {
            this.status = ProductStatus.SOLD_OUT;
        }
        return this.quantity;
    }

    // Getters

    public Boolean getIsActived()          { return isActived; }
    public String getName()                { return name; }
    public double getPrice()               { return price; }
    public LocalDate getStartDate()        { return startDate; }
    public LocalDate getEndDate()          { return endDate; }
    public String getCode()                { return code; }
    public String getDescription()         { return description; }
    public ProductQuantity getQuantity()   { return quantity; }
    public ProductStatus getStatus()       { return status; }
    public Double getDailyProfit()         { return dailyProfit; }
    public Long getCategoryId()            { return categoryId; }
    public Long getCustomerId()            { return customerId; }

    public List<Image> getImages() {
        return Collections.unmodifiableList(images);
    }
}
