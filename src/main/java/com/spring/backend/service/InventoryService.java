package com.spring.backend.service;

import com.spring.backend.entity.CartItemEntity;
import com.spring.backend.entity.OrderItemEntity;
import com.spring.backend.entity.ProductEntity;
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
   * Kiểm tra tồn kho trước khi tạo order. Ném exception nếu bất kỳ sản phẩm nào hết hàng hoặc không
   * đủ số lượng.
   */
  public void validateStock(List<CartItemEntity> cartItems) {
    for (CartItemEntity item : cartItems) {
      ProductEntity product = item.getProduct();
      if (product == null) {
        throw new RuntimeException("Product not found for cart item id=" + item.getId());
      }
      if (product.getQuantity() < item.getQuantity()) {
        throw new RuntimeException(
            "Insufficient stock for product '"
                + product.getName()
                + "': available="
                + product.getQuantity()
                + ", requested="
                + item.getQuantity());
      }
    }
  }

  /** Trừ tồn kho sau khi thanh toán thành công. */
  public void deductStock(List<OrderItemEntity> orderItems) {
    for (OrderItemEntity item : orderItems) {
      ProductEntity product = item.getProduct();
      if (product == null) {
        log.warn("Product not found for order item id={}, skipping deduct", item.getId());
        continue;
      }
      int newQty = product.getQuantity() - item.getQuantity();
      if (newQty < 0) {
        log.error(
            "Stock went negative for product '{}' (id={}), setting to 0",
            product.getName(),
            product.getId());
        newQty = 0;
      }
      product.setQuantity(newQty);
      productRepository.save(product);
      log.info(
          "Deducted {} units from product '{}', remaining={}",
          item.getQuantity(),
          product.getName(),
          newQty);
    }
  }
}
