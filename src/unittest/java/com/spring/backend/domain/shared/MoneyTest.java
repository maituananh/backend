package com.spring.backend.domain.shared;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  @DisplayName("of should create Money from a double value")
  void of_createsMoneyFromDouble() {
    Money money = Money.of(10.5);

    assertThat(money.getAmount().compareTo(new BigDecimal("10.50"))).isEqualTo(0);
  }

  @Test
  @DisplayName("add should return new Money with the sum of both amounts")
  void add_returnsNewMoneyWithSum() {
    Money result = Money.of(3.0).add(Money.of(2.0));

    assertThat(result.getAmount().compareTo(new BigDecimal("5.00"))).isEqualTo(0);
  }

  @Test
  @DisplayName("multiply should return new Money with the product of amount and factor")
  void multiply_returnsNewMoneyWithProduct() {
    Money result = Money.of(4.0).multiply(3);

    assertThat(result.getAmount().compareTo(new BigDecimal("12.00"))).isEqualTo(0);
  }

  @Test
  @DisplayName("constructor should reject null amount")
  void constructor_rejectsNullAmount() {
    assertThatThrownBy(() -> new Money(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("constructor should reject negative amount")
  void constructor_rejectsNegativeAmount() {
    assertThatThrownBy(() -> new Money(new BigDecimal("-1")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("ZERO should act as identity element for add")
  void ZERO_isUsableAsIdentityForAdd() {
    Money result = Money.ZERO.add(Money.of(5.0));

    assertThat(result).isEqualTo(Money.of(5.0));
  }
}
