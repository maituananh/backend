package com.spring.backend.dto.checkout;

import com.spring.backend.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CheckoutRequest {
  @NotEmpty private List<Long> cartItemIds;

  @NotBlank private String shippingName;

  @NotBlank private String shippingPhone;

  @NotBlank private String shippingAddress;

  private String note;

  @NotNull private PaymentMethod paymentMethod;
}
