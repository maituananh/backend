package com.spring.backend.service;

import com.spring.backend.domain.enums.ProductStatus;
import com.spring.backend.infrastructure.entity.CartItemEntity;
import com.spring.backend.infrastructure.entity.OrderItemEntity;
import com.spring.backend.infrastructure.entity.ProductEntity;
import com.spring.backend.infrastructure.repository.ProductJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryService {

  private final ProductJpaRepository productRepository;

  /**
   * Giữ chỗ (Reserve) tồn kho khi checkout. Sử dụng PESSIMISTIC_WRITE lock để tránh race condition
   * khi nhiều user cùng checkout.
   */
  public void reserveStock(List<CartItemEntity> cartItems) {
    for (CartItemEntity item : cartItems) {
      if (item.getProduct() == null) {
        throw new RuntimeException("Product not found for cart item id=" + item.getId());
      }
      ProductEntity product =
          productRepository
              .findByIdWithLock(item.getProduct().getId())
              .orElseThrow(
                  () -> new RuntimeException("Product not found for cart item id=" + item.getId()));

      if (product.getAvailableQty() < item.getQuantity()) {
        throw new RuntimeException(
            "Insufficient stock for product '"
                + product.getName()
                + "': available="
                + product.getAvailableQty()
                + ", requested="
                + item.getQuantity());
      }

      product.setReservedQty(product.getReservedQty() + item.getQuantity());
      product.setAvailableQty(product.getAvailableQty() - item.getQuantity());
      productRepository.save(product);
      log.info(
          "Reserved {} units for product '{}', new reserved_qty={}, new available_qty={}",
          item.getQuantity(),
          product.getName(),
          product.getReservedQty(),
          product.getAvailableQty());
    }
  }

  /** Trừ tồn kho thật (Stock) sau khi thanh toán thành công. Giảm stock_qty và reserved_qty. */
  public void deductStock(List<OrderItemEntity> orderItems) {
    for (OrderItemEntity item : orderItems) {
      if (item.getProduct() == null) {
        log.warn("Product not found for order item id={}, skipping deduct", item.getId());
        continue;
      }
      ProductEntity product =
          productRepository.findByIdWithLock(item.getProduct().getId()).orElse(null);

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

  /** Hoàn tồn kho khi hủy đơn hàng. Giảm reserved_qty. */
  public void releaseStock(List<OrderItemEntity> orderItems) {
    for (OrderItemEntity item : orderItems) {
      if (item.getProduct() == null) {
        log.warn("Product not found for order item id={}, skipping release", item.getId());
        continue;
      }
      ProductEntity product =
          productRepository.findByIdWithLock(item.getProduct().getId()).orElse(null);

      if (product == null) {
        log.warn("Product not found for order item id={}, skipping release", item.getId());
        continue;
      }
      product.setReservedQty(product.getReservedQty() - item.getQuantity());
      product.setAvailableQty(product.getAvailableQty() + item.getQuantity());
      productRepository.save(product);
      log.info(
          "Released reserve for product '{}', new reserved_qty={}, new available_qty={}",
          product.getName(),
          product.getReservedQty(),
          product.getAvailableQty());
    }
  }
}
