package com.spring.backend.adapter.stripe;

import com.spring.backend.adapter.stripe.dto.request.PaymentRequest;
import com.spring.backend.adapter.stripe.dto.response.PaymentResponse;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
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
}
