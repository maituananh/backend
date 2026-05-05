package com.spring.backend.domain.product;

import java.util.Objects;

public final class ProductQuantity {

    private final int stockQty;
    private final int reservedQty;

    public ProductQuantity(int stockQty, int reservedQty) {
        if (stockQty < 0)
            throw new IllegalArgumentException("stockQty cannot be negative");
        if (reservedQty < 0)
            throw new IllegalArgumentException("reservedQty cannot be negative");
        if (reservedQty > stockQty)
            throw new IllegalArgumentException("reservedQty cannot exceed stockQty");
        this.stockQty = stockQty;
        this.reservedQty = reservedQty;
    }

    public int getStockQty()    { return stockQty; }
    public int getReservedQty() { return reservedQty; }

    /** Replaces the stored availableQty column — computed on the fly. */
    public int availableQty()   { return stockQty - reservedQty; }

    /**
     * Reserves qty units. Returns a new ProductQuantity with increased reservedQty.
     * Throws if qty exceeds availableQty.
     */
    public ProductQuantity reserve(int qty) {
        if (qty > availableQty())
            throw new IllegalArgumentException(
                "Cannot reserve " + qty + ": only " + availableQty() + " available");
        return new ProductQuantity(stockQty, reservedQty + qty);
    }

    /**
     * Releases previously reserved qty. Returns new ProductQuantity.
     * Clamps to 0 if qty exceeds reservedQty.
     */
    public ProductQuantity release(int qty) {
        return new ProductQuantity(stockQty, Math.max(0, reservedQty - qty));
    }

    /**
     * Deducts qty from stock (after payment confirmed).
     * Also reduces reservedQty proportionally.
     */
    public ProductQuantity deduct(int qty) {
        return new ProductQuantity(
            Math.max(0, stockQty - qty),
            Math.max(0, reservedQty - qty));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductQuantity)) return false;
        ProductQuantity that = (ProductQuantity) o;
        return stockQty == that.stockQty && reservedQty == that.reservedQty;
    }

    @Override
    public int hashCode() { return Objects.hash(stockQty, reservedQty); }

    @Override
    public String toString() {
        return "ProductQuantity{stock=" + stockQty + ", reserved=" + reservedQty
               + ", available=" + availableQty() + "}";
    }
}
