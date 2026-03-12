package com.spring.backend.controller.order;

import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.dto.checkout.CheckoutResponse;
import com.spring.backend.dto.order.OrderDetailResponse;
import com.spring.backend.dto.order.OrderStatusResponse;
import com.spring.backend.dto.order.WebhookPayload;
import com.spring.backend.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  /** Bước 1: Tạo đơn hàng và lấy link thanh toán Stripe */
  @PostMapping("/orders/checkout")
  public ResponseEntity<CheckoutResponse> checkout(@RequestBody @Valid CheckoutRequest request) {
    return ResponseEntity.ok(orderService.checkout(request));
  }

  /** Bước 2: FE polling sau khi Stripe redirect về để biết kết quả */
  @GetMapping("/orders/{orderId}/status")
  public ResponseEntity<OrderStatusResponse> getStatus(@PathVariable Long orderId) {
    return ResponseEntity.ok(orderService.getOrderStatus(orderId));
  }

  /** Bước 3: Stripe/gateway gọi callback webhook sau khi thanh toán */
  @PostMapping("/payment/webhook")
  public ResponseEntity<Void> webhook(@RequestBody WebhookPayload payload) {
    orderService.handleWebhook(payload);
    return ResponseEntity.ok().build();
  }

  /** Lấy danh sách tất cả orders của user đang đăng nhập */
  @GetMapping("/orders")
  public ResponseEntity<List<OrderDetailResponse>> getOrders() {
    return ResponseEntity.ok(orderService.getOrders());
  }

  /** Lấy chi tiết một order theo ID */
  @GetMapping("/orders/{orderId}")
  public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
    return ResponseEntity.ok(orderService.getOrderDetail(orderId));
  }
}
