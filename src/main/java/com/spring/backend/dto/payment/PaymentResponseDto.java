package com.spring.backend.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class PaymentResponseDto {
    private String clientSecret;
    private Long amount;
    private String currency;
}
