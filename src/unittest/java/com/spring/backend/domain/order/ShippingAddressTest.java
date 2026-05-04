package com.spring.backend.domain.order;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShippingAddressTest {

  @Test
  @DisplayName("constructor should reject null name")
  void constructor_rejectsNullName() {
    assertThatThrownBy(() -> new ShippingAddress(null, "0901", "Hanoi"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  @DisplayName("constructor should reject blank name")
  void constructor_rejectsBlankName() {
    assertThatThrownBy(() -> new ShippingAddress("  ", "0901", "Hanoi"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("constructor should reject null phone")
  void constructor_rejectsNullPhone() {
    assertThatThrownBy(() -> new ShippingAddress("Nguyen", null, "Hanoi"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("phone");
  }

  @Test
  @DisplayName("constructor should reject null address")
  void constructor_rejectsNullAddress() {
    assertThatThrownBy(() -> new ShippingAddress("Nguyen", "0901", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("address");
  }

  @Test
  @DisplayName("constructor should accept valid input and return correct values from getters")
  void constructor_acceptsValidInput() {
    ShippingAddress address = new ShippingAddress("Nguyen Van A", "0901234567", "123 Le Loi, HCM");

    assertThat(address.getName()).isEqualTo("Nguyen Van A");
    assertThat(address.getPhone()).isEqualTo("0901234567");
    assertThat(address.getAddress()).isEqualTo("123 Le Loi, HCM");
  }
}
