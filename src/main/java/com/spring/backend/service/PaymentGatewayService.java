package com.spring.backend.service;

import com.spring.backend.adapter.stripe.StripeAdapter;
import com.spring.backend.adapter.stripe.dto.request.PaymentRequest;
import com.spring.backend.dto.order.WebhookPayload;
import com.spring.backend.entity.OrderEntity;
import com.spring.backend.entity.OrderItemEntity;
import com.spring.backend.entity.PaymentEntity;
import com.spring.backend.enums.PaymentMethod;
import com.spring.backend.repository.PaymentRepository;
import com.stripe.net.Webhook;
import java.math.RoundingMode;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayService {

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  private final StripeAdapter stripeAdapter;
  private final PaymentRepository paymentRepository;

  /**
   * Tạo payment URL cho order. - STRIPE: tạo Stripe Checkout Session và trả session URL - CASH:
   * không cần URL, trả rỗng (thanh toán khi nhận hàng)
   */
  public String createPaymentUrl(OrderEntity order, PaymentEntity payment) {
    if (payment.getPaymentMethod() == PaymentMethod.CASH) {
      log.info("Order {} is CASH on delivery - no payment URL needed", order.getId());
      return null;
    }

    // Tên sản phẩm: ghép tên các order items
    String productName =
        order.getItems().stream()
            .map(OrderItemEntity::getProductName)
            .collect(Collectors.joining(", "));
    if (productName.isBlank()) {
      productName = "Order #" + order.getId();
    }

    // VNĐ là zero-decimal currency, KHÔNG nhân 100
    long amountInVnd = order.getTotalAmount().setScale(0, RoundingMode.HALF_UP).longValue();

    var stripeResponse =
        stripeAdapter.payment(
            PaymentRequest.builder()
                .productName(productName)
                .amount(amountInVnd)
                .currency("vnd") // đổi thành vnd
                .quantity(1L)
                .build());

    // Lưu transactionId (Stripe session ID) vào payment
    payment.setTransactionId(stripeResponse.getSessionId());
    paymentRepository.save(payment);

    log.info(
        "Created Stripe session {} for order {}", stripeResponse.getSessionId(), order.getId());
    return stripeResponse.getSessionUrl();
  }

  /**
   * Xác minh chữ ký webhook từ Stripe. Stripe gửi header "Stripe-Signature" - trong môi trường thực
   * cần verify bằng Stripe.Webhook.constructEvent(). Hiện tại cho pass qua (cần bổ sung sau).
   *
   * <p>TODO: Thêm Stripe-Signature verification khi có webhook secret.
   */
  public boolean verifySignature(String sigHeader, String payload) {
    try {
      Webhook.constructEvent(payload, sigHeader, webhookSecret);
    } catch (Exception e) {
      log.error("Invalid signature");
      return false;
    }
    return true;
  }

  public boolean verifyTransaction(WebhookPayload payload) {
    // Kiểm tra transactionId không rỗng là điều kiện tối thiểu
    if (payload.getTransactionId() == null || payload.getTransactionId().isBlank()) {
      log.warn("Webhook received with empty transactionId");
      return false;
    }
    return true;
  }

  /** Hoàn tiền cho đơn hàng qua Stripe. */
  public void refund(String sessionId) {
    if (sessionId == null || sessionId.isBlank()) {
      log.warn("No session ID provided for refund");
      return;
    }
    stripeAdapter.refund(sessionId);
  }
}
