package com.spring.backend.service;

import com.spring.backend.entity.CartItemEntity;
import com.spring.backend.entity.OrderItemEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

  private final ProductRepository productRepository;

  /**
   * Kiểm tra tồn kho trước khi tạo order. Ném exception nếu bất kỳ sản phẩm nào không đủ số lượng
   * khả dụng (availableQty).
   */
  public void validateStock(List<CartItemEntity> cartItems) {
    for (CartItemEntity item : cartItems) {
      ProductEntity product = item.getProduct();
      if (product == null) {
        throw new RuntimeException("Product not found for cart item id=" + item.getId());
      }
      if (product.getAvailableQty() < item.getQuantity()) {
        throw new RuntimeException(
            "Insufficient stock for product '"
                + product.getName()
                + "': available="
                + product.getAvailableQty()
                + ", requested="
                + item.getQuantity());
      }
    }
  }

  /** Giữ chỗ (Reserve) tồn kho khi checkout. Tăng reserved_qty. */
  public void reserveStock(List<CartItemEntity> cartItems) {
    for (CartItemEntity item : cartItems) {
      ProductEntity product = item.getProduct();
      product.setReservedQty(product.getReservedQty() + item.getQuantity());
      productRepository.save(product);
      log.info(
          "Reserved {} units for product '{}', new reserved_qty={}",
          item.getQuantity(),
          product.getName(),
          product.getReservedQty());
    }
  }

  /** Trừ tồn kho thật (Stock) sau khi thanh toán thành công. Giảm stock_qty và reserved_qty. */
  public void deductStock(List<OrderItemEntity> orderItems) {
    for (OrderItemEntity item : orderItems) {
      ProductEntity product = item.getProduct();
      if (product == null) {
        log.warn("Product not found for order item id={}, skipping deduct", item.getId());
        continue;
      }

      product.setStockQty(product.getStockQty() - item.getQuantity());
      product.setReservedQty(product.getReservedQty() - item.getQuantity());

      // Update status if stock reaches 0
      if (product.getStockQty() <= 0) {
        product.setStatus(ProductStatus.SOLD_OUT);
        log.info(
            "Product '{}' stock reached 0, auto-updating status to SOLD_OUT", product.getName());
      }

      productRepository.save(product);
      log.info(
          "Confirmed payment: Deducted {} units from product '{}', remaining stock={}",
          item.getQuantity(),
          product.getName(),
          product.getStockQty());
    }
  }

  /** Hoàn tồn kho khi hủy đơn hàng. Giảm reserved_qty (available_qty sẽ tự tăng lại). */
  public void releaseStock(List<OrderItemEntity> orderItems) {
    for (OrderItemEntity item : orderItems) {
      ProductEntity product = item.getProduct();
      if (product == null) {
        log.warn("Product not found for order item id={}, skipping release", item.getId());
        continue;
      }
      product.setReservedQty(product.getReservedQty() - item.getQuantity());
      productRepository.save(product);
      log.info(
          "Released reserve for product '{}', new available_qty={}",
          product.getName(),
          product.getAvailableQty());
    }
  }
}
