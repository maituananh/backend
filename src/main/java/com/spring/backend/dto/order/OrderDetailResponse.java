package com.spring.backend.dto.order;

import com.spring.backend.enums.OrderStatus;
import com.spring.backend.enums.PaymentMethod;
import com.spring.backend.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDetailResponse {

  private Long orderId;
  private OrderStatus orderStatus;
  private BigDecimal totalAmount;
  private String note;
  private Instant createdAt;

  // Shipping info
  private String shippingName;
  private String shippingPhone;
  private String shippingAddress;

  // Payment info
  private PaymentMethod paymentMethod;
  private PaymentStatus paymentStatus;
  private Instant paidAt;

  // Items
  private List<OrderItemDto> items;

  @Data
  @Builder
  public static class OrderItemDto {
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
  }
}
