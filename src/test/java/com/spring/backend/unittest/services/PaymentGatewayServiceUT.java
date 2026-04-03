package com.spring.backend.unittest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.stripe.StripeAdapter;
import com.spring.backend.adapter.stripe.dto.request.PaymentRequest;
import com.spring.backend.adapter.stripe.dto.response.PaymentResponse;
import com.spring.backend.dto.order.WebhookPayload;
import com.spring.backend.entity.OrderEntity;
import com.spring.backend.entity.OrderItemEntity;
import com.spring.backend.entity.PaymentEntity;
import com.spring.backend.enums.PaymentMethod;
import com.spring.backend.repository.PaymentRepository;
import com.spring.backend.service.PaymentGatewayService;
import com.stripe.net.Webhook;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceUT {

  @Mock private StripeAdapter stripeAdapter;
  @Mock private PaymentRepository paymentRepository;
  @InjectMocks private PaymentGatewayService paymentGatewayService;

  private OrderEntity order;
  private PaymentEntity payment;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(paymentGatewayService, "webhookSecret", "test_secret");

    order =
        OrderEntity.builder()
            .id(1L)
            .totalAmount(BigDecimal.valueOf(100000))
            .items(
                List.of(
                    OrderItemEntity.builder().productName("Product A").build(),
                    OrderItemEntity.builder().productName("Product B").build()))
            .build();

    payment = PaymentEntity.builder().id(100L).paymentMethod(PaymentMethod.STRIPE).build();
  }

  @Test
  @DisplayName("createPaymentUrl should return null for CASH")
  void createPaymentUrl_Cash_ReturnsNull() {
    // Arrange
    payment.setPaymentMethod(PaymentMethod.CASH);

    // Act
    String result = paymentGatewayService.createPaymentUrl(order, payment);

    // Assert
    assertThat(result).isNull();
    verifyNoInteractions(stripeAdapter);
  }

  @Test
  @DisplayName("createPaymentUrl should return stripe URL for STRIPE")
  void createPaymentUrl_Stripe_Success() {
    // Arrange
    PaymentResponse stripeResponse =
        PaymentResponse.builder()
            .sessionId("sess_123")
            .sessionUrl("https://stripe.com/pay")
            .build();
    when(stripeAdapter.payment(any(PaymentRequest.class))).thenReturn(stripeResponse);

    // Act
    String result = paymentGatewayService.createPaymentUrl(order, payment);

    // Assert
    assertThat(result).isEqualTo("https://stripe.com/pay");
    assertThat(payment.getTransactionId()).isEqualTo("sess_123");
    verify(paymentRepository).save(payment);
    verify(stripeAdapter)
        .payment(
            argThat(
                req ->
                    req.getProductName().equals("Product A, Product B")
                        && req.getAmount() == 100000L
                        && req.getCurrency().equals("vnd")));
  }

  @Test
  @DisplayName("verifySignature should return true when signature is valid")
  void verifySignature_Valid_ReturnsTrue() {
    try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
      mockedWebhook
          .when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
          .thenReturn(null);

      boolean result = paymentGatewayService.verifySignature("sig", "payload");

      assertThat(result).isTrue();
    }
  }

  @Test
  @DisplayName("verifySignature should return false when signature is invalid")
  void verifySignature_Invalid_ReturnsFalse() {
    try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
      mockedWebhook
          .when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
          .thenThrow(new RuntimeException("invalid"));

      boolean result = paymentGatewayService.verifySignature("sig", "payload");

      assertThat(result).isFalse();
    }
  }

  @Test
  @DisplayName("verifyTransaction should validate transactionId")
  void verifyTransaction_Tests() {
    WebhookPayload payload = new WebhookPayload();

    // Empty id
    assertThat(paymentGatewayService.verifyTransaction(payload)).isFalse();

    // Valid id
    payload.setTransactionId("tx_123");
    assertThat(paymentGatewayService.verifyTransaction(payload)).isTrue();
  }

  @Test
  @DisplayName("refund should call stripe adapter")
  void refund_Success() {
    // Act
    paymentGatewayService.refund("sess_123");

    // Assert
    verify(stripeAdapter).refund("sess_123");
  }

  @Test
  @DisplayName("refund should skip when sessionId is null")
  void refund_Null_Skips() {
    // Act
    paymentGatewayService.refund(null);
    paymentGatewayService.refund("");

    // Assert
    verifyNoInteractions(stripeAdapter);
  }
}
