package com.spring.backend.adapter.stripe;

import com.spring.backend.adapter.stripe.dto.request.PaymentRequest;
import com.spring.backend.adapter.stripe.dto.response.PaymentResponse;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StripeAdapter {

  @Value("${stripe.secret-key}")
  private String secretKey;

  @Value("${stripe.success-url}")
  private String successUrl;

  @Value("${stripe.cancel-url}")
  private String cancelUrl;

  public StripeAdapter() {
    Stripe.apiKey = secretKey;
  }

  public PaymentResponse payment(final PaymentRequest request) {
    Stripe.apiKey = secretKey;

    final var productData =
        SessionCreateParams.LineItem.PriceData.ProductData.builder()
            .setName(request.getProductName())
            .build();

    final var priceData =
        SessionCreateParams.LineItem.PriceData.builder()
            .setCurrency(request.getCurrency())
            .setUnitAmount(request.getAmount())
            .setProductData(productData)
            .build();

    final var lineItem =
        SessionCreateParams.LineItem.builder()
            .setQuantity(request.getQuantity())
            .setPriceData(priceData)
            .build();

    final var sessionCreateParams =
        SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(successUrl)
            .setCancelUrl(cancelUrl)
            .addLineItem(lineItem)
            .build();

    Session session = null;
    try {
      session = Session.create(sessionCreateParams);

      log.info("Session created: {}", session);

      return PaymentResponse.builder()
          .status(session.getStatus())
          .sessionId(session.getId())
          .sessionUrl(session.getUrl())
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void refund(String sessionId) {
    Stripe.apiKey = secretKey;
    try {
      Session session = Session.retrieve(sessionId);
      String paymentIntentId = session.getPaymentIntent();

      if (paymentIntentId == null || paymentIntentId.isBlank()) {
        log.warn("No PaymentIntent found for session {}, cannot refund", sessionId);
        return;
      }

      RefundCreateParams params =
          RefundCreateParams.builder().setPaymentIntent(paymentIntentId).build();

      com.stripe.model.Refund.create(params);
      log.info("Refund successful for PaymentIntent: {}", paymentIntentId);
    } catch (Exception e) {
      log.error("Refund failed for session {}: {}", sessionId, e.getMessage());
      throw new RuntimeException("Stripe refund failed: " + e.getMessage());
    }
  }
}
