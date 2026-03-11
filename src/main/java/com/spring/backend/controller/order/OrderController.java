package com.spring.backend.controller.order;

import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.dto.checkout.CheckoutResponse;
import com.spring.backend.dto.order.OrderStatusResponse;
import com.spring.backend.service.OrderService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping("/orders/checkout")
  public ResponseEntity<CheckoutResponse> checkout(@RequestBody @Valid CheckoutRequest request) {
    return ResponseEntity.ok(orderService.checkout(request));
  }

  @GetMapping("/orders/{orderId}/status")
  public ResponseEntity<OrderStatusResponse> getStatus(@PathVariable Long orderId) {
    return ResponseEntity.ok(orderService.getOrderStatus(orderId));
  }

  @PostMapping("/payment/webhook")
  public ResponseEntity<Void> webhook(@RequestBody WebhookPayload payload) {
    orderService.handleWebhook(payload);
    return ResponseEntity.ok().build();
  }
}
