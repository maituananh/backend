package com.spring.backend.controller.payment;

import com.spring.backend.dto.payment.PaymentRequestDto;
import com.spring.backend.dto.payment.PaymentResponseDto;
import com.spring.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService paymentService;

  @PostMapping
  public PaymentResponseDto createPayment(@RequestBody PaymentRequestDto request) {
    return PaymentService.createPayment(request);
  }
}
