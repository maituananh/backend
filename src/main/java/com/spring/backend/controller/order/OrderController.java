package com.spring.backend.controller.order;

import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.dto.checkout.CheckoutResponse;
import com.spring.backend.dto.order.OrderDetailResponse;
import com.spring.backend.dto.order.OrderStatusResponse;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.enums.OrderStatus;
import com.spring.backend.exception.DuplicateWebhookEventException;
import com.spring.backend.service.OrderService;
import com.spring.backend.service.PaymentGatewayService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final PaymentGatewayService paymentGatewayService;

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
  public ResponseEntity<Void> webhook(
      @RequestHeader("Stripe-Signature") String sigHeader, @RequestBody String payload) {
    log.info("Webhook received, payload length={}", payload.length());

    // Step 1: Verify signature — D-04, D-05, D-06
    Event event;
    try {
      event = paymentGatewayService.verifyAndConstructEvent(sigHeader, payload);
    } catch (SignatureVerificationException e) {
      String partialPayload = payload.length() >= 32 ? payload.substring(0, 32) : payload;
      log.warn(
          "Webhook rejected: invalid signature. reason={}, partialPayload={}",
          e.getMessage(),
          partialPayload);
      return ResponseEntity.badRequest().build();
    }

    // Step 2: Process event — D-02, D-04
    try {
      orderService.handleWebhook(event, payload);
    } catch (DuplicateWebhookEventException e) {
      // Already processed — tell Stripe "OK, stop retrying" (D-02)
      return ResponseEntity.ok().build();
    } catch (DataIntegrityViolationException e) {
      // DB-level race: concurrent request already stored this event_id (D-04)
      log.warn("DB constraint duplicate for event {}", event.getId());
      return ResponseEntity.ok().build();
    }

    return ResponseEntity.ok().build();
  }

  /** Lấy danh sách tất cả orders của user đang đăng nhập (không paging) */
  @GetMapping("/orders/all")
  public ResponseEntity<List<OrderDetailResponse>> getOrders() {
    return ResponseEntity.ok(orderService.getOrders());
  }

  /** Lấy danh sách đơn hàng của user đang login có phân trang (mới nhất trước) */
  @GetMapping("/orders")
  public ResponseEntity<Pagination<OrderDetailResponse>> getMyOrdersPaginated(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) OrderStatus status) {
    return ResponseEntity.ok(orderService.getMyOrdersPaginated(page, size, status));
  }

  /** (ADMIN) Lấy danh sách toàn bộ đơn hàng của tất cả user có phân trang */
  @GetMapping("/admin/orders")
  public ResponseEntity<Pagination<OrderDetailResponse>> getAllOrdersPaginatedForAdmin(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) Long orderId) {
    return ResponseEntity.ok(
        orderService.getAllOrdersPaginatedForAdmin(page, size, status, orderId));
  }

  /** Lấy chi tiết một order theo ID */
  @GetMapping("/orders/{orderId}")
  public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
    return ResponseEntity.ok(orderService.getOrderDetail(orderId));
  }

  /** Hủy đơn hàng và hoàn tiền (nếu đã thanh toán qua Stripe) */
  @PostMapping("/orders/{orderId}/cancel")
  public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
    orderService.cancelOrder(orderId);
    return ResponseEntity.ok().build();
  }
}
