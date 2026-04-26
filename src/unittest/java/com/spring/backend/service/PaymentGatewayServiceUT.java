package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceUT {

  @Mock private StripeAdapter stripeAdapter;
  @Mock private PaymentRepository paymentRepository;

  @InjectMocks private PaymentGatewayService paymentGatewayService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(paymentGatewayService, "webhookSecret", "whsec_test");
  }

  @Test
  @DisplayName("createPaymentUrl should return null for CASH")
  void createPaymentUrl_Cash() {
    PaymentEntity payment = new PaymentEntity();
    payment.setPaymentMethod(PaymentMethod.CASH);

    String url = paymentGatewayService.createPaymentUrl(new OrderEntity(), payment);
    assertThat(url).isNull();
  }

  @Test
  @DisplayName("createPaymentUrl should return Stripe URL for STRIPE")
  void createPaymentUrl_Stripe() {
    OrderEntity order = new OrderEntity();
    order.setId(100L);
    order.setTotalAmount(BigDecimal.valueOf(100000));
    order.setItems(new ArrayList<>());

    OrderItemEntity item = new OrderItemEntity();
    item.setProductName("Test Product");
    order.getItems().add(item);

    PaymentEntity payment = new PaymentEntity();
    payment.setPaymentMethod(PaymentMethod.STRIPE);

    PaymentResponse stripeResponse = new PaymentResponse();
    stripeResponse.setSessionId("sess_123");
    stripeResponse.setSessionUrl("http://stripe.url");

    when(stripeAdapter.payment(any(PaymentRequest.class))).thenReturn(stripeResponse);

    String url = paymentGatewayService.createPaymentUrl(order, payment);

    assertThat(url).isEqualTo("http://stripe.url");
    assertThat(payment.getTransactionId()).isEqualTo("sess_123");
    verify(paymentRepository).save(payment);
  }

  @Test
  @DisplayName("createPaymentUrl should handle blank product name")
  void createPaymentUrl_BlankName() {
    OrderEntity order = new OrderEntity();
    order.setId(100L);
    order.setTotalAmount(BigDecimal.valueOf(1000));
    order.setItems(new ArrayList<>());

    PaymentEntity payment = new PaymentEntity();
    payment.setPaymentMethod(PaymentMethod.STRIPE);

    PaymentResponse stripeResponse = new PaymentResponse();
    stripeResponse.setSessionId("sess_123");

    when(stripeAdapter.payment(argThat(req -> req.getProductName().equals("Order #100"))))
        .thenReturn(stripeResponse);

    paymentGatewayService.createPaymentUrl(order, payment);
    verify(stripeAdapter).payment(any());
  }

  @Test
  @DisplayName("verifyTransaction should return true for valid payload")
  void verifyTransaction_Valid() {
    WebhookPayload payload = new WebhookPayload();
    payload.setTransactionId("tx_123");
    assertThat(paymentGatewayService.verifyTransaction(payload)).isTrue();
  }

  @Test
  @DisplayName("verifyTransaction should return false for invalid payload")
  void verifyTransaction_Invalid() {
    WebhookPayload payload = new WebhookPayload();
    assertThat(paymentGatewayService.verifyTransaction(payload)).isFalse();

    payload.setTransactionId(" ");
    assertThat(paymentGatewayService.verifyTransaction(payload)).isFalse();
  }

  @Test
  @DisplayName("refund should call adapter")
  void refund_Works() {
    paymentGatewayService.refund("sess_123");
    verify(stripeAdapter).refund("sess_123");
  }

  @Test
  @DisplayName("refund should skip if sessionId is blank")
  void refund_Blank() {
    paymentGatewayService.refund(null);
    paymentGatewayService.refund("");
    verify(stripeAdapter, never()).refund(anyString());
  }
}
