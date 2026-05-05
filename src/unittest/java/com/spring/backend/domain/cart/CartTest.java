package com.spring.backend.domain.cart;

import static org.assertj.core.api.Assertions.*;

import com.spring.backend.domain.enums.CartItemStatus;
import com.spring.backend.domain.shared.Money;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartTest {

  @Test
  @DisplayName("getTotalAmount should sum all item totals correctly")
  void getTotalAmount_sumsAllItemTotals() {
    CartItem item1 =
        CartItem.reconstitute(1L, 10L, "Watch", Money.of(100.0), 2, CartItemStatus.PENDING);
    CartItem item2 =
        CartItem.reconstitute(2L, 20L, "Ring", Money.of(50.0), 1, CartItemStatus.PENDING);
    Cart cart = Cart.reconstitute(1L, 100L, List.of(item1, item2));

    // item1 total = 100 * 2 = 200; item2 total = 50 * 1 = 50; grand total = 250
    assertThat(cart.getTotalAmount().getAmount().compareTo(new BigDecimal("250.00"))).isEqualTo(0);
  }

  @Test
  @DisplayName("getTotalAmount should return Money.ZERO for an empty cart")
  void getTotalAmount_returnsZeroForEmptyCart() {
    Cart cart = Cart.reconstitute(1L, 100L, Collections.emptyList());

    assertThat(cart.getTotalAmount()).isEqualTo(Money.ZERO);
  }

  @Test
  @DisplayName("getCartName should join product names with comma and space")
  void getCartName_joinsProductNames() {
    CartItem item1 =
        CartItem.reconstitute(1L, 10L, "Watch", Money.of(100.0), 1, CartItemStatus.PENDING);
    CartItem item2 =
        CartItem.reconstitute(2L, 20L, "Ring", Money.of(50.0), 1, CartItemStatus.PENDING);
    Cart cart = Cart.reconstitute(1L, 100L, List.of(item1, item2));

    assertThat(cart.getCartName()).isEqualTo("Watch, Ring");
  }
}
