package com.spring.backend.domain;

import com.spring.backend.domain.product.Product;

public class InventoryDomainService {

    /**
     * Reserves requestedQty units on the product.
     * Migrated from InventoryService.reserveStock (lines 26-55) — repository fetch/save removed.
     * Throws IllegalArgumentException (from ProductQuantity.reserve) if insufficient stock.
     */
    public Product reserveStock(Product product, int requestedQty) {
        if (product == null)
            throw new IllegalArgumentException("Product must not be null");
        product.reserveQty(requestedQty);
        return product;
    }

    /**
     * Deducts qty from product stock after payment success.
     * Migrated from InventoryService.deductStock (lines 58-89) — sets SOLD_OUT if stock reaches 0.
     */
    public Product deductStock(Product product, int qty) {
        if (product == null)
            throw new IllegalArgumentException("Product must not be null");
        product.deductQty(qty);
        return product;
    }

    /**
     * Releases previously reserved qty on order cancellation.
     * Migrated from InventoryService.releaseStock (lines 92-112).
     */
    public Product releaseStock(Product product, int qty) {
        if (product == null)
            throw new IllegalArgumentException("Product must not be null");
        product.releaseQty(qty);
        return product;
    }
}
