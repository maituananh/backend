package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.cart.AddToCartRequestDto;
import com.spring.backend.dto.cart.CartResponseDto;
import com.spring.backend.entity.*;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.CartItemRepository;
import com.spring.backend.repository.CartRepository;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceUT {

  @Mock private CartRepository cartRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Mock private ProductRepository productRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserHelper userHelper;
  @Mock private S3Adapter s3Adapter;

  @InjectMocks private CartService cartService;

  @Test
  @DisplayName("addToCart should work correctly")
  void addToCart_Works() {
    AddToCartRequestDto dto = new AddToCartRequestDto();
    dto.setProductId(1L);
    dto.setQuantity(2);

    Long userId = 10L;
    UserEntity user = new UserEntity();
    user.setId(userId);
    CartEntity cart = new CartEntity();
    cart.setId(100L);
    cart.setCustomer(user); // Fix NPE
    cart.setItems(new ArrayList<>());

    ProductEntity product = new ProductEntity();
    product.setId(1L);
    product.setPrice(100.0);
    product.setIsActived(true); // Required by mapper

    when(userHelper.getCurrentUserId()).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(cartRepository.findByCustomerId(userId)).thenReturn(Optional.of(cart));
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(cartItemRepository.findByCartIdAndProductId(100L, 1L)).thenReturn(Optional.empty());

    CartResponseDto result = cartService.addToCart(dto);

    assertThat(cart.getItems()).hasSize(1);
    assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
  }

  @Test
  @DisplayName("addToCart should update existing item")
  void addToCart_UpdateExisting() {
    AddToCartRequestDto dto = new AddToCartRequestDto();
    dto.setProductId(1L);
    dto.setQuantity(2);

    Long userId = 10L;
    UserEntity user = new UserEntity();
    user.setId(userId);
    CartEntity cart = new CartEntity();
    cart.setId(100L);
    cart.setCustomer(user); // Fix NPE
    cart.setItems(new ArrayList<>());

    CartItemEntity existing = new CartItemEntity();
    existing.setQuantity(1);
    ProductEntity product = new ProductEntity();
    product.setId(1L);
    product.setPrice(100.0);
    product.setIsActived(true);
    existing.setProduct(product);
    existing.setStatus(com.spring.backend.enums.CartItemStatus.PENDING); // Required by mapper

    when(userHelper.getCurrentUserId()).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(cartRepository.findByCustomerId(userId)).thenReturn(Optional.of(cart));
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(cartItemRepository.findByCartIdAndProductId(100L, 1L)).thenReturn(Optional.of(existing));

    cartService.addToCart(dto);

    assertThat(existing.getQuantity()).isEqualTo(3);
  }

  @Test
  @DisplayName("addToCart should fail if quantity invalid")
  void addToCart_InvalidQty() {
    AddToCartRequestDto dto = new AddToCartRequestDto();
    dto.setQuantity(0);
    assertThatThrownBy(() -> cartService.addToCart(dto))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("greater than 0");
  }

  @Test
  @DisplayName("getMyCart should return dto")
  void getMyCart_Works() {
    Long userId = 10L;
    UserEntity user = new UserEntity();
    user.setId(userId);
    CartEntity cart = new CartEntity();
    cart.setCustomer(user); // Fix NPE
    cart.setItems(new ArrayList<>());
    when(userHelper.getCurrentUserId()).thenReturn(userId);
    when(cartRepository.findByCustomerId(userId)).thenReturn(Optional.of(cart));

    CartResponseDto result = cartService.getMyCart();
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("getMyCart should return null if not found")
  void getMyCart_NotFound() {
    when(userHelper.getCurrentUserId()).thenReturn(10L);
    when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.empty());

    assertThat(cartService.getMyCart()).isNull();
  }

  @Test
  @DisplayName("deleteItemOnCart should work correctly")
  void deleteItemOnCart_Works() {
    Long userId = 10L;
    CartEntity cart = new CartEntity();
    cart.setItems(new ArrayList<>());

    ProductEntity p1 = new ProductEntity();
    p1.setId(1L);
    CartItemEntity item1 = new CartItemEntity();
    item1.setProduct(p1);
    cart.getItems().add(item1);

    when(userHelper.getCurrentUserId()).thenReturn(userId);
    when(cartRepository.findByCustomerId(userId)).thenReturn(Optional.of(cart));

    cartService.deleteItemOnCart(List.of(1L));

    assertThat(cart.getItems()).isEmpty();
  }

  @Test
  @DisplayName("deleteItemOnCart should fail if items missing")
  void deleteItemOnCart_ItemNotFound() {
    CartEntity cart = new CartEntity();
    cart.setItems(new ArrayList<>());
    when(userHelper.getCurrentUserId()).thenReturn(10L);
    when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(cart));

    assertThatThrownBy(() -> cartService.deleteItemOnCart(List.of(1L)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Some items not found");
  }
}
