package com.spring.backend.service;

import com.spring.backend.adapter.stripe.StripeAdapter;
import com.spring.backend.adapter.stripe.dto.request.PaymentRequest;
import com.spring.backend.dto.payment.PaymentRequestDto;
import com.spring.backend.dto.payment.PaymentResponseDto;
import com.spring.backend.entity.CartEntity;
import com.spring.backend.entity.CartItemEntity;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.CartRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final StripeAdapter stripeAdapter;
  private final UserHelper userHelper;
  private final CartRepository cartRepository;

  public PaymentResponseDto createPayment(PaymentRequestDto request) {
    long userId = userHelper.getCurrentUserId();
    CartEntity cart = cartRepository.findByCustomerId(userId).orElseThrow();
    List<CartItemEntity> cartItemEntities =
        cart.getItems().stream()
            .filter(i -> request.getProductIds().contains(i.getProduct().getId()))
            .toList();

    var result =
        stripeAdapter.payment(
            PaymentRequest.builder()
                .currency(request.getCurrency())
                .productName(cart.getCartName(cartItemEntities))
                .amount(
                    cart.getTotalAmount(cartItemEntities)
                        .multiply(java.math.BigDecimal.valueOf(100))
                        .longValue())
                .build());

    return PaymentResponseDto.builder()
        .message(result.getMessage())
        .status(result.getStatus())
        .sessionId(result.getSessionId())
        .sessionUrl(result.getSessionUrl())
        .build();
  }
}
